package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;

import java.util.Arrays;

/**
 * Applied horizontal row-wise Gaussian convolution and stores results in transposed grid
 *
 * [REFERENCE: Eduard AbstractFrequencyOperator class (HorizontalTransposingLowPassFilter private class)]
 */
public class HorizontalTransposingLowPassFilter implements AparapiOperator {
    private final double sigmaI;
    Grid src;           // the source grid
    Grid dest;          // the result grid
    double timer;       // timer

    boolean firstPassI;     // indicator of the progress

    // compute multiple rows in cached buffers before writing to the transposed
    // intermediate and final grids to reduce number of cache misses
    private static final int NBR_CACHED_ROWS = 4;

    // numbers of filter passes
    private static final int ITERATIONS = 4;

    // maximum value of the float can be (APARAPI not support FLOAT library, thus has to copy the attribute)
    private final float FLOAT_MAX = Float.MAX_VALUE;

    /**
     * Constructor of HorizontalTransposingLowPassFilter
     * @param firstPass project indicator
     * @param sigma the sigma value (standard deviation)
     */
    public HorizontalTransposingLowPassFilter(boolean firstPass, double sigma){
        firstPassI = firstPass;
        this.sigmaI = sigma;

        if (sigma < 0) {
            throw new IllegalArgumentException("negative sigma");
        }

    }

    @Override
    public Grid operate(Grid src) {
        int r = calculateR(this.sigmaI);                // r value
        double alpha = calculateAlpha(this.sigmaI, r);  // alpha value
        float c1 = calculateC1(this.sigmaI, r, alpha);
        float c2 = calculateC2(this.sigmaI, r, alpha);

        this.src = src;
        // create transposed grid
        this.dest = new Grid(src.getRows(), src.getCols(), src.getCellSize(),src.getNorth(), src.getSouth(), src.getEast(), src.getWest());

        float[] srcBuffer = this.src.getBuffer();       // holder
        float[] destBuffer = this.dest.getBuffer();     // holder
        boolean[] firstPass = {this.firstPassI};        // APARAPI not support "non-constant value", thus use a buffer holder

        final int srcCols = src.getCols();
        final int srcRows = src.getRows();

        /*
           calculate chunk size for each processing unit, thus each unit will have their own
           cachedRows[i], srcRow[i], tmpRow[i]
         */
        int size = srcRows/NBR_CACHED_ROWS + (srcRows%NBR_CACHED_ROWS > 0 ? 1 : 0);

        // cache of the destination rows (to be written into the dest buffer)
        float[][][] cachedRows = new float[size][][];

        // initialization
        for(int i = 0 ; i < size ; i++){
            cachedRows[i] = new float[NBR_CACHED_ROWS][];
            for (int j = 0; j < NBR_CACHED_ROWS; j ++)
                cachedRows[i][j] = new float[srcCols];
        }

        float[][] srcRow = new float[size][];
        float[][] tmpRow = new float[size][];            // temporary buffer for repeatetly smoothing a row

        // initialization
        for(int i = 0 ; i < size; i++){
            srcRow[i] = new float[srcCols];
            tmpRow[i] = new float[srcCols];
        }

        boolean[] foundVoid = new boolean[size];         // each unit will have their own foundVoid flag
        Arrays.fill(foundVoid, false);              // initialization
        int[] tester = {0};                             // testing flag

        // anonymous object of APARAPI kernel
        Kernel kernel = new Kernel(){
            @Override
            public void run() {
                // for each compute unit
                int index = getGlobalId();
                int start = index * NBR_CACHED_ROWS;
                for (int i = 0 ; i < NBR_CACHED_ROWS; i++){
                    int rowID = start + i;
                    if (rowID < srcRows){
                        for (int c = 0; c < srcCols; c++){
                            srcRow[index][c] = srcBuffer[getDirectIndex(c, rowID)];
                        }
                        if (firstPass[0] && foundVoid[index]){
                            // found a void value before, assume this row also contains void values
                            copyAndReplaceVoids(srcRow[index], cachedRows[index][i], srcRow[index].length);
                            blurRow(cachedRows[index][i], tmpRow[index], cachedRows[index][i].length);
                        } else{
                            // no void value found so far, assume there will be none in this row
                            blurRow(srcRow[index], tmpRow[index], srcRow[index].length);
                            if (firstPass[0] && !isFinite(tmpRow[index][srcCols -1])){
                                // if there is any void value in this filtered row, the last value will be void
                                foundVoid[index] = true;
                                copyAndReplaceVoids(srcRow[index], cachedRows[index][i], srcRow[index].length);
                                blurRow(cachedRows[index][i], tmpRow[index], cachedRows[index][i].length);

                            }
                        }
                        // if LowPassOperator::ITERATIONS is not 4, the following
                        // lines need to be changed. If it is not an even number,
                        // the result needs to be copied to cachedRows[i]
                        blurRow(tmpRow[index], cachedRows[index][i], tmpRow[index].length);
                        blurRow(cachedRows[index][i], tmpRow[index], cachedRows[index][i].length);
                        blurRow(tmpRow[index], cachedRows[index][i], tmpRow[index].length);
                    }
                    else{
                        tester[0] ++;
                    }
                }

                // write into the result buffer (transposed matrix in 1D array)
                for (int col = 0; col < srcCols; col ++){
                    for (int i = 0 ; i < NBR_CACHED_ROWS; i ++ ){
                        if (start + i < srcRows){
                            float[] r = cachedRows[index][i];
                            destBuffer[getDirectIndex(start+i, col)]= r[col];
                        }
                    }
                }

            }


            public int getDirectIndex(int col, int row){
                return col + row * srcCols;
            }

            /**
             * Reflect index for out-of-bounds access. This is currently a
             * bottleneck and could be avoided for the center of the grid (if the
             * filter is smaller than the grid).
             *
             * @param N
             * @param n
             * @return
             */
            private int extension(int N, int n) {
                while (n < 0 || n >= N) {
                    if (n < 0) {
                        n = -1 - n;
                        /* Reflect over n = -1/2.    */
                    } else if (n >= N) {
                        n = 2 * N - 1 - n;
                        /* Reflect over n = N - 1/2. */
                    } else{
                    }
                }
                return n;
            }

            /**
             * Blurs a row once.
             *
             * @param src input
             * @param dst output
             */
            private void blurRow(float[] src, float[] dest, int N){
                double sum = 0;
                for (int n = -r; n <= r ; ++n){
                    sum += src[extension(N, n)];
                }

                sum = c1 * (src[extension(N, r + 1)]
                        + src[extension(N, -r - 1)])
                        + (c1 + c2) * sum;
                dest[0] = (float) sum;

                for (int n = 1; n < N; ++n) {
                    sum += c1 * (src[extension(N, n + r + 1)]
                            - src[extension(N, n - r - 2)])
                            + c2 * (src[extension(N, n + r)]
                            - src[extension(N, n - r - 1)]);
                    dest[n] = (float) sum;
                }
            }

            /**
             * Copy a source row to a destination row and replace void values with
             * the nearest valid neighbor in the row.
             *
             * @param src input
             * @param dst output
             */
            private void copyAndReplaceVoids(float[] src, float[] dest, int N){
                int first = 0;

                // check from the first column (toward)
                while (first < N && !isFinite(src[first]))
                    first++;

                if (first == N){
                    for (int j =0 ; j < N; j++) {
                        dest[j] = 0;
                    }
                    return;
                }

                for (int j= 0 ; j < first; j++)
                    dest[j] = src[first];

                // check from last column (backward)
                int last = N -1;
                while (last > 0&& !isFinite(src[first]))
                    last --;

                for (int j= N-1; j > last ; j--)
                    dest[j] = src[last];

                for (int j = first; j <= last; j ++){
                    float v = src[j];
                    if (!isFinite(v)){
                        boolean flagChecker = false;

                        for (int k = 1 ; k < N && !flagChecker ; k++){
                            if (j+k <= last){
                                v = src[j +k];
                                if (isFinite(v)) {
                                    dest[j] = v;
                                    flagChecker = true;
                                }
                            }

                            if (j - k >= first){
                                v = src[j - k];
                                if (isFinite((v))){
                                    dest[j] = v;
                                    flagChecker = true;
                                }
                            }
                        }
                    } else{
                        dest[j] = src[j];
                    }
                }

            }

            public boolean isFinite(float f){
                return Math.abs(f) <= FLOAT_MAX;
            }


        };
        kernel.setExplicit(true);           // explicitly manage transfers between GPU memory and CPU
        kernel.put(srcBuffer);              // send srcBuffer to GPU
        kernel.put(destBuffer);             // send destBuffer to GPU

        kernel.put(firstPass);              // send firstPass to GPU
        kernel.put(foundVoid);              // send foundVoid to GPU
        kernel.put(cachedRows);             // send cachedRows to GPU
        kernel.put(srcRow);                 // send srcRow to GPU
        kernel.put(tmpRow);                 // send tmpRow to GPU

        // eagerly execution
        kernel.execute(1);
        timer = System.nanoTime();
        kernel.execute(Range.create(size));
        timer = System.nanoTime() - timer;
        timer = timer/ 1000000;

        kernel.get(destBuffer);             // move destBuffer from GPU
        kernel.dispose();

        dest.setBufferReceived(destBuffer);
        return this.dest;
    }

    private float calculateC1(double sigma, int r, double alpha){
        return (float) (alpha / (2.0 * (alpha + r) + 1));
    }

    private float calculateC2(double sigma, int r, double alpha) {
        return (float) ((1.0 - alpha) / (2.0 * (alpha + r) + 1));
    }

    private int calculateR(double sigma){
        return (int) (0.5 * Math.sqrt((12.0 * sigma * sigma) / ITERATIONS + 1.0) - 0.5);
    }

    private double calculateAlpha(double sigma, int r){
        return (2 * r + 1) * (r * (r + 1) - 3.0 * sigma * sigma / ITERATIONS)
                / (6.0 * (sigma * sigma / ITERATIONS - (r + 1) * (r + 1)));
    }

    @Override
    public double getTimer(){
        return timer;
    }


}

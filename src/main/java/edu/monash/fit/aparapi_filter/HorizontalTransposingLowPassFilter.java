package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;
import scala.Array;

import java.util.Arrays;

public class HorizontalTransposingLowPassFilter implements AparapiOperator {
    private final double sigmaI;
    Grid src;
    Grid dest;
    boolean firstPassI;
    private static final int NBR_CACHED_ROWS = 4;

    private static final int ITERATIONS = 4;
    private final float FLOAT_MAX = Float.MAX_VALUE;
    private final float FLOAT_INFINITY = Float.POSITIVE_INFINITY;
    private final float FLOAT_NaN = Float.NaN;


    public HorizontalTransposingLowPassFilter(boolean firstPass, double sigma){
        firstPassI = firstPass;
        this.sigmaI = sigma;
    }
    @Override
    public Grid operate(Grid src, Grid dest) {
        return null;
    }

    @Override
    public Grid operate(Grid src) {
        int r = calculateR(this.sigmaI);
        double alpha = calculateAlpha(this.sigmaI, r);
        float c1 = calculateC1(this.sigmaI, r, alpha);
        float c2 = calculateC2(this.sigmaI, r, alpha);

        this.src = src;
        this.dest = new Grid(src.getRows(), src.getCols(), src.getCellSize(),src.getNorth(), src.getSouth(), src.getEast(), src.getWest());

        float[] srcBuffer = this.src.getBuffer();
        float[] destBuffer = this.dest.getBuffer();
        boolean[] firstPass = {this.firstPassI};

        final int srcCols = src.getCols();
        final int srcRows = src.getRows();
        int size = srcRows/NBR_CACHED_ROWS + (srcRows%NBR_CACHED_ROWS > 0 ? 1 : 0);


        float[][][] cachedRows = new float[size][][];

        for(int i = 0 ; i < size ; i++){
            cachedRows[i] = new float[NBR_CACHED_ROWS][];
            for (int j = 0; j < NBR_CACHED_ROWS; j ++)
                cachedRows[i][j] = new float[srcCols];
        }
        float[][] srcRow = new float[size][];
        float[][] tmpRow = new float[size][];

        for(int i = 0 ; i < size; i++){
            srcRow[i] = new float[srcCols];
            tmpRow[i] = new float[srcCols];
        }

        boolean[] foundVoid = new boolean[size];
        Arrays.fill(foundVoid, false);
        int[] tester = {0};

        Kernel kernel = new Kernel(){
            @Override
            public void run() {
                int index = getGlobalId();
                int start = index * NBR_CACHED_ROWS;
//                for (int row=start; row< start+ NBR_CACHED_ROWS && row < srcRows; row++){}
                for (int i = 0 ; i < NBR_CACHED_ROWS; i++){
                    int rowID = start + i;
                    if (rowID < srcRows){
                        for (int c = 0; c < srcCols; c++){
                            srcRow[index][c] = srcBuffer[getDirectIndex(c, rowID)];
                        }
                        if (firstPass[0] && foundVoid[index]){
                            copyAndReplaceVoids(srcRow[index], cachedRows[index][i], srcRow[index].length);
                            blurRow(cachedRows[index][i], tmpRow[index], cachedRows[index][i].length);
                        } else{
                            blurRow(srcRow[index], tmpRow[index], srcRow[index].length);
                            if (firstPass[0] && !isFinite(tmpRow[index][srcCols -1])){
                                foundVoid[index] = true;
                                copyAndReplaceVoids(srcRow[index], cachedRows[index][i], srcRow[index].length);
                                blurRow(cachedRows[index][i], tmpRow[index], cachedRows[index][i].length);

                            }
                        }
                        blurRow(tmpRow[index], cachedRows[index][i], tmpRow[index].length);
                        blurRow(cachedRows[index][i], tmpRow[index], cachedRows[index][i].length);
                        blurRow(tmpRow[index], cachedRows[index][i], tmpRow[index].length);
                    }
                    else{
                        tester[0] ++;
                    }
                }

                for (int col = 0; col < srcCols; col ++){
                    for (int i = 0 ; i < NBR_CACHED_ROWS; i ++ ){
                        if (start + i < srcRows){
                            float[] r = cachedRows[index][i];
                            destBuffer[getDirectIndex(start+i, col)]= r[col];
                        }
                    }
                }

            }

            public int getRow(int directIndex){
                return directIndex / srcCols;
            }

            public int getCol(int directIndex){
                return directIndex % srcCols;
            }

            public int getDirectIndex(int col, int row){
                return col + row * srcCols;
            }

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

            private void copyAndReplaceVoids(float[] src, float[] dest, int N){
                int first = 0;
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
        kernel.execute(1);
//        kernel.execute(Range.create(srcRows/NBR_CACHED_ROWS));
        kernel.execute(Range.create(size));
        kernel.dispose();

//        System.out.println(srcRows + "  " +NBR_CACHED_ROWS + "::" +srcRows/NBR_CACHED_ROWS + "-->"  );
////        for (float i : destBuffer)
////            System.out.println(">> " + i);
//
//        for (int i = 0 ; i < size; i++){
//            for (int j = 0 ; j <srcCols; j ++){
//                System.out.println( "i " + i + " j " + j +"-->"  + srcRow[i][j]);
//            }
//        }

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

}

package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.Arrays;

public class DemoLowPassOperator implements AparapiOperator {
    Grid src;
    Grid dest;
    double sigmaI;
    private static final int ITERATIONS = 4;
    private final float FLOAT_MAX = Float.MAX_VALUE;
    private final float FLOAT_INFINITY = Float.POSITIVE_INFINITY;
    private final float FLOAT_NaN = Float.NaN;

    public DemoLowPassOperator(double sigma){
        this.sigmaI = sigma;
    }

    @Override
    public Grid operate(Grid src, Grid dest) {
        return null;
    }


    public Grid operate(Grid src) {
        this.src = src;
        this.dest = Grid.shallowCopy(src);
        double sigma = sigmaI;
        if (sigma < 0) {
            throw new IllegalArgumentException("negative sigma");
        }
        int srcCols = src.getCols();
        int srcRows = src.getRows();
        double srcNorth = src.getNorth();
        double srcCellSizes = src.getCellSize();

        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = this.dest.getBuffer();
        float[] tempBuffer = new float[srcCols * srcRows];

        int r = calculateR(sigma);
        double alpha = calculateAlpha(sigma, r);
        float c1 = calculateC1(sigma, r, alpha);
        float c2 = calculateC2(sigma, r, alpha);

        final boolean firstPass[] = new boolean[]{true};

        Kernel horizontalTransposingLowPassFilter = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                boolean foundVoid = false;

                if (firstPass[0] && foundVoid){
                    copyAndReplaceVoids(i);
                    blurRow(i, destBuffer, tempBuffer, srcCols);
                }
                else{
                    blurRow(i, srcBuffer, tempBuffer, srcCols);
                    if (firstPass[0] && !isFinite(tempBuffer[getDirectIndex(srcCols-1, i)])){
                        foundVoid = true;
                        copyAndReplaceVoids(i);
                        blurRow(i, destBuffer, tempBuffer, srcCols);
                    }

                }
                blurRow(i, tempBuffer, destBuffer, srcCols);
                blurRow(i, destBuffer, tempBuffer, srcCols);
                blurRow(i, tempBuffer, destBuffer, srcCols);
            }

            private void copyAndReplaceVoids(int i) {
                int N = srcCols;

                // Often void values are placed along borders of grids. Therefore
                // start searching from both ends of the row.
                // Test whether start of row contains void values
                int first = 0;
                while (first < N && !isFinite(srcBuffer[getDirectIndex(first, i)])) {
                    first++;
                }

                // test whether end of row contains void values
                int last = N - 1;
                while (last > 0 && !isFinite(srcBuffer[getDirectIndex(first, i)])) {
                    last--;
                }


                if (first == N) {
                    // entire row is void
                    for (int j =0 ; j < srcCols; j++){
                        destBuffer[getDirectIndex(j, i)] = 0;
                    }
                    return;
                }

                // fill first void cells of row with first valid value
                for (int j = 0; j < first; j++) {
                    destBuffer[getDirectIndex(j, i)] = srcBuffer[getDirectIndex(first, i)];
                }

                // fill void cells at end of row with last valid value
                for (int j = N - 1; j > last; j--) {
                    destBuffer[getDirectIndex(j, i)] = srcBuffer[getDirectIndex(last, i)];
                }

                for (int j = first; j <= last; j++) {
                    float v = srcBuffer[getDirectIndex(j, i)];
                    if (!isFinite(v)){
                        int k = 1;
                        boolean flagChecker = false;
                        while (k <N && !flagChecker){
                            // scan forwards
                            if (j + k <= last && !flagChecker){
                                v = srcBuffer[getDirectIndex(j+k,i)];
                                if (isFinite(v)){
                                    destBuffer[i] = v;
                                    flagChecker = true;
                                }
                            }

                            if (j - k >= first && !flagChecker) {
                                v = srcBuffer[getDirectIndex(j - k, i)];
                                if (isFinite(v)) {
                                    destBuffer[i] = v;
                                    flagChecker = true;
                                }
                            }
                            k ++;
                        }

                    }
                    else{
                        destBuffer[getDirectIndex(j,i)]= srcBuffer[getDirectIndex(j,i)];
                    }
                }
            }

            private void blurRow(int i, float[] src, float[] dest, int srcCols){
                int N = srcCols;
                double sum = 0;
                for (int n = -r; n <= r; ++n){
                    sum += src[getDirectIndex(extension(N, n), i)];
                }
                sum = c1 * (src[getDirectIndex(extension(N, r+1), i)]
                        + src[getDirectIndex(extension(N, -r-1), i)]
                        + (c1 + c2) * sum);

                dest[getDirectIndex(0, i)] = (float) sum;

                for (int n = 1; n < N; ++n){
                    sum += c1 *(src[getDirectIndex(extension(N, n+r+1), i)]
                            - src[getDirectIndex(extension(N, n-r-2), i)]
                            + c2 * src[getDirectIndex(extension(N, n+r), i)]
                            - src[getDirectIndex(extension(N, n-r-1), i)]
                    );
                    dest[getDirectIndex(n,i)]= (float) sum;
                }
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
                boolean flagChecker = false;
                while (!flagChecker) {
                    if (n < 0) {
                        n = -1 - n;
                        /* Reflect over n = -1/2.    */
                    } else if (n >= N) {
                        n = 2 * N - 1 - n;
                        /* Reflect over n = N - 1/2. */
                    } else{
                        flagChecker = true;
                    }
                }
                return n;
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

            public boolean isFinite(float f){
                return Math.abs(f) <= FLOAT_MAX;
            }

        };
        horizontalTransposingLowPassFilter.setExplicit(true);
        horizontalTransposingLowPassFilter.put(srcBuffer);
        horizontalTransposingLowPassFilter.put(destBuffer);
        horizontalTransposingLowPassFilter.put(tempBuffer);
        horizontalTransposingLowPassFilter.put(firstPass);
        horizontalTransposingLowPassFilter.execute(1);
        horizontalTransposingLowPassFilter.execute(Range.create(srcRows));
        horizontalTransposingLowPassFilter.get(destBuffer);
        horizontalTransposingLowPassFilter.dispose();

        firstPass[0] = false;
//        tempBuffer = new float[srcCols * srcRows];
        horizontalTransposingLowPassFilter.setExplicit(true);
        horizontalTransposingLowPassFilter.put(srcBuffer);
        horizontalTransposingLowPassFilter.put(destBuffer);
        horizontalTransposingLowPassFilter.put(tempBuffer);
        horizontalTransposingLowPassFilter.put(firstPass);
        horizontalTransposingLowPassFilter.execute(1);
        horizontalTransposingLowPassFilter.execute(Range.create(srcRows));
        horizontalTransposingLowPassFilter.get(destBuffer);
        horizontalTransposingLowPassFilter.dispose();

        Kernel copyVoidOperator = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                if (!isFinite(srcBuffer[i])){
                    destBuffer[i] = FLOAT_NaN;
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

            public boolean isFinite(float f){
                return Math.abs(f) <= FLOAT_MAX;
            }

        };
        copyVoidOperator.setExplicit(true);
        copyVoidOperator.put(srcBuffer);
        copyVoidOperator.put(destBuffer);
        copyVoidOperator.execute(1);
        copyVoidOperator.execute(Range.create(srcCols * srcRows));
        copyVoidOperator.get(destBuffer);
        copyVoidOperator.dispose();

        dest.setBufferReceived(destBuffer);
        return dest;
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

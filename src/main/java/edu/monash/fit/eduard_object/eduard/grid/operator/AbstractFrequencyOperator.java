package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;
import java.util.Arrays;

/**
 * Abstract base class for low-pass and high-pass filters. Results are close to
 * Gaussian blur but faster. An extended box filter with a moving sum and and an
 * intermediate transposed grid are used to to accelerate computations.
 *
 * For an introduction to the topic:
 * https://fgiesen.wordpress.com/2012/07/30/fast-blurs-1/
 * https://fgiesen.wordpress.com/2012/08/01/fast-blurs-2/
 *
 * Paper comparing different blur algorithms with reference code:
 *
 * Getreuer 2013 A Survey of Gaussian Convolution Algorithms
 * http://dev.ipol.im/~getreuer/code/doc/gaussian_20131215_doc/index.html
 *
 * Original paper introducing extended box filters:
 *
 * P. Gwosdek, S. Grewenig, A. Bruhn, J. Weickert, “Theoretical foundations of
 * Gaussian convolution by extended box filtering,” International Conference on
 * Scale Space and Variational Methods in Computer Vision, pp. 447–458, 2011.
 * http://dx.doi.org/10.1007/ 978-3-642-24785-9_38
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public abstract class AbstractFrequencyOperator extends GridOperator {

    // numbers of filtering passes. Not a parameter. If this is changed, the 
    // algorithm must be changed.
    private static final int ITERATIONS = 4;

    /**
     * compute multiple rows in cached buffers before writing to the transposed
     * intermediate and final grids to reduce number of cache misses. 1 is the
     * minimum value, 2 results in clear acceleration, 3 is somewhat faster.
     * There is no reason to go beyond 4.
     */
    private static final int NBR_CACHED_ROWS = 4;

    /**
     * Returns the sigma of the extended box filter. The radius is smaller than
     * sigma with four iterations.
     *
     * From Getreuer 2013 A Survey of Gaussian Convolution Algorithms, eq. 34.
     *
     * @param r radius
     * @return sigma
     */
    public static double sigmaForRadius(double r) {
        return Math.sqrt((r * r + r) / 3 * ITERATIONS);
    }

    /**
     * Returns the radius for sigma.
     *
     * From Getreuer 2013 A Survey of Gaussian Convolution Algorithms, eq. 34.
     *
     * @param s sigma
     * @return radius
     */
    public static double radiusForSigma(double s) {
        return 0.5 * Math.sqrt(12 * s * s / ITERATIONS + 1) - 0.5;
    }

    /**
     * standard deviation
     */
    private final double sigma;

    /**
     * radius of kernel
     */
    private final int r;

    /**
     * precomputed scale factors
     */
    private final double c1, c2;

    /**
     * Constructor
     *
     * @param sigma standard deviation
     * @param progressIndicator progress indicator
     */
    protected AbstractFrequencyOperator(double sigma, ProgressIndicator progressIndicator) {
        super(progressIndicator);

        if (sigma < 0) {
            throw new IllegalArgumentException("negative sigma");
        }

        this.sigma = sigma;
        r = (int) (0.5 * Math.sqrt((12.0 * sigma * sigma) / ITERATIONS + 1.0) - 0.5);
        double alpha = (2 * r + 1) * (r * (r + 1) - 3.0 * sigma * sigma / ITERATIONS)
                / (6.0 * (sigma * sigma / ITERATIONS - (r + 1) * (r + 1)));
        c1 = (float) (alpha / (2.0 * (alpha + r) + 1));
        c2 = (float) ((1.0 - alpha) / (2.0 * (alpha + r) + 1));
    }

    @Override
    public Grid operate(Grid src, Grid dst) {
        logStart();
        try {
            Grid transposedGrid = new HorizontalTransposingLowPassFilter(
                    progressIndicator, true).operate(src);
            if (isCancelled()) {
                return null;
            }

            return new HorizontalTransposingLowPassFilter(progressIndicator, false)
                    .operate(transposedGrid, dst);
        } finally {
            logEnd();
        }
    }

    @Override
    public String toString() {
        return getName() + ": sigma=" + sigma;
    }

    /**
     * Applies horizontal row-wise Gaussian convolution and stores results in a
     * transposed grid.
     */
    private class HorizontalTransposingLowPassFilter extends ThreadedGridOperator {

        /**
         * flag for progress indication
         */
        private final boolean firstPass;

        public HorizontalTransposingLowPassFilter(ProgressIndicator progressIndicator,
                boolean firstPass) {
            super(progressIndicator);
            this.firstPass = firstPass;
        }

        /**
         * Create a transposed grid
         *
         * @param src
         * @return
         */
        @Override
        protected Grid initDestinationGrid(Grid src) {
            final int nrows = src.getRows();
            final int ncols = src.getCols();
            return new Grid(nrows, ncols, src.getCellSize(), src.getWest(),
                    src.getSouth(), src.getPrjFileContent());
        }

        @Override
        protected void threadedOperate(Grid src, Grid dst, int startRow, int endRow) {

            // indicates whether a void value has been found in the grid.
            boolean foundVoid = false;

            final int ncols = src.getCols();
            final int progressOffset = firstPass ? 0 : (endRow - startRow - 1) / 2;

            // cache a few lines such that multiple lines can be written together
            // when transposing the grid. This reduces the number of cache misses.
            float[][] cachedRows = new float[NBR_CACHED_ROWS][];
            for (int i = 0; i < NBR_CACHED_ROWS; i++) {
                cachedRows[i] = new float[ncols];
            }

            float[] srcRow = new float[ncols];

            // temporary buffer for repeatetly smoothing a row
            float[] tmpRow = new float[ncols];

            for (int row = startRow; row < endRow; row += NBR_CACHED_ROWS) {
                if (reportProgress(startRow, endRow, row / 2 + progressOffset) == false) {
                    return;
                }

                for (int i = 0; i < NBR_CACHED_ROWS; i++) {
                    int rowID = row + i;
                    if (rowID < endRow) {
                        for (int c = 0; c < ncols; c++) {
                            srcRow[c] = src.getValue(c, rowID);
                        }
                        float[] dstRow = cachedRows[i];

                        if (firstPass && foundVoid) {
                            // found a void value before, assume this row also contains void values
                            copyAndReplaceVoids(srcRow, dstRow);
                            blurRow(dstRow, tmpRow);
                        } else {
                            // no void value found so far, assume there will be none in this row
                            blurRow(srcRow, tmpRow);
                            if (firstPass && !Grid.isValid(tmpRow[ncols - 1])) {
                                // if there is any void value in this filtered row, the last value will be void
                                foundVoid = true;
                                copyAndReplaceVoids(srcRow, dstRow);
                                blurRow(dstRow, tmpRow);
                            }
                        }

                        // if LowPassOperator::ITERATIONS is not 4, the following 
                        // lines need to be changed. If it is not an even number,
                        // the result needs to be copied to cachedRows[i]
                        blurRow(tmpRow, dstRow);
                        blurRow(dstRow, tmpRow);
                        blurRow(tmpRow, dstRow);
                    }
                }

                // write to transposed destination grid
                // write multiple columns to reduce number of cache misses
                for (int col = 0; col < ncols; col++) {
                    for (int i = 0; i < NBR_CACHED_ROWS; i++) {
                        if (row + i < endRow) {
                            float[] r = cachedRows[i];
                            dst.setValue(r[col], row + i, col); // transposed destination
                        }
                    }
                }
            }
        }

        /**
         * Blurs a row once.
         *
         * @param src input
         * @param dst output
         */
        private void blurRow(float[] src, float[] dst) {
            int N = src.length;
            double sum = 0;
            for (int n = -r; n <= r; ++n) {
                sum += src[extension(N, n)];
            }

            sum = c1 * (src[extension(N, r + 1)]
                    + src[extension(N, -r - 1)])
                    + (c1 + c2) * sum;
            dst[0] = (float) sum;

            for (int n = 1; n < N; ++n) {
                sum += c1 * (src[extension(N, n + r + 1)]
                        - src[extension(N, n - r - 2)])
                        + c2 * (src[extension(N, n + r)]
                        - src[extension(N, n - r - 1)]);
                dst[n] = (float) sum;
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
            while (true) {
                if (n < 0) {
                    n = -1 - n;
                    /* Reflect over n = -1/2.    */
                } else if (n >= N) {
                    n = 2 * N - 1 - n;
                    /* Reflect over n = N - 1/2. */
                } else {
                    break;
                }
            }
            return n;
        }

        /**
         * Copy a source row to a destination row and replace void values with
         * the nearest valid neighbor in the row.
         *
         * @param src input
         * @param dst output
         */
        private void copyAndReplaceVoids(float[] src, float[] dst) {
            int N = src.length;

            // Often void values are placed along borders of grids. Therefore
            // start searching from both ends of the row.
            // Test whether start of row contains void values
            int first = 0;
            while (first < N && !Grid.isValid(src[first])) {
                first++;
            }

            if (first == N) {
                // entire row is void
                Arrays.fill(dst, 0);
                return;
            }

            // fill first void cells of row with first valid value
            for (int i = 0; i < first; i++) {
                dst[i] = src[first];
            }

            // test whether end of row contains void values
            int last = N - 1;
            while (last > 0 && !Grid.isValid(src[last])) {
                last--;
            }

            // fill void cells at end of row with last valid value
            for (int i = N - 1; i > last; i--) {
                dst[i] = src[last];
            }

            // cells betwen first and last
            for (int i = first; i <= last; i++) {
                float v = src[i];
                if (!Grid.isValid(v)) {
                    // search closest non-void value in src
                    for (int j = 1; j < N; j++) {
                        // scan forwards
                        if (i + j <= last) {
                            v = src[i + j];
                            if (Grid.isValid(v)) {
                                dst[i] = v;
                                break;
                            }
                        }

                        // scan backwards
                        if (i - j >= first) {
                            v = src[i - j];
                            if (Grid.isValid(v)) {
                                dst[i] = v;
                                break;
                            }
                        }
                    }

                } else {
                    dst[i] = src[i];
                }
            }
        }

        @Override
        public String getName() {
            return "Horizontal transposing 1D low-pass filter";
        }
    }

}

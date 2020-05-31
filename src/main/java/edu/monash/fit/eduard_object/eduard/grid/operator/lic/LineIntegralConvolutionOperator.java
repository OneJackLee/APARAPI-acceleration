package edu.monash.fit.eduard_object.eduard.grid.operator.lic;

import edu.monash.fit.eduard_object.eduard.utils.MathUtils;
import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.grid.MinMax;
import edu.monash.fit.eduard_object.eduard.grid.operator.MinMaxOperator;

/**
 * Terrain generalization with line integral convolution with uniform weights or
 * Gaussian weights, and uniform sampling along a line of maximum slope.
 *
 * Applying line integral convolution for terrain simplification was introduced
 * by Roman Geisth�vel:
 *
 * Roman Geisth�vel & Lorenz Hurni (2018) Automated Swiss-Style Relief Shading
 * and Rock Hachuring, The Cartographic Journal, 55:4, 341-361, DOI:
 * 10.1080/00087041.2018.1551955
 *
 * Line Integral convolution was introduced by Cabral and Leedom:
 *
 * Cabral, B. and Leedom, L.C. (1993) �Imaging vector fields using line integral
 * convolution� Proceedings of the 20th Annual Conference on Computer Graphics
 * and Interactive Techniques: ACM, pp.263�270 DOI:10.1145/166117.166151.
 *
 * @author Bernie Jenny, Monash University
 */
public final class LineIntegralConvolutionOperator extends LicOperator {

    /**
     * If true, a box filter with uniform weights is used instead of Gaussian
     * filter.
     *
     * The kernel size of a box filter is about 60% of Gaussian filter, so the
     * filter operation is up to 40% faster with a box filter.
     *
     * However, box filter can produce artifacts along the top part of sharp
     * ridges, and create distinctive banding effect along ridges, which are
     * visible when shading.
     */
    private final boolean boxFilter = false;

    /**
     * Half length of the integration line for the box filter. The total length
     * of the integration line is 2 * halfBoxFilterLineLength. If Gaussian
     * weights are used, the filter size is larger.
     */
    private final float halfBoxFilterLineLength;

    /**
     * Half of a Gaussian bell curve for positive values. The size of the curve
     * is adjusted such that it fits half of the integration line defined by
     * halfLineLength.
     */
    private final double[] gaussianWeights;

    private final double[] gaussianWeightsSummed;

    /**
     * Weight between 0 and 1 to sharpen ridges
     */
    private final float sharpening;

    /**
     * Sharpen ridges above this relative value. Value between 0 and 1 relative
     * to the minimum and the maximum grid values.
     */
    private final float sharpeningLimit;

    /**
     * Minimum and maximum in unfiltered source grid. Initialized by the
     * single-threaded operate() method and then read by all threads in the
     * multi-threaded operateCell() method.
     */
    private MinMax minMax;

    /**
     * Constructor
     *
     * @param halfLineLength half length of the integration line in cells
     * @param sharpening Weight between 0 and 1 to sharpen ridges
     * @param sharpeningLimit Sharpen ridges above this relative elevation
     * value. Value between 0 and 1 relative to the minimum and the maximum grid
     * values.
     */
    public LineIntegralConvolutionOperator(
            float halfLineLength,
            float sharpening,
            float sharpeningLimit) {
        if (sharpening < -1 || sharpening > 1) {
            throw new IllegalArgumentException();
        }
        this.sharpening = sharpening;
        this.sharpeningLimit = sharpeningLimit;
        this.halfBoxFilterLineLength = halfLineLength;
        double std = gaussianStdForBoxFilter(halfLineLength);
        gaussianWeights = normalizedGaussianBellForStd(std);

        gaussianWeightsSummed = new double[gaussianWeights.length];
        double sum = 0;
        for (int i = gaussianWeights.length - 1; i >= 0; i--) {
            sum += gaussianWeights[i];
            gaussianWeightsSummed[i] = sum;
        }
    }

    @Override
    public Grid operate(Grid src, Grid dst) {

        // instead of calling src.getMinMax(), use operator with progress indicator
        minMax = new MinMaxOperator(progressIndicator).findMinMax(src);
        if (isCancelled()) {
            return null;
        }

        return super.operate(src, dst);
    }

    @Override
    protected void operateRow(Grid src, Grid dst, int row) {
        SlopeLineIterator iter = new SlopeLineIterator(src);
        for (int col = 0, nCols = src.getCols(); col < nCols; col++) {
            operateValue(src, dst, col, row, iter);
        }
    }

    protected void operateValue(Grid src, Grid dst, int col, int row, SlopeLineIterator iter) {
        float filtered = lic(iter, src, row, col);
        dst.setValue(filtered, col, row);
    }

    /**
     * Scaling factor for uphill Gaussian weight for sharpening along ridge
     * lines.
     *
     * @param relativeVal relative grid value between 0 and 1
     * @return scale factor between 0 and 1
     */
    private double uphillWeightScale(double relativeVal) {
        final double TOL = 0.1f;
        if (sharpening > 0) {
            double smoothstep = MathUtils.smoothstep(
                    sharpeningLimit - TOL,
                    sharpeningLimit + TOL,
                    relativeVal);
            return 1 - smoothstep * sharpening;
        }
        return 1;
    }

    /**
     * Line integral convolution with uniform sampling distance and Gaussian or
     * uniform weights along a line of steepest slope in uphill and downhill
     * direction.
     *
     * @param grid scalar field
     * @param row start position row
     * @param col start position column
     * @return
     */
    private float lic(SlopeLineIterator iter, Grid src, int row, int col) {
        final float centralVal = src.getValue(col, row);
        if (!Grid.isValid(centralVal)) {
            return Float.NaN;
        }

        final int halfLineLengthInt;
        if (boxFilter) {
            halfLineLengthInt = (int) Math.ceil(halfBoxFilterLineLength);
        } else {
            halfLineLengthInt = (int) Math.ceil(halfGaussianFilterLineLength(halfBoxFilterLineLength));
        }

        double centralW = boxFilter ? 1 : gaussianWeights[0];
        double totalH = centralW * centralVal;

        // sum of all weights. Initialize with weight for center cell and all 
        // downhill weights. Uphill weight vary with elevation, but downhill 
        // weights are always identical
        double totalW = gaussianWeightsSummed[0];

        // uphill
        iter.reset(col, row);
        float prevVal = centralVal;
        final double relativeVal = (centralVal - minMax.min) / minMax.range;
        final double uphillWeightScale = uphillWeightScale(relativeVal);
        for (int i = 1; i <= halfLineLengthInt; i++) {
            boolean insideGrid = iter.nextUphill();
            float z = insideGrid ? iter.getZ() : zOutside(iter, true);
            boolean validZ = Grid.isValid(z);
            if (!validZ) {
                // reached void value: use previous value
                z = prevVal;
            }
            if (!insideGrid || !validZ) {
                // use border value or last valid value for rest of slope line
                double w = (boxFilter ? halfLineLengthInt - i : gaussianWeightsSummed[i]) * uphillWeightScale;
                totalW += w; // weight varies with elevation, so sum uphill weights
                totalH += w * z;
                break;
            } else {
                double w = (boxFilter ? 1 : gaussianWeights[i]) * uphillWeightScale;
                totalW += w;
                totalH += w * z;
                prevVal = z;
            }
        }

        // downhill
        iter.reset(col, row);
        prevVal = centralVal;
        for (int i = 1; i <= halfLineLengthInt; i++) {
            boolean insideGrid = iter.nextDownhill();
            float z = insideGrid ? iter.getZ() : zOutside(iter, false);
            boolean validZ = Grid.isValid(z);
            if (!validZ) {
                // reached void value: use previous value
                z = prevVal;
            }
            if (!insideGrid || !validZ) {
                // use border value or last valid value for rest of slope line
                double w = (boxFilter ? halfLineLengthInt - i : gaussianWeightsSummed[i]);
                totalH += w * z;
                break;
            } else {
                double w = boxFilter ? 1 : gaussianWeights[i];
                totalH += w * z;
                prevVal = z;
            }
        }

        return (float) (totalH / totalW);
    }

    /**
     * Mirrored value outside of grid.
     *
     * @param iter
     * @param uphill
     * @return
     */
    private float zOutside(SlopeLineIterator iter, boolean uphill) {
        // value at curent position
        float closeToBorder = iter.getZ();

        // value at position in opposite direction
        if (uphill) {
            iter.nextDownhill();
        } else {
            iter.nextUphill();
        }
        float zInside = iter.getZ();

        // extrapolated value
        float zOutside = 2 * closeToBorder - zInside;
        return zOutside;
    }

    @Override
    public String getName() {
        return "Line integral convolution with Gaussian weights";
    }

    @Override
    public String toString() {
        return getName()
                + ": box filter=" + boxFilter
                + ", half box filter line length=" + halfBoxFilterLineLength
                + ", sharpening=" + sharpening
                + ", sharpening limit=" + sharpeningLimit;
    }
}

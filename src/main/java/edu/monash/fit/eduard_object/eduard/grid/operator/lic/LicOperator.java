package edu.monash.fit.eduard_object.eduard.grid.operator.lic;

import edu.monash.fit.eduard_object.eduard.grid.operator.ThreadedGridOperator;

/**
 * FIXME this class only adds static members. Move them to a utilities class an
 * remove this abstract class.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public abstract class LicOperator extends ThreadedGridOperator {

    private static final float SQRT2PI = (float) Math.sqrt(2 * Math.PI);

    /**
     * Compute the kernel size of a Gaussian filter that corresponds to a box
     * filter with a given size. A Gaussian filter is slower, because the kernel
     * size of the filter is larger.
     *
     * The kernel size of a box filter is about 60% of Gaussian filter.
     *
     * @param halfBoxFilterLineLength half of the box kernel size
     * @return half of the Gaussian kernel filter size. Full line length: 2 *
     * halfSize, and the number of samples along this line is 1 + 2 * halfSize.
     */
    protected static float halfGaussianFilterLineLength(float halfBoxFilterLineLength) {
        float std = gaussianStdForBoxFilter(halfBoxFilterLineLength);
        float kernelSize = 6 * std;
        return kernelSize / 2;
    }

    /**
     * Compute the standard deviation of a Gaussian filter that corresponds to a
     * box filter with a given size.
     *
     * The standard deviation std of the Gaussian filter and the size w of the
     * box filter are related: std = sqrt((w * w - 1) / 12).
     *
     * See p. 237 in W. M. Wells, "Efficient Synthesis of Gaussian Filters by
     * Cascaded Uniform Filters," in IEEE Transactions on Pattern Analysis and
     * Machine Intelligence, vol. PAMI-8, no. 2, pp. 234-239, March 1986. doi:
     * 10.1109/TPAMI.1986.4767776
     *
     * or see Kovesi: Fast Almost-Gaussian Filtering
     *
     * @param halfBoxFilterLineLength half of the box kernel size
     * @return standard deviation of the Gaussian curve corresponding to the box
     * filter.
     */
    protected static float gaussianStdForBoxFilter(float halfBoxFilterLineLength) {
        float w = 2 * halfBoxFilterLineLength;
        return (float) Math.sqrt((w * w - 1) / 12);
    }

    /**
     * Evaluates the Gaussian function with a given standard deviation.
     *
     * @param x evaluate at distance x from 0.
     * @param sigma standard deviation determining the size of the Gaussian bell
     * curve
     * @return the vertical distance at position x.
     */
    protected static float gaussian(float x, float sigma) {
        return (float) Math.exp(-x * x / (2d * sigma * sigma)) / (SQRT2PI * sigma);
    }

    protected static double gaussian(double x, double sigma) {
        return Math.exp(-x * x / (2d * sigma * sigma)) / (Math.sqrt(2 * Math.PI) * sigma);
    }

    protected static float[] normalizedGaussianBellForStd(float std) {
        float kernelSize = 6 * std;
        float halfLineLength = kernelSize / 2;
        int nbrCoef = (int) Math.ceil(halfLineLength) + 1;
        float coef[] = new float[nbrCoef];
        float coefSum = coef[0] = gaussian(0, std);

        for (int i = 1; i < nbrCoef; i++) {
            coef[i] = gaussian(i, std);
            coefSum += coef[i] * 2;
        }

        // normalize by sum of coefficients.
        // normalization does not really matter, because the sum of weights will
        // be normalised for each LIC slope line
        for (int i = 0; i < nbrCoef; i++) {
            coef[i] /= coefSum;
        }

        return coef;
    }

    protected static double[] normalizedGaussianBellForStd(double std) {
        double kernelSize = 6 * std;
        double halfLineLength = kernelSize / 2;
        int nbrCoef = (int) Math.ceil(halfLineLength) + 1;
        double coef[] = new double[nbrCoef];
        double coefSum = coef[0] = gaussian(0, std);

        for (int i = 1; i < nbrCoef; i++) {
            coef[i] = gaussian(i, std);
            coefSum += coef[i] * 2;
        }

        // normalize by sum of coefficients.
        // normalization does not really matter, because the sum of weights will
        // be normalised for each LIC slope line
        for (int i = 0; i < nbrCoef; i++) {
            coef[i] /= coefSum;
        }

        return coef;
    }

}

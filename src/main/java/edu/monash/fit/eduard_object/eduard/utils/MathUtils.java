/*
 * MathUtils.java
 *
 * Created on February 19, 2006, 1:13 AM
 *
 */
package edu.monash.fit.eduard_object.eduard.utils;

public class MathUtils {

    public static final int binomialCoeff(int n, int k) {
        return MathUtils.fact(n) / (MathUtils.fact(k) * MathUtils.fact(n - k));
    }

    public static final int fact(int m) {
        int fact = 1;
        for (int i = 2; i <= m; i++) {
            fact *= i;
        }
        return fact;
    }

    public static final double log2(double x) {
        return Math.log(x) / Math.log(2.);
    }

    public static boolean numbersAreClose(double x, double y) {
        final double TOL = 0.000000001;
        return MathUtils.numbersAreClose(x, y, TOL);
    }

    public static boolean numbersAreClose(double x, double y, double tolerance) {
        return (Math.abs(x - y) < tolerance);
    }

    /**
     * Numbers are signed in Java. This converts an unsigned byte to an int.
     */
    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * Returns whether a number is a power of two, e.g. 64, 128, 512.
     * http://www.codeguru.com/FORUM/showthread.php?p=1642580#post1642580
     *
     * @param x
     * @return
     */
    public static boolean isPower2(int x) {
        return ((x > 0) && ((x & (x - 1)) == 0));
    }

    /**
     * S-shaped smooth function using cubic Hermite interpolation
     * http://en.wikipedia.org/wiki/Smoothstep
     *
     * Returns 0.0 if x <= edge0 and 1.0 if x >= edge1 and performs smooth
     * Hermite interpolation between 0 and 1 when edge0 < x < edge1.
     * Results are undefined if edge0 >= edge1.
     *
     * @param edge0 interpolated values for x below edge0 will be 0.
     * @param edge1 interpolated values for x above edge1 will be 1.
     * @param x The x value to interpolate a value for.
     * @return smooth interpolation for value x
     */
    public static double smoothstep(double edge0, double edge1, double x) {
        // scale, bias and saturate x to 0..1 range
        x = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0)));
        // evaluate polynomial
        return x * x * (3 - 2 * x);
    }

    /**
     * Elegant pairing function for two non-negative integers. Creates a unique
     * hash code for a pair.
     *
     * By Matthew Szudzik @ Wolfram Research, Inc.
     *
     * @param x
     * @param y
     * @return
     */
    public static int elegantPairing(int x, int y) {
        return x < y ? y * y + x : x * x + x + y;
    }
}

package edu.monash.fit.eduard_object.eduard.grid;

/**
 * An immutable minimum/maximum pair.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public final class MinMax {

    /**
     * The minimum.
     */
    public final float min;
    
    /**
     * The maximum.
     */
    public final float max;
    
    /**
     * Maximum - minimum.
     */
    public final float range;

    /**
     * Constructor. Initializes minimum, maximum and range to Float.NaN.
     */
    public MinMax() {
        min = max = range = Float.NaN;
    }

    /**
     * Constructor. The minimum is required to be smaller or equal to the
     * maximum
     *
     * @param min minimum
     * @param max maximum
     */
    public MinMax(float min, float max) {
        this.min = min;
        this.max = max;
        range = max - min;
        if (min > max) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns {@code true} if the minimum and the maximum are both finite
     * floating-point values; returns {@code false} otherwise (for NaN and
     * infinity values).
     *
     * @return {@code true} if the minimum and the maximum are finite
     * floating-point values, {@code false} otherwise.
     */
    public boolean isValid() {
        return Float.isFinite(range);
    }

    @Override
    public String toString() {
        return min + " to " + max;
    }
}

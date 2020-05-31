package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Clamp grid values to given min-max range.
 *
 * @author Bernie Jenny, Monash University
 */
public final class ClampToRangeOperator extends ThreadedGridOperator {

    private final float min, max;

    /**
     * Constructor.
     *
     * @param min values smaller than min will be set to min.
     * @param max values greater than max will be set to max.
     */
    public ClampToRangeOperator(float min, float max) {
        this(min, max, null);
    }

    /**
     * Constructor.
     *
     * @param min values smaller than min will be set to min.
     * @param max values greater than max will be set to max.
     * @param progressIndicator progress indicator
     */
    public ClampToRangeOperator(float min, float max,
            ProgressIndicator progressIndicator) {
        super(progressIndicator);
        assert (min <= max);
        this.min = min;
        this.max = max;
    }

    @Override
    protected void operateValue(Grid src, Grid dst, int col, int row) {
        float val = src.getValue(col, row);
        val = Math.min(max, Math.max(min, val));
        dst.setValue(val, col, row);
    }

    @Override
    public String getName() {
        return "Clamp to range";
    }

    @Override
    public String toString() {
        return getName()
                + ": min=" + min
                + ", max=" + max;
    }

}

/*
 * ScaleToRangeOperator.java
 *
 */
package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.grid.MinMax;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Scale grid values to a new minimum-maximum range.
 *
 * @author Bernie Jenny
 */
public final class ScaleToRangeOperator extends ThreadedGridOperator {

    /**
     * New minimum and maximum values in the destination grid.
     */
    private final float dstMin, dstMax;

    /**
     * Minimum and maximum values in source grid. Initialized by the
     * single-threaded operate() method and then read by all threads in the
     * multi-threaded {@link operateValue} method. There is no need to make this
     * volatile, as {@link ThreadedGridOperator}s are used with an
     * ExecutorService, which ensures that the threaded operator will see all
     * actions taken prior to the submission of the task.
     */
    private MinMax srcMinMax = new MinMax();

    /**
     * Scale factor initialized by the single-threaded operate() method and then
     * read by all threads in the multi-threaded {@link operateValue} method. See
     * {@link srcMinMax} for absence of volatile qualifier.
     */
    private float scale;

    /**
     * Creates a new instance of ScaleToRangeOperator
     */
    public ScaleToRangeOperator() {
        dstMin = dstMax = 0.f;
    }

    public ScaleToRangeOperator(float newMin, float newMax) {
        this(newMin, newMax, null);
    }

    public ScaleToRangeOperator(float[] minMax) {
        this(minMax[0], minMax[1], null);
    }

    public ScaleToRangeOperator(float newMin, float newMax, ProgressIndicator progress) {
        super(progress);
        this.dstMin = newMin;
        this.dstMax = newMax;
    }

    @Override
    public Grid operate(Grid src, Grid dst) {
        srcMinMax = new MinMaxOperator(progressIndicator).findMinMax(src);
        float dstRange = dstMax - dstMin;
        scale = dstRange / srcMinMax.range;

        if (srcMinMax.range <= 0 || dstRange <= 0) {
            // set everything in dst to the new minimum
            return new FillOperator(dstMin, progressIndicator).operate(src, dst);
        } else {
            return super.operate(src, dst);
        }
    }

    @Override
    protected void operateValue(Grid src, Grid dst, int col, int row) {
        float v = src.getValue(col, row);
        v = (v - srcMinMax.min) * scale + dstMin;
        dst.setValue(v, col, row);
    }

    @Override
    public String getName() {
        return "Scale to range";
    }
    
    @Override
    public String toString() {
        return getName() 
                + ": new range=" + dstMin + " to " + dstMax 
                + ", old range=" + srcMinMax.toString();
    }
}

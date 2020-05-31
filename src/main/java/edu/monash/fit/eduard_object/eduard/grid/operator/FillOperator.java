package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Set all grid values to a constant value.
 *
 * This multi-threaded operator does not seem to be faster than a
 * single-threaded loop, but if offers the advantage of regularly checking for
 * cancellation.
 *
 * @author Bernie Jenny, Faculty of Information Technology, Monash University
 */
public final class FillOperator extends ThreadedGridOperator {

    private final float value;

    public FillOperator(float value) {
        this.value = value;
    }

    public FillOperator(float value, ProgressIndicator progressIndicator) {
        super(progressIndicator);
        this.value = value;
    }

    @Override
    protected void operateRow(Grid ignore, Grid dst, int row) {
        dst.fillRow(value, row);
    }

    @Override
    public String getName() {
        return "Fill";
    }

}

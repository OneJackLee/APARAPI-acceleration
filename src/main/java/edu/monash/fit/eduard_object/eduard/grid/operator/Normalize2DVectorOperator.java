package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Scales 2D vectors stored in two grids to length 1. This operator changes the
 * source grid (x component) and the destination grid (y component).
 * 
 * If the length of the vector is 0, the x and y components of the scaled vector
 * are 0.
 *
 * @author Bernie Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public final class Normalize2DVectorOperator extends ThreadedGridOperator {

    public Normalize2DVectorOperator(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    @Override
    public Grid operate(Grid src) {
        throw new UnsupportedOperationException();
    }

    /**
     * Normalize vector stored in two grids. Both grids are changed.
     *
     * @param xGrid grid with x components
     * @param yGrid grid with y components
     * @param col column
     * @param row row
     */
    @Override
    protected void operateValue(Grid xGrid, Grid yGrid, int col, int row) {
        float x = xGrid.getValue(col, row);
        float y = yGrid.getValue(col, row);
        float l = (float) Math.sqrt(x * x + y * y);
        if (l == 0) {
            xGrid.setValue(0, col, row);
            yGrid.setValue(0, col, row);
        } else {
            xGrid.setValue(x / l, col, row);
            yGrid.setValue(y / l, col, row);
        }
    }

    @Override
    public String getName() {
        return "Normalize 2D vectors";
    }

}

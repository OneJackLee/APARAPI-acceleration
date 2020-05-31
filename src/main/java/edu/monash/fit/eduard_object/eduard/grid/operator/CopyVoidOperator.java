/*
 * CopyVoidOperator.java
 *
 * Created on February 7, 2015
 *
 */
package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Copies void values from a source grid to a destination grid. Does not write
 * to destination cells if the corresponding cell in the source grid is a valid
 * value.
 *
 * @author Bernie Jenny, Monash University, Melbourne
 */
public final class CopyVoidOperator extends ThreadedGridOperator {

    public CopyVoidOperator() {
    }

    public CopyVoidOperator(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    @Override
    protected void operateValue(Grid src, Grid dst, int col, int row) {
        if (src.isVoid(col, row)) {
            dst.setVoid(col, row);
        }
    }

    @Override
    public String getName() {
        return "Copy void";
    }
}

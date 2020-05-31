package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Copy grid values.
 *
 * @author Bernie Jenny, Monash University.
 */
public final class CopyOperator extends ThreadedGridOperator {

    public CopyOperator() {
    }

    public CopyOperator(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    @Override
    protected void operateRow(Grid src, Grid dst, int row) {
        dst.copyRow(src, row);
    }
    
    @Override
    public String getName() {
        return "Copy";
    }

}

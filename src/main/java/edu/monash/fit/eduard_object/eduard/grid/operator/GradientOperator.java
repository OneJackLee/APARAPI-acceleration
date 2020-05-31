package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Compute dimensionless gradient (or slope steepness) as rise/run, not an
 * angle. Use SlopeOperator if slope in degree is needed.
 *
 * @author Bernie Jenny
 */
public final class GradientOperator extends ThreadedGridOperator {

    public GradientOperator() {
    }

    public GradientOperator(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    @Override
    public void operateValue(Grid src, Grid dst, int col, int row) {
        dst.setValue((float) src.get8NeighborGradient(col, row), col, row);
    }

    @Override
    public String getName() {
        return "Gradient [rise/run]";
    }

}

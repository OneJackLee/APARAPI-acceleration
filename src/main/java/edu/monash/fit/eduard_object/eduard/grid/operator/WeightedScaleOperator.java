package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * A weighted and scaled combination of a grid with itself:
 *
 * out = in * w * scale + in * (1 - w)
 *
 * @author Bernie Jenny
 */
public final class WeightedScaleOperator extends ThreadedGridOperator {

    private final Grid weightGrid;
    private final float scale;

    public WeightedScaleOperator(Grid weightGrid, float scale, ProgressIndicator progress) {
        super(progress);
        this.weightGrid = weightGrid;
        this.scale = scale;
    }

    @Override
    protected void operateValue(Grid src, Grid dst, int col, int row) {
        // out = in * w * scale + in * (1 - w)
        // out = in * (w * scale + 1 - w)
        // out = in * (w * (scale - 1) + 1)
        float v = src.getValue(col, row) * (weightGrid.getValue(col, row) * (scale - 1f) + 1f);
        dst.setValue(v, col, row);
    }
    
    @Override
    public String getName() {
        return "Weighted scale";
    }
    
    @Override
    public String toString() {
        return getName() 
                + ": weightGrid=" + (weightGrid != null ? weightGrid.toString() : "null")
                + ", scale=" + scale;
    }
}

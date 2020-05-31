package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Gradient in horizontal direction. Computes the gradient from the cells to the
 * left and to the right without considering the central value. This is the
 * Zevenbergen-Thorne method, which is using two neighbors.
 *
 * The Evans-Young method is an alternative that uses six neighbors.
 *
 * Reference: Zevenbergen & Thorne, 1987. Quantitative analysis of land surface
 * topography. Earth Surface Processes and Landforms 12, no. 1, 47-56.
 *
 * @author Bernie Jenny, Monash University
 */
public final class GradientXZevenbergenThorneOperator extends ThreadedGridOperator {

    public GradientXZevenbergenThorneOperator() {
    }

    public GradientXZevenbergenThorneOperator(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    /**
     * Operate on a single cell.
     *
     * @param src the source grid
     * @param dst the destination grid
     * @param col column of the cell
     * @param row row of the cell
     */
    @Override
    protected void operateValue(Grid src, Grid dst, int col, int row) {
        // Zevenbergen-Thorne method using left and right neighbors
        dst.setValue(src.getXGradient(col, row), col, row);
    }
    
    @Override
    public String getName() {
        return "X gradient (Zevenbergen-Thorne)";
    }
}

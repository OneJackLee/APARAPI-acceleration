/*
 * PlanCurvatureZevenbergenThorneOperator.java
 *
 * Created on February 14, 2006, 8:43 PM
 */
package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Plan curvature as defined by Zevenbergen and Thorne (1987). This differs from
 * <code>PlanCurvatureOperator</code>, which uses a method by Evans and returns
 * different values. This implementation inverses the sign of plan curvature
 * computed by Zevenbergen & Thorne.
 *
 * Reference: Zevenbergen, L. W., & Thorne, C. R. (1987). Quantitative analysis
 * of land surface topography. Earth surface processes and landforms, 12(1),
 * 47-56.
 *
 * @author Bernie Jenny
 */
public final class PlanCurvatureZevenbergenThorneOperator extends ThreadedGridOperator {

    public PlanCurvatureZevenbergenThorneOperator(ProgressIndicator progress) {
        super(progress);
    }

    public PlanCurvatureZevenbergenThorneOperator() {
    }

    @Override
    protected void operateRow(Grid src, Grid dst, int row) {
        double cellSize = src.getProjectedCellSize(row);
        int nCols = src.getCols();
        for (int col = 0; col < nCols; col++) {
            operateValue(src, dst, col, row, cellSize);
        }
    }

    private void operateValue(Grid src, Grid dst, int col, int row, double cellSize) {
        double invDoubleCellSize = 1 / (2 * cellSize);
        double invCellSizeSq = 1 / (cellSize * cellSize);

        int rabove = Math.max(0, row - 1);
        int rbelow = Math.min(src.getRows() - 1, row + 1);
        int cleft = Math.max(0, col - 1);
        int cright = Math.min(src.getCols() - 1, col + 1);
        double e0 = src.getValue(col, row); // center
        double e1 = src.getValue(cleft, rabove); // north-west
        double e2 = src.getValue(col, rabove); // north
        double e3 = src.getValue(cright, rabove); //north-east
        double e4 = src.getValue(cleft, row); // west
        double e5 = src.getValue(cright, row); // east
        double e6 = src.getValue(cleft, rbelow); // south-west
        double e7 = src.getValue(col, rbelow); // south
        double e8 = src.getValue(cright, rbelow); // south-east

        double D = ((e4 + e5) / 2 - e0) * invCellSizeSq;
        double E = ((e2 + e7) / 2 - e0) * invCellSizeSq;
        double F = (-e1 + e3 + e6 - e8) / 4 * invCellSizeSq;
        double G = (-e4 + e5) * invDoubleCellSize;
        double H = (e2 - e7) * invDoubleCellSize;
        double divider = G * G + H * H;
        double pc = divider == 0 ? 0 : -2 * ((D * H * H + E * G * G - F * G * H) / divider);
        dst.setValue((float) pc, col, row);
    }

    @Override
    public String getName() {
        return "Plan curvature (Zevenbergen-Thorne)";
    }

}

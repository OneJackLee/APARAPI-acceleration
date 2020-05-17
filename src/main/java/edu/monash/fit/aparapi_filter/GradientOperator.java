package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class GradientOperator implements AparapiOperator {
    @Override
    public void operate(Grid src, Grid dest) {
        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = dest.getBuffer();

        int srcCols = src.getCols();
        int srcRows = src.getRows();
        double srcNorth = src.getNorth();
        double srcCellSizes = src.getCellSize();

        Kernel kernel = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                destBuffer[i] = srcBuffer[i];
            }

            public double getProjectedCellSize(int row) {
                // TODO replace with a better test to detect geographic coordinate systems that also uses the extent of the grid
                if (srcCellSizes < 0.1) {
                    double latRad = Math.toRadians(srcNorth - row * srcCellSizes);
                    double latitudeCorrectedCellSizeRad = Math.cos(latRad) * Math.toRadians(srcCellSizes);
                    return latitudeCorrectedCellSizeRad * 6_371_007; // radius of authalic sphere for GRS 1980
                }
                return srcCellSizes;
            }



        };

        kernel.execute(Range.create(src.getCols()*src.getRows()));
        kernel.dispose();

    }

    @Override
    public void operateValue(Grid src, Grid dest, int col, int row) {

    }
}

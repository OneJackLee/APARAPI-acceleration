package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;

public class GradientOperator implements AparapiOperator {
    Grid src;
    Grid dest;
    double timer;

    public GradientOperator(){
    }

    public Grid operate(Grid src) {
        this.src = src;
        this.dest = Grid.shallowCopy(src);

        int srcCols = src.getCols();
        int srcRows = src.getRows();
        double srcNorth = src.getNorth();
        double srcCellSizes = src.getCellSize();
        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = dest.getBuffer();

        Kernel kernel = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                destBuffer[i] = get8NeighborGradient(getCol(i), getRow(i));
            }

            public int getRow(int directIndex){
                return directIndex / srcCols;
            }

            public int getCol(int directIndex){
                return directIndex % srcCols;
            }

            public int getDirectIndex(int col, int row){
                return col + row * srcCols;
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

            public float get8NeighborGradient(int col, int row) {
                final float projectedCellSize = (float) getProjectedCellSize(row);
                final float cellSizeTimes8 = 8 * projectedCellSize;

                final int cols = srcCols;
                final int rows = srcRows;
            //
                final int colLeft = col > 0 ? col - 1 : 0;
                final int colRight = col < cols - 1 ? col + 1 : cols - 1;
                final int rowTop = row > 0 ? row - 1 : 0;
                final int rowBottom = row < rows - 1 ? row + 1 : rows - 1;
            //
                final float a = srcBuffer[getDirectIndex(colLeft, rowTop)]; //(colLeft, rowTop);
                final float b = srcBuffer[getDirectIndex(col, rowTop)]; //get(col, rowTop);
                final float c = srcBuffer[getDirectIndex(colRight, rowTop)]; //get(colRight, rowTop);
                final float d = srcBuffer[getDirectIndex(colLeft, row)]; //get(colLeft, row);
            //
                final float f = srcBuffer[getDirectIndex(colRight, row)]; //get(colRight, row);
                final float g = srcBuffer[getDirectIndex(colLeft, rowBottom)]; //get(colLeft, rowBottom);
                final float h = srcBuffer[getDirectIndex(col, rowBottom)]; //get(col, rowBottom);
                final float i = srcBuffer[getDirectIndex(colRight, rowBottom)]; //get(colRight, rowBottom);
            //
                final float dZdX = ((c + (2 * f) + i) - (a + (2 * d) + g)) / cellSizeTimes8;
                final float dZdY = ((g + (2 * h) + i) - (a + (2 * b) + c)) / cellSizeTimes8;
                return (float) Math.sqrt((dZdX * dZdX) + (dZdY * dZdY));
            }

        };
        kernel.setExplicit(true);
        kernel.put(srcBuffer);
        kernel.put(destBuffer);

//        System.out.println(kernel.getTargetDevice());
        kernel.execute(1);
        timer = System.nanoTime();
        kernel.execute(Range.create(src.getLength()));
        timer = (System.nanoTime() - timer) / 1000000;

        MaskFilter.benchmarking.add("Gradient [rise/run]:  " + timer + " ms");

        kernel.get(destBuffer);
        kernel.dispose();
        dest.setBufferReceived(destBuffer);
        return dest;
    }

    public double getTimer(){
        return timer;
    }

}

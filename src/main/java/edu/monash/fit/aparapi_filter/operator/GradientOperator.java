package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;

/**
 * Compute dimensionless gradient or slope steepness as rise or run
 *
 * [REFERENCE: Eduard GradientOperator class]
 */
public class GradientOperator implements AparapiOperator {
    Grid src;           // the source grid
    Grid dest;          // the result grid
    double timer;       // timer

    /**
     * The empty constructor (default)
     */
    public GradientOperator(){
    }

    @Override
    public Grid operate(Grid src) {
        if (src == null){
            throw new NullPointerException("");
        }
        /*
         APARAPI unable to access the class attributes directly thus has to "copy" the references again
         */
        this.src = src;
        this.dest = Grid.shallowCopy(src);      // create the Grid holder with the attributes of src Grid

        /*
        As APARAPI doesnt supported Object-oriented design and method calling, thus arguments has to be
        assigned
         */
        int srcCols = src.getCols();
        int srcRows = src.getRows();
        double srcNorth = src.getNorth();
        double srcCellSizes = src.getCellSize();
        float[] srcBuffer = src.getBuffer();    // holder
        float[] destBuffer = dest.getBuffer();  // holder

        // anonymous object of APARAPI kernel
        /*
         * As no function call allowed in Kernel object and Kernel doesnt support Inherited method,
         * thus we will have to code the duplicate method. It is the trade-off between functionality and OODesign
         */
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

            /**
             * Returns the distance between two neighboring rows or columns. If the cell
             * size is in spherical coordinates, the cell size is converted to meters.
             * This conversion is approximate, as a spherical model of the Earth is used
             * (authalic sphere for GRS 1980 with R = 6371007 m).
             *
             * [REFERENCE: Eduard Grid class]
             * @param row if the grid coordinate system uses spherical coordinates, the
             * cell size for this grid row is computed.
             * @return the distance between two rows or columns in meters
             */
            public double getProjectedCellSize(int row) {
                // TODO replace with a better test to detect geographic coordinate systems that also uses the extent of the grid
                if (srcCellSizes < 0.1) {
                    double latRad = Math.toRadians(srcNorth - row * srcCellSizes);
                    double latitudeCorrectedCellSizeRad = Math.cos(latRad) * Math.toRadians(srcCellSizes);
                    return latitudeCorrectedCellSizeRad * 6_371_007; // radius of authalic sphere for GRS 1980
                }
                return srcCellSizes;
            }

            /**
             * Returns the dimensionless rise/run slope computed from 8 neighboring
             * cells.
             *
             * Equation from:
             * http://help.arcgis.com/en/arcgisdesktop/10.0/help../index.html#/How_Slope_works/009z000000vz000000/
             * [REFERENCE: Eduard Grid class]
             * @param col Column index. Must be in [0, columns - 1].
             * @param row Row index. Must be in [0, rows - 1].
             * @return dimensionless gradient
             */
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
        kernel.setExplicit(true);           // explicitly manage transfers between GPU memory and CPU
        kernel.put(srcBuffer);              // send srcBuffer to GPU
        kernel.put(destBuffer);             // send destBuffer to GPU

        /*
        generate a KernelRunner run instantly, to eagerly create it
         */
        kernel.execute(1);
        timer = System.nanoTime();
        kernel.execute(Range.create(src.getLength()));  // kernel execution
        timer = (System.nanoTime() - timer) / 1000000;

        MaskFilter.benchmarking.add("Gradient [rise/run]:  " + timer + " ms");

        kernel.get(destBuffer);             // fetch destBuffer from GPU
        kernel.dispose();                   // clean kernel when finish job
        dest.setBufferReceived(destBuffer);
        return dest;
    }

    @Override
    public double getTimer(){
        return timer;
    }

}

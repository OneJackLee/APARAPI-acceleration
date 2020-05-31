package edu.monash.fit.eduard_object.eduard.grid;

import edu.monash.fit.eduard_object.eduard.grid.operator.FillOperator;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A Grid is a regular scalar field, for example a digital elevation model.
 * <p>
 * A Grid stores field values in a {@link GridBuffer}, see
 * {@link FloatBuffer}, {@link ShortBuffer} and {@link SignedNormalizedBuffer}.
 * <p>
 * This is an immutable class with the important exception of the values in the
 * GridBuffer. It is the responsibility of the client to apply synchronization
 * mechanisms if necessary.
 *
 * @author Bernie Jenny
 */
public final class Grid {

    /**
     * Minimum number of rows and columns for a grid to be valid.
     */
    public static final int MIN_DIM = 2;

    private static final double SQRT2 = Math.sqrt(2);

    /**
     * The size between two neighboring columns or rows.
     */
    private final double cellSize;

    /**
     * grid values
     */
    private final GridBuffer grid;

    /**
     * horizontal coordinate of the first column
     */
    private final double west;

    /**
     * vertical coordinate of the last row
     */
    private final double south;

    /**
     * content of an associated .prj file with information about the coordinate
     * reference system of this grid. Can be null.
     */
    private final String prjFileContent;

    /**
     * Create a new grid with the same dimensions, position, resolution and
     * coordinate reference information as a template grid. Grid values are not
     * copied. A new {@link FloatBuffer} is allocated with all values equal to
     * 0.
     *
     * @param grid Grid to copy.
     * @return A shallow copy of the passed grid.
     */
    public static Grid shallowCopy(Grid grid) {
        return new Grid(grid.getCellSize(), grid.getWest(), grid.getSouth(),
                grid.getPrjFileContent(),
                new FloatBuffer(grid.getCols(), grid.getRows()));
    }
    
    /**
     * Copy constructor deep cloning all parameters and grid values. The new
     * Grid has the same type of GridBuffer as the passed grid.
     *
     * @param grid Grid to copy.
     */
    public Grid(Grid grid) {
        this(grid.getCellSize(), grid.getWest(), grid.getSouth(),
                grid.getPrjFileContent(), grid.grid.copy());
    }

    /**
     * Constructor.
     *
     * @param cols The number of vertical columns in the new grid.
     * @param rows The number of horizontal rows in the new grid.
     */
    public Grid(int cols, int rows) {
        this(cols, rows, 1, 0, 0, null);

    }

    /**
     * Constructor.
     *
     * @param cols The number of vertical columns in the new grid.
     * @param rows The number of horizontal rows in the new grid.
     * @param cellSize The size between two rows or columns.
     * @param west Horizontal coordinate of west-most column
     * @param south Vertical coordinate of south-most row
     * @param prjFileContent Projection file content with information about the
     * coordinate reference system of this grid.
     */
    public Grid(int cols, int rows, double cellSize, double west, double south,
            String prjFileContent) {
        this(cellSize, west, south, prjFileContent, new FloatBuffer(cols, rows));
    }

    /**
     * Constructor.
     *
     * @param cellSize The size between two rows or columns.
     * @param west Horizontal coordinate of west-most column
     * @param south Vertical coordinate of south-most row
     * @param prjFileContent Projection file content with information about the
     * coordinate reference system of this grid.
     * @param buffer {@link GridBuffer} for storing values.
     */
    public Grid(double cellSize, double west, double south, String prjFileContent,
            GridBuffer buffer) {

        if (buffer.getCols() < MIN_DIM
                || buffer.getRows() < MIN_DIM) {
            throw new IllegalArgumentException("Grid too small");
        }
        if (!Double.isFinite(cellSize) || cellSize <= 0) {
            throw new IllegalArgumentException("Negative cell size");
        }
        if (!Double.isFinite(west) || !Double.isFinite(south)) {
            throw new IllegalArgumentException();
        }

        grid = Objects.requireNonNull(buffer);
        this.cellSize = cellSize;
        this.west = west;
        this.south = south;
        this.prjFileContent = prjFileContent;
    }

    /**
     * Returns the value at a specified position in the grid.
     *
     * @param col The vertical column for which a value is returned.
     * @param row The horizontal row for which a value is returned.
     * @return The value at the specified position.
     */
    public float getValue(int col, int row) {
        return grid.get(col, row);
    }

    /**
     * Returns the value at a specified position in the grid.
     *
     * @param id Position in the grid: id = col+ row * getCols()
     * @return The value at the specified position.
     */
    public float getValue(int id) {
        return grid.get(id);
    }

    /**
     * Sets a value in the grid.
     *
     * @param value The value to store in the grid.
     * @param col The vertical column for which a value is set.
     * @param row The horizontal row for which a value is set.
     */
    public void setValue(float value, int col, int row) {
        grid.set(value, col, row);
    }

    /**
     * Sets a value in the grid.
     *
     * @param value The value to store in the grid. The value is cast to a
     * float.
     * @param col The vertical column for which a value must be set.
     * @param row The horizontal row for which a value must be set.
     */
    public void setValue(double value, int col, int row) {
        setValue((float) value, col, row);
    }

    /**
     * Void a value in the grid.
     *
     * @param col The vertical column for which a value is voided.
     * @param row The horizontal row for which a value is voided.
     */
    public void setVoid(int col, int row) {
        grid.setVoid(col, row);
    }

    /**
     * Returns true if the cell at a specified position in the grid is a void
     * value.
     *
     * @param col Column
     * @param row Row
     * @return True if cell at col/row is void, false otherwise.
     */
    public boolean isVoid(int col, int row) {
        return grid.isVoid(col, row);
    }

    /**
     * Returns true if the cell at a specified position in the grid is a void
     * value.
     *
     * @param col Column
     * @param row Row
     * @return True if cell at col/row is void, false otherwise.
     */
    public boolean isVoid(int id) {
        return grid.isVoid(id);
    }

    /**
     * Returns true if the passed value is a valid, non-void cell value.
     *
     * @param v value to test
     * @return true if the passed value is not a void value, false otherwise.
     */
    public static boolean isValid(float v) {
        return Float.isFinite(v);
    }

    /**
     * Returns true if all values are identical. Void values are ignored. If all
     * values are void, this method returns false. If there is only one non-void
     * value, this method returns true.
     *
     * @return true if all values are identical; false if at least two values
     * differ or if all values are void.
     */
    public boolean isFlat() {
        int nbrValues = getRows() * getCols();

        // search first non-void value
        int i = 0;
        for (; i < nbrValues; i++) {
            if (!isVoid(i)) {
                break;
            }
        }

        // test whether all values are void
        if (i == nbrValues) {
            return false;
        }

        // search for a value that is different from the first value
        float firstValue = getValue(i);
        for (int j = i; j < nbrValues; j++) {
            float v = getValue(j);
            if (v != firstValue && isValid(v)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets all grid values to a constant value.
     *
     * Important: this uses the multi-threaded FillOperator, and therefore must
     * not be called by another multi-threaded GridOperator method.
     *
     * @param value the value for all cells
     */
    public void fill(float value) {
        new FillOperator(value).operate(this, this);
    }

    /**
     * Sets all grid values of a row to a constant value.
     *
     * @param value the value for all calls in the row
     * @param rowID the row index
     */
    public void fillRow(float value, int rowID) {
        int cols = grid.getCols();
        for (int col = 0; col < cols; col++) {
            grid.set(value, col, rowID);
        }
    }

    /**
     * Copies all cells of a passed row to the corresponding row of this grid.
     *
     * @param value new value for the row
     * @param rowID row index
     */
    public void copyRow(Grid src, int rowID) {
        int cols = grid.getCols();
        for (int col = 0; col < cols; col++) {
            float v = src.getValue(col, rowID);
            setValue(v, col, rowID);
        }
    }

    public final float getNearestNeighbor(double x, double y) {
        final int rows = getRows();
        final int cols = getCols();
        final double north = south + (rows - 1) * cellSize;
        final int col = (int) Math.round((x - west) / cellSize);
        final int row = (int) Math.round((north - y) / cellSize);
        if (col < 0 || col > cols - 1 || row < 0 || row > rows - 1) {
            return Float.NaN;
        }
        return getValue(col, row);
    }

    /**
     * Bilinear interpolation for position x/y.
     *
     * If x/y aligns exactly with a point in the grid, no interpolation is
     * applied to avoid the influence of potential NaN neighbors.
     *
     * If any of the four corner points of the cell that contains x/y are NaN,
     * the result is NaN. An exception is when x/y exactly aligns with a grid
     * column or a grid row and the two neighboring values in vertical or
     * horizontal direction are both not NaN.
     *
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return interpolated value
     */
    public final float getBilinearInterpol(final double x, final double y) {
        final int rows = getRows();
        final int cols = getCols();
        final double north = south + (rows - 1) * cellSize;

        // coordinates in grid units relative to top-left corner
        final double dx = (x - west) / cellSize;
        final double dy = (north - y) / cellSize;

        // column and row of the top-left corner of the cell
        final int col = (int) dx;
        final int row = (int) dy;

        // check for out of bounds
        if (col < 0 || col > cols - 1 || row < 0 || row > rows - 1) {
            return Float.NaN;
        }

        // coordinates relative to cell between 0 and 1 from the bottom-left cell corner
        final double relX = dx - col;
        final double relY = 1 + row - dy;

        // check for case when x/y is exactly on a grid point to avoid potential 
        // neighboring NaN values.
        if (relX == 0 && relY == 1) {
            return getValue(col, row);
        }

        // point on the last column of the grid
        // or point aligns with a column (use linear interpolation to avoid 
        // potential NaN values in neighboring rows)
        if (col == cols - 1 || relX == 0) {
            // point on lower right corner
            if (row == rows - 1) {
                return getValue(cols - 1, rows - 1);
            }
            final float top = getValue(col, row);
            final float bottom = getValue(col, row + 1);
            return (float) (relY * (top - bottom) + bottom);
        }

        // point on lower border
        // or point aligns with a row (use linear interpolation to avoid 
        // potential NaN values in neighboring columns)
        if (row == rows - 1 || relY == 1) {
            final float left = getValue(col, row);
            final float right = getValue(col + 1, row);
            return (float) (relX * (right - left) + left);
        }

        // value at bottom left corner
        final float q11 = getValue(col, row + 1);
        // value at bottom right corner
        final float q21 = getValue(col + 1, row + 1);
        // value at top left corner
        final float q12 = getValue(col, row);
        // value at top right corner
        final float q22 = getValue(col + 1, row);

        // assume all values are not NaN
        return (float) (q11 + (q21 - q11) * relX + (q12 - q11) * relY + (q11 - q21 - q12 + q22) * relX * relY);
    }

    /**
     * Returns the minimum and the maximum value of the grid. Run length is
     * linear with the size of this grid, as each cell is visited.
     *
     * This method should be avoided if possible, because this search is not
     * multi-threaded and is blocking the calling thread. A faster,
     * multi-threaded search is provided by MinMaxOperator. However,
     * MinMaxOperator can not be run concurrently to another
     * ThreadedGridOperator. Only use grid.getMinMax() when there is the
     * possibility that MinMaxOperator will run concurrently to another
     * ThreadedGridOperator (which would block one of the threads). This occurs,
     * for example, when an event handler in the EDT starts a multi-threaded
     * operation on a grid, and then requires min/max to update the GUI.
     *
     * @return Returns an array with two elements. The first element is the
     * minimum value in the grid, the second value is the maximum value in the
     * grid.
     */
    public float[] getMinMax() {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        int nCols = getCols();
        int nRows = getRows();

        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < nCols; c++) {
                float val = getValue(c, r);
                if (val > max) {
                    max = val;
                }
                if (val < min) {
                    min = val;
                }
            }
        }

        if (min == Float.MAX_VALUE) {
            // grid contains NaN values only
            min = max = Float.NaN;
        }

        return new float[]{min, max};
    }

    /**
     * Returns the number of columns in the grid.
     *
     * @return The number of columns in the grid.
     */
    public int getCols() {
        return grid.getCols();
    }

    /**
     * Returns the number of rows in the grid.
     *
     * @return The number of rows in the grid.
     */
    public int getRows() {
        return grid.getRows();
    }

    /**
     * Returns the distance between two neighboring rows or columns. The
     * returned value can be in projected ground coordinates or in spherical
     * coordinates. Also see getProjectedCellSize().
     *
     * @return the distance between two rows or columns.
     */
    public double getCellSize() {
        return cellSize;
    }

    /**
     * Returns the distance between two neighboring rows or columns. If the cell
     * size is in spherical coordinates, the cell size is converted to meters.
     * This conversion is approximate, as a spherical model of the Earth is used
     * (authalic sphere for GRS 1980 with R = 6371007 m).
     *
     * @param row if the grid coordinate system uses spherical coordinates, the
     * cell size for this grid row is computed.
     * @return the distance between two rows or columns in meters
     */
    public double getProjectedCellSize(int row) {
        // TODO replace with a better test to detect geographic coordinate systems that also uses the extent of the grid
        if (cellSize < 0.1) {
            double latRad = Math.toRadians(getNorth() - row * cellSize);
            double latitudeCorrectedCellSizeRad = Math.cos(latRad) * Math.toRadians(cellSize);
            return latitudeCorrectedCellSizeRad * 6_371_007; // radius of authalic sphere for GRS 1980
        }
        return cellSize;
    }

    /**
     * Returns whether the value returned by getCellSize is in spherical
     * coordinates.
     *
     * Currently assumes that small cell size values always mean spherical
     * coordinates.
     *
     * TODO: should parse the WKT .prj file to extract units and size of
     * ellipsoid
     *
     * @return true if the cell size is in spherical coordinates, false
     * otherwise.
     */
    public boolean isCellSizeInSphericalCoordinates() {
        return cellSize < 0.1;
    }

    /**
     * Test whether the passed grid has the same number of columns and rows, the
     * same cell size, and the same west and north positions.
     *
     * @param grid grid to compare to. Can be null.
     * @return true if the passed grid has the same geometry, false otherwise or
     * if grid == null
     */
    public boolean sameGeometry(Grid grid) {
        return grid != null
                && getCols() == grid.getCols()
                && getRows() == grid.getRows()
                && getCellSize() == grid.getCellSize()
                && getWest() == grid.getWest()
                && getNorth() == grid.getNorth();
    }

    /**
     * Returns the aspect angle in radians in clockwise direction between 0 and
     * 2pi.
     *
     * After Terrain Analysis Principles and Applications by Wilson and Gallant,
     * p. 55.
     *
     * @param col column
     * @param row row
     * @return Aspect in radians in clockwise direction between 0 and 2 pi.
     * North is 0.
     */
    public double getAspect(int col, int row) {
        int cols = getCols();
        int rows = getRows();
        int colLeft = col > 0 ? col - 1 : 0;
        int colRight = col < cols - 1 ? col + 1 : cols - 1;
        int rowTop = row > 0 ? row - 1 : 0;
        int rowBottom = row < rows - 1 ? row + 1 : rows - 1;

        float w = getValue(colLeft, row);
        float e = getValue(colRight, row);
        float s = getValue(col, rowBottom);
        float n = getValue(col, rowTop);
        float dx = e - w;
        float dy = n - s;
        return Math.PI - Math.atan(dy / dx) + Math.PI / 2 * Math.signum(dx);
    }

    /**
     * Returns the terrain orientation angle in radians. Terrain orientation is
     * the angle in the horizontal plane along the steepest slope. This is
     * similar to the aspect angle, but is counter-clockwise counted from east,
     * and between -pi and pi.
     *
     * @param col column
     * @param row row
     * @return Aspect in radians in counter-clockwise direction between -pi and
     * pi. East is 0.
     */
    public double getOrientation(int col, int row) {
        int cols = getCols();
        int rows = getRows();
        int colLeft = col > 0 ? col - 1 : 0;
        int colRight = col < cols - 1 ? col + 1 : cols - 1;
        int rowTop = row > 0 ? row - 1 : 0;
        int rowBottom = row < rows - 1 ? row + 1 : rows - 1;

        float w = getValue(colLeft, row);
        float e = getValue(colRight, row);
        float s = getValue(col, rowBottom);
        float n = getValue(col, rowTop);
        float dx = e - w;
        float dy = n - s;
        return Math.atan2(dy, dx) + Math.PI;
    }

    /**
     * Returns the dimensionless rise/run slope computed from 8 neighboring
     * cells.
     *
     * Equation from:
     * http://help.arcgis.com/en/arcgisdesktop/10.0/help../index.html#/How_Slope_works/009z000000vz000000/
     *
     * @param col Column index. Must be in [0, columns - 1].
     * @param row Row index. Must be in [0, rows - 1].
     * @return dimensionless gradient
     */
    public float get8NeighborGradient(int col, int row) {
        final float projectedCellSize = (float) getProjectedCellSize(row);
        final float cellSizeTimes8 = 8 * projectedCellSize;

        final int cols = getCols();
        final int rows = getRows();

        final int colLeft = col > 0 ? col - 1 : 0;
        final int colRight = col < cols - 1 ? col + 1 : cols - 1;
        final int rowTop = row > 0 ? row - 1 : 0;
        final int rowBottom = row < rows - 1 ? row + 1 : rows - 1;

        final float a = getValue(colLeft, rowTop);
        final float b = getValue(col, rowTop);
        final float c = getValue(colRight, rowTop);
        final float d = getValue(colLeft, row);

        final float f = getValue(colRight, row);
        final float g = getValue(colLeft, rowBottom);
        final float h = getValue(col, rowBottom);
        final float i = getValue(colRight, rowBottom);

        final float dZdX = ((c + (2 * f) + i) - (a + (2 * d) + g)) / cellSizeTimes8;
        final float dZdY = ((g + (2 * h) + i) - (a + (2 * b) + c)) / cellSizeTimes8;
        return (float) Math.sqrt((dZdX * dZdX) + (dZdY * dZdY));
    }

    /**
     * Returns the slope in radians. Uses get8NeighborGradient() to compute the
     * rise/run gradient.
     *
     * @param col Column index. Must be in [0, columns - 1].
     * @param row Row index. Must be in [0, rows - 1].
     * @return slope in radians
     */
    public double getSlopeRad(int col, int row) {
        double gradient = get8NeighborGradient(col, row);
        double slopeRad = Math.atan(gradient);
        return slopeRad;
    }

    /**
     * Gradient in horizontal direction. Computes the gradient from the cells to
     * the left and to the right without considering the central value. This
     * uses the method by Zevenbergen and Thorne.
     *
     * Reference: Zevenbergen & Thorne, 1987. Quantitative analysis of land
     * surface topography. Earth Surface Processes and Landforms 12, no. 1,
     * 47-56.
     *
     * @param col column of central cell
     * @param row row of central cell
     * @return rise/run in horizontal direction
     */
    public double getXGradient(int col, int row) {
        // general equation: (right value - left value) / 2 cellsize

        double projectedCellSize = getProjectedCellSize(row);

        if (col == 0) {
            float centralValue = getValue(0, row);
            float rightValue = getValue(1, row);
            return (rightValue - centralValue) / projectedCellSize;
        } else if (col == getCols() - 1) {
            float centralValue = getValue(col, row);
            float leftValue = getValue(col - 1, row);
            return (centralValue - leftValue) / projectedCellSize;
        } else {
            float leftValue = getValue(col - 1, row);
            float rightValue = getValue(col + 1, row);
            double xGradient = (rightValue - leftValue) / (2 * projectedCellSize);

            // if the gradient is NaN, test whether a gradient can be computed 
            // from the central value and the left or right neighbor
            if (Double.isNaN(xGradient)) {
                float centralValue = getValue(col, row);
                if (!Float.isNaN(centralValue)) {
                    if (!Float.isNaN(leftValue)) {
                        xGradient = (centralValue - leftValue) / projectedCellSize;
                    }
                    if (!Float.isNaN(rightValue)) {
                        xGradient = (rightValue - centralValue) / projectedCellSize;
                    }
                    // If this point is reached, at least two of the three cells 
                    // are NaN.
                }
            }
            return xGradient;
        }
    }

    /**
     * Gradient in vertical direction. Computes the gradient from the cells
     * above and below without considering the central value.
     *
     * @param col column of central cell
     * @param row row of central cell
     * @return rise/run in vertical direction
     */
    public double getYGradient(int col, int row) {
        // general equation: (top value - bottom value) / 2 cellsize
        double yGradient;

        double projectedCellSize = getProjectedCellSize(row);
        if (row == 0) {
            final float centralValue = getValue(col, 0);
            final float bottomValue = getValue(col, 1);
            yGradient = (centralValue - bottomValue) / projectedCellSize;
        } else if (row == getRows() - 1) {
            final float centralValue = getValue(col, row);
            final float topValue = getValue(col, row - 1);
            yGradient = (topValue - centralValue) / projectedCellSize;
        } else {
            final float topValue = getValue(col, row - 1);
            final float bottomValue = getValue(col, row + 1);
            yGradient = (topValue - bottomValue) / (2 * projectedCellSize);

            // if the gradient is NaN, test whether a gradient can be computed 
            // from the central value and the top or bottom neighbor
            if (Double.isNaN(yGradient)) {
                final float centralValue = getValue(col, row);
                if (!Float.isNaN(centralValue)) {
                    if (!Float.isNaN(bottomValue)) {
                        yGradient = (centralValue - bottomValue) / projectedCellSize;
                    }
                    if (!Float.isNaN(topValue)) {
                        yGradient = (topValue - centralValue) / projectedCellSize;
                    }
                    // If this point is reached, at least two of the three cells 
                    // are NaN.
                }
            }
        }

        return yGradient;
    }

    /**
     * Returns true if the value at col/row is void, or if one of its 8
     * neighbors is void.
     *
     * @param col column
     * @param row row
     * @return true if col/row is inside or on the border of a void area, false
     * otherwise.
     */
    public boolean bordersVoidArea(int col, int row) {
        int firstRow = Math.max(0, row - 1);
        int lastRow = Math.min(getRows() - 1, row + 1);
        int firstCol = Math.max(0, col - 1);
        int lastCol = Math.min(getCols() - 1, col + 1);
        for (int r = firstRow; r <= lastRow; r++) {
            for (int c = firstCol; c <= lastCol; c++) {
                if (Float.isNaN(getValue(c, r))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the value at x/y is void, or if one of its 8 neighbors is
     * void.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if col/row is inside or on the border of a void area, false
     * otherwise.
     */
    public boolean bordersVoidArea(double x, double y) {
        int col = (int) Math.round((x - west) / cellSize);
        int row = (int) Math.round((getNorth() - y) / cellSize);
        return bordersVoidArea(col, row);
    }

    /**
     * Returns true if the value at col/row is part of a flat square. The four
     * cells around col/row are evaluated.
     *
     * @param col column
     * @param row row
     * @param maxDif maximum difference between the value at col/row and any of
     * the eight neighboring vertices.
     * @return
     */
    public boolean hasFlatNeighbor(int col, int row, double maxDif) {
        // TODO add support for border cells
        if (col == 0 || col == getCols() - 1 || row == 0 || row == getRows() - 1) {
            return false;
        }

        float c = getValue(col, row);

        float w = getValue(col - 1, row);
        float e = getValue(col + 1, row);
        float s = getValue(col, row + 1);
        float n = getValue(col, row - 1);

        float nw = getValue(col - 1, row - 1);
        float ne = getValue(col + 1, row - 1);
        float sw = getValue(col - 1, row + 1);
        float se = getValue(col + 1, row + 1);

        float dw = Math.abs(c - w);
        float de = Math.abs(c - e);
        float ds = Math.abs(c - s);
        float dn = Math.abs(c - n);

        double dnw = Math.abs(c - nw) * SQRT2;
        double dne = Math.abs(c - ne) * SQRT2;
        double dsw = Math.abs(c - sw) * SQRT2;
        double dse = Math.abs(c - se) * SQRT2;

        return (de < maxDif && dn < maxDif && dne < maxDif)
                || (dn < maxDif && dw < maxDif && dnw < maxDif)
                || (dw < maxDif && ds < maxDif && dsw < maxDif)
                || (ds < maxDif && de < maxDif && dse < maxDif);
    }

    public boolean hasFlatNeighbor(double x, double y, double maxDif) {
        float c = getBilinearInterpol(x, y);

        float w = getBilinearInterpol(x - cellSize, y);
        float e = getBilinearInterpol(x + cellSize, y);
        float s = getBilinearInterpol(x, y - cellSize);
        float n = getBilinearInterpol(x, y + cellSize);

        float nw = getBilinearInterpol(x - cellSize, y + cellSize);
        float ne = getBilinearInterpol(x + cellSize, y + cellSize);
        float sw = getBilinearInterpol(x - cellSize, y - cellSize);
        float se = getBilinearInterpol(x + cellSize, y - cellSize);

        float dw = Math.abs(c - w);
        float de = Math.abs(c - e);
        float ds = Math.abs(c - s);
        float dn = Math.abs(c - n);

        double dnw = Math.abs(c - nw) * SQRT2;
        double dne = Math.abs(c - ne) * SQRT2;
        double dsw = Math.abs(c - sw) * SQRT2;
        double dse = Math.abs(c - se) * SQRT2;

        return (de < maxDif && dn < maxDif && dne < maxDif)
                || (dn < maxDif && dw < maxDif && dnw < maxDif)
                || (dw < maxDif && ds < maxDif && dsw < maxDif)
                || (ds < maxDif && de < maxDif && dse < maxDif);
    }

    @Override
    public String toString() {
        float[] minMax = getMinMax(); // use single-threaded version to avoid blocking operators
        return "Grid: rows: " + getRows() + ",\t cols: " + getCols()
                + ",\t west: " + getWest() + ",\t east: " + getEast()
                + ",\t south: " + getSouth() + ",\t north: " + getNorth()
                + ",\t cell size: " + getCellSize()
                + ",\t range: " + minMax[0] + " to " + minMax[1];
    }

    /**
     * Returns a descriptive text for GUI display.
     *
     * @param newLine The line separator. Could be \n, <br> or null.
     * @return
     */
    public String getDescription(String newLine) {
        if (newLine == null) {
            newLine = System.getProperty("line.separator");
        }
        DecimalFormat f = new DecimalFormat(cellSize < 1 ? "#,##0.0#####" : "#,##0.0");
        DecimalFormat intFormat = new DecimalFormat("#,###");
        StringBuilder sb = new StringBuilder();
        sb.append("Dimension: ");
        sb.append(intFormat.format(getCols()));
        sb.append("x");
        sb.append(intFormat.format(getRows()));
        sb.append(newLine);
        sb.append("Cell size: ");
        sb.append(f.format(cellSize));
        sb.append(newLine);
        sb.append("West: ");
        sb.append(f.format(getWest()));
        sb.append(newLine);
        sb.append("East: ");
        sb.append(f.format(getWest() + (getCols() - 1) * getCellSize()));
        sb.append(newLine);
        sb.append("South: ");
        sb.append(f.format(getSouth()));
        sb.append(newLine);
        sb.append("North: ");
        sb.append(f.format(getSouth() + (getRows() - 1) * getCellSize()));
        return sb.toString();
    }

    /**
     * Returns a descriptive text for GUI display, including min and max values.
     *
     * @param newLine The line separator. Could be \n, <br> or null.
     * @return
     */
    public String getDescriptionWithStatistics(String newLine) {
        if (newLine == null) {
            newLine = System.getProperty("line.separator");
        }
        DecimalFormat f = new DecimalFormat("#,##0.######");
        StringBuilder sb = new StringBuilder(getDescription(newLine));
        float[] minMax = getMinMax(); // use single-threaded version to avoid blocking operators
        sb.append(newLine);
        sb.append("Minimum value: ");
        sb.append(f.format(minMax[0]));
        sb.append(newLine);
        sb.append("Maximum value: ");
        sb.append(f.format(minMax[1]));
        return sb.toString();
    }

    /**
     * Print top-left corner of grid to System.out. This is for debugging only.
     */
    public void print() {
        print(System.out, 16, 16);
    }

    /**
     * Print top-left corner of grid to a stream. This is for debugging only.
     *
     * @param printStream stream to print to, for example, System.out
     * @param cols number of columns to print.
     * @param rows number of row to print.
     */
    public void print(PrintStream printStream, int cols, int rows) {
        cols = Math.min(cols, getCols());
        rows = Math.min(rows, getRows());
        printStream.println("Columns: " + getCols());
        printStream.println("Rows: " + getRows());
        printStream.println("Cell size: " + getCellSize());
        printStream.println("West: " + getWest());
        printStream.println("South: " + getSouth());
        printStream.println("Top-left values (" + cols + "\u00D7" + rows + ")");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float v = getValue(c, r);
                printStream.printf("%9.2f ", v);
            }
            printStream.println();
        }
    }

    /**
     * @return the west
     */
    public double getWest() {
        return west;
    }

    /**
     * @return the south
     */
    public double getSouth() {
        return south;
    }

    /**
     * The northern border of this grid
     *
     * @return
     */
    public double getNorth() {
        return getSouth() + getHeight();
    }

    /**
     * The eastern border of the grid
     *
     * @return
     */
    public double getEast() {
        return getWest() + getWidth();
    }

    /**
     * The height of the grid.
     *
     * @return the height
     */
    public double getHeight() {
        return (getRows() - 1) * getCellSize();
    }

    /**
     * The width of the grid.
     *
     * @return the width
     */
    public double getWidth() {
        return (getCols() - 1) * getCellSize();
    }

    /**
     * Returns a bounding box for this grid.
     *
     * @return The bounding box.
     */
    public Rectangle2D getBoundingBox() {
        return new Rectangle2D.Double(getWest(), getSouth(), getWidth(), getHeight());
    }

    /**
     * Test whether a point is inside this grid.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return true if x/y is inside this grid; false otherwise.
     */
    public boolean contains(double x, double y) {
        return x >= west && x <= getEast() && y >= south && y <= getNorth();
    }

    /**
     * Returns true if the grid has non-zero dimensions and non-NaN position.
     *
     * @return
     */
    public boolean isWellFormed() {
        return getCols() > 0
                && getRows() > 0
                && getCellSize() > 0
                && !Double.isNaN(getWest())
                && !Double.isNaN(getNorth());
    }

    /**
     * Get the content of the .prj file with information about the coordinate
     * reference system of this grid.
     *
     * @return the content of the .prj file
     */
    public String getPrjFileContent() {
        return prjFileContent;
    }

    /**
     * get the buffer array
     * @return get buffer array
     */
    public float[] getBufferArray(){
        return grid.getBuffer();
    }

}

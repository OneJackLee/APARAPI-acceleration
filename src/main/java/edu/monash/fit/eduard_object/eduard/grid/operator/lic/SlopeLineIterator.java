package edu.monash.fit.eduard_object.eduard.grid.operator.lic;

import edu.monash.fit.eduard_object.eduard.grid.Grid;

/**
 * Iterate along a slope line of steepest ascent or descent at a regular
 * sampling distance. Caches horizontal and vertical gradients in two grids to
 * accelerate computations.
 *
 * @author Bernie Jenny, Monash University, Melbourne
 */
public final class SlopeLineIterator {

    /**
     * grid to trace slope line
     */
    private final Grid grid;

    /**
     * x cell coordinates of current location relative to left border of grid
     */
    private double relX;

    /**
     * y cell coordinates of current location relative to top border of grid
     */
    private double relY;

    /**
     * cached column and row for fast sampling of gradient grids
     */
    private int col, row;

    /**
     * Constructor.
     *
     * @param grid grid for which a slope line is to be computed
     */
    public SlopeLineIterator(Grid grid) {
        if (grid == null || grid.isWellFormed() == false) {
            throw new IllegalArgumentException("invalid grid");
        }
        this.grid = grid;
    }

    /**
     * Reset the current location of the iterator. The next call to
     * nextDownhill() or nextUphill() will start at this new location.
     *
     * @param x
     * @param y
     */
    public void reset(double x, double y) {
        double cellSize = grid.getCellSize();
        this.relX = (x - grid.getWest()) / cellSize;
        this.relY = (grid.getNorth() - y) / cellSize;
        col = (int) Math.round(relX);
        row = (int) Math.round(relY);
    }

    public void reset(int col, int row) {
        this.col = col;
        this.row = row;
        relX = col;
        relY = row;
    }

    /**
     * Move to the next position on the slope line in downhill direction if the
     * next position is inside the bounds of the grid.
     *
     * @return true if the position has changed and it is inside the bounds of
     * the grid.
     */
    public boolean nextDownhill() {
        return next(true);
    }

    /**
     * Move to the next position on the slope line in uphill direction if the
     * next position is inside the bounds of the grid.
     *
     * @return true if the position has changed and it is inside the bounds of
     * the grid.
     */
    public boolean nextUphill() {
        return next(false);
    }

    /**
     * Move to the next position on the slope line if the next position is
     * inside the bounds of the grid.
     *
     * @param downhill flag to indicate direction of line
     * @return true if the new position is inside the bounds of the grid.
     */
    private boolean next(boolean downhill) {
        int cols = grid.getCols();
        int rows = grid.getRows();

        // direction vector to next point
        double dx = grid.getXGradient(col, row);
        double dy = grid.getYGradient(col, row);
        double l = Math.sqrt(dx * dx + dy * dy);
        if (l == 0) {
            dx = dy = 0; // current point is in a flat area
        } else {
            dx /= l;
            dy /= l;
        }

        // invert direction vector for downhill slope
        if (downhill) {
            dx = -dx;
            dy = -dy;
        }

        // coordinates of new point
        double newRelX = relX + dx;
        double newRelY = relY - dy; // vertical gradient uses upwards coordinate axis
        int newCol = (int) Math.round(newRelX);
        int newRow = (int) Math.round(newRelY);

        boolean pointOnGrid = newCol >= 0
                && newRow >= 0
                && newCol < cols
                && newRow < rows;
        if (pointOnGrid) {
            relX = newRelX;
            relY = newRelY;
            col = newCol;
            row = newRow;
        }

        return pointOnGrid;
    }

    public double getX() {
        return grid.getWest() + relX * grid.getCellSize();
    }

    public double getY() {
        return grid.getNorth() - relY * grid.getCellSize();
    }

    public float getZ() {
        // grid.getBilinearInterpol(x, y) is twice slower and is only slightly sharper
        return grid.getValue(col, row);
        //return grid.getBilinearInterpol(getX(), getY());
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}

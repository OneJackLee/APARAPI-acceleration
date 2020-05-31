package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;

/**
 * This operator computes a diffuse shading with values between 0 (black) and 1
 * (white).
 */
public final class ShadingOperator extends ThreadedGridOperator {

    /**
     * transparent white for void (NaN) values.
     */
    public static final int VOID_COLOR = 0x00000000;

    /**
     * precomputed components of light vector with length one
     */
    private final double lx;
    private final double ly;
    private final double lz;

    /**
     * Three-dimensional normal vector.
     */
    private class Vector3D {

        public final double nx;
        public final double ny;
        public final double nz;

        public Vector3D(double nx, double ny, double nz) {
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
        }
    }

    /**
     * Creates a new instance
     *
     * @param progressIndicator Progress indicator that will be periodically
     * updated.
     */
    public ShadingOperator() {
        double azimuth = Math.toRadians(315);
        double zenith = Math.toRadians(45);
        double sinz = Math.sin(zenith);
        lx = Math.sin(azimuth) * sinz;
        ly = Math.cos(azimuth) * sinz;
        lz = Math.cos(zenith);
    }

    @Override
    protected void operateValue(Grid src, Grid dst, int col, int row) {
        int nCols = src.getCols();
        int nRows = src.getRows();

        if (row == 0 || row == nRows - 1 || col == 0 || col == nCols - 1) {
            dst.setValue(0, col, row);
        } else {
            Vector3D n = normal(src, col, row);

            // compute the dot product of the normal and the light vector. This
            // results in a value between -1 (surface faces directly away from
            // light) and 1 (surface faces directly toward light).
            double dotProduct = n.nx * lx + n.ny * ly + n.nz * lz;

            // scale dot product from [-1, +1] to a gray value in [0, 1]
            double gray = Math.max(Math.min((dotProduct + 1) / 2, 1), 0);

            dst.setValue(gray, col, row);
        }
    }

    /**
     * Computes a normal vector on a grid with length 1.
     *
     * @param grid grid
     * @param col column
     * @param row row
     * @return normal vector with length 1.
     */
    private Vector3D normal(Grid grid, int col, int row) {
        if (col == 0 || row == 0 || col == grid.getCols() - 1 || row == grid.getRows() - 1) {
            return new Vector3D(0f, 0f, 1f);
        }

        // the size of a grid cell
        double cellSize = grid.getProjectedCellSize(row);

        // z coordinate of normal vector
        double nz = 2 * cellSize;

        double w = grid.getValue(col - 1, row);
        double e = grid.getValue(col + 1, row);
        double s = grid.getValue(col, row + 1);
        double n = grid.getValue(col, row - 1);
        double dx = e - w;
        double dy = n - s;

        // normal vector at col/row
        double nx = -dx;
        double ny = -dy;
        double nL = (float) Math.sqrt(dx * dx + dy * dy + nz * nz);
        nx /= nL;
        ny /= nL;
        nz /= nL;
        
        return new Vector3D(nx, ny, nz);
    }

    @Override
    public String getName() {
        return "Shading";
    }

}

package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.grid.MinMax;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Searches minimum and maximum values in a grid.
 *
 * @author Bernie Jenny, Faculty of Information Technology, Monash University.
 */
public final class MinMaxOperator extends ThreadedGridOperator {

    private float min = Float.NaN;
    private float max = Float.NaN;

    /**
     * Constructor
     */
    public MinMaxOperator() {
    }

    /**
     * Constructor
     *
     * @param progressIndicator
     */
    public MinMaxOperator(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    public float findMin(Grid grid) {
        return findMinMax(grid).min;
    }
    
    public float findMax(Grid grid) {
        return findMinMax(grid).max;
    }
    
    /**
     * Returns the minimum and the maximum values in a grid. The progress
     * indicator is checked for cancellation, but not sent any progress
     * information or modified in any other way.
     *
     * @param grid grid to scan for minimum and maximum values.
     * @return an array with two elements, first the minimum, then the maximum.
     */
    public MinMax findMinMax(Grid grid) {
        boolean initialIgnoreProgress = false;
        try {
            if (progressIndicator != null) {
                initialIgnoreProgress = progressIndicator.isIgnoreProgress();
                progressIndicator.setIgnoreProgress(true);
            }

            // cannot initialise with first grid value as it might be NaN.
            min = Float.MAX_VALUE;
            max = -Float.MAX_VALUE;
            super.operate(grid, null);
            if (min == Float.MAX_VALUE) {
                min = max = Float.NaN; // all grid values are NaN or Inf
            }
            return new MinMax(min, max);
        } finally {
            min = max = Float.NaN;

            if (progressIndicator != null) {
                progressIndicator.setIgnoreProgress(initialIgnoreProgress);
            }
        }
    }

    /**
     * Do not call this method.
     *
     * @param src
     * @return
     */
    @Override
    protected Grid initDestinationGrid(Grid src) {
        throw new IllegalStateException("grid allocation for MinMaxOperator");
    }

    /**
     * Do not call this method.
     *
     * @param src
     * @return
     */
    @Override
    public Grid operate(Grid src) {
        throw new UnsupportedOperationException();
    }

    /**
     * * Do not call this method.
     *
     * @param src
     * @param dst
     * @return
     */
    @Override
    public Grid operate(Grid src, Grid dst) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void threadedOperate(Grid src, Grid ignore, int startRow, int endRow) {
        // cannot initialise with first grid value as it might be NaN.
        float localMin = Float.MAX_VALUE;
        float localMax = -Float.MAX_VALUE;

        final int nCols = src.getCols();
        for (int row = startRow; row < endRow; ++row) {
            if (reportProgress(startRow, endRow, row) == false) {
                return;
            }
            for (int col = 0; col < nCols; ++col) {
                float val = src.getValue(col, row);
                if (val > localMax) {
                    localMax = val;
                }
                if (val < localMin) {
                    localMin = val;
                }
            }
        }

        // use localMin and localMax for this thread to avoid repeated 
        // synchronised access to min and max fields
        synchronized (this) {
            if (localMin < min) {
                min = localMin;
            }
            if (localMax > max) {
                max = localMax;
            }
        }
    }

    @Override
    public String getName() {
        return "Find mimimum and maximum";
    }

}

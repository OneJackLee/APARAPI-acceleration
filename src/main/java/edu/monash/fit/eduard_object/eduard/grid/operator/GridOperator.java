/*
 * GridOperator.java
 *
 * Created on January 28, 2006, 2:10 PM
 *
 */
package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * A GridOperator performs an operation on a grid and normally stores the result
 * in a new grid.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class GridOperator {

    /**
     * Flag for logging to standard output.
     */
    public static final boolean LOG_TO_STD_OUT = true;

    /**
     * Progress indicator that needs to be updated periodically and be checked
     * for user cancellation.
     */
    protected ProgressIndicator progressIndicator;

    /**
     * time when operate() was last called. Used for logging information.
     */
    private volatile long startTimeNanoSeconds = -1L;

    /**
     * Constructor
     */
    protected GridOperator() {
    }

    /**
     * Constructor
     *
     * @param progressIndicator indicate progress in GUI and check for cancel
     * events.
     */
    protected GridOperator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    /**
     * Returns a descriptive name of this GridOperator
     *
     * @return The name of this GridOperator.
     */
    public abstract String getName();

    private static int printIndent = 0;

    /**
     * Prints operator name and start time to standard output stream.
     *
     * @return true to allow this to be called from an assert statement.
     */
    private boolean printLogStart() {
        if (printIndent > 0) {
            System.out.println();
        }
        for (int i = 0; i < printIndent; i++) {
            System.out.print("\t");
        }
        System.out.print(getName() + ": ");
        if (isCancelled()) {
            System.out.print(" CANCELLED AT START");
        }
        ++printIndent;
        return true;
    }

    /**
     * Prints execution time to standard output stream.
     *
     * @return true to allow this to be called from an assert statement.
     */
    private boolean printLogEnd() {
        if (isCancelled()) {
            System.out.println(" CANCELLED AT END");
        }
        long endTimeNanoSeconds = System.nanoTime();
        long durationMilliSec = (endTimeNanoSeconds - startTimeNanoSeconds) / 1000L / 1000L;
        System.out.println(" " + durationMilliSec + " ms");
        --printIndent;
        return true;
    }

    /**
     * Log start of operation in standard output stream.
     */
    protected void logStart() {
        if (LOG_TO_STD_OUT && startTimeNanoSeconds == -1L) {
            printLogStart();
            startTimeNanoSeconds = System.nanoTime();
        }
    }

    /**
     * Log end of operation in standard output stream.
     */
    protected void logEnd() {
        if (LOG_TO_STD_OUT && startTimeNanoSeconds != -1L) {
            printLogEnd();
            startTimeNanoSeconds = -1L;
        }
    }

    /**
     * Set the progress indicator to indicate progress in GUI and check for
     * cancel events.
     *
     * @param progressIndicator progress indicator to set
     */
    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    /**
     * Set message displayed by progress indicator.
     *
     * @param msg the message
     */
    protected void setProgressIndicatorMessage(String msg) {
        if (progressIndicator != null) {
            progressIndicator.setMessage(msg);
        }
    }

    /**
     * Returns whether the user cancelled the operation.
     *
     * @return true if the operation should be cancelled.
     */
    protected boolean isCancelled() {
        if (progressIndicator == null) {
            return false;
        }
        return progressIndicator.isCancelled();
    }

    /**
     * Update the value displayed by the progress indicator.
     *
     * @param percentage progress value between 0 and 100. Value can be outside
     * of this range.
     * @return true if the operation should continue, false if the operation
     * should be cancelled.
     */
    protected boolean reportProgress(float percentage) {
        if (progressIndicator == null) {
            return true;
        }
        if (progressIndicator.isCancelled()) {
            return false;
        } else if (progressIndicator.isIgnoreProgress()) {
            return true;
        } else {
            int p = Math.round(percentage);
            p = Math.min(100, Math.max(0, p));
            return progressIndicator.progress(p);
        }
    }

    /**
     * Apply the operator and store the result in a new grid that is returned.
     *
     * @param src The grid to operate on.
     * @return A new grid containing the result. The resulting grid may be of a
     * different size than the passed grid. It is null if the operator is
     * cancelled.
     */
    public Grid operate(Grid src) {
        try {
            logStart();
            Grid dst = initDestinationGrid(src);
            return operate(src, dst);
        } finally {
            logEnd();
        }
    }

    /**
     * Apply the operator and store the result in the passed destination grid.
     *
     * @param src The source grid.
     * @param dst The destination grid.
     * @return The passed dst grid is returned. Null is returned if the operator is
     * cancelled.
     */
    public abstract Grid operate(Grid src, Grid dst);

    /**
     * Creates a new grid that will store the results of the operator.
     *
     * This method creates a new grid of the same size as the source grid. It
     * must be overridden if the derived operator generates a grid that has a
     * different dimension or position than the source grid, or if no
     * destination grid is needed (for example, when an image is generated), or
     * if more than one grids are needed to store results. In these cases null
     * can be returned.
     *
     * @param src the source grid.
     * @return the new grid of the same size and position as the source grid.
     *
     */
    protected Grid initDestinationGrid(Grid src) {
        if (src == null) {
            throw new IllegalArgumentException(getName() + ": source grid is null");
        }
        if (!src.isWellFormed()) {
            throw new IllegalStateException(getName() + ": invalid source grid: " + src.toString());
        }
        return Grid.shallowCopy(src);
    }

    @Override
    public String toString() {
        return getName();
    }
}

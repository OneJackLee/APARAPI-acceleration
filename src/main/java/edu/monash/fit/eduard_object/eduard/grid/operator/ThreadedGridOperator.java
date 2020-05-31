package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.utils.ThreadUtils;
import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A base class for asynchronous multi-threaded grid operators. It uses as many
 * threads for operating on the grid as CPU processor cores are available. The
 * grid is split into equally sized chunks, and each thread operates on one
 * chunk.
 * <p>
 * Memory consistency effects: Subclasses are designed to be used with an
 * ExecutorService, which will ensure that the threaded operator will see all
 * actions taken prior to the submission of the task (i.e. actions of the calling
 * thread <i>happen-before</i> actions of the asynchronous operator), and that
 * actions taken by the operator will be seen by the calling thread after
 * calling {@code Future.get()} (i.e. operator actions
 * <i>happen-before</i> subsequent actions by the calling thread). This means
 * that ThreadedGridOperators do not need to make changes to Grids explicitly
 * visible by using volatile array access (via VarHandle or other mechanisms).
 * For more information about publishing/visibility see
 * <a href="package-summary.html#MemoryVisibility">java.util.concurrent package
 * documentation</a>.
 *
 * @author Bernie Jenny, Faculty of Information Technology, Monash University
 */
public abstract class ThreadedGridOperator extends GridOperator {

    /**
     * number of virtual cores
     */
    private static final int nbrThreads =Runtime.getRuntime().availableProcessors();

    /**
     * A thread pool shared among all ThreadedGridOperators to avoid expensive
     * creation of threads for each operation. This ExcecutorService is also
     * essential for publishing changes made by the asynchronous operator.
     */
    private static final ExecutorService threadPool
            = Executors.newFixedThreadPool(nbrThreads);

    /**
     * Constructor
     *
     * @param progressIndicator
     */
    protected ThreadedGridOperator() {
    }

    /**
     * Constructor
     *
     * @param progressIndicator indicate progress in GUI and check for cancel
     * events.
     */
    protected ThreadedGridOperator(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    /**
     * Update the value displayed by the progress indicator.
     *
     * @param startRow The first row of the chunk processed.
     * @param endRow The last row of the chunk processed.
     * @param row The current row being processed.
     * @return True if the operation should continue, false if the operation
     * should be cancelled.
     */
    protected boolean reportProgress(int startRow, int endRow, int row) {
        // report progress if this is the thread working on the first chunk of the grid
        if (startRow == 0) {
            float percentage = 100f * row / (endRow - startRow - 1);
            return reportProgress(percentage);
        }
        // for other threads, check whether the operator should be cancelled.
        return (progressIndicator == null) ? true : !progressIndicator.isCancelled();
    }

    /**
     * Operate on a chunk of the passed source grid and store the result in the
     * passed destination grid. This is called in parallel from multiple
     * threads. Subclasses must not change any grid value in {@code dst} outside
     * of the range indicated by startRow and endRow.
     *
     * @param src The source grid.
     * @param dst The destination grid.
     * @param startRow The index of the first row to operate on.
     * @param endRow The index of the last row. Derived classes must not operate
     * on this row. It may be equal to {@code src.getRows()}.
     */
    protected void threadedOperate(Grid src, Grid dst, int startRow, int endRow) {
        for (int row = startRow; row < endRow; row++) {
            if (reportProgress(startRow, endRow, row) == false) {
                return;
            }
            operateRow(src, dst, row);
        }
    }

    /**
     * Operate on a row of the passed source grid and store the result in the
     * passed destination grid. This can be called in parallel from multiple
     * threads for different rows. Subclasses must only change grid value in
     * {@code dst} of the indicated row.
     *
     * @param src The source grid.
     * @param dst The destination grid.
     * @param row The row to operate on.
     */
    protected void operateRow(Grid src, Grid dst, int row) {
        for (int col = 0, nCols = src.getCols(); col < nCols; col++) {
            operateValue(src, dst, col, row);
        }
    }

    /**
     * Operate on a single grid value. This can be called in parallel from
     * multiple threads for different grid values. If possible, subclasses
     * should override this method instead of the other {@code operateXYZ}
     * methods.
     *
     * @param src The source grid.
     * @param dst The destination grid.
     * @param col Column of the value.
     * @param row Cow of the value.
     */
    protected void operateValue(Grid src, Grid dst, int col, int row) {
    }

    /**
     * {@inheritDoc }
     * <p>
     * If this method is overridden, the overriding class needs to call this
     * method for multi-threading with super.operate(src, dst).
     * <p>
     * This method can be overridden for per-thread initialization, but
     * overriding methods must make sure all fields are visible to worker
     * threads. This can be achieved by making private fields immutable or
     * volatile, and ensuring that fields in referenced objects are also visible
     * to worker threads.
     *
     * @param src
     * @param dst
     * @return
     */
    @Override
    public Grid operate(Grid src, Grid dst) {
        try {
            logStart();
            Objects.requireNonNull(src, getName() + ": source grid is null");

            if (!src.isWellFormed()) {
                throw new IllegalStateException(getName() + ": grid is not well formed");
            }

            // number of rows that one task will operate on
            int nRows = src.getRows();
            int rowChunk = (nRows / nbrThreads) + 1;
            // ensure chunk size is even, as required by some operators
            rowChunk += rowChunk % 2 == 1 ? 1 : 0;

            // number of tasks cannot be greater than the number of rows of the grid
            int nbrTasks = Math.min(nbrThreads, nRows);

            ArrayList<Future> futures = new ArrayList<>(nbrTasks);
            ArrayList<Task> tasks = new ArrayList<>(nbrTasks);

            // create tasks, start them and add them to the futures array
            for (int i = 0; i < nbrTasks; i++) {
                int startRow = i * rowChunk;
                int endRow = Math.min(nRows, startRow + rowChunk);
                Task task = new Task(src, dst, startRow, endRow);
                tasks.add(task);
                Future f = threadPool.submit(task);
                futures.add(f);
            }

            // wait for all tasks to complete
            try {
                for (Future future : futures) {
                    future.get();
                }

                // give each task a chance to clean up, or treat border pixels
                // that cannot be handled in parallel mode
                for (Task task : tasks) {
                    task.cleanup();
                }
            } catch (InterruptedException ex) {
                // user cancelled
                return null;
            } catch (ExecutionException ex) {
                // ExecutionException wraps anything thrown by the task code
                throw ThreadUtils.launderThrowable(ex);
            }

            return dst;
        } finally {
            logEnd();
        }
    }

    /**
     * Called in main thread after threadedOperate() has completed for all
     * threads.
     *
     * @param src The source grid.
     * @param dst The destination grid.
     * @param startRow The index of the first row to operate on.
     * @param endRow The index of the last row. Derived classes should not
     * operate on this row. It may be equal to src.getRows().
     */
    protected void finalizeThreadedOperate(Grid src, Grid dst, int startRow, int endRow) {
    }

    /**
     * A task is run by the thread pool and operates on a chunk of a grid.
     */
    private class Task implements Runnable {

        private final Grid srcGrid;
        private final Grid dstGrid;
        private final int startRow;
        private final int endRow;

        private Task(
                Grid srcGrid,
                Grid dstGrid,
                int startRow,
                int endRow) {
            this.srcGrid = srcGrid;
            this.dstGrid = dstGrid;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public void run() {
            threadedOperate(srcGrid, dstGrid, startRow, endRow);
        }

        private void cleanup() {
            finalizeThreadedOperate(srcGrid, dstGrid, startRow, endRow);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

}

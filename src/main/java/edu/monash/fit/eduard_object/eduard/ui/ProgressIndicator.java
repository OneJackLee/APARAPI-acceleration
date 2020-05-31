/*
 * ProgressIndicator.java
 *
 * Created on August 15, 2006, 12:30 PM
 *
 */
package edu.monash.fit.eduard_object.eduard.ui;

/**
 * ProgressIndicator defines the methods that an object must implemented to
 * serve as progress indicator.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface ProgressIndicator {

    /**
     * startProgress() should be called when the operation starts.
     */
    public void startProgress();

    /**
     * complete() is called to inform that the operation terminated. The GUI
     * will be cleaned up.
     */
    public void closeGUI();

    /**
     * progress() informs of the progress of the current task.
     *
     * @param percentage A value between 0 and 100
     * @return True if the operation should continue, false if the user cancelled
     * the operation.
     */
    public boolean progress(int percentage);
    
    /**
     * If ignoreProgress is true, progress updates via progress() will be ignored.
     *
     * @param ignoreProgress whether to ignore updates via progress()
     */
    public void setIgnoreProgress(boolean ingoreProgress);

    /**
     * Returns whether progress updates via progress() are ignored.
     * @return true if updates are ignored, false otherwise.
     */
    public boolean isIgnoreProgress();
    
    /**
     * Set progress GUI status to indeterminate, i.e. the duration of the task
     * is unknown and calls to progress() will not update the progress
     * indicator.
     *
     * @param indeterminate
     */
    public void setIndeterminate(boolean indeterminate);
    
    /**
     * Return whether the user cancelled the operation, e.g. by pressing a Cancel
     * button.
     *
     * @return True if cancelled.
     */
    public boolean isCancelled();

    /**
     * Enable or disable button to cancel the operation.
     *
     * @param cancellable If true, the button is enabled. Default is true.
     */
    public void setCancellable(boolean cancellable);

    /**
     * Display a message to the user. The message can change regularly. HTML is
     * legal.
     *
     * @param msg
     */
    public void setMessage(final String msg);

    /**
     * Sets the number of tasks. Each task has a progress between 0 and 100. If
     * the number of tasks is 2, progress of task 1 will be rescaled to 0..50.
     *
     * @param tasksCount The total number of tasks.
     */
    public void setTasksCount(int tasksCount);

    /**
     * Returns the total numbers of tasks for this progress indicator.
     *
     * @return The total numbers of tasks.
     */
    public int getTasksCount();

    /**
     * Switch to the next task.
     */
    public void nextTask();

    /**
     * Returns the ID of the current task. The first task has ID 1 (and not 0).
     *
     * @return The ID of the current task.
     */
    public int currentTask();
}

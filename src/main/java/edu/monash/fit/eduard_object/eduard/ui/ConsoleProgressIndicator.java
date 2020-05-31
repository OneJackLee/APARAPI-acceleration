package edu.monash.fit.eduard_object.eduard.ui;

/**
 * Progress indicator that writes to the standard output. For development and
 * debugging only.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public class ConsoleProgressIndicator implements ProgressIndicator {

    private int lastProgressPercentage = -1;

    /**
     * The number of tasks to execute. The default is 1.
     */
    private int tasksCount = 1;

    /**
     * The ID of the current task.
     */
    private int currentTask = 1;

    @Override
    public void startProgress() {
    }

    @Override
    public void closeGUI() {
    }

    @Override
    public boolean progress(int percentage) {
        percentage = Math.min(100, Math.max(0, percentage));
        percentage = (int) Math.round((double) percentage / tasksCount + (currentTask - 1d) * 100d / tasksCount);
        if (percentage > lastProgressPercentage) {
            if (percentage % 5 == 0) {
                System.out.println(percentage+ "%");
            }
            lastProgressPercentage = percentage;
        }
        return true;
    }

    @Override
    public void setIgnoreProgress(boolean ingoreProgress) {
    }

    @Override
    public boolean isIgnoreProgress() {
        return false;
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancellable(boolean cancellable) {
    }

    @Override
    public void setMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void setTasksCount(int tasksCount) {
        this.tasksCount = tasksCount;
    }

    @Override
    public int getTasksCount() {
        return tasksCount;
    }

    @Override
    public void nextTask() {
        ++currentTask;
    }

    @Override
    public int currentTask() {
        return currentTask;
    }

}

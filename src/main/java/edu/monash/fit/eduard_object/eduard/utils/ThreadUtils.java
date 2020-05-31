/*
 * SwingThreadUtils.java
 *
 * Created on December 3, 2006, 6:01 PM
 *
 */
package edu.monash.fit.eduard_object.eduard.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Utility methods for threads.
 *
 */
public class ThreadUtils {

    /**
     * A wrapper around SwingUtilities.invokeAndWait() that makes sure that
     * SwingUtilities.invokeAndWait() is only called when the current thread is
     * not the AWT event dispatching thread, as required by the documentation of
     * SwingUtilities.invokeAndWait(); plus catches exceptions thrown by
     * SwingUtilities.invokeAndWait().
     *
     * @param runnable The Runnable to call in the event dispatch thread.
     */
    public static void invokeAndWait(Runnable runnable) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeAndWait(runnable);
            }
        } catch (InvocationTargetException | InterruptedException ex) {
            Logger.getLogger(ThreadUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Coerce an unchecked Throwable to a RuntimeException
     * <p/>
     * If the Throwable is an Error, throw it; if it is a RuntimeException
     * return it, otherwise throw IllegalStateException
     *
     * @author Brian Goetz and Tim Peierls, Java Concurrency in Practice,
     * Listing 5.13.
     *
     */
    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("Not unchecked", t);
        }
    }
}

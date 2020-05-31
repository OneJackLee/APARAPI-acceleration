package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;

/**
 * Low-pass filter very close to Gaussian blur but faster, see
 * {@link AbstractFrequencyOperator}.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public final class LowPassOperator extends AbstractFrequencyOperator {

    /**
     * Constructor
     *
     * @param sigma standard deviation
     * @param progressIndicator progress indicator
     */
    public LowPassOperator(double sigma, ProgressIndicator progressIndicator) {
        super(sigma, progressIndicator);
    }

    @Override
    public Grid operate(Grid src, Grid dst) {
        super.operate(src, dst);
        return new CopyVoidOperator(progressIndicator).operate(src, dst);
    }

    @Override
    public String getName() {
        return "Low-pass";
    }
}

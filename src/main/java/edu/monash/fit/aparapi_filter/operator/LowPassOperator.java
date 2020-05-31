package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;

/**
 * Low-pass filter which applies Gaussian blur
 * {@link HorizontalTransposingLowPassFilter}.
 *
 * [REFERENCE: Eduard LowPassOperator class]
 */
public class LowPassOperator implements AparapiOperator {
    private final float FLOAT_MAX = Float.MAX_VALUE, FLOAT_VOID = Float.NaN;    // FLOAT library attribute
    double timer;
    Grid src;           // the source grid
    float sigmaValue;   // sigma value holder

    /**
     * Constructor
     * @param sigma standard deviation
     */
    public LowPassOperator(float sigma){
        this.sigmaValue = sigma;
        if (sigma < 0) {
            throw new IllegalArgumentException("negative sigma");
        }
    }

    @Override
    public Grid operate(Grid src) {
        Grid postProcessingGrid;
        this.src = src;
        if (src == null){
            throw new NullPointerException("");
        }

        MaskFilter.benchmarking.add("Low-pass:");

        /*
        execute HorizontalTransposingLowPassFilter two times to blur the image
         */
        AparapiOperator horizontalTransposingFirstPass = new HorizontalTransposingLowPassFilter(true, this.sigmaValue);
        Grid transposedGrid = horizontalTransposingFirstPass.operate(this.src);
        MaskFilter.benchmarking.add("\tHorizontal transposing 1D low-pass filter:  " + horizontalTransposingFirstPass.getTimer() + " ms");

        AparapiOperator horizontalTransposingSecondPass = new HorizontalTransposingLowPassFilter(false, this.sigmaValue);
        postProcessingGrid = horizontalTransposingSecondPass.operate(transposedGrid);
        MaskFilter.benchmarking.add("\tHorizontal transposing 1D low-pass filter:  " + horizontalTransposingSecondPass.getTimer() + " ms");

        MaskFilter.benchmarking.add(" " + ((horizontalTransposingFirstPass.getTimer() + horizontalTransposingSecondPass.getTimer()))+ " ms");

        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = postProcessingGrid.getBuffer();

        // consistency ensure
        Kernel kernel = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                if (!isFinite(srcBuffer[i]))
                    destBuffer[i] = FLOAT_VOID;

            }

            public boolean isFinite(float f){
                return Math.abs(f) <= FLOAT_MAX;
            }
        };


        kernel.setExplicit(true);           // explicitly manage transfers between GPU memory and CPU
        kernel.put(srcBuffer);              // send srcBuffer to GPU
        kernel.put(destBuffer);             // send destBuffer to GPU

        kernel.execute(1);                  // generate a KernelRunner run instantly, to eagerly create it

        timer = System.nanoTime();
        kernel.execute(Range.create(src.getCols() * src.getRows()));
        timer = System.nanoTime() - timer;
        timer = timer/ 1000000;
        MaskFilter.benchmarking.add("Copy void:  " + timer + " ms");

        kernel.get(destBuffer);             // fetch destBuffer from GPU
        kernel.dispose();                   // clean kernel when finish job
        postProcessingGrid.setBufferReceived(destBuffer);

        return postProcessingGrid;
    }

    @Override
    public double getTimer(){
        return timer;
    }

}

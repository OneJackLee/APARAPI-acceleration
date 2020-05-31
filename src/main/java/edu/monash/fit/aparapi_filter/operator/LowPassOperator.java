package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;

public class LowPassOperator implements AparapiOperator {
    private final float FLOAT_MAX = Float.MAX_VALUE, FLOAT_VOID = Float.NaN;
    double timer, transposingTimer;
    Grid src;
    Grid dest;
    float sigmaValue;

    public LowPassOperator(float sigma){
        this.sigmaValue = sigma;

    }

    @Override
    public Grid operate(Grid src) {
        Grid postProcessingGrid;
        this.src = src;

        MaskFilter.benchmarking.add("Low-pass:");

        AparapiOperator horizontalTransposingFirstPass = new HorizontalTransposingLowPassFilter(true, this.sigmaValue);
        Grid transposedGrid = horizontalTransposingFirstPass.operate(this.src);
        MaskFilter.benchmarking.add("\tHorizontal transposing 1D low-pass filter:  " + horizontalTransposingFirstPass.getTimer() + " ms");

        AparapiOperator horizontalTransposingSecondPass = new HorizontalTransposingLowPassFilter(false, this.sigmaValue);
        postProcessingGrid = horizontalTransposingSecondPass.operate(transposedGrid);
        MaskFilter.benchmarking.add("\tHorizontal transposing 1D low-pass filter:  " + horizontalTransposingSecondPass.getTimer() + " ms");

        MaskFilter.benchmarking.add(" " + ((horizontalTransposingFirstPass.getTimer() + horizontalTransposingSecondPass.getTimer()))+ " ms");

//        postProcessingGrid = transposedGrid;

        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = postProcessingGrid.getBuffer();

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


        kernel.setExplicit(true); /////
        kernel.put(srcBuffer);
        kernel.put(destBuffer);

        kernel.execute(1);

        timer = System.nanoTime();
        kernel.execute(Range.create(src.getCols() * src.getRows()));
        timer = System.nanoTime() - timer;
        timer = timer/ 1000000;
        MaskFilter.benchmarking.add("Copy void:  " + timer + " ms");

        kernel.get(destBuffer);
        kernel.dispose();
        postProcessingGrid.setBufferReceived(destBuffer);

        return postProcessingGrid;
    }

    public double getTimer(){
        return timer;
    }

}

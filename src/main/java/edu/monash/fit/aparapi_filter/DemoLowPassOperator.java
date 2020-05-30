package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.Arrays;

public class DemoLowPassOperator implements AparapiOperator {
    private final float FLOAT_MAX = Float.MAX_VALUE, FLOAT_VOID = Float.NaN;


    Grid src;
    Grid dest;
    float sigmaValue;

    public DemoLowPassOperator(float sigma){
        this.sigmaValue = sigma;

    }

    @Override
    public Grid operate(Grid src, Grid dest) {
        return null;
    }

    @Override
    public Grid operate(Grid src) {
        this.src = src;
        Grid transposedGrid = new HorizontalTransposingLowPassFilter(true, this.sigmaValue).operate(this.src);
        Grid postProcessingGrid = new HorizontalTransposingLowPassFilter(false, this.sigmaValue).operate(transposedGrid);
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

        kernel.execute(1);
        kernel.execute(Range.create(src.getCols() * src.getRows()));
        kernel.dispose();
        postProcessingGrid.setBufferReceived(destBuffer);

        return postProcessingGrid;
    }
}

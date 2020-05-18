package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class MaskFilter {
    private final static float slopeThresholdDeg = 6f, slopeThreshold = (float) Math.tan(Math.toRadians(slopeThresholdDeg));
    private final static float sigmaBlur = 6f, relativeGain = 0.5f, sigmaSmooth = 20f;
    private Grid src;
    private Grid dest;
    private final float FLOAT_MAX = Float.MAX_VALUE;


    public MaskFilter(Grid src, Grid dest){
        this.src = src;
        this.dest = dest;
    }

    public void execute(){
        float gainSlopeThresholdDeg = slopeThresholdDeg * Math.min(0.995f, relativeGain);
        float gainSlopeThreshold = (float) Math.tan(Math.toRadians(gainSlopeThresholdDeg));
        float scale = 1f / (slopeThreshold - gainSlopeThreshold);

        new GradientOperator().operate(src, dest);
        // LowPassOperator
        // ClampToRangeOperator

        Kernel maskFilter = new OperatorKernel(src, dest){
            @Override
            public void run() {
                int i = getGlobalId();
                float value = srcBuffer[i];
                if (isFinite(value)){
                    value = 1 - (value - gainSlopeThreshold) * scale;
                    value = Math.max(0, Math.min(1, value));
                } else{
                    value = -1;
                }
                destBuffer[i] = value;
            }

            public boolean isFinite(float f){
                return Math.abs(f) <= FLOAT_MAX;
            }
        };

        maskFilter.execute(Range.create(dest.getLength()));
        maskFilter.dispose();


    }

    public void GradientOperatorExecute(){

    }



}

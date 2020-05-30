package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.operator.DemoClampToRangeOperator;
import edu.monash.fit.aparapi_filter.operator.GradientOperator;

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

    public Grid execute(){
        float gainSlopeThresholdDeg = slopeThresholdDeg * Math.min(0.995f, relativeGain);
        float gainSlopeThreshold = (float) Math.tan(Math.toRadians(gainSlopeThresholdDeg));
        float scale = 1f / (slopeThreshold - gainSlopeThreshold);

        int srcCols = src.getCols();
        int srcRows = src.getRows();

        dest = new GradientOperator().operate(src);

        dest = new DemoLowPassOperator(sigmaBlur).operate(dest);

        dest = new DemoClampToRangeOperator(gainSlopeThreshold, slopeThreshold).operate(dest);

        dest = new DemoLowPassOperator(sigmaSmooth).operate(dest);


        float[] newSrcBuffer = dest.getBuffer();
        float[] newDestBuffer = new float[srcCols * srcRows];
        Kernel maskFilter = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();
                float value = newSrcBuffer[i];
                if (isFinite(value)){
                    value = 1 - (value - gainSlopeThreshold) * scale;
                    value = Math.max(0, Math.min(1, value));
                } else{
                    value = -1;
                }
                newDestBuffer[i] = value;
            }

            public boolean isFinite(float f){
                return Math.abs(f) <= FLOAT_MAX;
            }

        };

        maskFilter.setExplicit(true);
        maskFilter.put(newSrcBuffer);
        maskFilter.put(newDestBuffer);
        maskFilter.execute(1);
        maskFilter.execute(Range.create(dest.getLength()));
        maskFilter.get(newDestBuffer);
        maskFilter.dispose();

        dest.setBufferReceived(newDestBuffer);
        return dest;
    }




}

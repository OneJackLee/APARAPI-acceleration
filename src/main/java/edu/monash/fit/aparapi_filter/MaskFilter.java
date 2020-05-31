package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.operator.AparapiOperator;
import edu.monash.fit.aparapi_filter.operator.ClampToRangeOperator;
import edu.monash.fit.aparapi_filter.operator.GradientOperator;
import edu.monash.fit.aparapi_filter.operator.LowPassOperator;

import java.util.ArrayList;

public class MaskFilter {
    private final static float slopeThresholdDeg = 6f, slopeThreshold = (float) Math.tan(Math.toRadians(slopeThresholdDeg));
    private final static float sigmaBlur = 6f, relativeGain = 0.5f, sigmaSmooth = 20f;
    private final Grid src;
    private Grid dest;
    private final float FLOAT_MAX = Float.MAX_VALUE;

    public static ArrayList<String> benchmarking = new ArrayList<>();

    double startTime;


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
        AparapiOperator gradientOperator = new GradientOperator();
        dest = gradientOperator.operate(src);

        AparapiOperator lowPassOperatorSigmaBlur = new LowPassOperator(sigmaBlur) ;
        dest = lowPassOperatorSigmaBlur.operate(dest);

        AparapiOperator clampToRangeOperator = new ClampToRangeOperator(gainSlopeThreshold, slopeThreshold);
        dest = clampToRangeOperator.operate(dest);

        AparapiOperator lowPassOperatorSigmaSmooth = new LowPassOperator(sigmaSmooth);
        dest =lowPassOperatorSigmaSmooth.operate(dest);

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
        startTime = System.nanoTime();

        maskFilter.execute(Range.create(dest.getLength()));
        double timerMaskFilter = (System.nanoTime() - startTime)/1000000;

        maskFilter.get(newDestBuffer);
        maskFilter.dispose();
        MaskFilter.benchmarking.add("Slope to normalized mask:  " + timerMaskFilter + " ms");

        dest.setBufferReceived(newDestBuffer);
        return dest;
    }




}

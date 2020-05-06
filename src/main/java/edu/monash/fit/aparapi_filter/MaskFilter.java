package edu.monash.fit.aparapi_filter;

import com.aparapi.*;

public class MaskFilter {
    private float[] bufferReceived;
    private int cols, rows;
    private final static float slopeThreshold = (float) Math.tan(Math.toRadians(6f));
    private final static float sigmaBlur = 6f, relativeGain = 0.5f, sigmaSmooth = 20f;


    public MaskFilter(float[] bufferReceived, int cols, int rows){
        this.bufferReceived = bufferReceived;
        this.cols = cols;
        this.rows = rows;
    }

    public void execute(){
        // gradient operator

        // ... //
    }

    public float[] result(){
        return bufferReceived;
    }



}

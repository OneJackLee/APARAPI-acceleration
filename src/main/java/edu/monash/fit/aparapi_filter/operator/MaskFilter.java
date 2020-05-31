package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;

import java.util.ArrayList;

/**
 * Blurred slope values are now between gainSlopeThreshold and slopeThreshold.
 * Scale all slope values from [gainSlopeThreshold..slopeThreshold] to [0..1].
 * Inverted mapping of slopeThreshold to 0 and gainSlopeThreshold to 1 after invoke all operator
 *
 * [REFERENCE: Eduard Main class MaskFilter method]
 */
public class MaskFilter {
    // Pixels with a slope equal or smaller than localScaleSlopeDeg are set to
    // a mask value of 0 (before blurring, smoothing, and gain are applied).
    // Value in decimal degrees.
    private final static float slopeThresholdDeg = 6f, slopeThreshold = (float) Math.tan(Math.toRadians(slopeThresholdDeg));
    // Amount of bluring for grid with slope values.
    // Gain value to control "strength" of the mask, between 0 and 1.
    // Amount of bluring for rise/run slope values after clamping.
    private final static float sigmaBlur = 6f, relativeGain = 0.5f, sigmaSmooth = 20f;
    private final Grid src;                                 // input grid
    private Grid dest;                                      // result grid
    private final float FLOAT_MAX = Float.MAX_VALUE;        // maximum valve of the float [float library not supported ]

    public static ArrayList<String> benchmarking = new ArrayList<>();   // list to store benchmark details

    double startTime;

    /**
     * the constructor
     * @param src source grid
     */
    public MaskFilter(Grid src){
        this.src = src;
        this.dest = Grid.shallowCopy(src);              // shallow copy of src
    }

    public Grid execute(){
        // Compute gain slope threshold in degrees. Threshold is between 0 and slopeThresholdDeg.
        float gainSlopeThresholdDeg = slopeThresholdDeg * Math.min(0.995f, relativeGain);
        // convert to rise/run
        float gainSlopeThreshold = (float) Math.tan(Math.toRadians(gainSlopeThresholdDeg));
        // scale the slopeThreshold and gainSlopeThreshold
        float scale = 1f / (slopeThreshold - gainSlopeThreshold);

        int srcCols = src.getCols();
        int srcRows = src.getRows();
        // compute grid with dimensionless rise/run slope values
        AparapiOperator gradientOperator = new GradientOperator();
        dest = gradientOperator.operate(src);

        // low pass filter to blur grid
        AparapiOperator lowPassOperatorSigmaBlur = new LowPassOperator(sigmaBlur) ;
        dest = lowPassOperatorSigmaBlur.operate(dest);

        // clamp the range of the mountaineous area
        AparapiOperator clampToRangeOperator = new ClampToRangeOperator(gainSlopeThreshold, slopeThreshold);
        dest = clampToRangeOperator.operate(dest);

        // low pass filter to blur edges again
        AparapiOperator lowPassOperatorSigmaSmooth = new LowPassOperator(sigmaSmooth);
        dest =lowPassOperatorSigmaSmooth.operate(dest);

        float[] newSrcBuffer = dest.getBuffer();                // get buffer
        float[] newDestBuffer = new float[srcCols * srcRows];   // initialize new buffer

        // Inverted mapping of slopeThreshold to 0 and gainSlopeThreshold to 1.
        Kernel maskFilter = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();
                float value = newSrcBuffer[i];
                if (isFinite(value)){
                    value = 1 - (value - gainSlopeThreshold) * scale;
                    value = Math.max(0, Math.min(1, value)); // clamp to valid range
                } else{
                    value = -1; // invalid value
                }
                newDestBuffer[i] = value;
            }

            // method copied as APARAPI not support object method call
            private boolean isFinite(float f){
                return Math.abs(f) <= FLOAT_MAX;
            }

        };

        maskFilter.setExplicit(true);               // explicitly manage transfers between GPU memory and CPU
        maskFilter.put(newSrcBuffer);                   // send srcBuffer to GPU
        maskFilter.put(newDestBuffer);                  // send destBuffer to GPU
        maskFilter.execute(1);
        startTime = System.nanoTime();

        maskFilter.execute(Range.create(dest.getLength()));     // kernel execution
        double timerMaskFilter = (System.nanoTime() - startTime)/1000000;

        maskFilter.get(newDestBuffer);
        maskFilter.dispose();
        MaskFilter.benchmarking.add("Slope to normalized mask:  " + timerMaskFilter + " ms");

        dest.setBufferReceived(newDestBuffer);
        return dest;
    }




}

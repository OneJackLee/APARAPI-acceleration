package edu.monash.fit.aparapi_filter;
import com.aparapi.Kernel;
import com.aparapi.Range;

public class MaskFilter {
    private final static float slopeThreshold = (float) Math.tan(Math.toRadians(6f));
    private final static float sigmaBlur = 6f, relativeGain = 0.5f, sigmaSmooth = 20f;
    private Grid grid;


    public MaskFilter(Grid grid){
        this.grid = grid;
    }

    public void execute(){

    }

    public void GradientOperatorExecute(){
        Kernel gradientOperator = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();

            }
        };

    }



}

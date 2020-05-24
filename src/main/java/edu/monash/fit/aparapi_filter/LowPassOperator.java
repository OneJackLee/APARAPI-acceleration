package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class LowPassOperator implements AparapiOperator {
    Grid src;
    Grid dest;
    double sigmaI;
    private static final int ITERATIONS = 4;


    public LowPassOperator(double sigma){
        this.sigmaI = sigma;
    }

    @Override
    public void operate(Grid src, Grid dest) {
        this.src = src;
        this.dest = dest;
        double sigma = sigmaI;
        if (sigma < 0) {
            throw new IllegalArgumentException("negative sigma");
        }
        int srcCols = src.getCols();
        int srcRows = src.getRows();
        double srcNorth = src.getNorth();
        double srcCellSizes = src.getCellSize();
        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = dest.getBuffer();

        int r = calculateR(sigma);
        double alpha = calculateAlpha(sigma, r);
        float c1 = calculateC1(sigma, r, alpha);
        float c2 = calculateC2(sigma, r, alpha);

        boolean firstPass = true;

        Kernel horizontalTransposingLowPassFilter = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                boolean foundVoid = false;



            }
        };
//        horizontalTransposingLowPassFilter.execute(Range.create(1));
//        horizontalTransposingLowPassFilter.dispose();



    }

    private float calculateC1(double sigma, int r, double alpha){
        return (float) (alpha / (2.0 * (alpha + r) + 1));
    }

    private float calculateC2(double sigma, int r, double alpha) {
        return (float) ((1.0 - alpha) / (2.0 * (alpha + r) + 1));
    }

    private int calculateR(double sigma){
        return (int) (0.5 * Math.sqrt((12.0 * sigma * sigma) / ITERATIONS + 1.0) - 0.5);
    }

    private double calculateAlpha(double sigma, int r){
        return (2 * r + 1) * (r * (r + 1) - 3.0 * sigma * sigma / ITERATIONS)
                / (6.0 * (sigma * sigma / ITERATIONS - (r + 1) * (r + 1)));
    }

}

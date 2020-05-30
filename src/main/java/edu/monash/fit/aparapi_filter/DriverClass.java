package edu.monash.fit.aparapi_filter;

import com.aparapi.*;
import com.aparapi.device.Device;

public class DriverClass {

    private static final int nbrThreads =Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        Grid input = new Grid(3, 7, 0.5, 0.6, 0.8, 0.8, 0.8);
//        System.out.println(input.getDirectIndex(2, 3));
//        System.out.println(input.getCol(11)+ " " + input.getRow(11));
        input.fillWithRandomFloat();
        Grid output = new Grid(3, 7, 0.5, 0.6, 0.8, 0.8, 0.8);

        input.set(Float.MAX_VALUE, 0);
        System.out.println("available " + nbrThreads); // print the selected mode
        System.out.println(">>>>>>>>>>>>" + ((int) (input.getRows() / nbrThreads) + 1) );

        System.out.println(Device.bestGPU());

        for (float i: input.getBuffer())
            System.out.println(i);

        output = new MaskFilter(input, output).execute();

        System.out.println("");

        for (float i: output.getBuffer())
            System.out.println("output: " +i);

        new HorizontalTransposingLowPassFilter(true, 6f).operate(input);
    }

}

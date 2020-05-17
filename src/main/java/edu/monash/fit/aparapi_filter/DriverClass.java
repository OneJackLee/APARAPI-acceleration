package edu.monash.fit.aparapi_filter;

import com.aparapi.*;

public class DriverClass {
    public static void main(String[] args) {
        Grid input = new Grid(3, 4, 0.5, 0.6, 0.8);
        System.out.println(input.getDirectIndex(2, 3));
        System.out.println(input.getCol(11)+ " " + input.getRow(11));
        input.fillWithRandomFloat();
        Grid output = new Grid(3, 4, 0.5, 0.6, 0.8);

        for (float i: input.getBuffer())
            System.out.println(i);

        new GradientOperator().operate(input, output);




//        Kernel

    }

}

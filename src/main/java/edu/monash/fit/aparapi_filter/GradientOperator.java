package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;

public class GradientOperator implements AparapiOperator {
    @Override
    public void operate(Grid src, Grid dest) {
        Kernel kernel = new Kernel(){
            @Override
            public void run() {

            }
        };

    }

    @Override
    public void operateValue(Grid src, Grid dest, int col, int row) {

    }
}

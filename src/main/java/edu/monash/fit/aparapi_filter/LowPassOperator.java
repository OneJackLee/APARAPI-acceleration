package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;

public class LowPassOperator implements AparapiOperator {
    @Override
    public void operate(Grid src, Grid dest) {
        // when making new instance Grid, is west included
        float[] srcGrid = src.getBuffer();
        int rows = src.getRows();
        //int nbrThreads
            //is there threads in Aparapi/GPU ?


        float[] dstGrid = dest.getBuffer();

        boolean isWellFormed =  src.getCols() > 0
                    && src.getRows() > 0
                    && src.getCellSize() > 0
                    && !Double.isNaN(src.getWest()) // why no get west
                    && !Double.isNaN(src.getNorth());

        // if isWellFormed false, send error Illegal StateException
        //otherwise cont..





        Kernel kernel = new Kernel(){
            @Override
            public void run() {

            }
        };

    }

}

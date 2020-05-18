package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;

public abstract class OperatorKernel extends Kernel {

    Grid src;
    Grid dest;

    float[] srcBuffer, destBuffer;
    int srcCols, srcRows;
    double srcNorth, srcCellSizes;

    public OperatorKernel(Grid src, Grid dest){
        this.src = src;
        this.dest = dest;

        srcBuffer = src.getBuffer();
        destBuffer = dest.getBuffer();

        srcCols = src.getCols();
        srcRows = src.getRows();
        srcNorth = src.getNorth();
        srcCellSizes = src.getCellSize();
    }

//    public int getRow(int directIndex){
//        return directIndex / srcCols;
//    }
//
//    public int getCol(int directIndex){
//        return directIndex % srcCols;
//    }
//
//    public int getDirectIndex(int col, int row){
//        return col + row * srcCols;
//    }
//
//    public float[] getDestBuffer(){
//        return destBuffer;
//    }

}

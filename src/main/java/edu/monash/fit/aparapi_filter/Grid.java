package edu.monash.fit.aparapi_filter;

public class Grid {
    private final float[] bufferReceived;
    private int cols, rows;
    private double cellSize, north, south;

//    private final static float slopeThreshold = (float) Math.tan(Math.toRadians(6f));
//    private final static float sigmaBlur = 6f, relativeGain = 0.5f, sigmaSmooth = 20f;


    public Grid(float[] bufferReceived, int cols, int rows, double cellSize, double north, double south){
        this.bufferReceived = bufferReceived;
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.north = north;
        this.south = south;
    }

    public float[] getBuffer(){
        return bufferReceived;
    }

    public float get(int col, int row) {
        return bufferReceived[col + row * cols];
    }

    public void set(float value, int col, int row) {
        bufferReceived[col + row * cols] = value;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public double getCellSize() {
        return cellSize;
    }

    public void setCellSize(double cellSize) {
        this.cellSize = cellSize;
    }
}

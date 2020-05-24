package edu.monash.fit.aparapi_filter;

import com.aparapi.*;
import java.util.Random;

public class Grid {
    private float[] bufferReceived;
    private int cols, rows;
    private double cellSize, north, south;
    private double east, west;

    public Grid(float[] bufferReceived, int cols, int rows, double cellSize, double north, double south){
        this.bufferReceived = bufferReceived;
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.north = north;
        this.south = south;
    }

    public Grid(float[] bufferReceived, int cols, int rows, double cellSize,
                double north, double south, double east, double west){
        this.bufferReceived = bufferReceived;
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }


    public Grid(int cols, int rows, double cellSize, double north, double south){
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.north = north;
        this.south = south;
        this.bufferReceived = new float[this.cols * this.rows];
    }

    public Grid(int cols, int rows, double cellSize, double north, double south, double east, double west){
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.bufferReceived = new float[this.cols * this.rows];
    }


    public void fillWithRandomFloat(){
        Random random = new Random();
        float max = 4000;
        float min = 1000;
        for(int i = 0; i <(  getCols() * getRows()); i++){
            this.set(random.nextFloat() * (max - min) + min, i);
        }
    }

    public void fillWithZero(){
        Random random = new Random();
        for(int i = 0; i <(  getCols() * getRows()); i++){
            this.set(0f, i);
        }
    }

    public int getLength(){
        return getCols() * getRows();
    }

    public void setBufferReceived(float[] bufferReceived){
        this.bufferReceived = bufferReceived;
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

    public float get(int directIndex){
        return bufferReceived[directIndex];
    }

    public void set(float value, int directIndex){
        bufferReceived[directIndex] = value;
    }

    public int getRow(int directIndex){
        return directIndex / getCols();
    }

    public int getCol(int directIndex){
        return directIndex % getCols();
    }

    public int getDirectIndex(int col, int row){
        return col + row * cols;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
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

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }


    public double getProjectedCellSize(int row) {
        // TODO replace with a better test to detect geographic coordinate systems that also uses the extent of the grid
        if (cellSize < 0.1) {
            double latRad = Math.toRadians(getNorth() - row * cellSize);
            double latitudeCorrectedCellSizeRad = Math.cos(latRad) * Math.toRadians(cellSize);
            return latitudeCorrectedCellSizeRad * 6_371_007; // radius of authalic sphere for GRS 1980
        }
        return cellSize;
    }

    public float get8NeighborGradient(int col, int row) {
        final float projectedCellSize = (float) getProjectedCellSize(row);
        final float cellSizeTimes8 = 8 * projectedCellSize;

        final int cols = getCols();
        final int rows = getRows();

        final int colLeft = col > 0 ? col - 1 : 0;
        final int colRight = col < cols - 1 ? col + 1 : cols - 1;
        final int rowTop = row > 0 ? row - 1 : 0;
        final int rowBottom = row < rows - 1 ? row + 1 : rows - 1;

        final float a = get(colLeft, rowTop);
        final float b = get(col, rowTop);
        final float c = get(colRight, rowTop);
        final float d = get(colLeft, row);

        final float f = get(colRight, row);
        final float g = get(colLeft, rowBottom);
        final float h = get(col, rowBottom);
        final float i = get(colRight, rowBottom);

        final float dZdX = ((c + (2 * f) + i) - (a + (2 * d) + g)) / cellSizeTimes8;
        final float dZdY = ((g + (2 * h) + i) - (a + (2 * b) + c)) / cellSizeTimes8;
        return (float) Math.sqrt((dZdX * dZdX) + (dZdY * dZdY));
    }


}

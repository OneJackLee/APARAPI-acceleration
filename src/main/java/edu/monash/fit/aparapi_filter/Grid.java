package edu.monash.fit.aparapi_filter;

import java.util.Random;

/**
 * The modified Grid class
 * The base idea is from the Eduard Grid class, however as the Raster only deal with float value
 * thus, some of the unused method and attributes removed.
 */
public class Grid {
    private float[] bufferReceived;         // hold the float grid value
    private int cols, rows;                 // total columns and rows
    private double cellSize, north, south;  // cell size, north and south latitude
    private double east, west;              // east and west longitude

    /**
     * Default constructor which can pass in the buffer received
     * @param bufferReceived the buffer float array
     * @param cols total columns
     * @param rows total rows
     * @param cellSize cellsize for each raster grid
     * @param north north latitude
     * @param south south latitude
     * @param east east longitude
     * @param west west longitude
     */
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

    /**
     * Default constructor, automatically create the holder buffer
     * @param cols total columns
     * @param rows total rows
     * @param cellSize cellsize for each raster grid
     * @param north north latitude
     * @param south south latitude
     * @param east east longitude
     * @param west west longitude
     */
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

    /**
     *  create a Grid object with the attribute of the toCopy Grid (but not the bufferArray)
     * @param toCopy Grid object
     * @return new grid object
     */
    public static Grid shallowCopy(Grid toCopy){
        return new Grid(toCopy.getCols(), toCopy.getRows(), toCopy.getCellSize(), toCopy.getNorth(),
                toCopy.getSouth(), toCopy.getEast(), toCopy.getWest());
    }

    /**
     * fill the array with random float value
     */
    public void fillWithRandomFloat(){
        Random random = new Random();
        float max = 4000;
        float min = 2500;
        for(int i = 0; i <(  getCols() * getRows()); i++){
            this.set(random.nextFloat() * (max - min) + min, i);
        }
    }

    /**
     * fill the array with 0
     */
    public void fillWithZero(){
        Random random = new Random();
        for(int i = 0; i <(  getCols() * getRows()); i++){
            this.set(0f, i);
        }
    }

    /**
     * get the length of the array
     * @return the length of the array
     */
    public int getLength(){
        return getCols() * getRows();
    }

    /**
     * Setter of the buffer received
     * @param bufferReceived float array
     */
    public void setBufferReceived(float[] bufferReceived){
        this.bufferReceived = bufferReceived;
    }

    /**
     * Getter of the buffer received
     * @return float buffer array
     */
    public float[] getBuffer(){
        return bufferReceived;
    }

    /**
     * getter of the value in the array (1d representation)
     * @param col column representation
     * @param row row representation
     * @return grid value from the array
     */
    public float get(int col, int row) {
        return bufferReceived[col + row * cols];
    }

    /**
     * setter of the value in the array (1d representation)
     * @param value the value to be set into the perspective cell
     * @param col column representation
     * @param row row representation
     */
    public void set(float value, int col, int row) {
        bufferReceived[col + row * cols] = value;
    }

    /**
     * getter of the value in the array (direct access)
     * @param directIndex the direct index representation
     * @return value from the array
     */
    public float get(int directIndex){
        return bufferReceived[directIndex];
    }

    /**
     * setter of the value in the array (direct access)
     * @param value the value to be set into the perspective cell
     * @param directIndex the direct index representation
     */
    public void set(float value, int directIndex){
        bufferReceived[directIndex] = value;
    }

    /**
     * translate the direct index representation to row representation
     * @param directIndex the direct index representation
     * @return row representation
     */
    public int getRow(int directIndex){
        return directIndex / getCols();
    }

    /**
     * translate the direct index representation to column representation
     * @param directIndex the direct index representation
     * @return column representation
     */
    public int getCol(int directIndex){
        return directIndex % getCols();
    }

    /**
     * translate the column and row representation (2d) to direct index representation
     * @param col column representation
     * @param row row representation
     * @return directIndex the direct index representation
     */
    public int getDirectIndex(int col, int row){
        return col + row * cols;
    }

    /**
     * get the total rows in the array
     * @return total rows in the array
     */
    public int getRows() {
        return rows;
    }

    /**
     * setter of the total rows in the array
     * @param rows total rows
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * get the total columns in the array
     * @return total columns in the array
     */
    public int getCols() {
        return cols;
    }

    /**
     * setter of the total columns in the array
     * @param cols total columns
     */
    public void setCols(int cols) {
        this.cols = cols;
    }

    /**
     * getter of the cellsize
     * @return cellsize
     */
    public double getCellSize() {
        return cellSize;
    }

    /**
     * setter of the cellsize
     * @param cellSize size of each cell
     */
    public void setCellSize(double cellSize) {
        this.cellSize = cellSize;
    }

    /**
     * get south latitude
     * @return south latitude
     */
    public double getSouth() {
        return south;
    }

    /**
     * set south latitude
     * @param south latitude
     */
    public void setSouth(double south) {
        this.south = south;
    }

    /**
     * get north latitude
     * @return north latitude
     */
    public double getNorth() {
        return north;
    }

    /**
     * set north latitude
     * @param north latitude
     */
    public void setNorth(double north) {
        this.north = north;
    }

    /**
     * get east longitude
     * @return east longitude
     */
    public double getEast() {
        return east;
    }

    /**
     * set east longitude
     * @param east longitude
     */
    public void setEast(double east) {
        this.east = east;
    }

    /**
     * get west longitude
     * @return west longitude
     */
    public double getWest() {
        return west;
    }

    /**
     * set west longitude
     * @param west longitude
     */
    public void setWest(double west) {
        this.west = west;
    }


}

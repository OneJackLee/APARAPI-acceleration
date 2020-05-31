package edu.monash.fit.eduard_object.eduard.grid;

/**
 * A GridBuffer stores grid values. Implementing classes can use float, short or
 * other primitive value types. The internal layout of the buffer and the way
 * void values are handled are private implementation details. Implementations
 * of GridBuffer should not provide access to their grid buffer.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public interface GridBuffer {

    /**
     * Returns an exact deep copy of this GridBuffer.
     * @return A new grid buffer. 
     */
    GridBuffer copy();
    
    /**
     * Returns the number of columns in the grid.
     *
     * @return The number of columns in the grid.
     */
    int getCols();

    /**
     * Returns the number of rows in the grid.
     *
     * @return The number of rows in the grid.
     */
    int getRows();

    /**
     * Returns the value at a specified position in the grid.
     *
     * @param col The vertical column for which a value is returned.
     * @param row The horizontal row for which a value is returned.
     * @return The value at the specified position.
     */
    float get(int col, int row);
    
    /**
     * Returns the value at a specified position in the grid.
     *
     * @param id Position in the grid: id = col+ row * getCols()
     * @return The value at the specified position.
     */
    float get(int id);

    /**
     * Sets a value in the grid.
     *
     * @param value The value to store in the grid.
     * @param col The vertical column for which a value is set.
     * @param row The horizontal row for which a value is set.
     */
    void set(float value, int col, int row);

    /**
     * Returns true if the value at col/row is void.
     *
     * @param col column
     * @param row row
     * @return true if cell at col/row is void, false otherwise.
     */
    boolean isVoid(int col, int row);
    
    /**
     * Returns true if the value at a specified position is void.
     *
     * @param id Position in the grid: id = col+ row * getCols()
     * @return true if cell at col/row is void, false otherwise.
     */
    boolean isVoid(int id);

    /**
     * Void a value in the grid.
     *
     * @param col The vertical column for which a value is voided.
     * @param row The horizontal row for which a value is voided.
     */
    void setVoid(int col, int row);
    
}

package edu.monash.fit.eduard_object.eduard.grid;

/**
 * GridBuffer with 4-bytes float values.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public final class FloatBuffer implements GridBuffer {

    private final float[] buffer;

    private final int cols, rows;

    /**
     * Constructor.
     *
     * @param cols number of horizontal columns
     * @param rows number of vertical rows
     */
    public FloatBuffer(int cols, int rows) {
        if (cols < Grid.MIN_DIM || rows < Grid.MIN_DIM) {
            throw new IllegalArgumentException();
        }
        buffer = new float[cols * rows];
        this.cols = cols;
        this.rows = rows;
    }

    /**
     * Copy constructor.
     *
     * @param b A new instance with all grid values copied.
     */
    public FloatBuffer(FloatBuffer b) {
        // arrays are best copied by clone: J. Bloch, Effective Java, 3rd ed. p. 65.
        buffer = b.buffer.clone();
        cols = b.cols;
        rows = b.rows;
    }
    
    @Override
    public FloatBuffer copy() {
        return new FloatBuffer(this);
    }

    @Override
    public int getCols() {
        return cols;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public float get(int col, int row) {
        return buffer[col + row * cols];
    }
    
    @Override
    public float get(int id) {
        return buffer[id];
    }

    @Override
    public void set(float value, int col, int row) {
        buffer[col + row * cols] = value;
    }

    @Override
    public boolean isVoid(int col, int row) {
        return isVoid(col + row * cols);
    }

    @Override
    public boolean isVoid(int id) {
        return !Float.isFinite(get(id));
    }
    
    @Override
    public void setVoid(int col, int row) {
        set(Float.NaN, col, row);
    }

    @Override
    public String toString() {
        return "FloatBuffer: columns=" + cols + ", rows=" + rows;
    }

    public float[] getBuffer(){
        return buffer;
    }

}

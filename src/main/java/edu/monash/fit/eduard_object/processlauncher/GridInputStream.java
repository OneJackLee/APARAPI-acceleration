package edu.monash.fit.eduard_object.processlauncher;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import java.io.IOException;
import java.io.InputStream;

/**
 * A binary stream that returns grid values. Cell values are converted to a
 * binary stream, from the top row to the bottom row, from left to right. Each
 * cell value is written as a four-bytes floating-point value according to the
 * IEEE 754 floating-point "single format" bit layout. Values can be NaN to
 * indicate void cells. The values use big-endian encoding.
 *
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public class GridInputStream extends InputStream {

    private final Grid grid;

    private int nbrBytesRead;

    /**
     * Constructor
     *
     * @param grid Grid to convert to binary stream. Only cell values are
     * streamed.
     */
    public GridInputStream(Grid grid) {
        this.grid = grid;
        nbrBytesRead = 0;
    }

    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream has been reached.
     */
    @Override
    public synchronized int read() {
        int arrayPos = nbrBytesRead / 4;
        int nbrGridCells = grid.getCols() * grid.getRows();
        if (arrayPos < nbrGridCells) {
            float f = grid.getValue(arrayPos);
            int i = Float.floatToIntBits(f);
            switch (nbrBytesRead++ % 4) {
                case 0:
                    return i & 0xFF;
                case 1:
                    return (i >>> 8) & 0xFF;
                case 2:
                    return (i >>> 16) & 0xFF;
                case 3:
                    return (i >>> 24) & 0xFF;
            }
        }
        return -1;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer bytes
     * might be skipped if the end of the input stream is reached. The actual
     * number <code>k</code> of bytes to be skipped is equal to the smaller of
     * <code>n</code> and  <code>count-pos</code>. The value <code>k</code> is
     * added into <code>pos</code> and <code>k</code> is returned.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    @Override
    public synchronized long skip(long n) {
        throw new UnsupportedOperationException("skip not supported");
    }

    /**
     * Returns the number of remaining bytes that can be read (or skipped over)
     * from this input stream.
     * <p>
     * The value returned is <code>count&nbsp;- pos</code>, which is the number
     * of bytes remaining to be read from the input buffer.
     *
     * @return the number of remaining bytes that can be read (or skipped over)
     * from this input stream without blocking.
     */
    @Override
    public synchronized int available() {
        return grid.getCols() * grid.getRows() * 4 - nbrBytesRead;
    }

    /**
     * Tests if this <code>InputStream</code> supports mark/reset. The
     * <code>markSupported</code> method of <code>ByteArrayInputStream</code>
     * always returns <code>true</code>.
     *
     * @since JDK1.1
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Set the current marked position in the stream. ByteArrayInputStream
     * objects are marked at position zero by default when constructed. They may
     * be marked at another position within the buffer by this method.
     * <p>
     * If no mark has been set, then the value of the mark is the offset passed
     * to the constructor (or 0 if the offset was not supplied).
     *
     * <p>
     * Note: The <code>readAheadLimit</code> for this class has no meaning.
     *
     * @since JDK1.1
     */
    @Override
    public void mark(int readAheadLimit) {
        throw new UnsupportedOperationException("mark not supported");
    }

    /**
     * Resets the buffer to the marked position. The marked position is 0 unless
     * another position was marked or an offset was specified in the
     * constructor.
     */
    @Override
    public synchronized void reset() {
        throw new UnsupportedOperationException("reset not supported");
    }

    /**
     * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     */
    @Override
    public void close() throws IOException {
    }

}

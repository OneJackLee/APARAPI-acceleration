package edu.monash.fit.eduard_object.eduard.grid.exporter;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.grid.importer.WKTProjectionFile;
import edu.monash.fit.eduard_object.eduard.grid.operator.MinMaxOperator;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract base class for grid exporters.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class GridExporter {

    /**
     * A ProgressIndicator that is displayed during a long export. The
     * ProgressIndicator must be set using setProgressIndicator(). The default
     * value is null.
     */
    protected ProgressIndicator progressIndicator = null;

    protected GridExporter() {
    }

    /**
     * Returns the file extension of the main file created by this exporter.
     *
     * @return The file extension.
     */
    public abstract String getFileExtension();

    /**
     * Returns a short string that can be used to construct a string for a file
     * selection dialog of the form "Save xyz file".
     *
     * @return The name of the format.
     */
    public abstract String getFileFormatName();

    /**
     * Returns whether this exporter can export the passed grid. For example,
     * certain formats can only export grids up to a certain size.
     *
     * @param grid The grid to exportToFile.
     * @return True if the grid can be exported, false otherwise.
     */
    public boolean canExport(Grid grid) {
        return true;
    }

    /**
     * The ProgressIndicator currently used. Can be null.
     *
     * @return the progress indicator or null
     */
    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    /**
     * The ProgressIndicator currently used. Can be null.
     *
     * @param progressIndicator the progressIndicator to set
     */
    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    /**
     * Export a grid to a file.
     *
     * @param grid the grid to exportToFile
     * @param filePath path to file
     * @throws IOException throws an exception of a file-related error occurs
     */
    public void exportToFile(Grid grid, String filePath) throws IOException {

        File file = new File(filePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            writeGridAndCloseStream(grid, fileOutputStream);
            
            if (this instanceof EsriASCIIGridExporter) {
                WKTProjectionFile.write(grid.getPrjFileContent(), filePath);
            }
        } catch (Exception exc) {
            // Delete the file if an exception is thrown. Before the file can be 
            // deleted, the output stream needs to be closed.
            closeQuietly(fileOutputStream);
            fileOutputStream = null;
            if (file.exists()) {
                file.delete();
            }
            throw exc;
        } finally {
            closeQuietly(fileOutputStream);

            // delete the file if the user cancelled the operation
            if (progressIndicator != null
                    && progressIndicator.isCancelled()) {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    // https://stackoverflow.com/questions/12096002/close-a-file-created-with-fileoutputstream-for-a-next-delete
    private void closeQuietly(FileOutputStream out) {
        try {
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Exports a Grid to an output stream. Derived classes can overwrite this
     * method to initialize themselves, but must call export().
     *
     * @param grid The grid to exportToFile.
     * @param outputStream The destination stream that will receive the result.
     * This stream will be closed when this method returns. This outputStream is
     * wrapped in a BufferedOutputStream, so overriding methods do not need to
     * create another buffered stream.
     */
    public void writeGridAndCloseStream(Grid grid, OutputStream outputStream)
            throws IOException {

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(outputStream);
            export(grid, bos);
        } finally {
            // make sure the passed stream is closed even when exportToFile() trows an exception
            if (bos != null) {
                try {
                    bos.close();
                } catch (Throwable ignore) {
                }
            } else {
                try {
                    outputStream.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    /**
     * Writes a grid to an output stream.
     *
     * Note: Derived classes writing character data should either not use a
     * PrintWriter, because a PrintWriter does not throw an exception when an
     * error occurs, or should regularly use PrintWriter.checkError().
     *
     * @param grid The grid to exportToFile.
     * @param outputStream The stream to exportToFile to. If a derived class
     * wraps this stream in an additional stream, it is responsible for closing
     * that additional stream.
     * @throws java.io.IOException
     */
    protected abstract void export(Grid grid, BufferedOutputStream bos)
            throws IOException;

    /**
     * Update progress indicator.
     *
     * @param row current row between 0 and grid.getRows() - 1
     * @param grid grid that is being written
     * @return if false, stop exportToFile.
     */
    protected boolean rowProgress(int row, Grid grid) {
        int perc = (int) Math.round(100d * row / (grid.getRows() - 1));
        return progressIndicator == null ? true : progressIndicator.progress(perc);
    }

    @Override
    public String toString() {
        return getFileFormatName();
    }

    /**
     * Returns a negative value consisting of nines only that is smaller than
     * the minimum value in the grid. The largest value returned is -9999. This
     * can be used to encode void values with a value that is guaranteed not to
     * be contained in the grid.
     *
     * @param grid grid
     * @return value that can be used to indicate void values grid.
     */
    protected float findVoidValue(Grid grid) {
        float min = new MinMaxOperator(progressIndicator).findMin(grid);
        String voidValue = "-9999";
        while (Float.parseFloat(voidValue) >= min) {
            voidValue += "9";
        }
        return Float.parseFloat(voidValue);
    }

}

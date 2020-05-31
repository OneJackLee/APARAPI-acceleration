/*
 * ESRIASCIIGridExporter.java
 *
 * Created on August 14, 2005, 4:17 PM
 *
 */
package edu.monash.fit.eduard_object.eduard.grid.exporter;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.ui.ProgressIndicator;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Exporter for Esri ASCII grid file format, optionally with a WKT file.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class EsriASCIIGridExporter extends GridExporter {

    /**
     * Number of decimals for grid values.
     */
    private int nbrDecimals = 3;

    public EsriASCIIGridExporter() {
    }

    /**
     * Utility method for exporting a grid to an Esri ASCII grid file, and a WKT
     * file if the grid has the associated WKT information.
     *
     * @param grid the grid to exportToFile
     * @param filePath path to file
     * @param progressIndicator progress indicator
     * @throws IOException throws an exception of a file-related error occurs
     */
    public static void export(Grid grid, String filePath,
            ProgressIndicator progressIndicator) throws IOException {
        EsriASCIIGridExporter exporter = new EsriASCIIGridExporter();
        exporter.setNbrDecimals(3);
        exporter.setProgressIndicator(progressIndicator);
        exporter.exportToFile(grid, filePath);
    }

    /**
     * Export a grid to an Esri ASCII grid file, and a WKT file if the grid has
     * the associated WKT information.
     *
     * @param grid the grid to exportToFile
     * @param filePath path to file
     * @param nbrDecimals the maximum number of decimals
     * @param progressIndicator progress indicator
     * @throws IOException throws an exception of a file-related error occurs
     */
    @Override
    protected void export(Grid grid, BufferedOutputStream bos) throws IOException {

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(bos, "US-ASCII")))) {

            DecimalFormat df = new DecimalFormat();
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
            df.setMinimumFractionDigits(0);
            df.setMaximumFractionDigits(nbrDecimals);
            df.setGroupingUsed(false);

            float voidValue = findVoidValue(grid);
            String voidValueStr = Float.toString(voidValue);
            String lineSeparator = System.getProperty("line.separator");

            // write header
            writer.write("ncols " + grid.getCols() + lineSeparator);
            writer.write("nrows " + grid.getRows() + lineSeparator);
            writer.write("xllcorner " + grid.getWest() + lineSeparator);
            writer.write("yllcorner " + grid.getSouth() + lineSeparator);
            writer.write("cellsize " + grid.getCellSize() + lineSeparator);
            writer.write("nodata_value " + voidValueStr + lineSeparator);

            // write grid values
            int nRows = grid.getRows();
            int nCols = grid.getCols();
            for (int row = 0; row < nRows && rowProgress(row, grid); ++row) {
                for (int col = 0; col < nCols; ++col) {
                    // after 1000 values are written, check for cancel event and write error
                    if (col % 1000 == 0) {
                        if (progressIndicator != null && progressIndicator.isCancelled()) {
                            return;
                        }

                        // PrintWriter does not throw an exception when an error 
                        // occurs, therefore check for errors with PrintWriter.checkError()
                        if (writer.checkError()) {
                            throw new IOException("could not write grid");
                        }
                    }
                    float v = grid.getValue(col, row);
                    if (Float.isNaN(v)) {
                        writer.write(voidValueStr);
                    } else {
                        writer.write(df.format(v));
                    }
                    writer.write(" ");
                }
                writer.write(lineSeparator);
                writer.flush();
            }
        }
    }

    @Override
    public String getFileExtension() {
        return "asc";
    }

    @Override
    public String getFileFormatName() {
        return "Esri ASCII Grid";
    }

    /**
     * @return the nbrDecimals
     */
    public int getNbrDecimals() {
        return nbrDecimals;
    }

    /**
     * @param nbrDecimals the nbrDecimals to set
     */
    public void setNbrDecimals(int nbrDecimals) {
        this.nbrDecimals = nbrDecimals;
    }

}

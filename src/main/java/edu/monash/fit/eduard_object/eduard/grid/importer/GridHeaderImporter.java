package edu.monash.fit.eduard_object.eduard.grid.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Importer for headers of Esri ASCII grid files.
 *
 * @author Bernie Jenny
 */
public final class GridHeaderImporter {

    private int cols = 0;
    private int rows = 0;
    private double west = Double.NaN;
    private double south = Double.NaN;
    private double cellSize = Double.NaN;
    private float noDataValue = Float.NaN;

    /**
     * Returns whether valid values have been found in the header.
     *
     * @return true if the header has been successfully read and if it contains
     * valid values.
     */
    protected boolean isValid() {
        return cols > 0 && rows > 0 && cellSize > 0 && !Double.isNaN(west) && !Double.isNaN(south);
        // noDataValue is optional
    }

    /**
     * Reads header line-by-line until the first grid line or an unknown header
     * line is encountered.
     *
     * @param reader read from this
     * @return The first grid line or the unknown header line.
     * @throws IOException
     */
    String readHeader(BufferedReader reader) throws IOException {
        cols = rows = 0;
        west = south = cellSize = Double.NaN;
        noDataValue = Float.NaN;
        boolean xCornerCoordinates = false, yCornerCoordinates = false;
        String line;
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, " \t,;");
            String str = tokenizer.nextToken().trim().toLowerCase();
            if (str.equals("ncols")) {
                cols = Integer.parseInt(tokenizer.nextToken());
            } else if (str.equals("nrows")) {
                rows = Integer.parseInt(tokenizer.nextToken());
            } else if (str.equals("xllcenter")) {
                west = Double.parseDouble(tokenizer.nextToken());
            } else if (str.equals("xllcorner")) {
                west = Double.parseDouble(tokenizer.nextToken());
                xCornerCoordinates = true;
            } else if (str.equals("yllcenter")) {
                south = Double.parseDouble(tokenizer.nextToken());
            } else if (str.equals("yllcorner")) {
                south = Double.parseDouble(tokenizer.nextToken());
                yCornerCoordinates = true;
            } else if (str.equals("cellsize")) {
                cellSize = Double.parseDouble(tokenizer.nextToken());
            } else if (str.startsWith("nodata")) {
                noDataValue = Float.parseFloat(tokenizer.nextToken());
            } else {
                // found first grid line or unknonwn header line
                if (xCornerCoordinates) {
                    west += cellSize / 2;
                }
                if (yCornerCoordinates) {
                    south += cellSize / 2;
                }
                return line;
            }
        }

        // this should not be reached: the header is not complete.
        cols = rows = 0;
        west = south = cellSize = Double.NaN;
        return null;
    }

    /**
     * @return the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return the west
     */
    public double getWest() {
        return west;
    }

    /**
     * @return the south
     */
    public double getSouth() {
        return south;
    }

    /**
     * @return the cellSize
     */
    public double getCellSize() {
        return cellSize;
    }

    /**
     * @return the noDataValue
     */
    public float getNoDataValue() {
        return noDataValue;
    }
}

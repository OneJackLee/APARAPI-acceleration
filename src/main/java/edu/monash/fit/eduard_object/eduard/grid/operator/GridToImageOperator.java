package edu.monash.fit.eduard_object.eduard.grid.operator;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.grid.MinMax;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Linear mapping of grid values to a gray scale image. Smallest value is black,
 * greatest value is white. Void values can be shown with a distinctive color.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public final class GridToImageOperator extends ThreadedGridOperator {

    /**
     * Utility method to convert grid to image.
     *
     * @param grid Grid to convert.
     * @param minVal This values is mapped to black.
     * @param maxVal This value is mapped to white.
     * @return Resulting image.
     */
    public static BufferedImage convert(Grid grid, float minVal, float maxVal) {
        return convert(grid, minVal, maxVal, 0x00FFFFFF);
    }
    
    /**
     * Utility method to convert grid to image.
     *
     * @param grid Grid to convert.
     * @param minVal This values is mapped to black.
     * @param maxVal This value is mapped to white.
     * @param voidColor Color for void values.
     * @return Resulting image.
     */
    public static BufferedImage convert(Grid grid, float minVal, float maxVal, int voidColor) {
        GridToImageOperator op = new GridToImageOperator(null, minVal, maxVal);
        op.setVoidColor(voidColor);
        op.operate(grid);
        return op.getImage();
    }

    /**
     * Color for void (NaN) values.
     */
    private int voidColor = 0x00FFFFFF;

    /**
     * Destination image.
     */
    private BufferedImage image;

    /**
     * Pixel buffer of the destination image.
     */
    private int[] imageBuffer;

    /**
     * Minimum and maximum grid value. NaN indicates that this value has not
     * been set and needs to be determined from the grid that is to be
     * converted.
     */
    private MinMax minMax = new MinMax();

    /**
     * Constructor. A destination image of the size of the grid will be created.
     *
     * @param image image to write to. The image must have the same size as the
     * grid.
     * @param minVal this values is mapped to black
     * @param maxVal this value is mapped to white
     */
    public GridToImageOperator(BufferedImage image, float minVal, float maxVal) {
        this.image = image;
        minMax = new MinMax(minVal, maxVal);
    }

    /**
     * Constructor
     *
     * @param image image to write to. The image must have the same size as the
     * grid.
     */
    public GridToImageOperator(BufferedImage image) {
        this.image = image;
    }

    @Override
    public Grid operate(Grid src) {
        if (image == null) {
            image = new BufferedImage(src.getCols(), src.getRows(), BufferedImage.TYPE_INT_ARGB);

        } else {
            if (src.getCols() != image.getWidth() || src.getRows() != image.getHeight()) {
                throw new IllegalStateException("Image and grid size not matching.");
            }
        }
        imageBuffer = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();

        // find min and max if they have not been set
        if (!minMax.isValid()) {
            minMax = new MinMaxOperator(progressIndicator).findMinMax(src);
        }

        return super.operate(src);
    }

    @Override
    protected Grid initDestinationGrid(Grid src) {
        // not creating new grid, as destination is an image
        return null;
    }

    private int cellValueToARGB(float gridVal) {
        float relativeVal = (gridVal - minMax.min) / minMax.range;
        int gray = (int) (255 * relativeVal);
        return (int) gray | ((int) gray << 8) | ((int) gray << 16) | 0xFF000000;
    }

    @Override
    protected void operateValue(Grid src, Grid ignore, int col, int row) {
        final int nCols = image.getWidth();
        final float v = src.getValue(col, row);
        if (Float.isNaN(v)) {
            imageBuffer[row * nCols + col] = voidColor;
        } else {
            final int argb = cellValueToARGB(v);
            imageBuffer[row * nCols + col] = argb; // FIXME this change is potentially not visible to other threads.
        }
    }

    /**
     * Color for void values.
     *
     * @return the ARGB color used for void values
     */
    public int getVoidColor() {
        return voidColor;
    }

    /**
     * Set the color for void values.
     *
     * @param voidColor the ARGB color used for void values
     */
    public void setVoidColor(int voidColor) {
        this.voidColor = voidColor;
    }

    /**
     * Returns the destination image.
     *
     * @return the image
     */
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public String getName() {
        return "Grid to image";
    }
}

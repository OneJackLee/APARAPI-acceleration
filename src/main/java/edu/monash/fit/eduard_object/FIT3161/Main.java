package edu.monash.fit.eduard_object.FIT3161;

import edu.monash.fit.eduard_object.eduard.grid.Grid;
import edu.monash.fit.eduard_object.eduard.grid.exporter.EsriASCIIGridExporter;
import edu.monash.fit.eduard_object.eduard.grid.importer.EsriASCIIGridImporter;
import edu.monash.fit.eduard_object.eduard.grid.operator.*;
import edu.monash.fit.eduard_object.eduard.grid.operator.lic.LineIntegralConvolutionOperator;
import edu.monash.fit.eduard_object.eduard.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sample application for edu.monash.fit.eduard_object.FIT3161 Semester 2 - 2019 to demonstrate reading and
 * writing grid files in Esri ASCII format, filtering a grid, and exporting a
 * shaded image.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public class Main {

    // path to input grid file in Esri ASCII grid file format
//    private static String inputFilePath = null;
    private static String inputFilePath = "C:\\Users\\charl\\Desktop\\git\\Eduard\\map\\Colorado scale series\\Colorado scale series\\Gore_Range_Albers_5m\\Gore_Range_Albers_5m.asc"; // "/Users/jennyb/Documents/Java/FIT3161Eduard/data/Gore_Range_Albers_5m.asc";

    // path to store filtered grid
    private static String outputFilePath = null;

    // path to image with shading of input grid
    private static final String shadingImagePath = null;

    // path to image with shading of filtered grid
    private static final String filteredShadingImagePath = null;

    // path to image of filtered grid
    private static final String filteredImagePath = null;
    
    private static void mainInEventDispatchThread() {
        try {
            // import grid
            if (inputFilePath == null) {
                inputFilePath = FileUtils.askFile(null, "Import Grid", true);
            }
            if (inputFilePath == null) {
                // user canceled
                return;
            }
            System.out.println("Original grid: " + inputFilePath);
            Grid grid = EsriASCIIGridImporter.read(inputFilePath);
            System.out.println(grid.getDescriptionWithStatistics("\n") + "\n");

            // setup filter and apply filter to grid
            System.out.println("Filtering");
            Grid filteredGrid = filter(grid);

            // export filtered grid
            if (outputFilePath == null) {
                String fileName = FileUtils.getFileNameWithoutExtension(
                        inputFilePath);
                fileName += "_filtered.asc";
                outputFilePath = FileUtils.askFile(null, "Export Grid",
                        fileName, false, "asc", null);
            }
            if (outputFilePath == null) {
                // user canceled
                return;
            }
            System.out.println("\nFiltered grid: " + outputFilePath);
            System.out.println(filteredGrid.getDescriptionWithStatistics("\n") + "\n");
            EsriASCIIGridExporter.export(filteredGrid, outputFilePath, null);

            // shade original and filtered grids and export to images
            exportShading(grid, shadingImagePath, inputFilePath);
            exportShading(filteredGrid, filteredShadingImagePath, outputFilePath);
            
            // export filtered grid (without shading) to image
            float[] minMax = filteredGrid.getMinMax();
            BufferedImage image = GridToImageOperator.convert(filteredGrid, 
                    minMax[0], minMax[1]);
            exportImage(image, filteredImagePath, outputFilePath);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.exit(0);
        }
    }

    /**
     * Apply a filter to a grid and return the result in a new grid.
     *
     * @param grid grid to filter
     * @return the filtered grid
     */
    private static Grid filter(Grid grid) {
        // Grid filteredGrid = lowPassFilter(grid);
//        Grid filteredGrid = lineIntegralConvolutionFilter(grid);
        // Grid filteredGrid = mountainFilter(grid);
        Grid filteredGrid = maskFilter(grid);
        return filteredGrid;
    }

    /**
     * Low pass filter
     *
     * @param grid grid to filter
     * @return filtered grid
     */
    private static Grid lowPassFilter(Grid grid) {
        double sigma = 5;
        GridOperator filter = new LowPassOperator(sigma, null);
        Grid filteredGrid = filter.operate(grid);
        return filteredGrid;
    }

    /**
     * Line integral convolution along slope lines.
     *
     * @param grid grid to filter
     * @return filtered grid
     */
    private static Grid lineIntegralConvolutionFilter(Grid grid) {
        // filter is applied this many times
        int iterations = 10;

        // the length of the slope line (in grid cells) along which values are smoothed
        int integrationLineLength = 10;

        // amount of sharpening of mountain ridges (between 0 and 1)
        float sharpening = 0.6f;

        // sharpening is only applied to grid values greater than this relative limit
        // (between 0 and 1)
        float sharpeningLimit = 0.3f;

        GridOperator licOp = new LineIntegralConvolutionOperator(
                integrationLineLength / 2f, sharpening, sharpeningLimit);

        Grid dst = Grid.shallowCopy(grid);

        if (iterations == 1) {
            // filter the grid only once
            licOp.operate(grid, dst);
        } else {
            // filter grid multiple times
            // reuse grids to avoid allocation of a new grid at each iteration
            Grid dstGrid = Grid.shallowCopy(grid);
            new CopyOperator(null).operate(grid, dst);

            for (int i = 1; i <= iterations; i++) {
                licOp.operate(dst, dstGrid);

                // swap grids
                Grid temp = dst;
                dst = dstGrid;
                dstGrid = temp;
            }
        }
        return dst;
    }

    /**
     * The Mountain Sculptor filter is a simplification of the Terrain Sculptor
     * algorithm. This filter only implements the part of the Terrain Sculptor
     * algorithm that this is applied to steep areas, and only uses plan oblique
     * curvature for identifying ridge lines. Terrain Sculptor also uses
     * positive maximum curvature, which was not found to be beneficial.
     *
     * https://github.com/OSUCartography/TerrainSculptor
     *
     * See:
     *
     * Leonowicz, A.M., Jenny, B. and Hurni, L. (2010). Automated reduction of
     * visual complexity in small-scale relief shading. Cartographica, 45-1, p.
     * 64�74.
     *
     * Leonowicz, A.M., Jenny, B. and Hurni, L. (2010) Terrain Sculptor:
     * Generalizing terrain models for relief shading. Cartographic
     * Perspectives, 67, p. 51�60.
     *
     * @param grid grid to filter
     * @return filtered grid
     */
    private static Grid mountainFilter(Grid grid) {

        // low pass blur for removing ridges
        float ridgesRemovalBlurSigma = 3.5f;

        // low pass blur for removing details
        float lodBlurSigma = 4;

        // amount of ridge line information to be added back to the blurred grid
        float ridgesExaggeration = 1.25f;

        // low pass blur for combined plan curvature and maximum curvature.
        float curvatureBlurSigma = 0.9f;

        // filter grid and compute plan curvature
        Grid lowPassGrid = new LowPassOperator(ridgesRemovalBlurSigma, null).operate(grid);
        Grid planCurvature = new PlanCurvatureZevenbergenThorneOperator(null).operate(lowPassGrid);

        // blur curvature grid to remove artefacts along mountain ridges
        new LowPassOperator(curvatureBlurSigma, null).operate(planCurvature, planCurvature);

        // scale curvature to 0..1
        new ScaleToRangeOperator(0, 1, null).operate(planCurvature, planCurvature);

        // smooth original grid; reuse lowPassGrid to avoid allocation of another grid
        new LowPassOperator(lodBlurSigma, null).operate(grid, lowPassGrid);

        // scale ridges in plan curavature grid and add it to the blurred grid
        WeightedScaleOperator wsOp = new WeightedScaleOperator(planCurvature,
                ridgesExaggeration, null);
        Grid filteredGrid = wsOp.operate(lowPassGrid);

        return filteredGrid;
    }

    private static Grid maskFilter(Grid grid) {

        // Pixels with a slope equal or smaller than localScaleSlopeDeg are set to 
        // a mask value of 0 (before blurring, smoothing, and gain are applied). 
        // Value in decimal degrees.     
        float slopeThresholdDeg = 6f;
        // convert from slope in degrees to dimensionless rise/run gradient
        float slopeThreshold = (float) Math.tan(Math.toRadians(slopeThresholdDeg));

        // Amount of bluring for grid with slope values.
        float sigmaBlur = 6f;

        // Gain value to control "strength" of the mask, between 0 and 1.
        float relativeGain = 0.5f;

        // Amount of bluring for rise/run slope values after clamping.
        // This blurring avoids hard transitions at slopeThresholdDeg and the slope
        // defined by relativeGain.
        float sigmaSmooth = 20f;

        // compute grid with dimensionless rise/run slope values instead of slope in 
        // degrees, which would require an expensive atan() operation for each 
        // cell. Results with rise/run are almost identical to results with degrees.
        Grid slopeGrid = new GradientOperator(null).operate(grid);

        // blur  grid with rise/run values
        slopeGrid = new LowPassOperator(sigmaBlur, null).operate(slopeGrid);

        // The combination of a low-pass filter (above) followed by clamping to 
        // slopeThreshold (below) results in mountaineous areas with values equal to 
        // slopeThreshold.
        // Compute gain slope threshold in degrees. Threshold is between 0 and slopeThresholdDeg.
        float gainSlopeThresholdDeg = slopeThresholdDeg * Math.min(0.995f, relativeGain);
        // convert to rise/run
        float gainSlopeThreshold = (float) Math.tan(Math.toRadians(gainSlopeThresholdDeg));

        // clamp slope values to range between gainSlopeThreshold and slopeThreshold
        new ClampToRangeOperator(gainSlopeThreshold, slopeThreshold, null).operate(slopeGrid, slopeGrid);

        // apply another low-pass filter to break sharp edges in the mask that
        // occur around the lower gain slope threshold and the upper slope threshold
        slopeGrid = new LowPassOperator(sigmaSmooth, null).operate(slopeGrid);

        // Blurred slope values are now between gainSlopeThreshold and slopeThreshold.
        // Scale all slope values from [gainSlopeThreshold..slopeThreshold] to [0..1].
        // Inverted mapping of slopeThreshold to 0 and gainSlopeThreshold to 1.
        float scale = 1f / (slopeThreshold - gainSlopeThreshold);
        Grid maskBuffer = new ThreadedGridOperator(null) {
            @Override
            protected void operateValue(Grid src, Grid dst, int col, int row) {
                float v = src.getValue(col, row);
                if (Grid.isValid(v)) {
                    v = 1 - (v - gainSlopeThreshold) * scale;
                    // clamp to valid range. Overshoots can happen with float values!
                    v = Math.max(0, Math.min(1, v));
                } else {
                    v = -1; // negative value indicates void cell
                }
                dst.setValue(v, col, row);
            }

            @Override
            public String getName() {
                return "Slope to normalized mask";
            }
        }.operate(slopeGrid);
        return maskBuffer;
    }

    /**
     * Create shading for a grid and export the shading to a PNG image file.
     *
     * @param grid grid to shade
     * @param filePath The path to the PNG image file. If this is null, the user
     * is asked for a path.
     * @param defaultFileName The file name to use when the user is asked for a
     * file path. This can be a file path.
     * @throws IOException
     */
    private static void exportShading(Grid grid, String filePath,
            String defaultFileName) throws IOException {
        // compute shaded relief image
        Grid filteredGridShaded = new ShadingOperator().operate(grid);
        BufferedImage image = GridToImageOperator.convert(filteredGridShaded, 0, 1);
        exportImage(image, filePath, defaultFileName);
    }
    
    private static void exportImage(BufferedImage image, String filePath,
            String defaultFileName) throws IOException {
        if (filePath == null) {
            String fileName = FileUtils.getFileNameWithoutExtension(
                    defaultFileName) + ".png";
            filePath = FileUtils.askFile(null, "Export Image",
                    fileName, false, "png", null);
        }
        if (filePath == null) {
            // user canceled
            return;
        }
        System.out.println("Image output: " + filePath);
        File file = new File(filePath);
        ImageIO.write(image, "png", file);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainInEventDispatchThread();
            }
        });
    }
}

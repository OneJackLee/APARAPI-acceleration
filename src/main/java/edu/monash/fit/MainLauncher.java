package edu.monash.fit;

import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.operator.MaskFilter;
import edu.monash.fit.eduard_object.eduard.grid.exporter.EsriASCIIGridExporter;
import edu.monash.fit.eduard_object.eduard.grid.importer.EsriASCIIGridImporter;
import edu.monash.fit.eduard_object.eduard.grid.operator.GridToImageOperator;
import edu.monash.fit.eduard_object.eduard.utils.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * MainLauncher of the APARAPI raster filter
 */
public class MainLauncher {

    // path to input grid file. Set to null to show a GUI dialog to select the file
    private static String inputGridFile = null;

    // fileName holder
    private static String fileName = null;

    // path to output grid file. Set to null to show a GUI dialog to select the file
    private static String outputGridFile = null;

    // path to output image file. Set to null to show a GUI dialog to select the file
    private static String outputImageFile = null;

    // path to output benchmark file. Set to null to show a GUI dialog to select the file
    private static String benchmarkingFile = null;

    private static String performanceString = "";

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            try{
                // read input grid file
                if (inputGridFile == null) {
                    inputGridFile = FileUtils.askFile(null, "Import Grid", true);
                }
                if (inputGridFile == null) {
                    JOptionPane.showMessageDialog(null, "The operation has been cancelled. Software ended.");
                    System.exit(0);
                }
                if (inputGridFile != null){
                    fileName = FileUtils.getFileNameWithoutExtension(inputGridFile);
                }
                // create Eduard grid class
                edu.monash.fit.eduard_object.eduard.grid.Grid inputGrid = EsriASCIIGridImporter.read(inputGridFile);

                // translate Eduard grid class into APARAPI grid class
                Grid source = new Grid(inputGrid.getBufferArray(), inputGrid.getCols(), inputGrid.getRows(),
                        inputGrid.getCellSize(), inputGrid.getNorth(), inputGrid.getSouth(),
                        inputGrid.getEast(), inputGrid.getWest());

                // execute the mask filter procedure
                Grid result = new MaskFilter(source).execute();

                // shallow copy of the Eduard inputGrid to create outputGrid
                edu.monash.fit.eduard_object.eduard.grid.Grid outputGrid = edu.monash.fit.eduard_object.eduard.grid.Grid.shallowCopy(inputGrid);

                // translate the Aparapi grid object to Eduard grid object
                for (int row = 0, nRows = outputGrid.getRows(); row < nRows; row++) {
                    for (int col = 0, nCols = outputGrid.getCols(); col < nCols; col++)
                        outputGrid.setValue(result.get(col, row), col, row);
                }
                // get the minimum value and maximum value from the grid matrix(1d array)
                float[] minMax = outputGrid.getMinMax();
                // create a bufferedImage (holder of the raster filter image)
                BufferedImage image = GridToImageOperator.convert(outputGrid,
                        minMax[0], minMax[1]);

                export_grid(outputGrid);    //export grid
                export_image(image);        //export image
                create_benchmark();         // generate benchmark
                export_benchmark();         //create the benchmark file
            }
            catch (Throwable ex){
                JOptionPane.showMessageDialog(null, ex);
                ex.printStackTrace();
                System.exit(-1);
            }
            finally {
                JOptionPane.showMessageDialog(null, "Execution done\n" + performanceString);
                System.exit(0);
            }
        });
    }

    /**
     * export the grid object to the asc file
     * @param outputGrid Eduard grid object
     * @throws IOException file not found
     */
    private static void export_grid(edu.monash.fit.eduard_object.eduard.grid.Grid outputGrid) throws IOException {
        if (outputGridFile == null) {
            outputGridFile = FileUtils.askFile(null, "Export Grid", fileName + "_filtered.asc",false, "asc", null);
        }
        if (outputGridFile != null) {
            EsriASCIIGridExporter.export(outputGrid, outputGridFile, null);
            System.out.println("Exported grid to " + outputGridFile);
        }
    }

    /**
     * export the grid image to the png file
     * @param image BufferImage object
     * @throws IOException file not found
     */
    private static void export_image(BufferedImage image) throws IOException {
        if (outputImageFile == null){
            outputImageFile = FileUtils.askFile(null, "Export Image", fileName + "_filtered.png", false, "png", null);
        }
        if (outputImageFile != null){
            File file = new File(outputImageFile);
            ImageIO.write(image, "png", file);
            System.out.println("Image output to " + outputImageFile);

        }
    }

    /**
     * export the benchmark file
     * @throws IOException file not found
     */
    private static void export_benchmark() throws IOException {
        if (benchmarkingFile == null){
            benchmarkingFile = FileUtils.askFile(null, "Export Benchmark", fileName + "_aparapi.txt", false, "txt", null);
        }
        if (benchmarkingFile != null){
            FileWriter myWriter = new FileWriter(benchmarkingFile);
            myWriter.write(performanceString);
            myWriter.close();
        }
    }

    /**
     * generate the benchmark file
     * @throws IOException file not found
     */
    private static void create_benchmark() {
        performanceString += "APARAPI performance benchmark\n";
        for (String line : MaskFilter.benchmarking)
            performanceString += line + "\n";
    }



    }

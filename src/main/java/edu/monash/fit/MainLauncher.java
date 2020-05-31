package edu.monash.fit;

import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.operator.MaskFilter;
import edu.monash.fit.eduard_object.eduard.grid.exporter.EsriASCIIGridExporter;
import edu.monash.fit.eduard_object.eduard.grid.importer.EsriASCIIGridImporter;
import edu.monash.fit.eduard_object.eduard.grid.operator.GridToImageOperator;
import edu.monash.fit.eduard_object.eduard.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainLauncher {

    // path to input grid file. Set to null to show a GUI dialog to select the file
    private static String inputGridFile = null;

    private static String fileName = null;

    // path to output grid file. Set to null to show a GUI dialog to select the file
    private static String outputGridFile = null;

    private static String outputImageFile = null;

    private static String benchmarkingFile = null;

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try{
                    // read input grid file
                    if (inputGridFile == null) {
                        inputGridFile = FileUtils.askFile(null, "Import Grid", true);
                        fileName = FileUtils.getFileNameWithoutExtension(inputGridFile);
                    }
                    if (inputGridFile == null) {
                        return;
                    }
                    edu.monash.fit.eduard_object.eduard.grid.Grid inputGrid = EsriASCIIGridImporter.read(inputGridFile);
                    Grid source = new Grid(inputGrid.getBufferArray(), inputGrid.getCols(), inputGrid.getRows(),
                            inputGrid.getCellSize(), inputGrid.getNorth(), inputGrid.getSouth(),
                            inputGrid.getEast(), inputGrid.getWest());

                    Grid result = new MaskFilter(source).execute();

                    edu.monash.fit.eduard_object.eduard.grid.Grid outputGrid = edu.monash.fit.eduard_object.eduard.grid.Grid.shallowCopy(inputGrid);
                    for (int row = 0, nRows = outputGrid.getRows(); row < nRows; row++) {
                        for (int col = 0, nCols = outputGrid.getCols(); col < nCols; col++)
                            outputGrid.setValue(result.get(col, row), col, row);
                    }
                    float[] minMax = outputGrid.getMinMax();
                    BufferedImage image = GridToImageOperator.convert(outputGrid,
                            minMax[0], minMax[1]);


                    export_grid(outputGrid);
                    export_image(image);
                    create_benchmark();
                }
                catch (Throwable ex){
                    ex.printStackTrace();
                }
                finally {
                    System.exit(0);
                }
            }
        });
    }

    private static void export_grid(edu.monash.fit.eduard_object.eduard.grid.Grid outputGrid) throws IOException {
        if (outputGridFile == null) {
            outputGridFile = FileUtils.askFile(null, "Export Grid", fileName + "_filtered.asc",false, "asc", null);
        }
        if (outputGridFile != null) {
            EsriASCIIGridExporter.export(outputGrid, outputGridFile, null);
            System.out.println("Exported grid to " + outputGridFile);
        }
    }

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

    private static void create_benchmark() throws IOException {
        if (benchmarkingFile == null){
            benchmarkingFile = FileUtils.askFile(null, "Export Benchmark", fileName + "_aparapi.txt", false, "txt", null);
        }
        if (benchmarkingFile != null){
            FileWriter myWriter = new FileWriter(benchmarkingFile);
            System.out.println("APARAPI performance benchmark");
            myWriter.write("APARAPI performance benchmark\n");
            for (String line : MaskFilter.benchmarking){
                System.out.println(line);
                myWriter.write(line + "\n");

            }
            myWriter.close();
        }
    }


}

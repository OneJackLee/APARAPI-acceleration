package edu.monash.fit.eduard_object.eduard.grid.importer;

import edu.monash.fit.eduard_object.eduard.utils.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A primitive reader and writer for WKT .prj files with coordinate reference
 * information for a geographically referenced grid or an image. Currently does
 * not parse or extract any useful information from .prj files.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public final class WKTProjectionFile {

    private WKTProjectionFile() {
    }

    /**
     * Reads a WKT .prj file with information about the coordinate reference of
     * a grid or an image.
     *
     * @param gridFilePath path to the grid file
     * @return the content of the .prj file or null if the .prj cannot be found
     */
    public static String read(String gridFilePath) {
        String prjFilePath = FileUtils.replaceExtension(gridFilePath, "prj", 3);
        File prjFile = new File(prjFilePath);
        if (prjFile.exists() && prjFile.length() > 0 && prjFile.length() < 10 * 1024) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(prjFile));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    line = line.trim();
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                return sb.toString();
            } catch (Exception ex) {
                Logger.getLogger(WKTProjectionFile.class.getName()).log(Level.WARNING, null, ex);
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(WKTProjectionFile.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
        return null;
    }

    /**
     * Writes a WKT .prj file with information about the coordinate reference of
     * a grid or an image.
     *
     * @param content to write to the file
     * @param gridFilePath path to the grid or image file
     * @return the content of the .prj file or null if the .prj cannot be found
     */
    public static void write(String content, String gridFilePath) {
        if (content == null || content.trim().length() == 0) {
            return;
        }

        PrintWriter writer = null;
        try {
            String prjFilePath = FileUtils.replaceExtension(gridFilePath, "prj", 3);
            writer = new PrintWriter(prjFilePath);
            writer.println(content.trim());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WKTProjectionFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}

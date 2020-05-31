package edu.monash.fit.aparapi_filter;

import edu.monash.fit.aparapi_filter.operator.MaskFilter;

import java.io.FileWriter;
import java.io.IOException;

/**
 * The original main class of the aparapi project
 * it will read in the stream input and store it into a Grid object, process and
 * output the post-process grid values through stream output
 */
public class Main {

    /**
     * The main execution method
     * @param args argument list
     */
    public static void main(String[] args) {
        // guardian code
        if (args.length < 7) {
            System.out.println("END");
            System.exit(-1);
        }


        /**
         * retrieve values from argument list
         */
        int cols = Integer.parseInt(args[0]);   // get cols
        int rows = Integer.parseInt(args[1]);   // get rows
        double cellsize = Double.parseDouble(args[2]);  // get cellsize
        double north = Double.parseDouble(args[3]);     // get north
        double south = Double.parseDouble(args[4]);     // get south

        double east = Double.parseDouble(args[5]);      // get east
        double west = Double.parseDouble(args[6]);      // get west

        int n = cols * rows;                        // size of buffer

        float[] buffer = new float[n];              // create buffer to store value
        Grid source, output;

        try {
            /*
            read from stream input, convert byte into float and store it into buffer
             */
            for (int i = 0; i < n; i++) {
                // read four bytes
                int b1 = System.in.read();
                int b2 = System.in.read();
                int b3 = System.in.read();
                int b4 = System.in.read();

                if (b4 == -1) {
                    break;
                }
                // convert bytes to float
                // Note: other languages likely use little-endian order, which would
                // require shuffling the bytes
                int intBits = b4 << 24 | (b3 & 0xFF) << 16 | (b2 & 0xFF) << 8 | (b1 & 0xFF);
                buffer[i] = Float.intBitsToFloat(intBits);
            }

            /*
            create grid object, execute mask filter procedure, and get the buffer array
             */
            source = new Grid(buffer, cols, rows, cellsize, north, south, east, west);
            output = new MaskFilter(source).execute();
            buffer = output.getBuffer();

            // generate the performance report
            try{
                // file writer
                FileWriter myWriter = new FileWriter("performance.txt");
                for (String line : MaskFilter.benchmarking)
                    myWriter.write(line + "\n");
                myWriter.close();

            }catch(IOException e){
                e.printStackTrace();
            }

            /*
            convert float to byte, and stream out the byte value
             */
            for (int i = 0; i < n; i++) {
                int intBits = Float.floatToIntBits(buffer[i]);
                int b4 = (intBits >>> 24) & 0xFF;
                int b3 = (intBits >>> 16) & 0xFF;
                int b2 = (intBits >>> 8) & 0xFF;
                int b1 = intBits & 0xFF;

                // write four bytes
                System.out.write(b1);
                System.out.write(b2);
                System.out.write(b3);
                System.out.write(b4);
                System.out.flush();
            }

        } catch (IOException ex) {
            System.exit(-1);
        }
        System.exit(0);

    }


}

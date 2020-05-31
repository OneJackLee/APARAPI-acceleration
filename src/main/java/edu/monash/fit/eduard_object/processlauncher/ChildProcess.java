package edu.monash.fit.eduard_object.processlauncher;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read and write grid float values from standard in and out streams.
 *
 * @author Bernhard Jenny, Faculty of Information Technology, Monash University,
 * Melbourne, Australia
 */
public class ChildProcess {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("END");
            System.exit(-1);
        }

        // size of grid is passed as command line arguments
        int cols = Integer.parseInt(args[0]);
        int rows = Integer.parseInt(args[1]);
//        double cellSize = Double.parseDouble(args[2]);
        int n = cols * rows;
//        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");


        try {
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
                // Note: other languages likely use little-endian order, wich would 
                // require shuffling the bytes
                int intBits = b4 << 24 | (b3 & 0xFF) << 16 | (b2 & 0xFF) << 8 | (b1 & 0xFF);
                float f = Float.intBitsToFloat(intBits);

//                System.exit(-1);
                // do something useful with float values
                f += 100;
                
                // convert float to bytes
                intBits = Float.floatToIntBits(f);
                b4 = (intBits >>> 24) & 0xFF;
                b3 = (intBits >>> 16) & 0xFF;
                b2 = (intBits >>> 8) & 0xFF;
                b1 = intBits & 0xFF;
                
                // write four bytes
                System.out.write(b1);
                System.out.write(b2);
                System.out.write(b3);
                System.out.write(b4);
                System.out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(ChildProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println("turn");
        System.exit(0);
    }
}

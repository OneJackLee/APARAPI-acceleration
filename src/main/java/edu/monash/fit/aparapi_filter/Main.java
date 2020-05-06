package edu.monash.fit.aparapi_filter;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("END");
            System.exit(-1);
        }

        int cols = Integer.parseInt(args[0]);
        int rows = Integer.parseInt(args[1]);
        int n = cols * rows;

        float[] buffer = new float[n];

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
                buffer[i] = Float.intBitsToFloat(intBits);
            }

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
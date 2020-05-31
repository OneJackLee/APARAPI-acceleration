package edu.monash.fit.aparapi_filter.playground;

import com.aparapi.Kernel;
import com.aparapi.Range;
import java.util.Random;

public class First {
    public static void main(String[] _args){
        System.out.println("APARAPI");
        Random rand = new Random();

        int greatest = 100000000;

        final float[] inA = new float[greatest];
        final float[] inB = new float[greatest];
        final float[] result = new float[greatest];

        for(int i = 0; i < inA.length; i++){
            inA[i] = rand.nextFloat();
            inB[i] = rand.nextFloat();
        }

        Kernel kernel = new Kernel(){
            public void run() {
                int i = getGlobalId();
//                result[i] = inA[i] + inB[i];
                result[i] = (float)(Math.cos(Math.sin(inA[i])) + Math.sin(Math.cos(inB[i])));

            }
        };

//        Range range = Range.create(result.length);
        long startTime = System.currentTimeMillis();
        kernel.execute(Range.create(result.length));
        long endTime = System.currentTimeMillis();
        kernel.dispose();

//        for(int i = 0; i < 5; i++) {
//            System.out.println("" + result[i]);
//        }

        System.out.println("Time spend:" + (endTime - startTime));

//        final float[] totalSum = new float[1];
//        Kernel kernel1 = new Kernel(){
//            public void run() {
//                int i = getGlobalId();
//                totalSum[0] += result[i];
//            }
//        };
//
////        Range newRange = Range.create(result.length);
//        startTime = System.currentTimeMillis();
//        kernel1.execute(Range.create(result.length));
//        endTime = System.currentTimeMillis();
//
//        kernel1.dispose();
//
//        System.out.println("total sum:" + totalSum[0]);
//
//        System.out.println("Time spend:" + (endTime - startTime));


    }
}

package edu.monash.fit.aparapi_filter.playground;

import java.util.Random;

public class Second {
    public static void main(String[] _args){
        System.out.println("JAVA normal");
        Random rand = new Random();

        int greatest = 100000000;

        final float[] inA = new float[greatest];
        final float[] inB = new float[greatest];
        final float[] result = new float[greatest];

        for(int i = 0; i < inA.length; i++){
            inA[i] = rand.nextFloat();
            inB[i] = rand.nextFloat();
        }

        long startTime = System.currentTimeMillis();
        for (int i= 0; i < greatest; i++){
            // result[i] = inA[i] + inB[i];
             result[i] = (float)(Math.cos(Math.sin(inA[i])) + Math.sin(Math.cos(inB[i])));
        }
        long endTime = System.currentTimeMillis();


//        for(int i = 0; i < 5; i++) {
//            System.out.println("" + result[i]);
//        }

        System.out.println("Time spend:" + (endTime - startTime));

//        final float[] totalSum = new float[1];
//        startTime = System.currentTimeMillis();
//        for (int i= 0; i < greatest; i++){
//            totalSum[0] += result[i];
//        }
//        endTime = System.currentTimeMillis();
//
//        System.out.println("total sum:" + totalSum[0]);
//
//        System.out.println("Time spend:" + (endTime - startTime));


    }
}

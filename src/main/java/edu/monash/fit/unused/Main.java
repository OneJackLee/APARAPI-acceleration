package edu.monash.fit.unused;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    // provide the connection of UI with the I/O
    // performing read and write file

    static String fileInputPath ;
    static String fileOutputPath;

    public static void main(String[] args) {
        // manually scan the filename
        Scanner readInput = new Scanner (System.in); // creates readInput scanner
        System.out.println("Enter file name");
        //
        fileInputPath = readInput.nextLine(); // reads next line aka the user input
        System.out.println( "Input file : "+ fileInputPath);

        try{
            System.out.println(ReadFile.read(fileInputPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

import java.io.File;
import java.util.Scanner;

// ask user to input file name or use JFileChooser (take from Oracle)
public class Input_Output {

    public void inputFile(){
        Scanner readInput = new Scanner (System.in); // creates readInput scanner
        System.out.println("Enter file name");

        String fileName = readInput.nextLine(); // reads next line aka the user input

        File inputFile = new File (fileName); // opens fileName, aka file has to be in the same folder.

        //additional comment for commit comment

    }





}

import javax.swing.*;
import java.io.File;
import java.util.Scanner;

// ask user to input file name or use JFileChooser (take from Oracle)
public class Input_Output {

//    public void inputFile(){
//        Scanner readInput = new Scanner (System.in); // creates readInput scanner
//        System.out.println("Enter file name");
//
//        String fileName = readInput.nextLine(); // reads next line aka the user input
//
//        File inputFile = new File (fileName); // opens fileName, aka file has to be in the same folder.
//
//        //additional comment for commit comment
//
//    }

    public static void main(String[] args){
        JButton open = new JButton();
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File("C:"));
        fc.setDialogTitle("Open a File");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(open)==JFileChooser.APPROVE_OPTION){
            //
        }
        System.out.println("Tis"+fc.getSelectedFile().getAbsolutePath());
    }

//    https://www.youtube.com/watch?v=9VrtranTJnc







}

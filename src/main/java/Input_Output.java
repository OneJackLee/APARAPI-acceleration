import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// ask user to input file name or use JFileChooser (take from Oracle)
public class Input_Output {

    public static void main(String[] args) throws FileNotFoundException {

        File inputFile =  input_file(); // opens file in that directory

        // read file // maybe in another class/method
        Scanner readFile = new Scanner(inputFile);
//        readFile.

    }

    public static File input_file(){
        JButton open = new JButton();
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File("C:"));
        fc.setDialogTitle("Open a File");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(open)==JFileChooser.APPROVE_OPTION){
            //
        }

        return fc.getSelectedFile();
    }

    public static void output_file(){
        JFrame parentFrame = new JFrame();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        int userSelection = fileChooser.showSaveDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
        }

//        https://www.codejava.net/java-se/swing/show-save-file-dialog-using-jfilechooser
//        need more understanding
    }


//    https://www.youtube.com/watch?v=9VrtranTJnc
//    https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html









}

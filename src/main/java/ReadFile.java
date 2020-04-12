import java.io.*;
import java.util.StringTokenizer;

public class ReadFile {
    public static String read(String filePath) throws Exception {

        String line;
        File fname = new File(filePath);
        try{
            InputStream fis = new FileInputStream(fname.getAbsolutePath());
        }
        catch(FileNotFoundException error){
            return "ERROR";
        }
        InputStream fis = new FileInputStream(fname.getAbsolutePath());
        InputStreamReader file = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(file);

        while ((line=br.readLine()) != null){
            StringTokenizer tokenizer = new StringTokenizer(line, " \t,;");
            String str = tokenizer.nextToken().trim().toLowerCase();
            System.out.println(str);
            if (str.equals("ncols")){
                System.out.println(Integer.parseInt(tokenizer.nextToken()));
            }
        }
        return "";
    }
}

import com.aparapi.Kernel;
import com.aparapi.Range;
import java.util.Random;

public class First {
    public static void main(String[] _args){
        System.out.println("testing");

        Random rand = new Random();

        int greatest = 100000;

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
                result[i] = inA[i] + inB[i];
            }
        };

        long startTime = System.currentTimeMillis();
        Range range = Range.create(result.length);
        kernel.execute(range);
        kernel.dispose();
        long endTime = System.currentTimeMillis();

        for(int i = 0; i < 5; i++) {
            System.out.println("" + result[i]);
        }

        System.out.println("Time spend:" + (endTime - startTime));

        final float[] totalSum = new float[1];
        Kernel kernel1 = new Kernel(){
            public void run() {
                int i = getGlobalId();
                totalSum[0] += result[i];
            }
        };

        startTime = System.currentTimeMillis();
        Range newRange = Range.create(result.length);
        kernel1.execute(newRange);
        kernel1.dispose();
        endTime = System.currentTimeMillis();

        System.out.println("total sum:" + totalSum[0]);

        System.out.println("Time spend:" + (endTime - startTime));


    }
}

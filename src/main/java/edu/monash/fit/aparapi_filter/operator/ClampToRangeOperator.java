package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;

/**
 * clamp slope values to range between min and max
 *
 * [REFERENCE: Eduard ClampToRangeOperator class]
 */
public class ClampToRangeOperator implements AparapiOperator {
    Grid src;           // the source grid
    Grid dest;          // the result grid
    float minI, maxI;
    double timer;       // timer

    /**
     * The default constructor of ClampToRangeOperator
     * @param min minimum value
     * @param max maximum value
     */
    public ClampToRangeOperator(float min, float max){
        this.minI = min;
        this.maxI = max;
        if (maxI < minI){
            throw new IllegalArgumentException("The max value is greater than min value");
        }
    }

    @Override
    public Grid operate(Grid src) {
        this.src = src;
        this.dest = Grid.shallowCopy(src);      // create the Grid holder with the attributes of src Grid

        /*
        As APARAPI doesnt supported Object-oriented design and method calling, thus arguments has to be
        assigned
         */
        int srcCols = src.getCols();
        int srcRows = src.getRows();

        float[] srcBuffer = src.getBuffer();    // holder
        float[] destBuffer = dest.getBuffer();  // holder

        float min = minI;
        float max = maxI;

        // anonymous object of APARAPI kernel
        Kernel kernel = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                // clamp the range of the value
                destBuffer[i] = Math.min(max, Math.max(min, srcBuffer[i]));
            }
        };
        kernel.setExplicit(true);           // explicitly manage transfers between GPU memory and CPU
        kernel.put(srcBuffer);              // send srcBuffer to GPU
        kernel.put(destBuffer);             // send destBuffer to GPU
        kernel.execute(1);                  // as creating KernelRunner is lazy, thus force create
        timer = System.nanoTime();
        kernel.execute(Range.create(srcCols* srcRows));     // execute KernelRunner
        timer = System.nanoTime() - timer;
        timer = timer/ 1000000;

        MaskFilter.benchmarking.add("Clamp to range:  " + timer + " ms");

        kernel.get(destBuffer);             // fetch destBuffer from GPU
        kernel.dispose();                   // clean kernel when finish job
        dest.setBufferReceived(destBuffer);
        return dest;
    }

    @Override
    public double getTimer(){
        return timer;
    }

}

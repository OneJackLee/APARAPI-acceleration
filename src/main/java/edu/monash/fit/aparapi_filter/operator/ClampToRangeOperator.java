package edu.monash.fit.aparapi_filter.operator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.MaskFilter;

public class ClampToRangeOperator implements AparapiOperator {
    Grid src;
    Grid dest;
    float minI, maxI;
    double timer;


    public ClampToRangeOperator(float min, float max){
        this.minI = min;
        this.maxI = max;

    }

    public Grid operate(Grid src) {
        this.src = src;
        this.dest = Grid.shallowCopy(src);

        int srcCols = src.getCols();
        int srcRows = src.getRows();
        double srcNorth = src.getNorth();
        double srcCellSizes = src.getCellSize();
        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = dest.getBuffer();

        float min = minI;
        float max = maxI;
        assert(min <= max);

        Kernel kernel = new Kernel(){
            @Override
            public void run() {
                int i = getGlobalId();
                destBuffer[i] = Math.min(max, Math.max(min, srcBuffer[i]));
            }
        };
        kernel.setExplicit(true);
        kernel.put(srcBuffer);
        kernel.put(destBuffer);
        kernel.execute(1);
        timer = System.nanoTime();
        kernel.execute(Range.create(srcCols* srcRows));
        timer = System.nanoTime() - timer;
        timer = timer/ 1000000;

        MaskFilter.benchmarking.add("Clamp to range:  " + timer + " ms");

        kernel.get(destBuffer);
        kernel.dispose();
        return dest;
    }

    public double getTimer(){
        return timer;
    }

}

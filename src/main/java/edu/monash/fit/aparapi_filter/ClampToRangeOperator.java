package edu.monash.fit.aparapi_filter;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class ClampToRangeOperator implements AparapiOperator {

    private final float min, max;
    Grid src;
    Grid dest;

    public ClampToRangeOperator(float min, float max) {
        assert(min <= max);
        this.min = min;
        this.max = max;
    }

    @Override
    public Grid operate(Grid src, Grid dest) {
        this.src = src;
        this.dest = dest;

        int srcCols = src.getCols();
        int srcRows = src.getRows();
        float[] srcBuffer = src.getBuffer();
        float[] destBuffer = dest.getBuffer();

        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();
                float val = srcBuffer[i];
                val = Math.min(max, Math.max(min, val));
                destBuffer[i] = val;
            }

        };

        kernel.setExplicit(true);
        kernel.put(srcBuffer);
        kernel.put(destBuffer);
        kernel.execute(1);
        kernel.execute(Range.create(srcCols* srcRows));
        kernel.get(destBuffer);
        kernel.dispose();

        return src;

    }

    @Override
    public Grid operate(Grid src) {
        return null;
    }
}

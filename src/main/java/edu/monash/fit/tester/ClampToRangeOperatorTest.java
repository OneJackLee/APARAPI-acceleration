package edu.monash.fit.tester;

import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.operator.ClampToRangeOperator;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class ClampToRangeOperatorTest {
    ClampToRangeOperator clampToRangeOperator;
    Grid aparapiSource, aparapiResult;
    edu.monash.fit.eduard_object.eduard.grid.Grid eduardSource, eduardResult;

    float slopeThresholdDeg = 6f, slopeThreshold = (float) Math.tan(Math.toRadians(slopeThresholdDeg)), relativeGain = 0.5f;
    float gainSlopeThresholdDeg = slopeThresholdDeg * Math.min(0.995f, relativeGain);
    float gainSlopeThreshold = (float) Math.tan(Math.toRadians(gainSlopeThresholdDeg));

    @Test
    void createClampToRangeOperator(){
        clampToRangeOperator = new ClampToRangeOperator(1, 99);
    }

    @Test
    void testMinIsGreaterThanMax() {
        try{
            clampToRangeOperator = new ClampToRangeOperator(999, 111);
            fail();
        } catch(IllegalStateException ex){
        }
    }

    @Test
    void testInvalidGrid() {
        try{
            clampToRangeOperator = new ClampToRangeOperator(1, 99);
            clampToRangeOperator.operate(null);
            fail();
        } catch(NullPointerException ex){
        }
    }

    @Test
    void testOutputLength() {
        generateInput();
        generateOutput();
        assertEquals(aparapiResult.getBuffer().length, eduardResult.getBufferArray().length);
    }

    @Test
    void testOutputCorrectness() {
        generateInput();
        generateOutput();
        for (int i=0; i <aparapiResult.getBuffer().length ; i++){
            assertEquals(aparapiResult.get(i), eduardResult.getValue(i), 2);
        }
    }


    private void generateInput(){
        aparapiSource = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        aparapiSource.fillWithRandomFloat();

        eduardSource = new edu.monash.fit.eduard_object.eduard.grid.Grid(1500, 1500, 0.5, 0.8, 0.8, "");
        for (int row = 0, nRows = eduardSource.getRows(); row < nRows; row++) {
            for (int col = 0, nCols = eduardSource.getCols(); col < nCols; col++)
                eduardSource.setValue(aparapiSource.get(col, row), col, row);
        }
    }

    private void generateOutput(){
        aparapiResult = new ClampToRangeOperator(gainSlopeThreshold, slopeThreshold).operate(aparapiSource);
        eduardResult = new edu.monash.fit.eduard_object.eduard.grid.operator.ClampToRangeOperator(gainSlopeThreshold, slopeThreshold).operate(eduardSource);
    }

}
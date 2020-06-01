package edu.monash.fit.tester;

import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.operator.HorizontalTransposingLowPassFilter;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


class HorizontalTransposingLowPassFilterTest {
    float sigmaBlur = 6f;
    HorizontalTransposingLowPassFilter horizontalTransposingFirstPass, horizontalTransposingSecondPass;
    Grid aparapiSource, aparapiSecond, aparapiResult;
    edu.monash.fit.eduard_object.eduard.grid.Grid eduardSource, eduardResult;


    @Test
    void createHorizontalOperatorObject() {
        horizontalTransposingFirstPass = new HorizontalTransposingLowPassFilter(true, sigmaBlur);
        horizontalTransposingSecondPass = new HorizontalTransposingLowPassFilter(false, sigmaBlur);
    }

    @Test
    void testInvalidGrid() {
        horizontalTransposingFirstPass = new HorizontalTransposingLowPassFilter(true, sigmaBlur);
        try{
            horizontalTransposingFirstPass.operate(null);
            fail();
        } catch(NullPointerException ex){
        }
    }

    @Test
    void testInvalidSigmaValue() {
        try {
            horizontalTransposingFirstPass = new HorizontalTransposingLowPassFilter(true, -100);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    void testOutputLength() {
        generateInput();
        generateOutputSigmaBlur();

        assertEquals(aparapiResult.getBuffer().length, eduardResult.getBufferArray().length);
    }

    @Test
    void testOutputCorrectness() {
        generateInput();
        generateOutputSigmaBlur();

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

    private void generateOutputSigmaBlur() {
        aparapiSecond = new HorizontalTransposingLowPassFilter(true, sigmaBlur).operate(aparapiSource);
        aparapiResult = new HorizontalTransposingLowPassFilter(false, sigmaBlur).operate(aparapiSecond);

        edu.monash.fit.eduard_object.eduard.grid.operator.AbstractFrequencyOperator afo;
        // HorizontalTransposingLowPassFilter is a private class in AbstractFrequencyOperator class
        // thus each HorizontalTransposingLowPassFilter instance is unable to test separately
        afo = new edu.monash.fit.eduard_object.eduard.grid.operator.AbstractFrequencyOperator(sigmaBlur, null) {
            @Override
            public String getName() {
                return "";
            }
        };

        eduardResult = afo.operate(eduardSource);

    }


}
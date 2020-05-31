package edu.monash.fit.tester;

import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.operator.LowPassOperator;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class LowPassOperatorTest {

    LowPassOperator lowPassOperatorSigmaBlur, lowPassOperatorSigmaSmooth;
    Grid aparapiSource, aparapiResult;
    edu.monash.fit.eduard_object.eduard.grid.Grid eduardSource, eduardResult;
    private final static float sigmaBlur = 6f, sigmaSmooth = 20f;


    @Test
    void createLowPasOperator(){
        lowPassOperatorSigmaBlur = new LowPassOperator(sigmaBlur);
        lowPassOperatorSigmaSmooth = new LowPassOperator(sigmaSmooth);
    }

    @Test
    void testInvalidGrid() {
        lowPassOperatorSigmaBlur = new LowPassOperator(sigmaBlur);
        lowPassOperatorSigmaSmooth = new LowPassOperator(sigmaSmooth);
        try{
            lowPassOperatorSigmaBlur.operate(null);
            fail();
        } catch(NullPointerException ex){
        }
    }
    @Test
    void testInvalidSigmaValue() {
        try {
            lowPassOperatorSigmaBlur = new LowPassOperator(-100);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    void testOutputLengthSigmaBlur() {
        generateInput();
        generateOutputSigmaBlur();

        assertEquals(aparapiResult.getBuffer().length, eduardResult.getBufferArray().length);
    }

    @Test
    void testOutputCorrectnessSigmaBlur() {
        generateInput();
        generateOutputSigmaBlur();

        for (int i=0; i <aparapiResult.getBuffer().length ; i++){
            assertEquals(aparapiResult.get(i), eduardResult.getValue(i), 2);
        }
    }

    @Test
    void testOutputLengthSigmaSmooth() {
        generateInput();
        generateOutputSigmaSmooth();

        assertEquals(aparapiResult.getBuffer().length, eduardResult.getBufferArray().length);
    }

    @Test
    void testOutputCorrectnessSigmaSmooth() {
        generateInput();
        generateOutputSigmaSmooth();

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
        aparapiResult = new LowPassOperator(sigmaBlur).operate(aparapiSource);
        eduardResult = new edu.monash.fit.eduard_object.eduard.grid.operator.LowPassOperator(sigmaBlur, null).operate(eduardSource);
    }

    private void generateOutputSigmaSmooth() {
        aparapiResult = new LowPassOperator(sigmaSmooth).operate(aparapiSource);
        eduardResult = new edu.monash.fit.eduard_object.eduard.grid.operator.LowPassOperator(sigmaSmooth, null).operate(eduardSource);

    }

}
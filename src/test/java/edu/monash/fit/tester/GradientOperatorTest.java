package edu.monash.fit.tester;

import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.operator.GradientOperator;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class GradientOperatorTest {

    GradientOperator gradientOperator;
    Grid aparapiSource, aparapiResult;
    edu.monash.fit.eduard_object.eduard.grid.Grid eduardSource, eduardResult;

    @Test
    void createGradientOperatorObject(){
        gradientOperator = new GradientOperator();
    }

    @Test
    void testInvalidGrid() {
        GradientOperator testVoid = new GradientOperator();
        try{
            testVoid.operate(null);
            fail();
        } catch(NullPointerException ex){
        }
    }

    @Test
    void testOutputLength() {
        gradientOperator = new GradientOperator();
        generateInput();
        generateOutput();

        assertEquals(aparapiResult.getBuffer().length, eduardResult.getBufferArray().length);
    }

    @Test
    void testOutputCorrectness() {
        gradientOperator = new GradientOperator();
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
        aparapiResult = gradientOperator.operate(aparapiSource);
        eduardResult = new edu.monash.fit.eduard_object.eduard.grid.operator.GradientOperator().operate(eduardSource);
    }
}
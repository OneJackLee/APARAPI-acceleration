package edu.monash.fit.tester;

import edu.monash.fit.aparapi_filter.Grid;
import edu.monash.fit.aparapi_filter.operator.MaskFilter;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class MaskFilterTest {
    MaskFilter maskFilter;
    Grid aparapiSource, aparapiResult;
    edu.monash.fit.eduard_object.eduard.grid.Grid eduardSource, eduardResult;


    @Test
    void createMaskFilterObject() {
        generateInput();
        maskFilter = new MaskFilter(aparapiSource);
    }

    @Test
    void testInvalidGrid() {
        try{
            maskFilter = new MaskFilter(null);
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

    private void generateOutput() {
        aparapiResult = new MaskFilter(aparapiSource).execute();
        eduardResult = edu.monash.fit.eduard_object.FIT3161.Main.maskFilter(eduardSource);
    }
}
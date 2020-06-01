package edu.monash.fit.tester;

import edu.monash.fit.aparapi_filter.Grid;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class GridTest {
    Grid grid, grid2;

    @Test
    void shallowCopy() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        grid.fillWithRandomFloat();

        grid2 = Grid.shallowCopy(grid);

        assertEquals(grid2.getCols() , grid.getCols());
        assertEquals(grid2.getRows() , grid.getRows());
        assertEquals(grid2.getCellSize() , grid.getCellSize(), 3);
        assertEquals(grid2.getNorth() , grid.getNorth(), 3);
        assertEquals(grid2.getSouth() , grid.getSouth(), 3);
        assertEquals(grid2.getEast() , grid.getEast(), 3);
        assertEquals(grid2.getWest() , grid.getWest(), 3);
    }

    @Test
    void fillWithRandomFloat() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        grid.fillWithRandomFloat();
        float max = 4000;
        float min = 2500;
        for (int i= 0 ; i <grid.getLength(); i++){
            if (grid.get(i) > max || grid.get(i) < min)
                fail();
        }
    }

    @Test
    void fillWithZero() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        grid.fillWithZero();
        for (int i= 0 ; i <grid.getLength(); i++){
            if (grid.get(i)!= 0)
                fail();
        }
    }

    @Test
    void getLength() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        grid.fillWithRandomFloat();
        int counter = 0;
        for (float i: grid.getBuffer())
            counter ++;
        assertEquals(counter, grid.getCols() * grid.getRows());
        assertEquals(grid.getLength(), grid.getCols() * grid.getRows());
        assertEquals(counter, grid.getLength());
    }

    @Test
    void setGetBufferReceived() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        float[] equal = new float[1500*1500];
        Arrays.fill(equal, 0f);
        grid.setBufferReceived(equal);
        assertEquals(grid.getBuffer(), equal);
    }


    @Test
    void setBufferReceivedNull() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        try{
            grid.setBufferReceived(null);
            fail();
        }catch(NullPointerException ex){
        }
    }


    @Test
    void testGet() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        float[] newBuffer = new float[1500*1500];
        newBuffer[0] = 1;
        newBuffer[1] = 2;
        newBuffer[2] = 3;

        newBuffer[grid.getDirectIndex(4,5)] = 6;
        newBuffer[grid.getDirectIndex(5,6)] = 7;
        newBuffer[grid.getDirectIndex(6,7)] = 8;

        grid.setBufferReceived(newBuffer);
        for (int i = 0; i < 3; i++)
            assertEquals(grid.get(i), i+1, 2);

        for (int i = 4; i < 7; i++)
            assertEquals(grid.get(i,i+1), i+2, 2);

        try{
            grid.get(-100);
            grid.get(-100, -200);
            fail();
        }
        catch(IllegalArgumentException ex){}


    }

    @Test
    void testSet() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        grid.fillWithRandomFloat();
        grid.set(0,0);
        grid.set(0,1);
        grid.set(0,2);

        grid.set(0, 3, 4);
        grid.set(0, 4, 5);
        grid.set(0, 5, 6);

        for (int i = 0; i < 3; i++)
            assertEquals(grid.get(i), 0, 2);

        for (int i = 3; i < 6; i++)
            assertEquals(grid.get(i, i+1), 0, 2);

        try{
            grid.set(0,-100);
            grid.set(0, -100, -200);
            fail();
        }
        catch(IllegalArgumentException ex){}
    }


    @Test
    void getRow() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getRow(1500), 1);
        assertNotEquals(grid.getRow(1499), 1);

        try{
            grid.getRow(-100);
            fail();
        }
        catch(IllegalArgumentException ex){

        }
    }

    @Test
    void getCol() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getCol(1500), 0);
        assertNotEquals(grid.getCol(1499), 0);

        try{
            grid.getCol(-100);
            fail();
        }
        catch(IllegalArgumentException ex){

        }

    }

    @Test
    void getDirectIndex() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getDirectIndex(2, 3), 4502);
        assertNotEquals(grid.getDirectIndex(1, 1), 1);

        try{
            grid.getDirectIndex(-100, 0);
            grid.getDirectIndex(0, -100);
            fail();
        }
        catch(IllegalArgumentException ex){
        }

    }

    @Test
    void getRows() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getRows(), 1500);
        grid = new Grid(80, 60, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertNotEquals(grid.getRows(), 1500);

    }

    @Test
    void getCols() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getCols(), 1500);
        grid = new Grid(80, 60, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertNotEquals(grid.getCols(), 1500);

    }

    @Test
    void getCellSize() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getCellSize(), 0.5, 3);
        grid = new Grid(80, 60, 0.8, 0.8, 0.3, 0.3,0.3);
        assertNotEquals(grid.getCellSize(), 0.5);
    }


    @Test
    void getSouth() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getSouth(), 0.8, 3);
        grid = new Grid(80, 60, 0.8, 0.8, 0.3, 0.3,0.3);
        assertNotEquals(grid.getSouth(), 0.5);

    }


    @Test
    void getNorth() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getNorth(), 0.6, 3);
        grid = new Grid(80, 60, 0.8, 0.8, 0.3, 0.3,0.3);
        assertNotEquals(grid.getNorth(), 0.5);

    }

    @Test
    void getEast() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getEast(), 0.8, 3);
        grid = new Grid(80, 60, 0.8, 0.8, 0.3, 0.3,0.3);
        assertNotEquals(grid.getEast(), 0.5);

    }


    @Test
    void getWest() {
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        assertEquals(grid.getEast(), 0.8, 3);
        grid = new Grid(80, 60, 0.8, 0.8, 0.3, 0.3,0.3);
        assertNotEquals(grid.getEast(), 0.5);

    }


    private void generateInput(){
        grid = new Grid(1500, 1500, 0.5, 0.6, 0.8, 0.8, 0.8);
        grid.fillWithRandomFloat();
    }

}
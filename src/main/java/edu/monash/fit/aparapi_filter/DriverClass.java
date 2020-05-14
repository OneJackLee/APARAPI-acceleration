package edu.monash.fit.aparapi_filter;

public class DriverClass {
    public static void main(String[] args) {
        Grid output = new Grid(3, 4, 0.5, 0.6, 0.8);
        System.out.println(output.getDirectIndex(2, 3));
        System.out.println(output.getCol(11)+ " " + output.getRow(11));
    }

}

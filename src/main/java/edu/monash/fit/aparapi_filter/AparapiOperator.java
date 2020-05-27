package edu.monash.fit.aparapi_filter;

public interface AparapiOperator {

    public Grid operate(Grid src, Grid dest);
    public Grid operate(Grid src);

}

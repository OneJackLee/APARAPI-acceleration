package edu.monash.fit.aparapi_filter.operator;

import edu.monash.fit.aparapi_filter.Grid;

public interface AparapiOperator {

    Grid operate(Grid src);

    double getTimer();

}

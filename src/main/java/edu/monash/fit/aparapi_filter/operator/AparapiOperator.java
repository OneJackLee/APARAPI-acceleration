package edu.monash.fit.aparapi_filter.operator;

import edu.monash.fit.aparapi_filter.Grid;

/**
 * The interface which reserved for the APARAPI operator.
 */
public interface AparapiOperator {

    /**
     * Operate the filter operator using APARAPI Kernel (create Kernel anon object)
     * @param src the source grid file
     * @return the result grid file
     */
    Grid operate(Grid src);

    /**
     * Get the runtime of the kernel (ignore overhead)
     * @return the timer of the kernel runtime
     */
    double getTimer();

}

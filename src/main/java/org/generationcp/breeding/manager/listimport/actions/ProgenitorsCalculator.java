package org.generationcp.breeding.manager.listimport.actions;

/**
 * This Interface take care to calculate gerplasm progenitors value to insert on database.
 */

public interface ProgenitorsCalculator {

    public int calculate(final Integer methodId, final Integer prevGnpgs) ;
}

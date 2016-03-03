package org.generationcp.breeding.manager.listimport.actions;

/**
 * This Interface take care to calculate gnpgs value to insert on database.
 */

public interface GNPGSCalculator {

    public int calculate(final Integer methodId, final Integer prevGnpgs) ;
}

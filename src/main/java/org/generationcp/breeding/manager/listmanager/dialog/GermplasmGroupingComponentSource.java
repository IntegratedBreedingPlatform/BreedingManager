
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.Set;

/**
 * This interface must be implemented by components that has an option to use "Marked Line as Fixed" feature i.e. ListComponent
 */
public interface GermplasmGroupingComponentSource {

	/**
	 * This method is use to update the germplasm list table that used this interface
	 * 
	 * @param gidsProcessed - list of gids that is marked as fixed line
	 */
	void updateGermplasmListTable(Set<Integer> gidsProcessed);

}

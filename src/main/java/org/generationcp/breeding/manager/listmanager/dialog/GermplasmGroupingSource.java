
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.Set;

/**
 * This interface is used for components that uses "Marked Line as Fixed"
 * 
 * @author Efficio
 * 
 */
public interface GermplasmGroupingSource {

	/**
	 * This method is use to update the germplasm list table that used this interface
	 * 
	 * @param gidsProcessed - list of gids that is marked as fixed line
	 */
	void updateGermplasmListTable(Set<Integer> gidsProcessed);

}

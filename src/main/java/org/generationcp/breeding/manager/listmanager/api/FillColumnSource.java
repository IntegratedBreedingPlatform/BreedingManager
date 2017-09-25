package org.generationcp.breeding.manager.listmanager.api;

import java.util.List;

public interface FillColumnSource {
	
	List<Object> getItemIdsToProcess();
	
	/**
	 * Return list of GIDs to use in filling up column values. This list could just be a subset of all GIDs if for example
	 * the table is a paged table, in which case the gids to process are just the GIDs for the current page.
	 * 
	 * @return
	 */
	List<Integer> getGidsToProcess();
	
	Integer getGidForItemId(Object itemId);
	
	void setColumnValueForItem(Object itemId, String column, Object value);
	
	void propagateUIChanges();
	
	/**
	 * Return list of all GIDs on table
	 * 
	 * @return
	 */
	List<Integer> getAllGids();
}

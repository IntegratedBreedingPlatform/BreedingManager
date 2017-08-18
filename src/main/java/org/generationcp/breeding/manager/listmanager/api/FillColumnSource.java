package org.generationcp.breeding.manager.listmanager.api;

import java.util.List;

public interface FillColumnSource {
	
	List<Integer> getItemIdsToProcess();
	
	List<Integer> getGidsToProcess();
	
	Integer getGidForItemId(Integer itemId);
	
	void setColumnValueForItem(Integer itemId, String column, Object value);
	
	void setUnsavedChanges();
	
	void resetEditableTable();

}

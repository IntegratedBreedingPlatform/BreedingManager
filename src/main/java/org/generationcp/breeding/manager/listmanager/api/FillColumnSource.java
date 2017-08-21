package org.generationcp.breeding.manager.listmanager.api;

import java.util.List;

public interface FillColumnSource {
	
	List<Object> getItemIdsToProcess();
	
	List<Integer> getGidsToProcess();
	
	Integer getGidForItemId(Object itemId);
	
	void setColumnValueForItem(Object itemId, String column, Object value);
	
	void setUnsavedChanges();
	
	void resetEditableTable();

}

package org.generationcp.breeding.manager.listmanager.api;

import org.generationcp.commons.constant.ColumnLabels;

public interface AddColumnSource extends FillColumnSource {
	
	void addColumn(ColumnLabels columnLabel);
	
	boolean columnExists(String columnName);

}

package org.generationcp.breeding.manager.listmanager.api;

import org.generationcp.commons.constant.ColumnLabels;

import com.vaadin.ui.Window;

public interface AddColumnSource extends FillColumnSource {
	
	void addColumn(ColumnLabels columnLabel);
	
	void addColumn(String columnName);
	
	boolean columnExists(String columnName);
	
	Window getWindow();
}

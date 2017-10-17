package org.generationcp.breeding.manager.listmanager.api;

import java.util.List;

import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.middleware.constant.ColumnLabels;

import com.vaadin.ui.Window;

public interface AddColumnSource extends FillColumnSource {
	
	void addColumn(ColumnLabels columnLabel);
	
	void addColumn(String columnName);
	
	boolean columnExists(String columnName);
	
	Window getWindow();
	
	List<FillWithOption> getColumnsToExclude();
}

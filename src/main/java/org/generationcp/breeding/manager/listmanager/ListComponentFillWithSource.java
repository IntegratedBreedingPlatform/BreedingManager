package org.generationcp.breeding.manager.listmanager;

import org.generationcp.middleware.constant.ColumnLabels;

import com.vaadin.ui.Table;

public class ListComponentFillWithSource extends ListComponentAddColumnSource {
	
	public ListComponentFillWithSource(final ListComponent listComponent, final Table targetTable, final String gidPropertyId) {
		super(listComponent, targetTable, gidPropertyId);
	}
	
	@Override
	public void addColumn(ColumnLabels columnLabel) {
		// Do nothing - we only want to generate values for existing column
	}
	
	@Override
	public void addColumn(String columnName) {
		// Do nothing - we only want to generate values for existing column
	}

}

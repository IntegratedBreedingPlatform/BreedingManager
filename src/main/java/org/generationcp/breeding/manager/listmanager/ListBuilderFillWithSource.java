package org.generationcp.breeding.manager.listmanager;

import org.generationcp.middleware.constant.ColumnLabels;

import com.vaadin.ui.Table;

public class ListBuilderFillWithSource extends ListBuilderAddColumnSource {
	
	public ListBuilderFillWithSource(final ListBuilderComponent listBuilderComponent, final Table targetTable, final String gidPropertyId) {
		super(listBuilderComponent, targetTable, gidPropertyId);
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

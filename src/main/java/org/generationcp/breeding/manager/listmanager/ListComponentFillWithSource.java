package org.generationcp.breeding.manager.listmanager;

import org.generationcp.commons.constant.ColumnLabels;

import com.vaadin.ui.Table;

public class ListComponentFillWithSource extends ListComponentAddColumnSource {
	
	public ListComponentFillWithSource(final ListTabComponent listTabComponent, final Table targetTable, final String gidPropertyId) {
		super(listTabComponent, targetTable, gidPropertyId);
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

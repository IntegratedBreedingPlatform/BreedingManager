
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.middleware.constant.ColumnLabels;

import com.vaadin.ui.Table;

public class ListComponentFillWithSource extends ListComponentAddColumnSource {

	public ListComponentFillWithSource(final ListComponent listComponent, final Table targetTable, final String gidPropertyId) {
		super(listComponent, targetTable, gidPropertyId);
	}

	@Override
	public void addColumn(final ColumnLabels columnLabel) {
		// Do nothing - we only want to generate values for existing column
	}

	@Override
	public void addColumn(final String columnName) {
		// Do nothing - we only want to generate values for existing column
	}

}

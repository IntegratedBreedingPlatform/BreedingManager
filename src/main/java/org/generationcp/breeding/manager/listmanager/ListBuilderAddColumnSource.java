package org.generationcp.breeding.manager.listmanager;

import com.vaadin.ui.Table;

public class ListBuilderAddColumnSource extends ListComponentAddColumnSource {
	
	protected ListBuilderComponent listBuilderComponent;
	
	public ListBuilderAddColumnSource(final ListBuilderComponent listBuilderComponent, final Table targetTable, final String gidPropertyId) {
		super();
		this.listBuilderComponent = listBuilderComponent;
		this.targetTable = targetTable;
		this.gidPropertyId = gidPropertyId;
	}
	
	@Override
	public void propagateUIChanges() {
		resetEditableTable();
		this.listBuilderComponent.setHasUnsavedChanges(true);
	}
	
	@Override
	public void addColumn(final String columnName) {
		addColumnToTable(columnName);
		this.listBuilderComponent.addAttributeAndNameTypeColumn(columnName);
	}

}

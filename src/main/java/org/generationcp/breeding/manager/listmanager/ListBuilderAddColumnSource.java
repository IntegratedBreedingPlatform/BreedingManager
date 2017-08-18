package org.generationcp.breeding.manager.listmanager;

import com.vaadin.ui.Table;

public class ListBuilderAddColumnSource extends ListComponentAddColumnSource {
	
	private ListBuilderComponent listBuilderComponent;
	
	public ListBuilderAddColumnSource(final ListBuilderComponent listBuilderComponent, final Table targetTable, final String gidPropertyId) {
		super();
		this.listBuilderComponent = listBuilderComponent;
		this.targetTable = targetTable;
		this.gidPropertyId = gidPropertyId;
	}
	
	@Override
	public void setUnsavedChanges() {
		this.listBuilderComponent.setHasUnsavedChanges(true);
	}

}

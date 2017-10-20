
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

@Configurable
public class ListComponentAddColumnSource implements AddColumnSource {

	@Autowired
	private OntologyDataManager ontologyDataManager;

	protected ListTabComponent listTabComponent;
	protected Table targetTable;
	protected String gidPropertyId;

	public ListComponentAddColumnSource() {
		// empty constructor needed for subclass
	}

	public ListComponentAddColumnSource(final ListTabComponent listTabComponent, final Table targetTable, final String gidPropertyId) {
		this.listTabComponent = listTabComponent;
		this.targetTable = targetTable;
		this.gidPropertyId = gidPropertyId;
	}

	@Override
	public List<Object> getItemIdsToProcess() {
		return new ArrayList<>(this.targetTable.getItemIds());
	}

	/*
	 * Get list of GIDs from caption of GID button/link (non-Javadoc)
	 *
	 */
	@Override
	public List<Integer> getGidsToProcess() {
		final List<Integer> gids = new ArrayList<>();
		final List<Object> listDataItemIds = this.getItemIdsToProcess();
		for (final Object itemId : listDataItemIds) {
			gids.add(this.getGidForItemId(itemId));
		}
		return gids;

	}

	/*
	 * Get GID string from caption of GID button/link of specified item in table (non-Javadoc)
	 *
	 */
	@Override
	public Integer getGidForItemId(final Object itemId) {
		final Button gidButton = (Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue();
		return Integer.valueOf(gidButton.getCaption().toString());
	}

	@Override
	public void setColumnValueForItem(final Object itemId, final String column, final Object value) {
		this.targetTable.getItem(itemId).getItemProperty(column).setValue(value);
	}

	@Override
	public void propagateUIChanges() {
		this.resetEditableTable();
		this.listTabComponent.getListComponent().setHasUnsavedChanges(true);
	}

	protected void resetEditableTable() {
		// To trigger TableFieldFactory (fix for truncated data)
		if (this.targetTable.isEditable()) {
			this.targetTable.setEditable(false);
			this.targetTable.setEditable(true);
		}
	}

	@Override
	public void addColumn(final ColumnLabels columnLabel) {
		this.targetTable.addContainerProperty(columnLabel.getName(), String.class, "");
		this.targetTable.setColumnHeader(columnLabel.getName(), columnLabel.getTermNameFromOntology(this.ontologyDataManager));

	}

	@Override
	public boolean columnExists(final String columnName) {
		return AddColumnContextMenu.propertyExists(columnName, this.targetTable);
	}

	@Override
	public void addColumn(final String columnName) {
		this.targetTable.addContainerProperty(columnName.toUpperCase(), String.class, "");
		this.targetTable.setColumnHeader(columnName.toUpperCase(), columnName);
	}

	@Override
	public Window getWindow() {
		return this.targetTable.getWindow();
	}

	@Override
	public List<FillWithOption> getColumnsToExclude() {
		return Arrays.asList(FillWithOption.FILL_WITH_ATTRIBUTE);
	}

	@Override
	public List<Integer> getAllGids() {
		return this.getGidsToProcess();
	}
	
	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

}


package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

/**
 * This takes care of adding columns and generating values for those added columns when  there are items
 * loaded in Germplasm Search Results table. The item ids and GIDs for current page are available once the items are loaded.
 */
@Configurable
public class GermplasmSearchLoadedItemsAddColumnSource implements AddColumnSource {

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private PagedBreedingManagerTable targetTable;
	private String gidPropertyId;

	
	public GermplasmSearchLoadedItemsAddColumnSource(final PagedBreedingManagerTable targetTable, final String gidPropertyId) {
		super();
		this.targetTable = targetTable;
		this.gidPropertyId = gidPropertyId;
	}

	@Override
	public List<Object> getItemIdsToProcess() {
		return this.targetTable.getAllEntriesForPage(this.targetTable.getCurrentPage());
	}

	@Override
	public List<Integer> getGidsToProcess() {
		final List<Integer> gids = new ArrayList<>();
		final List<Object> listDataItemIds = this.getItemIdsToProcess();
		for (final Object itemId : listDataItemIds) {
			gids.add(this.getGidForItemId(itemId));
		}
		return gids;
	}

	@Override
	public Integer getGidForItemId(final Object itemId) {
		return Integer.valueOf(this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue().toString());
	}

	@Override
	public void setColumnValueForItem(final Object itemId, final String column, final Object value) {
		final Item item = this.targetTable.getContainerDataSource().getItem(itemId);
		if (item != null) {
			final Property itemProperty = item.getItemProperty(column);
			// During first addition of added column, the property does not exist for item yet
			if (itemProperty == null) {
				item.addItemProperty(column, new ObjectProperty<>(value));
			} else {
				itemProperty.setValue(value);
			}
		}
	}

	@Override
	public void propagateUIChanges() {
		this.targetTable.refreshRowCache();
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

}

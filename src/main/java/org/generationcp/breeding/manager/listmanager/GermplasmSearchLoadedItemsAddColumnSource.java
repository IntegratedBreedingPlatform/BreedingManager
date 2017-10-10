
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.ui.Table;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Window;

/**
 * This takes care of adding columns and generating values for those added columns when  there are items
 * loaded in Germplasm Search Results table. The item ids and GIDs for current page are available once the items are loaded.
 */
@Configurable
public class GermplasmSearchLoadedItemsAddColumnSource implements AddColumnSource {

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private PagedBreedingManagerTable targetTable;
	private GermplasmSearchResultsComponent searchResultsComponent;
	private String gidPropertyId;

	
	public GermplasmSearchLoadedItemsAddColumnSource(final PagedBreedingManagerTable targetTable, final GermplasmSearchResultsComponent searchResultsComponent, final String gidPropertyId) {
		super();
		this.targetTable = targetTable;
		this.gidPropertyId = gidPropertyId;
		this.searchResultsComponent = searchResultsComponent;
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

		final LazyQueryDefinition definition = this.searchResultsComponent.getDefinition();

		if (!definition.getPropertyIds().contains(columnLabel.getName())) {

			definition.addProperty(columnLabel.getName(), String.class, "", false, true);

			this.targetTable.setColumnHeader(columnLabel.getName(), columnLabel.getTermNameFromOntology(this.ontologyDataManager));
			targetTable.addGeneratedColumn(columnLabel.getName(), new Table.ColumnGenerator() {

				@Override
				public Object generateCell(final Table table, final Object o, final Object o1) {
					return table.getItem(o).getItemProperty(o1).getValue();
				}
			});

		}

	}

	@Override
	public boolean columnExists(final String columnName) {
		return AddColumnContextMenu.propertyExists(columnName, this.targetTable);
	}

	@Override
	public void addColumn(final String columnName) {
		if (!this.columnExists(columnName.toUpperCase())) {
			this.targetTable.addContainerProperty(columnName.toUpperCase(), String.class, "");
			this.targetTable.setColumnHeader(columnName, columnName);
		}
		final LazyQueryDefinition definition = this.searchResultsComponent.getDefinition();
		if (!definition.getPropertyIds().contains(columnName)) {
			definition.addProperty(columnName, String.class, "", false, false);
		}
	}

	@Override
	public Window getWindow() {
		return this.targetTable.getWindow();
	}
	
	@Override
	public List<FillWithOption> getColumnsToExclude() {
		return Arrays.asList(FillWithOption.FILL_WITH_LOCATION, FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
	}
	
	@Override
	public List<Integer> getAllGids() {
		return this.searchResultsComponent.getAllGids();
	}

	
	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

}

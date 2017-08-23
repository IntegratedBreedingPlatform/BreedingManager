/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * <p>
 * Generation Challenge Programme (GCP)
 * <p>
 * <p>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.breeding.manager.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.AddColumnContextMenu;
import org.generationcp.breeding.manager.listmanager.AddedColumnsMapper;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchItemsToLoadFillColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.Listener.LotDetailsButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

/**
 * An implementation of Query which is needed for using the LazyQueryContainer.
 */
@Configurable
public class GermplasmQuery implements Query {
	
	public  static final String GID_REF_PROPERTY = ColumnLabels.GID.getName() + "_REF";
	
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmQuery.class);
	private final QueryDefinition definition;
	private final ListManagerMain listManagerMain;
	private final Table matchingGermplasmsTable;
	private final GermplasmSearchParameter searchParameter;
	@Resource
	private GermplasmDataManager germplasmDataManager;
	@Resource
	private LocationDataManager locationDataManager;
	@Resource
	private PedigreeService pedigreeService;
	@Resource
	private CrossExpansionProperties crossExpansionProperties;
	private boolean viaToolUrl = true;
	private boolean showAddToList = true;
	private int size;

	public GermplasmQuery(final ListManagerMain listManagerMain, final boolean viaToolUrl, final boolean showAddToList,
			final GermplasmSearchParameter searchParameter, final Table matchingGermplasmsTable, final QueryDefinition definition) {

		super();
		this.listManagerMain = listManagerMain;
		this.viaToolUrl = viaToolUrl;
		this.showAddToList = showAddToList;
		this.searchParameter = searchParameter;
		this.matchingGermplasmsTable = matchingGermplasmsTable;
		this.size = -1;
		this.definition = definition;
	}

	/**
	 * This should only be relevant for tables with editing (add new) feature, this should never be called
	 */
	@Override
	public Item constructItem() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create List of Items to feed to the Paged table
	 *
	 * @param startIndex - the starting index for the entry
	 * @param count - the number of items for current page
	 * @return
	 */
	@Override
	public List<Item> loadItems(final int startIndex, final int count) {
		GermplasmQuery.LOG.info(String.format("LoadItems(%d,%d): %s", startIndex, count, this.searchParameter));
		final List<Item> items = new ArrayList<>();
		final List<Germplasm> germplasmResults = this.getGermplasmSearchResults(startIndex, count);
		final List<Integer> gids = new ArrayList<>();
		for (final Germplasm germplasmToGeneratePedigreeStringsFor : germplasmResults) {
			gids.add(germplasmToGeneratePedigreeStringsFor.getGid());
		}

		final Map<Integer, String> pedigreeStringMap = this.pedigreeService.getCrossExpansions(new HashSet<>(gids), null, this.crossExpansionProperties);
		final Map<Integer, String> preferredNamesMap = this.germplasmDataManager.getPreferredNamesByGids(gids);
		for (int i = 0; i < germplasmResults.size(); i++) {
			items.add(this.getGermplasmItem(germplasmResults.get(i), i + startIndex, pedigreeStringMap, preferredNamesMap));
		}
		
		// Generate values for added columns, if any were added to table
		final List<Object> columns = Arrays.asList(this.matchingGermplasmsTable.getVisibleColumns());
		if (AddColumnContextMenu.sourceHadAddedColumn(columns.toArray())) {
			final GermplasmSearchItemsToLoadFillColumnSource fillColumnSource = new GermplasmSearchItemsToLoadFillColumnSource(items, gids);
			final AddedColumnsMapper addedColumnsMapper = new AddedColumnsMapper(fillColumnSource);
			addedColumnsMapper.generateValuesForAddedColumns(columns.toArray());
		}

		return items;
	}

	@Override
	public void saveItems(final List<Item> arg0, final List<Item> arg1, final List<Item> arg2) {
		throw new UnsupportedOperationException();

	}

	@Override
	public int size() {
		if (this.size == -1) {
			final String q = this.searchParameter.getSearchKeyword();
			final Operation o = this.searchParameter.getOperation();
			final boolean includeParents = this.searchParameter.isIncludeParents();
			final boolean withInventoryOnly = this.searchParameter.isWithInventoryOnly();
			final boolean includeMGMembers = this.searchParameter.isIncludeMGMembers();

			this.size = this.germplasmDataManager.countSearchForGermplasm(q, o, includeParents, withInventoryOnly, includeMGMembers);
		}
		return this.size;
	}

	@SuppressWarnings("rawtypes")
	Item getGermplasmItem(final Germplasm germplasm, final int index, final Map<Integer, String> pedigreeStringMap,
			final Map<Integer, String> preferredNamesMap) {

		final Integer gid = germplasm.getGid();
		final GermplasmInventory inventoryInfo = germplasm.getInventoryInfo();

		final Item item = new PropertysetItem();

		final Map<String, ObjectProperty> propertyMap = new HashMap<>();
		propertyMap.put(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID, new ObjectProperty<>(this.getItemCheckBox(index)));
		propertyMap.put(GermplasmSearchResultsComponent.NAMES, new ObjectProperty<>(this.getNamesButton(gid)));
		propertyMap.put(ColumnLabels.PARENTAGE.getName(), new ObjectProperty<>(pedigreeStringMap.get(gid)));
		propertyMap.put(ColumnLabels.AVAILABLE_INVENTORY.getName(),
				new ObjectProperty<>(this.getInventoryInfoButton(germplasm, preferredNamesMap)));
		propertyMap.put(ColumnLabels.TOTAL.getName(), new ObjectProperty<>(this.getAvailableBalanceButton(germplasm)));
		propertyMap.put(ColumnLabels.STOCKID.getName(), new ObjectProperty<>(this.getStockIDs(inventoryInfo)));
		propertyMap.put(ColumnLabels.GID.getName(), new ObjectProperty<>(this.getGidButton(gid)));
		propertyMap.put(ColumnLabels.GROUP_ID.getName(), new ObjectProperty<>(germplasm.getMgid() != 0 ? germplasm.getMgid() : "-"));
		propertyMap.put(ColumnLabels.GERMPLASM_LOCATION.getName(), new ObjectProperty<>(germplasm.getLocationName()));
		propertyMap.put(ColumnLabels.BREEDING_METHOD_NAME.getName(), new ObjectProperty<>(germplasm.getMethodName()));
		propertyMap.put(GID_REF_PROPERTY, new ObjectProperty<>(gid));

		for (final String propertyId : propertyMap.keySet()) {
			item.addItemProperty(propertyId, propertyMap.get(propertyId));
		}

		return item;
	}

	String getShortenedNames(final String germplasmFullName) {
		return germplasmFullName.length() > 20 ? germplasmFullName.substring(0, 20) + "..." : germplasmFullName;
	}

	protected List<Germplasm> getGermplasmSearchResults(final int startIndex, final int count) {
		this.searchParameter.setStartingRow(startIndex);
		this.searchParameter.setNumberOfEntries(count);
		return this.germplasmDataManager.searchForGermplasm(this.searchParameter);
	}

	private Button getGidButton(final Integer gid) {
		final Button gidButton = new Button(String.format("%s", gid.toString()), this.createGermplasmListener(gid));
		gidButton.setDebugId("gidButton");
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		return gidButton;
	}

	private Button getInventoryInfoButton(final Germplasm germplasm, final Map<Integer, String> preferredNamesMap) {
		String availInv = "-";
		availInv = germplasm.getInventoryInfo().getActualInventoryLotCount().toString().trim();
		final Integer gid = germplasm.getGid();
		final String germplasmName = preferredNamesMap.get(gid);
		final Button inventoryButton =
				new Button(availInv, new LotDetailsButtonClickListener(gid, germplasmName, this.listManagerMain, null));
		inventoryButton.setDebugId("inventoryButton");
		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
		return inventoryButton;
	}

	private Button getAvailableBalanceButton(final Germplasm germplasm) {
		final StringBuilder available = new StringBuilder();

		if (germplasm.getInventoryInfo().getScaleForGermplsm() != null) {
			if (GermplasmInventory.MIXED.equals(germplasm.getInventoryInfo().getScaleForGermplsm())) {
				available.append(germplasm.getInventoryInfo().getScaleForGermplsm());
			} else {
				available.append(germplasm.getInventoryInfo().getTotalAvailableBalance());
				available.append(" ");
				available.append(germplasm.getInventoryInfo().getScaleForGermplsm());
			}

		} else {
			available.append("-");
		}

		final Button inventoryButton =
				new Button(available.toString(), new InventoryLinkButtonClickListener(this.listManagerMain, germplasm.getGid()));
		inventoryButton.setDebugId("inventoryButton");
		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
		return inventoryButton;

	}

	private GidLinkButtonClickListener createGermplasmListener(final Integer gid) {
		return new GidLinkButtonClickListener(this.listManagerMain, String.valueOf(gid), this.viaToolUrl, this.showAddToList);
	}

	private Button getNamesButton(final Integer gid) {
		final String germplasmFullName = this.getGermplasmNames(gid);
		final String shortenedNames = this.getShortenedNames(germplasmFullName);

		final Button namesButton = new Button(shortenedNames, this.createGermplasmListener(gid));
		namesButton.setDebugId("namesButton");
		namesButton.setStyleName(BaseTheme.BUTTON_LINK);
		namesButton.setDescription(germplasmFullName);

		return namesButton;
	}

	private CheckBox getItemCheckBox(final Integer itemIndex) {
		final CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setDebugId("itemCheckBox");
		itemCheckBox.setData(itemIndex);
		itemCheckBox.setImmediate(true);

		// TODO needs to extract this listener so that the matching germplasms table will not be tightly coupled to this class
		itemCheckBox.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				final CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (((Boolean) itemCheckBox.getValue()).equals(true)) {
					GermplasmQuery.this.matchingGermplasmsTable.select(itemCheckBox.getData());
				} else {
					GermplasmQuery.this.matchingGermplasmsTable.unselect(itemCheckBox.getData());
				}
			}

		});
		return itemCheckBox;
	}

	private String getGermplasmNames(final int gid) {
		final StringBuilder germplasmNames = new StringBuilder("");

		final List<Name> names = this.germplasmDataManager.getNamesByGID(new Integer(gid), null, null);

		int i = 0;
		for (final Name n : names) {
			if (i < names.size() - 1) {
				germplasmNames.append(n.getNval() + ", ");
			} else {
				germplasmNames.append(n.getNval());
			}
			i++;
		}

		return germplasmNames.toString();
	}

	private Label getStockIDs(final GermplasmInventory inventoryInfo) {
		final String stockIDs = inventoryInfo.getStockIDs();
		final Label stockLabel = new Label(stockIDs);
		stockLabel.setDebugId("stockLabel");
		stockLabel.setDescription(stockIDs);
		return stockLabel;
	}
}

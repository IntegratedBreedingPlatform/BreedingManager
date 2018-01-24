/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.breeding.manager.containers;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.Listener.LotDetailsButtonClickListener;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of Query which is needed for using the LazyQueryContainer.
 */
@Configurable
public class GermplasmQuery implements Query {

	public static final int RESULTS_LIMIT = 5000;

	public static final String GID_REF_PROPERTY = ColumnLabels.GID.getName() + "_REF";

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmQuery.class);
	private final QueryDefinition definition;
	private final ListManagerMain listManagerMain;
	private final Table matchingGermplasmsTable;
	private final GermplasmSearchParameter searchParameter;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private PedigreeService pedigreeService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	private static final List<String> DEFAULT_COLUMNS = new ArrayList<>();

	static {

		DEFAULT_COLUMNS.add(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID);
		DEFAULT_COLUMNS.add(GermplasmSearchResultsComponent.NAMES);
		DEFAULT_COLUMNS.add(ColumnLabels.PARENTAGE.getName());
		DEFAULT_COLUMNS.add(ColumnLabels.AVAILABLE_INVENTORY.getName());
		DEFAULT_COLUMNS.add(ColumnLabels.TOTAL.getName());
		DEFAULT_COLUMNS.add(ColumnLabels.STOCKID.getName());
		DEFAULT_COLUMNS.add(ColumnLabels.GID.getName());
		DEFAULT_COLUMNS.add(ColumnLabels.GROUP_ID.getName());
		DEFAULT_COLUMNS.add(ColumnLabels.GERMPLASM_LOCATION.getName());
		DEFAULT_COLUMNS.add(ColumnLabels.BREEDING_METHOD_NAME.getName());
		DEFAULT_COLUMNS.add(GermplasmQuery.GID_REF_PROPERTY);

	}

	private boolean viaToolUrl = true;
	private boolean showAddToList = true;
	private int size;
	private List<Integer> allGids;

	public GermplasmQuery(final ListManagerMain listManagerMain, final boolean viaToolUrl, final boolean showAddToList,
			final GermplasmSearchParameter searchParameter, final Table matchingGermplasmsTable, final QueryDefinition definition) {

		super();
		this.listManagerMain = listManagerMain;
		this.viaToolUrl = viaToolUrl;
		this.showAddToList = showAddToList;
		this.searchParameter = searchParameter;
		this.matchingGermplasmsTable = matchingGermplasmsTable;
		this.size = -1;
		this.allGids = new ArrayList<>();
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

		final Map<Integer, String> pedigreeStringMap =
				this.pedigreeService.getCrossExpansions(new HashSet<>(gids), null, this.crossExpansionProperties);
		final Map<Integer, String> preferredNamesMap = this.germplasmDataManager.getPreferredNamesByGids(gids);
		final Map<Integer, String> immediatePreferredNameByGidMap = this.germplasmDataManager.getImmediateSourcePreferredNamesByGids(gids);
		final Map<Integer, String> groupSourcepreferredNameByGidMap = this.germplasmDataManager.getGroupSourcePreferredNamesByGids(gids);

		for (int i = 0; i < germplasmResults.size(); i++) {
			items.add(this.getGermplasmItem(germplasmResults.get(i), i + startIndex, pedigreeStringMap, preferredNamesMap,
				immediatePreferredNameByGidMap, groupSourcepreferredNameByGidMap));
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
			this.retrieveGIDsofMatchingGermplasm();
			this.size = this.allGids.size();
		}
		return this.size;
	}

	@SuppressWarnings("rawtypes")
	Item getGermplasmItem(final Germplasm germplasm, final int index, final Map<Integer, String> pedigreeStringMap,
			final Map<Integer, String> preferredNamesMap, final Map<Integer, String> immediatePreferredNameByGidMap, final Map<Integer, String> groupSourcepreferredNameByGidMap) {

		final Integer gid = germplasm.getGid();
		final GermplasmInventory inventoryInfo = germplasm.getInventoryInfo();

		final Item item = new PropertysetItem();

		final Map<String, ObjectProperty> propertyMap = new HashMap<>();
		propertyMap.put(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID, new ObjectProperty<>(this.getItemCheckBox(index)));
		propertyMap.put(GermplasmSearchResultsComponent.NAMES,
			new ObjectProperty<>(this.getNamesButton(germplasm.getGermplasmNamesString(), germplasm.getGid())));
		propertyMap.put(ColumnLabels.PARENTAGE.getName(), new ObjectProperty<>(pedigreeStringMap.get(gid)));
		propertyMap.put(ColumnLabels.AVAILABLE_INVENTORY.getName(),
			new ObjectProperty<>(this.getInventoryInfoButton(germplasm, preferredNamesMap)));
		propertyMap.put(ColumnLabels.TOTAL.getName(), new ObjectProperty<>(this.getAvailableBalanceButton(germplasm)));
		propertyMap.put(ColumnLabels.STOCKID.getName(), new ObjectProperty<>(this.getStockIDs(inventoryInfo)));
		propertyMap.put(ColumnLabels.GID.getName(), new ObjectProperty<>(this.getGidButton(gid)));
		propertyMap.put(ColumnLabels.GROUP_ID.getName(), new ObjectProperty<>(germplasm.getMgid() != 0 ? germplasm.getMgid() : "-"));
		propertyMap.put(ColumnLabels.GERMPLASM_LOCATION.getName(), new ObjectProperty<>(germplasm.getLocationName()));
		propertyMap.put(ColumnLabels.BREEDING_METHOD_NAME.getName(), new ObjectProperty<>(germplasm.getMethodName()));
		propertyMap.put(GermplasmQuery.GID_REF_PROPERTY, new ObjectProperty<>(gid));
		propertyMap.put(ColumnLabels.GERMPLASM_DATE.getName(), new ObjectProperty<>(germplasm.getGermplasmDate()));
		propertyMap.put(ColumnLabels.PREFERRED_ID.getName(), new ObjectProperty<>(germplasm.getGermplasmPeferredId()));
		propertyMap.put(ColumnLabels.PREFERRED_NAME.getName(), new ObjectProperty<>(germplasm.getGermplasmPeferredName()));
		propertyMap.put(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(), new ObjectProperty<>(germplasm.getMethodCode()));
		propertyMap.put(ColumnLabels.BREEDING_METHOD_NUMBER.getName(), new ObjectProperty<>(germplasm.getMethodId()));
		propertyMap.put(ColumnLabels.BREEDING_METHOD_GROUP.getName(), new ObjectProperty<>(germplasm.getMethodGroup()));
		propertyMap.put(ColumnLabels.CROSS_FEMALE_GID.getName(), new ObjectProperty<>(germplasm.getFemaleParentPreferredID()));
		propertyMap.put(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), new ObjectProperty<>(germplasm.getFemaleParentPreferredName()));
		propertyMap.put(ColumnLabels.CROSS_MALE_GID.getName(), new ObjectProperty<>(germplasm.getMaleParentPreferredID()));
		propertyMap.put(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), new ObjectProperty<>(germplasm.getMaleParentPreferredName()));
		final String groupSourceGid = germplasm.getGnpgs() == -1 && null != germplasm.getGpid1() ? germplasm.getGpid1().toString() : "-";
		propertyMap.put(ColumnLabels.GROUP_SOURCE_GID.getName(), new ObjectProperty<>(groupSourceGid));
		propertyMap.put(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName(), new ObjectProperty<>(groupSourcepreferredNameByGidMap.get(gid)));
		final String immediateSourceGid = germplasm.getGnpgs() == -1 && null != germplasm.getGpid2() ? germplasm.getGpid2().toString() : "-";
		propertyMap.put(ColumnLabels.IMMEDIATE_SOURCE_GID.getName(), new ObjectProperty<>(immediateSourceGid));
		propertyMap.put(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName(), new ObjectProperty<>(immediatePreferredNameByGidMap.get(gid)));

		for (final Map.Entry<String, String> entry : germplasm.getAttributeTypesValueMap().entrySet()) {
			final String attributeTypePropertyId = entry.getKey();
			final String attributeTypeValue = entry.getValue();
			propertyMap.put(attributeTypePropertyId, new ObjectProperty<>(attributeTypeValue));
		}

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


		// Retrieve and set the names of 'Fill With' columns added to the table so that search query will generate values for them.
		this.searchParameter
				.setAddedColumnsPropertyIds(getPropertyIdsOfAddableColumns(this.definition.getPropertyIds()));


		return this.germplasmDataManager.searchForGermplasm(this.searchParameter);
	}

	protected List<String> getPropertyIdsOfAddableColumns(final Collection<?> propertyIds) {

		final List<String> propertyIdsOfColumnsAdded = new LinkedList<>();

		for (final String propertyId : (Collection<? extends String>) propertyIds) {
			if (!DEFAULT_COLUMNS.contains(propertyId)) {
				propertyIdsOfColumnsAdded.add(propertyId);
			}
		}

		return propertyIdsOfColumnsAdded;

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

	private Button getNamesButton(final String germplasmFullNames, final Integer gid) {

		final String shortenedNames = this.getShortenedNames(germplasmFullNames);

		final Button namesButton = new Button(shortenedNames, this.createGermplasmListener(gid));
		namesButton.setDebugId("namesButton");
		namesButton.setStyleName(BaseTheme.BUTTON_LINK);
		namesButton.setDescription(germplasmFullNames);

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

	private Label getStockIDs(final GermplasmInventory inventoryInfo) {
		final String stockIDs = inventoryInfo.getStockIDs();
		final Label stockLabel = new Label(stockIDs);
		stockLabel.setDebugId("stockLabel");
		stockLabel.setDescription(stockIDs);
		return stockLabel;
	}

	void retrieveGIDsofMatchingGermplasm() {

		final GermplasmSearchParameter searchAllParameter = new GermplasmSearchParameter(this.searchParameter);
		final Set<Integer> allGermplasmGids = this.germplasmDataManager.retrieveGidsOfSearchGermplasmResult(searchAllParameter);

		this.allGids = new ArrayList<>(allGermplasmGids);

	}

	public List<Integer> getAllGids() {
		return this.allGids;
	}

}

package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.inventory.InventoryDropTargetContainer;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.AddColumnContextMenu;
import org.generationcp.breeding.manager.listmanager.AddedColumnsMapper;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.NewGermplasmEntriesFillColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

public class DropHandlerMethods {

	private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";

	private static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";

	private static final String NO_LOT_FOR_THIS_GERMPLASM = "No Lot for this Germplasm";

	private static final String STRING_EMPTY = "";

	private static final String STRING_DASH = "-";

	protected Table targetTable;

	protected GermplasmDataManager germplasmDataManager;
	protected GermplasmListManager germplasmListManager;
	protected InventoryDataManager inventoryDataManager;
	protected PedigreeService pedigreeService;
	protected PlatformTransactionManager transactionManager;

	private static final Logger LOG = LoggerFactory.getLogger(DropHandlerMethods.class);

	/**
	 * Temporary data holders / caching instead of loading it all the time
	 */
	protected Integer currentListId;
	protected GermplasmListNewColumnsInfo currentColumnsInfo;

	protected boolean changed = false;
	protected ListManagerMain listManagerMain;
	protected InventoryDropTargetContainer inventoryDropTargetContainer;

	protected List<ListUpdatedListener> listeners = null;

	protected static final String MATCHING_GERMPLASMS_TABLE_DATA = GermplasmSearchResultsComponent.MATCHING_GEMRPLASM_TABLE_DATA;
	protected static final String MATCHING_LISTS_TABLE_DATA = ListSearchResultsComponent.MATCHING_LISTS_TABLE_DATA;
	protected static final String LIST_DATA_TABLE_DATA = ListComponent.LIST_DATA_COMPONENT_TABLE_DATA;

	protected CrossExpansionProperties crossExpansionProperties;

	@SuppressWarnings("unchecked")
	protected Boolean hasSelectedItems(final Table table) {
		final List<Integer> selectedItemIds = new ArrayList<Integer>();
		selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
		if (!selectedItemIds.isEmpty()) {
			return true;
		}
		return false;
	}

	protected void addSelectedGermplasmListsFromTable(final Table sourceTable) {
		final List<Integer> selectedGermplasmListIds = this.getSelectedItemIds(sourceTable);
		for (final Integer listId : selectedGermplasmListIds) {
			this.addGermplasmList(listId);
		}
	}

	public void addGermplasmList(final Integer listId) {
		this.addGermplasmList(listId, false);
	}

	public void addGermplasmList(final Integer listId, final Boolean fromEditList) {

		this.currentListId = listId;

		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {

				// Load currentColumnsInfo if cached list info is null or not matching the needed list id
				if (DropHandlerMethods.this.currentColumnsInfo == null || !DropHandlerMethods.this.currentColumnsInfo.getListId()
						.equals(listId)) {
					DropHandlerMethods.this.currentColumnsInfo =
							DropHandlerMethods.this.germplasmListManager.getAdditionalColumnsForList(listId);
				}

				final GermplasmList germplasmList = DropHandlerMethods.this.getGermplasmList(listId);
				final List<GermplasmListData> germplasmListData = germplasmList.getListData();

				// Fix for adding entries in reverse
				if (germplasmListData.size() > 1 && germplasmListData.get(0).getEntryId() > germplasmListData.get(1).getEntryId()) {
					Collections.reverse(germplasmListData);
				}

				for (final GermplasmListData listData : germplasmListData) {
					DropHandlerMethods.this.addGermplasmFromList(listId, listData.getId(), germplasmList, fromEditList);
				}

				// mark that there is changes in a list that is currently building
				DropHandlerMethods.this.changed = true;

				DropHandlerMethods.this.currentColumnsInfo = null;
				DropHandlerMethods.this.currentListId = null;

				DropHandlerMethods.this.fireListUpdatedEvent();

			}
		});

	}

	protected void addSelectedGermplasmsFromTable(final Table sourceTable) {
		final List<Integer> selectedGermplasmIds = this.getSelectedItemIds(sourceTable);
		final List<Integer> gidList = new ArrayList<>();
		for (final Integer itemId : selectedGermplasmIds) {
			// note, for paged table, the itemId !== GID, but there is a hidden reference GID there so we can retrive actual GID
			// todo: lets cleanup that "_REF" string later
			final Property internalGIDReference = sourceTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName() + "_REF");

			if (internalGIDReference != null && internalGIDReference.getValue() != null) {
				gidList.add((Integer) internalGIDReference.getValue());
			} else {
				gidList.add(itemId);
			}
		}

		this.addGermplasm(gidList);
	}

	public void addGermplasm(final List<Integer> gids) {

		final Map<Integer, String> crossExpansions = this.getCrossExpansions(gids);
		final Map<Integer, Germplasm> gidGermplasmMap = this.generateGidGermplasmMap(gids);
		final Map<Integer, String> preferredNames = this.germplasmDataManager.getPreferredNamesByGids(gids);
		final Map<Integer, Germplasm> germplsmWithAvailableBalance = this.getAvailableBalanceWithScaleForGermplasm(gidGermplasmMap);

		try {
			final List<Integer> newItemIds = new ArrayList<>();
			for (final Integer gid : gids) {
				final Integer newItemId = this.getNextListEntryId();
				newItemIds.add(newItemId);
				final Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

				final Button gidButton = new Button(String.format("%s", gid),
						new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);

				final String crossExpansion = crossExpansions.get(gid);
				final String preferredName = preferredNames.get(gid);
				final Button designationButton =
						new Button(preferredName, new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
				designationButton.setStyleName(BaseTheme.BUTTON_LINK);
				designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

				final CheckBox tagCheckBox = new CheckBox();
				tagCheckBox.setDebugId("tagCheckBox");
				tagCheckBox.setImmediate(true);
				tagCheckBox.addListener(new ClickListener() {

					protected static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
						final CheckBox itemCheckBox = (CheckBox) event.getButton();
						if (((Boolean) itemCheckBox.getValue()).equals(true)) {
							DropHandlerMethods.this.targetTable.select(newItemId);
						} else {
							DropHandlerMethods.this.targetTable.unselect(newItemId);
						}
					}

				});

				final Germplasm germplasm = gidGermplasmMap.get(gid);
				final String groupIdDisplayValue = germplasm.getMgid() == 0 ? "-" : germplasm.getMgid().toString();
				newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupIdDisplayValue);

				// Inventory Related Columns

				// #1 Available Inventory
				String availInv = DropHandlerMethods.STRING_EMPTY;
				final Integer availInvGid = this.getAvailInvForGID(gid);
				if (availInvGid != null) {
					availInv = availInvGid.toString();
				}

				final Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(this.listManagerMain, gid));
				inventoryButton.setDebugId("inventoryButton");
				inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
				inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
				newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

				if (availInv.equals(DropHandlerMethods.STRING_DASH)) {
					inventoryButton.setEnabled(false);
					inventoryButton.setDescription(DropHandlerMethods.NO_LOT_FOR_THIS_GERMPLASM);
				} else {
					inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
				}

				// Available
				final StringBuilder available = new StringBuilder();
				final Germplasm germplasmWithAvailableBalance = germplsmWithAvailableBalance.get(gid);

				if(germplasmWithAvailableBalance != null) {

					if (germplasm.getInventoryInfo().getScaleForGermplsm() != null) {
						if(ListDataInventory.MIXED.equals(germplasm.getInventoryInfo().getScaleForGermplsm())) {
							available.append(germplasm.getInventoryInfo().getScaleForGermplsm());
						} else {
							available.append(germplasm.getInventoryInfo().getTotalAvailableBalance());
							available.append(" ");
							available.append(germplasm.getInventoryInfo().getScaleForGermplsm());
						}

					} else {
						available.append(germplasm.getInventoryInfo().getTotalAvailableBalance());
					}

				} else {
					available.append(DropHandlerMethods.STRING_DASH);
				}

				final Button availableButton = new Button(available.toString(),
						new InventoryLinkButtonClickListener(this.listManagerMain, gid));
				availableButton.setDebugId("availableButton");
				availableButton.setStyleName(BaseTheme.BUTTON_LINK);
				availableButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
				newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(availableButton);


				newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(tagCheckBox);
				if (newItem != null && gidButton != null) {
					newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
				}

				// TODO get plot code values in bulk for all GIDS to improve performance
				newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(this.germplasmDataManager.getPlotCodeValue(gid));
				newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
				newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(crossExpansion);

				this.assignSerializedEntryNumber();
			}
			
			this.generateAddedColumnValuesForAddedEntry(newItemIds, gids);

			this.fireListUpdatedEvent();

			this.setHasUnsavedChanges(true);

		} catch (final MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error in adding germplasm to germplasm table.", e);
		}

	}

	private Map<Integer, Germplasm> generateGidGermplasmMap(final List<Integer> gids) {
		final List<Germplasm> germplasms = this.germplasmDataManager.getGermplasms(gids);
		final Map<Integer, Germplasm> gidGermplasmMap = new HashMap<>();
		for (final Germplasm germplasm : germplasms) {
			final Integer gid = germplasm.getGid();
			gidGermplasmMap.put(gid, germplasm);
		}
		return gidGermplasmMap;
	}

	private Map<Integer, Germplasm> getAvailableBalanceWithScaleForGermplasm(final Map<Integer, Germplasm> germplasmMap) {
		Map<Integer, Germplasm> availableBalanceWithScale = new HashMap<>();

		List<Germplasm> germplasmList = new ArrayList<>();
		for(Entry<Integer, Germplasm> entry : germplasmMap.entrySet()) {
			germplasmList.add(entry.getValue());
		}

		List<Germplasm> availableBalanceForGermplsms = this.inventoryDataManager.getAvailableBalanceForGermplasms(germplasmList);

		for(Germplasm germplasm : availableBalanceForGermplsms) {
			availableBalanceWithScale.put(germplasm.getGid(), germplasm);
		}

		return availableBalanceWithScale;

	}

	private Map<Integer, String> getCrossExpansions(final List<Integer> gids) {
		final Iterable<List<Integer>> partition = Iterables.partition(gids, 5000);

		final Map<Integer, String> crossExpansions = new HashMap<>();

		for (final List<Integer> partitionedGidList : partition) {
			final Set<Integer> partitionedGidSet = new HashSet<Integer>(partitionedGidList);
			crossExpansions.putAll(this.pedigreeService.getCrossExpansions(partitionedGidSet, null, this.crossExpansionProperties));
		}

		return crossExpansions;
	}

	protected Integer getAvailInvForGID(final Integer gid) {
		Integer availInv;
		try {
			availInv = this.inventoryDataManager.countLotsWithAvailableBalanceForGermplasm(gid);
			return availInv;
		} catch (final MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public Integer addGermplasmFromList(final Integer listId, final Integer lrecid) {
		return this.addGermplasmFromList(listId, lrecid, this.getGermplasmList(listId));
	}

	protected Integer addGermplasmFromList(final Integer listId, final Integer lrecid, final GermplasmList germplasmList) {
		return this.addGermplasmFromList(listId, lrecid, germplasmList, false);
	}

	Integer addGermplasmFromList(final Integer listId, final Integer lrecid, final GermplasmList germplasmList, final Boolean forEditList) {

		this.currentListId = listId;

		try {
			// Load currentColumnsInfo if cached list info is null or not matching the needed list id
			if (this.currentColumnsInfo == null || !this.currentColumnsInfo.getListId().equals(listId)) {
				this.currentColumnsInfo = this.germplasmListManager.getAdditionalColumnsForList(listId);
			}

			for (final Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
				final String column = columnEntry.getKey();
				if (!AddColumnContextMenu.propertyExists(column, this.targetTable)) {
					this.targetTable.addContainerProperty(column, String.class, DropHandlerMethods.STRING_EMPTY);
					this.targetTable.setColumnWidth(column, 250);
				}
			}

			final GermplasmListData germplasmListData = this.getListDataByListIdAndLrecId(listId, lrecid, germplasmList);

			// handles the data for inventory

			if (germplasmListData != null && germplasmListData.getStatus() != 9) {

				final Integer gid = germplasmListData.getGid();

				Integer niid = null;
				if (forEditList.equals(true)) {
					niid = this.getNextListEntryId(germplasmListData.getId());
				} else {
					niid = this.getNextListEntryId();
				}
				final Integer newItemId = niid;

				final Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

				final Button gidButton = new Button(String.format("%s", gid),
						new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);

				final CheckBox tagCheckBox = new CheckBox();
				tagCheckBox.setDebugId("tagCheckBox");
				tagCheckBox.setImmediate(true);
				tagCheckBox.addListener(new ClickListener() {

					protected static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
						final CheckBox itemCheckBox = (CheckBox) event.getButton();
						if (((Boolean) itemCheckBox.getValue()).equals(true)) {
							DropHandlerMethods.this.targetTable.select(newItemId);
						} else {
							DropHandlerMethods.this.targetTable.unselect(newItemId);
						}
					}

				});

				final Button designationButton = new Button(germplasmListData.getDesignation(),
						new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
				designationButton.setStyleName(BaseTheme.BUTTON_LINK);
				designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

				newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(tagCheckBox);
				if (newItem != null && gidButton != null) {
					newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
				}
				newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(germplasmListData.getEntryCode());
				newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(germplasmListData.getSeedSource());
				newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
				newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(germplasmListData.getGroupName());

				final Integer groupId = germplasmListData.getGroupId();
				final String groupIdDisplayValue = groupId == null || groupId == 0 ? "-" : groupId.toString();
				newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupIdDisplayValue);

				// Inventory Related Columns

				// Lots
				Button lotButton = ListCommonActionsUtil
						.getLotCountButton(germplasmListData.getInventoryInfo().getLotCount().intValue(), germplasmListData.getGid(),
								germplasmListData.getDesignation(), this.listManagerMain, null);
				newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(lotButton);

				StringBuilder available = new StringBuilder();

				if (germplasmListData.getInventoryInfo().getDistinctScaleCountForGermplsm() == 0) {
					available.append("-");
				} else if (germplasmListData.getInventoryInfo().getDistinctScaleCountForGermplsm() == 1) {
					available.append(germplasmListData.getInventoryInfo().getTotalAvailableBalance());
					available.append(" ");

					if (!StringUtils.isEmpty(germplasmListData.getInventoryInfo().getScaleForGermplsm())) {
						available.append(germplasmListData.getInventoryInfo().getScaleForGermplsm());
					}

				} else {
					available.append(ListDataInventory.MIXED);
				}

				final Button availableButton = new Button(available.toString(),
						new InventoryLinkButtonClickListener(this.listManagerMain, germplasmListData.getGid()));
				availableButton.setStyleName(BaseTheme.BUTTON_LINK);
				availableButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
				newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(availableButton);

				String stockIDs = DropHandlerMethods.STRING_EMPTY;
				if (germplasmListData.getInventoryInfo() != null && germplasmListData.getInventoryInfo().getStockIDs() != null) {
					stockIDs = germplasmListData.getInventoryInfo().getStockIDs();
				}
				newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIDs);

				for (final Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap()
						.entrySet()) {
					final String column = columnEntry.getKey();
					for (final ListDataColumnValues columnValue : columnEntry.getValue()) {
						if (columnValue.getListDataId().equals(germplasmListData.getId())) {
							final String value = columnValue.getValue();
							newItem.getItemProperty(column).setValue(value == null ? DropHandlerMethods.STRING_EMPTY : value);
						}
					}
				}

				this.assignSerializedEntryNumber();

				this.generateAddedColumnValuesForAddedEntry(Arrays.asList(newItemId), Arrays.asList(gid));

				this.currentListId = null;

				this.fireListUpdatedEvent();
				return newItemId;
			}

			this.fireListUpdatedEvent();
			return null;

		} catch (final MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error in adding germplasm to germplasm table.", e);
			this.currentColumnsInfo = null;
			this.currentListId = null;

			return null;
		}

	}

	public GermplasmListData getListDataByListIdAndLrecId(final Integer listId, final Integer lrecid, final GermplasmList germplasmList) {
		GermplasmListData germplasmListData =
				this.inventoryDataManager.getLotCountsForListEntries(listId, Lists.newArrayList(lrecid)).get(0);
		return germplasmListData;
	}

	List<Integer> extractGidsFromTable(final Table sourceTable, final List<Integer> selectedTableItemIds) {

		final List<Integer> gids = new ArrayList<>();
		for (final Integer itemId : selectedTableItemIds) {
			Integer gid = getGidFromButtonCaption(sourceTable, itemId);
			gids.add(gid);
		}

		return gids;

	}

	public void addFromListDataTable(final Table sourceTable) {
		final List<Integer> itemIds = this.getSelectedItemIds(sourceTable);

		final List<Integer> gids = extractGidsFromTable(sourceTable, itemIds);

		final Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(gids);

		Integer listId = null;
		if (sourceTable.getParent() instanceof TableWithSelectAllLayout && sourceTable.getParent().getParent() instanceof ListComponent) {
			listId = ((ListComponent) sourceTable.getParent().getParent()).getGermplasmListId();
		}

		// Load currentColumnsInfo if cached list info is null or not matching the needed list id
		if (this.currentColumnsInfo == null || !this.currentColumnsInfo.getListId().equals(listId)) {
			try {
				this.currentColumnsInfo = this.germplasmListManager.getAdditionalColumnsForList(listId);
			} catch (final MiddlewareQueryException e) {
				DropHandlerMethods.LOG.error("Error During getAdditionalColumnsForList(" + listId + "): " + e);
			}
		}

		for (final Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
			final String column = columnEntry.getKey();
			if (!AddColumnContextMenu.propertyExists(column, this.targetTable)) {
				this.targetTable.addContainerProperty(column, String.class, DropHandlerMethods.STRING_EMPTY);
				this.targetTable.setColumnWidth(column, 250);
			}
		}
		final List<Integer> newItemIds = new ArrayList<>();
		for (final Integer itemId : itemIds) {

			final Item itemFromSourceTable = sourceTable.getItem(itemId);
			final Integer newItemId = this.getNextListEntryId();
			newItemIds.add(newItemId);
			final Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

			final Integer gid = this.getGidFromButtonCaption(sourceTable, itemId);
			final Button gidButton =
					new Button(String.format("%s", gid), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			gidButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			final CheckBox itemCheckBox = new CheckBox();
			itemCheckBox.setDebugId("itemCheckBox");
			itemCheckBox.setData(newItemId);
			itemCheckBox.setImmediate(true);
			itemCheckBox.addListener(new ClickListener() {

				protected static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
					final CheckBox itemCheckBox = (CheckBox) event.getButton();
					if (((Boolean) itemCheckBox.getValue()).equals(true)) {
						DropHandlerMethods.this.targetTable.select(itemCheckBox.getData());
					} else {
						DropHandlerMethods.this.targetTable.unselect(itemCheckBox.getData());
					}
				}

			});

			final String designation = preferredNames.get(gid) != null ? preferredNames.get(gid) : this.getDesignationFromButtonCaption(sourceTable, itemId);
			final Button designationButton =
					new Button(designation, new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			designationButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			// Inventory Related Columns

			// #1 Available Inventory
			final String availInv = this.getAvailInvFromButtonCaption(sourceTable, itemId);
			final Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(this.listManagerMain, gid));
			inventoryButton.setDebugId("inventoryButton");
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);

			if (availInv.equals(DropHandlerMethods.STRING_DASH)) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription(DropHandlerMethods.NO_LOT_FOR_THIS_GERMPLASM);
			} else {
				inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
			}

			final String parentage = (String) itemFromSourceTable.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
			final String entryCode = (String) itemFromSourceTable.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue();
			final String seedSource = (String) itemFromSourceTable.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue();
			final Object groupId = itemFromSourceTable.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue();

			// Available
			final Button availableButton = this.getAvailableBalanceButton(sourceTable, itemId, gid);

			newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
			newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
			newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupId);
			newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(seedSource);
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
			newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(parentage);
			newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entryCode);
			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
			newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(availableButton);

			newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(DropHandlerMethods.STRING_EMPTY);

			for (final Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
				final String column = columnEntry.getKey();
				for (final ListDataColumnValues columnValue : columnEntry.getValue()) {
					if (columnValue.getListDataId().equals(itemId)) {
						final String value = columnValue.getValue();
						newItem.getItemProperty(column).setValue(value == null ? DropHandlerMethods.STRING_EMPTY : value);
					}
				}
			}

		}

		this.assignSerializedEntryNumber();
		this.generateAddedColumnValuesForAddedEntry(newItemIds, gids);
		
		this.fireListUpdatedEvent();

		this.setHasUnsavedChanges(true);
	}
	
	private void generateAddedColumnValuesForAddedEntry(final List<Integer> itemIds, final List<Integer> gids){
		final NewGermplasmEntriesFillColumnSource fillColumnSource = new NewGermplasmEntriesFillColumnSource(this.targetTable, itemIds, gids);
		final AddedColumnsMapper addedColumnsMapper = new AddedColumnsMapper(fillColumnSource);
		addedColumnsMapper.generateValuesForAddedColumns(this.targetTable.getVisibleColumns());
	}

	/**
	 * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
	 */
	protected void assignSerializedEntryNumber() {
		final List<Integer> itemIds = this.getItemIds(this.targetTable);

		int id = 1;
		for (final Integer itemId : itemIds) {
			this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(id);

			final Property entryCodeProperty = this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_CODE.getName());
			if (entryCodeProperty.getValue() == null || entryCodeProperty.getValue().toString().equals(DropHandlerMethods.STRING_EMPTY)) {
				this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(id);
			}
			id++;
		}
	}

	/**
	 * Get item id's of a table, and return it as a list
	 *
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> getItemIds(final Table table) {
		final List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
		return itemIds;
	}

	public Integer getNextListEntryId() {
		int maxId = 0;
		for (final Object id : this.targetTable.getItemIds()) {
			final Integer itemId = (Integer) id;
			if (itemId > maxId) {
				maxId = itemId;
			}
		}
		maxId++;
		return Integer.valueOf(maxId);
	}

	public Integer getNextListEntryId(final Integer lrecId) {
		try {
			final GermplasmListData entry = this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(this.currentListId, lrecId);

			if (entry != null) {
				return entry.getId();
			} else {
				return this.getNextListEntryId();
			}
		} catch (final MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error retrieving germplasm list data", e);
		}

		return this.getNextListEntryId();
	}

	/**
	 * Iterates through the whole table, gets selected item ID's, make sure it's sorted as seen on the UI
	 */
	@SuppressWarnings("unchecked")
	protected List<Integer> getSelectedItemIds(final Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		final List<Integer> selectedItemIds = new ArrayList<Integer>();
		final List<Integer> trueOrderedSelectedItemIds = new ArrayList<Integer>();

		selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
		itemIds = this.getItemIds(table);

		for (final Integer itemId : itemIds) {
			if (selectedItemIds.contains(itemId)) {
				trueOrderedSelectedItemIds.add(itemId);
			}
		}

		return trueOrderedSelectedItemIds;
	}

	public Integer getGidFromButtonCaption(final Table table, final Integer itemId) {
		final Item item = table.getItem(itemId);
		if (item != null) {
			final String buttonCaption = ((Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue()).getCaption().toString();
			return Integer.valueOf(buttonCaption);
		}
		return null;
	}

	public String getStockIDFromStockIDLabel(final Table table, final Integer itemId) {
		final Item item = table.getItem(itemId);
		if (item != null) {
			return ((Label) item.getItemProperty(ColumnLabels.STOCKID.getName()).getValue()).getValue().toString();
		}
		return null;
	}

	protected String getAvailInvFromButtonCaption(final Table table, final Integer itemId) {
		final Item item = table.getItem(itemId);
		if (item != null) {
			return ((Button) item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue()).getCaption().toString();

		}
		return null;
	}

	protected Button getAvailableBalanceButton(final Table table, final Integer itemId, Integer gid) {
		final Item item = table.getItem(itemId);
		String availableButtonCaption = DropHandlerMethods.STRING_DASH;
		if (item != null) {
			availableButtonCaption = ((Button)item.getItemProperty(ColumnLabels.TOTAL.getName()).getValue()).getCaption();
		}

		final Button availableButton = new Button(availableButtonCaption, new InventoryLinkButtonClickListener(this.listManagerMain, gid));
		availableButton.setDebugId("availableButton");
		availableButton.setStyleName(BaseTheme.BUTTON_LINK);
		availableButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);

		return availableButton;
	}

	protected String getDesignationFromButtonCaption(final Table table, final Integer itemId) {
		final Item item = table.getItem(itemId);
		if (item != null) {
			return ((Button) item.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue()).getCaption().toString();

		}
		return null;
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(final boolean changed) {
		this.changed = changed;
	}

	protected void fireListUpdatedEvent() {
		if (this.listeners != null) {
			final ListUpdatedEvent event = new ListUpdatedEvent(this.targetTable.size());
			for (final ListUpdatedListener listener : this.listeners) {
				listener.listUpdated(event);
			}
		}
		this.listManagerMain.showListBuilder();
	}

	public void addListener(final ListUpdatedListener listener) {
		if (this.listeners == null) {
			this.listeners = new ArrayList<ListUpdatedListener>();
		}
		this.listeners.add(listener);
	}

	public void removeListener(final ListUpdatedListener listener) {
		if (this.listeners == null) {
			this.listeners = new ArrayList<ListUpdatedListener>();
		}
		this.listeners.remove(listener);
	}

	/**
	 * Retrieve the germplasmList, and make sure that the inventory columns are properly filled up
	 */
	public GermplasmList getGermplasmList(final Integer listId) {
		try {
			final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);
			this.inventoryDataManager.populateLotCountsIntoExistingList(germplasmList);
			return germplasmList;
		} catch (final MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error(e.getMessage(), e);
			throw e;
		}
	}

	public interface ListUpdatedListener {

		public void listUpdated(final ListUpdatedEvent event);
	}


	public class ListUpdatedEvent {

		private final int listCount;

		public ListUpdatedEvent(final int listCount) {
			this.listCount = listCount;
		}

		public int getListCount() {
			return this.listCount;
		}
	}

	/*
	 * Marks List Builder if there is unsaved changes in the list data table during drop and drag actions
	 */
	public void setHasUnsavedChanges(final boolean changed) {
		this.changed = changed;
		this.listManagerMain.getListBuilderComponent().setHasUnsavedChanges(changed);
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	void setCurrentColumnsInfo(final GermplasmListNewColumnsInfo currentColumnsInfo) {
		this.currentColumnsInfo = currentColumnsInfo;
	}

	void setTargetTable(final Table targetTable) {
		this.targetTable = targetTable;
	}

	void setListManagerMain(final ListManagerMain listManagerMain) {
		this.listManagerMain = listManagerMain;
	}

	void setPedigreeService(final PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}


package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.inventory.InventoryDropTargetContainer;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.AddColumnContextMenu;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
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

	protected static final String MATCHING_GERMPLASMS_TABLE_DATA = GermplasmSearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA;
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

		try {
			// Load currentColumnsInfo if cached list info is null or not matching the needed list id
			if (this.currentColumnsInfo == null || !this.currentColumnsInfo.getListId().equals(listId)) {
				this.currentColumnsInfo = this.germplasmListManager.getAdditionalColumnsForList(listId);
			}

			final GermplasmList germplasmList = this.getGermplasmList(listId);
			final List<GermplasmListData> germplasmListData = germplasmList.getListData();

			// Fix for adding entries in reverse
			if (germplasmListData.size() > 1 && germplasmListData.get(0).getEntryId() > germplasmListData.get(1).getEntryId()) {
				Collections.reverse(germplasmListData);
			}

			for (final GermplasmListData listData : germplasmListData) {
				this.addGermplasmFromList(listId, listData.getId(), germplasmList, fromEditList);
			}

			// mark that there is changes in a list that is currently building
			this.changed = true;

		} catch (final MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error in getting germplasm list.", e);
		}

		this.currentColumnsInfo = null;
		this.currentListId = null;

		this.fireListUpdatedEvent();

	}

	protected void addSelectedGermplasmsFromTable(final Table sourceTable) {
		final List<Integer> selectedGermplasmIds = this.getSelectedItemIds(sourceTable);
		for (final Integer itemId : selectedGermplasmIds) {
			// note, for paged table, the itemId !== GID, but there is a hidden reference GID there so we can retrive actual GID
			// todo: lets cleanup that "_REF" string later
			final Property internalGIDReference = sourceTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName() + "_REF");

			if (internalGIDReference != null && internalGIDReference.getValue() != null) {
				this.addGermplasm((Integer) internalGIDReference.getValue());
			} else {
				this.addGermplasm(itemId);
			}

		}
	}

	public Integer addGermplasm(final Integer gid) {
		try {

			final Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);

			final Integer newItemId = this.getNextListEntryId();
			final Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

			final Button gidButton =
					new Button(String.format("%s", gid), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);

			String crossExpansion = DropHandlerMethods.STRING_EMPTY;
			if (germplasm != null) {
				crossExpansion = this.getCrossExpansion(germplasm);
			}

			final List<Integer> importedGermplasmGids = new ArrayList<Integer>();
			importedGermplasmGids.add(gid);
			final Map<Integer, String> preferredNames = this.germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);
			final String preferredName = preferredNames.get(gid);
			final Button designationButton =
					new Button(preferredName, new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			designationButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			final CheckBox tagCheckBox = new CheckBox();
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
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

			if (availInv.equals(DropHandlerMethods.STRING_DASH)) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription(DropHandlerMethods.NO_LOT_FOR_THIS_GERMPLASM);
			} else {
				inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
			}

			// #2 Seed Reserved
			final String seedRes = DropHandlerMethods.STRING_DASH;
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

			newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(tagCheckBox);
			if (newItem != null && gidButton != null) {
				newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
			}

			newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(this.germplasmDataManager.getPlotCodeValue(gid));
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
			newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(crossExpansion);

			this.assignSerializedEntryNumber();

			final FillWith fillWith = new FillWith(ColumnLabels.GID.getName(), this.targetTable);

			for (final String column : AddColumnContextMenu.getTablePropertyIds(this.targetTable)) {
				fillWith.fillWith(this.targetTable, column, true);
			}

			this.fireListUpdatedEvent();

			this.setHasUnsavedChanges(true);

			return newItemId;

		} catch (final MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error in adding germplasm to germplasm table.", e);
			return null;
		}

	}

	private String getCrossExpansion(final Germplasm germplasm) {
		String crossExpansion = DropHandlerMethods.STRING_EMPTY;
		try {
			if (this.germplasmDataManager != null) {
				crossExpansion = this.pedigreeService.getCrossExpansion(germplasm.getGid(), this.crossExpansionProperties);
			}
		} catch (final MiddlewareQueryException ex) {
			DropHandlerMethods.LOG.error("Error in retrieving cross expansion data for GID: " + germplasm.getGid() + ".", ex);
			crossExpansion = DropHandlerMethods.STRING_DASH;
		}

		return crossExpansion;
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

				final Button gidButton =
						new Button(String.format("%s", gid), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true,
								true));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);

				final CheckBox tagCheckBox = new CheckBox();
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

				final Button designationButton =
						new Button(germplasmListData.getDesignation(), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(),
								true, true));
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
				final String groupIdDisplayValue = (groupId == null || groupId == 0) ? "-" : groupId.toString();
				newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupIdDisplayValue);

				// Inventory Related Columns

				// #1 Available Inventory
				String availInv = DropHandlerMethods.STRING_DASH;
				if (germplasmListData.getInventoryInfo().getLotCount().intValue() != 0) {
					availInv = germplasmListData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
				}
				final Button inventoryButton =
						new Button(availInv, new InventoryLinkButtonClickListener(this.listManagerMain, germplasmListData.getGid()));
				inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
				newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

				if (availInv.equals(DropHandlerMethods.STRING_DASH)) {
					inventoryButton.setEnabled(false);
					inventoryButton.setDescription(DropHandlerMethods.NO_LOT_FOR_THIS_GERMPLASM);
				} else {
					inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
				}

				// #2 Seed Reserved
				String seedRes = DropHandlerMethods.STRING_DASH;
				if (forEditList && germplasmListData.getInventoryInfo().getReservedLotCount().intValue() != 0) {

					seedRes = germplasmListData.getInventoryInfo().getReservedLotCount().toString().trim();

				}
				newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

				if (forEditList) {
					newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(germplasmListData.getInventoryInfo().getStockIDs());
				} else {
					newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(DropHandlerMethods.STRING_EMPTY);
				}

				for (final Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
					final String column = columnEntry.getKey();
					for (final ListDataColumnValues columnValue : columnEntry.getValue()) {
						if (columnValue.getListDataId().equals(germplasmListData.getId())) {
							final String value = columnValue.getValue();
							newItem.getItemProperty(column).setValue(value == null ? DropHandlerMethods.STRING_EMPTY : value);
						}
					}
				}

				this.assignSerializedEntryNumber();

				final FillWith fillWith = new FillWith(ColumnLabels.GID.getName(), this.targetTable);

				for (final String column : AddColumnContextMenu.getTablePropertyIds(this.targetTable)) {
					fillWith.fillWith(this.targetTable, column, true);
				}

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
		GermplasmListData germplasmListData = null;

		if (germplasmList.getListData() != null && !germplasmList.getListData().isEmpty()) {
			for (final GermplasmListData listData : germplasmList.getListData()) {
				if (listData.getId().equals(lrecid)) {
					germplasmListData = listData;
				}
			}
		} else {
			germplasmListData = this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(listId, lrecid);
		}
		return germplasmListData;
	}

	public void addFromListDataTable(final Table sourceTable) {
		final List<Integer> itemIds = this.getSelectedItemIds(sourceTable);

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

		for (final Integer itemId : itemIds) {

			final Item itemFromSourceTable = sourceTable.getItem(itemId);
			final Integer newItemId = this.getNextListEntryId();
			final Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

			final Integer gid = this.getGidFromButtonCaption(sourceTable, itemId);
			final Button gidButton =
					new Button(String.format("%s", gid), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			gidButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			final CheckBox itemCheckBox = new CheckBox();
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

			final String designation = this.getDesignationFromButtonCaption(sourceTable, itemId);
			final Button designationButton =
					new Button(designation, new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			designationButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			// Inventory Related Columns

			// #1 Available Inventory
			final String availInv = this.getAvailInvFromButtonCaption(sourceTable, itemId);
			final Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(this.listManagerMain, gid));
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

			// #2 Seed Reserved
			final String seedRes = DropHandlerMethods.STRING_DASH;

			newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
			newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
			newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupId);
			newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(seedSource);
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
			newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(parentage);
			newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entryCode);
			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

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

		final FillWith fillWith = new FillWith(ColumnLabels.GID.getName(), this.targetTable);

		for (final String column : AddColumnContextMenu.getTablePropertyIds(this.targetTable)) {
			fillWith.fillWith(this.targetTable, column, true);
		}
		this.fireListUpdatedEvent();

		this.setHasUnsavedChanges(true);
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

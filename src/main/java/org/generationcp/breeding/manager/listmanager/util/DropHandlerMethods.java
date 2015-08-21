
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
	protected Boolean hasSelectedItems(Table table) {
		List<Integer> selectedItemIds = new ArrayList<Integer>();
		selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
		if (!selectedItemIds.isEmpty()) {
			return true;
		}
		return false;
	}

	protected void addSelectedGermplasmListsFromTable(Table sourceTable) {
		List<Integer> selectedGermplasmListIds = this.getSelectedItemIds(sourceTable);
		for (Integer listId : selectedGermplasmListIds) {
			this.addGermplasmList(listId);
		}
	}

	public void addGermplasmList(Integer listId) {
		this.addGermplasmList(listId, false);
	}

	public void addGermplasmList(Integer listId, Boolean fromEditList) {

		this.currentListId = listId;

		try {
			// Load currentColumnsInfo if cached list info is null or not matching the needed list id
			if (this.currentColumnsInfo == null || !this.currentColumnsInfo.getListId().equals(listId)) {
				this.currentColumnsInfo = this.germplasmListManager.getAdditionalColumnsForList(listId);
			}

			GermplasmList germplasmList = this.getGermplasmList(listId);
			List<GermplasmListData> germplasmListData = germplasmList.getListData();

			// Fix for adding entries in reverse
			if (germplasmListData.size() > 1 && germplasmListData.get(0).getEntryId() > germplasmListData.get(1).getEntryId()) {
				Collections.reverse(germplasmListData);
			}

			for (GermplasmListData listData : germplasmListData) {
				this.addGermplasmFromList(listId, listData.getId(), germplasmList, fromEditList);
			}

			// mark that there is changes in a list that is currently building
			this.changed = true;

		} catch (MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error in getting germplasm list.", e);
		}

		this.currentColumnsInfo = null;
		this.currentListId = null;

		this.fireListUpdatedEvent();

	}

	protected void addSelectedGermplasmsFromTable(Table sourceTable) {
		List<Integer> selectedGermplasmIds = this.getSelectedItemIds(sourceTable);
		for (Integer itemId : selectedGermplasmIds) {
			this.addGermplasm(itemId);
		}
	}

	public Integer addGermplasm(Integer gid) {
		try {

			Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);

			final Integer newItemId = this.getNextListEntryId();
			Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

			Button gidButton =
					new Button(String.format("%s", gid), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);

			String crossExpansion = DropHandlerMethods.STRING_EMPTY;
			if (germplasm != null) {
				crossExpansion = this.getCrossExpansion(germplasm);
			}

			List<Integer> importedGermplasmGids = new ArrayList<Integer>();
			importedGermplasmGids.add(gid);
			Map<Integer, String> preferredNames = this.germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);
			String preferredName = preferredNames.get(gid);
			Button designationButton =
					new Button(preferredName, new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			designationButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			CheckBox tagCheckBox = new CheckBox();
			tagCheckBox.setImmediate(true);
			tagCheckBox.addListener(new ClickListener() {

				protected static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
					CheckBox itemCheckBox = (CheckBox) event.getButton();
					if (((Boolean) itemCheckBox.getValue()).equals(true)) {
						DropHandlerMethods.this.targetTable.select(newItemId);
					} else {
						DropHandlerMethods.this.targetTable.unselect(newItemId);
					}
				}

			});

			// Inventory Related Columns

			// #1 Available Inventory
			String availInv = DropHandlerMethods.STRING_EMPTY;
			Integer availInvGid = this.getAvailInvForGID(gid);
			if (availInvGid != null) {
				availInv = availInvGid.toString();
			}

			Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(this.listManagerMain, gid));
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
			String seedRes = DropHandlerMethods.STRING_DASH;
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

			newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(tagCheckBox);
			if (newItem != null && gidButton != null) {
				newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
			}
			newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue("Germplasm Search");
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
			newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(crossExpansion);

			this.assignSerializedEntryNumber();

			FillWith fillWith = new FillWith(ColumnLabels.GID.getName(), this.targetTable);

			for (String column : AddColumnContextMenu.getTablePropertyIds(this.targetTable)) {
				fillWith.fillWith(this.targetTable, column, true);
			}

			this.fireListUpdatedEvent();

			this.setHasUnsavedChanges(true);

			return newItemId;

		} catch (MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error in adding germplasm to germplasm table.", e);
			return null;
		}

	}

	private String getCrossExpansion(Germplasm germplasm) {
		String crossExpansion = DropHandlerMethods.STRING_EMPTY;
		try {
			if (this.germplasmDataManager != null) {
				crossExpansion = this.pedigreeService.getCrossExpansion(germplasm.getGid(), this.crossExpansionProperties);
			}
		} catch (MiddlewareQueryException ex) {
			DropHandlerMethods.LOG.error("Error in retrieving cross expansion data for GID: " + germplasm.getGid() + ".", ex);
			crossExpansion = DropHandlerMethods.STRING_DASH;
		}

		return crossExpansion;
	}

	protected Integer getAvailInvForGID(Integer gid) {
		Integer availInv;
		try {
			availInv = this.inventoryDataManager.countLotsWithAvailableBalanceForGermplasm(gid);
			return availInv;
		} catch (MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public Integer addGermplasmFromList(Integer listId, Integer lrecid) {
		return this.addGermplasmFromList(listId, lrecid, null);
	}

	protected Integer addGermplasmFromList(Integer listId, Integer lrecid, GermplasmList germplasmList) {
		return this.addGermplasmFromList(listId, lrecid, germplasmList, false);
	}

	protected Integer addGermplasmFromList(Integer listId, Integer lrecid, GermplasmList germplasmList, Boolean forEditList) {

		this.currentListId = listId;

		try {
			// Load currentColumnsInfo if cached list info is null or not matching the needed list id
			if (this.currentColumnsInfo == null || !this.currentColumnsInfo.getListId().equals(listId)) {
				this.currentColumnsInfo = this.germplasmListManager.getAdditionalColumnsForList(listId);
			}

			for (Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
				String column = columnEntry.getKey();
				if (!AddColumnContextMenu.propertyExists(column, this.targetTable)) {
					this.targetTable.addContainerProperty(column, String.class, DropHandlerMethods.STRING_EMPTY);
					this.targetTable.setColumnWidth(column, 250);
				}
			}

			// making sure that germplasmList has value
			if (germplasmList == null) {
				germplasmList = this.getGermplasmList(listId);
			}
			GermplasmListData germplasmListData = this.getListDataByListIdAndLrecId(listId, lrecid, germplasmList);

			// handles the data for inventory

			if (germplasmListData != null && germplasmListData.getStatus() != 9) {

				Integer gid = germplasmListData.getGid();

				Integer niid = null;
				if (forEditList.equals(true)) {
					niid = this.getNextListEntryId(germplasmListData.getId());
				} else {
					niid = this.getNextListEntryId();
				}
				final Integer newItemId = niid;

				Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

				Button gidButton =
						new Button(String.format("%s", gid), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true,
								true));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);

				CheckBox tagCheckBox = new CheckBox();
				tagCheckBox.setImmediate(true);
				tagCheckBox.addListener(new ClickListener() {

					protected static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
						CheckBox itemCheckBox = (CheckBox) event.getButton();
						if (((Boolean) itemCheckBox.getValue()).equals(true)) {
							DropHandlerMethods.this.targetTable.select(newItemId);
						} else {
							DropHandlerMethods.this.targetTable.unselect(newItemId);
						}
					}

				});

				Button designationButton =
						new Button(germplasmListData.getDesignation(), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(),
								true, true));
				designationButton.setStyleName(BaseTheme.BUTTON_LINK);
				designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

				newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(tagCheckBox);
				if (newItem != null && gidButton != null) {
					newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
				}
				newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(germplasmListData.getEntryCode());
				if (forEditList.equals(true)) {
					newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(germplasmListData.getSeedSource());
				} else {
					newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(
							germplasmList.getName() + ": " + germplasmListData.getEntryId());
				}
				newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
				newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(germplasmListData.getGroupName());

				// Inventory Related Columns

				// #1 Available Inventory
				String availInv = DropHandlerMethods.STRING_DASH;
				if (germplasmListData.getInventoryInfo().getLotCount().intValue() != 0) {
					availInv = germplasmListData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
				}
				Button inventoryButton =
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

				for (Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
					String column = columnEntry.getKey();
					for (ListDataColumnValues columnValue : columnEntry.getValue()) {
						if (columnValue.getListDataId().equals(germplasmListData.getId())) {
							String value = columnValue.getValue();
							newItem.getItemProperty(column).setValue(value == null ? DropHandlerMethods.STRING_EMPTY : value);
						}
					}
				}

				this.assignSerializedEntryNumber();

				FillWith fillWith = new FillWith(ColumnLabels.GID.getName(), this.targetTable);

				for (String column : AddColumnContextMenu.getTablePropertyIds(this.targetTable)) {
					fillWith.fillWith(this.targetTable, column, true);
				}

				this.currentListId = null;

				this.fireListUpdatedEvent();
				return newItemId;
			}

			this.fireListUpdatedEvent();
			return null;

		} catch (MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error in adding germplasm to germplasm table.", e);
			this.currentColumnsInfo = null;
			this.currentListId = null;

			return null;
		}

	}

	public GermplasmListData getListDataByListIdAndLrecId(Integer listId, Integer lrecid, GermplasmList germplasmList) {
		GermplasmListData germplasmListData = null;

		if (germplasmList.getListData() != null && !germplasmList.getListData().isEmpty()) {
			for (GermplasmListData listData : germplasmList.getListData()) {
				if (listData.getId().equals(lrecid)) {
					germplasmListData = listData;
				}
			}
		} else {
			germplasmListData = this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(listId, lrecid);
		}
		return germplasmListData;
	}

	public void addFromListDataTable(Table sourceTable) {
		List<Integer> itemIds = this.getSelectedItemIds(sourceTable);

		Integer listId = null;
		if (sourceTable.getParent() instanceof TableWithSelectAllLayout && sourceTable.getParent().getParent() instanceof ListComponent) {
			listId = ((ListComponent) sourceTable.getParent().getParent()).getGermplasmListId();
		}

		GermplasmList germplasmList = this.getGermplasmList(listId);

		// Load currentColumnsInfo if cached list info is null or not matching the needed list id
		if (this.currentColumnsInfo == null || !this.currentColumnsInfo.getListId().equals(listId)) {
			try {
				this.currentColumnsInfo = this.germplasmListManager.getAdditionalColumnsForList(listId);
			} catch (MiddlewareQueryException e) {
				DropHandlerMethods.LOG.error("Error During getAdditionalColumnsForList(" + listId + "): " + e);
			}
		}

		for (Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
			String column = columnEntry.getKey();
			if (!AddColumnContextMenu.propertyExists(column, this.targetTable)) {
				this.targetTable.addContainerProperty(column, String.class, DropHandlerMethods.STRING_EMPTY);
				this.targetTable.setColumnWidth(column, 250);
			}
		}

		for (Integer itemId : itemIds) {

			Item itemFromSourceTable = sourceTable.getItem(itemId);
			Integer newItemId = this.getNextListEntryId();
			Item newItem = this.targetTable.getContainerDataSource().addItem(newItemId);

			Integer gid = this.getGidFromButtonCaption(sourceTable, itemId);
			Button gidButton =
					new Button(String.format("%s", gid), new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			gidButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			CheckBox itemCheckBox = new CheckBox();
			itemCheckBox.setData(newItemId);
			itemCheckBox.setImmediate(true);
			itemCheckBox.addListener(new ClickListener() {

				protected static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
					CheckBox itemCheckBox = (CheckBox) event.getButton();
					if (((Boolean) itemCheckBox.getValue()).equals(true)) {
						DropHandlerMethods.this.targetTable.select(itemCheckBox.getData());
					} else {
						DropHandlerMethods.this.targetTable.unselect(itemCheckBox.getData());
					}
				}

			});

			String designation = this.getDesignationFromButtonCaption(sourceTable, itemId);
			Button designationButton =
					new Button(designation, new GidLinkButtonClickListener(this.listManagerMain, gid.toString(), true, true));
			designationButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			String parentage = (String) itemFromSourceTable.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
			Integer entryId = (Integer) itemFromSourceTable.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			String entryCode = (String) itemFromSourceTable.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue();

			// Inventory Related Columns

			// #1 Available Inventory
			String availInv = this.getAvailInvFromButtonCaption(sourceTable, itemId);
			Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(this.listManagerMain, gid));
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);

			if (availInv.equals(DropHandlerMethods.STRING_DASH)) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription(DropHandlerMethods.NO_LOT_FOR_THIS_GERMPLASM);
			} else {
				inventoryButton.setDescription(DropHandlerMethods.CLICK_TO_VIEW_INVENTORY_DETAILS);
			}

			// #2 Seed Reserved
			String seedRes = DropHandlerMethods.STRING_DASH;

			newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
			newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
			newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(germplasmList.getName() + ": " + entryId);
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
			newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(parentage);
			newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entryCode);
			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

			newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(DropHandlerMethods.STRING_EMPTY);

			for (Entry<String, List<ListDataColumnValues>> columnEntry : this.currentColumnsInfo.getColumnValuesMap().entrySet()) {
				String column = columnEntry.getKey();
				for (ListDataColumnValues columnValue : columnEntry.getValue()) {
					if (columnValue.getListDataId().equals(itemId)) {
						String value = columnValue.getValue();
						newItem.getItemProperty(column).setValue(value == null ? DropHandlerMethods.STRING_EMPTY : value);
					}
				}
			}

		}

		this.assignSerializedEntryNumber();

		FillWith fillWith = new FillWith(ColumnLabels.GID.getName(), this.targetTable);

		for (String column : AddColumnContextMenu.getTablePropertyIds(this.targetTable)) {
			fillWith.fillWith(this.targetTable, column, true);
		}
		this.fireListUpdatedEvent();

		this.setHasUnsavedChanges(true);
	}

	/**
	 * Iterates through the whole table, and sets the entry code from 1 to n based on the row position
	 */
	protected void assignSerializedEntryCode() {
		/**
		 *
		 */
	}

	/**
	 * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
	 */
	protected void assignSerializedEntryNumber() {
		List<Integer> itemIds = this.getItemIds(this.targetTable);

		int id = 1;
		for (Integer itemId : itemIds) {
			this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(id);

			Property entryCodeProperty = this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_CODE.getName());
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
	public List<Integer> getItemIds(Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
		return itemIds;
	}

	public Integer getNextListEntryId() {
		int maxId = 0;
		for (Object id : this.targetTable.getItemIds()) {
			Integer itemId = (Integer) id;
			if (itemId > maxId) {
				maxId = itemId;
			}
		}
		maxId++;
		return Integer.valueOf(maxId);
	}

	public Integer getNextListEntryId(Integer lrecId) {
		try {
			GermplasmListData entry = this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(this.currentListId, lrecId);

			if (entry != null) {
				return entry.getId();
			} else {
				return this.getNextListEntryId();
			}
		} catch (MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error("Error retrieving germplasm list data", e);
		}

		return this.getNextListEntryId();
	}

	/**
	 * Iterates through the whole table, gets selected item ID's, make sure it's sorted as seen on the UI
	 */
	@SuppressWarnings("unchecked")
	protected List<Integer> getSelectedItemIds(Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		List<Integer> selectedItemIds = new ArrayList<Integer>();
		List<Integer> trueOrderedSelectedItemIds = new ArrayList<Integer>();

		selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
		itemIds = this.getItemIds(table);

		for (Integer itemId : itemIds) {
			if (selectedItemIds.contains(itemId)) {
				trueOrderedSelectedItemIds.add(itemId);
			}
		}

		return trueOrderedSelectedItemIds;
	}

	public Integer getGidFromButtonCaption(Table table, Integer itemId) {
		Item item = table.getItem(itemId);
		if (item != null) {
			String buttonCaption = ((Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue()).getCaption().toString();
			return Integer.valueOf(buttonCaption);
		}
		return null;
	}

	public String getStockIDFromStockIDLabel(Table table, Integer itemId) {
		Item item = table.getItem(itemId);
		if (item != null) {
			return ((Label) item.getItemProperty(ColumnLabels.STOCKID.getName()).getValue()).getValue().toString();
		}
		return null;
	}

	protected String getAvailInvFromButtonCaption(Table table, Integer itemId) {
		Item item = table.getItem(itemId);
		if (item != null) {
			return ((Button) item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue()).getCaption().toString();

		}
		return null;
	}

	protected String getDesignationFromButtonCaption(Table table, Integer itemId) {
		Item item = table.getItem(itemId);
		if (item != null) {
			return ((Button) item.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue()).getCaption().toString();

		}
		return null;
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	protected void fireListUpdatedEvent() {
		if (this.listeners != null) {
			final ListUpdatedEvent event = new ListUpdatedEvent(this.targetTable.size());
			for (ListUpdatedListener listener : this.listeners) {
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
	 * Reset the germplasmList
	 */
	public GermplasmList getGermplasmList(Integer listId) {
		GermplasmList germplasmList = null;

		try {
			germplasmList = this.germplasmListManager.getGermplasmListById(listId);
		} catch (MiddlewareQueryException e) {
			DropHandlerMethods.LOG.error(e.getMessage(), e);
		}

		return germplasmList;
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
	public void setHasUnsavedChanges(boolean changed) {
		this.changed = changed;
		this.listManagerMain.getListBuilderComponent().setHasUnsavedChanges(changed);
	}

}


package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.SelectParentsListDataComponent;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListManagerInventoryTable;
import org.generationcp.breeding.manager.inventory.InventoryDropTargetContainer;
import org.generationcp.breeding.manager.inventory.ListDataAndLotDetails;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.Listener.LotDetailsButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.themes.BaseTheme;

public class InventoryTableDropHandler extends DropHandlerMethods implements DropHandler {

	private static final Logger LOG = LoggerFactory.getLogger(InventoryTableDropHandler.class);
	private static final long serialVersionUID = 1L;
	private Integer lastDroppedListId;

	private List<ListDataAndLotDetails> listDataAndLotDetails;

	private boolean hasChanges = false;

	public InventoryTableDropHandler(ListManagerMain listManagerMain, GermplasmDataManager germplasmDataManager,
			GermplasmListManager germplasmListManager, InventoryDataManager inventoryDataManager, PedigreeService pedigreeService,
			CrossExpansionProperties crossExpansionProperties, Table targetTable) {
		this.listManagerMain = listManagerMain;
		this.germplasmDataManager = germplasmDataManager;
		this.germplasmListManager = germplasmListManager;
		this.inventoryDataManager = inventoryDataManager;
		this.setTargetTable(targetTable);
		this.pedigreeService = pedigreeService;
		this.crossExpansionProperties = crossExpansionProperties;
		this.listDataAndLotDetails = new ArrayList<ListDataAndLotDetails>();
	}

	public InventoryTableDropHandler(InventoryDropTargetContainer inventoryDropTargetContainer, GermplasmDataManager germplasmDataManager,
			GermplasmListManager germplasmListManager, InventoryDataManager inventoryDataManager, PedigreeService pedigreeService,
			CrossExpansionProperties crossExpansionProperties, Table targetTable) {
		this.inventoryDropTargetContainer = inventoryDropTargetContainer;
		this.germplasmDataManager = germplasmDataManager;
		this.germplasmListManager = germplasmListManager;
		this.inventoryDataManager = inventoryDataManager;
		this.setTargetTable(targetTable);
		this.pedigreeService = pedigreeService;
		this.crossExpansionProperties = crossExpansionProperties;
		this.listDataAndLotDetails = new ArrayList<ListDataAndLotDetails>();
	}

	@Override
	public void drop(DragAndDropEvent event) {

		if (event.getTransferable() instanceof TableTransferable) {

			TableTransferable transferable = (TableTransferable) event.getTransferable();
			Table sourceTable = transferable.getSourceComponent();
			String sourceTableData = sourceTable.getData().toString();

			if (sourceTableData.equals(DropHandlerMethods.MATCHING_GERMPLASMS_TABLE_DATA)) {
				String message = "Please switch to list view first before adding a germplasm entry to the list.";

				MessageNotifier.showWarning(this.listManagerMain.getWindow(), "Warning!", message);

			} else if (sourceTableData.equals(ListManagerInventoryTable.INVENTORY_TABLE_DATA) && !sourceTable.equals(this.getTargetTable())) {
				super.setHasUnsavedChanges(true);

				this.lastDroppedListId = ((ListComponent) transferable.getSourceComponent().getParent().getParent()).getGermplasmListId();

				List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();

				// If table has selected items, add selected items
				if (this.hasSelectedItems(sourceTable)) {
					lotDetails.addAll(this.getInventoryTableSelectedItemIds(sourceTable));
					// If none, add what was dropped
				} else if (transferable.getSourceComponent().getParent().getParent() instanceof ListComponent) {
					lotDetails.add((ListEntryLotDetails) transferable.getItemId());
				}
				this.addSelectedInventoryDetails(lotDetails, sourceTable);

			} else if (sourceTableData.equals(SelectParentsListDataComponent.CROSSING_MANAGER_PARENT_TAB_INVENTORY_TABLE)
					&& !sourceTable.equals(this.getTargetTable())) {
				this.inventoryDropTargetContainer.setHasUnsavedChanges(true);
				this.hasChanges = true;

				this.lastDroppedListId =
						((SelectParentsListDataComponent) transferable.getSourceComponent().getParent().getParent()).getGermplasmListId();

				List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();

				// If table has selected items, add selected items
				if (this.hasSelectedItems(sourceTable)) {
					lotDetails.addAll(this.getInventoryTableSelectedItemIds(sourceTable));
					// If none, add what was dropped
				} else if (transferable.getSourceComponent().getParent().getParent() instanceof SelectParentsListDataComponent) {
					lotDetails.add((ListEntryLotDetails) transferable.getItemId());
				}
				this.addSelectedInventoryDetails(lotDetails, sourceTable);

			} else {
				InventoryTableDropHandler.LOG.error("Error During Drop: Unknown table data: " + sourceTableData);
			}

			// If source is from tree
		} else {
			// Not handled
		}
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

	/**
	 * Use this to handle drop events from list inventory view of list tab to list inventory view of list builder
	 * 
	 * @param selectedItemIds
	 * @param sourceTable
	 */
	private void addSelectedInventoryDetails(List<ListEntryLotDetails> selectedItemIds, Table sourceTable) {

		List<Integer> uniqueEntryIds = this.getUniqueEntryNumbers(selectedItemIds, sourceTable);
		List<ListEntryLotDetails> allLotDetailsToBeAdded = new ArrayList<ListEntryLotDetails>();

		for (Integer uniqueEntryId : uniqueEntryIds) {
			allLotDetailsToBeAdded.addAll(this.getLotDetailsWithEntryId(uniqueEntryId, sourceTable));
		}

		int nextId = this.getInventoryTableNextEntryId();
		int lastLrecId = allLotDetailsToBeAdded.get(0).getId();

		for (ListEntryLotDetails lotDetail : allLotDetailsToBeAdded) {
			if (lastLrecId != lotDetail.getId()) {
				nextId++;
			}

			this.addItemToDestinationTable(lotDetail, nextId, sourceTable, this.getTargetTable());
			lastLrecId = lotDetail.getId();
		}

		// Update counter
		if (this.listManagerMain != null) {
			this.listManagerMain.getListBuilderComponent().refreshListInventoryItemCount();
		} else if (this.inventoryDropTargetContainer != null) {
			this.inventoryDropTargetContainer.refreshListInventoryItemCount();
		}

	}

	/**
	 * Get distinct entry #'s, given a list of selected item ids and the source table
	 * 
	 * @param selectedItemIds
	 * @param sourceTable
	 * @return
	 */
	private List<Integer> getUniqueEntryNumbers(List<ListEntryLotDetails> selectedItemIds, Table sourceTable) {
		List<Integer> uniqueEntryIds = new ArrayList<Integer>();
		for (ListEntryLotDetails lotDetail : selectedItemIds) {

			Item item = sourceTable.getItem(lotDetail);
			if (item != null) {
				int currentEntryId = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
				if (!uniqueEntryIds.contains(currentEntryId)) {
					uniqueEntryIds.add(currentEntryId);
				}
			}

		}

		return uniqueEntryIds;
	}

	/**
	 * Get lotDetails with given entry ID, given the entry ID and the source table
	 * 
	 * @param entryId
	 * @param sourceTable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ListEntryLotDetails> getLotDetailsWithEntryId(Integer entryId, Table sourceTable) {
		List<ListEntryLotDetails> allLotDetails = new ArrayList<ListEntryLotDetails>();
		List<ListEntryLotDetails> matchingLotDetails = new ArrayList<ListEntryLotDetails>();
		allLotDetails.addAll((Collection<? extends ListEntryLotDetails>) sourceTable.getItemIds());

		for (ListEntryLotDetails lotDetail : allLotDetails) {

			Item item = sourceTable.getItem(lotDetail);
			if (item != null) {
				int currentEntryId = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
				if (currentEntryId == entryId) {
					matchingLotDetails.add(lotDetail);
				}
			}

		}

		return matchingLotDetails;
	}

	/**
	 * To be called after (callback) saving new entries in list builder (on list inventory view) to update lrecid of listentrylotdetails
	 * 
	 * @param lrecId
	 * @param entryId
	 */
	public void assignLrecIdToRowsFromListWithEntryId(Integer listId, Integer entryId) {
		List<ListEntryLotDetails> itemIds = this.getLotDetailsWithEntryId(entryId, this.getTargetTable());
		for (ListEntryLotDetails itemId : itemIds) {
			try {
				GermplasmListData listData = this.germplasmListManager.getGermplasmListDataByListIdAndEntryId(listId, entryId);
				if (listData != null) {
					itemId.setId(listData.getId());
				}
			} catch (MiddlewareQueryException e) {
				InventoryTableDropHandler.LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Use this to handle drop events from list inventory view of list tab to list inventory view of list builder
	 * 
	 * @param selectedItemIds
	 * @param sourceTable
	 */
	public void addGermplasmListInventoryData(Integer listId) {

		List<GermplasmListData> inventoryDetails;

		if (this.inventoryDropTargetContainer != null) {
			this.inventoryDropTargetContainer.setHasUnsavedChanges(true);
		}

		try {
			inventoryDetails = this.inventoryDataManager.getLotDetailsForList(listId, 0, Integer.MAX_VALUE);

			Integer lastEntryId = this.getInventoryTableLastEntryId();

			if (inventoryDetails != null) {
				for (GermplasmListData inventoryDetail : inventoryDetails) {

					this.listDataAndLotDetails
							.add(new ListDataAndLotDetails(listId, inventoryDetail.getId(), inventoryDetail.getEntryId()));

					Integer entryId = lastEntryId + inventoryDetail.getEntryId();
					String designation = inventoryDetail.getDesignation();

					ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();
					@SuppressWarnings("unchecked")
					List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>) listDataInventory.getLotRows();

					if (lotDetails != null) {
						for (ListEntryLotDetails lotDetail : lotDetails) {
							Item newItem = this.getTargetTable().addItem(lotDetail);

							CheckBox itemCheckBox = new CheckBox();
							itemCheckBox.setDebugId("itemCheckBox");
							itemCheckBox.setData(lotDetail);
							itemCheckBox.setImmediate(true);
							itemCheckBox.addListener(new ClickListener() {

								private static final long serialVersionUID = 1L;

								@Override
								public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
									CheckBox itemCheckBox = (CheckBox) event.getButton();
									if (((Boolean) itemCheckBox.getValue()).equals(true)) {
										InventoryTableDropHandler.this.getTargetTable().select(itemCheckBox.getData());
									} else {
										InventoryTableDropHandler.this.getTargetTable().unselect(itemCheckBox.getData());
									}
								}

							});

							Button desigButton =
									new Button(String.format("%s", designation), new GidLinkButtonClickListener(inventoryDetail.getGid()
											.toString(), true));
							desigButton.setStyleName(BaseTheme.BUTTON_LINK);

							newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
							newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);
							newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
							newItem.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).setValue(lotDetail.getLocationOfLot().getLname());
							newItem.getItemProperty(ColumnLabels.UNITS.getName()).setValue(lotDetail.getScaleOfLot().getName());
							newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName())
									.setValue(lotDetail.getAvailableLotBalance());
							newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(lotDetail.getActualLotBalance());
							newItem.getItemProperty(ColumnLabels.RESERVED.getName()).setValue(lotDetail.getReservedTotalForEntry());
							newItem.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(0);
							newItem.getItemProperty(ColumnLabels.COMMENT.getName()).setValue(lotDetail.getCommentOfLot());
							newItem.getItemProperty(ColumnLabels.LOT_ID.getName()).setValue(lotDetail.getLotId());
							newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(inventoryDetail.getSeedSource());
						}
					}
				}
			}

			// Update counter
			if (this.listManagerMain != null) {
				this.listManagerMain.getListBuilderComponent().refreshListInventoryItemCount();
			} else if (this.inventoryDropTargetContainer != null) {
				this.inventoryDropTargetContainer.refreshListInventoryItemCount();
			}

		} catch (MiddlewareQueryException e) {
			InventoryTableDropHandler.LOG.error(e.getMessage(), e);
		}

	}

	private Item addItemToDestinationTable(ListEntryLotDetails lotDetail, Integer entryId, Table sourceTable, final Table targetTable) {

		this.listDataAndLotDetails.add(new ListDataAndLotDetails(this.lastDroppedListId, lotDetail.getId(), entryId));

		ListEntryLotDetails newLotDetail = lotDetail.makeClone();
		Item newItem = targetTable.addItem(newLotDetail);
		newLotDetail.setId(0);

		CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setDebugId("itemCheckBox");
		itemCheckBox.setData(newLotDetail);
		itemCheckBox.setImmediate(true);
		itemCheckBox.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (((Boolean) itemCheckBox.getValue()).equals(true)) {
					targetTable.select(itemCheckBox.getData());
				} else {
					targetTable.unselect(itemCheckBox.getData());
				}
			}

		});

		Button sourceDesignationButton = new Button();
		sourceDesignationButton.setDebugId("sourceDesignationButton");
		Button targetDesignationButton = new Button();
		targetDesignationButton.setDebugId("targetDesignationButton");

		Item itemFromSourceTable = sourceTable.getItem(lotDetail);
		String seedSource = "";
		if (itemFromSourceTable != null) {
			sourceDesignationButton = (Button) itemFromSourceTable.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
			if (sourceDesignationButton != null) {
				targetDesignationButton.setValue(sourceDesignationButton.getValue());
				targetDesignationButton.setCaption(sourceDesignationButton.getCaption());
				for (Object listener : sourceDesignationButton.getListeners(ClickEvent.class)) {
					targetDesignationButton.addListener((GidLinkButtonClickListener) listener);
				}
			}
			
			Property seedSourceProperty = itemFromSourceTable.getItemProperty(ColumnLabels.SEED_SOURCE.getName());
			if (seedSourceProperty != null) {
				seedSource = (String) seedSourceProperty.getValue();
			}
		}

		targetDesignationButton.setStyleName(BaseTheme.BUTTON_LINK);

		newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
		newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);
		newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(targetDesignationButton);

		StringBuilder lotLocation = new StringBuilder("");
		if (lotDetail.getLocationOfLot() != null) {
			lotLocation.append(lotDetail.getLocationOfLot().getLname());
		}

		newItem.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).setValue(lotLocation.toString());
		newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(lotDetail.getActualLotBalance());
		newItem.getItemProperty(ColumnLabels.RESERVATION.getName()).setValue(lotDetail.getReservedTotalForEntry());
		newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(lotDetail.getCommittedTotalForEntry());
		newItem.getItemProperty(ColumnLabels.COMMENT.getName()).setValue(lotDetail.getCommentOfLot());
		newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(lotDetail.getStockIds());

		final Button lotButton = new Button(lotDetail.getLotId().toString(),
				new LotDetailsButtonClickListener(lotDetail.getEntityIdOfLot(), targetDesignationButton.toString(), this.getTargetTable(), lotDetail.getLotId()));
		lotButton.setStyleName(BaseTheme.BUTTON_LINK);
		newItem.getItemProperty(ColumnLabels.LOT_ID.getName()).setValue(lotButton);

		newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(seedSource);
		

		return newItem;
	}

	@SuppressWarnings("unchecked")
	private List<ListEntryLotDetails> getInventoryTableSelectedItemIds(Table table) {
		List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		lotDetails.addAll((Collection<? extends ListEntryLotDetails>) table.getValue());
		return lotDetails;
	}

	@SuppressWarnings("unchecked")
	private Integer getInventoryTableLastEntryId() {
		int topId = 0;
		for (ListEntryLotDetails lotDetails : (Collection<? extends ListEntryLotDetails>) this.getTargetTable().getItemIds()) {

			Integer entryId = 0;
			Item item = this.getTargetTable().getItem(lotDetails);
			if (item != null) {
				entryId = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			}

			if (entryId > topId) {
				topId = entryId;
			}

		}
		return topId;
	}

	private Integer getInventoryTableNextEntryId() {
		return this.getInventoryTableLastEntryId() + 1;
	}

	public List<ListDataAndLotDetails> getListDataAndLotDetails() {
		return this.listDataAndLotDetails;
	}

	public void setListDataAndLotDetails(List<ListDataAndLotDetails> listDataAndLotDetails) {
		this.listDataAndLotDetails = listDataAndLotDetails;
	}

	public void resetListDataAndLotDetails() {
		this.listDataAndLotDetails.clear();
	}

	public boolean hasChanges() {
		return this.hasChanges;
	}

	public void setHasChanges(boolean hasChanges) {
		this.hasChanges = hasChanges;
	}

}

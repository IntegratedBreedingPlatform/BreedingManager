package org.generationcp.breeding.manager.listmanager.listeners;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AddColumnContextMenuOption;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.AddColumnContextMenu;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Configurable
public class SaveListButtonClickListener implements Button.ClickListener, InitializingBean {

	private static final long serialVersionUID = -2641642996209640461L;

	private static final Logger LOG = LoggerFactory.getLogger(SaveListButtonClickListener.class);

	private static final String STRING_DASH = "-";

	private ListBuilderComponent source;

	@Resource
	private GermplasmListManager dataManager;

	@Resource
	private InventoryDataManager inventoryDataManager;

	@Resource
	private ContextUtil contextUtil;

	private Table listDataTable;

	private Boolean forceHasChanges = false;

	private SimpleResourceBundleMessageSource messageSource;

	public SaveListButtonClickListener(ListBuilderComponent source, Table listDataTable
			, SimpleResourceBundleMessageSource messageSource) {
		this.source = source;
		this.listDataTable = listDataTable;
		this.messageSource = messageSource;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		doSaveAction();
	}

	public void doSaveAction() {
		doSaveAction(true);
	}

	public void doSaveAction(Boolean showMessages) {
		doSaveAction(showMessages, true);
	}

	public void doSaveAction(Boolean showMessages, Boolean callSaveReservation) {
		GermplasmList currentlySavedList = this.source.getCurrentlySavedGermplasmList();
		GermplasmList listToSave = this.source.getCurrentlySetGermplasmListInfo();

		if (listToSave == null) {
			return;
		}
		List<GermplasmListData> listEntries = this.source.getListEntriesFromTable();

		if (!validateListDetails(listToSave, currentlySavedList)) {
			return;
		}

		if (currentlySavedList == null || listToSave.getId() == null) {
			listToSave.setStatus(1);

			try {
				listToSave.setUserId(contextUtil.getCurrentUserLocalId());

				Integer listId = this.dataManager.addGermplasmList(listToSave);

				if (listId != null) {
					GermplasmList listSaved = this.dataManager.getGermplasmListById(listId);
					currentlySavedList = listSaved;
					this.source.setCurrentlySavedGermplasmList(listSaved);

					source.setHasUnsavedChanges(false);

					this.source.getSource().getListSelectionComponent().showNodeOnTree(listId);

				} else {
					showErrorOnSavingGermplasmList(showMessages);
					return;
				}
			} catch (MiddlewareQueryException ex) {
				LOG.error("Error in saving germplasm list: " + listToSave, ex);
				showErrorOnSavingGermplasmList(showMessages);
				return;
			}

			if (!listEntries.isEmpty()) {
				setNeededValuesForNewListEntries(currentlySavedList, listEntries);

				if (!saveNewListEntries(listEntries)) {
					return;
				}

				updateListDataTableContent(currentlySavedList);

				saveListDataColumns(listToSave);
			}

		} else if (currentlySavedList != null) {

			if (areThereChangesToList(currentlySavedList, listToSave) || forceHasChanges) {
				if (!currentlySavedList.getName().equals(listToSave.getName()) && !validateListName(
						listToSave)) {
					return;
				}

				listToSave = ListCommonActionsUtil.overwriteList(
						listToSave,
						dataManager, source, messageSource, showMessages);
			}

			if (listToSave != null) {
				boolean thereAreChangesInListEntries =
						ListCommonActionsUtil.overwriteListEntries(
								listToSave,
								listEntries, forceHasChanges,
								dataManager, source, messageSource, showMessages);

				if (thereAreChangesInListEntries) {
					updateListDataTableContent(currentlySavedList);
				}

				if (!listEntries.isEmpty()) {
					saveListDataColumns(listToSave);
				}
			}
		}

		try {
			contextUtil.logProgramActivity("List Manager Save List",
					"Successfully saved list and list entries for: " + currentlySavedList.getId()
							+ " - " + currentlySavedList.getName());

			source.getBuildNewListDropHandler().setChanged(false);

		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with saving Workbench activity.", ex);
		}

		if (showMessages) {
			MessageNotifier
					.showMessage(this.source.getWindow(), messageSource.getMessage(Message.SUCCESS),
							messageSource.getMessage(Message.LIST_DATA_SAVED_SUCCESS)
							, 3000);
		}

		if (callSaveReservation) {
			source.saveReservationChangesAction();
		}

		source.resetUnsavedChangesFlag();

		this.source.getSource().closeList(currentlySavedList);
	}

	public void showErrorOnSavingGermplasmList(Boolean showMessages) {
		if (showMessages) {
			MessageNotifier.showError(this.source.getWindow(),
					messageSource.getMessage(Message.ERROR_DATABASE)
					, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
		}
	}

	private void saveListDataColumns(GermplasmList listToSave) {
		try {
			dataManager.saveListDataColumns(
					source.getAddColumnContextMenu().getListDataCollectionFromTable(listDataTable));
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in saving added germplasm list columns: " + listToSave, e);
			MessageNotifier.showError(this.source.getWindow(),
					messageSource.getMessage(Message.ERROR_DATABASE),
					messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
		}
	}

	public boolean validateListDetails(GermplasmList list, GermplasmList currentlySavedList) {
		boolean isValid = true;
		if (list.getName() == null || list.getName().length() == 0) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					messageSource.getMessage(Message.NAME_CAN_NOT_BE_BLANK));
			isValid = false;
		} else if (list.getDescription() == null || list.getDescription().length() == 0) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					messageSource.getMessage(Message.DESCRIPTION_CAN_NOT_BE_BLANK));
			isValid = false;
		} else if (list.getName().length() > 50) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					messageSource.getMessage(Message.NAME_CAN_NOT_BE_LONG));
			isValid = false;
		} else if (list.getDescription().length() > 255) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					messageSource.getMessage(Message.DESCRIPTION_CAN_NOT_BE_LONG));
			isValid = false;
		} else if (list.getDate() == null) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), 
					"Please select a date.");
			isValid = false;
		} else {
			if (currentlySavedList == null) {
				isValid = validateListName(list);
			}
		}
		return isValid;
	}

	private boolean validateListName(GermplasmList list) {
		try {
			List<GermplasmList> lists = this.dataManager
					.getGermplasmListByName(list.getName(), 0, 5, Operation.EQUAL);
			if (!lists.isEmpty() && lists.size() == 1 && lists.get(0).getId() != list.getId()) {
				MessageNotifier.showRequiredFieldError(this.source.getWindow(),
						messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE));
				return false;
			}
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with getting germplasm list by list name - " + list.getName(), ex);
			MessageNotifier.showError(this.source.getWindow(),
					messageSource.getMessage(Message.ERROR_DATABASE),
					messageSource.getMessage(Message.ERROR_VALIDATING_LIST));
			return false;
		}

		return true;
	}

	private void updateListDataTableContent(GermplasmList currentlySavedList) {
		try {
			int listDataCount = (int) this.dataManager
					.countGermplasmListDataByListId(currentlySavedList.getId());
			List<GermplasmListData> savedListEntries = this.inventoryDataManager
					.getLotCountsForList(currentlySavedList.getId(), 0, listDataCount);

			Table tempTable = cloneAddedColumnsToTemp(this.listDataTable);

			this.listDataTable.setImmediate(true);
			this.listDataTable.removeAllItems();

			for (final GermplasmListData entry : savedListEntries) {
				final Item item = this.listDataTable.addItem(entry.getId());

				Button gidButton = new Button(String.format("%s", entry.getGid()), new GidLinkClickListener(entry.getGid().toString(), true));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);

				CheckBox tagCheckBox = new CheckBox();
				tagCheckBox.setImmediate(true);
				tagCheckBox.addListener(new ClickListener() {
					private static final long serialVersionUID = 1L;
					@Override
					public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
						CheckBox itemCheckBox = (CheckBox) event.getButton();
						if(((Boolean) itemCheckBox.getValue()).equals(true)){
							listDataTable.select(entry.getId());
						} else {
							listDataTable.unselect(entry.getId());
						}
					}

				});

				Button designationButton = new Button(entry.getDesignation(), new GidLinkClickListener(entry.getGid().toString(), true));
				designationButton.setStyleName(BaseTheme.BUTTON_LINK);
				designationButton.setDescription("Click to view Germplasm information");

				//Inventory Related Columns

				//#1 Available Inventory
				String availInv = STRING_DASH;
				if(entry.getInventoryInfo().getActualInventoryLotCount() != null && entry.getInventoryInfo().getActualInventoryLotCount() != 0){
					availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
				}
				Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(source,currentlySavedList.getId(),entry.getId(), entry.getGid()));
				inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
				inventoryButton.setDescription("Click to view Inventory Details");


				if(availInv.equals(STRING_DASH)){
					inventoryButton.setEnabled(false);
					inventoryButton.setDescription("No Lot for this Germplasm");
				} else {
					inventoryButton.setDescription("Click to view Inventory Details");
				}

				//#2 Seed Reserved
				String seedRes = STRING_DASH;
				if(entry.getInventoryInfo().getReservedLotCount() != null && entry.getInventoryInfo().getReservedLotCount() != 0){
					seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
				}

				item.getItemProperty(ColumnLabels.TAG.getName()).setValue(tagCheckBox);
				item.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
				item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
				item.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(
						entry.getEntryCode());
				item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
				item.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(
						entry.getGroupName());
				item.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(
						entry.getSeedSource());
				item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(
						inventoryButton);
				item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

			}

			copyAddedColumnsFromTemp(tempTable);
			this.listDataTable.requestRepaint();

		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(),
					messageSource.getMessage(Message.ERROR_DATABASE),
					messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES));
		}
	}

	private Table cloneAddedColumnsToTemp(Table sourceTable) {
		Table newTable = new Table();

		// copy added column values from source table
		for (Object sourceItemId : sourceTable.getItemIds()) {
			Item sourceItem = sourceTable.getItem(sourceItemId);
			Item newItem = newTable.addItem(sourceItemId);

			for (String addablePropertyId : AddColumnContextMenu.ADDABLE_PROPERTY_IDS) {
				// copy only addable properties present in source table
				if (AddColumnContextMenu.propertyExists(addablePropertyId, sourceTable)) {
					// setup added columns first before copying values
					createContainerPropertyOfAddedColumnToTempTable(newTable,
							addablePropertyId);

					// copy value to new table
					Property sourceItemProperty = sourceItem.getItemProperty(addablePropertyId);
					newItem.getItemProperty(addablePropertyId)
							.setValue(sourceItemProperty.getValue());
				}
			}
		}

		return newTable;
	}

	public void createContainerPropertyOfAddedColumnToTempTable(Table newTable,
			String addablePropertyId) {

		if (AddColumnContextMenuOption.isPartOfAddColumnContextMenuOption(addablePropertyId)) {
			newTable.addContainerProperty(addablePropertyId,
					AddColumnContextMenuOption.getClassProperty(addablePropertyId), "");
		}
	}

	private void copyAddedColumnsFromTemp(Table tempTable) {
		List<Object> listDataIdList = new ArrayList<Object>(this.listDataTable.getItemIds());
		List<Object> tempTableIdList = new ArrayList<Object>(tempTable.getItemIds());

		// iterate through actual table rows using index (so temp table counterpart items can be accessed easily)
		for (int i = 0; i < listDataIdList.size(); i++) {
			Item listDataItem = this.listDataTable.getItem(listDataIdList.get(i));
			Item tempItem = tempTable.getItem(tempTableIdList.get(i));

			// for each row, get columns from temp table, then copy to actual table
			for (Object tempPropertyId : tempTable.getContainerPropertyIds()) {
				// copy value from temp table to actual list data table
				Property tempItemProperty = tempItem.getItemProperty(tempPropertyId);
				listDataItem.getItemProperty(tempPropertyId).setValue(tempItemProperty.getValue());
			}
		}
	}

	private boolean areThereChangesToList(GermplasmList currentlySavedList,
			GermplasmList newListInfo) {
		if (!currentlySavedList.getName().equals(newListInfo.getName())) {
			return true;
		} else if (!currentlySavedList.getDescription().equals(newListInfo.getDescription())) {
			return true;
		} else if (!currentlySavedList.getType().equals(newListInfo.getType())) {
			return true;
		} else if (currentlySavedList.getDate() != newListInfo.getDate()) {
			return true;
		}

		return false;
	}

	private void setNeededValuesForNewListEntries(GermplasmList list,
			List<GermplasmListData> listEntries) {
		for (GermplasmListData listEntry : listEntries) {
			listEntry.setList(list);
			listEntry.setStatus(Integer.valueOf(0));
			listEntry.setLocalRecordId(Integer.valueOf(0));
		}
	}

	private boolean saveNewListEntries(List<GermplasmListData> listEntries) {
		try {
			List<Integer> savedEntryPKs = this.dataManager.addGermplasmListData(listEntries);

			if (!(savedEntryPKs.size() == listEntries.size())) {
				MessageNotifier.showError(this.source.getWindow(),
						messageSource.getMessage(Message.ERROR_DATABASE)
						, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
				return false;
			}
			return true;
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error in saving germplasm list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(),
					messageSource.getMessage(Message.ERROR_DATABASE)
					, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
			return false;
		}
	}

	public void setForceHasChanges(Boolean hasChanges) {
		forceHasChanges = hasChanges;
	}

	public void setDataManager(GermplasmListManager dataManager) {
		this.dataManager = dataManager;
	}

	public void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setSource(ListBuilderComponent source) {
		this.source = source;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// do nothing
	}
}


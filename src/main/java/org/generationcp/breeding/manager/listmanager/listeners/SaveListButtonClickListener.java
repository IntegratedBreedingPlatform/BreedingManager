
package org.generationcp.breeding.manager.listmanager.listeners;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class SaveListButtonClickListener implements Button.ClickListener, InitializingBean {

	private static final long serialVersionUID = -2641642996209640461L;

	private static final Logger LOG = LoggerFactory.getLogger(SaveListButtonClickListener.class);

	private static final String STRING_DASH = "-";

	private ListBuilderComponent source;

	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private InventoryDataManager inventoryDataManager;

	@Resource
	private ContextUtil contextUtil;

	private final Table listDataTable;

	private Boolean forceHasChanges = false;

	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	public SaveListButtonClickListener(final ListBuilderComponent source, final Table listDataTable,
			final SimpleResourceBundleMessageSource messageSource) {
		this.source = source;
		this.listDataTable = listDataTable;
		this.messageSource = messageSource;
	}

	@Override
	public void buttonClick(final ClickEvent event) {
		this.doSaveAction();
	}

	public void doSaveAction() {
		this.doSaveAction(true);
	}

	public void doSaveAction(final Boolean showMessages) {
		this.doSaveAction(showMessages, true);
	}

	public void doSaveAction(final Boolean showMessages, final Boolean callSaveReservation) {
		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				GermplasmList currentlySavedList = SaveListButtonClickListener.this.source.getCurrentlySavedGermplasmList();
				GermplasmList listToSave = SaveListButtonClickListener.this.source.getCurrentlySetGermplasmListInfo();

				if (listToSave == null) {

					return;
				}
				final List<GermplasmListData> listEntries = SaveListButtonClickListener.this.source.getListEntriesFromTable();

				if (!SaveListButtonClickListener.this.validateListDetails(listToSave, currentlySavedList)) {
					return;
				}

				if (currentlySavedList == null || listToSave.getId() == null) {
					listToSave.setStatus(1);

					try {
						listToSave.setUserId(SaveListButtonClickListener.this.contextUtil.getCurrentUserLocalId());
						listToSave.setProgramUUID(SaveListButtonClickListener.this.contextUtil.getCurrentProgramUUID());

						final Integer listId = SaveListButtonClickListener.this.germplasmListManager.addGermplasmList(listToSave);

						if (listId != null) {
							final GermplasmList listSaved =
									SaveListButtonClickListener.this.germplasmListManager.getGermplasmListById(listId);
							currentlySavedList = listSaved;
							SaveListButtonClickListener.this.source.setCurrentlySavedGermplasmList(listSaved);

							SaveListButtonClickListener.this.source.setHasUnsavedChanges(false);

							SaveListButtonClickListener.this.source.getSource().getListSelectionComponent().showNodeOnTree(listId);

						} else {
							SaveListButtonClickListener.this.showErrorOnSavingGermplasmList(showMessages);
							return;
						}
					} catch (final MiddlewareQueryException ex) {
						SaveListButtonClickListener.LOG.error("Error in saving germplasm list: " + listToSave, ex);
						SaveListButtonClickListener.this.showErrorOnSavingGermplasmList(showMessages);
						return;
					}

					if (!listEntries.isEmpty()) {
						SaveListButtonClickListener.this.setNeededValuesForNewListEntries(currentlySavedList, listEntries);

						if (!SaveListButtonClickListener.this.saveNewListEntries(listEntries)) {
							return;
						}

						SaveListButtonClickListener.this.updateListDataTableContent(currentlySavedList);

						SaveListButtonClickListener.this.saveListDataColumns(listToSave);
					}

				} else if (currentlySavedList != null) {

					if (SaveListButtonClickListener.this.areThereChangesToList(currentlySavedList, listToSave)
							|| SaveListButtonClickListener.this.forceHasChanges) {
						if (!currentlySavedList.getName().equals(listToSave.getName())
								&& !SaveListButtonClickListener.this.validateListName(listToSave)) {
							return;
						}

						listToSave =
								ListCommonActionsUtil.overwriteList(listToSave, SaveListButtonClickListener.this.germplasmListManager,
										SaveListButtonClickListener.this.source, SaveListButtonClickListener.this.messageSource,
										showMessages);
					}

					if (listToSave != null) {
						final boolean thereAreChangesInListEntries =
								ListCommonActionsUtil.overwriteListEntries(listToSave, listEntries,
										SaveListButtonClickListener.this.forceHasChanges,
										SaveListButtonClickListener.this.germplasmListManager, SaveListButtonClickListener.this.source,
										SaveListButtonClickListener.this.messageSource, showMessages);

						if (thereAreChangesInListEntries) {
							SaveListButtonClickListener.this.updateListDataTableContent(currentlySavedList);
						}

						if (!listEntries.isEmpty()) {
							SaveListButtonClickListener.this.saveListDataColumns(listToSave);
						}
					}
				}

				try {
					SaveListButtonClickListener.this.contextUtil.logProgramActivity(
							"List Manager Save List",
							"Successfully saved list and list entries for: " + currentlySavedList.getId() + " - "
									+ currentlySavedList.getName());

					SaveListButtonClickListener.this.source.getBuildNewListDropHandler().setChanged(false);

				} catch (final MiddlewareQueryException ex) {
					SaveListButtonClickListener.LOG.error("Error with saving Workbench activity.", ex);
				}

				if (showMessages) {
					MessageNotifier.showMessage(SaveListButtonClickListener.this.source.getWindow(),
							SaveListButtonClickListener.this.messageSource.getMessage(Message.SUCCESS),
							SaveListButtonClickListener.this.messageSource.getMessage(Message.LIST_DATA_SAVED_SUCCESS), 3000);
				}

				if (callSaveReservation) {
					SaveListButtonClickListener.this.source.saveReservationChangesAction();
				}

				SaveListButtonClickListener.this.source.resetUnsavedChangesFlag();

				SaveListButtonClickListener.this.source.getSource().closeList(currentlySavedList);
			}
		});
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	public void showErrorOnSavingGermplasmList(final Boolean showMessages) {
		if (showMessages) {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
		}
	}

	private void saveListDataColumns(final GermplasmList listToSave) {
		try {
			this.germplasmListManager.saveListDataColumns(this.source.getAddColumnContextMenu().getListDataCollectionFromTable(
					this.listDataTable));
		} catch (final MiddlewareQueryException e) {
			SaveListButtonClickListener.LOG.error("Error in saving added germplasm list columns: " + listToSave, e);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
		}
	}

	public boolean validateListDetails(final GermplasmList list, final GermplasmList currentlySavedList) {
		boolean isValid = true;
		if (list.getName() == null || list.getName().length() == 0) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), this.messageSource.getMessage(Message.NAME_CAN_NOT_BE_BLANK));
			isValid = false;
		} else if (list.getName().length() > 50) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), this.messageSource.getMessage(Message.NAME_CAN_NOT_BE_LONG));
			isValid = false;
		} else if (list.getDescription().length() > 255) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					this.messageSource.getMessage(Message.DESCRIPTION_CAN_NOT_BE_LONG));
			isValid = false;
		} else if (list.getDate() == null) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), "Please select a date.");
			isValid = false;
		} else {
			if (currentlySavedList == null) {
				isValid = this.validateListName(list);
			}
		}
		return isValid;
	}

	private boolean validateListName(final GermplasmList list) {
		try {
			final List<GermplasmList> lists =
					this.germplasmListManager.getGermplasmListByName(list.getName(), this.getCurrentProgramUUID(), 0, 5, Operation.EQUAL);
			if (!lists.isEmpty() && lists.size() == 1 && lists.get(0).getId() != list.getId()) {
				MessageNotifier.showRequiredFieldError(this.source.getWindow(),
						this.messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE));
				return false;
			}
		} catch (final MiddlewareQueryException ex) {
			SaveListButtonClickListener.LOG.error("Error with getting germplasm list by list name - " + list.getName(), ex);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_VALIDATING_LIST));
			return false;
		}

		return true;
	}

	private void updateListDataTableContent(final GermplasmList currentlySavedList) {
		try {
			final int listDataCount = (int) this.germplasmListManager.countGermplasmListDataByListId(currentlySavedList.getId());
			final List<GermplasmListData> savedListEntries =
					this.inventoryDataManager.getLotCountsForList(currentlySavedList.getId(), 0, listDataCount);

			final Table tempTable = this.cloneAddedColumnsToTemp(this.listDataTable);

			this.listDataTable.setImmediate(true);
			this.listDataTable.removeAllItems();

			for (final GermplasmListData entry : savedListEntries) {
				final Item item = this.listDataTable.addItem(entry.getId());

				final Button gidButton =
						new Button(String.format("%s", entry.getGid()), new GidLinkClickListener(entry.getGid().toString(), true));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);

				final CheckBox tagCheckBox = new CheckBox();
				tagCheckBox.setImmediate(true);
				tagCheckBox.addListener(new ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
						final CheckBox itemCheckBox = (CheckBox) event.getButton();
						if (((Boolean) itemCheckBox.getValue()).equals(true)) {
							SaveListButtonClickListener.this.listDataTable.select(entry.getId());
						} else {
							SaveListButtonClickListener.this.listDataTable.unselect(entry.getId());
						}
					}

				});

				final Button designationButton =
						new Button(entry.getDesignation(), new GidLinkClickListener(entry.getGid().toString(), true));
				designationButton.setStyleName(BaseTheme.BUTTON_LINK);
				designationButton.setDescription("Click to view Germplasm information");

				// Inventory Related Columns

				// #1 Available Inventory
				String availInv = SaveListButtonClickListener.STRING_DASH;
				if (entry.getInventoryInfo().getActualInventoryLotCount() != null
						&& entry.getInventoryInfo().getActualInventoryLotCount() != 0) {
					availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
				}
				final Button inventoryButton =
						new Button(availInv, new InventoryLinkButtonClickListener(this.source, currentlySavedList.getId(), entry.getId(),
								entry.getGid()));
				inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
				inventoryButton.setDescription("Click to view Inventory Details");

				if (availInv.equals(SaveListButtonClickListener.STRING_DASH)) {
					inventoryButton.setEnabled(false);
					inventoryButton.setDescription("No Lot for this Germplasm");
				} else {
					inventoryButton.setDescription("Click to view Inventory Details");
				}

				// #2 Seed Reserved
				String seedRes = SaveListButtonClickListener.STRING_DASH;
				if (entry.getInventoryInfo().getReservedLotCount() != null && entry.getInventoryInfo().getReservedLotCount() != 0) {
					seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
				}

				item.getItemProperty(ColumnLabels.TAG.getName()).setValue(tagCheckBox);
				item.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
				item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
				item.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
				item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
				item.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(entry.getGroupName());
				item.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(entry.getSeedSource());
				item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
				item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

			}

			this.copyAddedColumnsFromTemp(tempTable);
			this.listDataTable.requestRepaint();

		} catch (final MiddlewareQueryException ex) {
			SaveListButtonClickListener.LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES));
		}
	}

	private Table cloneAddedColumnsToTemp(final Table sourceTable) {
		final Table newTable = new Table();

		// copy added column values from source table
		for (final Object sourceItemId : sourceTable.getItemIds()) {
			final Item sourceItem = sourceTable.getItem(sourceItemId);
			final Item newItem = newTable.addItem(sourceItemId);

			for (final String addablePropertyId : AddColumnContextMenu.ADDABLE_PROPERTY_IDS) {
				// copy only addable properties present in source table
				if (AddColumnContextMenu.propertyExists(addablePropertyId, sourceTable)) {
					// setup added columns first before copying values
					this.createContainerPropertyOfAddedColumnToTempTable(newTable, addablePropertyId);

					// copy value to new table
					final Property sourceItemProperty = sourceItem.getItemProperty(addablePropertyId);
					newItem.getItemProperty(addablePropertyId).setValue(sourceItemProperty.getValue());
				}
			}
		}

		return newTable;
	}

	public void createContainerPropertyOfAddedColumnToTempTable(final Table newTable, final String addablePropertyId) {

		if (AddColumnContextMenuOption.isPartOfAddColumnContextMenuOption(addablePropertyId)) {
			newTable.addContainerProperty(addablePropertyId, AddColumnContextMenuOption.getClassProperty(addablePropertyId), "");
		}
	}

	private void copyAddedColumnsFromTemp(final Table tempTable) {
		final List<Object> listDataIdList = new ArrayList<Object>(this.listDataTable.getItemIds());
		final List<Object> tempTableIdList = new ArrayList<Object>(tempTable.getItemIds());

		// iterate through actual table rows using index (so temp table counterpart items can be accessed easily)
		for (int i = 0; i < listDataIdList.size(); i++) {
			final Item listDataItem = this.listDataTable.getItem(listDataIdList.get(i));
			final Item tempItem = tempTable.getItem(tempTableIdList.get(i));

			// for each row, get columns from temp table, then copy to actual table
			for (final Object tempPropertyId : tempTable.getContainerPropertyIds()) {
				// copy value from temp table to actual list data table
				final Property tempItemProperty = tempItem.getItemProperty(tempPropertyId);
				listDataItem.getItemProperty(tempPropertyId).setValue(tempItemProperty.getValue());
			}
		}
	}

	private boolean areThereChangesToList(final GermplasmList currentlySavedList, final GermplasmList newListInfo) {
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

	private void setNeededValuesForNewListEntries(final GermplasmList list, final List<GermplasmListData> listEntries) {
		for (final GermplasmListData listEntry : listEntries) {
			listEntry.setList(list);
			listEntry.setStatus(Integer.valueOf(0));
			listEntry.setLocalRecordId(Integer.valueOf(0));
		}
	}

	private boolean saveNewListEntries(final List<GermplasmListData> listEntries) {
		try {
			final List<Integer> savedEntryPKs = this.germplasmListManager.addGermplasmListData(listEntries);

			if (!(savedEntryPKs.size() == listEntries.size())) {
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
				return false;
			}
			return true;
		} catch (final MiddlewareQueryException ex) {
			SaveListButtonClickListener.LOG.error("Error in saving germplasm list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
			return false;
		}
	}

	public void setForceHasChanges(final Boolean hasChanges) {
		this.forceHasChanges = hasChanges;
	}

	public void setDataManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setSource(final ListBuilderComponent source) {
		this.source = source;
	}

	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// do nothing
	}
}

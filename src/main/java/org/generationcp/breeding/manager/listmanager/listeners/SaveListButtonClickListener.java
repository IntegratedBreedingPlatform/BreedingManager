package org.generationcp.breeding.manager.listmanager.listeners;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.AddColumnContextMenu;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

public class SaveListButtonClickListener implements Button.ClickListener{

	private static final String STRING_DASH = "-";

	private static final long serialVersionUID = -2641642996209640461L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SaveListButtonClickListener.class);

	private ListBuilderComponent source;
	private GermplasmListManager dataManager;
	private WorkbenchDataManager workbenchDataManager;
	private InventoryDataManager inventoryDataManager;
	private Table listDataTable;
	
	private Boolean forceHasChanges = false;
	
	private SimpleResourceBundleMessageSource messageSource;
	
	public SaveListButtonClickListener(ListBuilderComponent source, GermplasmListManager dataManager, Table listDataTable
			, SimpleResourceBundleMessageSource messageSource, WorkbenchDataManager workbenchDataManager, InventoryDataManager inventoryDataManager){
		this.source = source;
		this.dataManager = dataManager;
		this.listDataTable = listDataTable;
		this.messageSource = messageSource;
		this.workbenchDataManager = workbenchDataManager;
		this.inventoryDataManager = inventoryDataManager;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		doSaveAction();
	}
	
	public void doSaveAction(){
		doSaveAction(true);
	}
	
	public void doSaveAction(Boolean showMessages){
		doSaveAction(showMessages, true);
	}
	
	public void doSaveAction(Boolean showMessages, Boolean callSaveReservation){
		GermplasmList currentlySavedList = this.source.getCurrentlySavedGermplasmList();
		GermplasmList listToSave = this.source.getCurrentlySetGermplasmListInfo();
		
		if(listToSave == null){
			return;
		}
		List<GermplasmListData> listEntries = this.source.getListEntriesFromTable();
		
		if(!validateListDetails(listToSave, currentlySavedList)){
			return;
		}
		
		if(currentlySavedList == null || listToSave.getId()==null){
			listToSave.setStatus(Integer.valueOf(1));
			listToSave.setUserId(getLocalIBDBUserId());
			
			try{
				Integer listId = this.dataManager.addGermplasmList(listToSave);
				
				if(listId != null){
					GermplasmList listSaved = this.dataManager.getGermplasmListById(listId);
					currentlySavedList = listSaved;
					this.source.setCurrentlySavedGermplasmList(listSaved);
					
					source.setHasUnsavedChanges(false);
					
					((ListManagerMain) this.source.getSource()).getListSelectionComponent().showNodeOnTree(listId);
					
				} else{
					if(showMessages){
					    MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
							, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
					}
					return;
				}
			} catch(MiddlewareQueryException ex){
				LOG.error("Error in saving germplasm list: " + listToSave, ex);
				if(showMessages) {
                    MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
                }
				return;
			}
			
			if(!listEntries.isEmpty()){
				setNeededValuesForNewListEntries(currentlySavedList, listEntries);
				
				if(!saveNewListEntries(listEntries)){
					return;
				}
				
				updateListDataTableContent(currentlySavedList);
				
				saveListDataColumns(listToSave);
			}
			
		} else if(currentlySavedList != null){
			
			if(areThereChangesToList(currentlySavedList, listToSave) || forceHasChanges){
				if(!currentlySavedList.getName().equals(listToSave.getName())){
					if(!validateListName(listToSave)){
						return;
					}
				}
				
				listToSave = ListCommonActionsUtil.overwriteList(
						listToSave, 
						dataManager, source, messageSource, showMessages);
			} 
			
			if(listToSave!=null) {
				boolean thereAreChangesInListEntries = 
					ListCommonActionsUtil.overwriteListEntries(
						listToSave, 
						listEntries, forceHasChanges, 
						dataManager, source, messageSource, showMessages);
				
				if(thereAreChangesInListEntries) {
					updateListDataTableContent(currentlySavedList);
				}
				
				if(!listEntries.isEmpty()){
					saveListDataColumns(listToSave);
				}
			}
		}
		
		try{
			ProjectActivity activity = new ProjectActivity();
			activity.setCreatedAt(new Date());
			activity.setName("List Manager Save List");
			activity.setDescription("Successfully saved list and list entries for: " + currentlySavedList.getId() + " - " + currentlySavedList.getName());
			WorkbenchRuntimeData runtimeData = this.workbenchDataManager.getWorkbenchRuntimeData();
			Project project = this.workbenchDataManager.getLastOpenedProject(runtimeData.getUserId());
			User user = this.workbenchDataManager.getUserById(runtimeData.getUserId());
			activity.setProject(project);
			activity.setUser(user);
			this.workbenchDataManager.addProjectActivity(activity);

			source.getBuildNewListDropHandler().setChanged(false);
			
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with saving Workbench activity.", ex);
		}
		
		if(showMessages) {
            MessageNotifier.showMessage(this.source.getWindow(), messageSource.getMessage(Message.SUCCESS), messageSource.getMessage(Message.LIST_DATA_SAVED_SUCCESS)
                    , 3000);
        }
		
		if(callSaveReservation) {
            source.saveReservationChangesAction();
        }
		
		source.resetUnsavedChangesFlag();
		
		((ListManagerMain) this.source.getSource()).closeList(currentlySavedList);
	}
	
	private void saveListDataColumns(GermplasmList listToSave) {
	    try {
            dataManager.saveListDataColumns(source.getAddColumnContextMenu().getListDataCollectionFromTable(listDataTable));
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in saving added germplasm list columns: " + listToSave, e);
            MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
        }
	}
	
	
	private boolean validateListDetails(GermplasmList list, GermplasmList currentlySavedList){
		
		if(list.getName() == null || list.getName().length() == 0){
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), messageSource.getMessage(Message.NAME_CAN_NOT_BE_BLANK));
			return false;
		} else if(list.getDescription() == null || list.getDescription().length() == 0){
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), messageSource.getMessage(Message.DESCRIPTION_CAN_NOT_BE_BLANK));
			return false;
		} else if(list.getName().length() > 50){
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), messageSource.getMessage(Message.NAME_CAN_NOT_BE_LONG));
			return false;
		} else if(list.getDescription().length() > 255){
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), messageSource.getMessage(Message.DESCRIPTION_CAN_NOT_BE_LONG));
			return false;
		} else if(list.getDate() == null){
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), "Please select a date.");
			return false;
		} else {
			if(currentlySavedList == null){
				return validateListName(list);
			}
		}
		return true;
	}
	
	private boolean validateListName(GermplasmList list){
		try{
			List<GermplasmList> centralLists = this.dataManager.getGermplasmListByName(list.getName(), 0, 5, Operation.EQUAL, Database.CENTRAL);
			if(!centralLists.isEmpty()){
				if(centralLists.size()==1 && centralLists.get(0).getId()!=list.getId()){
					MessageNotifier.showRequiredFieldError(this.source.getWindow(), messageSource.getMessage(Message.EXISTING_LIST_IN_CENTRAL_ERROR_MESSAGE));
					return false;
				}
			}
			
			List<GermplasmList> localLists = this.dataManager.getGermplasmListByName(list.getName(), 0, 5, Operation.EQUAL, Database.LOCAL);
			if(!localLists.isEmpty()){
				if(localLists.size()==1 && localLists.get(0).getId()!=list.getId()){
					MessageNotifier.showRequiredFieldError(this.source.getWindow(), messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE));
					return false;
				}
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting germplasm list by list name - " + list.getName(), ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_VALIDATING_LIST));
			return false;
		}
		
		return true;
	}
	
	private Integer getLocalIBDBUserId(){
		try{
			WorkbenchRuntimeData runtimeData = this.workbenchDataManager.getWorkbenchRuntimeData();
			Project project = this.workbenchDataManager.getLastOpenedProject(runtimeData.getUserId());
			return this.workbenchDataManager.getLocalIbdbUserId(runtimeData.getUserId(), project.getProjectId());
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting the local IBDB user ID of the currently logged in workbench user.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_LOCAL_IBDB_USER_ID));
			return null;
		}
	}
	
	private void updateListDataTableContent(GermplasmList currentlySavedList){
		try{
			int listDataCount = (int) this.dataManager.countGermplasmListDataByListId(currentlySavedList.getId());
			List<GermplasmListData> savedListEntries = this.inventoryDataManager.getLotCountsForList(currentlySavedList.getId(), 0, listDataCount);

			Table tempTable = cloneAddedColumnsToTemp(this.listDataTable);
			
			this.listDataTable.setImmediate(true);
			this.listDataTable.removeAllItems();

			for(final GermplasmListData entry : savedListEntries){
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
				String availInv = STRING_DASH; //default value
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
				String seedRes = STRING_DASH; //default value
				if(entry.getInventoryInfo().getReservedLotCount() != null && entry.getInventoryInfo().getReservedLotCount() != 0){
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

			copyAddedColumnsFromTemp(tempTable);
			
            this.listDataTable.requestRepaint();
			return;
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES));
			return;
		}
	}

	
	private Table cloneAddedColumnsToTemp(Table sourceTable) {
	    Table newTable = new Table();
	    
	    // copy added column values from source table
	    for (Object sourceItemId : sourceTable.getItemIds()){
            Item sourceItem = sourceTable.getItem(sourceItemId);
            Item newItem = newTable.addItem(sourceItemId);
            
            for(String addablePropertyId : AddColumnContextMenu.ADDABLE_PROPERTY_IDS){
                // copy only addable properties present in source table
                if(AddColumnContextMenu.propertyExists(addablePropertyId, sourceTable)){
                    // setup added columns first before copying values
                    if(addablePropertyId.equals(ColumnLabels.PREFERRED_ID.getName())){
                        newTable.addContainerProperty(ColumnLabels.PREFERRED_ID.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.PREFERRED_NAME.getName())){
                        newTable.addContainerProperty(ColumnLabels.PREFERRED_NAME.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.GERMPLASM_DATE.getName())){
                        newTable.addContainerProperty(ColumnLabels.GERMPLASM_DATE.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.GERMPLASM_LOCATION.getName())){
                        newTable.addContainerProperty(ColumnLabels.GERMPLASM_LOCATION.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())){
                        newTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())){
                        newTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())){
                        newTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NUMBER.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
                        newTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_GROUP.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.CROSS_FEMALE_GID.getName())){
                        newTable.addContainerProperty(ColumnLabels.CROSS_FEMALE_GID.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
                        newTable.addContainerProperty(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.CROSS_MALE_GID.getName())){
                        newTable.addContainerProperty(ColumnLabels.CROSS_MALE_GID.getName(), String.class, "");
                    } else if(addablePropertyId.equals(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
                        newTable.addContainerProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), String.class, "");
                    }

                    // copy value to new table
                    Property sourceItemProperty = sourceItem.getItemProperty(addablePropertyId);
                    newItem.getItemProperty(addablePropertyId).setValue(sourceItemProperty.getValue());
                }
            }
	    }
	    
	    return newTable;
	}
	
	private void copyAddedColumnsFromTemp(Table tempTable) {
	    List<Object> listDataIdList = new ArrayList<Object>(this.listDataTable.getItemIds());
        List<Object> tempTableIdList = new ArrayList<Object>(tempTable.getItemIds());
        
        // iterate through actual table rows using index (so temp table counterpart items can be accessed easily)
        for (int i=0; i<listDataIdList.size(); i++) {
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
	
    private boolean areThereChangesToList(GermplasmList currentlySavedList, GermplasmList newListInfo){
		if(!currentlySavedList.getName().equals(newListInfo.getName())){
			return true;
		} else if(!currentlySavedList.getDescription().equals(newListInfo.getDescription())){
			return true;
		} else if(!currentlySavedList.getType().equals(newListInfo.getType())){
			return true;
		} else if(currentlySavedList.getDate() != newListInfo.getDate()){
			return true;
		}
		
		return false;
	}
	
	private void setNeededValuesForNewListEntries(GermplasmList list, List<GermplasmListData> listEntries){
		for(GermplasmListData listEntry : listEntries){
			listEntry.setList(list);
			listEntry.setStatus(Integer.valueOf(0));
			listEntry.setLocalRecordId(Integer.valueOf(0));
		}
	}
	
	private boolean saveNewListEntries(List<GermplasmListData> listEntries){
		try{
			List<Integer> savedEntryPKs = this.dataManager.addGermplasmListData(listEntries);
			
			if(!(savedEntryPKs.size() == listEntries.size())){
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
						, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
				return false;
			}
			return true;
		} catch(MiddlewareQueryException ex){
			LOG.error("Error in saving germplasm list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
			return false;
		}
	}
	
	public void setForceHasChanges(Boolean hasChanges){
		forceHasChanges = hasChanges;
	}
	
    
}


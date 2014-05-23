package org.generationcp.breeding.manager.listmanager.sidebyside.listeners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMain;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.util.AddColumnContextMenu;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
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
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

public class SaveListButtonClickListener implements Button.ClickListener{

	private static final long serialVersionUID = -2641642996209640461L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SaveListButtonClickListener.class);

	private ListBuilderComponent source;
	private GermplasmListManager dataManager;
	private WorkbenchDataManager workbenchDataManager;
	private Table listDataTable;
	
	private Boolean forceHasChanges = false;
	
	private SimpleResourceBundleMessageSource messageSource;
	
	public SaveListButtonClickListener(ListBuilderComponent source, GermplasmListManager dataManager, Table listDataTable
			, SimpleResourceBundleMessageSource messageSource, WorkbenchDataManager workbenchDataManager){
		this.source = source;
		this.dataManager = dataManager;
		this.listDataTable = listDataTable;
		this.messageSource = messageSource;
		this.workbenchDataManager = workbenchDataManager;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		doSaveAction();
	}
	
	public void doSaveAction(){
		GermplasmList currentlySavedList = this.source.getCurrentlySavedGermplasmList();
		GermplasmList listToSave = this.source.getCurrentlySetGermplasmListInfo();
		
		if(listToSave == null){
			return;
		}
		List<GermplasmListData> listEntries = this.source.getListEntriesFromTable();
		System.out.println("List entries: "+listEntries);
		
		if(!validateListDetails(listToSave, currentlySavedList)){
			return;
		}
		
		if(currentlySavedList == null){
			listToSave.setStatus(Integer.valueOf(1));
			listToSave.setUserId(getLocalIBDBUserId());
			
			System.out.println("currentlySavedList is null");
			
			try{
				Integer listId = this.dataManager.addGermplasmList(listToSave);
				
				if(listId != null){
					GermplasmList listSaved = this.dataManager.getGermplasmListById(listId);
					currentlySavedList = listSaved;
					this.source.setCurrentlySavedGermplasmList(listSaved);
					
					source.setChanged(false);
					
					((ListManagerMain) this.source.getSource()).showNodeOnTree(listId);
					
				} else{
					MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
							, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
							, Notification.POSITION_CENTERED);
					return;
				}
			} catch(MiddlewareQueryException ex){
				LOG.error("Error in saving germplasm list: " + listToSave, ex);
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
						, Notification.POSITION_CENTERED);
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
			
			System.out.println("Currently saved list: "+currentlySavedList);
			System.out.println("List to save: "+listToSave);
			
			Boolean hasChangesToListDetails = false;
			
			SimpleDateFormat formatter = new SimpleDateFormat(CrossingManagerMain.DATE_AS_NUMBER_FORMAT);
			
			if(areThereChangesToList(currentlySavedList, listToSave) || forceHasChanges){
				System.out.println("has changes");
				if(!currentlySavedList.getName().equals(listToSave.getName())){
					if(!validateListName(listToSave)){
						return;
					}
				}
				
				try{
					GermplasmList listFromDB = this.dataManager.getGermplasmListById(currentlySavedList.getId());
					listFromDB.setName(listToSave.getName());
					listFromDB.setDescription(listToSave.getDescription());
					listFromDB.setDate(listToSave.getDate());
					listFromDB.setType(listToSave.getType());
					listFromDB.setNotes(listToSave.getNotes());
					listFromDB.setParent(listToSave.getParent());
					
					Integer listId = this.dataManager.updateGermplasmList(listFromDB);
					
					if(listId == null){
						MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
								, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
								, Notification.POSITION_CENTERED);
						return;
					} else{
						currentlySavedList = listFromDB;
						this.source.setCurrentlySavedGermplasmList(listFromDB);
						source.setChanged(false);
						
						((ListManagerMain) this.source.getSource()).showNodeOnTree(listId);
						
					}
				} catch(MiddlewareQueryException ex){
					LOG.error("Error in updating germplasm list: " + currentlySavedList.getId(), ex);
					MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
							, Notification.POSITION_CENTERED);
					return;
				}
			} else {
				System.out.println("has NO changes");
			}
			
			boolean thereAreChangesInListEntries = false;
			List<GermplasmListData> newEntries = getNewEntriesToSave(listEntries);
			System.out.println("New entries "+newEntries);
			if(!newEntries.isEmpty()){
				setNeededValuesForNewListEntries(currentlySavedList, newEntries);
				if(!saveNewListEntries(newEntries)){
					return;
				}
				thereAreChangesInListEntries = true;
			}
			
			List<GermplasmListData> entriesToUpdate = getUpdatedEntriesToSave(currentlySavedList, listEntries);
			System.out.println("Entries to update: "+entriesToUpdate);
			if(!entriesToUpdate.isEmpty()){
				if(!updateListEntries(entriesToUpdate)){
					return;
				}
				thereAreChangesInListEntries = true;
			}
			
			List<GermplasmListData> entriesToDelete = getEntriesToDelete(currentlySavedList, listEntries);
			System.out.println("Entries to delete: "+entriesToDelete);
			if(!entriesToDelete.isEmpty()){
				if(!updateListEntries(entriesToDelete)){
					return;
				}
				thereAreChangesInListEntries = true;
			}
			
			if(thereAreChangesInListEntries){
				updateListDataTableContent(currentlySavedList);
			}
			
			if(!listEntries.isEmpty()){
				saveListDataColumns(listToSave);
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
			
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with saving Workbench activity.", ex);
			ex.printStackTrace();
		}
		
		MessageNotifier.showMessage(this.source.getWindow(), messageSource.getMessage(Message.SUCCESS), messageSource.getMessage(Message.LIST_AND_ENTRIES_SAVED_SUCCESS)
				, 3000, Notification.POSITION_CENTERED);
		
		((ListManagerMain) this.source.getSource()).closeList(currentlySavedList);
	}
	
	private void saveListDataColumns(GermplasmList listToSave) {
	    try {
            dataManager.saveListDataColumns(source.getAddColumnContextMenu().getListDataCollectionFromTable(listDataTable)); 
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in saving added germplasm list columns: " + listToSave, e);
            MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
                    , Notification.POSITION_CENTERED);
            e.printStackTrace();
        }
	}
	
	private boolean validateListDetails(GermplasmList list, GermplasmList currentlySavedList){
		
		if(list.getName() == null || list.getName().length() == 0){
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT), messageSource.getMessage(Message.NAME_CAN_NOT_BE_BLANK)
					, Notification.POSITION_CENTERED);
			return false;
		} else if(list.getDescription() == null || list.getDescription().length() == 0){
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT), messageSource.getMessage(Message.DESCRIPTION_CAN_NOT_BE_BLANK)
					, Notification.POSITION_CENTERED);
			return false;
		} else if(list.getName().length() > 50){
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT), messageSource.getMessage(Message.NAME_CAN_NOT_BE_LONG)
					, Notification.POSITION_CENTERED);
			return false;
		} else if(list.getDescription().length() > 255){
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT), messageSource.getMessage(Message.DESCRIPTION_CAN_NOT_BE_LONG)
					, Notification.POSITION_CENTERED);
			return false;
		} else if(list.getDate() == null){
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT), "Please select a date."
					, Notification.POSITION_CENTERED);
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
					MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
						, messageSource.getMessage(Message.EXISTING_LIST_IN_CENTRAL_ERROR_MESSAGE)
						, Notification.POSITION_CENTERED);
					return false;
				}
			}
			
			List<GermplasmList> localLists = this.dataManager.getGermplasmListByName(list.getName(), 0, 5, Operation.EQUAL, Database.LOCAL);
			if(!localLists.isEmpty()){
				if(localLists.size()==1 && localLists.get(0).getId()!=list.getId()){
					MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
						, messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE)
						, Notification.POSITION_CENTERED);
					return false;
				}
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting germplasm list by list name - " + list.getName(), ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_VALIDATING_LIST)
					, Notification.POSITION_CENTERED);
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
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_LOCAL_IBDB_USER_ID)
					, Notification.POSITION_CENTERED);
			return null;
		}
	}
	
	private void updateListDataTableContent(GermplasmList currentlySavedList){
		try{
			int listDataCount = (int) this.dataManager.countGermplasmListDataByListId(currentlySavedList.getId());
			List<GermplasmListData> savedListEntries = this.dataManager.getGermplasmListDataByListId(currentlySavedList.getId(), 0, listDataCount);
			
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
	            
	            item.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(tagCheckBox);
	            item.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
	            item.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(designationButton);
	            item.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
	            item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entry.getEntryId());
	            item.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(entry.getGroupName());
	            item.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(entry.getSeedSource());
//	            item.getItemProperty(ListDataTablePropertyID.STATUS.getName()).setValue(entry.getStatusString());
			}

			copyAddedColumnsFromTemp(tempTable);
            
            this.listDataTable.requestRepaint();
			return;
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES)
					, Notification.POSITION_CENTERED);
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
                    if(addablePropertyId.equals(AddColumnContextMenu.PREFERRED_ID)){
                        newTable.addContainerProperty(AddColumnContextMenu.PREFERRED_ID, AddColumnContextMenu.PREFERRED_ID_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.PREFERRED_NAME)){
                        newTable.addContainerProperty(AddColumnContextMenu.PREFERRED_NAME, AddColumnContextMenu.PREFERRED_NAME_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.GERMPLASM_DATE)){
                        newTable.addContainerProperty(AddColumnContextMenu.GERMPLASM_DATE, AddColumnContextMenu.GERMPLASM_DATE_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.LOCATIONS)){
                        newTable.addContainerProperty(AddColumnContextMenu.LOCATIONS, AddColumnContextMenu.LOCATIONS_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.METHOD_NAME)){
                        newTable.addContainerProperty(AddColumnContextMenu.METHOD_NAME, AddColumnContextMenu.METHOD_NAME_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.METHOD_ABBREV)){
                        newTable.addContainerProperty(AddColumnContextMenu.METHOD_ABBREV, AddColumnContextMenu.METHOD_ABBREV_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.METHOD_NUMBER)){
                        newTable.addContainerProperty(AddColumnContextMenu.METHOD_NUMBER, AddColumnContextMenu.METHOD_NUMBER_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.METHOD_GROUP)){
                        newTable.addContainerProperty(AddColumnContextMenu.METHOD_GROUP, AddColumnContextMenu.METHOD_GROUP_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.CROSS_FEMALE_GID)){
                        newTable.addContainerProperty(AddColumnContextMenu.CROSS_FEMALE_GID, AddColumnContextMenu.CROSS_FEMALE_GID_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.CROSS_FEMALE_PREF_NAME)){
                        newTable.addContainerProperty(AddColumnContextMenu.CROSS_FEMALE_PREF_NAME, AddColumnContextMenu.CROSS_FEMALE_PREF_NAME_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.CROSS_MALE_GID)){
                        newTable.addContainerProperty(AddColumnContextMenu.CROSS_MALE_GID, AddColumnContextMenu.CROSS_MALE_GID_TYPE, "");
                    } else if(addablePropertyId.equals(AddColumnContextMenu.CROSS_MALE_PREF_NAME)){
                        newTable.addContainerProperty(AddColumnContextMenu.CROSS_MALE_PREF_NAME, AddColumnContextMenu.CROSS_MALE_PREF_NAME_TYPE, "");
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
	
	private List<GermplasmListData> getNewEntriesToSave(List<GermplasmListData> listEntries){
		List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		
		for(GermplasmListData entry: listEntries){
			if(entry.getId() > 0 || forceHasChanges){
				toreturn.add(entry);
			}
		}
		
		return toreturn;
	}
	
	private boolean saveNewListEntries(List<GermplasmListData> listEntries){
		try{
			List<Integer> savedEntryPKs = this.dataManager.addGermplasmListData(listEntries);
			
			if(!(savedEntryPKs.size() == listEntries.size())){
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
						, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES)
						, Notification.POSITION_CENTERED);
				return false;
			}
			return true;
		} catch(MiddlewareQueryException ex){
			LOG.error("Error in saving germplasm list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES)
					, Notification.POSITION_CENTERED);
			return false;
		}
	}
	
	private boolean updateListEntries(List<GermplasmListData> listEntries){
		try{
			List<Integer> savedEntryPKs = this.dataManager.updateGermplasmListData(listEntries);
			
			if(!(savedEntryPKs.size() == listEntries.size())){
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
						, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES)
						, Notification.POSITION_CENTERED);
				return false;
			}
			return true;
		} catch(MiddlewareQueryException ex){
			LOG.error("Error in updating germplasm list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES)
					, Notification.POSITION_CENTERED);
			return false;
		}
	}
	
	private List<GermplasmListData> getUpdatedEntriesToSave(GermplasmList currentlySavedList, List<GermplasmListData> listEntriesToCheck){
		List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		
		try{
			int listDataCount = (int) this.dataManager.countGermplasmListDataByListId(currentlySavedList.getId());
			List<GermplasmListData> savedListEntries = this.dataManager.getGermplasmListDataByListId(currentlySavedList.getId(), 0, listDataCount);
			
			for(GermplasmListData entryToCheck : listEntriesToCheck){
				if(entryToCheck.getId() < 0){
					GermplasmListData matchingSavedEntry = null;
					for(GermplasmListData savedEntry: savedListEntries){
						if(entryToCheck.getId().equals(savedEntry.getId())){
							matchingSavedEntry = savedEntry;
							break;
						}
					}
					
					if(matchingSavedEntry != null){
						boolean thereIsAChange = false;
						if(!matchingSavedEntry.getDesignation().equals(entryToCheck.getDesignation())){
							thereIsAChange = true;
							String designation = entryToCheck.getDesignation();
							if(designation != null && designation.length() != 0){
								matchingSavedEntry.setDesignation(designation);
							} else{
								matchingSavedEntry.setDesignation("-");
							}
						}
						
						if(!matchingSavedEntry.getEntryCode().equals(entryToCheck.getEntryCode())){
							thereIsAChange = true;
							String entryCode = entryToCheck.getEntryCode();
							if(entryCode != null && entryCode.length() != 0){
								matchingSavedEntry.setEntryCode(entryCode);
							} else{
								matchingSavedEntry.setEntryCode(entryToCheck.getEntryId().toString());
							}
						}
						
						if(!matchingSavedEntry.getEntryId().equals(entryToCheck.getEntryId())){
							thereIsAChange = true;
							matchingSavedEntry.setEntryId(entryToCheck.getEntryId());
						}
						
						if(!matchingSavedEntry.getGroupName().equals(entryToCheck.getGroupName())){
							thereIsAChange = true;
							String groupName = entryToCheck.getGroupName();
							if(groupName != null && groupName.length() != 0){
								if(groupName.length() > 255){
									groupName = groupName.substring(0, 255);
								}
								matchingSavedEntry.setGroupName(groupName);
							} else{
								matchingSavedEntry.setGroupName("-");
							}
						}
						
						if(!matchingSavedEntry.getSeedSource().equals(entryToCheck.getSeedSource())){
							thereIsAChange = true;
							String seedSource = entryToCheck.getSeedSource();
							if(seedSource != null && seedSource.length() != 0){
								matchingSavedEntry.setSeedSource(seedSource);
							} else{
								matchingSavedEntry.setSeedSource("-");
							}
						}
						
						if(thereIsAChange){
							toreturn.add(matchingSavedEntry);
						}
					}
				}
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES)
					, Notification.POSITION_CENTERED);
		}
		
		return toreturn;
	}
	
	private List<GermplasmListData> getEntriesToDelete(GermplasmList currentlySavedList, List<GermplasmListData> listEntriesToCheck){
		List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		
		try{
			int listDataCount = (int) this.dataManager.countGermplasmListDataByListId(currentlySavedList.getId());
			List<GermplasmListData> savedListEntries = this.dataManager.getGermplasmListDataByListId(currentlySavedList.getId(), 0, listDataCount);
			
			for(GermplasmListData savedEntry : savedListEntries){
				if(!listEntriesToCheck.contains(savedEntry)){
					savedEntry.setStatus(Integer.valueOf(9));
					toreturn.add(savedEntry);
				}
			}
			
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES)
					, Notification.POSITION_CENTERED);
		}
		
		return toreturn;
	}
	
	public void setForceHasChanges(Boolean hasChanges){
		forceHasChanges = hasChanges;
	}
	
    
}

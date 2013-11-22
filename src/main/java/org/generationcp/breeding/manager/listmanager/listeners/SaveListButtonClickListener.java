package org.generationcp.breeding.manager.listmanager.listeners;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.BuildNewListComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

public class SaveListButtonClickListener implements Button.ClickListener{

	private static final long serialVersionUID = -2641642996209640461L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SaveListButtonClickListener.class);

	private BuildNewListComponent source;
	private GermplasmListManager dataManager;
	private WorkbenchDataManager workbenchDataManager;
	private Table listDataTable;
	
	private SimpleResourceBundleMessageSource messageSource;
	
	public SaveListButtonClickListener(BuildNewListComponent source, GermplasmListManager dataManager, Table listDataTable
			, SimpleResourceBundleMessageSource messageSource, WorkbenchDataManager workbenchDataManager){
		this.source = source;
		this.dataManager = dataManager;
		this.listDataTable = listDataTable;
		this.messageSource = messageSource;
		this.workbenchDataManager = workbenchDataManager;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		GermplasmList currentlySavedList = this.source.getCurrentlySavedGermplasmList();
		GermplasmList listToSave = this.source.getCurrentlySetGermplasmListInfo();
		List<GermplasmListData> listEntries = this.source.getListEntriesFromTable();
		
		if(listEntries.isEmpty()){
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
					, messageSource.getMessage(Message.NO_ENTRIES_ERROR_MESSAGE), Notification.POSITION_CENTERED);
			return;
		}
		
		if(!validateListDetails(listToSave, currentlySavedList)){
			return;
		}
		
		if(currentlySavedList == null){
			listToSave.setStatus(Integer.valueOf(1));
			listToSave.setParent(null);
			listToSave.setUserId(getLocalIBDBUserId());
			
			try{
				Integer listId = this.dataManager.addGermplasmList(listToSave);
				
				if(listId != null){
					GermplasmList listSaved = this.dataManager.getGermplasmListById(listId);
					currentlySavedList = listSaved;
					this.source.setCurrentlySavedGermplasmList(listSaved);
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
			
			setNeededValuesForNewListEntries(currentlySavedList, listEntries);
			
			if(!saveNewListEntries(listEntries)){
				return;
			}
			
			updateListDataTableContent(currentlySavedList);
		} else if(currentlySavedList != null){
			
			if(areThereChangesToList(currentlySavedList, listToSave)){
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
					
					Integer listId = this.dataManager.updateGermplasmList(listFromDB);
					
					if(listId == null){
						MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
								, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
								, Notification.POSITION_CENTERED);
						return;
					} else{
						currentlySavedList = listFromDB;
					}
				} catch(MiddlewareQueryException ex){
					LOG.error("Error in updating germplasm list: " + currentlySavedList.getId(), ex);
					MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
							, Notification.POSITION_CENTERED);
					return;
				}
			}
			
			boolean thereAreChangesInListEntries = false;
			List<GermplasmListData> newEntries = getNewEntriesToSave(listEntries);
			if(!newEntries.isEmpty()){
				setNeededValuesForNewListEntries(currentlySavedList, newEntries);
				if(!saveNewListEntries(newEntries)){
					return;
				}
				thereAreChangesInListEntries = true;
			}
			
			List<GermplasmListData> entriesToUpdate = getUpdatedEntriesToSave(currentlySavedList, listEntries);
			if(!entriesToUpdate.isEmpty()){
				if(!updateListEntries(entriesToUpdate)){
					return;
				}
				thereAreChangesInListEntries = true;
			}
			
			List<GermplasmListData> entriesToDelete = getEntriesToDelete(currentlySavedList, listEntries);
			if(!entriesToDelete.isEmpty()){
				if(!updateListEntries(entriesToDelete)){
					return;
				}
				thereAreChangesInListEntries = true;
			}
			
			if(thereAreChangesInListEntries){
				updateListDataTableContent(currentlySavedList);
			}
		}
		
		MessageNotifier.showMessage(this.source.getWindow(), messageSource.getMessage(Message.SUCCESS), messageSource.getMessage(Message.LIST_AND_ENTRIES_SAVED_SUCCESS)
				, 3000, Notification.POSITION_CENTERED);
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
		} else {
			if(currentlySavedList == null){
				return validateListName(list);
			}
		}
		return true;
	}
	
	private boolean validateListName(GermplasmList list){
		try{
			List<GermplasmList> lists = this.dataManager.getGermplasmListByName(list.getName(), 0, 5, Operation.EQUAL, Database.LOCAL);
			if(!lists.isEmpty()){
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
						, messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE)
						, Notification.POSITION_CENTERED);
				return false;
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
			
			this.listDataTable.setImmediate(true);
			this.listDataTable.removeAllItems();
			
			for(GermplasmListData entry : savedListEntries){
				Item item = this.listDataTable.addItem(entry.getId());
				
				Button gidButton = new Button(String.format("%s", entry.getGid()), new GidLinkButtonClickListener(entry.getGid().toString(), true));
	            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
				
	            item.getItemProperty(BuildNewListComponent.GID).setValue(gidButton);
	            item.getItemProperty(BuildNewListComponent.DESIGNATION).setValue(entry.getDesignation());
	            item.getItemProperty(BuildNewListComponent.ENTRY_CODE).setValue(entry.getEntryCode());
	            item.getItemProperty(BuildNewListComponent.ENTRY_ID).setValue(entry.getEntryId());
	            item.getItemProperty(BuildNewListComponent.PARENTAGE).setValue(entry.getGroupName());
	            item.getItemProperty(BuildNewListComponent.SEED_SOURCE).setValue(entry.getSeedSource());
	            item.getItemProperty(BuildNewListComponent.STATUS).setValue(entry.getStatusString());
			}
			
			this.listDataTable.requestRepaint();
			return;
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES)
					, Notification.POSITION_CENTERED);
			return;
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
			if(entry.getId() > 0){
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
							matchingSavedEntry.setDesignation(entryToCheck.getDesignation());
						}
						
						if(!matchingSavedEntry.getEntryCode().equals(entryToCheck.getEntryCode())){
							thereIsAChange = true;
							matchingSavedEntry.setEntryCode(entryToCheck.getEntryCode());
						}
						
						if(!matchingSavedEntry.getEntryId().equals(entryToCheck.getEntryId())){
							thereIsAChange = true;
							matchingSavedEntry.setEntryId(entryToCheck.getEntryId());
						}
						
						if(!matchingSavedEntry.getGroupName().equals(entryToCheck.getGroupName())){
							thereIsAChange = true;
							matchingSavedEntry.setGroupName(entryToCheck.getGroupName());
						}
						
						if(!matchingSavedEntry.getSeedSource().equals(entryToCheck.getSeedSource())){
							thereIsAChange = true;
							matchingSavedEntry.setSeedSource(entryToCheck.getSeedSource());
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
}

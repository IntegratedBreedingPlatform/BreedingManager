package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class ListCommonActionsUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(ListCommonActionsUtil.class);
	
	// so class cannot be instantiated
	private ListCommonActionsUtil(){
	}
	
	public static void deleteGermplasmList(GermplasmListManager germplasmListManager, 
			GermplasmList germplasmList, WorkbenchDataManager workbenchDataManager, 
			Window window, SimpleResourceBundleMessageSource messageSource, String item) throws MiddlewareQueryException{
		
		germplasmListManager.deleteGermplasmList(germplasmList);
        
        User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                "Deleted a germplasm list.", 
                "Deleted germplasm list with id = "+germplasmList.getId()+" and name = "+germplasmList.getName()+".",
                user,
                new Date());
        workbenchDataManager.addProjectActivity(projAct);
        
        MessageNotifier.showMessage(window,
                messageSource.getMessage(Message.SUCCESS), 
                messageSource.getMessage(Message.SUCCESSFULLY_DELETED_ITEM, item));      

	}
	
	/**
     * Iterates through the whole table, gets selected item GID's, make sure it's sorted as seen on the UI
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getSelectedGidsFromListDataTable(Table table, String GIDItemId){
        List<Integer> itemIds = new ArrayList<Integer>();
        List<Integer> selectedItemIds = new ArrayList<Integer>();
        List<Integer> trueOrderedSelectedGIDs = new ArrayList<Integer>();
        
        selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
        itemIds.addAll((Collection<Integer>) table.getItemIds());
    
        for(Integer itemId: itemIds){
            if(selectedItemIds.contains(itemId)){
                Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDItemId).getValue()).getCaption().toString());
                trueOrderedSelectedGIDs.add(gid);
            }
        }
        
        return trueOrderedSelectedGIDs;
    }
    
    public static GermplasmList overwriteList(
    		GermplasmList listToSave, 
    		GermplasmListManager dataManager,
    		Component source,
    		SimpleResourceBundleMessageSource messageSource,
    		Boolean showMessages) {
    	GermplasmList savedList = null;
    	try{
    		Integer listId = null;
    		GermplasmList listFromDB = dataManager.getGermplasmListById(listToSave.getId());
    		if(listFromDB!=null) {
	    		listFromDB.setName(listToSave.getName());
				listFromDB.setDescription(listToSave.getDescription());
				listFromDB.setDate(listToSave.getDate());
				listFromDB.setType(listToSave.getType());
				listFromDB.setNotes(listToSave.getNotes());
				listFromDB.setParent(listToSave.getParent());
			
				listId = dataManager.updateGermplasmList(listFromDB);
    		}
			
			if(listId == null){
				if(showMessages) {
                    MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
                            , messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
                }
				return null;
			} else{
				savedList = listFromDB;
				
				if(source instanceof ListBuilderComponent) {
					ListBuilderComponent component = (ListBuilderComponent) source;
					component.setCurrentlySavedGermplasmList(listFromDB);
					component.setHasUnsavedChanges(false);
					((ListManagerMain) component.getSource()).getListSelectionComponent().showNodeOnTree(listId);
				} else if(source instanceof ListManagerMain) {
					ListManagerMain component = (ListManagerMain) source;
					component.getListSelectionComponent().
							updateUIForRenamedList(listToSave, listToSave.getName());
					
					component.getListSelectionComponent().showNodeOnTree(listFromDB.getId());
					MessageNotifier.showMessage(source.getWindow(), messageSource.getMessage(Message.SUCCESS), 
							"Changes to list header were saved.", 3000);
				}
				
				
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error in updating germplasm list: " + listToSave.getId(), ex);
			if(showMessages) {
                MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
            }
			return null;
		}
    	return savedList;
    }
    
    public static boolean overwriteListEntries(
    		GermplasmList listToSave,
    		List<GermplasmListData> listEntries,
    		Boolean forceHasChanges,
    		GermplasmListManager germplasmListManager,
    		Component source,
    		SimpleResourceBundleMessageSource messageSource,
    		Boolean showMessages) {
    	
    	if(forceHasChanges) {
    		return replaceListEntries(
    				listToSave,
    				listEntries,
    				germplasmListManager,
    				source,
    				messageSource,
    				showMessages);
    	}
    	
    	List<GermplasmListData> newEntries = new ArrayList<GermplasmListData>();
		List<GermplasmListData> entriesToUpdate = new ArrayList<GermplasmListData>();
		List<GermplasmListData> entriesToDelete = new ArrayList<GermplasmListData>();
		
		getNewEntriesToSaveUpdateDelete(
				listToSave, 
	    		listEntries, 
	    		forceHasChanges,
	    		newEntries,
	    		entriesToUpdate,
	    		entriesToDelete,
	    		germplasmListManager,
	    		source,
	    		messageSource);
		
		return saveListEntries(
				listToSave,
				newEntries,
				entriesToUpdate,
				entriesToDelete,
				germplasmListManager,
				source,
				messageSource,
				showMessages);
    }
    
    private static void getNewEntriesToSaveUpdateDelete(
    		GermplasmList listToSave, 
    		List<GermplasmListData> listEntries, 
    		Boolean forceHasChanges,
    		List<GermplasmListData> newEntries,
    		List<GermplasmListData> entriesToUpdate,
    		List<GermplasmListData> entriesToDelete,
    		GermplasmListManager dataManager,
    		Component source,
    		SimpleResourceBundleMessageSource messageSource){
    	
    	Map<Integer,GermplasmListData> savedListEntriesMap = new HashMap<Integer, GermplasmListData>();
    	try {
			int listDataCount = (int) dataManager.countGermplasmListDataByListId(listToSave.getId());
			List<GermplasmListData> savedListEntries = dataManager.getGermplasmListDataByListId(listToSave.getId(), 0, listDataCount);
			if(savedListEntries!=null) {
				for (GermplasmListData savedEntry : savedListEntries) {
					//check entries to be deleted
					if(!listEntries.contains(savedEntry) || forceHasChanges){
						savedEntry.setStatus(Integer.valueOf(9));
						entriesToDelete.add(savedEntry);
					} else {//add to map for possible update
						savedListEntriesMap.put(savedEntry.getId(), savedEntry);
					}
				}
			}
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES));
		}
		
		
		for(GermplasmListData entry: listEntries){
			if(entry.getId() > 0 && !savedListEntriesMap.isEmpty()){
				GermplasmListData matchingSavedEntry = savedListEntriesMap.get(entry.getId());
				if(matchingSavedEntry != null){
					//check if it will be updated
					boolean thereIsAChange = false;
					if(!matchingSavedEntry.getDesignation().equals(entry.getDesignation())){
						thereIsAChange = true;
						String designation = entry.getDesignation();
						if(designation != null && designation.length() != 0){
							matchingSavedEntry.setDesignation(designation);
						} else{
							matchingSavedEntry.setDesignation("-");
						}
					}
					
					if(!matchingSavedEntry.getEntryCode().equals(entry.getEntryCode())){
						thereIsAChange = true;
						String entryCode = entry.getEntryCode();
						if(entryCode != null && entryCode.length() != 0){
							matchingSavedEntry.setEntryCode(entryCode);
						} else{
							matchingSavedEntry.setEntryCode(entry.getEntryId().toString());
						}
					}
					
					if(!matchingSavedEntry.getEntryId().equals(entry.getEntryId())){
						thereIsAChange = true;
						matchingSavedEntry.setEntryId(entry.getEntryId());
					}
					
					if(!matchingSavedEntry.getGroupName().equals(entry.getGroupName())){
						thereIsAChange = true;
						String groupName = entry.getGroupName();
						if(groupName != null && groupName.length() != 0){
							if(groupName.length() > 255){
								groupName = groupName.substring(0, 255);
							}
							matchingSavedEntry.setGroupName(groupName);
						} else{
							matchingSavedEntry.setGroupName("-");
						}
					}
					
					if(!matchingSavedEntry.getSeedSource().equals(entry.getSeedSource())){
						thereIsAChange = true;
						String seedSource = entry.getSeedSource();
						if(seedSource != null && seedSource.length() != 0){
							matchingSavedEntry.setSeedSource(seedSource);
						} else{
							matchingSavedEntry.setSeedSource("-");
						}
					}
					
					if(thereIsAChange){
						entriesToUpdate.add(matchingSavedEntry);
					}
				}
			}
		}
		
		
	}

    private static void copyFieldsToNewListEntry(GermplasmListData destination,
			GermplasmListData origin, GermplasmList listToSave) {
    	if(destination!=null && origin!=null) {
    		destination.setDesignation(origin.getDesignation());
    		destination.setEntryCode(origin.getEntryCode());
    		destination.setEntryId(origin.getEntryId());
    		destination.setGid(origin.getGid());
    		destination.setGroupName(origin.getGroupName());
    		destination.setSeedSource(origin.getSeedSource());
    		destination.setList(listToSave);
    		destination.setStatus(Integer.valueOf(0));
    		destination.setLocalRecordId(Integer.valueOf(0));
    	}
	}

	private static boolean saveListEntries(
    		GermplasmList listToSave,
    		List<GermplasmListData> newEntries,
    		List<GermplasmListData> entriesToUpdate,
    		List<GermplasmListData> entriesToDelete,
    		GermplasmListManager dataManager,
    		Component source,
    		SimpleResourceBundleMessageSource messageSource,
    		Boolean showMessages){
    	boolean hasError = false;
    	
    	if(!newEntries.isEmpty()) {
    		try{
    			List<Integer> savedEntryPKs = dataManager.addGermplasmListData(newEntries);
				if(!(savedEntryPKs.size() == newEntries.size())){
					hasError = true;
				}			
			} catch(MiddlewareQueryException ex){
				LOG.error("Error in saving germplasm list entries.", ex);
				hasError = true;
			}
    	}
    	if(!hasError && !entriesToUpdate.isEmpty()) {
			try {
				List<Integer> updatedEntryPKs = dataManager.updateGermplasmListData(entriesToUpdate);
				if(!(updatedEntryPKs.size() == entriesToUpdate.size())){
					hasError = true;
				}
			} catch (MiddlewareQueryException ex) {
				LOG.error("Error in updating germplasm list entries.", ex);
			}
    	}
    	if(!hasError && !entriesToDelete.isEmpty()) {
			try {
				List<Integer> deletedEntryPKs = dataManager.updateGermplasmListData(entriesToDelete);
				if(!(deletedEntryPKs.size() == entriesToDelete.size())){
					hasError = true;
				}
			} catch (MiddlewareQueryException ex) {
				LOG.error("Error in deleting germplasm list entries.", ex);
			}
    	}
		if(hasError) {
			if(showMessages) {
				MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
			}
			return false;
		}
		return true;
	}
    
    private static boolean replaceListEntries(
    		GermplasmList listToSave,
    		List<GermplasmListData> listEntries,
    		GermplasmListManager dataManager,
    		Component source,
    		SimpleResourceBundleMessageSource messageSource,
    		Boolean showMessages){
    	
    	boolean hasError = false;
    	
		try {
			dataManager.deleteGermplasmListDataByListId(listToSave.getId());
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error in deleting germplasm list entries.", ex);
			hasError = true;
		}
		
    	if(!hasError && !listEntries.isEmpty()) {
    		try{
    			List<GermplasmListData> newEntries = new ArrayList<GermplasmListData>();
    			for(GermplasmListData entry: listEntries){
    				GermplasmListData listEntry = new GermplasmListData();
    				copyFieldsToNewListEntry(listEntry,entry,listToSave);
    				newEntries.add(listEntry);
    			}
    			List<Integer> savedEntryPKs = dataManager.addGermplasmListData(newEntries);
				if(!(savedEntryPKs.size() == newEntries.size())){
					hasError = true;
				}			
			} catch(MiddlewareQueryException ex){
				LOG.error("Error in saving germplasm list entries.", ex);
				hasError = true;
			}
    	}
    	
		if(hasError) {
			if(showMessages) {
				MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
			}
			return false;
		}
		return true;
	}
    
}

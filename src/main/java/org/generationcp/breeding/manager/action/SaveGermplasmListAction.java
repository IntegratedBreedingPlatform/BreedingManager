package org.generationcp.breeding.manager.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SaveGermplasmListAction implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Autowired
    private GermplasmListManager germplasmListManager;
	
    @Autowired
    private GermplasmDataManager germplasmManager;

    
	public static final String LIST_DATA_SOURCE = "Crossing Manager Tool";
    public static final Integer LIST_DATA_STATUS = 1;
    public static final Integer LIST_DATA_LRECID = 0;
    
	private GermplasmList germplasmList;
	private List<GermplasmListEntry> listEntries;
	
	private SaveGermplasmListActionSource source;
	
    public SaveGermplasmListAction(SaveGermplasmListActionSource source, GermplasmList germplasmList, List<GermplasmListEntry> listEntries){
    	this.source = source;
    	this.germplasmList = germplasmList;
    	this.listEntries = listEntries;
    }
	
	public GermplasmList saveRecords() throws MiddlewareQueryException{
		germplasmList = saveGermplasmListRecord(germplasmList);
		saveGermplasmListDataRecords(germplasmList,listEntries);
        
        return germplasmList;
	}
	
	private GermplasmList saveGermplasmListRecord(GermplasmList germplasmList) throws MiddlewareQueryException {
		int newListId = 0;
		
		if(germplasmList.getId() == null){ // add new
			newListId = this.germplasmListManager.addGermplasmList(germplasmList);
		}
		else{ // update
			newListId = this.germplasmListManager.updateGermplasmList(germplasmList);
		}
		
		GermplasmList list = this.germplasmListManager.getGermplasmListById(newListId);
        
        return list;
    }
	
	private void saveGermplasmListDataRecords(GermplasmList list,
			List<GermplasmListEntry> listEntries) throws MiddlewareQueryException {
		
		List<GermplasmListData> currentListDataEntries = new ArrayList<GermplasmListData>();
        
		for (GermplasmListEntry listEntry : listEntries){
			int id = listEntry.getListDataId();
            int gid = listEntry.getGid();
            int entryId = listEntry.getEntryId(); 
            String designation = listEntry.getDesignation();
            String groupName = germplasmManager.getCrossExpansion(gid, 1);
            String seedSource = listEntry.getSeedSource();
            	
            GermplasmListData germplasmListData = buildGermplasmListData(
                list, gid, entryId, designation, groupName, seedSource);
            
            germplasmListData.setId(id);
            currentListDataEntries.add(germplasmListData); // with no ids
        }
		
		List<GermplasmListData> existingListDataEntries = germplasmListManager.getGermplasmListDataByListId(germplasmList.getId(), 0, Integer.MAX_VALUE);
		
		//get all the list to add
		List<GermplasmListData> listToAdd = new ArrayList<GermplasmListData>();
		if(existingListDataEntries.size() > 0){
			listToAdd = getNewEntriesToSave(currentListDataEntries,existingListDataEntries);
		}
		else{
			listToAdd.addAll(currentListDataEntries);
		}
        this.germplasmListManager.addGermplasmListData(listToAdd); // ADD the newly created
        
        //get all the updated entries 
        List<GermplasmListData> listToUpdate = new ArrayList<GermplasmListData>();
        if(existingListDataEntries.size() > 0){
        	listToUpdate = getEntriesToUpdate(currentListDataEntries,existingListDataEntries);
		}
        
        this.germplasmListManager.updateGermplasmListData(listToUpdate); // UPDATE the existing created list
		
        //after saving iterate through the itemIds
        int currentSaveListCount = (int) germplasmListManager.countGermplasmListDataByListId(germplasmList.getId());
        List<GermplasmListData> currentSavedList = germplasmListManager.getGermplasmListDataByListId(germplasmList.getId(), 0, currentSaveListCount);
        if (source != null){
        	source.updateListDataTable(currentSavedList);
        }
        
	}
	
	private List<GermplasmListData> getNewEntriesToSave(List<GermplasmListData> currentListDataEntries, List<GermplasmListData> existingListDataEntries){
		List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		for(GermplasmListData entry: currentListDataEntries){
			if(!existingListDataEntries.contains(entry)){
				entry.setId(null);
				toreturn.add(entry);
			}
		}
		return toreturn;
	}
	
	private List<GermplasmListData> getEntriesToUpdate(List<GermplasmListData> currentListDataEntries, List<GermplasmListData> existingListDataEntries){		
		List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		for(GermplasmListData entry: existingListDataEntries){
			if(currentListDataEntries.contains(entry)){
				toreturn.add(entry);
			}
		}
		return toreturn;
	}
	
	private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId, 
	        String designation, String groupName, String seedSource) {
		
        GermplasmListData germplasmListData = new GermplasmListData();
        germplasmListData.setList(list);
        germplasmListData.setGid(gid);
        germplasmListData.setEntryId(entryId);
        germplasmListData.setEntryCode(String.valueOf(entryId));
        germplasmListData.setSeedSource(seedSource);
        germplasmListData.setDesignation(designation);
        germplasmListData.setStatus(LIST_DATA_STATUS);
        germplasmListData.setGroupName(groupName);
        germplasmListData.setLocalRecordId(LIST_DATA_LRECID);
        
        return germplasmListData;
	}
}

package org.generationcp.breeding.manager.crossingmanager.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SaveGermplasmListAction implements Serializable, InitializingBean {
	
	private static final long serialVersionUID = 1L;

	@Autowired
    private GermplasmListManager germplasmListManager;
    
	public static final String LIST_DATA_SOURCE = "Crossing Manager Tool";
    public static final Integer LIST_DATA_STATUS = 0;
    public static final Integer LIST_DATA_LRECID = 0;
	
	private GermplasmList germplasmList;
	List<GermplasmListEntry> listEntries;
	
    public SaveGermplasmListAction(GermplasmList germplasmList, List<GermplasmListEntry> listEntries){
    	this.germplasmList = germplasmList;
    	this.listEntries = listEntries;
    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public GermplasmList saveRecords() throws MiddlewareQueryException{
		germplasmList = saveGermplasmListRecord(germplasmList);
        saveGermplasmListDataRecords(germplasmList,listEntries);
        
        return germplasmList;
	}
	
	private GermplasmList saveGermplasmListRecord(GermplasmList germplasmList) throws MiddlewareQueryException {
        int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
        GermplasmList list = this.germplasmListManager.getGermplasmListById(newListId);
        
        return list;
    }
	
	private void saveGermplasmListDataRecords(GermplasmList list,
			List<GermplasmListEntry> listEntries) throws MiddlewareQueryException {
			
		List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
        
        for (GermplasmListEntry listEntry : listEntries){
            int gid = listEntry.getGid();
            int entryId = listEntry.getEntryId(); 
            String designation = listEntry.getDesignation();
            String groupName = ""; //TO DO determine group name
            
            GermplasmListData germplasmListData = buildGermplasmListData(
                list, gid, entryId, designation, groupName);
            
            listToSave.add(germplasmListData);
        }
    
        this.germplasmListManager.addGermplasmListData(listToSave);
	}
	
	private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId, 
	        String designation, String groupName) {
		
		String groupNameSplit[]=groupName.split(",");
		String grpName=groupNameSplit[0];
		String seedSource=groupNameSplit[1];
		
        GermplasmListData germplasmListData = new GermplasmListData();
        germplasmListData.setList(list);
        germplasmListData.setGid(gid);
        germplasmListData.setEntryId(entryId);
        germplasmListData.setEntryCode(String.valueOf(entryId));
        germplasmListData.setSeedSource(seedSource);
        germplasmListData.setDesignation(designation);
        germplasmListData.setStatus(LIST_DATA_STATUS);
        germplasmListData.setGroupName(grpName);
        germplasmListData.setLocalRecordId(LIST_DATA_LRECID);
        
        return germplasmListData;
	}
}

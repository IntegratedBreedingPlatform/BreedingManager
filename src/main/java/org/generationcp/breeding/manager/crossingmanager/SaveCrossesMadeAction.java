/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package org.generationcp.breeding.manager.crossingmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;


/**
 * Creates Germplasm, GermplasmList, GermplasmListData records for crosses defined.
 * Adds a ProjectActivity (Workbench) record for the save action.
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class SaveCrossesMadeAction implements Serializable, SaveGermplasmListActionSource {
    
    public static final Integer GERMPLASM_LIST_STATUS = 1;    
    
    public static final Integer GERMPLASM_GNPGS = 2;
    public static final Integer GERMPLASM_GRPLCE = 0;
    public static final Integer GERMPLASM_LGID = 0;
    public static final Integer GERMPLASM_MGID = 0;
    public static final Integer GERMPLASM_REFID = 0;
    
    public static final Integer NAME_REFID = 0;
    
    public static final String LIST_DATA_SOURCE = "Crossing Manager Tool";
    public static final Integer LIST_DATA_STATUS = 0;
    public static final Integer LIST_DATA_LRECID = 0;
    
    public static final String WB_ACTIVITY_NAME = "Created a list of crosses";
    public static final String WB_ACTIVITY_DESCRIPTION = "List cross id = ";
    
    private static final long serialVersionUID = -6273933938066390358L;

    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private GermplasmDataManager germplasmManager;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    private Integer wbUserId;
    private Project project;
    private Integer ibdbUserId;
    
    private GermplasmList germplasmList;
    private List<GermplasmListData> existingListEntries = new ArrayList<GermplasmListData>();
    
    private List<Integer> indicesOfAddedCrosses = new ArrayList<Integer>();
    
    public SaveCrossesMadeAction(GermplasmList germplasmList){
    	this.germplasmList = germplasmList;
    }
    
    public SaveCrossesMadeAction(){
    	super();
    }
    
    /**
     * Saves records in Germplasm, GermplasmList and GermplasmListData,
     * ProjectActivity (Workbench).
     * 
     * @param crossesMade where crosses information is defined
     * @return id of new Germplasm List created
     * @throws MiddlewareQueryException
     */
    public GermplasmList saveRecords(CrossesMade crossesMade)throws MiddlewareQueryException{

        retrieveIbdbUserId();
        updateConstantFields(crossesMade);
        
        List<Integer> germplasmIDs = saveGermplasmsAndNames(crossesMade);
        
//        SaveGermplasmListAction saveListAction = new SaveGermplasmListAction(this, this.germplasmList, this.listEntries);
        
        GermplasmList list = saveGermplasmListRecord(crossesMade);
        saveGermplasmListDataRecords(crossesMade, germplasmIDs, list);
        
        // log project activity in Workbench
        if (germplasmList == null){
        	addWorkbenchProjectActivity(list.getId());
        }

        return list;
    }

	private List<Integer> saveGermplasmsAndNames(CrossesMade crossesMade)
			throws MiddlewareQueryException {
		List<Integer> germplasmIDs = new ArrayList<Integer>();
		
		Map<Germplasm, Name> currentCrossesMap = crossesMade.getCrossesMap();
		Map<Germplasm, Name> crossesToInsert  = new LinkedHashMap<Germplasm, Name>();
		if (this.germplasmList == null){
			crossesToInsert = currentCrossesMap;
		
		//when updating a list, determine which germplasms to insert
		} else {
			GenerateCrossNameAction generateAction = new GenerateCrossNameAction();
			CrossingManagerSetting setting = crossesMade.getSetting();
			int nextNumberInSequence = generateAction.getNextNumberInSequence(setting.getCrossNameSetting());
			
			List<Germplasm> existingGplasms = retrieveGermplasmsOfList();
			int ctr = 0;
			for (Germplasm currentGplasm : currentCrossesMap.keySet()){
				boolean existsAlready = false;
				for (Germplasm existingGplasm : existingGplasms){
					if (haveSameParents(currentGplasm, existingGplasm)){
						existsAlready = true;
						break;
					}
				}
				if (!existsAlready){
					/*
					 * Regenerate name in the case entries are appended in the end and names
					 * were generated for existing listdata records  
					 */
					Name name = currentCrossesMap.get(currentGplasm);
					if (setting != null){
						name.setNval(generateAction.buildNextNameInSequence(nextNumberInSequence++));
					}
					
					crossesToInsert.put(currentGplasm, name);
					indicesOfAddedCrosses.add(ctr); // keep track of index of new crosses
				} 
				ctr++;
			}
		}
		
		if (crossesToInsert.size() > 0){
			germplasmIDs = this.germplasmManager.addGermplasm(crossesToInsert);
		}
		return germplasmIDs;
	}
	
	
	private List<Germplasm> retrieveGermplasmsOfList()
			throws MiddlewareQueryException {
		this.existingListEntries = this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId(), 0, Integer.MAX_VALUE);
		List<Integer> gids = new ArrayList<Integer>();
		for (GermplasmListData entry : existingListEntries){
			gids.add(entry.getGid());
		}
		List<Germplasm> existingGplasms = this.germplasmManager.getGermplasms(gids);
		return existingGplasms;
	}
	
	private boolean haveSameParents(Germplasm g1, Germplasm g2){
		return g1.getGpid1().equals(g2.getGpid1()) &&  g1.getGpid2().equals(g2.getGpid2());
	}

    private void retrieveIbdbUserId() throws MiddlewareQueryException {
        this.wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
        this.project = workbenchDataManager.getLastOpenedProject(wbUserId);
        this.ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, this.project.getProjectId());
    }

    /*
     * Adds a ProjectActivity record in Workbench for creating the GermplasmList through 
     * Crossing Manager tool
     * 
     * @param listId if of GermplasmList created
     * @throws MiddlewareQueryException
     */
    private void addWorkbenchProjectActivity(Integer listId) throws MiddlewareQueryException{
        User user = workbenchDataManager.getUserById(this.wbUserId);
        ProjectActivity activity = new ProjectActivity(project.getProjectId().intValue(), project, 
            WB_ACTIVITY_NAME, WB_ACTIVITY_DESCRIPTION + listId, user, new Date());
        
        workbenchDataManager.addProjectActivity(activity);
    }

    
    private GermplasmList saveGermplasmListRecord(CrossesMade crossesMade) throws MiddlewareQueryException {
    	int listId;
    	if (this.germplasmList == null){
    		listId = this.germplasmListManager.addGermplasmList(crossesMade.getGermplasmList());
    	} else {
    		listId = this.germplasmListManager.updateGermplasmList(crossesMade.getGermplasmList());
    	}
        GermplasmList list = this.germplasmListManager.getGermplasmListById(listId);
        
        return list;
    }

    
    private void saveGermplasmListDataRecords(CrossesMade crossesMade,
        List<Integer> germplasmIDs, GermplasmList list) throws MiddlewareQueryException {
    
        Iterator<Integer> germplasmIdIterator = germplasmIDs.iterator();
        List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
        int ctr = 0;
        
        int entryId = existingListEntries.size() + 1;
        for (Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()){
        	if (germplasmList == null || indicesOfAddedCrosses.contains(ctr)){
        		Integer gid = germplasmIdIterator.next();
        		String designation = entry.getValue().getNval();
        		String groupName = getFemaleMaleCrossName(crossesMade, designation, ctr);
        		
        		GermplasmListData germplasmListData = buildGermplasmListData(
        				list, gid, entryId, designation, groupName);
        		
        		listToSave.add(germplasmListData);
        		entryId++;
        	}
        	ctr++;
        }
        
        this.germplasmListManager.addGermplasmListData(listToSave);
    }

    
    /*
     * If current names were generated using prefix in tool, retrieve the 
     * <female parent>/<male parent> cross names from oldCrossNames in CrossesMade
     */
    private String getFemaleMaleCrossName(CrossesMade crossesMade, String designation, Integer ctr) {
    
        List<GermplasmListEntry>  oldCrossNames = crossesMade.getOldCrossNames();
        if (oldCrossNames != null){
            return oldCrossNames.get(ctr).getDesignation();
        }
        return designation;
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
    
        
    private void updateConstantFields(CrossesMade crossesMade){    
        for (Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()){
            Germplasm g = entry.getKey();
            g.setGnpgs(GERMPLASM_GNPGS);
            g.setGrplce(GERMPLASM_GRPLCE);
            g.setLgid(GERMPLASM_LGID);
            g.setMgid(GERMPLASM_MGID);
            g.setUserId(this.ibdbUserId);
            g.setReferenceId(GERMPLASM_REFID);
            
            Name n = entry.getValue();
            n.setReferenceId(NAME_REFID);
            n.setUserId(this.ibdbUserId);
        }
    
        GermplasmList list = crossesMade.getGermplasmList();
        if (list != null){
            list.setStatus(GERMPLASM_LIST_STATUS);
            list.setUserId(this.ibdbUserId);
        }
    
    }

	@Override
	public void updateListDataTable(List<GermplasmListData> listDataEntries) {
		// TODO Auto-generated method stub
		
	}

}

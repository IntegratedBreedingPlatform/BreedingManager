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
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
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
import org.springframework.beans.factory.InitializingBean;
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
public class SaveCrossesMadeAction implements Serializable, InitializingBean {
    
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
    
    public static final String WB_ACTIVITY_NAME = "Create a list of crosses";
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
    
    
    public SaveCrossesMadeAction(){
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * Saves records in Germplasm, GermplasmList and GermplasmListData,
     * ProjectActivity (Workbench).
     * 
     * @param crossesMade where crosses information is defined
     * @return id of new Germplasm List created
     * @throws MiddlewareQueryException
     */
    public Integer saveRecords(CrossesMade crossesMade)throws MiddlewareQueryException{

    	retrieveIbdbUserId();
		updateConstantFields(crossesMade);
		
		// save the IBDB records
		List<Integer> germplasmIds = this.germplasmManager.addGermplasm(crossesMade.getCrossesMap());
		GermplasmList list = saveGermplasmListRecord(crossesMade);
		saveGermplasmListDataRecords(crossesMade, germplasmIds, list);
		
		// log project activity in Workbench
		addWorkbenchProjectActivity(list.getId());

		return list.getId();
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
		int newListId = this.germplasmListManager.addGermplasmList(crossesMade.getGermplasmList());
		GermplasmList list = this.germplasmListManager.getGermplasmListById(newListId);
		
		return list;
    }

    
    private void saveGermplasmListDataRecords(CrossesMade crossesMade,
	    List<Integer> germplasmIds, GermplasmList list) throws MiddlewareQueryException {
	
		Iterator<Integer> germplasmIdIterator = germplasmIds.iterator();
		List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
		int ctr = 1;
		
		for (Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()){
		    Integer gid = germplasmIdIterator.next();
		    int entryId = ctr++;
		    
		    Germplasm germplasm = entry.getKey();
		    String designation = entry.getValue().getNval();
		    
		    String groupName = getFemaleMaleCrossName(crossesMade, germplasm,
			    designation, entryId-1);
		    
		    GermplasmListData germplasmListData = buildGermplasmListData(
			    list, gid, entryId, designation, groupName);
		    
		    listToSave.add(germplasmListData);
		}
	
		this.germplasmListManager.addGermplasmListData(listToSave);
    }

    
    /*
     * If current names were generated using prefix in tool, retrieve the 
     * <female parent>/<male parent> cross names from oldCrossNames in CrossesMade
     */
    private String getFemaleMaleCrossName(CrossesMade crossesMade,
	    Germplasm germplasm, String designation, Integer ctr) {
	
    	List<GermplasmListEntry>  oldCrossNames = crossesMade.getOldCrossNames();
		if (oldCrossNames != null){
		    return oldCrossNames.get(ctr).getDesignation();
		}
		return designation;
    }

    
    private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId, 
	    String designation, String groupName) {
	
		GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setList(list);
		germplasmListData.setGid(gid);
		germplasmListData.setEntryId(entryId);
		germplasmListData.setEntryCode(String.valueOf(entryId));
		germplasmListData.setSeedSource(LIST_DATA_SOURCE);
		germplasmListData.setDesignation(designation);
		germplasmListData.setStatus(LIST_DATA_STATUS);
		germplasmListData.setGroupName(groupName);
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

}

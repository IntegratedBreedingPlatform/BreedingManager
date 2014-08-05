package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.*;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Efficio.Daniel
 * Date: 8/20/13
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Configurable
public class SaveGermplasmListAction  implements Serializable, InitializingBean {


    public static final String WB_ACTIVITY_NAME = "Imported a Germplasm List";
    public static final String WB_ACTIVITY_DESCRIPTION = "Imported list from file ";
    public static final Integer LIST_DATA_STATUS = 0;
    public static final Integer LIST_DATA_LRECID = 0;

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


    public SaveGermplasmListAction(){
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
    public Integer saveRecords(GermplasmList germplasmList, List<GermplasmName> germplasmNameObjects, List<Name> newNames, String filename
    		, List<Integer> doNotCreateGermplasmsWithId, List<ImportedGermplasm> importedGermplasms)throws MiddlewareQueryException{

        retrieveIbdbUserId();
        germplasmList.setUserId(ibdbUserId);

        List<Integer> germplasmIds = new ArrayList<Integer>();
        Map<Integer, GermplasmName> addedGermplasmNameMap = new HashMap<Integer, GermplasmName>();
        
        try {
            for(GermplasmName germplasmName : germplasmNameObjects){
                Name name = germplasmName.getName();
                name.setNid(null);
                name.setNstat(Integer.valueOf(1));
                
                if(doNotCreateGermplasmsWithId.contains(germplasmName.getGermplasm().getGid()) || germplasmIds.contains(germplasmName.getGermplasm().getGid())){
                    //If do not create new germplasm
                	germplasmName.setGermplasm(germplasmManager.getGermplasmByGID(germplasmName.getGermplasm().getGid()));
                    germplasmIds.add(germplasmName.getGermplasm().getGid());
                    name.setGermplasmId(germplasmName.getGermplasm().getGid());
                } else {
                	Germplasm addedGermplasmMatch = getAlreadyAddedGermplasm(germplasmName, addedGermplasmNameMap);
                	
            		germplasmName.getGermplasm().setLgid(Integer.valueOf(0));
            		
                	//not yet added, create new germplasm record
                	if(addedGermplasmMatch==null){
                		germplasmName.getGermplasm().setGid(null);
                        germplasmIds.add(germplasmManager.addGermplasm(germplasmName.getGermplasm(), name));
                		addedGermplasmNameMap.put(germplasmName.getGermplasm().getGid(), germplasmName);
                	//if already addded (re-use that one)
                	} else {
                		germplasmName.setGermplasm(addedGermplasmMatch);
                	}
                	
                	
                }
            }
        } catch (Exception e) {
        }
        
        GermplasmList list = saveGermplasmListRecord(germplasmList);
        saveGermplasmListDataRecords(germplasmNameObjects, germplasmIds, list, filename, importedGermplasms);
        addNewNamesToExistingGermplasm(newNames);
        
        // log project activity in Workbench
        addWorkbenchProjectActivity(filename);

        return list.getId();
    }

	protected void addNewNamesToExistingGermplasm(List<Name> newNames)
			throws MiddlewareQueryException {
		for (Name name: newNames){
        	germplasmManager.addGermplasmName(name);
        }
	}

    private Germplasm getAlreadyAddedGermplasm(GermplasmName germplasmName, Map<Integer, GermplasmName> addedGermplasmNameMap){
    	Germplasm germplasm = germplasmName.getGermplasm();
    	for(Integer gid : addedGermplasmNameMap.keySet()){
    		if(addedGermplasmNameMap.get(gid).getGermplasm().getGpid1().equals(germplasm.getGpid1()) 
        			&& addedGermplasmNameMap.get(gid).getGermplasm().getGpid2().equals(germplasm.getGpid2())
        			&& addedGermplasmNameMap.get(gid).getName().getNval().equals(germplasmName.getName().getNval())
    		  ){
    			return addedGermplasmNameMap.get(gid).getGermplasm();
    		}
    	}
    	return null;
    }
    
    private GermplasmList saveGermplasmListRecord(GermplasmList germplasmList) throws MiddlewareQueryException {
        int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
        GermplasmList list = this.germplasmListManager.getGermplasmListById(newListId);

        return list;
    }


    private void saveGermplasmListDataRecords( List<GermplasmName> germplasmNameObjects,
        List<Integer> germplasmIds, GermplasmList list, String filename, List<ImportedGermplasm> importedGermplasms) throws MiddlewareQueryException {

        List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
        int ctr = 1;

        for (GermplasmName germplasmName : germplasmNameObjects){
        	
            int entryId = ctr++;
            ImportedGermplasm importedGermplasm = importedGermplasms.get(entryId - 1);
            Integer gid = germplasmName.getGermplasm().getGid(); //germplasmIdIterator.next();
            
            String designation = germplasmName.getName().getNval();
            
            String source = filename + ":" +entryId;
            if(importedGermplasm.getSource() != null && importedGermplasm.getSource().length() > 0){
            	source = importedGermplasm.getSource();
            }

            String groupName = "-";
            if(importedGermplasm.getCross() != null && importedGermplasm.getCross().length() > 0){
            	groupName = importedGermplasm.getCross();
            }
            
            String entryCode = String.valueOf(entryId);
            if(importedGermplasm.getEntryCode() != null && importedGermplasm.getEntryCode().length() > 0){
            	entryCode = importedGermplasm.getEntryCode();
            }

            GermplasmListData germplasmListData = buildGermplasmListData(
                list, gid, entryId, designation, groupName, source, entryCode);

            listToSave.add(germplasmListData);
        }

        this.germplasmListManager.addGermplasmListData(listToSave);
    }
    private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId,
            String designation, String groupName, String source, String entryCode) {

            GermplasmListData germplasmListData = new GermplasmListData();
            germplasmListData.setList(list);
            germplasmListData.setGid(gid);
            germplasmListData.setEntryId(entryId);
            germplasmListData.setEntryCode(entryCode);
            germplasmListData.setSeedSource(source);
            germplasmListData.setDesignation(designation);
            germplasmListData.setStatus(LIST_DATA_STATUS);
            germplasmListData.setGroupName(groupName);
            germplasmListData.setLocalRecordId(entryId);


            return germplasmListData;
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
    private void addWorkbenchProjectActivity(String filename) throws MiddlewareQueryException{
        User user = workbenchDataManager.getUserById(this.wbUserId);
        ProjectActivity activity = new ProjectActivity(project.getProjectId().intValue(), project,
            WB_ACTIVITY_NAME, WB_ACTIVITY_DESCRIPTION + filename, user, new Date());

        workbenchDataManager.addProjectActivity(activity);
    }
}

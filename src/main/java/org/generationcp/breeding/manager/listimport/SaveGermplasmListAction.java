package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
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
    public Integer saveRecords(GermplasmList germplasmList, List<GermplasmName> germplasmNameObjects, String filename, List<Integer> doNotCreateGermplasmsWithId)throws MiddlewareQueryException{

        retrieveIbdbUserId();
        germplasmList.setUserId(ibdbUserId);

        //-- ORIG --
        // save the IBDB records
        //List<Integer> germplasmIds = this.germplasmManager.addGermplasm(germplasmMap);
        //-- END --
        
        
        //
        List<Integer> germplasmIds = new ArrayList<Integer>();
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
                    germplasmManager.addGermplasmName(name);
                } else {
                    //Create new germplasm
                    //Integer negativeId = germplasmManager.getN .getNegativeId("gid");
                	germplasmName.getGermplasm().setGid(null);
                	germplasmName.getGermplasm().setLgid(Integer.valueOf(0));
                    germplasmIds.add(germplasmManager.addGermplasm(germplasmName.getGermplasm(), name));
                }
            }
        } catch (Exception e) {
        }
        
        //GermplasmListData germplasmListData = new GermplasmListData();
        //germplasmList.setListData();

        System.out.println("GIDs saved: "+germplasmIds);
        
        GermplasmList list = saveGermplasmListRecord(germplasmList);
        saveGermplasmListDataRecords(germplasmNameObjects, germplasmIds, list, filename);

        // log project activity in Workbench
        addWorkbenchProjectActivity(filename);

        return list.getId();
    }

    private GermplasmList saveGermplasmListRecord(GermplasmList germplasmList) throws MiddlewareQueryException {
        int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
        GermplasmList list = this.germplasmListManager.getGermplasmListById(newListId);

        return list;
    }


    private void saveGermplasmListDataRecords( List<GermplasmName> germplasmNameObjects,
        List<Integer> germplasmIds, GermplasmList list, String filename) throws MiddlewareQueryException {

        Iterator<Integer> germplasmIdIterator = germplasmIds.iterator();
        List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
        int ctr = 1;

        for (GermplasmName germplasmName : germplasmNameObjects){
        	
            int entryId = ctr++;
            Integer gid = germplasmName.getGermplasm().getGid(); //germplasmIdIterator.next();
            
            Germplasm germplasm = germplasmName.getGermplasm();
            String designation = germplasmName.getName().getNval();
            String source = filename + ":" +entryId;

            String groupName = "";   //for simple file, this is blank

            GermplasmListData germplasmListData = buildGermplasmListData(
                list, gid, entryId, designation, groupName, source);

            listToSave.add(germplasmListData);
        }

        this.germplasmListManager.addGermplasmListData(listToSave);
    }
    private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId,
            String designation, String groupName, String source) {

            GermplasmListData germplasmListData = new GermplasmListData();
            germplasmListData.setList(list);
            germplasmListData.setGid(gid);
            germplasmListData.setEntryId(entryId);
            germplasmListData.setEntryCode(String.valueOf(entryId));
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

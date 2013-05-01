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

package org.generationcp.browser.germplasm;

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class SaveGermplasmListAction.
 */
@Configurable
public class SaveGermplasmListAction implements Serializable, InitializingBean{

    private static final long    serialVersionUID = 1L;

    @Autowired
    private GermplasmListManager germplasmListManager;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    /**
     * Instantiates a new SaveGermplasmListAction.
     */
    public SaveGermplasmListAction() {

    }

    /**
     * Adds the germplasm list name and data.
     * 
     * @param listName
     *            the list name
     * @param tabSheet
     *            the tab sheet
     * @param type
     * @param description
     * @throws MiddlewareQueryException
     *             the query exception
     */
    @SuppressWarnings("unused")
    public void addGermplasListNameAndData(String listName, String listId,
            TabSheet tabSheet, String description, String type)
            throws InternationalizableException {

        try {
            // SaveGermplasmListAction saveGermplasmAction = new
            // SaveGermplasmListAction();
            Date date = new Date();
            Format formatter = new SimpleDateFormat("yyyyMMdd");
            Long currentDate = Long.valueOf(formatter.format(date));
            Integer userId = getCurrentUserLocalId();
            GermplasmList parent = null;
            int statusListName = 1;
            String GIDListString = "";

            if (listId == "null") {
                GermplasmList listNameData = new GermplasmList(null, listName,
                        currentDate, type, userId, description, parent,
                        statusListName);

                int listid = germplasmListManager
                        .addGermplasmList(listNameData);

                GermplasmList germList = germplasmListManager
                        .getGermplasmListById(listid);
                String entryCode = "-";
                String seedSource = "-";
                String groupName = "-";
                String designation = "-";
                int status = 0;
                int localRecordId = 0;
                int entryid = 1;

                for (int i = 0; i < tabSheet.getComponentCount(); i++) {
                    Tab currentTab = tabSheet.getTab(i);
                    int gid = Integer.valueOf(currentTab.getCaption()
                            .toString());
                    
                    // save germplasm's preferred name as designation
                    designation = getDesignationFromTab(currentTab);
                    
                    GermplasmListData germplasmListData = new GermplasmListData(
                            null, germList, gid, entryid, entryCode,
                            seedSource, designation, groupName, status,
                            localRecordId);

                    germplasmListManager
                            .addGermplasmListData(germplasmListData);
                    entryid++;

                    GIDListString = GIDListString + ", "
                            + Integer.toString(gid);

                }
            } else {

                GermplasmList germList = germplasmListManager
                        .getGermplasmListById(Integer.valueOf(listId));
                String entryCode = "-";
                String seedSource = "-";
                String groupName = "-";
                String designation = "-";
                int status = 0;
                int localRecordId = 0;
                int entryid = (int) germplasmListManager
                        .countGermplasmListDataByListId(Integer.valueOf(listId));

                for (int i = 0; i < tabSheet.getComponentCount(); i++) {
                    Tab currentTab = tabSheet.getTab(i);
                    int gid = Integer.valueOf(currentTab.getCaption()
                            .toString());

                    // check if there is existing gid in the list
                    List<GermplasmListData> germplasmList = germplasmListManager
                            .getGermplasmListDataByListIdAndGID(
                                    Integer.valueOf(listId), gid);

                    if (germplasmList.size() < 1) {
                        ++entryid;
                        
                        // save germplasm's preferred name as designation
                        designation = getDesignationFromTab(currentTab);
                        
                        GermplasmListData germplasmListData = new GermplasmListData(
                                null, germList, gid, entryid, entryCode,
                                seedSource, designation, groupName, status,
                                localRecordId);

                        germplasmListManager
                                .addGermplasmListData(germplasmListData);

                    }
                    GIDListString = GIDListString + ", "
                            + Integer.toString(gid);
                }

            }

            // Save Project Activity
            GermplasmStudyBrowserApplication app = GermplasmStudyBrowserApplication
                    .get();
            GIDListString = GIDListString.substring(2); // remove ", ";

            User user = (User) workbenchDataManager
                    .getUserById(workbenchDataManager.getWorkbenchRuntimeData()
                            .getUserId());

            ProjectActivity projAct = new ProjectActivity(new Integer(
                    workbenchDataManager
                            .getLastOpenedProject(
                                    workbenchDataManager
                                            .getWorkbenchRuntimeData()
                                            .getUserId()).getProjectId()
                            .intValue()),
                    workbenchDataManager
                            .getLastOpenedProject(workbenchDataManager
                                    .getWorkbenchRuntimeData().getUserId()),
                    "Saved a germplasm list.", "Saved list - " + listName
                            + " with type - " + type, user, new Date());

            try {
                workbenchDataManager.addProjectActivity(projAct);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }

        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_ADDING_GERMPLASM_LIST);
        }
    }

    private Integer getCurrentUserLocalId() throws MiddlewareQueryException {
        Integer workbenchUserId = this.workbenchDataManager
                .getWorkbenchRuntimeData().getUserId();
        Project lastProject = this.workbenchDataManager
                .getLastOpenedProject(workbenchUserId);
        Integer localIbdbUserId = this.workbenchDataManager.getLocalIbdbUserId(workbenchUserId,
                lastProject.getProjectId());
        if (localIbdbUserId != null) {
            return localIbdbUserId;
        } else {
            return 1; // TODO: verify actual default value if no workbench_ibdb_user_map was found
        }
    }
    
    private String getDesignationFromTab(Tab currentTab) {
        // save germplasm's preferred name as designation
        VerticalLayout tabLayout = (VerticalLayout) currentTab.getComponent();
        for (final Iterator<Component> iterator = tabLayout.getComponentIterator(); iterator.hasNext();) {
            Component component = iterator.next();
            //retrieve preferred name from the germplasm details tab
            if (component instanceof GermplasmDetail) {
                GermplasmDetail germplasmDetail = (GermplasmDetail) component;
                return germplasmDetail.getGermplasmDetailModel().getGermplasmPreferredName();
            }
        }
        return "-";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}

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
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.TabSheet;

/**
 * The Class SaveGermplasmListAction.
 */
@Configurable
public class SaveGermplasmListAction implements Serializable, InitializingBean{

    private static final long serialVersionUID = 1L;

    /** The manager factory. */
    @Autowired
    private ManagerFactory managerFactory;

    private GermplasmListManager germplasmListManager;

    /**
     * Instantiates a new SaveGermplasmListAction.
     */
    public SaveGermplasmListAction() {

    }

    /**
     * Adds the germplas list name and data.
     *
     * @param listName the list name
     * @param tabSheet the tab sheet
     * @param statusFinal 
     * @param statusLocked 
     * @param statusHidden 
     * @param type 
     * @param description 
     * @throws QueryException the query exception
     */
    @SuppressWarnings("unused")
    public void addGermplasListNameAndData(String listName, TabSheet tabSheet, String description, String type, String statusHidden, String statusLocked, String statusFinal) throws InternationalizableException {

        try {
            SaveGermplasmListAction saveGermplasmAction = new SaveGermplasmListAction();
            Date date = new Date();
            Format formatter = new SimpleDateFormat("yyyyMMdd");
            Long currentDate = Long.valueOf(formatter.format(date));
            int userId = 1;
            GermplasmList parent = null;
            int statusListName = Integer.valueOf(getStatus(statusHidden,statusLocked,statusFinal));

            GermplasmList listNameData = new GermplasmList(null, listName, currentDate, type, userId, description, parent, statusListName);

            int listid = germplasmListManager.addGermplasmList(listNameData);

            GermplasmList germList = germplasmListManager.getGermplasmListById(listid);
            String entryCode = "-";
            String seedSource = "-";
            String groupName = "-";
            String designation = "-";
            int status = 0;
            int localRecordId = 0;
            int entryid = 1;

            for (int i = 0; i < tabSheet.getComponentCount(); i++) {
                int gid = Integer.valueOf(tabSheet.getTab(i).getCaption().toString());
                GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryCode, seedSource,
                        designation, groupName, status, localRecordId);

                germplasmListManager.addGermplasmListData(germplasmListData);
                entryid++;
            }
        } catch (QueryException e) {
            throw new InternationalizableException(e, Message.error_database, Message.error_in_adding_germplasm_list);
        }
    }

    private String getStatus(String statusHidden, String statusLocked,String statusFinal) {
    	String status="";
    	
    	if(statusHidden.equals("true")){
    		status="1";
    	}else{
    		status="0";
    	}
    	
    	if(statusLocked.equals("true")){
    		status+="1";
    	}else{
    		status+="0";
    	}
    	
    	if(statusFinal.equals("true")){
    		status+="1";
    	}else{
    		status+="0";
    	}
    	
		return status+"1";
	}

	/* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.germplasmListManager = managerFactory.getGermplasmListManager();
    }

}

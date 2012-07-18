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
import java.util.ArrayList;

import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.ManagerFactory;
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
    private ManagerFactory managerFactory;
    
    private GermplasmListManager germplasmListManager;

    public SaveGermplasmListAction() {
        
    }

    public void addGermplasListNameAndData(GermplasmList germplasmList, ArrayList<Integer> listOfGids) throws QueryException{
    	int listid=germplasmListManager.addGermplasmList(germplasmList);
    	
    	GermplasmList germList = germplasmListManager.getGermplasmListById(listid);
    	int entryId=1;
    	String entryCode="-";
    	String seedSource="-";
    	String groupName="-";
    	String designation="-";
    	int status=0;
    	int localRecordId=0;
    	
    	for(Integer gid:listOfGids){
    		GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryId, entryCode, seedSource, designation, groupName, status, localRecordId);
    		germplasmListManager.addGermplasmListData(germplasmListData);
    		entryId++;
    	}
    	
    }
    

	@Override
	public void afterPropertiesSet() throws Exception {

		this.germplasmListManager = managerFactory.getGermplasmListManager();
		
	}

}

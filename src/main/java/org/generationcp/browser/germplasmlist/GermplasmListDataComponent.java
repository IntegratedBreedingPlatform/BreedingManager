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

package org.generationcp.browser.germplasmlist;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class GermplasmListDataComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListDataComponent.class);
    private static final long serialVersionUID = -6487623269938610915L;
    
    private static final String GID = "gid";
    private static final String ENTRY_ID = "entryId";
    private static final String ENTRY_CODE = "entryCode";
    private static final String SEED_SOURCE = "seedSource";
    private static final String DESIGNATION = "designation";
    private static final String GROUP_NAME = "groupName";
    private static final String STATUS = "status";

    private Table listDataTable;
    
    private GermplasmListManager germplasmListManager;
    private int germplasmListId;
    
    private boolean fromUrl;	//this is true if this component is created by accessing the Germplasm List Details page directly from the URL

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmListDataComponent(GermplasmListManager germplasmListManager, int germplasmListId, boolean fromUrl){
    	this.germplasmListManager = germplasmListManager;
    	this.germplasmListId = germplasmListId;
    	this.fromUrl = fromUrl;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception{
        List<GermplasmListData> listData = new ArrayList<GermplasmListData>();
        long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
        if (listDataCount == 0) {
            addComponent(new Label(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL))); // "No Germplasm List Data retrieved."
        } else {
            
            listData = this.germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, (int) listDataCount);
            
            // create the Vaadin Table to display the Germplasm List Data
            listDataTable = new Table("");
            listDataTable.setColumnCollapsingAllowed(true);
            listDataTable.setColumnReorderingAllowed(true);
            listDataTable.setPageLength(15); // number of rows to display in the Table
            listDataTable.setSizeFull(); // to make scrollbars appear on the Table component
            
            //make GID as link only if the page wasn't directly accessed from the URL
            if (!fromUrl) {
        	listDataTable.addContainerProperty(GID, Button.class, null);
            } else {
        	listDataTable.addContainerProperty(GID, Integer.class, null);
            }
            
            listDataTable.addContainerProperty(ENTRY_ID, Integer.class, null);
            listDataTable.addContainerProperty(ENTRY_CODE, String.class, null);
            listDataTable.addContainerProperty(SEED_SOURCE, String.class, null);
            listDataTable.addContainerProperty(DESIGNATION, String.class, null);
            listDataTable.addContainerProperty(GROUP_NAME, String.class, null);
            listDataTable.addContainerProperty(STATUS, String.class, null);
            
            messageSource.setColumnHeader(listDataTable, GID, Message.LISTDATA_GID_HEADER);
            messageSource.setColumnHeader(listDataTable, ENTRY_ID, Message.LISTDATA_ENTRY_ID_HEADER);
            messageSource.setColumnHeader(listDataTable, ENTRY_CODE, Message.LISTDATA_ENTRY_CODE_HEADER);
            messageSource.setColumnHeader(listDataTable, SEED_SOURCE, Message.LISTDATA_SEEDSOURCE_HEADER);
            messageSource.setColumnHeader(listDataTable, DESIGNATION, Message.LISTDATA_DESIGNATION_HEADER);
            messageSource.setColumnHeader(listDataTable, GROUP_NAME, Message.LISTDATA_GROUPNAME_HEADER);
            messageSource.setColumnHeader(listDataTable, STATUS, Message.LISTDATA_STATUS_HEADER);
            
            for (GermplasmListData data : listData) {
        	Object gidObject;
        	
        	if (!fromUrl) {
        	    // make GID as link only if the page wasn't directly accessed from the URL
                    String gid = String.format("%s", data.getGid().toString());
                    Button gidButton = new Button(gid, new GidLinkButtonClickListener(gid));
                    gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                    gidButton.setDescription("Click to view Germplasm information");
                    gidObject = gidButton;
                    //item.addItemProperty(columnId, new ObjectProperty<Button>(gidButton));
        	} else {
        	    gidObject = data.getGid();
        	}
        	
        	listDataTable.addItem(new Object[] {
                        gidObject, data.getEntryId(), data.getEntryCode(), data.getSeedSource(),
                        data.getDesignation(), data.getGroupName(), data.getStatusString()
                }, data.getId());
            }
            
            listDataTable.sort(new Object[]{"entryId"}, new boolean[]{true});
    
            setSpacing(true);
            addComponent(listDataTable);
        }
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    }

}

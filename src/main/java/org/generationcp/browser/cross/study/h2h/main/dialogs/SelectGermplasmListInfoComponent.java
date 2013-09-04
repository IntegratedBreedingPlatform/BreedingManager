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

package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class SelectGermplasmListInfoComponent extends GridLayout implements InitializingBean, InternationalizableComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 3594330437767497353L;
    
    private final static Logger LOG = LoggerFactory.getLogger(SelectGermplasmListInfoComponent.class);
    
    public static final String GID = "gid";
    public static final String ENTRY_ID = "entryId";
    public static final String SEED_SOURCE = "seedSource";
    public static final String DESIGNATION = "designation";
    public static final String GROUP_NAME = "groupName";
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    private Label selectedListLabel;
    private Label selectedListValue;
    private Label descriptionLabel;
    private Label descriptionValue;
    private Label listEntriesLabel;
    private Table listEntryValues;
    private String listName="";
    
    private Integer lastOpenedListId;
    private Integer germplasmListId;
    private Component source;
    
    public SelectGermplasmListInfoComponent(Integer lastOpenedListId, Component source) {
        this.lastOpenedListId = lastOpenedListId;
        this.source = source;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    
    
    public Integer getGermplasmListId() {
		return germplasmListId;
	}

	public void setGermplasmListId(Integer germplasmListId) {
		this.germplasmListId = germplasmListId;
	}

	protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        selectedListLabel = new Label();
        selectedListValue = new Label();
        descriptionLabel = new Label();
        descriptionValue = new Label();
        listEntriesLabel = new Label();
        listEntryValues = createEntryTable();
    }
    
    protected void initializeValues() {
        if (lastOpenedListId != null) {
            try {
                GermplasmList lastOpenedList = this.germplasmListManager.getGermplasmListById(lastOpenedListId);
                displayListInfo(lastOpenedList);
                populateEntryTable(lastOpenedList);
            } catch (MiddlewareQueryException e) {
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                /*
                if (getWindow() != null){
                    MessageNotifier.showWarning(getWindow(), 
                            messageSource.getMessage(Message.ERROR_DATABASE),
                        messageSource.getMessage(Message.ERROR_IN_GETTING_LAST_SELECTED_LIST));
                }
                */
            }
        }
    }
    
    protected void initializeLayout() {
        this.setColumns(2);
        // set column 1 to take up 1/5 (20%) the width of the layout. column 2 gets 4/5 (80%).
        this.setColumnExpandRatio(0, 1);
        this.setColumnExpandRatio(1, 4);
        this.setRows(4);
        this.setRowExpandRatio(0, 1);
        this.setRowExpandRatio(1, 1);
        this.setRowExpandRatio(2, 1);
        this.setRowExpandRatio(3, 35);
        setSpacing(false);
        
        addComponent(selectedListLabel, 0, 0);
        setComponentAlignment(selectedListLabel, Alignment.MIDDLE_LEFT);
        addComponent(selectedListValue, 1, 0);
        setComponentAlignment(selectedListValue, Alignment.MIDDLE_LEFT);
        addComponent(descriptionLabel, 0, 1);
        setComponentAlignment(descriptionLabel, Alignment.MIDDLE_LEFT);
        addComponent(descriptionValue, 1, 1);
        setComponentAlignment(descriptionValue, Alignment.MIDDLE_LEFT);
        addComponent(listEntriesLabel, 0, 2);
        setComponentAlignment(listEntriesLabel, Alignment.MIDDLE_LEFT);
        addComponent(listEntryValues, 0, 3, 1, 3);
    }
    
    protected void initializeActions() {
        
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(selectedListLabel, Message.SELECTED_LIST_LABEL);
        messageSource.setCaption(descriptionLabel, Message.DESCRIPTION_LABEL);
        messageSource.setCaption(listEntriesLabel, Message.LIST_ENTRIES_LABEL);
    }
    
    public void displayListInfo(GermplasmList germplasmList) throws MiddlewareQueryException {
        String listDesc = "";
        if (germplasmList != null) {
            listName = germplasmList.getName();
            listDesc = germplasmList.getDescription();
            
            // assign Germplasm List ID as data for List Entries table, to be retrieved in 
            // SelectGermplasmListWindow.populateParentList() to remember last selected Germplasm List
            listEntryValues.setData(germplasmList.getId());
        }
        selectedListValue.setCaption(listName);
        descriptionValue.setCaption(listDesc);
        
        populateEntryTable(germplasmList);
        
        this.requestRepaint();
    }
    
    private Table createEntryTable() {
        Table listEntryValues = new Table("");
        
        listEntryValues.setPageLength(15); // number of rows to display in the Table
        listEntryValues.setSizeFull(); // to make scrollbars appear on the Table component

        listEntryValues.addContainerProperty(ENTRY_ID, Integer.class, null);
        listEntryValues.addContainerProperty(GID, Integer.class, null);
        listEntryValues.addContainerProperty(DESIGNATION, String.class, null);
        listEntryValues.addContainerProperty(SEED_SOURCE, String.class, null);
        listEntryValues.addContainerProperty(GROUP_NAME, String.class, null);

        messageSource.setColumnHeader(listEntryValues, ENTRY_ID, Message.LISTDATA_ENTRY_ID_HEADER);
        messageSource.setColumnHeader(listEntryValues, GID, Message.LISTDATA_GID_HEADER);
        messageSource.setColumnHeader(listEntryValues, DESIGNATION, Message.LISTDATA_DESIGNATION_HEADER);
        messageSource.setColumnHeader(listEntryValues, SEED_SOURCE, Message.LISTDATA_SEEDSOURCE_HEADER);
        messageSource.setColumnHeader(listEntryValues, GROUP_NAME, Message.LISTDATA_GROUPNAME_HEADER);
        
        return listEntryValues;
    }
    
    private void populateEntryTable(GermplasmList germplasmList) throws MiddlewareQueryException {
        if (listEntryValues.removeAllItems() && germplasmList != null) {
            int germplasmListId = germplasmList.getId();
            long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
            List<GermplasmListData> listDatas = this.germplasmListManager.getGermplasmListDataByListId(
                    germplasmListId, 0, (int) listDataCount);
            for (GermplasmListData data : listDatas) {
                listEntryValues.addItem(new Object[] {
                                data.getEntryId(), data.getGid(), data.getDesignation(),
                                data.getSeedSource(), data.getGroupName()
                }, data.getId());
            }
            listEntryValues.sort(new Object[]{ENTRY_ID}, new boolean[]{true});
            listEntryValues.setVisibleColumns(new String[] {ENTRY_ID,GID,DESIGNATION,SEED_SOURCE,GROUP_NAME});
            
            if(source instanceof SelectGermplasmListDialog){
            	((SelectGermplasmListDialog)source).setDoneButton(true);
            }
            
        }
    }
    
    public Table getEntriesTable() {
        return listEntryValues;
    }
    
    public String getListName() {
        return listName;
    }
    
}

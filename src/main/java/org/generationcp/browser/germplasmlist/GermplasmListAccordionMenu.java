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

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListSelectedTabChangeListener;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmListAccordionMenu extends Accordion implements InitializingBean, InternationalizableComponent {
    
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListAccordionMenu.class);
    private static final long serialVersionUID = -1409312205229461614L;
    
    private static final String LIST_DETAILS = "List Details";
    private static final String LIST_DATA = "List Data";
    private static final String LIST_SEED_INVENTORY = "List Seed Inventory";
    
    private int germplasmListId;
    private String listName;
    private int userId;
    private GermplasmListDetailComponent germplasmListDetailComponent;
    
    private VerticalLayout layoutListData;
    private VerticalLayout layoutListDataInventory;
    
    private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL
   
    private GermplasmStudyBrowserApplication germplasmStudyBrowserApplication;
    private GermplasmListTreeComponent germplasmListTreeComponent;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    private boolean forGermplasmListWindow;
    private GermplasmList germplasmList;
    

    public GermplasmListAccordionMenu(int germplasmListId,String listName,int germplasmListStatus,int userId, boolean fromUrl) {
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.userId=userId;
    }

    public GermplasmListAccordionMenu(GermplasmStudyBrowserApplication germplasmStudyBrowserApplication, int germplasmListId,String listName, int userId, boolean fromUrl,boolean forGermplasmListWindow) {
        this.germplasmStudyBrowserApplication = germplasmStudyBrowserApplication;
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.userId=userId;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }
    
    public GermplasmListAccordionMenu(GermplasmListTreeComponent germplasmListTreeComponent, int germplasmListId,String listName,int germplasmListStatus, int userId, boolean fromUrl,boolean forGermplasmListWindow) {
        this.germplasmListTreeComponent = germplasmListTreeComponent;
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.userId=userId;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }   
    
    public void refreshListData(){
    	try {
            germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
            layoutListData.removeAllComponents();
            layoutListData.addComponent(new GermplasmListDataComponent(germplasmListId,listName,userId,fromUrl,forGermplasmListWindow,germplasmList.getStatus(), this));
            layoutListData.setMargin(true);
            layoutListData.setSpacing(true);
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }
    }
    public void selectedTabChangeAction() throws InternationalizableException{
        Component selected = this.getSelectedTab();
        Tab tab = this.getTab(selected);
        if (tab.getComponent() instanceof VerticalLayout) {
            if (((VerticalLayout) tab.getComponent()).getData().equals(LIST_DATA)) { // "Germplasm List Data"
            try {
            germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }
                
            if (layoutListData.getComponentCount() == 0) {
                    layoutListData.addComponent(new GermplasmListDataComponent(germplasmListId,listName,userId,fromUrl,forGermplasmListWindow,germplasmList.getStatus(), this));
                    layoutListData.setMargin(true);
                    layoutListData.setSpacing(true);
                }
            }else if (((VerticalLayout) tab.getComponent()).getData().equals(LIST_SEED_INVENTORY)) {
                if (layoutListDataInventory.getComponentCount() == 0) {
                    layoutListDataInventory.addComponent(new GermplasmListDataInventoryComponent(germplasmListId));
                    layoutListDataInventory.setMargin(true);
                }
            }
        }
        
        if(!fromUrl){
            if(tab.getCaption().equals(messageSource.getMessage(Message.GERMPLASM_LIST_DETAILS_TAB))){
                Tab tabInfo = Util.getTabAlreadyExist(this.getGermplasmListTreeComponent().getTabSheetGermplasmList(), germplasmList.getName());
                this.getGermplasmListTreeComponent().getTabSheetGermplasmList().removeTab(tabInfo);
    
                try {
                	this.getGermplasmListTreeComponent().createGermplasmListInfoTab(germplasmListId);
                } catch (MiddlewareQueryException e) {
                	e.printStackTrace();
                }
                tab = Util.getTabAlreadyExist(this.getGermplasmListTreeComponent().getTabSheetGermplasmList(), germplasmList.getName());
                this.getGermplasmListTreeComponent().getTabSheetGermplasmList().setSelectedTab(tab.getComponent());
            }
        }
    }
    
    @Override
    public void afterPropertiesSet() {
        this.setSizeFull();
        germplasmListDetailComponent = new GermplasmListDetailComponent(this, germplasmListManager, germplasmListId, fromUrl);
        germplasmListDetailComponent.setData(LIST_DETAILS);

        layoutListData = new VerticalLayout();
        layoutListData.setData(LIST_DATA);
        
        layoutListDataInventory = new VerticalLayout();
        layoutListDataInventory.setData(LIST_SEED_INVENTORY);
        
        this.addTab(germplasmListDetailComponent, messageSource.getMessage(Message.GERMPLASM_LIST_DETAILS_TAB)); // "Germplasm List Details"
        this.addTab(layoutListData, messageSource.getMessage(Message.GERMPLASM_LIST_DATA_TAB)); // "Germplasm List Data"
        this.addTab(layoutListDataInventory, messageSource.getMessage(Message.GERMPLASM_LIST_SEED_INVENTORY_TAB)); // "List Data Inventory"
        
        this.addListener(new GermplasmListSelectedTabChangeListener(this));        
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    }
    
    public GermplasmListDetailComponent getGermplasmListDetailComponent() {
        return germplasmListDetailComponent;
    }

    public GermplasmStudyBrowserApplication getGermplasmStudyBrowserApplication() {
        return germplasmStudyBrowserApplication;
    }

    public GermplasmListTreeComponent getGermplasmListTreeComponent() {
        return germplasmListTreeComponent;
    }
}

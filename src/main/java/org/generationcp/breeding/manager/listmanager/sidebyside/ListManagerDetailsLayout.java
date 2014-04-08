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
package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ListManagerDetailsTabSource;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.util.ListManagerDetailsTabCloseHandler;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.Runo;


/**
 * @author Mark Agarrado
 *
 */
@Configurable
public class ListManagerDetailsLayout extends VerticalLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

    protected static final Logger LOG = LoggerFactory.getLogger(ListManagerDetailsLayout.class);
    private static final long serialVersionUID = -6583178887344009055L;
    
    public static final String CLOSE_ALL_TABS_ID = "ListManagerDetailsLayout Close All Tabs ID";
    public static final String TAB_DESCRIPTION_PREFIX = "List ID: ";
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private ListManagerMain listManagerMain;
    private ListManagerDetailsTabSource detailSource;
    private Label noListLabel;
    private TabSheet detailsTabSheet;
    private Label heading;
    private HorizontalLayout headingBar;
    private Button btnCloseAllTabs;
    private Label defaultLabel;
    
    private Integer listId;
    
    private VerticalLayout innerLayout;
    
    public ListManagerDetailsLayout(ListManagerMain listManagerMain, ListManagerDetailsTabSource detailSource) {
    	super();
        this.listManagerMain = listManagerMain;
        this.detailSource = detailSource;
        this.listId = null;
    }
    
    public ListManagerDetailsLayout(ListManagerMain listManagerMain, ListManagerDetailsTabSource detailSource, Integer listId) {
    	super();
        this.listManagerMain = listManagerMain;
        this.detailSource = detailSource;
        this.listId = listId;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        instantiateComponents();
        initializeValues();
        layoutComponents();
        addListeners();
        
        if(listId != null){
        	try{
        		createListDetailsTab(listId);
        	} catch(MiddlewareQueryException ex){
        		LOG.error("Error with opening list details tab of list with id: " + listId);
        	}
        }
        else{
        	displayDefault();
        }
    }
    
    @Override
    public void instantiateComponents() {
    	detailsTabSheet = new TabSheet();
    	
        noListLabel = new Label();
        noListLabel.setImmediate(true);
        
        btnCloseAllTabs = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
        btnCloseAllTabs.setData(CLOSE_ALL_TABS_ID);
        btnCloseAllTabs.setImmediate(true);
        btnCloseAllTabs.setStyleName(Reindeer.BUTTON_LINK);
        
        headingBar = new HorizontalLayout();
        
        heading = new Label();
        heading.setImmediate(true);
        heading.setWidth("300px");
        heading.setStyleName(Bootstrap.Typography.H3.styleName());
        
        defaultLabel = new Label();
        defaultLabel.setWidth("100%");
    }

    @Override
    public void initializeValues() {
        String headingLabel = "";
        String defaultLabel = "";
        if (detailSource.equals(ListManagerDetailsTabSource.BROWSE)){
            headingLabel = messageSource.getMessage(Message.REVIEW_LIST_DETAILS);
            defaultLabel = messageSource.getMessage(Message.BROWSE_LIST_DEFAULT_MESSAGE);
        } else if (detailSource.equals(ListManagerDetailsTabSource.SEARCH)) {
            headingLabel = messageSource.getMessage(Message.REVIEW_DETAILS);
            defaultLabel = messageSource.getMessage(Message.SEARCH_LIST_DEFAULT_MESSAGE);
        }
        heading.setValue(headingLabel);
        this.defaultLabel.setValue(defaultLabel); 
    }
    
    @Override
    public void layoutComponents() {
    	setWidth("98%");
    	setStyleName(Runo.TABSHEET_SMALL);
        setMargin(false);
    	
    	if(detailSource == ListManagerDetailsTabSource.BROWSE){
    		detailsTabSheet.setHeight("445px");
    	}
    	else if(detailSource == ListManagerDetailsTabSource.SEARCH){
        	detailsTabSheet.setHeight("558px");
    	}
    	 
    	//Components
        headingBar.setWidth("98%");
        headingBar.setHeight("27px");
        
        HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_LIST_DETAILS, heading);
        headingBar.addComponent(headingLayout);
        headingBar.addComponent(btnCloseAllTabs);
        headingBar.setComponentAlignment(btnCloseAllTabs, Alignment.BOTTOM_RIGHT);
        
        VerticalLayout innerLayout = new VerticalLayout();
        innerLayout.addComponent(noListLabel);
        innerLayout.addComponent(headingBar);
        innerLayout.addComponent(defaultLabel);
        innerLayout.addComponent(detailsTabSheet);
        
        addComponent(innerLayout);
        displayDefault();
    }
    
    public void displayDefault(){
    	noListLabel.setVisible(false);        
        headingBar.setVisible(true);
        btnCloseAllTabs.setVisible(false);
        defaultLabel.setVisible(true);
        detailsTabSheet.setVisible(false);
    }

    @Override
    public void addListeners() {
        ListManagerDetailsTabCloseHandler closeHandler = new ListManagerDetailsTabCloseHandler(this);
        btnCloseAllTabs.addListener(closeHandler);
        detailsTabSheet.setCloseHandler(closeHandler);
        detailsTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {

            private static final long serialVersionUID = -7822326039221887888L;

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                if(detailsTabSheet.getComponentCount() <= 1){
                    btnCloseAllTabs.setVisible(false);
                }
                else{
                    btnCloseAllTabs.setVisible(true);
                }
            }
        });
    }

    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub

    }

    public void createGermplasmDetailsTab(Integer gid){
        String tabName = "Germplasm - " + gid;
        createTab(gid, null, tabName);
        showDetailsTabsheet();
    }
    
    public void createListDetailsTab(Integer listId) throws MiddlewareQueryException{
        GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
        if(germplasmList == null){
            hideDetailsTabsheet();
            noListLabel.setCaption("There is no list in the database with id: " + listId);
            noListLabel.setVisible(true);
        } else {
            noListLabel.setVisible(false);
            String tabName = "";            
            if (detailSource.equals(ListManagerDetailsTabSource.BROWSE)) {
                tabName = germplasmList.getName();
            } else if (detailSource.equals(ListManagerDetailsTabSource.SEARCH)) {
                tabName = "List - " + germplasmList.getName();
            }
            createTab(listId, germplasmList, tabName);
            showDetailsTabsheet();
        }
    }
    
    private void createTab(int id, GermplasmList germplasmList, String tabName) {
        boolean tabExists = false;
        //workaround since Browse Lists and Search Lists have different tab name formats
        if (germplasmList != null){
            tabExists = Util.isTabDescriptionExist(detailsTabSheet, generateTabDescription(germplasmList.getId()));
        } else { 
            tabExists = Util.isTabExist(detailsTabSheet, tabName);
        }
        
        if (!tabExists) {
            Component tabContent = createTabContent(id, germplasmList, tabName);
            Tab tab = detailsTabSheet.addTab(tabContent, tabName, null);
            if (germplasmList != null){
                tab.setDescription(generateTabDescription(germplasmList.getId()));
            }
            tab.setClosable(true);
            detailsTabSheet.setSelectedTab(tabContent);
        } else {
            Tab tab;
            if (germplasmList != null) {
                tab = Util.getTabWithDescription(detailsTabSheet, generateTabDescription(germplasmList.getId()));
            } else {
                tab = Util.getTabToFocus(detailsTabSheet, tabName);
            }
            if (tab != null){
                detailsTabSheet.setSelectedTab(tab.getComponent());
            }
        }
    }
    
    private Component createTabContent(int id, GermplasmList germplasmList, String tabName) {
        if (germplasmList != null){
            return new ListDetailsComponent(listManagerMain, this, germplasmList);
        } else {
            return new GermplasmDetailsComponent(listManagerMain, id);
        }
    }
    
    private String generateTabDescription(Integer listId){
        return TAB_DESCRIPTION_PREFIX + listId;
    }
    
    public TabSheet getDetailsTabsheet() {
        return this.detailsTabSheet;
    }
    
    public ListManagerDetailsTabSource getDetailsSource() {
        return this.detailSource;
    }
    
    public void showDetailsTabsheet() {
    	this.removeAllComponents();
    	this.addComponent(headingBar);
    	this.addComponent(detailsTabSheet);
    	
        headingBar.setVisible(true);
        defaultLabel.setVisible(false);
        detailsTabSheet.setVisible(true);
        
    	this.requestRepaint();
    }
    
    public void hideDetailsTabsheet() {
        this.removeAllComponents();
    	this.addComponent(headingBar);
    	this.addComponent(defaultLabel);
    	
        headingBar.setVisible(true);
        btnCloseAllTabs.setVisible(false);
        defaultLabel.setVisible(true);
        detailsTabSheet.setVisible(false);

    	this.requestRepaint();
    }
    
    public void renameTab(Integer listId, String newName){
        String tabDescription = generateTabDescription(listId);
        Tab tab = Util.getTabWithDescription(detailsTabSheet, tabDescription);
        if (tab != null){
            tab.setCaption(newName);
            ListDetailsComponent listDetails = (ListDetailsComponent) tab.getComponent();
            listDetails.setListNameLabel(newName);
        }
    }
    
    public void removeTab(Integer listId){
        String tabDescription = generateTabDescription(listId);
        Tab tab = Util.getTabWithDescription(detailsTabSheet, tabDescription);
        if (tab != null){
            detailsTabSheet.removeTab(tab);
        }
        
        if(detailsTabSheet.getComponentCount() == 0){
            this.hideDetailsTabsheet();
        }
    }
    
    /*public void closeAllTab(String tabName){
    	// TODO method stub
    }*/

}

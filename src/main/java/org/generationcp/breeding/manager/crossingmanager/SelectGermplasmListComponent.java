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

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.Database;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class SelectGermplasmListComponent extends HorizontalLayout implements InitializingBean, InternationalizableComponent  {

    private static final long serialVersionUID = 1L;
    
    private VerticalLayout tabLocalInstance;
    private VerticalLayout tabCentralInstance;
    private TabSheet tabSheetGermplasmListDatabaseInstance;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private SelectGermplasmListTreeComponent localGermplasmListTreeComponent;
    private SelectGermplasmListTreeComponent centralGermplasmListTreeComponent;
    private SelectGermplasmListInfoComponent listInfoComponent;

    private HorizontalLayout mainLayout;
    
    private Integer lastOpenedListId;
    
    public SelectGermplasmListComponent(Integer lastOpenedListId) {
        this.lastOpenedListId = lastOpenedListId;
    }
    
    @Override
    public void afterPropertiesSet() {
        assemble();
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        mainLayout = new HorizontalLayout();
        
        tabSheetGermplasmListDatabaseInstance = new TabSheet();
        tabLocalInstance = new VerticalLayout();
        tabCentralInstance = new VerticalLayout();
        
        tabSheetGermplasmListDatabaseInstance.addTab(tabLocalInstance).setCaption(messageSource.getMessage(Message.DB_LOCAL_TEXT)); // "Local"
        tabSheetGermplasmListDatabaseInstance.addTab(tabCentralInstance).setCaption(messageSource.getMessage(Message.DB_CENTRAL_TEXT)); // "Central"
        tabSheetGermplasmListDatabaseInstance.setSelectedTab(tabLocalInstance);
        
        listInfoComponent = new SelectGermplasmListInfoComponent(lastOpenedListId);
        centralGermplasmListTreeComponent = new SelectGermplasmListTreeComponent(Database.CENTRAL, listInfoComponent);
        localGermplasmListTreeComponent = new SelectGermplasmListTreeComponent(Database.LOCAL, listInfoComponent);
    }
    
    protected void initializeValues() {
        
    }
    
    protected void initializeLayout() {
        this.setSpacing(true);
        this.setMargin(true);
        
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        
        tabSheetGermplasmListDatabaseInstance.setWidth("275px");
        tabSheetGermplasmListDatabaseInstance.setHeight("420px");
        
        listInfoComponent.setWidth("475px");
        listInfoComponent.setHeight("420px");
        
        tabCentralInstance.addComponent(centralGermplasmListTreeComponent);
        tabLocalInstance.addComponent(localGermplasmListTreeComponent);

        mainLayout.addComponent(tabSheetGermplasmListDatabaseInstance);
        mainLayout.addComponent(listInfoComponent);
        mainLayout.setExpandRatio(listInfoComponent, 1.0f);
        
        this.addComponent(mainLayout);
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
        messageSource.setCaption(tabLocalInstance, Message.DB_LOCAL_TEXT);
        messageSource.setCaption(tabCentralInstance, Message.DB_CENTRAL_TEXT);
    }
    
    public SelectGermplasmListInfoComponent getListInfoComponent() {
        return listInfoComponent;
    }
}

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
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.Database;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmListBrowserMain extends VerticalLayout implements InitializingBean, InternationalizableComponent  {

    private static final long serialVersionUID = 1L;
    
    private final static String VERSION = "1.1.3.0";
    
    private VerticalLayout tabLocalInstance;
    private VerticalLayout tabCentralInstance;
    private TabSheet tabSheetGermplasmListDatabaseInstance;
    private GermplasmStudyBrowserApplication germplasmStudyBrowserApplication;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private GermplasmListTreeComponent localGermplasmListTreeComponent;
    private GermplasmListTreeComponent centralGermplasmListTreeComponent;

    private boolean forGermplasmListWindow;
    
    public GermplasmListBrowserMain() {
        
    }
    
    public GermplasmListBrowserMain(GermplasmStudyBrowserApplication germplasmStudyBrowserApplication,boolean forGermplasmListWindow) {
        this.germplasmStudyBrowserApplication = germplasmStudyBrowserApplication;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }
    
    @Override
    public void afterPropertiesSet() {
        this.setSpacing(true);
        this.setMargin(false, true, true, true);
        
        String title =  "<h1>Germplasm List Browser</h1> <h2>: " + VERSION + "</h2>";
        Label applicationTitle = new Label();
        applicationTitle.setStyleName("gcp-window-title");
        applicationTitle.setContentMode(Label.CONTENT_XHTML);
        applicationTitle.setValue(title);
        this.addComponent(applicationTitle);
        
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        
        tabSheetGermplasmListDatabaseInstance = new TabSheet();
        tabSheetGermplasmListDatabaseInstance.setWidth("100%");
        tabSheetGermplasmListDatabaseInstance.setHeight("600px");

        tabLocalInstance = new VerticalLayout();
        tabCentralInstance = new VerticalLayout();

        tabSheetGermplasmListDatabaseInstance.addTab(tabLocalInstance).setCaption(messageSource.getMessage(Message.DB_LOCAL_TEXT)); // "Local"
        tabSheetGermplasmListDatabaseInstance.addTab(tabCentralInstance).setCaption(messageSource.getMessage(Message.DB_CENTRAL_TEXT)); // "Central"
        tabSheetGermplasmListDatabaseInstance.setSelectedTab(tabLocalInstance);
        
        centralGermplasmListTreeComponent = new GermplasmListTreeComponent(mainLayout, Database.CENTRAL,forGermplasmListWindow);
        localGermplasmListTreeComponent = new GermplasmListTreeComponent(this,mainLayout, Database.LOCAL,forGermplasmListWindow);
        
        tabCentralInstance.addComponent(centralGermplasmListTreeComponent);
        tabLocalInstance.addComponent(localGermplasmListTreeComponent);

        mainLayout.addComponent(tabSheetGermplasmListDatabaseInstance);
        mainLayout.setExpandRatio(tabSheetGermplasmListDatabaseInstance, .40f);
        
        this.addComponent(mainLayout);
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
    
    public GermplasmStudyBrowserApplication getGermplasmStudyBrowserApplication() {
        return germplasmStudyBrowserApplication;
    }
    
    public GermplasmListTreeComponent getCentralGermplasmListTreeComponent() {
        return centralGermplasmListTreeComponent;
    }
    
    public GermplasmListTreeComponent getLocalGermplasmListTreeComponent() {
        return localGermplasmListTreeComponent;
    }
}

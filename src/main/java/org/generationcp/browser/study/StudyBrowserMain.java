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

package org.generationcp.browser.study;

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
public class StudyBrowserMain extends VerticalLayout implements InitializingBean, InternationalizableComponent  {

    private static final long serialVersionUID = 1L;
    
    private final static String VERSION = "1.1.2.4";
    
    private VerticalLayout tabLocalInstance;
    private VerticalLayout tabCentralInstance;
    private VerticalLayout tabSearch;
    private TabSheet tabSheetStudyDatabaseInstance;
    
    private boolean forStudyWindow;         //this is true if this component is created for the study browser only window
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudyBrowserMain(boolean forStudyWindow) {
    	this.forStudyWindow = forStudyWindow;
    }
    
    @Override
    public void afterPropertiesSet() {
        this.setSpacing(true);
        this.setMargin(true);
        
        Label applicationTitle = new Label("<h1>Study Browser " + VERSION + "</h1>");
        applicationTitle.setContentMode(Label.CONTENT_XHTML);
        this.addComponent(applicationTitle);
        
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        
        tabSheetStudyDatabaseInstance = new TabSheet();
        tabSheetStudyDatabaseInstance.setWidth("100%");
        tabSheetStudyDatabaseInstance.setHeight("600px");

        tabLocalInstance = new VerticalLayout();
        tabCentralInstance = new VerticalLayout();
        tabSearch = new VerticalLayout();

        tabSheetStudyDatabaseInstance.addTab(tabLocalInstance).setCaption(messageSource.getMessage(Message.DB_LOCAL_TEXT)); // "Local"
        tabSheetStudyDatabaseInstance.addTab(tabCentralInstance).setCaption(messageSource.getMessage(Message.DB_CENTRAL_TEXT)); // "Central"
        tabSheetStudyDatabaseInstance.addTab(tabSearch).setCaption(messageSource.getMessage(Message.SEARCH_LABEL)); // "Search"
        tabSheetStudyDatabaseInstance.setSelectedTab(tabCentralInstance);
        tabCentralInstance.addComponent(new StudyTreeComponent(mainLayout, Database.CENTRAL, forStudyWindow));
        tabLocalInstance.addComponent(new StudyTreeComponent(mainLayout, Database.LOCAL, forStudyWindow));
        tabSearch.addComponent(new StudySearchMainComponent(mainLayout, forStudyWindow));

        mainLayout.addComponent(tabSheetStudyDatabaseInstance);
        mainLayout.setExpandRatio(tabSheetStudyDatabaseInstance, .40f);
        
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
        messageSource.setCaption(tabSearch, Message.SEARCH_LABEL);
    }
    
}

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
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class StudyBrowserMain extends HorizontalLayout implements InitializingBean, InternationalizableComponent  {

    private static final long serialVersionUID = 1L;
    private VerticalLayout tabLocalInstance;
    private VerticalLayout tabCentralInstance;
    private TabSheet tabSheetStudyDatabaseInstance;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudyBrowserMain() {
    	
    }
    
    @Override
    public void afterPropertiesSet() {
        setSizeFull();
        setSpacing(true);
        setMargin(true);
        tabSheetStudyDatabaseInstance = new TabSheet();
        tabSheetStudyDatabaseInstance.setWidth("100%");
        tabSheetStudyDatabaseInstance.setHeight("600px");

        tabLocalInstance = new VerticalLayout();
        tabCentralInstance = new VerticalLayout();

        tabSheetStudyDatabaseInstance.addTab(tabLocalInstance).setCaption(messageSource.getMessage(Message.db_local_text)); // "Local"
        tabSheetStudyDatabaseInstance.addTab(tabCentralInstance).setCaption(messageSource.getMessage(Message.db_central_text)); // "Central"
        tabSheetStudyDatabaseInstance.setSelectedTab(tabCentralInstance);
        tabCentralInstance.addComponent(new StudyTreeComponent(this, Database.CENTRAL));
        tabLocalInstance.addComponent(new StudyTreeComponent(this, Database.LOCAL));

        addComponent(tabSheetStudyDatabaseInstance);
        setExpandRatio(tabSheetStudyDatabaseInstance, .40f);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(tabLocalInstance, Message.db_local_text);
        messageSource.setCaption(tabCentralInstance, Message.db_central_text);
    }
    
}

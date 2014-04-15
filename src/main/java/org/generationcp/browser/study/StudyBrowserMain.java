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
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class StudyBrowserMain extends VerticalLayout implements InitializingBean, InternationalizableComponent  {

    private static final long serialVersionUID = 1L;
    
    private final static String VERSION = "1.2.0";
    
    private VerticalLayout tabStudies;
    private VerticalLayout tabSearch;
    private TabSheet tabSheetStudyDatabaseInstance;
    
    private StudyTreeComponent combinedStudyTree;
    private StudySearchMainComponent studySearchMain;
    
    private StudyBrowserMainLayout mainLayout;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudyBrowserMain() {
    }
    
    @Override
    public void afterPropertiesSet() {
        this.setSpacing(true);
        this.setMargin(false, true, true, true);
        
        String title =  "Study Browser  <h2>" + VERSION + "</h2>";
        Label applicationTitle = new Label();
        applicationTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        applicationTitle.setContentMode(Label.CONTENT_XHTML);
        applicationTitle.setValue(title);
        this.addComponent(applicationTitle);
        
        tabSheetStudyDatabaseInstance = new TabSheet();
        tabSheetStudyDatabaseInstance.setWidth("100%");
        tabSheetStudyDatabaseInstance.setHeight("800px");

        mainLayout = new StudyBrowserMainLayout(this);
        
        tabStudies = new VerticalLayout();
        tabStudies.addStyleName("overflow_x_auto");
        tabStudies.addStyleName("min_width_340px");
        
        combinedStudyTree = new StudyTreeComponent(this);
        tabStudies.addComponent(combinedStudyTree);
        
        tabSearch = new VerticalLayout();
        studySearchMain = new StudySearchMainComponent(this);
        tabSearch.addComponent(studySearchMain);

        tabSheetStudyDatabaseInstance.addTab(tabStudies).setCaption(messageSource.getMessage(Message.STUDIES)); // "Combined Central and Local"
        tabSheetStudyDatabaseInstance.addTab(tabSearch).setCaption(messageSource.getMessage(Message.SEARCH_LABEL)); // "Search"
        tabSheetStudyDatabaseInstance.setSelectedTab(tabStudies);
        
        mainLayout.hideDetailsLayout();
        this.addComponent(mainLayout);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(tabStudies, Message.STUDIES);
        messageSource.setCaption(tabSearch, Message.SEARCH_LABEL);
    }
    
    public void setSelectedTab(VerticalLayout layout){
    	tabSheetStudyDatabaseInstance.setSelectedTab(layout);
    }
    
    public VerticalLayout getTabStudies(){
    	return tabStudies;
    }
    
    public StudyTreeComponent getCombinedStudyTreeComponent(){
    	return combinedStudyTree;
    }
    
    public StudySearchMainComponent getStudySearchComponent(){
        return studySearchMain;
    }
    
    public StudyBrowserMainLayout getMainLayout() {
        return mainLayout;
    }
    
    public TabSheet getTabSheetStudyDatabaseInstance() {
        return tabSheetStudyDatabaseInstance;
    }
}

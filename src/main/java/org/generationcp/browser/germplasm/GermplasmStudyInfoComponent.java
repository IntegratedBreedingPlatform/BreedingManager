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

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.study.listeners.StudyItemClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmStudyInfoComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(GermplasmStudyInfoComponent.class);
    
    private static final String STUDY_ID = "Study ID";
    private static final String STUDY_NAME = "Study Name";
    private static final String DESCRIPTION = "Description";
	public static final String STUDY_BROWSER_LINK = "http://localhost:18080/GermplasmStudyBrowser/main/study-";
    
    private GermplasmIndexContainer dataIndexContainer;
    private GermplasmDetailModel gDetailModel;
    
    private Table studiesTable;
    private Label noDataAvailableLabel;
    private boolean fromUrl;                //this is true if this component is created by accessing the Germplasm Details page directly from the URL
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmStudyInfoComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel, boolean fromUrl) {
        this.dataIndexContainer = dataIndexContainer;
        this.gDetailModel = gDetailModel;
        this.fromUrl = fromUrl;
    }
    
    @Override
    public void afterPropertiesSet() {
        initializeComponents();
        addListeners();
        layoutComponents();
    }
    
    private void initializeComponents(){
    	IndexedContainer studies = dataIndexContainer.getGermplasmStudyInformation(gDetailModel);
    	
    	if(!studies.getItemIds().isEmpty()){
    		studiesTable = new Table();
    		studiesTable.setWidth("90%");
	        studiesTable.setContainerDataSource(studies);
	        if(studies.getItemIds().size() < 10){
	        	studiesTable.setPageLength(studies.getItemIds().size());
	        } else{
	        	studiesTable.setPageLength(10);
	        }
	        studiesTable.setSelectable(true);
	        studiesTable.setMultiSelect(false);
	        studiesTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
	        studiesTable.setColumnReorderingAllowed(true);
	        studiesTable.setColumnCollapsingAllowed(true);
	        studiesTable.setColumnHeaders(new String[] { messageSource.getMessage(Message.STUDY_ID_LABEL)
	        		, messageSource.getMessage(Message.STUDY_NAME_LABEL)
	        		, messageSource.getMessage(Message.DESCRIPTION_LABEL)});
	        studiesTable.setVisibleColumns(new String[] { GermplasmIndexContainer.STUDY_NAME, GermplasmIndexContainer.STUDY_DESCRIPTION});
    	} else{
    		noDataAvailableLabel = new Label("There is no Study Information for this germplasm.");
    	}
    }
    
    private void addListeners(){
    	if (!fromUrl && studiesTable != null) {
            studiesTable.addListener(new StudyItemClickListener(this));
        }
    }
    
    private void layoutComponents(){
    	if(studiesTable != null){
    		addComponent(studiesTable);
    	} else{
    		addComponent(noDataAvailableLabel);
    	}
    }
    
    @Override
    public void updateLabels() {
        
    }
    
    public void studyItemClickAction(ItemClickEvent event, Integer studyId) {
        Window mainWindow = event.getComponent().getWindow();
        
        Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.study_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error("QueryException", qe);
        }
        
        ExternalResource studyBrowserLink = null;
        if (tool == null) {
            studyBrowserLink = new ExternalResource(STUDY_BROWSER_LINK + studyId);
        } else {
            studyBrowserLink = new ExternalResource(tool.getPath().replace("study/", "study-") + studyId);
        }
        
        Window studyWindow = new Window("Study Information - " + studyId);
        
        VerticalLayout layoutForStudy = new VerticalLayout();
        layoutForStudy.setMargin(false);
        layoutForStudy.setWidth("640px");
        layoutForStudy.setHeight("560px");
        
        Embedded studyInfoPage = new Embedded("", studyBrowserLink);
        studyInfoPage.setType(Embedded.TYPE_BROWSER);
        studyInfoPage.setSizeFull();
        layoutForStudy.addComponent(studyInfoPage);
        
        studyWindow.setContent(layoutForStudy);
        studyWindow.setWidth("645px");
        studyWindow.setHeight("600px");
        studyWindow.center();
        studyWindow.setResizable(false);
        studyWindow.setModal(true);
        
        mainWindow.addWindow(studyWindow);
    }

}

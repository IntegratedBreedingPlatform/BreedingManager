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

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.GermplasmStudyBrowserLayout;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.util.StudyBrowserTabCloseHandler;
import org.generationcp.browser.study.containers.StudyDataIndexContainer;
import org.generationcp.browser.study.listeners.StudyItemClickListener;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Joyce Avestro
 * 
 */
@Configurable
public class StudySearchMainComponent extends HorizontalLayout implements InitializingBean, 
					InternationalizableComponent, GermplasmStudyBrowserLayout{

    private static final long serialVersionUID = 1L;

    private final static Logger LOG = LoggerFactory.getLogger(StudySearchMainComponent.class);

    private VerticalLayout searchResultLayout;
    private Label totalEntriesLabel;
    private TabSheet tabSheetStudy;

    private StudySearchInputComponent searchInputComponent;
    private Table searchResultTable;
    
    private StudyBrowserMain studyBrowserMain;
    private StudyBrowserMainLayout studyBrowserMainLayout;

    private StudyDataIndexContainer studyDataIndexContainer;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private StudyDataManager studyDataManager;

    public StudySearchMainComponent(StudyBrowserMain studyBrowserMain) {
        this.studyBrowserMain = studyBrowserMain;
        this.studyBrowserMainLayout = studyBrowserMain.getMainLayout();
    }

    @Override
    public void afterPropertiesSet() throws Exception { 
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
    }
    
	@Override
	public void instantiateComponents() {
		studyDataIndexContainer = new StudyDataIndexContainer(studyDataManager, 0);
        tabSheetStudy = studyBrowserMain.getCombinedStudyTreeComponent().getTabSheetStudy();
        searchInputComponent = new StudySearchInputComponent(this);
        
        totalEntriesLabel = new Label("",Label.CONTENT_XHTML);
        totalEntriesLabel.setWidth("120px");
        updateNoOfEntries(0);
	}
	
	public void updateNoOfEntries(int count){
		totalEntriesLabel.setValue(messageSource.getMessage(Message.SEARCH_RESULT_LABEL) + ": " 
	       		 + "  <b>" + count + "</b>");
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		setMargin(false,false,false,true);
		searchResultLayout = new VerticalLayout();
		searchResultLayout.setSpacing(true);
		searchResultLayout.setWidth("425px");
        searchResultLayout.setVisible(false);

        addComponent(searchInputComponent);
        addComponent(searchResultLayout);
	}

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    }
    
    public void searchStudy(String name, String country, Season season, Integer date){
    	if (searchResultTable != null){
    		searchResultTable.removeAllItems();
    	}
        IndexedContainer dataSourceResult = studyDataIndexContainer.getStudies(name, country, season, date);
        
        if (dataSourceResult.size() == 0){
        	updateNoOfEntries(0);	
            MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.NO_STUDIES_FOUND), ""); 
        } else {
            searchResultTable = new StudySearchResultTable(dataSourceResult).getResultTable();
            updateNoOfEntries(dataSourceResult.size());
            searchResultTable.addListener(new StudyItemClickListener(this));
            
            searchResultLayout.removeAllComponents();
            searchResultLayout.addComponent(totalEntriesLabel);
            searchResultLayout.addComponent(searchResultTable);
            searchResultLayout.setVisible(true);
            requestRepaint();
        }

    }
    
    public void studyItemClickAction(Integer studyId) {
        studyDataIndexContainer = new StudyDataIndexContainer(studyDataManager, studyId);

        try {
            Study study = this.studyDataManager.getStudy(Integer.valueOf(studyId));
            //don't show study details if study record is a Folder ("F")
            String studyType = study.getType();
            if (!hasChildStudy(studyId) && !isFolderType(studyType)){
                createStudyInfoTab(studyId);
            }
        } catch (NumberFormatException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                    messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
        }

    }

    private String getStudyName(int studyId) throws InternationalizableException {
        try {
            Study studyDetails = this.studyDataManager.getStudy(Integer.valueOf(studyId));
            if(studyDetails != null){
                return studyDetails.getName();
            } else {
                return null;
            }
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID);
        }
    }

    private boolean hasChildStudy(int studyId) {

        List<Reference> studyChildren = new ArrayList<Reference>();

        try {
            studyChildren.addAll(this.studyDataManager.getChildrenOfFolder(Integer.valueOf(studyId)));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
            studyChildren = new ArrayList<Reference>();
        }
        if (!studyChildren.isEmpty()) {
            return true;
        }
        return false;
    }

    private void createStudyInfoTab(int studyId) throws InternationalizableException {
        VerticalLayout layout = new VerticalLayout();

        if (!Util.isTabExist(tabSheetStudy, getStudyName(studyId))) {
            layout.addComponent(new StudyAccordionMenu(studyId, new StudyDetailComponent(this.studyDataManager, studyId), studyDataManager, false,false));
            Tab tab = tabSheetStudy.addTab(layout, getStudyName(studyId), null);
            tab.setClosable(true);

            studyBrowserMainLayout.addStudyInfoTabSheet(tabSheetStudy);
            studyBrowserMainLayout.showDetailsLayout();
            tabSheetStudy.setSelectedTab(layout);
            tabSheetStudy.setCloseHandler(new StudyBrowserTabCloseHandler(studyBrowserMainLayout));
        } else {
            Tab tab = Util.getTabAlreadyExist(tabSheetStudy, getStudyName(studyId));
            tabSheetStudy.setSelectedTab(tab.getComponent());
        }
    }

    private boolean isFolderType(String type){
        if(type != null){
            type = type.toLowerCase();
            if(type.equals("f") || type.equals("folder")){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }
}

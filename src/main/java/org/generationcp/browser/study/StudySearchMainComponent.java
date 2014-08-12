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

import org.generationcp.browser.application.GermplasmStudyBrowserLayout;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.util.StudyBrowserTabCloseHandler;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
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

    private StudySearchResultComponent searchResultComponent;
    private StudySearchInputComponent searchInputComponent;

    private StudyBrowserMain studyBrowserMain;
    private StudyBrowserMainLayout studyBrowserMainLayout;
    private TabSheet tabSheetStudy;
    
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
        tabSheetStudy = studyBrowserMain.getCombinedStudyTreeComponent().getTabSheetStudy();
        searchInputComponent = new StudySearchInputComponent(this);
        searchResultComponent = new StudySearchResultComponent(this);
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
        addComponent(searchInputComponent);
        addComponent(searchResultComponent);
	}

	@Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    	
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

    public void createStudyInfoTab(int studyId) throws InternationalizableException {
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

    //SETTERS AND GETTERS
	public StudySearchResultComponent getSearchResultComponent() {
		return searchResultComponent;
	}

	public StudySearchInputComponent getSearchInputComponent() {
		return searchInputComponent;
	}
  
}

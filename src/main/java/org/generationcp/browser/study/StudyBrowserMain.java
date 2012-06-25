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

import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class StudyBrowserMain extends HorizontalLayout{

    private VerticalLayout tabLocalInstance;
    private VerticalLayout tabCentralInstance;
    private TabSheet tabSheetStudyDatabaseInstance;
//    private HorizontalLayout studyBrowserMainLayout;
//    private StudyDataManager studyDataManager;

    public StudyBrowserMain(ManagerFactory factory) {

        setSizeFull();
        setSpacing(true);
        setMargin(true);
        tabSheetStudyDatabaseInstance = new TabSheet();
        tabSheetStudyDatabaseInstance.setWidth("100%");
        tabSheetStudyDatabaseInstance.setHeight("600px");

        tabLocalInstance = new VerticalLayout();
        tabCentralInstance = new VerticalLayout();

        tabSheetStudyDatabaseInstance.addTab(tabLocalInstance).setCaption("Local");
        tabSheetStudyDatabaseInstance.addTab(tabCentralInstance).setCaption("Central");
        tabSheetStudyDatabaseInstance.setSelectedTab(tabCentralInstance);
        tabCentralInstance.addComponent(new StudyTreeComponent(factory, this, Database.CENTRAL));
        tabLocalInstance.addComponent(new StudyTreeComponent(factory, this, Database.LOCAL));

        addComponent(tabSheetStudyDatabaseInstance);
        setExpandRatio(tabSheetStudyDatabaseInstance, .40f);
    }
}

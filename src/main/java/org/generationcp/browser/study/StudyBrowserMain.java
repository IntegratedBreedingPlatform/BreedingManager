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

import org.generationcp.browser.i18n.ui.I18NHorizontalLayout;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class StudyBrowserMain extends I18NHorizontalLayout{

    private VerticalLayout tabLocalInstance;
    private VerticalLayout tabCentralInstance;
    private TabSheet tabSheetStudyDatabaseInstance;

    // private HorizontalLayout studyBrowserMainLayout;
    // private StudyDataManager studyDataManager;

    public StudyBrowserMain(ManagerFactory factory, I18N i18n) {
        super(i18n);

        setSizeFull();
        setSpacing(true);
        setMargin(true);
        tabSheetStudyDatabaseInstance = new TabSheet();
        tabSheetStudyDatabaseInstance.setWidth("100%");
        tabSheetStudyDatabaseInstance.setHeight("600px");

        tabLocalInstance = new VerticalLayout();
        tabCentralInstance = new VerticalLayout();

        tabSheetStudyDatabaseInstance.addTab(tabLocalInstance).setCaption(i18n.getMessage("db.local.text")); // "Local"
        tabSheetStudyDatabaseInstance.addTab(tabCentralInstance).setCaption(i18n.getMessage("db.central.text")); // "Central"
        tabSheetStudyDatabaseInstance.setSelectedTab(tabCentralInstance);
        tabCentralInstance.addComponent(new StudyTreeComponent(factory, this, Database.CENTRAL, i18n));
        tabLocalInstance.addComponent(new StudyTreeComponent(factory, this, Database.LOCAL, i18n));

        addComponent(tabSheetStudyDatabaseInstance);
        setExpandRatio(tabSheetStudyDatabaseInstance, .40f);
    }
}

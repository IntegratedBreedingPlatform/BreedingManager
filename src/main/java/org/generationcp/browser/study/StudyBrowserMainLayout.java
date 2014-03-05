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

import org.generationcp.browser.util.Util;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


/**
 * @author Mark Agarrado
 *
 */
public class StudyBrowserMainLayout extends HorizontalLayout{
    
    private static final long serialVersionUID = -1375083442943045398L;
    
    private StudyBrowserMain studyBrowserMain;
    private TabSheet tabSheetStudyDatabaseInstance;
    private VerticalLayout studyDetailsLayout;
    
    public StudyBrowserMainLayout(StudyBrowserMain studyBrowserMain) {
        this.studyBrowserMain = studyBrowserMain;
        this.tabSheetStudyDatabaseInstance = studyBrowserMain.getTabSheetStudyDatabaseInstance();
        studyDetailsLayout = new VerticalLayout();
        studyDetailsLayout.setVisible(false);
        
        this.addComponent(tabSheetStudyDatabaseInstance);
        this.addComponent(studyDetailsLayout);
        
        this.setSizeFull();
        this.setSpacing(true);
    }
    
    public VerticalLayout getStudyDetailsLayout() {
        return studyDetailsLayout;
    }
    
    public void showDetailsLayout() {
        studyDetailsLayout.setVisible(true);
        this.setExpandRatio(tabSheetStudyDatabaseInstance, 1);
        this.setExpandRatio(studyDetailsLayout, 2);
    }
    
    public void hideDetailsLayout() {
        this.setExpandRatio(tabSheetStudyDatabaseInstance, 1);
        this.setExpandRatio(studyDetailsLayout, 0);
        studyDetailsLayout.setVisible(false);
    }
    
    public void closeAllDetailTabs(){
        Util.closeAllTab(studyBrowserMain.getCombinedStudyTreeComponent().getTabSheetStudy());
        hideDetailsLayout();
    }
    
}

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
package org.generationcp.browser.cross.study.util;

import org.generationcp.browser.study.StudyBrowserMainLayout;
import org.generationcp.browser.util.SelectedTabCloseHandler;
import org.generationcp.browser.util.Util;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;


/**
 * @author Mark Agarrado
 *
 */
public class StudyBrowserTabCloseHandler extends SelectedTabCloseHandler{

    private static final long serialVersionUID = 1765481898070267958L;
    
    private StudyBrowserMainLayout studyBrowserMainLayout;
    
    public StudyBrowserTabCloseHandler(StudyBrowserMainLayout studyBrowserMainLayout){
        this.studyBrowserMainLayout = studyBrowserMainLayout;
    }
    
    @Override
    public void onTabClose(TabSheet tabsheet, Component tabContent) {
        super.onTabClose(tabsheet, tabContent);
        
        if (tabsheet.getComponentCount() == 0) {
            studyBrowserMainLayout.hideDetailsLayout();
        }
    }

}

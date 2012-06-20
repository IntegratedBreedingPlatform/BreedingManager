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

package org.generationcp.browser.study.listeners;

import org.generationcp.browser.study.StudyAccordionMenu;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class StudySelectedTabChangeListener implements TabSheet.SelectedTabChangeListener{

    private static final long serialVersionUID = -1276034489275080024L;

    private Object source;

    public StudySelectedTabChangeListener(Object source) {
        this.source = source;
    }

    @Override
    public void selectedTabChange(SelectedTabChangeEvent event) {

        if (source instanceof StudyAccordionMenu) {
            ((StudyAccordionMenu) source).selectedTabChangeAction();

        }
    }

}

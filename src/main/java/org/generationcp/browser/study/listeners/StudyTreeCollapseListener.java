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

import org.generationcp.browser.study.StudyTreeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;

public class StudyTreeCollapseListener implements Tree.CollapseListener{
    
    private static final long serialVersionUID = -5091664285613837786L;

    private Layout source;

    public StudyTreeCollapseListener(Layout source) {
        this.source = source;
    }

    @Override
    public void nodeCollapse(CollapseEvent event) {
        if (source instanceof StudyTreeComponent) {
            ((StudyTreeComponent) source).getStudyTree().select(event.getItemId());
            ((StudyTreeComponent) source).getStudyTree().setValue(event.getItemId());
            ((StudyTreeComponent) source).updateButtons(event.getItemId());
        }
    }

}

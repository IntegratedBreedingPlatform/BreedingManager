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

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class StudyTreeExpandListener implements Tree.ExpandListener{

    private static final long serialVersionUID = -5091664285613837786L;

    private Layout source;

    public StudyTreeExpandListener(Layout source) {
        this.source = source;
    }

    @Override
    public void nodeExpand(ExpandEvent event) {

        if (source instanceof StudyTreeComponent) {

            ((StudyTreeComponent) source).addStudyNode(Integer.valueOf(event.getItemId().toString()));

        }

    }

}

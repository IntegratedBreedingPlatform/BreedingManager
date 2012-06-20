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

import java.util.ArrayList;

import org.generationcp.browser.study.StudyEffectComponent;
import org.generationcp.browser.study.StudyTreePanel;

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class StudyTreeExpandListener implements Tree.ExpandListener{

    private static final long serialVersionUID = -5091664285613837786L;

    private Layout source;
    private ArrayList<Object> parameters;

    public StudyTreeExpandListener(Layout source) {
        this.source = source;
    }

    public StudyTreeExpandListener(Layout source, ArrayList<Object> parameters) {
        this.source = source;
        this.parameters = parameters;
    }

    @Override
    public void nodeExpand(ExpandEvent event) {

        if (source instanceof StudyEffectComponent) {

            ((StudyEffectComponent) source).addRepAndFactorNodes(event.getItemId().toString());
            ;

        } else if (source instanceof StudyTreePanel) {

            ((StudyTreePanel) source).addStudyNode(Integer.valueOf(event.getItemId().toString()));

        }

    }

}

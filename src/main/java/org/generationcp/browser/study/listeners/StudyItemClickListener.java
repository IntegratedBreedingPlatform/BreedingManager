/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/

package org.generationcp.browser.study.listeners;

import java.util.ArrayList;

import org.generationcp.browser.study.RepresentationDatasetComponent;
import org.generationcp.browser.study.StudyEffectComponent;
import org.generationcp.browser.study.StudyTreePanel;
import org.generationcp.browser.util.Util;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TabSheet.Tab;

public class StudyItemClickListener implements ItemClickEvent.ItemClickListener{
    
    private static final Logger LOG = LoggerFactory.getLogger(StudyItemClickListener.class);
    private static final long serialVersionUID = -5286616518840026212L;

    private Layout source;
    private ArrayList<Object> parameters;

    public StudyItemClickListener(Layout source) {
	this.source = source;
    }

    public StudyItemClickListener(Layout source, ArrayList<Object> parameters) {
	this.source = source;
	this.parameters = parameters;
    }

    @Override
    public void itemClick(ItemClickEvent event) {

	if (source instanceof StudyEffectComponent) {

	    if (event.getButton() == ItemClickEvent.BUTTON_LEFT) {
		((StudyEffectComponent) source).effectTreeItemClickAction(event.getItemId().toString(), (Tree) event.getComponent());
	    }

	} else if (source instanceof StudyTreePanel){
	    int studyId = Integer.valueOf(event.getItemId().toString());
	    if (event.getButton() == ItemClickEvent.BUTTON_LEFT) {
		((StudyTreePanel) source).studyTreeItemClickAction(studyId);
	    }
	    
	}

    }

}

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

import org.generationcp.browser.application.GermplasmBrowserOnlyApplication;
import org.generationcp.browser.application.WelcomeTab;
import org.generationcp.browser.study.RepresentationDatasetComponent;
import org.generationcp.browser.study.StudyBrowserMainApplication;
import org.generationcp.browser.study.StudyTreePanel;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.ui.Button;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;

public class StudyButtonClickListener implements Button.ClickListener{
    
    private static final Logger LOG = LoggerFactory.getLogger(StudyButtonClickListener.class);
    private static final long serialVersionUID = 7921109465618354206L;
    
    private Layout source;
    private ArrayList<Object> parameters;
    
    public StudyButtonClickListener(Layout source){
	this.source = source;
    }

    public StudyButtonClickListener(Layout source, ArrayList<Object> parameters){
	this.source = source;
	this.parameters = parameters;
    }

    @Override
    public void buttonClick(ClickEvent event) {
	
	if (event.getComponent().getCaption().equals("Export to CSV") && (source instanceof RepresentationDatasetComponent)){

	    ((RepresentationDatasetComponent) source).exportToCSVAction();
	    
	} else if ((event.getComponent().getCaption().equals("I want to browse Studies and their Datasets")) && (source instanceof WelcomeTab)) {
	    ((WelcomeTab) source).browseStudiesAndDataSets();

	} else if  (event.getComponent().getCaption().equals("Refresh") && (source instanceof StudyTreePanel)){
	    ((StudyTreePanel) source).createTree();
	}
	
    }


}

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

import org.generationcp.browser.application.WelcomeTab;
import org.generationcp.browser.study.RepresentationDatasetComponent;
import org.generationcp.browser.study.SaveRepresentationDatasetExcelDialog;
import org.generationcp.browser.study.StudyTreeComponent;
import org.generationcp.browser.study.TableViewerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;

public class StudyButtonClickListener implements Button.ClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(StudyButtonClickListener.class);
    private static final long serialVersionUID = 7921109465618354206L;

    private Object source;

    public StudyButtonClickListener(Layout source) {
        this.source = source;
    }

    public StudyButtonClickListener(TableViewerComponent source) {
    	this.source = source;
	}

	@Override
    public void buttonClick(ClickEvent event) {
        
        if (event.getButton().getData().equals(RepresentationDatasetComponent.EXPORT_CSV_BUTTON_ID) // "Export to CSV"
                && (source instanceof RepresentationDatasetComponent)) {
            ((RepresentationDatasetComponent) source).exportToCSVAction();
            
        } else if (event.getButton().getData().equals(RepresentationDatasetComponent.EXPORT_EXCEL_BUTTON_ID) // "Export to Fieldbook Excel File"
                    && (source instanceof RepresentationDatasetComponent)) {
                ((RepresentationDatasetComponent) source).exportToExcelAction();
                
        } else if (event.getButton().getData().equals(TableViewerComponent.EXPORT_EXCEL_BUTTON_ID) // "Export Dataset to Excel"
                && (source instanceof TableViewerComponent)) {
            ((TableViewerComponent) source).exportToExcelAction();
            
        } else  if (event.getButton().getData().equals(SaveRepresentationDatasetExcelDialog.SAVE_EXCEL_BUTTON_ID) // "Save to Fieldbook Excel File"
                && (source instanceof SaveRepresentationDatasetExcelDialog)) {
            ((SaveRepresentationDatasetExcelDialog) source).saveExcelFileButtonClickAction();

        } else  if (event.getButton().getData().equals(SaveRepresentationDatasetExcelDialog.CANCEL_EXCEL_BUTTON_ID) // "Cancel"
                && (source instanceof SaveRepresentationDatasetExcelDialog)) {
            ((SaveRepresentationDatasetExcelDialog) source).cancelSavingExcelFileButtonClickAction();

        } else  if (event.getButton().getData().equals(SaveRepresentationDatasetExcelDialog.BROWSE_FOLDER_BUTTON_ID) 
                && (source instanceof SaveRepresentationDatasetExcelDialog)) {
            ((SaveRepresentationDatasetExcelDialog) source).browseDirectoryButtonClickAction();

        } else if (event.getButton().getData().equals(WelcomeTab.BROWSE_STUDY_BUTTON_ID) // "I want to browse Studies and their Datasets"
                && (source instanceof WelcomeTab)) {
            ((WelcomeTab) source).browseStudiesAndDataSets();

        } else if (event.getButton().getData().equals(StudyTreeComponent.REFRESH_BUTTON_ID) // "Refresh")
                && (source instanceof StudyTreeComponent)) {
            ((StudyTreeComponent) source).createTree();
        } else if (event.getButton().getData().equals(RepresentationDatasetComponent.OPEN_TABLE_VIEWER_BUTTON_ID) // "Open in Table Viewer"
                && (source instanceof RepresentationDatasetComponent)) {
            ((RepresentationDatasetComponent) source).openTableViewerAction();
            
        } else {
            LOG.error("StudyButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

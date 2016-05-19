/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.study.listeners;

import org.generationcp.breeding.manager.study.RepresentationDatasetComponent;
import org.generationcp.breeding.manager.study.SaveRepresentationDatasetExcelDialog;
import org.generationcp.breeding.manager.study.StudyTreeComponent;
import org.generationcp.breeding.manager.study.TableViewerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;

public class StudyButtonClickListener implements Button.ClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(StudyButtonClickListener.class);
	private static final long serialVersionUID = 7921109465618354206L;

	private final Object source;

	public StudyButtonClickListener(Layout source) {
		this.source = source;
	}

	public StudyButtonClickListener(TableViewerComponent source) {
		this.source = source;
	}

	@Override
	public void buttonClick(ClickEvent event) {

		// "Export to CSV"
		if (event.getButton().getData().equals(RepresentationDatasetComponent.EXPORT_CSV_BUTTON_ID)
				&& this.source instanceof RepresentationDatasetComponent) {
			((RepresentationDatasetComponent) this.source).exportToCSVAction();

			// "Export to Fieldbook Excel File"
		} else if (event.getButton().getData().equals(RepresentationDatasetComponent.EXPORT_EXCEL_BUTTON_ID)
				&& this.source instanceof RepresentationDatasetComponent) {
			((RepresentationDatasetComponent) this.source).exportToExcelAction();

			// "Export Dataset to Excel"
		} else if (event.getButton().getData().equals(TableViewerComponent.EXPORT_EXCEL_BUTTON_ID)
				&& this.source instanceof TableViewerComponent) {
			((TableViewerComponent) this.source).exportToExcelAction();

			// "Save to Fieldbook Excel File"
		} else if (event.getButton().getData().equals(SaveRepresentationDatasetExcelDialog.SAVE_EXCEL_BUTTON_ID)
				&& this.source instanceof SaveRepresentationDatasetExcelDialog) {
			((SaveRepresentationDatasetExcelDialog) this.source).saveExcelFileButtonClickAction();

			// "Cancel"
		} else if (event.getButton().getData().equals(SaveRepresentationDatasetExcelDialog.CANCEL_EXCEL_BUTTON_ID)
				&& this.source instanceof SaveRepresentationDatasetExcelDialog) {
			((SaveRepresentationDatasetExcelDialog) this.source).cancelSavingExcelFileButtonClickAction();

		} else if (event.getButton().getData().equals(SaveRepresentationDatasetExcelDialog.BROWSE_FOLDER_BUTTON_ID)
				&& this.source instanceof SaveRepresentationDatasetExcelDialog) {
			((SaveRepresentationDatasetExcelDialog) this.source).browseDirectoryButtonClickAction();

			// "Refresh")
		} else if (event.getButton().getData().equals(StudyTreeComponent.REFRESH_BUTTON_ID) && this.source instanceof StudyTreeComponent) {
			((StudyTreeComponent) this.source).createTree();

			// "Open in Table Viewer"
		} else if (event.getButton().getData().equals(RepresentationDatasetComponent.OPEN_TABLE_VIEWER_BUTTON_ID)
				&& this.source instanceof RepresentationDatasetComponent) {
			((RepresentationDatasetComponent) this.source).openTableViewerAction();

		} else {
			StudyButtonClickListener.LOG.error("StudyButtonClickListener: Error with buttonClick action. Source not identified.");
		}
	}

}

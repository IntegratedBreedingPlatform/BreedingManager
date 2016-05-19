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

import org.generationcp.breeding.manager.germplasm.GermplasmStudyInfoComponent;
import org.generationcp.breeding.manager.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.breeding.manager.study.StudySearchMainComponent;
import org.generationcp.breeding.manager.study.StudyTreeComponent;
import org.generationcp.breeding.manager.study.containers.StudyDataIndexContainer;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;

/**
 *
 * @author Joyce Avestro
 *
 */

public class StudyItemClickListener implements ItemClickEvent.ItemClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(StudyItemClickListener.class);
	private static final long serialVersionUID = -5286616518840026212L;

	private final Object source;

	public StudyItemClickListener(Object source) {
		this.source = source;
	}

	@Override
	public void itemClick(ItemClickEvent event) {

		if (this.source instanceof StudyTreeComponent) {
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((StudyTreeComponent) this.source).studyTreeItemClickAction(event.getItemId());
				} catch (InternationalizableException e) {
					StudyItemClickListener.LOG.error(e.getMessage(), e);
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription()); // TESTED
				}
				((StudyTreeComponent) this.source).getStudyTree().setNullSelectionAllowed(false);
				((StudyTreeComponent) this.source).getStudyTree().select(event.getItemId());
				((StudyTreeComponent) this.source).getStudyTree().setValue(event.getItemId());
			}
		}

		if (this.source instanceof StudySearchMainComponent && event.getButton() == ClickEvent.BUTTON_LEFT) {
			int studyId = Integer.valueOf(event.getItem().getItemProperty(StudyDataIndexContainer.STUDY_ID).getValue().toString());
			try {
				((StudySearchMainComponent) this.source).getSearchResultComponent().studyItemClickAction(studyId);
			} catch (InternationalizableException e) {
				StudyItemClickListener.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		}

		if (this.source instanceof GermplasmStudyInfoComponent) {
			int studyId = Integer.valueOf(event.getItem().getItemProperty(GermplasmIndexContainer.STUDY_ID).getValue().toString());
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((GermplasmStudyInfoComponent) this.source).studyItemClickAction(event, studyId);
				} catch (InternationalizableException e) {
					StudyItemClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
					e.printStackTrace();
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}
		}
	}

}

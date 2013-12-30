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

import org.generationcp.browser.germplasm.GermplasmStudyInfoComponent;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.study.StudySearchMainComponent;
import org.generationcp.browser.study.StudyTreeComponent;
import org.generationcp.browser.study.containers.StudyDataIndexContainer;
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

public class StudyItemClickListener implements ItemClickEvent.ItemClickListener{

    private static final Logger LOG = LoggerFactory.getLogger(StudyItemClickListener.class);
    private static final long serialVersionUID = -5286616518840026212L;

    private Object source;

    public StudyItemClickListener(Object source) {
        this.source = source;
    }

    @Override
    public void itemClick(ItemClickEvent event) {

        if (source instanceof StudyTreeComponent) {
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                try {
                    ((StudyTreeComponent) source).studyTreeItemClickAction(event.getItemId());
                } catch (InternationalizableException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription()); // TESTED 
                }
            }
        }
        
        if (source instanceof StudySearchMainComponent){
            int studyId = Integer.valueOf(event.getItem().getItemProperty(StudyDataIndexContainer.STUDY_ID).getValue().toString());
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                try {
                    ((StudySearchMainComponent) source).studyItemClickAction(studyId);
                } catch (InternationalizableException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());  
                }
            }
            
        }
        
        if (source instanceof GermplasmStudyInfoComponent) {
            int studyId = Integer.valueOf(event.getItem().getItemProperty(GermplasmIndexContainer.STUDY_ID).getValue().toString());
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                try {
                    ((GermplasmStudyInfoComponent) source).studyItemClickAction(event, studyId);
                } catch (InternationalizableException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());  
                }
            }
        }
    }

}

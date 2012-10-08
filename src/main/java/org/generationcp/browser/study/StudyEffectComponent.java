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

package org.generationcp.browser.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.listeners.StudyValueChangedListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Macky
 * 
 */
@Configurable
public class StudyEffectComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(StudyEffectComponent.class);
    private static final long serialVersionUID = 116672292965099233L;
    
    private final Accordion studyInfoAccordion;
    private final StudyDataManager studyDataManager;
    private final Integer studyId;
    private final Accordion accordion;
    private ListSelect datasetList;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudyEffectComponent(StudyDataManager studyDataManager, int studyId, Accordion accordion) {
        this.studyInfoAccordion = accordion;
        this.studyDataManager = studyDataManager;
        this.studyId = studyId;
        this.accordion = accordion;
    }

    // called by StudyValueChangedListener.valueChange()
    public void datasetListValueChangeAction(String datasetLabel) throws InternationalizableException{
        String[] parts = datasetLabel.split("-");
        Integer repId = Integer.valueOf(parts[0].replaceAll(messageSource.getMessage(Message.dataset_text), "").trim()); // "Dataset"
        String repName = parts[1].trim();

        // if repName is null or empty, use repId in dataset tab title
        repName = ((repName == null || repName.equals("")) ? repId.toString() : repName);
        
        String tabTitle = messageSource.getMessage(Message.dataset_of_text) + repName; // "Dataset of "

        if (!Util.isAccordionDatasetExist(accordion, tabTitle)) {
            RepresentationDatasetComponent datasetComponent = new RepresentationDatasetComponent(studyDataManager, repId, tabTitle,
            		studyId);
            studyInfoAccordion.addTab(datasetComponent, tabTitle);
            studyInfoAccordion.setSelectedTab(datasetComponent);
        } else {
            // open the representation dataset tab already exist if the user click the same representation dataset
            for (int i = 3; i < studyInfoAccordion.getComponentCount(); i++) {
                Tab tab = studyInfoAccordion.getTab(i);
                if (tab.getCaption().equals(tabTitle)) {
                    studyInfoAccordion.setSelectedTab(tab.getComponent());
                    break;
                }
            }
        }
    }
    
    @Override
    public void afterPropertiesSet() throws Exception{
    	
        List<Representation> representations = new ArrayList<Representation>();
        try {
            representations = studyDataManager.getRepresentationByStudyID(studyId);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.error_database, Message.error_in_getting_representation_by_study_id);
        }

        if (representations.isEmpty()) {
            addComponent(new Label(messageSource.getMessage(Message.no_datasets_retrieved_label))); // "No datasets retrieved."
        } else {
            List<String> datasets = new ArrayList<String>();

            for (Representation rep : representations) {
                if (rep.getName() != null) {
                    if (!rep.getName().equals(messageSource.getMessage(Message.study_effect_header))) { // "STUDY EFFECT"
                        datasets.add(messageSource.getMessage(Message.dataset_text) + " " + rep.getId() + " - " + rep.getName()); // Dataset
                    }
                } else {
                    datasets.add(messageSource.getMessage(Message.dataset_text) + " " + rep.getId() + " - " + rep.getName()); // Dataset
                }
            }

            this.datasetList = new ListSelect("", datasets);
            this.datasetList.setNullSelectionAllowed(false);
            this.datasetList.setImmediate(true);
            this.datasetList.setDescription(messageSource.getMessage(Message.click_dataset_to_view_text)); // "Click on a dataset to view it"
            this.datasetList.addListener(new StudyValueChangedListener(this));

            addComponent(this.datasetList);
        }
    	
    }
    
    @Override
    public void attach() {    	
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    }

}

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

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.listeners.StudySelectedTabChangeListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class StudyAccordionMenu extends Accordion implements InitializingBean, InternationalizableComponent {
    
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(StudyAccordionMenu.class);
    private static final long serialVersionUID = -1409312205229461614L;
    private int studyId;
    private VerticalLayout layoutVariate;
    private VerticalLayout layoutFactor;
    private VerticalLayout layoutEffect;

    private StudyDataManager studyDataManager;
    private TraitDataManager traitDataManager;
    private StudyDetailComponent studyDetailComponent;
   
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudyAccordionMenu(int studyId, StudyDetailComponent studyDetailComponent, StudyDataManager studyDataManager,
            TraitDataManager traitDataManager) {
        this.studyId = studyId;
        this.studyDataManager = studyDataManager;
        this.traitDataManager = traitDataManager;
        this.studyDetailComponent = studyDetailComponent;
        // Have it take all space available in the layout.
    }

    public void selectedTabChangeAction() throws InternationalizableException{
        Component selected = this.getSelectedTab();
        Tab tab = this.getTab(selected);
        if (tab.getCaption().equals(layoutFactor.getCaption())) { // "Factors"
            if (layoutFactor.getComponentCount() == 0) {
                layoutFactor.addComponent(new StudyFactorComponent(studyDataManager, traitDataManager, studyId));
                layoutFactor.setMargin(true);
                layoutFactor.setSpacing(true);
            }
        } else if (tab.getCaption().equals(layoutVariate.getCaption())) { // "Variates"
            if (layoutVariate.getComponentCount() == 0) {
                layoutVariate.addComponent(new StudyVariateComponent(studyDataManager, traitDataManager, studyId));
                layoutVariate.setMargin(true);
                layoutVariate.setSpacing(true);
            }
        } else if (tab.getCaption().equals(layoutEffect.getCaption())) { // "Datasets"
            if (layoutEffect.getComponentCount() == 0) {
                layoutEffect.addComponent(new StudyEffectComponent(studyDataManager, studyId, this));
            }
        }
    }
    
    @Override
    public void afterPropertiesSet() {
        this.setSizeFull();

        layoutVariate = new VerticalLayout();
        layoutFactor = new VerticalLayout();
        layoutEffect = new VerticalLayout();
        this.addTab(studyDetailComponent, "Study Details"); // "Study Details"
        this.addTab(layoutFactor, "Factors"); // "Factors"
        this.addTab(layoutVariate, "Variates"); // "Variates"
        this.addTab(layoutEffect, "Datasets"); // "Effects"

        this.addListener(new StudySelectedTabChangeListener(this));    	
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(studyDetailComponent, Message.study_details_label);
        messageSource.setCaption(layoutFactor, Message.factors_text);
        messageSource.setCaption(layoutVariate, Message.variates_text);
        messageSource.setCaption(layoutEffect, Message.datasets_text);
    }

}

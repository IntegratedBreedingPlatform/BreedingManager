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

import org.generationcp.browser.study.listeners.StudySelectedTabChangeListener;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class StudyAccordionMenu extends Accordion{

    private static final long serialVersionUID = -1409312205229461614L;
    private int studyId;
    private VerticalLayout layoutVariate;
    private VerticalLayout layoutFactor;
    private VerticalLayout layoutEffect;

    private StudyDataManager studyDataManager;
    private TraitDataManager traitDataManager;

    public StudyAccordionMenu(int studyId, StudyDetailComponent studyDetailComponent, StudyDataManager studyDataManager,
            TraitDataManager traitDataManager) {
        this.studyId = studyId;
        this.studyDataManager = studyDataManager;
        this.traitDataManager = traitDataManager;
        // Have it take all space available in the layout.
        this.setSizeFull();

        layoutVariate = new VerticalLayout();
        layoutFactor = new VerticalLayout();
        layoutEffect = new VerticalLayout();
        this.addTab(studyDetailComponent, "Study Details");
        this.addTab(layoutFactor, "Factors");
        this.addTab(layoutVariate, "Variates");
        this.addTab(layoutEffect, "Datasets");

        this.addListener(new StudySelectedTabChangeListener(this));
    }

    public void selectedTabChangeAction() {
        Component selected = this.getSelectedTab();
        Tab tab = this.getTab(selected);
        if (tab.getCaption().equals("Factors")) {
            if (layoutFactor.getComponentCount() == 0) {
                try {
                    layoutFactor.addComponent(new StudyFactorComponent(studyDataManager, traitDataManager, studyId));
                } catch (QueryException e) {
                    e.printStackTrace();
                }
            }
        } else if (tab.getCaption().equals("Variates")) {
            if (layoutVariate.getComponentCount() == 0) {
                try {
                    layoutVariate.addComponent(new StudyVariateComponent(studyDataManager, traitDataManager, studyId));
                } catch (QueryException e) {
                    e.printStackTrace();
                }
            }
        } else if (tab.getCaption().equals("Datasets")) {
            if (layoutEffect.getComponentCount() == 0) {
                layoutEffect.addComponent(new StudyEffectComponent(studyDataManager, studyId, this));

            }
        }

    }

}

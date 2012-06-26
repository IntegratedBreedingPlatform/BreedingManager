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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.generationcp.browser.study.listeners.StudyValueChangedListener;
import org.generationcp.browser.util.Util;
import org.generationcp.browser.i18n.ui.I18NVerticalLayout;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Representation;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TabSheet.Tab;

/**
 * 
 * @author Macky
 * 
 */
public class StudyEffectComponent extends I18NVerticalLayout {

    private final static Logger LOG = LoggerFactory.getLogger(StudyEffectComponent.class);
    private static final long serialVersionUID = 116672292965099233L;

    private final Accordion studyInfoAccordion;
    private final StudyDataManager managerToPass;
    private final Integer studyIdToPass;
    private final Accordion accordion;
    private ListSelect datasetList;

	public StudyEffectComponent(StudyDataManager studyDataManager, int studyId,
			Accordion accordion, I18N i18n) {
		super(i18n);

		this.studyInfoAccordion = accordion;
		this.managerToPass = studyDataManager;
		this.studyIdToPass = studyId;
		this.accordion = accordion;

		List<Representation> representations = new ArrayList<Representation>();
		try {
			representations = studyDataManager
					.getRepresentationByStudyID(studyIdToPass);
		} catch (QueryException e) {
			LOG.error(e.toString() + "\n" + e.getStackTrace());

			// TODO an error window in the UI should pop-up for this
			System.out.println(e);
			e.printStackTrace();
		}

		if (representations.isEmpty()) {
			addComponent(new Label(i18n.getMessage("noDatasetsRetrieved.label"))); //"No datasets retrieved."
		} else {
			List<String> datasets = new ArrayList<String>();

			for (Representation rep : representations) {
				if (rep.getName() != null) {
					if (!rep.getName().equals(i18n.getMessage("studyEffect.header"))){ //"STUDY EFFECT" 
						datasets.add(i18n.getMessage("dataset.text") + " " + rep.getId() + " - "
								+ rep.getName()); // Dataset
					}
				} else {
					datasets.add(i18n.getMessage("dataset.text") + " " + rep.getId() + " - "
							+ rep.getName()); // Dataset
				}
			}

			this.datasetList = new ListSelect("", datasets);
			this.datasetList.setNullSelectionAllowed(false);
			this.datasetList.setImmediate(true);
			this.datasetList.setDescription(i18n.getMessage("clickDatasetToView.text")); //"Click on a dataset to view it" 
			this.datasetList.addListener(new StudyValueChangedListener(this));

			addComponent(this.datasetList);
		}
	}

	// called by StudyValueChangedListener.valueChange()
	public void datasetListValueChangeAction(String datasetLabel) {
		String[] parts = datasetLabel.split("-");
		Integer repId = Integer.valueOf(parts[0].replaceAll(getI18N().getMessage("dataset.text"), "")  
				.trim()); //"Dataset"
		String repName = parts[1].trim();

		String tabTitle = getI18N().getMessage("datasetOf.text") + repName; //"Dataset of "

		if (!Util.isAccordionDatasetExist(accordion, tabTitle)) {
			RepresentationDatasetComponent datasetComponent = new RepresentationDatasetComponent(
					managerToPass, repId, tabTitle, studyIdToPass, getI18N());
			studyInfoAccordion.addTab(datasetComponent, tabTitle);
			studyInfoAccordion.setSelectedTab(datasetComponent);
		} else {
			// open the representation dataset tab already
			// exist if the user click the same
			// representation dataset
			for (int i = 3; i < studyInfoAccordion.getComponentCount(); i++) {
				Tab tab = studyInfoAccordion.getTab(i);
				if (tab.getCaption().equals(tabTitle)) {
					studyInfoAccordion.setSelectedTab(tab.getComponent());
					break;

				}
			}

		}
	}

}

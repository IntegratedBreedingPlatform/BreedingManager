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

package org.generationcp.breeding.manager.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.study.listeners.StudyValueChangedListener;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
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

	private final boolean fromUrl; // this is true if this component is created by accessing the Study Details page directly from the URL

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	private final boolean h2hCall;

	public StudyEffectComponent(StudyDataManager studyDataManager, int studyId, Accordion accordion, boolean fromUrl, boolean h2hCall) {
		this.studyInfoAccordion = accordion;
		this.studyDataManager = studyDataManager;
		this.studyId = studyId;
		this.accordion = accordion;
		this.fromUrl = fromUrl;
		this.h2hCall = h2hCall;
	}

	// called by StudyValueChangedListener.valueChange()
	public void datasetListValueChangeAction(String datasetLabel) throws InternationalizableException {
		String[] parts = datasetLabel.split("->");
		Integer datasetId = Integer.valueOf(parts[0].replaceAll(this.messageSource.getMessage(Message.DATASET_TEXT), "").trim()); // "Dataset"
		String datasetName = parts[1].trim();

		// if repName is null or empty, use repId in dataset tab title
		datasetName = datasetName == null || datasetName.equals("") ? datasetId.toString() : datasetName;

		String tabTitle = this.messageSource.getMessage(Message.DATASET_OF_TEXT) + datasetName; // "Dataset of "

		if (!Util.isAccordionDatasetExist(this.accordion, tabTitle)) {
			RepresentationDatasetComponent datasetComponent =
					new RepresentationDatasetComponent(this.studyDataManager, datasetId, tabTitle, this.studyId, this.fromUrl, this.h2hCall);
			this.studyInfoAccordion.addTab(datasetComponent, tabTitle);
			this.studyInfoAccordion.setSelectedTab(datasetComponent);
		} else {
			// open the representation dataset tab already exist if the user click the same representation dataset
			for (int i = 3; i < this.studyInfoAccordion.getComponentCount(); i++) {
				Tab tab = this.studyInfoAccordion.getTab(i);
				if (tab.getCaption().equals(tabTitle)) {
					this.studyInfoAccordion.setSelectedTab(tab.getComponent());
					break;
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		List<DatasetReference> datasetNodes = new ArrayList<DatasetReference>();
		try {
			datasetNodes = this.studyDataManager.getDatasetReferences(this.studyId);
		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_REPRESENTATION_BY_STUDY_ID);
		}

		if (datasetNodes.isEmpty()) {
			this.addComponent(new Label(this.messageSource.getMessage(Message.NO_DATASETS_RETRIEVED_LABEL))); // "No datasets retrieved."
		} else {
			List<String> datasets = new ArrayList<String>();

			for (DatasetReference node : datasetNodes) {
				datasets.add(this.messageSource.getMessage(Message.DATASET_TEXT) + " " + node.getId() + " -> " + node.getName()); // Dataset
			}

			this.datasetList = new ListSelect("", datasets);
			this.datasetList.setNullSelectionAllowed(false);
			this.datasetList.setImmediate(true);
			this.datasetList.setDescription(this.messageSource.getMessage(Message.CLICK_DATASET_TO_VIEW_TEXT)); // "Click on a dataset to view it"
			this.datasetList.addListener(new StudyValueChangedListener(this));

			this.addComponent(this.datasetList);
		}

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
	}

}

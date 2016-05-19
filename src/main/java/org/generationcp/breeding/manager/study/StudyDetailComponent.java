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

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class StudyDetailComponent extends GridLayout implements InitializingBean, InternationalizableComponent {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(StudyDetailComponent.class);
	private static final long serialVersionUID = 1738426765643928293L;

	private Label lblName;
	private Label lblTitle;
	private Label lblObjective;
	private Label lblType;
	private Label lblStartDate;
	private Label lblEndDate;

	private Label studyName;
	private Label studyTitle;
	private Label studyObjective;
	private Label studyType;
	private Label studyStartDate;
	private Label studyEndDate;

	private final org.generationcp.middleware.manager.api.StudyDataManager studyDataManager;
	private final int studyId;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public StudyDetailComponent(org.generationcp.middleware.manager.api.StudyDataManager studyDataManager, int studyId) {
		this.studyDataManager = studyDataManager;
		this.studyId = studyId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setRows(7);
		this.setColumns(3);
		this.setSpacing(true);
		this.setMargin(true);

		this.lblName = new Label(this.messageSource.getMessage(Message.NAME_LABEL)); // "Name"
		this.lblName.addStyleName("bold");

		this.lblTitle = new Label(this.messageSource.getMessage(Message.TITLE_LABEL)); // "Title"
		this.lblTitle.addStyleName("bold");

		this.lblObjective = new Label(this.messageSource.getMessage(Message.OBJECTIVE_LABEL)); // "Objective"
		this.lblObjective.addStyleName("bold");

		this.lblType = new Label(this.messageSource.getMessage(Message.TYPE_LABEL)); // "Type"
		this.lblType.addStyleName("bold");

		this.lblStartDate = new Label(this.messageSource.getMessage(Message.START_DATE_LABEL)); // "Start Date"
		this.lblStartDate.addStyleName("bold");

		this.lblEndDate = new Label(this.messageSource.getMessage(Message.END_DATE_LABEL)); // "End Date"
		this.lblEndDate.addStyleName("bold");

		// get Study
		Study study;

		try {
			study = this.studyDataManager.getStudy(this.studyId);

			this.studyName = new Label(this.setStudyDetailValue(study.getName()));
			this.studyTitle = new Label(this.setStudyDetailValue(study.getTitle()));
			this.studyObjective = new Label(this.setStudyDetailValue(study.getObjective()));
			this.studyType = new Label(this.setStudyDetailValue(study.getType().getName()));
			this.studyStartDate = new Label(this.setStudyDetailValue(String.valueOf(study.getStartDate())));
			this.studyEndDate = new Label(this.setStudyDetailValue(String.valueOf(study.getEndDate())));

		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID, Message.EMPTY_STRING);
		}

		this.addComponent(this.lblName, 1, 1);
		this.addComponent(this.lblTitle, 1, 2);
		this.addComponent(this.lblObjective, 1, 3);
		this.addComponent(this.lblType, 1, 4);
		this.addComponent(this.lblStartDate, 1, 5);
		this.addComponent(this.lblEndDate, 1, 6);

		this.addComponent(this.studyName, 2, 1);
		this.addComponent(this.studyTitle, 2, 2);
		this.addComponent(this.studyObjective, 2, 3);
		this.addComponent(this.studyType, 2, 4);
		this.addComponent(this.studyStartDate, 2, 5);
		this.addComponent(this.studyEndDate, 2, 6);
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
	}

	private String setStudyDetailValue(String value) {

		if (value == null || value.equals("null")) {
			return "";
		}
		return value;
	}

	public void setStudyName(String name) {
		this.studyName.setValue(name);
	}

}

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

import org.generationcp.browser.i18n.ui.I18NGridLayout;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Study;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.ui.Label;

public class StudyDetailComponent extends I18NGridLayout {

	private static final long serialVersionUID = 1738426765643928293L;

	private Label lblName;
	private Label lblTitle;
	private Label lblObjective;
	private Label lblType;
	private Label lblStartDate;
	private Label lblEndDate;

	public StudyDetailComponent(StudyDataManager studyDataManager, int StudyId,
			I18N i18n) throws QueryException {

		super(i18n);

		setRows(7);
		setColumns(3);
		setSpacing(true);
		setMargin(true);

		lblName = new Label(i18n.getMessage("name.label")); // "Name"
		lblTitle = new Label(i18n.getMessage("title.label")); // "Title"
		lblObjective = new Label(i18n.getMessage("objective.label")); // "Objective"
		lblType = new Label(i18n.getMessage("type.label")); // "Type"
		lblStartDate = new Label(i18n.getMessage("startDate.label")); // "Start Date"
		lblEndDate = new Label(i18n.getMessage("endDate.label")); // "End Date"

		addComponent(lblName, 1, 1);
		addComponent(lblTitle, 1, 2);
		addComponent(lblObjective, 1, 3);
		addComponent(lblType, 1, 4);
		addComponent(lblStartDate, 1, 5);
		addComponent(lblEndDate, 1, 6);

		// get Study Detail

		Study s = studyDataManager.getStudyByID(StudyId);

		Label studyName = new Label(s.getName());
		Label studyTitle = new Label(s.getTitle());
		Label studyObjective = new Label(s.getObjective());
		Label studyType = new Label(s.getType());
		Label studyStartDate = new Label(String.valueOf(s.getStartDate()));
		Label studyEndDate = new Label(String.valueOf(s.getEndDate()));

		addComponent(studyName, 2, 1);
		addComponent(studyTitle, 2, 2);
		addComponent(studyObjective, 2, 3);
		addComponent(studyType, 2, 4);
		addComponent(studyStartDate, 2, 5);
		addComponent(studyEndDate, 2, 6);

	}

}

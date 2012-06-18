/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/

package org.generationcp.browser.study;

import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Study;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class StudyDetailComponent extends VerticalLayout{

    public StudyDetailComponent(StudyDataManager studyDataManager, int StudyId) throws QueryException {

	GridLayout grid = new GridLayout(3, 7);
	grid.setSpacing(true);

	Label lblName = new Label("Name");
	Label lblTitle = new Label("Title");
	Label lblObjective = new Label("Objective");
	Label lblType = new Label("Type");
	Label lblStartDate = new Label("Start Date");
	Label lblEndDate = new Label("End Date");

	grid.addComponent(lblName, 1, 1);
	grid.addComponent(lblTitle, 1, 2);
	grid.addComponent(lblObjective, 1, 3);
	grid.addComponent(lblType, 1, 4);
	grid.addComponent(lblStartDate, 1, 5);
	grid.addComponent(lblEndDate, 1, 6);

	// get Study Detail

	Study s = studyDataManager.getStudyByID(StudyId);

	Label studyName = new Label(s.getName());
	Label studyTitle = new Label(s.getTitle());
	Label studyObjective = new Label(s.getObjective());
	Label studyType = new Label(s.getType());
	Label studyStartDate = new Label(String.valueOf(s.getStartDate()));
	Label studyEndDate = new Label(String.valueOf(s.getEndDate()));

	grid.addComponent(studyName, 2, 1);
	grid.addComponent(studyTitle, 2, 2);
	grid.addComponent(studyObjective, 2, 3);
	grid.addComponent(studyType, 2, 4);
	grid.addComponent(studyStartDate, 2, 5);
	grid.addComponent(studyEndDate, 2, 6);
	addComponent(grid);
	setSpacing(true);
	setMargin(true);
    }

}

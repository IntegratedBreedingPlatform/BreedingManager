/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.study.application;

import org.generationcp.browser.study.table.indexcontainer.StudyDataIndexContainer;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class StudyVariateComponent extends VerticalLayout {

	public StudyVariateComponent(StudyDataManager studyDataManager,TraitDataManager traitDataManager,int studyId) throws QueryException {

		Table studyVariateTable=new Table();
		StudyDataIndexContainer dataIndexContainer= new StudyDataIndexContainer(studyDataManager,traitDataManager,studyId);
		IndexedContainer dataStudyFactor=dataIndexContainer.getStudyVariate();
		studyVariateTable = new Table("",dataStudyFactor);
		studyVariateTable.setSelectable(true);
		studyVariateTable.setMultiSelect(false);
		studyVariateTable.setImmediate(true); // react at once when something is selected
		studyVariateTable.setSizeFull();
		// turn on column reordering and collapsing
		studyVariateTable.setColumnReorderingAllowed(true);
		studyVariateTable.setColumnCollapsingAllowed(true);

		// set column headers
		studyVariateTable.setColumnHeaders(new String[] {"NAME","DESCRIPTION","PROPERTY","SCALE","METHOD","DATATYPE"});
		addComponent(studyVariateTable);
		setMargin(true);
		setSpacing(true);
	}

}

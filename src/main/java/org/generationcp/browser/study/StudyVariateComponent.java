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

import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class StudyVariateComponent extends Table{

	public StudyVariateComponent(StudyDataManager studyDataManager, TraitDataManager traitDataManager, int studyId) throws QueryException {

		StudyDataIndexContainer dataIndexContainer = new StudyDataIndexContainer(studyDataManager, traitDataManager, studyId);
		IndexedContainer dataStudyFactor = dataIndexContainer.getStudyVariate();
		this.setContainerDataSource(dataStudyFactor);
		setSelectable(true);
		setMultiSelect(false);
		setImmediate(true); // react at once when something is
		setSizeFull();
		setColumnReorderingAllowed(true);
		setColumnCollapsingAllowed(true);
		setColumnHeaders(new String[] { "NAME", "DESCRIPTION", "PROPERTY", "SCALE", "METHOD", "DATATYPE" });
	}

}

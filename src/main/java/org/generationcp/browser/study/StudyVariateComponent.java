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

import org.generationcp.browser.i18n.ui.I18NTable;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class StudyVariateComponent extends I18NTable{

	private static final long serialVersionUID = -3225098517785018744L;

	public StudyVariateComponent(StudyDataManager studyDataManager,
			TraitDataManager traitDataManager, int studyId, I18N i18n)
			throws QueryException {
		super(i18n);

		StudyDataIndexContainer dataIndexContainer = new StudyDataIndexContainer(studyDataManager, traitDataManager, studyId);
		IndexedContainer dataStudyFactor = dataIndexContainer.getStudyVariate();
		this.setContainerDataSource(dataStudyFactor);
		setSelectable(true);
		setMultiSelect(false);
		setImmediate(true); // react at once when something is
		setSizeFull();
		setColumnReorderingAllowed(true);
		setColumnCollapsingAllowed(true);
		//setColumnHeaders(new String[] { "NAME", "DESCRIPTION", "PROPERTY", "SCALE", "METHOD", "DATATYPE" });
		setColumnHeaders(new String[] {
				i18n.getMessage("name.header"),
				i18n.getMessage("description.header"),
				i18n.getMessage("property.header"),
				i18n.getMessage("scale.header"),
				i18n.getMessage("method.header"),
				i18n.getMessage("datatye.header") });
	}

}

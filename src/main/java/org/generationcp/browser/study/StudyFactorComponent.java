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

public class StudyFactorComponent extends VerticalLayout{

    public StudyFactorComponent(StudyDataManager studyDataManager, TraitDataManager traitDataManager, int studyId) throws QueryException {
        // TODO Auto-generated constructor stub
        Table studyFactorTable = new Table();
        StudyDataIndexContainer dataIndexContainer = new StudyDataIndexContainer(studyDataManager, traitDataManager, studyId);
        IndexedContainer dataStudyFactor = dataIndexContainer.getStudyFactor();
        studyFactorTable = new Table("", dataStudyFactor);
        studyFactorTable.setSelectable(true);
        studyFactorTable.setMultiSelect(false);
        studyFactorTable.setImmediate(true); // react at once when something is
        // selected
        studyFactorTable.setSizeFull();
        // turn on column reordering and collapsing
        studyFactorTable.setColumnReorderingAllowed(true);
        studyFactorTable.setColumnCollapsingAllowed(true);

        // set column headers
        studyFactorTable.setColumnHeaders(new String[] { "NAME", "DESCRIPTION", "PROPERTY", "SCALE", "METHOD", "DATATYPE" });
        addComponent(studyFactorTable);
        setMargin(true);
        setSpacing(true);
    }

}

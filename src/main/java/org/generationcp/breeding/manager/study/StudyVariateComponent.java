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
import org.generationcp.breeding.manager.study.containers.StudyDataIndexContainer;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

@Configurable
public class StudyVariateComponent extends Table implements InitializingBean, InternationalizableComponent {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(StudyVariateComponent.class);
	private static final long serialVersionUID = -3225098517785018744L;

	private final StudyDataManager studyDataManager;
	private final int studyId;

	private static final String NAME = "NAME";
	private static final String DESC = "DESCRIPTION";
	private static final String PROP = "PROPERTY";
	private static final String SCA = "SCALE";
	private static final String METH = "METHOD";
	private static final String DTYPE = "DATATYPE";
	private static final String VALUE = "VALUE";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public StudyVariateComponent(StudyDataManager studyDataManager, int studyId) {
		this.studyDataManager = studyDataManager;
		this.studyId = studyId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		StudyDataIndexContainer dataIndexContainer = new StudyDataIndexContainer(this.studyDataManager, this.studyId);
		IndexedContainer dataStudyFactor;

		dataStudyFactor = dataIndexContainer.getStudyVariate();
		this.setContainerDataSource(dataStudyFactor);

		this.setSelectable(true);
		this.setMultiSelect(false);
		this.setImmediate(true); // react at once when something is
		this.setSizeFull();
		this.setColumnReorderingAllowed(true);
		this.setColumnCollapsingAllowed(true);
		this.setColumnHeaders(new String[] {StudyVariateComponent.NAME, StudyVariateComponent.DESC, StudyVariateComponent.PROP,
				StudyVariateComponent.SCA, StudyVariateComponent.METH, StudyVariateComponent.DTYPE, StudyVariateComponent.VALUE});
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setColumnHeader(this, "NAME", Message.NAME_HEADER);
		this.messageSource.setColumnHeader(this, "DESCRIPTION", Message.DESCRIPTION_HEADER);
		this.messageSource.setColumnHeader(this, "PROPERTY", Message.PROPERTY_HEADER);
		this.messageSource.setColumnHeader(this, "SCALE", Message.SCALE_HEADER);
		this.messageSource.setColumnHeader(this, "METHOD", Message.METHOD_HEADER);
		this.messageSource.setColumnHeader(this, "DATATYPE", Message.DATATYPE_HEADER);
		this.messageSource.setColumnHeader(this, "VALUE", Message.VALUE_HEADER);
	}

}

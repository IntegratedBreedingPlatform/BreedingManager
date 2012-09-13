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

package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

@Configurable
public class GermplasmStudyInfoComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    
    private static final String STUDY_NAME = "Study Name";
    private static final String DESCRIPTION = "Description";
    private static final String NUM_ROWS = "Number of Rows";
   
    
    private GermplasmIndexContainer dataIndexContainer;
    
    private GermplasmDetailModel gDetailModel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmStudyInfoComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
    	this.dataIndexContainer = dataIndexContainer;
    	this.gDetailModel = gDetailModel;
    }
    
    @Override
    public void afterPropertiesSet() {
        IndexedContainer dataSourceStudyInformation = dataIndexContainer.getGermplasmStudyInformation(gDetailModel);
        this.setContainerDataSource(dataSourceStudyInformation);
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        setColumnHeaders(new String[] { STUDY_NAME, DESCRIPTION, NUM_ROWS});
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(this, STUDY_NAME, Message.study_name_label);
        messageSource.setColumnHeader(this, DESCRIPTION, Message.description_label);
        messageSource.setColumnHeader(this, NUM_ROWS, Message.number_of_rows);

    }

}

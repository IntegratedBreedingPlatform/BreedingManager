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
public class GermplasmNamesComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    
    private static final String TYPE = "Type";
    private static final String NAME = "Name";
    private static final String DATE = "Date";
    private static final String LOCATION = "Location";
    private static final String TYPE_DESC = "Type Desc";
    
    private GermplasmIndexContainer dataIndexContainer;
    
    private GermplasmDetailModel gDetailModel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmNamesComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
    	this.dataIndexContainer = dataIndexContainer;
    	this.gDetailModel = gDetailModel;
    }
    
    @Override
    public void afterPropertiesSet() {
        IndexedContainer dataSourceNames = dataIndexContainer.getGermplasmNames(gDetailModel);
        this.setContainerDataSource(dataSourceNames);
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        setColumnHeaders(new String[] { NAME, DATE, LOCATION, TYPE, TYPE_DESC });
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(this, TYPE, Message.TYPE_LABEL);
        messageSource.setColumnHeader(this, NAME, Message.NAME_LABEL);
        messageSource.setColumnHeader(this, DATE, Message.DATE_LABEL);
        messageSource.setColumnHeader(this, LOCATION, Message.LOCATION_LABEL);
        messageSource.setColumnHeader(this, TYPE_DESC, Message.TYPEDESC_LABEL);
    }

}

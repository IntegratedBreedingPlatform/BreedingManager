/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmIndexContainer;
import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Window.Notification;

@Configurable
public class GermplasmAttributesComponent extends BreedingManagerTable implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmAttributesComponent.class);
    
    private static final String TYPE = "Type";
    private static final String NAME = "Value";
    private static final String DATE = "Date";
    private static final String LOCATION = "Location";
    private static final String TYPE_DESC = "Type Desc";
    
    GermplasmIndexContainer dataIndexContainer;
    
    GermplasmDetailModel gDetailModel;
    
    private static final Integer MAX_RECORDS = 8;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmAttributesComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel, int recordCount, int maxRecords) {
    	super(recordCount, maxRecords);
        this.dataIndexContainer = dataIndexContainer;
        this.gDetailModel = gDetailModel;
    }
    
    public GermplasmAttributesComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
    	super(MAX_RECORDS, MAX_RECORDS);
        this.dataIndexContainer = dataIndexContainer;
        this.gDetailModel = gDetailModel;
    }
    
    public GermplasmAttributesComponent(int recordCount, int maxRecords){
    	super(recordCount, maxRecords);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try{
	    	IndexedContainer dataSourceAttribute = dataIndexContainer.getGermplasmAttribute(gDetailModel);
	        this.setContainerDataSource(dataSourceAttribute);
	        setSelectable(true);
	        setMultiSelect(false);
	        setHeight("100%");
	        setWidth("100%");
	        //setPageLength(5);
	        setImmediate(true); // react at once when something is
	        setColumnReorderingAllowed(true);
	        setColumnCollapsingAllowed(true);
	        setColumnHeaders(new String[] { TYPE, TYPE_DESC, NAME, DATE, LOCATION, });
        } catch(InternationalizableException ex){
        	LOG.error("Error in getting germplasm attributes.", ex);
        	MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
        			, messageSource.getMessage(Message.ERROR_IN_GETTING_ATTRIBUTES_BY_GERMPLASM_ID));
        	return;
        }
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

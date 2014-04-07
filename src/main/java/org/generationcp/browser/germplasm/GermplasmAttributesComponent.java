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
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmAttributesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    
    GermplasmIndexContainer dataIndexContainer;
    
    GermplasmDetailModel gDetailModel;
    
    private Table attributesTable;
    private Label noDataAvailableLabel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmAttributesComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
        this.dataIndexContainer = dataIndexContainer;
        this.gDetailModel = gDetailModel;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeComponents();
        initializeValues();
        addListeners();
        layoutComponents();
    }
    
    public void initializeComponents(){
    	IndexedContainer attributes = dataIndexContainer.getGermplasmAttribute(gDetailModel);
        
    	if(attributes.getItemIds().isEmpty()){
    		noDataAvailableLabel = new Label("There is no Attributes information for this germplasm.");
    	} else{
	    	attributesTable = new Table();
	    	attributesTable.setWidth("90%");
	    	attributesTable.setContainerDataSource(attributes);
	    	if(attributes.getItemIds().size() < 10){
	    		attributesTable.setPageLength(attributes.getItemIds().size());
	    	} else{
	    		attributesTable.setPageLength(10);
	    	}
	    	attributesTable.setSelectable(true);
	        attributesTable.setMultiSelect(false);
	        attributesTable.setImmediate(true); // react at once when something is
	        attributesTable.setColumnReorderingAllowed(true);
	        attributesTable.setColumnCollapsingAllowed(true);
	        attributesTable.setColumnHeaders(new String[] { messageSource.getMessage(Message.TYPE_LABEL)
	        		, messageSource.getMessage(Message.TYPEDESC_LABEL)
	        		, messageSource.getMessage(Message.NAME_LABEL)
	        		, messageSource.getMessage(Message.DATE_LABEL)
	        		, messageSource.getMessage(Message.LOCATION_LABEL)});
    	}
    }
    
    public void initializeValues(){
    	
    }
    
    public void addListeners(){
    	
    }
    
    public void layoutComponents(){
    	if(attributesTable != null){
    		addComponent(attributesTable);
    	} else{
    		addComponent(noDataAvailableLabel);
    	}
    }
    
    @Override
    public void updateLabels() {
        
    }

}

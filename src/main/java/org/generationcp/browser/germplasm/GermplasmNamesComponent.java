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
public class GermplasmNamesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    
    private GermplasmIndexContainer dataIndexContainer;
    private GermplasmDetailModel gDetailModel;
    
    private Table namesTable;
    private Label noDataAvailableLabel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmNamesComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
        this.dataIndexContainer = dataIndexContainer;
        this.gDetailModel = gDetailModel;
    }
    
    @Override
    public void afterPropertiesSet() {
    	initializeComponents();
        initializeValues();
        addListeners();
        layoutComponents();
    }
    
    private void initializeComponents(){
    	IndexedContainer names = dataIndexContainer.getGermplasmNames(gDetailModel);
    	
    	if(names.getItemIds().isEmpty()){
    		noDataAvailableLabel = new Label("There is no Names information for this germplasm.");
    	} else{
	    	namesTable = new Table();
	    	namesTable.setWidth("90%");
	    	namesTable.setContainerDataSource(names);
	    	if(names.getItemIds().size() < 10){
	    		namesTable.setPageLength(names.getItemIds().size());
	    	} else{
	    		namesTable.setPageLength(10);
	    	}
	    	namesTable.setSelectable(true);
	    	namesTable.setMultiSelect(false);
	        namesTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
	        namesTable.setColumnReorderingAllowed(true);
	        namesTable.setColumnCollapsingAllowed(true);
	        namesTable.setColumnHeaders(new String[] { messageSource.getMessage(Message.NAME_LABEL)
	        		, messageSource.getMessage(Message.DATE_LABEL)
	        		, messageSource.getMessage(Message.LOCATION_LABEL)
	        		, messageSource.getMessage(Message.TYPE_LABEL)
	        		, messageSource.getMessage(Message.TYPEDESC_LABEL)});
    	}
    }
    
    private void initializeValues(){
    	
    }
    
    private void addListeners(){
    	
    }
    
    private void layoutComponents(){
    	if(namesTable != null){
    		addComponent(namesTable);
    	} else{
    		addComponent(noDataAvailableLabel);
    	}
    }
    
    @Override
    public void updateLabels() {
        
    }

}

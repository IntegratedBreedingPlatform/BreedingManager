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
public class InventoryInformationComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;

    private static final String ACTUAL_LOT_BALANCE = "Actual Lot Balance";
    private static final String LOCATION_NAME = "Location Name";
    private static final String SCALE_NAME = "Scale Name";
    private static final String LOT_COMMENT = "Lot Comment";
    
    private GermplasmIndexContainer dataIndexContainer;
    private GermplasmDetailModel gDetailModel;
    
    private Table inventoryTable;
    private Label noDataAvailableLabel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public InventoryInformationComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
        this.dataIndexContainer = dataIndexContainer;
        this.gDetailModel=gDetailModel;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents(){
    	IndexedContainer inventoryInformation = dataIndexContainer.getReportOnLots(gDetailModel);
        
        if(inventoryInformation.getItemIds().isEmpty()){
        	noDataAvailableLabel = new Label("There is no Inventory Information available for this germplasm.");
        } else{
        	inventoryTable = new Table();
        	inventoryTable.setWidth("90%");
        	inventoryTable.setContainerDataSource(inventoryInformation);
        	if(inventoryInformation.getItemIds().size() < 10){
        		inventoryTable.setPageLength(inventoryInformation.getItemIds().size());
        	} else{
        		inventoryTable.setPageLength(10);
        	}
        	inventoryTable.setSelectable(true);
        	inventoryTable.setMultiSelect(false);
	        inventoryTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
	        inventoryTable.setColumnReorderingAllowed(true);
	        inventoryTable.setColumnCollapsingAllowed(true);
	        inventoryTable.setColumnHeaders(new String[] { ACTUAL_LOT_BALANCE, LOCATION_NAME, SCALE_NAME, LOT_COMMENT });
        }
    }
    
    private void layoutComponents(){
    	if(inventoryTable != null){
    		addComponent(inventoryTable);
    	} else{
    		addComponent(noDataAvailableLabel);
    	}
    }
    @Override
    public void updateLabels() {
        
    }

}

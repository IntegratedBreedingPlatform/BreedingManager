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
public class GermplasmGenerationHistoryComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    
    GermplasmIndexContainer dataIndexContainer;
    GermplasmDetailModel gDetailModel;
    
    private Table generationHistoryTable;
    private Label noDataAvailableLabel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmGenerationHistoryComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
        this.dataIndexContainer = dataIndexContainer;
        this.gDetailModel = gDetailModel;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	initializeComponents();
    	layoutComponents();
    }
    
    private void initializeComponents(){
    	IndexedContainer generationHistory = dataIndexContainer.getGermplasmGenerationHistory(gDetailModel);

    	if(generationHistory.getItemIds().isEmpty()){
    		noDataAvailableLabel = new Label("There is no Generation History Information for this gemrplasm.");
    	} else{
    		generationHistoryTable = new Table();
    		generationHistoryTable.setContainerDataSource(generationHistory);
    		if(generationHistory.getItemIds().size() < 10){
    			generationHistoryTable.setPageLength(generationHistory.getItemIds().size());
    		} else{
    			generationHistoryTable.setPageLength(10);
    		}
    		generationHistoryTable.setSelectable(true);
    		generationHistoryTable.setMultiSelect(false);
    		generationHistoryTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
    		generationHistoryTable.setColumnReorderingAllowed(true);
    		generationHistoryTable.setColumnCollapsingAllowed(true);
    		generationHistoryTable.setColumnHeaders(new String[] { messageSource.getMessage(Message.GID_LABEL)
    				, messageSource.getMessage(Message.PREFNAME_LABEL)});
    	}
    }
    
    private void layoutComponents(){
    	if(generationHistoryTable != null){
    		addComponent(generationHistoryTable);
    	} else{
    		addComponent(noDataAvailableLabel);
    	}
    }

    @Override
    public void updateLabels() {
        
    }

}

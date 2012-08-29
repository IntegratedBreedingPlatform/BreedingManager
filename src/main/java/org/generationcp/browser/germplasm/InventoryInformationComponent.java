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
public class InventoryInformationComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;

    private static final String ACTUAL_LOT_BALANCE = "Actual Lot Balance";
    private static final String LOCATION_NAME = "Location Name";
    private static final String SCALE_NAME = "Scale Name";
    private static final String LOT_COMMENT = "Lot Comment";
    
    private GermplasmIndexContainer dataIndexContainer;
    private GermplasmDetailModel gDetailModel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public InventoryInformationComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
    	this.dataIndexContainer = dataIndexContainer;
    	this.gDetailModel=gDetailModel;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        IndexedContainer dataSourceNames = dataIndexContainer.getReportOnLots(gDetailModel);
        this.setContainerDataSource(dataSourceNames);
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        setColumnHeaders(new String[] { ACTUAL_LOT_BALANCE, LOCATION_NAME, SCALE_NAME, LOT_COMMENT });
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(this, ACTUAL_LOT_BALANCE, Message.lot_balance_header);
        messageSource.setColumnHeader(this, LOCATION_NAME, Message.location_header);
        messageSource.setColumnHeader(this, SCALE_NAME, Message.scale_header);
        messageSource.setColumnHeader(this, LOT_COMMENT, Message.lot_comment_header);
    }

}

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

package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;

/**
 * This class contains the absolute layout of UI elements in Cross Info section
 * in "Enter Additional Details..." tab in Crossing Manager application
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class AdditionalDetailsCrossInfoComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

	private static final long serialVersionUID = -1197900610042529900L;
	private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsCrossNameComponent.class);

    private Label harvestDateLabel;
    private Label harvestLocationLabel;
    
    private DateField harvestDtDateField;
    private ComboBox harvestLocComboBox;
    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    
	@Override
	public void afterPropertiesSet() throws Exception {  
		setHeight("95px");
        setWidth("700px");
        	
		harvestDateLabel = new Label();
		addComponent(harvestDateLabel, "top:30px;left:20px");
		
		harvestDtDateField = new DateField();
		harvestDtDateField.setResolution(DateField.RESOLUTION_DAY);
		harvestDtDateField.setDateFormat(CrossingManagerMain.DATE_FORMAT);
		addComponent(harvestDtDateField, "top:10px;left:140px");
		
		
		harvestLocationLabel = new Label();
		addComponent(harvestLocationLabel, "top:60px;left:20px");
		
		harvestLocComboBox = new ComboBox();
		harvestLocComboBox.setWidth("400px");
		addComponent(harvestLocComboBox, "top:40px;left:140px");
		
	}
	
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
	@Override
	public void updateLabels() {
		messageSource.setCaption(harvestDateLabel, Message.HARVEST_DATE);
		messageSource.setCaption(harvestLocationLabel, Message.HARVEST_LOCATION);
		
	}

}

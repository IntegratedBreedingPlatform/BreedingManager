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
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;

/**
 * This class contains the absolute layout of UI elements in Breeding Method section
 * in "Enter Additional Details..." tab in Crossing Manager application
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class AdditionalDetailsBreedingMethodComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

	private static final long serialVersionUID = 2539886412902509326L;
	private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsBreedingMethodComponent.class);

    private Label selectOptionLabel;
    private Label selectBreedingMethodLabel;
    
    private OptionGroup breedingMethodOptionGroup;
    private ComboBox breedingMethodComboBox;
    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    
	@Override
	public void afterPropertiesSet() throws Exception {  
		setHeight("130px");
        setWidth("700px");
        
        selectOptionLabel = new Label();
		addComponent(selectOptionLabel, "top:25px;left:20px");
		
		breedingMethodOptionGroup = new OptionGroup();
		breedingMethodOptionGroup.addItem(messageSource.getMessage(Message.BREEDING_METHOD_WILL_BE_THE_SAME_FOR_ALL_CROSSES));
		breedingMethodOptionGroup.addItem(messageSource.getMessage(Message.BREEDING_METHOD_WILL_BE_SET_BASED_ON_STATUS_OF_PARENTAL_LINES));
		addComponent(breedingMethodOptionGroup, "top:35px;left:20px");
		
		selectBreedingMethodLabel = new Label();
		addComponent(selectBreedingMethodLabel, "top:105px;left:20px");
		
		breedingMethodComboBox = new ComboBox();
		breedingMethodComboBox.setWidth("400px");
		addComponent(breedingMethodComboBox, "top:85px;left:180px");
	}
	
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
	@Override
	public void updateLabels() {
		messageSource.setCaption(selectOptionLabel, Message.SELECT_AN_OPTION);
		messageSource.setCaption(selectBreedingMethodLabel, Message.SELECT_BREEDING_METHOD);
	}

}

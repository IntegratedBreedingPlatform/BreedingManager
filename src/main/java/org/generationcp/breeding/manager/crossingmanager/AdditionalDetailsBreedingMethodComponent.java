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

import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
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

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
      
    private Label selectOptionLabel;
    private Label selectBreedingMethodLabel;
    
    private OptionGroup breedingMethodOptionGroup;
    private ComboBox breedingMethodComboBox;
    private HashMap<String, Integer> mapMethods;
    private CrossingManagerImportFileComponent wizardScreenOne;
    
    private enum BreedingMethodOption{
    	SAME_FOR_ALL_CROSSES, BASED_ON_PARENTAL_LINES
    };
    
	public AdditionalDetailsBreedingMethodComponent(
	    CrossingManagerImportFileComponent wizardScreenOne) {
	this.wizardScreenOne=wizardScreenOne;
    }

	@SuppressWarnings("serial")
	@Override
	public void afterPropertiesSet() throws Exception {  
		setHeight("130px");
        setWidth("700px");
        
        selectOptionLabel = new Label();
		
		breedingMethodOptionGroup = new OptionGroup();
		breedingMethodOptionGroup.addItem(BreedingMethodOption.SAME_FOR_ALL_CROSSES);
		breedingMethodOptionGroup.setItemCaption(BreedingMethodOption.SAME_FOR_ALL_CROSSES, 
				messageSource.getMessage(Message.BREEDING_METHOD_WILL_BE_THE_SAME_FOR_ALL_CROSSES));
		breedingMethodOptionGroup.addItem(BreedingMethodOption.BASED_ON_PARENTAL_LINES);
		breedingMethodOptionGroup.setItemCaption(BreedingMethodOption.BASED_ON_PARENTAL_LINES, 
				messageSource.getMessage(Message.BREEDING_METHOD_WILL_BE_SET_BASED_ON_STATUS_OF_PARENTAL_LINES));
		breedingMethodOptionGroup.select(BreedingMethodOption.BASED_ON_PARENTAL_LINES);
		breedingMethodOptionGroup.setImmediate(true);
		breedingMethodOptionGroup.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
			    if(breedingMethodOptionGroup.getValue().equals(BreedingMethodOption.SAME_FOR_ALL_CROSSES)){
				selectBreedingMethodLabel.setEnabled(true);
				breedingMethodComboBox.setEnabled(true);
				
				try {
				    populateBreedinMethod();
				} catch (MiddlewareQueryException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}
			    }else{
				selectBreedingMethodLabel.setEnabled(false);
				breedingMethodComboBox.setEnabled(false);
				breedingMethodComboBox.removeAllItems();
			    }
			}
		
		});
		selectBreedingMethodLabel = new Label();
		selectBreedingMethodLabel.setEnabled(false);
		
		breedingMethodComboBox = new ComboBox();
		breedingMethodComboBox.setWidth("400px");
		breedingMethodComboBox.setEnabled(false);

		//layout components
		addComponent(selectOptionLabel, "top:25px;left:20px");
		addComponent(breedingMethodOptionGroup, "top:35px;left:20px");
		addComponent(selectBreedingMethodLabel, "top:105px;left:20px");
		addComponent(breedingMethodComboBox, "top:85px;left:180px");
	}
	
    private void populateBreedinMethod() throws MiddlewareQueryException {
	    // TODO Auto-generated method stub
	
	List<Method> methods = germplasmDataManager.getMethodsByType("GEN");

	mapMethods = new HashMap<String, Integer>();
	String breedingMethod=wizardScreenOne.getCrossingManagerUploader().getBreedingMethod();
	String beedingMethodId=wizardScreenOne.getCrossingManagerUploader().getBreedingMethodId();
	if(breedingMethod.length() > 0 && beedingMethodId.length() > 0){
	    breedingMethodComboBox.addItem(breedingMethod);
	    mapMethods.put(breedingMethod, Integer.valueOf(beedingMethodId));
	    breedingMethodComboBox.select(breedingMethod);
	}else{
	    breedingMethodComboBox.select("");
	}
	for (Method m : methods) {
	    breedingMethodComboBox.addItem(m.getMname());
	    mapMethods.put(m.getMname(), new Integer(m.getMid()));
	}
	
	 //Integer mId = mapMethods.get(breedingMethodComboBox.getValue());
	    
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

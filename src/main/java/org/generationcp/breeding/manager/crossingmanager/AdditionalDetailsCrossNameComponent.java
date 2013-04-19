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
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;

/**
 * This class contains the absolute layout of UI elements in Cross Name section
 * in "Enter Additional Details..." tab in Crossing Manager application
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class AdditionalDetailsCrossNameComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

	private static final long serialVersionUID = -1197900610042529900L;
	private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsCrossNameComponent.class);

    private Label specifyPrefixLabel;
    private Label specifySuffixLabel;
    private Label howManyDigitsLabel; 
    private Label nextNameInSequenceLabel;
    
    private OptionGroup crossNameOptionGroup;
   
    private TextField prefixTextField;
    private TextField suffixTextField;
    
    private CheckBox sequenceNumCheckBox;
    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    
	@Override
	public void afterPropertiesSet() throws Exception {  
		setHeight("200px");
        setWidth("700px");
        	
		crossNameOptionGroup = new OptionGroup();
		crossNameOptionGroup.addItem(messageSource.getMessage(Message.USE_DEFAULT_CROSS_NAME_FOR_ALL));
		crossNameOptionGroup.addItem(messageSource.getMessage(Message.SPECIFY_CROSS_NAME_TEMPLATE_FOR_ALL));
		addComponent(crossNameOptionGroup, "top:10px;left:20px");
		
		specifyPrefixLabel = new Label();
		prefixTextField = new TextField();
		prefixTextField.setWidth("500px");
		addComponent(specifyPrefixLabel, "top:80px;left:20px");
		addComponent(prefixTextField, "top:60px;left:180px");
		
		sequenceNumCheckBox = new CheckBox();
		addComponent(sequenceNumCheckBox, "top:95px;left:20px");
		
		howManyDigitsLabel = new Label();
		howManyDigitsLabel.setEnabled(false);
		addComponent(howManyDigitsLabel, "top:111px;left:400px");
		
		ListSelect digitsSelect = new ListSelect();
		digitsSelect.addItem("1");
		digitsSelect.addItem("2");
		digitsSelect.addItem("3");
		
		// TODO: find workaround (try IntStepper). Bandaid fix was to set multiselect true
		// to render up and down buttons on select field
		digitsSelect.setMultiSelect(true); 
		digitsSelect.setHeight("22px");
		digitsSelect.setEnabled(false);
		
		digitsSelect.select("3"); // TODO: remove initialization (in mockup only)
		addComponent(digitsSelect, "top:95px;left:520px");
		
		
		specifySuffixLabel = new Label();
		suffixTextField = new TextField();
		suffixTextField.setWidth("500px");
		addComponent(specifySuffixLabel, "top:150px;left:20px");
		addComponent(suffixTextField, "top:130px;left:180px");
		
		nextNameInSequenceLabel = new Label();
		addComponent(nextNameInSequenceLabel, "top:180px;left:20px");
	}
	
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
	@Override
	public void updateLabels() {
		messageSource.setCaption(specifyPrefixLabel, Message.SPECIFY_PREFIX_REQUIRED);
		messageSource.setCaption(specifySuffixLabel, Message.SPECIFY_SUFFIX_OPTIONAL);
		messageSource.setCaption(sequenceNumCheckBox, Message.SEQUENCE_NUMBER_SHOULD_HAVE_LEADING_ZEROS);
		messageSource.setCaption(howManyDigitsLabel, Message.HOW_MANY_DIGITS);
		messageSource.setCaption(nextNameInSequenceLabel, Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE);
	}

}

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

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
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
	public static final String GENERATE_BUTTON_ID = "Generate Next Name Id";

	private static final long serialVersionUID = -1197900610042529900L;
	private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsCrossNameComponent.class);
	private static final Integer MAX_LEADING_ZEROS = 10;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmManager;
	
    private Label specifyPrefixLabel;
    private Label specifySuffixLabel;
    private Label howManyDigitsLabel; 
    private Label nextNameInSequenceLabel;
    private Label generatedNameLabel;
    
    private OptionGroup crossNameOptionGroup;
    private TextField prefixTextField;
    private TextField suffixTextField;
    private CheckBox sequenceNumCheckBox;
    private Select leadingZerosSelect;
    private Button generateButton;

    private AbstractComponent[] digitsToggableComponents = new AbstractComponent[2];
    private AbstractComponent[] otherToggableComponents = new AbstractComponent[8];
    
    private enum CrossNameOption{
    	USE_DEFAULT, SPECIFY_CROSS_NAME
    };
    
    
	@Override
	public void afterPropertiesSet() throws Exception {  
		setHeight("240px");
        setWidth("700px");
        	
		initializeCrossNameOptionGroup();
		
		sequenceNumCheckBox = new CheckBox();
		sequenceNumCheckBox.setImmediate(true);
		sequenceNumCheckBox.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				enableSpecifyLeadingZerosComponents(sequenceNumCheckBox.booleanValue());
			}
		});
	
		specifyPrefixLabel = new Label();
		prefixTextField = new TextField();
		prefixTextField.setWidth("500px");
		
		howManyDigitsLabel = new Label();
		leadingZerosSelect = new Select();
		for (int i = 1; i <= MAX_LEADING_ZEROS; i++){
			leadingZerosSelect.addItem(Integer.valueOf(i));
		}
		leadingZerosSelect.select(Integer.valueOf(1));
		leadingZerosSelect.setWidth("50px");
		
		
		specifySuffixLabel = new Label();
		suffixTextField = new TextField();
		suffixTextField.setWidth("500px");
		
		nextNameInSequenceLabel = new Label();
		generatedNameLabel = new Label();
		
		generateButton = new Button();
		generateButton.setData(GENERATE_BUTTON_ID);
		generateButton.addListener(new CrossingManagerImportButtonClickListener(this));

		layoutComponents();
		initializeToggableComponents();
	}

	private void initializeCrossNameOptionGroup() {
		crossNameOptionGroup = new OptionGroup();
		crossNameOptionGroup.addItem(CrossNameOption.USE_DEFAULT);
		crossNameOptionGroup.setItemCaption(CrossNameOption.USE_DEFAULT, 
				messageSource.getMessage(Message.USE_DEFAULT_CROSS_NAME_FOR_ALL));
		crossNameOptionGroup.addItem(CrossNameOption.SPECIFY_CROSS_NAME);
		crossNameOptionGroup.setItemCaption(CrossNameOption.SPECIFY_CROSS_NAME, 
				messageSource.getMessage(Message.SPECIFY_CROSS_NAME_TEMPLATE_FOR_ALL));
		
		crossNameOptionGroup.setImmediate(true);
		crossNameOptionGroup.select(CrossNameOption.USE_DEFAULT); //first option selected by default
		crossNameOptionGroup.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				enableSpecifyCrossNameComponents(specifyCrossNameOptionSelected());
			}
		});
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
		messageSource.setCaption(generateButton, Message.GENERATE);
	}
    
    private void layoutComponents() {
		addComponent(crossNameOptionGroup, "top:10px;left:20px");
		addComponent(specifyPrefixLabel, "top:80px;left:20px");
		addComponent(prefixTextField, "top:60px;left:180px");
		addComponent(sequenceNumCheckBox, "top:97px;left:20px");
		addComponent(howManyDigitsLabel, "top:113px;left:400px");
		addComponent(leadingZerosSelect, "top:95px;left:520px");
		addComponent(specifySuffixLabel, "top:150px;left:20px");
		addComponent(suffixTextField, "top:130px;left:180px");
		addComponent(nextNameInSequenceLabel, "top:185px;left:20px");
		addComponent(generatedNameLabel, "top:185px;left:237px");
		addComponent(generateButton, "top:195px;left:20px");
	}
    
    private void initializeToggableComponents(){
    	digitsToggableComponents[0] = howManyDigitsLabel;
    	digitsToggableComponents[1] = leadingZerosSelect;
    	
    	otherToggableComponents[0] = specifyPrefixLabel;
    	otherToggableComponents[1] = specifySuffixLabel;
    	otherToggableComponents[2] = nextNameInSequenceLabel;
    	otherToggableComponents[3] = prefixTextField;
    	otherToggableComponents[4] = suffixTextField;
    	otherToggableComponents[5] = sequenceNumCheckBox;
    	otherToggableComponents[6] = generatedNameLabel;
    	otherToggableComponents[7] = generateButton;
    	
    	enableSpecifyCrossNameComponents(false);
    }
    
    // Enables / disables UI elements for specifying Cross Name details
	private void enableSpecifyCrossNameComponents(boolean enabled){
		for (AbstractComponent component : otherToggableComponents){
			component.setEnabled(enabled);
		}
		enableSpecifyLeadingZerosComponents(enabled && sequenceNumCheckBox.booleanValue());
	}
	
	private void enableSpecifyLeadingZerosComponents(boolean enabled){
		for (AbstractComponent component : digitsToggableComponents){
			component.setEnabled(enabled);
		}
	}
    
	// Action handler for Generation button
    public void generateNextNameButtonAction(){
    	String prefix = (String) prefixTextField.getValue();
    	String suffix = (String) suffixTextField.getValue();
    	prefix = prefix.trim();
    	suffix = suffix.trim();
    	
    	if (StringUtils.isEmpty(prefix)){
    		MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.ERROR_ENTER_PREFIX_FIRST), "");
    	
    	} else if (prefix.contains(" ")){
    		MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.ERROR_PREFIX_HAS_WHITESPACE), "");
        	
    	} else {
    		try {
    			prefix += getLeadingZeroesAsString();
    			StringBuilder sb = new StringBuilder();
    			sb.append(prefix);
    			sb.append(" ");
    			sb.append(germplasmManager.getNextSequenceNumberForCrossName(prefix));
    			if (!StringUtils.isEmpty(suffix)){
    				sb.append(" ");
    				sb.append(suffix);
    			}
    			generatedNameLabel.setCaption(sb.toString());
    			
    		} catch (MiddlewareQueryException e) {
    			LOG.error(e.toString() + "\n" + e.getStackTrace());
    			e.printStackTrace();
    			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
    					messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE));
    		}
    	}
    	
    }
    
    
    private String getLeadingZeroesAsString(){
    	StringBuilder sb = new StringBuilder();
    	if (sequenceNumCheckBox.booleanValue()){
    		Integer numOfZeros = (Integer) leadingZerosSelect.getValue();
    		for (int i = 0; i < numOfZeros; i++){
    			sb.append("0");
    		}
    	}
    	return sb.toString();
    }
    
    public boolean specifyCrossNameOptionSelected(){
    	CrossNameOption optionId = (CrossNameOption) crossNameOptionGroup.getValue();
    	return CrossNameOption.SPECIFY_CROSS_NAME.equals(optionId);
    }
    
	
}

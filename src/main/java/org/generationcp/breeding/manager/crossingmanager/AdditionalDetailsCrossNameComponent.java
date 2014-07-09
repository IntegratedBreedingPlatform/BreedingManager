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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * This class contains the absolute layout of UI elements in Cross Name section
 * in "Enter Additional Details..." tab in Crossing Manager application
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class AdditionalDetailsCrossNameComponent extends AbsoluteLayout 
        implements InitializingBean, InternationalizableComponent, CrossesMadeContainerUpdateListener{
    
    public static final String GENERATE_BUTTON_ID = "Generate Next Name Id";

    private static final long serialVersionUID = -1197900610042529900L;
    private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsCrossNameComponent.class);
    private static final Integer MAX_LEADING_ZEROS = 9;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmManager;
    
    private Label specifyPrefixLabel;
    private Label specifySuffixLabel;
    private Label howManyDigitsLabel; 
    private Label nextNameInSequenceLabel;
    private Label generatedNameLabel;
    private Label specifyStartNumberLabel;
    
    private TextField prefixTextField;
    private TextField suffixTextField;
    private TextField startNumberTextField;
    private CheckBox sequenceNumCheckBox;
    private CheckBox addSpaceCheckBox;
    private CheckBox addSpaceAfterSuffixCheckBox;
    private Select leadingZerosSelect;
    private Button generateButton;
    private Button okButton;
    private Button cancelButton;
    
    private AbstractComponent[] digitsToggableComponents = new AbstractComponent[2];
    private AbstractComponent[] otherToggableComponents = new AbstractComponent[9];

    private String lastPrefixUsed; //store prefix used for MW method including zeros, if any
    private Integer nextNumberInSequence;
    
    private CrossesMadeContainer container;
    
    private FillWith fillWithSource;
    private String propertyIdToFill;
    private boolean forFillWith = false;
    private Window parentWindow;
    
    public AdditionalDetailsCrossNameComponent(){
    	super();
    	this.forFillWith = false;
    }
    
    public AdditionalDetailsCrossNameComponent(FillWith fillWithSource, String propertyIdToFill, Window parentWindow){
    	super();
    	this.forFillWith = true;
    	this.fillWithSource = fillWithSource;
    	this.propertyIdToFill = propertyIdToFill;
    	this.parentWindow = parentWindow;
    }
    
    @SuppressWarnings("serial")
    @Override
    public void afterPropertiesSet() throws Exception {  
        setHeight("200px");
        setWidth("700px");
            
        sequenceNumCheckBox = new CheckBox();
        sequenceNumCheckBox.setImmediate(true);
        sequenceNumCheckBox.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                enableSpecifyLeadingZerosComponents(sequenceNumCheckBox.booleanValue());
            }
        });

        addSpaceCheckBox = new CheckBox();
        addSpaceCheckBox.setImmediate(true);
        
        specifyPrefixLabel = new Label();
        prefixTextField = new TextField();
        prefixTextField.setWidth("300px");
        
        howManyDigitsLabel = new Label();
        leadingZerosSelect = new Select();
        for (int i = 1; i <= MAX_LEADING_ZEROS; i++){
            leadingZerosSelect.addItem(Integer.valueOf(i));
        }
        leadingZerosSelect.setNullSelectionAllowed(false);
        leadingZerosSelect.select(Integer.valueOf(1));
        leadingZerosSelect.setWidth("50px");
        
        
        specifySuffixLabel = new Label();
        suffixTextField = new TextField();
        suffixTextField.setWidth("300px");
        
        nextNameInSequenceLabel = new Label();
        generatedNameLabel = new Label();
        
        generateButton = new Button();
        generateButton.setData(GENERATE_BUTTON_ID);
        generateButton.addListener(new CrossingManagerImportButtonClickListener(this));
        
        if(this.forFillWith){
        	setHeight("250px");
        	setWidth("490px");
        	specifyStartNumberLabel = new Label();
        	
        	startNumberTextField = new TextField();
        	startNumberTextField.setWidth("90px");
        	
        	addSpaceAfterSuffixCheckBox = new CheckBox();
        	addSpaceAfterSuffixCheckBox.setImmediate(true);
        	
        	cancelButton = new Button();
        	cancelButton.addListener(new Button.ClickListener() {
    			private static final long serialVersionUID = -3519880320817778816L;

    			@Override
    			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    				Window parent = parentWindow.getParent();
    				parent.removeWindow(parentWindow);
    			}
    		});

        	okButton = new Button();
        	okButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        	okButton.addListener(new Button.ClickListener() {
    			private static final long serialVersionUID = -3519880320817778816L;

    			@Override
    			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    				boolean spaceBetweenPrefixAndCode = addSpaceCheckBox.booleanValue();
    				boolean spaceBetweenSuffixAndCode = addSpaceAfterSuffixCheckBox.booleanValue();
    				
    				String prefix = null;
    				if(prefixTextField.getValue() == null || prefixTextField.getValue().toString().length() == 0){
    					MessageNotifier.showError(parentWindow, messageSource.getMessage(Message.INVALID_INPUT)
    							, messageSource.getMessage(Message.PLEASE_SPECIFY_A_PREFIX), Notification.POSITION_CENTERED);
    					return;
    				} else{
    					prefix = prefixTextField.getValue().toString().trim();
    				}
    				
    				String suffix = null;
    				if(suffixTextField.getValue() != null){
    					suffix = suffixTextField.getValue().toString().trim();
    				}
    				
    				int numOfZerosNeeded = 0;
    				boolean isNumOfZerosNeeded = sequenceNumCheckBox.booleanValue();
    				if(isNumOfZerosNeeded){
    					numOfZerosNeeded = ((Integer) leadingZerosSelect.getValue()).intValue();
    				}
    				
    				if(startNumberTextField.getValue() == null || startNumberTextField.getValue().toString().length() == 0){
    					MessageNotifier.showError(parentWindow, messageSource.getMessage(Message.INVALID_INPUT)
    							, messageSource.getMessage(Message.PLEASE_SPECIFY_A_STARTING_NUMBER), Notification.POSITION_CENTERED);
    					return;
    				} else if(startNumberTextField.getValue().toString().length() > 9){
    					MessageNotifier.showError(parentWindow, messageSource.getMessage(Message.INVALID_INPUT) 
    							, messageSource.getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS), Notification.POSITION_CENTERED);
    					return;
    				} else {
    					try{
    						Integer.parseInt(startNumberTextField.getValue().toString());
    					} catch(NumberFormatException ex){
    						MessageNotifier.showError(parentWindow, messageSource.getMessage(Message.INVALID_INPUT) 
    								, messageSource.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER), Notification.POSITION_CENTERED);
        					return;
    					}
    				}
    				int startNumber = Integer.parseInt(startNumberTextField.getValue().toString());
    				
    				int numberOfEntries = fillWithSource.getNumberOfEntries();
    				StringBuilder builder = new StringBuilder();
    	            builder.append(prefix);
    	            if(spaceBetweenPrefixAndCode){
    	            	builder.append(" ");
    	            }
    	            
    	            if(numOfZerosNeeded > 0){
    	                for (int i = 0; i < numOfZerosNeeded; i++){
    	                builder.append("0");
    	                }
    	            }
    	            int lastNumber = startNumber + numberOfEntries;
    	            builder.append(lastNumber);
    	           
    	            
    	            if(suffix != null && spaceBetweenSuffixAndCode){
    	            	builder.append(" ");
    	            }
    	            
    	            if(suffix != null){
    	            	builder.append(suffix);
    	            }
    	            
    	            if(propertyIdToFill.equals(ListDataTablePropertyID.SEED_SOURCE.getName()) && builder.toString().length() > 255){
    	            	MessageNotifier.showError(parentWindow, messageSource.getMessage(Message.INVALID_INPUT), 
    	            			messageSource.getMessage(Message.SEQUENCE_TOO_LONG_FOR_SEED_SOURCE), Notification.POSITION_CENTERED);
    					return;
    	            } else if(propertyIdToFill.equals(ListDataTablePropertyID.ENTRY_CODE.getName()) && builder.toString().length() > 47){
    	            	MessageNotifier.showError(parentWindow, messageSource.getMessage(Message.INVALID_INPUT), 
    	            			messageSource.getMessage(Message.SEQUENCE_TOO_LONG_FOR_ENTRY_CODE), Notification.POSITION_CENTERED);
    					return;
    	            }
    	            
    				fillWithSource.fillWithSequence(propertyIdToFill, prefix, suffix, startNumber, numOfZerosNeeded, spaceBetweenPrefixAndCode, spaceBetweenSuffixAndCode);
    				Window parent = parentWindow.getParent();
    				parent.removeWindow(parentWindow);
    			}
    		});

        }

        layoutComponents();
        initializeToggableComponents();
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
        messageSource.setCaption(sequenceNumCheckBox, Message.SEQUENCE_NUMBER_SHOULD_HAVE);
        messageSource.setCaption(addSpaceCheckBox, Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE);
        messageSource.setCaption(howManyDigitsLabel, Message.DIGITS);
        
        if(!this.forFillWith){
	        messageSource.setCaption(nextNameInSequenceLabel, Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE);
	        messageSource.setCaption(generateButton, Message.GENERATE);
        } else{
        	messageSource.setCaption(addSpaceAfterSuffixCheckBox, Message.ADD_SPACE_BETWEEN_SUFFIX_AND_CODE);
        	messageSource.setCaption(cancelButton, Message.CANCEL);
        	messageSource.setCaption(okButton, Message.OK);
        	messageSource.setCaption(specifyStartNumberLabel, Message.SPECIFY_START_NUMBER);
        }
    }
    
    private void layoutComponents() {
        if(!this.forFillWith){
        	addComponent(specifyPrefixLabel, "top:25px;left:0px");
            addComponent(prefixTextField, "top:6px;left:165px");
            addComponent(sequenceNumCheckBox, "top:37px;left:0px");
            addComponent(howManyDigitsLabel, "top:53px;left:289px");
            addComponent(leadingZerosSelect, "top:35px;left:235px");
            addComponent(addSpaceCheckBox, "top:65px;left:0px");
            addComponent(specifySuffixLabel, "top:115px;left:0px");
            addComponent(suffixTextField, "top:95px;left:165px");
        	addComponent(nextNameInSequenceLabel, "top:145px;left:0px");
            addComponent(generatedNameLabel, "top:145px;left:265px");
	        addComponent(generateButton, "top:155px;left:0px");
        } else{
        	addComponent(specifyPrefixLabel, "top:25px;left:10px");
            addComponent(prefixTextField, "top:6px;left:175px");
            addComponent(addSpaceCheckBox, "top:37px;left:10px");
            addComponent(specifyStartNumberLabel, "top:87px;left:10px");
        	addComponent(startNumberTextField, "top:67px;left:175px");
            addComponent(sequenceNumCheckBox, "top:100px;left:10px");
            addComponent(leadingZerosSelect, "top:98px;left:335px");
            addComponent(howManyDigitsLabel, "top:119px;left:389px");
            addComponent(specifySuffixLabel, "top:150px;left:10px");
            addComponent(suffixTextField, "top:130px;left:175px");
        	addComponent(addSpaceAfterSuffixCheckBox, "top:162px;left:10px");
        	
        	HorizontalLayout layoutButtonArea = new HorizontalLayout();
            layoutButtonArea.setSpacing(true);
            layoutButtonArea.addComponent(cancelButton);
            layoutButtonArea.addComponent(okButton);
            
            addComponent(layoutButtonArea, "top:205px; left:200px");
        }
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
        otherToggableComponents[6] = addSpaceCheckBox;
        otherToggableComponents[7] = generatedNameLabel;
        otherToggableComponents[8] = generateButton;
        
        enableSpecifyCrossNameComponents(true);
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
    
    // Action handler for generating cross names
    public void generateNextNameButtonAction(){
        if (validateCrossNameFields()) {
            String suffix = ((String) suffixTextField.getValue()).trim();
            
            try {
                lastPrefixUsed = buildPrefixString();
                String nextSequenceNumberString = this.germplasmManager.getNextSequenceNumberForCrossName(lastPrefixUsed.trim());
                
                nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
                generatedNameLabel.setCaption(buildNextNameInSequence(lastPrefixUsed, suffix, nextNumberInSequence));
                
            } catch (MiddlewareQueryException e) {
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                        messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE), Notification.POSITION_CENTERED);
            }
        }
        
        
    }
    
    private boolean validateCrossNameFields(){
        Window window = getWindow();
        String prefix = ((String) prefixTextField.getValue()).trim();
        
        if (StringUtils.isEmpty(prefix)){
            MessageNotifier.showError(window, messageSource.getMessage(Message.ERROR_WITH_CROSS_CODE), 
            		messageSource.getMessage(Message.ERROR_ENTER_PREFIX_FIRST), Notification.POSITION_CENTERED);
            return false;
        
//        } else if (prefix.contains(" ")){
//            MessageNotifier.showError(window, messageSource.getMessage(Message.ERROR_WITH_CROSS_CODE), 
//            		messageSource.getMessage(Message.ERROR_PREFIX_HAS_WHITESPACE), Notification.POSITION_CENTERED);
//            return false;
//        
//        } else if (prefix.substring(prefix.length()-1).matches("\\d")){
//            MessageNotifier.showError(window, messageSource.getMessage(Message.ERROR_WITH_CROSS_CODE),  
//            		messageSource.getMessage(Message.ERROR_PREFIX_ENDS_IN_NUMBER), Notification.POSITION_CENTERED);
//            return false;
        } 
        
        return true;
    }
    
    private boolean validateGeneratedName(){
        
        // if Generate button never pressed
        if (nextNumberInSequence == null ){
            MessageNotifier.showError(getWindow(), "Error with Cross Code", MessageFormat.format(
                    messageSource.getMessage(Message.ERROR_NEXT_NAME_MUST_BE_GENERATED_FIRST), ""
                    ), Notification.POSITION_CENTERED);
            return false;

        // if prefix specifications were changed and next name in sequence not generated first
        } else {
            String currentPrefixString = buildPrefixString();
            if (!currentPrefixString.equals(lastPrefixUsed)){
                MessageNotifier.showError(getWindow(), "Error with Cross Code", MessageFormat.format(
                        messageSource.getMessage(Message.ERROR_NEXT_NAME_MUST_BE_GENERATED_FIRST), " (" + currentPrefixString +")"
                        ), Notification.POSITION_CENTERED);
                return false;
            }
        }
        
        return true;
    }
    
    private String buildPrefixString(){
        if(addSpaceCheckBox.booleanValue()){
            return ((String) prefixTextField.getValue()).trim()+" ";
        }
        return ((String) prefixTextField.getValue()).trim();
    }

    private String buildNextNameInSequence(String prefix, String suffix,
            Integer number) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(getNumberWithLeadingZeroesAsString(number));
        if (!StringUtils.isEmpty(suffix)){
            sb.append(" ");
            sb.append(suffix);
        }
        return sb.toString();
    }
    
    
    private String getNumberWithLeadingZeroesAsString(Integer number){
        StringBuilder sb = new StringBuilder();
        String numberString = number.toString();
        if (sequenceNumCheckBox.booleanValue()){
            Integer numOfZeros = (Integer) leadingZerosSelect.getValue();
            int numOfZerosNeeded = numOfZeros - numberString.length();
            if(numOfZerosNeeded > 0){
                for (int i = 0; i < numOfZerosNeeded; i++){
                	sb.append("0");
                }
            }
        }
        sb.append(number);
        return sb.toString();
    }

    
    @Override
    public boolean updateCrossesMadeContainer(CrossesMadeContainer container) {
        
        if (this.container != null && this.container.getCrossesMade() != null && 
                this.container.getCrossesMade().getCrossesMap()!= null && validateCrossNameFields() 
                    && validateGeneratedName()) { 
            
            int ctr = nextNumberInSequence;
            String suffix = (String) suffixTextField.getValue();
            
            Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
            List<GermplasmListEntry> oldCrossNames = new ArrayList<GermplasmListEntry>();
            
            // Store old cross name and generate new names based on prefix, suffix specifications
            for (Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()){
                Name nameObject = entry.getValue();
                String oldCrossName = nameObject.getNval();
                nameObject.setNval(buildNextNameInSequence(lastPrefixUsed, suffix, ctr++));
                
                Germplasm germplasm = entry.getKey();
                Integer tempGid = germplasm.getGid();
                GermplasmListEntry oldNameEntry = new GermplasmListEntry(tempGid, tempGid, tempGid, oldCrossName);
                
                oldCrossNames.add(oldNameEntry);
            }
            // Only store the "original" cross names, would not store previous names on 2nd, 3rd, ... change
            if (this.container.getCrossesMade().getOldCrossNames()== null ||
                    this.container.getCrossesMade().getOldCrossNames().isEmpty()){
                this.container.getCrossesMade().setOldCrossNames(oldCrossNames);                
            }
            
            return true;
                
        }
        
        return false;
    }

    
}

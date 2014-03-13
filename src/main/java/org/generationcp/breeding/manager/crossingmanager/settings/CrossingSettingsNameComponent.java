package org.generationcp.breeding.manager.crossingmanager.settings;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.GenerateCrossNameAction;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;

@Configurable
public class CrossingSettingsNameComponent extends AbsoluteLayout implements
		BreedingManagerLayout, InternationalizableComponent,
		InitializingBean {

	public static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsNameComponent.class);
	private static final long serialVersionUID = 1887628092049615806L;
	private static final Integer MAX_LEADING_ZEROS = 10;
	private static final Integer MAX_PREFIX_SUFFIX_LENGTH = 12;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private Label specifyNamingConventionLabel;
    private Label crossNameLabel;
    private Label specifyPrefixLabel;
    private Label specifySuffixLabel;
    private Label digitsLabel; 
    private Label addSpaceLabel;
    private Label nextNameInSequenceLabel;
    private Label generatedNextNameLabel;
   
    private TextField prefixTextField;
    private TextField suffixTextField;
    private CheckBox sequenceNumCheckBox;
    private OptionGroup addSpaceOptionGroup;
    private Select leadingZerosSelect;
    private CheckBox specifyStartNumberCheckbox;
    private TextField startNumberTextField;
    
    private AbstractComponent[] digitsToggableComponents = new AbstractComponent[2];
    
    private final class CrossNameFieldsValueChangeListener implements
			Property.ValueChangeListener {
		private static final long serialVersionUID = -8395381042668695941L;

		@Override
		public void valueChange(ValueChangeEvent event) {
			generateNextNameAction();
		}
	}

	public enum AddSpaceBetPrefixAndCodeOption{
        YES, NO
    };
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

	@Override
	public void updateLabels() {
		crossNameLabel.setValue(messageSource.getMessage(Message.NAMING).toUpperCase());
		specifyNamingConventionLabel.setValue(messageSource.getMessage(Message.SPECIFY_NAMING_CONVENTION_FOR_CROSSES));
		specifyPrefixLabel.setValue(messageSource.getMessage(Message.CROSS_NAME_PREFIX) + ": *");
		specifySuffixLabel.setValue(messageSource.getMessage(Message.SUFFIX_OPTIONAL) + ":");
		addSpaceLabel.setValue(messageSource.getMessage(Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE) + "?");
		digitsLabel.setValue(messageSource.getMessage(Message.DIGITS));
		
        messageSource.setCaption(sequenceNumCheckBox, Message.SEQUENCE_NUMBER_SHOULD_HAVE);
        specifyStartNumberCheckbox.setCaption(messageSource.getMessage(Message.SPECIFY_DIFFERENT_STARTING_SEQUENCE_NUMBER) + ":");
	}

	@Override
	public void instantiateComponents() {  
		crossNameLabel = new Label();
		crossNameLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		specifyNamingConventionLabel = new Label();
		
        sequenceNumCheckBox = new CheckBox();
        sequenceNumCheckBox.setImmediate(true);

        addSpaceLabel = new Label();
        addSpaceOptionGroup = new OptionGroup();
        addSpaceOptionGroup.setImmediate(true);
        addSpaceOptionGroup.addStyleName(AppConstants.CssStyles.HORIZONTAL_GROUP);
        
        specifyPrefixLabel = new Label();
        prefixTextField = new TextField();
        prefixTextField.setImmediate(true);
        prefixTextField.setWidth("120px");
        prefixTextField.setMaxLength(MAX_PREFIX_SUFFIX_LENGTH);
        
        digitsLabel = new Label();
        
        leadingZerosSelect = new Select();
        leadingZerosSelect.setImmediate(true);
        leadingZerosSelect.setNullSelectionAllowed(false);
        leadingZerosSelect.select(Integer.valueOf(1));
        leadingZerosSelect.setWidth("50px");
        
        specifySuffixLabel = new Label();
        suffixTextField = new TextField();
        suffixTextField.setImmediate(true);
        suffixTextField.setWidth("120px");
        suffixTextField.setMaxLength(MAX_PREFIX_SUFFIX_LENGTH);
        
        nextNameInSequenceLabel = new Label("<b>" +messageSource.getMessage(Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE) 
				+ ": </b>", Label.CONTENT_XHTML);
        generatedNextNameLabel = new Label();
        
        specifyStartNumberCheckbox = new CheckBox();
        specifyStartNumberCheckbox.setImmediate(true);
        startNumberTextField = new TextField();
        startNumberTextField.setWidth("70px");
        startNumberTextField.setImmediate(true);
	}

	@Override
	public void initializeValues() {
        digitsToggableComponents[0] = digitsLabel;
        digitsToggableComponents[1] = leadingZerosSelect;
              
        for (int i = 1; i <= MAX_LEADING_ZEROS; i++){
            leadingZerosSelect.addItem(Integer.valueOf(i));
        }
        leadingZerosSelect.select(1);

        // Add space option group 
        String yes = messageSource.getMessage(Message.YES);
        String no = messageSource.getMessage(Message.NO);
        addSpaceOptionGroup.addItem(AddSpaceBetPrefixAndCodeOption.YES);
        addSpaceOptionGroup.setItemCaption(AddSpaceBetPrefixAndCodeOption.YES, yes);
        addSpaceOptionGroup.addItem(AddSpaceBetPrefixAndCodeOption.NO);
        addSpaceOptionGroup.setItemCaption(AddSpaceBetPrefixAndCodeOption.NO, no);

        setFieldsDefaultValue();
	}

	@Override
	public void addListeners() {
        sequenceNumCheckBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -7760859627667025586L;

			@Override
            public void valueChange(ValueChangeEvent event) {
                enableSpecifyLeadingZerosComponents(sequenceNumCheckBox.booleanValue());
            }
        });
        
		specifyStartNumberCheckbox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				startNumberTextField.setEnabled((Boolean) event.getProperty().getValue());
				
			}
		});
		
		// generate new cross name when any of the fields change
		prefixTextField.addListener(new CrossNameFieldsValueChangeListener());
		suffixTextField.addListener(new CrossNameFieldsValueChangeListener());
		addSpaceOptionGroup.addListener(new CrossNameFieldsValueChangeListener());
		leadingZerosSelect.addListener(new CrossNameFieldsValueChangeListener());
		startNumberTextField.addListener(new CrossNameFieldsValueChangeListener());
		specifyStartNumberCheckbox.addListener(new CrossNameFieldsValueChangeListener());

	}

	@Override
	public void layoutComponents() {
		addComponent(crossNameLabel, "top:0px; left:0px");
		addComponent(specifyNamingConventionLabel, "top:26px; left:0px");
			
		addComponent(specifyPrefixLabel, "top:71px;left:0px");
        addComponent(prefixTextField, "top:71px;left:145px");
        addComponent(sequenceNumCheckBox, "top:75px;left:300px");
        addComponent(leadingZerosSelect, "top:75px;left:540px");
        addComponent(digitsLabel, "top:75px;left:595px");
        
        addComponent(specifySuffixLabel, "top:102px;left:0px");
        addComponent(suffixTextField, "top:102px;left:145px");
        addComponent(addSpaceLabel, "top:105px;left:305px");
        addComponent(addSpaceOptionGroup, "top:105px;left:530px");
        
        addComponent(nextNameInSequenceLabel, "top:135px;left:0px");
        addComponent(generatedNextNameLabel, "top:135px;left:270px");
        
        addComponent(specifyStartNumberCheckbox, "top:162px;left:0px");
        addComponent(startNumberTextField, "top:162px;left:305px");
	}
	
	public boolean validateCrossNameFields(){
        String prefix = ((String) prefixTextField.getValue()).trim();
        
        if (!StringUtils.isEmpty(prefix)){
        	// do not show error if no number field shown
            if (!validateStartNumberField(false)){
            	return false;
            }
            
        } else {
        	generatedNextNameLabel.setValue("");
        	return false;
        }
        
        
        return true;
    }
	
    
    private void enableSpecifyLeadingZerosComponents(boolean enabled){
        for (AbstractComponent component : digitsToggableComponents){
            component.setEnabled(enabled);
        }
    }

	public boolean validateInputFields(){
		String prefix = (String) prefixTextField.getValue();
		if(prefix == null || prefix.trim().length() == 0){
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
					, messageSource.getMessage(Message.PLEASE_SPECIFY_A_PREFIX), Notification.POSITION_CENTERED);
			return false;
		}
		
		if (!validateStartNumberField(true)){
			return false;
		}
		
		return true;
	}

	/**
	 * Validate value for cross name start number
	 * @param isRequired - flag whether error should be raised if no number raised. 
	 * 		this is set to true only when saving / proceeding to next wizard step.
	 * @return
	 */
	private boolean validateStartNumberField(boolean isRequired) {
		String startNumberString = startNumberTextField.getValue().toString();
		
		if (doSpecifyNameStartNumber()){
			if (!StringUtils.isEmpty(startNumberString)){
				if(startNumberString.length() > 10){
					MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT) 
							, messageSource.getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS), Notification.POSITION_CENTERED);
					return false;
				} 
				if (!NumberUtils.isDigits(startNumberString)){
					MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT) 
							, messageSource.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER), Notification.POSITION_CENTERED);
					return false;
				}
				
			} else {
				if (isRequired){
					MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
							, messageSource.getMessage(Message.PLEASE_SPECIFY_A_STARTING_NUMBER), Notification.POSITION_CENTERED);
				}
				return false;
			}
		}
		
		return true;
	}

	public void setFields(CrossNameSetting crossNameSetting) {
		prefixTextField.setValue(crossNameSetting.getPrefix());
		
		if(crossNameSetting.isAddSpaceBetweenPrefixAndCode()){
			addSpaceOptionGroup.select(AddSpaceBetPrefixAndCodeOption.YES);
		}
		else{
			addSpaceOptionGroup.select(AddSpaceBetPrefixAndCodeOption.NO);
		}
		
		if(crossNameSetting.getNumOfDigits() != null
		        && crossNameSetting.getNumOfDigits() > 0){
			sequenceNumCheckBox.setValue(true);
			leadingZerosSelect.select(crossNameSetting.getNumOfDigits());
		}
		else{
			sequenceNumCheckBox.setValue(false);
		}
		enableSpecifyLeadingZerosComponents(sequenceNumCheckBox.booleanValue());
		
		String suffix = crossNameSetting.getSuffix();
		if (suffix == null) {
		    suffix = "";
		}
		suffixTextField.setValue(suffix);
	}

	// #####
	private void generateNextNameAction(){
        if (validateCrossNameFields()) {
            try {
        		GenerateCrossNameAction generateAction = new GenerateCrossNameAction();
                generatedNextNameLabel.setValue(generateAction.getNextNameInSequence(getCrossNameSettingObject()));
                
            } catch (MiddlewareQueryException e) {
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                        messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE), Notification.POSITION_CENTERED);
            }
        }
    }
	
    
    public boolean doAddSpaceBetPrefixAndCode() {
		return AddSpaceBetPrefixAndCodeOption.YES.equals(addSpaceOptionGroup.getValue());
	}
    
    
	public void setFieldsDefaultValue() {
		prefixTextField.setValue("");
		addSpaceOptionGroup.select(AddSpaceBetPrefixAndCodeOption.NO);
		
		sequenceNumCheckBox.setValue(false);
		leadingZerosSelect.select(null);
		enableSpecifyLeadingZerosComponents(sequenceNumCheckBox.booleanValue());
		suffixTextField.setValue("");
		generatedNextNameLabel.setValue("");
		
		specifyStartNumberCheckbox.setValidationVisible(false);
		startNumberTextField.setValue("");
		startNumberTextField.setEnabled(false);
	}
	
	private boolean doSpecifyNameStartNumber(){
		return (Boolean)specifyStartNumberCheckbox.getValue();
	}
	
	public CrossNameSetting getCrossNameSettingObject(){
		String prefix = (String) prefixTextField.getValue();
		String suffix = (String) suffixTextField.getValue();
		if(suffix != null){
			suffix = suffix.trim();
		}
		if (suffix.length() == 0) {
		    suffix = null; //set as null so attribute will not be marshalled
		}
		boolean addSpaceBetweenPrefixAndCode = doAddSpaceBetPrefixAndCode();
		Integer numOfDigits = null;
		if(sequenceNumCheckBox.booleanValue()){
			numOfDigits = (Integer) leadingZerosSelect.getValue();
		}
		CrossNameSetting crossNameSettingPojo = new CrossNameSetting(prefix.trim(), suffix
				, addSpaceBetweenPrefixAndCode, numOfDigits);
		String startNumber = (String) startNumberTextField.getValue();
		if (doSpecifyNameStartNumber() && !startNumber.isEmpty() && NumberUtils.isDigits(startNumber)){
			crossNameSettingPojo.setStartNumber(Integer.parseInt(startNumber));
		}
		
		return crossNameSettingPojo;
	}
}

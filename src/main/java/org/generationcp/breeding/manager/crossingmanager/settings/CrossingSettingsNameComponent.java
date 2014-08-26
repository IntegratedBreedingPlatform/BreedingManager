package org.generationcp.breeding.manager.crossingmanager.settings;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.actions.GenerateCrossNameAction;
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
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;

@Configurable
public class CrossingSettingsNameComponent extends CssLayout implements
		BreedingManagerLayout, InternationalizableComponent,
		InitializingBean {

	private static final int SEPARATOR_MAX_CHARS_LENGTH = 3;
	private static final int STARTING_NUM_MAX_CHARS_LENGTH = 9;
	public static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsNameComponent.class);
	private static final long serialVersionUID = 1887628092049615806L;
	private static final Integer MAX_LEADING_ZEROS = 9;
	private static final Integer MAX_PREFIX_SUFFIX_LENGTH = 12;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;

	private Label namingLabel;
	private Label namingDescLabel;

	private OptionGroup addSpaceBetPrefixAndCodeOptionGroup;
	private OptionGroup addSpaceBetSuffixAndCodeOptionGroup;
	
	private TextField crossNamePrefix;
    private TextField crossNameSuffix;

    private Select leadingZerosSelect;

    private TextField startNumberTextField;

    private TextField separatorTextField;

    private TextField generatedNextName;
    private TextField generatedExampleParentage;

    public enum AddSpaceOption{
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
		namingLabel.setValue(messageSource.getMessage(Message.NAMING).toUpperCase());
		namingDescLabel.setValue(messageSource.getMessage(Message.SPECIFY_NAMING_CONVENTION_FOR_CROSSES));
	}

	@Override
	public void instantiateComponents() {
		namingLabel = new Label(messageSource.getMessage(Message.NAMING).toUpperCase());
		namingLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		namingDescLabel = new Label(messageSource.getMessage(Message.SPECIFY_NAMING_CONVENTION_FOR_CROSSES));
		namingDescLabel.addStyleName("gcp-content-help-text");

		addSpaceBetPrefixAndCodeOptionGroup = new OptionGroup(messageSource.getMessage(Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE));
        addSpaceBetPrefixAndCodeOptionGroup.setImmediate(true);
        
        addSpaceBetSuffixAndCodeOptionGroup = new OptionGroup(messageSource.getMessage(Message.ADD_SPACE_BETWEEN_SUFFIX_AND_CODE));
        addSpaceBetSuffixAndCodeOptionGroup.setImmediate(true);
        
        crossNamePrefix = new TextField(messageSource.getMessage(Message.CROSS_NAME_PREFIX));
        crossNamePrefix.setImmediate(true);
        crossNamePrefix.setMaxLength(MAX_PREFIX_SUFFIX_LENGTH);
        crossNamePrefix.addStyleName("mandatory-field");

        crossNameSuffix = new TextField(messageSource.getMessage(Message.SUFFIX_OPTIONAL));
        crossNameSuffix.setImmediate(true);
        crossNameSuffix.setMaxLength(MAX_PREFIX_SUFFIX_LENGTH);

        leadingZerosSelect = new Select(messageSource.getMessage(Message.SEQUENCE_NUMBER_SHOULD_HAVE));
        leadingZerosSelect.setImmediate(true);
        leadingZerosSelect.setNullSelectionAllowed(true);

        startNumberTextField = new TextField(messageSource.getMessage(Message.SPECIFY_DIFFERENT_STARTING_SEQUENCE_NUMBER));
        startNumberTextField.setImmediate(true);
        startNumberTextField.setMaxLength(STARTING_NUM_MAX_CHARS_LENGTH);

        separatorTextField = new TextField(messageSource.getMessage(Message.SEPARATOR_FOR_PARENTAGE_DESIGNATION));
        separatorTextField.setImmediate(true);
        separatorTextField.setMaxLength(SEPARATOR_MAX_CHARS_LENGTH);
        separatorTextField.addStyleName("mandatory-field");

        generatedNextName = new TextField(messageSource.getMessage(Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE));
        generatedNextName.setReadOnly(true);

        generatedExampleParentage = new TextField(messageSource.getMessage(Message.GENERATED_PARENT_DESIGNATION));
        generatedExampleParentage.setReadOnly(true);
	}

	@Override
	public void initializeValues() {

		for (int i = 1; i <= MAX_LEADING_ZEROS; i++){
            leadingZerosSelect.addItem(Integer.valueOf(i));
        }
        leadingZerosSelect.select(1);

        // Add space option group
        final String yes = messageSource.getMessage(Message.YES);
        final String no = messageSource.getMessage(Message.NO);
        
        addSpaceBetPrefixAndCodeOptionGroup.addItem(AddSpaceOption.YES);
        addSpaceBetPrefixAndCodeOptionGroup.setItemCaption(AddSpaceOption.YES, yes);
        addSpaceBetPrefixAndCodeOptionGroup.addItem(AddSpaceOption.NO);
        addSpaceBetPrefixAndCodeOptionGroup.setItemCaption(AddSpaceOption.NO, no);
        
        addSpaceBetSuffixAndCodeOptionGroup.addItem(AddSpaceOption.YES);
        addSpaceBetSuffixAndCodeOptionGroup.setItemCaption(AddSpaceOption.YES, yes);
        addSpaceBetSuffixAndCodeOptionGroup.addItem(AddSpaceOption.NO);
        addSpaceBetSuffixAndCodeOptionGroup.setItemCaption(AddSpaceOption.NO, no);

        setFieldsDefaultValue();
	}

	@Override
	public void addListeners() {
		separatorTextField.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -8395381042668695941L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateDesignationExample();
			}
		});

		// Generate new cross name when any of the fields change
		crossNamePrefix.addListener(new CrossNameFieldsValueChangeListener());
		crossNameSuffix.addListener(new CrossNameFieldsValueChangeListener());
		addSpaceBetPrefixAndCodeOptionGroup.addListener(new CrossNameFieldsValueChangeListener());
		addSpaceBetSuffixAndCodeOptionGroup.addListener(new CrossNameFieldsValueChangeListener());
		leadingZerosSelect.addListener(new CrossNameFieldsValueChangeListener());
		startNumberTextField.addListener(new CrossNameFieldsValueChangeListener());
	}

	@Override
	public void layoutComponents() {

		final FormLayout formFields = new FormLayout();

		formFields.addComponent(crossNamePrefix);
		formFields.addComponent(crossNameSuffix);
		formFields.addComponent(addSpaceBetPrefixAndCodeOptionGroup);
		formFields.addComponent(addSpaceBetSuffixAndCodeOptionGroup);
		formFields.addComponent(leadingZerosSelect);
		formFields.addComponent(startNumberTextField);
		formFields.addComponent(separatorTextField);
		formFields.addComponent(generatedNextName);
		formFields.addComponent(generatedExampleParentage);

		addComponent(namingLabel);
		addComponent(namingDescLabel);
		addComponent(formFields);
	}

    public CrossNameSetting getCrossNameSettingObject() {

		final String prefix = (String) crossNamePrefix.getValue();
		String suffix = (String) crossNameSuffix.getValue();

		if (suffix != null){
			suffix = suffix.trim();
		}
		if (suffix.length() == 0) {
		    suffix = null; //set as null so attribute will not be marshalled
		}

		final boolean addSpaceBetweenPrefixAndCode = AddSpaceOption.YES.equals(addSpaceBetPrefixAndCodeOptionGroup.getValue());
		final boolean addSpaceBetweenSuffixAndCode = AddSpaceOption.YES.equals(addSpaceBetSuffixAndCodeOptionGroup.getValue());
		final Integer numOfDigits = leadingZerosSelect.getValue() == null ? null : (Integer) leadingZerosSelect.getValue();

		final String separator = (String) separatorTextField.getValue();
		final CrossNameSetting crossNameSettingPojo = new CrossNameSetting(prefix.trim(), suffix, addSpaceBetweenPrefixAndCode, addSpaceBetweenSuffixAndCode, numOfDigits, separator);
		final String startNumber = (String) startNumberTextField.getValue();

		if (!startNumber.isEmpty() && NumberUtils.isDigits(startNumber)){
			crossNameSettingPojo.setStartNumber(Integer.parseInt(startNumber));
		}

		return crossNameSettingPojo;
	}

	public void setFields(CrossNameSetting crossNameSetting) {
		crossNamePrefix.setValue(crossNameSetting.getPrefix());

		if(crossNameSetting.isAddSpaceBetweenPrefixAndCode()){
			addSpaceBetPrefixAndCodeOptionGroup.select(AddSpaceOption.YES);
		}
		else{
			addSpaceBetPrefixAndCodeOptionGroup.select(AddSpaceOption.NO);
		}
		
		if(crossNameSetting.isAddSpaceBetweenSuffixAndCode()){
			addSpaceBetSuffixAndCodeOptionGroup.select(AddSpaceOption.YES);
		}
		else{
			addSpaceBetSuffixAndCodeOptionGroup.select(AddSpaceOption.NO);
		}

		if(crossNameSetting.getNumOfDigits() != null
		        && crossNameSetting.getNumOfDigits() > 0){
			leadingZerosSelect.select(crossNameSetting.getNumOfDigits());
		}
		else{
			leadingZerosSelect.select(null);
		}

		String suffix = crossNameSetting.getSuffix();
		if (suffix == null) {
		    suffix = "";
		}
		crossNameSuffix.setValue(suffix);

		separatorTextField.setValue(crossNameSetting.getSeparator());
	}

    public void setFieldsDefaultValue() {
		crossNamePrefix.setValue("");
		crossNameSuffix.setValue("");
		addSpaceBetPrefixAndCodeOptionGroup.select(AddSpaceOption.NO);
		addSpaceBetSuffixAndCodeOptionGroup.select(AddSpaceOption.NO);
		leadingZerosSelect.select(null);
		startNumberTextField.setValue("");
		separatorTextField.setValue(CrossNameSetting.DEFAULT_SEPARATOR);
		updateNextNameInSequence("");
		updateDesignationExample();
	}

	public boolean validateInputFields(){

		final String prefix = (String) crossNamePrefix.getValue();

		if (prefix == null || prefix.trim().length() == 0) {
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
					, messageSource.getMessage(Message.PLEASE_SPECIFY_A_PREFIX));
			return false;
		}
		return validateStartNumberField();
	}

    private final class CrossNameFieldsValueChangeListener implements
			Property.ValueChangeListener {
		private static final long serialVersionUID = -8395381042668695941L;

		@Override
		public void valueChange(ValueChangeEvent event) {
			generateNextNameAction();
		}
	}

	private void generateNextNameAction(){
        if (validateCrossNameFields()) {
            try {
        		final GenerateCrossNameAction generateAction = new GenerateCrossNameAction();
                updateNextNameInSequence(generateAction.getNextNameInSequence(getCrossNameSettingObject()));

            } catch (MiddlewareQueryException e) {
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                        messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE));
            }
        } else {
        	updateNextNameInSequence("");
        }
    }

	private boolean validateCrossNameFields() {
		final String prefix = ((String) crossNamePrefix.getValue()).trim();
        return !StringUtils.isEmpty(prefix) && validateStartNumberField();
    }

	private boolean validateStartNumberField() {
		final String startNumberString = startNumberTextField.getValue().toString();

		if (!StringUtils.isEmpty(startNumberString)){
			if(startNumberString.length() > 10){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
						, messageSource.getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS));
				return false;
			}
			if (!NumberUtils.isDigits(startNumberString)){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
						, messageSource.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER));
				return false;
			}

		}
		return true;
	}

	private void updateNextNameInSequence(final String updatedName) {
		generatedNextName.setReadOnly(false);
		generatedNextName.setValue(updatedName);
		generatedNextName.setReadOnly(true);
	}

	private void updateDesignationExample() {
		final String female = "FEMALE-123";
		final String male = "MALE-456";
		String separator = separatorTextField.getValue().toString();

		if (StringUtils.isEmpty(separator)){
			separatorTextField.setValue(CrossNameSetting.DEFAULT_SEPARATOR);
			separator = CrossNameSetting.DEFAULT_SEPARATOR;
		}
		generatedExampleParentage.setReadOnly(false);
		generatedExampleParentage.setValue(female + separator + male);
		generatedExampleParentage.setReadOnly(true);

	}
}

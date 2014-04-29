package org.generationcp.breeding.manager.crossingmanager.settings;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
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
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;

@Configurable
public class CrossingSettingsNameComponent extends CssLayout implements
		BreedingManagerLayout, InternationalizableComponent,
		InitializingBean {

	private static final int SEPARATOR_MAX_CHARS_LENGTH = 3;
	public static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsNameComponent.class);
	private static final long serialVersionUID = 1887628092049615806L;
	private static final Integer MAX_LEADING_ZEROS = 9;
	private static final Integer MAX_PREFIX_SUFFIX_LENGTH = 12;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;

	private Label namingLabel;
	private Label namingDescLabel;

	private OptionGroup addSpaceOptionGroup;

	private TextField crossNamePrefix;
    private TextField crossNameSuffix;

    private Select leadingZerosSelect;

    private TextField startNumberTextField;

    private TextField separatorTextField;

    private TextField generatedNextName;
    private TextField generatedExampleParentage;

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
		namingLabel.setValue(messageSource.getMessage(Message.NAMING).toUpperCase());
		namingDescLabel.setValue(messageSource.getMessage(Message.SPECIFY_NAMING_CONVENTION_FOR_CROSSES));
	}

	@Override
	public void instantiateComponents() {
		namingLabel = new Label(messageSource.getMessage(Message.NAMING).toUpperCase());
		namingLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		namingDescLabel = new Label(messageSource.getMessage(Message.SPECIFY_NAMING_CONVENTION_FOR_CROSSES));
		namingDescLabel.addStyleName("gcp-content-help-text");

		addSpaceOptionGroup = new OptionGroup(messageSource.getMessage(Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE));
        addSpaceOptionGroup.setImmediate(true);

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
        addSpaceOptionGroup.addItem(AddSpaceBetPrefixAndCodeOption.YES);
        addSpaceOptionGroup.setItemCaption(AddSpaceBetPrefixAndCodeOption.YES, yes);
        addSpaceOptionGroup.addItem(AddSpaceBetPrefixAndCodeOption.NO);
        addSpaceOptionGroup.setItemCaption(AddSpaceBetPrefixAndCodeOption.NO, no);

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
		addSpaceOptionGroup.addListener(new CrossNameFieldsValueChangeListener());
		leadingZerosSelect.addListener(new CrossNameFieldsValueChangeListener());
		startNumberTextField.addListener(new CrossNameFieldsValueChangeListener());
	}

	@Override
	public void layoutComponents() {

		final FormLayout formFields = new FormLayout();

		formFields.addComponent(crossNamePrefix);
		formFields.addComponent(crossNameSuffix);
		formFields.addComponent(addSpaceOptionGroup);
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

		final boolean addSpaceBetweenPrefixAndCode = AddSpaceBetPrefixAndCodeOption.YES.equals(addSpaceOptionGroup.getValue());
		final Integer numOfDigits = leadingZerosSelect.getValue() == null ? null : (Integer) leadingZerosSelect.getValue();

		final String separator = (String) separatorTextField.getValue();
		final CrossNameSetting crossNameSettingPojo = new CrossNameSetting(prefix.trim(), suffix, addSpaceBetweenPrefixAndCode, numOfDigits, separator);
		final String startNumber = (String) startNumberTextField.getValue();

		if (!startNumber.isEmpty() && NumberUtils.isDigits(startNumber)){
			crossNameSettingPojo.setStartNumber(Integer.parseInt(startNumber));
		}

		return crossNameSettingPojo;
	}

	public void setFields(CrossNameSetting crossNameSetting) {
		crossNamePrefix.setValue(crossNameSetting.getPrefix());

		if(crossNameSetting.isAddSpaceBetweenPrefixAndCode()){
			addSpaceOptionGroup.select(AddSpaceBetPrefixAndCodeOption.YES);
		}
		else{
			addSpaceOptionGroup.select(AddSpaceBetPrefixAndCodeOption.NO);
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
		addSpaceOptionGroup.select(AddSpaceBetPrefixAndCodeOption.NO);
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
					, messageSource.getMessage(Message.PLEASE_SPECIFY_A_PREFIX), Notification.POSITION_CENTERED);
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
                        messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE), Notification.POSITION_CENTERED);
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
						, messageSource.getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS), Notification.POSITION_CENTERED);
				return false;
			}
			if (!NumberUtils.isDigits(startNumberString)){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
						, messageSource.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER), Notification.POSITION_CENTERED);
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

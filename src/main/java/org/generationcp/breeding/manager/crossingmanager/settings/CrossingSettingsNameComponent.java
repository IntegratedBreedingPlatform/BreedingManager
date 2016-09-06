
package org.generationcp.breeding.manager.crossingmanager.settings;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ConfirmOption;
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
public class CrossingSettingsNameComponent extends CssLayout implements BreedingManagerLayout, InternationalizableComponent,
		InitializingBean {

	private static final int SEPARATOR_MAX_CHARS_LENGTH = 3;
	private static final int STARTING_NUM_MAX_CHARS_LENGTH = 9;
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsNameComponent.class);
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

	private OptionGroup saveParentageDesignationAsAStringGroup;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.namingLabel.setValue(this.messageSource.getMessage(Message.NAMING).toUpperCase());
		this.namingDescLabel.setValue(this.messageSource.getMessage(Message.SPECIFY_NAMING_CONVENTION_FOR_CROSSES));
	}

	@Override
	public void instantiateComponents() {

		this.namingLabel = new Label(this.messageSource.getMessage(Message.NAMING).toUpperCase());
		this.namingLabel.setDebugId("namingLabel");
		this.namingLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		this.namingDescLabel = new Label(this.messageSource.getMessage(Message.SPECIFY_NAMING_CONVENTION_FOR_CROSSES));
		this.namingDescLabel.setDebugId("namingDescLabel");
		this.namingDescLabel.addStyleName("gcp-content-help-text");

		this.addSpaceBetPrefixAndCodeOptionGroup =
				new OptionGroup(this.messageSource.getMessage(Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE));
		this.addSpaceBetPrefixAndCodeOptionGroup.setImmediate(true);

		this.addSpaceBetSuffixAndCodeOptionGroup =
				new OptionGroup(this.messageSource.getMessage(Message.ADD_SPACE_BETWEEN_SUFFIX_AND_CODE));
		this.addSpaceBetSuffixAndCodeOptionGroup.setImmediate(true);

		this.crossNamePrefix = new TextField(this.messageSource.getMessage(Message.CROSS_NAME_PREFIX) + ":");
		this.crossNamePrefix.setDebugId("crossNamePrefix");
		this.crossNamePrefix.setImmediate(true);
		this.crossNamePrefix.setMaxLength(CrossingSettingsNameComponent.MAX_PREFIX_SUFFIX_LENGTH);
		this.crossNamePrefix.addStyleName("mandatory-field");

		this.crossNameSuffix = new TextField(this.messageSource.getMessage(Message.SUFFIX_OPTIONAL) + ":");
		this.crossNameSuffix.setDebugId("crossNameSuffix");
		this.crossNameSuffix.setImmediate(true);
		this.crossNameSuffix.setMaxLength(CrossingSettingsNameComponent.MAX_PREFIX_SUFFIX_LENGTH);

		this.leadingZerosSelect = new Select(this.messageSource.getMessage(Message.SEQUENCE_NUMBER_SHOULD_HAVE) + ":");
		this.leadingZerosSelect.setDebugId("leadingZerosSelect");
		this.leadingZerosSelect.setImmediate(true);
		this.leadingZerosSelect.setNullSelectionAllowed(true);

		this.startNumberTextField = new TextField(this.messageSource.getMessage(Message.SPECIFY_DIFFERENT_STARTING_SEQUENCE_NUMBER) + ":");
		this.startNumberTextField.setDebugId("startNumberTextField");
		this.startNumberTextField.setImmediate(true);
		this.startNumberTextField.setMaxLength(CrossingSettingsNameComponent.STARTING_NUM_MAX_CHARS_LENGTH);

		this.separatorTextField = new TextField(this.messageSource.getMessage(Message.SEPARATOR_FOR_PARENTAGE_DESIGNATION) + ":");
		this.separatorTextField.setDebugId("separatorTextField");
		this.separatorTextField.setImmediate(true);
		this.separatorTextField.setMaxLength(CrossingSettingsNameComponent.SEPARATOR_MAX_CHARS_LENGTH);
		this.separatorTextField.addStyleName("mandatory-field");

		this.generatedNextName = new TextField(this.messageSource.getMessage(Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE) + ":");
		this.generatedNextName.setDebugId("generatedNextName");
		this.generatedNextName.setReadOnly(true);

		this.generatedExampleParentage = new TextField(this.messageSource.getMessage(Message.GENERATED_PARENT_DESIGNATION) + ":");
		this.generatedExampleParentage.setDebugId("generatedExampleParentage");
		this.generatedExampleParentage.setReadOnly(true);

		this.saveParentageDesignationAsAStringGroup =
				new OptionGroup(this.messageSource.getMessage(Message.SAVE_PARENTAGE_DESIGNATION_AS_STRING));
		this.saveParentageDesignationAsAStringGroup.setImmediate(true);
	}

	@Override
	public void initializeValues() {

		for (int i = 1; i <= CrossingSettingsNameComponent.MAX_LEADING_ZEROS; i++) {
			this.leadingZerosSelect.addItem(Integer.valueOf(i));
		}
		this.leadingZerosSelect.select(1);

		// Add space option group
		final String yes = this.messageSource.getMessage(Message.YES);
		final String no = this.messageSource.getMessage(Message.NO);

		this.addSpaceBetPrefixAndCodeOptionGroup.addItem(ConfirmOption.YES);
		this.addSpaceBetPrefixAndCodeOptionGroup.setItemCaption(ConfirmOption.YES, yes);
		this.addSpaceBetPrefixAndCodeOptionGroup.addItem(ConfirmOption.NO);
		this.addSpaceBetPrefixAndCodeOptionGroup.setItemCaption(ConfirmOption.NO, no);

		this.addSpaceBetSuffixAndCodeOptionGroup.addItem(ConfirmOption.YES);
		this.addSpaceBetSuffixAndCodeOptionGroup.setItemCaption(ConfirmOption.YES, yes);
		this.addSpaceBetSuffixAndCodeOptionGroup.addItem(ConfirmOption.NO);
		this.addSpaceBetSuffixAndCodeOptionGroup.setItemCaption(ConfirmOption.NO, no);

		this.saveParentageDesignationAsAStringGroup.addItem(ConfirmOption.YES);
		this.saveParentageDesignationAsAStringGroup.setItemCaption(ConfirmOption.YES, yes);
		this.saveParentageDesignationAsAStringGroup.addItem(ConfirmOption.NO);
		this.saveParentageDesignationAsAStringGroup.setItemCaption(ConfirmOption.NO, no);

		this.setFieldsDefaultValue();
	}

	@Override
	public void addListeners() {
		this.separatorTextField.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -8395381042668695941L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				CrossingSettingsNameComponent.this.updateDesignationExample();
			}
		});

		// Generate new cross name when any of the fields change
		this.crossNamePrefix.addListener(new CrossNameFieldsValueChangeListener());
		this.crossNameSuffix.addListener(new CrossNameFieldsValueChangeListener());
		this.addSpaceBetPrefixAndCodeOptionGroup.addListener(new CrossNameFieldsValueChangeListener());
		this.addSpaceBetSuffixAndCodeOptionGroup.addListener(new CrossNameFieldsValueChangeListener());
		this.leadingZerosSelect.addListener(new CrossNameFieldsValueChangeListener());
		this.startNumberTextField.addListener(new CrossNameFieldsValueChangeListener());
	}

	@Override
	public void layoutComponents() {

		final FormLayout formFields = new FormLayout();
		formFields.setDebugId("formFields");

		formFields.addComponent(this.crossNamePrefix);
		formFields.addComponent(this.leadingZerosSelect);
		formFields.addComponent(this.crossNameSuffix);
		formFields.addComponent(this.addSpaceBetPrefixAndCodeOptionGroup);
		formFields.addComponent(this.addSpaceBetSuffixAndCodeOptionGroup);
		formFields.addComponent(this.generatedNextName);
		formFields.addComponent(this.startNumberTextField);
		formFields.addComponent(this.separatorTextField);
		formFields.addComponent(this.generatedExampleParentage);
		formFields.addComponent(this.saveParentageDesignationAsAStringGroup);

		this.addComponent(this.namingLabel);
		this.addComponent(this.namingDescLabel);
		this.addComponent(formFields);
	}

	public CrossNameSetting getCrossNameSettingObject() {

		final String prefix = (String) this.crossNamePrefix.getValue();
		String suffix = (String) this.crossNameSuffix.getValue();

		if (suffix != null) {
			suffix = suffix.trim();
		}
		if (suffix.length() == 0) {
			// set as null so attribute will not be marshaled
			suffix = null;
		}

		final boolean addSpaceBetweenPrefixAndCode = ConfirmOption.YES.equals(this.addSpaceBetPrefixAndCodeOptionGroup.getValue());
		final boolean addSpaceBetweenSuffixAndCode = ConfirmOption.YES.equals(this.addSpaceBetSuffixAndCodeOptionGroup.getValue());
		final Integer numOfDigits = this.leadingZerosSelect.getValue() == null ? null : (Integer) this.leadingZerosSelect.getValue();
		final boolean saveParentageDesignationAsAString = ConfirmOption.YES.equals(this.saveParentageDesignationAsAStringGroup.getValue());

		final String separator = (String) this.separatorTextField.getValue();
		final CrossNameSetting crossNameSettingPojo =
				new CrossNameSetting(prefix.trim(), suffix, addSpaceBetweenPrefixAndCode, addSpaceBetweenSuffixAndCode, numOfDigits,
						separator, saveParentageDesignationAsAString);
		final String startNumber = (String) this.startNumberTextField.getValue();

		if (StringUtils.isNotEmpty(startNumber) && NumberUtils.isDigits(startNumber)) {
			crossNameSettingPojo.setStartNumber(Integer.parseInt(startNumber));
		}

		return crossNameSettingPojo;
	}

	public void setFields(final CrossNameSetting crossNameSetting) {

		this.crossNamePrefix.setValue(crossNameSetting.getPrefix());

		if (crossNameSetting.isAddSpaceBetweenPrefixAndCode()) {
			this.addSpaceBetPrefixAndCodeOptionGroup.select(ConfirmOption.YES);
		} else {
			this.addSpaceBetPrefixAndCodeOptionGroup.select(ConfirmOption.NO);
		}

		if (crossNameSetting.isAddSpaceBetweenSuffixAndCode()) {
			this.addSpaceBetSuffixAndCodeOptionGroup.select(ConfirmOption.YES);
		} else {
			this.addSpaceBetSuffixAndCodeOptionGroup.select(ConfirmOption.NO);
		}

		if (crossNameSetting.getNumOfDigits() != null && crossNameSetting.getNumOfDigits() > 0) {
			this.leadingZerosSelect.select(crossNameSetting.getNumOfDigits());
		} else {
			this.leadingZerosSelect.select(null);
		}

		String suffix = crossNameSetting.getSuffix();
		if (suffix == null) {
			suffix = "";
		}
		this.crossNameSuffix.setValue(suffix);

		this.separatorTextField.setValue(crossNameSetting.getSeparator());

		if (crossNameSetting.getStartNumber() != null) {
			this.startNumberTextField.setValue(crossNameSetting.getStartNumber().toString());
		}

		if (crossNameSetting.isSaveParentageDesignationAsAString()) {
			this.saveParentageDesignationAsAStringGroup.select(ConfirmOption.YES);
		} else {
			this.saveParentageDesignationAsAStringGroup.select(ConfirmOption.NO);
		}
	}

	public void setFieldsDefaultValue() {
		this.crossNamePrefix.setValue("");
		this.crossNameSuffix.setValue("");
		this.addSpaceBetPrefixAndCodeOptionGroup.select(ConfirmOption.NO);
		this.addSpaceBetSuffixAndCodeOptionGroup.select(ConfirmOption.NO);
		this.leadingZerosSelect.select(null);
		this.startNumberTextField.setValue("");
		this.separatorTextField.setValue(CrossNameSetting.DEFAULT_SEPARATOR);
		this.updateNextNameInSequence("");
		this.updateDesignationExample();
		this.saveParentageDesignationAsAStringGroup.select(ConfirmOption.NO);
	}

	public boolean validateInputFields() {

		final String prefix = (String) this.crossNamePrefix.getValue();

		if (prefix == null || prefix.trim().length() == 0) {
			MessageNotifier.showRequiredFieldError(this.getWindow(), this.messageSource.getMessage(Message.PLEASE_SPECIFY_A_PREFIX));
			return false;
		}
		return this.validateStartNumberField();
	}

	private final class CrossNameFieldsValueChangeListener implements Property.ValueChangeListener {

		private static final long serialVersionUID = -8395381042668695941L;

		@Override
		public void valueChange(final ValueChangeEvent event) {
			CrossingSettingsNameComponent.this.generateNextNameAction();
		}
	}

	private void generateNextNameAction() {
		if (this.validateCrossNameFields()) {
			try {
				final GenerateCrossNameAction generateAction = new GenerateCrossNameAction();
				this.updateNextNameInSequence(generateAction.getNextNameInSequence(this.getCrossNameSettingObject()));

			} catch (final MiddlewareQueryException e) {
				CrossingSettingsNameComponent.LOG.error(e.toString() + "\n" + e.getStackTrace());
				LOG.error(e.getMessage(), e);
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE));
			}
		} else {
			this.updateNextNameInSequence("");
		}
	}

	private boolean validateCrossNameFields() {
		final String prefix = ((String) this.crossNamePrefix.getValue()).trim();
		return !StringUtils.isEmpty(prefix) && this.validateStartNumberField();
	}

	protected boolean validateStartNumberField() {

		String startNumberString = "";
		if (StringUtils.isNotEmpty((String) this.startNumberTextField.getValue())) {
			startNumberString = this.startNumberTextField.getValue().toString();
		}

		if (!StringUtils.isEmpty(startNumberString)) {
			if (startNumberString.length() > 10) {
				MessageNotifier.showRequiredFieldError(this.getWindow(),
						this.messageSource.getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS));
				return false;
			}
			if (!NumberUtils.isDigits(startNumberString)) {
				MessageNotifier.showRequiredFieldError(this.getWindow(),
						this.messageSource.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER));
				return false;
			}

		}
		return true;
	}

	private void updateNextNameInSequence(final String updatedName) {
		this.generatedNextName.setReadOnly(false);
		this.generatedNextName.setValue(updatedName);
		this.generatedNextName.setReadOnly(true);
	}

	private void updateDesignationExample() {
		final String female = "FEMALE-123";
		final String male = "MALE-456";
		String separator = this.separatorTextField.getValue().toString();

		if (StringUtils.isEmpty(separator)) {
			this.separatorTextField.setValue(CrossNameSetting.DEFAULT_SEPARATOR);
			separator = CrossNameSetting.DEFAULT_SEPARATOR;
		}
		this.generatedExampleParentage.setReadOnly(false);
		this.generatedExampleParentage.setValue(female + separator + male);
		this.generatedExampleParentage.setReadOnly(true);

	}
}

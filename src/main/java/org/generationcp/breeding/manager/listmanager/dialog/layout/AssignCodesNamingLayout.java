package org.generationcp.breeding.manager.listmanager.dialog.layout;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.MandatoryMarkLabel;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class AssignCodesNamingLayout {

	private static final String NO = "N";
	private static final String YES = "Y";
	private static final Integer MAX_NUM_OF_ALLOWED_DIGITS = 9;
	
	private TextField prefixTextField;
	private TextField suffixTextField;
	private Select numOfAllowedDigitsSelect;
	private OptionGroup addSpaceAfterPrefixOptionGroup;
	private OptionGroup addSpaceBeforeSuffixOptionGroup;
	private TextField startNumberTextField;
	
	private Label prefixLabel;
	private Label suffixLabel;
	private Label numOfDigitsLabel;
	private Label addSpaceAfterPrefixLabel;
	private Label addSpaceBeforeSuffixLabel;
	private Label startNumberLabel;
	private Label nextNameLabel;
	private Label nextValueLabel;
	private MandatoryMarkLabel prefixMandatoryLabel;
	private MandatoryMarkLabel numOfDigitsMandatoryLabel;
	
	// the value we are getting from the common layout
	private final VerticalLayout codesLayout;
	private final SimpleResourceBundleMessageSource messageSource;

	public AssignCodesNamingLayout(final VerticalLayout codesLayout, final SimpleResourceBundleMessageSource messageSource) {
		this.codesLayout = codesLayout;
		this.messageSource = messageSource;
	}

	public void instantiateComponents() {
		this.prefixTextField = new TextField();
		this.prefixTextField.setDebugId("prefixTextField");
		this.prefixTextField.setImmediate(true);
		this.prefixTextField.addValidator(new StringLengthValidator(
				this.messageSource.getMessage(Message.ERROR_TOO_LONG, this.messageSource.getMessage(Message.CODE_PREFIX), 50), 0, 50,
				false));
		
		this.suffixTextField = new TextField();
		this.suffixTextField.setDebugId("suffixTextField");
		this.suffixTextField.setImmediate(true);
		this.suffixTextField.addValidator(new StringLengthValidator(
				this.messageSource.getMessage(Message.ERROR_TOO_LONG, this.messageSource.getMessage(Message.CODE_SUFFIX), 50), 0, 50,
				true));
		
		this.numOfAllowedDigitsSelect = new Select();
		this.numOfAllowedDigitsSelect.setDebugId("numOfAllowedDigitsSelect");
		this.numOfAllowedDigitsSelect.setImmediate(true);
		for (int i = 1; i <= AssignCodesNamingLayout.MAX_NUM_OF_ALLOWED_DIGITS; i++) {
			this.numOfAllowedDigitsSelect.addItem(Integer.valueOf(i));
		}
		this.numOfAllowedDigitsSelect.setNullSelectionAllowed(false);
		this.numOfAllowedDigitsSelect.select(Integer.valueOf(1));
		
		this.addSpaceAfterPrefixOptionGroup = new OptionGroup();
		this.addSpaceAfterPrefixOptionGroup.setDebugId("addSpaceAfterPrefixOptionGroup");
		this.addSpaceAfterPrefixOptionGroup.setImmediate(true);
		this.addSpaceAfterPrefixOptionGroup.addItem(YES);
		this.addSpaceAfterPrefixOptionGroup.setItemCaption(YES, this.messageSource.getMessage(Message.YES));
		this.addSpaceAfterPrefixOptionGroup.addItem(NO);
		this.addSpaceAfterPrefixOptionGroup.setItemCaption(NO, this.messageSource.getMessage(Message.NO));
		this.addSpaceAfterPrefixOptionGroup.select(NO);
		this.addSpaceAfterPrefixOptionGroup.addStyleName("lst-horizontal-options");
		
		this.addSpaceBeforeSuffixOptionGroup = new OptionGroup();
		this.addSpaceBeforeSuffixOptionGroup.setDebugId("addSpaceBeforeSuffixOptionGroup");
		this.addSpaceBeforeSuffixOptionGroup.setImmediate(true);
		this.addSpaceBeforeSuffixOptionGroup.addItem(YES);
		this.addSpaceBeforeSuffixOptionGroup.setItemCaption(YES, this.messageSource.getMessage(Message.YES));
		this.addSpaceBeforeSuffixOptionGroup.addItem(NO);
		this.addSpaceBeforeSuffixOptionGroup.setItemCaption(NO, this.messageSource.getMessage(Message.NO));
		this.addSpaceBeforeSuffixOptionGroup.select(NO);
		this.addSpaceBeforeSuffixOptionGroup.addStyleName("lst-horizontal-options");
			
		this.startNumberTextField = new TextField();
		this.startNumberTextField.setDebugId("startNumberTextField");
		this.startNumberTextField.addValidator(new IntegerValidator(this.messageSource.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER)));
		
		this.nextNameLabel = new Label(this.messageSource.getMessage(Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE));
		this.nextNameLabel.setDebugId("nextNameLabel");
		this.nextNameLabel.addStyleName("italic");
		
		this.nextValueLabel = new Label();
		this.nextValueLabel.setDebugId("nextNameLabel");
		
		this.prefixLabel = new Label(this.messageSource.getMessage(Message.CODE_PREFIX));
		this.prefixLabel.setDebugId("prefixLabel");
		this.prefixLabel.addStyleName("bold");
		
		this.numOfDigitsLabel = new Label(this.messageSource.getMessage(Message.CODE_NUMBER_OF_DIGITS));
		this.numOfDigitsLabel.setDebugId("numOfDigitsLabel");
		this.numOfDigitsLabel.addStyleName("bold");
		
		this.suffixLabel = new Label(this.messageSource.getMessage(Message.CODE_SUFFIX));
		this.suffixLabel.setDebugId("suffixLabel");
		this.suffixLabel.addStyleName("bold");
		
		this.addSpaceAfterPrefixLabel = new Label(this.messageSource.getMessage(Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE));
		this.addSpaceAfterPrefixLabel.setDebugId("addSpaceAfterPrefixLabel");
		this.addSpaceAfterPrefixLabel.addStyleName("bold");
		
		this.addSpaceBeforeSuffixLabel = new Label(this.messageSource.getMessage(Message.ADD_SPACE_BETWEEN_SUFFIX_AND_CODE));
		this.addSpaceBeforeSuffixLabel.setDebugId("addSpaceBeforeSuffixLabel");
		this.addSpaceBeforeSuffixLabel.addStyleName("bold");
		
		this.startNumberLabel = new Label(this.messageSource.getMessage(Message.CODE_START_NUMBER));
		this.startNumberLabel.setDebugId("startNumberLabel");
		this.startNumberLabel.addStyleName("bold");
		
		this.prefixMandatoryLabel = new MandatoryMarkLabel();
		this.prefixMandatoryLabel.setDebugId("prefixMandatoryLabel");
		
		this.numOfDigitsMandatoryLabel = new MandatoryMarkLabel();
		this.numOfDigitsMandatoryLabel.setDebugId("numOfDigitsMandatoryLabel");
	}

	public AbsoluteLayout constructDefaultCodeControlsLayout() {
		final AbsoluteLayout namingLayout = new AbsoluteLayout();
		namingLayout.setDebugId("namingLayout");
		
		final String fieldsWidth = "300px";
		this.prefixTextField.setWidth(fieldsWidth);
		this.suffixTextField.setWidth(fieldsWidth);
		this.numOfAllowedDigitsSelect.setWidth(fieldsWidth);
		this.startNumberTextField.setWidth(fieldsWidth);
		
		namingLayout.addComponent(this.prefixLabel, "top:0px;left:0px");
		namingLayout.addComponent(this.prefixMandatoryLabel, "top:0px;left:80px");
		namingLayout.addComponent(this.prefixTextField, "top:0px;left:290px");
		namingLayout.addComponent(this.numOfDigitsLabel, "top:40px;left:0px");
		namingLayout.addComponent(this.numOfDigitsMandatoryLabel, "top:40px;left:250px");
		namingLayout.addComponent(this.numOfAllowedDigitsSelect, "top:40px;left:290px");
		namingLayout.addComponent(this.suffixLabel, "top:80px;left:0px");
		namingLayout.addComponent(this.suffixTextField, "top:80px;left:290px");
		namingLayout.addComponent(this.addSpaceAfterPrefixLabel, "top:120px;left:0px");
		namingLayout.addComponent(this.addSpaceAfterPrefixOptionGroup, "top:115px;left:290px");
		namingLayout.addComponent(this.addSpaceBeforeSuffixLabel, "top:160px;left:0px");
		namingLayout.addComponent(this.addSpaceBeforeSuffixOptionGroup, "top:155px;left:290px");
		namingLayout.addComponent(this.nextNameLabel, "top:190px;left:290px");
		namingLayout.addComponent(this.startNumberLabel , "top:220px;left:0px");
		namingLayout.addComponent(this.startNumberTextField, "top:225px;left:290px");
		
		return namingLayout;
	}

	public void addListeners() {
		final Property.ValueChangeListener valueChangeListener = new Property.ValueChangeListener() {
			private static final long serialVersionUID = -8157550024548429537L;

			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				AssignCodesNamingLayout.this.updateNextNameValue();
			}
		};
		this.prefixTextField.addListener(valueChangeListener);
		this.suffixTextField.addListener(valueChangeListener);
		this.numOfAllowedDigitsSelect.addListener(valueChangeListener);
		this.addSpaceAfterPrefixOptionGroup.addListener(valueChangeListener);
		this.addSpaceBeforeSuffixOptionGroup.addListener(valueChangeListener);
	}
	
	public void updateNextNameValue() {
		
	}

	public String getGroupNamePrefix() {
		return this.prefixTextField.getValue().toString();
	}

	public TextField getPrefixDefault() {
		return this.prefixTextField;
	}

	public void layoutComponents() {
		final AbsoluteLayout codeControlsLayoutDefault = this.constructDefaultCodeControlsLayout();
		this.codesLayout.addComponent(codeControlsLayoutDefault);
		this.codesLayout.setComponentAlignment(codeControlsLayoutDefault, Alignment.MIDDLE_LEFT);
	}

	public void validate() throws Validator.InvalidValueException {
		this.prefixTextField.validate();
		this.suffixTextField.validate();
		this.startNumberTextField.validate();
	}
}

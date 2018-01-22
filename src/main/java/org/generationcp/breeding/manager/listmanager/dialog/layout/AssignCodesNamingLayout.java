package org.generationcp.breeding.manager.listmanager.dialog.layout;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.MandatoryMarkLabel;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmNamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class AssignCodesNamingLayout {

	public static final Integer MAX_NUM_OF_ALLOWED_DIGITS = 9;
	public static final String NO = "N";
	public static final String YES = "Y";
	
	@Autowired
	private GermplasmNamingService germplasmNamingService;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
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
	private final Button applyCodesButton;

	public AssignCodesNamingLayout(final VerticalLayout codesLayout, final Button applyCodesButton) {
		this.codesLayout = codesLayout;
		this.applyCodesButton = applyCodesButton;
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
		this.startNumberTextField.setImmediate(true);
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
		namingLayout.addComponent(this.nextValueLabel, "top:190px;left:455px");
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
		this.startNumberTextField.addListener(valueChangeListener);
	}
	
	void updateNextNameValue() {
		final GermplasmNameSetting setting = this.generateGermplasmNameSetting();
		if (!setting.getPrefix().trim().isEmpty()) {
			synchronized (AssignCodesNamingLayout.class) {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(final TransactionStatus status) {
						String nextName = "";
						try {
							nextName = AssignCodesNamingLayout.this.germplasmNamingService.getNextNameInSequence(setting);
						} catch (InvalidGermplasmNameSettingException e) {
							MessageNotifier.showError(AssignCodesNamingLayout.this.codesLayout.getWindow(),
									AssignCodesNamingLayout.this.messageSource.getMessage(Message.ERROR), e.getMessage());
						}
						AssignCodesNamingLayout.this.applyCodesButton.setEnabled(!nextName.isEmpty());
						AssignCodesNamingLayout.this.nextValueLabel.setValue(nextName);
					}
				});
			}
		} else {
			this.applyCodesButton.setEnabled(false);
			this.nextValueLabel.setValue("");
		}
	}

	public GermplasmNameSetting generateGermplasmNameSetting() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setPrefix(this.prefixTextField.getValue().toString());
		setting.setSuffix(this.suffixTextField.getValue().toString());
		setting.setNumOfDigits((Integer)this.numOfAllowedDigitsSelect.getValue());
		final String startNumberString = this.startNumberTextField.getValue().toString();
		setting.setStartNumber(startNumberString.isEmpty() ?  0 : Integer.valueOf(startNumberString));
		setting.setAddSpaceBetweenPrefixAndCode(YES.equals(this.addSpaceAfterPrefixOptionGroup.getValue()));
		setting.setAddSpaceBetweenSuffixAndCode(YES.equals(this.addSpaceBeforeSuffixOptionGroup.getValue()));
		return setting;
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

	
	public void setGermplasmNamingService(GermplasmNamingService germplasmNamingService) {
		this.germplasmNamingService = germplasmNamingService;
	}

	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	
	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	
	public TextField getPrefixTextField() {
		return prefixTextField;
	}

	
	public TextField getSuffixTextField() {
		return suffixTextField;
	}

	
	public Select getNumOfAllowedDigitsSelect() {
		return numOfAllowedDigitsSelect;
	}

	
	public OptionGroup getAddSpaceAfterPrefixOptionGroup() {
		return addSpaceAfterPrefixOptionGroup;
	}

	
	public OptionGroup getAddSpaceBeforeSuffixOptionGroup() {
		return addSpaceBeforeSuffixOptionGroup;
	}

	
	public TextField getStartNumberTextField() {
		return startNumberTextField;
	}

	
	
	public void setPrefixTextField(TextField prefixTextField) {
		this.prefixTextField = prefixTextField;
	}

	
	public void setSuffixTextField(TextField suffixTextField) {
		this.suffixTextField = suffixTextField;
	}

	
	public void setStartNumberTextField(TextField startNumberTextField) {
		this.startNumberTextField = startNumberTextField;
	}

	public Label getNextValueLabel() {
		return nextValueLabel;
	}
}

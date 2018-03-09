package org.generationcp.breeding.manager.listmanager.dialog.layout;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import junit.framework.Assert;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.service.GermplasmCodeGenerationService;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collection;

public class AssignCodesNamingLayoutTest {

	private static final int NUM_OF_DIGITS = 3;
	private static final String PREFIX = "AAA";
	private static final String SUFFIX = "XYZ";
	private static final String INVALID_STARTING_NUMBER = "Please enter valid start number.";
	private static final String NEXT_NAME = AssignCodesNamingLayoutTest.PREFIX + " 006 " + AssignCodesNamingLayoutTest.SUFFIX;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private VerticalLayout parentLayout;

	@Mock
	private Button applyCodesButton;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private GermplasmCodeGenerationService germplasmCodeGenerationService;

	@Mock
	private Window parent;

	@InjectMocks
	private AssignCodesNamingLayout namingLayout;

	@Before
	public void setup() throws InvalidGermplasmNameSettingException {
		MockitoAnnotations.initMocks(this);
		this.namingLayout = new AssignCodesNamingLayout(this.parentLayout, this.applyCodesButton);
		this.namingLayout.setTransactionManager(this.transactionManager);
		this.namingLayout.setMessageSource(this.messageSource);
		this.namingLayout.setGermplasmCodeGenerationService(this.germplasmCodeGenerationService);

		Mockito.doReturn(AssignCodesNamingLayoutTest.INVALID_STARTING_NUMBER).when(this.messageSource)
				.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER);
		Mockito.doReturn(AssignCodesNamingLayoutTest.NEXT_NAME).when(this.germplasmCodeGenerationService)
				.getNextNameInSequence(Matchers.any(GermplasmNameSetting.class));
		Mockito.doReturn(this.parent).when(this.parentLayout).getWindow();

		this.namingLayout.instantiateComponents();
	}

	@Test
	public void testInstantiatedComponents() {
		final TextField prefixTextField = this.namingLayout.getPrefixTextField();
		Assert.assertTrue(prefixTextField.isImmediate());
		final Collection<Validator> prefixValidators = prefixTextField.getValidators();
		Assert.assertNotNull(prefixValidators);
		final StringLengthValidator prefixValidator = (StringLengthValidator) prefixValidators.iterator().next();
		Assert.assertEquals(49, prefixValidator.getMaxLength());

		final TextField suffixTextField = this.namingLayout.getSuffixTextField();
		Assert.assertTrue(suffixTextField.isImmediate());
		final Collection<Validator> suffixValidators = suffixTextField.getValidators();
		Assert.assertNotNull(suffixValidators);
		final StringLengthValidator suffixValidator = (StringLengthValidator) suffixValidators.iterator().next();
		Assert.assertEquals(49, suffixValidator.getMaxLength());

		final TextField startTextField = this.namingLayout.getStartNumberTextField();
		Assert.assertTrue(startTextField.isImmediate());
		final Collection<Validator> startNumberValidators = startTextField.getValidators();
		Assert.assertNotNull(startNumberValidators);
		final IntegerValidator startNumberValidator = (IntegerValidator) startNumberValidators.iterator().next();
		Assert.assertEquals(AssignCodesNamingLayoutTest.INVALID_STARTING_NUMBER, startNumberValidator.getErrorMessage());

		final Select numOfDigitsSelect = this.namingLayout.getNumOfAllowedDigitsSelect();
		Assert.assertTrue(numOfDigitsSelect.isImmediate());
		for (int i = 1; i <= AssignCodesNamingLayout.MAX_NUM_OF_ALLOWED_DIGITS; i++) {
			Assert.assertNotNull(numOfDigitsSelect.getItem(i));
		}
		Assert.assertEquals(1, numOfDigitsSelect.getValue());

		final OptionGroup addSpaceAfterPrefixOption = this.namingLayout.getAddSpaceAfterPrefixOptionGroup();
		Assert.assertTrue(addSpaceAfterPrefixOption.isImmediate());
		Assert.assertEquals(2, addSpaceAfterPrefixOption.getItemIds().size());
		Assert.assertNotNull(addSpaceAfterPrefixOption.getItem(AssignCodesNamingLayout.YES));
		Assert.assertNotNull(addSpaceAfterPrefixOption.getItem(AssignCodesNamingLayout.NO));
		Assert.assertEquals(AssignCodesNamingLayout.NO, addSpaceAfterPrefixOption.getValue());

		final OptionGroup addSpaceBeforeSuffixOption = this.namingLayout.getAddSpaceAfterPrefixOptionGroup();
		Assert.assertTrue(addSpaceBeforeSuffixOption.isImmediate());
		Assert.assertEquals(2, addSpaceBeforeSuffixOption.getItemIds().size());
		Assert.assertNotNull(addSpaceBeforeSuffixOption.getItem(AssignCodesNamingLayout.YES));
		Assert.assertNotNull(addSpaceBeforeSuffixOption.getItem(AssignCodesNamingLayout.NO));
		Assert.assertEquals(AssignCodesNamingLayout.NO, addSpaceBeforeSuffixOption.getValue());
	}

	@Test
	public void testAddListeners() {
		this.namingLayout.addListeners();

		final Collection<?> prefixChangeListener = this.namingLayout.getPrefixTextField().getListeners(ValueChangeEvent.class);
		Assert.assertNotNull(prefixChangeListener);
		Assert.assertEquals(1, prefixChangeListener.size());

		final Collection<?> suffixValueChangeListener = this.namingLayout.getSuffixTextField().getListeners(ValueChangeEvent.class);
		Assert.assertNotNull(suffixValueChangeListener);
		Assert.assertEquals(1, suffixValueChangeListener.size());

		final Collection<?> spaceAfterPrefixListener =
				this.namingLayout.getAddSpaceAfterPrefixOptionGroup().getListeners(ValueChangeEvent.class);
		Assert.assertNotNull(spaceAfterPrefixListener);
		Assert.assertEquals(1, spaceAfterPrefixListener.size());

		final Collection<?> spaceBeforeSuffixListener =
				this.namingLayout.getAddSpaceBeforeSuffixOptionGroup().getListeners(ValueChangeEvent.class);
		Assert.assertNotNull(spaceBeforeSuffixListener);
		Assert.assertEquals(1, spaceBeforeSuffixListener.size());

		final Collection<?> numOfDigitsListener = this.namingLayout.getNumOfAllowedDigitsSelect().getListeners(ValueChangeEvent.class);
		Assert.assertNotNull(numOfDigitsListener);
		Assert.assertEquals(1, numOfDigitsListener.size());

		final Collection<?> startNumberListener = this.namingLayout.getStartNumberTextField().getListeners(ValueChangeEvent.class);
		Assert.assertNotNull(startNumberListener);
		Assert.assertEquals(1, startNumberListener.size());
	}

	@Test
	public void testUpdateNextNameValueWhenPrefixIsEmpty() throws InvalidGermplasmNameSettingException {
		this.namingLayout.updateNextNameValue();
		Mockito.verify(this.germplasmCodeGenerationService, Mockito.never())
				.getNextNameInSequence(Matchers.any(GermplasmNameSetting.class));
		Assert.assertEquals("", this.namingLayout.getNextValueLabel().getValue());

		// Specify Prefix to enable button
		this.setupTestValuesForNameFields();
		this.namingLayout.updateNextNameValue();
		Mockito.verify(this.applyCodesButton).setEnabled(true);

		// Empty Prefix again to check if it was set back to disabled
		this.namingLayout.getPrefixTextField().setValue("");
		this.namingLayout.updateNextNameValue();
		Mockito.verify(this.applyCodesButton, Mockito.times(2)).setEnabled(false);
		Assert.assertEquals("", this.namingLayout.getNextValueLabel().getValue());
	}

	@Test
	public void testUpdateNextNameValueWithInvalidGermplasmNameSettingException() throws InvalidGermplasmNameSettingException {
		Mockito.doThrow(new InvalidGermplasmNameSettingException(AssignCodesNamingLayoutTest.INVALID_STARTING_NUMBER))
				.when(this.germplasmCodeGenerationService).getNextNameInSequence(Matchers.any(GermplasmNameSetting.class));
		this.setupTestValuesForNameFields();
		this.namingLayout.updateNextNameValue();

		Mockito.verify(this.parentLayout).getWindow();
		Mockito.verify(this.applyCodesButton).setEnabled(false);
		Assert.assertEquals("", this.namingLayout.getNextValueLabel().getValue());
	}

	@Test
	public void testUpdateNextNameValue() throws InvalidGermplasmNameSettingException {
		this.setupTestValuesForNameFields();
		this.namingLayout.updateNextNameValue();

		Mockito.verify(this.germplasmCodeGenerationService).getNextNameInSequence(this.createGermplasmNameSetting());
		Mockito.verify(this.applyCodesButton).setEnabled(true);
		Assert.assertEquals(AssignCodesNamingLayoutTest.NEXT_NAME, this.namingLayout.getNextValueLabel().getValue());
	}

	@Test
	public void testGenerateGermplasmNameSetting() {
		this.setupTestValuesForNameFields();
		final GermplasmNameSetting setting = this.createGermplasmNameSetting();
		GermplasmNameSetting generatedSetting = this.namingLayout.generateGermplasmNameSetting();
		Assert.assertEquals(setting, generatedSetting);

		final Integer startNumber = 101;
		this.namingLayout.getAddSpaceAfterPrefixOptionGroup().setValue(AssignCodesNamingLayout.NO);
		this.namingLayout.getAddSpaceBeforeSuffixOptionGroup().setValue(AssignCodesNamingLayout.NO);
		this.namingLayout.getStartNumberTextField().setValue(startNumber);
		setting.setAddSpaceBetweenPrefixAndCode(false);
		setting.setAddSpaceBetweenSuffixAndCode(false);
		setting.setStartNumber(startNumber);
		generatedSetting = this.namingLayout.generateGermplasmNameSetting();
		Assert.assertEquals(setting, generatedSetting);
	}

	@Test
	public void testGenerateGermplasmNameSettingForNonDigitStartNumber() {
		this.setupTestValuesForNameFields();
		this.namingLayout.getStartNumberTextField().setValue("a");
		try {
			final GermplasmNameSetting setting = this.createGermplasmNameSetting();
			final GermplasmNameSetting generatedSetting = this.namingLayout.generateGermplasmNameSetting();
			Assert.assertEquals(setting, generatedSetting);
			Mockito.verify(this.parentLayout).getWindow();
		} catch (final NumberFormatException e) {
			Assert.fail("Not expecting NumberFormatException but was thrown.");
		}
	}

	@Test
	public void testValidate() {
		final TextField prefixTextField = Mockito.mock(TextField.class);
		this.namingLayout.setPrefixTextField(prefixTextField);
		final TextField suffixTextField = Mockito.mock(TextField.class);
		this.namingLayout.setSuffixTextField(suffixTextField);
		final TextField startNumberTextField = Mockito.mock(TextField.class);
		this.namingLayout.setStartNumberTextField(startNumberTextField);

		this.namingLayout.validate();
		Mockito.verify(prefixTextField).validate();
		Mockito.verify(suffixTextField).validate();
		Mockito.verify(startNumberTextField).validate();
	}

	private void setupTestValuesForNameFields() {
		this.namingLayout.getPrefixTextField().setValue(AssignCodesNamingLayoutTest.PREFIX);
		this.namingLayout.getSuffixTextField().setValue(AssignCodesNamingLayoutTest.SUFFIX);
		this.namingLayout.getAddSpaceAfterPrefixOptionGroup().setValue(AssignCodesNamingLayout.YES);
		this.namingLayout.getAddSpaceBeforeSuffixOptionGroup().setValue(AssignCodesNamingLayout.YES);
		this.namingLayout.getNumOfAllowedDigitsSelect().setValue(AssignCodesNamingLayoutTest.NUM_OF_DIGITS);
	}

	private GermplasmNameSetting createGermplasmNameSetting() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();

		setting.setPrefix(AssignCodesNamingLayoutTest.PREFIX);
		setting.setSuffix(AssignCodesNamingLayoutTest.SUFFIX);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(AssignCodesNamingLayoutTest.NUM_OF_DIGITS);

		return setting;
	}
}


package org.generationcp.breeding.manager.crossingmanager.settings;

import junit.framework.Assert;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ConfirmOption;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class CrossingSettingsNameComponentTest {

	private static final String START_NUMBER = "100";

	private static final String CROSS_NAME_SUFFIX = "RIN";

	private static final String CROSS_NAME_PREFIX = "ALD";

	private static final int LEADING_ZEROS = 9;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private TextField crossNamePrefix;

	@Mock
	private OptionGroup addSpaceBetPrefixAndCodeOptionGroup;

	@Mock
	private OptionGroup addSpaceBetSuffixAndCodeOptionGroup;

	@Mock
	private Select leadingZerosSelect;

	@Mock
	private TextField crossNameSuffix;

	@Mock
	private TextField separatorTextField;

	@Mock
	private TextField startNumberTextField;

	@Mock
	private Component parent;

	@Mock
	private OptionGroup saveParentageDesignationAsAStringGroup;

	@InjectMocks
	private CrossingSettingsNameComponent crossingSettingsNameComponent;

	@Before
	public void setUp() {

		Mockito.when(this.parent.getWindow()).thenReturn(Mockito.mock(Window.class));

		this.initializeFieldValues();
	}

	@Test
	public void testSetFieldsDefaultValues() throws Exception {

		final CrossNameSetting crossNameSetting = new CrossNameSetting();
		crossNameSetting.setSeparator(CrossNameSetting.DEFAULT_SEPARATOR);

		this.crossingSettingsNameComponent.setFields(crossNameSetting);

		Mockito.verify(this.crossNamePrefix).setValue(null);
		Mockito.verify(this.crossNameSuffix).setValue("");
		Mockito.verify(this.addSpaceBetPrefixAndCodeOptionGroup).select(ConfirmOption.NO);
		Mockito.verify(this.addSpaceBetSuffixAndCodeOptionGroup).select(ConfirmOption.NO);
		Mockito.verify(this.leadingZerosSelect).select(null);
		Mockito.verify(this.separatorTextField).setValue(CrossNameSetting.DEFAULT_SEPARATOR);
		Mockito.verify(this.startNumberTextField, Mockito.times(0)).setValue(null);
		Mockito.verify(this.saveParentageDesignationAsAStringGroup).select(ConfirmOption.NO);

	}

	@Test
	public void testSetFields() throws Exception {

		final CrossNameSetting crossNameSetting = this.createNameSetting();

		this.crossingSettingsNameComponent.setFields(crossNameSetting);

		Mockito.verify(this.crossNamePrefix).setValue(CROSS_NAME_PREFIX);
		Mockito.verify(this.addSpaceBetPrefixAndCodeOptionGroup).select(ConfirmOption.YES);
		Mockito.verify(this.addSpaceBetSuffixAndCodeOptionGroup).select(ConfirmOption.YES);
		Mockito.verify(this.leadingZerosSelect).select(LEADING_ZEROS);
		Mockito.verify(this.crossNameSuffix).setValue(CROSS_NAME_SUFFIX);
		Mockito.verify(this.separatorTextField).setValue(CrossNameSetting.DEFAULT_SEPARATOR);
		Mockito.verify(this.startNumberTextField).setValue(START_NUMBER);
		Mockito.verify(this.saveParentageDesignationAsAStringGroup).select(ConfirmOption.YES);

	}

	@Test
	public void testGetCrossNameSettingObject() {

		final CrossNameSetting crossNameSetting = this.crossingSettingsNameComponent.getCrossNameSettingObject();

		Assert.assertEquals(true, crossNameSetting.isAddSpaceBetweenPrefixAndCode());
		Assert.assertEquals(true, crossNameSetting.isAddSpaceBetweenSuffixAndCode());
		Assert.assertEquals((Integer) 9, crossNameSetting.getNumOfDigits());
		Assert.assertEquals(CROSS_NAME_PREFIX, crossNameSetting.getPrefix());
		Assert.assertEquals(CROSS_NAME_SUFFIX, crossNameSetting.getSuffix());
		Assert.assertEquals(CrossNameSetting.DEFAULT_SEPARATOR, crossNameSetting.getSeparator());
		Assert.assertEquals(true, crossNameSetting.isSaveParentageDesignationAsAString());

	}

	@Test
	public void testValidateStartNumberFieldValid() {

		Assert.assertTrue(this.crossingSettingsNameComponent.validateStartNumberField());

	}

	@Test
	public void testValidateStartNumberFieldStartingNumberHasTooManyDigits() {

		Mockito.when(this.startNumberTextField.getValue()).thenReturn("10000000000");

		Assert.assertFalse(this.crossingSettingsNameComponent.validateStartNumberField());

		Mockito.verify(this.messageSource).getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS);

	}

	@Test
	public void testValidateStartNumberFieldInvalid() {

		Mockito.when(this.startNumberTextField.getValue()).thenReturn("JH1");

		Assert.assertFalse(this.crossingSettingsNameComponent.validateStartNumberField());

		Mockito.verify(this.messageSource).getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER);

	}

	private CrossNameSetting createNameSetting() {

		final CrossNameSetting crossNameSetting = new CrossNameSetting();

		crossNameSetting.setAddSpaceBetweenPrefixAndCode(true);
		crossNameSetting.setAddSpaceBetweenSuffixAndCode(true);
		crossNameSetting.setNumOfDigits(LEADING_ZEROS);
		crossNameSetting.setPrefix(CROSS_NAME_PREFIX);
		crossNameSetting.setSuffix(CROSS_NAME_SUFFIX);
		crossNameSetting.setSaveParentageDesignationAsAString(true);
		crossNameSetting.setSeparator(CrossNameSetting.DEFAULT_SEPARATOR);
		crossNameSetting.setStartNumber(Integer.parseInt(START_NUMBER));

		return crossNameSetting;
	}

	private void initializeFieldValues() {

		Mockito.when(this.crossNamePrefix.getValue()).thenReturn(CROSS_NAME_PREFIX);
		Mockito.when(this.crossNameSuffix.getValue()).thenReturn(CROSS_NAME_SUFFIX);
		Mockito.when(this.addSpaceBetPrefixAndCodeOptionGroup.getValue()).thenReturn(ConfirmOption.YES);
		Mockito.when(this.addSpaceBetSuffixAndCodeOptionGroup.getValue()).thenReturn(ConfirmOption.YES);
		Mockito.when(this.leadingZerosSelect.getValue()).thenReturn(9);
		Mockito.when(this.saveParentageDesignationAsAStringGroup.getValue()).thenReturn(ConfirmOption.YES);
		Mockito.when(this.separatorTextField.getValue()).thenReturn(CrossNameSetting.DEFAULT_SEPARATOR);
		Mockito.when(this.startNumberTextField.getValue()).thenReturn(START_NUMBER);

	}

}

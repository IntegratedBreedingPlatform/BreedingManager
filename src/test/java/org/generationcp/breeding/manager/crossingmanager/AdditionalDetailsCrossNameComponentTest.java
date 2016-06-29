
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class AdditionalDetailsCrossNameComponentTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private FillWith fillWithSource;

	@Mock
	private Window parentWindow;

	@InjectMocks
	private AdditionalDetailsCrossNameComponent additionalDetailCNComponent;

	private Button okButton;

	@Before
	public void setUp() throws Exception {
		this.additionalDetailCNComponent.setMessageSource(this.messageSource);
		this.additionalDetailCNComponent.setParent(this.parentWindow);
		Mockito.when(this.parentWindow.getParent()).thenReturn(new Window());

		Mockito.when(this.messageSource.getMessage(Matchers.anyString())).thenReturn("Please specify a prefix");
		this.additionalDetailCNComponent.afterPropertiesSet();

		Mockito.when(this.fillWithSource.getNumberOfEntries()).thenReturn(1);
		this.additionalDetailCNComponent.setFillWithSource(this.fillWithSource);

		this.okButton = this.additionalDetailCNComponent.getOkButton();
	}

	@Test
	public void testButtonClickWithoutPrefix() {
		this.okButton.click();
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.PLEASE_SPECIFY_A_PREFIX);
	}

	@Test
	public void testButtonClickWithoutStartingNumber() {
		this.additionalDetailCNComponent.setPrefixTextFieldValue("Prefix");

		this.okButton.click();
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.PLEASE_SPECIFY_A_STARTING_NUMBER);
	}

	@Test
	public void testButtonClickWhereStartingNumberGreaterThanAllowed() {
		this.additionalDetailCNComponent.setPrefixTextFieldValue("Prefix");
		this.additionalDetailCNComponent.setSequenceNumCheckBoxValue(true);
		this.additionalDetailCNComponent.setLeadingZerosSelectValue(1);
		this.additionalDetailCNComponent.setStartingNumberTextFieldValue("10");

		this.okButton.click();
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.STARTING_NUMBER_IS_GREATER_THAN_THE_ALLOWED_NO_OF_DIGITS);
	}

	@Test
	public void testButtonClickWhereStartingNumberHasTooManyDigits() {
		this.additionalDetailCNComponent.setPrefixTextFieldValue("Prefix");
		this.additionalDetailCNComponent.setStartingNumberTextFieldValue("1111111111");

		this.okButton.click();
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS);
	}

	@Test
	public void testButtonClickWhereStartingNumberInValid() {
		this.additionalDetailCNComponent.setPrefixTextFieldValue("Prefix");
		this.additionalDetailCNComponent.setStartingNumberTextFieldValue("x");

		this.okButton.click();
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER);
	}

	@Test
	public void testButtonClickWhereSequenceForSeedSourceTooLong() {
		this.additionalDetailCNComponent.setPrefixTextFieldValue(
				"SEQUENCETOLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONG");
		this.additionalDetailCNComponent.setStartingNumberTextFieldValue("1");
		this.additionalDetailCNComponent.setPropertyIdtoFillValue(ColumnLabels.SEED_SOURCE.getName());

		this.okButton.click();
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SEQUENCE_TOO_LONG_FOR_SEED_SOURCE);
	}

	@Test
	public void testButtonClickWhereSequenceForEntryCodeTooLong() {
		this.additionalDetailCNComponent
				.setPrefixTextFieldValue("SEQUENCETOLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONG");
		this.additionalDetailCNComponent.setStartingNumberTextFieldValue("1");
		this.additionalDetailCNComponent.setPropertyIdtoFillValue(ColumnLabels.ENTRY_CODE.getName());

		this.okButton.click();
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SEQUENCE_TOO_LONG_FOR_ENTRY_CODE);
	}

	@Test
	public void testButtonClickSuccess() {
		this.additionalDetailCNComponent.setPrefixTextFieldValue("SEQUENCETOLOOOOOOOOOOOONG");
		this.additionalDetailCNComponent.setStartingNumberTextFieldValue("1");
		this.additionalDetailCNComponent.setPropertyIdtoFillValue(ColumnLabels.ENTRY_CODE.getName());

		this.okButton.click();
		Mockito.verify(this.parentWindow, Mockito.times(1)).getParent();
	}
}

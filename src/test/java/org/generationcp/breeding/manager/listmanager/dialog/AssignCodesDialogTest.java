
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodesNamingLayout;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmNamingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

import junit.framework.Assert;

public class AssignCodesDialogTest {

	private static final String PREFIX = "AAA";
	private static final String SUFFIX = "XYZ";
	private static final String CODING_LEVEL_CODE1 = "CODE1";
	private static final String CODING_LEVEL_NAME1 = "Code 1";
	private static final String CODING_LEVEL_CODE2 = "CODE2";
	private static final String CODING_LEVEL_NAME2 = "Code 2";
	private static final String CODING_LEVEL_CODE3 = "CODE3";
	private static final String CODING_LEVEL_NAME3 = "Code 3";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmNamingService germplasmNamingService;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private AssignCodesNamingLayout assignCodesNamingLayout;

	@Mock
	private Window parent;

	@InjectMocks
	private AssignCodesDialog assignCodesDialog;

	@Mock
	private OptionGroup codingLevelOptions;

	@Mock
	private GermplasmListManager germplasmListManager;

	private UserDefinedField nameType;

	private GermplasmNameSetting setting;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.assignCodesDialog.setAssignCodesNamingLayout(this.assignCodesNamingLayout);
		this.assignCodesDialog.setMessageSource(this.messageSource);
		this.assignCodesDialog.setParent(this.parent);
		this.assignCodesDialog.setGidsToProcess(this.createGidsToProcess());
		this.assignCodesDialog.setTransactionManager(this.transactionManager);
		this.assignCodesDialog.setCodingLevelOptions(this.codingLevelOptions);
		this.assignCodesDialog.setGermplasmNamingService(this.germplasmNamingService);
		this.assignCodesDialog.setGermplasmListManager(this.germplasmListManager);

		this.setting = this.createGermplasmNameSetting();
		Mockito.doReturn(this.setting).when(this.assignCodesNamingLayout).generateGermplasmNameSetting();
		this.nameType = this.createUserDefinedField(1, CODING_LEVEL_CODE1, CODING_LEVEL_NAME1);
		Mockito.doReturn(this.nameType).when(this.codingLevelOptions).getValue();
	}

	@Test
	public void testAssignCodes() {
		this.assignCodesDialog.assignCodes();

		// Make sure that the codes are assigned to all GIDs
		this.verifyAssignCodesActions();

	}

	private void verifyAssignCodesActions() {
		Mockito.verify(this.germplasmNamingService).applyGroupNames(this.createGidsToProcess(), this.setting, this.nameType, 0, 0);

		Mockito.verify(this.parent).addWindow(Mockito.any(AssignCodesResultsDialog.class));
		Mockito.verify(this.parent).removeWindow(this.assignCodesDialog);
	}

	@Test
	public void testInstantiateButtons() {
		this.assignCodesDialog.instantiateButtons();

		final Button continueButton = this.assignCodesDialog.getContinueButton();
		Assert.assertFalse(continueButton.isEnabled());
	}

	@Test
	public void testAddListeners() {
		this.assignCodesDialog.instantiateButtons();
		this.assignCodesDialog.addListeners();

		Mockito.verify(this.assignCodesNamingLayout).addListeners();
		// Test Continue Button click
		final Button continueButton = this.assignCodesDialog.getContinueButton();
		continueButton.setEnabled(true);
		continueButton.click();
		Mockito.verify(this.assignCodesNamingLayout).validate();
		this.verifyAssignCodesActions();
	}
	
	@Test
	public void testClickCancelButton() {
		this.assignCodesDialog.instantiateButtons();
		this.assignCodesDialog.addListeners();

		Mockito.verify(this.assignCodesNamingLayout).addListeners();
		// Test Cancel Button click
		final Button cancelButton = this.assignCodesDialog.getCancelButton();
		cancelButton.click();
		Mockito.verify(this.parent).removeWindow(this.assignCodesDialog);
	}

	@Test
	public void testClickContinueButtonWhenThereIsNamingValidationError() {
		this.assignCodesDialog.instantiateButtons();
		this.assignCodesDialog.addListeners();
		final String errorMessage = "Prefix cannot be empty";
		Mockito.doThrow(new Validator.InvalidValueException(errorMessage)).when(this.assignCodesNamingLayout).validate();

		final Button continueButton = this.assignCodesDialog.getContinueButton();
		continueButton.setEnabled(true);
		continueButton.click();
		Mockito.verify(this.germplasmNamingService, Mockito.never()).applyGroupName(Mockito.anyInt(),
				Mockito.any(GermplasmNameSetting.class), Mockito.any(UserDefinedField.class), Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(this.parent, Mockito.never()).addWindow(Mockito.any(AssignCodesResultsDialog.class));
		Mockito.verify(this.parent, Mockito.never()).removeWindow(this.assignCodesDialog);
	}

	@Test
	public void testInitializeValues() {
		this.setupTestNameTypes();
		final OptionGroup codingLevelOptions = new OptionGroup();
		this.assignCodesDialog.setCodingLevelOptions(codingLevelOptions);
		this.assignCodesDialog.initializeValues();

		Assert.assertEquals(3, codingLevelOptions.getItemIds().size());
		Assert.assertEquals(this.nameType, codingLevelOptions.getValue());
	}

	private void setupTestNameTypes() {
		final List<UserDefinedField> nameTypes = new ArrayList<>();
		nameTypes.add(this.nameType);
		nameTypes.add(this.createUserDefinedField(2, "CRSNM", "CROSS NAME"));
		nameTypes.add(this.createUserDefinedField(3, CODING_LEVEL_CODE2, CODING_LEVEL_NAME2));
		nameTypes.add(this.createUserDefinedField(4, CODING_LEVEL_CODE3, CODING_LEVEL_NAME3));
		nameTypes.add(this.createUserDefinedField(5, "LNAME", "LINE NAME"));
		Mockito.doReturn(nameTypes).when(this.germplasmListManager).getGermplasmNameTypes();
	}

	@Test
	public void testIsCodingNameType() {
		Assert.assertTrue(this.assignCodesDialog.isCodingNameType("CODE 1"));
		Assert.assertTrue(this.assignCodesDialog.isCodingNameType("Code 2"));
		Assert.assertTrue(this.assignCodesDialog.isCodingNameType("code 3"));
		Assert.assertTrue(this.assignCodesDialog.isCodingNameType("CODE1"));
		Assert.assertTrue(this.assignCodesDialog.isCodingNameType("Code2"));
		Assert.assertTrue(this.assignCodesDialog.isCodingNameType("code3"));

		Assert.assertFalse(this.assignCodesDialog.isCodingNameType("CODE 1 ABC"));
		Assert.assertFalse(this.assignCodesDialog.isCodingNameType("CROSS CODE"));
	}
	
	@Test
	public void testWindowClose() {
		this.assignCodesDialog.windowClose(Mockito.mock(CloseEvent.class));
		Mockito.verify(this.parent).removeWindow(this.assignCodesDialog);
	}
	
	@Test
	public void testUpdateLabels() {
		this.assignCodesDialog.instantiateButtons();
		this.assignCodesDialog.updateLabels();
		
		Mockito.verify(this.messageSource).setCaption(this.assignCodesDialog, Message.ASSIGN_CODES_HEADER);
		Mockito.verify(this.messageSource).setCaption(this.assignCodesDialog.getContinueButton(), Message.APPLY_CODES);
		Mockito.verify(this.messageSource).setCaption(this.assignCodesDialog.getCancelButton(), Message.CANCEL);
	}

	private Set<Integer> createGidsToProcess() {

		final Set<Integer> gidsToProcess = new LinkedHashSet<>();

		gidsToProcess.add(1);
		gidsToProcess.add(2);
		gidsToProcess.add(3);

		return gidsToProcess;

	}

	private UserDefinedField createUserDefinedField(final Integer fieldNo, final String fieldCode, final String fieldName) {
		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFldno(fieldNo);
		userDefinedField.setFcode(fieldCode);
		userDefinedField.setFname(fieldName);
		return userDefinedField;

	}

	private GermplasmNameSetting createGermplasmNameSetting() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();

		setting.setPrefix(AssignCodesDialogTest.PREFIX);
		setting.setSuffix(AssignCodesDialogTest.SUFFIX);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(7);

		return setting;
	}

}

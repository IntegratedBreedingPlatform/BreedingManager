package org.generationcp.breeding.manager.listmanager.dialog;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import junit.framework.Assert;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.MandatoryMarkLabel;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodesNamingLayout;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.service.GermplasmCodeGenerationService;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.pojos.workbench.NamingConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private GermplasmCodeGenerationService germplasmCodeGenerationService;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private AssignCodesNamingLayout assignCodesNamingLayout;

	@Mock
	private Window parent;

	@Mock
	private OptionGroup codingLevelOptions;

	@Mock
	private GermplasmListManager germplasmListManager;

	@InjectMocks
	private AssignCodesDialog assignCodesDialog;

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
		this.assignCodesDialog.setGermplasmCodeGenerationService(this.germplasmCodeGenerationService);
		this.assignCodesDialog.setGermplasmListManager(this.germplasmListManager);
		this.assignCodesDialog.setWorkbenchDataManager(this.workbenchDataManager);

		final OptionGroup codingLevelOptions = new OptionGroup();
		final OptionGroup namingOptions = new OptionGroup();
		this.assignCodesDialog.setCodingLevelOptions(codingLevelOptions);
		this.assignCodesDialog.setNamingOptions(namingOptions);

		this.setting = this.createGermplasmNameSetting();
		Mockito.doReturn(this.setting).when(this.assignCodesNamingLayout).generateGermplasmNameSetting();
		this.nameType = this.createUserDefinedField(1, CODING_LEVEL_CODE1, CODING_LEVEL_NAME1);
		Mockito.doReturn(this.nameType).when(this.codingLevelOptions).getValue();
	}

	@Test
	public void testGenerateCodeNamesManualNaming() {

		this.assignCodesDialog.initializeValues();
		this.assignCodesDialog.getNamingOptions().setValue(AssignCodesDialog.NAMING_OPTION.MANUAL);
		this.assignCodesDialog.setCodingLevelOptions(this.codingLevelOptions);
		this.assignCodesDialog.generateCodeNames();

		// Make sure that the codes are assigned to all GIDs
		Mockito.verify(this.germplasmCodeGenerationService).applyGroupNames(this.createGidsToProcess(), this.setting, this.nameType, 0, 0);
		Mockito.verify(this.parent).addWindow(Mockito.any(AssignCodesResultsDialog.class));
		Mockito.verify(this.parent).removeWindow(this.assignCodesDialog);

	}

	@Test
	public void testGenerateCodeNamesAutomaticNaming() throws RuleException {

		this.assignCodesDialog.initializeValues();
		this.assignCodesDialog.getNamingOptions().setValue(AssignCodesDialog.NAMING_OPTION.AUTOMATIC);
		this.assignCodesDialog.setCodingLevelOptions(this.codingLevelOptions);

		final NamingConfiguration namingConfiguration = new NamingConfiguration();
		Mockito.when(workbenchDataManager.getNamingConfigurationByName(nameType.getFname())).thenReturn(namingConfiguration);

		this.assignCodesDialog.generateCodeNames();

		// Make sure that the codes are assigned to all GIDs
		Mockito.verify(this.germplasmCodeGenerationService).applyGroupNames(this.createGidsToProcess(), namingConfiguration, this.nameType);
		Mockito.verify(this.parent).addWindow(Mockito.any(AssignCodesResultsDialog.class));
		Mockito.verify(this.parent).removeWindow(this.assignCodesDialog);

	}

	@Test
	public void testGenerateCodeNamesAutomaticNamingWithRuleException() throws RuleException {

		this.assignCodesDialog.initializeValues();
		this.assignCodesDialog.getNamingOptions().setValue(AssignCodesDialog.NAMING_OPTION.AUTOMATIC);
		this.assignCodesDialog.setCodingLevelOptions(this.codingLevelOptions);

		final NamingConfiguration namingConfiguration = new NamingConfiguration();
		Mockito.when(workbenchDataManager.getNamingConfigurationByName(nameType.getFname())).thenReturn(namingConfiguration);
		Mockito.when(this.germplasmCodeGenerationService.applyGroupNames(this.createGidsToProcess(), namingConfiguration, this.nameType))
				.thenThrow(new RuleException(""));

		this.assignCodesDialog.generateCodeNames();

		Mockito.verify(this.messageSource).getMessage(Message.ASSIGN_CODES);
		Mockito.verify(this.parent).showNotification(Mockito.any(Window.Notification.class));
		Mockito.verify(this.parent).addWindow(Mockito.any(AssignCodesResultsDialog.class));
		Mockito.verify(this.parent).removeWindow(this.assignCodesDialog);

	}

	@Test
	public void testInstantiateButtons() {
		this.assignCodesDialog.instantiateButtons();
		Assert.assertNotNull(this.assignCodesDialog.getContinueButton());
		Assert.assertNotNull(this.assignCodesDialog.getCancelButton());
	}

	@Test
	public void testAddListeners() {
		this.assignCodesDialog.instantiateButtons();
		this.assignCodesDialog.addListeners();

		Mockito.verify(this.assignCodesNamingLayout).addListeners();
		// Test Continue Button click
		final Button continueButton = this.assignCodesDialog.getContinueButton();
		continueButton.setEnabled(true);
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
		Mockito.verify(this.germplasmCodeGenerationService, Mockito.never())
				.applyGroupName(Mockito.anyInt(), Mockito.any(GermplasmNameSetting.class), Mockito.any(UserDefinedField.class),
						Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(this.parent, Mockito.never()).addWindow(Mockito.any(AssignCodesResultsDialog.class));
		Mockito.verify(this.parent, Mockito.never()).removeWindow(this.assignCodesDialog);
	}

	@Test
	public void testNamingOptionValueChangedToAutomatic() {

		this.assignCodesDialog.instantiateButtons();
		this.assignCodesDialog.addListeners();

		final VerticalLayout manualCodeNamingLayout = new VerticalLayout();
		this.assignCodesDialog.setManualCodeNamingLayout(manualCodeNamingLayout);
		final Field.ValueChangeEvent event = Mockito.mock(Field.ValueChangeEvent.class);
		final Property property = Mockito.mock(Property.class);
		Mockito.when(property.getValue()).thenReturn(AssignCodesDialog.NAMING_OPTION.AUTOMATIC);
		Mockito.when(event.getProperty()).thenReturn(property);

		this.assignCodesDialog.getNamingOptions().valueChange(event);

		Assert.assertFalse(manualCodeNamingLayout.isVisible());
		Assert.assertTrue(assignCodesDialog.getContinueButton().isEnabled());
		Assert.assertEquals(AssignCodesDialog.DEFAULT_DIALOG_HEIGHT, Math.round(assignCodesDialog.getHeight()) + "px");

	}

	@Test
	public void testNamingOptionValueChangedToManual() {

		this.assignCodesDialog.instantiateButtons();
		this.assignCodesDialog.addListeners();

		final VerticalLayout manualCodeNamingLayout = new VerticalLayout();
		this.assignCodesDialog.setManualCodeNamingLayout(manualCodeNamingLayout);
		final Field.ValueChangeEvent event = Mockito.mock(Field.ValueChangeEvent.class);
		final Property property = Mockito.mock(Property.class);
		Mockito.when(property.getValue()).thenReturn(AssignCodesDialog.NAMING_OPTION.MANUAL);
		Mockito.when(event.getProperty()).thenReturn(property);

		this.assignCodesDialog.getNamingOptions().valueChange(event);

		Assert.assertTrue(manualCodeNamingLayout.isVisible());
		Assert.assertFalse(assignCodesDialog.getContinueButton().isEnabled());
		Assert.assertEquals(AssignCodesDialog.DEFAULT_DIALOG_HEIGHT_FOR_MANUAL_NAMING, Math.round(assignCodesDialog.getHeight()) + "px");

	}

	@Test
	public void testInitializeValues() {
		this.setupTestNameTypes();
		this.assignCodesDialog.initializeValues();

		final OptionGroup codingLevel = this.assignCodesDialog.getCodingLevelOptions();
		final OptionGroup naming = this.assignCodesDialog.getNamingOptions();

		Assert.assertEquals(3, codingLevel.getItemIds().size());
		Assert.assertEquals(this.nameType, codingLevel.getValue());
		Assert.assertEquals(2, naming.getItemIds().size());
		Assert.assertEquals(AssignCodesDialog.NAMING_OPTION.AUTOMATIC, naming.getValue());
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

	@Test
	public void testCreateNamingAndCodeLevelGridLayout() {

		final GridLayout gridLayout = this.assignCodesDialog.createNamingAndCodeLevelGridLayout(new OptionGroup(), new OptionGroup());
		Assert.assertNotNull(gridLayout.getComponent(0, 0));
		Assert.assertNotNull(gridLayout.getComponent(1, 0));
		Assert.assertNotNull(gridLayout.getComponent(0, 1));
		Assert.assertNotNull(gridLayout.getComponent(1, 1));

		Assert.assertEquals(0.3f, gridLayout.getColumnExpandRatio(0));
		Assert.assertEquals(0.7f, gridLayout.getColumnExpandRatio(1));
		Assert.assertEquals(100.0f, gridLayout.getWidth());
	}

	@Test
	public void testCreateCodingLevelOptionsLabelLayout() {

		final String codingLevel = "Coding Level";
		Mockito.when(messageSource.getMessage(Message.CODING_LEVEL)).thenReturn(codingLevel);

		final HorizontalLayout horizontalLayout = this.assignCodesDialog.createCodingLevelOptionsLabelLayout();

		final Label codingLevelLabel = (Label) horizontalLayout.getComponent(0);
		Assert.assertNotNull(codingLevelLabel);
		Assert.assertEquals(codingLevel, codingLevelLabel.getValue());
		Assert.assertTrue(horizontalLayout.getComponent(1) instanceof MandatoryMarkLabel);

	}

	@Test
	public void testCreateNamingOptionsLabelLayout() {

		final String naming = "Naming";
		Mockito.when(messageSource.getMessage(Message.NAMING)).thenReturn(naming);

		final HorizontalLayout horizontalLayout = this.assignCodesDialog.createNamingOptionsLabelLayout();

		final Label namingLabel = (Label) horizontalLayout.getComponent(0);
		Assert.assertNotNull(namingLabel);
		Assert.assertEquals(naming, namingLabel.getValue());
		Assert.assertTrue(horizontalLayout.getComponent(1) instanceof MandatoryMarkLabel);

	}

	@Test
	public void testCreateManualCodeNamingLayout() {

		final VerticalLayout verticalLayout = this.assignCodesDialog.createManualCodeNamingLayout();
		Assert.assertTrue(verticalLayout.isImmediate());
		Assert.assertFalse(verticalLayout.isVisible());
		Assert.assertEquals(270.0f, verticalLayout.getHeight());
		Assert.assertEquals(100.0f, verticalLayout.getWidth());

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

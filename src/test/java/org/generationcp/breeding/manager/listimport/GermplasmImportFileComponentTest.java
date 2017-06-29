
package org.generationcp.breeding.manager.listimport;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.validator.ShowNameHandlingPopUpValidator;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.components.validator.ErrorCollection;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Window;

public class GermplasmImportFileComponentTest {

	public static final String DUMMY_MESSAGE = "DUMMY_MESSAGE";
	private GermplasmImportFileComponent importFileComponent;

	private Window importWindow;

	@Mock
	private GermplasmImportMain importMain;
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private GermplasmListUploader germplasmListUploader;

	@Mock
	private ShowNameHandlingPopUpValidator showNameHandlingPopUpValidator;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		this.importFileComponent = new GermplasmImportFileComponent(this.importMain);
		this.importFileComponent.setGermplasmListUploader(this.germplasmListUploader);
		this.importFileComponent.setShowNameHandlingPopUpValidationRule(this.showNameHandlingPopUpValidator);

		this.importWindow = new Window();
		doReturn(this.importWindow).when(this.importMain).getWindow();
	}

	@Test
	public void testCancelActionFromMainImportTool() {
		this.importFileComponent.cancelButtonAction();
		verify(this.importMain).reset();
	}

	@Test
	public void testCancelActionFromListManager() {
		final GermplasmImportPopupSource popupSource = Mockito.mock(GermplasmImportPopupSource.class);
		final Window listManagerWindow = new Window();
		listManagerWindow.addWindow(this.importWindow);
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("List Manager Window has Germplasm Import sub-window", !listManagerWindow.getChildWindows().isEmpty());

		doReturn(popupSource).when(this.importMain).getGermplasmImportPopupSource();
		doReturn(this.importWindow).when(this.importMain).getComponentContainer();
		doReturn(listManagerWindow).when(popupSource).getParentWindow();

		this.importFileComponent.cancelButtonAction();
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("Germplasm Import sub-window should have been closed", listManagerWindow.getChildWindows().isEmpty());

	}

	@Test
	public void testCancelActionFromFieldbook() {
		this.importWindow = Mockito.mock(Window.class);
		doReturn(this.importWindow).when(this.importMain).getWindow();

		doReturn(true).when(this.importMain).isViaPopup();

		this.importFileComponent.cancelButtonAction();
		verify(this.importMain).reset();
		verify(this.importWindow).executeJavaScript(GermplasmImportFileComponent.FB_CLOSE_WINDOW_JS_CALL);
	}

	private List<UserDefinedField> createUserDefinedFieldsForNameType() {
		final List<UserDefinedField> validNameTypes = new ArrayList<UserDefinedField>();
		validNameTypes.add(new UserDefinedField(5, "NAMES", "NAME", "DRVNM", "DERIVATIVE NAMES", "", "", 0, 0, 0, 0));
		return validNameTypes;
	}

	private ImportedGermplasmList initImportedGermplasmList(final boolean withNameFactors) {
		final ImportedGermplasmList importedGermplasmList =
				ImportedGermplasmListDataInitializer.createImportedGermplasmList(10, withNameFactors);

		doReturn(this.createUserDefinedFieldsForNameType()).when(this.germplasmDataManager).getUserDefinedFieldByFieldTableNameAndType(
				RowColumnType.NAME_TYPES.getFtable(), RowColumnType.NAME_TYPES.getFtype());
		doReturn(importedGermplasmList).when(this.germplasmListUploader).getImportedGermplasmList();

		return importedGermplasmList;
	}

	@Test
	public void testNextStepShowsNameHandlingDialogWhenThereIsImportedNameFactor() {
		final ImportedGermplasmList importedGermplasmList = this.initImportedGermplasmList(true);
		final GermplasmImportPopupSource importPopupSource = Mockito.mock(GermplasmImportPopupSource.class);
		doReturn(importPopupSource).when(this.importMain).getGermplasmImportPopupSource();
		doReturn(new Window()).when(importPopupSource).getParentWindow();
		final List<ImportedGermplasm> list = importedGermplasmList.getImportedGermplasm();
		final ErrorCollection success = new ErrorCollection();
		when(this.showNameHandlingPopUpValidator.validate(list)).thenReturn(success);

		this.importFileComponent.nextStep();

		verify(this.importMain, Mockito.times(0)).nextStep();
	}

	@Test
	public void testNextStepGoesDirectlyToNextScreenWhenThereIsNoImportedNameFactor() {
		final ImportedGermplasmList importedGermplasmList = this.initImportedGermplasmList(false);
		final List<ImportedGermplasm> list = importedGermplasmList.getImportedGermplasm();
		final ErrorCollection error = new ErrorCollection();
		error.add(DUMMY_MESSAGE);
		when(this.showNameHandlingPopUpValidator.validate(list)).thenReturn(error);

		this.importFileComponent.nextStep();

		verify(this.importMain, Mockito.times(1)).nextStep();
	}
}

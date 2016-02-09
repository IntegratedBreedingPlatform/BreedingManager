
package org.generationcp.breeding.manager.listimport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.workbook.generator.RowColumnType;
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

	private GermplasmImportFileComponent importFileComponent;

	private Window importWindow;

	@Mock
	private GermplasmImportMain importMain;
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private GermplasmListUploader germplasmListUploader;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		this.importFileComponent = new GermplasmImportFileComponent(this.importMain);
		this.importFileComponent.setGermplasmDataManager(this.germplasmDataManager);
		this.importFileComponent.setGermplasmListUploader(this.germplasmListUploader);

		this.importWindow = new Window();
		Mockito.doReturn(this.importWindow).when(this.importMain).getWindow();
	}

	@Test
	public void testCancelActionFromMainImportTool() {
		this.importFileComponent.cancelButtonAction();
		Mockito.verify(this.importMain).reset();
	}

	@Test
	public void testCancelActionFromListManager() {
		final GermplasmImportPopupSource popupSource = Mockito.mock(GermplasmImportPopupSource.class);
		final Window listManagerWindow = new Window();
		listManagerWindow.addWindow(this.importWindow);
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("List Manager Window has Germplasm Import sub-window", !listManagerWindow.getChildWindows().isEmpty());

		Mockito.doReturn(popupSource).when(this.importMain).getGermplasmImportPopupSource();
		Mockito.doReturn(this.importWindow).when(this.importMain).getComponentContainer();
		Mockito.doReturn(listManagerWindow).when(popupSource).getParentWindow();

		this.importFileComponent.cancelButtonAction();
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("Germplasm Import sub-window should have been closed", listManagerWindow.getChildWindows().isEmpty());

	}

	@Test
	public void testCancelActionFromFieldbook() {
		this.importWindow = Mockito.mock(Window.class);
		Mockito.doReturn(this.importWindow).when(this.importMain).getWindow();

		Mockito.doReturn(true).when(this.importMain).isViaPopup();

		this.importFileComponent.cancelButtonAction();
		Mockito.verify(this.importMain).reset();
		Mockito.verify(this.importWindow).executeJavaScript(GermplasmImportFileComponent.FB_CLOSE_WINDOW_JS_CALL);
	}

	@Test
	public void testExtractListOfImportedNamesWithNoNameFactors() {
		this.initImportedGermplasmList(false);
		final List<ImportedFactor> importedNameFactors = this.importFileComponent.extractListOfImportedNames();
		Assert.assertTrue(
				"Expected to return an empty list for name factors since there is no name factors included from the imported file.",
				importedNameFactors.isEmpty());
	}

	@Test
	public void testExtractListOfImportedNamesWithNameFactors() {
		this.initImportedGermplasmList(true);
		final List<ImportedFactor> importedNameFactors = this.importFileComponent.extractListOfImportedNames();
		Assert.assertTrue("Expected to return a list with name factors since there is name factors included from the imported file.",
				!importedNameFactors.isEmpty());
	}

	private List<UserDefinedField> createUserDefinedFieldsForNameType() {
		final List<UserDefinedField> validNameTypes = new ArrayList<UserDefinedField>();
		validNameTypes.add(new UserDefinedField(5, "NAMES", "NAME", "DRVNM", "DERIVATIVE NAMES", "", "", 0, 0, 0, 0));
		return validNameTypes;
	}

	private void initImportedGermplasmList(final boolean withNameFactors) {
		final ImportedGermplasmList importedGermplasmList =
				new ImportedGermplasmList("GermplasmImportTemplate.xls", "", "", "", new Date());
		importedGermplasmList.setImportedFactors(this.createImportedFactors(withNameFactors));

		Mockito.doReturn(this.createUserDefinedFieldsForNameType()).when(this.germplasmDataManager)
				.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.NAME_TYPES.getFtable(), RowColumnType.NAME_TYPES.getFtype());
		Mockito.doReturn(importedGermplasmList).when(this.germplasmListUploader).getImportedGermplasmList();
	}

	private List<ImportedFactor> createImportedFactors(final boolean withNameFactors) {
		final List<ImportedFactor> importedFactors = new ArrayList<ImportedFactor>();

		importedFactors.add(new ImportedFactor("ENTRY", "The germplasm entry number", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", "C", ""));
		importedFactors.add(new ImportedFactor("DESIGNATION", "The name of the germplasm", "GERMPLASM ID", "DBCV", "ASSIGNED", "C", ""));
		if (withNameFactors) {
			importedFactors.add(new ImportedFactor("DRVNM", "Derivative Name", "GERMPLASM ID", "NAME", "ASSIGNED", "C", ""));
		}

		return importedFactors;
	}

	@Test
	public void testNextStepShowsNameHandlingDialogWhenThereIsImportedNameFactor() {
		this.initImportedGermplasmList(true);
		this.importFileComponent.nextStep();
		Mockito.verify(this.importMain, Mockito.times(0)).nextStep();
	}

	@Test
	public void testNextStepGoesDirectlyToNextScreenWhenThereIsNoImportedNameFactor() {
		this.initImportedGermplasmList(false);
		this.importFileComponent.nextStep();
		Mockito.verify(this.importMain, Mockito.times(1)).nextStep();
	}
}

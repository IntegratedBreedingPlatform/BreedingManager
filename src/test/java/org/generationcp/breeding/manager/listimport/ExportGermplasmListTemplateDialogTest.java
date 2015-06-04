
package org.generationcp.breeding.manager.listimport;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.commons.lang.reflect.FieldUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchSetting;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.Application;
import com.vaadin.ui.Component;

public class ExportGermplasmListTemplateDialogTest {

	private static final String DUMMY_STRING = "DUMMY_STRING";

	private static final String BASIC_FILENAME = "GermplasmImportTemplate-Basic-rev4.xls";

	private static final String CROP_TYPE = "maize";

	private static final String EMPTY_STRING = "";

	private static final String INSTALLATION_DIRECTORY = "C:" + File.separator + "InstallationDirectory";

	private ExportGermplasmListTemplateDialog exportDialog;

	@Mock
	private Component source;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private HttpServletRequest request;

	@Before
	public void setUp() throws MiddlewareQueryException, IllegalAccessException {
		MockitoAnnotations.initMocks(this);
		this.exportDialog = new ExportGermplasmListTemplateDialog(this.source);
		this.exportDialog.setWorkbenchDataManager(this.workbenchDataManager);
		this.exportDialog.setMessageSource(this.messageSource);
		FieldUtils.writeDeclaredField(this.exportDialog, "contextUtil", this.contextUtil, true);

		this.exportDialog = Mockito.spy(this.exportDialog);

		Mockito.when(this.messageSource.getMessage(Message.TEMPLATE_FORMAT)).thenReturn(ExportGermplasmListTemplateDialogTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.CHOOSE_A_TEMPLATE_FORMAT)).thenReturn(
				ExportGermplasmListTemplateDialogTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.CANCEL)).thenReturn(ExportGermplasmListTemplateDialogTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.EXPORT)).thenReturn(ExportGermplasmListTemplateDialogTest.DUMMY_STRING);

		this.exportDialog.instantiateComponents();
		this.exportDialog.initializeValues();

		Project project = new Project();
		project.setProjectId(1L);
		CropType cropType = new CropType();
		cropType.setCropName(ExportGermplasmListTemplateDialogTest.CROP_TYPE);
		project.setCropType(cropType);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();
	}

	@Test
	public void testGetInstallationDirectory_ReturnsTheActualInstallationDirectory() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory(ExportGermplasmListTemplateDialogTest.INSTALLATION_DIRECTORY);
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);

		String installationDirectory = this.exportDialog.getInstallationDirectory();
		Assert.assertEquals("Expected to return \"" + "\" but returned \"" + installationDirectory + "\"", installationDirectory,
				ExportGermplasmListTemplateDialogTest.INSTALLATION_DIRECTORY);
	}

	@Test
	public void testGetInstallationDirectory_ReturnsEmptyString() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = null;
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);

		String installationDirectory = this.exportDialog.getInstallationDirectory();
		Assert.assertEquals("Expected to return an empty string but didn't.", installationDirectory,
				ExportGermplasmListTemplateDialogTest.EMPTY_STRING);
	}

	@Test
	public void testGetFileToDownloadPath_ReturnsInstallationDirectoryPath() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory(ExportGermplasmListTemplateDialogTest.INSTALLATION_DIRECTORY);
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);
		Mockito.when(this.exportDialog.getGermplasmTemplateFileName()).thenReturn(ExportGermplasmListTemplateDialogTest.BASIC_FILENAME);

		String fileToDownloadPath = this.exportDialog.getFileToDownloadPath(ExportGermplasmListTemplateDialogTest.BASIC_FILENAME);

		String expectedPath =
				ExportGermplasmListTemplateDialogTest.INSTALLATION_DIRECTORY + "" + File.separator + "Examples" + File.separator + "maize"
						+ File.separator + "templates" + File.separator + "GermplasmImportTemplate-Basic-rev4.xls";

		Assert.assertEquals("Expected to return " + expectedPath + " but returned " + fileToDownloadPath, expectedPath, fileToDownloadPath);
	}

	@Test
	public void testGetFileToDownloadPath_ReturnsDefaultInstallationDirectoryPath() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory("");
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);
		Mockito.when(this.exportDialog.getGermplasmTemplateFileName()).thenReturn(ExportGermplasmListTemplateDialogTest.BASIC_FILENAME);

		String fileToDownloadPath = this.exportDialog.getFileToDownloadPath(ExportGermplasmListTemplateDialogTest.BASIC_FILENAME);

		String expectedPath =
				"C:" + File.separator + "Breeding Management System" + File.separator + "Examples" + File.separator + "maize"
						+ File.separator + "templates" + File.separator + "GermplasmImportTemplate-Basic-rev4.xls";

		Assert.assertEquals("Expected to return " + expectedPath + " but returned " + fileToDownloadPath, expectedPath, fileToDownloadPath);
	}

	@Test
	public void testCreateFileDownloadResource() {
		Mockito.doReturn(Mockito.mock(Application.class)).when(this.source).getApplication();

		String fileToDownloadPath =
				"C:" + File.separator + "Breeding Management System" + File.separator + "Examples" + File.separator + "maize"
						+ File.separator + "templates" + File.separator + "GermplasmImportTemplate-Basic-rev4.xls";

		File fileToDownload = new File(fileToDownloadPath);
		if (fileToDownload.exists()) {
			try {
				this.exportDialog.createFileDownloadResource(fileToDownloadPath);
			} catch (IOException e) {
				Assert.fail("should not throw an exception here.");
			}
		} else {
			try {
				this.exportDialog.createFileDownloadResource(fileToDownloadPath);
			} catch (IOException e) {
				Assert.assertEquals("Germplasm Template File does not exist.", e.getMessage());
			}
		}
	}

	@Test
	public void testIsADefaultCrop_ReturnsTrueForDefaultCrop() {
		Assert.assertTrue("Expecting that Maize is a default crop but didn't", this.exportDialog.isADefaultCrop("maize"));
		Assert.assertTrue("Expecting that Chickpea is a default crop but didn't", this.exportDialog.isADefaultCrop("CHICKPEA"));
	}

	@Test
	public void testIsADefaultCrop_ReturnsFalseForCustomCrop() {
		Assert.assertFalse("Expecting that Banana is not a default crop but didn't", this.exportDialog.isADefaultCrop("banana"));
	}
}

package org.generationcp.breeding.manager.listimport;

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.Application;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchSetting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListTemplateDownloaderTest {
	private static final String CROP_TYPE = "maize";
	private static final String INSTALLATION_DIRECTORY = "C:" + File.separator + "InstallationDirectory";

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private GermplasmListTemplateDownloader exportDialog = spy(new GermplasmListTemplateDownloader());

	@Before
	public void setUp() throws MiddlewareQueryException, IllegalAccessException {
		Project project = new Project();
		project.setProjectId(1L);
		CropType cropType = new CropType();
		cropType.setCropName(GermplasmListTemplateDownloaderTest.CROP_TYPE);
		project.setCropType(cropType);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();
	}

	@Test
	public void testGetInstallationDirectory_ReturnsTheActualInstallationDirectory() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory(GermplasmListTemplateDownloaderTest.INSTALLATION_DIRECTORY);
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);

		String installationDirectory = this.exportDialog.getInstallationDirectory();
		Assert.assertEquals("Expected to return \"" + "\" but returned \"" + installationDirectory + "\"", installationDirectory,
				GermplasmListTemplateDownloaderTest.INSTALLATION_DIRECTORY);
	}

	@Test
	public void testGetInstallationDirectory_ReturnsEmptyString() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = null;
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);

		String installationDirectory = this.exportDialog.getInstallationDirectory();
		Assert.assertTrue("Expected to return an empty string but didn't.", StringUtils.isEmpty(installationDirectory));
	}

	@Test
	public void testGetFileToDownloadPath_ReturnsInstallationDirectoryPath() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory(GermplasmListTemplateDownloaderTest.INSTALLATION_DIRECTORY);
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);

		String fileToDownloadPath = this.exportDialog.getBMSCropTemplateDownloadLocation(GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE,CROP_TYPE);

		String expectedPath =
				GermplasmListTemplateDownloaderTest.INSTALLATION_DIRECTORY + "" + File.separator + "Examples" + File.separator + CROP_TYPE
						+ File.separator + "templates" + File.separator + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE;

		Assert.assertEquals("Expected to return " + expectedPath + " but returned " + fileToDownloadPath, expectedPath, fileToDownloadPath);
	}

	@Test
	public void testGetFileToDownloadPath_ReturnsDefaultInstallationDirectoryPath() throws MiddlewareQueryException {
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory("");
		Mockito.when(this.workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);

		String fileToDownloadPath = this.exportDialog.getBMSCropTemplateDownloadLocation(GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE,CROP_TYPE);

		String expectedPath =
				"C:" + File.separator + "BMS4" + File.separator + "Examples" + File.separator + CROP_TYPE
						+ File.separator + "templates" + File.separator + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE;

		Assert.assertEquals("Expected to return " + expectedPath + " but returned " + fileToDownloadPath, expectedPath, fileToDownloadPath);
	}

	@Test
	public void testCreateFileDownloadResource() {
		Application mockApp = Mockito.mock(Application.class);

		String fileToDownloadPath =
				"C:" + File.separator + "BMS4" + File.separator + "Examples" + File.separator + CROP_TYPE
						+ File.separator + "templates" + File.separator + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE;

		File fileToDownload = new File(fileToDownloadPath);
		if (fileToDownload.exists()) {
			try {
				this.exportDialog.createFileDownloadResource(fileToDownloadPath,mockApp);
			} catch (IOException e) {
				Assert.fail("should not throw an exception here.");
			}
		} else {
			try {
				this.exportDialog.createFileDownloadResource(fileToDownloadPath,mockApp);
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

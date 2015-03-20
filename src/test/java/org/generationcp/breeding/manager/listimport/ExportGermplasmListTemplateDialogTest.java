package org.generationcp.breeding.manager.listimport;

import static org.mockito.Mockito.*;

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
import org.mockito.MockitoAnnotations;

import com.vaadin.Application;
import com.vaadin.ui.Component;

public class ExportGermplasmListTemplateDialogTest {
	
	private static final String DUMMY_STRING = "DUMMY_STRING";

	private static final String BASIC_FILENAME = "GermplasmImportTemplate-Basic-rev4.xls";

	private static final String CROP_TYPE = "maize";

	private static final String EMPTY_STRING = "";

	private static final String INSTALLATION_DIRECTORY = "C:\\InstallationDirectory";

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
		exportDialog = new ExportGermplasmListTemplateDialog(source);
		exportDialog.setWorkbenchDataManager(workbenchDataManager);
		exportDialog.setMessageSource(messageSource);
		FieldUtils.writeDeclaredField(exportDialog,"contextUtil",contextUtil,true);

		exportDialog = spy(exportDialog);


		when(messageSources.getMessage(Message.TEMPLATE_FORMAT)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.CHOOSE_A_TEMPLATE_FORMAT)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.CANCEL)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.EXPORT)).thenReturn(DUMMY_STRING);
		
		exportDialog.instantiateComponents();
		exportDialog.initializeValues();
		
		Project project = new Project();
		project.setProjectId(1L);
		CropType cropType = new CropType();
		cropType.setCropName(CROP_TYPE);
		project.setCropType(cropType);
		doReturn(project).when(contextUtil).getProjectInContext();
	}
	
	@Test
	public void testGetInstallationDirectory_ReturnsTheActualInstallationDirectory() throws MiddlewareQueryException{
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory(INSTALLATION_DIRECTORY);
		when(workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);
		
		String installationDirectory = exportDialog.getInstallationDirectory();
		Assert.assertEquals("Expected to return \"" + "\" but returned \""+ installationDirectory + "\"" ,installationDirectory, INSTALLATION_DIRECTORY);
	}
	
	@Test
	public void testGetInstallationDirectory_ReturnsEmptyString() throws MiddlewareQueryException{
		WorkbenchSetting workbenchSetting = null;
		when(workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);
		
		String installationDirectory = exportDialog.getInstallationDirectory();
		Assert.assertEquals("Expected to return an empty string but didn't.",installationDirectory, EMPTY_STRING);
	}
	
	@Test
	public void testGetFileToDownloadPath_ReturnsInstallationDirectoryPath() throws MiddlewareQueryException{
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory(INSTALLATION_DIRECTORY);
		when(workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);
		when(exportDialog.getGermplasmTemplateFileName()).thenReturn(BASIC_FILENAME);
		
		String fileToDownloadPath = exportDialog.getFileToDownloadPath(BASIC_FILENAME);
		
		String expectedPath = INSTALLATION_DIRECTORY + "\\Examples\\maize\\templates\\GermplasmImportTemplate-Basic-rev4.xls";
		
		Assert.assertEquals("Expected to return " + expectedPath + " but returned " + fileToDownloadPath,expectedPath, fileToDownloadPath);
	}
	
	@Test
	public void testGetFileToDownloadPath_ReturnsDefaultInstallationDirectoryPath() throws MiddlewareQueryException{
		WorkbenchSetting workbenchSetting = new WorkbenchSetting();
		workbenchSetting.setInstallationDirectory("");
		when(workbenchDataManager.getWorkbenchSetting()).thenReturn(workbenchSetting);
		when(exportDialog.getGermplasmTemplateFileName()).thenReturn(BASIC_FILENAME);
		
		String fileToDownloadPath = exportDialog.getFileToDownloadPath(BASIC_FILENAME);
		
		String expectedPath = "C:\\Breeding Management System\\Examples\\maize\\templates\\GermplasmImportTemplate-Basic-rev4.xls";
		
		Assert.assertEquals("Expected to return " + expectedPath + " but returned " + fileToDownloadPath,expectedPath, fileToDownloadPath);
	}
	
	@Test
	public void testCreateFileDownloadResource(){
		doReturn(mock(Application.class)).when(source).getApplication();
		
		String fileToDownloadPath = "C:\\Breeding Management System\\Examples\\maize\\templates\\GermplasmImportTemplate-Basic-rev4.xls";
		
		File fileToDownload = new File(fileToDownloadPath);
		if(fileToDownload.exists()){
			try {
				exportDialog.createFileDownloadResource(fileToDownloadPath);
			} catch (IOException e) {
				Assert.fail("should not throw an exception here.");
			}
		} else {
			try {
				exportDialog.createFileDownloadResource(fileToDownloadPath);
			} catch (IOException e) {
				Assert.assertEquals("Germplasm Template File does not exist.", e.getMessage());
			}
		}
	}
	
	@Test
	public void testIsADefaultCrop_ReturnsTrueForDefaultCrop(){
		Assert.assertTrue("Expecting that Maize is a default crop but didn't", exportDialog.isADefaultCrop("maize"));
		Assert.assertTrue("Expecting that Chickpea is a default crop but didn't", exportDialog.isADefaultCrop("CHICKPEA"));
	}
	
	@Test
	public void testIsADefaultCrop_ReturnsFalseForCustomCrop(){
		Assert.assertFalse("Expecting that Banana is not a default crop but didn't", exportDialog.isADefaultCrop("banana"));
	}
}

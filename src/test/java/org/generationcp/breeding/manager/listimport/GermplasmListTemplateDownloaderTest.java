package org.generationcp.breeding.manager.listimport;

import com.vaadin.ui.Component;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.workbook.generator.CodesSheetGenerator;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListTemplateDownloaderTest {

	public static final String TEMPORARY_FILE_PATH_XLS = "temporaryFilePath.xls";

	@Mock
	private CodesSheetGenerator codesSheetGenerator;

	@Mock
	private FileDownloaderUtility fileDownloaderUtility;

	@Mock
	private FileService fileService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private Component component;

	@Mock
	private InstallationDirectoryUtil installationDirectoryUtil;

	@Mock
	private GermplasmListTemplateDownloader.WorkbookFileWriter workbookFileWriter;

	private final HSSFWorkbook workbook = new HSSFWorkbook();

	private GermplasmListTemplateDownloader germplasmListTemplateDownloader;

	@Before
	public void setUp() throws Exception {

		this.germplasmListTemplateDownloader = new GermplasmListTemplateDownloader();
		this.germplasmListTemplateDownloader.setCodesSheetGenerator(this.codesSheetGenerator);
		this.germplasmListTemplateDownloader.setFileDownloaderUtility(this.fileDownloaderUtility);
		this.germplasmListTemplateDownloader.setFileService(this.fileService);
		this.germplasmListTemplateDownloader.setContextUtil(this.contextUtil);
		this.germplasmListTemplateDownloader.setInstallationDirectoryUtil(this.installationDirectoryUtil);
		this.germplasmListTemplateDownloader.setWorkbookFileWriter(this.workbookFileWriter);

		final Project project = new Project();
		when(this.contextUtil.getProjectInContext()).thenReturn(project);
		when(this.installationDirectoryUtil
				.getFileInTemporaryDirectoryForProjectAndTool(GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE, project,
						ToolName.BM_LIST_MANAGER_MAIN)).thenReturn(TEMPORARY_FILE_PATH_XLS);
		when(this.fileService.retrieveWorkbookTemplate("templates/" + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE))
				.thenReturn(this.workbook);

	}

	@Test
	public void testExportGermplasmTemplate() throws Exception {

		this.germplasmListTemplateDownloader.exportGermplasmTemplate(this.component);

		verify(this.workbookFileWriter, times(1)).write(this.workbook, TEMPORARY_FILE_PATH_XLS);
		verify(this.codesSheetGenerator, times(1)).generateCodesSheet(this.workbook, "maize");
		verify(this.fileDownloaderUtility, times(1))
				.initiateFileDownload(TEMPORARY_FILE_PATH_XLS, GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE, this.component);

	}

}

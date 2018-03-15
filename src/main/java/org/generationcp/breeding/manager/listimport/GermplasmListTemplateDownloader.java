package org.generationcp.breeding.manager.listimport;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.workbook.generator.CodesSheetGenerator;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;

@Configurable
public class GermplasmListTemplateDownloader {

	protected static final String EXPANDED_TEMPLATE_FILE = "GermplasmImportTemplate-Expanded-rev5a.xls";


	@Resource
	private CodesSheetGenerator codesSheetGenerator;

	@Resource
	private FileDownloaderUtility fileDownloaderUtility;

	@Resource
	private FileService fileService;

	@Resource
	private ContextUtil contextUtil;

	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	private WorkbookFileWriter workbookFileWriter = new WorkbookFileWriter();

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public void exportGermplasmTemplate(Component component) throws FileDownloadException {
		try {

			final String temporaryFilePath = installationDirectoryUtil
					.getFileInTemporaryDirectoryForProjectAndTool(EXPANDED_TEMPLATE_FILE, this.contextUtil.getProjectInContext(),
							ToolName.BM_LIST_MANAGER_MAIN);

			final HSSFWorkbook wb = (HSSFWorkbook) this.fileService
					.retrieveWorkbookTemplate("templates/" + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE);

			this.workbookFileWriter.write(wb, temporaryFilePath);

			this.codesSheetGenerator.generateCodesSheet(wb);

			this.fileDownloaderUtility.initiateFileDownload(temporaryFilePath, EXPANDED_TEMPLATE_FILE, component);

		} catch (IOException | InvalidFormatException e) {
			throw new FileDownloadException(this.messageSource.getMessage(Message.ERROR_IN_GERMPLASMLIST_TEMPLATE_DOWNLOAD), e);
		}
	}

	public void setCodesSheetGenerator(final CodesSheetGenerator codesSheetGenerator) {
		this.codesSheetGenerator = codesSheetGenerator;
	}

	public void setFileDownloaderUtility(final FileDownloaderUtility fileDownloaderUtility) {
		this.fileDownloaderUtility = fileDownloaderUtility;
	}

	public void setFileService(final FileService fileService) {
		this.fileService = fileService;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setInstallationDirectoryUtil(final InstallationDirectoryUtil installationDirectoryUtil) {
		this.installationDirectoryUtil = installationDirectoryUtil;
	}

	public void setWorkbookFileWriter(final WorkbookFileWriter workbookFileWriter) {
		this.workbookFileWriter = workbookFileWriter;
	}

	class FileDownloadException extends Exception {

		public FileDownloadException(String message, Exception e) {
			super(message, e);
		}

	}

	class WorkbookFileWriter {

		void write(final Workbook workbook, final String filePath) throws IOException {

			final FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			workbook.write(fileOutputStream);
			fileOutputStream.close();

		}

	}
}

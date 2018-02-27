
package org.generationcp.breeding.manager.listimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.dellroad.stuff.vaadin.ContextApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.util.VaadinFileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.workbook.generator.CodesSheetGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.Application;
import com.vaadin.ui.Component;

@Configurable
public class GermplasmListTemplateDownloader {

	@Resource
	CodesSheetGenerator codesSheetGenerator;

	@Resource
	private FileService fileService;

	static final String EXPANDED_TEMPLATE_FILE = "GermplasmImportTemplate-Expanded-rev5a.xls";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public void exportGermplasmTemplate(Component component) throws FileDownloadException {
		try {
			File templateFile = new File(EXPANDED_TEMPLATE_FILE);

			HSSFWorkbook wb =
					(HSSFWorkbook) this.fileService.retrieveWorkbookTemplate("templates/"
							+ GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE);
			this.codesSheetGenerator.generateCodesSheet(wb);
			final FileOutputStream fileOutputStream = new FileOutputStream(templateFile);
			wb.write(fileOutputStream);
			fileOutputStream.close();
			VaadinFileDownloadResource fileDownloadResource = this.getTemplateAsDownloadResource(templateFile);
			if (!this.getCurrentApplication().getMainWindow().getChildWindows().isEmpty()) {
				this.getCurrentApplication().getMainWindow().open(fileDownloadResource);
			} else {
				component.getWindow().open(fileDownloadResource);
			}

		} catch (IOException | InvalidFormatException e) {
			throw new FileDownloadException(this.messageSource.getMessage(Message.ERROR_IN_GERMPLASMLIST_TEMPLATE_DOWNLOAD), e);
		}
	}

	protected VaadinFileDownloadResource getTemplateAsDownloadResource(File templateFile) throws IOException {
		VaadinFileDownloadResource fileDownloadResource = null;
		if (!templateFile.exists()) {
			throw new IOException("Germplasm Template File does not exist.");
		} else {
			fileDownloadResource = new VaadinFileDownloadResource(templateFile,EXPANDED_TEMPLATE_FILE, this.getCurrentApplication());
		}

		return fileDownloadResource;
	}

	protected Application getCurrentApplication() {
		return ContextApplication.currentApplication();
	}

	protected HttpServletRequest getCurrentRequest() {
		return ContextApplication.currentRequest();
	}

	public class FileDownloadException extends Exception {

		public FileDownloadException(String message, Exception e) {
			super(message, e);
		}
	}
}

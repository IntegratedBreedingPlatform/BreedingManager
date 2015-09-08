package org.generationcp.breeding.manager.listimport;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.ui.Component;
import org.dellroad.stuff.vaadin.ContextApplication;
import org.generationcp.commons.util.FileDownloadResource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.ClassPathResource;

import com.vaadin.Application;

@Configurable
public class GermplasmListTemplateDownloader {

	static final String EXPANDED_TEMPLATE_FILE = "GermplasmImportTemplate-Expanded-rev5.xls";

	private static final long serialVersionUID = -9047374755825933209L;

	public void exportGermplasmTemplate(Component component) throws FileDownloadException {
		try {
			ClassPathResource cpr = new ClassPathResource("templates/" + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE);
			File templateFile = cpr.getFile();

			FileDownloadResource fileDownloadResource = getTemplateAsDownloadResource(templateFile);

			if (!this.getCurrentApplication().getMainWindow().getChildWindows().isEmpty()) {
				this.getCurrentApplication().getMainWindow().open(fileDownloadResource);
			} else {
				component.getWindow().open(fileDownloadResource);

			}

		} catch (IOException e) {
			throw new FileDownloadException(e.getMessage(), e);
		}
	}

	protected FileDownloadResource getTemplateAsDownloadResource(File templateFile) throws IOException {
		FileDownloadResource fileDownloadResource = null;
		if (!templateFile.exists()) {
			throw new IOException("Germplasm Template File does not exist.");
		} else {
			fileDownloadResource = new FileDownloadResource(templateFile, getCurrentApplication());
		}

		fileDownloadResource.setFilename(
				FileDownloadResource.getDownloadFileName(EXPANDED_TEMPLATE_FILE, getCurrentRequest()));
		return fileDownloadResource;
	}

	protected Application getCurrentApplication() {
		return ContextApplication.currentApplication();
	}

	protected HttpServletRequest getCurrentRequest(){
		return ContextApplication.currentRequest();
	}

	public class FileDownloadException extends Exception {

		public FileDownloadException(String message, Exception e) {
			super(message, e);
		}
	}
}

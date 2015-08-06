package org.generationcp.breeding.manager.listimport;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Resource;

import com.vaadin.Application;
import org.apache.commons.lang.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.CropType.CropEnum;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GermplasmListTemplateDownloader {

	static final String EXPANDED_TEMPLATE_FILE = "GermplasmImportTemplate-Expanded-rev5.xls";

	private static final long serialVersionUID = -9047374755825933209L;
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTemplateDownloader.class);

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private ContextUtil contextUtil;

	public void exportGermplasmTemplate() throws FileDownloadException {
		try {
			FileDownloadResource fileDownloadResource = this.createFileDownloadResource(
					this.getBMSCropTemplateDownloadLocation(EXPANDED_TEMPLATE_FILE, getCurrentProjectCropType()),
					BreedingManagerApplication.get());
			fileDownloadResource.setFilename(
					FileDownloadResource.getDownloadFileName(EXPANDED_TEMPLATE_FILE, BreedingManagerUtil.getApplicationRequest()));
			BreedingManagerApplication.get().getMainWindow().open(fileDownloadResource);
		} catch (MiddlewareQueryException | IOException e) {
			throw new FileDownloadException(e.getMessage(), e);
		}
	}

	private String getCurrentProjectCropType() throws MiddlewareQueryException {
		Project currentProject = this.contextUtil.getProjectInContext();
		String cropType = currentProject.getCropType().getCropName();
		// if it is a custom crop
		if (!this.isADefaultCrop(cropType)) {
			cropType = "generic";
		}

		return cropType;
	}

	protected boolean isADefaultCrop(String cropType) {
		for (CropEnum type : CropType.CropEnum.values()) {
			if (cropType.equalsIgnoreCase(type.toString())) {
				return true;
			}
		}

		return false;
	}

	protected FileDownloadResource createFileDownloadResource(String fileToDownloadPath, Application application) throws IOException {
		File fileToDownload = new File(fileToDownloadPath);

		FileDownloadResource fileDownloadResource = null;
		if (!fileToDownload.exists()) {
			throw new IOException("Germplasm Template File does not exist.");
		} else {
			fileDownloadResource = new FileDownloadResource(fileToDownload, application);
		}
		return fileDownloadResource;
	}

	public String getBMSCropTemplateDownloadLocation(String fileName, String cropType) {
		String fileDownloadPathFormat = "%s" + File.separator + "Examples" + File.separator + "%s" + File.separator + "templates" + File.separator + "%s";

		String installationDirectory = this.getInstallationDirectory();
		installationDirectory = !StringUtils.isEmpty(installationDirectory) ? installationDirectory : "C:" + File.separator + "BMS4";

		return String.format(fileDownloadPathFormat,installationDirectory,cropType,fileName);
	}

	public String getInstallationDirectory() {
		try {
			WorkbenchSetting workbenchSetting = this.workbenchDataManager.getWorkbenchSetting();
			return !Objects.equals(workbenchSetting, null) ? workbenchSetting.getInstallationDirectory() : "";

		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return "";
	}

	public class FileDownloadException extends Exception {

		public FileDownloadException(String message, Exception e) {
			super(message, e);
		}
	}
}

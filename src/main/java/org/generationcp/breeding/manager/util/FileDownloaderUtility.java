
package org.generationcp.breeding.manager.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.util.FileUtils;

import com.vaadin.ui.Component;

public class FileDownloaderUtility {

	public boolean initiateFileDownload(final String sourceFilename, final String visibleFilename, final Component source) {

		if (StringUtils.isEmpty(sourceFilename) || !new File(sourceFilename).exists()) {
			return false;
		}

		final String userAgent = BreedingManagerUtil.getApplicationRequest().getHeader("User-Agent");
		final FileDownloadResource fileDownloadResource =
				new FileDownloadResource(new File(sourceFilename),visibleFilename, source.getApplication());

		source.getWindow().open(fileDownloadResource);

		return true;
	}
}


package org.generationcp.breeding.manager.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.VaadinFileDownloadResource;
import org.generationcp.commons.util.FileUtils;

import com.vaadin.ui.Component;

public class FileDownloaderUtility {

	public boolean initiateFileDownload(final String sourceFilename, final String visibleFilename, final Component source) {

		if (StringUtils.isEmpty(sourceFilename) || !new File(sourceFilename).exists()) {
			return false;
		}

		final VaadinFileDownloadResource fileDownloadResource =
				new VaadinFileDownloadResource(new File(sourceFilename),visibleFilename, source.getApplication());

		source.getWindow().open(fileDownloadResource);

		return true;
	}
}

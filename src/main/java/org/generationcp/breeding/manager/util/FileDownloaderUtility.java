package org.generationcp.breeding.manager.util;

import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileDownloadResource;

import java.io.File;

public class FileDownloaderUtility {
    public boolean initiateFileDownload(final String sourceFilename, final String visibleFilename, final Component source) {

        if (StringUtils.isEmpty(sourceFilename) || ! new File(sourceFilename).exists()) {
            return false;
        }

		final FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(sourceFilename), source.getApplication());
		if (visibleFilename != null) {
			final String adjustedFileName = FileUtils.encodeFilenameForDownload(visibleFilename);
			fileDownloadResource.setFilename(adjustedFileName);
		}

        source.getWindow().open(fileDownloadResource);

        return true;
    }
}

package org.generationcp.breeding.manager.util.awhere;

import java.io.File;
import java.io.IOException;

import org.generationcp.breeding.manager.application.GermplasmStudyBrowserApplication;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.util.FileDownloadResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.Window;

public class UtilTester {

	private File tempFile;
	private GermplasmStudyBrowserApplication browserApplication;
	private Window window;
	private FileDownloadResource fileDownloadResource;

	@Before
	public void setUp() throws IOException {
		this.tempFile = new File("dataset-temp.xls");
		this.tempFile.createNewFile();
		this.browserApplication = new GermplasmStudyBrowserApplication();
		this.window = Mockito.mock(Window.class);
		this.fileDownloadResource = new FileDownloadResource(this.tempFile, this.browserApplication, "");
	}

	@Test
	public void testShowExportExcelDownloadFile() {
		// this is for the positive case where there is a file and a window
		boolean results = Util.showExportExcelDownloadFile(this.fileDownloadResource, this.window);
		Assert.assertTrue("Should be true when there is a file for download and the window object is not null ", results);
	}

	@Test
	public void testShowExportExcelDownloadFileReturnFalseWhenThereIsFileAndNoWindow() {
		boolean results = Util.showExportExcelDownloadFile(this.fileDownloadResource, null);
		Assert.assertFalse("Should be false when there is a file for download and the window object is null ", results);
	}

	@Test
	public void testShowExportExcelDownloadFileReturnFalseWhenThereIsNoFileAndAWindow() {
		boolean results = Util.showExportExcelDownloadFile(null, this.window);
		Assert.assertFalse("Should be false when there is no file for download and the window object is not null ", results);
	}

	@Test
	public void testShowExportExcelDownloadFileReturnFalseWhenThereIsNoFileAndNoWindow() {
		boolean results = Util.showExportExcelDownloadFile(null, null);
		Assert.assertFalse("Should be false when there is no file for download and the window object is null ", results);
	}
}

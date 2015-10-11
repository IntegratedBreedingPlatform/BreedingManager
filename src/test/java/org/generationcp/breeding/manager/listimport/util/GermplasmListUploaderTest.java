
package org.generationcp.breeding.manager.listimport.util;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.generationcp.breeding.manager.listimport.exceptions.InvalidFileTypeImportException;
import org.generationcp.commons.parsing.InvalidFileDataException;
import org.generationcp.commons.parsing.FileParsingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

public class GermplasmListUploaderTest {

	private GermplasmListUploader uploader;
	private static final String NONXLS_FILENAME = "FileName.txt";
	private static final String XLS_FILENAME = "FileName.xls";

	@Before
	public void setUp() {
		this.uploader = Mockito.spy(new GermplasmListUploader());
	}

	@Test
	public void testCreateFile() {

		this.uploader.createFile(NONXLS_FILENAME, "");

		Assert.assertEquals("Expecting that the original file name is set after successful creation of file but didn't. ",
				this.uploader.getOriginalFilename(), NONXLS_FILENAME);
	}

	@Test
	public void testGetFileInputStream_ForInvalidFileName() {
		Assert.assertNull("Expecting to return null when the filename is invalid.", this.uploader.createFileInputStream(""));
	}

	@Test
	public void testCreateWorkbook() throws InvalidFormatException, IOException {
		FileInputStream fileStream = Mockito.mock(FileInputStream.class);

		Mockito.doReturn(fileStream).when(this.uploader).createFileInputStream(Mockito.anyString());
		Mockito.doReturn(new HSSFWorkbook()).when(this.uploader).createWorkbookFromFactory(Mockito.anyString());

		this.uploader.createFile(XLS_FILENAME, "");

		try {
			this.uploader.createWorkbook(XLS_FILENAME);
		} catch (InvalidFileTypeImportException e) {
			Assert.fail("Expecting to not throw an IOException for valid template but didn't.");
		}
	}

	@Test
	public void testCreateWorkbook_ForHandlingIOException() throws InvalidFormatException, IOException {
		Mockito.doThrow(new IOException()).when(this.uploader).createWorkbookFromFactory(Mockito.anyString());
		try {
			this.uploader.createWorkbook(XLS_FILENAME);
			Assert.fail("Expecting to throw an IOException for invalid template but didn't.");
		} catch (InvalidFileTypeImportException e) {
			Assert.assertEquals("Expecting to return an error message, but didn't.",
					"Please upload a properly formatted XLS or XLSX file.", e.getMessage());
		}
	}

	@Test
	public void testCreateWorkbook_ForHandlingInvalidFormatException() throws InvalidFormatException, IOException {
		Mockito.doThrow(new InvalidFormatException("Invalid Format")).when(this.uploader).createWorkbookFromFactory(Mockito.anyString());

		try {
			this.uploader.createWorkbook(XLS_FILENAME);
			Assert.fail("Expecting to throw an InvalidFormatException for invalid template but didn't.");
		} catch (InvalidFileTypeImportException e) {
			Assert.assertEquals("Expecting to return an error message, but didn't.",
					"Please upload a properly formatted XLS or XLSX file.", e.getMessage());
		}
	}

	@Test
	public void testDoParseWorkbook() throws InvalidFormatException, IOException, FileParsingException, InvalidFileDataException {
		FileInputStream fileStream = Mockito.mock(FileInputStream.class);
		Mockito.doReturn(fileStream).when(this.uploader).createFileInputStream(Mockito.anyString());
		Mockito.doReturn(new HSSFWorkbook()).when(this.uploader).createWorkbookFromFactory(Mockito.anyString());
		Mockito.doReturn(new HSSFWorkbook()).when(this.uploader).createWorkbook(Mockito.anyString());
		Mockito.doNothing().when(this.uploader).updateImportGermplasmList();

		this.uploader.createFile(XLS_FILENAME, "");
		this.uploader.doParseWorkbook();

		try {
			Mockito.verify(this.uploader, Mockito.times(1)).updateImportGermplasmList();
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting that the importGermplasmList field has been set after successful parsing of Workbook");
		}

	}
}

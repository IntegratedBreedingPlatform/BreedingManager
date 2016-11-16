package org.generationcp.breeding.manager.inventory;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryImportException;
import org.generationcp.breeding.manager.listimport.exceptions.InvalidFileTypeImportException;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventoryList;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.InvalidFileDataException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.easyuploads.FileFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configurable
public class SeedInventoryListUploader implements FileFactory {

	private static final Logger LOG = LoggerFactory.getLogger(SeedInventoryListUploader.class);
	private static final String TEMP_FILE_DIR = new File(System.getProperty("java.io.tmpdir")).getPath();

	private String originalFilename;
	private String tempFileName;

	SeedInventoryImportListParser seedInventoryImportListParser;

	ImportedSeedInventoryList importedSeedInventoryList;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Override
	public File createFile(final String fileName, final String mimeType) {
		final File f = new File(SeedInventoryListUploader.TEMP_FILE_DIR + "/" + fileName);
		this.tempFileName = f.getAbsolutePath();
		this.originalFilename = fileName;

		return f;
	}

	public void doParseWorkbook() throws FileParsingException, InvalidFileDataException, SeedInventoryImportException {
		this.seedInventoryImportListParser = new SeedInventoryImportListParser();
		this.seedInventoryImportListParser.setOriginalFilename(this.originalFilename);

		this.updateImportedSeedInventoryList();
	}

	void updateImportedSeedInventoryList() throws FileParsingException, InvalidFileDataException {
		this.importedSeedInventoryList = this.seedInventoryImportListParser.parseWorkbook(this.createWorkbook(this.tempFileName), null);

	}

	public Workbook createWorkbook(final String tempFileName) {
		try {
			return this.createWorkbookFromFactory(tempFileName);
		} catch (IOException | InvalidFormatException e) {
			LOG.error(e.getMessage(), e);
			throw new InvalidFileTypeImportException(this.messageSource.getMessage(Message.SEED_INVALID_FILE_EXTENSION_ERROR));
		}
	}

	Workbook createWorkbookFromFactory(final String tempFileName) throws IOException, InvalidFormatException {
		return WorkbookFactory.create(this.createFileInputStream(tempFileName));
	}

	FileInputStream createFileInputStream(final String tempFileName) {
		try {
			return new FileInputStream(tempFileName);
		} catch (final FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getTempFileName() {
		return tempFileName;
	}

	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}

	public ImportedSeedInventoryList getImportedSeedInventoryList() {
		return importedSeedInventoryList;
	}

	public void setImportedSeedInventoryList(ImportedSeedInventoryList importedSeedInventoryList) {
		this.importedSeedInventoryList = importedSeedInventoryList;
	}
}

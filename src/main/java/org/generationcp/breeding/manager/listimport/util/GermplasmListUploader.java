
package org.generationcp.breeding.manager.listimport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.listimport.exceptions.InvalidFileTypeImportException;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.InvalidFileDataException;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.easyuploads.FileFactory;

@Configurable
public class GermplasmListUploader implements FileFactory {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListUploader.class);
	private static final String TEMP_FILE_DIR = new File(System.getProperty("java.io.tmpdir")).getPath();

	ImportedGermplasmList importedGermplasmList;
	GermplasmListParser germplasmListParser;

	private String originalFilename;
	private String tempFileName;

	@Override
	public File createFile(final String fileName, final String mimeType) {
		final File f = new File(GermplasmListUploader.TEMP_FILE_DIR + "/" + fileName);
		this.tempFileName = f.getAbsolutePath();
		this.originalFilename = fileName;

		return f;
	}

	/**
	 * Adopter methods, left over from legacy parser
	 */
	public boolean hasInventoryVariable() {
		return this.germplasmListParser.hasInventoryVariable();
	}

	public boolean hasInventoryAmountOnly() {
		return this.germplasmListParser.hasInventoryAmountOnly();
	}

	public boolean hasInventoryAmount() {
		return this.germplasmListParser.hasInventoryAmount();
	}

	public boolean hasAtLeastOneRowWithInventoryAmountButNoDefinedStockID() {
		return this.germplasmListParser.hasAtLeastOneRowWithInventoryAmountButNoDefinedStockID();
	}

	/*
	 * Returns true if variate property = "INVENTORY AMOUNT" or any of its synonyms
	 */
	public boolean isSeedAmountVariable(final ImportedVariate variate) {
		return this.germplasmListParser.isSeedAmountVariable(variate);
	}

	public boolean hasStockIdValues() {
		return this.germplasmListParser.hasStockIdValues();
	}

	public boolean hasStockIdFactor() {
		return this.germplasmListParser.hasStockIdFactor();
	}

	public boolean importFileIsAdvanced() {
		return this.germplasmListParser.importFileIsAdvanced();
	}

	public List<String> getNameFactors() {
		return this.germplasmListParser.getNameFactors();
	}

	public void doParseWorkbook() throws FileParsingException, InvalidFileDataException {
		this.germplasmListParser = new GermplasmListParser();
		this.germplasmListParser.setOriginalFilename(this.originalFilename);

		this.updateImportGermplasmList();
	}

	void updateImportGermplasmList() throws FileParsingException, InvalidFileDataException {
		this.importedGermplasmList = this.germplasmListParser.parseWorkbook(this.createWorkbook(this.tempFileName), null);

		if (this.importedGermplasmList.getImportedGermplasm().isEmpty()) {
			throw new InvalidFileDataException("GERMPLSM_EMPTY_FILE_PARSE_ERROR");
		}
	}

	public String hasWarnings() {
		return this.germplasmListParser.getNoInventoryWarning();
	}

	public ImportedGermplasmList getImportedGermplasmList() {
		return this.importedGermplasmList;
	}

	public Workbook createWorkbook(final String tempFileName) {
		try {
			return this.createWorkbookFromFactory(tempFileName);
		} catch (IOException | InvalidFormatException e) {
			GermplasmListUploader.LOG.error(e.getMessage(), e);
			throw new InvalidFileTypeImportException("Please upload a properly formatted XLS or XLSX file.");
		}
	}

	Workbook createWorkbookFromFactory(final String tempFileName) throws IOException, InvalidFormatException {
		return WorkbookFactory.create(this.createFileInputStream(tempFileName));
	}

	FileInputStream createFileInputStream(final String tempFileName) {
		try {
			return new FileInputStream(tempFileName);
		} catch (final FileNotFoundException e) {
			GermplasmListUploader.LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public String getOriginalFilename() {
		return this.originalFilename;
	}
}

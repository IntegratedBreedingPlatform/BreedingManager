
package org.generationcp.breeding.manager.listimport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.listimport.exceptions.InvalidFileTypeImportException;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.FileParsingException;
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
	public File createFile(String fileName, String mimeType) {
		File f = new File(GermplasmListUploader.TEMP_FILE_DIR + "/" + fileName);
		this.tempFileName = f.getAbsolutePath();
		this.originalFilename = fileName;

		return f;
	}

	/**
	 * Adopter methods, left over from legacy parser
	 */
	public boolean hasInventoryAmountOnly() {
		return this.germplasmListParser.hasInventoryAmountOnly();
	}

	public boolean hasInventoryAmount() {
		return this.germplasmListParser.hasInventoryAmount();
	}

	/*
	 * Returns true if variate property = "INVENTORY AMOUNT" or any of its synonyms
	 */
	public boolean isSeedAmountVariable(ImportedVariate variate) {
		return this.germplasmListParser.isSeedAmountVariable(variate);
	}

	public boolean hasStockIdFactor() {
		return this.germplasmListParser.hasStockIdFactor();
	}

	public boolean importFileIsAdvanced() {
		return this.germplasmListParser.importFileIsAdvanced();
	}

	public void doParseWorkbook() throws FileParsingException {
		this.germplasmListParser = new GermplasmListParser();
		this.germplasmListParser.setOriginalFilename(this.originalFilename);

		this.updateImportGermplasmList();
	}

	void updateImportGermplasmList() throws FileParsingException {
		this.importedGermplasmList = this.germplasmListParser.parseWorkbook(this.createWorkbook(this.tempFileName), null);
	}

	public String hasWarnings() {
	    StringBuilder mySB = new StringBuilder();
	    mySB.append(this.germplasmListParser.getNoInventoryWarning());
	    if (StringUtils.isNotBlank(this.germplasmListParser.getNoVariatesWarning())) {
	        // Add a blank line to separate warnings   
	        if (StringUtils.isNotBlank(mySB.toString())) {
	            mySB.append("\n\n");
	        }
	        mySB.append(this.germplasmListParser.getNoVariatesWarning());            
	    }		    
	    return mySB.toString();
	}

	public ImportedGermplasmList getImportedGermplasmList() {
		return this.importedGermplasmList;
	}

	public Workbook createWorkbook(String tempFileName) {
		try {
			return this.createWorkbookFromFactory(tempFileName);
		} catch (IOException | InvalidFormatException e) {
			LOG.error(e.getMessage(), e);
			throw new InvalidFileTypeImportException("Please upload a properly formatted XLS or XLSX file.");
		}
	}

	Workbook createWorkbookFromFactory(String tempFileName) throws IOException, InvalidFormatException {
		return WorkbookFactory.create(this.createFileInputStream(tempFileName));
	}

	FileInputStream createFileInputStream(String tempFileName) {
		try {
			return new FileInputStream(tempFileName);
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public String getOriginalFilename() {
		return this.originalFilename;
	}
}

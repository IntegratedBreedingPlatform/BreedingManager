package org.generationcp.breeding.manager.inventory;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventoryList;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;

@Configurable
public class SeedInventoryListParser extends AbstractExcelFileParser<ImportedSeedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(SeedInventoryListParser.class);

	ImportedSeedInventoryList importedSeedInventoryList;
	public static final int DESCRIPTION_SHEET_NO = 0;
	public static final int OBSERVATION_SHEET_NO = 1;

	@Override
	public ImportedSeedInventoryList parseWorkbook(Workbook workbook, Map<String, Object> additionalParams)
			throws FileParsingException {

		this.workbook = workbook;
		parseListDetails();
		return this.importedSeedInventoryList;
	}

	protected void parseListDetails() throws FileParsingException {
		final String listName = this.getCellStringValue(SeedInventoryListParser.DESCRIPTION_SHEET_NO, 0, 1);


		this.importedSeedInventoryList = new ImportedSeedInventoryList(this.originalFilename);
	}

	public void setOriginalFilename(final String originalFilename) {
		this.originalFilename = originalFilename;
	}

}

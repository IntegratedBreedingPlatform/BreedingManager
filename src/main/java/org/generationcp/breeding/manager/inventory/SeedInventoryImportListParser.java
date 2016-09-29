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
public class SeedInventoryImportListParser extends AbstractExcelFileParser<ImportedSeedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(SeedInventoryImportListParser.class);

	ImportedSeedInventoryList importedSeedInventoryList;
	public static final int DESCRIPTION_SHEET_NO = 0;
	public static final int OBSERVATION_SHEET_NO = 1;

	@Override
	public ImportedSeedInventoryList parseWorkbook(Workbook workbook, Map<String, Object> additionalParams)
			throws FileParsingException {

		this.workbook = workbook;
		parseDescriptionSheet();
		parseObservationSheet();
		return this.importedSeedInventoryList;
	}

	protected void parseDescriptionSheet() throws FileParsingException {
		this.importedSeedInventoryList = new ImportedSeedInventoryList(this.originalFilename);

		final String listName = this.getCellStringValue(SeedInventoryImportListParser.DESCRIPTION_SHEET_NO, 0, 1);

		this.importedSeedInventoryList.setListName(listName);
	}

	protected void parseObservationSheet() throws FileParsingException {

	}


	public void setOriginalFilename(final String originalFilename) {
		this.originalFilename = originalFilename;
	}

}

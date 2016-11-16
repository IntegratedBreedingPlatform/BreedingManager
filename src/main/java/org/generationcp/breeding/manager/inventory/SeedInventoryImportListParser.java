package org.generationcp.breeding.manager.inventory;

import com.google.common.collect.Lists;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventory;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventoryList;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configurable
public class SeedInventoryImportListParser extends AbstractExcelFileParser<ImportedSeedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(SeedInventoryImportListParser.class);

	protected ImportedSeedInventoryList importedSeedInventoryList;
	public static final int DESCRIPTION_SHEET_NO = 0;
	public static final int OBSERVATION_SHEET_NO = 1;

	private final int ENTRY_INDEX = 0;
	private final int DESIGNATION_INDEX = 1;
	private final int GID_INDEX = 2;
	private final int CROSS_INDEX = 3;
	private final int SOURCE_INDEX = 4;
	private final int LOT_ID_INDEX = 5;
	private final int LOT_LOCATION_INDEX = 6;
	private final int STOCK_ID_INDEX = 7;
	private final int TRN_INDEX = 8;
	private final int RESERVATION_INDEX = 9;
	private final int WITHDRAWAL_INDEX = 10;
	private final int BALANCE_INDEX = 11;
	private final int NOTES_INDEX = 12;

	private final int SEED_FILE_COLUMN_NO = 13;

	private Map<Integer, String> headerRow = new HashMap<>();

	@Override
	public ImportedSeedInventoryList parseWorkbook(Workbook workbook, Map<String, Object> additionalParams) throws FileParsingException {

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
		boolean isRowEmpty = false;
		int rowIndex = 0;
		List<ImportedSeedInventory> importedSeedInventories = Lists.newArrayList();
		while (true) {
			isRowEmpty = this.isRowEmpty(OBSERVATION_SHEET_NO, ++rowIndex, SEED_FILE_COLUMN_NO);

			if (isRowEmpty) {
				break;
			}

			String entryCellValue = this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, ENTRY_INDEX);
			Integer entry = null;
			if (!StringUtil.isEmpty(entryCellValue)) {
				entry = Integer.valueOf(entryCellValue);
			}

			String designation = this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, DESIGNATION_INDEX);
			Integer gid = Integer.valueOf(this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, GID_INDEX));
			Integer lotID = Integer.valueOf(this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, LOT_ID_INDEX));

			String transactionCellValue = this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, TRN_INDEX);
			Integer transactionId = null;

			if (!StringUtil.isEmpty(transactionCellValue)) {
				transactionId = Integer.valueOf(this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, TRN_INDEX));
			}

			String reservationCellValue = this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, RESERVATION_INDEX);
			Double reservationAmount = null;
			if (!StringUtil.isEmpty(reservationCellValue)) {
				reservationAmount = Double.valueOf(this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, RESERVATION_INDEX));
			}

			String withdrawalCellValue = this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, WITHDRAWAL_INDEX);
			Double withdrawalAmount = null;
			if (!StringUtil.isEmpty(withdrawalCellValue)) {
				withdrawalAmount = Double.valueOf(this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, WITHDRAWAL_INDEX));
			}

			String balanceAmountCellValue = this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, BALANCE_INDEX);
			Double balanceAmount = null;
			if (!StringUtil.isEmpty(balanceAmountCellValue)) {
				balanceAmount = Double.valueOf(this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, BALANCE_INDEX));
			}

			String comments = this.getCellStringValue(OBSERVATION_SHEET_NO, rowIndex, NOTES_INDEX);

			ImportedSeedInventory importedSeedInventory =
					new ImportedSeedInventory(entry, designation, gid, lotID, transactionId, reservationAmount, withdrawalAmount,
							balanceAmount, comments);
			importedSeedInventories.add(importedSeedInventory);

		}
		this.importedSeedInventoryList.setImportedSeedInventoryList(importedSeedInventories);
	}

	public void setOriginalFilename(final String originalFilename) {
		this.originalFilename = originalFilename;
	}

}

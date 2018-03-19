package org.generationcp.breeding.manager.inventory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryExportException;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.collect.Lists;
import com.vaadin.ui.Component;

@Configurable
public class SeedInventoryListExporter {

	public static final String SEED_EXPORT_FILE_NAME_FORMAT = "%s-Seed Prep.xls";
	public static final String TEMPORARY_FILE_NAME = "temp";
	public static final String XLS_EXTENSION = ".xls";

	private String seedTemplateFile = "SeedPrepTemplate.xls";

	private Component source;

	private GermplasmList germplasmList;

	private static final int ENTRY_INDEX = 0;
	private static final int DESIGNATION_INDEX = 1;
	private static final int GID_INDEX = 2;
	private static final int CROSS_INDEX = 3;
	private static final int SOURCE_INDEX = 4;
	private static final int LOT_ID_INDEX = 5;
	private static final int LOT_LOCATION_INDEX = 6;
	private static final int STOCK_ID_INDEX = 7;
	private static final int TRN_INDEX = 8;
	private static final int RESERVATION_INDEX = 9;
	private static final int NOTES_INDEX = 12;

	@Autowired
	private FileService fileService;

	@Autowired
	private FileDownloaderUtility fileDownloaderUtility;

	@Autowired
	protected InventoryDataManager inventoryDataManager;

	@Autowired
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Autowired
	protected ContextUtil contextUtil;

	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	protected Workbook excelWorkbook;
	
	public SeedInventoryListExporter() {
		// Empty constructor expected for autowiring this component
	}

	public SeedInventoryListExporter(final Component source, final GermplasmList germplasmList) {
		this.source = source;
		this.germplasmList = germplasmList;
	}

	public void exportSeedPreparationList() throws SeedInventoryExportException {
		try {

			excelWorkbook = this.fileService.retrieveWorkbookTemplate(seedTemplateFile);

			this.fillSeedPreparationExcel();

			String temporaryExcelFile = this.createExcelOutputFile(excelWorkbook);

			String visibleFileName = String.format(SeedInventoryListExporter.SEED_EXPORT_FILE_NAME_FORMAT,
					StringUtil.replaceInvalidChacaracterFileName(germplasmList.getName(), "_"));
			visibleFileName = FileUtils.sanitizeFileName(visibleFileName);

			this.fileDownloaderUtility.initiateFileDownload(temporaryExcelFile, visibleFileName, this.source);
		} catch (MiddlewareException | IOException | InvalidFormatException e) {
			throw new SeedInventoryExportException(e.getMessage(), e);
		}

	}

	public void fillSeedPreparationExcel() {
		this.writeListDetailsSection();
		this.writeObservationSheet();
	}

	public void writeListDetailsSection() {
		Sheet descriptionSheet = excelWorkbook.getSheetAt(0);

		String listName = this.germplasmList.getName();
		//B1 cell with the list name
		descriptionSheet.getRow(0).getCell(1).setCellValue(listName);

		final String listDescription = this.germplasmList.getDescription();
		//B2 cell with the list description
		descriptionSheet.getRow(1).getCell(1).setCellValue(listDescription);

		final String listType = this.germplasmList.getType();
		//B3 cell with the list type
		descriptionSheet.getRow(2).getCell(1).setCellValue(listType);

		final Long listDate = this.germplasmList.getDate();
		//B4 cell with the list date
		descriptionSheet.getRow(3).getCell(1).setCellValue(listDate);

		final String currentExportingUserName = this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId());
		//G7 cell with the Username
		descriptionSheet.getRow(6).getCell(6).setCellValue(currentExportingUserName);
	}

	private void writeObservationSheet() {
		final List<GermplasmListData> inventoryDetails =
				this.inventoryDataManager.getReservedLotDetailsForExportList(this.germplasmList.getId());

		final Map<Integer, Transaction> transactionMap = createReservedTransactionMap(inventoryDetails);
		Sheet observationSheet = excelWorkbook.getSheetAt(1);
		HashSet<String> reservedLotScaleSet = new HashSet<>();
		HashSet<String> reservedLotMethodSet = new HashSet<>();
		int rowIndex = 1;
		for (final GermplasmListData inventoryDetail : inventoryDetails) {

			final ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();

			final List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>) listDataInventory.getLotRows();

			if (lotDetails != null) {
				for (final ListEntryLotDetails lotDetail : lotDetails) {
					if (lotDetail.getReservedTotalForEntry() != null && lotDetail.getReservedTotalForEntry() > 0) {

						reservedLotScaleSet.add(lotDetail.getLotScaleNameAbbr());
						reservedLotMethodSet.add(lotDetail.getLotScaleMethodName());

						PoiUtil.setCellValue(observationSheet, ENTRY_INDEX, rowIndex, inventoryDetail.getEntryId());
						PoiUtil.setCellValue(observationSheet, DESIGNATION_INDEX, rowIndex, inventoryDetail.getDesignation());
						PoiUtil.setCellValue(observationSheet, GID_INDEX, rowIndex, inventoryDetail.getGid());
						PoiUtil.setCellValue(observationSheet, CROSS_INDEX, rowIndex, inventoryDetail.getGroupName());
						PoiUtil.setCellValue(observationSheet, SOURCE_INDEX, rowIndex, inventoryDetail.getSeedSource());

						PoiUtil.setCellValue(observationSheet, LOT_ID_INDEX, rowIndex, lotDetail.getLotId().toString());

						String lotLocation = "";
						if (lotDetail.getLocationOfLot() != null && lotDetail.getLocationOfLot().getLname() != null) {
							lotLocation = lotDetail.getLocationOfLot().getLname();
						}
						PoiUtil.setCellValue(observationSheet, LOT_LOCATION_INDEX, rowIndex, lotLocation);

						PoiUtil.setCellValue(observationSheet, STOCK_ID_INDEX, rowIndex, lotDetail.getStockIds());

						PoiUtil.setCellValue(observationSheet, TRN_INDEX, rowIndex, lotDetail.getTransactionId().toString());

						String reservation = lotDetail.getReservedTotalForEntry().toString();
						PoiUtil.setCellValue(observationSheet, RESERVATION_INDEX, rowIndex, reservation);

						Transaction transaction = transactionMap.get(lotDetail.getTransactionId());
						PoiUtil.setCellValue(observationSheet, NOTES_INDEX, rowIndex, transaction.getComments());

						rowIndex++;
					} else {
						// will skip lots having not reservation
					}

				}
			}
		}

		Sheet descriptionSheet = excelWorkbook.getSheetAt(0);
		String scaleName = "";
		String methodName = "";

		if (!reservedLotMethodSet.isEmpty()) {
			if (reservedLotMethodSet.size() == 1) {
				methodName = reservedLotMethodSet.iterator().next();
			} else {
				methodName = ListDataInventory.MIXED;
			}

			//E21 cell with withdrawal amount method
			descriptionSheet.getRow(20).getCell(4).setCellValue(methodName);
			//E22 cell with withdrawal amount method
			descriptionSheet.getRow(21).getCell(4).setCellValue(methodName);
			//E23 cell with withdrawal amount method
			descriptionSheet.getRow(22).getCell(4).setCellValue(methodName);
		}

		if (!reservedLotScaleSet.isEmpty()) {
			if (reservedLotScaleSet.size() == 1) {
				scaleName = reservedLotScaleSet.iterator().next();
			} else {
				scaleName = ListDataInventory.MIXED;
			}
			//D21 cell with withdrawal amount scale
			descriptionSheet.getRow(20).getCell(3).setCellValue(scaleName);
			//D22 cell with withdrawal amount scale
			descriptionSheet.getRow(21).getCell(3).setCellValue(scaleName);
			//D23 cell with withdrawal amount scale
			descriptionSheet.getRow(22).getCell(3).setCellValue(scaleName);
		}

	}

	private String createExcelOutputFile(final Workbook excelWorkbook) throws IOException {

		final String temporaryFilenamePath = installationDirectoryUtil
				.getTempFileInOutputDirectoryForProjectAndTool(TEMPORARY_FILE_NAME, XLS_EXTENSION, contextUtil.getProjectInContext(),
						ToolName.BM_LIST_MANAGER_MAIN);

		try (OutputStream out = new FileOutputStream(temporaryFilenamePath)) {
			excelWorkbook.write(out);
		}

		return temporaryFilenamePath;
	}

	private Map<Integer, Transaction> createReservedTransactionMap(final List<GermplasmListData> inventoryDetails) {
		final List<Integer> reservedTransactionIdList = Lists.newArrayList();
		final Map<Integer, Transaction> reservedTransactionMap = new HashMap<>();

		for (final GermplasmListData inventoryDetail : inventoryDetails) {

			final ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();

			final List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>) listDataInventory.getLotRows();

			if (lotDetails != null) {
				for (final ListEntryLotDetails lotDetail : lotDetails) {
					if (lotDetail.getReservedTotalForEntry() != null && lotDetail.getReservedTotalForEntry() > 0) {
						reservedTransactionIdList.add(lotDetail.getTransactionId());
					}
				}
			}
		}

		List<Transaction> listTransactions = this.inventoryDataManager.getTransactionsByIdList(reservedTransactionIdList);

		for (Transaction transaction : listTransactions) {
			reservedTransactionMap.put(transaction.getId(), transaction);
		}
		return reservedTransactionMap;
	}

	public void setSource(Component source) {
		this.source = source;
	}

	public void setGermplasmList(GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

	public void setFileDownloaderUtility(FileDownloaderUtility fileDownloaderUtility) {
		this.fileDownloaderUtility = fileDownloaderUtility;
	}

	public void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public String getSeedTemplateFile() {
		return seedTemplateFile;
	}

	public Workbook getExcelWorkbook() {
		return excelWorkbook;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setInstallationDirectoryUtil(final InstallationDirectoryUtil installationDirectoryUtil) {
		this.installationDirectoryUtil = installationDirectoryUtil;
	}
}



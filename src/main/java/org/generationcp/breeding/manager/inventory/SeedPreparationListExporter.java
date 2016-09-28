package org.generationcp.breeding.manager.inventory;

import com.vaadin.ui.Component;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.inventory.exception.SeedPreparationExportException;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Configurable
public class SeedPreparationListExporter {

	public static final String SEED_EXPORT_FILE_NAME_FORMAT = "%s-Seed Prep.xls";

	private String seedTemplateFile = "SeedPrepTemplate.xls";

	private Component source;

	private GermplasmList germplasmList;

	@Resource
	private FileService fileService;

	@Resource
	private FileDownloaderUtility fileDownloaderUtility;

	@Autowired
	protected InventoryDataManager inventoryDataManager;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;


	private Workbook excelWorkbook;

	public SeedPreparationListExporter(){

	}

	public SeedPreparationListExporter(final Component source, final GermplasmList germplasmList) {
		this.source = source;
		this.germplasmList = germplasmList;
	}

	public void exportSeedPreparationList() throws SeedPreparationExportException {
		try{

			excelWorkbook = this.fileService.retrieveWorkbookTemplate(seedTemplateFile);
			this.fillSeedPreparationExcel();
			File excelOutputFile = this.createExcelOutputFile(germplasmList.getName(), excelWorkbook);

			this.fileDownloaderUtility.initiateFileDownload(excelOutputFile.getAbsolutePath(), excelOutputFile.getName(), this.source);
		}catch(MiddlewareException | IOException | InvalidFormatException e){
			throw new SeedPreparationExportException(e.getMessage(), e);
		}

	}

	public void fillSeedPreparationExcel(){
		this.writeListDetailsSection();
	}

	public void writeListDetailsSection(){
		Sheet descriptionSheet = excelWorkbook.getSheetAt(0);

		String listName = this.germplasmList.getName();
		descriptionSheet.getRow(0).getCell(1).setCellValue(listName); //B1 cell with the list name

		final String listDescription = this.germplasmList.getDescription();
		descriptionSheet.getRow(1).getCell(1).setCellValue(listDescription); //B2 cell with the list name

		final String listType = this.germplasmList.getType();
		descriptionSheet.getRow(2).getCell(1).setCellValue(listType); //B3 cell with the list name


		final Long listDate = this.germplasmList.getDate();
		descriptionSheet.getRow(3).getCell(1).setCellValue(listDate); //B4 cell with the list name

		final String currentExportingUserName = this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId());
		descriptionSheet.getRow(6).getCell(6).setCellValue(currentExportingUserName); //G7 cell with the Username
	}

	private File createExcelOutputFile(final String listName, final Workbook excelWorkbook) throws IOException {
		String outputFileName =
				String.format(SeedPreparationListExporter.SEED_EXPORT_FILE_NAME_FORMAT, StringUtil
						.replaceInvalidChacaracterFileName(listName,"_"));

		outputFileName = FileUtils.sanitizeFileName(outputFileName);

		try (OutputStream out = new FileOutputStream(outputFileName)) {
			excelWorkbook.write(out);
		}

		return new File(outputFileName);
	}

}



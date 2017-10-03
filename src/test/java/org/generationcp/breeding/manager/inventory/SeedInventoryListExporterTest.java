package org.generationcp.breeding.manager.inventory;

import com.beust.jcommander.internal.Lists;
import com.vaadin.ui.Component;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SeedInventoryListExporterTest {

	private Component source;

	private final String LIST_NAME = "ListName";

	private GermplasmList germplasmList;


	private FileService fileService;

	private FileDownloaderUtility fileDownloaderUtility;

	protected InventoryDataManager inventoryDataManager;

	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	private Workbook excelWorkbook;

	private SeedInventoryListExporter seedInventoryListExporter;

	@Before
	public void setUp(){
		this.source = Mockito.mock(Component.class);
		this.germplasmList = Mockito.mock(GermplasmList.class);
		this.fileService = Mockito.mock(FileService.class);
		this.fileDownloaderUtility = Mockito.mock(FileDownloaderUtility.class);
		this.inventoryDataManager = Mockito.mock(InventoryDataManager.class);
		this.fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);

		this.seedInventoryListExporter = new SeedInventoryListExporter(this.source, this.germplasmList);
		this.seedInventoryListExporter.setFileService(this.fileService);
		this.seedInventoryListExporter.setFileDownloaderUtility(this.fileDownloaderUtility);
		this.seedInventoryListExporter.setInventoryDataManager(this.inventoryDataManager);
		this.seedInventoryListExporter.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
	}

	@After
	public void tearDown() {
		final File file = new File(LIST_NAME+"-Seed Prep.xls");
		file.deleteOnExit();
	}

	@Test
	public void testExportSeedPreparationListSuccess() throws Exception{

		final File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(seedInventoryListExporter.getSeedTemplateFile()).toURI());

		Workbook workbook =  WorkbookFactory.create(workbookFile);
		Mockito.when(this.fileService.retrieveWorkbookTemplate(Mockito.anyString())).thenReturn(workbook);

		Mockito.when(this.germplasmList.getName()).thenReturn(LIST_NAME);
		Mockito.when(this.germplasmList.getDescription()).thenReturn("ListNameDescription");
		Mockito.when(this.germplasmList.getType()).thenReturn("ListType");
		Mockito.when(this.germplasmList.getDate()).thenReturn(new Long(20161005));


		Mockito.when(this.fieldbookMiddlewareService.getOwnerListName(Mockito.anyInt())).thenReturn("listOwner");

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(createReservedTransactions());

		List<GermplasmListData> germplasmListDataForReservedEntries = createGermplasmListDataForReservedEntries();
		Mockito.when(this.inventoryDataManager.getReservedLotDetailsForExportList(Mockito.anyInt(), Mockito.anyString())).thenReturn(germplasmListDataForReservedEntries);

		seedInventoryListExporter.exportSeedPreparationList();

		Assert.assertNotNull("Workbook should not be null", this.seedInventoryListExporter.getExcelWorkbook());

		Workbook excelWorkbook = seedInventoryListExporter.getExcelWorkbook();
		Sheet descriptionSheet = excelWorkbook.getSheetAt(0);

		Assert.assertNotNull("Description sheet should not be null", descriptionSheet);

		Assert.assertEquals("List Description in exported work book does not match", "ListNameDescription",
				descriptionSheet.getRow(1).getCell(1).getStringCellValue());

		Assert.assertEquals("List Type in exported work book does not match", "ListType",
				descriptionSheet.getRow(2).getCell(1).getStringCellValue());

		Assert.assertEquals( new Double(20161005).doubleValue(),
				descriptionSheet.getRow(3).getCell(1).getNumericCellValue(),  new Double(20161005).doubleValue() -descriptionSheet.getRow(3).getCell(1).getNumericCellValue());

		Assert.assertEquals("List Owner in exported work book does not match", "listOwner",
				descriptionSheet.getRow(6).getCell(6).getStringCellValue());

		Sheet observationSheet = excelWorkbook.getSheetAt(1);

		Assert.assertNotNull("Observation sheet should not be null", observationSheet);

		Row row = observationSheet.getRow(1);

		Assert.assertEquals("Entry no does not match with exported reserved list data", "1.0",
				new Double(row.getCell(0).getNumericCellValue()).toString());

		Assert.assertEquals("Designation  does not match with exported reserved list data", "Designation",
				row.getCell(1).getStringCellValue());

		Assert.assertEquals("GID  does not match with exported reserved list data", "28.0",
				new Double(row.getCell(2).getNumericCellValue()).toString());

		Assert.assertEquals("CrossName  does not match with exported reserved list data", "GroupName",
				row.getCell(3).getStringCellValue());

		Assert.assertEquals("Source  does not match with exported reserved list data", "SeedSource",
				row.getCell(4).getStringCellValue());

		Assert.assertEquals("LotID  does not match with exported reserved list data", "10.0",
				new Double(row.getCell(5).getStringCellValue()).toString());

		Assert.assertEquals("Location  does not match with exported reserved list data", "locName",
				row.getCell(6).getStringCellValue());

		Assert.assertEquals("Stock ID  does not match with exported reserved list data", "stockIds",
				row.getCell(7).getStringCellValue());


		Assert.assertEquals("Transaction ID  does not match with exported reserved list data", "120.0",
				new Double(row.getCell(8).getStringCellValue()).toString());


		Assert.assertEquals("Reservation amount  does not match with exported reserved list data", "2.0",
				new Double(row.getCell(9).getStringCellValue()).toString());

		Assert.assertEquals("Withdrawal amount should be empty in exported reserved list data","", row.getCell(10).getStringCellValue());
		Assert.assertEquals("Balance amount should be empty in exported reserved list data","", row.getCell(11).getStringCellValue());

		Assert.assertEquals("Comments  does not match with exported reserved list data", "comments",
				row.getCell(12).getStringCellValue());


	}

	private List<GermplasmListData> createGermplasmListDataForReservedEntries(){
		List germplasmListData = Lists.newArrayList();

		final GermplasmListData listEntry = new GermplasmListData();
		listEntry.setId(1);
		listEntry.setDesignation("Designation");
		listEntry.setEntryCode("EntryCode");
		listEntry.setEntryId(1);
		listEntry.setGroupName("GroupName");
		listEntry.setStatus(0);
		listEntry.setSeedSource("SeedSource");
		listEntry.setGid(28);
		listEntry.setMgid(0);

		final List<ListEntryLotDetails> lots = new ArrayList<ListEntryLotDetails>();

		final ListEntryLotDetails lotDetails = new ListEntryLotDetails();
		lotDetails.setLotId(10);
		lotDetails.setReservedTotalForEntry(2.0);
		lotDetails.setLotScaleMethodName("weight");
		lotDetails.setLotScaleNameAbbr("g");
		Location location = new Location(1);
		location.setLname("locName");
		lotDetails.setLocationOfLot(location);
		lotDetails.setStockIds("stockIds");
		lotDetails.setTransactionId(120);
		lotDetails.setCommentOfLot("comments");

		lots.add(lotDetails);
		ListDataInventory listDataInfo = new ListDataInventory(1,28);
		listDataInfo.setLotRows(lots);
		listEntry.setInventoryInfo(listDataInfo);
		germplasmListData.add(listEntry);

		return germplasmListData;
	}

	private List<Transaction> createReservedTransactions(){
		Transaction transaction = new Transaction();
		transaction.setId(120);
		transaction.setQuantity(-2.0);
		transaction.setStatus(0);
		transaction.setComments("comments");

		return Lists.newArrayList(transaction);
	}

}

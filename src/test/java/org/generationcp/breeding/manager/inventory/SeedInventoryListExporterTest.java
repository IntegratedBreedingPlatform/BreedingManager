package org.generationcp.breeding.manager.inventory;

import com.beust.jcommander.internal.Lists;
import com.vaadin.ui.Component;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SeedInventoryListExporterTest {

	public static final String TEMPORAY_FILE_XLS = "temporayFile.xls";
	private final String LIST_NAME = "ListName";

	@Mock
	private Component source;

	@Mock
	private GermplasmList germplasmList;

	@Mock
	private FileService fileService;

	@Mock
	private FileDownloaderUtility fileDownloaderUtility;

	@Mock
	protected InventoryDataManager inventoryDataManager;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	protected ContextUtil contextUtil;

	@Mock
	protected InstallationDirectoryUtil installationDirectoryUtil;

	private SeedInventoryListExporter seedInventoryListExporter;

	@Before
	public void setUp() throws IOException {

		this.seedInventoryListExporter = new SeedInventoryListExporter(this.source, this.germplasmList);
		this.seedInventoryListExporter.setFileService(this.fileService);
		this.seedInventoryListExporter.setFileDownloaderUtility(this.fileDownloaderUtility);
		this.seedInventoryListExporter.setInventoryDataManager(this.inventoryDataManager);
		this.seedInventoryListExporter.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.seedInventoryListExporter.setContextUtil(this.contextUtil);
		this.seedInventoryListExporter.setInstallationDirectoryUtil(this.installationDirectoryUtil);

		final Project project = new Project();
		when(this.contextUtil.getProjectInContext()).thenReturn(project);
		when(this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(SeedInventoryListExporter.TEMPORARY_FILE_NAME,
				SeedInventoryListExporter.XLS_EXTENSION, project, ToolName.BM_LIST_MANAGER_MAIN)).thenReturn(TEMPORAY_FILE_XLS);

	}

	@After
	public void tearDown() {
		final File file = new File(TEMPORAY_FILE_XLS);
		file.deleteOnExit();
	}

	@Test
	public void testExportSeedPreparationListSuccess() throws Exception {

		final File workbookFile =
				new File(ClassLoader.getSystemClassLoader().getResource(seedInventoryListExporter.getSeedTemplateFile()).toURI());

		Workbook workbook = WorkbookFactory.create(workbookFile);
		when(this.fileService.retrieveWorkbookTemplate(Mockito.anyString())).thenReturn(workbook);

		when(this.germplasmList.getName()).thenReturn(LIST_NAME);
		when(this.germplasmList.getDescription()).thenReturn("ListNameDescription");
		when(this.germplasmList.getType()).thenReturn("ListType");
		when(this.germplasmList.getDate()).thenReturn(new Long(20161005));

		when(this.fieldbookMiddlewareService.getOwnerListName(Mockito.anyInt())).thenReturn("listOwner");

		when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(createReservedTransactions());

		List<GermplasmListData> germplasmListDataForReservedEntries = createGermplasmListDataForReservedEntries();
		when(this.inventoryDataManager.getReservedLotDetailsForExportList(Mockito.anyInt()))
				.thenReturn(germplasmListDataForReservedEntries);

		seedInventoryListExporter.exportSeedPreparationList();

		Mockito.verify(this.fileDownloaderUtility, Mockito.times(1))
				.initiateFileDownload(TEMPORAY_FILE_XLS, "ListName-Seed Prep.xls", this.source);

		Assert.assertNotNull("Workbook should not be null", this.seedInventoryListExporter.getExcelWorkbook());

		Workbook excelWorkbook = seedInventoryListExporter.getExcelWorkbook();
		Sheet descriptionSheet = excelWorkbook.getSheetAt(0);

		Assert.assertNotNull("Description sheet should not be null", descriptionSheet);

		Assert.assertEquals("List Description in exported work book does not match", "ListNameDescription",
				descriptionSheet.getRow(1).getCell(1).getStringCellValue());

		Assert.assertEquals("List Type in exported work book does not match", "ListType",
				descriptionSheet.getRow(2).getCell(1).getStringCellValue());

		Assert.assertEquals(new Double(20161005).doubleValue(), descriptionSheet.getRow(3).getCell(1).getNumericCellValue(),
				new Double(20161005).doubleValue() - descriptionSheet.getRow(3).getCell(1).getNumericCellValue());

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

		Assert.assertEquals("CrossName  does not match with exported reserved list data", "GroupName", row.getCell(3).getStringCellValue());

		Assert.assertEquals("Source  does not match with exported reserved list data", "SeedSource", row.getCell(4).getStringCellValue());

		Assert.assertEquals("LotID  does not match with exported reserved list data", "10.0",
				new Double(row.getCell(5).getStringCellValue()).toString());

		Assert.assertEquals("Location  does not match with exported reserved list data", "locName", row.getCell(6).getStringCellValue());

		Assert.assertEquals("Stock ID  does not match with exported reserved list data", "stockIds", row.getCell(7).getStringCellValue());

		Assert.assertEquals("Transaction ID  does not match with exported reserved list data", "120.0",
				new Double(row.getCell(8).getStringCellValue()).toString());

		Assert.assertEquals("Reservation amount  does not match with exported reserved list data", "2.0",
				new Double(row.getCell(9).getStringCellValue()).toString());

		Assert.assertEquals("Withdrawal amount should be empty in exported reserved list data", "", row.getCell(10).getStringCellValue());
		Assert.assertEquals("Balance amount should be empty in exported reserved list data", "", row.getCell(11).getStringCellValue());

		Assert.assertEquals("Comments  does not match with exported reserved list data", "comments", row.getCell(12).getStringCellValue());

	}

	private List<GermplasmListData> createGermplasmListDataForReservedEntries() {
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
		ListDataInventory listDataInfo = new ListDataInventory(1, 28);
		listDataInfo.setLotRows(lots);
		listEntry.setInventoryInfo(listDataInfo);
		germplasmListData.add(listEntry);

		return germplasmListData;
	}

	private List<Transaction> createReservedTransactions() {
		Transaction transaction = new Transaction();
		transaction.setId(120);
		transaction.setQuantity(-2.0);
		transaction.setStatus(0);
		transaction.setComments("comments");

		return Lists.newArrayList(transaction);
	}

}

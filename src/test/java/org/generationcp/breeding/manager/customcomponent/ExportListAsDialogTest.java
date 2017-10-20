
package org.generationcp.breeding.manager.customcomponent;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.Application;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.data.initializer.GermplasmExportDataInitializer;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.constant.ToolEnum;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.reports.service.JasperReportService;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.reports.Reporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import org.springframework.transaction.PlatformTransactionManager;

public class ExportListAsDialogTest {

	private static final int NO_OF_LIST_ENTRIES = 10;
	private static final Integer USER_ID = 1;
	private static final Integer TEST_GERMPLASM_LIST_ID = 2;

	private static Table listDataTable;
	private static List<GermplasmListData> listEntries;
	private static GermplasmList germplasmList;

	@Mock
	private Component source;

	@Mock
	private Window window;

	@Mock
	private FileDownloadResource fileDownloadResource;

	@Mock
	private GermplasmListExporter listExporter;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private JasperReportService jasperReportService;

	@Mock
	private FileDownloaderUtility fileDownloaderUtility;

	@Mock
	private Application application;

	private ExportListAsDialog dialog;

	private static Table emptyTable;

	@BeforeClass
	public static void setUpClass() {
		ExportListAsDialogTest.emptyTable = new Table();
		ExportListAsDialogTest.listEntries = ExportListAsDialogTest.generateListEntries();
		ExportListAsDialogTest.germplasmList = ExportListAsDialogTest.getGermplasmList();
	}

	@Before
	public void setUp() throws MiddlewareQueryException, GermplasmListExporterException {

		MockitoAnnotations.initMocks(this);

		ExportListAsDialogTest.listDataTable = ExportListAsDialogTest.generateTestTable();
		this.dialog = new ExportListAsDialog(this.source, ExportListAsDialogTest.germplasmList, ExportListAsDialogTest.listDataTable);

		this.dialog.setListExporter(this.listExporter);
		this.dialog.setMessageSource(this.messageSource);
		this.dialog.setTransactionManager(this.transactionManager);
		this.dialog.setJasperReportService(this.jasperReportService);
		this.dialog.setFileDownloaderUtility(this.fileDownloaderUtility);

		Mockito.doReturn(Mockito.mock(FileOutputStream.class)).when(this.listExporter)
				.exportGermplasmListXLS(TEST_GERMPLASM_LIST_ID, ExportListAsDialog.TEMP_FILENAME, ExportListAsDialogTest.listDataTable);
		Mockito.doReturn("Export Format").when(this.messageSource).getMessage(Message.EXPORT_FORMAT);
		Mockito.doReturn("Genotyping Order").when(this.messageSource).getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER);
		Mockito.doReturn(GermplasmExportDataInitializer.createCustomReportTypeList()).when(this.jasperReportService)
				.getCustomReportTypes(ToolSection.BM_LIST_MGR_CUSTOM_REPORT.name(), ToolEnum.LIST_MANAGER.getToolName());

		Mockito.doReturn(this.window).when(this.source).getWindow();
		Mockito.doNothing().when(this.window).open(this.fileDownloadResource);
		Mockito.doReturn(this.application).when(this.source).getApplication();

		dialog.instantiateComponents();
		dialog.initializeValues();
	}

	@Test
	public void testIsARequiredColumn() {
		Assert.assertTrue("GID is a required column.", this.dialog.isARequiredColumn(ColumnLabels.GID.getName()));
		Assert.assertTrue("ENTRY_ID is a required column.", this.dialog.isARequiredColumn(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertTrue("DESIGNATION is a required column.", this.dialog.isARequiredColumn(ColumnLabels.DESIGNATION.getName()));
		Assert.assertFalse("SEED_SOURCE is a not required column.", this.dialog.isARequiredColumn(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testIsARequiredColumnHidden() {
		Assert.assertFalse("Expecting no required columns is hidden but didn't.",
				this.dialog.isARequiredColumnHidden(ExportListAsDialogTest.listDataTable));

		ExportListAsDialogTest.listDataTable.setColumnCollapsed(ColumnLabels.GID.getName(), true);
		Assert.assertTrue("Expecting one of the required columns is hidden but didn't.",
				this.dialog.isARequiredColumnHidden(ExportListAsDialogTest.listDataTable));
	}

	@Test
	public void testShowWarningMessageWhenListDataTableHasHiddenRequiredColumns() {
		ExportListAsDialogTest.listDataTable.setColumnCollapsed(ColumnLabels.GID.getName(), true);

		Assert.assertTrue(this.dialog.isARequiredColumnHidden(ExportListAsDialogTest.listDataTable));
		this.dialog.showWarningMessage(ExportListAsDialogTest.listDataTable);
	}

	@Test
	public void testShowWarningMessageWhenListDataTableDoNotHaveHiddenRequiredColumns() {
		Assert.assertFalse(this.dialog.isARequiredColumnHidden(ExportListAsDialogTest.listDataTable));
	}

	@Test
	public void testExportListAsXLS() throws GermplasmListExporterException, MiddlewareQueryException {
		this.dialog.exportListAsXLS(ExportListAsDialogTest.listDataTable);
		Mockito.verify(this.listExporter, Mockito.times(1)).exportGermplasmListXLS(TEST_GERMPLASM_LIST_ID,
				ExportListAsDialog.TEMP_FILENAME, ExportListAsDialogTest.listDataTable);
	}

	@Test
	public void testExportListAsXLSWithException() throws GermplasmListExporterException, MiddlewareQueryException {
		Mockito.doThrow(new GermplasmListExporterException()).when(this.listExporter)
				.exportGermplasmListXLS(TEST_GERMPLASM_LIST_ID, ExportListAsDialog.TEMP_FILENAME, ExportListAsDialogTest.emptyTable);
		this.dialog.exportListAsXLS(ExportListAsDialogTest.emptyTable);
		Mockito.verify(this.window, Mockito.times(0)).open(this.fileDownloadResource);
	}

	@Test
	public void testExportListAsCSV() throws GermplasmListExporterException {

		this.dialog.exportListAsCSV(ExportListAsDialogTest.listDataTable);
		Mockito.verify(this.listExporter, Mockito.times(1)).exportGermplasmListCSV(ExportListAsDialog.TEMP_FILENAME,
				ExportListAsDialogTest.listDataTable);
	}

	@Test
	public void testExportListAsCustomReport() throws GermplasmListExporterException {

		// set the drop down so that it simulates selection of the user of a custom report type
		List<CustomReportType> customReportTypes = GermplasmExportDataInitializer.createCustomReportTypeList();
        CustomReportType customReport = customReportTypes.get(0);
        this.dialog.setExportOptionValue(this.dialog.formatCustomReportString(customReport));

        Reporter reporter = Mockito.mock(Reporter.class);
		Mockito.doReturn(reporter)
				.when(this.listExporter)
				.exportGermplasmListCustomReport(TEST_GERMPLASM_LIST_ID, ExportListAsDialog.TEMP_FILENAME,
						customReport.getCode());

		this.dialog.exportListAction(new Table());

		// verify that custom report generation is triggered when executing the current export action
		Mockito.verify(this.listExporter).exportGermplasmListCustomReport(TEST_GERMPLASM_LIST_ID, ExportListAsDialog.TEMP_FILENAME,
				customReport.getCode());
	}

	@Test
	public void testExportListAsCSVWithException() throws GermplasmListExporterException {
		Mockito.doThrow(new GermplasmListExporterException()).when(this.listExporter)
				.exportGermplasmListCSV(ExportListAsDialog.TEMP_FILENAME, ExportListAsDialogTest.emptyTable);
		this.dialog.exportListAsCSV(ExportListAsDialogTest.emptyTable);
		Mockito.verify(this.window, Mockito.times(0)).open(this.fileDownloadResource);
	}

	private static GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(ExportListAsDialogTest.USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(ExportListAsDialogTest.generateListEntries());
		germplasmList.setId(TEST_GERMPLASM_LIST_ID);
		germplasmList.setStatus(100);

		return germplasmList;
	}

	private static Table generateTestTable() {
		Table listDataTable = new Table();

		listDataTable.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		listDataTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		listDataTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		listDataTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		listDataTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		listDataTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		listDataTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);
		listDataTable.setColumnCollapsingAllowed(true);

		ExportListAsDialogTest.loadEntriesToListDataTable(listDataTable);

		return listDataTable;
	}

	private static List<GermplasmListData> generateListEntries() {
		List<GermplasmListData> entries = new ArrayList<>();

		for (int x = 1; x <= ExportListAsDialogTest.NO_OF_LIST_ENTRIES; x++) {
			GermplasmListData germplasmListData = new GermplasmListData();
			germplasmListData.setId(x);
			germplasmListData.setEntryId(x);
			germplasmListData.setDesignation(ColumnLabels.DESIGNATION.getName() + x);
			germplasmListData.setGroupName(ColumnLabels.PARENTAGE.getName() + x);
			ListDataInventory inventoryInfo = new ListDataInventory(x, x);
			inventoryInfo.setLotCount(1);
			inventoryInfo.setReservedLotCount(1);
			inventoryInfo.setActualInventoryLotCount(1);
			germplasmListData.setInventoryInfo(inventoryInfo);
			germplasmListData.setEntryCode(ColumnLabels.ENTRY_CODE.getName() + x);
			germplasmListData.setSeedSource(ColumnLabels.SEED_SOURCE.getName() + x);
			germplasmListData.setGid(x);
			entries.add(germplasmListData);
		}

		return entries;
	}

	private static void loadEntriesToListDataTable(Table listDataTable) {
		for (GermplasmListData entry : ExportListAsDialogTest.listEntries) {
			ExportListAsDialogTest.addListEntryToTable(entry, listDataTable);
		}

		listDataTable.sort(new Object[] {ColumnLabels.ENTRY_ID.getName()}, new boolean[] {true});
	}

	private static void addListEntryToTable(GermplasmListData entry, final Table listDataTable) {
		String gid = String.format("%s", entry.getGid().toString());
		Button gidButton = new Button(gid, new GidLinkButtonClickListener(null, gid, true, true));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription("Click to view Germplasm information");

		Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(null, gid, true, true));
		desigButton.setStyleName(BaseTheme.BUTTON_LINK);
		desigButton.setDescription("Click to view Germplasm information");

		CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setData(entry.getId());
		itemCheckBox.setImmediate(true);
		itemCheckBox.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (((Boolean) itemCheckBox.getValue()).equals(true)) {
					listDataTable.select(itemCheckBox.getData());
				} else {
					listDataTable.unselect(itemCheckBox.getData());
				}
			}

		});

		Item newItem = listDataTable.getContainerDataSource().addItem(entry.getId());
		newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
		newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
		newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
		newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(entry.getGroupName());
		newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
		newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
		newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(entry.getSeedSource());

		// #1 Available Inventory
		// default value
		String availInv = "-";
		if (entry.getInventoryInfo().getLotCount().intValue() != 0) {
			availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
		}
		Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(null, null, entry.getId(), entry.getGid()));
		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
		inventoryButton.setDescription(null);
		newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

		if ("-".equals(availInv)) {
			inventoryButton.setEnabled(false);
			inventoryButton.setDescription("No Lot for this Germplasm");
		} else {
			inventoryButton.setDescription(null);
		}

		// #2 Seed Reserved
		// default value
		String seedRes = "-";
		if (entry.getInventoryInfo().getReservedLotCount().intValue() != 0) {
			seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
		}
		newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
	}

	@Test
	public void testFinishExportListenerLockedList() {

		Integer lockedStatus = 100;

		Window parentWindow = Mockito.mock(Window.class);
		ExportListAsDialog exportListAsDialogMock = Mockito.mock(ExportListAsDialog.class);
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setStatus(lockedStatus);

		Mockito.when(exportListAsDialogMock.getParent()).thenReturn(parentWindow);
		Mockito.when(exportListAsDialogMock.getGermplasmList()).thenReturn(germplasmList);

		ExportListAsDialog.FinishButtonListener finishButtonListener = new ExportListAsDialog.FinishButtonListener(exportListAsDialogMock);

		finishButtonListener.buttonClick(null);

		// ExportListAction should be called
		Mockito.verify(exportListAsDialogMock, Mockito.times(1)).exportListAction(Mockito.any(Table.class));
		// ExportListAsDialog window should be closed
		Mockito.verify(parentWindow, Mockito.times(1)).removeWindow(exportListAsDialogMock);
	}

	@Test
	public void testFinishExportListenerListIsNotLocked() {

		Integer lockedStatus = 1;

		Window parentWindow = Mockito.mock(Window.class);
		ExportListAsDialog exportListAsDialogMock = Mockito.mock(ExportListAsDialog.class);
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setStatus(lockedStatus);

		Mockito.when(exportListAsDialogMock.getParent()).thenReturn(parentWindow);
		Mockito.when(exportListAsDialogMock.getWindow()).thenReturn(parentWindow);
		Mockito.when(exportListAsDialogMock.getGermplasmList()).thenReturn(germplasmList);
		Mockito.when(exportListAsDialogMock.getMessageSource()).thenReturn(messageSource);

		ExportListAsDialog.FinishButtonListener finishButtonListener = new ExportListAsDialog.FinishButtonListener(exportListAsDialogMock);

		finishButtonListener.buttonClick(null);

		// ExportListAction should not be called
		Mockito.verify(exportListAsDialogMock, Mockito.times(0)).exportListAction(Mockito.any(Table.class));
		// Window should not be closed
		Mockito.verify(parentWindow, Mockito.times(0)).removeWindow(exportListAsDialogMock);

		// Error message should be displayed
		Mockito.verify(messageSource).getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED);
	}

}

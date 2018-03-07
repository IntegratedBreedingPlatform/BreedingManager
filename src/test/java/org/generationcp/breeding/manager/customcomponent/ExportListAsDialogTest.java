package org.generationcp.breeding.manager.customcomponent;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.data.initializer.GermplasmExportDataInitializer;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.reports.service.JasperReportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.util.VaadinFileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.reports.Reporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class ExportListAsDialogTest {

	private static final int NO_OF_LIST_ENTRIES = 10;
	private static final Integer USER_ID = 1;
	private static final Integer TEST_GERMPLASM_LIST_ID = 2;
	private static final String TEMPORARY_FILE_PATH_CSV = "/temporary/file/path/temporaryFile.csv";
	private static final String TEMPORARY_FILE_PATH_XLS = "/temporary/file/path/temporaryFile.csv";
	
	public static final String ERROR_EXPORTING_LIST = "Error Exporting list";

	private static Table listDataTable;
	private static List<GermplasmListData> listEntries;
	private static GermplasmList germplasmList;

	@Mock
	private Component source;

	@Mock
	private Window window;

	@Mock
	private VaadinFileDownloadResource fileDownloadResource;

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

	@Mock
	private InstallationDirectoryUtil installationDirectoryUtil;

	@Mock
	private ContextUtil contextUtil;

	private ExportListAsDialog dialog;

	private static Table emptyTable;

	@BeforeClass
	public static void setUpClass() {
		ExportListAsDialogTest.emptyTable = new Table();
		ExportListAsDialogTest.listEntries = ExportListAsDialogTest.generateListEntries();
		ExportListAsDialogTest.germplasmList = ExportListAsDialogTest.getGermplasmList();
	}

	@Before
	public void setUp() throws GermplasmListExporterException, IOException {

		MockitoAnnotations.initMocks(this);

		ExportListAsDialogTest.listDataTable = ExportListAsDialogTest.generateTestTable();
		this.dialog = new ExportListAsDialog(this.source, ExportListAsDialogTest.germplasmList, ExportListAsDialogTest.listDataTable);

		this.dialog.setListExporter(this.listExporter);
		this.dialog.setMessageSource(this.messageSource);
		this.dialog.setTransactionManager(this.transactionManager);
		this.dialog.setJasperReportService(this.jasperReportService);
		this.dialog.setFileDownloaderUtility(this.fileDownloaderUtility);
		this.dialog.setInstallationDirectoryUtil(this.installationDirectoryUtil);
		this.dialog.setContextUtil(this.contextUtil);

		Mockito.doReturn(Mockito.mock(FileOutputStream.class)).when(this.listExporter)
				.exportGermplasmListXLS(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID, ExportListAsDialog.TEMP_FILENAME,
						ExportListAsDialogTest.listDataTable);
		Mockito.doReturn("Export Format").when(this.messageSource).getMessage(Message.EXPORT_FORMAT);
		Mockito.doReturn("Genotyping Order").when(this.messageSource).getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER);
		Mockito.doReturn(ERROR_EXPORTING_LIST).when(this.messageSource).getMessage(Message.ERROR_EXPORTING_LIST);
		Mockito.doReturn(GermplasmExportDataInitializer.createCustomReportTypeList()).when(this.jasperReportService)
				.getCustomReportTypes(ToolSection.BM_LIST_MGR_CUSTOM_REPORT.name(), ToolName.LIST_MANAGER.getName());

		Mockito.doReturn(this.window).when(this.source).getWindow();
		Mockito.doNothing().when(this.window).open(this.fileDownloadResource);
		Mockito.doReturn(this.application).when(this.source).getApplication();

		Mockito.when(contextUtil.getProjectInContext()).thenReturn(new Project());

		Mockito.when(this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(Mockito.anyString(),
				Mockito.eq(ExportListAsDialog.CSV_EXT), Mockito.any(Project.class),
				Mockito.any(ToolName.class))).thenReturn(TEMPORARY_FILE_PATH_CSV);
		Mockito.when(this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(Mockito.anyString(),
				Mockito.eq(ExportListAsDialog.XLS_EXT), Mockito.any(Project.class),
				Mockito.any(ToolName.class))).thenReturn(TEMPORARY_FILE_PATH_XLS);

		this.dialog.instantiateComponents();
		this.dialog.initializeValues();
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
	public void testExportListAsXLS() throws GermplasmListExporterException {
		this.dialog.exportListAsXLS(ExportListAsDialogTest.listDataTable);
		verify(this.listExporter, Mockito.times(1))
				.exportGermplasmListXLS(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID, TEMPORARY_FILE_PATH_XLS,
						ExportListAsDialogTest.listDataTable);
		verify(this.fileDownloaderUtility, Mockito.times(1))
				.initiateFileDownload(TEMPORARY_FILE_PATH_XLS, ExportListAsDialogTest.germplasmList.getName() + ExportListAsDialog.XLS_EXT, this.source);
	}

	@Test
	public void testExportListAsXLSWithException() throws GermplasmListExporterException {
		Mockito.doThrow(new GermplasmListExporterException()).when(this.listExporter)
				.exportGermplasmListXLS(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID, TEMPORARY_FILE_PATH_XLS,
						ExportListAsDialogTest.emptyTable);
		this.dialog.exportListAsXLS(ExportListAsDialogTest.emptyTable);

		verify(this.fileDownloaderUtility, Mockito.times(0))
				.initiateFileDownload(TEMPORARY_FILE_PATH_XLS, ExportListAsDialogTest.germplasmList.getName() + ExportListAsDialog.XLS_EXT, this.source);

		final ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);
		verify(this.window).showNotification(captor.capture());
		final Window.Notification notification = captor.getValue();
		assertEquals(ERROR_EXPORTING_LIST, notification.getCaption());
	}

	@Test
	public void testExportListAsCSV() throws GermplasmListExporterException {

		this.dialog.exportListAsCSV(ExportListAsDialogTest.listDataTable);
		verify(this.listExporter, Mockito.times(1))
				.exportGermplasmListCSV(TEMPORARY_FILE_PATH_CSV, ExportListAsDialogTest.listDataTable,
						ExportListAsDialogTest.germplasmList.getId());
		verify(this.fileDownloaderUtility, Mockito.times(1))
				.initiateFileDownload(TEMPORARY_FILE_PATH_CSV, ExportListAsDialogTest.germplasmList.getName() + ExportListAsDialog.CSV_EXT, this.source);
	}

	@Test
	public void testExportListAsCustomReport() throws GermplasmListExporterException {

		// set the drop down so that it simulates selection of the user of a custom report type
		final List<CustomReportType> customReportTypes = GermplasmExportDataInitializer.createCustomReportTypeList();
		final CustomReportType customReport = customReportTypes.get(0);
		this.dialog.setExportOptionValue(this.dialog.formatCustomReportString(customReport));

		final Reporter reporter = Mockito.mock(Reporter.class);
		Mockito.doReturn(reporter).when(this.listExporter)
				.exportGermplasmListCustomReport(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID, TEMPORARY_FILE_PATH_XLS,
						customReport.getCode());

		this.dialog.exportListAction(new Table());

		// verify that custom report generation is triggered when executing the current export action
		verify(this.listExporter)
				.exportGermplasmListCustomReport(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID, TEMPORARY_FILE_PATH_XLS,
						customReport.getCode());
	}

	@Test
	public void testExportListAsCSVWithException() throws GermplasmListExporterException {
		Mockito.doThrow(new GermplasmListExporterException()).when(this.listExporter)
				.exportGermplasmListCSV(TEMPORARY_FILE_PATH_CSV, ExportListAsDialogTest.emptyTable,
						ExportListAsDialogTest.germplasmList.getId());
		this.dialog.exportListAsCSV(ExportListAsDialogTest.emptyTable);
		verify(this.fileDownloaderUtility, Mockito.times(0))
				.initiateFileDownload(TEMPORARY_FILE_PATH_CSV, ExportListAsDialogTest.germplasmList.getName() + ExportListAsDialog.CSV_EXT, this.source);

		final ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);
		verify(this.window).showNotification(captor.capture());
		final Window.Notification notification = captor.getValue();
		assertEquals(ERROR_EXPORTING_LIST, notification.getCaption());
	}

	@Test
	public void testExportListForGenotypingOrderAction() throws GermplasmListExporterException {
		this.dialog.exportListForGenotypingOrderAction();
		verify(this.listExporter, Mockito.times(1))
				.exportKBioScienceGenotypingOrderXLS(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID, TEMPORARY_FILE_PATH_CSV,
						ExportListAsDialog.DEFAULT_PLATE_SIZE);
		verify(this.fileDownloaderUtility, Mockito.times(1)).initiateFileDownload(TEMPORARY_FILE_PATH_CSV,
				ExportListAsDialogTest.germplasmList.getName().replace(" ", "_") + "ForGenotyping.xls", this.source);
	}

	@Test
	public void testExportListForGenotypingOrderActionWithException() throws GermplasmListExporterException {
		Mockito.doThrow(new GermplasmListExporterException()).when(this.listExporter)
				.exportKBioScienceGenotypingOrderXLS(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID, TEMPORARY_FILE_PATH_CSV,
						ExportListAsDialog.DEFAULT_PLATE_SIZE);
		this.dialog.exportListForGenotypingOrderAction();
		verify(this.fileDownloaderUtility, Mockito.times(0)).initiateFileDownload(TEMPORARY_FILE_PATH_CSV,
				ExportListAsDialogTest.germplasmList.getName().replace(" ", "_") + "ForGenotyping.xls", this.source);

		final ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);
		verify(this.window).showNotification(captor.capture());
		final Window.Notification notification = captor.getValue();
		assertEquals(ERROR_EXPORTING_LIST, notification.getCaption());
	}
	
	@Test
	public void testExportListForGenotypingOrderActionForUnlockedList() throws GermplasmListExporterException {
		// Set status of list = unlocked
		ExportListAsDialogTest.germplasmList.setStatus(1);
		this.dialog.exportListForGenotypingOrderAction();
		Mockito.verifyZeroInteractions(this.listExporter);
		Mockito.verifyZeroInteractions(this.fileDownloaderUtility);
		Mockito.verifyZeroInteractions(this.installationDirectoryUtil);
	}

	private static GermplasmList getGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(ExportListAsDialogTest.USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(ExportListAsDialogTest.generateListEntries());
		germplasmList.setId(ExportListAsDialogTest.TEST_GERMPLASM_LIST_ID);
		germplasmList.setStatus(100);

		return germplasmList;
	}

	private static Table generateTestTable() {
		final Table listDataTable = new Table();

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
		final List<GermplasmListData> entries = new ArrayList<>();

		for (int x = 1; x <= ExportListAsDialogTest.NO_OF_LIST_ENTRIES; x++) {
			final GermplasmListData germplasmListData = new GermplasmListData();
			germplasmListData.setId(x);
			germplasmListData.setEntryId(x);
			germplasmListData.setDesignation(ColumnLabels.DESIGNATION.getName() + x);
			germplasmListData.setGroupName(ColumnLabels.PARENTAGE.getName() + x);
			final ListDataInventory inventoryInfo = new ListDataInventory(x, x);
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

	private static void loadEntriesToListDataTable(final Table listDataTable) {
		for (final GermplasmListData entry : ExportListAsDialogTest.listEntries) {
			ExportListAsDialogTest.addListEntryToTable(entry, listDataTable);
		}

		listDataTable.sort(new Object[] {ColumnLabels.ENTRY_ID.getName()}, new boolean[] {true});
	}

	private static void addListEntryToTable(final GermplasmListData entry, final Table listDataTable) {
		final String gid = String.format("%s", entry.getGid().toString());
		final Button gidButton = new Button(gid, new GidLinkButtonClickListener(null, gid, true, true));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription("Click to view Germplasm information");

		final Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(null, gid, true, true));
		desigButton.setStyleName(BaseTheme.BUTTON_LINK);
		desigButton.setDescription("Click to view Germplasm information");

		final CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setData(entry.getId());
		itemCheckBox.setImmediate(true);
		itemCheckBox.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				final CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (((Boolean) itemCheckBox.getValue()).equals(true)) {
					listDataTable.select(itemCheckBox.getData());
				} else {
					listDataTable.unselect(itemCheckBox.getData());
				}
			}

		});

		final Item newItem = listDataTable.getContainerDataSource().addItem(entry.getId());
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
		final Button inventoryButton =
				new Button(availInv, new InventoryLinkButtonClickListener(null, null, entry.getId(), entry.getGid()));
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

		final Integer lockedStatus = 100;

		final Window parentWindow = Mockito.mock(Window.class);
		final ExportListAsDialog exportListAsDialogMock = Mockito.mock(ExportListAsDialog.class);
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setStatus(lockedStatus);

		Mockito.when(exportListAsDialogMock.getParent()).thenReturn(parentWindow);
		Mockito.when(exportListAsDialogMock.getGermplasmList()).thenReturn(germplasmList);

		final ExportListAsDialog.FinishButtonListener finishButtonListener =
				new ExportListAsDialog.FinishButtonListener(exportListAsDialogMock);

		finishButtonListener.buttonClick(null);

		// ExportListAction should be called
		verify(exportListAsDialogMock, Mockito.times(1)).exportListAction(Matchers.any(Table.class));
		// ExportListAsDialog window should be closed
		verify(parentWindow, Mockito.times(1)).removeWindow(exportListAsDialogMock);
	}

	@Test
	public void testFinishExportListenerListIsNotLocked() {

		final Integer lockedStatus = 1;

		final Window parentWindow = Mockito.mock(Window.class);
		final ExportListAsDialog exportListAsDialogMock = Mockito.mock(ExportListAsDialog.class);
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setStatus(lockedStatus);

		Mockito.when(exportListAsDialogMock.getParent()).thenReturn(parentWindow);
		Mockito.when(exportListAsDialogMock.getWindow()).thenReturn(parentWindow);
		Mockito.when(exportListAsDialogMock.getGermplasmList()).thenReturn(germplasmList);
		Mockito.when(exportListAsDialogMock.getMessageSource()).thenReturn(this.messageSource);

		final ExportListAsDialog.FinishButtonListener finishButtonListener =
				new ExportListAsDialog.FinishButtonListener(exportListAsDialogMock);

		finishButtonListener.buttonClick(null);

		// ExportListAction should not be called
		verify(exportListAsDialogMock, Mockito.times(0)).exportListAction(Matchers.any(Table.class));
		// Window should not be closed
		verify(parentWindow, Mockito.times(0)).removeWindow(exportListAsDialogMock);

		// Error message should be displayed
		verify(this.messageSource).getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED);
	}

}

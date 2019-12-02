
package org.generationcp.breeding.manager.listmanager.util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.service.impl.GermplasmExportServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmListNewColumnsInfoTestDataInitializer;
import org.generationcp.middleware.data.initializer.UserDefinedFieldTestDataInitializer;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

public class GermplasmListExporterTest {

	private static final String SEED_SOURCE = "SEED_SOURCE";
	private static final String CROSS = "CROSS";
	private static final String DESIG = "DESIG";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	private static final String GID = "GID";
	private static final String ENTRY_ID = "ENTRY_ID";
	private static final String OWNER_NAME = "User User";
	private static final String FILE_NAME = "testGermplasmListExporter.csv";
	private static final Integer GERMPLASM_LIST_ID = 10970378;
	private static final GermplasmListNewColumnsInfo CURRENT_COLUMNS_INFO =
			new GermplasmListNewColumnsInfo(GermplasmListExporterTest.GERMPLASM_LIST_ID);
	public static final String LIST_NAME = "ABCD";
	public static final String PREFERRED_NAME = "PREFERRED_NAME";
	public static final  String CODE1 = "CODE1";
	public static final  String CODE_1 = "CODE 1";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private FileService fileService;

	@Mock
	private GermplasmExportServiceImpl germplasmExportService;

	@Mock
	private UserService userService;

	@InjectMocks
	private final GermplasmListExporter _germplasmListExporter = new GermplasmListExporter();

	private GermplasmListExporter germplasmListExporter;
	private static final int USER_ID = 1;
	private static final int PERSON_ID = 1;
	private static final long NO_OF_LIST_ENTRIES = 10;
	private static Table listDataTable;
	private static List<GermplasmListData> listEntries;

	private static final int PLATE_SIZE = 98;
	private static final String STOCKID = "STOCKID";
	private static final String SEED_AMOUNT_G = "SEED_AMOUNT_G";

	@BeforeClass
	public static void setUpClass() {

		GermplasmListExporterTest.listEntries = GermplasmListExporterTest.generateListEntries();
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();
	}

	@Before
	public void setUp() throws MiddlewareQueryException, IllegalAccessException {

		MockitoAnnotations.initMocks(this);

		this._germplasmListExporter.setGermplasmExportService(this.germplasmExportService);
		this._germplasmListExporter.setGermplasmListManager(this.germplasmListManager);
		this._germplasmListExporter.setInventoryDataManager(this.inventoryDataManager);
		this._germplasmListExporter.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		this.germplasmListExporter = Mockito.spy(this._germplasmListExporter);

		Mockito.doReturn("#").when(this.messageSource).getMessage(Message.HASHTAG);

		Mockito.doReturn(GermplasmListExporterTest.GID).when(this.germplasmListExporter).getTermNameFromOntology(ColumnLabels.GID);
		Mockito.doReturn(GermplasmListExporterTest.ENTRY_CODE).when(this.germplasmListExporter)
				.getTermNameFromOntology(ColumnLabels.ENTRY_CODE);
		Mockito.doReturn(GermplasmListExporterTest.DESIG).when(this.germplasmListExporter)
				.getTermNameFromOntology(ColumnLabels.DESIGNATION);
		Mockito.doReturn(GermplasmListExporterTest.CROSS).when(this.germplasmListExporter).getTermNameFromOntology(ColumnLabels.PARENTAGE);
		Mockito.doReturn(GermplasmListExporterTest.SEED_SOURCE).when(this.germplasmListExporter)
				.getTermNameFromOntology(ColumnLabels.SEED_SOURCE);

		// set up test data for germplasm list
		Mockito.doReturn(this.getGermplasmList()).when(this.germplasmListManager).getGermplasmListById(GermplasmListExporterTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(GermplasmListExporterTest.NO_OF_LIST_ENTRIES).when(this.germplasmListManager)
				.countGermplasmListDataByListId(GermplasmListExporterTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(GermplasmListExporterTest.generateListEntries()).when(this.germplasmListManager)
				.getGermplasmListDataByListId(GermplasmListExporterTest.GERMPLASM_LIST_ID);
		Mockito.when(this.germplasmListManager.getAdditionalColumnsForList(GERMPLASM_LIST_ID)).thenReturn(CURRENT_COLUMNS_INFO);

	}

	@Test
	public void testGetColumnHeadersForGenotypingData() {
		final List<ExportColumnHeader> exportColumnList =
				this.germplasmListExporter.getColumnHeadersForGenotypingData(GermplasmListExporterTest.PLATE_SIZE);

		Assert.assertTrue("Expected that the total number of columns will be 8", exportColumnList.size() == 8);
		Assert.assertEquals("Expected to have a Subject ID as the 1st column", exportColumnList.get(0).getName(), "Subject ID");
		Assert.assertEquals("Expected to have a Plate ID as the 2nd column", exportColumnList.get(1).getName(), "Plate ID");
		Assert.assertEquals("Expected to have a Well ID as the 3rd column", exportColumnList.get(2).getName(), "Well");
		Assert.assertEquals("Expected to have a Sample type as the 4th column", exportColumnList.get(3).getName(), "Sample type");
		Assert.assertEquals("Expected to have a " + GermplasmListExporterTest.PLATE_SIZE + " as the 5th column",
				exportColumnList.get(4).getName(), String.valueOf(GermplasmListExporterTest.PLATE_SIZE));
		Assert.assertEquals("Expected to have a Primer as the 6th column", exportColumnList.get(5).getName(), "Primer");
		Assert.assertEquals("Expected to have a Subject BC as the 7th column", exportColumnList.get(6).getName(), "Subject BC");
		Assert.assertEquals("Expected to have a Plate BC as the 8th column", exportColumnList.get(7).getName(), "Plate BC");
	}

	@Test
	public void testGetColumnValuesForGenotypingData() throws GermplasmListExporterException {
		final List<ExportRow> exportRows = this.germplasmListExporter
				.getColumnValuesForGenotypingData(GermplasmListExporterTest.GERMPLASM_LIST_ID, GermplasmListExporterTest.PLATE_SIZE);
		Assert.assertTrue("Expected to have a total of " + GermplasmListExporterTest.NO_OF_LIST_ENTRIES + " entries",
				exportRows.size() == GermplasmListExporterTest.NO_OF_LIST_ENTRIES);
	}

	@Test
	public void testExportKBioScienceGenotypingOrderXLS() {
		try {

			this.germplasmListExporter.exportKBioScienceGenotypingOrderXLS(GermplasmListExporterTest.GERMPLASM_LIST_ID,
					GermplasmListExporterTest.FILE_NAME, GermplasmListExporterTest.PLATE_SIZE);

		} catch (final GermplasmListExporterException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetGermplasmListAndListData() {

		GermplasmList germplasmList = null;
		try {
			germplasmList = this.germplasmListExporter.getGermplasmListAndListData(GermplasmListExporterTest.GERMPLASM_LIST_ID);
		} catch (final GermplasmListExporterException e) {
			Assert.fail("Expected to return a germplasmList object but throws an exception instead.");
		}

		Assert.assertNotNull("Expected the returned germplasmList object is not null.", germplasmList);
		Assert.assertNotNull("Expected the listData of the returned germplasmList object is not null.", germplasmList.getListData());
	}

	private Person getPerson() {
		final Person person = new Person();
		person.setId(GermplasmListExporterTest.PERSON_ID);
		person.setFirstName("FirstName");
		person.setLastName("LastName");
		return person;
	}

	private WorkbenchUser getUser() {
		final WorkbenchUser user = new WorkbenchUser();
		user.setUserid(GermplasmListExporterTest.USER_ID);
		user.setName(GermplasmListExporterTest.OWNER_NAME);
		user.setPerson(this.getPerson());
		return user;
	}

	@Test
	public void testGetVisibleColumnMap() {
		Map<String, Boolean> visibleColumnsMap;
		int visibleColumnCount;
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();

		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected to have exactly 10 visible columns.", visibleColumnCount == 10);

		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);
		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.PARENTAGE.getName(), true);
		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected to have exactly 8 visible columns.", visibleColumnCount == 8);

		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.DESIGNATION.getName(), true);
		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.GID.getName(), true);
		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected still to have exactly 8 visible columns when collapsing the required columns.",
				visibleColumnCount == 8);

		// reset
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();
	}

	private int getNoOfVisibleColumns(final Map<String, Boolean> visibleColumnsMap) {
		int visibleColumnCount = 0;
		for (final Map.Entry<String, Boolean> column : visibleColumnsMap.entrySet()) {
			final Boolean isVisible = column.getValue();
			if (isVisible) {
				visibleColumnCount++;
			}
		}
		return visibleColumnCount;
	}

	@Test
	public void testExportGermplasmListXLS()
			throws MiddlewareQueryException, GermplasmListExporterException, IOException, InvalidFormatException {
		this.configureTermNamesFromDefault();
		Mockito.doReturn(this.getUser()).when(this.userService).getUserById(Matchers.anyInt());

		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SOURCE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.PREFERRED_NAME.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntology);

		Mockito.doReturn(this.createWorkbook()).when(this.fileService).retrieveWorkbookTemplate(GermplasmListExporterTest.FILE_NAME);

		this.germplasmListExporter.exportGermplasmListXLS(GermplasmListExporterTest.GERMPLASM_LIST_ID, GermplasmListExporterTest.FILE_NAME,
				GermplasmListExporterTest.listDataTable);
		// make sure that generateGermplasmListExcelFile is called and without errors
		Mockito.verify(this.germplasmExportService, Mockito.times(1))
				.generateGermplasmListExcelFile(Matchers.any(GermplasmListExportInputValues.class));
	}

	@Test(expected = GermplasmListExporterException.class)
	public void testExportGermplasmListXLSWithException() throws GermplasmListExporterException, MiddlewareQueryException {

		final WorkbenchUser user = this.getUser();

		Mockito.doReturn(Mockito.mock(Variable.class)).when(this.ontologyVariableDataManager).getVariable(Matchers.anyString(),
				Matchers.anyInt(), Matchers.eq(false));
		Mockito.doReturn(user).when(this.userService).getUserById(Matchers.anyInt());

		Mockito.doThrow(new GermplasmListExporterException()).when(this.germplasmExportService)
				.generateGermplasmListExcelFile(Matchers.any(GermplasmListExportInputValues.class));
		this.germplasmListExporter.exportGermplasmListXLS(GermplasmListExporterTest.GERMPLASM_LIST_ID, GermplasmListExporterTest.FILE_NAME,
				new Table());

	}

	@Test
	public void testExportGermplasmListCSV() {

		try {

			this.germplasmListExporter.exportGermplasmListCSV(GermplasmListExporterTest.FILE_NAME, GermplasmListExporterTest.listDataTable,
					GermplasmListExporterTest.GERMPLASM_LIST_ID);
			// make sure that generateCSVFile is called and without errors
			Mockito.verify(this.germplasmExportService, Mockito.times(1)).generateCSVFile(Matchers.any(List.class),
					Matchers.any(List.class), Matchers.anyString());

		} catch (GermplasmListExporterException | IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(expected = GermplasmListExporterException.class)
	public void testExportGermplasmListCSVWithException() throws GermplasmListExporterException, IOException {

		Mockito.doThrow(new IOException()).when(this.germplasmExportService).generateCSVFile(Matchers.any(List.class),
				Matchers.any(List.class), Matchers.anyString());
		this.germplasmListExporter.exportGermplasmListCSV(GermplasmListExporterTest.FILE_NAME, GermplasmListExporterTest.listDataTable,
				GermplasmListExporterTest.GERMPLASM_LIST_ID);

	}

	@Test
	public void testGetExportColumnHeadersFromTable_AllColumnsAreVisible() throws MiddlewareQueryException {

		this.configureTermNamesFromDefault();

		final List<ExportColumnHeader> exportColumnHeaders = this.germplasmListExporter
				.getExportColumnHeadersFromTable(GermplasmListExporterTest.listDataTable, GermplasmListExporterTest.CURRENT_COLUMNS_INFO);

		// no columns in the table are hidden/collapsed, therefore the isDisplay is always true for all headers
		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(4).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(5).isDisplay());
		
		// make sure the header in CSV is same as the header in table
		Assert.assertEquals(GermplasmListExporterTest.ENTRY_ID, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(GermplasmListExporterTest.GID, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(GermplasmListExporterTest.ENTRY_CODE, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(GermplasmListExporterTest.DESIG, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(GermplasmListExporterTest.CROSS, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(GermplasmListExporterTest.SEED_SOURCE, exportColumnHeaders.get(5).getName());
	}

	@Test
	public void testGetExportColumnHeadersFromTable_NoColumnsAreVisible() throws MiddlewareQueryException {

		this.configureTermNamesFromDefault();

		final Table collapsedColumnTable = GermplasmListExporterTest.generateTestTable();
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.ENTRY_ID.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.DESIGNATION.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.PARENTAGE.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.GID.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.ENTRY_CODE.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);

		final List<ExportColumnHeader> exportColumnHeaders = this.germplasmListExporter
				.getExportColumnHeadersFromTable(collapsedColumnTable, GermplasmListExporterTest.CURRENT_COLUMNS_INFO);

		// EntryID, GID and DESIGNATION are all required columns, so their isDisplay is always true.
		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertFalse(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertFalse(exportColumnHeaders.get(4).isDisplay());
		Assert.assertFalse(exportColumnHeaders.get(5).isDisplay());

		// make sure the header in CSV is same as the header in table
		Assert.assertEquals(GermplasmListExporterTest.ENTRY_ID, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(GermplasmListExporterTest.GID, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(GermplasmListExporterTest.ENTRY_CODE, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(GermplasmListExporterTest.DESIG, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(GermplasmListExporterTest.CROSS, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(GermplasmListExporterTest.SEED_SOURCE, exportColumnHeaders.get(5).getName());
	}

	@Test
	public void testGetExportColumnValuesFromTable() throws GermplasmListExporterException {

		final List<ExportRow> exportRows = this.germplasmListExporter
				.getExportColumnValuesFromTable(GermplasmListExporterTest.listDataTable, GermplasmListExporterTest.CURRENT_COLUMNS_INFO);
		Assert.assertEquals(10, exportRows.size());

		// check if the values from the table match the created pojos
		for (int x = 0; x < exportRows.size(); x++) {
			final ExportRow row = exportRows.get(x);
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getEntryId().toString(), row.getValueForColumn(0));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getGid().toString(), row.getValueForColumn(1));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getEntryCode(), row.getValueForColumn(2));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getDesignation(), row.getValueForColumn(3));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getGroupName(), row.getValueForColumn(4));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getSeedSource(), row.getValueForColumn(5));
		}

	}

	@Test
	public void testHasParentsColumn_returnsFalseForTableWithoutFemaleParentColumnHeader() {
		final Table listDataTable = GermplasmListExporterTest.generateTestTable();
		Assert.assertFalse("Expected to return false for table without Parents Column but didn't.",
				this.germplasmListExporter.hasParentsColumn(listDataTable));
	}

	@Test
	public void testHasParentsColumn_returnsTrueForTableWithFemaleParentColumnHeader() {
		final Table listDataTable = GermplasmListExporterTest.generateTestTable();
		listDataTable.addContainerProperty(ColumnLabels.FEMALE_PARENT.getName(), Button.class, null);
		Assert.assertFalse("Expected to return true for table with Parents Column but didn't.",
				this.germplasmListExporter.hasParentsColumn(listDataTable));
	}

	@Test
	public void testAddedPreferredNameColumn() throws GermplasmListExporterException {
		final GermplasmListNewColumnsInfo currentColumnsInfo = new GermplasmListNewColumnsInfo(GermplasmListExporterTest.GERMPLASM_LIST_ID);

		final Map<String, List<ListDataColumnValues>> map = new HashMap<>();
		List list = new ArrayList();
		for (final Object itemId : GermplasmListExporterTest.listDataTable.getItemIds()){
			ListDataColumnValues ldcv = new ListDataColumnValues(PREFERRED_NAME, (Integer) itemId, LIST_NAME);
			list.add(ldcv);
		}
		map.put(PREFERRED_NAME, list);
		currentColumnsInfo.setColumnValuesMap(map);

		listDataTable.addContainerProperty(ColumnLabels.PREFERRED_NAME.getName(), String.class, null);

		final List<ExportRow> exportRows =
			this.germplasmListExporter.getExportColumnValuesFromTable(GermplasmListExporterTest.listDataTable, currentColumnsInfo);
		Assert.assertEquals(10, exportRows.size());

		// check if the values from the table match the created pojos
		for (int x = 0; x < exportRows.size(); x++) {
			final ExportRow row = exportRows.get(x);
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getEntryId().toString(), row.getValueForColumn(0));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getGid().toString(), row.getValueForColumn(1));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getEntryCode(), row.getValueForColumn(2));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getDesignation(), row.getValueForColumn(3));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getGroupName(), row.getValueForColumn(4));
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getSeedSource(), row.getValueForColumn(5));
			Assert.assertEquals(LIST_NAME, row.getValueForColumn(6)); //PREFERRED_NAME
		}
	}
	
	@Test
	public void testAddAttributeAndNameTypeHeadersForAttributes() {
		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();
		GermplasmListNewColumnsInfo currentColumnsInfo = GermplasmListNewColumnsInfoTestDataInitializer.createGermplasmListNewColumnsInfo();
		this.germplasmListExporter.addAttributeAndNameTypeHeaders(currentColumnsInfo, exportColumnHeaders);
		int counter = 0;
		for (final Map.Entry<String, List<ListDataColumnValues>> columnEntry : currentColumnsInfo.getColumnValuesMap().entrySet()) {
			Assert.assertEquals(columnEntry.getKey(), exportColumnHeaders.get(counter++).getName());
		}
	}
	@Test
	public void testAddAttributeAndNameTypeHeadersForNameTypes() {
		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();
		GermplasmListNewColumnsInfo currentColumnsInfo = GermplasmListNewColumnsInfoTestDataInitializer.createGermplasmListNewColumnsInfo(GermplasmListExporterTest.CODE_1, GermplasmListExporterTest.CODE_1);
		Mockito.when(this.germplasmListManager.getGermplasmNameTypes()).thenReturn(Arrays.asList(UserDefinedFieldTestDataInitializer.createUserDefinedField(GermplasmListExporterTest.CODE1, GermplasmListExporterTest.CODE_1)));
		this.germplasmListExporter.addAttributeAndNameTypeHeaders(currentColumnsInfo, exportColumnHeaders);
		Assert.assertEquals(GermplasmListExporterTest.CODE1, exportColumnHeaders.get(0).getName());
	}
	
	@Test
	public void testAddAttributesValues() {
		GermplasmListNewColumnsInfo currentColumnsInfo = GermplasmListNewColumnsInfoTestDataInitializer.createGermplasmListNewColumnsInfo();
		final ExportRow row = new ExportRow();
		this.germplasmListExporter.addAttributeAndNameTypeValues(currentColumnsInfo, (Object)1, row);
		Integer counter = 6;
		for (final Map.Entry<String, List<ListDataColumnValues>> columnEntry : currentColumnsInfo.getColumnValuesMap().entrySet()) {
			Assert.assertEquals(columnEntry.getValue().get(0).getValue(), row.getValueForColumn(counter));
		}
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
		listDataTable.addContainerProperty(ColumnLabels.STOCKID.getName(), String.class, null);
		listDataTable.setColumnCollapsingAllowed(true);

		GermplasmListExporterTest.loadEntriesToListDataTable(listDataTable);

		return listDataTable;
	}

	private static Table generateTestTable(ArrayList<String> addedColumns) {
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
		listDataTable.addContainerProperty(ColumnLabels.STOCKID.getName(), String.class, null);
		listDataTable.setColumnCollapsingAllowed(true);

		for(String column : addedColumns) {
			listDataTable.addContainerProperty(column, String.class, null);
		}

		GermplasmListExporterTest.loadEntriesToListDataTable(listDataTable);

		return listDataTable;
	}

	private static List<GermplasmListData> generateListEntries() {
		final List<GermplasmListData> entries = new ArrayList<>();

		for (int x = 1; x <= GermplasmListExporterTest.NO_OF_LIST_ENTRIES; x++) {
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

		for (final GermplasmListData entry : GermplasmListExporterTest.listEntries) {
			GermplasmListExporterTest.addListEntryToTable(entry, listDataTable);
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
		newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(entry.getStockIDs());

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

	private void configureTermNamesFromDefault() {
		try {
			final String programUUID = "12345uuid";
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.ENTRY_ID.getTermId().getId(),
					programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.GID.getTermId().getId(), programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.PARENTAGE.getTermId().getId(),
					programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.ENTRY_CODE.getTermId().getId(),
					programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.SEED_SOURCE.getTermId().getId(),
					programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.DESIGNATION.getTermId().getId(),
					programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId(),
					programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(ColumnLabels.SEED_RESERVATION.getTermId().getId(),
					programUUID);
		} catch (final Exception e) {

		}

	}

	private GermplasmList getGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(GermplasmListExporterTest.USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(GermplasmListExporterTest.generateListEntries());

		return germplasmList;
	}

	private HSSFWorkbook createWorkbook() {
		final HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Codes");
		return wb;
	}

	@AfterClass
	public static void cleanUp() {

		try {
			final File file = new File(GermplasmListExporterTest.FILE_NAME);
			file.delete();
		} catch (final Exception e) {
			// do nothing
		}

	}

	@Test
	public void testGetVisibleColumnMapWithMGID() {
		Map<String, Boolean> visibleColumnsMap;
		int visibleColumnCount;
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable(new ArrayList<>(Arrays.asList(ColumnLabels.MGID.getName())));

		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertEquals(10, visibleColumnCount);
		Assert.assertTrue("Expected to have exactly 10 visible columns.", visibleColumnCount == 10);

		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();
	}

	@Test
	public void testGetVisibleColumnMapWithFGID() {
		Map<String, Boolean> visibleColumnsMap;
		int visibleColumnCount;
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable(new ArrayList<>(Arrays.asList(ColumnLabels.FGID.getName())));

		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertEquals(10, visibleColumnCount);
		Assert.assertTrue("Expected to have exactly 10 visible columns.", visibleColumnCount == 10);

		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();
	}

	@Test
	public void testGetVisibleColumnMapWithAddedColumn() {
		Map<String, Boolean> visibleColumnsMap;
		int visibleColumnCount;
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable(new ArrayList<>(Arrays.asList(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())));

		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertEquals(11, visibleColumnCount);
		Assert.assertTrue("Expected to have exactly 10 visible columns.", visibleColumnCount == 11);

		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();
	}

}

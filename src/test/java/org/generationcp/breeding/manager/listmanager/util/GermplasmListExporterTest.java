
package org.generationcp.breeding.manager.listmanager.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.service.impl.GermplasmExportServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GermplasmListExporterTest {

	private static final String SEED_SOURCE = "SEED_SOURCE";
	private static final String CROSS = "CROSS";
	private static final String DESIG = "DESIG";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	private static final String GID = "GID";
	private static final String ENTRY_ID = "ENTRY_ID";
	private static final String OWNER_NAME = "User User";
	private static final String FILE_NAME = "testGermplasmListExporter.csv";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private UserDataManager userDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@InjectMocks
	private final GermplasmListExporter _germplasmListExporter = new GermplasmListExporter(GermplasmListExporterTest.LIST_ID);

	@InjectMocks
	private final GermplasmExportService germplasmExportService = Mockito.spy(new GermplasmExportServiceImpl());

	private GermplasmListExporter germplasmListExporter;

	private static Integer LIST_ID = 1;
	private static final int USER_ID = 1;
	private static final int PERSON_ID = 1;
	private static final long NO_OF_LIST_ENTRIES = 10;
	private static Table listDataTable;
	private static List<GermplasmListData> listEntries;

	private static final int PLATE_SIZE = 98;
	
	// provide a fake UUID, assuming this is a Unit test and we do not need to verify DB data
	private String programUUID = "12345uuid";

	@BeforeClass
	public static void setUpClass() {

		GermplasmListExporterTest.listEntries = GermplasmListExporterTest.generateListEntries();
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();
	}

	@Before
	public void setUp() throws MiddlewareQueryException, IllegalAccessException {

		MockitoAnnotations.initMocks(this);

		this._germplasmListExporter.setGermplasmExportService(this.germplasmExportService);
		this._germplasmListExporter.setMessageSource(this.messageSource);
		this._germplasmListExporter.setGermplasmListManager(this.germplasmListManager);
		this._germplasmListExporter.setUserDataManager(this.userDataManager);
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
		Mockito.doReturn(this.getGermplasmList()).when(this.germplasmListManager).getGermplasmListById(GermplasmListExporterTest.LIST_ID);
		Mockito.doReturn(GermplasmListExporterTest.NO_OF_LIST_ENTRIES).when(this.germplasmListManager)
		.countGermplasmListDataByListId(GermplasmListExporterTest.LIST_ID);
		Mockito.doReturn(GermplasmListExporterTest.generateListEntries()).when(this.germplasmListManager)
		.getGermplasmListDataByListId(GermplasmListExporterTest.LIST_ID, 0, (int) GermplasmListExporterTest.NO_OF_LIST_ENTRIES);

	}

	@Test
	public void testGetColumnHeadersForGenotypingData() {
		List<ExportColumnHeader> exportColumnList =
				this.germplasmListExporter.getColumnHeadersForGenotypingData(GermplasmListExporterTest.PLATE_SIZE);

		Assert.assertTrue("Expected that the total number of columns will be 8", exportColumnList.size() == 8);
		Assert.assertEquals("Expected to have a Subject ID as the 1st column", exportColumnList.get(0).getName(), "Subject ID");
		Assert.assertEquals("Expected to have a Plate ID as the 2nd column", exportColumnList.get(1).getName(), "Plate ID");
		Assert.assertEquals("Expected to have a Well ID as the 3rd column", exportColumnList.get(2).getName(), "Well");
		Assert.assertEquals("Expected to have a Sample type as the 4th column", exportColumnList.get(3).getName(), "Sample type");
		Assert.assertEquals("Expected to have a " + GermplasmListExporterTest.PLATE_SIZE + " as the 5th column", exportColumnList.get(4)
				.getName(), String.valueOf(GermplasmListExporterTest.PLATE_SIZE));
		Assert.assertEquals("Expected to have a Primer as the 6th column", exportColumnList.get(5).getName(), "Primer");
		Assert.assertEquals("Expected to have a Subject BC as the 7th column", exportColumnList.get(6).getName(), "Subject BC");
		Assert.assertEquals("Expected to have a Plate BC as the 8th column", exportColumnList.get(7).getName(), "Plate BC");
	}

	@Test
	public void testGetColumnValuesForGenotypingData() throws GermplasmListExporterException {
		List<Map<Integer, ExportColumnValue>> exportColumnValues =
				this.germplasmListExporter.getColumnValuesForGenotypingData(GermplasmListExporterTest.PLATE_SIZE);
		Assert.assertTrue("Expected to have a total of " + GermplasmListExporterTest.NO_OF_LIST_ENTRIES + " entries",
				exportColumnValues.size() == GermplasmListExporterTest.NO_OF_LIST_ENTRIES);
	}

	@Test
	public void testExportKBioScienceGenotypingOrderXLS() {
		try {

			this.germplasmListExporter.exportKBioScienceGenotypingOrderXLS(GermplasmListExporterTest.FILE_NAME,
					GermplasmListExporterTest.PLATE_SIZE);

		} catch (GermplasmListExporterException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetGermplasmListAndListData() {

		GermplasmList germplasmList = null;
		try {
			germplasmList = this.germplasmListExporter.getGermplasmListAndListData(GermplasmListExporterTest.LIST_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail("Expected to return a germplasmList object but throws an exception instead.");
		}

		Assert.assertNotNull("Expected the returned germplasmList object is not null.", germplasmList);
		Assert.assertNotNull("Expected the listData of the returned germplasmList object is not null.", germplasmList.getListData());
	}

	@Test
	public void testGetOwnerName() throws MiddlewareQueryException {
		User user = this.getUser();
		Person person = this.getPerson();
		String ownerName = "";
		Mockito.doReturn(user).when(this.userDataManager).getUserById(GermplasmListExporterTest.USER_ID);
		Mockito.doReturn(person).when(this.userDataManager).getPersonById(GermplasmListExporterTest.PERSON_ID);
		try {
			ownerName = this.germplasmListExporter.getOwnerName(GermplasmListExporterTest.USER_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail("Expected to return ownerName but didn't.");
		}
		// when person is not null
		Assert.assertEquals("Expected to return the person's name when the person is not null.", ownerName, person.getFirstName() + " "
				+ person.getLastName());

		Mockito.doReturn(null).when(this.userDataManager).getPersonById(GermplasmListExporterTest.PERSON_ID);
		try {
			ownerName = this.germplasmListExporter.getOwnerName(GermplasmListExporterTest.USER_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail("Expected to return ownerName but didn't.");
		}
		// when person does not exist
		Assert.assertEquals("Expected to return the user's name when the person is null.", ownerName, user.getName());

		Mockito.doReturn(null).when(this.userDataManager).getUserById(Matchers.any(Integer.class));
		try {
			ownerName = this.germplasmListExporter.getExporterName(Matchers.any(Integer.class));
		} catch (NullPointerException e) {
			Assert.assertEquals("Expected to return a NullPointerException when the userID does not exist.", NullPointerException.class, e);
		} catch (GermplasmListExporterException e) {
			Assert.fail("must not throw this exception");
		}
	}

	private Person getPerson() {
		Person person = new Person();
		person.setId(GermplasmListExporterTest.PERSON_ID);
		person.setFirstName("FirstName");
		person.setLastName("LastName");
		return person;
	}

	private User getUser() {
		User user = new User();
		user.setUserid(GermplasmListExporterTest.USER_ID);
		user.setName(GermplasmListExporterTest.OWNER_NAME);
		user.setPersonid(GermplasmListExporterTest.PERSON_ID);

		return user;
	}

	@Test
	public void testGetVisibleColumnMap() {
		Map<String, Boolean> visibleColumnsMap = null;
		int visibleColumnCount = 0;
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();

		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected to have exactly 9 visible columns.", visibleColumnCount == 9);

		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);
		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.PARENTAGE.getName(), true);
		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected to have exactly 7 visible columns.", visibleColumnCount == 7);

		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.DESIGNATION.getName(), true);
		GermplasmListExporterTest.listDataTable.setColumnCollapsed(ColumnLabels.GID.getName(), true);
		visibleColumnsMap = this.germplasmListExporter.getVisibleColumnMap(GermplasmListExporterTest.listDataTable);
		visibleColumnCount = this.getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected still to have exactly 7 visible columns when collapsing the required columns.", visibleColumnCount == 7);

		// reset
		GermplasmListExporterTest.listDataTable = GermplasmListExporterTest.generateTestTable();
	}

	private int getNoOfVisibleColumns(Map<String, Boolean> visibleColumnsMap) {
		int visibleColumnCount = 0;
		for (Map.Entry<String, Boolean> column : visibleColumnsMap.entrySet()) {
			Boolean isVisible = column.getValue();
			if (isVisible) {
				visibleColumnCount++;
			}
		}
		return visibleColumnCount;
	}

	@Test
	public void testGetExporterName() throws MiddlewareQueryException {
		User user = this.getUser();
		Person person = this.getPerson();
		String exporterName = "";
		Mockito.doReturn(user).when(this.userDataManager).getUserById(GermplasmListExporterTest.USER_ID);
		Mockito.doReturn(person).when(this.userDataManager).getPersonById(GermplasmListExporterTest.PERSON_ID);
		try {
			exporterName = this.germplasmListExporter.getExporterName(GermplasmListExporterTest.USER_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail("Expected to return exporterName but didn't.");
		}
		// when person is not null
		Assert.assertEquals("Expected that the returned exporter name equal to the person's firstname and lastname.", exporterName,
				person.getFirstName() + " " + person.getLastName());

		Mockito.doReturn(null).when(this.userDataManager).getUserById(Matchers.any(Integer.class));
		try {
			exporterName = this.germplasmListExporter.getExporterName(Matchers.any(Integer.class));
		} catch (NullPointerException e) {
			Assert.assertEquals("Expected to return a NullPointerException when the userID does not exist.", NullPointerException.class, e);
		} catch (GermplasmListExporterException e) {
			Assert.fail("must not throw this exception");
		}
	}

	@Test
	@Ignore(value = "Temporarily skipping. To be fixed by Team Manila soon.")
	public void testExportGermplasmListXLS() throws MiddlewareQueryException, GermplasmListExporterException {
		this.configureTermNamesFromDefault();
		User user = this.getUser();
		Person person = this.getPerson();
		Mockito.doReturn(user).when(this.userDataManager).getUserById(Mockito.anyInt());
		Mockito.doReturn(person).when(this.userDataManager).getPersonById(GermplasmListExporterTest.PERSON_ID);

		this.germplasmListExporter.exportGermplasmListXLS(GermplasmListExporterTest.FILE_NAME, GermplasmListExporterTest.listDataTable);
		// make sure that generateGermplasmListExcelFile is called and without errors
		Mockito.verify(this.germplasmExportService, Mockito.times(1)).generateGermplasmListExcelFile(
				Matchers.any(GermplasmListExportInputValues.class));
	}

	@Test(expected = GermplasmListExporterException.class)
	public void testExportGermplasmListXLSWithException() throws GermplasmListExporterException, MiddlewareQueryException {

		User user = this.getUser();
		Person person = this.getPerson();

		Mockito.doReturn(Mockito.mock(Variable.class)).when(this.ontologyVariableDataManager)
				.getVariable(Mockito.anyString(), Mockito.anyInt(), Mockito.eq(false));
		Mockito.doReturn(user).when(this.userDataManager).getUserById(Mockito.anyInt());
		Mockito.doReturn(person).when(this.userDataManager).getPersonById(Mockito.anyInt());

		Mockito.doThrow(new GermplasmListExporterException()).when(this.germplasmExportService)
		.generateGermplasmListExcelFile(Matchers.any(GermplasmListExportInputValues.class));
		this.germplasmListExporter.exportGermplasmListXLS(GermplasmListExporterTest.FILE_NAME, new Table());

	}

	@Test
	public void testExportGermplasmListCSV() {

		try {

			this.germplasmListExporter.exportGermplasmListCSV(GermplasmListExporterTest.FILE_NAME, GermplasmListExporterTest.listDataTable);
			// make sure that generateCSVFile is called and without errors
			Mockito.verify(this.germplasmExportService, Mockito.times(1)).generateCSVFile(Matchers.any(List.class), Matchers.any(List.class),
					Matchers.anyString());

		} catch (GermplasmListExporterException | IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(expected = GermplasmListExporterException.class)
	public void testExportGermplasmListCSVWithException() throws GermplasmListExporterException, IOException {

		Mockito.doThrow(new IOException()).when(this.germplasmExportService)
		.generateCSVFile(Matchers.any(List.class), Matchers.any(List.class), Matchers.anyString());
		this.germplasmListExporter.exportGermplasmListCSV(GermplasmListExporterTest.FILE_NAME, GermplasmListExporterTest.listDataTable);

	}

	@Test
	public void testGetExportColumnHeadersFromTable_AllColumnsAreVisible() throws MiddlewareQueryException {

		this.configureTermNamesFromDefault();

		List<ExportColumnHeader> exportColumnHeaders =
				this.germplasmListExporter.getExportColumnHeadersFromTable(GermplasmListExporterTest.listDataTable);

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

		Table collapsedColumnTable = GermplasmListExporterTest.generateTestTable();
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.ENTRY_ID.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.DESIGNATION.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.PARENTAGE.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.GID.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.ENTRY_CODE.getName(), true);
		collapsedColumnTable.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);

		List<ExportColumnHeader> exportColumnHeaders = this.germplasmListExporter.getExportColumnHeadersFromTable(collapsedColumnTable);

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
	public void testGetExportColumnValuesFromTable() {

		List<Map<Integer, ExportColumnValue>> exportColumnValues =
				this.germplasmListExporter.getExportColumnValuesFromTable(GermplasmListExporterTest.listDataTable);
		Assert.assertEquals(10, exportColumnValues.size());

		// check if the values from the table match the created pojos
		for (int x = 0; x < exportColumnValues.size(); x++) {
			Map<Integer, ExportColumnValue> row = exportColumnValues.get(x);
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getEntryId().toString(), row.get(0).getValue());
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getGid().toString(), row.get(1).getValue());
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getEntryCode().toString(), row.get(2).getValue());
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getDesignation().toString(), row.get(3).getValue());
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getGroupName().toString(), row.get(4).getValue());
			Assert.assertEquals(GermplasmListExporterTest.listEntries.get(x).getSeedSource().toString(), row.get(5).getValue());
		}

	}

	@Test
	public void testHasParentsColumn_returnsFalseForTableWithoutFemaleParentColumnHeader() {
		Table listDataTable = GermplasmListExporterTest.generateTestTable();
		Assert.assertFalse("Expected to return false for table without Parents Column but didn't.",
				this.germplasmListExporter.hasParentsColumn(listDataTable));
	}

	@Test
	public void testHasParentsColumn_returnsTrueForTableWithFemaleParentColumnHeader() {
		Table listDataTable = GermplasmListExporterTest.generateTestTable();
		listDataTable.addContainerProperty(ColumnLabels.FEMALE_PARENT.getName(), Button.class, null);
		Assert.assertFalse("Expected to return true for table with Parents Column but didn't.",
				this.germplasmListExporter.hasParentsColumn(listDataTable));
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

		GermplasmListExporterTest.loadEntriesToListDataTable(listDataTable);

		return listDataTable;
	}

	private static List<GermplasmListData> generateListEntries() {
		List<GermplasmListData> entries = new ArrayList<>();

		for (int x = 1; x <= GermplasmListExporterTest.NO_OF_LIST_ENTRIES; x++) {
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

		for (GermplasmListData entry : GermplasmListExporterTest.listEntries) {
			GermplasmListExporterTest.addListEntryToTable(entry, listDataTable);
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

	private void configureTermNamesFromDefault() {
		try {
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.ENTRY_ID.getTermId().getId(),programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.GID.getTermId().getId(),programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.PARENTAGE.getTermId().getId(),programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.ENTRY_CODE.getTermId().getId(),programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.SEED_SOURCE.getTermId().getId(),programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.DESIGNATION.getTermId().getId(),programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId(),programUUID);
			Mockito.doReturn(null).when(this.ontologyDataManager).getStandardVariable(
					ColumnLabels.SEED_RESERVATION.getTermId().getId(),programUUID);
		} catch (Exception e) {

		}

	}

	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(GermplasmListExporterTest.USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(GermplasmListExporterTest.generateListEntries());

		return germplasmList;
	}

	@AfterClass
	public static void cleanUp() {

		try {
			File file = new File(GermplasmListExporterTest.FILE_NAME);
			file.delete();
		} catch (Exception e) {
			// do nothing
		}

	}

}

package org.generationcp.breeding.manager.listmanager.util;

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
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.service.impl.ExportServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
	private ContextUtil contextUtil;

	@InjectMocks
	private GermplasmListExporter _germplasmListExporter = new GermplasmListExporter(LIST_ID);

	@InjectMocks
	private ExportService exportService = spy(new ExportServiceImpl());

	private GermplasmListExporter germplasmListExporter;

	private static Integer LIST_ID = 1;
	private static final int USER_ID = 1;
	private static final int PERSON_ID = 1;
	private static final long NO_OF_LIST_ENTRIES = 10;
	private static Table listDataTable;
	private static List<GermplasmListData> listEntries;

	private static final int PLATE_SIZE = 98;

	@BeforeClass
	public static void setUpClass() {

		listEntries = generateListEntries();
		listDataTable = generateTestTable();
	}

	@Before
	public void setUp() throws MiddlewareQueryException, IllegalAccessException {

		MockitoAnnotations.initMocks(this);

		_germplasmListExporter.setExportService(exportService);
		_germplasmListExporter.setMessageSource(messageSource);
		_germplasmListExporter.setGermplasmListManager(germplasmListManager);
		_germplasmListExporter.setUserDataManager(userDataManager);
		germplasmListExporter = spy(_germplasmListExporter);

		doReturn("#").when(messageSource).getMessage(Message.HASHTAG);
		
		doReturn(GID).when(germplasmListExporter).getTermNameFromOntology(ColumnLabels.GID);
		doReturn(ENTRY_CODE).when(germplasmListExporter).getTermNameFromOntology(ColumnLabels.ENTRY_CODE);
		doReturn(DESIG).when(germplasmListExporter).getTermNameFromOntology(ColumnLabels.DESIGNATION);
		doReturn(CROSS).when(germplasmListExporter).getTermNameFromOntology(ColumnLabels.PARENTAGE);
		doReturn(SEED_SOURCE).when(germplasmListExporter).getTermNameFromOntology(ColumnLabels.SEED_SOURCE);
		
		// set up test data for germplasm list
		doReturn(getGermplasmList()).when(germplasmListManager).getGermplasmListById(LIST_ID);
		doReturn(NO_OF_LIST_ENTRIES).when(germplasmListManager)
				.countGermplasmListDataByListId(LIST_ID);
		doReturn(generateListEntries()).when(germplasmListManager)
				.getGermplasmListDataByListId(LIST_ID, 0, (int) NO_OF_LIST_ENTRIES);

	}
	
	@Test
	public void testGetColumnHeadersForGenotypingData() {
		List<ExportColumnHeader> exportColumnList = germplasmListExporter
				.getColumnHeadersForGenotypingData(PLATE_SIZE);

		Assert.assertTrue("Expected that the total number of columns will be 8",
				exportColumnList.size() == 8);
		Assert.assertEquals("Expected to have a Subject ID as the 1st column",
				exportColumnList.get(0).getName(), "Subject ID");
		Assert.assertEquals("Expected to have a Plate ID as the 2nd column",
				exportColumnList.get(1).getName(), "Plate ID");
		Assert.assertEquals("Expected to have a Well ID as the 3rd column",
				exportColumnList.get(2).getName(), "Well");
		Assert.assertEquals("Expected to have a Sample type as the 4th column",
				exportColumnList.get(3).getName(), "Sample type");
		Assert.assertEquals("Expected to have a " + PLATE_SIZE + " as the 5th column",
				exportColumnList.get(4).getName(), String.valueOf(PLATE_SIZE));
		Assert.assertEquals("Expected to have a Primer as the 6th column",
				exportColumnList.get(5).getName(), "Primer");
		Assert.assertEquals("Expected to have a Subject BC as the 7th column",
				exportColumnList.get(6).getName(), "Subject BC");
		Assert.assertEquals("Expected to have a Plate BC as the 8th column",
				exportColumnList.get(7).getName(), "Plate BC");
	}

	@Test
	public void testGetColumnValuesForGenotypingData() throws GermplasmListExporterException {
		List<Map<Integer, ExportColumnValue>> exportColumnValues = germplasmListExporter
				.getColumnValuesForGenotypingData(PLATE_SIZE);
		Assert.assertTrue("Expected to have a total of " + NO_OF_LIST_ENTRIES + " entries",
				exportColumnValues.size() == NO_OF_LIST_ENTRIES);
	}

	@Test
	public void testExportKBioScienceGenotypingOrderXLS() {
		try {

			germplasmListExporter.exportKBioScienceGenotypingOrderXLS(FILE_NAME, PLATE_SIZE);

		} catch (GermplasmListExporterException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetGermplasmListAndListData() {

		GermplasmList germplasmList = null;
		try {
			germplasmList = germplasmListExporter.getGermplasmListAndListData(LIST_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail(
					"Expected to return a germplasmList object but throws an exception instead.");
		}

		Assert.assertNotNull("Expected the returned germplasmList object is not null.",
				germplasmList);
		Assert.assertNotNull(
				"Expected the listData of the returned germplasmList object is not null.",
				germplasmList.getListData());
	}

	@Test
	public void testGetOwnerName() throws MiddlewareQueryException {
		User user = getUser();
		Person person = getPerson();
		String ownerName = "";
		doReturn(user).when(userDataManager).getUserById(USER_ID);
		doReturn(person).when(userDataManager).getPersonById(PERSON_ID);
		try {
			ownerName = germplasmListExporter.getOwnerName(USER_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail("Expected to return ownerName but didn't.");
		}
		//when person is not null
		Assert.assertEquals("Expected to return the person's name when the person is not null.",
				ownerName, person.getFirstName() + " " + person.getLastName());

		doReturn(null).when(userDataManager).getPersonById(PERSON_ID);
		try {
			ownerName = germplasmListExporter.getOwnerName(USER_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail("Expected to return ownerName but didn't.");
		}
		//when person does not exist
		Assert.assertEquals("Expected to return the user's name when the person is null.",
				ownerName, user.getName());

		doReturn(null).when(userDataManager).getUserById(any(Integer.class));
		try {
			ownerName = germplasmListExporter.getExporterName(any(Integer.class));
		} catch (NullPointerException e) {
			Assert.assertEquals(
					"Expected to return a NullPointerException when the userID does not exist.",
					NullPointerException.class, e);
		} catch (GermplasmListExporterException e) {
			Assert.fail("must not throw this exception");
		}
	}

	private Person getPerson() {
		Person person = new Person();
		person.setId(PERSON_ID);
		person.setFirstName("FirstName");
		person.setLastName("LastName");
		return person;
	}

	private User getUser() {
		User user = new User();
		user.setUserid(USER_ID);
		user.setName(OWNER_NAME);
		user.setPersonid(PERSON_ID);

		return user;
	}

	@Test
	public void testGetVisibleColumnMap() {
		Map<String, Boolean> visibleColumnsMap = null;
		int visibleColumnCount = 0;
		listDataTable = generateTestTable();

		visibleColumnsMap = germplasmListExporter.getVisibleColumnMap(listDataTable);
		visibleColumnCount = getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected to have exactly 9 visible columns.", visibleColumnCount == 9);

		listDataTable.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);
		listDataTable.setColumnCollapsed(ColumnLabels.PARENTAGE.getName(), true);
		visibleColumnsMap = germplasmListExporter.getVisibleColumnMap(listDataTable);
		visibleColumnCount = getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue("Expected to have exactly 7 visible columns.", visibleColumnCount == 7);

		listDataTable.setColumnCollapsed(ColumnLabels.DESIGNATION.getName(), true);
		listDataTable.setColumnCollapsed(ColumnLabels.GID.getName(), true);
		visibleColumnsMap = germplasmListExporter.getVisibleColumnMap(listDataTable);
		visibleColumnCount = getNoOfVisibleColumns(visibleColumnsMap);
		Assert.assertTrue(
				"Expected still to have exactly 7 visible columns when collapsing the required columns.",
				visibleColumnCount == 7);

		//reset
		listDataTable = generateTestTable();
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
		User user = getUser();
		Person person = getPerson();
		String exporterName = "";
		doReturn(user).when(userDataManager).getUserById(USER_ID);
		doReturn(person).when(userDataManager).getPersonById(PERSON_ID);
		try {
			exporterName = germplasmListExporter.getExporterName(USER_ID);
		} catch (GermplasmListExporterException e) {
			Assert.fail("Expected to return exporterName but didn't.");
		}
		//when person is not null
		Assert.assertEquals(
				"Expected that the returned exporter name equal to the person's firstname and lastname.",
				exporterName, person.getFirstName() + " " + person.getLastName());

		doReturn(null).when(userDataManager).getUserById(any(Integer.class));
		try {
			exporterName = germplasmListExporter.getExporterName(any(Integer.class));
		} catch (NullPointerException e) {
			Assert.assertEquals(
					"Expected to return a NullPointerException when the userID does not exist.",
					NullPointerException.class, e);
		} catch (GermplasmListExporterException e) {
			Assert.fail("must not throw this exception");
		}
	}

	@Test
	public void testExportGermplasmListXLS()
			throws MiddlewareQueryException, GermplasmListExporterException {
		configureTermNamesFromDefault();
			
		germplasmListExporter.exportGermplasmListXLS(FILE_NAME, listDataTable);
		//make sure that generateGermplasmListExcelFile is called and without errors
		verify(exportService, times(1)).generateGermplasmListExcelFile(any(GermplasmListExportInputValues.class));
	}

	@Test(expected = GermplasmListExporterException.class)
	public void testExportGermplasmListXLSWithException()
			throws GermplasmListExporterException, MiddlewareQueryException {

		doThrow(new GermplasmListExporterException()).when(exportService)
				.generateGermplasmListExcelFile(any(GermplasmListExportInputValues.class));
		germplasmListExporter.exportGermplasmListXLS(FILE_NAME, new Table());

	}

	@Test
	public void testExportGermplasmListCSV() {

		try {

			germplasmListExporter.exportGermplasmListCSV(FILE_NAME, listDataTable);
			//make sure that generateCSVFile is called and without errors
			verify(exportService, times(1))
					.generateCSVFile(any(List.class), any(List.class), anyString());

		} catch (GermplasmListExporterException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test(expected = GermplasmListExporterException.class)
	public void testExportGermplasmListCSVWithException()
			throws GermplasmListExporterException, IOException {

		doThrow(new IOException()).when(exportService)
				.generateCSVFile(any(List.class), any(List.class), anyString());
		germplasmListExporter.exportGermplasmListCSV(FILE_NAME, listDataTable);

	}

	@Test
	public void testGetExportColumnHeadersFromTable_AllColumnsAreVisible() throws MiddlewareQueryException{
		
		configureTermNamesFromDefault();
	
		List<ExportColumnHeader> exportColumnHeaders = germplasmListExporter.getExportColumnHeadersFromTable(listDataTable);
		
		
		//no columns in the table are hidden/collapsed, therefore the isDisplay is always true for all headers
		assertEquals(6, exportColumnHeaders.size());
		assertTrue(exportColumnHeaders.get(0).isDisplay());
		assertTrue(exportColumnHeaders.get(1).isDisplay());
		assertTrue(exportColumnHeaders.get(2).isDisplay());
		assertTrue(exportColumnHeaders.get(3).isDisplay());
		assertTrue(exportColumnHeaders.get(4).isDisplay());
		assertTrue(exportColumnHeaders.get(5).isDisplay());

		//make sure the header in CSV is same as the header in table
		assertEquals(ENTRY_ID, exportColumnHeaders.get(0).getName());
		assertEquals(GID, exportColumnHeaders.get(1).getName());
		assertEquals(ENTRY_CODE, exportColumnHeaders.get(2).getName());
		assertEquals(DESIG, exportColumnHeaders.get(3).getName());
		assertEquals(CROSS, exportColumnHeaders.get(4).getName());
		assertEquals(SEED_SOURCE, exportColumnHeaders.get(5).getName());
			
	}

	@Test
	public void testGetExportColumnHeadersFromTable_NoColumnsAreVisible() throws MiddlewareQueryException{
		
		
		configureTermNamesFromDefault();
	
		Table collapsedColumnTable = generateTestTable();
		collapsedColumnTable.setColumnCollapsed((Object)ColumnLabels.ENTRY_ID.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ColumnLabels.DESIGNATION.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ColumnLabels.PARENTAGE.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ColumnLabels.GID.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ColumnLabels.ENTRY_CODE.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ColumnLabels.SEED_SOURCE.getName(), true);
		
		List<ExportColumnHeader> exportColumnHeaders = germplasmListExporter.getExportColumnHeadersFromTable(collapsedColumnTable);
		
		//EntryID, GID and DESIGNATION are all required columns, so their isDisplay is always true.
		assertEquals(6, exportColumnHeaders.size());
		assertTrue(exportColumnHeaders.get(0).isDisplay());
		assertTrue(exportColumnHeaders.get(1).isDisplay());
		assertFalse(exportColumnHeaders.get(2).isDisplay());
		assertTrue(exportColumnHeaders.get(3).isDisplay());
		assertFalse(exportColumnHeaders.get(4).isDisplay());
		assertFalse(exportColumnHeaders.get(5).isDisplay());

		//make sure the header in CSV is same as the header in table
		assertEquals(ENTRY_ID, exportColumnHeaders.get(0).getName());
		assertEquals(GID, exportColumnHeaders.get(1).getName());
		assertEquals(ENTRY_CODE, exportColumnHeaders.get(2).getName());
		assertEquals(DESIG, exportColumnHeaders.get(3).getName());
		assertEquals(CROSS, exportColumnHeaders.get(4).getName());
		assertEquals(SEED_SOURCE, exportColumnHeaders.get(5).getName());
	}

	@Test
	public void testGetExportColumnValuesFromTable() {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = germplasmListExporter
				.getExportColumnValuesFromTable(listDataTable);
		assertEquals(10, exportColumnValues.size());

		//check if the values from the table match the created pojos
		for (int x = 0; x < exportColumnValues.size(); x++) {
			Map<Integer, ExportColumnValue> row = exportColumnValues.get(x);
			assertEquals(listEntries.get(x).getEntryId().toString(), row.get(0).getValue());
			assertEquals(listEntries.get(x).getGid().toString(), row.get(1).getValue());
			assertEquals(listEntries.get(x).getEntryCode().toString(), row.get(2).getValue());
			assertEquals(listEntries.get(x).getDesignation().toString(), row.get(3).getValue());
			assertEquals(listEntries.get(x).getGroupName().toString(), row.get(4).getValue());
			assertEquals(listEntries.get(x).getSeedSource().toString(), row.get(5).getValue());
		}

	}
	
	@Test
	public void testHasParentsColumn_returnsFalseForTableWithoutFemaleParentColumnHeader(){
		Table listDataTable = generateTestTable();
		Assert.assertFalse("Expected to return false for table without Parents Column but didn't.",germplasmListExporter.hasParentsColumn(listDataTable));
	}
	
	@Test
	public void testHasParentsColumn_returnsTrueForTableWithFemaleParentColumnHeader(){
		Table listDataTable = generateTestTable();
		listDataTable.addContainerProperty(ColumnLabels.FEMALE_PARENT.getName(), Button.class, null);
		Assert.assertFalse("Expected to return true for table with Parents Column but didn't.",germplasmListExporter.hasParentsColumn(listDataTable));
	}
	
	private static Table generateTestTable(){
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

		loadEntriesToListDataTable(listDataTable);

		return listDataTable;
	}

	private static List<GermplasmListData> generateListEntries() {
		List<GermplasmListData> entries = new ArrayList<>();
    	
    	for (int x=1; x <= NO_OF_LIST_ENTRIES; x++){
    		GermplasmListData germplasmListData = new GermplasmListData();
    		germplasmListData.setId(x);
    		germplasmListData.setEntryId(x);
    		germplasmListData.setDesignation(ColumnLabels.DESIGNATION.getName() + x);
    		germplasmListData.setGroupName(ColumnLabels.PARENTAGE.getName() + x);
    		ListDataInventory inventoryInfo = new ListDataInventory(x,x);
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
	
	private static void loadEntriesToListDataTable(Table listDataTable){
	    	
			for(GermplasmListData entry : listEntries){
				addListEntryToTable(entry, listDataTable);
		   	}
			
			listDataTable.sort(new Object[]{ColumnLabels.ENTRY_ID.getName()}, new boolean[]{true});    
	}

	private static void addListEntryToTable(GermplasmListData entry, final Table listDataTable) {
		String gid = String.format("%s", entry.getGid().toString());
		Button gidButton = new Button(gid, new GidLinkButtonClickListener(null, gid, true, true));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription("Click to view Germplasm information");

		Button desigButton = new Button(entry.getDesignation(),
				new GidLinkButtonClickListener(null, gid, true, true));
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
		
		//#1 Available Inventory
		//default value
		String availInv = "-";
		if (entry.getInventoryInfo().getLotCount().intValue() != 0) {
			availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
		}
		Button inventoryButton = new Button(availInv,
				new InventoryLinkButtonClickListener(null, null, entry.getId(), entry.getGid()));
		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
		inventoryButton.setDescription(null);
		newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
		
		if("-".equals(availInv)){
			inventoryButton.setEnabled(false);
			inventoryButton.setDescription("No Lot for this Germplasm");
		} else {
			inventoryButton.setDescription(null);
		}

		//#2 Seed Reserved
		//default value
		String seedRes = "-";
		if (entry.getInventoryInfo().getReservedLotCount().intValue() != 0) {
			seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
		}
		newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
	}
	
	private void configureTermNamesFromOntology(){
		try{
			doReturn(generateStandardVariable(ColumnLabels.ENTRY_ID)).when(ontologyDataManager).getStandardVariable(ColumnLabels.ENTRY_ID.getTermId().getId());
			doReturn(generateStandardVariable(ColumnLabels.GID)).when(ontologyDataManager).getStandardVariable(ColumnLabels.GID.getTermId().getId());
			doReturn(generateStandardVariable(ColumnLabels.PARENTAGE)).when(ontologyDataManager).getStandardVariable(ColumnLabels.PARENTAGE.getTermId().getId());
			doReturn(generateStandardVariable(ColumnLabels.ENTRY_CODE)).when(ontologyDataManager).getStandardVariable(ColumnLabels.ENTRY_CODE.getTermId().getId());
			doReturn(generateStandardVariable(ColumnLabels.SEED_SOURCE)).when(ontologyDataManager).getStandardVariable(ColumnLabels.SEED_SOURCE.getTermId().getId());
			doReturn(generateStandardVariable(ColumnLabels.DESIGNATION)).when(ontologyDataManager).getStandardVariable(ColumnLabels.DESIGNATION.getTermId().getId());
			doReturn(generateStandardVariable(ColumnLabels.AVAILABLE_INVENTORY)).when(ontologyDataManager).getStandardVariable(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
			doReturn(generateStandardVariable(ColumnLabels.SEED_RESERVATION)).when(ontologyDataManager).getStandardVariable(ColumnLabels.SEED_RESERVATION.getTermId().getId());
		}catch(Exception e){
			
		}
		
	}
	
	private void configureTermNamesFromDefault(){
		try{
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.ENTRY_ID.getTermId().getId());
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.GID.getTermId().getId());
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.PARENTAGE.getTermId().getId());
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.ENTRY_CODE.getTermId().getId());
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.SEED_SOURCE.getTermId().getId());
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.DESIGNATION.getTermId().getId());
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
			doReturn(null).when(ontologyDataManager).getStandardVariable(ColumnLabels.SEED_RESERVATION.getTermId().getId());
		}catch(Exception e){
			
		}
		
	}
	
	private StandardVariable generateStandardVariable(ColumnLabels columnLabel){
		StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(columnLabel.getTermId().getId());
		standardVariable.setName(columnLabel.getName());
		standardVariable.setProperty(generateTerm(0,""));
		standardVariable.setScale(generateTerm(0,""));
		standardVariable.setMethod(generateTerm(0,""));
		standardVariable.setDataType(generateTerm(0,""));
		return standardVariable;
	}
	
	private Term generateTerm(int id, String name){
		Term term = new Term();
		term.setId(id);
		term.setName(name);
		return term;
	}
	
	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(generateListEntries());
		
		return germplasmList;
	}
	
	@AfterClass
	public static void cleanUp() {
		
		try {
			File file = new File(FILE_NAME);
			file.delete();
		}catch(Exception e){
			//do nothing
		}
		
				
	}

}

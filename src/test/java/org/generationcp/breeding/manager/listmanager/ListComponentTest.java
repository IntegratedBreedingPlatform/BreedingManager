
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.reflect.FieldUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class ListComponentTest {

	private static final String STOCKID = "STOCKID";
	private static final String SEED_RES = "SEED_RES";
	private static final String AVAIL_INV = "AVAIL_INV";
	private static final String HASH = "#";
	private static final String CHECK = "CHECK";
	private static final String SEED_SOURCE = "SEED_SOURCE";
	private static final String CROSS = "CROSS";
	private static final String DESIG = "DESIG";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	private static final String GID = "GID";

	private static final String UPDATED_GERMPLASM_LIST_NOTE = "UPDATED Germplasm List Note";
	private static final String UPDATED_GERMPLASM_LIST_NAME = "UPDATED Germplasm List Name";
	private static final String UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE = "UPDATED Germplasm List Description Value";
	private static final long UPDATED_GERMPLASM_LIST_DATE = 20141205;
	private static final String UPDATED_GERMPLASM_LIST_TYPE = "F1 LST";
	private static final String GERMPLASM_LIST_NOTE = "Germplasm List Note";
	private static final String GERMPLASM_LIST_NAME = "Germplasm List Name";
	private static final String GERMPLASM_LIST_DESCRIPTION_VALUE = "Germplasm List Description Value";
	private static final long GERMPLASM_LIST_DATE = 20141104;
	private static final String GERMPLASM_LIST_TYPE = "LST";

	private ListManagerMain source;

	@Mock
	private ListTabComponent parentListDetailsComponent;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private Window window;

	@Mock
	private AddColumnContextMenu addColumnContextMenu;

	@Mock
	private ContextUtil contextUtil;

	private ListComponent listComponent;

	private GermplasmList germplasmList;

	private final Integer EXPECTED_USER_ID = 1;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.setUpWorkbench();

		ListManagerMain listManagerMain = new ListManagerMain();
		FieldUtils.writeDeclaredField(listManagerMain, "germplasmListManager", this.germplasmListManager, true);
		FieldUtils.writeDeclaredField(listManagerMain, "contextUtil", this.contextUtil, true);

		this.source = Mockito.spy(listManagerMain);

		this.germplasmList = new GermplasmList();
		this.germplasmList.setId(1);
		this.germplasmList.setDescription(ListComponentTest.GERMPLASM_LIST_DESCRIPTION_VALUE);
		this.germplasmList.setName(ListComponentTest.GERMPLASM_LIST_NAME);
		this.germplasmList.setNotes(ListComponentTest.GERMPLASM_LIST_NOTE);
		this.germplasmList.setDate(ListComponentTest.GERMPLASM_LIST_DATE);
		this.germplasmList.setType(ListComponentTest.GERMPLASM_LIST_TYPE);
		this.germplasmList.setStatus(1);

		List<GermplasmListData> listEntries = new ArrayList<GermplasmListData>();
		listEntries.add(Mockito.mock(GermplasmListData.class));

		this.listComponent = Mockito.spy(new ListComponent(this.source, this.parentListDetailsComponent, this.germplasmList));

		Mockito.doReturn(ListComponentTest.CHECK).when(this.messageSource).getMessage(Message.CHECK_ICON);
		Mockito.doReturn(ListComponentTest.HASH).when(this.messageSource).getMessage(Message.HASHTAG);
		Mockito.doReturn(ListComponentTest.AVAIL_INV).when(this.listComponent).getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY);
		Mockito.doReturn(ListComponentTest.SEED_RES).when(this.listComponent).getTermNameFromOntology(ColumnLabels.SEED_RESERVATION);
		Mockito.doReturn(ListComponentTest.GID).when(this.listComponent).getTermNameFromOntology(ColumnLabels.GID);
		Mockito.doReturn(ListComponentTest.ENTRY_CODE).when(this.listComponent).getTermNameFromOntology(ColumnLabels.ENTRY_CODE);
		Mockito.doReturn(ListComponentTest.DESIG).when(this.listComponent).getTermNameFromOntology(ColumnLabels.DESIGNATION);
		Mockito.doReturn(ListComponentTest.CROSS).when(this.listComponent).getTermNameFromOntology(ColumnLabels.PARENTAGE);
		Mockito.doReturn(ListComponentTest.SEED_SOURCE).when(this.listComponent).getTermNameFromOntology(ColumnLabels.SEED_SOURCE);
		Mockito.doReturn(ListComponentTest.STOCKID).when(this.listComponent).getTermNameFromOntology(ColumnLabels.STOCKID);

		this.listComponent.setGermplasmListManager(this.germplasmListManager);
		this.listComponent.setMessageSource(this.messageSource);
		this.listComponent.setListEntries(listEntries);

		Mockito.doReturn(Mockito.mock(Window.class)).when(this.source).getWindow();
		Mockito.doReturn(Mockito.mock(ListSelectionComponent.class)).when(this.source).getListSelectionComponent();
		Mockito.doNothing().when(this.listComponent).refreshTreeOnSave();
		Mockito.doNothing().when(this.contextUtil).logProgramActivity(Matchers.anyString(), Matchers.anyString());
	}

	private void setUpWorkbench() {
		this.workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);

		WorkbenchRuntimeData runtimeDate = new WorkbenchRuntimeData();
		runtimeDate.setUserId(new Integer(5));

		Project dummyProject = new Project();
		dummyProject.setProjectId(new Long(5));

		try {
			Mockito.when(this.workbenchDataManager.getWorkbenchRuntimeData()).thenReturn(runtimeDate);
			Mockito.when(this.workbenchDataManager.getLastOpenedProject(runtimeDate.getUserId())).thenReturn(dummyProject);
			Mockito.when(this.workbenchDataManager.getLocalIbdbUserId(runtimeDate.getUserId(), dummyProject.getProjectId())).thenReturn(
					this.EXPECTED_USER_ID);

		} catch (MiddlewareQueryException e) {
			Assert.fail("Failed to create an ibdbuser instance.");
		}
	}

	@Test
	public void testSaveList_OverwriteExistingGermplasmList() {

		GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(1);
		germplasmListToBeSaved.setDescription(ListComponentTest.UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(ListComponentTest.UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(ListComponentTest.UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(ListComponentTest.UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(ListComponentTest.UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);

		try {
			Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
					.getGermplasmListById(this.germplasmList.getId().intValue());

			this.listComponent.saveList(germplasmListToBeSaved);

			GermplasmList savedList = this.listComponent.getGermplasmList();

			Assert.assertEquals(savedList.getId(), germplasmListToBeSaved.getId());
			Assert.assertEquals(savedList.getDescription(), germplasmListToBeSaved.getDescription());
			Assert.assertEquals(savedList.getName(), germplasmListToBeSaved.getName());
			Assert.assertEquals(savedList.getNotes(), germplasmListToBeSaved.getNotes());
			Assert.assertEquals(savedList.getDate(), germplasmListToBeSaved.getDate());
			Assert.assertEquals(savedList.getType(), germplasmListToBeSaved.getType());
			Assert.assertEquals(savedList.getStatus(), germplasmListToBeSaved.getStatus());

			Assert.assertSame(savedList, this.germplasmList);

		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testSaveList_OverwriteExistingGermplasmListWithDifferentID() {

		GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(1000);
		germplasmListToBeSaved.setDescription(ListComponentTest.UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(ListComponentTest.UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(ListComponentTest.UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(ListComponentTest.UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(ListComponentTest.UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);

		try {
			Mockito.doNothing().when(this.source).closeList(germplasmListToBeSaved);
			Mockito.doReturn(germplasmListToBeSaved).when(this.germplasmListManager).getGermplasmListById(Matchers.anyInt());

			// this will overwrite the list entries of the current germplasm list. Germplasm List Details will not be updated.
			this.listComponent.saveList(germplasmListToBeSaved);

			GermplasmList savedList = this.listComponent.getGermplasmList();

			Assert.assertFalse("", savedList.getId().equals(germplasmListToBeSaved.getId()));
			Assert.assertFalse(savedList.getDescription().equals(germplasmListToBeSaved.getDescription()));
			Assert.assertFalse(savedList.getName().equals(germplasmListToBeSaved.getName()));
			Assert.assertFalse(savedList.getNotes().equals(germplasmListToBeSaved.getNotes()));
			Assert.assertFalse(savedList.getDate().equals(germplasmListToBeSaved.getDate()));
			Assert.assertFalse(savedList.getType().equals(germplasmListToBeSaved.getType()));

			Assert.assertSame(savedList, this.germplasmList);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testSaveList_OverwriteNonExistingGermplasmList() {

		GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(1);
		germplasmListToBeSaved.setDescription(ListComponentTest.UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(ListComponentTest.UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(ListComponentTest.UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(ListComponentTest.UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(ListComponentTest.UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);

		try {
			Mockito.doReturn(null).when(this.germplasmListManager).getGermplasmListById(this.germplasmList.getId().intValue());

			this.listComponent.saveList(germplasmListToBeSaved);

			GermplasmList savedList = this.listComponent.getGermplasmList();

			Assert.assertTrue(savedList.getId().equals(germplasmListToBeSaved.getId()));
			Assert.assertFalse(savedList.getDescription().equals(germplasmListToBeSaved.getDescription()));
			Assert.assertFalse(savedList.getName().equals(germplasmListToBeSaved.getName()));
			Assert.assertFalse(savedList.getNotes().equals(germplasmListToBeSaved.getNotes()));
			Assert.assertFalse(savedList.getDate().equals(germplasmListToBeSaved.getDate()));
			Assert.assertFalse(savedList.getType().equals(germplasmListToBeSaved.getType()));

			Assert.assertSame(savedList, this.germplasmList);

		} catch (Exception e) {

			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testInitializeListDataTable() {

		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		Mockito.doReturn(tableWithSelectAll).when(this.listComponent).getListDataTableWithSelectAll();
		Mockito.doNothing().when(this.listComponent).initializeAddColumnContextMenu();

		this.listComponent.initializeListDataTable();

		Table table = tableWithSelectAll.getTable();

		Assert.assertEquals(ListComponentTest.CHECK, table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals(ListComponentTest.HASH, table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ListComponentTest.AVAIL_INV, table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ListComponentTest.SEED_RES, table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals(ListComponentTest.STOCKID, table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals(ListComponentTest.GID, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ListComponentTest.ENTRY_CODE, table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals(ListComponentTest.DESIG, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ListComponentTest.CROSS, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ListComponentTest.SEED_SOURCE, table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));

	}

	@Test
	public void testLockGermplasmList() {
		// set up
		try {
			Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
					.getGermplasmListById(this.germplasmList.getId().intValue());
		} catch (MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}

		Mockito.doNothing().when(this.listComponent).setLockedState(true);

		this.listComponent.lockGermplasmList(this.germplasmList);

		Assert.assertEquals(
				"Expecting the that the germplasmList status was changed to locked(101) but returned (" + this.germplasmList.getStatus()
						+ ")", Integer.valueOf(101), this.germplasmList.getStatus());
	}

	@Test
	public void testSaveChangesAction_verifyIfTheListTreeIsRefreshedAfterSavingList() {
		Table listDataTable = new Table();
		this.listComponent.setAddColumnContextMenu(this.addColumnContextMenu);
		Mockito.when(this.addColumnContextMenu.getListDataCollectionFromTable(listDataTable)).thenReturn(new ArrayList<ListDataInfo>());
		Mockito.doNothing().when(this.listComponent).setHasUnsavedChanges(true);
		Mockito.doNothing().when(this.listComponent).setHasUnsavedChanges(false);
		Mockito.doNothing().when(this.listComponent).updateNoOfEntries();
		this.listComponent.setListDataTable(listDataTable);

		this.listComponent.saveChangesAction(this.window, false);
		Mockito.verify(this.listComponent, Mockito.times(1)).refreshTreeOnSave();
	}

	@Test
	public void testIsInventoryColumn() {
		Assert.assertTrue("Expecting AVAILABLE_INVENTORY as an inventory column.",
				this.listComponent.isInventoryColumn(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertTrue("Expecting SEED_RESERVATION as an inventory column.",
				this.listComponent.isInventoryColumn(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertTrue("Expecting STOCKID as an inventory column.", this.listComponent.isInventoryColumn(ColumnLabels.STOCKID.getName()));
		Assert.assertFalse("Expecting ENTRY_ID as an inventory column.",
				this.listComponent.isInventoryColumn(ColumnLabels.ENTRY_ID.getName()));
	}

}

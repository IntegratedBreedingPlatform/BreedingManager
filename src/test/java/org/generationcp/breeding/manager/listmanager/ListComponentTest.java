package org.generationcp.breeding.manager.listmanager;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.ControllableRefreshTable;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.breeding.manager.listmanager.dialog.GermplasmGroupingComponent;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.ListDataPropertiesRenderer;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class ListComponentTest {

	private static final String STOCKID = "STOCKID";
	private static final String TOTAL_AVAILBALE = "AVAILABLE";
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
	private static final Integer TEST_GERMPLASM_LIST_ID = 111;
	private static final Integer TEST_GERMPLASM_NO_OF_ENTRIES = 5;
	private static final String INVENTORY_VIEW = "Inventory View";
	private static final String LIST_ENTRIES_VIEW = "List Entries View";

	@Mock
	private ListManagerMain source;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private Window window;

	@Mock
	private Component parentComponent;

	@Mock
	private AddColumnContextMenu addColumnContextMenu;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private ListSelectionComponent listSelectionComponent;

	@Mock
	private ListSelectionLayout listDetailsLayout;

	@Mock
	private BreedingManagerApplication breedingManagerApplication;

	@Mock
	private ListDataPropertiesRenderer newColumnsRenderer;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private UserService userService;

	@Mock
	private GermplasmGroupingService germplasmGroupingService;

	@InjectMocks
	private final ListComponent listComponent = new ListComponent();

	private GermplasmList germplasmList;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Before
	public void setUp() throws Exception {

		this.setUpWorkbenchDataManager();
		this.setUpOntologyManager();
		this.setUpListComponent();
	}

	@Test
	public void testSaveListOverwriteExistingGermplasmList() {

		final GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(ListComponentTest.TEST_GERMPLASM_LIST_ID);
		germplasmListToBeSaved.setDescription(ListComponentTest.UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(ListComponentTest.UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(ListComponentTest.UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(ListComponentTest.UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(ListComponentTest.UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(SaveListAsDialog.LIST_LOCKED_STATUS);

		try {
			Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
				.getGermplasmListById(this.germplasmList.getId());

			this.listComponent.saveList(germplasmListToBeSaved);

			final GermplasmList savedList = this.listComponent.getGermplasmList();

			Assert.assertEquals(savedList.getId(), germplasmListToBeSaved.getId());
			Assert.assertEquals(savedList.getDescription(), germplasmListToBeSaved.getDescription());
			Assert.assertEquals(savedList.getName(), germplasmListToBeSaved.getName());
			Assert.assertEquals(savedList.getNotes(), germplasmListToBeSaved.getNotes());
			Assert.assertEquals(savedList.getDate(), germplasmListToBeSaved.getDate());
			Assert.assertEquals(savedList.getType(), germplasmListToBeSaved.getType());
			Assert.assertEquals(savedList.getStatus(), germplasmListToBeSaved.getStatus());

			Assert.assertSame(savedList, this.germplasmList);

		} catch (final Exception e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testSaveListOverwriteExistingGermplasmListWithDifferentID() {

		final GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(1000);
		germplasmListToBeSaved.setDescription(ListComponentTest.UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(ListComponentTest.UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(ListComponentTest.UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(ListComponentTest.UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(ListComponentTest.UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);

		try {
			Mockito.doNothing().when(this.source).closeList(germplasmListToBeSaved);
			Mockito.doReturn(germplasmListToBeSaved).when(this.germplasmListManager)
				.getGermplasmListById(Matchers.anyInt());

			// this will overwrite the list entries of the current germplasm
			// list. Germplasm List Details will not be updated.
			this.listComponent.saveList(germplasmListToBeSaved);

			final GermplasmList savedList = this.listComponent.getGermplasmList();

			Assert.assertFalse(germplasmListToBeSaved.getId().equals(savedList.getId()));
			Assert.assertFalse(germplasmListToBeSaved.getDescription().equals(savedList.getDescription()));
			Assert.assertFalse(germplasmListToBeSaved.getName().equals(savedList.getName()));
			Assert.assertFalse(germplasmListToBeSaved.getNotes().equals(savedList.getNotes()));
			Assert.assertFalse(germplasmListToBeSaved.getDate().equals(savedList.getDate()));
			Assert.assertFalse(germplasmListToBeSaved.getType().equals(savedList.getType()));

			Assert.assertSame(savedList, this.germplasmList);

		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testSaveListOverwriteNonExistingGermplasmList() {

		final GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(ListComponentTest.TEST_GERMPLASM_LIST_ID);
		germplasmListToBeSaved.setDescription(ListComponentTest.UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(ListComponentTest.UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(ListComponentTest.UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(ListComponentTest.UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(ListComponentTest.UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);

		Mockito.doReturn(null).when(this.germplasmListManager)
			.getGermplasmListById(ListComponentTest.TEST_GERMPLASM_LIST_ID);

		this.listComponent.saveList(germplasmListToBeSaved);

		final GermplasmList savedList = this.listComponent.getGermplasmList();

		Assert.assertTrue(germplasmListToBeSaved.getId().equals(savedList.getId()));
		Assert.assertFalse(germplasmListToBeSaved.getDescription().equals(savedList.getDescription()));
		Assert.assertFalse(germplasmListToBeSaved.getName().equals(savedList.getName()));
		Assert.assertFalse(germplasmListToBeSaved.getNotes().equals(savedList.getNotes()));
		Assert.assertFalse(germplasmListToBeSaved.getDate().equals(savedList.getDate()));
		Assert.assertFalse(germplasmListToBeSaved.getType().equals(savedList.getType()));

		Assert.assertSame(savedList, this.germplasmList);

	}

	@Test
	public void testInitializeListDataTable() {

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.listComponent.instantiateComponents();
		this.listComponent.initializeListDataTable(tableWithSelectAll);

		final Table table = tableWithSelectAll.getTable();

		Assert.assertEquals(ListComponentTest.CHECK, table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals(ListComponentTest.HASH, table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(
			ListComponentTest.AVAIL_INV,
			table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ListComponentTest.TOTAL_AVAILBALE, table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(ListComponentTest.STOCKID, table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals(ListComponentTest.GID, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ListComponentTest.ENTRY_CODE, table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals(ListComponentTest.DESIG, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ListComponentTest.CROSS, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ListComponentTest.SEED_SOURCE, table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));

	}

	@Test
	public void testLockGermplasmList() {
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		this.listComponent.setContextUtil(contextUtil);
		Mockito.doNothing().when(contextUtil).logProgramActivity(Matchers.anyString(), Matchers.anyString());
		Mockito.doReturn("Test").when(this.messageSource).getMessage(Matchers.any(Message.class));
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
			.getGermplasmListById(this.germplasmList.getId());
		final FillWith fillWith = Mockito.mock(FillWith.class);
		this.listComponent.setFillWith(fillWith);
		this.listComponent.instantiateComponents();
		this.listComponent.getViewListHeaderWindow().instantiateComponents();

		this.listComponent.toggleGermplasmListStatus();

		Assert.assertEquals("Expecting the that the germplasmList status was changed to locked(101) but returned ("
			+ this.germplasmList.getStatus() + ")", Integer.valueOf(101), this.germplasmList.getStatus());
		Assert.assertEquals(
			Integer.valueOf(101),
			this.listComponent.getViewListHeaderWindow().getGermplasmList().getStatus());
		Assert.assertEquals(
			Integer.valueOf(101),
			this.listComponent.getViewListHeaderWindow().getListHeaderComponent().getGermplasmList().getStatus());
		Assert.assertEquals(
			"Locked List",
			this.listComponent.getViewListHeaderWindow().getListHeaderComponent().getStatusValueLabel().toString());
		Mockito.verify(fillWith).setContextMenuEnabled(this.listComponent.getListDataTable(), false);
	}

	@Test
	public void testUnlockGermplasmList() {

		Mockito.when(this.messageSource.getMessage(Matchers.any(Message.class))).thenReturn("");

		this.germplasmList.setStatus(101);
		this.listComponent.setListDataTable(new Table());
		this.listComponent.instantiateComponents();
		this.listComponent.getViewListHeaderWindow().instantiateComponents();
		final FillWith fillWith = Mockito.mock(FillWith.class);
		this.listComponent.setFillWith(fillWith);
		this.listComponent.toggleGermplasmListStatus();

		Assert.assertEquals("Expecting the that the germplasmList status was changed to unlocked(1) but returned ("
			+ this.germplasmList.getStatus() + ")", Integer.valueOf(1), this.germplasmList.getStatus());
		Assert.assertEquals(
			Integer.valueOf(1),
			this.listComponent.getViewListHeaderWindow().getGermplasmList().getStatus());
		Assert.assertEquals(
			Integer.valueOf(1),
			this.listComponent.getViewListHeaderWindow().getGermplasmList().getStatus());
		Assert.assertEquals(
			Integer.valueOf(1),
			this.listComponent.getViewListHeaderWindow().getListHeaderComponent().getGermplasmList().getStatus());
		Assert.assertEquals(
			"Unlocked List",
			this.listComponent.getViewListHeaderWindow().getListHeaderComponent().getStatusValueLabel().toString());
		Mockito.verify(fillWith).setContextMenuEnabled(this.listComponent.getListDataTable(), true);
	}

	@Test
	public void testSaveChangesAction() {
		Mockito.when(this.messageSource.getMessage(Matchers.any(Message.class))).thenReturn("");

		final Table listDataTable = Mockito.mock(Table.class);
		this.listComponent.setListDataTable(listDataTable);
		final String column = "PASSPORT ATTRIBUTE";

		final List<String> attributeAndNameTypes = Arrays.asList(column);
		this.listComponent.setAttributeAndNameTypeColumns(attributeAndNameTypes);
		this.listComponent.setAddColumnContextMenu(this.addColumnContextMenu);
		this.listComponent.instantiateComponents();

		final List<ListDataInfo> listDataInfo = Arrays.asList(new ListDataInfo(1, Arrays.asList(new ListDataColumn(column, "123"))));
		Mockito
			.when(this.addColumnContextMenu.getListDataCollectionFromTable(Matchers.eq(listDataTable), Matchers.eq(attributeAndNameTypes)))
			.thenReturn(listDataInfo);

		this.listComponent.saveChangesAction(this.window, false);
		Mockito.verify(this.germplasmListManager).updateGermplasmListData(Matchers.anyListOf(GermplasmListData.class));
		Mockito.verify(this.germplasmListManager).saveListDataColumns(listDataInfo);
		Mockito.verify(listDataTable).requestRepaint();
		Mockito.verify(this.breedingManagerApplication).refreshListManagerTree();
	}

	@Test
	public void testIsInventoryColumn() {
		Assert.assertTrue(
			"Expecting AVAILABLE_INVENTORY as an inventory column.",
			this.listComponent.isInventoryColumn(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertTrue(
			"Expecting SEED_RESERVATION as an inventory column.",
			this.listComponent.isInventoryColumn(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertTrue(
			"Expecting STOCKID as an inventory column.",
			this.listComponent.isInventoryColumn(ColumnLabels.STOCKID.getName()));
		Assert.assertFalse(
			"Expecting ENTRY_ID as an inventory column.",
			this.listComponent.isInventoryColumn(ColumnLabels.ENTRY_ID.getName()));
	}

	@Test
	public void testDeleteRemovedGermplasmEntriesFromTableAllEntries() {

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		this.listComponent.instantiateComponents();
		this.listComponent.initializeListDataTable(tableWithSelectAll);

		this.listComponent.deleteRemovedGermplasmEntriesFromTable();

		Mockito.verify(this.germplasmListManager)
			.deleteGermplasmListDataByListId(ListComponentTest.TEST_GERMPLASM_LIST_ID);

	}

	@Test
	public void testDeleteRemovedGermplasmEntriesFromTableOnlySelectedEntries() {

		this.initializeTableWithTestData();

		// Add one item to delete from list data table
		this.listComponent.getItemsToDelete().putAll(this.createItemsToDelete());

		this.listComponent.deleteRemovedGermplasmEntriesFromTable();

		// deleteGermplasmListDataByListIdLrecId should only be called once
		Mockito.verify(this.germplasmListManager, Mockito.times(1)).deleteGermplasmListDataByListIdLrecId(
			Matchers.eq(ListComponentTest.TEST_GERMPLASM_LIST_ID), Matchers.anyInt());

		Assert.assertTrue(this.listComponent.getItemsToDelete().isEmpty());

	}

	@Test
	public void testMarkLinesAsFixedActionWithSelectedEntries() {

		this.initializeTableWithTestData();

		// This selects all items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(table.getItemIds());

		this.listComponent.markLinesAsFixedAction();

		Mockito.verify(this.window).addWindow(Matchers.any(GermplasmGroupingComponent.class));

	}

	@Test
	public void testMarkLinesAsFixedActionWithoutSelectedEntries() {

		this.initializeTableWithTestData();

		// This removes the selected items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(null);

		this.listComponent.markLinesAsFixedAction();

		Mockito.verify(this.messageSource).getMessage(Message.ERROR_MARK_LINES_AS_FIXED_NOTHING_SELECTED);
		Mockito.verify(this.window).showNotification(Matchers.any(Window.Notification.class));

	}

	@Test
	public void testAssignCodesActionWithSelectedEntries() {

		this.initializeTableWithTestData();

		// This selects all items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(table.getItemIds());

		this.listComponent.assignCodesAction();

		Mockito.verify(this.window).addWindow(Matchers.any(AssignCodesDialog.class));

	}

	@Test
	public void testAssignCodesActionWithoutSelectedEntries() {

		this.initializeTableWithTestData();

		// This removes the selected items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(null);

		this.listComponent.assignCodesAction();

		Mockito.verify(this.messageSource).getMessage(Message.ERROR_ASSIGN_CODES_NOTHING_SELECTED);
		Mockito.verify(this.window).showNotification(Matchers.any(Window.Notification.class));

	}

	@Test
	public void testExtractGidListFromListDataTable() {

		this.initializeTableWithTestData();

		// This selects all items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(table.getItemIds());

		final Set<Integer> result = this.listComponent
			.extractGidListFromListDataTable(this.listComponent.getListDataTable());

		Assert.assertEquals(ListComponentTest.TEST_GERMPLASM_NO_OF_ENTRIES.intValue(), result.size());

		final Collection<Integer> selectedRows = (Collection<Integer>) table.getValue();
		final Iterator<Integer> selectedRowsIterator = selectedRows.iterator();
		for (final Integer gid : result) {
			final Item selectedRowItem = table.getItem(selectedRowsIterator.next());
			final Button gidCell = (Button) selectedRowItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
			Assert.assertEquals("The order of extracted GIDs should be same order as the entries in the table.",
				Integer.valueOf(gidCell.getCaption()), gid);
		}

	}

	@Test
	public void testExtractGidListFromListDataTableWithoutSelectedEntries() {

		this.initializeTableWithTestData();

		// This removes all items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(null);

		final Set<Integer> result = this.listComponent
			.extractGidListFromListDataTable(this.listComponent.getListDataTable());

		Assert.assertEquals(0, result.size());

	}

	private void initializeTableWithTestData() {

		Mockito.when(this.inventoryDataManager.getLotCountsForList(ListComponentTest.TEST_GERMPLASM_LIST_ID, 0,
			ListComponentTest.TEST_GERMPLASM_NO_OF_ENTRIES))
			.thenReturn(ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails());

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.listComponent.instantiateComponents();
		this.listComponent.initializeListDataTable(tableWithSelectAll);
		this.listComponent.setListDataTable(tableWithSelectAll.getTable());
		this.listComponent.initializeValues();

	}

	private void setUpOntologyManager() {

		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId()))
			.thenReturn(this.createTerm(TermId.AVAILABLE_INVENTORY.getId(), ListComponentTest.AVAIL_INV));

		Mockito.when(this.ontologyDataManager.getTermById(TermId.TOTAL_INVENTORY.getId()))
			.thenReturn(this.createTerm(TermId.TOTAL_INVENTORY.getId(), ListComponentTest.TOTAL_AVAILBALE));

		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId()))
			.thenReturn(this.createTerm(TermId.GID.getId(), ListComponentTest.GID));

		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId()))
			.thenReturn(this.createTerm(TermId.ENTRY_CODE.getId(), ListComponentTest.ENTRY_CODE));

		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId()))
			.thenReturn(this.createTerm(TermId.DESIG.getId(), ListComponentTest.DESIG));

		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId()))
			.thenReturn(this.createTerm(TermId.CROSS.getId(), ListComponentTest.CROSS));

		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId()))
			.thenReturn(this.createTerm(TermId.SEED_SOURCE.getId(), ListComponentTest.SEED_SOURCE));

		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId()))
			.thenReturn(this.createTerm(TermId.STOCKID.getId(), ListComponentTest.STOCKID));

	}

	private void setUpWorkbenchDataManager() {
		final Project dummyProject = new Project();
		dummyProject.setProjectId((long) 5);
	}

	private Term createTerm(final int id, final String name) {
		final Term term = new Term(id, name, "");
		return term;
	}

	private Map<Object, String> createItemsToDelete() {

		final Map<Object, String> itemsToDelete = new HashMap<>();

		// delete the first record from the germplasm list data table
		itemsToDelete.put(1, "Designation 1");

		return itemsToDelete;
	}

	private void setUpListComponent() {

		this.germplasmList = GermplasmListTestDataInitializer.createGermplasmListWithListData(
			ListComponentTest.TEST_GERMPLASM_LIST_ID, ListComponentTest.TEST_GERMPLASM_NO_OF_ENTRIES);
		this.germplasmList.setStatus(1);
		this.listComponent.setGermplasmList(this.germplasmList);
		this.listComponent.setParent(this.parentComponent);

		final TableWithSelectAllLayout tableWithSelectAllLayout = Mockito.mock(TableWithSelectAllLayout.class);
		final ControllableRefreshTable table = Mockito.mock(ControllableRefreshTable.class);
		this.listComponent.setListDataTableWithSelectAll(tableWithSelectAllLayout);
		this.listComponent.setToolsMenuContainer(Mockito.mock(HorizontalLayout.class));
		this.listComponent.setListDataTable(new Table());
		this.listComponent.setTotalListEntriesLabel(new Label());
		this.listComponent.setTotalSelectedListEntriesLabel(new Label());

		Mockito.when(this.germplasmListManager.countGermplasmListDataByListId(ListComponentTest.TEST_GERMPLASM_LIST_ID))
			.thenReturn(Long.valueOf(ListComponentTest.TEST_GERMPLASM_NO_OF_ENTRIES));
		Mockito.when(this.germplasmListManager.getGermplasmListById(ListComponentTest.TEST_GERMPLASM_LIST_ID))
			.thenReturn(this.germplasmList);

		Mockito.when(this.parentComponent.getWindow()).thenReturn(this.window);
		Mockito.when(this.source.getListSelectionComponent()).thenReturn(this.listSelectionComponent);
		Mockito.when(this.source.getWindow()).thenReturn(this.window);
		Mockito.when(this.listSelectionComponent.getListDetailsLayout()).thenReturn(this.listDetailsLayout);

		Mockito.when(this.messageSource.getMessage(Matchers.any(Message.class))).thenReturn("");
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn(ListComponentTest.CHECK);
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn(ListComponentTest.HASH);
		Mockito.doReturn(LIST_ENTRIES_VIEW).when(this.messageSource).getMessage(Message.LIST_ENTRIES_LABEL);


		Mockito.doNothing().when(this.contextUtil).logProgramActivity(Matchers.anyString(), Matchers.anyString());

	}

	@Test
	public void testRemoveSelectedGermplasmButtonClickAction() {
		this.initializeTableWithTestData();
		this.listComponent.removeSelectedGermplasmButtonClickAction();
		Mockito.verify(this.messageSource).getMessage(Message.ERROR_REMOVING_GERMPLASM);
		Mockito.verify(this.window).showNotification(Matchers.any(Window.Notification.class));

	}

	@Test
	public void testUnfixLinesAllGermplasmAreFixed() {

		final int gid1 = 1;
		final int gid2 = 2;
		final int gid3 = 3;
		final Set<Integer> gidsToProcess = new HashSet<>(Arrays.asList(gid1, gid2, gid3));
		final List<Germplasm> listOfGermplasm = new ArrayList<>();
		Mockito.when(this.germplasmDataManager.getGermplasmWithoutGroup(new ArrayList<Integer>(gidsToProcess)))
			.thenReturn(listOfGermplasm);

		this.listComponent.unfixLines(gidsToProcess);

		Mockito.verify(this.messageSource, Mockito.never()).getMessage(Message.WARNING_UNFIX_LINES);
		Mockito.verify(this.germplasmGroupingService).unfixLines(gidsToProcess);
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS_UNFIX_LINES, gidsToProcess.size());

	}

	@Test
	public void testUnfixLinesOnlyOneGermplasmIsFixed() {

		final int gid1 = 1;
		final int gid2 = 2;
		final int gid3 = 3;
		final Set<Integer> gidsToProcess = new HashSet<>(Arrays.asList(gid1, gid2, gid3));
		final List<Germplasm> listOfGermplasmWithoutGroup = new ArrayList<>();

		final Germplasm germplasm1 = this.createGermplasm(gid1, 0);
		final Germplasm germplasm3 = this.createGermplasm(gid3, 0);

		listOfGermplasmWithoutGroup.add(germplasm1);
		listOfGermplasmWithoutGroup.add(germplasm3);

		Mockito.when(this.germplasmDataManager.getGermplasmWithoutGroup(new ArrayList<Integer>(gidsToProcess)))
			.thenReturn(listOfGermplasmWithoutGroup);

		this.listComponent.unfixLines(gidsToProcess);

		Mockito.verify(this.messageSource).getMessage(Message.WARNING_UNFIX_LINES);
		Mockito.verify(this.germplasmGroupingService).unfixLines(gidsToProcess);
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS_UNFIX_LINES, 1);

	}

	@Test
	public void testUnfixLinesNoGermplasmAreFixed() {

		final int gid1 = 1;
		final int gid2 = 2;
		final int gid3 = 3;
		final Set<Integer> gidsToProcess = new HashSet<>(Arrays.asList(gid1, gid2, gid3));
		final List<Germplasm> listOfGermplasm = new ArrayList<>();

		final Germplasm germplasm1 = this.createGermplasm(gid1, 0);
		listOfGermplasm.add(germplasm1);
		final Germplasm germplasm2 = this.createGermplasm(gid2, 0);
		listOfGermplasm.add(germplasm2);
		final Germplasm germplasm3 = this.createGermplasm(gid3, 0);
		listOfGermplasm.add(germplasm3);

		Mockito.when(this.germplasmDataManager.getGermplasmWithoutGroup(new ArrayList<Integer>(gidsToProcess)))
			.thenReturn(listOfGermplasm);

		this.listComponent.unfixLines(gidsToProcess);

		Mockito.verify(this.messageSource).getMessage(Message.WARNING_UNFIX_LINES);
		Mockito.verify(this.germplasmGroupingService).unfixLines(gidsToProcess);
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS_UNFIX_LINES, 0);

	}

	@Test
	public void testCountGermplasmWithoutGroup() {

		final Set<Integer> gidsToProcess = new HashSet<>(Arrays.asList(1, 2, 3));
		final List<Germplasm> listOfGermplasm = new ArrayList<>();
		listOfGermplasm.add(this.createGermplasm(1, 0));
		listOfGermplasm.add(this.createGermplasm(2, 0));

		Mockito.when(this.germplasmDataManager.getGermplasmWithoutGroup(new ArrayList<Integer>(gidsToProcess)))
			.thenReturn(listOfGermplasm);

		Assert.assertEquals("There are only 2 ungrouped germplasm in the list", 2,
			this.listComponent.countGermplasmWithoutGroup(gidsToProcess));

	}

	@Test
	public void testConfirmUnfixLinesListener() {

		final Set<Integer> gidsToProcess = new HashSet<>(Arrays.asList(1, 2, 3));
		final ListComponent listComponent = Mockito.mock(ListComponent.class);

		final ListComponent.ConfirmUnfixLinesListener listener = this.listComponent.new ConfirmUnfixLinesListener(
			gidsToProcess, listComponent);

		final ConfirmDialog confirmDialog = ConfirmDialog.show(this.window, "", "", "", "", listener);
		confirmDialog.getOkButton().click();

		Mockito.verify(listComponent).unfixLines(gidsToProcess);
		Mockito.verify(listComponent).updateGermplasmListTable(gidsToProcess);

	}

	@Test
	public void testConfirmUnfixLinesAction() {

		this.initializeTableWithTestData();

		// This selects all items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(table.getItemIds());

		this.listComponent.confirmUnfixLinesAction();

		Mockito.verify(this.messageSource).getMessage(Message.CONFIRM_UNFIX_LINES);

	}

	@Test
	public void testConfirmUnfixLinesActionNoSelectedGermplasm() {

		this.initializeTableWithTestData();
		// This selects all items in the table
		final Table table = this.listComponent.getListDataTable();
		table.setValue(null);

		this.listComponent.confirmUnfixLinesAction();

		Mockito.verify(this.messageSource).getMessage(Message.ERROR_UNFIX_LINES_NOTHING_SELECTED);

	}

	@Test
	public void testUpdateGermplasmListStatusLocked() {
		this.initializeTableWithTestData();
		this.germplasmList = GermplasmListTestDataInitializer.createGermplasmList(1);
		this.germplasmList.setStatus(100);
		Mockito.when(this.germplasmListManager.getGermplasmListById(this.germplasmList.getId()))
			.thenReturn(this.germplasmList);
		this.listComponent.setGermplasmList(this.germplasmList);
		final ViewListHeaderWindow viewListHeaderWindow = Mockito.mock(ViewListHeaderWindow.class);
		this.listComponent.setViewListHeaderWindow(viewListHeaderWindow);
		final FillWith fillWith = Mockito.mock(FillWith.class);
		this.listComponent.setFillWith(fillWith);
		this.listComponent.updateGermplasmListStatus();
		Mockito.verify(this.germplasmListManager).getGermplasmListById(this.germplasmList.getId());
		Mockito.verify(viewListHeaderWindow).setGermplasmListStatus(this.germplasmList.getStatus());
		Assert.assertFalse(this.listComponent.getLockButton().isVisible());
		Assert.assertTrue(this.listComponent.getUnlockButton().isVisible());
		Assert.assertFalse(this.listComponent.getEditHeaderButton().isVisible());
		Mockito.verify(fillWith).setContextMenuEnabled(this.listComponent.getListDataTable(), false);
	}

	@Test
	public void testUpdateGermplasmListStatusNotLocked() {
		this.initializeTableWithTestData();
		this.germplasmList = GermplasmListTestDataInitializer.createGermplasmList(1);
		this.germplasmList.setStatus(1);
		Mockito.when(this.germplasmListManager.getGermplasmListById(this.germplasmList.getId()))
			.thenReturn(this.germplasmList);
		this.listComponent.setGermplasmList(this.germplasmList);
		final ViewListHeaderWindow viewListHeaderWindow = Mockito.mock(ViewListHeaderWindow.class);
		this.listComponent.setViewListHeaderWindow(viewListHeaderWindow);
		final FillWith fillWith = Mockito.mock(FillWith.class);
		this.listComponent.setFillWith(fillWith);
		this.listComponent.updateGermplasmListStatus();
		Mockito.verify(this.germplasmListManager).getGermplasmListById(this.germplasmList.getId());
		Mockito.verify(viewListHeaderWindow).setGermplasmListStatus(this.germplasmList.getStatus());
		Assert.assertTrue(this.listComponent.getLockButton().isVisible());
		Assert.assertFalse(this.listComponent.getUnlockButton().isVisible());
		Assert.assertTrue(this.listComponent.getEditHeaderButton().isVisible());
		Mockito.verify(fillWith).setContextMenuEnabled(this.listComponent.getListDataTable(), true);
	}

	@Test
	public void testUpdateGermplasmListTableGermplasmHasNoMGID() {

		this.initializeTableWithTestData();

		final Table table = this.listComponent.getListDataTable();

		final int gid1 = 1;
		final Set<Integer> gidsToProcess = new HashSet<>(Arrays.asList(gid1));
		final List<Germplasm> listOfGermplasm = new ArrayList<>();
		final List<GermplasmListData> listOfGermplasmListData = new ArrayList<>();
		listOfGermplasm.add(this.createGermplasm(gid1, 0));
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setGid(gid1);
		final int germplasmListDataId = 1;
		germplasmListData.setId(germplasmListDataId);
		listOfGermplasmListData.add(germplasmListData);

		Mockito.when(this.germplasmDataManager.getGermplasms(new ArrayList<Integer>(gidsToProcess)))
			.thenReturn(listOfGermplasm);
		Mockito.when(this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId()))
			.thenReturn(listOfGermplasmListData);

		this.listComponent.updateGermplasmListTable(gidsToProcess);

		final Item selectedRowItem = table.getItem(gid1);
		Assert.assertEquals("-", selectedRowItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());

	}

	@Test
	public void testUpdateGermplasmListTableGermplasmHasGID() {

		this.initializeTableWithTestData();

		final Table table = this.listComponent.getListDataTable();

		final int gid1 = 1;
		final int mgid = 1234;
		final Set<Integer> gidsToProcess = new HashSet<>(Arrays.asList(gid1));
		final List<Germplasm> listOfGermplasm = new ArrayList<>();
		final List<GermplasmListData> listOfGermplasmListData = new ArrayList<>();
		listOfGermplasm.add(this.createGermplasm(gid1, mgid));
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setGid(gid1);
		final int germplasmListDataId = 1;
		germplasmListData.setId(germplasmListDataId);
		listOfGermplasmListData.add(germplasmListData);

		Mockito.when(this.germplasmDataManager.getGermplasms(new ArrayList<Integer>(gidsToProcess)))
			.thenReturn(listOfGermplasm);
		Mockito.when(this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId()))
			.thenReturn(listOfGermplasmListData);

		this.listComponent.updateGermplasmListTable(gidsToProcess);

		final Item selectedRowItem = table.getItem(gid1);
		Assert.assertEquals(
			String.valueOf(mgid),
			selectedRowItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());

	}

	@Test
	public void testAddAttributeAndNameTypeColumn() {
		final List<String> attributeAndNameTypes = new ArrayList<>();
		this.listComponent.setAttributeAndNameTypeColumns(attributeAndNameTypes);
		final String column = "PASSPORT ATTRIBUTE";
		this.listComponent.addAttributeAndNameTypeColumn(column);
		Assert.assertFalse(this.listComponent.getAttributeAndNameTypeColumns().isEmpty());
		Assert.assertTrue(this.listComponent.getAttributeAndNameTypeColumns().contains(column));
	}

	@Test
	public void testListHasAddedColumns() {
		final Table table = new Table();
		final List<String> attributeAndNameTypes = new ArrayList<>();
		this.listComponent.setListDataTable(table);
		this.listComponent.setAttributeAndNameTypeColumns(attributeAndNameTypes);
		this.listComponent.setAddColumnContextMenu(this.addColumnContextMenu);

		Mockito.doReturn(true).when(this.addColumnContextMenu).hasAddedColumn(table, attributeAndNameTypes);
		Assert.assertTrue(this.listComponent.listHasAddedColumns());

		Mockito.doReturn(false).when(this.addColumnContextMenu).hasAddedColumn(table, attributeAndNameTypes);
		Assert.assertFalse(this.listComponent.listHasAddedColumns());

	}

	private Germplasm createGermplasm(final int gid, final int mgid) {
		final Germplasm germplasm = new Germplasm();
		germplasm.setMgid(mgid);
		germplasm.setGid(gid);
		return germplasm;
	}

}


package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListManagerInventoryTable;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.SeedInventoryListExporter;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryExportException;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.InventoryTableDropHandler;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

import com.beust.jcommander.internal.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ListBuilderComponentTest {

	private static final String SEED_RES = "SEED_RES";
	private static final String AVAIL_INV = "AVAIL_INV";
	private static final String TOTAL = "AVAILABLE";
	private static final String HASH = "#";
	private static final String CHECK = "CHECK";
	private static final String SEED_SOURCE = "SEED_SOURCE";
	private static final String CROSS = "CROSS";
	private static final String DESIG = "DESIG";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	private static final String GID = "GID";
	private static final String STOCKID = "STOCKID";
	private static String DELETE_GERMPLASM_ENTRIES = "Delete Germplasm Entries";
	private static String DELETE_SELECTED_ENTRIES_CONFIRM = "Delete selected germplasm entries?";
	private static String YES = "YES";
	private static String NO = "NO";
	private static String CAPTION = "2";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private TableWithSelectAllLayout tableWithSelectAllLayout;

	@Mock
	private BreedingManagerTable breedingManagerTable;

	@InjectMocks
	private ListBuilderComponent listBuilderComponent;

	@Mock
	private ListManagerMain listManagerMain;

	@Mock
	private BreedingManagerListDetailsComponent breedingManagerListDetailsComponent;

	@Mock
	private BuildNewListDropHandler dropHandler;

	@Mock
	private ListManagerInventoryTable listInventoryTable;

	@Mock
	private UserDataManager userDataManager;

	@Mock
	private ReserveInventoryAction reserveInventoryAction;

	@Mock
	private InventoryTableDropHandler inventoryTableDropHandler;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private Button button;

	@Mock
	private GermplasmList currentlySavedGermplasmList;

	private ImportedGermplasmListDataInitializer importedGermplasmListInitializer;

	@Mock
	private ContextMenuItem menuDeleteSelectedEntries;

	@Mock
	private AddColumnContextMenu addColumnContextMenu;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private FillWith fillWith;

	@Mock
	private Item item;

  	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private Label listEntriesLabel;

	@Mock
	private ContextMenuItem contextMenuItem;

	private static final Integer TEST_GERMPLASM_LIST_ID = 111;
	private static final Integer TEST_GERMPLASM_NO_OF_ENTRIES = 5;
    private static final long LIST_ENTRIES_COUNT = 1;

	@Before
	public void setUp() {
		this.listBuilderComponent.setOntologyDataManager(this.ontologyDataManager);
		this.listBuilderComponent.setMessageSource(this.messageSource);
		this.listBuilderComponent.setTransactionManager(transactionManager);
	    this.listBuilderComponent.setGermplasmListManager(germplasmListManager);

		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn(ListBuilderComponentTest.CHECK);
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn(ListBuilderComponentTest.HASH);
		Mockito.when(this.messageSource.getMessage(Message.DELETE_GERMPLASM_ENTRIES))
				.thenReturn(ListBuilderComponentTest.DELETE_GERMPLASM_ENTRIES);
		Mockito.when(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES_CONFIRM))
				.thenReturn(ListBuilderComponentTest.DELETE_SELECTED_ENTRIES_CONFIRM);
		Mockito.when(this.messageSource.getMessage(Message.YES)).thenReturn(ListBuilderComponentTest.YES);
		Mockito.when(this.messageSource.getMessage(Message.NO)).thenReturn(ListBuilderComponentTest.NO);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.AVAIL_INV, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.SEED_RES, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(new Term(1, ListBuilderComponentTest.GID, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.ENTRY_CODE, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.DESIG, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.CROSS, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.SEED_SOURCE, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.STOCKID, ""));
	}

	@Test
	public void testAddBasicTableColumns() {

		final Table table = new Table();
		this.listBuilderComponent.addBasicTableColumns(table);

		Assert.assertEquals(ListBuilderComponentTest.CHECK, table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals(ListBuilderComponentTest.HASH, table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.AVAIL_INV, table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ListBuilderComponentTest.TOTAL, table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(ListBuilderComponentTest.GID, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.ENTRY_CODE, table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.DESIG, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ListBuilderComponentTest.CROSS, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.SEED_SOURCE, table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.STOCKID, table.getColumnHeader(ColumnLabels.STOCKID.getName()));

	}

	@Test
	public void testDeleteSelectedEntriesWithNoSelectedEntries() {
		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);

		Mockito.when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);
		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] {});
		Mockito.when(this.breedingManagerTable.getValue()).thenReturn(selectedItems);

		this.listBuilderComponent.setTableWithSelectAllLayout(this.tableWithSelectAllLayout);

		this.listBuilderComponent.deleteSelectedEntries();
		try {
			Mockito.verify(source, Mockito.times(1)).getWindow();
		} catch (final WantedButNotInvoked e) {
			Assert.fail("Expecting to show 'no selected germplasm entry' error but didn't.");
		}
	}

	@Test
	public void testDeleteSelectedEntriesWithSelectedEntries() {
		Mockito.when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);

		final Container container = Mockito.mock(Container.class);
		Mockito.when(this.breedingManagerTable.getContainerDataSource()).thenReturn(container);

		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] {1});
		Mockito.when(this.breedingManagerTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setTableWithSelectAllLayout(this.tableWithSelectAllLayout);

		final Table listDataTable = Mockito.mock(Table.class);
		listDataTable.addItem(1);
		listDataTable.addItem(2);
		Mockito.when(listDataTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setListDataTable(listDataTable);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getModeView()).thenReturn(ModeView.LIST_VIEW);
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.setTotalListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.setTotalSelectedListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.doDeleteSelectedEntries();

		try {
			Mockito.verify(container, Mockito.times(1)).removeItem(Matchers.any());
		} catch (final WantedButNotInvoked e) {
			Assert.fail("Expecting to show 'no selected germplasm entry' error but didn't.");
		}
	}

	@Test
	public void testSaveReservationsWithNotEnoughAvailableBalance(){
		this.setUpCurrentlySavedGermplasmList();
		final ReserveInventoryAction reserveInventoryAction = Mockito.mock(ReserveInventoryAction.class);
		this.listBuilderComponent.setReserveInventoryAction(reserveInventoryAction);
		Mockito.when(reserveInventoryAction.saveReserveTransactions(Mockito.anyMap(), Mockito.anyInt())).thenReturn(false);
		this.listBuilderComponent.saveReservationsAction();
		Mockito.verify(this.messageSource).getMessage(Message.INVENTORY_NOT_AVAILABLE_BALANCE);
	}


	@Test
	public void testSaveReservationsAction(){
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setReserveInventoryAction(new ReserveInventoryAction(this.listBuilderComponent));
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		this.listBuilderComponent.getReserveInventoryAction().setContextUtil(contextUtil);
		this.listBuilderComponent.getReserveInventoryAction().setUserDataManager(this.userDataManager);
		this.listBuilderComponent.getReserveInventoryAction().setInventoryDataManager(this.inventoryDataManager);
		final User user = new User();
		user.setUserid(12);
		user.setPersonid(123);
		Mockito.doReturn(user).when(this.userDataManager).getUserById(Matchers.anyInt());
		Mockito.when(contextUtil.getCurrentUserLocalId()).thenReturn(1);

		List<GermplasmListData> germplasmListData = Lists.newArrayList(ListInventoryDataInitializer.createGermplasmListData(1));

		Mockito.when(this.inventoryDataManager.getLotCountsForListEntries(Mockito.isA(Integer.class), Mockito.isA(List.class)))
				.thenReturn(germplasmListData);

	  	Mockito.when(this.inventoryDataManager.getLotCountsForList(this.currentlySavedGermplasmList.getId(), 0,1))
			  .thenReturn(germplasmListData);

		Mockito.when(this.inventoryDataManager.getLotDetailsForList(Mockito.isA(Integer.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(germplasmListData);

	 	 Mockito.when(this.germplasmListManager.countGermplasmListDataByListId(Mockito.isA(Integer.class)))
			  .thenReturn(ListBuilderComponentTest.LIST_ENTRIES_COUNT);

		this.listBuilderComponent.saveReservationsAction();

		Assert.assertEquals("Expecting Valid reservation to save should have size 0 ", 0,this.listBuilderComponent.getValidReservationsToSave().size());
		Mockito.verify(this.messageSource).getMessage(Message.SAVE_RESERVED_AND_CANCELLED_RESERVATION);
		Mockito.verify(this.item, Mockito.times(1)).getItemProperty(ColumnLabels.TOTAL.getName());
		Mockito.verify(this.item, Mockito.times(1)).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());

	}

	private void setUpCurrentlySavedGermplasmList() {
		this.currentlySavedGermplasmList =
				GermplasmListTestDataInitializer.createGermplasmListWithListData(TEST_GERMPLASM_LIST_ID, TEST_GERMPLASM_NO_OF_ENTRIES);
		this.currentlySavedGermplasmList.setStatus(1);
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		this.listBuilderComponent.setContextUtil(contextUtil);
		this.listBuilderComponent.setUnlockButton(button);
		this.listBuilderComponent.setLockButton(button);
		this.listBuilderComponent.setTableWithSelectAllLayout(this.tableWithSelectAllLayout);
		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] {1});
		Mockito.when(this.breedingManagerTable.getValue()).thenReturn(selectedItems);

		this.importedGermplasmListInitializer = new ImportedGermplasmListDataInitializer();
		this.listBuilderComponent.setValidReservationsToSave(this.importedGermplasmListInitializer.createReservations(1));

		final Table listDataTable = Mockito.mock(Table.class);
		listDataTable.addItem(1);
		listDataTable.addItem(2);
		Mockito.when(listDataTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setListDataTable(listDataTable);

		Mockito.when(this.listBuilderComponent.getListDataTable().getItem(Mockito.anyInt())).thenReturn(this.item);
		Property property = Mockito.mock(Property.class);
		Mockito.when(this.item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName())).thenReturn(property);
		Mockito.when(this.item.getItemProperty(ColumnLabels.GID.getName())).thenReturn(property);
		Mockito.when(this.item.getItemProperty(ColumnLabels.GID.getName()).getValue()).thenReturn(button);
		Mockito.when(button.getCaption()).thenReturn(ListBuilderComponentTest.CAPTION);
		Mockito.when(this.item.getItemProperty(ColumnLabels.TOTAL.getName())).thenReturn(property);


		Mockito.when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);
		this.listBuilderComponent.setDropHandler(dropHandler);

		this.listBuilderComponent.setMenuDeleteSelectedEntries(menuDeleteSelectedEntries);
		this.listBuilderComponent.setAddColumnContextMenu(addColumnContextMenu);
		this.listBuilderComponent.setEditHeaderButton(button);
		this.listBuilderComponent.setViewHeaderButton(button);
		this.listBuilderComponent.setSaveButton(button);
		this.listBuilderComponent.setFillWith(fillWith);

		this.listBuilderComponent.setCurrentlySavedGermplasmList(this.currentlySavedGermplasmList);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getWindow()).thenReturn(new Window());
		Mockito.when(source.getModeView()).thenReturn(ModeView.LIST_VIEW);
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.setHasUnsavedChanges(true);

		this.listBuilderComponent.setBreedingManagerListDetailsComponent(breedingManagerListDetailsComponent);
		this.listBuilderComponent.setDropHandler(dropHandler);
		this.listBuilderComponent.setListInventoryTable(listInventoryTable);

		this.listBuilderComponent.setInventoryDataManager(inventoryDataManager);
		this.listBuilderComponent.setMenuSaveReserveInventory(menuDeleteSelectedEntries);
		this.listBuilderComponent.setMenuCopyToListFromInventory(menuDeleteSelectedEntries);
		this.listBuilderComponent.setMenuReserveInventory(menuDeleteSelectedEntries);

		this.listBuilderComponent.setTotalListEntriesLabel(this.listEntriesLabel);
		this.listBuilderComponent.setTotalSelectedListEntriesLabel(this.listEntriesLabel);
		this.listBuilderComponent.setMenuCancelReservation(this.contextMenuItem);
		this.listBuilderComponent.setMenuExportList(this.contextMenuItem);
		this.listBuilderComponent.setMenuCopyToList(this.contextMenuItem);
		this.listBuilderComponent.setBuildNewListTitle(this.listEntriesLabel);

		Mockito.when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler()).thenReturn
				(inventoryTableDropHandler);


	}

	@Test
	public void testSaveReservationContextItemClickWithConcurrentUsersFailToSave() throws Exception {
		final ListBuilderComponent.InventoryViewMenuClickListener inner = this.listBuilderComponent.new InventoryViewMenuClickListener();

		final ContextMenu.ClickEvent clickEventMock = Mockito.mock(ContextMenu.ClickEvent.class);
		final ReserveInventoryAction reserveInventoryAction = Mockito.mock(ReserveInventoryAction.class);

		ContextMenu.ContextMenuItem contextMenuItem = Mockito.mock(ContextMenu.ContextMenuItem.class);
		Mockito.when(contextMenuItem.getName()).thenReturn("Save Changes");

		Mockito.when(clickEventMock.getClickedItem()).thenReturn(contextMenuItem);

		Mockito.when(messageSource.getMessage(Message.SAVE_RESERVATIONS)).thenReturn("Save Changes");

		int threads = 2;

		this.setUpCurrentlySavedGermplasmList();

		this.listBuilderComponent.setReserveInventoryAction(reserveInventoryAction);

		ExecutorService threadPool = Executors.newFixedThreadPool(threads);

		final Map<ListEntryLotDetails, Double> unsavedReservations = new HashMap<>();
		unsavedReservations.put(new ListEntryLotDetails(), new Double(10));
		this.listBuilderComponent.setValidReservationsToSave(unsavedReservations);
		Mockito.when(reserveInventoryAction.saveReserveTransactions(Mockito.anyMap(), Mockito.anyInt())).thenReturn(true);

		Future<Void> threadOne = threadPool.submit(new Callable<Void>() {

			@Override
			public Void call() {
				inner.contextItemClick(clickEventMock);
				return null;
			}
		});

		Future<Void> threadTwo = threadPool.submit(new Callable<Void>() {

			@Override
			public Void call() {
				inner.contextItemClick(clickEventMock);
				return null;
			}
		});

		threadPool.shutdown();
		while (!threadPool.isTerminated()) {
		}
		Mockito.verify(this.messageSource, Mockito.times(2)).getMessage(Message.SAVE_RESERVATIONS);
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SAVE_RESERVED_AND_CANCELLED_RESERVATION);
	}

	@Test
	public void testResetListWhileHavingUnsavedReservations(){
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.resetList();
		Assert.assertEquals("Expecting Valid reservation to save should have size 0 ", 0,this.listBuilderComponent.getValidReservationsToSave().size());
		Assert.assertEquals("Expecting changes to be false ", false,this.listBuilderComponent.hasUnsavedChanges());
		Mockito.verify(this.listInventoryTable).reset();
		Mockito.verify(this.breedingManagerListDetailsComponent).resetFields();
		Mockito.verify(this.messageSource).getMessage(Message.BUILD_A_NEW_LIST);

	}

	@Test
	public void testUserSelectedLotEntriesToCancelReservations(){

		this.setUpCurrentlySavedGermplasmList();
		List<ListEntryLotDetails> userSelectedLotEntriesToCancel = ListInventoryDataInitializer.createLotDetails(1);
		Mockito.doReturn(userSelectedLotEntriesToCancel).when(this.listInventoryTable).getSelectedLots();

		this.listBuilderComponent.userSelectedLotEntriesToCancelReservations();

		Assert.assertEquals("Expecting Valid reservation to save should have size 0 ", 0,this.listBuilderComponent.getValidReservationsToSave().size());
		Assert.assertEquals("Expecting Cancel reservation should have size 4 ", 4,this.listBuilderComponent.getPersistedReservationToCancel().size());
		Mockito.verify(this.menuDeleteSelectedEntries).setEnabled(true);
		Mockito.verify(this.messageSource).getMessage(Message.UNSAVED_RESERVARTION_CANCELLED);

	}

	@Test
	public void testExportSeedPreparationListWithUnsavedList() throws SeedInventoryExportException {
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setCurrentlySavedGermplasmList(null);
		final SeedInventoryListExporter exporterMock = Mockito.mock(SeedInventoryListExporter.class);
		this.listBuilderComponent.exportSeedPreparationList(exporterMock);
		Mockito.verify(this.messageSource).getMessage(Message.ERROR_SAVE_LIST_BEFORE_EXPORTING_LIST);
		Mockito.verify(this.messageSource, Mockito.never()).getMessage(Message.UNSAVED_RESERVATION_WARNING);
		Mockito.verify(exporterMock, Mockito.never()).exportSeedPreparationList();
	}



	@Test
	public void testExportSeedPreparationListWithUnsavedReservations() throws SeedInventoryExportException {
		this.setUpCurrentlySavedGermplasmList();
		final SeedInventoryListExporter exporterMock = Mockito.mock(SeedInventoryListExporter.class);
		this.listBuilderComponent.exportSeedPreparationList(exporterMock);
		Mockito.verify(this.messageSource).getMessage(Message.UNSAVED_RESERVATION_WARNING);
		Mockito.verify(exporterMock).exportSeedPreparationList();
	}

	@Test
	public void testExportSeedPreparationListWithNoUnsavedReservations() throws SeedInventoryExportException {
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setValidReservationsToSave(null);
		final SeedInventoryListExporter exporterMock = Mockito.mock(SeedInventoryListExporter.class);
		this.listBuilderComponent.exportSeedPreparationList(exporterMock);
		Mockito.verify(this.messageSource, Mockito.never()).getMessage(Message.UNSAVED_RESERVATION_WARNING);
		Mockito.verify(exporterMock).exportSeedPreparationList();
	}

	@Test
	public void testPrintLabelsWithUnsavedList(){
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setCurrentlySavedGermplasmList(null);
		this.listBuilderComponent.createLabelsAction();
		Mockito.verify(this.messageSource).getMessage(Message.ERROR_COULD_NOT_CREATE_LABELS);
	}

	@Test
	public void testImportSeedPreparationListWithUnsavedList(){
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setCurrentlySavedGermplasmList(null);
		this.listBuilderComponent.openImportSeedPreparationDialog();
		Mockito.verify(this.messageSource).getMessage(Message.ERROR_SAVE_LIST_BEFORE_IMPORTING_LIST);
	}

	@Test
	public void testReserveInventoryActionForUnsavedList() {
		ContextMenu inventoryViewMenu = Mockito.mock(ContextMenu.class);
		this.listBuilderComponent.setInventoryViewMenu(inventoryViewMenu);
		this.listBuilderComponent.setListInventoryTable(listInventoryTable);

		Mockito.when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler())
				.thenReturn(inventoryTableDropHandler);
		Mockito.doReturn(true).when(this.listBuilderComponent.getInventoryViewMenu()).isVisible();
		Mockito.when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler().isChanged()).thenReturn(true);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.reserveInventoryAction();

		Mockito.verify(this.messageSource).getMessage(Message.ERROR_SAVE_LIST_BEFORE_RESERVING_INVENTORY);
		Mockito.verify(this.messageSource, Mockito.never()).getMessage(Message.ERROR_RESERVE_INVENTORY_IF_NO_LOT_IS_SELECTED);


	}

	@Test
	public void testCancelReservationsActionForUnsavedList(){
		this.listBuilderComponent.setListInventoryTable(listInventoryTable);
		Mockito.when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler())
				.thenReturn(inventoryTableDropHandler);
		Mockito.when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler().isChanged()).thenReturn(true);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.cancelReservationsAction();

		Mockito.verify(this.messageSource).getMessage(Message.ERROR_SAVE_LIST_BEFORE_CANCELLING_RESERVATION);
		Mockito.verify(this.messageSource, Mockito.never()).getMessage(Message.WARNING_CANCEL_RESERVATION_IF_NO_LOT_IS_SELECTED);
		Mockito.verify(this.messageSource, Mockito.never()).getMessage(Message.WARNING_IF_THERE_IS_NO_RESERVATION_FOR_SELECTED_LOT);
	}
}

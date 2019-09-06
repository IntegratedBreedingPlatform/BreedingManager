
package org.generationcp.breeding.manager.listmanager;

import com.beust.jcommander.internal.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.ControllableRefreshTable;
import org.generationcp.breeding.manager.customcomponent.SortableButton;
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
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.WorkbenchTestDataUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListBuilderComponentTest {

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
	private static final String CAPTION = "2";
	public static final int CURRENT_USER_ID = 1;
	public static final String INVENTORY_VIEW = "Inventory View";
	public static final String LIST_ENTRIES_VIEW = "List Entries View";

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
	private InventoryTableDropHandler inventoryTableDropHandler;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private SortableButton button;

	@Mock
	private GermplasmList currentlySavedGermplasmList;

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

	@Mock
	private ContextMenu contextMenu;

	@Mock
	private UserService userService;

	@Mock
	private ContextUtil contextUtil;


	private static final Integer TEST_GERMPLASM_LIST_ID = 111;
	private static final Integer TEST_GERMPLASM_NO_OF_ENTRIES = 5;
	private static final long LIST_ENTRIES_COUNT = 1;

	@Before
	public void setUp() {
		this.listBuilderComponent.setOntologyDataManager(this.ontologyDataManager);
		this.listBuilderComponent.setMessageSource(this.messageSource);
		this.listBuilderComponent.setTransactionManager(this.transactionManager);
		this.listBuilderComponent.setGermplasmListManager(this.germplasmListManager);
		this.listBuilderComponent.setGermplasmListManager(this.germplasmListManager);
		this.listBuilderComponent.setUserService(this.userService);
		this.listBuilderComponent.setContextUtil(this.contextUtil);

		when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn(ListBuilderComponentTest.CHECK);
		when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn(ListBuilderComponentTest.HASH);
		when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.AVAIL_INV, ""));
		when(this.ontologyDataManager.getTermById(TermId.GID.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.GID, ""));
		when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.ENTRY_CODE, ""));
		when(this.ontologyDataManager.getTermById(TermId.DESIG.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.DESIG, ""));
		when(this.ontologyDataManager.getTermById(TermId.CROSS.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.CROSS, ""));
		when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.SEED_SOURCE, ""));
		when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.STOCKID, ""));
		this.listBuilderComponent.setTableWithSelectAllLayout(this.tableWithSelectAllLayout);

		final WorkbenchUser workbenchUser = new WorkbenchTestDataUtil().createTestUserData();
		when(this.contextUtil.getCurrentWorkbenchUserId()).thenReturn(CURRENT_USER_ID);
		when(this.userService.getUserById(CURRENT_USER_ID)).thenReturn(workbenchUser);

		Mockito.when(this.messageSource.getMessage(Message.INVENTORY)).thenReturn(INVENTORY_VIEW);
		Mockito.when(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL)).thenReturn(LIST_ENTRIES_VIEW);

		this.listBuilderComponent.setToolsButtonContainer(Mockito.mock(AbsoluteLayout.class));
		final Table listDataTable = Mockito.mock(Table.class);
		Mockito.when(listDataTable.getValue()).thenReturn(new ArrayList<>());
		this.listBuilderComponent.setListDataTable(listDataTable);
		this.listBuilderComponent.setTotalListEntriesLabel(Mockito.mock(Label.class));

		this.listBuilderComponent.setTotalSelectedListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.setBreedingManagerListDetailsComponent(Mockito.mock(BreedingManagerListDetailsComponent.class));
		this.listBuilderComponent.setDropHandler(Mockito.mock(BuildNewListDropHandler.class));
		this.listBuilderComponent.setTopLabel(new Label());
	}

	@Test
	public void testAddBasicTableColumns() {

		final Table table = new Table();
		this.listBuilderComponent.addBasicTableColumns(table);

		Assert.assertEquals(ListBuilderComponentTest.CHECK, table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals(ListBuilderComponentTest.HASH, table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.AVAIL_INV,
				table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ListBuilderComponentTest.TOTAL, table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(ListBuilderComponentTest.GID, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.ENTRY_CODE,
				table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.DESIG, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ListBuilderComponentTest.CROSS, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.SEED_SOURCE,
				table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.STOCKID, table.getColumnHeader(ColumnLabels.STOCKID.getName()));

	}

	@Test
	public void testDeleteSelectedEntriesWithNoSelectedEntries() {
		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);

		when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);
		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] {});
		when(this.breedingManagerTable.getValue()).thenReturn(selectedItems);

		this.listBuilderComponent.deleteSelectedEntries();
		try {
			Mockito.verify(source, Mockito.times(1)).getWindow();
		} catch (final WantedButNotInvoked e) {
			Assert.fail("Expecting to show 'no selected germplasm entry' error but didn't.");
		}
	}

	@Test
	public void testDeleteSelectedEntriesWithSelectedEntries() {
		when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);

		final Container container = Mockito.mock(Container.class);
		when(this.breedingManagerTable.getContainerDataSource()).thenReturn(container);

		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] { 1 });
		when(this.breedingManagerTable.getValue()).thenReturn(selectedItems);

		final Table listDataTable = Mockito.mock(Table.class);
		listDataTable.addItem(1);
		listDataTable.addItem(2);
		when(listDataTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setListDataTable(listDataTable);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		when(source.getModeView()).thenReturn(ModeView.LIST_VIEW);
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
	public void testSaveReservationsWithNotEnoughAvailableBalance() {
		this.setUpCurrentlySavedGermplasmList();
		final ReserveInventoryAction reserveInventoryAction = Mockito.mock(ReserveInventoryAction.class);
		this.listBuilderComponent.setReserveInventoryAction(reserveInventoryAction);
		when(reserveInventoryAction.saveReserveTransactions(ArgumentMatchers.<Map<ListEntryLotDetails, Double>>any(), Matchers.anyInt()))
				.thenReturn(false);
		this.listBuilderComponent.saveReservationsAction();
		Mockito.verify(this.messageSource).getMessage(Message.INVENTORY_NOT_AVAILABLE_BALANCE);
	}

	@Test
	public void testSaveReservationsAction() {
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setReserveInventoryAction(new ReserveInventoryAction(this.listBuilderComponent));
		this.listBuilderComponent.getReserveInventoryAction().setInventoryDataManager(this.inventoryDataManager);
		this.listBuilderComponent.getReserveInventoryAction().setContextUtil(this.contextUtil);
		this.listBuilderComponent.getReserveInventoryAction().setUserService(this.userService);

		final List<GermplasmListData> germplasmListData = Lists
				.newArrayList(ListInventoryDataInitializer.createGermplasmListData(1));

		when(this.inventoryDataManager.getLotCountsForList(this.currentlySavedGermplasmList.getId(), 0, 1))
				.thenReturn(germplasmListData);

		when(this.inventoryDataManager.getLotDetailsForList(Matchers.isA(Integer.class), Matchers.anyInt(),
				Matchers.anyInt())).thenReturn(germplasmListData);

		when(this.germplasmListManager.countGermplasmListDataByListId(Matchers.isA(Integer.class)))
				.thenReturn(ListBuilderComponentTest.LIST_ENTRIES_COUNT);

		this.listBuilderComponent.saveReservationsAction();

		Assert.assertEquals("Expecting Valid reservation to save should have size 0 ", 0,
				this.listBuilderComponent.getValidReservationsToSave().size());
		Mockito.verify(this.messageSource).getMessage(Message.SAVE_RESERVED_AND_CANCELLED_RESERVATION);
		Mockito.verify(this.item, Mockito.times(1)).getItemProperty(ColumnLabels.TOTAL.getName());
		Mockito.verify(this.item, Mockito.times(1)).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());

	}

	private void setUpCurrentlySavedGermplasmList() {
		this.currentlySavedGermplasmList = GermplasmListTestDataInitializer.createGermplasmListWithListData(
				ListBuilderComponentTest.TEST_GERMPLASM_LIST_ID, ListBuilderComponentTest.TEST_GERMPLASM_NO_OF_ENTRIES);
		this.currentlySavedGermplasmList.setStatus(1);
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		this.listBuilderComponent.setContextUtil(contextUtil);
		this.listBuilderComponent.setUnlockButton(this.button);
		this.listBuilderComponent.setLockButton(this.button);
		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] { 1 });

		this.listBuilderComponent
				.setValidReservationsToSave(ImportedGermplasmListDataInitializer.createReservations(1));

		final Table listDataTable = Mockito.mock(Table.class);
		listDataTable.addItem(1);
		listDataTable.addItem(2);
		when(listDataTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setListDataTable(listDataTable);

		when(this.listBuilderComponent.getListDataTable().getItem(Matchers.anyInt())).thenReturn(this.item);
		final Property property = Mockito.mock(Property.class);
		when(this.item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName())).thenReturn(property);
		when(this.item.getItemProperty(ColumnLabels.GID.getName())).thenReturn(property);
		when(this.item.getItemProperty(ColumnLabels.GID.getName()).getValue()).thenReturn(this.button);
		when(this.button.getCaption()).thenReturn(ListBuilderComponentTest.CAPTION);
		when(this.item.getItemProperty(ColumnLabels.TOTAL.getName())).thenReturn(property);

		when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);
		this.listBuilderComponent.setDropHandler(this.dropHandler);

		this.listBuilderComponent.setMenuDeleteSelectedEntries(this.menuDeleteSelectedEntries);
		this.listBuilderComponent.setAddColumnContextMenu(this.addColumnContextMenu);
		this.listBuilderComponent.setEditHeaderButton(this.button);
		this.listBuilderComponent.setViewHeaderButton(this.button);
		this.listBuilderComponent.setSaveButton(this.button);
		this.listBuilderComponent.setFillWith(this.fillWith);

		this.listBuilderComponent.setCurrentlySavedGermplasmList(this.currentlySavedGermplasmList);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		when(source.getWindow()).thenReturn(new Window());
		when(source.getModeView()).thenReturn(ModeView.LIST_VIEW);
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.setHasUnsavedChanges(true);

		this.listBuilderComponent.setBreedingManagerListDetailsComponent(this.breedingManagerListDetailsComponent);
		this.listBuilderComponent.setDropHandler(this.dropHandler);
		this.listBuilderComponent.setListInventoryTable(this.listInventoryTable);

		this.listBuilderComponent.setInventoryDataManager(this.inventoryDataManager);
		this.listBuilderComponent.setMenuSaveReserveInventory(this.menuDeleteSelectedEntries);
		this.listBuilderComponent.setMenuCopyToListFromInventory(this.menuDeleteSelectedEntries);
		this.listBuilderComponent.setMenuReserveInventory(this.menuDeleteSelectedEntries);

		this.listBuilderComponent.setTotalListEntriesLabel(this.listEntriesLabel);
		this.listBuilderComponent.setTotalSelectedListEntriesLabel(this.listEntriesLabel);
		this.listBuilderComponent.setMenuCancelReservation(this.contextMenuItem);
		this.listBuilderComponent.setMenuExportList(this.contextMenuItem);
		this.listBuilderComponent.setMenuCopyToList(this.contextMenuItem);
		this.listBuilderComponent.setBuildNewListTitle(this.listEntriesLabel);

		when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler())
				.thenReturn(this.inventoryTableDropHandler);

	}

	@Test
	public void testSaveReservationContextItemClickWithConcurrentUsersFailToSave() throws Exception {
		final ListBuilderComponent.InventoryViewMenuClickListener inner = this.listBuilderComponent.new InventoryViewMenuClickListener();

		final ContextMenu.ClickEvent clickEventMock = Mockito.mock(ContextMenu.ClickEvent.class);
		final ReserveInventoryAction reserveInventoryAction = Mockito.mock(ReserveInventoryAction.class);

		final ContextMenu.ContextMenuItem contextMenuItem = Mockito.mock(ContextMenu.ContextMenuItem.class);
		when(contextMenuItem.getName()).thenReturn("Save Changes");

		when(clickEventMock.getClickedItem()).thenReturn(contextMenuItem);

		when(this.messageSource.getMessage(Message.SAVE_RESERVATIONS)).thenReturn("Save Changes");

		final int threads = 2;

		this.setUpCurrentlySavedGermplasmList();

		this.listBuilderComponent.setReserveInventoryAction(reserveInventoryAction);

		final ExecutorService threadPool = Executors.newFixedThreadPool(threads);

		final Map<ListEntryLotDetails, Double> unsavedReservations = new HashMap<>();
		unsavedReservations.put(new ListEntryLotDetails(), new Double(10));
		this.listBuilderComponent.setValidReservationsToSave(unsavedReservations);
		when(reserveInventoryAction.saveReserveTransactions(ArgumentMatchers.<Map<ListEntryLotDetails, Double>>any(), Matchers.anyInt()))
				.thenReturn(true);

		final Future<Void> threadOne = threadPool.submit(new Callable<Void>() {

			@Override
			public Void call() {
				inner.contextItemClick(clickEventMock);
				return null;
			}
		});

		final Future<Void> threadTwo = threadPool.submit(new Callable<Void>() {

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
		Mockito.verify(this.messageSource, Mockito.times(1))
				.getMessage(Message.SAVE_RESERVED_AND_CANCELLED_RESERVATION);
	}

	@Test
	public void testResetListWhileHavingUnsavedReservations() {
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.resetList();
		Assert.assertEquals("Expecting Valid reservation to save should have size 0 ", 0,
				this.listBuilderComponent.getValidReservationsToSave().size());
		Assert.assertEquals("Expecting changes to be false ", false, this.listBuilderComponent.hasUnsavedChanges());
		Mockito.verify(this.listInventoryTable).reset();
		Mockito.verify(this.breedingManagerListDetailsComponent).resetFields();
		Mockito.verify(this.messageSource).getMessage(Message.BUILD_A_NEW_LIST);

	}

	@Test
	public void testUserSelectedLotEntriesToCancelReservations() {

		this.setUpCurrentlySavedGermplasmList();
		final List<ListEntryLotDetails> userSelectedLotEntriesToCancel = ListInventoryDataInitializer
				.createLotDetails(1);
		Mockito.doReturn(userSelectedLotEntriesToCancel).when(this.listInventoryTable).getSelectedLots();

		this.listBuilderComponent.userSelectedLotEntriesToCancelReservations();

		Assert.assertEquals("Expecting Valid reservation to save should have size 0 ", 0,
				this.listBuilderComponent.getValidReservationsToSave().size());
		Assert.assertEquals("Expecting Cancel reservation should have size 4 ", 4,
				this.listBuilderComponent.getPersistedReservationToCancel().size());
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
	public void testPrintLabelsWithUnsavedList() {
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setCurrentlySavedGermplasmList(null);
		this.listBuilderComponent.createLabelsAction();
		Mockito.verify(this.messageSource).getMessage(Message.ERROR_COULD_NOT_CREATE_LABELS);
	}

	@Test
	public void testImportSeedPreparationListWithUnsavedList() {
		this.setUpCurrentlySavedGermplasmList();
		this.listBuilderComponent.setCurrentlySavedGermplasmList(null);
		this.listBuilderComponent.openImportSeedPreparationDialog();
		Mockito.verify(this.messageSource).getMessage(Message.ERROR_SAVE_LIST_BEFORE_IMPORTING_LIST);
	}

	@Test
	public void testReserveInventoryActionForUnsavedList() {
		final ContextMenu inventoryViewMenu = Mockito.mock(ContextMenu.class);
		this.listBuilderComponent.setInventoryViewMenu(inventoryViewMenu);
		this.listBuilderComponent.setListInventoryTable(this.listInventoryTable);

		when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler())
				.thenReturn(this.inventoryTableDropHandler);
		Mockito.doReturn(true).when(this.listBuilderComponent.getInventoryViewMenu()).isVisible();
		when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler().isChanged())
				.thenReturn(true);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.reserveInventoryAction();

		Mockito.verify(this.messageSource).getMessage(Message.ERROR_SAVE_LIST_BEFORE_RESERVING_INVENTORY);
		Mockito.verify(this.messageSource, Mockito.never())
				.getMessage(Message.ERROR_RESERVE_INVENTORY_IF_NO_LOT_IS_SELECTED);

	}

	@Test
	public void testCancelReservationsActionForUnsavedList() {
		this.listBuilderComponent.setListInventoryTable(this.listInventoryTable);
		when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler())
				.thenReturn(this.inventoryTableDropHandler);
		when(this.listBuilderComponent.getListInventoryTable().getInventoryTableDropHandler().isChanged())
				.thenReturn(true);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.cancelReservationsAction();

		Mockito.verify(this.messageSource).getMessage(Message.ERROR_SAVE_LIST_BEFORE_CANCELLING_RESERVATION);
		Mockito.verify(this.messageSource, Mockito.never())
				.getMessage(Message.WARNING_CANCEL_RESERVATION_IF_NO_LOT_IS_SELECTED);
		Mockito.verify(this.messageSource, Mockito.never())
				.getMessage(Message.WARNING_IF_THERE_IS_NO_RESERVATION_FOR_SELECTED_LOT);
	}

	@Test
	public void testInitializeAddColumnContextMenu() {

		this.listBuilderComponent.setContextMenu(this.contextMenu);
		final ContextMenuItem item = Mockito.mock(ContextMenuItem.class);
		this.addColumnContextMenu.setAddColumnItem(item);

		this.listBuilderComponent.setAddColumnContextMenu(this.addColumnContextMenu);
		when(item.addItem(Matchers.anyString())).thenReturn(item);
		when(this.contextMenu.addItem(ArgumentMatchers.isNull(String.class))).thenReturn(item);
		when(this.contextMenu.addItem(Matchers.any(String.class))).thenReturn(item);
		when(this.messageSource.getMessage(ArgumentMatchers.isNull(Message.class))).thenReturn("Bye");
		when(this.messageSource.getMessage(Matchers.any(Message.class))).thenReturn("Hi");

		this.listBuilderComponent.initializeAddColumnContextMenu();
		Mockito.verify(this.contextMenu).addListener(Matchers.any(ContextMenu.ClickListener.class));
	}

	@Test
	public void testAddAttributeAndNameTypeColumn() {
		final List<String> attributeAndNameTypes = new ArrayList<>();
		this.listBuilderComponent.setAttributeAndNameTypeColumns(attributeAndNameTypes);
		final String column = "PASSPORT ATTRIBUTE";
		this.listBuilderComponent.addAttributeAndNameTypeColumn(column);
		Assert.assertFalse(this.listBuilderComponent.getAttributeAndNameTypeColumns().isEmpty());
		Assert.assertTrue(this.listBuilderComponent.getAttributeAndNameTypeColumns().contains(column));
	}

	@Test
	public void testListHasAddedColumns() {
		final Table table = new Table();
		final List<String> attributeAndNameTypes = new ArrayList<>();
		this.listBuilderComponent.setListDataTable(table);
		this.listBuilderComponent.setAttributeAndNameTypeColumns(attributeAndNameTypes);
		this.listBuilderComponent.setAddColumnContextMenu(this.addColumnContextMenu);

		Mockito.doReturn(true).when(this.addColumnContextMenu).hasAddedColumn(table, attributeAndNameTypes);
		Assert.assertTrue(this.listBuilderComponent.listHasAddedColumns());

		Mockito.doReturn(false).when(this.addColumnContextMenu).hasAddedColumn(table, attributeAndNameTypes);
		Assert.assertFalse(this.listBuilderComponent.listHasAddedColumns());

	}

	@Test
	public void testChangeToListView() {
		final Label topLabel = new Label(INVENTORY_VIEW);
		this.listBuilderComponent.setTopLabel(topLabel);
		final ListManagerInventoryTable listInventoryTable = Mockito.mock(ListManagerInventoryTable.class);
		Mockito.when(listInventoryTable.getInventoryTableDropHandler()).thenReturn(Mockito.mock(InventoryTableDropHandler.class));
		this.listBuilderComponent.setListInventoryTable(listInventoryTable);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getModeView()).thenReturn(ModeView.LIST_VIEW);
		this.listBuilderComponent.setSource(source);
		this.listBuilderComponent.changeToListView();

		Assert.assertEquals(LIST_ENTRIES_VIEW, topLabel.getValue().toString());
	}

	@Test
	public void testChangeToInventoryView() {

		final Label topLabel = new Label(LIST_ENTRIES_VIEW);
		this.listBuilderComponent.setTopLabel(topLabel);
		final ListManagerInventoryTable listInventoryTable = Mockito.mock(ListManagerInventoryTable.class);
		Mockito.when(listInventoryTable.getInventoryTableDropHandler()).thenReturn(Mockito.mock(InventoryTableDropHandler.class));
		final ControllableRefreshTable refreshTable = Mockito.mock(ControllableRefreshTable.class);
		Mockito.when(refreshTable.getValue()).thenReturn(new ArrayList<>());
		Mockito.when(listInventoryTable.getTable()).thenReturn(refreshTable);
		this.listBuilderComponent.setListInventoryTable(listInventoryTable);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getModeView()).thenReturn(ModeView.INVENTORY_VIEW);
		this.listBuilderComponent.setSource(source);
		this.listBuilderComponent.changeToInventoryView();

		Assert.assertEquals(INVENTORY_VIEW, topLabel.getValue().toString());
	}
}

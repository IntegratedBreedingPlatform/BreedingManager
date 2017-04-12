package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.ControllableRefreshTable;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


@RunWith(MockitoJUnitRunner.class)
public class SelectParentsListDataComponentTest {

	private static final String SELECTED = "Selected";
	private static final String WARNING = "Warning";
	private static final String LOTS = "Lots";
	private static final int GERMPLASM_LIST_ID = 1;
	private static final int NO_OF_ENTRIES = 5;
	private static final int NO_OF_SELECTED = 2;
	private static final String DUMMY_MESSAGE = "Dummy Message";
	private static final String LIST_NAME = "Sample List";
	private static final String TOTAL_LOTS = "Total Lots";
	private static final String NO_LIST_DATA_RETURNED = "No list data retrieved";
	private static final String TOTAL_ENTRIES = "Total Entries";

	@Mock
	private MakeCrossesParentsComponent makeCrossesParentsComponent;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;
	@Mock
	private ViewListHeaderWindow viewListHeaderWindow;
	@Mock
	private SelectParentsComponent selectParentComponent;
	@Mock
	private InventoryDataManager inventoryDataManager;
	@Mock
	private CrossingManagerInventoryTable listInventoryTable;
	@Mock
	private Component component;

	@Mock
	private UserDataManager userDataManager;

	@InjectMocks
	private final SelectParentsListDataComponent selectParents =
			new SelectParentsListDataComponent(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID,
					SelectParentsListDataComponentTest.LIST_NAME, this.makeCrossesParentsComponent);

	private GermplasmList germplasmList;
	private Table listDataTable;
	private ControllableRefreshTable inventoryTable;
	private List<GermplasmListData> listEntries;
	private final List<ListEntryLotDetails> selectedLotEntries = new ArrayList<ListEntryLotDetails>();
	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;

	@Before
	public void setUp() throws Exception {
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		this.germplasmList =
				this.germplasmListTestDataInitializer.createGermplasmList(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
				.getGermplasmListById(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(Long.valueOf(SelectParentsListDataComponentTest.NO_OF_ENTRIES)).when(this.germplasmListManager)
				.countGermplasmListDataByListId(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);

		this.listEntries = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();
		Mockito.doReturn(this.listEntries).when(this.inventoryDataManager)
				.getLotCountsForList(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID, 0, Integer.MAX_VALUE);

		Mockito.doReturn(this.makeCrossesMain).when(this.makeCrossesParentsComponent).getMakeCrossesMain();
		Mockito.doReturn(this.selectParentComponent).when(this.makeCrossesMain).getSelectParentsComponent();
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();

		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.ADD_TO_MALE_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.ADD_TO_FEMALE_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.INVENTORY_VIEW);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_ALL);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_EVEN_ENTRIES);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_ODD_ENTRIES);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.COPY_TO_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.RESERVE_INVENTORY);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.RETURN_TO_LIST_VIEW);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SAVE_CHANGES);

		Mockito.doReturn(TOTAL_ENTRIES).when(this.messageSource).getMessage(Message.TOTAL_LIST_ENTRIES);
		Mockito.doReturn(NO_LIST_DATA_RETURNED).when(this.messageSource).getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL);
		Mockito.doReturn(TOTAL_LOTS).when(this.messageSource).getMessage(Message.TOTAL_LOTS);
		Mockito.doReturn(SELECTED).when(this.messageSource).getMessage(Message.SELECTED);
		Mockito.doReturn(WARNING).when(this.messageSource).getMessage(Message.WARNING);
		Mockito.doReturn(LOTS).when(this.messageSource).getMessage(Message.INVENTORY);

		this.selectParents.instantiateComponents();
		this.listDataTable = this.initListDataTable();
		this.selectParents.setListInventoryTable(this.listInventoryTable);

		this.inventoryTable = this.prepareInventoryTable();
		Mockito.doReturn(this.inventoryTable).when(this.listInventoryTable).getTable();

		this.selectParents.addListeners();
		this.selectParents.layoutComponents();

		final Window window = Mockito.mock(Window.class);
		Mockito.doReturn(window).when(this.component).getWindow();
		this.selectParents.setParent(this.component);

		Mockito.doReturn(window).when(this.makeCrossesParentsComponent).getWindow();
	}

	private ControllableRefreshTable prepareInventoryTable() {
		final List<ListEntryLotDetails> lotDetails = this.createLotDetails(SelectParentsListDataComponentTest.NO_OF_ENTRIES);
		final ControllableRefreshTable table = this.createListInventoryTable(lotDetails);

		// init selected entries
		for (int i = 0; i < SelectParentsListDataComponentTest.NO_OF_SELECTED; i++) {
			final ListEntryLotDetails lotDetail = lotDetails.get(i);
			this.selectedLotEntries.add(lotDetail);
			table.select(lotDetail);
		}

		return table;
	}

	private List<ListEntryLotDetails> createLotDetails(final int noOfEntries) {
		final List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		for (int i = 0; i < noOfEntries; i++) {
			final ListEntryLotDetails lotDetail = ListInventoryDataInitializer.createLotDetail(i, 1);
			lotDetails.add(lotDetail);
		}
		return lotDetails;
	}

	private ControllableRefreshTable createListInventoryTable(final List<ListEntryLotDetails> lotDetails) {
		final ControllableRefreshTable table = new ControllableRefreshTable();
		table.setMultiSelect(true);
		table.addContainerProperty(ColumnLabels.NEWLY_RESERVED.getName(), Double.class, null);

		for (final ListEntryLotDetails lotDetail : lotDetails) {
			final Item item = table.addItem(lotDetail);
			item.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(0.0);
		}

		return table;
	}

	@Test
	public void testInitializeListDataTable_returnsTheValueFromColumLabelDefaultName() {
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		final ControllableRefreshTable table = tableWithSelectAll.getTable();

		this.selectParents.initializeListDataTable(table);

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("LOTS", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("TOTAL WITHDRAWALS", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitializeListDataTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		final Long count = 5L;
		this.selectParents.setCount(count);
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.TOTAL_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GROUP_ID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout("Tag");
		tableWithSelectAll.instantiateComponents();
		final Table table = tableWithSelectAll.getTable();

		this.selectParents.initializeListDataTable(table);

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GROUP_ID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitializeValues() {
		this.selectParents.initializeValues();
		Assert.assertEquals("Expecting that the List Data Table rows are initialized properly but didn't.", this.listEntries.size(),
				this.listDataTable.size());

	}

	private Table initListDataTable() {
		// Initialize List Data Table first
		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		final Table listDataTable = tableWithSelectAll.getTable();
		this.selectParents.initializeListDataTable(listDataTable);
		this.selectParents.setListDataTableWithSelectAll(tableWithSelectAll);
		return listDataTable;
	}

	@Test
	public void testUpdateNoOfEntries_InListViewAndCountHasValue() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		final long count = 10L;

		this.selectParents.updateNoOfEntries(count);

		final String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue("Expecting that the count is included in the caption of the total list entries label for list view but didn't.",
				actualValue.contains(String.valueOf(count)));
		Assert.assertTrue(
				"Expecting that the label caption is set to " + SelectParentsListDataComponentTest.TOTAL_ENTRIES + " but didn't.l",
				actualValue.startsWith(SelectParentsListDataComponentTest.TOTAL_ENTRIES));
	}

	@Test
	public void testUpdateNoOfEntries_InListViewAndCountIsZero() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		final long count = 0;

		this.selectParents.updateNoOfEntries(count);

		final String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertFalse(
				"Expecting that the count is not included in the caption of the total list entries label for list view but didn't.",
				actualValue.contains(String.valueOf(count)));
		Assert.assertEquals(
				"Expecting that the label caption is set to " + SelectParentsListDataComponentTest.NO_LIST_DATA_RETURNED + " but didn't.l",
				SelectParentsListDataComponentTest.NO_LIST_DATA_RETURNED, actualValue);

	}

	@Test
	public void testUpdateNoOfEntries_InInventoryViewAndCountHasValue() {
		Mockito.doReturn(ModeView.INVENTORY_VIEW).when(this.makeCrossesMain).getModeView();
		final long count = 10L;

		this.selectParents.updateNoOfEntries(count);

		final String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue("Expecting that the count is included in the caption of the total list entries label for list view but didn't.",
				actualValue.contains(String.valueOf(count)));
		Assert.assertTrue("Expecting that the label caption is set to " + SelectParentsListDataComponentTest.TOTAL_LOTS + " but didn't.l",
				actualValue.startsWith(SelectParentsListDataComponentTest.TOTAL_LOTS));
	}

	@Test
	public void testUpdateNoOfEntries_InListView() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		this.selectParents.initializeValues();
		final long count = this.selectParents.getListDataTable().size();

		this.selectParents.updateNoOfEntries();
		final String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue(
				"Expecting that the count included in the caption of the total list entries label is from the list data table but didn't.",
				actualValue.contains(String.valueOf(count)));
	}

	@Test
	public void testUpdateNoOfEntries_InInventoryView() {
		Mockito.doReturn(ModeView.INVENTORY_VIEW).when(this.makeCrossesMain).getModeView();
		final ControllableRefreshTable table = new ControllableRefreshTable();
		table.addItem(ListInventoryDataInitializer.createLotDetail(1, 1));
		Mockito.doReturn(table).when(this.listInventoryTable).getTable();

		final long count = this.selectParents.getListInventoryTable().getTable().size();
		this.selectParents.updateNoOfEntries();
		final String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue(
				"Expecting that the count included in the caption of the total lot entries label is from inventory table but didn't.",
				actualValue.contains(String.valueOf(count)));
	}

	@Test
	public void testUpdateNoOfSelectedEntries_InListView() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();

		// select at least 2 entry from the list data table
		this.selectParents.initializeValues();
		final Table table = this.selectParents.getListDataTable();
		for (int i = 1; i <= SelectParentsListDataComponentTest.NO_OF_SELECTED; i++) {
			table.select(i);
		}

		this.selectParents.updateNoOfSelectedEntries();

		final String selectedEntriesLabel = this.selectParents.getTotalSelectedListEntriesLabel().getValue().toString();
		Assert.assertTrue("Expecting that the count included in the caption of the Selected label is from list data table but didn't.",
				selectedEntriesLabel.contains(String.valueOf(SelectParentsListDataComponentTest.NO_OF_SELECTED)));
	}

	@Test
	public void testUpdateNoOfSelectedEntries_InInventoryView() {
		Mockito.doReturn(ModeView.INVENTORY_VIEW).when(this.makeCrossesMain).getModeView();

		this.selectParents.updateNoOfSelectedEntries();

		final String selectedEntriesLabel = this.selectParents.getTotalSelectedListEntriesLabel().getValue().toString();
		Assert.assertTrue("Expecting that the count included in the caption of the Selected label is from list inventory table but didn't.",
				selectedEntriesLabel.contains(String.valueOf(SelectParentsListDataComponentTest.NO_OF_SELECTED)));
	}

	@Test
	public void testViewListAction_WhenThereIsNoChanges() {
		this.selectParents.setHasUnsavedChanges(false);

		this.selectParents.viewListAction();

		Assert.assertTrue("Expecting the mode is still in LIST VIEW when there is no change.",
				this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW));

	}

	@Test
	public void testViewListAction_WhenThereIsChanges() {
		this.selectParents.setHasUnsavedChanges(true);

		this.selectParents.viewListAction();

		Mockito.verify(this.makeCrossesMain, Mockito.times(1))
				.showUnsavedChangesConfirmDialog(Matchers.anyString(), Matchers.any(ModeView.class));

		Assert.assertTrue("Expecting the mode is set to LIST VIEW when there is a change.",
				this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW));

	}

	@Test
	public void testViewInventoryAction_WhenThereIsNoChanges() {
		Mockito.doReturn(ModeView.INVENTORY_VIEW).when(this.makeCrossesMain).getModeView();
		this.selectParents.setHasUnsavedChanges(false);

		this.selectParents.viewInventoryAction();

		Assert.assertTrue("Expecting the mode is still in INVENTORY VIEW when there is no change.",
				this.makeCrossesMain.getModeView().equals(ModeView.INVENTORY_VIEW));

	}

	@Test
	public void testViewInventoryAction_WhenThereIsChanges() {
		Mockito.doReturn(ModeView.INVENTORY_VIEW).when(this.makeCrossesMain).getModeView();
		this.selectParents.setHasUnsavedChanges(true);

		this.selectParents.viewInventoryAction();

		Mockito.verify(this.makeCrossesMain, Mockito.times(1))
				.showUnsavedChangesConfirmDialog(Matchers.anyString(), Matchers.any(ModeView.class));

		Assert.assertTrue("Expecting the mode is set to INVENTORY VIEW when there is a change.",
				this.makeCrossesMain.getModeView().equals(ModeView.INVENTORY_VIEW));

	}

	@Test
	public void testChangeToListView_WhenListInventoryTableIsVisible() {
		Mockito.doReturn(true).when(this.listInventoryTable).isVisible();

		this.selectParents.changeToListView();

		try {
			Mockito.verify(this.listInventoryTable, Mockito.times(1)).setVisible(false);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting the the listInventoryTable is hidden after calling the method but didn't.");
		}
	}

	@Test
	public void testChangeToListView_WhenListInventoryTableIsInvisible() {
		Mockito.doReturn(false).when(this.listInventoryTable).isVisible();

		this.selectParents.changeToListView();

		try {
			Mockito.verify(this.listInventoryTable, Mockito.times(0)).setVisible(false);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail(
					"Expecting the the listInventoryTable is already hidden after calling the method so no need to actually set it to hidden but didn't.");
		}

	}

	@Test
	public void testChangeToInventoryView_WhenTableWithSelectAllLayoutIsVisible() {
		final TableWithSelectAllLayout tableWithSelectAllLayout = Mockito.mock(TableWithSelectAllLayout.class);
		Mockito.doReturn(true).when(tableWithSelectAllLayout).isVisible();
		final ControllableRefreshTable table = new ControllableRefreshTable();
		table.setMultiSelect(true);
		table.addItem(1);
		table.addItem(2);
		table.addItem(3);

		Mockito.doReturn(table).when(tableWithSelectAllLayout).getTable();
		this.selectParents.setListDataTableWithSelectAll(tableWithSelectAllLayout);

		this.selectParents.changeToInventoryView();
		final String label = this.selectParents.getListEntriesLabel().getValue().toString();
		Assert.assertTrue("Expecting that the label starts with LOT.", label.startsWith(label));
	}

	@Test
	public void testChangeToInventoryView_WhenTableWithSelectAllLayoutIsInvisible() {
		final TableWithSelectAllLayout tableWithSelectAllLayout = Mockito.mock(TableWithSelectAllLayout.class);
		Mockito.doReturn(false).when(tableWithSelectAllLayout).isVisible();
		this.selectParents.setListDataTableWithSelectAll(tableWithSelectAllLayout);

		this.selectParents.changeToInventoryView();
		try {
			Mockito.verify(this.messageSource, Mockito.times(0)).getMessage(Message.INVENTORY);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the label is not set at all but didn't.");
		}

	}

	@Test
	public void testReserveInventoryAction_InListView() {
		Mockito.doReturn(false).when(this.listInventoryTable).isVisible();
		this.selectParents.reserveInventoryAction();

		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.WARNING);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the warning message is called when the user tries to perform inventory reservation in List View.");
		}
	}

	@Test
	public void testReserveInventoryAction_InInventoryViewWithoutSelectedLotsWhenNull() {
		Mockito.doReturn(true).when(this.listInventoryTable).isVisible();
		Mockito.doReturn(null).when(this.listInventoryTable).getSelectedLots();
		this.selectParents.reserveInventoryAction();

		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.WARNING);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail(
					"Expecting that the warning message is called when the user tries to perform inventory reservation without selected lots.");
		}
	}

	@Test
	public void testReserveInventoryAction_InInventoryViewWithoutSelectedLotsWhenEmpty() {
		Mockito.doReturn(true).when(this.listInventoryTable).isVisible();
		Mockito.doReturn(new ArrayList<ListEntryLotDetails>()).when(this.listInventoryTable).getSelectedLots();
		this.selectParents.reserveInventoryAction();

		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.WARNING);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail(
					"Expecting that the warning message is called when the user tries to perform inventory reservation without selected lots.");
		}
	}

	@Test
	public void testReserveInventoryAction_InInventoryViewWithSelectedLots() {
		Mockito.doReturn(true).when(this.listInventoryTable).isVisible();
		final List<ListEntryLotDetails> lotDetails = this.createLotDetails(SelectParentsListDataComponentTest.NO_OF_ENTRIES);
		Mockito.doReturn(lotDetails).when(this.listInventoryTable).getSelectedLots();

		this.selectParents.reserveInventoryAction();

		try {
			Mockito.verify(this.messageSource, Mockito.times(0)).getMessage(Message.WARNING);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail(
					"Expecting that NO warning message is called when the user tries to perform inventory reservation with selected lots.");
		}
	}

	@Test
	public void testUpdateListInventoryTable_WithAllValidReservation() {

		final Map<ListEntryLotDetails, Double> validReservations = new HashMap<ListEntryLotDetails, Double>();

		Double reservationAmount = 1.5D;
		for (final ListEntryLotDetails lotDetail : this.selectedLotEntries) {
			validReservations.put(lotDetail, reservationAmount++);
		}

		this.selectParents.updateListInventoryTable(validReservations, false);

		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SUCCESS);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the success notification is invoked when all reservations are valid but didn't.");
		}
	}

	@Test
	public void testUpdateListInventoryTable_WithInvalidReservation() {

		this.selectParents.updateListInventoryTable(new HashMap<ListEntryLotDetails, Double>(), true);

		try {
			Mockito.verify(this.messageSource, Mockito.times(1))
					.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the error notification is invoked when there is an invalid reservation didn't.");
		}
	}

}

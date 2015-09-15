
package org.generationcp.breeding.manager.crossingmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.breeding.manager.data.initializer.GermplasmListDataInitializer;
import org.generationcp.breeding.manager.data.initializer.ListInventoryDataInitializer;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Table;

@RunWith(MockitoJUnitRunner.class)
public class SelectParentsListDataComponentTest {

	private static final int GERMPLASM_LIST_ID = 1;
	private static final long NO_OF_ENTRIES = 5;
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

	@InjectMocks
	private SelectParentsListDataComponent selectParents = new SelectParentsListDataComponent(
			SelectParentsListDataComponentTest.GERMPLASM_LIST_ID, SelectParentsListDataComponentTest.LIST_NAME,
			this.makeCrossesParentsComponent);

	private GermplasmList germplasmList;
	private Table listDataTable;
	private List<GermplasmListData> listEntries;

	@Before
	public void setUp() throws Exception {
		this.germplasmList = GermplasmListDataInitializer.createGermplasmList(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
				.getGermplasmListById(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(NO_OF_ENTRIES).when(this.germplasmListManager)
				.countGermplasmListDataByListId(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);

		this.listEntries = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();
		Mockito.doReturn(this.listEntries).when(this.inventoryDataManager).getLotCountsForList(GERMPLASM_LIST_ID, 0, Integer.MAX_VALUE);

		Mockito.doReturn(this.makeCrossesMain).when(this.makeCrossesParentsComponent).getMakeCrossesMain();
		Mockito.doReturn(this.selectParentComponent).when(this.makeCrossesMain).getSelectParentsComponent();
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();

		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.ADD_TO_MALE_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.ADD_TO_FEMALE_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.INVENTORY_VIEW);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_ALL);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_EVEN_ENTRIES);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_ODD_ENTRIES);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.COPY_TO_NEW_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.RESERVE_INVENTORY);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.RETURN_TO_LIST_VIEW);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SAVE_CHANGES);

		Mockito.doReturn(TOTAL_ENTRIES).when(this.messageSource).getMessage(Message.TOTAL_LIST_ENTRIES);
		Mockito.doReturn(NO_LIST_DATA_RETURNED).when(this.messageSource).getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL);
		Mockito.doReturn(TOTAL_LOTS).when(this.messageSource).getMessage(Message.TOTAL_LOTS);

		this.selectParents.instantiateComponents();
		this.selectParents.layoutComponents();

		this.listDataTable = this.initListDataTable();
	}

	@Test
	public void testInitializeListDataTable_returnsTheValueFromColumLabelDefaultName() {
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		Table table = tableWithSelectAll.getTable();

		this.selectParents.initializeListDataTable(table);

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("LOTS AVAILABLE", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("SEED RES", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitializeListDataTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		Long count = 5L;
		this.selectParents.setCount(count);
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		Table table = tableWithSelectAll.getTable();

		this.selectParents.initializeListDataTable(table);

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
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
		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		Table listDataTable = tableWithSelectAll.getTable();
		this.selectParents.initializeListDataTable(listDataTable);
		this.selectParents.setListDataTableWithSelectAll(tableWithSelectAll);
		return listDataTable;
	}

	@Test
	public void testUpdateNoOfEntries_InListViewAndCountHasValue() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		long count = 10L;

		this.selectParents.updateNoOfEntries(count);

		String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue("Expecting that the count is included in the caption of the total list entries label for list view but didn't.",
				actualValue.contains(String.valueOf(count)));
		Assert.assertTrue("Expecting that the label caption is set to " + TOTAL_ENTRIES + " but didn't.l",
				actualValue.startsWith(TOTAL_ENTRIES));
	}

	@Test
	public void testUpdateNoOfEntries_InListViewAndCountIsZero() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		long count = 0;

		this.selectParents.updateNoOfEntries(count);

		String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertFalse(
				"Expecting that the count is not included in the caption of the total list entries label for list view but didn't.",
				actualValue.contains(String.valueOf(count)));
		Assert.assertEquals("Expecting that the label caption is set to " + NO_LIST_DATA_RETURNED + " but didn't.l", NO_LIST_DATA_RETURNED,
				actualValue);

	}

	@Test
	public void testUpdateNoOfEntries_InInventoryViewAndCountHasValue() {
		Mockito.doReturn(ModeView.INVENTORY_VIEW).when(this.makeCrossesMain).getModeView();
		long count = 10L;

		this.selectParents.updateNoOfEntries(count);

		String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue("Expecting that the count is included in the caption of the total list entries label for list view but didn't.",
				actualValue.contains(String.valueOf(count)));
		Assert.assertTrue("Expecting that the label caption is set to " + TOTAL_LOTS + " but didn't.l", actualValue.startsWith(TOTAL_LOTS));
	}

	@Test
	public void testUpdateNoOfEntries_InListView() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		this.selectParents.initializeValues();
		long count = this.selectParents.getListDataTable().size();

		this.selectParents.updateNoOfEntries();
		String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue(
				"Expecting that the count included in the caption of the total list entries label is from the list data table but didn't.",
				actualValue.contains(String.valueOf(count)));
	}

	@Test
	public void testUpdateNoOfEntries_InInventoryView() {
		Mockito.doReturn(ModeView.INVENTORY_VIEW).when(this.makeCrossesMain).getModeView();
		Table table = new Table();
		table.addItem(ListInventoryDataInitializer.createLotDetail(1, 1));
		Mockito.doReturn(table).when(this.listInventoryTable).getTable();

		this.selectParents.setListInventoryTable(this.listInventoryTable);
		long count = this.selectParents.getListInventoryTable().getTable().size();
		this.selectParents.updateNoOfEntries();
		String actualValue = this.selectParents.getTotalListEntriesLabel().getValue().toString();
		Assert.assertTrue(
				"Expecting that the count included in the caption of the total lot entries label is from inventory table but didn't.",
				actualValue.contains(String.valueOf(count)));
	}
}

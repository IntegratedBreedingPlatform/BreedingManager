package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.AddedColumnsMapper;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.NewGermplasmEntriesFillColumnSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

public class DropHandlerMethodsTest {

	private static final int NO_OF_ENTRIES_SELECTED = 2;

	private static final int NO_OF_ENTRIES = 5;

	private static final int GERMPLASM_LIST_ID = 1;

	private static final int GROUP_ID = 1;

	private static final String GERMPLASM_NAME = "Germplasm Name";

	private static final String PREFERRED_NAME = "Preferred Germplasm Name";

	private static final String PARENTAGE = "A/B";

	private static final String TOTAL = "-";

	private static final String STOCK_ID = "STOCK";

	private static final String SEED_SOURCE = "Source Table Entry:";

	private static final String CROSS_EXPANSION = "Cross Name-";

	private final Integer GID = 1;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private ListManagerMain listManagerMain;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private GermplasmListNewColumnsInfo currentColumnsInfo;

	@Mock
	private ListBuilderComponent listBuilderComponent;

	@Mock
	private TableWithSelectAllLayout tableWithSelectAllLayout;

	@Mock
	private ListComponent listComponent;
	
	@Mock
	private NewGermplasmEntriesFillColumnSource newEntriesFillSource;
	
	@Mock
	private AddedColumnsMapper addedColumnsMapper;

	private final GermplasmListNewColumnsInfo germplasmListNewColumnsInfo = new GermplasmListNewColumnsInfo(1);

	@InjectMocks
	private DropHandlerMethods dropHandlerMethods;

	// Data Initializer

	private Table targetTable;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);

		this.dropHandlerMethods.setPedigreeService(this.pedigreeService);
		this.dropHandlerMethods.setCrossExpansionProperties(this.crossExpansionProperties);
		this.dropHandlerMethods.setInventoryDataManager(this.inventoryDataManager);
		this.dropHandlerMethods.setCurrentColumnsInfo(this.currentColumnsInfo);

		Mockito.doReturn(DropHandlerMethodsTest.SEED_SOURCE).when(this.germplasmDataManager).getPlotCodeValue(Matchers.anyInt());
		Mockito.doReturn(this.listBuilderComponent).when(this.listManagerMain).getListBuilderComponent();

		this.targetTable = this.createListDataTable();
		this.dropHandlerMethods.setTargetTable(this.targetTable);
	}

	@Test
	public void testHasSelectedItems() {
		// prepare table without selection
		final Table tableWithoutSelectedItem = this.prepareSourceTable(false);
		Assert.assertFalse("Returns false if the table has selected items",
				this.dropHandlerMethods.hasSelectedItems(tableWithoutSelectedItem));

		// prepare table with selection
		final Table tableWithSelectedItem = this.prepareSourceTable(true);
		Assert.assertTrue("Returns true if the table has selected items", this.dropHandlerMethods.hasSelectedItems(tableWithSelectedItem));
	}

	private Table prepareSourceTable(final boolean hasSelectedValue) {
		final Table table = new Table();
		table.setMultiSelect(true);

		// init items
		final List<Integer> items = this.prepareItemIds();
		for (final Integer itemId : items) {
			table.addItem(itemId);
		}

		if (hasSelectedValue) {
			// select items from the table
			table.setValue(items.subList(0, DropHandlerMethodsTest.NO_OF_ENTRIES_SELECTED));
		}

		return table;
	}

	private List<Integer> prepareItemIds() {
		final List<Integer> items = new ArrayList<Integer>();
		for (int i = 1; i <= DropHandlerMethodsTest.NO_OF_ENTRIES; i++) {
			items.add(i);
		}
		return items;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddSelectedGermplasmListsFromTable() {
		// initialize table data
		final Table sourceTable = this.prepareSourceTable(true);
		final Collection<Integer> selectedItems = (Collection<Integer>) sourceTable.getValue();
		final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		for (final Integer listId : selectedItems) {
			final GermplasmList germplasmList = GermplasmListTestDataInitializer
					.createGermplasmListWithListDataAndInventoryInfo(listId, DropHandlerMethodsTest.NO_OF_ENTRIES);
			Mockito.doReturn(germplasmList).when(this.germplasmListManager).getGermplasmListById(listId);
			germplasmLists.add(germplasmList);
			Mockito.doReturn(this.currentColumnsInfo).when(this.germplasmListManager).getAdditionalColumnsForList(listId);
		}

		List<GermplasmListData> listData1 = Lists.newArrayList(germplasmLists.get(0).getListData().get(0));
		List<GermplasmListData> listData2 = Lists.newArrayList(germplasmLists.get(0).getListData().get(1));
		List<GermplasmListData> listData3 = Lists.newArrayList(germplasmLists.get(0).getListData().get(2));
		List<GermplasmListData> listData4 = Lists.newArrayList(germplasmLists.get(0).getListData().get(3));
		List<GermplasmListData> listData5 = Lists.newArrayList(germplasmLists.get(0).getListData().get(4));

		Mockito.when(this.inventoryDataManager.getLotCountsForListEntries(Mockito.anyInt(), ArgumentMatchers.<List<Integer>>any())).thenReturn(listData1,
				listData2, listData3, listData4, listData5, listData1,
				listData2, listData3, listData4, listData5);

		this.dropHandlerMethods.addSelectedGermplasmListsFromTable(sourceTable);

		this.verifyGermplasmListDataFromSourceListsIsTransferredProperly(germplasmLists);
	}

	@Test
	public void testAddGermplasm() {
		// Setup test data
		final List<Integer> gidList = Arrays.asList(this.GID);
		final List<Germplasm> germplasmList = new ArrayList<>();
		this.prepareGermplasmPerGid(this.GID, germplasmList);

		// Setup mocks
		Mockito.doReturn(germplasmList).when(this.germplasmDataManager).getGermplasms(Matchers.anyListOf(Integer.class));
		Mockito.doReturn(this.getTestCrossExpansions(gidList)).when(this.pedigreeService)
				.getCrossExpansions(new HashSet<>(gidList), null, this.crossExpansionProperties);
		Mockito.doReturn(this.getPreferredNames(gidList)).when(this.germplasmDataManager).getPreferredNamesByGids(gidList);

		// call method to add germplasm to target table
		this.dropHandlerMethods.addGermplasm(gidList);

		// Verify bulk call to Middleware methods
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getGermplasms(Matchers.anyListOf(Integer.class));
		Mockito.verify(this.pedigreeService, Mockito.times(1))
				.getCrossExpansions(Matchers.anySetOf(Integer.class), Matchers.anyInt(), Matchers.any(CrossExpansionProperties.class));
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getPreferredNamesByGids(Matchers.anyListOf(Integer.class));

		this.verifyEachPropertyIsProperlyFilledUpForAddedGermplasm(gidList);
	}

	private void prepareGermplasmPerGid(final Integer gid, final List<Germplasm> germplasmList) {
		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(gid);
		germplasm.setMgid(gid);
		germplasmList.add(germplasm);
	}

	@Test
	public void testAddSelectedGermplasmsFromTable() {
		// Create 5 germplasm records but only select first three items
		final Table sourceTbl = this.createListDataTable();
		final List<Germplasm> germplasmList = new ArrayList<>();
		for (int i = 1; i <= DropHandlerMethodsTest.NO_OF_ENTRIES; i++) {
			this.prepareGermplasmPerGid(i, germplasmList);
			this.addItemToTestTable(sourceTbl, i);
		}
		final List<Integer> selectedIDs = Arrays.asList(1, 2, 3);
		sourceTbl.setValue(selectedIDs);

		// set mocks
		Mockito.doReturn(germplasmList).when(this.germplasmDataManager).getGermplasms(selectedIDs);
		Mockito.doReturn(this.getTestCrossExpansions(selectedIDs)).when(this.pedigreeService)
				.getCrossExpansions(new HashSet<>(selectedIDs), null, this.crossExpansionProperties);
		Mockito.doReturn(this.getPreferredNames(selectedIDs)).when(this.germplasmDataManager).getPreferredNamesByGids(selectedIDs);

		// call method to add germplasm to target table
		this.dropHandlerMethods.addSelectedGermplasmsFromTable(sourceTbl);

		// Verify bulk call to Middleware methods
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getGermplasms(Matchers.anyListOf(Integer.class));
		Mockito.verify(this.pedigreeService, Mockito.times(1))
				.getCrossExpansions(Matchers.anySetOf(Integer.class), Matchers.anyInt(), Matchers.any(CrossExpansionProperties.class));
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getPreferredNamesByGids(Matchers.anyListOf(Integer.class));

		this.verifyEachPropertyIsProperlyFilledUpForAddedGermplasm(selectedIDs);
	}

	private void verifyEachPropertyIsProperlyFilledUpForAddedGermplasm(final List<Integer> expectedIDs) {
		Assert.assertTrue(this.targetTable.size() == expectedIDs.size());
		final Iterator<Integer> expectedIDsIterator = expectedIDs.iterator();

		// Check values of target table entries
		for (final Object id : this.targetTable.getItemIds()) {
			final Integer expectedID = expectedIDsIterator.next();
			final Item tableItem = this.targetTable.getItem(id);
			Assert.assertEquals(expectedID, id);
			Assert.assertEquals(expectedID.toString(), tableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
			Assert.assertEquals(expectedID, tableItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
			Assert.assertEquals(DropHandlerMethodsTest.SEED_SOURCE,
					tableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
			// parentage value is from cross expansion string
			Assert.assertEquals(DropHandlerMethodsTest.CROSS_EXPANSION + expectedID,
					tableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
			Assert.assertEquals(expectedID.toString(), tableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
			Assert.assertEquals(DropHandlerMethodsTest.TOTAL,
					((Button)tableItem.getItemProperty(ColumnLabels.TOTAL.getName()).getValue()).getCaption());

			final Button gidButton = (Button) tableItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
			Assert.assertEquals(expectedID.toString(), gidButton.getCaption());
			// designation should be preferred name of germplasm
			final Button desigButton = (Button) tableItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
			Assert.assertEquals(DropHandlerMethodsTest.PREFERRED_NAME + expectedID.toString(), desigButton.getCaption());
		}
	}

	private void verifyGermplasmListDataFromListDataTableIsTransferredProperly(final List<Integer> expectedIDs, final Table sourceTable) {
		Assert.assertTrue(this.targetTable.size() == expectedIDs.size());
		final Iterator<Integer> expectedIDsIterator = expectedIDs.iterator();

		// Check values of target table entries
		for (final Object id : this.targetTable.getItemIds()) {
			final Integer expectedID = expectedIDsIterator.next();
			final Item targetTableItem = this.targetTable.getItem(id);
			final Item sourceTableItem = sourceTable.getItem(id);

			Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue(),
					targetTableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
			Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue(),
					targetTableItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
			Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue(),
					targetTableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
			Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue(),
					targetTableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
			Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue(),
					targetTableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
			Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue(),
					targetTableItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());
			final Button gidButton = (Button) targetTableItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
			Assert.assertEquals(expectedID.toString(), gidButton.getCaption());
			// designation should be preferred name of germplasm
			final Button targetDesigButton = (Button) targetTableItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
			final Button sourceDesigButton = (Button) sourceTableItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
			Assert.assertEquals(sourceDesigButton.getCaption(), targetDesigButton.getCaption());
		}
	}

	@Test
	public void testAddGermplasmFromList() {

		final Map<String, List<ListDataColumnValues>> map = new HashMap<>();
		final ListDataColumnValues ldcv = new ListDataColumnValues("GID", 1, "1");
		map.put("GID", Lists.newArrayList(ldcv));
		this.germplasmListNewColumnsInfo.setColumnValuesMap(map);
		this.dropHandlerMethods.setCurrentColumnsInfo(this.germplasmListNewColumnsInfo);

		final GermplasmList testList = GermplasmListTestDataInitializer
				.createGermplasmListWithListDataAndInventoryInfo(DropHandlerMethodsTest.GERMPLASM_LIST_ID,
						DropHandlerMethodsTest.NO_OF_ENTRIES);

		// retrieve the first list entry from list data with inventory information
		final GermplasmListData listData = testList.getListData().get(0);
		// MGID or group ID of Germplasm List Data has default value to 0, so this field will never be null
		listData.setGroupId(DropHandlerMethodsTest.GROUP_ID);

		Mockito.doReturn(testList.getListData()).when(this.inventoryDataManager).getLotCountsForListEntries(Mockito.anyInt(), Mockito.anyListOf(Integer.class));

		this.dropHandlerMethods.addGermplasmFromList(DropHandlerMethodsTest.GERMPLASM_LIST_ID, listData.getId(), testList, false);

		// Verify that new table item was added with expected values from list data object
		Assert.assertTrue(this.targetTable.size() == 1);
		final Item tableItem = this.targetTable.getItem(this.targetTable.firstItemId());
		Assert.assertEquals(listData.getEntryCode(), tableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
		Assert.assertEquals(listData.getSeedSource(), tableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
		Assert.assertEquals(listData.getGroupName(), tableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
		Assert.assertEquals(listData.getGroupId().toString(), tableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
		final Button desigButton = (Button) tableItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
		Assert.assertEquals(listData.getDesignation(), desigButton.getCaption());
		final Button gidButton = (Button) tableItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
		Assert.assertEquals(listData.getGid().toString(), gidButton.getCaption());

		final Label stockIdLabel = (Label) tableItem.getItemProperty(ColumnLabels.STOCKID.getName()).getValue();
		Assert.assertEquals(listData.getInventoryInfo().getStockIDs(), stockIdLabel.getValue().toString());
	}

	@Test
	public void testAddGermplasmListUsingListId() {
		final GermplasmList germplasmList = GermplasmListTestDataInitializer
				.createGermplasmListWithListDataAndInventoryInfo(DropHandlerMethodsTest.GERMPLASM_LIST_ID,
						DropHandlerMethodsTest.NO_OF_ENTRIES);
		Mockito.doReturn(germplasmList).when(this.germplasmListManager).getGermplasmListById(DropHandlerMethodsTest.GERMPLASM_LIST_ID);

		Mockito.doReturn(DropHandlerMethodsTest.GERMPLASM_LIST_ID).when(this.currentColumnsInfo).getListId();
		Mockito.doReturn(new HashMap<>()).when(this.currentColumnsInfo).getColumnValuesMap();

		List<GermplasmListData> listData1 = Lists.newArrayList(germplasmList.getListData().get(0));
		List<GermplasmListData> listData2 = Lists.newArrayList(germplasmList.getListData().get(1));
		List<GermplasmListData> listData3 = Lists.newArrayList(germplasmList.getListData().get(2));
		List<GermplasmListData> listData4 = Lists.newArrayList(germplasmList.getListData().get(3));
		List<GermplasmListData> listData5 = Lists.newArrayList(germplasmList.getListData().get(4));

		Mockito.when(this.inventoryDataManager.getLotCountsForListEntries(Mockito.anyInt(), ArgumentMatchers.<List<Integer>>any())).thenReturn(listData1,
				listData2, listData3, listData4, listData5);

		this.dropHandlerMethods.addGermplasmList(DropHandlerMethodsTest.GERMPLASM_LIST_ID);

		this.verifyGermplasmListDataFromListIsTransferredProperly(germplasmList);
	}

	@Test
	public void testAddFromListDataTable() {
		// Create 5 germplasm records but only select first three items
		final Table sourceTbl = this.createListDataTable();
		final List<Germplasm> germplasmList = new ArrayList<>();
		for (int i = 1; i <= DropHandlerMethodsTest.NO_OF_ENTRIES; i++) {
			this.prepareGermplasmPerGid(i, germplasmList);
			this.addItemToTestTable(sourceTbl, i);
		}
		sourceTbl.setParent(this.tableWithSelectAllLayout);
		final List<Integer> selectedIDs = Arrays.asList(1, 2, 3);
		sourceTbl.setValue(selectedIDs);

		// Setup mocks
		Mockito.doReturn(this.listComponent).when(this.tableWithSelectAllLayout).getParent();
		Mockito.doReturn(DropHandlerMethodsTest.GERMPLASM_LIST_ID).when(this.listComponent).getGermplasmListId();
		Mockito.doReturn(DropHandlerMethodsTest.GERMPLASM_LIST_ID).when(this.currentColumnsInfo).getListId();
		Mockito.doReturn(new HashMap<>()).when(this.currentColumnsInfo).getColumnValuesMap();

		for (final Integer itemId : selectedIDs) {
			Mockito.doReturn(this.currentColumnsInfo).when(this.germplasmListManager).getAdditionalColumnsForList(itemId);
		}

		this.dropHandlerMethods.addFromListDataTable(sourceTbl);

		Mockito.verify(germplasmDataManager, Mockito.times(1)).getPreferredNamesByGids(Mockito.anyListOf(Integer.class));
		this.verifyGermplasmListDataFromListDataTableIsTransferredProperly(selectedIDs, sourceTbl);

	}

	@Test
	public void testAddFromListDataTableUsePreferredNameInDesignationIfAvailable() {

		final Integer gid = 1;
		final String preferredName = "BST1111";

		final Table sourceTable = this.createListDataTable();
		final List<Germplasm> germplasmList = new ArrayList<>();

		// Add one list entry to the table
		this.prepareGermplasmPerGid(gid, germplasmList);
		this.addItemToTestTable(sourceTable, gid);

		sourceTable.setParent(this.tableWithSelectAllLayout);

		// Select the first item in the table
		final List<Integer> selectedIDs = Arrays.asList(gid);
		sourceTable.setValue(selectedIDs);

		// Setup mocks
		Mockito.doReturn(this.listComponent).when(this.tableWithSelectAllLayout).getParent();
		Mockito.doReturn(DropHandlerMethodsTest.GERMPLASM_LIST_ID).when(this.listComponent).getGermplasmListId();
		Mockito.doReturn(DropHandlerMethodsTest.GERMPLASM_LIST_ID).when(this.currentColumnsInfo).getListId();
		Mockito.doReturn(new HashMap<>()).when(this.currentColumnsInfo).getColumnValuesMap();


		// Create preferred name map for gid in the table
		final Map<Integer, String> preferredNames = new HashMap<>();
		preferredNames.put(gid, preferredName);
		Mockito.doReturn(preferredNames).when(germplasmDataManager).getPreferredNamesByGids(Mockito.anyListOf(Integer.class));

		for (final Integer itemId : selectedIDs) {
			Mockito.doReturn(this.currentColumnsInfo).when(this.germplasmListManager).getAdditionalColumnsForList(itemId);
		}

		this.dropHandlerMethods.addFromListDataTable(sourceTable);

		Mockito.verify(germplasmDataManager, Mockito.times(1)).getPreferredNamesByGids(Mockito.anyListOf(Integer.class));

		final Item targetTableItem = this.targetTable.getItem(gid);
		final Item sourceTableItem = sourceTable.getItem(gid);

		Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue(),
				targetTableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
		Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue(),
				targetTableItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
		Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue(),
				targetTableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
		Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue(),
				targetTableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
		Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue(),
				targetTableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
		Assert.assertEquals(sourceTableItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue(),
				targetTableItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());
		final Button gidButton = (Button) targetTableItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
		Assert.assertEquals(String.valueOf(gid), gidButton.getCaption());

		// designation should be preferred name of germplasm
		final Button targetDesigButton = (Button) targetTableItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
		Assert.assertEquals(preferredName, targetDesigButton.getCaption());

	}

	@Test
	public void testExtractGidsFromTable() {

		final Table sourceTable = this.createListDataTable();
		final List<Germplasm> germplasmList = new ArrayList<>();

		for (int gid = 1; gid <= DropHandlerMethodsTest.NO_OF_ENTRIES; gid++) {
			this.prepareGermplasmPerGid(gid, germplasmList);
			this.addItemToTestTable(sourceTable, gid);
		}
		sourceTable.setParent(this.tableWithSelectAllLayout);
		final List<Integer> selectedItemIds = Arrays.asList(1, 2);
		sourceTable.setValue(selectedItemIds);

		final List<Integer> itemIds = this.dropHandlerMethods.getSelectedItemIds(sourceTable);

		List<Integer> gids = this.dropHandlerMethods.extractGidsFromTable(sourceTable, itemIds);

		Assert.assertEquals("Only 2 entries are selected so the gids extracted should only be 2", 2, gids.size());
		Assert.assertTrue("Gid 1 should be in the extracted gid list", gids.contains(1));
		Assert.assertTrue("Gid 2 should be in the extracted gid list", gids.contains(2));
	}
	
	@Test
	public void testGenerateAddedColumnValuesForAddedEntryWhenNoAddedColumns() {
		this.dropHandlerMethods.setAddedColumnsMapper(this.addedColumnsMapper);
		this.dropHandlerMethods.setNewEntriesFillSource(this.newEntriesFillSource);
		final List<Integer> selectedItems = new ArrayList<>();
		for (int i = 1; i <= NO_OF_ENTRIES_SELECTED; i++) {
			this.addItemToTestTable(this.targetTable, i);
			selectedItems.add(i);
		}
		this.dropHandlerMethods.generateAddedColumnValuesForAddedEntry(selectedItems, selectedItems);
		
		Mockito.verifyZeroInteractions(this.newEntriesFillSource);
		
	}
	
	@Test
	public void testGenerateAddedColumnValuesForAddedEntryWhenColumnsAdded() {
		this.dropHandlerMethods.setAddedColumnsMapper(this.addedColumnsMapper);
		this.dropHandlerMethods.setNewEntriesFillSource(this.newEntriesFillSource);
		final DropHandlerMethods spyHandler = Mockito.spy(this.dropHandlerMethods);
		Mockito.doReturn(true).when(spyHandler).targetTableHasAddedColumn();
		this.addColumnsToTable(this.targetTable, ColumnLabels.PREFERRED_NAME, ColumnLabels.GERMPLASM_DATE);
		final List<Integer> selectedItems = new ArrayList<>();
		for (int i = 1; i <= NO_OF_ENTRIES_SELECTED; i++) {
			this.addItemToTestTable(this.targetTable, i);
			selectedItems.add(i);
		}
		
		spyHandler.generateAddedColumnValuesForAddedEntry(selectedItems, selectedItems);
		
		Mockito.verify(this.newEntriesFillSource).setAddedGids(selectedItems);
		Mockito.verify(this.newEntriesFillSource).setAddedItemIds(selectedItems);
		Mockito.verify(this.addedColumnsMapper).generateValuesForAddedColumns(this.targetTable.getVisibleColumns());
	}
	
	@Test
	public void testTargetTableHasAddedColumn() {
		Assert.assertFalse(this.dropHandlerMethods.targetTableHasAddedColumn());
		
	}

	private void verifyGermplasmListDataFromListIsTransferredProperly(final GermplasmList germplasmList) {
		Assert.assertTrue(this.targetTable.size() == germplasmList.getListData().size());
		final Iterator<GermplasmListData> listDataIterator = germplasmList.getListData().iterator();
		for (final Object id : this.targetTable.getItemIds()) {
			final Item tableItem = this.targetTable.getItem(id);
			final GermplasmListData listData = listDataIterator.next();
			Assert.assertEquals(listData.getEntryCode(), tableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
			Assert.assertEquals(listData.getSeedSource(), tableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
			Assert.assertEquals(listData.getGroupName(), tableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
			Assert.assertEquals("-", tableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
		}

		// verify that the last event for this event is called properly after the adding of germplasm to build new list table
		Mockito.verify(this.listManagerMain, Mockito.atLeast(1)).showListBuilder();
	}

	private void verifyGermplasmListDataFromSourceListsIsTransferredProperly(final List<GermplasmList> germplasmLists) {
		int numberOfItems = 0;
		for (final GermplasmList germplasmList : germplasmLists) {
			numberOfItems += germplasmList.getListData().size();
		}
		// Check that the number of items in target table is the sum of count of list data in all lists
		Assert.assertEquals(numberOfItems, this.targetTable.size());

		// Check key columns in target table items were retrieved from source list items
		final Iterator<?> tableItemIterator = this.targetTable.getItemIds().iterator();
		for (final GermplasmList germplasmList : germplasmLists) {
			for (final GermplasmListData listData : germplasmList.getListData()) {
				final Item tableItem = this.targetTable.getItem(tableItemIterator.next());
				Assert.assertEquals(listData.getEntryCode(), tableItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
				Assert.assertEquals(listData.getSeedSource(), tableItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
				Assert.assertEquals(listData.getGroupName(), tableItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
				Assert.assertEquals("-", tableItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
			}
		}

		// verify that the last event for this event is called properly after the adding of germplasm to build new list table
		Mockito.verify(this.listManagerMain, Mockito.atLeast(1)).showListBuilder();
	}

	private Table createListDataTable() {
		final Table table = new Table();
		table.setMultiSelect(true);
		table.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		table.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		table.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		table.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		table.addContainerProperty(ColumnLabels.TOTAL.getName(), Button.class, null);
		table.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		table.addContainerProperty(ColumnLabels.GROUP_ID.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		table.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		return table;
	}
	
	private void addColumnsToTable(final Table table, final ColumnLabels...columnLabels) {
		for (ColumnLabels columnLabel : columnLabels) {
			table.addContainerProperty(columnLabel.getName(), String.class, null);
		}
	}

	private void addItemToTestTable(final Table table, final Integer itemId) {
		final Item item = table.getContainerDataSource().addItem(itemId);

		item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(itemId);
		item.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(itemId);
		item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(new Button(DropHandlerMethodsTest.GERMPLASM_NAME + itemId));
		item.getItemProperty(ColumnLabels.GID.getName()).setValue(new Button(itemId.toString()));
		item.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(DropHandlerMethodsTest.PARENTAGE);
		item.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(new Button(DropHandlerMethodsTest.TOTAL + itemId));
		item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(new Button("1"));
		item.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(DropHandlerMethodsTest.GROUP_ID);
		item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(DropHandlerMethodsTest.STOCK_ID + itemId);
		item.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(DropHandlerMethodsTest.SEED_SOURCE + itemId);
	}

	private Map<Integer, String> getTestCrossExpansions(final List<Integer> itemIDs) {
		final Map<Integer, String> crossExpansionmap = new HashMap<>();
		for (final Integer id : itemIDs) {
			crossExpansionmap.put(id, DropHandlerMethodsTest.CROSS_EXPANSION + id);
		}
		return crossExpansionmap;
	}

	private Map<Integer, String> getPreferredNames(final List<Integer> itemIDs) {
		final Map<Integer, String> crossExpansionmap = new HashMap<>();
		for (final Integer id : itemIDs) {
			crossExpansionmap.put(id, DropHandlerMethodsTest.PREFERRED_NAME + id);
		}
		return crossExpansionmap;
	}

}

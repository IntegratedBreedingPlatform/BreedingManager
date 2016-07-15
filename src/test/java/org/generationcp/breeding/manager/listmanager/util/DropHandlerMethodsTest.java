
package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
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
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

public class DropHandlerMethodsTest {

	private static final int NO_OF_ENTRIES_SELECTED = 2;

	private static final int NO_OF_ENTRIES = 5;

	private static final int GERMPLASM_LIST_ID = 1;

	private static final int GROUP_ID = 1;

	private final Integer GID = 1;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private Table targetTable;

	@Mock
	private Container mockContainer;

	@Mock
	private Item mockTableItem;

	@Mock
	private Property mockProperty;

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

	private final GermplasmListNewColumnsInfo germplasmListNewColumnsInfo = new GermplasmListNewColumnsInfo(1);
	
	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;
	
	@InjectMocks
	private DropHandlerMethods dropHandlerMethods;

	// Data Initializer
	private GermplasmTestDataInitializer germplasmInitializer;
	private GermplasmListTestDataInitializer germplasmListInitializer;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();

		this.mockContainer = Mockito.mock(Container.class);
		Mockito.when(this.targetTable.getContainerDataSource()).thenReturn(this.mockContainer);

		this.mockTableItem = Mockito.mock(Item.class);
		Mockito.when(this.mockContainer.addItem(Matchers.any())).thenReturn(this.mockTableItem);

		this.mockProperty = Mockito.mock(Property.class);
		Mockito.when(this.mockTableItem.getItemProperty(Matchers.anyString())).thenReturn(this.mockProperty);

		this.germplasmInitializer = new GermplasmTestDataInitializer();
		this.germplasmListInitializer = new GermplasmListTestDataInitializer();

		this.dropHandlerMethods.setPedigreeService(this.pedigreeService);
		this.dropHandlerMethods.setCrossExpansionProperties(this.crossExpansionProperties);
		this.dropHandlerMethods.setInventoryDataManager(this.inventoryDataManager);
		this.dropHandlerMethods.setCurrentColumnsInfo(this.currentColumnsInfo);

		Mockito.doReturn(this.listBuilderComponent).when(this.listManagerMain).getListBuilderComponent();

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
			table.setValue(items.subList(0, NO_OF_ENTRIES_SELECTED));
		}

		return table;
	}

	private List<Integer> prepareItemIds() {
		final List<Integer> items = new ArrayList<Integer>();
		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
			items.add(i);
		}
		return items;
	}

	@Test
	public void testAddSelectedGermplasmListsFromTable() {
		// initialize table data
		final Table sourceTable = this.prepareSourceTable(true);
		final Collection<Integer> selectedItems = (Collection<Integer>) sourceTable.getValue();
		final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		for (final Integer listId : selectedItems) {
			final GermplasmList germplasmList =
					this.germplasmListInitializer.createGermplasmListWithListDataAndInventoryInfo(listId, NO_OF_ENTRIES);
			Mockito.doReturn(germplasmList).when(this.germplasmListManager).getGermplasmListById(listId);
			germplasmLists.add(germplasmList);
			Mockito.doReturn(this.currentColumnsInfo).when(this.germplasmListManager).getAdditionalColumnsForList(listId);
		}

		this.dropHandlerMethods.addSelectedGermplasmListsFromTable(sourceTable);

		for (final GermplasmList germplasmList : germplasmLists) {
			this.verifyGermplasmListDataFromListIsTransferredProperly(germplasmList);
		}
	}

	@Test
	public void testAddGermplasm() {
		this.prepareGermplasmPerGid(this.GID);
		this.dropHandlerMethods.addGermplasm(this.GID);
		this.verifyEachPropertyIsProperlyFilledUp();
	}

	private void prepareGermplasmPerGid(final Integer gid) {
		final Germplasm germplasm = this.germplasmInitializer.createGermplasm(gid);
		germplasm.setMgid(1);
		Mockito.doReturn(germplasm).when(this.germplasmDataManager).getGermplasmByGID(gid);
	}

	@Test
	public void testAddSelectedGermplasmsFromTable() {

		final List<Integer> itemIds = this.prepareItemIds();
		Mockito.doReturn(itemIds).when(this.targetTable).getValue();
		Mockito.doReturn(itemIds).when(this.targetTable).getItemIds();

		for (final Integer itemId : itemIds) {
			Mockito.doReturn(this.mockTableItem).when(this.targetTable).getItem(itemId);
			this.prepareGermplasmPerGid(itemId);
		}

		this.dropHandlerMethods.addSelectedGermplasmsFromTable(this.targetTable);

		this.verifyEachPropertyIsProperlyFilledUp();
	}

	private void verifyEachPropertyIsProperlyFilledUp() {
		// Verify if that each property in item is properly filled up
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.GROUP_ID.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.SEED_RESERVATION.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.DESIGNATION.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.PARENTAGE.getName());
	}

	@Test
	public void testAddGermplasmFromList() {

		final Map<String, List<ListDataColumnValues>> map = new HashMap<>();
		final ListDataColumnValues ldcv = new ListDataColumnValues("GID", 1, "1");
		map.put("GID", Lists.newArrayList(ldcv));
		this.germplasmListNewColumnsInfo.setColumnValuesMap(map);
		this.dropHandlerMethods.setCurrentColumnsInfo(this.germplasmListNewColumnsInfo);

		final GermplasmList testList =
				this.germplasmListTestDataInitializer.createGermplasmListWithListDataAndInventoryInfo(GERMPLASM_LIST_ID, NO_OF_ENTRIES);

		// retrieve the first list entry from list data with inventory information
		final GermplasmListData listData = testList.getListData().get(0);
		// MGID or group ID of Germplasm List Data has default value to 0, so this field will never be null
		listData.setGroupId(GROUP_ID);

		this.dropHandlerMethods.addGermplasmFromList(GERMPLASM_LIST_ID, listData.getId(), testList, false);

		// verify if the list data fields are properly retrieved
		Mockito.verify(this.mockProperty).setValue(listData.getEntryCode());
		Mockito.verify(this.mockProperty).setValue(listData.getSeedSource());
		Mockito.verify(this.mockProperty).setValue(listData.getGroupName());
		// Others (e.g. gid and designation) are added as buttons so hard to verify from outside the class in this test harness.
	}

	@Test
	public void testAddGermplasmListUsingListId() {
		final GermplasmList germplasmList =
				this.germplasmListInitializer.createGermplasmListWithListDataAndInventoryInfo(GERMPLASM_LIST_ID, NO_OF_ENTRIES);
		Mockito.doReturn(germplasmList).when(this.germplasmListManager).getGermplasmListById(GERMPLASM_LIST_ID);

		Mockito.doReturn(GERMPLASM_LIST_ID).when(this.currentColumnsInfo).getListId();
		Mockito.doReturn(new HashMap<>()).when(this.currentColumnsInfo).getColumnValuesMap();

		this.dropHandlerMethods.addGermplasmList(GERMPLASM_LIST_ID);

		this.verifyGermplasmListDataFromListIsTransferredProperly(germplasmList);
	}

	@Test
	public void testAddFromListDataTable() {
		Mockito.doReturn(this.tableWithSelectAllLayout).when(this.targetTable).getParent();
		Mockito.doReturn(this.listComponent).when(this.tableWithSelectAllLayout).getParent();
		Mockito.doReturn(GERMPLASM_LIST_ID).when(this.listComponent).getGermplasmListId();
		Mockito.doReturn(GERMPLASM_LIST_ID).when(this.currentColumnsInfo).getListId();
		Mockito.doReturn(new HashMap<>()).when(this.currentColumnsInfo).getColumnValuesMap();

		final List<Integer> itemIds = this.prepareItemIds();
		Mockito.doReturn(itemIds).when(this.targetTable).getValue();
		Mockito.doReturn(itemIds).when(this.targetTable).getItemIds();

		for (final Integer itemId : itemIds) {
			Mockito.doReturn(this.mockTableItem).when(this.targetTable).getItem(itemId);
			Mockito.doReturn(this.currentColumnsInfo).when(this.germplasmListManager).getAdditionalColumnsForList(itemId);
		}

		// Initialize specific properties per item

		// for GID
		final Property gidProp = Mockito.mock(Property.class);
		Mockito.doReturn(gidProp).when(this.mockTableItem).getItemProperty(ColumnLabels.GID.getName());
		Mockito.doReturn(new Button("1")).when(gidProp).getValue();

		// for DESIGNATION
		final Property designationProp = Mockito.mock(Property.class);
		Mockito.doReturn(designationProp).when(this.mockTableItem).getItemProperty(ColumnLabels.DESIGNATION.getName());
		Mockito.doReturn(new Button("Germplasm Name")).when(designationProp).getValue();

		// for AVAILABLE INVENTORY
		final Property availInvProp = Mockito.mock(Property.class);
		Mockito.doReturn(availInvProp).when(this.mockTableItem).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
		Mockito.doReturn(new Button("1")).when(availInvProp).getValue();

		this.dropHandlerMethods.addFromListDataTable(this.targetTable);

		this.verifyEachPropertyIsProperlyFilledUp();

	}

	private void verifyGermplasmListDataFromListIsTransferredProperly(final GermplasmList germplasmList) {
		final GermplasmListData listData = germplasmList.getListData().get(0);
		// verify if the list data fields are properly retrieved
		Mockito.verify(this.mockProperty, Mockito.atLeast(1)).setValue(listData.getEntryCode());
		Mockito.verify(this.mockProperty, Mockito.atLeast(1)).setValue(listData.getSeedSource());
		Mockito.verify(this.mockProperty, Mockito.atLeast(1)).setValue(listData.getGroupName());

		// verify that the last event for this event is called properly after the adding of germplasm to build new list table
		Mockito.verify(this.listManagerMain, Mockito.atLeast(1)).showListBuilder();
	}

}

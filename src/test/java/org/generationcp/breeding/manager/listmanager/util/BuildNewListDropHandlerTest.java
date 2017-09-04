
package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;

public class BuildNewListDropHandlerTest {
	
	private static final String[] STANDARD_COLUMNS =
		{ColumnLabels.GID.getName(), ColumnLabels.DESIGNATION.getName(), ColumnLabels.SEED_SOURCE.getName(),
				ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.GROUP_ID.getName(), ColumnLabels.STOCKID.getName()};

	private static final int NO_OF_ENTRIES = 5;
	private static final int GID1 = 1;
	private static final int GID2 = 2;
	private static final int GERMPLASM_LIST_ID = 1;
	@Mock
	private ListManagerMain listManagerMain;
	@Mock
	private ListBuilderComponent listBuilderComponent;
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private InventoryDataManager inventoryDataManager;
	@Mock
	private PedigreeService pedigreeService;
	@Mock
	private CrossExpansionProperties crossExpansionProperties;
	@Mock
	private Table targetTable;
	@Mock
	private PlatformTransactionManager transactionManager;
	@Mock
	private DragAndDropEvent event;
	@Mock
	private Transferable transferable;
	@Mock
	private TableTransferable tableTransferable;
	@Mock
	private GermplasmListNewColumnsInfo currentColumnsInfo;
	@Mock
	private Container mockContainer;
	@Mock
	private Item mockTableItem;
	@Mock
	private Property mockProperty;
	@Mock
	private Table sourceTable;
	@Mock
	private AbstractSelectTargetDetails targetDetails;
	@Mock
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	@Mock
	private ListComponent listComponent;

	private BuildNewListDropHandler dropHandler;

	private final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		// other mocks
		Mockito.when(this.targetTable.getContainerDataSource()).thenReturn(this.mockContainer);
		Mockito.when(this.mockContainer.addItem(Matchers.any())).thenReturn(this.mockTableItem);
		Mockito.when(this.mockTableItem.getItemProperty(Matchers.anyString())).thenReturn(this.mockProperty);
		Mockito.doReturn(BuildNewListDropHandlerTest.GERMPLASM_LIST_ID).when(this.currentColumnsInfo).getListId();
		Mockito.doReturn(new HashMap<>()).when(this.currentColumnsInfo).getColumnValuesMap();
		Mockito.doReturn(this.listBuilderComponent).when(this.listManagerMain).getListBuilderComponent();

		// drop related mocks
		Mockito.doReturn(this.tableTransferable).when(this.event).getTransferable();
		Mockito.doReturn(this.sourceTable).when(this.tableTransferable).getSourceComponent();
		Mockito.doReturn(this.targetDetails).when(this.event).getTargetDetails();
		Mockito.doReturn(this.targetTable).when(this.targetDetails).getTarget();
		Mockito.doReturn(STANDARD_COLUMNS).when(this.targetTable).getVisibleColumns();

		final List<Integer> itemIds = this.prepareItemIds();
		Mockito.doReturn(itemIds).when(this.sourceTable).getValue();
		Mockito.doReturn(itemIds).when(this.sourceTable).getItemIds();

		final List<Germplasm> germplasms = new ArrayList<>();
		for (final Integer itemId : itemIds) {
			final Item item = Mockito.mock(Item.class);
			Mockito.doReturn(item).when(this.sourceTable).getItem(itemId);

			// Initialize specific properties per item
			// for GID_REF
			final Property gidRefProp = Mockito.mock(Property.class);
			Mockito.doReturn(gidRefProp).when(item).getItemProperty(ColumnLabels.GID.getName() + "_REF");
			Mockito.doReturn(itemId).when(gidRefProp).getValue();
			// for GID
			final Property gidProp = Mockito.mock(Property.class);
			Mockito.doReturn(gidProp).when(item).getItemProperty(ColumnLabels.GID.getName());
			Mockito.doReturn(new Button(String.valueOf(itemId))).when(gidProp).getValue();
			// for DESIGNATION
			final Property designationProp = Mockito.mock(Property.class);
			Mockito.doReturn(designationProp).when(item).getItemProperty(ColumnLabels.DESIGNATION.getName());
			Mockito.doReturn(new Button("Germplasm Name")).when(designationProp).getValue();
			// for AVAILABLE INVENTORY
			final Property availInvProp = Mockito.mock(Property.class);
			Mockito.doReturn(availInvProp).when(item).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
			Mockito.doReturn(new Button(String.valueOf(itemId))).when(availInvProp).getValue();
			// for AVAILABLE Balance
			final Property availableProp = Mockito.mock(Property.class);
			Mockito.doReturn(availInvProp).when(item).getItemProperty(ColumnLabels.TOTAL.getName());
			Mockito.doReturn(new Button(String.valueOf(itemId))).when(availableProp).getValue();
			// for PARENTAGE
			final Property parentageProp = Mockito.mock(Property.class);
			Mockito.doReturn(parentageProp).when(item).getItemProperty(ColumnLabels.PARENTAGE.getName());
			Mockito.doReturn("Parent of GID: " + itemId).when(parentageProp).getValue();
			// for ENTRY_CODE
			final Property entryCodeProp = Mockito.mock(Property.class);
			Mockito.doReturn(entryCodeProp).when(item).getItemProperty(ColumnLabels.ENTRY_CODE.getName());
			Mockito.doReturn("Entry code of GID: " + itemId).when(entryCodeProp).getValue();
			// for SEED_SOURCE
			final Property seedSourceProp = Mockito.mock(Property.class);
			Mockito.doReturn(seedSourceProp).when(item).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
			Mockito.doReturn("Seed source of GID: " + itemId).when(seedSourceProp).getValue();
			// for GROUP_ID
			final Property groupIdProp = Mockito.mock(Property.class);
			Mockito.doReturn(groupIdProp).when(item).getItemProperty(ColumnLabels.GROUP_ID.getName());
			Mockito.doReturn("Seed source of GID: " + itemId).when(groupIdProp).getValue();

			final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(itemId);
			Mockito.doReturn(germplasm).when(this.germplasmDataManager).getGermplasmByGID(itemId);
			germplasms.add(germplasm);

			final GermplasmList germplasmList = GermplasmListTestDataInitializer.createGermplasmListWithListDataAndInventoryInfo(itemId,
					BuildNewListDropHandlerTest.NO_OF_ENTRIES);
			Mockito.doReturn(germplasmList).when(this.germplasmListManager).getGermplasmListById(itemId);

			this.germplasmLists.add(germplasmList);
			Mockito.doReturn(this.currentColumnsInfo).when(this.germplasmListManager).getAdditionalColumnsForList(itemId);

		}
		Mockito.doReturn(germplasms).when(this.germplasmDataManager).getGermplasms(Matchers.anyListOf(Integer.class));

		final GermplasmList germplasmList = GermplasmListTestDataInitializer.createGermplasmListWithListDataAndInventoryInfo(1, 1);
		List<GermplasmListData> listData = germplasmList.getListData();

		Mockito.doReturn(listData).when(this.inventoryDataManager).getLotCountsForListEntries(Mockito.anyInt(), Mockito.anyList());

		this.dropHandler = new BuildNewListDropHandler(this.listManagerMain, this.germplasmDataManager, this.germplasmListManager,
				this.inventoryDataManager, this.pedigreeService, this.crossExpansionProperties, this.targetTable, this.transactionManager);

		// other mock injections
		this.dropHandler.setCurrentColumnsInfo(this.currentColumnsInfo);

	}

	@Test
	public void testDropWhenTheEventIsNotATableTransferable() {
		Mockito.doReturn(this.transferable).when(this.event).getTransferable();
		Mockito.doReturn(BuildNewListDropHandlerTest.GERMPLASM_LIST_ID).when(this.transferable).getData("itemId");

		this.dropHandler.drop(this.event);

		// retrieval of any further information about TableTransferable will not be called at all
		Mockito.verify(this.transferable, Mockito.times(0)).getSourceComponent();
		Mockito.verify(this.event, Mockito.times(0)).getTargetDetails();
	}

	@Test
	public void testDropWhenTheEventIsATableTransferableAndComesFromGermplasmSearchTable() {
		Mockito.doReturn(DropHandlerMethods.MATCHING_GERMPLASMS_TABLE_DATA).when(this.sourceTable).getData();

		this.dropHandler.drop(this.event);

		// retrieval of further information about TableTransferable will be called
		Mockito.verify(this.tableTransferable, Mockito.times(1)).getSourceComponent();
		Mockito.verify(this.event, Mockito.times(1)).getTargetDetails();
		// verify that the retrieval of each column to fill after the drop are properly called
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.GROUP_ID.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.TOTAL.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.DESIGNATION.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.PARENTAGE.getName());
	}

	@Test
	public void testDropWhenTheEventIsATableTransferableAndComesFromListSearchTable() {

		Mockito.doReturn(DropHandlerMethods.MATCHING_LISTS_TABLE_DATA).when(this.sourceTable).getData();

		this.dropHandler.drop(this.event);

		// retrieval of further information about TableTransferable will be called
		Mockito.verify(this.tableTransferable, Mockito.times(1)).getSourceComponent();
		Mockito.verify(this.event, Mockito.times(1)).getTargetDetails();

		// verify each list selected from the list table is transferred to the target table
		for (final GermplasmList germplasmList : this.germplasmLists) {
			this.verifyGermplasmListDataFromListIsTransferredProperly(germplasmList);
		}

		// verify that the retrieval of each column to fill after the drop are properly called
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.GROUP_ID.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.TOTAL.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.DESIGNATION.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.PARENTAGE.getName());
	}

	@Test
	public void testDropWhenTheEventIsATableTransferableAndComesFromListDataTable() {

		Mockito.doReturn(this.tableWithSelectAllLayout).when(this.sourceTable).getParent();
		Mockito.doReturn(this.listComponent).when(this.tableWithSelectAllLayout).getParent();
		Mockito.doReturn(BuildNewListDropHandlerTest.GERMPLASM_LIST_ID).when(this.listComponent).getGermplasmListId();
		Mockito.doReturn(BuildNewListDropHandlerTest.GERMPLASM_LIST_ID).when(this.currentColumnsInfo).getListId();

		Mockito.doReturn(DropHandlerMethods.LIST_DATA_TABLE_DATA).when(this.sourceTable).getData();

		this.dropHandler.drop(this.event);

		// retrieval of further information about TableTransferable will be called
		Mockito.verify(this.tableTransferable, Mockito.times(1)).getSourceComponent();
		Mockito.verify(this.event, Mockito.times(1)).getTargetDetails();

		// verify that the retrieval of each column to fill after the drop are properly called
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.GROUP_ID.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.TOTAL.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.DESIGNATION.getName());
		Mockito.verify(this.mockTableItem, Mockito.atLeast(1)).getItemProperty(ColumnLabels.PARENTAGE.getName());

	}

	@Test
	public void testDropWhenTheEventIsATableTransferableAndComesFromListBuilderGermplasmTable() {

		Mockito.doReturn(BuildNewListDropHandlerTest.GID1).when(this.targetDetails).getItemIdOver();
		Mockito.doReturn(BuildNewListDropHandlerTest.GID2).when(this.tableTransferable).getItemId();
		final Item oldItem = Mockito.mock(Item.class);
		Mockito.doReturn(oldItem).when(this.sourceTable).getItem(BuildNewListDropHandlerTest.GID2);
		this.addMockPropToItem(oldItem);

		// new item
		final Item newItem = Mockito.mock(Item.class);
		Mockito.doReturn(newItem).when(this.sourceTable).addItemAfter(BuildNewListDropHandlerTest.GID1, BuildNewListDropHandlerTest.GID2);
		this.addMockPropToItem(newItem);

		this.dropHandler.drop(this.event);

		// retrieval of further information about TableTransferable will be called
		Mockito.verify(this.tableTransferable, Mockito.times(1)).getSourceComponent();
		Mockito.verify(this.event, Mockito.times(1)).getTargetDetails();

		Mockito.verify(this.sourceTable, Mockito.times(1)).getItem(BuildNewListDropHandlerTest.GID2);

		this.verifyEachPropertyIsProperlyFilledUp(newItem);
	}

	private void addMockPropToItem(final Item item) {
		Mockito.doReturn(ListBuilderComponent.GERMPLASMS_TABLE_DATA).when(this.sourceTable).getData();
		final Property tagProp = Mockito.mock(Property.class);
		Mockito.doReturn(tagProp).when(item).getItemProperty(ColumnLabels.TAG.getName());
		final Property gidProp = Mockito.mock(Property.class);
		Mockito.doReturn(gidProp).when(item).getItemProperty(ColumnLabels.GID.getName());
		final Property groupIdProp = Mockito.mock(Property.class);
		Mockito.doReturn(groupIdProp).when(item).getItemProperty(ColumnLabels.GROUP_ID.getName());
		final Property entryCodeProp = Mockito.mock(Property.class);
		Mockito.doReturn(entryCodeProp).when(item).getItemProperty(ColumnLabels.ENTRY_CODE.getName());
		final Property seedSourceProp = Mockito.mock(Property.class);
		Mockito.doReturn(seedSourceProp).when(item).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
		final Property desigProp = Mockito.mock(Property.class);
		Mockito.doReturn(desigProp).when(item).getItemProperty(ColumnLabels.DESIGNATION.getName());
		final Property parentageProp = Mockito.mock(Property.class);
		Mockito.doReturn(parentageProp).when(item).getItemProperty(ColumnLabels.PARENTAGE.getName());
		final Property availInvProp = Mockito.mock(Property.class);
		Mockito.doReturn(availInvProp).when(item).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
		final Property seedResProp = Mockito.mock(Property.class);
		Mockito.doReturn(seedResProp).when(item).getItemProperty(ColumnLabels.TOTAL.getName());
		final Property stockIDProp = Mockito.mock(Property.class);
		Mockito.doReturn(stockIDProp).when(item).getItemProperty(ColumnLabels.STOCKID.getName());
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

	private void verifyEachPropertyIsProperlyFilledUp(final Item item) {
		// Verify if that each property in item is properly filled up
		Mockito.verify(item, Mockito.atLeast(1)).getItemProperty(ColumnLabels.GROUP_ID.getName());
		Mockito.verify(item, Mockito.atLeast(1)).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName());
		Mockito.verify(item, Mockito.atLeast(1)).getItemProperty(ColumnLabels.TOTAL.getName());
		Mockito.verify(item, Mockito.atLeast(1)).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
		Mockito.verify(item, Mockito.atLeast(1)).getItemProperty(ColumnLabels.DESIGNATION.getName());
		Mockito.verify(item, Mockito.atLeast(1)).getItemProperty(ColumnLabels.PARENTAGE.getName());
	}

	private List<Integer> prepareItemIds() {
		final List<Integer> items = new ArrayList<Integer>();
		for (int i = 1; i <= BuildNewListDropHandlerTest.NO_OF_ENTRIES; i++) {
			items.add(i);
		}
		return items;
	}
}

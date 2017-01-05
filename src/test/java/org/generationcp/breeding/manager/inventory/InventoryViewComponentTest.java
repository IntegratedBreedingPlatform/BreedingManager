package org.generationcp.breeding.manager.inventory;

import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

import com.beust.jcommander.internal.Lists;

@RunWith(MockitoJUnitRunner.class)
public class InventoryViewComponentTest {

	@InjectMocks
	private InventoryViewComponent inventoryViewComponentForGermplasm = new InventoryViewComponent(null, null, 1);

	@InjectMocks
	private InventoryViewComponent inventoryViewComponentForListEntry = new InventoryViewComponent(1, 1, null);

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private static final String LOCATION_HEADER_NAME = "LOCATION";

	private static final String ACTUAL_BALANCE_HEADER_NAME = "ACTUAL BALANCE";

	private static final String AVAILABLE_BALANCE_HEADER_NAME = "AVAILABLE BALANCE";

	private static final String LOT_STATUS = "LOT STATUS";

	private static final String COMMENT_HEADER_NAME = "COMMENT";

	private static final String STOCKID_HEADER_NAME = "STOCKID";

	private static final String LOTID_HEADER_NAME = "LOTID";

	private static final String SEED_SOURCE_HEADER_NAME = "SEEDSOURCE";

	private static final String LOT_STATUS_HEADER_NAME = "LOT STATUS";

	private static final int TABLE_SIZE = 5;
	private static final String LOT_LOCATION = "Location1";
	private static final String ACTUAL_BALANCE = "100.0g";
	private static final String AVAILABLE_BALANCE = "100.0g";
	private static final String COMMENTS = "Lot Comment1";
	private static final String STOCKID = "STK1-1,STK2-2,STK-3";
	private static final String LOT_ID = "1";
	private static final String SEED_SOURCE = "SeedSource";

	@Before
	public void setup() {

		Mockito.doReturn(new Term(TermId.LOT_LOCATION_INVENTORY.getId(), LOCATION_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.doReturn(new Term(TermId.ACTUAL_BALANCE.getId(), ACTUAL_BALANCE_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.ACTUAL_BALANCE.getTermId().getId());
		Mockito.doReturn(new Term(TermId.TOTAL_INVENTORY.getId(), AVAILABLE_BALANCE_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.TOTAL.getTermId().getId());
		Mockito.doReturn(new Term(TermId.LOT_STATUS.getId(), LOT_STATUS, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_STATUS.getTermId().getId());
		Mockito.doReturn(new Term(TermId.COMMENT_INVENTORY.getId(), COMMENT_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(new Term(TermId.STOCKID.getId(), STOCKID_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(new Term(TermId.LOT_ID_INVENTORY.getId(), LOTID_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_ID.getTermId().getId());
		Mockito.doReturn(new Term(TermId.SEED_SOURCE.getId(), SEED_SOURCE_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId());

	}

	@Test
	public void testInstantiateComponentsForGermplasm() {
		this.inventoryViewComponentForGermplasm.instantiateComponents();
		Assert.assertNull(inventoryViewComponentForGermplasm.getGermplasmListData());
	}

	@Test
	public void testInstantiateComponentsForListEntry() {
		GermplasmListData germplasmListData = ListInventoryDataInitializer.createGermplasmListData(1);
		Mockito.when(this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(germplasmListData);

		this.inventoryViewComponentForListEntry.instantiateComponents();
		Assert.assertNotNull(inventoryViewComponentForListEntry.getGermplasmListData());
		Mockito.verify(this.messageSource).getMessage(Message.LOT_DETAILS_FOR_SELECTED_ENTRIES,
				inventoryViewComponentForListEntry.getGermplasmListData().getDesignation());

	}

	@Test
	public void testInitializeLotEntriesTableForGermplasm() {

		Table table = new Table();
		this.inventoryViewComponentForGermplasm.initializeLotEntriesTable(table);

		Assert.assertNotNull(table);
		Collection<?> columnIds = table.getContainerPropertyIds();

		Assert.assertTrue(columnIds.size() == 7);
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_LOCATION));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.ACTUAL_BALANCE));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.AVAILABLE_BALANCE));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_STATUS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.COMMENTS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.STOCKID));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_ID));
		Assert.assertFalse(columnIds.contains(InventoryViewComponent.SEED_SOURCE));

		List<String> columnHeaders = Lists.newArrayList(table.getColumnHeaders());

		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.LOTID_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.ACTUAL_BALANCE_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.AVAILABLE_BALANCE_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.LOT_STATUS_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.COMMENT_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.LOTID_HEADER_NAME)));
		Assert.assertFalse(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.SEED_SOURCE_HEADER_NAME)));

	}

	@Test
	public void testInitializeLotEntriesTableForListEntry() {

		Table table = new Table();
		this.inventoryViewComponentForListEntry.initializeLotEntriesTable(table);

		Assert.assertNotNull(table);
		Collection<?> columnIds = table.getContainerPropertyIds();

		Assert.assertTrue(columnIds.size() == 8);
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_LOCATION));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.ACTUAL_BALANCE));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.AVAILABLE_BALANCE));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_STATUS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.COMMENTS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.STOCKID));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_ID));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.SEED_SOURCE));

		List<String> columnHeaders = Lists.newArrayList(table.getColumnHeaders());

		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.LOTID_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.ACTUAL_BALANCE_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.AVAILABLE_BALANCE_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.LOT_STATUS_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.COMMENT_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.LOTID_HEADER_NAME)));
		Assert.assertTrue(columnHeaders.contains(table.getColumnHeader(InventoryViewComponentTest.SEED_SOURCE_HEADER_NAME)));

	}

	@Test
	public void testInitializeValuesForGermplasm() {

		final List<? extends LotDetails> inventoryDetails = ListInventoryDataInitializer.createLotDetails(1);
		Mockito.when(this.inventoryDataManager.getLotDetailsForGermplasm(Mockito.anyInt())).thenReturn((List<LotDetails>) inventoryDetails);
		Table table = new Table();
		this.inventoryViewComponentForGermplasm.initializeLotEntriesTable(table);
		this.inventoryViewComponentForGermplasm.setLotEntriesTable(table);

		this.inventoryViewComponentForGermplasm.initializeValues();

		Item item = this.inventoryViewComponentForGermplasm.getLotEntriesTable().getItem(1);

		Assert.assertEquals(InventoryViewComponentTest.TABLE_SIZE, inventoryViewComponentForGermplasm.getLotEntriesTable().size());
		Assert.assertEquals(InventoryViewComponentTest.LOT_LOCATION, item.getItemProperty(InventoryViewComponent.LOT_LOCATION).getValue());
		Assert.assertEquals(InventoryViewComponentTest.ACTUAL_BALANCE,
				item.getItemProperty(InventoryViewComponent.ACTUAL_BALANCE).getValue());
		Assert.assertEquals(InventoryViewComponentTest.AVAILABLE_BALANCE,
				item.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).getValue());
		Assert.assertEquals(LotStatus.ACTIVE.name(), item.getItemProperty(InventoryViewComponent.LOT_STATUS).getValue());
		Assert.assertEquals(InventoryViewComponentTest.COMMENTS, item.getItemProperty(InventoryViewComponent.COMMENTS).getValue());
		Assert.assertEquals(InventoryViewComponentTest.STOCKID, item.getItemProperty(InventoryViewComponent.STOCKID).getValue().toString());
		Assert.assertEquals(InventoryViewComponentTest.LOT_ID, item.getItemProperty(InventoryViewComponent.LOT_ID).getValue().toString());
		Assert.assertNull(item.getItemProperty(InventoryViewComponent.SEED_SOURCE).getValue());

	}

	@Test
	public void testInitializeValuesForGermplasmWithAvailableAndWithdrawalBalanceNull() {

		final List<? extends LotDetails> inventoryDetails = ListInventoryDataInitializer.createLotDetails(1);
		inventoryDetails.get(0).setWithdrawalBalance(null);
		inventoryDetails.get(0).setAvailableLotBalance(null);
		Mockito.when(this.inventoryDataManager.getLotDetailsForGermplasm(Mockito.anyInt())).thenReturn((List<LotDetails>) inventoryDetails);
		Table table = new Table();
		this.inventoryViewComponentForGermplasm.initializeLotEntriesTable(table);
		this.inventoryViewComponentForGermplasm.setLotEntriesTable(table);

		this.inventoryViewComponentForGermplasm.initializeValues();

		Item item = this.inventoryViewComponentForGermplasm.getLotEntriesTable().getItem(1);

		Assert.assertEquals(InventoryViewComponentTest.TABLE_SIZE, inventoryViewComponentForGermplasm.getLotEntriesTable().size());
		Assert.assertEquals(InventoryViewComponentTest.LOT_LOCATION, item.getItemProperty(InventoryViewComponent.LOT_LOCATION).getValue());
		Assert.assertEquals(InventoryViewComponentTest.ACTUAL_BALANCE,
				item.getItemProperty(InventoryViewComponent.ACTUAL_BALANCE).getValue());
		Assert.assertTrue(StringUtil.isEmpty(item.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).getValue().toString()));
		Assert.assertEquals(LotStatus.ACTIVE.name(), item.getItemProperty(InventoryViewComponent.LOT_STATUS).getValue().toString());
		Assert.assertEquals(InventoryViewComponentTest.COMMENTS, item.getItemProperty(InventoryViewComponent.COMMENTS).getValue());
		Assert.assertEquals(InventoryViewComponentTest.STOCKID, item.getItemProperty(InventoryViewComponent.STOCKID).getValue().toString());
		Assert.assertEquals(InventoryViewComponentTest.LOT_ID, item.getItemProperty(InventoryViewComponent.LOT_ID).getValue().toString());
		Assert.assertNull(item.getItemProperty(InventoryViewComponent.SEED_SOURCE).getValue());

	}


	@Test
	public void testInitializeValuesForListEntry() {

		final List<? extends LotDetails> inventoryDetails = ListInventoryDataInitializer.createLotDetails(1);

		Mockito.when(this.inventoryDataManager.getLotDetailsForListEntry(Mockito.anyInt(), Mockito.anyInt(), (Integer) Mockito.isNull()))
				.thenReturn((List<ListEntryLotDetails>) inventoryDetails);

		Table table = new Table();
		this.inventoryViewComponentForListEntry.initializeLotEntriesTable(table);
		this.inventoryViewComponentForListEntry.setLotEntriesTable(table);
		GermplasmListData germplasmListData = ListInventoryDataInitializer.createGermplasmListData(1);
		germplasmListData.setSeedSource(InventoryViewComponentTest.SEED_SOURCE);
		this.inventoryViewComponentForListEntry.setGermplasmListData(germplasmListData);

		this.inventoryViewComponentForListEntry.initializeValues();

		Item item = this.inventoryViewComponentForListEntry.getLotEntriesTable().getItem(1);

		Assert.assertEquals(InventoryViewComponentTest.TABLE_SIZE, inventoryViewComponentForListEntry.getLotEntriesTable().size());
		Assert.assertEquals(InventoryViewComponentTest.LOT_LOCATION, item.getItemProperty(InventoryViewComponent.LOT_LOCATION).getValue());
		Assert.assertEquals(InventoryViewComponentTest.ACTUAL_BALANCE,
				item.getItemProperty(InventoryViewComponent.ACTUAL_BALANCE).getValue());
		Assert.assertEquals(InventoryViewComponentTest.AVAILABLE_BALANCE,
				item.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).getValue());
		Assert.assertEquals(LotStatus.ACTIVE.name(), item.getItemProperty(InventoryViewComponent.LOT_STATUS).getValue().toString());
		Assert.assertEquals(InventoryViewComponentTest.COMMENTS, item.getItemProperty(InventoryViewComponent.COMMENTS).getValue());
		Assert.assertEquals(InventoryViewComponentTest.STOCKID, item.getItemProperty(InventoryViewComponent.STOCKID).getValue().toString());
		Assert.assertEquals(InventoryViewComponentTest.LOT_ID, item.getItemProperty(InventoryViewComponent.LOT_ID).getValue().toString());
		Assert.assertEquals(InventoryViewComponentTest.SEED_SOURCE,
				item.getItemProperty(InventoryViewComponent.SEED_SOURCE).getValue().toString());

	}

}

package org.generationcp.breeding.manager.inventory;

import java.util.Collection;
import java.util.List;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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

/**
 * Created by info on 17-Nov-16.
 */

@RunWith(MockitoJUnitRunner.class)
public class InventoryViewComponentTest {

	@InjectMocks
	private InventoryViewComponent inventoryViewComponent = new InventoryViewComponent(null, null, 1);

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	private static final String LOCATION_HEADER_NAME = "LOCATION";

	private static final String ACTUAL_BALANCE_HEADER_NAME = "ACTUAL BALANCE";

	private static final String AVAILABLE_BALANCE_HEADER_NAME = "AVAILABLE BALANCE";

	private static final String WITHDRAWAL_HEADER_NAME = "WITHDRAWAL";

	private static final String STATUS_HEADER_NAME = "STATUS";

	private static final String COMMENT_HEADER_NAME = "COMMENT";

	private static final String STOCKID_HEADER_NAME = "STOCKID";

	private static final String LOTID_HEADER_NAME = "LOTID";

	private static final int TABLE_SIZE = 5;
	private static final String LOT_LOCATION = "Location1";
	private static final String ACTUAL_BALANCE = "100.0g";
	private static final String AVAILABLE_BALANCE = "100.0g";
	private static final String WITHDRAWAL = "12.0g";
	private static final String STATUS = "1";
	private static final String COMMENTS = "Lot Comment1";
	private static final String STOCKID = "STK1-1,STK2-2,STK-3";
	private static final String LOT_ID = "1";


	@Before
	public void setup() {

		Mockito.doReturn(new Term(TermId.LOT_LOCATION_INVENTORY.getId(), LOCATION_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.doReturn(new Term(TermId.ACTUAL_BALANCE.getId(), ACTUAL_BALANCE_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.ACTUAL_BALANCE.getTermId().getId());
		Mockito.doReturn(new Term(TermId.TOTAL_INVENTORY.getId(), AVAILABLE_BALANCE_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.TOTAL.getTermId().getId());
		Mockito.doReturn(new Term(TermId.SEED_RESERVATION.getId(), WITHDRAWAL_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.SEED_RESERVATION.getTermId().getId());
		Mockito.doReturn(new Term(TermId.STATUS.getId(), STATUS_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.STATUS.getTermId().getId());
		Mockito.doReturn(new Term(TermId.COMMENT_INVENTORY.getId(), COMMENT_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(new Term(TermId.STOCKID.getId(), STOCKID_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(new Term(TermId.LOT_ID_INVENTORY.getId(), LOTID_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_ID.getTermId().getId());

	}

	@Test
	public void testInventoryViewComponentColumnsAndHeaderNames() {

		Table table = new Table();
		this.inventoryViewComponent.initializeLotEntriesTable(table);

		Assert.assertNotNull(table);
		Collection<?> columnIds = table.getContainerPropertyIds();

		Assert.assertTrue(columnIds.size() == 8);
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_LOCATION));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.ACTUAL_BALANCE));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.AVAILABLE_BALANCE));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.WITHDRAWAL));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.STATUS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.COMMENTS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.STOCKID));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_ID));

		Assert.assertEquals(LOTID_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.LOT_ID));
		Assert.assertEquals(ACTUAL_BALANCE_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.ACTUAL_BALANCE));
		Assert.assertEquals(AVAILABLE_BALANCE_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.AVAILABLE_BALANCE));
		Assert.assertEquals(WITHDRAWAL_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.WITHDRAWAL));
		Assert.assertEquals(STATUS_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.STATUS));
		Assert.assertEquals(COMMENT_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.COMMENTS));
		Assert.assertEquals(STOCKID_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.STOCKID));
		Assert.assertEquals(LOTID_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.LOT_ID));

	}

	@Test
	public void testInitializeValues() {

		final List<? extends LotDetails> inventoryDetails = ListInventoryDataInitializer.createLotDetails(1);
		Mockito.when(this.inventoryDataManager.getLotDetailsForGermplasm(Mockito.anyInt())).thenReturn((List<LotDetails>) inventoryDetails);
		Table table = new Table();
		this.inventoryViewComponent.initializeLotEntriesTable(table);
		this.inventoryViewComponent.setLotEntriesTable(table);

		this.inventoryViewComponent.initializeValues();

		Item item = this.inventoryViewComponent.getLotEntriesTable().getItem(1);

		Assert.assertEquals(InventoryViewComponentTest.TABLE_SIZE, inventoryViewComponent.getLotEntriesTable().size());
		Assert.assertEquals(InventoryViewComponentTest.LOT_LOCATION, item.getItemProperty(InventoryViewComponent.LOT_LOCATION).getValue());
		Assert.assertEquals(InventoryViewComponentTest.ACTUAL_BALANCE, item.getItemProperty(InventoryViewComponent.ACTUAL_BALANCE).getValue());
		Assert.assertEquals(InventoryViewComponentTest.AVAILABLE_BALANCE, item.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).getValue());
		Assert.assertEquals(InventoryViewComponentTest.WITHDRAWAL, item.getItemProperty(InventoryViewComponent.WITHDRAWAL).getValue());
		Assert.assertEquals(InventoryViewComponentTest.STATUS, item.getItemProperty(InventoryViewComponent.STATUS).getValue());
		Assert.assertEquals(InventoryViewComponentTest.COMMENTS, item.getItemProperty(InventoryViewComponent.COMMENTS).getValue());
		Assert.assertEquals(InventoryViewComponentTest.STOCKID, item.getItemProperty(InventoryViewComponent.STOCKID).getValue().toString());
		Assert.assertEquals(InventoryViewComponentTest.LOT_ID, item.getItemProperty(InventoryViewComponent.LOT_ID).getValue().toString());

	}

}

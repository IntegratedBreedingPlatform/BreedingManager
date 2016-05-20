
package org.generationcp.breeding.manager.germplasm.inventory;

import java.util.Collection;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
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

import com.vaadin.ui.Table;

@RunWith(MockitoJUnitRunner.class)
public class InventoryViewComponentTest {

	private static final String RESERVED_HEADER_NAME = "RESERVED";

	private static final String AVAILABLE_BALANCE_HEADER_NAME = "AVAILABLE BALANCE";

	private static final String TOTAL_BALANCE_HEADER_NAME = "TOTAL";

	private static final String LOTID_HEADER_NAME = "LOTID";

	private static final String STOCKID_HEADER_NAME = "STOCKID";

	private static final String COMMENT_HEADER_NAME = "COMMENT";

	private static final String UNITS_HEADER_NAME = "UNITS";

	private static final String LOCATION_HEADER_NAME = "LOCATION";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@InjectMocks
	private InventoryViewComponent inventoryView = new InventoryViewComponent(null, null, 1);

	@Before
	public void initializeMocks() {

		Mockito.doReturn(new Term(TermId.LOT_LOCATION_INVENTORY.getId(), LOCATION_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.doReturn(new Term(TermId.UNITS_INVENTORY.getId(), UNITS_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.UNITS.getTermId().getId());
		Mockito.doReturn(new Term(TermId.COMMENT_INVENTORY.getId(), COMMENT_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(new Term(TermId.STOCKID.getId(), STOCKID_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(new Term(TermId.LOT_ID_INVENTORY.getId(), LOTID_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_ID.getTermId().getId());
		Mockito.doReturn(new Term(TermId.RESERVED_INVENTORY.getId(), LOTID_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.RESERVED.getTermId().getId());
		Mockito.doReturn(new Term(TermId.AVAILABLE_INVENTORY.getId(), AVAILABLE_BALANCE_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(new Term(TermId.RESERVED_INVENTORY.getId(), RESERVED_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.RESERVED.getTermId().getId());
		Mockito.doReturn(new Term(TermId.TOTAL_INVENTORY.getId(), TOTAL_BALANCE_HEADER_NAME, "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.TOTAL.getTermId().getId());

	}

	@Test
	public void testInventoryViewColumnsAndHeaderNames() {

		this.inventoryView.instantiateComponents();

		// check expected list of table columns
		Table table = this.inventoryView.getTable();
		Assert.assertNotNull(table);
		Collection<?> columnIds = table.getContainerPropertyIds();

		Assert.assertTrue(columnIds.size() == 8);
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_ID));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_LOCATION));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.LOT_UNITS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.TOTAL));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.AVAILABLE_BALANCE));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.RES_THIS_ENTRY));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.COMMENTS));
		Assert.assertTrue(columnIds.contains(InventoryViewComponent.STOCKID));

		Assert.assertEquals(LOTID_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.LOT_ID));
		Assert.assertEquals(LOCATION_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.LOT_LOCATION));
		Assert.assertEquals(UNITS_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.LOT_UNITS));
		Assert.assertEquals(TOTAL_BALANCE_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.TOTAL));
		Assert.assertEquals(AVAILABLE_BALANCE_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.AVAILABLE_BALANCE));
		Assert.assertEquals(RESERVED_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.RES_THIS_ENTRY));
		Assert.assertEquals(COMMENT_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.COMMENTS));
		Assert.assertEquals(STOCKID_HEADER_NAME, table.getColumnHeader(InventoryViewComponent.STOCKID));
	}

}

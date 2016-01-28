
package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.data.initializer.ListInventoryDataInitializer;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class ListInventoryTableTest {

	private static final int LIST_ID = 1;
	private static final double PREV_AVAIL_INVENTORY = 99.9;
	private static final double PREV_RESERVED_VALUE = 0.01;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	protected InventoryDataManager inventoryDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private ListInventoryTable listInventoryTable = new ListInventoryTable(ListInventoryTableTest.LIST_ID);

	@Before
	public void setUp() throws MiddlewareQueryException {

		MockitoAnnotations.initMocks(this);

		Mockito.doReturn("CHECK").when(this.messageSource).getMessage(Message.CHECK_ICON);
		Mockito.doReturn("#").when(this.messageSource).getMessage(Message.HASHTAG);

		this.listInventoryTable.instantiateComponents();
	}

	@Test
	public void testInstantiateComponentsHeaderNameFromOntology() throws MiddlewareQueryException {

		Term desigTerm = ListInventoryDataInitializer.createTerm("Designation");
		Mockito.doReturn(desigTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.DESIGNATION.getTermId().getId());

		Term locTerm = ListInventoryDataInitializer.createTerm("Location");
		Mockito.doReturn(locTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());

		Term unitsTerm = ListInventoryDataInitializer.createTerm("Units");
		Mockito.doReturn(unitsTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.UNITS.getTermId().getId());

		Term availInvTerm = ListInventoryDataInitializer.createTerm("Available Inventory");
		Mockito.doReturn(availInvTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());

		Term totalTerm = ListInventoryDataInitializer.createTerm("Total");
		Mockito.doReturn(totalTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());

		Term reservedTerm = ListInventoryDataInitializer.createTerm("RES");
		Mockito.doReturn(reservedTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());

		Term newlyResTerm = ListInventoryDataInitializer.createTerm("Newly Reserved");
		Mockito.doReturn(newlyResTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());

		Term commentTerm = ListInventoryDataInitializer.createTerm("Comment");
		Mockito.doReturn(commentTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());

		Term stockIdTerm = ListInventoryDataInitializer.createTerm("Stock ID");
		Mockito.doReturn(stockIdTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());

		Term lotIdTerm = ListInventoryDataInitializer.createTerm("Lot ID");
		Mockito.doReturn(lotIdTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());

		Term seedSourceTerm = ListInventoryDataInitializer.createTerm("Seed Source");
		Mockito.doReturn(seedSourceTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId());

		this.listInventoryTable.instantiateComponents();

		Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(desigTerm.getName(), table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(locTerm.getName(), table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		Assert.assertEquals(unitsTerm.getName(), table.getColumnHeader(ColumnLabels.UNITS.getName()));
		Assert.assertEquals(availInvTerm.getName(), table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(totalTerm.getName(), table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(reservedTerm.getName(), table.getColumnHeader(ColumnLabels.RESERVED.getName()));
		Assert.assertEquals(newlyResTerm.getName(), table.getColumnHeader(ColumnLabels.NEWLY_RESERVED.getName()));
		Assert.assertEquals(commentTerm.getName(), table.getColumnHeader(ColumnLabels.COMMENT.getName()));
		Assert.assertEquals(stockIdTerm.getName(), table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals(lotIdTerm.getName(), table.getColumnHeader(ColumnLabels.LOT_ID.getName()));
		Assert.assertEquals(seedSourceTerm.getName(), table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInstantiateComponentsHeaderNameDoesntExistFromOntology() throws MiddlewareQueryException {

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.DESIGNATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.UNITS.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId());

		this.listInventoryTable.instantiateComponents();

		Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ColumnLabels.DESIGNATION.getName(), table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ColumnLabels.LOT_LOCATION.getName(), table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		Assert.assertEquals(ColumnLabels.UNITS.getName(), table.getColumnHeader(ColumnLabels.UNITS.getName()));
		Assert.assertEquals(ColumnLabels.AVAILABLE_INVENTORY.getName(), table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ColumnLabels.TOTAL.getName(), table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(ColumnLabels.RESERVED.getName(), table.getColumnHeader(ColumnLabels.RESERVED.getName()));
		Assert.assertEquals(ColumnLabels.NEWLY_RESERVED.getName(), table.getColumnHeader(ColumnLabels.NEWLY_RESERVED.getName()));
		Assert.assertEquals(ColumnLabels.COMMENT.getName(), table.getColumnHeader(ColumnLabels.COMMENT.getName()));
		Assert.assertEquals(ColumnLabels.LOT_ID.getName(), table.getColumnHeader(ColumnLabels.LOT_ID.getName()));
		Assert.assertEquals(ColumnLabels.SEED_SOURCE.getName(), table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testDisplayInventoryDetails() {
		List<GermplasmListData> inventoryDetails = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();
		this.listInventoryTable.displayInventoryDetails(inventoryDetails);

		int expectedNoOFLotEntries = ListInventoryDataInitializer.getNumberOfEntriesInInventoryView();
		Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("Expecting that all entries from inventoryDetails are properly inserted in listinventory table but didn't.",
				expectedNoOFLotEntries, table.getContainerDataSource().size());
		
		GermplasmListData row1InventoryDetails = inventoryDetails.get(0);
		final LotDetails row1LotDetails = row1InventoryDetails.getInventoryInfo().getLotRows().get(0);
		Item row1VaadinTable = table.getItem(row1LotDetails);
		Assert.assertNotNull(row1VaadinTable);
		
		Assert.assertEquals(row1InventoryDetails.getEntryId(), row1VaadinTable.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getLocationOfLot().getLname(), row1VaadinTable.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getScaleOfLot().getName(), row1VaadinTable.getItemProperty(ColumnLabels.UNITS.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getActualLotBalance(), row1VaadinTable.getItemProperty(ColumnLabels.TOTAL.getName()).getValue());
		Assert.assertEquals(0.0, row1VaadinTable.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getCommentOfLot(), row1VaadinTable.getItemProperty(ColumnLabels.COMMENT.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getLotId(), row1VaadinTable.getItemProperty(ColumnLabels.LOT_ID.getName()).getValue());
		Assert.assertEquals(row1InventoryDetails.getSeedSource(), row1VaadinTable.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
		
	}

	@Test
	public void testLoadInventoryData_WhenListIdIsNull() {
		this.listInventoryTable.setListId(null);
		this.listInventoryTable.loadInventoryData();
		Assert.assertEquals(
				"Expecting that the method for loading inventory data in inventory table is not called when there is no listid set but didn't.",
				0, this.listInventoryTable.getTable().getContainerDataSource().size());
	}

	@Test
	public void testLoadInventoryData() {
		this.initDataToInventoryTable();

		int expectedNoOFLotEntries = ListInventoryDataInitializer.getNumberOfEntriesInInventoryView();
		Assert.assertEquals("Expecting that the method for loading inventory data in inventory table is called but didn't.",
				expectedNoOFLotEntries, this.listInventoryTable.getTable().getContainerDataSource().size());

	}

	@Test
	public void testLoadInventoryDataWithException() {
		Mockito.doThrow(new MiddlewareQueryException("Some Exception Message Here")).when(this.inventoryDataManager)
				.getLotDetailsForList(ListInventoryTableTest.LIST_ID, 0, Integer.MAX_VALUE);
		this.listInventoryTable.loadInventoryData();

		Assert.assertEquals("Expecting that no rows is loaded in inventory table when there is an exception.", 0, this.listInventoryTable
				.getTable().getContainerDataSource().size());
	}

	private void initDataToInventoryTable() {
		List<GermplasmListData> inventoryDetails = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();
		Mockito.doReturn(inventoryDetails).when(this.inventoryDataManager)
				.getLotDetailsForList(ListInventoryTableTest.LIST_ID, 0, Integer.MAX_VALUE);

		this.listInventoryTable.loadInventoryData();
	}

	@Test
	public void testToggleSelectOnLotEntries() {
		this.initDataToInventoryTable();

		Table table = this.listInventoryTable.getTable();

		// retrieve a checkbox from one of the rows in inventory table
		@SuppressWarnings("unchecked")
		Collection<ListEntryLotDetails> itemIds = (Collection<ListEntryLotDetails>) table.getItemIds();
		Iterator<ListEntryLotDetails> itr = itemIds.iterator();
		ListEntryLotDetails lotDetail = itr.next();
		Item item = table.getItem(lotDetail);
		CheckBox itemCheckBox = (CheckBox) item.getItemProperty(ColumnLabels.TAG.getName()).getValue();

		itemCheckBox.setValue(true);
		this.listInventoryTable.toggleSelectOnLotEntries(itemCheckBox);
		Assert.assertEquals("Expecting that only 1 checkbox is selected but didn't.", 1, this.listInventoryTable.getSelectedLots().size());

		itemCheckBox.setValue(false);
		this.listInventoryTable.toggleSelectOnLotEntries(itemCheckBox);
		Assert.assertEquals("Expecting that no checkbox is selected but didn't.", 0, this.listInventoryTable.getSelectedLots().size());
	}

	@Test
	public void testResetRowsForCancelledReservation() {
		List<ListEntryLotDetails> lotDetailsToCancel = new ArrayList<ListEntryLotDetails>();
		this.initDataToInventoryTable();
		Table table = this.listInventoryTable.getTable();
		this.updateReservationForLotEntries(lotDetailsToCancel, table, PREV_RESERVED_VALUE);

		this.listInventoryTable.resetRowsForCancelledReservation(lotDetailsToCancel, LIST_ID);

		double expectedNewAvailInventory = PREV_AVAIL_INVENTORY + PREV_RESERVED_VALUE;
		for (ListEntryLotDetails lotDetail : lotDetailsToCancel) {
			Item item = table.getItem(lotDetail);
			double availVal = (double) item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
			double reservedVal = (double) item.getItemProperty(ColumnLabels.RESERVED.getName()).getValue();
			double newReservedVal = (double) item.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).getValue();

			Assert.assertEquals("Expecting that the available inventory is increased by the amount of reservation but didn't.",
					expectedNewAvailInventory, availVal, 0.00);
			Assert.assertEquals("Expecting that the reservation amount is reset to 0 but didn't.", 0, reservedVal, 0.00);
			Assert.assertEquals("Expecting that the new reservation amount is also reset to 0 but didn't", 0, newReservedVal, 0.00);
		}

	}

	@Test
	public void testIsSelectedEntriesHasReservation_WhenThereIsReservation() {
		List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		this.initDataToInventoryTable();
		Table table = this.listInventoryTable.getTable();
		this.updateReservationForLotEntries(lotDetails, table, PREV_RESERVED_VALUE);

		Assert.assertTrue("Expecting true for at least one lot details with reservation but didn't.",
				this.listInventoryTable.isSelectedEntriesHasReservation(lotDetails));
	}

	@Test
	public void testIsSelectedEntriesHasReservation_WhenThereIsNoReservation() {
		List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		this.initDataToInventoryTable();
		Table table = this.listInventoryTable.getTable();
		this.updateReservationForLotEntries(lotDetails, table, 0);

		Assert.assertFalse("Expecting false for at least one lot details with reservation but didn't.",
				this.listInventoryTable.isSelectedEntriesHasReservation(lotDetails));
	}

	private void updateReservationForLotEntries(List<ListEntryLotDetails> lotEntries, Table table, double reservedVal) {
		@SuppressWarnings("unchecked")
		Collection<ListEntryLotDetails> itemIds = (Collection<ListEntryLotDetails>) table.getItemIds();
		Iterator<ListEntryLotDetails> itr = itemIds.iterator();
		while (itr.hasNext()) {
			ListEntryLotDetails lotDetail = itr.next();
			Item item = table.getItem(lotDetail);
			item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(PREV_AVAIL_INVENTORY);
			item.getItemProperty(ColumnLabels.RESERVED.getName()).setValue(reservedVal);
			item.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(reservedVal);
			lotEntries.add(lotDetail);
		}
	}
}

package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class ListInventoryTableTest {

	private static final int LIST_ID = 1;
	private static final double PREV_AVAIL_INVENTORY = 99.9;
	private static final double PREV_RESERVED_VALUE = 0.1;
	private static final String STATUS = "Reserved";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	protected InventoryDataManager inventoryDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private final ListInventoryTable listInventoryTable = new ListInventoryTable(ListInventoryTableTest.LIST_ID);

	@Before
	public void setUp() throws MiddlewareQueryException {

		MockitoAnnotations.initMocks(this);

		Mockito.doReturn("CHECK").when(this.messageSource).getMessage(Message.CHECK_ICON);
		Mockito.doReturn("#").when(this.messageSource).getMessage(Message.HASHTAG);

		this.listInventoryTable.instantiateComponents();
	}

	@Test
	public void testInstantiateComponentsHeaderNameFromOntology() throws MiddlewareQueryException {

		final Term desigTerm = ListInventoryDataInitializer.createTerm("Designation");
		Mockito.doReturn(desigTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.DESIGNATION.getTermId().getId());

		final Term locTerm = ListInventoryDataInitializer.createTerm("Location");
		Mockito.doReturn(locTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());

		final Term totalTerm = ListInventoryDataInitializer.createTerm("Total");
		Mockito.doReturn(totalTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());

		final Term totalWithdrawalTerm = ListInventoryDataInitializer.createTerm("TOTAL WITHDRAWAL");
		Mockito.doReturn(totalWithdrawalTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_RESERVATION.getTermId().getId());

		final Term newReservationTerm = ListInventoryDataInitializer.createTerm("RESERVATION");
		Mockito.doReturn(newReservationTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.RESERVATION.getTermId().getId());

		final Term commentTerm = ListInventoryDataInitializer.createTerm("Comment");
		Mockito.doReturn(commentTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());

		final Term stockIdTerm = ListInventoryDataInitializer.createTerm("Stock ID");
		Mockito.doReturn(stockIdTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());

		final Term lotIdTerm = ListInventoryDataInitializer.createTerm("Lot ID");
		Mockito.doReturn(lotIdTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());

		final Term seedSourceTerm = ListInventoryDataInitializer.createTerm("Seed Source");
		Mockito.doReturn(seedSourceTerm).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId());

		this.listInventoryTable.instantiateComponents();

		final Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(desigTerm.getName(), table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(locTerm.getName(), table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		Assert.assertEquals(totalTerm.getName(), table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(totalWithdrawalTerm.getName(), table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals(newReservationTerm.getName(), table.getColumnHeader(ColumnLabels.RESERVATION.getName()));
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
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.GROUP_ID.getTermId().getId());

		this.listInventoryTable.instantiateComponents();

		final Table table = this.listInventoryTable.getTable();
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
		Assert.assertEquals(ColumnLabels.GROUP_ID.getName(), table.getColumnHeader(ColumnLabels.GROUP_ID.getName()));

	}

	@Test
	public void testDisplayInventoryDetailsWithEmptyInventoryDetails() {

		this.listInventoryTable.displayInventoryDetails(new ArrayList<GermplasmListData>());

		Assert.assertTrue("The table should be empty because there inventory detail list is empty.",
				this.listInventoryTable.getTable().size() == 0);

	}

	@Test
	public void testDisplayInventoryDetailsInventoryDetailHasNoLotsAssociated() {

		// Create an inventory detail with no associated lot
		GermplasmListData inventoryDetail = ListInventoryDataInitializer.createGermplasmListData(1);
		inventoryDetail.getInventoryInfo().setLotRows(new ArrayList<ListEntryLotDetails>());

		List<GermplasmListData> inventoryDetails = new ArrayList<>();
		inventoryDetails.add(inventoryDetail);

		this.listInventoryTable.displayInventoryDetails(inventoryDetails);

		Assert.assertTrue("The table should be empty because the inventory detail has no lots.",
				this.listInventoryTable.getTable().size() == 0);

	}

	@Test
	public void testDisplayInventoryDetails() {
		final List<GermplasmListData> inventoryDetails = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();
		this.listInventoryTable.displayInventoryDetails(inventoryDetails);

		final int expectedNoOFLotEntries = ListInventoryDataInitializer.getNumberOfEntriesInInventoryView();
		final Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("Expecting that all entries from inventoryDetails are properly inserted in listinventory table but didn't.",
				expectedNoOFLotEntries, table.getContainerDataSource().size());

		final GermplasmListData row1InventoryDetails = inventoryDetails.get(0);
		final ListEntryLotDetails row1LotDetails = ((List<ListEntryLotDetails>)row1InventoryDetails.getInventoryInfo().getLotRows()).get(0);
		final Item row1VaadinTable = table.getItem(row1LotDetails);
		Assert.assertNotNull(row1VaadinTable);

		Assert.assertEquals(row1InventoryDetails.getEntryId(), row1VaadinTable.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getLocationOfLot().getLname(),
				row1VaadinTable.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getActualLotBalance() + row1LotDetails.getLotScaleNameAbbr(),
				row1VaadinTable.getItemProperty(ColumnLabels.TOTAL.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getCommittedTotalForEntry() + row1LotDetails.getLotScaleNameAbbr(),
				row1VaadinTable.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getCommentOfLot(), row1VaadinTable.getItemProperty(ColumnLabels.COMMENT.getName()).getValue());

		Button lotIdButton = (Button) row1VaadinTable.getItemProperty(ColumnLabels.LOT_ID.getName()).getValue();
		Assert.assertEquals(row1LotDetails.getLotId(),Integer.valueOf(lotIdButton.getCaption().toString()));

		Assert.assertEquals(row1InventoryDetails.getSeedSource(),
				row1VaadinTable.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getReservedTotalForEntry() + row1LotDetails.getLotScaleNameAbbr(),
				row1VaadinTable.getItemProperty(ColumnLabels.RESERVATION.getName()).getValue());

	}

	@Test
	public void testDisplayInventoryDetailsWithNoReservedAndCommiitedAndAvailableBalance() {
		final List<GermplasmListData> inventoryDetails = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();

		final GermplasmListData row1InventoryDetails = inventoryDetails.get(0);
		final ListEntryLotDetails row1LotDetails = ((List<ListEntryLotDetails>)row1InventoryDetails.getInventoryInfo().getLotRows()).get(0);

		row1LotDetails.setReservedTotalForEntry(null);
		row1LotDetails.setCommittedTotalForEntry(null);
		row1LotDetails.setAvailableLotBalance(null);

		this.listInventoryTable.displayInventoryDetails(inventoryDetails);

		final int expectedNoOFLotEntries = ListInventoryDataInitializer.getNumberOfEntriesInInventoryView();
		final Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("Expecting that all entries from inventoryDetails are properly inserted in listinventory table but didn't.",
				expectedNoOFLotEntries, table.getContainerDataSource().size());

		final Item row1VaadinTable = table.getItem(row1LotDetails);
		Assert.assertNotNull(row1VaadinTable);

		Assert.assertEquals(row1InventoryDetails.getEntryId(), row1VaadinTable.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getLocationOfLot().getLname(),
				row1VaadinTable.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).getValue());
		Assert.assertEquals("", row1VaadinTable.getItemProperty(ColumnLabels.TOTAL.getName()).getValue());
		Assert.assertEquals("", row1VaadinTable.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getCommentOfLot(), row1VaadinTable.getItemProperty(ColumnLabels.COMMENT.getName()).getValue());

		Button lotIdButton = (Button) row1VaadinTable.getItemProperty(ColumnLabels.LOT_ID.getName()).getValue();
		Assert.assertEquals(row1LotDetails.getLotId(),Integer.valueOf(lotIdButton.getCaption().toString()));

		Assert.assertEquals(row1InventoryDetails.getSeedSource(),
				row1VaadinTable.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
		Assert.assertEquals("",
				row1VaadinTable.getItemProperty(ColumnLabels.RESERVATION.getName()).getValue());

	}

	@Test
	public void testDisplayInventoryDetailsWhenLotLocationAndScaleAreNull() {
		final List<GermplasmListData> inventoryDetails = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();

		// Simulate empty location and scale for the first list data entry.
		final GermplasmListData listData = inventoryDetails.get(0);
		listData.setInventoryInfo(ListInventoryDataInitializer.createInventoryInfoWithEmptyLocationAndScale(LIST_ID));

		this.listInventoryTable.displayInventoryDetails(inventoryDetails);

		final int expectedNoOFLotEntries = ListInventoryDataInitializer.getNumberOfEntriesInInventoryView();
		final Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("Expecting that all entries from inventoryDetails are properly inserted in listinventory table but didn't.",
				expectedNoOFLotEntries, table.getContainerDataSource().size());

		final GermplasmListData row1InventoryDetails = inventoryDetails.get(0);
		final ListEntryLotDetails row1LotDetails = ((List<ListEntryLotDetails>)row1InventoryDetails.getInventoryInfo().getLotRows()).get(0);
		final Item row1VaadinTable = table.getItem(row1LotDetails);
		Assert.assertNotNull(row1VaadinTable);

		Assert.assertEquals(row1InventoryDetails.getEntryId(), row1VaadinTable.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
		Assert.assertEquals("The first list data contains an empty location, the location value displayed in table should be empty", "",
				row1VaadinTable.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getActualLotBalance() + row1LotDetails.getLotScaleNameAbbr(),
				row1VaadinTable.getItemProperty(ColumnLabels.TOTAL.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getCommittedTotalForEntry() + row1LotDetails.getLotScaleNameAbbr(),
				row1VaadinTable.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getReservedTotalForEntry() + row1LotDetails.getLotScaleNameAbbr(),
				row1VaadinTable.getItemProperty(ColumnLabels.RESERVATION.getName()).getValue());
		Assert.assertEquals(row1LotDetails.getCommentOfLot(), row1VaadinTable.getItemProperty(ColumnLabels.COMMENT.getName()).getValue());

		Button lotIdButton = (Button) row1VaadinTable.getItemProperty(ColumnLabels.LOT_ID.getName()).getValue();
		Assert.assertEquals(row1LotDetails.getLotId(), Integer.valueOf(lotIdButton.getCaption().toString()));

		Assert.assertEquals(row1InventoryDetails.getSeedSource(),
				row1VaadinTable.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());

	}

	@Test
	public void testLoadInventoryDataWhenListIdIsNull() {
		this.listInventoryTable.setListId(null);
		this.listInventoryTable.loadInventoryData();
		Assert.assertEquals(
				"Expecting that the method for loading inventory data in inventory table is not called when there is no listid set but didn't.",
				0, this.listInventoryTable.getTable().getContainerDataSource().size());
	}

	@Test
	public void testLoadInventoryData() {
		this.initDataToInventoryTable();

		final int expectedNoOFLotEntries = ListInventoryDataInitializer.getNumberOfEntriesInInventoryView();
		Assert.assertEquals("Expecting that the method for loading inventory data in inventory table is called but didn't.",
				expectedNoOFLotEntries, this.listInventoryTable.getTable().getContainerDataSource().size());

	}

	private void initDataToInventoryTable() {
		final List<GermplasmListData> inventoryDetails = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();
		Mockito.doReturn(inventoryDetails).when(this.inventoryDataManager)
				.getLotDetailsForList(ListInventoryTableTest.LIST_ID, 0, Integer.MAX_VALUE);

		this.listInventoryTable.loadInventoryData();
	}

	@Test
	public void testToggleSelectOnLotEntries() {
		this.initDataToInventoryTable();

		final Table table = this.listInventoryTable.getTable();

		// retrieve a checkbox from one of the rows in inventory table
		@SuppressWarnings("unchecked") final Collection<ListEntryLotDetails> itemIds = (Collection<ListEntryLotDetails>) table.getItemIds();
		final Iterator<ListEntryLotDetails> itr = itemIds.iterator();
		final ListEntryLotDetails lotDetail = itr.next();
		final Item item = table.getItem(lotDetail);
		final CheckBox itemCheckBox = (CheckBox) item.getItemProperty(ColumnLabels.TAG.getName()).getValue();

		itemCheckBox.setValue(true);
		this.listInventoryTable.toggleSelectOnLotEntries(itemCheckBox);
		Assert.assertEquals("Expecting that only 1 checkbox is selected but didn't.", 1, this.listInventoryTable.getSelectedLots().size());

		itemCheckBox.setValue(false);
		this.listInventoryTable.toggleSelectOnLotEntries(itemCheckBox);
		Assert.assertEquals("Expecting that no checkbox is selected but didn't.", 0, this.listInventoryTable.getSelectedLots().size());
	}

	@Test
	public void testResetRowsForCancelledReservation() {
		final List<ListEntryLotDetails> lotDetailsToCancel = new ArrayList<ListEntryLotDetails>();
		this.initDataToInventoryTable();
		final Table table = this.listInventoryTable.getTable();
		this.updateReservationForLotEntries(lotDetailsToCancel, table, PREV_RESERVED_VALUE, STATUS);

		this.listInventoryTable.resetRowsForCancelledReservation(lotDetailsToCancel, LIST_ID);

		final double expectedNewAvailInventory = PREV_AVAIL_INVENTORY + PREV_RESERVED_VALUE;
		for (final ListEntryLotDetails lotDetail : lotDetailsToCancel) {
			final Item item = table.getItem(lotDetail);
			String totalVal = (String) item.getItemProperty(ColumnLabels.TOTAL.getName()).getValue();
			totalVal = totalVal.replace("g", "");
			String reservedVal = (String) item.getItemProperty(ColumnLabels.RESERVATION.getName()).getValue();
			reservedVal = reservedVal.replace("g", "");
			Assert.assertEquals("Expecting that the total column is increased by the amount of reservation but didn't.",
					expectedNewAvailInventory, Double.valueOf(totalVal), 0.00);
			Assert.assertEquals("Expecting that the reservation amount is reset to 0 but didn't.", 100D, Double.valueOf(reservedVal),
					0.00);

		}

	}

	@Test
	public void testIsSelectedEntriesHasReservationWhenThereIsSavedReservation() {
		final List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		this.initDataToInventoryTable();
		final Table table = this.listInventoryTable.getTable();
		this.updateReservationForLotEntries(lotDetails, table, PREV_RESERVED_VALUE, STATUS);

		Assert.assertTrue("Expecting true for at least one lot details with reservation but didn't.",
				this.listInventoryTable.isSelectedEntriesHasReservation(lotDetails,null));
	}

	@Test
	public void testIsSelectedEntriesHasReservationWhenThereIsUnSavedReservation() {
		final List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		this.initDataToInventoryTable();
		final Table table = this.listInventoryTable.getTable();
		this.updateReservationForLotEntries(lotDetails, table, 0, "");
		ImportedGermplasmListDataInitializer importedGermplasmListDataInitializer = new ImportedGermplasmListDataInitializer();
		Assert.assertTrue("Expecting true for at least one lot details with reservation but didn't.",
				this.listInventoryTable.isSelectedEntriesHasReservation(lotDetails,importedGermplasmListDataInitializer.createReservations(2)));
	}

	@Test
	public void testIsSelectedEntriesHasReservationWhenThereIsNoReservation() {
		final List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		this.initDataToInventoryTable();
		final Table table = this.listInventoryTable.getTable();
		this.updateReservationForLotEntries(lotDetails, table, 0, "");

		Assert.assertFalse("Expecting false for at least one lot details with reservation but didn't.",
				this.listInventoryTable.isSelectedEntriesHasReservation(lotDetails,null));
	}

	private void updateReservationForLotEntries(final List<ListEntryLotDetails> lotEntries, final Table table, final double reservedVal,
			final String status) {
		@SuppressWarnings("unchecked") final Collection<ListEntryLotDetails> itemIds = (Collection<ListEntryLotDetails>) table.getItemIds();
		final Iterator<ListEntryLotDetails> itr = itemIds.iterator();
		while (itr.hasNext()) {
			final ListEntryLotDetails lotDetail = itr.next();
			final Item item = table.getItem(lotDetail);
			item.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(PREV_AVAIL_INVENTORY);
			item.getItemProperty(ColumnLabels.RESERVATION.getName()).setValue(reservedVal);
			lotEntries.add(lotDetail);
		}
	}
}

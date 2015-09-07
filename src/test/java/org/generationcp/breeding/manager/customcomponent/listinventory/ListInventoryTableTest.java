
package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Location;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class ListInventoryTableTest {

	private static final int NO_OF_LISTDATA = 5;

	private static final int NO_OF_LOTS_PER_LISTDATA = 5;

	private static final int LIST_ID = 1;

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

		Mockito.doReturn(this.createTerm("DESIGNATION")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.DESIGNATION.getTermId().getId());
		Mockito.doReturn(this.createTerm("LOCATION")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.doReturn(this.createTerm("UNITS")).when(this.ontologyDataManager).getTermById(ColumnLabels.UNITS.getTermId().getId());
		Mockito.doReturn(this.createTerm("AVAIL_INV")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(this.createTerm("TOTAL")).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());
		Mockito.doReturn(this.createTerm("RES")).when(this.ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());
		Mockito.doReturn(this.createTerm("NEW RES")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());
		Mockito.doReturn(this.createTerm("COMMENT")).when(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(this.createTerm("STOCKID")).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(this.createTerm("LOT_ID")).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());

		this.listInventoryTable.instantiateComponents();

		Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("LOCATION", table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		Assert.assertEquals("UNITS", table.getColumnHeader(ColumnLabels.UNITS.getName()));
		Assert.assertEquals("AVAIL_INV", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("TOTAL", table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals("RES", table.getColumnHeader(ColumnLabels.RESERVED.getName()));
		Assert.assertEquals("NEW RES", table.getColumnHeader(ColumnLabels.NEWLY_RESERVED.getName()));
		Assert.assertEquals("COMMENT", table.getColumnHeader(ColumnLabels.COMMENT.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("LOT_ID", table.getColumnHeader(ColumnLabels.LOT_ID.getName()));
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
	}

	@Test
	public void testDisplayInventoryDetails() {
		List<GermplasmListData> inventoryDetails = this.createGermplasmListDataWithInventoryDetails();
		this.listInventoryTable.displayInventoryDetails(inventoryDetails);

		int expectedNoOFLotEntries = NO_OF_LISTDATA * NO_OF_LOTS_PER_LISTDATA;
		Assert.assertEquals("Expecting that all entries from inventoryDetails are properly inserted in listinventory table but didn't.",
				expectedNoOFLotEntries, this.listInventoryTable.getTable().getContainerDataSource().size());
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

		int expectedNoOFLotEntries = NO_OF_LISTDATA * NO_OF_LOTS_PER_LISTDATA;
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
		List<GermplasmListData> inventoryDetails = this.createGermplasmListDataWithInventoryDetails();
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
		Iterator itr = itemIds.iterator();
		ListEntryLotDetails lotDetail = (ListEntryLotDetails) itr.next();
		Item item = table.getItem(lotDetail);
		CheckBox itemCheckBox = (CheckBox) item.getItemProperty(ColumnLabels.TAG.getName()).getValue();

		itemCheckBox.setValue(true);
		this.listInventoryTable.toggleSelectOnLotEntries(itemCheckBox);
		Assert.assertEquals("Expecting that only 1 checkbox is selected but didn't.", 1, this.listInventoryTable.getSelectedLots().size());

		itemCheckBox.setValue(false);
		this.listInventoryTable.toggleSelectOnLotEntries(itemCheckBox);
		Assert.assertEquals("Expecting that no checkbox is selected but didn't.", 0, this.listInventoryTable.getSelectedLots().size());
	}

	// ------------------------------------------ DATA INITIALIZATION HERE -----------------------------------------------------------------

	private Term createTerm(String name) {
		Term term = new Term();
		term.setName(name);
		term.setId(0);
		return term;
	}

	private List<GermplasmListData> createGermplasmListDataWithInventoryDetails() {
		List<GermplasmListData> inventoryDetails = new ArrayList<GermplasmListData>();

		for (int i = 0; i < NO_OF_LISTDATA; i++) {
			GermplasmListData listData = new GermplasmListData();
			int id = i + 1;
			listData.setEntryId(id);
			listData.setDesignation("Germplasm" + id);
			listData.setGid(id);
			listData.setInventoryInfo(this.createInventoryInfo(id));
			inventoryDetails.add(listData);
		}

		return inventoryDetails;
	}

	private ListDataInventory createInventoryInfo(int listDataId) {
		ListDataInventory inventoryInfo = new ListDataInventory(listDataId, listDataId);
		List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		for (int i = 0; i < NO_OF_LOTS_PER_LISTDATA; i++) {
			lotDetails.add(this.createLotDetail(i));
		}
		inventoryInfo.setLotRows(lotDetails);
		return inventoryInfo;
	}

	private ListEntryLotDetails createLotDetail(int i) {
		ListEntryLotDetails lotDetail = new ListEntryLotDetails();
		int id = i + 1;
		lotDetail.setId(id);
		lotDetail.setLotId(id);
		lotDetail.setLocationOfLot(this.createLocation(id));
		lotDetail.setScaleOfLot(this.createScale(id));
		lotDetail.setAvailableLotBalance(100D);
		lotDetail.setActualLotBalance(100D);
		lotDetail.setReservedTotalForEntry(100D);
		lotDetail.setCommentOfLot("Lot Comment" + id);
		lotDetail.setStockIds("STK1-1,STK2-2,STK-3");
		return lotDetail;
	}

	private Term createScale(int id) {
		Term scale = new Term();
		scale.setId(id);
		scale.setName("Scale" + id);
		return scale;
	}

	private Location createLocation(int id) {
		Location location = new Location();
		location.setLocid(id);
		location.setLname("Location" + id);
		return location;
	}

}


package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
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
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class ListManagerInventoryTableTest {

	private static final int LIST_ID = 1;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	protected InventoryDataManager inventoryDataManager;

	@Mock
	protected GermplasmListManager germplasmListManager;

	@Mock
	private ListManagerMain listManagerMain;

	@InjectMocks
	private ListManagerInventoryTable listInventoryTable = new ListManagerInventoryTable(this.listManagerMain,
			ListManagerInventoryTableTest.LIST_ID, true, true);

	private List<GermplasmListData> inventoryDetails;

	@Before
	public void setUp() throws MiddlewareQueryException {

		MockitoAnnotations.initMocks(this);

		this.listInventoryTable.instantiateComponents();

		this.inventoryDetails = ListInventoryTableUtil.createGermplasmListDataWithInventoryDetails();
		this.mockGetGermplasmListDataByListIdAndLrecId();

	}

	private void mockGetGermplasmListDataByListIdAndLrecId() {
		Map<Integer, GermplasmListData> inventoryDetailsMap = new HashMap<Integer, GermplasmListData>();
		for (GermplasmListData listData : this.inventoryDetails) {
			inventoryDetailsMap.put(listData.getId(), listData);
		}

		for (int i = 1; i <= ListInventoryTableUtil.getNumberOfEntries(); i++) {
			int id = (i % 5 == 0) ? 5 : i % 5;
			Mockito.doReturn(inventoryDetailsMap.get(id)).when(this.germplasmListManager)
					.getGermplasmListDataByListIdAndLrecId(ListManagerInventoryTableTest.LIST_ID, i);
		}
	}

	@Test
	public void testDisplayInventoryDetails() {
		this.listInventoryTable.displayInventoryDetails(this.inventoryDetails);
		Assert.assertEquals("Expecting that the inventory table is properly been filled.", ListInventoryTableUtil.getNumberOfEntries()
				.intValue(), this.listInventoryTable.getTable().size());
	}

	@Test
	public void testRetrieveGermplasmListDataUsingLrecIdWithException() {
		Mockito.doThrow(new MiddlewareQueryException("Some exception message here.")).when(this.germplasmListManager)
				.getGermplasmListDataByListIdAndLrecId(Mockito.anyInt(), Mockito.anyInt());

		ListEntryLotDetails lotDetail = new ListEntryLotDetails();
		lotDetail.setId(1);
		Assert.assertNull("Expecting a null return when there is an exception thrown.",
				this.listInventoryTable.retrieveGermplasmListDataUsingLrecId(lotDetail));
	}

	@Test
	public void testToggleSelectOnLotEntries() {
		this.listInventoryTable.displayInventoryDetails(this.inventoryDetails);

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

}


package org.generationcp.breeding.manager.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.Listener.LotDetailsButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.MethodTestDataInitializer;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmQueryTest {

	public static final String SEARCH_STRING = "Search String";
	public static final String TEST_21_LENGTH_STRING = ">20LENGTHSTRING_ABCDE";
	public static final String TEST_SHORT_STRING = "TEST_SHORT_STRING";
	public static final String TEST_STOCK_ID_STRING = "STOK_TEST01";
	public static final String GERMPLSM_SCALE = "kg";
	public static final String TEST_CROSS_EXPANSION_STRING = "TEST CROSS EXPANSION STRING";
	public static final String TEST_GERMPLASM_NAME = "TEST GERMPLASM NAME";
	public static final String TEST_DASH_STRING = "-";
	public static final int TEST_GID = 0;
	public static final Integer TEST_SEED_RES_COUNT = 10;
	public static final Integer TEST_INVENTORY_COUNT = 10;
	public static final int TEST_LOCATION_ID = 5;
	public static final int GERMPLASM_SIZE = 20;
	public static final Double AVAILABLE_BALANCE = 5.0d;

	private final String[] itemPropertyIds = new String[] {"LOCATIONS", "GROUP ID", "GID_REF", "Tag All Column", "LOTS", "AVAILABLE",
			"PARENTAGE", "METHOD NAME", "STOCKID", "NAMES", "GID"};
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private LocationDataManager locationDataManager;
	@Mock
	private PedigreeService pedigreeService;
	@Mock
	private CrossExpansionProperties crossExpansionProperties;
	private final GermplasmSearchParameter germplasmSearchParameter =
			new GermplasmSearchParameter(GermplasmQueryTest.SEARCH_STRING, Operation.LIKE);
	private final List<Germplasm> germplasms = new ArrayList<>();
	@InjectMocks
	private final GermplasmQuery query = new GermplasmQuery(Mockito.mock(ListManagerMain.class), false, false,
			this.germplasmSearchParameter, Mockito.mock(Table.class), Mockito.mock(QueryDefinition.class));

	@Before
	public void setUp() throws Exception {
		// create a test list of germplasms with inventory information
		final Map<Integer, String> pedigreeString = new HashMap<>();
		for (int i = 0; i < 20; i++) {
			GermplasmListTestDataInitializer.createGermplasmListWithListDataAndInventoryInfo(1, 10);

			final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(i);
			final GermplasmInventory inventoryInfo = new GermplasmInventory(germplasm.getGid());
			inventoryInfo.setStockIDs(GermplasmQueryTest.TEST_STOCK_ID_STRING);
			inventoryInfo.setActualInventoryLotCount(GermplasmQueryTest.TEST_INVENTORY_COUNT);
			inventoryInfo.setReservedLotCount(GermplasmQueryTest.TEST_SEED_RES_COUNT);
			inventoryInfo.setScaleForGermplsm(GermplasmQueryTest.GERMPLSM_SCALE);
			inventoryInfo.setTotalAvailableBalance(GermplasmQueryTest.AVAILABLE_BALANCE);
			germplasm.setInventoryInfo(inventoryInfo);
			pedigreeString.put(germplasm.getGid(), GermplasmQueryTest.TEST_CROSS_EXPANSION_STRING);
			this.germplasms.add(germplasm);
		}

		// initialize middleware service calls
		Mockito.when(
				this.pedigreeService.getCrossExpansions(Matchers.anySetOf(Integer.class), Matchers.anyInt(), Matchers.eq(this.crossExpansionProperties)))
				.thenReturn(pedigreeString);
		Mockito.when(this.germplasmDataManager.searchForGermplasm(this.germplasmSearchParameter)).thenReturn(this.germplasms);
		Mockito.when(this.germplasmDataManager.countSearchForGermplasm(Matchers.eq(GermplasmQueryTest.SEARCH_STRING),
				Matchers.eq(Operation.LIKE), Matchers.eq(this.germplasmSearchParameter.isIncludeParents()),
				Matchers.eq(this.germplasmSearchParameter.isWithInventoryOnly()),
				Matchers.eq(this.germplasmSearchParameter.isIncludeMGMembers()))).thenReturn(this.germplasms.size());

		Mockito.when(this.germplasmDataManager.getNamesByGID(Matchers.anyInt(), Matchers.isNull(Integer.class),
				Matchers.isNull(GermplasmNameType.class))).thenReturn(GermplasmTestDataInitializer.createNameList(this.germplasms.size()));

		Mockito.when(this.germplasmDataManager.getMethodByID(Matchers.anyInt()))
				.thenReturn(new MethodTestDataInitializer().createMethod(1, "testMethodType"));

		Mockito.when(this.locationDataManager.getLocationByID(Matchers.anyInt()))
				.thenReturn(new Location(GermplasmQueryTest.TEST_LOCATION_ID));

	}

	@Test
	public void testGetGermplasmItem() throws Exception {
		final Germplasm germplasm = this.germplasms.get(0);
		final Item item = this.query.getGermplasmItem(this.germplasms.get(0), 1,
				Collections.<Integer, String>singletonMap(germplasm.getGid(), GermplasmQueryTest.TEST_CROSS_EXPANSION_STRING),
				Collections.<Integer, String>singletonMap(germplasm.getGid(), GermplasmQueryTest.TEST_GERMPLASM_NAME));

		final List<String> itemPropertyIDList = Arrays.asList(this.itemPropertyIds);

		Assert.assertNotNull("getGermplasmItem should return an item object", item);
		Assert.assertTrue("the formed item object should contain the property ids",
				item.getItemPropertyIds().containsAll(itemPropertyIDList) && itemPropertyIDList.containsAll(item.getItemPropertyIds()));

		// The following asserts should just verify the content / values of the item object given itemPropertyId
		Assert.assertEquals("LocationName", item.getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).getValue());
		Assert.assertEquals(GermplasmQueryTest.TEST_DASH_STRING, item.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
		Assert.assertEquals(GermplasmQueryTest.TEST_GID, item.getItemProperty(ColumnLabels.GID.getName() + "_REF").getValue());
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof CheckBox);

		Assert.assertEquals(GermplasmQueryTest.TEST_CROSS_EXPANSION_STRING,
				item.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
		Assert.assertEquals("MethodName", item.getItemProperty(ColumnLabels.BREEDING_METHOD_NAME.getName()).getValue());
		Assert.assertEquals(GermplasmQueryTest.TEST_STOCK_ID_STRING,
				((Label) item.getItemProperty(ColumnLabels.STOCKID.getName()).getValue()).getValue());
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof Button);
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof Button);

		// Check value and action listener for property "LOTS" (# of lots with available inventory)
		final Button availableLot = (Button) item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
		Assert.assertEquals(GermplasmQueryTest.TEST_INVENTORY_COUNT.toString(), availableLot.getCaption().toString());
		Collection<?> listeners = availableLot.getListeners(Button.ClickEvent.class);
		Assert.assertNotNull(listeners);
		Assert.assertTrue(listeners.size() == 1);
		Assert.assertTrue(listeners.iterator().next() instanceof LotDetailsButtonClickListener);

		// Check value and action listener for property "AVAILABLE" (total available inventory
		final Button availableBalance = (Button) item.getItemProperty(ColumnLabels.TOTAL.getName()).getValue();
		Assert.assertEquals(GermplasmQueryTest.AVAILABLE_BALANCE + " " + GermplasmQueryTest.GERMPLSM_SCALE,
				availableBalance.getCaption().toString());
		listeners = availableBalance.getListeners(Button.ClickEvent.class);
		Assert.assertNotNull(listeners);
		Assert.assertTrue(listeners.size() == 1);
		Assert.assertTrue(listeners.iterator().next() instanceof InventoryLinkButtonClickListener);

	}

	@Test
	public void testLoadItems() throws Exception {
		assert this.query != null;

		final List<Item> items = this.query.loadItems(0, GermplasmQueryTest.GERMPLASM_SIZE);

		// Verify number of loaded items plus that key Middleware methods were called
		Assert.assertEquals("Should return the correct number of items", 20, items.size());
		Mockito.verify(this.pedigreeService, Mockito.times(1)).getCrossExpansions(Matchers.anySetOf(Integer.class), Matchers.anyInt(),
				Matchers.any(CrossExpansionProperties.class));
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getPreferredNamesByGids(Matchers.anyListOf(Integer.class));

	}

	@Test
	public void testGetGermplasmSearchResults() throws Exception {
		Assert.assertEquals("Verify the call should return the expected list of germplasms", this.germplasms,
				this.query.getGermplasmSearchResults(1, 20));
	}

	@Test
	public void testSize() throws Exception {
		Assert.assertEquals("The count call should be the same with the test germplasm list", this.germplasms.size(), this.query.size());
	}

	@Test
	public void testGetShortenedNames() throws Exception {
		final String resultWithEllipses = this.query.getShortenedNames(GermplasmQueryTest.TEST_21_LENGTH_STRING);
		final String resultWithoutElipses = this.query.getShortenedNames(GermplasmQueryTest.TEST_SHORT_STRING);

		Assert.assertEquals(resultWithEllipses, GermplasmQueryTest.TEST_21_LENGTH_STRING.substring(0, 20) + "...");
		Assert.assertEquals(resultWithoutElipses, GermplasmQueryTest.TEST_SHORT_STRING);

	}
}

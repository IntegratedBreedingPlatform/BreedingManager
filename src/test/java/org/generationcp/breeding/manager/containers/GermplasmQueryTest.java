package org.generationcp.breeding.manager.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.MethodTestDataInitializer;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pedigree.Pedigree;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
	public static final String TEST_CROSS_EXPANSION_STRING = "TEST CROSS EXPANSION STRING";
	public static final String TEST_DASH_STRING = "-";
	public static final int TEST_GID = 0;
	public static final Integer TEST_SEED_RES_COUNT = 10;
	public static final Integer TEST_INVENTORY_COUNT = 10;
	public static final int TEST_LOCATION_ID = 5;
	public static final int GERMPLASM_SIZE = 20;
	private final String[] itemPropertyIds =
			new String[] {"LOCATIONS", "GROUP ID", "GID_REF", "Tag All Column", "SEED RES", "LOTS AVAILABLE", "PARENTAGE", "METHOD NAME",
					"STOCKID", "NAMES", "GID"};
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private LocationDataManager locationDataManager;
	@Mock
	private PedigreeService pedigreeService;
	@Mock
	private CrossExpansionProperties crossExpansionProperties;
	private GermplasmSearchParameter germplasmSearchParameter = new GermplasmSearchParameter(SEARCH_STRING, Operation.LIKE);
	private List<Germplasm> germplasms = new ArrayList<>();
	@InjectMocks
	private GermplasmQuery query =
			new GermplasmQuery(Mockito.mock(ListManagerMain.class), false, false, germplasmSearchParameter, Mockito.mock(Table.class),
					Mockito.mock(QueryDefinition.class));

	@Before
	public void setUp() throws Exception {
		// create a test list of germplasms with inventory information
		for (int i = 0; i < 20; i++) {
			GermplasmListTestDataInitializer.createGermplasmListWithListDataAndInventoryInfo(1, 10);

			final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(i);
			final GermplasmInventory inventoryInfo = new GermplasmInventory(germplasm.getGid());
			inventoryInfo.setStockIDs(TEST_STOCK_ID_STRING);
			inventoryInfo.setActualInventoryLotCount(TEST_INVENTORY_COUNT);
			inventoryInfo.setReservedLotCount(TEST_SEED_RES_COUNT);
			germplasm.setInventoryInfo(inventoryInfo);
			germplasms.add(germplasm);
		}

		// initialize middleware service calls
		Mockito.when(this.pedigreeService.getCrossExpansion(Mockito.anyInt(), Mockito.eq(this.crossExpansionProperties)))
				.thenReturn(TEST_CROSS_EXPANSION_STRING);
		Mockito.when(this.germplasmDataManager.searchForGermplasm(germplasmSearchParameter)).thenReturn(germplasms);
		Mockito.when(this.germplasmDataManager.countSearchForGermplasm(Mockito.eq(SEARCH_STRING), Mockito.eq(Operation.LIKE),
				Mockito.eq(germplasmSearchParameter.isIncludeParents()), Mockito.eq(germplasmSearchParameter.isWithInventoryOnly()),
				Mockito.eq(germplasmSearchParameter.isIncludeMGMembers()))).thenReturn(germplasms.size());

		Mockito.when(this.germplasmDataManager
				.getNamesByGID(Mockito.anyInt(), Mockito.isNull(Integer.class), Mockito.isNull(GermplasmNameType.class)))
				.thenReturn(GermplasmTestDataInitializer.createNameList(germplasms.size()));

		Mockito.when(this.germplasmDataManager.getMethodByID(Mockito.anyInt()))
				.thenReturn(new MethodTestDataInitializer().createMethod(1, "testMethodType"));

		Mockito.when(this.locationDataManager.getLocationByID(Mockito.anyInt())).thenReturn(new Location(TEST_LOCATION_ID));

	}

	@Test
	public void testGetGermplasmItem() throws Exception {
	  	ContextHolder.setCurrentCrop("maize");
		Item item = this.query.getGermplasmItem(germplasms.get(0), 1);

		final List<String> itemPropertyIDList = Arrays.asList(itemPropertyIds);

		Assert.assertNotNull("getGermplasmItem should return an item object", item);
		Assert.assertTrue("the formed item object should contain the property ids",
				item.getItemPropertyIds().containsAll(itemPropertyIDList) && itemPropertyIDList.containsAll(item.getItemPropertyIds()));

		// The following asserts should jist verify the content / values of the item object given itemPropertyId
		Assert.assertEquals("LocationName", item.getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).getValue());
		Assert.assertEquals(TEST_DASH_STRING, item.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
		Assert.assertEquals(TEST_GID, item.getItemProperty(ColumnLabels.GID.getName() + "_REF").getValue());
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof CheckBox);
		Assert.assertEquals(TEST_SEED_RES_COUNT.toString(), item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());
		Assert.assertEquals(TEST_INVENTORY_COUNT.toString(), item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue());
		Assert.assertEquals(TEST_CROSS_EXPANSION_STRING, item.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
		Assert.assertEquals("MethodName", item.getItemProperty(ColumnLabels.BREEDING_METHOD_NAME.getName()).getValue());
		Assert.assertEquals(TEST_STOCK_ID_STRING, ((Label) item.getItemProperty(ColumnLabels.STOCKID.getName()).getValue()).getValue());
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof Button);
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof Button);

	}

	@Test
	public void testLoadItems() throws Exception {
		assert query != null;

		List<Item> items = this.query.loadItems(0, GERMPLASM_SIZE);

		Assert.assertEquals("Should return the correct number of items", 20, items.size());

	}

	@Test
	public void testGetGermplasmSearchResults() throws Exception {
		Assert.assertEquals("Verify the call should return the expected list of germplasms", germplasms,
				this.query.getGermplasmSearchResults(1, 20));
	}

	@Test
	public void testSize() throws Exception {
		Assert.assertEquals("The count call should be the same with the test germplasm list", germplasms.size(), this.query.size());
	}

	@Test
	public void testGetShortenedNames() throws Exception {
		String resultWithEllipses = this.query.getShortenedNames(TEST_21_LENGTH_STRING);
		String resultWithoutElipses = this.query.getShortenedNames(TEST_SHORT_STRING);

		Assert.assertEquals(resultWithEllipses, TEST_21_LENGTH_STRING.substring(0, 20) + "...");
		Assert.assertEquals(resultWithoutElipses, TEST_SHORT_STRING);

	}

  	@Test
  	public void testGetGermplasmItemWithPedigreeString() throws Exception {
	  	ContextHolder.setCurrentCrop("maize");

	  	Germplasm germplasm = germplasms.get(0);
	  	Pedigree pedigree = new Pedigree();
	  	pedigree.setPedigreeString("pedigreeString");
	  	pedigree.setAlgorithmUsed("default");
	  	pedigree.setLevels(0);
	  	pedigree.setInvalidate(0);
	  	germplasm.setPedigree(pedigree);

	  	Mockito.when(this.crossExpansionProperties.getProfile()).thenReturn("default");
	  	Mockito.when(this.crossExpansionProperties.getCropGenerationLevel(Mockito.anyString())).thenReturn(0);

	  	Item item = this.query.getGermplasmItem(germplasm, 1);

		// The following asserts should jist verify the content / values of the item object given itemPropertyId
	  	Assert.assertEquals("LocationName", item.getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).getValue());
		Assert.assertEquals(TEST_DASH_STRING, item.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
		Assert.assertEquals(TEST_GID, item.getItemProperty(ColumnLabels.GID.getName() + "_REF").getValue());
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof CheckBox);
		Assert.assertEquals(TEST_SEED_RES_COUNT.toString(), item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());
		Assert.assertEquals(TEST_INVENTORY_COUNT.toString(), item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue());
		Assert.assertEquals("pedigreeString", item.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
		Assert.assertEquals("MethodName", item.getItemProperty(ColumnLabels.BREEDING_METHOD_NAME.getName()).getValue());
		Assert.assertEquals(TEST_STOCK_ID_STRING, ((Label) item.getItemProperty(ColumnLabels.STOCKID.getName()).getValue()).getValue());
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof Button);
		Assert.assertTrue(item.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).getValue() instanceof Button);

  }
}

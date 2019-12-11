
package org.generationcp.breeding.manager.containers;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import junit.framework.Assert;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.Listener.LotDetailsButtonClickListener;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmQueryTest {

	private static final int NUMBER_OF_ITEMS_ON_PAGE = 20;
	private static final int ALL_GERMPLASM = 35;
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
	public static final Double AVAILABLE_BALANCE = 5.0d;
	public static final String ORI_COUN = "ORI_COUN";
	public static final String NOTE = "NOTE";
	public static final String NAMETYPE1 = "DRVNM";
	public static final String NAMETYPE2 = "CROSS";
	

	private static final String[] STANDARD_COLUMNS = {"Tag All Column", "GROUP ID", "PARENTAGE", "AVAILABLE", "GID", "NAMES", "LOCATIONS",
			"METHOD NAME", "GID_REF", "LOTS", "STOCKID"};
	private final List<String> itemPropertyIds = new ArrayList<>();
	
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private PedigreeService pedigreeService;
	@Mock
	private CrossExpansionProperties crossExpansionProperties;
	private final GermplasmSearchParameter germplasmSearchParameter =
			new GermplasmSearchParameter(GermplasmQueryTest.SEARCH_STRING, Operation.LIKE);
	private final List<Germplasm> allGermplasm = new ArrayList<>();
	private final List<Germplasm> currentGermplasm = new ArrayList<>();
	private final List<Integer> gids = new ArrayList<>();
	private GermplasmSearchParameter searchAllParameter;
			
	@InjectMocks
	private final GermplasmQuery query = new GermplasmQuery(Mockito.mock(ListManagerMain.class), false, false,
			this.germplasmSearchParameter, Mockito.mock(Table.class), Mockito.mock(QueryDefinition.class));

	@Before
	public void setUp() throws Exception {
		this.itemPropertyIds.addAll(Arrays.asList(STANDARD_COLUMNS));
		this.itemPropertyIds.addAll(ColumnLabels.getAddableGermplasmColumns());
		this.itemPropertyIds.add(ORI_COUN);
		this.itemPropertyIds.add(NOTE);
		this.itemPropertyIds.add(NAMETYPE1);
		this.itemPropertyIds.add(NAMETYPE2);

		//In added column menu CROSS_FEMALE_GID and CROSS_MALE_GID is still use but the property is FGID and MGID
		this.itemPropertyIds.remove(ColumnLabels.CROSS_FEMALE_GID.getName());
		this.itemPropertyIds.add(ColumnLabels.FGID.getName());
		this.itemPropertyIds.remove(ColumnLabels.CROSS_MALE_GID.getName());
		this.itemPropertyIds.add(ColumnLabels.MGID.getName());

		// create a test list of germplasms with inventory information
		final Map<Integer, String> pedigreeString = new HashMap<>();
		final Map<Integer, String> groupSourcepreferredNamesMap = new HashMap<>();
		final Map<Integer, String> immediatepreferredNamesMap = new HashMap<>();

		for (int i = 0; i < ALL_GERMPLASM; i++) {
			GermplasmListTestDataInitializer.createGermplasmListWithListDataAndInventoryInfo(1, 10);

			final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(i);
			final Integer gid = germplasm.getGid();
			final GermplasmInventory inventoryInfo = new GermplasmInventory(gid);
			inventoryInfo.setStockIDs(GermplasmQueryTest.TEST_STOCK_ID_STRING);
			inventoryInfo.setActualInventoryLotCount(GermplasmQueryTest.TEST_INVENTORY_COUNT);
			inventoryInfo.setReservedLotCount(GermplasmQueryTest.TEST_SEED_RES_COUNT);
			inventoryInfo.setScaleForGermplsm(GermplasmQueryTest.GERMPLSM_SCALE);
			inventoryInfo.setTotalAvailableBalance(GermplasmQueryTest.AVAILABLE_BALANCE);
			germplasm.setInventoryInfo(inventoryInfo);
			pedigreeString.put(gid, GermplasmQueryTest.TEST_CROSS_EXPANSION_STRING);
			germplasm.setGermplasmNamesString("NAME 1, NAME 2, NAME 3");
			germplasm.setGermplasmDate("20050101");
			germplasm.setGermplasmPeferredId("Preferred Id");
			germplasm.setGermplasmPeferredName("Preferred Name");
			germplasm.setMethodCode("ABC");
			germplasm.setMethodId(100);
			germplasm.setMethodGroup("123");
			germplasm.setFemaleParentPreferredID("101");
			germplasm.setFemaleParentPreferredName("Female Preferred Name");
			germplasm.setMaleParentPreferredID("102");
			germplasm.setMaleParentPreferredName("Male Preferred Name");
			germplasm.setGroupSourceGID("-");
			germplasm.setGroupSourcePreferredName("-");
			germplasm.setImmediateSourceGID("-");
			germplasm.setImmediateSourcePreferredName("-");
			immediatepreferredNamesMap.put(gid,"AA");
			groupSourcepreferredNamesMap.put(gid,"-");
			
			final Map<String, String> attributesMap = new HashMap<>();
			attributesMap.put(ORI_COUN, ORI_COUN + gid);
			attributesMap.put(NOTE, NOTE + gid);
			germplasm.setAttributeTypesValueMap(attributesMap);
			
			final Map<String, String> namesMap = new HashMap<>();
			namesMap.put(NAMETYPE1, NAMETYPE1 + gid);
			namesMap.put(NAMETYPE2, NAMETYPE2 + gid);
			germplasm.setNameTypesValueMap(namesMap);

			if (i < NUMBER_OF_ITEMS_ON_PAGE){
				this.currentGermplasm.add(germplasm);
			} 
			this.allGermplasm.add(germplasm);
			this.gids.add(gid);

		}

		// initialize middleware service calls
		Mockito.when(
			this.pedigreeService.getCrossExpansions(Matchers.anySetOf(Integer.class), ArgumentMatchers.isNull(Integer.class),
				Matchers.eq(this.crossExpansionProperties)))
			.thenReturn(pedigreeString);
		Mockito.when(this.germplasmDataManager.searchForGermplasm(this.germplasmSearchParameter)).thenReturn(this.currentGermplasm);
		Mockito.when(this.germplasmDataManager.retrieveGidsOfSearchGermplasmResult(this.germplasmSearchParameter)).thenReturn(new HashSet<Integer>(this.gids));

		this.searchAllParameter = new GermplasmSearchParameter(this.germplasmSearchParameter);
		this.searchAllParameter.setStartingRow(0);
		this.searchAllParameter.setNumberOfEntries(GermplasmQuery.RESULTS_LIMIT);
	}

	@Test
	public void testGetGermplasmItem() throws Exception {
		final Germplasm germplasm = this.allGermplasm.get(0);
		final Item item = this.query.getGermplasmItem(germplasm, 1,
			Collections.singletonMap(germplasm.getGid(), GermplasmQueryTest.TEST_CROSS_EXPANSION_STRING),
			Collections.singletonMap(germplasm.getGid(), GermplasmQueryTest.TEST_GERMPLASM_NAME));

		final List<String> itemPropertyIDList = this.itemPropertyIds;

		Assert.assertNotNull("getGermplasmItem should return an item object", item);
		Assert.assertTrue("the formed item object should contain the property ids",
				item.getItemPropertyIds().containsAll(itemPropertyIDList) && itemPropertyIDList.containsAll(item.getItemPropertyIds()));

		// The following asserts should just verify the content / values of the item object given itemPropertyId
		Assert.assertEquals("LocationName", item.getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).getValue());
		Assert.assertEquals(GermplasmQueryTest.TEST_DASH_STRING, item.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue());
		Assert.assertEquals(GermplasmQueryTest.TEST_GID, item.getItemProperty(GermplasmQuery.GID_REF_PROPERTY).getValue());
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
		
		final Button namesButton = (Button) item.getItemProperty(GermplasmSearchResultsComponent.NAMES).getValue();
		Assert.assertEquals(germplasm.getGermplasmNamesString().substring(0, 20) + "...", namesButton.getCaption());
		Assert.assertEquals(germplasm.getGermplasmNamesString(), namesButton.getDescription());
		listeners = namesButton.getListeners(Button.ClickEvent.class);
		Assert.assertNotNull(listeners);
		Assert.assertTrue(listeners.size() == 1);
		Assert.assertTrue(listeners.iterator().next() instanceof GidLinkButtonClickListener);
		
		// Verify added columns
		Assert.assertEquals(germplasm.getGermplasmPeferredName(), item.getItemProperty(ColumnLabels.PREFERRED_NAME.getName()).getValue());
		Assert.assertEquals(germplasm.getGermplasmPeferredId(), item.getItemProperty(ColumnLabels.PREFERRED_ID.getName()).getValue());
		Assert.assertEquals(germplasm.getGermplasmDate(), item.getItemProperty(ColumnLabels.GERMPLASM_DATE.getName()).getValue());
		Assert.assertEquals(germplasm.getMethodCode(), item.getItemProperty(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).getValue());
		Assert.assertEquals(germplasm.getMethodId(), item.getItemProperty(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).getValue());
		Assert.assertEquals(germplasm.getFemaleParentPreferredID(), item.getItemProperty(ColumnLabels.FGID.getName()).getValue());
		Assert.assertEquals(germplasm.getFemaleParentPreferredName(), item.getItemProperty(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).getValue());
		Assert.assertEquals(germplasm.getMaleParentPreferredID(), item.getItemProperty(ColumnLabels.MGID.getName()).getValue());
		Assert.assertEquals(germplasm.getMaleParentPreferredName(), item.getItemProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).getValue());
		Assert.assertEquals(germplasm.getGroupSourceGID(), item.getItemProperty(ColumnLabels.GROUP_SOURCE_GID.getName()).getValue());
		Assert.assertEquals(germplasm.getGroupSourcePreferredName(), item.getItemProperty(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).getValue());
		Assert.assertEquals(germplasm.getImmediateSourceGID(), item.getItemProperty(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).getValue());
		Assert.assertEquals(germplasm.getImmediateSourcePreferredName(), item.getItemProperty(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).getValue());
		
		// Verify Attribute and Name Type values
		Assert.assertEquals(ORI_COUN + GermplasmQueryTest.TEST_GID, item.getItemProperty(ORI_COUN).getValue());
		Assert.assertEquals(NOTE + GermplasmQueryTest.TEST_GID, item.getItemProperty(NOTE).getValue());
		Assert.assertEquals(NAMETYPE1 + GermplasmQueryTest.TEST_GID, item.getItemProperty(NAMETYPE1).getValue());
		Assert.assertEquals(NAMETYPE2 + GermplasmQueryTest.TEST_GID, item.getItemProperty(NAMETYPE2).getValue());
	}

	@Test
	public void testLoadItems() throws Exception {
		assert this.query != null;

		final List<Item> items = this.query.loadItems(0, GermplasmQueryTest.NUMBER_OF_ITEMS_ON_PAGE);

		// Verify number of loaded items plus that key Middleware methods were called
		Assert.assertEquals("Should return the correct number of items", NUMBER_OF_ITEMS_ON_PAGE, items.size());
		Mockito.verify(this.pedigreeService, Mockito.times(1))
			.getCrossExpansions(Matchers.anySetOf(Integer.class), ArgumentMatchers.isNull(Integer.class),
				Matchers.any(CrossExpansionProperties.class));
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getPreferredNamesByGids(Matchers.anyListOf(Integer.class));

	}

	@Test
	public void testGetGermplasmSearchResults() throws Exception {
		Assert.assertEquals("Verify the call should return the expected list of germplasms", this.currentGermplasm,
				this.query.getGermplasmSearchResults(1, NUMBER_OF_ITEMS_ON_PAGE));
	}

	@Test
	public void testSize() throws Exception {
		Assert.assertEquals("The count call should be the same with the test germplasm list", this.allGermplasm.size(), this.query.size());
		Mockito.verify(this.germplasmDataManager).retrieveGidsOfSearchGermplasmResult(this.germplasmSearchParameter);
	}

	@Test
	public void testGetShortenedNames() throws Exception {
		final String resultWithEllipses = this.query.getShortenedNames(GermplasmQueryTest.TEST_21_LENGTH_STRING);
		final String resultWithoutElipses = this.query.getShortenedNames(GermplasmQueryTest.TEST_SHORT_STRING);

		Assert.assertEquals(resultWithEllipses, GermplasmQueryTest.TEST_21_LENGTH_STRING.substring(0, 20) + "...");
		Assert.assertEquals(resultWithoutElipses, GermplasmQueryTest.TEST_SHORT_STRING);

	}
	
	@Test
	public void testRetrieveGIDsofMatchingGermplasm() {
		this.query.retrieveGIDsofMatchingGermplasm();

		// compare the gid lists independent of order
		Assert.assertEquals(new HashSet<>(this.gids), new HashSet<>(this.query.getAllGids()));
	}

	@Test
	public void testGetPropertyIdsOfAddableColumns() {

		final List<String> propertyIdsDefinition = new ArrayList<>();

		// Add default table propertyIds
		propertyIdsDefinition.add(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID);
		propertyIdsDefinition.add(GermplasmSearchResultsComponent.NAMES);
		propertyIdsDefinition.add(ColumnLabels.PARENTAGE.getName());
		propertyIdsDefinition.add(ColumnLabels.AVAILABLE_INVENTORY.getName());
		propertyIdsDefinition.add(ColumnLabels.TOTAL.getName());
		propertyIdsDefinition.add(ColumnLabels.STOCKID.getName());
		propertyIdsDefinition.add(ColumnLabels.GID.getName());
		propertyIdsDefinition.add(ColumnLabels.GROUP_ID.getName());
		propertyIdsDefinition.add(ColumnLabels.GERMPLASM_LOCATION.getName());
		propertyIdsDefinition.add(ColumnLabels.BREEDING_METHOD_NAME.getName());
		propertyIdsDefinition.add(GermplasmQuery.GID_REF_PROPERTY);

		// Add expected addable columns
		propertyIdsDefinition.add(ColumnLabels.PREFERRED_ID.getName());
		propertyIdsDefinition.add(ColumnLabels.PREFERRED_NAME.getName());
		propertyIdsDefinition.add(ColumnLabels.GERMPLASM_DATE.getName());
		propertyIdsDefinition.add(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		propertyIdsDefinition.add(ColumnLabels.BREEDING_METHOD_NUMBER.getName());
		propertyIdsDefinition.add(ColumnLabels.BREEDING_METHOD_GROUP.getName());
		propertyIdsDefinition.add(ColumnLabels.CROSS_FEMALE_GID.getName());
		propertyIdsDefinition.add(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		propertyIdsDefinition.add(ColumnLabels.CROSS_MALE_GID.getName());
		propertyIdsDefinition.add(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());

		propertyIdsDefinition.add(ColumnLabels.GROUP_SOURCE_GID.getName());
		propertyIdsDefinition.add(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName());
		propertyIdsDefinition.add(ColumnLabels.IMMEDIATE_SOURCE_GID.getName());
		propertyIdsDefinition.add(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName());


		// Add attribute type property Id
		propertyIdsDefinition.add(ORI_COUN);
		
		final List<String> result = this.query.getPropertyIdsOfAddedColumns(propertyIdsDefinition);

		Assert.assertFalse(result.contains(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID));
		Assert.assertFalse(result.contains(GermplasmSearchResultsComponent.NAMES));
		Assert.assertFalse(result.contains(ColumnLabels.PARENTAGE.getName()));
		Assert.assertFalse(result.contains(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertFalse(result.contains(ColumnLabels.TOTAL.getName()));
		Assert.assertFalse(result.contains(ColumnLabels.STOCKID.getName()));
		Assert.assertFalse(result.contains(ColumnLabels.GID.getName()));
		Assert.assertFalse(result.contains(ColumnLabels.GROUP_ID.getName()));
		Assert.assertFalse(result.contains(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertFalse(result.contains(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		Assert.assertFalse(result.contains(GermplasmQuery.GID_REF_PROPERTY));

		// Only the expected addable columns and attribute and name type property Ids should be included in the
		// result.
		Assert.assertTrue(result.contains(ColumnLabels.PREFERRED_ID.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.PREFERRED_NAME.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.GERMPLASM_DATE.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.BREEDING_METHOD_NUMBER.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.BREEDING_METHOD_GROUP.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.CROSS_FEMALE_GID.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.CROSS_MALE_GID.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.GROUP_SOURCE_GID.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()));
		Assert.assertTrue(result.contains(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()));


		Assert.assertTrue(result.contains(ORI_COUN));

	}
}

package org.generationcp.breeding.manager.containers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.manager.Operation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.vaadin.addons.lazyquerycontainer.Query;

import com.vaadin.ui.Table;

public class GermplasmQueryFactoryTest {

	public static final int TEST_SAMPLE_QUERY_SIZE = 10;
	private static final List<Integer> GID_LIST = Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
	private GermplasmQueryFactory germplasmQueryFactory;
	private GermplasmSearchParameter germplasmSearchParameter;

	private HashMap<String, Boolean> testSortState;

	@Before
	public void setUp() throws Exception {
		ListManagerMain listManagerMain = Mockito.mock(ListManagerMain.class);
		germplasmSearchParameter = new GermplasmSearchParameter("test string", Operation.LIKE);

		this.germplasmQueryFactory =
				new GermplasmQueryFactory(listManagerMain, false, true, germplasmSearchParameter, Mockito.mock(Table.class));

		// init testSortState
		testSortState = new LinkedHashMap<>();
		testSortState.put(ColumnLabels.SEED_RESERVATION.getName(), true);
		testSortState.put(ColumnLabels.STOCKID.getName(), true);
		testSortState.put(ColumnLabels.GROUP_ID.getName(), false);
	}

	@Test
	public void testConstructQuery() throws Exception {
		// create a query given the current table's sort state
		final Query query = this.germplasmQueryFactory.constructQuery(testSortState.keySet().toArray(new String[testSortState.size()]),
				ArrayUtils.toPrimitive(testSortState.values().toArray(new Boolean[testSortState.size()])));

		Assert.assertNotNull("Verify that we have a query object created", query);

		// verify that the sort state of the germplasm parameter has been initialized
		Assert.assertEquals("Makes sure that we have a sort state generated", germplasmSearchParameter.getSortState().size(),
				testSortState.size());

		Assert.assertArrayEquals("Check if the generated sortState propertyIds is equal to our test data",
				testSortState.keySet().toArray(new String[testSortState.keySet().size()]),
				germplasmSearchParameter.getSortState().keySet().toArray(new String[testSortState.keySet().size()]));
		Assert.assertArrayEquals("Check if the generated sortState boolean states is equal to our test data",
				testSortState.values().toArray(new Boolean[testSortState.keySet().size()]),
				germplasmSearchParameter.getSortState().values().toArray(new Boolean[testSortState.keySet().size()]));

	}

	@Test
	public void testGetNumberOfItems() throws Exception {
		final Query query = this.germplasmQueryFactory.constructQuery(testSortState.keySet().toArray(new String[testSortState.size()]),
				ArrayUtils.toPrimitive(testSortState.values().toArray(new Boolean[testSortState.size()])));

		// set a test size on the query so we can bypass the middleware service call
		ReflectionTestUtils.setField(query, "size", TEST_SAMPLE_QUERY_SIZE);

		Assert.assertEquals("Query count/size should be the same", TEST_SAMPLE_QUERY_SIZE, this.germplasmQueryFactory.getNumberOfItems());

	}
	
	@Test
	public void testGetAllGids() {
		final GermplasmQuery query = Mockito.mock(GermplasmQuery.class);
		Mockito.doReturn(GID_LIST).when(query).getAllGids();
		this.germplasmQueryFactory.setQuery(query);
		
		final List<Integer> allGids = this.germplasmQueryFactory.getAllGids();
		Mockito.verify(query).getAllGids();
		Assert.assertEquals(GID_LIST, allGids);
	}
}

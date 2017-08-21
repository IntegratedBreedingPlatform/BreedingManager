
package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryView;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class PagedBreedingManagerTableTest {

	public static final String PAGELENGTH = "pagelength";
	private static final int NO_OF_ENTRIES = 10;
	private static final int INIT_RECORD_COUNT = 10;
	private static final int MAX_RECORD_COUNT = 20;
	private static final int BATCH_SIZE = 5;
	private PagedBreedingManagerTable pagedTable;
	private TableMultipleSelectionHandler tableHandler;

	@Captor
	private ArgumentCaptor<Map<String, Object>> modifiedVariableMapCaptor;

	@Mock
	private ListManagerMain listManagerMain;
	
	@Before
	public void setUp() {
		this.pagedTable =
				new PagedBreedingManagerTable(PagedBreedingManagerTableTest.INIT_RECORD_COUNT,
						PagedBreedingManagerTableTest.MAX_RECORD_COUNT);
		// we need to be able to stub the handler call so we can use mockito's verify methods against it
		this.tableHandler = Mockito.mock(TableMultipleSelectionHandler.class);
		this.pagedTable.setTableHandler(this.tableHandler);
	}

	@Test
	public void testChangeVariables() throws Exception {

		final Map<String, Object> variableMap = new HashMap<>();
		variableMap.put(PagedBreedingManagerTableTest.PAGELENGTH, 25);

		// we need to partially stub the pagedTable, note that we are not suppressing/stubbing out the function calls
		// we just need to have the arguments captured to verify and assert its contents
		final PagedBreedingManagerTable pagedTableStub = Mockito.spy(this.pagedTable);

		final Object sourceMock = Mockito.mock(Object.class);

		// do actual call
		pagedTableStub.changeVariables(sourceMock, variableMap);

		// capture the resulting updated variable map
		Mockito.verify(pagedTableStub, Mockito.times(1)).doChangeVariables(Matchers.eq(sourceMock),
				this.modifiedVariableMapCaptor.capture());

		// assert the captured variable does not contain 'pagelength' anymore
		Assert.assertFalse("VariableMap passed to the table's super.changeVariable() should not contain pagelength",
				this.modifiedVariableMapCaptor.getValue().containsKey(PagedBreedingManagerTableTest.PAGELENGTH));

		// check whether we have set the value for selected items
		Mockito.verify(this.tableHandler, Mockito.times(1)).setValueForSelectedItems();
	}

	@Test
	public void testHasItemsFalse() {
		this.setupContainerDataSource(0);
		final boolean hasItems = this.pagedTable.hasItems();
		Assert.assertFalse("The table should be empty", hasItems);
	}

	@Test
	public void testHasItemsTrue() {
		this.setupContainerDataSource(PagedBreedingManagerTableTest.NO_OF_ENTRIES);
		final boolean hasItems = this.pagedTable.hasItems();
		Assert.assertTrue("The table should not be empty", hasItems);
	}

	@Test
	public void testUpdateBatchSizeBatchSizeUpdated() {
		this.setupContainerDataSource(PagedBreedingManagerTableTest.NO_OF_ENTRIES);

		final int batchSize = this.pagedTable.getBatchSize();
		final int newPageLength = this.pagedTable.getPageLength() + 5;
		this.pagedTable.setPageLength(newPageLength);
		this.pagedTable.updateBatchsize();
		final int updatedBatchSize = this.pagedTable.getBatchSize();
		Assert.assertFalse("The batch size should not be equal to the updated batch size", batchSize == updatedBatchSize);
		Assert.assertEquals("The new page length and the batchSize's value should be equal", newPageLength, updatedBatchSize);
	}

	@Test
	public void testUpdateBatchSizeBatchSizeNotUpdated() {
		this.setupContainerDataSource(0);

		final int batchSize = this.pagedTable.getBatchSize();

		final int newPageLength = this.pagedTable.getPageLength() + 5;
		this.pagedTable.setPageLength(newPageLength);

		this.pagedTable.updateBatchsize();
		final int updatedBatchSize = this.pagedTable.getBatchSize();

		Assert.assertEquals("The batch size should be equal to the updated batch size", batchSize, updatedBatchSize);
		Assert.assertFalse("The new page length and the updated batch size should not be equal", newPageLength == updatedBatchSize);
	}
	
	@Test
	public void testGetAllEntriesForPage() {
		final int numberOfItems = 21;
		final int firstPage = 1;
		final int lastPage = 3;
		this.setupContainerDataSource(numberOfItems);

		final List<Object> result = this.pagedTable.getAllEntriesForPage(firstPage);
		Assert.assertEquals("The number of entries per page should be equal to the table's page length.", this.pagedTable.getPageLength(),
				result.size());

		final List<Object> result2 = this.pagedTable.getAllEntriesForPage(lastPage);
		Assert.assertEquals("The last page should only have 1 item", 1, result2.size());
	}

	private void setupContainerDataSource(final int numberOfItems) {
		final QueryDefinition queryDefinition = new LazyQueryDefinition(false, PagedBreedingManagerTableTest.BATCH_SIZE);
		final QueryView queryView = Mockito.mock(QueryView.class);
		Mockito.doReturn(numberOfItems).when(queryView).size();
		Mockito.doReturn(queryDefinition).when(queryView).getQueryDefinition();

		final LazyQueryContainer container = new LazyQueryContainer(queryView);
		if (numberOfItems > 0) {
			for (int i = 0; i < numberOfItems; i++) {
				Mockito.doReturn(i).when(queryView).addItem();
			}

			for (int i = 0; i < numberOfItems; i++) {
				container.addItem();
			}
		}
		
		this.pagedTable.setContainerDataSource(container);
	}
}


package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.breeding.manager.data.initializer.GermplasmQueryFactoryTestDataInitializer;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
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

import com.vaadin.data.Item;

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

	private GermplasmQueryFactoryTestDataInitializer germplasmQueryFactoryTDI;
	
	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Before
	public void setUp() {
		this.pagedTable = new PagedBreedingManagerTable(PagedBreedingManagerTableTest.INIT_RECORD_COUNT,
				PagedBreedingManagerTableTest.MAX_RECORD_COUNT);
		// we need to be able to stub the handler call so we can use mockito's verify methods against it
		this.tableHandler = Mockito.mock(TableMultipleSelectionHandler.class);
		this.pagedTable.setTableHandler(this.tableHandler);
	}

	private void addItems() {
		this.pagedTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.pagedTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");

		for (int i = 1; i < PagedBreedingManagerTableTest.NO_OF_ENTRIES; i++) {
			final Item newItem = this.pagedTable.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);
		}
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
		this.pagedTable.removeAllItems();
		final boolean hasItems = this.pagedTable.hasItems();
		Assert.assertFalse("The table should be empty", hasItems);
	}

	@Test
	public void testHasItemsTrue() {
		this.addItems();
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
	
	private void setupContainerDataSource(int numberOfItems) {
		final QueryDefinition queryDefinition = new LazyQueryDefinition(false, BATCH_SIZE);
		final QueryView queryView = Mockito.mock(QueryView.class);
		Mockito.doReturn(numberOfItems).when(queryView).size();
		Mockito.doReturn(queryDefinition).when(queryView).getQueryDefinition();
		
		final LazyQueryContainer container = new LazyQueryContainer(queryView);
		if(numberOfItems > 0) {
			for (int i = 0; i < numberOfItems ; i++) {
				Mockito.doReturn(i).when(queryView).addItem();
			}
			
			for (int i = 0; i < numberOfItems; i++) {
				container.addItem();
			}
		}
		
		this.pagedTable.setContainerDataSource(container);
		
		
		
		
	}
}

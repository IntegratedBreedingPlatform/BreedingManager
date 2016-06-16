
package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.breeding.manager.containers.GermplasmQueryFactory;
import org.generationcp.breeding.manager.data.initializer.GermplasmQueryFactoryTestDataInitializer;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.manager.Operation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.vaadin.data.Item;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class PagedBreedingManagerTableTest {

	public static final String PAGELENGTH = "pagelength";
	private static final int NO_OF_ENTRIES = 10;
	private static final int INIT_RECORD_COUNT = 10;
	private static final int MAX_RECORD_COUNT = 20;
	private PagedBreedingManagerTable pagedTable;
	private TableMultipleSelectionHandler tableHandler;

	@Captor
	private ArgumentCaptor<Map<String, Object>> modifiedVariableMapCaptor;

	@Mock
	private ListManagerMain listManagerMain;

	private GermplasmQueryFactoryTestDataInitializer germplasmQueryFactoryTDI;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

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
		final PagedBreedingManagerTable spyTable = this.setUpSpyTable();

		Mockito.doReturn(true).when(spyTable).hasItems();
		final int batchSize = spyTable.getBatchSize();
		final int newPageLength = spyTable.getPageLength() + 5;
		spyTable.setPageLength(newPageLength);
		spyTable.updateBatchsize();
		final int updatedBatchSize = spyTable.getBatchSize();
		Assert.assertFalse("The batch size should not be equal to the updated batch size", batchSize == updatedBatchSize);
		Assert.assertEquals("The new page length and the batchSize's value should be equal", newPageLength, updatedBatchSize);
	}

	@Test
	public void testUpdateBatchSizeBatchSizeNotUpdated() {
		final PagedBreedingManagerTable spyTable = this.setUpSpyTable();

		Mockito.doReturn(false).when(spyTable).hasItems();
		final int batchSize = spyTable.getBatchSize();

		final int newPageLength = spyTable.getPageLength() + 5;
		spyTable.setPageLength(newPageLength);

		spyTable.updateBatchsize();
		final int updatedBatchSize = spyTable.getBatchSize();

		Assert.assertEquals("The batch size should be equal to the updated batch size", batchSize, updatedBatchSize);
		Assert.assertFalse("The new page length and the updated batch size should not be equal", newPageLength == updatedBatchSize);
	}
	
	private PagedBreedingManagerTable setUpSpyTable() {
		// We need to spy the table since we only need to know if the batch size is being updated if needed
		final PagedBreedingManagerTable spyTable = Mockito.spy(this.pagedTable);

		final GermplasmSearchParameter searchParameter = new GermplasmSearchParameter("", Operation.LIKE);
		
		this.germplasmQueryFactoryTDI = new GermplasmQueryFactoryTestDataInitializer();
		final GermplasmQueryFactory factory =
				this.germplasmQueryFactoryTDI.createGermplasmQueryFactory(this.listManagerMain, searchParameter, this.pagedTable);
		
		final LazyQueryDefinition definition = new LazyQueryDefinition(true, 10);
		final LazyQueryContainer container = new LazyQueryContainer(definition, factory);
		
		spyTable.setContainerDataSource(container);
		
		return spyTable;
	}
}

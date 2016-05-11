package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.commons.constant.ColumnLabels;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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

	@Before
	public void setUp() {
		this.pagedTable = new PagedBreedingManagerTable(INIT_RECORD_COUNT, MAX_RECORD_COUNT);

		// we need to be able to stub the handler call so we can use mockito's verify methods against it
		tableHandler = Mockito.mock(TableMultipleSelectionHandler.class);
		this.pagedTable.setTableHandler(tableHandler);

		this.addItems();
	}

	private void addItems() {
		this.pagedTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.pagedTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");

		for (int i = 1; i < NO_OF_ENTRIES; i++) {
			Item newItem = this.pagedTable.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);
		}
	}

	@Test
	public void testChangeVariables() throws Exception {
		final Map<String, Object> variableMap = new HashMap<>();
		variableMap.put(PAGELENGTH, 25);

		// we need to partially stub the pagedTable, note that we are not suppressing/stubbing out the function calls
		// we just need to have the arguments captured to verify and assert its contents
		PagedBreedingManagerTable pagedTableStub = Mockito.spy(this.pagedTable);

		final Object sourceMock = Mockito.mock(Object.class);

		// do actual call
		pagedTableStub.changeVariables(sourceMock, variableMap);

		// capture the resulting updated variable map
		Mockito.verify(pagedTableStub, Mockito.times(1)).doChangeVariables(Mockito.eq(sourceMock), modifiedVariableMapCaptor.capture());

		// assert the captured variable does not contain 'pagelength' anymore
		Assert.assertFalse("VariableMap passed to the table's super.changeVariable() should not contain pagelength", modifiedVariableMapCaptor.getValue().containsKey(PAGELENGTH));

		// check whether we have set the value for selected items
		Mockito.verify(tableHandler, Mockito.times(1)).setValueForSelectedItems();
	}
}

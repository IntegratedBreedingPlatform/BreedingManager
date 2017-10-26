
package org.generationcp.breeding.manager.customfields;

import java.util.Map;

import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.data.Item;

public class BreedingManagerTableTest {

	private static final int NO_OF_ENTRIES = 10;
	private static final int INIT_RECORD_COUNT = 10;
	private static final int MAX_RECORD_COUNT = 20;
	private BreedingManagerTable bmTable;
	private TableMultipleSelectionHandler tableHandlerStub;

	@Before
	public void setUp() {
		this.bmTable = new BreedingManagerTable(INIT_RECORD_COUNT, MAX_RECORD_COUNT);

		// we need to be able to stub the handler call so we can use mockito's verify methods against it
		tableHandlerStub = Mockito.mock(TableMultipleSelectionHandler.class);
		this.bmTable.setTableHandler(tableHandlerStub);

		this.addItems();
	}

	private void addItems() {
		this.bmTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.bmTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");

		for (int i = 1; i < NO_OF_ENTRIES; i++) {
			Item newItem = this.bmTable.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);
		}
	}

	@Test
	public void testChangeVariables() throws Exception {
		this.bmTable.changeVariables(Mockito.mock(Object.class),Mockito.mock(Map.class));

		// check wether we have set the value for selected items
		Mockito.verify(tableHandlerStub,Mockito.times(1)).setValueForSelectedItems();

	}
}

package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class TableMultipleSelectionHandlerTest {

	private TableMultipleSelectionHandler tableMultipleSelectionHandler;

	private static final int NO_OF_ENTRIES = 10;
	private static final int END_KEY = 5;
	private static final int START_KEY = 1;
	private static final int INIT_RECORD_COUNT = 10;
	private static final int MAX_RECORD_COUNT = 20;
	private Table breedingManagerTable;

	@Before
	public void setUp() throws Exception {
		// this is an actual object so we can add initial table entries but we partially stub it so
		// we can use verify methods and argument captors for grabbing currently selected values
		breedingManagerTable = Mockito.spy(new BreedingManagerTable(INIT_RECORD_COUNT,MAX_RECORD_COUNT));
		this.initTable(breedingManagerTable);

		// the object to test
		this.tableMultipleSelectionHandler = new TableMultipleSelectionHandler(breedingManagerTable);

	}

	@Test
	public void testItemClickMultipleSelectForStartKey() throws Exception {
		ItemClickEvent event = Mockito.mock(ItemClickEvent.class);
		Mockito.doReturn(false).when(event).isShiftKey();
		Mockito.doReturn(START_KEY).when(event).getItemId();
		this.tableMultipleSelectionHandler.itemClick(event);

		Assert.assertEquals("Expecting that the start key is set to " + START_KEY + " but didn't.", START_KEY,
				this.tableMultipleSelectionHandler.getMultiSelectStartKey());
		Assert.assertNull("Expecting that the end key is set to null but didn't.", this.tableMultipleSelectionHandler.getMultiSelectEndKey());
	}

	@Test
	public void testItemClickMultipleSelectForEndKey() throws Exception {
		ItemClickEvent event = Mockito.mock(ItemClickEvent.class);
		Mockito.doReturn(true).when(event).isShiftKey();
		Mockito.doReturn(END_KEY).when(event).getItemId();
		this.tableMultipleSelectionHandler.setMultiSelectStartKey(START_KEY);
		this.tableMultipleSelectionHandler.itemClick(event);

		Assert.assertEquals("Expecting that the end key is set to " + END_KEY + " but didn't.", END_KEY,
				this.tableMultipleSelectionHandler.getMultiSelectEndKey());

		verifySelectedEntriesIsExpected();

	}


	@Test
	public void testSetValueForSelectedItems() throws Exception {
		this.tableMultipleSelectionHandler.setMultiSelectStartKey(START_KEY);
		this.tableMultipleSelectionHandler.setMultiSelectEndKey(END_KEY);

		this.tableMultipleSelectionHandler.setValueForSelectedItems();

		verifySelectedEntriesIsExpected();

	}

	@Test
	public void testUpdateSelectedEntries() throws Exception {
		Map<Integer, Object> idEntryMap = new HashMap<>();

		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
			idEntryMap.put(i, i);
		}

		this.tableMultipleSelectionHandler.updateSelectedEntries(idEntryMap, START_KEY, END_KEY);

		Assert.assertNotNull(this.tableMultipleSelectionHandler.getSelectedEntries());
		Assert.assertEquals(this.tableMultipleSelectionHandler.getSelectedEntries().size(), END_KEY - START_KEY + 1);
	}

	@Test
	public void testIsPartOfSelectedDecRange() throws Exception {
		int startIndex = 5;
		int endIndex = 1;

		int index = 1;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 3;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 5;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 8;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 0;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedDecRange(startIndex, endIndex, index));

	}

	@Test
	public void testIsPartOfSelectedIncRange() throws Exception {
		int startIndex = 1;
		int endIndex = 5;

		int index = 1;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 3;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 5;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 8;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 0;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.tableMultipleSelectionHandler.isPartOfSelectedIncRange(startIndex, endIndex, index));
	}

	/**
	 * Initialize test table with test entries
	 * @param table
	 */
	private void initTable(Table table) {
		table.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		table.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");

		for (int i = 1; i < NO_OF_ENTRIES; i++) {
			Item newItem = table.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);
		}
	}

	/**
	 * This will check if the selected entries matches the expected entries we select from the table
	 */
	private void verifySelectedEntriesIsExpected() {
		// check if the table instance has set correct selected entries
		// since start = 1 and end = 5, the entries selected should be 1,2,3,4,5
		ArgumentCaptor<List> captureEntriesOnTable = ArgumentCaptor.forClass(List.class);
		Mockito.verify(breedingManagerTable,Mockito.times(1)).setValue(captureEntriesOnTable.capture());

		List<Integer> expectedEntries = ContiguousSet.create(Range.closed(START_KEY, END_KEY), DiscreteDomain.integers()).asList();
		Assert.assertTrue("Selected entries should be the same as the expected values", captureEntriesOnTable.getValue().containsAll(expectedEntries));
	}

}

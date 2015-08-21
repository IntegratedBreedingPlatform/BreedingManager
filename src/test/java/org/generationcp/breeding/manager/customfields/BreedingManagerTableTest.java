
package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.commons.constant.ColumnLabels;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;

public class BreedingManagerTableTest {

	private static final int NO_OF_ENTRIES = 10;
	private static final int END_KEY = 5;
	private static final int START_KEY = 1;
	private static final int INIT_RECORD_COUNT = 10;
	private static final int MAX_RECORD_COUNT = 20;
	private BreedingManagerTable bmTable;

	@Before
	public void setUp() {
		this.bmTable = Mockito.spy(new BreedingManagerTable(INIT_RECORD_COUNT, MAX_RECORD_COUNT));
		this.addItems(this.bmTable);
	}

	private void addItems(BreedingManagerTable bmTable2) {
		this.bmTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.bmTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");

		for (int i = 1; i < NO_OF_ENTRIES; i++) {
			Item newItem = this.bmTable.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);
		}
	}

	@Test
	public void testIsPartOfSelectedIncRange() {
		int startIndex = 1;
		int endIndex = 5;

		int index = 1;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 3;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 5;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 8;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedIncRange(startIndex, endIndex, index));

		index = 0;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedIncRange(startIndex, endIndex, index));

	}

	public void testIsPartOfSelectedDecRange() {
		int startIndex = 5;
		int endIndex = 1;

		int index = 1;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 3;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 5;
		Assert.assertTrue("Expecting that the index=" + index + " is part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 8;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedDecRange(startIndex, endIndex, index));

		index = 0;
		Assert.assertFalse("Expecting that the index=" + index + " is no part of range from " + startIndex + " to " + endIndex,
				this.bmTable.isPartOfSelectedDecRange(startIndex, endIndex, index));

	}

	@Test
	public void testAssignMarkersForMultipleSelectionForStartKey() {
		ItemClickEvent event = Mockito.mock(ItemClickEvent.class);
		Mockito.doReturn(false).when(event).isShiftKey();
		Mockito.doReturn(START_KEY).when(event).getItemId();
		this.bmTable.assignMarkersForMultipleSelection(event);

		Assert.assertEquals("Expecting that the start key is set to " + START_KEY + " but didn't.", START_KEY,
				this.bmTable.getMultiSelectStartKey());
		Assert.assertNull("Expecting that the end key is set to null but didn't.", this.bmTable.getMultiSelectEndKey());
	}

	@Test
	public void testAssignMarkersForMultipleSelectionForEndKey() {
		ItemClickEvent event = Mockito.mock(ItemClickEvent.class);
		Mockito.doReturn(true).when(event).isShiftKey();
		Mockito.doReturn(START_KEY).when(this.bmTable).getMultiSelectStartKey();
		Mockito.doReturn(END_KEY).when(event).getItemId();
		this.bmTable.assignMarkersForMultipleSelection(event);

		Assert.assertEquals("Expecting that the end key is set to " + END_KEY + " but didn't.", END_KEY,
				this.bmTable.getMultiSelectEndKey());

		try {
			Mockito.verify(this.bmTable, Mockito.times(1)).setValueForSelectedItems();
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting to call the method for assigning multiple selection ids to the table but didn't.");
		}
	}

	@Test
	public void testSetValueForSelectedItems() {
		Mockito.doReturn(START_KEY).when(this.bmTable).getMultiSelectStartKey();
		Mockito.doReturn(END_KEY).when(this.bmTable).getMultiSelectEndKey();
		this.bmTable.setValueForSelectedItems();

		try {
			Mockito.verify(this.bmTable, Mockito.times(1)).updateSelectedEntries(Mockito.anyMapOf(Integer.class, Object.class),
					Mockito.anyInt(), Mockito.anyInt());
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting to call the method for updating the ids of the table but didn't.");
		}
	}

	@Test
	public void testUpdateSelectedEntries() {
		Map<Integer, Object> idEntryMap = new HashMap<Integer, Object>();
		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
			idEntryMap.put(i, i);
		}

		this.bmTable.updateSelectedEntries(idEntryMap, START_KEY, END_KEY);

		Assert.assertNotNull(this.bmTable.getSelectedEntries());
		Assert.assertEquals(this.bmTable.getSelectedEntries().size(), END_KEY - START_KEY + 1);
	}

}

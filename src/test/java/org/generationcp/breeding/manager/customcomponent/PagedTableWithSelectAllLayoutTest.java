
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import junit.framework.Assert;

/**
 * Created by Aldrin Batac on 5/23/16.
 */
public class PagedTableWithSelectAllLayoutTest {

	private static final int INDEX_OF_PAGING_CONTROLS = 1;

	private static final String CHECKBOX_COLUMN_ID = "CheckBoxColumnId";

	private static final int DEFAULT_NO_OF_ITEMS = 101;

	private final PagedTableWithSelectAllLayout pagedTableWithSelectAllLayout = new PagedTableWithSelectAllLayout(0,
			PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID);

	@Before
	public void init() {
		this.pagedTableWithSelectAllLayout.instantiateComponents();
		this.pagedTableWithSelectAllLayout.getTable().setMultiSelect(true);
		this.pagedTableWithSelectAllLayout.getTable().setSelectable(true);
	}

	@Test
	public void testGetAllEntriesPerPage() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		final int pageLength = 5;
		final int firstPage = 1;
		final int lastPage = 21;

		this.pagedTableWithSelectAllLayout.getTable().setPageLength(pageLength);

		final List<Object> result = this.pagedTableWithSelectAllLayout.getAllEntriesForPage(firstPage);

		Assert.assertEquals("The number of entries per page should be equal to the table's page length.", pageLength, result.size());

		final List<Object> result2 = this.pagedTableWithSelectAllLayout.getAllEntriesForPage(lastPage);

		Assert.assertEquals("The last page should only have 1 item", 1, result2.size());

	}

	@Test
	public void testUpdateItemSelectCheckboxes() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		final Integer firstSelectedObjectItemId = 1;
		final Integer secondSelectedObjectItemId = 2;

		final List<Object> selectedItems = new ArrayList<>();
		selectedItems.add(firstSelectedObjectItemId);
		selectedItems.add(secondSelectedObjectItemId);

		final List<Object> loadedItems = new ArrayList<>(this.pagedTableWithSelectAllLayout.getTable().getItemIds());

		this.pagedTableWithSelectAllLayout.updateItemSelectCheckboxes(selectedItems, loadedItems);

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();

		final CheckBox checkboxOfFirstItem =
				(CheckBox) table.getItem(firstSelectedObjectItemId).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID)
						.getValue();
		Assert.assertTrue("The first item in table is selected so the checkbox value should be true",
				(Boolean) checkboxOfFirstItem.getValue());

		final CheckBox checkboxOfSecondItem =
				(CheckBox) table.getItem(secondSelectedObjectItemId).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID)
						.getValue();
		Assert.assertTrue("The second item in table is selected so the checkbox value should be true",
				(Boolean) checkboxOfSecondItem.getValue());

		// Other items that are not selected should have a checkbox with false value.
		for (Integer i = 3; i < table.getItemIds().size(); i++) {
			final CheckBox checkbox =
					(CheckBox) table.getItem(i).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
			Assert.assertFalse("Item is not selected, the checkbox value should be false.", (Boolean) checkbox.getValue());
		}

	}

	@Test
	public void testUpdateSelectAllOnPageCheckBoxStatusEmptyList() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		// Set the select all checkbox to true so we can verify that it's changed later
		this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().setValue(true);

		this.pagedTableWithSelectAllLayout.updateSelectAllOnPageCheckBoxStatus();

		Assert.assertFalse("There entry list is empty so the \"Select All On Page\" checkbox value should be false",
				this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().booleanValue());

	}

	@Test
	public void testUpdateSelectAllOnPageCheckBoxStatusAllEntriesInAPageAreSelected() {
		// Initialize table with page length = 5
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);
		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);
		this.pagedTableWithSelectAllLayout.getTable().setPageLength(5);

		// Select each item on current page
		final List<Object> entriesList = this.createEntriesList();
		this.markCheckboxItemsInTable(entriesList, true);

		// Method to test
		this.pagedTableWithSelectAllLayout.updateSelectAllOnPageCheckBoxStatus();

		Assert.assertTrue("All entries in page are selected, the \"Select All On Page\" checkbox value should be true",
				this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().booleanValue());

	}

	@Test
	public void testUpdateSelectAllOnPageCheckBoxStatusSomeEntriesInAPageAreSelected() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);

		// Mark the first item as selected
		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();
		final CheckBox cb = (CheckBox) table.getItem(1).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
		cb.setValue(true);

		this.pagedTableWithSelectAllLayout.updateSelectAllOnPageCheckBoxStatus();

		Assert.assertFalse("Some entries in page are selected, the select all checkbox value should be false",
				this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().booleanValue());

	}

    @Test
	public void testUpdateLoadedPageCurrentPageDoesntExistAfterUpdate() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		final int loadedPageNumber = 21;

		// Let's assume that page 21 is loaded in the screen
		this.pagedTableWithSelectAllLayout.getLoadedPages().add(loadedPageNumber);

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();
		// then the user changed the page length (number of items per page) to a higher number
		table.setPageLength(25);

		// this will recalculate the number of page available, and since page 21 is now not available, we should remove it from loadedPage
		// list.
		this.pagedTableWithSelectAllLayout.updateLoadedPages();

		Assert.assertFalse("Page number " + loadedPageNumber + " should be removed from loaded page", this.pagedTableWithSelectAllLayout
				.getLoadedPages().contains(loadedPageNumber));

	}

	@Test
	public void testUpdateLoadedPageCurrentPageStillExistAfterUpdate() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		final int loadedPageNumber = 5;

		// Let's assume that page 5 is loaded in the screen
		this.pagedTableWithSelectAllLayout.getLoadedPages().add(loadedPageNumber);

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();
		// then the user changed the page length (number of items per page) to a lower number
		table.setPageLength(4);

		// this will recalculate the number of page available, and since page 5 is still available, we should not remove it from loadedPage
		// list.
		this.pagedTableWithSelectAllLayout.updateLoadedPages();

		Assert.assertTrue("Page number " + loadedPageNumber + " should be in loaded page list", this.pagedTableWithSelectAllLayout
				.getLoadedPages().contains(loadedPageNumber));

	}

	private List<Object> createEntriesList() {

		final List<Object> entriesList = new ArrayList<>();
		entriesList.add(1);
		entriesList.add(2);
		entriesList.add(3);
		entriesList.add(4);
		entriesList.add(5);
		return entriesList;
	}

	private void markCheckboxItemsInTable(final List<Object> selectedItems, final Boolean checkboxValue) {

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();

		for (final Object itemId : selectedItems) {
			final CheckBox cb =
					(CheckBox) table.getItem(itemId).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
			cb.setValue(checkboxValue);
		}

	}

	void initializePagedBreedingManagerTable(final int numberOfItems) {

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();

		table.addContainerProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID, CheckBox.class, null);

		int i = 1;
		while (i <= numberOfItems) {
			table.addItem(i);
			table.getItem(i).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).setValue(new CheckBox());
			i++;
		}

	}

	List<Object> createListObject() {
		final List<Object> entriesList = new ArrayList<>();
		int i = 1;
		while (i <= PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS) {
			entriesList.add(new Object());
			i++;
		}
		return entriesList;
	}

	@Test
	public void testRefreshTablePagingControlsWithOnePageOnly() {

		this.pagedTableWithSelectAllLayout.layoutComponents();
		this.initializePagedBreedingManagerTable(2);

		this.pagedTableWithSelectAllLayout.refreshTablePagingControls();
		Component pagingControlsComponent = this.pagedTableWithSelectAllLayout.getComponent(INDEX_OF_PAGING_CONTROLS);
		Iterator<Component> pagingControlsIterator = ((HorizontalLayout) pagingControlsComponent).getComponentIterator();
		Assert.assertNotNull("The paging controls should be displayed", pagingControlsIterator);

		// first iteration: page size
		final HorizontalLayout pageSize = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page size should be displayed", pageSize);
		
		// second iteration: page management
		final HorizontalLayout pageManagement = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page management should be displayed", pageManagement);
		final Iterator<Component> pageManagementIterator = pageManagement.getComponentIterator();
		int numberOfButtons = 0;
		while (pageManagementIterator.hasNext()) {
			final Component component = pageManagementIterator.next();
			// verify that all buttons are disabled since we only have 1 page
			if (component instanceof Button) {
				numberOfButtons++;
				final Button button = (Button) component;
				Assert.assertFalse("The button should be disabled because there is only 1 page", button.isEnabled());
			}
		}
		Assert.assertEquals("There should be 4 buttons displayed for first, previous, next and last", 4, numberOfButtons);
	}

	@Test
	public void testRefreshTablePagingControlsWithMoreThan1Page() {

		this.pagedTableWithSelectAllLayout.layoutComponents();
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		// Method to test
		this.pagedTableWithSelectAllLayout.refreshTablePagingControls();
		
		Component pagingControlsComponent = this.pagedTableWithSelectAllLayout.getComponent(INDEX_OF_PAGING_CONTROLS);
		Iterator<Component> pagingControlsIterator = ((HorizontalLayout) pagingControlsComponent).getComponentIterator();
		
		// first iteration: page size
		final HorizontalLayout pageSize = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page size should be displayed", pageSize);
		// second iterator: page management
		final HorizontalLayout pageManagement = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page management should be displayed", pageManagement);
		final Iterator<Component> pageManagementIterator = pageManagement.getComponentIterator();
		int numberOfButtons = 0;
		while (pageManagementIterator.hasNext()) {
			final Component component = pageManagementIterator.next();
			// verify that the first and previous buttons are disabled while the next and last buttons are enabled
			if (component instanceof Button) {
				numberOfButtons++;
				final Button button = (Button) component;
				// first and previous button
				if (numberOfButtons <= 2) {
					Assert.assertFalse("The button should be disabled because the current page is 1", button.isEnabled());
				} else {
					Assert.assertTrue("The button should be enabled because there are more than 1 page", button.isEnabled());
				}

			}
		}
		Assert.assertEquals("There should be 4 buttons displayed for first, previous, next and last", 4, numberOfButtons);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSelectAllOnPageCheckboxClick(){
		// Initialize test table listeners and properties
		this.pagedTableWithSelectAllLayout.addListeners();

		// Populate test table and set page length = 10. Set 2nd page as current page
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);
		this.pagedTableWithSelectAllLayout.getTable().setPageLength(10);
		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(2);
		
		
		// Method to test - Check "Select All on Page" checkbox on 2nd page
		// Have to hack setting checkbox value to true before firing click event
		this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().setValue(true);
		this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().click();
		
		// Entries 11-20 should be selected (they are all entries on the 2nd page)
		final Integer[] ids = new Integer[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
		final List<Integer> expectedIdsList = Arrays.asList(ids);
		Collection<Integer> selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		final List<Integer> selectedEntriesList = new ArrayList<>();
		selectedEntriesList.addAll(selectedEntries);
		Collections.sort(selectedEntriesList);
		Assert.assertEquals(expectedIdsList, selectedEntriesList);
		
		
		// Method to test - now uncheck "Select All on Page" checkbox on 2nd page
		// Have to hack setting checkbox value to false before firing click event
		this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().setValue(false);
		this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().click();
		
		// Check that no selected entries on table
		selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		Assert.assertTrue(selectedEntries.isEmpty());
		
	}
	
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSelectAllEntriesOnCurrentPage() {
		// Populate test table and set page length = 10. Set 2nd page as current page
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);
		this.pagedTableWithSelectAllLayout.getTable().setPageLength(10);
		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(2);
		
		
		// Method to test - select all entries on 2nd page
		this.pagedTableWithSelectAllLayout.selectAllEntriesOnCurrentPage();
		
		
		// Entries 11-20 should be selected (they are all entries on the 2nd page)
		final Integer[] ids = new Integer[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
		final List<Integer> expectedIdsList = Arrays.asList(ids);
		Collection<Integer> selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		final List<Integer> selectedEntriesList = new ArrayList<>();
		selectedEntriesList.addAll(selectedEntries);
		Collections.sort(selectedEntriesList);
		Assert.assertEquals(expectedIdsList, selectedEntriesList);
		
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSelectAllEntriesCheckboxClick(){
		// Initialize test table listeners and properties
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);
		this.pagedTableWithSelectAllLayout.addListeners();

		// Method to test - Check "Select All Pages" checkbox
		// Have to hack setting checkbox value to true before firing click event
		this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().setValue(true);
		this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().click();
		
		// Check that all entries on all pages were selected
		Collection<Integer> allEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getItemIds();
		Collection<Integer> selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		Assert.assertEquals("Expecting size of selected entries equals size of table entries", allEntries.size(), selectedEntries.size());
		// "Select All on Page" checkbox should have been checked too
		Assert.assertTrue(this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().booleanValue());
		
		
		// Method to test - now uncheck "Select All Pages" checkbox 
		// Have to hack setting checkbox value to false before firing click event
		this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().setValue(false);
		this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().click();
		
		// Check that there are no selected entries on table
		selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		Assert.assertTrue(selectedEntries.isEmpty());
		// "Select All on Page" checkbox should have been unchecked too
		Assert.assertFalse(this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().booleanValue());
				
	}
	
	@Test
	public void testUpdateSelectAllEntriesCheckBoxStatusEmptyList() {
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		// Set the "Select all Pages" checkbox to true so we can verify that it's changed later
		this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().setValue(true);

		this.pagedTableWithSelectAllLayout.updateSelectAllEntriesCheckboxStatus();

		Assert.assertFalse("There entry list is empty so the \"Select All Entries\" checkbox value should be false",
				this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().booleanValue());

	}

	@Test
	@SuppressWarnings("unchecked")
	public void testUpdateSelectAllEntriesCheckBoxStatusAllEntriesAreSelected() {
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		// Select all entries on table
		final Collection<Object> allEntries = (Collection<Object>) this.pagedTableWithSelectAllLayout.getTable().getItemIds();
		this.pagedTableWithSelectAllLayout.getTable().setValue(allEntries);

		// Method to test
		this.pagedTableWithSelectAllLayout.updateSelectAllEntriesCheckboxStatus();

		Assert.assertTrue("All entries in table are selected, the \"Select All Entries\" checkbox value should be true",
				this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().booleanValue());

	}
	
	@Test
	public void testUpdateSelectAllEntriesCheckBoxStatusOnlyFirstPageSelected() {
		// Initialize test table with page length = 5
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);
		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);
		this.pagedTableWithSelectAllLayout.getTable().setPageLength(5);

		// Select items on current page
		final List<Object> entriesList = this.createEntriesList();
		this.markCheckboxItemsInTable(entriesList, true);

		// Method to test
		this.pagedTableWithSelectAllLayout.updateSelectAllEntriesCheckboxStatus();

		Assert.assertFalse("Only entries on first page are selected, the \"Select All Entries\" checkbox value should be false",
				this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().booleanValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testClearAllSelectedEntries() {
		// Initialize table and select all entries in the table
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);
		this.pagedTableWithSelectAllLayout.selectAllEntries();
		
		// Method to test
		this.pagedTableWithSelectAllLayout.clearAllSelectedEntries();
		
		// Check that there are no selected entries on table
		Collection<Integer> selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		Assert.assertTrue(selectedEntries.isEmpty());
		// "Select All on Page" and "Select All Pages" checkbox should have been unchecked too
		Assert.assertFalse(this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().booleanValue());
		Assert.assertFalse(this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().booleanValue());
		
	}
	

}

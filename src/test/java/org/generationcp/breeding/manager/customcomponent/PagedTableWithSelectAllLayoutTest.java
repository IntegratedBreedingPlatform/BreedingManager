
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * Created by Aldrin Batac on 5/23/16.
 */
public class PagedTableWithSelectAllLayoutTest {

	private static final String CHECKBOX_COLUMN_ID = "CheckBoxColumnId";

	private static final int DEFAULT_NO_OF_ITEMS = 101;

	private final PagedTableWithSelectAllLayout pagedTableWithSelectAllLayout = new PagedTableWithSelectAllLayout(0,
			PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID);

	@Before
	public void init() {
		this.pagedTableWithSelectAllLayout.instantiateComponents();
	}

	@Test
	public void testGetAllEntriesPerPage() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		final int pageLength = 5;
		final int firstPage = 1;
		final int lastPage = 21;

		final List<Object> entriesList = this.createListObject();
		this.pagedTableWithSelectAllLayout.getTable().setPageLength(pageLength);

		final List<Object> result = this.pagedTableWithSelectAllLayout.getAllEntriesForPage(entriesList, firstPage);

		Assert.assertEquals("The number of entries per page should be equal to the table's page length.", pageLength, result.size());

		final List<Object> result2 = this.pagedTableWithSelectAllLayout.getAllEntriesForPage(entriesList, lastPage);

		Assert.assertEquals("The last page should only have 1 item", 1, result2.size());

	}

	@Test
	public void testUpdateTagPerRowItem() {

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
	public void testUpdateSelectAllCheckBoxStatusEmptyList() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		// Set the select all checkbox to true so we can verify that it's changed later
		this.pagedTableWithSelectAllLayout.getSelectAllCheckBox().setValue(true);

		this.pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(new ArrayList<>());

		Assert.assertFalse("There entry list is empty so the select all checkbox value should be false",
				(Boolean) this.pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

	}

	@Test
	public void testUpdateSelectAllCheckBoxStatusAllEntriesInAPageAreSelected() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);

		final List<Object> entriesList = this.createEntriesList();

		this.markCheckboxItemsInTable(entriesList, true);

		this.pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(entriesList);

		Assert.assertTrue("All entries in page are selected, the select all checkbox value should be true",
				(Boolean) this.pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

	}

	@Test
	public void testUpdateSelectAllCheckBoxStatusSomeEntriesInAPageAreSelected() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);

		final List<Object> entriesList = this.createEntriesList();

		// Mark the first item as selected
		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();
		final CheckBox cb = (CheckBox) table.getItem(1).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
		cb.setValue(true);

		this.pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(entriesList);

		Assert.assertFalse("Some entries in page are selected, the select all checkbox value should be false",
				(Boolean) this.pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

	}

	@Test
	public void testUpdatePagedTableSelectedEntries() {
		final List<Object> entriesList = this.createEntriesList();
		final ArgumentCaptor<List<Object>> captor = ArgumentCaptor.forClass((Class) List.class);
		final PagedBreedingManagerTable table = Mockito.mock(PagedBreedingManagerTable.class);
		Mockito.when(table.getValue()).thenReturn(entriesList);

		this.pagedTableWithSelectAllLayout.setTable(table);

		this.pagedTableWithSelectAllLayout.updatePagedTableSelectedEntries(true);

		Mockito.verify(table, Mockito.atLeast(1)).setValue(captor.capture());
		Assert.assertTrue("updatePagedTableSelectedEntries(true) should select all entries",
				entriesList.equals(new ArrayList<Object>(captor.getValue())));

		this.pagedTableWithSelectAllLayout.updatePagedTableSelectedEntries(false);

		Mockito.verify(table, Mockito.atLeast(1)).setValue(captor.capture());
		Assert.assertTrue("updatePagedTableSelectedEntries(false) should deselect all entries",
				new ArrayList<Object>(captor.getValue()).isEmpty());
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
		final Iterator<Component> componentIterator = this.pagedTableWithSelectAllLayout.getComponentIterator();
		Iterator<Component> pagingControlsIterator = null;
		while (componentIterator.hasNext()) {
			final Component component = componentIterator.next();
			// use this to get the paging controls
			// because we're sure that the paging controls is the only HorizontalLayout
			if (component instanceof HorizontalLayout) {
				pagingControlsIterator = ((HorizontalLayout) component).getComponentIterator();
			}
		}
		Assert.assertNotNull("The paging controls should be displayed", pagingControlsIterator);
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

		this.pagedTableWithSelectAllLayout.refreshTablePagingControls();
		final Iterator<Component> componentIterator = this.pagedTableWithSelectAllLayout.getComponentIterator();
		Iterator<Component> pagingControlsIterator = null;
		while (componentIterator.hasNext()) {
			final Component component = componentIterator.next();
			// use this to get the paging controls
			// because we're sure that the paging controls is the only HorizontalLayout
			if (component instanceof HorizontalLayout) {
				pagingControlsIterator = ((HorizontalLayout) component).getComponentIterator();
			}
		}
		Assert.assertNotNull("The paging controls should be displayed", pagingControlsIterator);
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

}


package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.CheckBox;

import junit.framework.Assert;

/**
 * Created by Aldrin Batac on 5/23/16.
 */
public class PagedTableWithSelectAllLayoutTest {

	private static final String CHECKBOX_COLUMN_ID = "CheckBoxColumnId";

	private static final int DEFAULT_NO_OF_ITEMS = 101;

	private static final String SELECT_ALL_ON_PAGE_CAPTION = "Select All (this page)";
	private static final String SELECT_ALL_PAGES_CAPTION = "Select All (this page)";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	final PagedTableWithSelectAllLayout pagedTableWithSelectAllLayout =
			new PagedTableWithSelectAllLayout(0, PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID);

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.pagedTableWithSelectAllLayout.setMessageSource(this.messageSource);
		// Setup mock return values
		Mockito.doReturn(PagedTableWithSelectAllLayoutTest.SELECT_ALL_ON_PAGE_CAPTION).when(this.messageSource)
				.getMessage(Message.SELECT_ALL_ON_PAGE);
		Mockito.doReturn(PagedTableWithSelectAllLayoutTest.SELECT_ALL_PAGES_CAPTION).when(this.messageSource)
				.getMessage(Message.SELECT_ALL_PAGES);

		this.pagedTableWithSelectAllLayout.instantiateComponents();
		this.pagedTableWithSelectAllLayout.getTable().setMultiSelect(true);
		this.pagedTableWithSelectAllLayout.getTable().setSelectable(true);
	}

	@Test
	public void testGetAllEntriesForPage() {

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
	public void testUpdateItemSelectCheckboxesSomeSelectedOnCurrentPage() {

		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		final Integer firstSelectedObjectItemId = 1;
		final Integer secondSelectedObjectItemId = 2;

		final List<Object> selectedItems = new ArrayList<>();
		selectedItems.add(firstSelectedObjectItemId);
		selectedItems.add(secondSelectedObjectItemId);
		this.pagedTableWithSelectAllLayout.getTable().setValue(selectedItems);

		// Method to test - some entries selected on current page
		this.pagedTableWithSelectAllLayout.updateItemSelectCheckboxes();

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();

		final CheckBox checkboxOfFirstItem = (CheckBox) table.getItem(firstSelectedObjectItemId)
				.getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
		Assert.assertTrue("The first item in table is selected so the checkbox value should be true", checkboxOfFirstItem.booleanValue());

		final CheckBox checkboxOfSecondItem = (CheckBox) table.getItem(secondSelectedObjectItemId)
				.getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
		Assert.assertTrue("The second item in table is selected so the checkbox value should be true", checkboxOfSecondItem.booleanValue());

		// Other items that are not selected should have a checkbox with false value.
		for (Integer i = 3; i < table.getItemIds().size(); i++) {
			final CheckBox checkbox =
					(CheckBox) table.getItem(i).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
			Assert.assertFalse("Item is not selected, the checkbox value should be false.", checkbox.booleanValue());
		}

	}

	@Test
	public void testUpdateItemSelectCheckboxesNoneSelectedOnCurrentPage() {
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		final int currentPage = 2;
		final Integer firstSelectedObjectItemId = 1;
		final Integer secondSelectedObjectItemId = currentPage;
		final List<Object> selectedItems = new ArrayList<>();
		selectedItems.add(firstSelectedObjectItemId);
		selectedItems.add(secondSelectedObjectItemId);

		// Selected entries on first page, but current page is 2nd page
		this.pagedTableWithSelectAllLayout.getTable().setValue(selectedItems);
		this.pagedTableWithSelectAllLayout.getTable().setPageLength(10);
		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(currentPage);

		// Method to test - update checkboxes of second page
		this.pagedTableWithSelectAllLayout.updateItemSelectCheckboxes();

		// The selected items on first page should be unchecked as they are not on current page
		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();

		final CheckBox checkboxOfFirstItem = (CheckBox) table.getItem(firstSelectedObjectItemId)
				.getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
		Assert.assertFalse("The first item in table is selected but not on current page so the checkbox value should be false",
				checkboxOfFirstItem.booleanValue());

		final CheckBox checkboxOfSecondItem = (CheckBox) table.getItem(secondSelectedObjectItemId)
				.getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
		Assert.assertFalse("The second item in table is selected but not on current page so the checkbox value should be false",
				checkboxOfSecondItem.booleanValue());

		// Items on the 2nd page are not selected
		final List<Object> entriesForCurrentPage = this.pagedTableWithSelectAllLayout.getAllEntriesForPage(currentPage);
		for (final Object item : entriesForCurrentPage) {
			final CheckBox checkbox =
					(CheckBox) table.getItem(item).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
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
	@SuppressWarnings("unchecked")
	public void testSelectAllOnPageCheckboxClick() {
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
		final Integer[] ids = new Integer[] {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
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
		final Integer[] ids = new Integer[] {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
		final List<Integer> expectedIdsList = Arrays.asList(ids);
		final Collection<Integer> selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		final List<Integer> selectedEntriesList = new ArrayList<>();
		selectedEntriesList.addAll(selectedEntries);
		Collections.sort(selectedEntriesList);
		Assert.assertEquals(expectedIdsList, selectedEntriesList);

	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSelectAllEntriesCheckboxClick() {
		// Initialize test table listeners and properties
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);
		this.pagedTableWithSelectAllLayout.addListeners();

		// Method to test - Check "Select All Pages" checkbox
		// Have to hack setting checkbox value to true before firing click event
		this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().setValue(true);
		this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().click();

		// Check that all entries on all pages were selected
		final Collection<Integer> allEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getItemIds();
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
		final Collection<Integer> selectedEntries = (Collection<Integer>) this.pagedTableWithSelectAllLayout.getTable().getValue();
		Assert.assertNotNull(selectedEntries);
		Assert.assertTrue(selectedEntries.isEmpty());
		// "Select All on Page" and "Select All Pages" checkbox should have been unchecked too
		Assert.assertFalse(this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().booleanValue());
		Assert.assertFalse(this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().booleanValue());

	}

	@Test
	public void testUpdateSelectAllCheckboxesCaptionWhenEmptyTable() {
		// Hack setting of checkboxes caption to another value to verify if they are reset later
		this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().setCaption("ABC");
		this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().setCaption("XYZ");

		// Method to test
		this.pagedTableWithSelectAllLayout.updateSelectAllCheckboxesCaption();

		// Expecting select all checkboxes caption to be reset to default as table is empty
		Assert.assertEquals(PagedTableWithSelectAllLayoutTest.SELECT_ALL_ON_PAGE_CAPTION,
				this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().getCaption());
		Assert.assertEquals(PagedTableWithSelectAllLayoutTest.SELECT_ALL_PAGES_CAPTION,
				this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().getCaption());
	}

	@Test
	public void testUpdateSelectAllCheckboxesCaptionTableHasEntries() {
		this.initializePagedBreedingManagerTable(PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS);

		// Method to test
		this.pagedTableWithSelectAllLayout.updateSelectAllCheckboxesCaption();

		final String selectAllOnPageCaptionSubstr = PagedTableWithSelectAllLayoutTest.SELECT_ALL_ON_PAGE_CAPTION.substring(0,
				PagedTableWithSelectAllLayoutTest.SELECT_ALL_ON_PAGE_CAPTION.length() - 1);
		final String selectAllCaptionSubstr = PagedTableWithSelectAllLayoutTest.SELECT_ALL_PAGES_CAPTION.substring(0,
				PagedTableWithSelectAllLayoutTest.SELECT_ALL_PAGES_CAPTION.length() - 1);

		// Expecting "Select All on Page" checkbox caption to include # of entries, which is the page length
		Assert.assertEquals(
				selectAllOnPageCaptionSubstr + " - " + this.pagedTableWithSelectAllLayout.getTable().getPageLength() + " entries)",
				this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().getCaption());
		// Expecting "Select All Pages" checkbox caption to include total # of entries
		Assert.assertEquals(selectAllCaptionSubstr + " - " + PagedTableWithSelectAllLayoutTest.DEFAULT_NO_OF_ITEMS + " entries)",
				this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().getCaption());
	}

	@Test
	public void testUpdateSelectAllCheckboxesCaptionTableEntriesLessThanPageLength() {
		// Populate with 3 entries but default page length is 10
		final int numberOfItems = 3;
		this.initializePagedBreedingManagerTable(numberOfItems);

		// Method to test
		this.pagedTableWithSelectAllLayout.updateSelectAllCheckboxesCaption();

		final String selectAllOnPageCaptionSubstr = PagedTableWithSelectAllLayoutTest.SELECT_ALL_ON_PAGE_CAPTION.substring(0,
				PagedTableWithSelectAllLayoutTest.SELECT_ALL_ON_PAGE_CAPTION.length() - 1);
		final String selectAllCaptionSubstr = PagedTableWithSelectAllLayoutTest.SELECT_ALL_PAGES_CAPTION.substring(0,
				PagedTableWithSelectAllLayoutTest.SELECT_ALL_PAGES_CAPTION.length() - 1);

		// Expecting "Select All on Page" checkbox caption to include # of entries and not the page length
		Assert.assertEquals(selectAllOnPageCaptionSubstr + " - " + numberOfItems + " entries)",
				this.pagedTableWithSelectAllLayout.getSelectAllOnPageCheckBox().getCaption());
		// Expecting "Select All Pages" checkbox caption to include total # of entries
		Assert.assertEquals(selectAllCaptionSubstr + " - " + numberOfItems + " entries)",
				this.pagedTableWithSelectAllLayout.getSelectAllEntriesCheckBox().getCaption());
	}

}

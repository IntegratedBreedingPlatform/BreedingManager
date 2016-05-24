
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.CheckBox;

import junit.framework.Assert;

/**
 * Created by Aldrin Batac on 5/23/16.
 */
public class PagedTableWithSelectAllLayoutTest {

	private static final String CHECKBOX_COLUMN_ID = "CheckBoxColumnId";

	private final PagedTableWithSelectAllLayout pagedTableWithSelectAllLayout =
			new PagedTableWithSelectAllLayout(0, PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID);

	@Before
	public void init() {
		this.pagedTableWithSelectAllLayout.instantiateComponents();
		this.initializePagedBreedingManagerTable();
	}

	@Test
	public void testGetAllEntriesPerPage() {

		final int pageLength = 5;
		final int firstPage = 1;
		final int lastPage = 21;

		final List<Object> entriesList = this.createListObject();
		this.pagedTableWithSelectAllLayout.getTable().setPageLength(pageLength);

		final List<Object> result = this.pagedTableWithSelectAllLayout.getAllEntriesPerPage(entriesList, firstPage);

		Assert.assertEquals("The number of entries per page should be equal to the table's page length.", pageLength, result.size());

		final List<Object> result2 = this.pagedTableWithSelectAllLayout.getAllEntriesPerPage(entriesList, lastPage);

		Assert.assertEquals("The last page should only have 1 item", 1, result2.size());

	}

	@Test
	public void testUpdateTagPerRowItem() {

		final Integer firstSelectedObjectItemId = 1;
		final Integer secondSelectedObjectItemId = 2;

		final List<Object> selectedItems = new ArrayList<>();
		selectedItems.add(firstSelectedObjectItemId);
		selectedItems.add(secondSelectedObjectItemId);

		final List<Object> loadedItems = new ArrayList<>(this.pagedTableWithSelectAllLayout.getTable().getItemIds());

		this.pagedTableWithSelectAllLayout.updateTagPerRowItem(selectedItems, loadedItems);

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();

		final CheckBox checkboxOfFirstItem = (CheckBox) table.getItem(firstSelectedObjectItemId)
				.getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
		Assert.assertTrue("The first item in table is selected so the checkbox value should be true",
				(Boolean) checkboxOfFirstItem.getValue());

		final CheckBox checkboxOfSecondItem = (CheckBox) table.getItem(secondSelectedObjectItemId)
				.getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).getValue();
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

		// Set the select all checkbox to true so we can verify that it's changed later
		this.pagedTableWithSelectAllLayout.getSelectAllCheckBox().setValue(true);

		this.pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(new ArrayList<>());

		Assert.assertFalse("There entry list is empty so the select all checkbox value should be false",
				(Boolean) this.pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

	}

	@Test
	public void testUpdateSelectAllCheckBoxStatusAllEntriesInAPageAreSelected() {

		this.pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);

		final List<Object> entriesList = this.createEntriesList();

		this.markCheckboxItemsInTable(entriesList, true);

		this.pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(entriesList);

		Assert.assertTrue("All entries in page are selected, the select all checkbox value should be true",
				(Boolean) this.pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

	}

	@Test
	public void testUpdateSelectAllCheckBoxStatusSomeEntriesInAPageAreSelected() {

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
    public void testUpdateLoadedPageCurrentPageDoesntExistAfterUpdate() {

        final int loadedPageNumber = 21;

        // Let's assume that page 21 is loaded in the screen
        this.pagedTableWithSelectAllLayout.getLoadedPaged().add(loadedPageNumber);

        PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();
        // then the user changed the page length (number of items per page) to a higher number
        table.setPageLength(25);

        // this will recalculate the number of page available, and since page 21 is now not available, we should remove it from loadedPage list.
        this.pagedTableWithSelectAllLayout.updateLoadedPage();

        Assert.assertFalse("Page number " + loadedPageNumber + " should be removed from loaded page", this.pagedTableWithSelectAllLayout.getLoadedPaged().contains(loadedPageNumber));

    }

    @Test
    public void testUpdateLoadedPageCurrentPageStillExistAfterUpdate() {

        final int loadedPageNumber = 5;

        // Let's assume that page 5 is loaded in the screen
        this.pagedTableWithSelectAllLayout.getLoadedPaged().add(loadedPageNumber);

        PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();
        // then the user changed the page length (number of items per page) to a lower number
        table.setPageLength(4);

        // this will recalculate the number of page available, and since page 5 is still available, we should not remove it from loadedPage list.
        this.pagedTableWithSelectAllLayout.updateLoadedPage();

        Assert.assertTrue("Page number " + loadedPageNumber + " should be in loaded page list", this.pagedTableWithSelectAllLayout.getLoadedPaged().contains(loadedPageNumber));

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

	void initializePagedBreedingManagerTable() {

		final PagedBreedingManagerTable table = this.pagedTableWithSelectAllLayout.getTable();

		table.addContainerProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID, CheckBox.class, null);

		int i = 1;
		while (i <= 101) {
			table.addItem(i);
			table.getItem(i).getItemProperty(PagedTableWithSelectAllLayoutTest.CHECKBOX_COLUMN_ID).setValue(new CheckBox());
			i++;
		}

	}

	List<Object> createListObject() {
		final List<Object> entriesList = new ArrayList<>();
		int i = 1;
		while (i <= 101) {
			entriesList.add(new Object());
			i++;
		}
		return entriesList;
	}

}

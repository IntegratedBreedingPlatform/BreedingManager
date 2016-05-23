package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.CheckBox;
import junit.framework.Assert;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aldrin Batac on 5/23/16.
 */
public class PagedTableWithSelectAllLayoutTest {


    private static final String CHECKBOX_COLUMN_ID = "CheckBoxColumnId";

    private PagedTableWithSelectAllLayout pagedTableWithSelectAllLayout = new PagedTableWithSelectAllLayout(0, CHECKBOX_COLUMN_ID);

    @Before
    public void init(){

        pagedTableWithSelectAllLayout.instantiateComponents();

        this.initializePagedBreedingManagerTable();

    }


    @Test
    public void testGetAllEntriesPerPage(){


        int pageLength = 5;
        int firstPage = 1;
        int lastPage = 21;

        List<Object> entriesList = createListObject();
        pagedTableWithSelectAllLayout.getTable().setPageLength(pageLength);

        List<Object> result = pagedTableWithSelectAllLayout.getAllEntriesPerPage(entriesList, firstPage);

        Assert.assertEquals("The number of entries per page should be equal to the table's page length.", pageLength, result.size());

        List<Object> result2 = pagedTableWithSelectAllLayout.getAllEntriesPerPage(entriesList, lastPage);

        Assert.assertEquals("The last page should only have 1 item", 1, result2.size());

    }

    @Test
    public void testUpdateTagPerRowItem() {

        Integer firstSelectedObjectItemId = 1;
        Integer secondSelectedObjectItemId = 2;

        List<Object> selectedItems = new ArrayList<>();
        selectedItems.add(firstSelectedObjectItemId);
        selectedItems.add(secondSelectedObjectItemId);

        List<Object> loadedItems = new ArrayList<>(pagedTableWithSelectAllLayout.getTable().getItemIds());

        pagedTableWithSelectAllLayout.updateTagPerRowItem(selectedItems, loadedItems);

        PagedBreedingManagerTable table = pagedTableWithSelectAllLayout.getTable();

        CheckBox checkboxOfFirstItem = (CheckBox) table.getItem(firstSelectedObjectItemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
        Assert.assertTrue("The first item in table is selected so the checkbox value should be true", (Boolean) checkboxOfFirstItem.getValue());

        CheckBox checkboxOfSecondItem = (CheckBox) table.getItem(secondSelectedObjectItemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
        Assert.assertTrue("The second item in table is selected so the checkbox value should be true", (Boolean) checkboxOfSecondItem.getValue());

        // Other items that are not selected should have a checkbox with false value.
        for (Integer i = 3; i < table.getItemIds().size(); i++) {
            CheckBox checkbox = (CheckBox) table.getItem(i).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
            Assert.assertFalse("Item is not selected, the checkbox value should be false.", (Boolean) checkbox.getValue());
        }


    }

    @Test
    public void testUpdateSelectAllCheckBoxStatusEmptyList() {

        // Set the select all checkbox to true so we can verify that it's changed later
        pagedTableWithSelectAllLayout.getSelectAllCheckBox().setValue(true);

        pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(new ArrayList<>());

        Assert.assertFalse("There entry list is empty so the select all checkbox value should be false", (Boolean) pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

    }

    @Test
    public void testUpdateSelectAllCheckBoxStatusAllEntriesInAPageAreSelected() {


        pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);

        List<Object> entriesList = this.createEntriesList();

        markCheckboxItemsInTable(entriesList, true);

        pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(entriesList);

        Assert.assertTrue("All entries in page are selected, the select all checkbox value should be true", (Boolean) pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

    }

    @Test
    public void testUpdateSelectAllCheckBoxStatusSomeEntriesInAPageAreSelected() {


        pagedTableWithSelectAllLayout.getTable().setCurrentPage(1);

        List<Object> entriesList = this.createEntriesList();

        // Mark the first item as selected
        PagedBreedingManagerTable table = pagedTableWithSelectAllLayout.getTable();
        CheckBox cb = (CheckBox) table.getItem(1).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
        cb.setValue(true);

        pagedTableWithSelectAllLayout.updateSelectAllCheckBoxStatus(entriesList);

        Assert.assertFalse("Some entries in page are selected, the select all checkbox value should be false", (Boolean) pagedTableWithSelectAllLayout.getSelectAllCheckBox().getValue());

    }


    private List<Object> createEntriesList() {

        List<Object> entriesList = new ArrayList<>();
        entriesList.add(1);
        entriesList.add(2);
        entriesList.add(3);
        entriesList.add(4);
        entriesList.add(5);

        return entriesList;

    }

    private void markCheckboxItemsInTable(List<Object> selectedItems, Boolean checkboxValue) {

        PagedBreedingManagerTable table = pagedTableWithSelectAllLayout.getTable();

        for (Object itemId : selectedItems) {
            CheckBox cb = (CheckBox) table.getItem(itemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
            cb.setValue(checkboxValue);
        }

    }

    void initializePagedBreedingManagerTable() {

        PagedBreedingManagerTable table = pagedTableWithSelectAllLayout.getTable();

        table.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);

        int i = 1;
        while (i <= 101){
            table.addItem(i);
            table.getItem(i).getItemProperty(CHECKBOX_COLUMN_ID).setValue(new CheckBox());
            i++;
        }

    }

    List<Object> createListObject(){
        List<Object> entriesList = new ArrayList<>();
        int i = 1;
        while (i <= 101){
            entriesList.add(new Object());
            i++;
        }
        return entriesList;
    }



}

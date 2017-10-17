
package org.generationcp.breeding.manager.crossingmanager.util;

import java.util.Collection;

import junit.framework.Assert;

import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

public class CrossingManagerUtilTest {

	private static final int NO_OF_ENTRIES = 15;

	@Test
	public void testGetOddEntries() {
		Table listDataTable = this.getListDataTable();

		Collection<?> itemsWithOddEntryNo = CrossingManagerUtil.getOddEntries(listDataTable);

		for (Object item : itemsWithOddEntryNo) {
			Integer entryNo = (Integer) listDataTable.getItem(item).getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			Assert.assertTrue("Expecting that the row has odd entry no but didn't.", entryNo % 2 != 0);
		}
	}

	@Test
	public void testGetEvenEntries() {
		Table listDataTable = this.getListDataTable();

		Collection<?> itemsWithEvenEntryNo = CrossingManagerUtil.getEvenEntries(listDataTable);

		for (Object item : itemsWithEvenEntryNo) {
			Integer entryNo = (Integer) listDataTable.getItem(item).getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			Assert.assertTrue("Expecting that the row has even entry no but didn't.", entryNo % 2 == 0);
		}
	}

	private Table getListDataTable() {
		Table listDataTable = new Table();

		listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");

		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
			Item newItem = listDataTable.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);
		}

		return listDataTable;
	}
}


package org.generationcp.breeding.manager.crossingmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

public class CrossingManagerUtil {

	private CrossingManagerUtil() {
		// do not implement this method
	}

	public static Collection<?> getOddEntries(Table listDataTable) {
		List<Object> oddIds = new ArrayList<Object>();

		Collection<?> itemIds = listDataTable.getItemIds();
		for (Object itemId : itemIds) {
			Item item = listDataTable.getItem(itemId);
			Integer entryNo = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			if (entryNo.intValue() % 2 != 0) {
				oddIds.add(itemId);
			}
		}

		return oddIds;
	}

	public static Collection<?> getEvenEntries(Table listDataTable) {
		List<Object> evenIds = new ArrayList<Object>();

		Collection<?> itemIds = listDataTable.getItemIds();
		for (Object itemId : itemIds) {
			Item item = listDataTable.getItem(itemId);
			Integer entryNo = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			if (entryNo.intValue() % 2 == 0) {
				evenIds.add(itemId);
			}
		}

		return evenIds;
	}
}


package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

public class NewGermplasmEntriesFillColumnSource implements FillColumnSource {
	
	private final Table targetTable;
	private List<Integer> addedItemIds = new ArrayList<>();
	private List<Integer> addedGids = new ArrayList<>();

	public NewGermplasmEntriesFillColumnSource(final Table targetTable) {
		this.targetTable = targetTable;
	}

	@Override
	public List<Object> getItemIdsToProcess() {
		return Arrays.asList(this.addedItemIds.toArray());
	}

	@Override
	public List<Integer> getGidsToProcess() {
		return this.addedGids;
	}

	@Override
	public Integer getGidForItemId(final Object itemId) {
		if (this.addedItemIds.contains(itemId)) {
			final int index = this.addedItemIds.indexOf(itemId);
			return this.addedGids.get(index);
		}
		return null;
	}

	@Override
	public void setColumnValueForItem(final Object itemId, final String column, final Object value) {
		final Item item = this.targetTable.getItem(itemId);
		if (item != null) {
			final Property itemProperty = item.getItemProperty(column);
			if (itemProperty != null) {
				itemProperty.setValue(value);
			}
		}
	}

	@Override
	public void propagateUIChanges() {
		this.resetEditableTable();
	}

	protected void resetEditableTable() {
		// To trigger TableFieldFactory (fix for truncated data)
		if (this.targetTable.isEditable()) {
			this.targetTable.setEditable(false);
			this.targetTable.setEditable(true);
		}
	}
	
	public void setAddedItemIds(List<Integer> addedItemIds) {
		this.addedItemIds = addedItemIds;
	}

	
	public void setAddedGids(List<Integer> addedGids) {
		this.addedGids = addedGids;
	}


}

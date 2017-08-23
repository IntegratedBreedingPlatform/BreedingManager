
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;

import com.vaadin.ui.Table;

public class AddedEntryFillColumnSource implements FillColumnSource {

	private final Table targetTable;
	private final Integer addedItemId;
	private final Integer addedGid;

	public AddedEntryFillColumnSource(final Table targetTable, final Integer addedItemid, final Integer addedGid) {
		this.targetTable = targetTable;
		this.addedItemId = addedItemid;
		this.addedGid = addedGid;
	}

	@Override
	public List<Object> getItemIdsToProcess() {
		final List<Object> list =  new ArrayList<>();
		list.add(this.addedItemId);
		return list;
	}

	@Override
	public List<Integer> getGidsToProcess() {
		return Arrays.asList(this.addedGid);
	}

	@Override
	public Integer getGidForItemId(final Object itemId) {
		return this.addedGid;
	}

	@Override
	public void setColumnValueForItem(final Object itemId, final String column, final Object value) {
		this.targetTable.getItem(itemId).getItemProperty(column).setValue(value);
	}

	@Override
	public void propagateUIChanges() {
		// To trigger TableFieldFactory (fix for truncated data)
		if (this.targetTable.isEditable()) {
			this.targetTable.setEditable(false);
			this.targetTable.setEditable(true);
		}

	}

}

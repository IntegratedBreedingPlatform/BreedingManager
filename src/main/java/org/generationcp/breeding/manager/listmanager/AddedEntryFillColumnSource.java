
package org.generationcp.breeding.manager.listmanager;

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
	public List<Integer> getItemIdsToProcess() {
		return Arrays.asList(this.addedItemId);
	}

	@Override
	public List<Integer> getGidsToProcess() {
		return Arrays.asList(this.addedGid);
	}

	@Override
	public Integer getGidForItemId(final Integer itemId) {
		return this.addedGid;
	}

	@Override
	public void setColumnValueForItem(final Integer itemId, final String column, final Object value) {
		this.targetTable.getItem(itemId).getItemProperty(column).setValue(value);
	}

	@Override
	public void setUnsavedChanges() {
		// Do nothing as new column values were result of adding entry to table and not by user adding/filling up column manually
	}

	@Override
	public void resetEditableTable() {
		// To trigger TableFieldFactory (fix for truncated data)
		if (this.targetTable.isEditable()) {
			this.targetTable.setEditable(false);
			this.targetTable.setEditable(true);
		}

	}

}

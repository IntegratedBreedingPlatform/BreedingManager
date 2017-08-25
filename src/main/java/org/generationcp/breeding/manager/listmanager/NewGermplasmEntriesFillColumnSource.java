
package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

public class NewGermplasmEntriesFillColumnSource implements FillColumnSource {
	
	private static final Logger LOG = LoggerFactory.getLogger(NewGermplasmEntriesFillColumnSource.class);

	private final Table targetTable;
	private final List<Integer> addedItemIds;
	private final List<Integer> addedGids;

	public NewGermplasmEntriesFillColumnSource(final Table targetTable, final List<Integer> addedItemids, final List<Integer> addedGids) {
		this.targetTable = targetTable;
		this.addedItemIds = addedItemids;
		this.addedGids = addedGids;
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
			LOG.info("## GID = " + this.addedGids.get(index) + " for item " + itemId);
			return this.addedGids.get(index);
		}
		LOG.info("## NO GID found for = " + itemId);
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

}

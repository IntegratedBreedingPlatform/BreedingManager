package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;

/**
 * This handles generating values for added columns for items yet to be loaded (eg. when navigating pages) in paged table.
 * This does not have reference to search results table so the constructor parameters get information on items and gids
 * to be loaded on current page.
 *
 */
public class GermplasmSearchItemsToLoadFillColumnSource implements FillColumnSource {

	private List<Item> items;
	private List<Integer> gids;
	
	public GermplasmSearchItemsToLoadFillColumnSource(final List<Item> items, final List<Integer> gids) {
		super();
		this.items = items;
		this.gids = gids;
	}

	@Override
	public List<Object> getItemIdsToProcess() {
		return Arrays.asList(this.items.toArray());
	}

	@Override
	public List<Integer> getGidsToProcess() {
		return this.gids;
	}

	@Override
	public Integer getGidForItemId(Object itemId) {
		if (this.items.contains(itemId)) {
			final int index = this.items.indexOf(itemId);
			return this.gids.get(index);
		}
		return null;
	}

	@Override
	public void setColumnValueForItem(Object itemId, String column, Object value) {
		final Item item = (Item) itemId;
		item.addItemProperty(column, new ObjectProperty<>(value));
	}

	@Override
	public void setUnsavedChanges() {
		// Do nothing, we are not saving values for added columns from germplasm search results table
	}

	@Override
	public void resetEditableTable() {
		// Do nothing, the items are yet to be loaded on target table so no need refresh UI for generated changes
	}

}

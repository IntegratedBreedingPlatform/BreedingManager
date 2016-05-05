
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;

import com.jensjansson.pagedtable.PagedTable;

public class BreedingManagerTable extends PagedTable {

	private static final long serialVersionUID = 745102380412622592L;

	/**
	 * Variables used to keep track the multiple selection
	 */
	private Object multiSelectStartKey = 0;
	private Object multiSelectEndKey = 0;
	private List<Object> selectedEntries;

	public BreedingManagerTable(int recordCount, int maxRecords) {
		super();
		Integer pageLength = Math.min(recordCount, maxRecords);
		if (pageLength > 0) {
			this.setPageLength(pageLength);
		} else {
			this.setPageLength(maxRecords);
		}
		this.setImmediate(true);
		this.addListener();
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		this.setValueForSelectedItems();
	}

	private void addListener() {
		this.addListener(new ItemClickListener() {

			private static final long serialVersionUID = -2961821743069002565L;

			@Override
			public void itemClick(ItemClickEvent event) {
				BreedingManagerTable.this.assignMarkersForMultipleSelection(event);
			}
		});

		/**
		 * This handles the consecutive multiple selection within a table through CLICK 1st entry, then SHIFT + ARROW DOWN for the next
		 * entries
		 */
		this.addShortcutListener(new ShortcutListener("SHIFT + ARROW DOWN", KeyCode.ARROW_DOWN, new int[] {ModifierKey.SHIFT}) {

			private static final long serialVersionUID = -295921587056657139L;

			@Override
			public void handleAction(Object sender, Object target) {
				BreedingManagerTable bmTable = (BreedingManagerTable) target;
				@SuppressWarnings("unchecked")
				List<Object> selectedIds = new ArrayList<Object>((Collection<Object>) bmTable.getValue());
				List<Object> itemIds = new ArrayList<Object>(bmTable.getItemIds());
				if (selectedIds.size() != itemIds.size()) {
					bmTable.setMultiSelectEndKey(itemIds.get(selectedIds.size()));
					bmTable.setValueForSelectedItems();
				}

			}
		});
	}

	/**
	 * This handles the consecutive multiple selection within a table through CLICK 1st entry, then SHIFT + CLICK for the next entries
	 */
	void assignMarkersForMultipleSelection(ItemClickEvent event) {
		if (!event.isShiftKey()) {
			// marks the first key selected for multi-selection
			this.setMultiSelectStartKey(event.getItemId());
			// make sure the last key has no value
			this.setMultiSelectEndKey(null);
		} else {
			// if shift key is selected
			if (this.getMultiSelectStartKey() != null && event.isShiftKey()) {
				// marks the last key selected for multi-selection
				this.setMultiSelectEndKey(event.getItemId());
				this.setValueForSelectedItems();
			}
		}
	}

	void setValueForSelectedItems() {
		@SuppressWarnings("unchecked")
		Collection<Object> entries = (Collection<Object>) this.getItemIds();
		Map<Integer, Object> idEntryMap = new HashMap<Integer, Object>();
		int startIndex = 0;
		int endIndex = 0;
		int count = 1;
		for (Object entry : entries) {

			if (entry.equals(this.getMultiSelectStartKey())) {
				startIndex = count;
			}

			if (entry.equals(this.getMultiSelectEndKey())) {
				endIndex = count;
			}

			idEntryMap.put(count, entry);
			count++;
		}

		if (startIndex != 0 && endIndex != 0) {
			this.updateSelectedEntries(idEntryMap, startIndex, endIndex);
		}
	}

	void updateSelectedEntries(Map<Integer, Object> idEntryMap, int startIndex, int endIndex) {
		this.selectedEntries = new ArrayList<Object>();

		// traverse the ids within the current order
		for (Map.Entry<Integer, Object> entry : idEntryMap.entrySet()) {

			int index = entry.getKey();
			Object value = entry.getValue();

			if (this.isPartOfSelectedIncRange(startIndex, endIndex, index) || this.isPartOfSelectedDecRange(startIndex, endIndex, index)) {
				this.selectedEntries.add(value);
			}

		}

		if (!this.selectedEntries.isEmpty()) {
			this.setValue(this.selectedEntries);
		}
	}

	// decreasing range
	boolean isPartOfSelectedDecRange(int startIndex, int endIndex, int index) {
		return startIndex > endIndex && (index <= startIndex && index >= endIndex);
	}

	// increasing range
	boolean isPartOfSelectedIncRange(int startIndex, int endIndex, int index) {
		return startIndex < endIndex && (index >= startIndex && index <= endIndex);
	}

	public Object getMultiSelectStartKey() {
		return this.multiSelectStartKey;
	}

	public void setMultiSelectStartKey(Object multiSelectStartKey) {
		this.multiSelectStartKey = multiSelectStartKey;
	}

	public Object getMultiSelectEndKey() {
		return this.multiSelectEndKey;
	}

	public void setMultiSelectEndKey(Object multiSelectEndKey) {
		this.multiSelectEndKey = multiSelectEndKey;
	}

	public List<Object> getSelectedEntries() {
		return this.selectedEntries;
	}

	public void setSelectedEntries(List<Object> selectedEntries) {
		this.selectedEntries = selectedEntries;
	}

}

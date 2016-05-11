package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Table;

/**
 * Created by cyrus on 05/05/2016.
 */
public class TableMultipleSelectionHandler extends ShortcutListener implements ItemClickEvent.ItemClickListener {

	/**
	 * Variables used to keep track the multiple selection
	 */
	private Object multiSelectStartKey = 0;
	private Object multiSelectEndKey = 0;
	private List<Object> selectedEntries;

	private Table tableInstance;

	public TableMultipleSelectionHandler(Table tableInstance) {
		super("SHIFT + ARROW DOWN", KeyCode.ARROW_DOWN, new int[] {ModifierKey.SHIFT});

		this.tableInstance = tableInstance;
	}

	/**
	 * This will assign the markers for multiple selection
	 * This handles the consecutive multiple selection within a table through CLICK 1st entry, then SHIFT + CLICK for the next entries
	 * @param itemClickEvent
	 */
	@Override
	public void itemClick(ItemClickEvent itemClickEvent) {
		if (!itemClickEvent.isShiftKey()) {
			// marks the first key selected for multi-selection
			this.setMultiSelectStartKey(itemClickEvent.getItemId());
			// make sure the last key has no value
			this.setMultiSelectEndKey(null);
		} else {
			// if shift key is selected
			if (this.getMultiSelectStartKey() != null && itemClickEvent.isShiftKey()) {
				// marks the last key selected for multi-selection
				this.setMultiSelectEndKey(itemClickEvent.getItemId());
				this.setValueForSelectedItems();
			}
		}

	}

	@Override
	public void handleAction(Object source, Object target) {
		// do not need to handle
	}

	/**
	 * This will set the value of checkbox as checked when it is selected
	 */
	public void setValueForSelectedItems() {
		@SuppressWarnings("unchecked")
		Collection<Object> entries = (Collection<Object>) tableInstance.getItemIds();
		Map<Integer, Object> idEntryMap = new HashMap<>();
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
			tableInstance.setValue(this.selectedEntries);
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

	Object getMultiSelectStartKey() {
		return this.multiSelectStartKey;
	}

	void setMultiSelectStartKey(Object multiSelectStartKey) {
		this.multiSelectStartKey = multiSelectStartKey;
	}

	Object getMultiSelectEndKey() {
		return this.multiSelectEndKey;
	}

	void setMultiSelectEndKey(Object multiSelectEndKey) {
		this.multiSelectEndKey = multiSelectEndKey;
	}

	List<Object> getSelectedEntries() {
		return this.selectedEntries;
	}

	void setSelectedEntries(List<Object> selectedEntries) {
		this.selectedEntries = selectedEntries;
	}

}

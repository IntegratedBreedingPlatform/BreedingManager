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

	@Override
	public void itemClick(ItemClickEvent itemClickEvent) {
		this.assignMarkersForMultipleSelection(itemClickEvent);
	}

	@Override
	public void handleAction(Object source, Object target) {
		TableMultipleSelectionHandler targetTableSelectionHandler = (TableMultipleSelectionHandler) target;

		Table tableInstance = targetTableSelectionHandler.getTableInstance();

		@SuppressWarnings("unchecked")
		List<Object> selectedIds = new ArrayList<>((Collection<Object>) tableInstance.getValue());
		List<Object> itemIds = new ArrayList<>(tableInstance.getItemIds());
		if (selectedIds.size() != itemIds.size()) {
			targetTableSelectionHandler.setMultiSelectEndKey(itemIds.get(selectedIds.size()));
			targetTableSelectionHandler.setValueForSelectedItems();
		}
	}

	public Table getTableInstance() {
		return tableInstance;
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

	public void setValueForSelectedItems() {
		@SuppressWarnings("unchecked")
		Collection<Object> entries = (Collection<Object>) tableInstance.getItemIds();
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

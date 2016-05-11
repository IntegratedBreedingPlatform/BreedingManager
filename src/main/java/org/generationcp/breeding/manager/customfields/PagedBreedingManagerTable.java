package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import com.jensjansson.pagedtable.PagedTable;

/**
 * Created by cyrus on 05/05/2016.
 */
public class PagedBreedingManagerTable extends PagedTable {
	private TableMultipleSelectionHandler tableMultipleSelectionHandler;

	public PagedBreedingManagerTable(int recordCount, int maxRecords) {
		super();

		Integer pageLength = Math.min(recordCount, maxRecords);
		if (pageLength > 0) {
			this.setPageLength(pageLength);
		} else {
			this.setPageLength(maxRecords);
		}

		this.setTableHandler(new TableMultipleSelectionHandler(this));
	}


	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		// clone the variables map into mutable hashmap
		// this fixes the vaadin issue that the page length is being modified when this method activates
		Map<String, Object> variablesCopy = new HashMap<>(variables);
		if (variablesCopy.containsKey("pagelength")) {
			variablesCopy.remove("pagelength");
		}

		// perform actual table.changeVariables
		this.doChangeVariables(source, variablesCopy);
	}

	@Override
	/**
	 * This will just override the styles and look of the PagedTable paging controls
	 */
	public HorizontalLayout createControls() {
		HorizontalLayout controls = super.createControls();

		Iterator<Component> iterator = controls.getComponentIterator();

		while (iterator.hasNext()) {
			Component c = iterator.next();
			if (c instanceof HorizontalLayout) {
				Iterator<Component> iterator2 = ((HorizontalLayout) c).getComponentIterator();

				while (iterator2.hasNext()) {
					Component d = iterator2.next();

					if (d instanceof Button) {
						d.setStyleName("");
					}
					if (d instanceof TextField) {
						d.setWidth("30px");
					}

				}
			}
		}
		return controls;
	}

	/**
	 * Set the instance of tableMultipleSelectionHandler
	 */
	void setTableHandler(TableMultipleSelectionHandler tableMultipleSelectionHandler) {

		// remove this tables listener if exists and replace it with the new handler
		this.removeListener(this.tableMultipleSelectionHandler);
		this.removeShortcutListener(this.tableMultipleSelectionHandler);

		// set this table's current handler
		this.tableMultipleSelectionHandler = tableMultipleSelectionHandler;

		// add the new handler as this table's listener
		this.addListener(this.tableMultipleSelectionHandler);
		this.addShortcutListener(this.tableMultipleSelectionHandler);
	}

	/**
	 * Perform actual changeVariables
	 * @param source
	 * @param variablesCopy
	 */
	void doChangeVariables(Object source, Map<String, Object> variablesCopy) {
		super.changeVariables(source, variablesCopy);
		tableMultipleSelectionHandler.setValueForSelectedItems();
	}

}

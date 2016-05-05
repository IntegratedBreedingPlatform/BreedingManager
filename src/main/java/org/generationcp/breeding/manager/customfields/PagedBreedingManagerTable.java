package org.generationcp.breeding.manager.customfields;

import java.util.Map;

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
		this.setImmediate(true);

		this.tableMultipleSelectionHandler = new TableMultipleSelectionHandler(this);

		this.addListener(this.tableMultipleSelectionHandler);
		this.addShortcutListener(this.tableMultipleSelectionHandler);

	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		tableMultipleSelectionHandler.setValueForSelectedItems();
	}
}

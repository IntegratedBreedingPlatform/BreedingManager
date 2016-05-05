
package org.generationcp.breeding.manager.customfields;

import java.util.Map;

import com.vaadin.ui.Table;

public class BreedingManagerTable extends Table {

	private static final long serialVersionUID = 745102380412622592L;

	private TableMultipleSelectionHandler tableMultipleSelectionHandler;

	public BreedingManagerTable(int recordCount, int maxRecords) {
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

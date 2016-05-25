package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Container;
import com.vaadin.ui.HorizontalLayout;

import com.jensjansson.pagedtable.PagedTable;
import com.jensjansson.pagedtable.PagedTableContainer;

public class PagedBreedingManagerTable extends PagedTable {

	private EntrySelectSyncHandler entrySelectSyncHandler;
	private TableMultipleSelectionHandler tableMultipleSelectionHandler;
	private Integer pageLength;

	public PagedBreedingManagerTable(final int recordCount, final int maxRecords) {
		super();

		pageLength = Math.min(recordCount, maxRecords);
		if (pageLength <= 0) {
			pageLength = 20;
		}

		this.setPageLength(pageLength);

		this.setTableHandler(new TableMultipleSelectionHandler(this));
	}

	@Override
	public void changeVariables(final Object source, final Map<String, Object> variables) {
		// clone the variables map into mutable hashmap
		// this fixes the vaadin issue that the page length is being modified when this method activates
		final Map<String, Object> variablesCopy = new HashMap<>(variables);
		if (variablesCopy.containsKey("pagelength")) {
			variablesCopy.remove("pagelength");
		}

		// perform actual table.changeVariables
		this.doChangeVariables(source, variablesCopy);
	}

	/**
	 * This will just override the styles and look of the PagedTable paging controls
	 */
	@Override
	public HorizontalLayout createControls() {
		return new PagedBreedingManagerTableControls(this, this.entrySelectSyncHandler);
	}

	/**
	 * Register a table select all handler
	 * @param handler
	 */
	public void registerTableSelectHandler(final EntrySelectSyncHandler handler) {
		this.entrySelectSyncHandler = handler;
	}

	/**
	 * Set the instance of tableMultipleSelectionHandler
	 */
	void setTableHandler(final TableMultipleSelectionHandler tableMultipleSelectionHandler) {

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
	 *
	 * @param source
	 * @param variablesCopy
	 */
	void doChangeVariables(final Object source, final Map<String, Object> variablesCopy) {
		super.changeVariables(source, variablesCopy);
		tableMultipleSelectionHandler.setValueForSelectedItems();
	}

	boolean hasItems() {
		return this.getContainerDataSource().getContainerPropertyIds().isEmpty();
	}

	void setBatchSize(int batchSize) {
		final Container.Indexed contanerSource = this.getContainerDataSource();
		((LazyQueryContainer)((PagedTableContainer) contanerSource).getContainer()).getQueryView().getQueryDefinition().setBatchSize(batchSize);
	}

	void updateBatchsize() {
		if (this.hasItems()) {
			this.setBatchSize(this.getPageLength());
		}
	}

	/**
	 * Handler used to dispatch selectAll events to this table
	 */
	public interface EntrySelectSyncHandler {

		void dispatch();
	}
}

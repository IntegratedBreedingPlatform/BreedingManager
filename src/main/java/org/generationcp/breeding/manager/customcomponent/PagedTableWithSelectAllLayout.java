
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class PagedTableWithSelectAllLayout extends VerticalLayout implements BreedingManagerLayout, InitializingBean {

	private static final long serialVersionUID = -4500578362272218341L;

	private PagedBreedingManagerTable table;
	private final Object checkboxColumnId;

	private int recordCount = 0;
	private int maxRecords = 0;

	private CheckBox selectAllCheckBox;
	/**
	 * This will serve as a marker of pages already loaded in the paged table
	 */
	private final Set<Integer> loadedPaged;

	public PagedTableWithSelectAllLayout(final int recordCount, final Object checkboxColumnId) {
		this.recordCount = this.maxRecords = recordCount;
		this.checkboxColumnId = checkboxColumnId;
		this.loadedPaged = new LinkedHashSet<Integer>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.table = new PagedBreedingManagerTable(this.recordCount, this.maxRecords);
		this.table.setImmediate(true);

		this.selectAllCheckBox = new CheckBox("Select All");
		this.selectAllCheckBox.setImmediate(true);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		this.table.addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
				PagedTableWithSelectAllLayout.this.syncItemCheckBoxes();
			}
		});

		this.table.registerTableSelectHandler(new PagedBreedingManagerTable.EntrySelectSyncHandler() {
			@Override
			public void dispatch() {
				PagedTableWithSelectAllLayout.this.syncItemCheckBoxes();
			}
		});

		this.selectAllCheckBox.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7882379695058054587L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final boolean checkBoxValue = event.getButton().booleanValue();

				final Collection<Object> entries = (Collection<Object>) PagedTableWithSelectAllLayout.this.table.getItemIds();
				final List<Object> entriesList = new ArrayList<>(entries);

				final List<Object> entriesPerPage =
						PagedTableWithSelectAllLayout.this.getAllEntriesPerPage(entriesList,
								PagedTableWithSelectAllLayout.this.table.getCurrentPage());

				for (final Object entry : entriesPerPage) {
					final CheckBox tag =
							(CheckBox) PagedTableWithSelectAllLayout.this.table.getItem(entry)
									.getItemProperty(PagedTableWithSelectAllLayout.this.checkboxColumnId).getValue();
					tag.setValue(checkBoxValue);
				}

				PagedTableWithSelectAllLayout.this.updatePagedTableSelectedEntries(entriesPerPage, checkBoxValue);
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setSizeUndefined();
		this.setWidth("100%");

		this.addComponent(this.table);
		this.addComponent(((PagedTable) this.table).createControls());
		this.addComponent(this.selectAllCheckBox);
	}

	@SuppressWarnings("unchecked")
	public void syncItemCheckBoxes() {
		final Collection<Object> entries = (Collection<Object>) this.table.getItemIds();
		final List<Object> entriesList = new ArrayList<>(entries);

		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();

		// update the loaded list of page no
		this.updateLoadedPage();

		// update the select all status based on the selected items within the current page of the table
		this.updateSelectAllCheckBoxStatus(entriesList);

		final List<Object> loadedItems = new ArrayList<Object>();
		if (entries.size() > this.table.getPageLength()) {
			for (final Integer pageNo : this.loadedPaged) {
				final List<Object> entriesPerPage = this.getAllEntriesPerPage(entriesList, pageNo);
				loadedItems.addAll(entriesPerPage);
			}
		} else {
			loadedItems.addAll(entriesList);
		}

		this.updateTagPerRowItem(selectedEntries, loadedItems);
	}

	/***
	 * Retrieves all items per page
	 * 
	 * @param entriesList - list of all entries of the entire table
	 * @param pageNo - current page
	 * @return
	 */
	private List<Object> getAllEntriesPerPage(final List<Object> entriesList, final Integer pageNo) {
		final Integer noOfEntriesPerPage = this.table.getPageLength();
		final Integer startIdx = pageNo * noOfEntriesPerPage - noOfEntriesPerPage;
		Integer endIdx = startIdx + noOfEntriesPerPage;
		endIdx = (endIdx > entriesList.size()) ? entriesList.size() : endIdx;

		return entriesList.subList(startIdx, endIdx);
	}

	/***
	 * Make sure to update the current status of selection per page
	 * 
	 * @param selectedEntries - list of items currently selected on the table
	 * @param loadedItems - the list of items loaded on the paged table (could be multiple entries across pages)
	 */
	private void updateTagPerRowItem(final Collection<Object> selectedEntries, final List<Object> loadedItems) {
		for (final Object entry : loadedItems) {
			final Property itemProperty = this.table.getItem(entry).getItemProperty(this.checkboxColumnId);
			if (itemProperty != null) {
				final CheckBox tag = (CheckBox) itemProperty.getValue();
				if (selectedEntries.contains(entry)) {
					tag.setValue(true);
				} else {
					tag.setValue(false);
				}
			}
		}
	}

	/**
	 * Update "Select All" based on the number of selected entries from the table trigger through user selection of entries
	 * 
	 * @param entriesList - collection of all entries within the table
	 */
	private void updateSelectAllCheckBoxStatus(final List<Object> entriesList) {

		final List<Object> entriesPerPage = this.getAllEntriesPerPage(entriesList, this.table.getCurrentPage());
		int noOfSelectedEntriesPerPage = 0;

		for (final Object entry : entriesPerPage) {
			final Property itemProperty = this.table.getItem(entry).getItemProperty(this.checkboxColumnId);
			if (itemProperty != null) {
				final CheckBox tag = (CheckBox) itemProperty.getValue();
				if (Boolean.valueOf(tag.getValue().toString())) {
					noOfSelectedEntriesPerPage++;
				}
			}
		}

		if (entriesPerPage.size() == noOfSelectedEntriesPerPage && !entriesPerPage.isEmpty()) {
			this.selectAllCheckBox.setValue(true);
		} else {
			this.selectAllCheckBox.setValue(false);
		}
	}

	/**
	 * Select All Entries on the Current Page of the Paged Table
	 */
	public void selectAllEntriesOnCurrentPage() {

		final Collection<Object> entries = (Collection<Object>) this.table.getItemIds();
		final List<Object> entriesList = new ArrayList<>(entries);

		final List<Object> entriesPerPage = this.getAllEntriesPerPage(entriesList, this.table.getCurrentPage());

		// set the selected items on the table
		this.updatePagedTableSelectedEntries(entriesPerPage, true);

		// select all entries within the current page
		this.updateTagPerRowItem(entriesPerPage, entriesPerPage);

		// update the Select All checkbox based on the selected entries
		this.updateSelectAllCheckBoxStatus(entriesList);
	}

	/**
	 * Update the selected entries from paged table
	 * 
	 * @param entriesPerPage - to add or remove from the existing list of entries
	 * @param addEntry - if true, add the entries, otherwise, removed the entire list from selected entries
	 */
	private void updatePagedTableSelectedEntries(final List<Object> entriesPerPage, final boolean addEntry) {
		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();
		final Set<Object> selectedEntriesSet = new HashSet<Object>();
		selectedEntriesSet.addAll(selectedEntries);

		if (addEntry) {
			selectedEntriesSet.addAll(entriesPerPage);
		} else {
			selectedEntriesSet.removeAll(entriesPerPage);
		}

		this.table.setValue(selectedEntriesSet);
	}

	public PagedBreedingManagerTable getTable() {
		return this.table;
	}

	public void setTable(final PagedBreedingManagerTable table) {
		this.table = table;
	}

	public void refreshTablePagingControls() {
		this.replaceComponent(this.getComponent(1), ((PagedTable) this.table).createControls());
		this.resetLoadedPage();
	}

	/**
	 * Make sure that the list of loaded page no is included to the possible no of page given the current page length
	 */
	public void updateLoadedPage() {
		this.loadedPaged.add(this.table.getCurrentPage());

		final Integer totalNoOfTableEntries = this.table.getItemIds().size();
		final Integer noOfEntriesPerPage = this.table.getPageLength();

		Iterator<Integer> loadedPagedIterator = this.loadedPaged.iterator();
		while (loadedPagedIterator.hasNext()) {

			Integer pageNo = loadedPagedIterator.next();

			final Integer startIdx = pageNo * noOfEntriesPerPage - noOfEntriesPerPage;
			Integer endIdx = startIdx + noOfEntriesPerPage;
			endIdx = (endIdx > totalNoOfTableEntries) ? totalNoOfTableEntries : endIdx;
			if (startIdx > endIdx) {
				loadedPagedIterator.remove();
			}
		}

	}

	public void resetLoadedPage() {
		this.loadedPaged.clear();
	}

}

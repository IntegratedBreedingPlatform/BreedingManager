
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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

	private CheckBox selectAllOnPageCheckBox;
	

	/**
	 * This will serve as a marker of pages already loaded in the paged table
	 */
	private final Set<Integer> loadedPages;

	public PagedTableWithSelectAllLayout(final int recordCount, final Object checkboxColumnId) {
		this.recordCount = this.maxRecords = recordCount;
		this.checkboxColumnId = checkboxColumnId;
		this.loadedPages = new LinkedHashSet<Integer>();
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
		this.table.setDebugId("table");
		this.table.setImmediate(true);

		this.selectAllOnPageCheckBox = new CheckBox("Select All (this page)");
		this.selectAllOnPageCheckBox.setDebugId("selectAllOnPageCheckBox");
		this.selectAllOnPageCheckBox.setImmediate(true);
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

		this.selectAllOnPageCheckBox.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7882379695058054587L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final boolean checkBoxValue = event.getButton().booleanValue();
				toggleAllEntriesSelectionOnCurrentPage(checkBoxValue);

			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setSizeUndefined();
		this.setWidth("100%");

		this.addComponent(this.table);
		this.addComponent(((PagedTable) this.table).createControls());
		this.addComponent(this.selectAllOnPageCheckBox);
	}

	@SuppressWarnings("unchecked")
	public void syncItemCheckBoxes() {
		final Collection<Object> entries = (Collection<Object>) this.table.getItemIds();
		final List<Object> entriesList = new ArrayList<>(entries);

		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();

		// update the loaded list of page no
		this.updateLoadedPages();

		// update the "Select all on page" status based on the selected items within the current page of the table
		this.updateSelectAllOnPageCheckBoxStatus(entriesList);

		final List<Object> loadedItems = new ArrayList<Object>();
		if (entries.size() > this.table.getPageLength()) {
			for (final Integer pageNo : this.loadedPages) {
				final List<Object> entriesPerPage = this.getAllEntriesForPage(entriesList, pageNo);
				loadedItems.addAll(entriesPerPage);
			}
		} else {
			loadedItems.addAll(entriesList);
		}

		this.updateItemSelectCheckboxes(selectedEntries, loadedItems);
	}

	/***
	 * Retrieves all items for given page
	 *
	 * @param entriesList - list of all entries of the entire table
	 * @param pageNo - current page
	 * @return
	 */
	List<Object> getAllEntriesForPage(final List<Object> entriesList, final Integer pageNo) {
		final Integer pageLength = this.table.getPageLength();
		final Integer startingIndex = pageNo * pageLength - pageLength;
		Integer endingIndex = startingIndex + pageLength;
		endingIndex = endingIndex > entriesList.size() ? entriesList.size() : endingIndex;

		return entriesList.subList(startingIndex, endingIndex);
	}

	/***
	 * Update the selection checkboxes per item on loaded pages
	 *
	 * @param selectedEntries - list of items currently selected on the table
	 * @param loadedItems - the list of items loaded on the paged table (entries could span across multiple pages)
	 */
	void updateItemSelectCheckboxes(final Collection<Object> selectedEntries, final List<Object> loadedItems) {
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
	 * Update "Select All On Page" checkbox based on the number of selected entries on current page.
	 * If at least one is unselected, then "Select All On Page" checkbox will be unselected.
	 *
	 * @param entriesList - list of all entries within the table
	 */
	void updateSelectAllOnPageCheckBoxStatus(final List<Object> entriesList) {

		final List<Object> entriesForCurrentPage = this.getAllEntriesForPage(entriesList, this.table.getCurrentPage());
		int noOfSelectedEntriesForCurrentPage = 0;

		for (final Object entry : entriesForCurrentPage) {
			final Property itemProperty = this.table.getItem(entry).getItemProperty(this.checkboxColumnId);
			if (itemProperty != null) {
				final CheckBox tag = (CheckBox) itemProperty.getValue();
				if (Boolean.valueOf(tag.getValue().toString())) {
					noOfSelectedEntriesForCurrentPage++;
				}
			}
		}

		if (entriesForCurrentPage.size() == noOfSelectedEntriesForCurrentPage && !entriesForCurrentPage.isEmpty()) {
			this.selectAllOnPageCheckBox.setValue(true);
		} else {
			this.selectAllOnPageCheckBox.setValue(false);
		}
	}

	/**
	 * Select All Entries on the Current Page of the Paged Table
	 */
	public void selectAllEntriesOnCurrentPage() {
		toggleAllEntriesSelectionOnCurrentPage(true);
	}

	/**
	 * Selects or unselects all entries on current page.
	 * 
	 * @param doSelectAll - if true, selects all entries on current page. Else, unselects all entries on current page.
	 */
	@SuppressWarnings("unchecked")
	private void toggleAllEntriesSelectionOnCurrentPage(final boolean doSelectAll) {
		final Collection<Object> allEntries = (Collection<Object>) this.table.getItemIds();
		final List<Object> allEntriesList = new ArrayList<>(allEntries);

		final List<Object> entriesForCurrentPage = this.getAllEntriesForPage(allEntriesList, this.table.getCurrentPage());

		// set the selected items on the table
		this.updatePagedTableSelectedEntries(entriesForCurrentPage, doSelectAll);

		// Update checkbox of each item within the current page
		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();
		this.updateItemSelectCheckboxes(selectedEntries, entriesForCurrentPage);

		// update the Select All checkbox based on the selected entries
		this.updateSelectAllOnPageCheckBoxStatus(allEntriesList);
	}

	/**
	 * Update the selected entries from paged table
	 *
	 * @param entriesToAddOrRemove - entries to add or remove from the existing list of selected entries
	 * @param addEntry - if true, add the entries. Otherwise, remove the entries from selected entries
	 */
	@SuppressWarnings("unchecked")
	private void updatePagedTableSelectedEntries(final List<Object> entriesToAddOrRemove, final boolean addEntry) {
		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();
		final Set<Object> selectedEntriesSet = new HashSet<Object>();

		selectedEntriesSet.addAll(selectedEntries);

		if (addEntry) {
			selectedEntriesSet.addAll(entriesToAddOrRemove);
		} else {
			selectedEntriesSet.removeAll(entriesToAddOrRemove);
		}

		this.table.setValue(selectedEntriesSet);
	}

	/**
	 * Update selected status for all entries
	 *
	 * @param addEntry - if true, select all entries. Otherwise, remove all entries
	 */
	@SuppressWarnings("unchecked")
	public void updatePagedTableSelectedEntries(final boolean addEntry) {
		final ArrayList<Object> entries = new ArrayList<Object>((Collection<Object>) this.table.getValue());
		this.updatePagedTableSelectedEntries(entries, addEntry);
	}

	public PagedBreedingManagerTable getTable() {
		return this.table;
	}

	public void setTable(final PagedBreedingManagerTable table) {
		this.table = table;
	}

	public void refreshTablePagingControls() {
		this.replaceComponent(this.getComponent(1), this.table.createControls());
		/*
		 * Since the controls will only be disabled/enabled if a page change occurs, we need to call firePagedChangedEvent but since it is a
		 * private method we need to have a hack where we simulate clicking the previous page.
		 */
		this.table.previousPage();
		this.resetLoadedPages();
	}

	/**
	 * Updates the set of loaded page numbers given the current page length (which could have been changed)
	 */
	public void updateLoadedPages() {
		this.loadedPages.add(this.table.getCurrentPage());

		final Integer totalNoOfTableEntries = this.table.getItemIds().size();
		final Integer pageLength = this.table.getPageLength();

		final Iterator<Integer> loadedPagesIterator = this.loadedPages.iterator();
		while (loadedPagesIterator.hasNext()) {

			final Integer pageNo = loadedPagesIterator.next();

			final Integer startingIndex = pageNo * pageLength - pageLength;
			Integer endingIndex = startingIndex + pageLength;
			endingIndex = endingIndex > totalNoOfTableEntries ? totalNoOfTableEntries : endingIndex;
			if (startingIndex > endingIndex) {
				loadedPagesIterator.remove();
			}
		}

	}

	public void resetLoadedPages() {
		this.loadedPages.clear();
	}

	public CheckBox getSelectAllOnPageCheckBox() {
		return this.selectAllOnPageCheckBox;
	}

	public Set<Integer> getLoadedPages() {
		return this.loadedPages;
	}

}

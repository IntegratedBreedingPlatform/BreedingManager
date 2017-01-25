
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class PagedTableWithSelectAllLayout extends VerticalLayout implements BreedingManagerLayout, InitializingBean {

	public static final int INDEX_OF_CHECKBOXES_LAYOUT = 2;
	private static final long serialVersionUID = -4500578362272218341L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private PagedBreedingManagerTable table;
	private final Object checkboxColumnId;

	private int recordCount = 0;
	private int maxRecords = 0;

	private CheckBox selectAllOnPageCheckBox;
	private CheckBox selectAllEntriesCheckBox;
	private Button unselectAllEntriesBtn;

	public PagedTableWithSelectAllLayout(final int recordCount, final Object checkboxColumnId) {
		this.recordCount = this.maxRecords = recordCount;
		this.checkboxColumnId = checkboxColumnId;
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

		this.selectAllOnPageCheckBox = new CheckBox(this.messageSource.getMessage(Message.SELECT_ALL_ON_PAGE));
		this.selectAllOnPageCheckBox.setDebugId("selectAllOnPageCheckBox");
		this.selectAllOnPageCheckBox.setImmediate(true);

		this.selectAllEntriesCheckBox = new CheckBox(this.messageSource.getMessage(Message.SELECT_ALL_PAGES));
		this.selectAllEntriesCheckBox.setDebugId("selectAllEntriesCheckBox");
		this.selectAllEntriesCheckBox.setImmediate(true);

		this.unselectAllEntriesBtn = new Button(this.messageSource.getMessage(Message.CLEAR));
		this.unselectAllEntriesBtn.setDebugId("unselectAllEntriesBtn");
		this.unselectAllEntriesBtn.setImmediate(true);
		this.unselectAllEntriesBtn.setStyleName(BaseTheme.BUTTON_LINK);
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
				PagedTableWithSelectAllLayout.this.toggleAllEntriesSelectionOnCurrentPage(checkBoxValue);

			}
		});

		this.selectAllEntriesCheckBox.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 8083546048521503747L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final boolean checkBoxValue = event.getButton().booleanValue();
				PagedTableWithSelectAllLayout.this.toggleAllEntriesSelection(checkBoxValue);
			}
		});

		this.unselectAllEntriesBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1661230371092902980L;

			@Override
			public void buttonClick(final ClickEvent event) {
				PagedTableWithSelectAllLayout.this.clearAllSelectedEntries();
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setSizeUndefined();
		this.setWidth("100%");

		this.addComponent(this.table);
		this.addComponent(((PagedTable) this.table).createControls());

		final HorizontalLayout selectAllLayout = generateCheckboxesLayout();

		this.addComponent(selectAllLayout);
	}

	/*
	 * Generate  layout for select checkboxes and clear button/link
	 */
	private HorizontalLayout generateCheckboxesLayout() {
		final HorizontalLayout selectAllLayout = new HorizontalLayout();
		selectAllLayout.setDebugId("selectAllLayout");
		selectAllLayout.setSpacing(true);
		selectAllLayout.addComponent(this.selectAllOnPageCheckBox);
		if (this.table.getTotalAmountOfPages() > 1) {
			selectAllLayout.addComponent(this.selectAllEntriesCheckBox);
		}
		selectAllLayout.addComponent(this.unselectAllEntriesBtn);
		return selectAllLayout;
	}

	public void syncItemCheckBoxes() {
		// Update checkboxes only on current page as other pages are refreshed upon loading them
		// "Selected" items are not affected - Actual table selection is determined by table.getValue()
		this.updateItemSelectCheckboxes();

		// update the "Select all on page" and "Select All Pages" checkboxes status based on the selected items
		this.updateSelectAllOnPageCheckBoxStatus();
		this.updateSelectAllEntriesCheckboxStatus();
	}

	/***
	 * Retrieves all items for given page
	 *
	 * @param pageNo - current page
	 * @return
	 */
	@SuppressWarnings("unchecked")
	List<Object> getAllEntriesForPage(final Integer pageNo) {
		final Collection<Object> allEntries = (Collection<Object>) this.table.getItemIds();
		final List<Object> allEntriesList = new ArrayList<>(allEntries);

		final Integer pageLength = this.table.getPageLength();
		final Integer startingIndex = pageNo * pageLength - pageLength;
		Integer endingIndex = startingIndex + pageLength;
		endingIndex = endingIndex > allEntriesList.size() ? allEntriesList.size() : endingIndex;

		return allEntriesList.subList(startingIndex, endingIndex);
	}

	/***
	 * Update the selection checkboxes per item on current page.
	 */
	@SuppressWarnings("unchecked")
	void updateItemSelectCheckboxes() {
		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();
		final List<Object> entriesForCurrentPage = this.getAllEntriesForPage(this.table.getCurrentPage());

		for (final Object entry : entriesForCurrentPage) {
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
	 * Update "Select All On Page" checkbox based on the number of selected entries on current page. If at least one is unselected, then
	 * "Select All On Page" checkbox will be unselected.
	 *
	 */
	void updateSelectAllOnPageCheckBoxStatus() {

		final List<Object> entriesForCurrentPage = this.getAllEntriesForPage(this.table.getCurrentPage());
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
	 * Update "Select All Pages" checkbox based on the number of selected entries on all pages. If at least one is unselected, then
	 * "Select All Pages" checkbox will be unselected.
	 *
	 */
	@SuppressWarnings("unchecked")
	void updateSelectAllEntriesCheckboxStatus() {
		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();
		final Collection<Object> allEntries = (Collection<Object>) this.table.getItemIds();

		if (!allEntries.isEmpty() && selectedEntries.size() == allEntries.size()) {
			this.selectAllEntriesCheckBox.setValue(true);
		} else {
			this.selectAllEntriesCheckBox.setValue(false);
		}
	}

	/**
	 * Select All Entries on the Current Page of the Paged Table
	 */
	public void selectAllEntriesOnCurrentPage() {
		this.toggleAllEntriesSelectionOnCurrentPage(true);
	}

	/**
	 * Selects or unselects all entries on current page.
	 *
	 * @param doSelectAll - if true, selects all entries on current page. Else, unselects all entries on current page.
	 */
	private void toggleAllEntriesSelectionOnCurrentPage(final boolean doSelectAll) {
		final List<Object> entriesForCurrentPage = this.getAllEntriesForPage(this.table.getCurrentPage());
		this.toggleEntriesSelectionStatus(entriesForCurrentPage, doSelectAll);
	}

	/**
	 * Select All Entries on the Current Page of the Paged Table
	 */
	public void selectAllEntries() {
		this.toggleAllEntriesSelection(true);
	}

	/**
	 * Selects or unselects all entries on all pages.
	 *
	 * @param doSelectAll - if true, selects all entries on all pages. Else, unselects all entries on all pages.
	 */
	@SuppressWarnings("unchecked")
	private void toggleAllEntriesSelection(final boolean doSelectAll) {
		final Collection<Object> allEntries = (Collection<Object>) this.table.getItemIds();
		final List<Object> allEntriesList = new ArrayList<>(allEntries);
		this.toggleEntriesSelectionStatus(allEntriesList, doSelectAll);
	}

	/**
	 * Selects or unselects given set of entries
	 *
	 * @param entriesToAddOrRemove - if true, selects all entries on all pages. Else, unselects all entries on all pages.
	 * @param isSelected - if true, mark items as selected. Else, unselect given items.
	 */
	private void toggleEntriesSelectionStatus(final List<Object> entriesToAddOrRemove, final boolean isSelected) {
		// Set specified items as selected or not
		this.updatePagedTableSelectedEntries(entriesToAddOrRemove, isSelected);

		// Update checkbox of each item on current page
		this.updateItemSelectCheckboxes();

		// Update the "Select All On Page" and "Select All Pages" checkbox based on the selected entries
		this.updateSelectAllOnPageCheckBoxStatus();
		this.updateSelectAllEntriesCheckboxStatus();
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
	 * Unselects any selected entries on the table
	 *
	 */
	@SuppressWarnings("unchecked")
	public void clearAllSelectedEntries() {
		final List<Object> entries = new ArrayList<Object>((Collection<Object>) this.table.getValue());
		this.toggleEntriesSelectionStatus(entries, false);
	}

	public PagedBreedingManagerTable getTable() {
		return this.table;
	}

	public void setTable(final PagedBreedingManagerTable table) {
		this.table = table;
	}

	/**
	 * Update captions of "Select All on Page" and "Select All Pages" checkboxes based on the entries in the paged table. If the table had
	 * entries, the page length and the entries size will be included in the captions. Also, if there is just
	 * one page in the table, hide "Select All Pages" checkbox, else display it.
	 */
	public void updateSelectAllCheckboxes() {
		this.updateSelectAllCheckboxesCaption();
		this.replaceComponent(this.getComponent(INDEX_OF_CHECKBOXES_LAYOUT), this.generateCheckboxesLayout());;
	}

	private void updateSelectAllCheckboxesCaption() {
		final int allEntriesSize = this.table.getItemIds().size();
		final String selectAllCaption = this.messageSource.getMessage(Message.SELECT_ALL_PAGES);
		final String selectAllOnPageCaption = this.messageSource.getMessage(Message.SELECT_ALL_ON_PAGE);
		if (allEntriesSize > 0) {
			// Assumes that default caption's last character is ")"
			// appends total # of entries to caption. eg. Select All (all pages - 5000 entries)
			StringBuilder sb = new StringBuilder();
			sb.append(selectAllCaption.substring(0, selectAllCaption.length() - 1));
			sb.append(" - ");
			sb.append(allEntriesSize);
			sb.append(" entries)");
			this.selectAllEntriesCheckBox.setCaption(sb.toString());

			sb = new StringBuilder();
			// Assumes that default caption's last character is ")"
			// appends total # of entries to caption. eg. Select All (this page - 5000 entries)
			sb.append(selectAllOnPageCaption.substring(0, selectAllOnPageCaption.length() - 1));
			sb.append(" - ");
			final int pageLength = this.table.getPageLength();
			// check if the items visible is less than max possible items on board
			final int noOfEntries = Math.min(pageLength, this.table.size());
			sb.append(noOfEntries);
			sb.append(" entries)");
			this.selectAllOnPageCheckBox.setCaption(sb.toString());

		} else {
			this.selectAllEntriesCheckBox.setCaption(selectAllCaption);
			this.selectAllOnPageCheckBox.setCaption(selectAllOnPageCaption);
		}
	}

	public CheckBox getSelectAllOnPageCheckBox() {
		return this.selectAllOnPageCheckBox;
	}

	public CheckBox getSelectAllEntriesCheckBox() {
		return this.selectAllEntriesCheckBox;
	}

	public Button getUnselectAllEntriesButton() {
		return this.unselectAllEntriesBtn;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}

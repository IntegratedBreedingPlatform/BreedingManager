
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Collection;
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

		this.selectAllCheckBox.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7882379695058054587L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final boolean checkBoxValue = event.getButton().booleanValue();
				final Collection<Object> entries = (Collection<Object>) PagedTableWithSelectAllLayout.this.table.getItemIds();
				for (final Object entry : entries) {
					final CheckBox tag =
							(CheckBox) PagedTableWithSelectAllLayout.this.table.getItem(entry)
									.getItemProperty(PagedTableWithSelectAllLayout.this.checkboxColumnId).getValue();
					tag.setValue(checkBoxValue);
				}
				if (checkBoxValue) {
					PagedTableWithSelectAllLayout.this.table.setValue(entries);
				} else {
					PagedTableWithSelectAllLayout.this.table.setValue(null);
				}

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
		final Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();
		if (selectedEntries.size() == entries.size() && !selectedEntries.isEmpty()) {
			this.selectAllCheckBox.setValue(true);
		} else {
			this.selectAllCheckBox.setValue(false);
		}

		// update the loaded list of page no
		this.addLoadedPage();

		final Integer noOfEntriesPerPage = this.table.getPageLength();

		final List<Object> loadedItems = new ArrayList<Object>();
		if (entries.size() > noOfEntriesPerPage) {
			for (final Integer pageNo : this.loadedPaged) {
				final List<Object> entriesList = new ArrayList<>(entries);
				final Integer startIdx = pageNo * noOfEntriesPerPage - noOfEntriesPerPage;
				Integer endIdx = startIdx + noOfEntriesPerPage;
				endIdx = (endIdx > entries.size()) ? entries.size() : endIdx;
				loadedItems.addAll(entriesList.subList(startIdx, endIdx));
			}
		}

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

	public void addLoadedPage() {
		this.loadedPaged.add(this.table.getCurrentPage());
	}

	public void resetLoadedPage() {
		this.loadedPaged.clear();
	}

}

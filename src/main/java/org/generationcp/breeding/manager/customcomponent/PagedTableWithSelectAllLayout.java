package org.generationcp.breeding.manager.customcomponent;

import java.util.Collection;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import com.jensjansson.pagedtable.PagedTable;

public class PagedTableWithSelectAllLayout extends VerticalLayout {

	private PagedBreedingManagerTable table;
	private final Object checkboxColumnId;

	private int recordCount = 0;
	private int maxRecords = 0;

	private CheckBox selectAllCheckBox;

	public PagedTableWithSelectAllLayout(int recordCount, Object checkboxColumnId) {
		this.recordCount = this.maxRecords = recordCount;

		this.checkboxColumnId = checkboxColumnId;

		initComponents();
		initLayout();
		initActions();
	}

	public void initComponents() {
		this.table = new PagedBreedingManagerTable(recordCount,maxRecords);
		this.table.setImmediate(true);

		this.selectAllCheckBox = new CheckBox("Select All");
		this.selectAllCheckBox.setImmediate(true);
	}

	public void initLayout() {
		this.setSizeUndefined();
		this.setWidth("100%");

		this.addComponent(this.table);
		this.addComponent(((PagedTable)this.table).createControls());
		this.addComponent(this.selectAllCheckBox);

	}

	public void initActions() {
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
			public void buttonClick(Button.ClickEvent event) {
				boolean checkBoxValue = event.getButton().booleanValue();
				Collection<Object> entries = (Collection<Object>) PagedTableWithSelectAllLayout.this.table.getItemIds();
				for (Object entry : entries) {
					CheckBox tag =
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

	@SuppressWarnings("unchecked")
	public void syncItemCheckBoxes() {
		Collection<Object> entries = (Collection<Object>) this.table.getItemIds();
		Collection<Object> selectedEntries = (Collection<Object>) this.table.getValue();
		if (selectedEntries.size() == entries.size() && !selectedEntries.isEmpty()) {
			this.selectAllCheckBox.setValue(true);
		} else {
			this.selectAllCheckBox.setValue(false);
		}

		for (Object entry : entries) {
			Property itemProperty = this.table.getItem(entry).getItemProperty(this.checkboxColumnId);
			if (itemProperty != null) {
				CheckBox tag = (CheckBox) itemProperty.getValue();
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

	public void refreshTablePagingControls() {
		this.replaceComponent(this.getComponent(1),((PagedTable)this.table).createControls());
	}

}

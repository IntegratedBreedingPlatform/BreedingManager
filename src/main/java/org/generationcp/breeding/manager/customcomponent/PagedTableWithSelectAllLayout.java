package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import com.jensjansson.pagedtable.PagedTable;

public class PagedTableWithSelectAllLayout extends TableWithSelectAllLayout {

	public PagedTableWithSelectAllLayout(int recordCount, Object checkboxColumnId) {
		super(recordCount, checkboxColumnId);
	}

	@Override
	public void instantiateComponents() {
		this.table = new PagedBreedingManagerTable(this.recordCount, this.maxRecords);
		this.table.setImmediate(true);

		this.selectAllCheckBox = new CheckBox("Select All");
		this.selectAllCheckBox.setImmediate(true);

		// label is just for indenting the Select All checkbox to align with table checkboxes
		this.dummyLabel = new Label();
		this.dummyLabel.setWidth("7px");

	}

	@Override
	public void layoutComponents() {
		this.setWidth("100%");
		if (!(this.doHideEmptyTable() && this.recordCount == 0)) {
			this.addComponent(this.table);
			this.addComponent(((PagedTable)table).createControls());
		} else {
			this.addComponent(this.emptyTableLabel);
		}

		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(this.dummyLabel);
		layout.addComponent(this.selectAllCheckBox);

		this.selectAllCheckBox.addStyleName("lm-table-select-all");

		this.addComponent(layout);
	}

	public void refreshTablePagingControls() {
		this.replaceComponent(this.getComponent(1),((PagedTable)this.table).createControls());
	}

}

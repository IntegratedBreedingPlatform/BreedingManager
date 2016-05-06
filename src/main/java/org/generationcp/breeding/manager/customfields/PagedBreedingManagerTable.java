package org.generationcp.breeding.manager.customfields;

import java.util.Iterator;
import java.util.Map;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;

import com.jensjansson.pagedtable.PagedTable;

/**
 * Created by cyrus on 05/05/2016.
 */
public class PagedBreedingManagerTable extends PagedTable {
	private TableMultipleSelectionHandler tableMultipleSelectionHandler;

	public PagedBreedingManagerTable(int recordCount, int maxRecords) {
		super();

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

	@Override
	public HorizontalLayout createControls() {
		HorizontalLayout controls = super.createControls();

		controls.setMargin(new Layout.MarginInfo(true, false, false, false));

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
}

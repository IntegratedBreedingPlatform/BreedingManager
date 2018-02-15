
package org.generationcp.breeding.manager.crossingmanager.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class ParentsTableCheckboxListener implements Button.ClickListener {

	private static final long serialVersionUID = -1891133133274865175L;

	private final Table parentsTable;
	private final GermplasmListEntry entry;
	private final CheckBox tagAllBox;

	public ParentsTableCheckboxListener(Table parentsTable, GermplasmListEntry entry, CheckBox tagAllBox) {
		super();
		this.parentsTable = parentsTable;
		this.entry = entry;
		this.tagAllBox = tagAllBox;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void buttonClick(ClickEvent event) {
		boolean checkBoxValue = event.getButton().booleanValue();
		Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) this.parentsTable.getValue();
		Set<GermplasmListEntry> entriesToSelect = new HashSet<>();
		if (selectedEntries != null) {
			entriesToSelect.addAll(selectedEntries);
			if (checkBoxValue) {
				entriesToSelect.add(this.entry);
			} else {
				this.tagAllBox.setValue(false);
				entriesToSelect.remove(this.entry);
			}
		} else if (checkBoxValue) {
			entriesToSelect.add(this.entry);
		}
		this.parentsTable.setValue(entriesToSelect);
		this.parentsTable.requestRepaint();
	}

}

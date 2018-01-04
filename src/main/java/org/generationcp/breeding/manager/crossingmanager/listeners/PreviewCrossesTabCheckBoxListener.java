package org.generationcp.breeding.manager.crossingmanager.listeners;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PreviewCrossesTabCheckBoxListener implements Button.ClickListener{

	private final Table makeCrossTable;
	private final CrossParents entry;
	private final CheckBox tagAllBox;

	public PreviewCrossesTabCheckBoxListener(Table makeCrossTable, CrossParents entry, CheckBox tagAllBox) {
		super();
		this.makeCrossTable = makeCrossTable;
		this.entry = entry;
		this.tagAllBox = tagAllBox;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void buttonClick(Button.ClickEvent event) {
		boolean checkBoxValue = event.getButton().booleanValue();
		Collection<CrossParents> selectedEntries = (Collection<CrossParents>) this.makeCrossTable.getValue();
		Set<CrossParents> entriesToSelect = new HashSet<>();
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
		this.makeCrossTable.setValue(entriesToSelect);
		this.makeCrossTable.requestRepaint();
	}

}

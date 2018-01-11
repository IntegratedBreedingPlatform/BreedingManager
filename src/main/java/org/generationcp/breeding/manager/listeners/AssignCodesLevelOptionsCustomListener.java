package org.generationcp.breeding.manager.listeners;

import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodeCustomLayout;

import com.vaadin.data.Property;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;

public class AssignCodesLevelOptionsCustomListener implements Property.ValueChangeListener {
	private final OptionGroup codingLevelOptions;
	private final VerticalLayout codesLayout;
	private final AssignCodeCustomLayout assignCodeCustomLayout;
	private final VerticalLayout exampleLayout;

	public AssignCodesLevelOptionsCustomListener(final OptionGroup codingLevelOptions, final VerticalLayout codesLayout,
			final AssignCodeCustomLayout assignCodeCustomLayout, final VerticalLayout exampleLayout) {
		this.codingLevelOptions = codingLevelOptions;
		this.codesLayout = codesLayout;
		this.assignCodeCustomLayout = assignCodeCustomLayout;
		this.exampleLayout = exampleLayout;
	}

	@Override
	public void valueChange(final Property.ValueChangeEvent event) {
		//toggle codes controls panel
		this.codesLayout.removeAllComponents();
		if (this.codingLevelOptions.getValue().equals(1)) {
			this.codesLayout.addComponent(this.assignCodeCustomLayout.getCodeControlsLayoutLevel1());
		} else if (this.codingLevelOptions.getValue().equals(2)) {
			this.codesLayout.addComponent(this.assignCodeCustomLayout.getCodeControlsLayoutLevel2());
		} else if (this.codingLevelOptions.getValue().equals(3)) {
			this.codesLayout.addComponent(this.assignCodeCustomLayout.getCodeControlsLayoutLevel3());
		}
		this.codesLayout.addComponent(this.exampleLayout);
		this.assignCodeCustomLayout.updateExampleValue();
	}
}

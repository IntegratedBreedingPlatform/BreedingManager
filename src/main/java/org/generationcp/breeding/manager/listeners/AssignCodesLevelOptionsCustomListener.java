package org.generationcp.breeding.manager.listeners;

import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodeCustomLayout;

import com.vaadin.data.Property;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;

public class AssignCodesLevelOptionsCustomListener implements Property.ValueChangeListener {
	private final OptionGroup codingLevelOptions;
	private final HorizontalLayout codesLayout;
	private final AssignCodeCustomLayout assignCodeCustomLayout;

	public AssignCodesLevelOptionsCustomListener(final OptionGroup codingLevelOptions, final HorizontalLayout codesLayout,
			final AssignCodeCustomLayout assignCodeCustomLayout) {
		this.codingLevelOptions = codingLevelOptions;
		this.codesLayout = codesLayout;
		this.assignCodeCustomLayout = assignCodeCustomLayout;
	}

	@Override
	public void valueChange(Property.ValueChangeEvent event) {
		//toggle codes controls panel
		if (this.codingLevelOptions.getValue().equals(AssignCodesDialog.LEVEL1)) {
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel1().setVisible(true);
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel2().setVisible(false);
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel3().setVisible(false);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel1(), 2);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel2(), 0);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel3(), 0);
		} else if (this.codingLevelOptions.getValue().equals(AssignCodesDialog.LEVEL2)) {
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel1().setVisible(false);
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel2().setVisible(true);
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel3().setVisible(false);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel1(), 0);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel2(), 2);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel3(), 0);
		} else if (this.codingLevelOptions.getValue().equals(AssignCodesDialog.LEVEL3)) {
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel1().setVisible(false);
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel2().setVisible(false);
			this.assignCodeCustomLayout.getCodeControlsLayoutLevel3().setVisible(true);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel1(), 0);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel2(), 0);
			this.codesLayout.setExpandRatio(this.assignCodeCustomLayout.getCodeControlsLayoutLevel3(), 2);
		}
		this.assignCodeCustomLayout.updateExampleValue();
	}
}

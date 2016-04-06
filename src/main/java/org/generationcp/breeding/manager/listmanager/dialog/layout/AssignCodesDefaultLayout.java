package org.generationcp.breeding.manager.listmanager.dialog.layout;

import org.generationcp.commons.vaadin.theme.Bootstrap;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;

public class AssignCodesDefaultLayout {
	public static final String SEQUENCE_LABEL = "SEQ";
	public static final String LST_SEQUENCE_LABEL_CLASS = "lst-sequence-label";

	private TextField prefixDefault;

	public void instantiateComponents() {
		this.prefixDefault = new TextField();

		this.prefixDefault.setImmediate(true);
	}

	public HorizontalLayout constructDefaultCodeControlsLayout() {
		final HorizontalLayout codeControlsLayout = new HorizontalLayout();
		codeControlsLayout.setWidth("100%");
		codeControlsLayout.setHeight("60px");

		this.prefixDefault.setWidth(10, Sizeable.UNITS_EM);
		codeControlsLayout.addComponent(this.prefixDefault);
		codeControlsLayout.setComponentAlignment(this.prefixDefault, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel3 = new Label(SEQUENCE_LABEL);
		sequenceLabel3.setStyleName(LST_SEQUENCE_LABEL_CLASS);
		codeControlsLayout.addComponent(sequenceLabel3);
		codeControlsLayout.setComponentAlignment(sequenceLabel3, Alignment.MIDDLE_LEFT);
		return codeControlsLayout;
	}

	public TextField getPrefixDefault() {
		return this.prefixDefault;
	}

	public void setPrefixDefault(TextField prefixDefault) {
		this.prefixDefault = prefixDefault;
	}

}

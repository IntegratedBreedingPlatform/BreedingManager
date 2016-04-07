package org.generationcp.breeding.manager.listmanager.dialog.layout;

import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.commons.vaadin.theme.Bootstrap;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.StringLengthValidator;
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
	private HorizontalLayout codeControlsLayoutDefault;

	// the value we are getting from the common layout
	private Label exampleText;
	private final HorizontalLayout codesLayout;

	public AssignCodesDefaultLayout(final Label exampleText, final HorizontalLayout codesLayout) {
		this.exampleText = exampleText;
		this.codesLayout = codesLayout;
	}

	public void instantiateComponents() {
		this.prefixDefault = new TextField();
		this.prefixDefault.setImmediate(true);
		//TODO localise message
		this.prefixDefault.addValidator(new StringLengthValidator("The prefix could not exceed 50 characters", 0, 50, false));

		//update example text after setting defaults
		this.updateExampleValue();
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

	public void addListeners() {
		final Property.ValueChangeListener prefixChangeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				AssignCodesDefaultLayout.this.updateExampleValue();
			}
		};
		this.prefixDefault.addListener(prefixChangeListener);
	}

	public String getGroupNamePrefix() {
		return this.prefixDefault.getValue().toString();
	}

	public TextField getPrefixDefault() {
		return this.prefixDefault;
	}

	public void setPrefixDefault(TextField prefixDefault) {
		this.prefixDefault = prefixDefault;
	}
	public void updateExampleValue() {
		this.exampleText.setValue(this.prefixDefault.getValue() + AssignCodesDialog.SEQUENCE_PLACEHOLDER);
	}

	public void layoutComponents() {
		//TODO Implement layout for the default case
		this.codeControlsLayoutDefault = this.constructDefaultCodeControlsLayout();
		this.codesLayout.addComponent(this.codeControlsLayoutDefault);
		this.codesLayout.setComponentAlignment(this.codeControlsLayoutDefault, Alignment.MIDDLE_LEFT);
		this.codesLayout.setExpandRatio(this.codeControlsLayoutDefault, 2);
	}

	public void validate() throws Validator.InvalidValueException {
		this.prefixDefault.validate();
	}
}

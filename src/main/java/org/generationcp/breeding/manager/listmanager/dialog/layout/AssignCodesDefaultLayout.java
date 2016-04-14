package org.generationcp.breeding.manager.listmanager.dialog.layout;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class AssignCodesDefaultLayout {
	public static final String SEQUENCE_LABEL = "SEQ";
	public static final String LST_SEQUENCE_LABEL_CLASS = "lst-sequence-label";

	private TextField prefixDefault;

	// the value we are getting from the common layout
	private final Label exampleText;
	private final VerticalLayout codesLayout;
	private final SimpleResourceBundleMessageSource messageSource;

	public AssignCodesDefaultLayout(final Label exampleText, final VerticalLayout codesLayout, final SimpleResourceBundleMessageSource messageSource) {
		this.exampleText = exampleText;
		this.codesLayout = codesLayout;
		this.messageSource = messageSource;
	}

	public void instantiateComponents() {
		this.prefixDefault = new TextField();
		this.prefixDefault.setImmediate(true);
		this.prefixDefault.addValidator(
				new StringLengthValidator(this.messageSource.getMessage(Message.ERROR_PREFIX_TOO_LONG), 0, 50, false));
		this.prefixDefault.setCaption(this.messageSource.getMessage(Message.PREFIX_LABEL));

		//update example text after setting defaults
		this.updateExampleValue();
	}

	public HorizontalLayout constructDefaultCodeControlsLayout() {
		final HorizontalLayout codeControlsLayout = new HorizontalLayout();
		codeControlsLayout.setWidth("40%");
		codeControlsLayout.setHeight("60px");
		//TODO do we still need this?
		codeControlsLayout.setSpacing(false);
		codeControlsLayout.setMargin(false);

		this.prefixDefault.setWidth(10, Sizeable.UNITS_EM);
		codeControlsLayout.addComponent(this.prefixDefault);
		codeControlsLayout.setComponentAlignment(this.prefixDefault, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel3 = new Label(SEQUENCE_LABEL);
		sequenceLabel3.setStyleName(LST_SEQUENCE_LABEL_CLASS);
		codeControlsLayout.addComponent(sequenceLabel3);
		codeControlsLayout.setComponentAlignment(sequenceLabel3, Alignment.MIDDLE_LEFT);
		codeControlsLayout.addStyleName("lst-margin-left");
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

	public void updateExampleValue() {
		this.exampleText.setValue(this.prefixDefault.getValue() + AssignCodesDialog.SEQUENCE_PLACEHOLDER);
	}

	public void layoutComponents() {
		final HorizontalLayout codeControlsLayoutDefault = this.constructDefaultCodeControlsLayout();
		this.codesLayout.addComponent(codeControlsLayoutDefault);
		this.codesLayout.setComponentAlignment(codeControlsLayoutDefault, Alignment.MIDDLE_LEFT);
	}

	public void validate() throws Validator.InvalidValueException {
		this.prefixDefault.validate();
	}
}

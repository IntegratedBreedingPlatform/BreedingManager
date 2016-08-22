
package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

@Configurable
public class ListDescriptionField extends HorizontalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private Label captionLabel;
	private final String caption;
	private TextArea descriptionTextArea;
	private final boolean isMandatory;
	private Label mandatoryMark;
	private boolean changed;

	public ListDescriptionField(String caption, boolean isMandatory) {
		this.isMandatory = isMandatory;
		this.caption = caption + ": ";
		this.changed = false;
	}

	@Override
	public void instantiateComponents() {

		this.captionLabel = new Label(this.caption);
		this.captionLabel.setDebugId("captionLabel");
		this.captionLabel.addStyleName("bold");

		this.descriptionTextArea = new TextArea();
		this.descriptionTextArea.setDebugId("descriptionTextArea");
		this.descriptionTextArea.setWidth("200px");
		this.descriptionTextArea.setHeight("65px");
		this.descriptionTextArea.setImmediate(true);
		this.descriptionTextArea.addValidator(new StringLengthValidator("List Description must not exceed 255 characters.", 1, 255, false));

		if (this.isMandatory) {
			this.mandatoryMark = new MandatoryMarkLabel();
			this.mandatoryMark.setDebugId("mandatoryMark");

			this.descriptionTextArea.setRequired(true);
			this.descriptionTextArea.setRequiredError("Please specify the description of the list.");
		}
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListeners() {
		this.descriptionTextArea.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 2323698194362809907L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListDescriptionField.this.changed = true;
			}

		});
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		this.addComponent(this.captionLabel);

		if (this.isMandatory) {
			this.addComponent(this.mandatoryMark);
		}

		this.addComponent(this.descriptionTextArea);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public TextArea getDescriptionTextArea() {
		return this.descriptionTextArea;
	}

	public void setDescriptionTextArea(TextArea descriptionTextArea) {
		this.descriptionTextArea = descriptionTextArea;
	}

	public void setValue(String value) {
		this.descriptionTextArea.setValue(value);
	}

	public String getValue() {
		return (String) this.descriptionTextArea.getValue();
	}

	public void validate() {
		this.descriptionTextArea.validate();
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}

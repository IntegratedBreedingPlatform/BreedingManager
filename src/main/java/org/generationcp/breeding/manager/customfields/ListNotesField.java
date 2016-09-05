
package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

@Configurable
public class ListNotesField extends HorizontalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private Label captionLabel;
	private final String caption;
	private TextArea listNotesTextArea;
	private final boolean isMandatory;
	private Label mandatoryMark;
	private boolean changed;

	public ListNotesField(String caption, boolean isMandatory) {
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}

	@Override
	public void instantiateComponents() {
		this.captionLabel = new Label(this.caption);
		this.captionLabel.setDebugId("captionLabel");
		this.captionLabel.addStyleName("bold");

		this.listNotesTextArea = new TextArea();
		this.listNotesTextArea.setDebugId("listNotesTextArea");
		this.listNotesTextArea.setWidth("250px");
		this.listNotesTextArea.setHeight("65px");
		this.listNotesTextArea.setImmediate(true);

		if (this.isMandatory) {
			this.mandatoryMark = new MandatoryMarkLabel();
			this.mandatoryMark.setDebugId("mandatoryMark");

			this.listNotesTextArea.setRequired(true);
			this.listNotesTextArea.setRequiredError("Please specify the notes of the list.");
		}
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListeners() {
		this.listNotesTextArea.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 2323698194362809907L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListNotesField.this.changed = true;
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

		this.addComponent(this.listNotesTextArea);
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

	public TextArea getListNotesTextArea() {
		return this.listNotesTextArea;
	}

	public void setListNotesTextArea(TextArea listNotesTextArea) {
		this.listNotesTextArea = listNotesTextArea;
	}

	public void setValue(String value) {
		this.listNotesTextArea.setValue(value);
	}

	public String getValue() {
		return (String) this.listNotesTextArea.getValue();
	}

	public void validate() {
		this.listNotesTextArea.validate();
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}

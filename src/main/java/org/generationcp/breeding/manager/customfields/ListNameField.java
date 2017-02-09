
package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.ui.fields.SanitizedTextField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListNameField extends HorizontalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private Label captionLabel;
	private final String caption;
	private SanitizedTextField listNameTextField;
	private final boolean isMandatory;
	private Label mandatoryMark;
	private ListNameValidator listNameValidator;
	private boolean changed;

	public ListNameField(String caption, boolean isMandatory) {
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}

	@Override
	public void instantiateComponents() {
		this.captionLabel = new Label(this.caption);
		this.captionLabel.setDebugId("captionLabel");
		this.captionLabel.addStyleName("bold");

		this.listNameTextField = new SanitizedTextField();
		this.listNameTextField.setDebugId("listNameTextField");
		this.listNameTextField.setWidth("180px");
		this.listNameTextField.setImmediate(true);
		this.listNameTextField.addValidator(new StringLengthValidator("List Name must not exceed 50 characters.", 1, 50, false));

		this.listNameValidator = new ListNameValidator();
		this.listNameTextField.addValidator(this.listNameValidator);

		if (this.isMandatory) {
			this.mandatoryMark = new MandatoryMarkLabel();
			this.mandatoryMark.setDebugId("mandatoryMark");

			this.listNameTextField.setRequired(true);
			this.listNameTextField.setRequiredError("Please specify the name of the list.");
		}
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListeners() {
		this.listNameTextField.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 2323698194362809907L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListNameField.this.changed = true;
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

		this.addComponent(this.listNameTextField);
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

	public SanitizedTextField getListNameTextField() {
		return this.listNameTextField;
	}

	public void setListNameTextField(SanitizedTextField listNameTextField) {
		this.listNameTextField = listNameTextField;
	}

	public void setValue(String listName) {
		this.listNameTextField.setValue(listName);
	}

	public String getValue() {
		return (String) this.listNameTextField.getValue();
	}

	public ListNameValidator getListNameValidator() {
		return this.listNameValidator;
	}

	public void setListNameValidator(ListNameValidator listNameValidator) {
		this.listNameTextField.removeValidator(this.listNameValidator);
		this.listNameValidator = listNameValidator;
		this.listNameTextField.addValidator(this.listNameValidator);
	}

	public void validate() {
		this.listNameTextField.validate();
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}


package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListOwnerField extends HorizontalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private Label captionLabel;
	private final String caption;
	private Label listOwnerLabel;
	private final boolean isMandatory;
	private Label mandatoryMark;
	private ListNameValidator listNameValidator;
	private boolean changed;

	public ListOwnerField(String caption, boolean isMandatory) {
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}

	@Override
	public void instantiateComponents() {
		this.captionLabel = new Label(this.caption);
		this.captionLabel.setDebugId("captionLabel");
		this.captionLabel.addStyleName("bold");

		this.listOwnerLabel = new Label();
		this.listOwnerLabel.setDebugId("listOwnerLabel");
		this.listOwnerLabel.setWidth("180px");

		if (this.isMandatory) {
			this.mandatoryMark = new MandatoryMarkLabel();
			this.mandatoryMark.setDebugId("mandatoryMark");
		}
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {
		this.listOwnerLabel.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 2323698194362809907L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListOwnerField.this.changed = true;
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

		this.addComponent(this.listOwnerLabel);
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

	public Label getListOwnerLabel() {
		return this.listOwnerLabel;
	}

	public void setListOwnerLabel(Label listOwnerLabel) {
		this.listOwnerLabel = listOwnerLabel;
	}

	public void setValue(String listOwnerName) {
		this.listOwnerLabel.setValue(listOwnerName);
	}

	public String getValue() {
		return (String) this.listOwnerLabel.getValue();
	}

	public ListNameValidator getListNameValidator() {
		return this.listNameValidator;
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}

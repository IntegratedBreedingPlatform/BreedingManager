
package org.generationcp.breeding.manager.customfields;

import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.ui.fields.BmsDateField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListDateField extends HorizontalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private Label captionLabel;
	private String caption;
	private BmsDateField listDtDateField;
	private final boolean isMandatory;
	private Label mandatoryMark;
	private boolean changed;

	public ListDateField(String caption, boolean isMandatory) {
		this.isMandatory = isMandatory;
		if (!"".equals(caption)) {
			this.caption = caption + ": ";
		}
		this.changed = false;
	}

	@Override
	public void instantiateComponents() {
		this.captionLabel = new Label(this.caption);
		this.captionLabel.setDebugId("captionLabel");
		this.captionLabel.addStyleName("bold");

		this.listDtDateField = new BmsDateField();
		this.listDtDateField.setDebugId("listDtDateField");
		this.listDtDateField.setImmediate(true);

		if (this.isMandatory) {
			this.mandatoryMark = new MandatoryMarkLabel();
			this.mandatoryMark.setDebugId("mandatoryMark");

			this.listDtDateField.setRequired(true);
			this.listDtDateField.setRequiredError("Date must be specified in the YYYY-MM-DD format");
		}
	}

	@Override
	public void initializeValues() {
		this.listDtDateField.setValue(new Date());
	}

	@Override
	public void addListeners() {
		this.listDtDateField.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 2323698194362809907L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListDateField.this.changed = true;
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

		this.addComponent(this.listDtDateField);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public BmsDateField getListDtDateField() {
		return this.listDtDateField;
	}

	public void setListDtDateField(BmsDateField listDtDateField) {
		this.listDtDateField = listDtDateField;
	}

	public void setValue(Date date) {
		this.listDtDateField.setValue(date);
	}

	public Date getValue() {
		return (Date) this.listDtDateField.getValue();
	}

	public void validate() {
		this.listDtDateField.validate();
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}

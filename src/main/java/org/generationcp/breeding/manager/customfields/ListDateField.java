package org.generationcp.breeding.manager.customfields;

import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.ui.fields.BmsDateField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListDateField extends HorizontalLayout
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private Label captionLabel;
	private String caption;
	private BmsDateField listDtDateField ;
	private boolean isMandatory;
	private Label mandatoryMark;
	private boolean changed;
	
	public ListDateField(String caption, boolean isMandatory){
		this.isMandatory = isMandatory;
		if(!caption.equals("")) {
            this.caption = caption + ": ";
        }
		this.changed = false;
	}
	

	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		listDtDateField = new BmsDateField();
		listDtDateField.setImmediate(true);
		
		if(isMandatory){
			mandatoryMark = new MandatoryMarkLabel();
			
			listDtDateField.setRequired(true);
			listDtDateField.setRequiredError("Date must be specified in the YYYY-MM-DD format");
		}
		listDtDateField.setDebugId("vaadin-listdate-date");
	}

	@Override
	public void initializeValues() {
		listDtDateField.setValue(new Date());
	}

	@Override
	public void addListeners() {
		listDtDateField.addListener(new Property.ValueChangeListener(){
            
            private static final long serialVersionUID = 2323698194362809907L;

            public void valueChange(ValueChangeEvent event) {
                changed = true;
            }
            
        });
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		
		addComponent(captionLabel);
		
		if(isMandatory){
			addComponent(mandatoryMark);
		}
		
		addComponent(listDtDateField);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	public BmsDateField getListDtDateField() {
		return listDtDateField;
	}

	public void setListDtDateField(BmsDateField listDtDateField) {
		this.listDtDateField = listDtDateField;
	}
	
	public void setValue(Date date){
		listDtDateField.setValue(date);
	}

	public Date getValue(){
		return (Date)listDtDateField.getValue();
	}
	
	public void validate() throws InvalidValueException {
		listDtDateField.validate();
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}

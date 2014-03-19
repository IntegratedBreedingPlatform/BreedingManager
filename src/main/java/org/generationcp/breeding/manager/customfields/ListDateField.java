package org.generationcp.breeding.manager.customfields;

import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListDateField extends HorizontalLayout
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private Label captionLabel;
	private String caption;
	private BreedingManagerDateField listDtDateField ;
	private boolean isMandatory;
	private Label mandatoryMark;
	
	public ListDateField(String caption, boolean isMandatory){
		this.isMandatory = isMandatory;
		this.caption = caption + ": ";
	}

	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		listDtDateField = new BreedingManagerDateField(caption);
		listDtDateField.setImmediate(true);
		
		if(isMandatory){
			mandatoryMark = new Label("* ");
			mandatoryMark.setWidth("5px");
			mandatoryMark.addStyleName("marked_mandatory");
			
			listDtDateField.setRequired(true);
			listDtDateField.setRequiredError("Date must be specified in the YYYY-MM-DD format");
		}
	}

	@Override
	public void initializeValues() {
		listDtDateField.setValue(new Date());
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
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
	
	public BreedingManagerDateField getListDtDateField() {
		return listDtDateField;
	}

	public void setListDtDateField(BreedingManagerDateField listDtDateField) {
		this.listDtDateField = listDtDateField;
	}
	
	public void setValue(Long dateValue){
		Date date = new Date(dateValue);
		listDtDateField.setValue(date);
	}

	public Object getValue(){
		return listDtDateField.getValue();
	}
	
	public void validate() throws InvalidValueException {
		listDtDateField.validate();
	}
}

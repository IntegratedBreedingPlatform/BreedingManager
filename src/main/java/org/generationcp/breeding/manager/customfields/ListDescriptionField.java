package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

@Configurable 
public class ListDescriptionField extends HorizontalLayout
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private Label captionLabel;
	private String caption;
	private TextArea descriptionTextArea;
	private boolean isMandatory;
	private Label mandatoryMark;
	private boolean changed;
	
	public ListDescriptionField(String caption, boolean isMandatory){
		this.isMandatory = isMandatory;
		this.caption = caption + ": ";
		this.changed = false;
	}
	
	@Override
	public void instantiateComponents() {
		
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		descriptionTextArea = new TextArea();
		descriptionTextArea.setWidth("200px");
		descriptionTextArea.setHeight("35px");
		descriptionTextArea.setImmediate(true);
		descriptionTextArea.addValidator(new StringLengthValidator(
                "List Description must not exceed 255 characters.", 1, 255, false));
		
		if(isMandatory){
			mandatoryMark = new MandatoryMarkLabel();
			
			descriptionTextArea.setRequired(true);
			descriptionTextArea.setRequiredError("Please specify the description of the list.");
		}
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		descriptionTextArea.addListener(new Property.ValueChangeListener(){
            
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
		
		addComponent(descriptionTextArea);
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

	public TextArea getDescriptionTextArea() {
		return descriptionTextArea;
	}

	public void setDescriptionTextArea(TextArea descriptionTextArea) {
		this.descriptionTextArea = descriptionTextArea;
	}
	
	public void setValue(String value){
		descriptionTextArea.setValue(value);
	}
	
	public Object getValue(){
		return descriptionTextArea.getValue();
	}
	
	public void validate() throws InvalidValueException {
		descriptionTextArea.validate();
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}

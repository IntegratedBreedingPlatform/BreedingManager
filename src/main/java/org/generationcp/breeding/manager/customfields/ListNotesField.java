package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

@Configurable
public class ListNotesField extends HorizontalLayout
implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private Label captionLabel;
	private String caption;
	private TextArea listNotesTextArea;
	private boolean isMandatory;
	private Label mandatoryMark;
	private boolean changed;
	
	public ListNotesField(String caption, boolean isMandatory){
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}
	
	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		listNotesTextArea = new TextArea();
		listNotesTextArea.setWidth("255px");
		listNotesTextArea.setHeight("65px");
		listNotesTextArea.setImmediate(true);
		
		if(isMandatory){
			mandatoryMark = new Label("* ");
			mandatoryMark.setWidth("5px");
			mandatoryMark.addStyleName("marked_mandatory");
			
			listNotesTextArea.setRequired(true);
			listNotesTextArea.setRequiredError("Please specify the notes of the list.");
		}
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		listNotesTextArea.addListener(new Property.ValueChangeListener(){
            
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
		
		addComponent(listNotesTextArea);
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

	public TextArea getListNotesTextArea() {
		return listNotesTextArea;
	}

	public void setListNotesTextArea(TextArea listNotesTextArea) {
		this.listNotesTextArea = listNotesTextArea;
	}
	
	public void setValue(String value){
		listNotesTextArea.setValue(value);
	}
	
	public Object getValue(){
		return listNotesTextArea.getValue();
	}
	
	public void validate() throws InvalidValueException {
		listNotesTextArea.validate();
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}

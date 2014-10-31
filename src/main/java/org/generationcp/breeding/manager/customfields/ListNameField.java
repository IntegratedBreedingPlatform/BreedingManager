package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@Configurable
public class ListNameField extends HorizontalLayout
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private Label captionLabel;
	private String caption;
	private TextField listNameTextField;
	private boolean isMandatory;
	private Label mandatoryMark;
	private ListNameValidator listNameValidator;
	private boolean changed;
	
	public ListNameField(String caption, boolean isMandatory){
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}
	
	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		listNameTextField = new TextField();
		listNameTextField.setWidth("180px");
		listNameTextField.setImmediate(true);
		listNameTextField.addValidator(new StringLengthValidator(
                "List Name must not exceed 50 characters.", 1, 50, false));
		
		listNameValidator = new ListNameValidator();
		listNameTextField.addValidator(listNameValidator);
		
		if(isMandatory){
			mandatoryMark = new MandatoryMarkLabel();
			
			listNameTextField.setRequired(true);
			listNameTextField.setRequiredError("Please specify the name of the list.");
		}
		listNameTextField.setDebugId("vaadin-listname-txt");
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		listNameTextField.addListener(new Property.ValueChangeListener(){
            
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
		
		addComponent(listNameTextField);
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
	
	
	public TextField getListNameTextField() {
		return listNameTextField;
	}

	public void setListNameTextField(TextField listNameTextField) {
		this.listNameTextField = listNameTextField;
	}
	
	public void setValue(String listName){
		listNameTextField.setValue(listName);
	}

	public String getValue(){
		return (String)listNameTextField.getValue();
	}
	
	public ListNameValidator getListNameValidator() {
		return listNameValidator;
	}

	public void setListNameValidator(ListNameValidator listNameValidator) {
		listNameTextField.removeValidator(this.listNameValidator);
		this.listNameValidator = listNameValidator;
		listNameTextField.addValidator(this.listNameValidator);
	}

	public void validate() throws InvalidValueException {
		listNameTextField.validate();
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}



}

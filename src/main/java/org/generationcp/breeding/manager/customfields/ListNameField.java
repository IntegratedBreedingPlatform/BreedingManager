package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

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
	
	public ListNameField(String caption, boolean isMandatory){
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
	}
	
	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		listNameTextField = new TextField();
		listNameTextField.setWidth("180px");
		listNameTextField.setImmediate(true);
		listNameTextField.addValidator(new StringLengthValidator(
                "List Name must not exceed 100 characters.", 1, 100, false));
		
		listNameValidator = new ListNameValidator();
		listNameTextField.addValidator(listNameValidator);
		
		if(isMandatory){
			mandatoryMark = new Label("* ");
			mandatoryMark.setWidth("5px");
			mandatoryMark.addStyleName("marked_mandatory");
			
			listNameTextField.setRequired(true);
			listNameTextField.setRequiredError("Please specify the name of the list.");
		}
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
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

	public Object getValue(){
		return listNameTextField.getValue();
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
}

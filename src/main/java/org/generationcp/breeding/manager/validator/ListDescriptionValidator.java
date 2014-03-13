package org.generationcp.breeding.manager.validator;

import com.vaadin.data.Validator;
import com.vaadin.ui.Field;

public class ListDescriptionValidator implements Validator {
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_ERROR = "Please specify the description of the list.";
	private static final String INVALID_LENGTH = "List Description must not exceed 255 characters.";
	
	private Field listDescription;
	private String errorDetails;

	public ListDescriptionValidator(Field listDescription) {
		this.listDescription = listDescription;
	}

	@Override
	public void validate(Object value) throws InvalidValueException {
		
		if (!isValid(value)){
			throw new InvalidValueException(this.errorDetails);
		}
		
	}

	@Override
	public boolean isValid(Object value) {
		
		if(listDescription.getValue() == null || listDescription.getValue().toString().trim().length() == 0){
			this.errorDetails = DEFAULT_ERROR;
			return false;
		}
		
		if(listDescription.getValue().toString().trim().length() > 255){
			this.errorDetails = INVALID_LENGTH;
			return false;
		}
		
		return true;
	}

}

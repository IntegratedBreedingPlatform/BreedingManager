package org.generationcp.breeding.manager.validator;

import com.vaadin.data.Validator;
import com.vaadin.ui.Field;

public class ListTypeValidator implements Validator {

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_ERROR = "Please specify the type of the list.";
	private Field listType;
	private String errorDetails;
	
	public ListTypeValidator(Field listType){
		this.listType = listType;
	}
	
	@Override
	public void validate(Object value) throws InvalidValueException {
		
		if (!isValid(value)){
			throw new InvalidValueException(this.errorDetails);
		}
		
	}

	@Override
	public boolean isValid(Object value) {
		
		if(listType.getValue() == null || listType.getValue().toString().trim().length() == 0){
			this.errorDetails = DEFAULT_ERROR;
			return false;
		}
		
		return true;
	}

}

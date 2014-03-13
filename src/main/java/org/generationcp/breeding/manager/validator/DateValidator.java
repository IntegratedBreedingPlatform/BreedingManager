package org.generationcp.breeding.manager.validator;

import com.vaadin.data.Validator;
import com.vaadin.ui.DateField;

public class DateValidator implements Validator {
	private static final long serialVersionUID = 1L;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd"; 
	
	private static final String INVALID_FORMAT = "Date must be specified in the YYYY-MM-DD format";
	

	private DateField dateField;
	private String errorDetails;
	
	public DateValidator(DateField dateField){
		this.dateField = dateField;
	}
	
	@Override
	public void validate(Object value) throws InvalidValueException {
		
		if (!isValid(value)){
			throw new InvalidValueException(this.errorDetails);
		}
		
	}

	@Override
	public boolean isValid(Object value) {
		
		if(dateField.getValue() == null || dateField.getValue().toString().trim().length() == 0){
			this.errorDetails = INVALID_FORMAT;
			return false;
		}
		
		return true;
	}
	
}

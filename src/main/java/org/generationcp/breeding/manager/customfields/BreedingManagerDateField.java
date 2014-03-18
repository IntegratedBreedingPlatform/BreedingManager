package org.generationcp.breeding.manager.customfields;

import com.vaadin.ui.DateField;

public class BreedingManagerDateField extends DateField {

	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private static final long serialVersionUID = 8109945056202208596L;
	private static final String DEFAULT_LABEL = "Date";
	private static final String INVALID_FORMAT = " must be specified in the YYYY-MM-DD format";
	
	private String fieldName;
	
	public BreedingManagerDateField(String fieldName) {
		super();
		this.fieldName = fieldName;
	    initializeFormat(this.fieldName);
	}
	
	public BreedingManagerDateField(){
		super();
		initializeFormat(DEFAULT_LABEL);
	}

	private void initializeFormat(String fieldName) {
		setResolution(DateField.RESOLUTION_DAY);
        setDateFormat(DATE_FORMAT);
        setParseErrorMessage(fieldName + INVALID_FORMAT);
	}

}

package org.generationcp.breeding.manager.customfields;

import com.vaadin.ui.ComboBox;

public class BreedingManagerYearField extends ComboBox {
	private static final long serialVersionUID = 1L;

	private static final Integer RANGE_INTERVAL_FROM_BASE_YEAR = 30;  
	
	public BreedingManagerYearField(Integer year){
		super();
		
		initializeValues(year);
		setValue(year);
		
		setNullSelectionAllowed(false);
		setTextInputAllowed(false);
	}

	private void initializeValues(Integer year) {
		addItem("Year");
		
		int minYear = year - RANGE_INTERVAL_FROM_BASE_YEAR;
		int maxYear = year + RANGE_INTERVAL_FROM_BASE_YEAR;
		
		for(int i = minYear; i <= maxYear; i++){
			addItem(i);
		}
	}
}

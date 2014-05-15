package org.generationcp.breeding.manager.customfields;

import com.vaadin.ui.ComboBox;

public class BreedingManagerYearField extends ComboBox {
	
	public BreedingManagerYearField(Integer year){
		super();
		
		initializeValues(year);
		setValue(year);
		
		setNullSelectionAllowed(false);
		setTextInputAllowed(false);
	}

	private void initializeValues(Integer year) {
		addItem("Year");
		
		for(int i = (year-10); i <= (year+10); i++){
			addItem(i);
		}
	}
}

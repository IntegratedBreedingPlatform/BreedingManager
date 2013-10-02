package org.generationcp.browser.cross.study.util;

import org.generationcp.browser.cross.study.constants.EnvironmentWeight;

import com.vaadin.ui.ComboBox;

public class CrossStudyUtil {
	
	/**
	 * Creates a combobox with values of <class>EnvironmentWeight</class> enum
	 * @return
	 */
    public static ComboBox getWeightComboBox(){
    	ComboBox combo = new ComboBox();
    	combo.setNullSelectionAllowed(false);
    	combo.setTextInputAllowed(false);
    	combo.setImmediate(true);
    	
    	for (EnvironmentWeight weight : EnvironmentWeight.values()){
    		combo.addItem(weight);
    		combo.setItemCaption(weight, weight.getLabel());
    	}
			
		combo.setValue(EnvironmentWeight.IMPORTANT);
		
		combo.setEnabled(false);
		return combo;
    }

}

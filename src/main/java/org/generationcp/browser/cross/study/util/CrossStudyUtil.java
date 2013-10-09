package org.generationcp.browser.cross.study.util;

import org.generationcp.browser.cross.study.constants.EnvironmentWeight;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.constants.TraitWeight;

import com.vaadin.ui.ComboBox;

public class CrossStudyUtil {
	
	/**
	 * Creates a ComboBox with values of <class>EnvironmentWeight</class> enum
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
			
		combo.setValue(EnvironmentWeight.IGNORED);
		
		combo.setEnabled(false);
		return combo;
    }
    
    /**
	 * Creates a combobox with values of <class>NumericTraitCriteria</class> enum
	 * @return
	 */
    public static ComboBox getNumericTraitCombobox(){
    	ComboBox combo = new ComboBox();
    	combo.setNullSelectionAllowed(false);
    	combo.setTextInputAllowed(false);
    	combo.setImmediate(true);
    	
    	for (NumericTraitCriteria criteria : NumericTraitCriteria.values()){
    		combo.addItem(criteria);
    		combo.setItemCaption(criteria, criteria.getLabel());
    	}
			
		combo.setValue(NumericTraitCriteria.KEEP_ALL);
		
		combo.setEnabled(false);
		return combo;
    }

    /**
	 * Creates a ComboBox with values of <class>TraitWeight</class> enum
	 * @return
	 */
    public static ComboBox getTraitWeightsComboBox(){
    	ComboBox combo = new ComboBox();
    	combo.setNullSelectionAllowed(false);
    	combo.setTextInputAllowed(false);
    	combo.setImmediate(true);
    	
    	for (TraitWeight weight : TraitWeight.values()){
    		combo.addItem(weight);
    		combo.setItemCaption(weight, weight.getLabel());
    	}
			
    	//default value is important
		combo.setValue(TraitWeight.IMPORTANT);
		
		combo.setEnabled(false);
		return combo;
    }
}

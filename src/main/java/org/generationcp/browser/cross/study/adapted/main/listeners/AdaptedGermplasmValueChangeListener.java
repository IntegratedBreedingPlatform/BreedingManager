package org.generationcp.browser.cross.study.adapted.main.listeners;

import org.generationcp.browser.cross.study.commons.trait.filter.NumericTraitsSection;
import org.generationcp.browser.cross.study.constants.EnvironmentWeight;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class AdaptedGermplasmValueChangeListener implements ValueChangeListener {
	
	private static final long serialVersionUID = 1L;

	private Component source;
	private CheckBox checkbox;
	private Component conditionCombobox;
	private Component limitsTextField;
	private Component weightCombobox;
		   

	public AdaptedGermplasmValueChangeListener(Component source, Component conditionCombobox, 
			Component weightCombobox, Component limitsTextField) {
		super();
		this.source = source;
		this.conditionCombobox = conditionCombobox;
		this.weightCombobox = weightCombobox;
		this.limitsTextField = limitsTextField;
	}
	

	public AdaptedGermplasmValueChangeListener(Component source, CheckBox checkbox,
			Component limitsTextField, Component weightCombobox) {
		super();
		this.source = source;
		this.checkbox = checkbox;
		this.limitsTextField = limitsTextField;
		this.weightCombobox = weightCombobox;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		if (source instanceof NumericTraitsSection) {
			if (conditionCombobox != null && weightCombobox != null){
				toggleTrait((Boolean)event.getProperty().getValue(), conditionCombobox, 
						weightCombobox, limitsTextField);
				
			} else if (limitsTextField != null && this.checkbox != null){
				toggleDependentFields(event.getProperty().getValue(), this.checkbox, this.limitsTextField, weightCombobox);
			}

		} 
	}
	
	public void toggleTrait(boolean selected, Component conditionCombobox, Component weightCombobox, Component textField){
		if (conditionCombobox != null && weightCombobox != null && textField != null){
			conditionCombobox.setEnabled(selected);
						
			if (!selected){
				textField.setEnabled(false);
				weightCombobox.setEnabled(false);
			} else {
				Object value = ((ComboBox)conditionCombobox).getValue();
				toggleDependentFields( value, null, textField, weightCombobox);
			}
		}
	}
	
	
	
	public void toggleDependentFields(Object value, CheckBox checkbox, Component textfield, Component weightCombobox){
		if (value != null && value instanceof NumericTraitCriteria && textfield != null){
			NumericTraitCriteria criteria = (NumericTraitCriteria) value;
			
			boolean dropTrait = NumericTraitCriteria.DROP_TRAIT.equals(criteria);
			boolean doDisable = (NumericTraitCriteria.KEEP_ALL.equals (criteria) || dropTrait);
			textfield.setEnabled(!doDisable);
			
			toggleWeightCombobox(!dropTrait, weightCombobox);
			if (checkbox != null){
				checkbox.setEnabled(!dropTrait);
				checkbox.setValue(!dropTrait);
			}
			
		}
	}
	
	public void toggleWeightCombobox(boolean enabled, Component weightCombobox){
		weightCombobox.setEnabled(enabled);
		((ComboBox) weightCombobox).setValue(
				enabled? EnvironmentWeight.IMPORTANT : EnvironmentWeight.IGNORED);
	}

}

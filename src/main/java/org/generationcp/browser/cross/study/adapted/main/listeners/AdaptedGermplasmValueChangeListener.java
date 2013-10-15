package org.generationcp.browser.cross.study.adapted.main.listeners;

import org.generationcp.browser.cross.study.commons.trait.filter.CategoricalVariatesSection;
import org.generationcp.browser.cross.study.commons.trait.filter.NumericTraitsSection;
import org.generationcp.browser.cross.study.constants.CategoricalVariatesCondition;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.constants.TraitWeight;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class AdaptedGermplasmValueChangeListener implements ValueChangeListener {
	
	private static final long serialVersionUID = 1L;

	private Component source;
//	private CheckBox checkbox;
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
	

	public AdaptedGermplasmValueChangeListener(Component source,
			Component limitsTextField, Component weightCombobox) {
		super();
		this.source = source;
		this.limitsTextField = limitsTextField;
		this.weightCombobox = weightCombobox;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		if (source instanceof NumericTraitsSection || source instanceof CategoricalVariatesSection) {
			if (conditionCombobox != null && weightCombobox != null){
				toggleTrait((Boolean)event.getProperty().getValue(), conditionCombobox, 
						weightCombobox, limitsTextField);
			} else if (limitsTextField != null ){
				toggleDependentFields(event.getProperty().getValue(), this.limitsTextField, weightCombobox);
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
				toggleDependentFields( value, textField, weightCombobox);
			}
		}
	}
	
	
	
	public void toggleDependentFields(Object value, Component textfield, Component weightCombobox){
		if (value != null && value instanceof NumericTraitCriteria && textfield != null){
			NumericTraitCriteria criteria = (NumericTraitCriteria) value;
			
			boolean dropTrait = NumericTraitCriteria.DROP_TRAIT.equals(criteria);
			boolean doDisable = (NumericTraitCriteria.KEEP_ALL.equals (criteria) || dropTrait);
			textfield.setEnabled(!doDisable);
			
			toggleWeightCombobox(!dropTrait, weightCombobox);
			
		} else if (value != null && value instanceof CategoricalVariatesCondition && textfield != null){
			CategoricalVariatesCondition criteria = (CategoricalVariatesCondition) value;
			
			boolean dropTrait = CategoricalVariatesCondition.DROP_TRAIT.equals(criteria);
			boolean doDisable = (CategoricalVariatesCondition.KEEP_ALL.equals (criteria) || dropTrait);
			textfield.setEnabled(!doDisable);
			
			toggleWeightCombobox(!dropTrait, weightCombobox);
			
		}
	}
	
	public void toggleWeightCombobox(boolean enabled, Component weightCombobox){
		weightCombobox.setEnabled(enabled);
		((ComboBox) weightCombobox).setValue(
				enabled? TraitWeight.IMPORTANT : TraitWeight.IGNORED);
	}

}

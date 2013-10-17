package org.generationcp.browser.cross.study.adapted.main.listeners;

import org.generationcp.browser.cross.study.commons.trait.filter.CategoricalVariatesSection;
import org.generationcp.browser.cross.study.commons.trait.filter.CharacterTraitsSection;
import org.generationcp.browser.cross.study.commons.trait.filter.NumericTraitsSection;
import org.generationcp.browser.cross.study.constants.CategoricalVariatesCondition;
import org.generationcp.browser.cross.study.constants.CharacterTraitCondition;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.constants.TraitWeight;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class AdaptedGermplasmValueChangeListener implements ValueChangeListener {
	
	private static final long serialVersionUID = 1L;

	private Component source;
//	private CheckBox checkbox;
	private ComboBox conditionCombobox;
	private TextField limitsTextField;
	private ComboBox weightCombobox;
		   

	public AdaptedGermplasmValueChangeListener(Component source, ComboBox conditionCombobox, 
			ComboBox weightCombobox, TextField limitsTextField) {
		super();
		this.source = source;
		this.conditionCombobox = conditionCombobox;
		this.weightCombobox = weightCombobox;
		this.limitsTextField = limitsTextField;
	}
	

	public AdaptedGermplasmValueChangeListener(Component source,
			TextField limitsTextField, ComboBox weightCombobox) {
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
		} else if(source instanceof CharacterTraitsSection){
			if(conditionCombobox != null && limitsTextField != null){
				toggleDependentFieldsGivenTraitPriority(event.getProperty().getValue(), conditionCombobox, limitsTextField);
			} else if(limitsTextField != null && weightCombobox != null){
				toggleDependentFields(event.getProperty().getValue(), this.limitsTextField, weightCombobox);
			}
		}
	}
	
	public void toggleTrait(boolean selected, ComboBox conditionCombobox, ComboBox weightCombobox, TextField textField){
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
	
	
	
	public void toggleDependentFields(Object value, TextField textfield, ComboBox weightCombobox){
		if(value != null && textfield != null && weightCombobox != null){
			if (value instanceof NumericTraitCriteria){
				NumericTraitCriteria criteria = (NumericTraitCriteria) value;
				
				boolean dropTrait = NumericTraitCriteria.DROP_TRAIT.equals(criteria);
				boolean doDisable = (NumericTraitCriteria.KEEP_ALL.equals (criteria) || dropTrait);
				textfield.setEnabled(!doDisable);
				
				toggleWeightCombobox(!dropTrait, weightCombobox);
			} else if(value instanceof CharacterTraitCondition){
				CharacterTraitCondition condition = (CharacterTraitCondition) value;
					
				if(condition == CharacterTraitCondition.DROP_TRAIT){
					textfield.setEnabled(false);
					toggleWeightCombobox(false, weightCombobox);
				} else if (condition == CharacterTraitCondition.KEEP_ALL){
					textfield.setEnabled(false);
					toggleWeightCombobox(true, weightCombobox);
				} else {
					textfield.setEnabled(true);
					if(!weightCombobox.isEnabled()){
						toggleWeightCombobox(true, weightCombobox);
					}
				}
		    } else if (value instanceof CategoricalVariatesCondition){
  		   	    CategoricalVariatesCondition criteria = (CategoricalVariatesCondition) value;
			
			    boolean dropTrait = CategoricalVariatesCondition.DROP_TRAIT.equals(criteria);
			    boolean doDisable = (CategoricalVariatesCondition.KEEP_ALL.equals (criteria) || dropTrait);
			    textfield.setEnabled(!doDisable);
			
			    toggleWeightCombobox(!dropTrait, weightCombobox);
			}
		}
	}
	
	public void toggleWeightCombobox(boolean enabled, ComboBox weightCombobox){
		weightCombobox.setEnabled(enabled);
		weightCombobox.setValue(enabled? TraitWeight.IMPORTANT : TraitWeight.IGNORED);
	}

	public void toggleDependentFieldsGivenTraitPriority(Object value, ComboBox conditionComboBox, TextField limitsTextField){
		if(value != null && value instanceof TraitWeight){
			if(value == TraitWeight.IGNORED){
				limitsTextField.setEnabled(false);
				if(source instanceof CharacterTraitsSection){
					conditionComboBox.setValue(CharacterTraitCondition.DROP_TRAIT);
				}
			} else{
				limitsTextField.setEnabled(true);
			}
		}
	}
}

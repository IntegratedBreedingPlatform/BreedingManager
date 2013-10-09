package org.generationcp.browser.cross.study.adapted.main.listeners;

import org.generationcp.browser.cross.study.adapted.main.SetUpTraitFilter;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Component;

public class AdaptedGermplasmValueChangeListener implements ValueChangeListener {
	
	private static final long serialVersionUID = 1L;

	private Component source;
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
		if (source instanceof SetUpTraitFilter) {
			if (conditionCombobox != null && weightCombobox != null){
				((SetUpTraitFilter) source).toggleTrait((Boolean)event.getProperty().getValue(), conditionCombobox, 
						weightCombobox, limitsTextField);
				
			} else if (limitsTextField != null){
				((SetUpTraitFilter) source).toggleDependentFields(event.getProperty().getValue(), 
						limitsTextField, weightCombobox);
			}

		} 
	}

}

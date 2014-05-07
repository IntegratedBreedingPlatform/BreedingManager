package org.generationcp.browser.cross.study.traitdonors.main.listeners;

import java.util.List;

import org.generationcp.browser.cross.study.adapted.main.SetUpTraitFilter;
import org.generationcp.browser.cross.study.commons.EnvironmentFilter;
import org.generationcp.browser.cross.study.commons.trait.filter.CategoricalVariatesSection;
import org.generationcp.browser.cross.study.commons.trait.filter.CharacterTraitsSection;
import org.generationcp.browser.cross.study.commons.trait.filter.NumericTraitsSection;
import org.generationcp.browser.cross.study.traitdonors.main.PreselectTraitFilter;
import org.generationcp.browser.cross.study.traitdonors.main.SetUpTraitDonorFilter;
import org.generationcp.browser.cross.study.traitdonors.main.TraitWelcomeScreen;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

public class TraitDonorButtonClickListener implements ClickListener {
	
	private static final long serialVersionUID = 1L;

	private Component source;
	private Integer traitId;
	private String traitName;
	private String variateType;
	private List<Integer> envIds;

	public TraitDonorButtonClickListener(Component source) {
		super();
		this.source = source;
	}

	public TraitDonorButtonClickListener(Component source, Integer traitId, String traitName, String variateType, List<Integer> envIds){
		super();
		this.source = source;
		this.traitId = traitId;
		this.traitName = traitName;
		this.variateType = variateType;
		this.envIds = envIds;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Object data = event.getButton().getData();

		if (source instanceof TraitWelcomeScreen){
			TraitWelcomeScreen screen = (TraitWelcomeScreen) source;
			if (TraitWelcomeScreen.NEXT_BUTTON_ID.equals(data)){
				screen.nextButtonClickAction();
			}
		} 
		else if (source instanceof PreselectTraitFilter){
			PreselectTraitFilter screen = (PreselectTraitFilter) source;
			
			if (PreselectTraitFilter.NEXT_BUTTON_ID.equals(data)){
				screen.nextButtonClickAction();
			}
		} 		
		else if (source instanceof NumericTraitsSection){
			NumericTraitsSection screen = (NumericTraitsSection) source;
			
			if (NumericTraitsSection.TRAIT_BUTTON_ID.equals(data)){
				screen.showNumericVariateClickAction(traitId, traitName, envIds);
			}
		} 
		else if (source instanceof SetUpTraitDonorFilter){
			SetUpTraitDonorFilter screen = (SetUpTraitDonorFilter) source;
			
			if (SetUpTraitFilter.NEXT_BUTTON_ID.equals(data)){
				screen.nextButtonClickAction();
			}
			
		}
		else if (source instanceof EnvironmentFilter) {
			EnvironmentFilter screen  = (EnvironmentFilter) source;
			if(EnvironmentFilter.NEXT_BUTTON_ID.equals(data)) {
				screen.nextButtonClickAction();
			}
		}

		else if (source instanceof CharacterTraitsSection){
			CharacterTraitsSection screen = (CharacterTraitsSection) source;
			if (CharacterTraitsSection.TRAIT_BUTTON_ID.equals(data)){
				screen.showTraitObservationClickAction(traitId, variateType, traitName, envIds);
			}
		}
		else if (source instanceof CategoricalVariatesSection){
			CategoricalVariatesSection screen = (CategoricalVariatesSection) source;
			if (CategoricalVariatesSection.TRAIT_BUTTON_ID.equals(data)){
				screen.showTraitObservationClickAction(traitId, variateType, traitName, envIds);
			}
		}
	}

}

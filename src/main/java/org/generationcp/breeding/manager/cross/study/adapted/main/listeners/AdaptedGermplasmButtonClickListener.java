
package org.generationcp.breeding.manager.cross.study.adapted.main.listeners;

import java.util.List;

import org.generationcp.breeding.manager.cross.study.adapted.main.SetUpTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.WelcomeScreen;
import org.generationcp.breeding.manager.cross.study.commons.trait.filter.CategoricalVariatesSection;
import org.generationcp.breeding.manager.cross.study.commons.trait.filter.CharacterTraitsSection;
import org.generationcp.breeding.manager.cross.study.commons.trait.filter.NumericTraitsSection;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

public class AdaptedGermplasmButtonClickListener implements ClickListener {

	private static final long serialVersionUID = 1L;

	private final Component source;
	private Integer traitId;
	private String traitName;
	private String variateType;
	private List<Integer> envIds;

	public AdaptedGermplasmButtonClickListener(Component source) {
		super();
		this.source = source;
	}

	public AdaptedGermplasmButtonClickListener(Component source, Integer traitId, String traitName, String variateType, List<Integer> envIds) {
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

		if (this.source instanceof WelcomeScreen) {
			WelcomeScreen screen = (WelcomeScreen) this.source;
			if (WelcomeScreen.NEXT_BUTTON_ID.equals(data)) {
				screen.nextButtonClickAction();
			}
		} else if (this.source instanceof NumericTraitsSection) {
			NumericTraitsSection screen = (NumericTraitsSection) this.source;

			if (NumericTraitsSection.TRAIT_BUTTON_ID.equals(data)) {
				screen.showNumericVariateClickAction(this.traitId, this.traitName, this.envIds);
			}
		} else if (this.source instanceof SetUpTraitFilter) {
			SetUpTraitFilter screen = (SetUpTraitFilter) this.source;

			if (SetUpTraitFilter.NEXT_BUTTON_ID.equals(data)) {
				screen.nextButtonClickAction();
			}

		} else if (this.source instanceof CharacterTraitsSection) {
			CharacterTraitsSection screen = (CharacterTraitsSection) this.source;
			if (CharacterTraitsSection.TRAIT_BUTTON_ID.equals(data)) {
				screen.showTraitObservationClickAction(this.traitId, this.variateType, this.traitName, this.envIds);
			}
		} else if (this.source instanceof CategoricalVariatesSection) {
			CategoricalVariatesSection screen = (CategoricalVariatesSection) this.source;
			if (CategoricalVariatesSection.TRAIT_BUTTON_ID.equals(data)) {
				screen.showTraitObservationClickAction(this.traitId, this.variateType, this.traitName, this.envIds);
			}
		}
	}

}

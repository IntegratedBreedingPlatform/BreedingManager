package org.generationcp.browser.cross.study.adapted.main.listeners;

import org.generationcp.browser.cross.study.adapted.main.SetUpTraitFilter;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

public class AdaptedGermplasmButtonClickListener implements ClickListener {
	
	private static final long serialVersionUID = 1L;

	private Component source;

	public AdaptedGermplasmButtonClickListener(Component source) {
		super();
		this.source = source;
	}


	@Override
	public void buttonClick(ClickEvent event) {
		Object data = event.getButton().getData();
		if (source instanceof SetUpTraitFilter){
			SetUpTraitFilter screen = (SetUpTraitFilter) source;
			if (SetUpTraitFilter.PROCEED1_BUTTON_ID.equals(data)){
				screen.proceedButtonClickAction(0);
			}
		}

	}

}

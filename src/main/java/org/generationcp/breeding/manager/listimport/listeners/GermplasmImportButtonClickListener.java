
package org.generationcp.breeding.manager.listimport.listeners;

import org.generationcp.breeding.manager.listimport.GermplasmImportFileComponent;
import org.generationcp.breeding.manager.listimport.SelectGermplasmWindow;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class GermplasmImportButtonClickListener implements Button.ClickListener {

	private static final long serialVersionUID = 6666976205957048892L;
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmImportButtonClickListener.class);

	private final Object source;

	public GermplasmImportButtonClickListener(Object source) {
		this.source = source;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton().getData().equals(GermplasmImportFileComponent.NEXT_BUTTON_ID)
				&& this.source instanceof GermplasmImportFileComponent) {
			((GermplasmImportFileComponent) this.source).nextButtonClickAction();
		} else if (event.getButton().getData().equals(SpecifyGermplasmDetailsComponent.NEXT_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmDetailsComponent) {
			((SpecifyGermplasmDetailsComponent) this.source).nextButtonClickAction();
		} else if (event.getButton().getData().equals(SpecifyGermplasmDetailsComponent.BACK_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmDetailsComponent) {
			((SpecifyGermplasmDetailsComponent) this.source).backButtonClickAction();
		} else if (event.getButton().getData().equals(SelectGermplasmWindow.DONE_BUTTON_ID) && this.source instanceof SelectGermplasmWindow) {
			((SelectGermplasmWindow) this.source).doneAction();
		} else {
			GermplasmImportButtonClickListener.LOG
					.error("GermplasmImportButtonClickListener: Error with buttonClick action. Source not identified.");
		}
	}
}

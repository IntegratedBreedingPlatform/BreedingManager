
package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingMethodComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class CrossingManagerImportButtonClickListener implements Button.ClickListener {

	private static final long serialVersionUID = 6666976205957048892L;
	private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerImportButtonClickListener.class);

	private final Object source;

	public CrossingManagerImportButtonClickListener(final Object source) {
		this.source = source;
	}

	@Override
	public void buttonClick(final ClickEvent event) {
		final Object eventButtonData = event.getButton().getData();

		if (CrossingManagerMakeCrossesComponent.BACK_BUTTON_ID.equals(eventButtonData)
				&& this.source instanceof CrossingManagerMakeCrossesComponent) {
			((CrossingManagerMakeCrossesComponent) this.source).backButtonClickAction();

		} else if (CrossingManagerMakeCrossesComponent.NEXT_BUTTON_ID.equals(eventButtonData)
				&& this.source instanceof CrossingManagerMakeCrossesComponent) {
			((CrossingManagerMakeCrossesComponent) this.source).nextButtonClickAction();

		} else if (CrossingMethodComponent.GENERATE_CROSS_BUTTON_ID.equals(eventButtonData)
				&& this.source instanceof CrossingMethodComponent) {
			((CrossingMethodComponent) this.source).makeCrossButtonAction();

		} else {
			CrossingManagerImportButtonClickListener.LOG
					.error("CrossingManagerButtonClickListener: Error with buttonClick action. Source not identified.");
		}
	}
}

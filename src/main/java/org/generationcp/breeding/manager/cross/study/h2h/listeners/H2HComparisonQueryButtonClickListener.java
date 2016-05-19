
package org.generationcp.breeding.manager.cross.study.h2h.listeners;

import org.generationcp.breeding.manager.cross.study.h2h.EnvironmentsAvailableComponent;
import org.generationcp.breeding.manager.cross.study.h2h.SpecifyGermplasmsComponent;
import org.generationcp.breeding.manager.cross.study.h2h.TraitsAvailableComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

public class H2HComparisonQueryButtonClickListener implements Button.ClickListener {

	private static final long serialVersionUID = -3422805642974069212L;

	private static final Logger LOG = LoggerFactory.getLogger(H2HComparisonQueryButtonClickListener.class);

	private final Component source;

	public H2HComparisonQueryButtonClickListener(Component source) {
		this.source = source;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton().getData().equals(SpecifyGermplasmsComponent.SELECT_TEST_ENTRY_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).selectTestEntryButtonClickAction();
			} catch (InternationalizableException e) {
				H2HComparisonQueryButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(SpecifyGermplasmsComponent.SELECT_STANDARD_ENTRY_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).selectStandardEntryButtonClickAction();
			} catch (InternationalizableException e) {
				H2HComparisonQueryButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(SpecifyGermplasmsComponent.NEXT_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).nextButtonClickAction();
			} catch (InternationalizableException e) {
				H2HComparisonQueryButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(TraitsAvailableComponent.BACK_BUTTON_ID)
				&& this.source instanceof TraitsAvailableComponent) {
			try {
				((TraitsAvailableComponent) this.source).backButtonClickAction();
			} catch (InternationalizableException e) {
				H2HComparisonQueryButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(TraitsAvailableComponent.NEXT_BUTTON_ID)
				&& this.source instanceof TraitsAvailableComponent) {
			try {
				((TraitsAvailableComponent) this.source).nextButtonClickAction();
			} catch (InternationalizableException e) {
				H2HComparisonQueryButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(EnvironmentsAvailableComponent.BACK_BUTTON_ID)
				&& this.source instanceof EnvironmentsAvailableComponent) {
			try {
				((EnvironmentsAvailableComponent) this.source).backButtonClickAction();
			} catch (InternationalizableException e) {
				H2HComparisonQueryButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(EnvironmentsAvailableComponent.NEXT_BUTTON_ID)
				&& this.source instanceof EnvironmentsAvailableComponent) {
			try {
				((EnvironmentsAvailableComponent) this.source).nextButtonClickAction();
			} catch (InternationalizableException e) {
				H2HComparisonQueryButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else {
			H2HComparisonQueryButtonClickListener.LOG
					.error("H2HComparisonQueryButtonClickListener: Error with buttonClick action. Source not identified.");
		}
	}

}

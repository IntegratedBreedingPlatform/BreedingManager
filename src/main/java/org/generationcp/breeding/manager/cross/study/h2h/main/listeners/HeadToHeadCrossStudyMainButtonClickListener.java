
package org.generationcp.breeding.manager.cross.study.h2h.main.listeners;

import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.h2h.main.ResultsComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.TraitsAvailableComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.AddEnvironmentalConditionsDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.FilterLocationDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.FilterStudyDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmEntryDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmListDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

public class HeadToHeadCrossStudyMainButtonClickListener implements Button.ClickListener {

	private static final long serialVersionUID = -3422805642974069212L;

	private static final Logger LOG = LoggerFactory.getLogger(HeadToHeadCrossStudyMainButtonClickListener.class);

	private final Component source;
	private Integer studyId;
	private Component parentOfSource; // EnvironmentsAvailableCompoent or SpecifyAndWeighEnvironment

	public HeadToHeadCrossStudyMainButtonClickListener(Component source) {
		this.source = source;
	}

	public HeadToHeadCrossStudyMainButtonClickListener(Component source, String countryName) {
		this.source = source;
	}

	public HeadToHeadCrossStudyMainButtonClickListener(Component source, String countryName, String provinceName) {
		this.source = source;
	}

	public HeadToHeadCrossStudyMainButtonClickListener(Component source, String countryName, String provinceName, Integer studyId) {
		this.source = source;
		this.studyId = studyId;
	}

	public HeadToHeadCrossStudyMainButtonClickListener(Component source, Component parentOfSource) {
		this.source = source;
		this.parentOfSource = parentOfSource;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton().getData().equals(SpecifyGermplasmsComponent.SELECT_TEST_SEARCH_GERMPLASM_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).selectTestEntryButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(SpecifyGermplasmsComponent.SELECT_STANDARD_SEARCH_GERMPLASM_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).selectStandardEntryButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(SpecifyGermplasmsComponent.SELECT_TEST_SEARCH_GERMPLASM_LIST_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).selectTestGermplasmListButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(SpecifyGermplasmsComponent.SELECT_STANDARD_SEARCH_GERMPLASM_LIST_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).selectStandardGermplasmListButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(SpecifyGermplasmsComponent.NEXT_BUTTON_ID)
				&& this.source instanceof SpecifyGermplasmsComponent) {
			try {
				((SpecifyGermplasmsComponent) this.source).nextButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(TraitsAvailableComponent.BACK_BUTTON_ID)
				&& this.source instanceof TraitsAvailableComponent) {
			try {
				((TraitsAvailableComponent) this.source).backButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(TraitsAvailableComponent.NEXT_BUTTON_ID)
				&& this.source instanceof TraitsAvailableComponent) {
			try {
				((TraitsAvailableComponent) this.source).nextButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(ResultsComponent.EXPORT_BUTTON_ID) && this.source instanceof ResultsComponent) {
			try {
				((ResultsComponent) this.source).exportButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(ResultsComponent.BACK_BUTTON_ID) && this.source instanceof ResultsComponent) {
			try {
				((ResultsComponent) this.source).backButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (event.getButton().getData().equals(SelectGermplasmEntryDialog.ADD_BUTTON_ID)
				&& this.source instanceof SelectGermplasmEntryDialog) {
			try {
				((SelectGermplasmEntryDialog) this.source).addButtonClickAction();
			} catch (InternationalizableException e) {
				HeadToHeadCrossStudyMainButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else if (this.source instanceof SelectGermplasmEntryDialog
				&& event.getButton().getData().equals(SelectGermplasmEntryDialog.SEARCH_BUTTON_ID)) {
			((SelectGermplasmEntryDialog) this.source).searchButtonClickAction();
		} else if (this.source instanceof SelectGermplasmListDialog
				&& event.getButton().getData().equals(SelectGermplasmListDialog.ADD_BUTTON_ID)) {
			((SelectGermplasmListDialog) this.source).populateParentList();
		} else if (this.source instanceof FilterLocationDialog && event.getButton().getData().equals(FilterLocationDialog.APPLY_BUTTON_ID)) {
			// Common in Adapted Germplasm and H2H
			((FilterLocationDialog) this.source).clickApplyButton(this.getParentClass(this.parentOfSource));

		} else if (this.source instanceof FilterStudyDialog && event.getButton().getData().equals(FilterStudyDialog.APPLY_BUTTON_ID)) {

			((FilterStudyDialog) this.source).clickApplyButton(this.getParentClass(this.parentOfSource));

		} else if (this.source instanceof FilterStudyDialog && event.getButton().getData().equals(FilterStudyDialog.STUDY_BUTTON_ID)) {
			((FilterStudyDialog) this.source).showStudyInfo(this.studyId);
		} else if (this.source instanceof AddEnvironmentalConditionsDialog
				&& event.getButton().getData().equals(AddEnvironmentalConditionsDialog.APPLY_BUTTON_ID)) {

			((AddEnvironmentalConditionsDialog) this.source).clickApplyButton(this.getParentClass(this.parentOfSource));
		} else {
			HeadToHeadCrossStudyMainButtonClickListener.LOG
					.error("HeadToHeadCrossStudyMainButtonClickListener: Error with buttonClick action. Source not identified.");
		}
	}

	public String getParentClass(Component parentOfSource) {
		String parentClass = "";

		if (parentOfSource instanceof EnvironmentFilter) {
			parentClass = "EnvironmentFilter";
		}

		return parentClass;
	}

}

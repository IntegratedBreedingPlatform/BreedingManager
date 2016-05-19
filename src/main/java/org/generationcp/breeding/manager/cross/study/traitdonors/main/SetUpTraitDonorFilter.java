
package org.generationcp.breeding.manager.cross.study.traitdonors.main;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CategoricalTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CharacterTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.breeding.manager.cross.study.commons.trait.filter.CategoricalVariatesSection;
import org.generationcp.breeding.manager.cross.study.commons.trait.filter.CharacterTraitsSection;
import org.generationcp.breeding.manager.cross.study.commons.trait.filter.NumericTraitsSection;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.listeners.TraitDonorButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * Prompts the Breeder to select a range for each trait that they are interested in adaptation for the germplasm.
 *
 * Traits are split into Numeric, Character and Categorial for processing
 *
 * @author rebecca
 *
 */
@Configurable
public class SetUpTraitDonorFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	public static final String NEXT_BUTTON_ID = "SetUpTraitFilter Next Button ID";

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(SetUpTraitDonorFilter.class);

	private static final int NUM_OF_SECTIONS = 3;
	private static final Message[] tabLabels = {Message.NUMERIC_TRAITS, Message.CHARACTER_TRAIT_FILTER_TAB_TITLE,
			Message.CATEGORICAL_VARIATES};

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final TraitDonorsQueryMain mainScreen;
	private final TraitDisplayResults nextScreen;
	private CharacterTraitsSection characterSection;
	private NumericTraitsSection numericSection;
	private CategoricalVariatesSection categoricalVariatesSection;

	private TabSheet mainTabSheet;
	private Button nextButton;

	private List<EnvironmentForComparison> environmentsForComparisonList;
	private List<Integer> selectedTraits;
	private List<Integer> environmentIds;

	public SetUpTraitDonorFilter(TraitDonorsQueryMain traitDonorsQueryMain, TraitDisplayResults nextScreen) {
		this.mainScreen = traitDonorsQueryMain;
		this.nextScreen = nextScreen;
	}

	@Override
	public void updateLabels() {
		if (this.nextButton != null) {
			this.messageSource.setCaption(this.nextButton, Message.NEXT);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("550px");
		this.setWidth("1000px");
	}

	/*
	 * Creates a three tabbed panel, one tab each to contain Numeric, Character and Categorical traits. These traits have been pre-specified
	 * in the earlier PreselectTraitFiler panel.
	 */
	public void createTraitsTabs() {
		this.mainTabSheet = new TabSheet();
		this.mainTabSheet.setHeight("470px");

		for (int i = 0; i < SetUpTraitDonorFilter.NUM_OF_SECTIONS; i++) {
			VerticalLayout layout = new VerticalLayout();

			switch (i) {
				case 0:
					this.numericSection = new NumericTraitsSection(this.environmentIds, this.selectedTraits, this.getWindow());
					this.numericSection.showEmptyTraitsMessage();
					layout = this.numericSection;
					break;

				case 1:
					this.characterSection = new CharacterTraitsSection(this.environmentIds, this.selectedTraits, this.getWindow());
					layout = this.characterSection;
					break;

				case 2:
					this.categoricalVariatesSection =
							new CategoricalVariatesSection(this.environmentIds, this.selectedTraits, this.getWindow());
					layout = this.categoricalVariatesSection;
					break;

			}

			this.mainTabSheet.addTab(layout, this.messageSource.getMessage(SetUpTraitDonorFilter.tabLabels[i]));
		}

		this.mainTabSheet.addListener(new SelectedTabChangeListener() {

			private static final long serialVersionUID = -7294872922580572493L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component selected = SetUpTraitDonorFilter.this.mainTabSheet.getSelectedTab();
				Tab tab = SetUpTraitDonorFilter.this.mainTabSheet.getTab(selected);

				if (tab != null
						&& tab.getCaption().equals(SetUpTraitDonorFilter.this.messageSource.getMessage(SetUpTraitDonorFilter.tabLabels[0]))) {
					SetUpTraitDonorFilter.this.numericSection.showEmptyTraitsMessage();
				} else if (tab != null
						&& tab.getCaption().equals(SetUpTraitDonorFilter.this.messageSource.getMessage(SetUpTraitDonorFilter.tabLabels[1]))) {
					SetUpTraitDonorFilter.this.characterSection.showEmptyTraitsMessage();
				} else if (tab != null
						&& tab.getCaption().equals(SetUpTraitDonorFilter.this.messageSource.getMessage(SetUpTraitDonorFilter.tabLabels[2]))) {
					SetUpTraitDonorFilter.this.categoricalVariatesSection.showEmptyTraitsMessage();
				}
			}
		});

		this.addComponent(this.mainTabSheet, "top:20px");
	}

	public void populateTraitsTables(List<EnvironmentForComparison> environments, List<Integer> traitsList) {
		this.environmentsForComparisonList = environments;
		this.selectedTraits = traitsList;
		this.environmentIds = new ArrayList<Integer>();
		for (EnvironmentForComparison envt : environments) {
			this.environmentIds.add(envt.getEnvironmentNumber());
		}

		this.createTraitsTabs();
		this.createButtonLayout();
	}

	private void createButtonLayout() {
		this.nextButton = new Button(this.messageSource.getMessage(Message.NEXT));
		this.nextButton.setWidth("80px");
		this.nextButton.setData(SetUpTraitDonorFilter.NEXT_BUTTON_ID);
		this.nextButton.addListener(new TraitDonorButtonClickListener(this));
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addComponent(this.nextButton, "top:500px;left:460px");
		this.updateLabels();
	}

	// validate conditions before proceeding to next tab
	public void nextButtonClickAction() {
		if (this.numericSection != null) {
			if (!this.numericSection.allFieldsValid()) {
				return;
			}

			if (!this.categoricalVariatesSection.allFieldsValid()) {
				return;
			}
		}

		// each Section looks after its own details for selection
		List<NumericTraitFilter> numericFilters = this.numericSection.getFilters();
		List<CharacterTraitFilter> characterFilters = this.characterSection.getFilters();
		List<CategoricalTraitFilter> categoricalFilters = this.categoricalVariatesSection.getFilters();

		// Do not allow user to proceed if all traits dropped
		if (this.numericSection.allTraitsDropped() && this.characterSection.allTraitsDropped()
				&& this.categoricalVariatesSection.allTraitsDropped()) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.ALL_TRAITS_DROPPED_WARNING));

		} else {
			this.mainScreen.selectFourthTab();
			this.nextScreen.populateResultsTable(this.environmentsForComparisonList, numericFilters, characterFilters, categoricalFilters);
		}
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

}

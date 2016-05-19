
package org.generationcp.breeding.manager.cross.study.traitdonors.main;

import org.generationcp.breeding.manager.application.GermplasmStudyBrowserLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.commons.help.document.HelpButton;
import org.generationcp.commons.help.document.HelpModule;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * Main screen to govern and house the main Trait Donors Query accordion
 * 
 * Progression is: -- Welcome (TraitWelcomeScreen) -- Select traits of interest (PreselectTraitFilter) -- Select and weight environments
 * where the traits have been observed (EnvironmentFilter) -- Weight and range select measurements from the trait measurements
 * (SetUpTraitDonorFilter) -- Analyse results where germplasm has performed within a desired range for as many locations as possible
 * (TraitDisplayResults)
 * 
 * @author rebecca
 * 
 */
@Configurable
public class TraitDonorsQueryMain extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		GermplasmStudyBrowserLayout {

	private static final long serialVersionUID = -3488805933508882321L;
	private Accordion accordion;

	private HorizontalLayout titleLayout;

	private Label toolTitle;

	private TraitWelcomeScreen welcomeScreen;
	private PreselectTraitFilter screenOne;
	private EnvironmentFilter screenTwo;
	private SetUpTraitDonorFilter screenThree;
	private TraitDisplayResults screenFour;

	private Tab welcomeTab;
	private Tab firstTab;
	private Tab secondTab;
	private Tab thirdTab;
	private Tab fourthTab;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();

	}

	@Override
	public void instantiateComponents() {

		this.setTitleContent();

		this.accordion = new Accordion();
		this.accordion.setWidth("1000px");

		// set up the accordion panels and their cascading includes of the previous one
		this.screenFour = new TraitDisplayResults(this);
		this.screenThree = new SetUpTraitDonorFilter(this, this.screenFour);
		this.screenTwo = new EnvironmentFilter(this, this.screenThree);
		this.screenOne = new PreselectTraitFilter(this, this.screenTwo);
		this.welcomeScreen = new TraitWelcomeScreen(this, this.screenOne);

		this.welcomeTab = this.accordion.addTab(this.welcomeScreen, this.messageSource.getMessage(Message.INTRODUCTION));
		this.firstTab = this.accordion.addTab(this.screenOne, this.messageSource.getMessage(Message.SETUP_TRAIT_FILTER));
		this.secondTab = this.accordion.addTab(this.screenTwo, this.messageSource.getMessage(Message.SPECIFY_WEIGHT_ENVIRONMENT));
		this.thirdTab = this.accordion.addTab(this.screenThree, this.messageSource.getMessage(Message.SETUP_TRAIT_FILTER));
		this.fourthTab = this.accordion.addTab(this.screenFour, this.messageSource.getMessage(Message.DISPLAY_RESULTS));

		this.welcomeTab.setEnabled(true);
		this.firstTab.setEnabled(true);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {
		this.accordion.addListener(new SelectedTabChangeListener() {

			private static final long serialVersionUID = 7580598154898239027L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component selected = TraitDonorsQueryMain.this.accordion.getSelectedTab();
				Tab tab = TraitDonorsQueryMain.this.accordion.getTab(selected);

				if (tab != null && tab.equals(TraitDonorsQueryMain.this.welcomeTab)) {
					TraitDonorsQueryMain.this.welcomeTab.setEnabled(true);
					TraitDonorsQueryMain.this.firstTab.setEnabled(true);
					TraitDonorsQueryMain.this.secondTab.setEnabled(false);
				} else if (tab != null && tab.equals(TraitDonorsQueryMain.this.firstTab)) {
					TraitDonorsQueryMain.this.welcomeTab.setEnabled(true);
					TraitDonorsQueryMain.this.secondTab.setEnabled(true);
					TraitDonorsQueryMain.this.thirdTab.setEnabled(false);
					TraitDonorsQueryMain.this.fourthTab.setEnabled(false);
				} else if (tab != null && tab.equals(TraitDonorsQueryMain.this.secondTab)) {
					TraitDonorsQueryMain.this.welcomeTab.setEnabled(false);
					TraitDonorsQueryMain.this.firstTab.setEnabled(true);
					TraitDonorsQueryMain.this.thirdTab.setEnabled(true);
					TraitDonorsQueryMain.this.fourthTab.setEnabled(false);
				} else if (tab != null && tab.equals(TraitDonorsQueryMain.this.thirdTab)) {
					TraitDonorsQueryMain.this.welcomeTab.setEnabled(false);
					TraitDonorsQueryMain.this.firstTab.setEnabled(false);
					TraitDonorsQueryMain.this.thirdTab.setEnabled(true);
					TraitDonorsQueryMain.this.fourthTab.setEnabled(true);
				} else if (tab != null && tab.equals(TraitDonorsQueryMain.this.fourthTab)) {
					TraitDonorsQueryMain.this.welcomeTab.setEnabled(false);
					TraitDonorsQueryMain.this.firstTab.setEnabled(false);
					TraitDonorsQueryMain.this.thirdTab.setEnabled(true);
					TraitDonorsQueryMain.this.fourthTab.setEnabled(true);
				}
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setMargin(false, false, false, true);
		this.setSpacing(true);
		this.addComponent(this.titleLayout);
		this.addComponent(this.accordion);

	}

	private void setTitleContent() {
		this.titleLayout = new HorizontalLayout();
		this.titleLayout.setSpacing(true);

		this.toolTitle = new Label("Trait Donors Query");
		this.toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.toolTitle.setContentMode(Label.CONTENT_XHTML);
		this.toolTitle.setWidth("283px");

		this.titleLayout.addComponent(this.toolTitle);
		this.titleLayout.addComponent(new HelpButton(HelpModule.TRAIT_DONOR, "View Trait Donors Query Tutorial"));
	}

	@Override
	public void updateLabels() {
		// not implemented
	}

	// -----------
	// The following methods control tab activation
	// -----------

	public void selectFirstTabAndReset() {
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenOne);
		this.secondTab.setEnabled(true);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectWelcomeTab() {
		this.welcomeTab.setEnabled(true);
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.welcomeScreen);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectFirstTab() {
		this.welcomeTab.setEnabled(true);
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenOne);
		this.secondTab.setEnabled(true);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectSecondTab() {
		this.welcomeTab.setEnabled(false);
		this.firstTab.setEnabled(true);
		this.secondTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenTwo);
		this.thirdTab.setEnabled(true);
		this.fourthTab.setEnabled(false);
	}

	public void selectThirdTab() {
		this.welcomeTab.setEnabled(false);
		this.firstTab.setEnabled(false);
		this.secondTab.setEnabled(true);
		this.thirdTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenThree);
		this.fourthTab.setEnabled(true);
	}

	public void selectFourthTab() {
		this.welcomeTab.setEnabled(false);
		this.firstTab.setEnabled(false);
		this.secondTab.setEnabled(true);
		this.thirdTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenFour);
		this.fourthTab.setEnabled(true);
	}
}

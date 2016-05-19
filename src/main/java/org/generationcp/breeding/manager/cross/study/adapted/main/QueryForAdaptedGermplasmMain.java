
package org.generationcp.breeding.manager.cross.study.adapted.main;

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

@Configurable
public class QueryForAdaptedGermplasmMain extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		GermplasmStudyBrowserLayout {

	private static final long serialVersionUID = -3488805933508882321L;
	private Accordion accordion;

	private HorizontalLayout titleLayout;

	private Label toolTitle;

	private WelcomeScreen welcomeScreen;
	private EnvironmentFilter screenOne;
	private SetUpTraitFilter screenTwo;
	private DisplayResults screenThree;

	private Tab welcomeTab;
	private Tab firstTab;
	private Tab secondTab;
	private Tab thirdTab;

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

		this.screenThree = new DisplayResults(this);
		this.screenTwo = new SetUpTraitFilter(this, this.screenThree);
		this.screenOne = new EnvironmentFilter(this, this.screenTwo);
		this.welcomeScreen = new WelcomeScreen(this, this.screenOne);

		this.welcomeTab = this.accordion.addTab(this.welcomeScreen, this.messageSource.getMessage(Message.INTRODUCTION));
		this.firstTab = this.accordion.addTab(this.screenOne, this.messageSource.getMessage(Message.SPECIFY_WEIGHT_ENVIRONMENT));
		this.secondTab = this.accordion.addTab(this.screenTwo, this.messageSource.getMessage(Message.SETUP_TRAIT_FILTER));
		this.thirdTab = this.accordion.addTab(this.screenThree, this.messageSource.getMessage(Message.DISPLAY_RESULTS));

		this.welcomeTab.setEnabled(true);
		this.firstTab.setEnabled(true);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
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
				Component selected = QueryForAdaptedGermplasmMain.this.accordion.getSelectedTab();
				Tab tab = QueryForAdaptedGermplasmMain.this.accordion.getTab(selected);

				if (tab != null && tab.equals(QueryForAdaptedGermplasmMain.this.welcomeTab)) {
					QueryForAdaptedGermplasmMain.this.welcomeTab.setEnabled(true);
					QueryForAdaptedGermplasmMain.this.firstTab.setEnabled(true);
					QueryForAdaptedGermplasmMain.this.secondTab.setEnabled(false);
				} else if (tab != null && tab.equals(QueryForAdaptedGermplasmMain.this.firstTab)) {
					QueryForAdaptedGermplasmMain.this.welcomeTab.setEnabled(true);
					QueryForAdaptedGermplasmMain.this.secondTab.setEnabled(true);
					QueryForAdaptedGermplasmMain.this.thirdTab.setEnabled(false);
				} else if (tab != null && tab.equals(QueryForAdaptedGermplasmMain.this.secondTab)) {
					QueryForAdaptedGermplasmMain.this.welcomeTab.setEnabled(false);
					QueryForAdaptedGermplasmMain.this.firstTab.setEnabled(true);
					QueryForAdaptedGermplasmMain.this.thirdTab.setEnabled(true);
				} else if (tab != null && tab.equals(QueryForAdaptedGermplasmMain.this.thirdTab)) {
					QueryForAdaptedGermplasmMain.this.welcomeTab.setEnabled(false);
					QueryForAdaptedGermplasmMain.this.firstTab.setEnabled(false);
					QueryForAdaptedGermplasmMain.this.thirdTab.setEnabled(true);
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
		this.titleLayout.setHeight("40px");

		this.toolTitle = new Label("Adapted Germplasm Query");
		this.toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.toolTitle.setContentMode(Label.CONTENT_XHTML);
		this.toolTitle.setWidth("378px");

		this.titleLayout.addComponent(this.toolTitle);
		this.titleLayout.addComponent(new HelpButton(HelpModule.ADAPTED_GERMPLASM, "View Adapted Germplasm Query Tutorial"));
	}

	@Override
	public void updateLabels() {
		// not implemented
	}

	public void selectFirstTabAndReset() {
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenOne);
		this.secondTab.setEnabled(true);
		this.thirdTab.setEnabled(false);
	}

	public void selectWelcomeTab() {
		this.welcomeTab.setEnabled(true);
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.welcomeScreen);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
	}

	public void selectFirstTab() {
		this.welcomeTab.setEnabled(true);
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenOne);
		this.secondTab.setEnabled(true);
		this.thirdTab.setEnabled(false);
	}

	public void selectSecondTab() {
		this.welcomeTab.setEnabled(false);
		this.firstTab.setEnabled(true);
		this.secondTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenTwo);
		this.thirdTab.setEnabled(true);
	}

	public void selectThirdTab() {
		this.welcomeTab.setEnabled(false);
		this.firstTab.setEnabled(false);
		this.secondTab.setEnabled(true);
		this.thirdTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenThree);
	}
}

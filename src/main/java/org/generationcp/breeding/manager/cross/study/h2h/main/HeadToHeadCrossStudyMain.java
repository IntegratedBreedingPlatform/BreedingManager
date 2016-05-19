
package org.generationcp.breeding.manager.cross.study.h2h.main;

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
public class HeadToHeadCrossStudyMain extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		GermplasmStudyBrowserLayout {

	private static final long serialVersionUID = -3488805933508882321L;
	private Accordion accordion;

	private HorizontalLayout titleLayout;

	private Label toolTitle;

	private SpecifyGermplasmsComponent screenOne;
	private TraitsAvailableComponent screenTwo;
	private EnvironmentFilter screenThree;
	private ResultsComponent screenFour;

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

		this.screenFour = new ResultsComponent(this);
		this.screenThree = new EnvironmentFilter(this, this.screenFour);
		this.screenTwo = new TraitsAvailableComponent(this, this.screenThree);
		this.screenOne = new SpecifyGermplasmsComponent(this, this.screenTwo);

		this.firstTab = this.accordion.addTab(this.screenOne, this.messageSource.getMessage(Message.SPECIFY_ENTRIES));
		this.secondTab = this.accordion.addTab(this.screenTwo, this.messageSource.getMessage(Message.SELECT_TRAITS));
		this.thirdTab = this.accordion.addTab(this.screenThree, this.messageSource.getMessage(Message.SELECT_ENVIRONMENTS));
		this.fourthTab = this.accordion.addTab(this.screenFour, this.messageSource.getMessage(Message.DISPLAY_RESULTS));

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

			private static final long serialVersionUID = -5006519828997264724L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component selected = HeadToHeadCrossStudyMain.this.accordion.getSelectedTab();
				Tab tab = HeadToHeadCrossStudyMain.this.accordion.getTab(selected);

				if (tab != null && tab.equals(HeadToHeadCrossStudyMain.this.firstTab)) {
					HeadToHeadCrossStudyMain.this.secondTab.setEnabled(false);
					HeadToHeadCrossStudyMain.this.thirdTab.setEnabled(false);
					HeadToHeadCrossStudyMain.this.fourthTab.setEnabled(false);
				} else if (tab != null && tab.equals(HeadToHeadCrossStudyMain.this.secondTab)) {
					HeadToHeadCrossStudyMain.this.thirdTab.setEnabled(false);
					HeadToHeadCrossStudyMain.this.fourthTab.setEnabled(false);
				} else if (tab != null && tab.equals(HeadToHeadCrossStudyMain.this.thirdTab)) {

					HeadToHeadCrossStudyMain.this.fourthTab.setEnabled(false);
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

	@Override
	public void updateLabels() {
		// does nothng for now
	}

	private void setTitleContent() {
		this.titleLayout = new HorizontalLayout();
		this.titleLayout.setSpacing(true);
		this.titleLayout.setHeight("40px");

		this.toolTitle = new Label("Main Head to Head Query");
		this.toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.toolTitle.setContentMode(Label.CONTENT_XHTML);
		this.toolTitle.setWidth("360px");

		this.titleLayout.addComponent(this.toolTitle);
		this.titleLayout.addComponent(new HelpButton(HelpModule.HEAD_TO_HEAD, "View Head to Head Query Tutorial"));
	}

	public void selectFirstTabAndReset() {
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenOne);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectFirstTab() {
		this.firstTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenOne);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectSecondTab() {
		this.secondTab.setEnabled(true);
		this.firstTab.setEnabled(false);
		this.accordion.setSelectedTab(this.screenTwo);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectThirdTab() {
		this.firstTab.setEnabled(false);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenThree);
		this.fourthTab.setEnabled(false);
	}

	public void selectFourthTab() {
		this.firstTab.setEnabled(false);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenFour);
	}
}

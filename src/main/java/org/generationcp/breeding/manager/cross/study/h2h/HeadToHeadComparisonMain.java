
package org.generationcp.breeding.manager.cross.study.h2h;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
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
public class HeadToHeadComparisonMain extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -3488805933508882321L;
	private static final String VERSION = "1.0.0";

	private Accordion accordion;

	private HorizontalLayout titleLayout;

	private Label mainTitle;

	private SpecifyGermplasmsComponent screenOne;
	private TraitsAvailableComponent screenTwo;
	private EnvironmentsAvailableComponent screenThree;
	private ResultsComponent screenFour;

	private Tab firstTab;
	private Tab secondTab;
	private Tab thirdTab;
	private Tab fourthTab;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setMargin(false, false, false, true);
		this.setSpacing(true);

		this.titleLayout = new HorizontalLayout();
		this.titleLayout.setSpacing(true);
		this.setTitleContent("");
		this.addComponent(this.titleLayout);

		this.accordion = new Accordion();
		this.accordion.setWidth("1000px");

		this.screenFour = new ResultsComponent();
		this.screenThree = new EnvironmentsAvailableComponent(this, this.screenFour);
		this.screenTwo = new TraitsAvailableComponent(this, this.screenThree);
		this.screenOne = new SpecifyGermplasmsComponent(this, this.screenTwo, this.screenFour);

		this.firstTab = this.accordion.addTab(this.screenOne, "Specify the Test and Standard Entries to Compare");
		this.secondTab = this.accordion.addTab(this.screenTwo, "Review Traits Available for Comparison");
		this.thirdTab = this.accordion.addTab(this.screenThree, "Review Environments Available for Comparison");
		this.fourthTab = this.accordion.addTab(this.screenFour, "View Results");

		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);

		this.accordion.addListener(new SelectedTabChangeListener() {

			private static final long serialVersionUID = -8540598103207115009L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component selected = HeadToHeadComparisonMain.this.accordion.getSelectedTab();
				Tab tab = HeadToHeadComparisonMain.this.accordion.getTab(selected);

				if (tab != null && tab.equals(HeadToHeadComparisonMain.this.firstTab)) {
					HeadToHeadComparisonMain.this.secondTab.setEnabled(false);
					HeadToHeadComparisonMain.this.thirdTab.setEnabled(false);
					HeadToHeadComparisonMain.this.fourthTab.setEnabled(false);
				} else if (tab != null && tab.equals(HeadToHeadComparisonMain.this.secondTab)) {
					HeadToHeadComparisonMain.this.thirdTab.setEnabled(false);
					HeadToHeadComparisonMain.this.fourthTab.setEnabled(false);
				} else if (tab != null && tab.equals(HeadToHeadComparisonMain.this.thirdTab)) {
					HeadToHeadComparisonMain.this.fourthTab.setEnabled(false);
				}
			}
		});

		this.addComponent(this.accordion);
	}

	@Override
	public void updateLabels() {
	}

	private void setTitleContent(String guideMessage) {
		this.titleLayout.removeAllComponents();

		String title = "Main Head to Head Query <h2>" + HeadToHeadComparisonMain.VERSION + "</h2>";
		this.mainTitle = new Label();
		this.mainTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.mainTitle.setWidth("370px");
		this.mainTitle.setContentMode(Label.CONTENT_XHTML);
		this.mainTitle.setValue(title);
		this.titleLayout.addComponent(this.mainTitle);

		/**
		 * Label descLbl = new Label(guideMessage); descLbl.setWidth("300px");
		 * 
		 * PopupView popup = new PopupView("?",descLbl); popup.setStyleName("gcp-popup-view"); titleLayout.addComponent(popup);
		 * 
		 * titleLayout.setComponentAlignment(popup, Alignment.MIDDLE_LEFT);
		 **/
	}

	public void selectFirstTab() {
		this.accordion.setSelectedTab(this.screenOne);
		this.secondTab.setEnabled(false);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectSecondTab() {
		this.secondTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenTwo);
		this.thirdTab.setEnabled(false);
		this.fourthTab.setEnabled(false);
	}

	public void selectThirdTab() {
		this.thirdTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenThree);
		this.fourthTab.setEnabled(false);
	}

	public void selectFourthTab() {
		this.fourthTab.setEnabled(true);
		this.accordion.setSelectedTab(this.screenFour);
	}
}

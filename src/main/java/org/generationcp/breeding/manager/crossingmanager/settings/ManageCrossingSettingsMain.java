
package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainer;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainerUpdateListener;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerSummaryComponent;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.customcomponent.BreedingManagerWizardDisplay;
import org.generationcp.breeding.manager.customcomponent.BreedingManagerWizardDisplay.StepChangeListener;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.HeaderLabelLayout;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ManageCrossingSettingsMain extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout, CrossesMadeContainer {

	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_STEPS = 2;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private Label toolTitle;
	private Label makeCrossesLabel;
	private PopupView toolPopupView;
	private BreedingManagerWizardDisplay wizardDisplay;

	private CrossingSettingsDetailComponent detailComponent;
	private CrossingManagerMakeCrossesComponent makeCrossesComponent;
	private TabSheet tabSheet;

	private CrossesMade crossesMade = new CrossesMade();
	private final ComponentContainer parent;

	private final String[] wizardStepNames = new String[ManageCrossingSettingsMain.NUMBER_OF_STEPS];

	public ManageCrossingSettingsMain(ComponentContainer parent) {
		this.parent = parent;
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.toolTitle = new Label(this.messageSource.getMessage(Message.MANAGE_CROSSES));
		this.toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.toolTitle.setWidth("230px");

		this.makeCrossesLabel = new Label(this.messageSource.getMessage(Message.MAKE_CROSSES));
		this.makeCrossesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.makeCrossesLabel.addStyleName(AppConstants.CssStyles.BOLD);

		Label popupLabel = new Label(this.messageSource.getMessage(Message.CROSSING_MANAGER_TOOL_DESCRIPTION));
		popupLabel.setWidth("470px");
		this.toolPopupView = new PopupView(AppConstants.Icons.POPUP_VIEW_ICON, popupLabel);
		this.toolPopupView.addStyleName(AppConstants.CssStyles.POPUP_VIEW);

		this.instantiateWizardDisplay();

		// use tab approach to display which step to display
		this.tabSheet = new TabSheet();

		// tab names are not actually shown
		this.tabSheet.hideTabs(true);

		this.tabSheet.setHeight("1250px");
		this.tabSheet.setWidth("100%");

		this.tabSheet.addStyleName(AppConstants.CssStyles.TABSHEET_WHITE);

		this.detailComponent = new CrossingSettingsDetailComponent(this);
		this.makeCrossesComponent = new CrossingManagerMakeCrossesComponent(this);

		this.tabSheet.addTab(this.detailComponent, this.wizardStepNames[0]);
		this.tabSheet.addTab(this.makeCrossesComponent, this.wizardStepNames[1]);
	}

	private void instantiateWizardDisplay() {
		this.wizardStepNames[0] = this.messageSource.getMessage(Message.CHOOSE_SETTING);
		this.wizardStepNames[1] = this.messageSource.getMessage(Message.CREATE_CROSSES);
		this.wizardDisplay = new BreedingManagerWizardDisplay(this.wizardStepNames);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		this.setWidth("100%");
		this.setMargin(false, false, false, true);

		AbsoluteLayout headingLayout = new AbsoluteLayout();
		headingLayout.setHeight("40px");
		headingLayout.setWidth("260px");
		headingLayout.addComponent(this.toolTitle);
		headingLayout.addComponent(this.toolPopupView, "top: 13px;left:240px");

		HeaderLabelLayout subHeaderLabel = new HeaderLabelLayout(AppConstants.Icons.ICON_MANAGE_SETTINGS, this.makeCrossesLabel);

		HorizontalLayout subHeadingLayout = new HorizontalLayout();
		subHeadingLayout.setSpacing(true);
		subHeadingLayout.setWidth("600px");
		subHeadingLayout.addComponent(subHeaderLabel);
		subHeadingLayout.addComponent(this.wizardDisplay);

		this.addComponent(headingLayout);
		this.addComponent(subHeadingLayout);
		this.addComponent(this.tabSheet);
	}

	public void nextStep() {
		Component selectedStep = this.tabSheet.getSelectedTab();
		// abstract getting updates to crosses made from each wizard step
		if (selectedStep instanceof CrossesMadeContainerUpdateListener) {
			CrossesMadeContainerUpdateListener listener = (CrossesMadeContainerUpdateListener) selectedStep;
			listener.updateCrossesMadeContainer(this);
		}

		int step = this.wizardDisplay.nextStep();
		this.showNextWizardStep(step);
		this.getWindow().setScrollTop(0);
	}

	public void backStep() {
		int step = this.wizardDisplay.backStep();
		this.showNextWizardStep(step);
	}

	private void showNextWizardStep(int step) {
		Tab tab = Util.getTabAlreadyExist(this.tabSheet, this.wizardStepNames[step]);
		if (tab != null) {
			Component tabComponent = tab.getComponent();
			this.tabSheet.setSelectedTab(tabComponent);
			if (tabComponent instanceof StepChangeListener) {
				StepChangeListener listener = (StepChangeListener) tabComponent;
				listener.updatePage();
			}
		}
	}

	public void viewGermplasmListCreated(GermplasmList crossList, GermplasmList femaleList, GermplasmList maleList) {
		CrossingManagerSummaryComponent summaryComponent =
				new CrossingManagerSummaryComponent(this, crossList, femaleList, maleList,
						this.detailComponent.getCurrentlyDefinedSetting());

		this.removeComponent(this.wizardDisplay);
		this.removeComponent(this.tabSheet);

		this.addComponent(summaryComponent);
		this.getWindow().setScrollTop(0);
	}

	public void reset() {

		if (this.parent.getWindow() != null) {
			this.parent.getWindow().setScrollTop(0);
		}

		this.parent.removeAllComponents();

		ManageCrossingSettingsMain crossingManagerMain = new ManageCrossingSettingsMain(this.parent);

		// remove the redundant left margin after reloading the choose setting page
		crossingManagerMain.setMargin(false);

		this.parent.addComponent(crossingManagerMain);
	}

	// SETTER AND GETTERS
	public CrossingSettingsDetailComponent getDetailComponent() {
		return this.detailComponent;
	}

	@Override
	public CrossesMade getCrossesMade() {
		return this.crossesMade;
	}

	@Override
	public void setCrossesMade(CrossesMade crossesMade) {
		this.crossesMade = crossesMade;
	}

	public CrossingManagerMakeCrossesComponent getMakeCrossesComponent() {
		return this.makeCrossesComponent;
	}

}

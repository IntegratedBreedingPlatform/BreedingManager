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
public class ManageCrossingSettingsMain extends VerticalLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout, CrossesMadeContainer {
	
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
	
	private final String[] wizardStepNames = new String[NUMBER_OF_STEPS];
	
	public ManageCrossingSettingsMain(ComponentContainer parent) {
		this.parent = parent;
	}
	
	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		toolTitle = new Label(messageSource.getMessage(Message.MANAGE_CROSSES));
		toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		toolTitle.setWidth("230px");
		
		makeCrossesLabel = new Label(messageSource.getMessage(Message.MAKE_CROSSES));
		makeCrossesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		makeCrossesLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		Label popupLabel = new Label(messageSource.getMessage(Message.CROSSING_MANAGER_TOOL_DESCRIPTION));
		popupLabel.setWidth("470px");
		toolPopupView = new PopupView(AppConstants.Icons.POPUP_VIEW_ICON, 
				popupLabel);
		toolPopupView.addStyleName(AppConstants.CssStyles.POPUP_VIEW);
		
		instantiateWizardDisplay();
		
		// use tab approach to display which step to display
		tabSheet = new TabSheet();
		
		//tab names are not actually shown
		tabSheet.hideTabs(true); 
		
		tabSheet.setHeight("1250px");
		tabSheet.setWidth("100%");
		
		tabSheet.addStyleName(AppConstants.CssStyles.TABSHEET_WHITE);
		
		this.detailComponent = new CrossingSettingsDetailComponent(this);
		this.makeCrossesComponent  = new CrossingManagerMakeCrossesComponent(this);
		
		tabSheet.addTab(detailComponent, wizardStepNames[0]);
		tabSheet.addTab(makeCrossesComponent, wizardStepNames[1]);
	}

	private void instantiateWizardDisplay() {
		wizardStepNames[0] = messageSource.getMessage(Message.CHOOSE_SETTING);
		wizardStepNames[1] = messageSource.getMessage(Message.CREATE_CROSSES);
		wizardDisplay = new BreedingManagerWizardDisplay(wizardStepNames);
	}

	@Override
	public void initializeValues() {
		//do nothing
	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		setWidth("100%");
		setMargin(false,false,false,true);
		
		AbsoluteLayout headingLayout = new AbsoluteLayout();
		headingLayout.setHeight("40px");
		headingLayout.setWidth("260px");
		headingLayout.addComponent(toolTitle);
		headingLayout.addComponent(toolPopupView, "top: 13px;left:240px");
		
		HeaderLabelLayout subHeaderLabel = new HeaderLabelLayout(AppConstants.Icons.ICON_MANAGE_SETTINGS, makeCrossesLabel);
		
		HorizontalLayout subHeadingLayout = new HorizontalLayout();
		subHeadingLayout.setSpacing(true);
		subHeadingLayout.setWidth("600px");
		subHeadingLayout.addComponent(subHeaderLabel);
		subHeadingLayout.addComponent(wizardDisplay);
		
		addComponent(headingLayout);
		addComponent(subHeadingLayout);
		addComponent(tabSheet);
	}
	
	public void nextStep(){
		Component selectedStep = tabSheet.getSelectedTab();
		// abstract getting updates to crosses made from each wizard step
		if (selectedStep instanceof CrossesMadeContainerUpdateListener){
			CrossesMadeContainerUpdateListener listener = 
				(CrossesMadeContainerUpdateListener) selectedStep;
			listener.updateCrossesMadeContainer(this);
		}
		
		int step = wizardDisplay.nextStep();
		showNextWizardStep(step);
		getWindow().setScrollTop(0);
	}
	
	public void backStep(){
		int step = wizardDisplay.backStep();
		showNextWizardStep(step);
	}

	private void showNextWizardStep(int step) {
		Tab tab = Util.getTabAlreadyExist(tabSheet, wizardStepNames[step]);
		if (tab != null){
			Component tabComponent = tab.getComponent();
			tabSheet.setSelectedTab(tabComponent);
			if (tabComponent instanceof StepChangeListener){
				StepChangeListener listener = (StepChangeListener) tabComponent;
				listener.updatePage();
			}
		}
	}
	
    public void viewGermplasmListCreated(GermplasmList crossList, GermplasmList femaleList, GermplasmList maleList){
    	CrossingManagerSummaryComponent summaryComponent = new CrossingManagerSummaryComponent(this, crossList, 
    			femaleList, maleList, detailComponent.getCurrentlyDefinedSetting());
        
        this.removeComponent(this.wizardDisplay);
        this.removeComponent(this.tabSheet);
        
        this.addComponent(summaryComponent);
        getWindow().setScrollTop(0);
    }
    
    public void reset(){
    	
    	if(this.parent.getWindow() != null){
    		this.parent.getWindow().setScrollTop(0);
    	}
    	
        this.parent.removeAllComponents();
    	
    	ManageCrossingSettingsMain crossingManagerMain = new ManageCrossingSettingsMain(this.parent);
    	
    	//remove the redundant left margin after reloading the choose setting page
    	crossingManagerMain.setMargin(false); 
    	
    	this.parent.addComponent(crossingManagerMain);
    }
    
    //SETTER AND GETTERS
	public CrossingSettingsDetailComponent getDetailComponent() {
		return detailComponent;
	}
	
	@Override
	public CrossesMade getCrossesMade() {
		return this.crossesMade;
	}
	
	@Override
	public void setCrossesMade(CrossesMade crossesMade) {
		this.crossesMade = crossesMade;
	}
	
	public CrossingManagerMakeCrossesComponent getMakeCrossesComponent(){
		return makeCrossesComponent;
	}
	
}

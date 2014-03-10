package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainer;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainerUpdateListener;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerDetailsComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.EmbeddedGermplasmListDetailComponent;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.util.BreedingManagerWizardDisplay;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;

@Configurable
public class ManageCrossingSettingsMain extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout, CrossesMadeContainer {
	
	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_STEPS = 3;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private Label toolTitle;
	private BreedingManagerWizardDisplay wizardDisplay;
	
	private CrossingSettingsDetailComponent detailComponent;
	private TabSheet tabSheet;
	
	private CrossesMade crossesMade = new CrossesMade();
	private ComponentContainer parent;
	
	private String[] wizardStepNames = new String[NUMBER_OF_STEPS];
	
	public ManageCrossingSettingsMain(ComponentContainer parent) {
		this.parent = parent;
	}
	
	@Override
	public void updateLabels() {
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
		toolTitle = new Label(messageSource.getMessage(Message.MAKE_CROSSES));
		toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		
		instantiateWizardDisplay();
		
		// use tab approach to display which step to display
		tabSheet = new TabSheet();
		tabSheet.hideTabs(true); //tab names are not actually shown
		
		tabSheet.setHeight("900px");
		
		this.detailComponent = new CrossingSettingsDetailComponent(this);
		
		tabSheet.addTab(detailComponent, wizardStepNames[0]);
		tabSheet.addTab(new CrossingManagerMakeCrossesComponent(this), wizardStepNames[1]);
		tabSheet.addTab(new CrossingManagerDetailsComponent(this), wizardStepNames[2]);
	}

	private void instantiateWizardDisplay() {
		wizardStepNames[0] = messageSource.getMessage(Message.CHOOSE_SETTING);
		wizardStepNames[1] = messageSource.getMessage(Message.CREATE_CROSSES);
		wizardStepNames[2] = messageSource.getMessage(Message.SAVE_CROSS_LIST);
		wizardDisplay = new BreedingManagerWizardDisplay(wizardStepNames);
		wizardDisplay.setWidth("90%");
	}

	@Override
	public void initializeValues() {
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		setWidth("90%");
		setHeight("1000px");
		
		addComponent(toolTitle);
		addComponent(wizardDisplay, "top:40px");
		addComponent(tabSheet, "top:80px;");
	}

	public CrossingSettingsDetailComponent getDetailComponent() {
		return detailComponent;
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
	}
	
	public void backStep(){
		int step = wizardDisplay.backStep();
		showNextWizardStep(step);
	}

	private void showNextWizardStep(int step) {
		Tab tab = Util.getTabAlreadyExist(tabSheet, wizardStepNames[step]);
		if (tab != null){
			tabSheet.setSelectedTab(tab.getComponent());
		}
	}

	@Override
	public CrossesMade getCrossesMade() {
		return this.crossesMade;
	}
	
	@Override
	public void setCrossesMade(CrossesMade crossesMade) {
		this.crossesMade = crossesMade;
	}
	
    public void viewGermplasmListCreated(Integer listId){
        EmbeddedGermplasmListDetailComponent germplasmListBrowser = 
            new EmbeddedGermplasmListDetailComponent(this, listId);
        
        this.removeComponent(this.wizardDisplay);
        this.removeComponent(this.tabSheet);
        
        this.addComponent(germplasmListBrowser, "top:40px");
    }
    
    public void reset(){
        this.parent.replaceComponent(this, new ManageCrossingSettingsMain(this.parent));
    }

}

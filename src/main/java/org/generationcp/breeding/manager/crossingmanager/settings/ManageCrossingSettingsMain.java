package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.util.BreedingManagerWizardDisplay;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ManageCrossingSettingsMain extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_STEPS = 3;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private Label toolTitle;
	private BreedingManagerWizardDisplay wizardDisplay;
	private CrossingSettingsDetailComponent detailComponent;
	private TabSheet tabSheet;
	
	
	
	private String[] wizardStepNames = new String[NUMBER_OF_STEPS];
	
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
		tabSheet.addTab(new CrossingSettingsDetailComponent(this), wizardStepNames[0]);
		tabSheet.addTab(new CrossingManagerMakeCrossesComponent(this), wizardStepNames[1]);
		tabSheet.addTab(new VerticalLayout(), wizardStepNames[2]);
	}

	private void instantiateWizardDisplay() {
		wizardStepNames[0] = messageSource.getMessage(Message.CHOOSE_SETTING);
		wizardStepNames[1] = messageSource.getMessage(Message.CREATE_CROSSES);
		wizardStepNames[2] = messageSource.getMessage(Message.SAVE_CROSS_LIST);
		wizardDisplay = new BreedingManagerWizardDisplay(wizardStepNames);
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
		int step = wizardDisplay.nextStep();
		showNextWizardStep(step);
	}
	
	public void backStep(){
		int step = wizardDisplay.backStep();
		showNextWizardStep(step);
	}

	private void showNextWizardStep(int step) {
		Tab tab = Util.getTabAlreadyExist(tabSheet, wizardStepNames[step]);
		tabSheet.setSelectedTab(tab.getComponent());
	}

}

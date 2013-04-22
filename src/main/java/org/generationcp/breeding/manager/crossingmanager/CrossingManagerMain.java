package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.GermplasmImportFileComponent;
import org.generationcp.breeding.manager.listimport.SaveGermplasmListComponent;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingManagerMain extends VerticalLayout implements InitializingBean, InternationalizableComponent{
    
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
    private static final long serialVersionUID = -6656072296236475385L;

    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerMain.class);
    
    private final static String VERSION = "1.1.1.0";
    
    private CrossingManagerImportFileComponent wizardScreenOne;
    private CrossingManagerMakeCrossesComponent wizardScreenTwo;
    private CrossingManagerAdditionalDetailsComponent wizardScreenThree;
    private CrossingManagerDetailsComponent wizardScreenFour;
    
    private Label importToolTitle;
    private Accordion accordion;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public CrossingManagerMain(){
        
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setMargin(false);
        setSpacing(true);
        
        importToolTitle = new Label("Crossing Manager " + VERSION);
        importToolTitle.setStyleName("h1");
        addComponent(importToolTitle);
        
        accordion = new Accordion();
        accordion.setWidth("800px");
        
        wizardScreenOne = new CrossingManagerImportFileComponent(this, accordion);
        wizardScreenTwo = new CrossingManagerMakeCrossesComponent(this, accordion);
        wizardScreenThree = new CrossingManagerAdditionalDetailsComponent(this, accordion);
        wizardScreenFour = new CrossingManagerDetailsComponent(this,accordion);
        
        wizardScreenOne.setNextScreen(wizardScreenTwo);
        wizardScreenOne.setNextNextScreen(wizardScreenThree);
        
        accordion.addTab(wizardScreenOne, messageSource.getMessage(Message.SELECT_NURSERY_TEMPLATE)); //Select Nursery Template
        accordion.addTab(wizardScreenTwo, messageSource.getMessage(Message.MAKE_CROSSES)); //Make crosses
        accordion.addTab(wizardScreenThree, messageSource.getMessage(Message.ENTER_ADDITIONAL_DETAILS_OF_GERMPLASM_RECORDS_FOR_CROSSES)); //Enter additional details of germplasm records for crosses
        accordion.addTab(wizardScreenFour, messageSource.getMessage(Message.ENTER_DETAILS_FOR_LIST_OF_CROSS)); //Enter details for list of cross
        addComponent(accordion);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        
    }
    
    public CrossingManagerImportFileComponent getWizardScreenOne() {
    	return wizardScreenOne;
    }
    public CrossingManagerMakeCrossesComponent getWizardScreenTwo() {
    	return wizardScreenTwo;
    }
    public CrossingManagerAdditionalDetailsComponent getWizardScreenThree() {
    	return wizardScreenThree;
    }
    public CrossingManagerDetailsComponent getWizardScreenFour() {
    	return wizardScreenFour;
    }

    
    
}

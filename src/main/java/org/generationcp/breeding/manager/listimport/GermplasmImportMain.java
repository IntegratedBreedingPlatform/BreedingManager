package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
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
public class GermplasmImportMain extends VerticalLayout implements InitializingBean, InternationalizableComponent{
    
    private static final long serialVersionUID = -6656072296236475385L;

    private final static Logger LOG = LoggerFactory.getLogger(GermplasmImportMain.class);
    
    private final static String VERSION = "1.1.1.0";
    
    private Label importToolTitle;
    private Accordion accordion;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmImportMain(){
        
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setMargin(false);
        setSpacing(true);
        
        importToolTitle = new Label("Import Germplasm List " + VERSION);
        importToolTitle.setStyleName("h1");
        addComponent(importToolTitle);
        
        accordion = new Accordion();
        accordion.setWidth("800px");
        
        GermplasmImportFileComponent wizardScreenOne = new GermplasmImportFileComponent(accordion);
        SpecifyGermplasmDetailsComponent wizardScreenTwo = new SpecifyGermplasmDetailsComponent(accordion);
        SaveGermplasmListComponent wizardScreenThree = new SaveGermplasmListComponent(accordion);
        
        wizardScreenOne.setNextScreen(wizardScreenTwo);
        wizardScreenTwo.setNextScreen(wizardScreenThree);
        wizardScreenTwo.setPreviousScreen(wizardScreenOne);
        wizardScreenThree.setPreviousScreen(wizardScreenTwo);
        
        accordion.addTab(wizardScreenOne, messageSource.getMessage(Message.OPEN_GERMPLASM_IMPORT_FILE)); //Open Germplasm Import File
        accordion.addTab(wizardScreenTwo, messageSource.getMessage(Message.SPECIFY_GERMPLASM_DETAILS)); //Specify Germplasm Details
        accordion.addTab(wizardScreenThree, messageSource.getMessage(Message.SAVE_GERMPLASM_LIST)); //Save Germplasm List
        
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
}

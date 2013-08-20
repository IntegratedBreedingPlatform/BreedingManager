package org.generationcp.breeding.manager.listimport;

import com.vaadin.ui.ComponentContainer;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.EmbeddedGermplasmListDetailComponent;
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
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private final static Logger LOG = LoggerFactory.getLogger(GermplasmImportMain.class);
    
    private final static String VERSION = "1.1.1.0";
    
    private GermplasmImportFileComponent wizardScreenOne;
    private SpecifyGermplasmDetailsComponent wizardScreenTwo;
    private SaveGermplasmListComponent wizardScreenThree;
    
    private Label importToolTitle;
    private Accordion accordion;
    private ComponentContainer parent;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmImportMain(ComponentContainer parent){
        this.parent = parent;
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
        
        wizardScreenOne = new GermplasmImportFileComponent(this, accordion);
        wizardScreenTwo = new SpecifyGermplasmDetailsComponent(this, accordion);
        wizardScreenThree = new SaveGermplasmListComponent(this, accordion);
        
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
    
    public GermplasmImportFileComponent getWizardScreenOne() {
        return wizardScreenOne;
    }
    public SpecifyGermplasmDetailsComponent getWizardScreenTwo() {
        return wizardScreenTwo;
    }
    public SaveGermplasmListComponent getWizardScreenThree() {
        return wizardScreenThree;
    }

    public void viewGermplasmListCreated(Integer listId){
        EmbeddedGermplasmListDetailComponent germplasmListBrowser =
            new EmbeddedGermplasmListDetailComponent(this, listId);

        this.removeComponent(this.accordion);
        this.addComponent(germplasmListBrowser);
    }

    public void reset(){
        this.parent.replaceComponent(this, new GermplasmImportMain(this.parent));
    }
    
    
}

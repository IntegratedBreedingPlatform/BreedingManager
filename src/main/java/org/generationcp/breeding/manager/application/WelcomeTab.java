package org.generationcp.breeding.manager.application;

import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class WelcomeTab extends VerticalLayout implements InitializingBean, InternationalizableComponent{
    
    private static final long serialVersionUID = -355658097734163565L;

    public static final Integer IMPORT_GERMPLASM_LIST_BUTTON_ID = 1;
    
    private TabSheet tabSheet;
    private VerticalLayout rootLayoutsForOtherTabs[];
    
    private Label welcomeLabel;
    private Label questionLabel;
    
    private Button importGermplasmListButton;
    
    private VerticalLayout rootLayoutForGermplasmImport;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public WelcomeTab(TabSheet tabSheet, VerticalLayout rootLayoutsForOtherTabs[]){
        this.tabSheet = tabSheet;
        this.rootLayoutsForOtherTabs = rootLayoutsForOtherTabs;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        
        setMargin(true);
        setSpacing(true);
        
        welcomeLabel = new Label();  //Welcome to the Breeding Manager
        welcomeLabel.setStyleName("h1");
        addComponent(welcomeLabel);
        
        questionLabel = new Label(); // "<h3>What do you want to do?</h3>"
        questionLabel.setStyleName("h3");
        addComponent(questionLabel);
        
        importGermplasmListButton = new Button(); // "I want to import a Germplasm list."
        importGermplasmListButton.setWidth(400, UNITS_PIXELS);
        importGermplasmListButton.setData(IMPORT_GERMPLASM_LIST_BUTTON_ID);
        
        rootLayoutForGermplasmImport = rootLayoutsForOtherTabs[0];
        
        importGermplasmListButton.addListener(new WelcomeTabButtonClickListener(this));
        addComponent(importGermplasmListButton);
    }
    
    @Override
    public void attach() {      
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(welcomeLabel, Message.WELCOME_LABEL);
        messageSource.setCaption(questionLabel, Message.WELCOME_QUESTION_LABEL);
        messageSource.setCaption(importGermplasmListButton, Message.I_WANT_TO_IMPORT_GERMPLASM_LIST);
    }
    
    public void importGermplasmButtonClickAction() throws InternationalizableException {
        if (rootLayoutForGermplasmImport.getComponentCount() == 0) {
            rootLayoutForGermplasmImport.addComponent(new GermplasmImportMain());
            rootLayoutForGermplasmImport.addStyleName("addSpacing");
        }

        tabSheet.setSelectedTab(this.rootLayoutForGermplasmImport);
    }

}

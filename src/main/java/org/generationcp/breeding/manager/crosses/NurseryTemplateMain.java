/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.crosses;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Mark Agarrado
 *
 */
@Configurable
public class NurseryTemplateMain extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 4701041621872315948L;
    
    private final static String VERSION = "1.1.1.0";
    
    private VerticalLayout selectNurseryTemplateScreen;
    private VerticalLayout specifyNurseryConditionsScreen;
    
    private Label nurseryTemplateTitle;
    private Accordion accordion;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public NurseryTemplateMain() {
        
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        nurseryTemplateTitle = new Label("Crossing Manager " + VERSION);
        nurseryTemplateTitle.setStyleName("h1");
        addComponent(nurseryTemplateTitle);
        
        accordion = new Accordion();
        selectNurseryTemplateScreen = new NurseryTemplateImportFileComponent();
        specifyNurseryConditionsScreen = new NurseryTemplateConditionsComponent();
        
        accordion.addTab(selectNurseryTemplateScreen, messageSource.getMessage(Message.SELECT_NURSERY_TEMPLATE)); //Select Nursery Template
        accordion.addTab(specifyNurseryConditionsScreen, messageSource.getMessage(Message.SPECIFY_NURSERY_CONDITIONS_LABEL)); //Specify Nursery Conditions
        addComponent(accordion); 
    }
    
    protected void initializeValues() {
        
    }
    
    protected void initializeLayout() {
        setMargin(false);
        setSpacing(true);
        
        accordion.setWidth("800px");
    }
    
    protected void initializeActions() {
        
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }

}

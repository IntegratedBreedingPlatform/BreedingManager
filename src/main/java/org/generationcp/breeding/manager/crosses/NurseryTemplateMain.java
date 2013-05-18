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
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

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
    
    private NurseryTemplateImportFileComponent selectNurseryTemplateTab;
    private NurseryTemplateConditionsComponent specifyNurseryConditionsTab;
    
    private Label nurseryTemplateTitle;
    private Accordion accordion;
    
    private Tab wizardTabOne;
    private Tab wizardTabTwo;
    
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
        accordion.setImmediate(true);
        selectNurseryTemplateTab = new NurseryTemplateImportFileComponent(this,accordion);
        specifyNurseryConditionsTab = new NurseryTemplateConditionsComponent(this);
        
        wizardTabOne=accordion.addTab(selectNurseryTemplateTab, messageSource.getMessage(Message.SELECT_NURSERY_TEMPLATE)); //Select Nursery Template
        wizardTabTwo=accordion.addTab(specifyNurseryConditionsTab, messageSource.getMessage(Message.SPECIFY_NURSERY_CONDITIONS_LABEL)); //Specify Nursery Conditions
       
        accordion.addListener(new SelectedTabChangeListener() {
    	    @Override
    	    public void selectedTabChange(SelectedTabChangeEvent event) {
    	        Component selected =accordion.getSelectedTab();
    	        Tab tab = accordion.getTab(selected);
    	        
    	        if(tab!=null && tab.equals(wizardTabOne)){
    	           disableNurseryTemplateConditionsComponent();
                }
    	                    
    	    }
    	});
       
        disableNurseryTemplateConditionsComponent();
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

    
    public NurseryTemplateImportFileComponent getSelectNurseryTemplateScreen() {
        return selectNurseryTemplateTab;
    }

    public NurseryTemplateConditionsComponent getSpecifyNurseryConditionsScreen() {
        return specifyNurseryConditionsTab;
    }
    
    public void disableNurseryTemplateConditionsComponent(){
	wizardTabTwo.setEnabled(false);
    }
    
    public void enableNurseryTemplateConditionsComponent(){
	wizardTabTwo.setEnabled(true);
    }

}

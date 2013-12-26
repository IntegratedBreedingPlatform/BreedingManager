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
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
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
    
    private static final String VERSION = "1.1.1.0";
    private static final String STEP_1_GUIDE_MESSAGE = "This allows you to write a nursery template file with values for the conditions on the first screen. "
            + "First you need to select and upload a blank nursery template file.";
    private static final String STEP_2_GUIDE_MESSAGE = "Filling up the values on this screen is optional.  The values specified here will get written on the file" 
            + " you will get when you click on the Done button.";
    
    private NurseryTemplateImportFileComponent selectNurseryTemplateTab;
    private NurseryTemplateConditionsComponent specifyNurseryConditionsTab;
    
    private Label nurseryTemplateTitle;
    private Accordion accordion;
    private HorizontalLayout titleLayout;
    
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
        titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        setTitleContent(STEP_1_GUIDE_MESSAGE);
        addComponent(titleLayout);
        
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
                    setTitleContent(STEP_1_GUIDE_MESSAGE);
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
        setTitleContent(STEP_2_GUIDE_MESSAGE);
    wizardTabTwo.setEnabled(true);
    }

    public void setTitleContent(String guideMessage){
        titleLayout.removeAllComponents();
        
        //String title =  "<h1>Crossing Manager:</h1> <h1>Nursery Template File</h1> <h2>" + VERSION + "</h2>";
        String title =  "Crossing Manager: Nursery Template File <h2>" + VERSION + "</h2>";
        nurseryTemplateTitle = new Label();
        nurseryTemplateTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        nurseryTemplateTitle.setContentMode(Label.CONTENT_XHTML);
        nurseryTemplateTitle.setValue(title);
        titleLayout.addComponent(nurseryTemplateTitle);
        
        Label descLbl = new Label(guideMessage);
        descLbl.setWidth("300px");
        
        PopupView popup = new PopupView("?",descLbl);
        popup.setStyleName("gcp-popup-view");
        titleLayout.addComponent(popup);
        
        titleLayout.setComponentAlignment(popup, Alignment.MIDDLE_LEFT);
    }
}

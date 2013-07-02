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
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUploader;
import org.generationcp.breeding.manager.nurserytemplate.listeners.NurseryTemplateButtonClickListener;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @author Mark Agarrado
 *
 */
@Configurable
public class NurseryTemplateImportFileComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{

    /**
     * 
     */
    private static final long serialVersionUID = -7232189815380758238L;
    
    public static final String NEXT_BUTTON_ID = "NurseryTemplateImportFileComponent Next Button";
    
    private Label selectFileLabel;
    private Upload uploadComponents;
    private Button nextButton;
    private Component buttonArea;
    
    private Label filenameLabel;
    
    private CrossingManagerUploader crossingManagerUploader;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;

    private NurseryTemplateMain source;
    private Accordion accordion;
    
    public NurseryTemplateImportFileComponent(NurseryTemplateMain source, Accordion accordion) {
    this.source=source;
    this.accordion=accordion;
        
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
        selectFileLabel = new Label();
        addComponent(selectFileLabel);
        
        uploadComponents = new Upload();
        uploadComponents.setButtonCaption(messageSource.getMessage(Message.UPLOAD));
        addComponent(uploadComponents);
        
        filenameLabel = new Label();
        addComponent(filenameLabel);
        
        buttonArea = layoutButtonArea();
        addComponent(buttonArea);
    }
    
    protected void initializeValues() {
        
    }
    
    protected void initializeLayout() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();
        uploadComponents.setWidth("600px");
        setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
    }
    
    protected void initializeActions() {
        crossingManagerUploader = new CrossingManagerUploader(this, germplasmListManager);
        uploadComponents.setReceiver(crossingManagerUploader);
        uploadComponents.addListener(crossingManagerUploader);
        
        uploadComponents.addListener(new FinishedListener() {
            private static final long serialVersionUID = -1145331690007735485L;

            @Override
            public void uploadFinished(FinishedEvent event) {
                ImportedGermplasmCrosses importedGermplasmCrosses = crossingManagerUploader.getImportedGermplasmCrosses();
                nextButton.setEnabled(false);
                // display uploaded filename
                if (importedGermplasmCrosses != null) {
                    updateFilenameLabelValue(importedGermplasmCrosses.getFilename());
                    enableNextButton();
                } else {
                    updateFilenameLabelValue("");
                }

            }
        });
        nextButton.addListener(new NurseryTemplateButtonClickListener(this));
    }
    
    protected Component layoutButtonArea() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(true, false, false, false);

        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        disableNextButton();
        buttonLayout.addComponent(nextButton);
        
        return buttonLayout;
    }
    
    public void updateFilenameLabelValue(String filename){
        messageSource.setCaption(filenameLabel, Message.UPLOADED_FILE);
        filenameLabel.setCaption(filenameLabel.getCaption()+": "+filename);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(selectFileLabel, Message.SELECT_NURSERY_TEMPLATE_FILE);
        messageSource.setCaption(filenameLabel, Message.UPLOADED_FILE);
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    public CrossingManagerUploader getCrossingManagerUploader() {
        return crossingManagerUploader;
    }

    public void nextButtonClickAction() {
    
    if(crossingManagerUploader.getImportedGermplasmCrosses()==null){
        MessageNotifier.showError(getWindow(), "Error with file.", "You must upload a nursery template file before clicking on next.", Notification.POSITION_CENTERED);
    }else{
        source.enableNurseryTemplateConditionsComponent();
        this.accordion.setSelectedTab(source.getSpecifyNurseryConditionsScreen());
    }
    
    }
    
    public void disableNextButton(){
        nextButton.setEnabled(false);
    }
    
    public void enableNextButton(){
        nextButton.setEnabled(true);
    }

}
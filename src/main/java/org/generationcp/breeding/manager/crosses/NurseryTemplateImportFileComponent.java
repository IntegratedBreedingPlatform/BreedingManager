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
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

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
    
    private CrossingManagerUploader crossingManagerUploader;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    public NurseryTemplateImportFileComponent() {
        
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
        
        buttonArea = layoutButtonArea();
        addComponent(buttonArea);
    }
    
    protected void initializeValues() {
        
    }
    
    protected void initializeLayout() {
        setMargin(true);
        setSpacing(true);
        setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
    }
    
    protected void initializeActions() {
        //TODO: uncomment after tweaking/refactoring CrossingManagerUploader.java
//        crossingManagerUploader = new CrossingManagerUploader(this, germplasmListManager);
//        uploadComponents.setReceiver(crossingManagerUploader);
//        uploadComponents.addListener(crossingManagerUploader);
    }
    
    protected Component layoutButtonArea() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(true, false, false, false);

        nextButton = new Button();
        buttonLayout.addComponent(nextButton);
        return buttonLayout;
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(selectFileLabel, Message.SELECT_NURSERY_TEMPLATE_FILE);
        messageSource.setCaption(nextButton, Message.NEXT);
    }

}
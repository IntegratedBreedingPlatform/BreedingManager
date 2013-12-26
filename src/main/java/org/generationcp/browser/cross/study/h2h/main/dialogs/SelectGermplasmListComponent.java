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

package org.generationcp.browser.cross.study.h2h.main.dialogs;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class SelectGermplasmListComponent extends HorizontalLayout implements InitializingBean, InternationalizableComponent  {

    private static final long serialVersionUID = 1L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private GermplasmListTreeComponent treeComponent;
    private SelectGermplasmListInfoComponent listInfoComponent;
    private Label treeLabel;

    private HorizontalLayout mainLayout;
    
    private Integer lastOpenedListId;
    private Component source;
    
    public SelectGermplasmListComponent(Integer lastOpenedListId, Component source) {
        this.lastOpenedListId = lastOpenedListId;
        this.source = source;
    }
    
    @Override
    public void afterPropertiesSet() {
        assemble();
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        mainLayout = new HorizontalLayout();
        
        treeComponent = new GermplasmListTreeComponent(this);
        treeLabel = new Label(messageSource.getMessage(Message.PROJECT_LISTS));
        treeLabel.addStyleName("gcp-content-title");
        
        listInfoComponent = new SelectGermplasmListInfoComponent(lastOpenedListId, source);
    }
    
    protected void initializeValues() {
        
    }
    
    protected void initializeLayout() {
        //this.setSpacing(true);
        this.setMargin(true);
        
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        
        listInfoComponent.setWidth("495px");
        listInfoComponent.setHeight("420px");
        
        VerticalLayout treeLayout = new VerticalLayout();
        treeLayout.setWidth("240px");
        treeLayout.setHeight("420px");
        treeLayout.addComponent(treeLabel);
        treeLayout.addComponent(treeComponent);
        
        mainLayout.addComponent(treeLayout);
        mainLayout.addComponent(listInfoComponent);
        mainLayout.setExpandRatio(listInfoComponent, 1.0f);
        
        this.addComponent(mainLayout);
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
    }
    
    public SelectGermplasmListInfoComponent getListInfoComponent() {
        return listInfoComponent;
    }
}

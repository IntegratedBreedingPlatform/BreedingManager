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

package org.generationcp.browser.germplasmlist;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class GermplasmListDetailComponent extends GridLayout implements InitializingBean, InternationalizableComponent {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListDetailComponent.class);
    private static final long serialVersionUID = 1738426765643928293L;

    private Label lblName;
    private Label lblDescription;
    private Label lblCreationDate;
    private Label lblType;
    private Label lblStatus;
    
    private Label listName;
    private Label listDescription;
    private Label listCreationDate;
    private Label listType;
    private Label listStatus;
    
    private GermplasmListManager germplasmListManager;
    private int germplasmListId;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmListDetailComponent(GermplasmListManager germplasmListManager, int germplasmListId){
    	this.germplasmListManager = germplasmListManager;
    	this.germplasmListId = germplasmListId;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception{
        setRows(6);
        setColumns(3);
        setSpacing(true);
        setMargin(true);

        lblName = new Label(messageSource.getMessage(Message.NAME_LABEL)); // "Name"
        lblDescription = new Label(messageSource.getMessage(Message.DESCRIPTION_LABEL)); // "Description"
        lblCreationDate = new Label(messageSource.getMessage(Message.CREATION_DATE_LABEL)); // "Creation Date"
        lblType = new Label(messageSource.getMessage(Message.TYPE_LABEL)); // "Type"
        lblStatus = new Label(messageSource.getMessage(Message.STATUS_LABEL)); // "Status"
        
        // get GermplasmList Detail
        GermplasmList germplasmList;
        germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);

        listName = new Label(germplasmList.getName());
        listDescription = new Label(germplasmList.getDescription());
        listCreationDate = new Label(String.valueOf(germplasmList.getDate()));
        listType = new Label(germplasmList.getType());
        listStatus = new Label(germplasmList.getStatusString());
		
        addComponent(lblName, 1, 1);
        addComponent(lblDescription, 1, 2);
        addComponent(lblCreationDate, 1, 3);
        addComponent(lblType, 1, 4);
        addComponent(lblStatus, 1, 5);
        
        addComponent(listName, 2, 1);
        addComponent(listDescription, 2, 2);
        addComponent(listCreationDate, 2, 3);
        addComponent(listType, 2, 4);
        addComponent(listStatus, 2, 5);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        /*messageSource.setCaption(lblName, Message.name_label);
        messageSource.setCaption(lblTitle, Message.title_label);
        messageSource.setCaption(lblObjective, Message.objective_label);
        messageSource.setCaption(lblType, Message.type_label);
        messageSource.setCaption(lblStartDate, Message.start_date_label);
        messageSource.setCaption(lblEndDate, Message.end_date_label);*/
    }

}

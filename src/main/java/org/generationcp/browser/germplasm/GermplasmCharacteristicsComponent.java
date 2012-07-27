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

package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class GermplasmCharacteristicsComponent extends GridLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    private Label lblGID;
    private Label lblPrefName;
    private Label lblLocation;
    private Label lblGermplasmMethod;
    private Label lblCreationDate;
    private Label lblReference;

    private Label gid;
    private Label prefName;
    private Label location;
    private Label germplasmMethod;
    private Label creationDate;
    private Label reference;

    private GermplasmDetailModel gDetailModel;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmCharacteristicsComponent(GermplasmDetailModel gDetailModel) {
    	this.gDetailModel = gDetailModel;
    }
    
    @Override
    public void afterPropertiesSet() {
        setRows(7);
        setColumns(3);
        setSpacing(true);
        setMargin(true);

        lblGID = new Label(messageSource.getMessage(Message.gid_label)); // "Name"
        lblPrefName = new Label(messageSource.getMessage(Message.prefname_label)); // "Title"
        lblLocation = new Label(messageSource.getMessage(Message.location_label)); // "Objective"
        lblGermplasmMethod = new Label(messageSource.getMessage(Message.method_label)); // "Type"
        lblCreationDate = new Label(messageSource.getMessage(Message.creation_date_label)); // "Start Date"
        lblReference = new Label(messageSource.getMessage(Message.gid_label)); // "End Date"

        gid = new Label(String.valueOf(gDetailModel.getGid()));
        prefName = new Label(gDetailModel.getGermplasmPreferredName());
        location = new Label( gDetailModel.getGermplasmLocation());
        germplasmMethod = new Label(gDetailModel.getGermplasmMethod());
        creationDate = new Label(String.valueOf(gDetailModel.getGermplasmCreationDate()));
        reference = new Label(String.valueOf( gDetailModel.getReference()));

        addComponent(lblGID, 1, 1);
        addComponent(lblPrefName, 1, 2);
        addComponent(lblLocation, 1, 3);
        addComponent(lblGermplasmMethod, 1, 4);
        addComponent(lblCreationDate, 1, 5);
        addComponent(lblReference, 1, 6);
        
        addComponent(gid, 2, 1);
        addComponent(prefName, 2, 2);
        addComponent(location, 2, 3);
        addComponent(germplasmMethod, 2, 4);
        addComponent(creationDate, 2, 5);
        addComponent(reference, 2, 6);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
/*        messageSource.setCaption(lblGID, Message.gid_label);
        messageSource.setCaption(lblPrefName, Message.prefname_label);
        messageSource.setCaption(lblLocation, Message.location_label);
        messageSource.setCaption(lblGermplasmMethod, Message.method_label);
        messageSource.setCaption(lblCreationDate, Message.creation_date_label);
        messageSource.setCaption(lblReference, Message.reference_label);*/
    }

}

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

import org.apache.commons.lang3.StringUtils;
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

    private GermplasmDetailModel gDetailModel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmCharacteristicsComponent(GermplasmDetailModel gDetailModel) {
        this.gDetailModel = gDetailModel;
    }
    
    @Override
    public void afterPropertiesSet() {
        setRows(2);
        setColumns(3);
        setSpacing(true);
        setWidth("98%");
        
        lblPrefName = new Label("<b>" + messageSource.getMessage(Message.PREFNAME_LABEL) + ":</b> " + gDetailModel.getGermplasmPreferredName()); // "Preferred Name"
        lblPrefName.setContentMode(Label.CONTENT_XHTML);
        lblGermplasmMethod = new Label("<b>" + messageSource.getMessage(Message.CREATION_METHOD_LABEL) + ":</b> " + gDetailModel.getGermplasmMethod()); // "Creation Method"
        lblGermplasmMethod.setContentMode(Label.CONTENT_XHTML);
        lblGermplasmMethod.setDescription(gDetailModel.getGermplasmMethod());
        lblCreationDate = new Label("<b>" + messageSource.getMessage(Message.CREATION_DATE_LABEL) + ":</b> " + String.valueOf(gDetailModel.getGermplasmCreationDate())); // "Creation Date"
        lblCreationDate.setContentMode(Label.CONTENT_XHTML);
        String locationName = gDetailModel.getGermplasmLocation();
        if(locationName != null && locationName.length() > 40){
        	locationName = locationName.substring(0, 40) + "...";
        } else if (StringUtils.isEmpty(locationName)){
        	locationName = "-";
        }
        lblLocation = new Label("<b>" + messageSource.getMessage(Message.LOCATION_LABEL) + ":</b> " + locationName); // "Location"
        lblLocation.setContentMode(Label.CONTENT_XHTML);
        lblLocation.setDescription(gDetailModel.getGermplasmLocation());
        lblGID = new Label("<b>" + messageSource.getMessage(Message.GID_LABEL) + ":</b> " + String.valueOf(gDetailModel.getGid())); // "GID"
        lblGID.setContentMode(Label.CONTENT_XHTML);
        String reference = "-";
        String referenceFullString = null;
        if(gDetailModel.getReference() != null){
        	referenceFullString = String.valueOf(gDetailModel.getReference());
        	if(referenceFullString.length() > 40){
        		reference = referenceFullString.substring(0, 40) + "...";
        	} else{
        		reference = referenceFullString;
        	}
        }
        lblReference = new Label("<b>" + messageSource.getMessage(Message.REFERENCE_LABEL) + ":</b> " + reference); // "Reference"
        lblReference.setContentMode(Label.CONTENT_XHTML);
        lblReference.setDescription(referenceFullString);
        
        addComponent(lblPrefName, 0, 0);
        addComponent(lblGermplasmMethod, 0, 1);
        addComponent(lblCreationDate, 1, 0);
        addComponent(lblLocation, 1, 1);
        addComponent(lblGID, 2, 0);
        addComponent(lblReference, 2, 1);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {

    }

}

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

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class GermplasmCharacteristicsComponent extends VerticalLayout{

    public GermplasmCharacteristicsComponent(GermplasmDetailModel gDetailModel) {

        Label lblGID = new Label("GID : " + gDetailModel.getGid());
        addComponent(lblGID);
        Label lblPrefName = new Label("Preferred Name : " + gDetailModel.getGermplasmPreferredName());
        addComponent(lblPrefName);
        Label lblLocation = new Label("Location : " + gDetailModel.getGermplasmLocation());
        addComponent(lblLocation);
        Label lblGermplasmMethod = new Label("Method : " + gDetailModel.getGermplasmMethod());
        addComponent(lblGermplasmMethod);
        Label lblCreationDate = new Label("Creation Date : " + gDetailModel.getGermplasmCreationDate());
        addComponent(lblCreationDate);
        Label lblReference = new Label("Reference : " + gDetailModel.getReference());
        addComponent(lblReference);
        setSpacing(true);
        setMargin(true);
    }

}

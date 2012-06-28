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

import org.generationcp.browser.i18n.ui.I18NVerticalLayout;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.ui.Label;

public class GermplasmCharacteristicsComponent extends I18NVerticalLayout{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GermplasmCharacteristicsComponent(GermplasmDetailModel gDetailModel, I18N i18n) {
    	
    	super(i18n);

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

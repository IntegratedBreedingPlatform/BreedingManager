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

import org.generationcp.browser.i18n.ui.I18NGridLayout;
import org.generationcp.browser.i18n.ui.I18NVerticalLayout;
import org.generationcp.middleware.pojos.Study;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.ui.Label;

public class GermplasmCharacteristicsComponent extends I18NGridLayout{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
	private Label lblGID;
	private Label lblPrefName;
	private Label lblLocation;
	private Label lblGermplasmMethod;
	private Label lblCreationDate;
	private Label lblReference;

    public GermplasmCharacteristicsComponent(GermplasmDetailModel gDetailModel, I18N i18n) {

        super(i18n);

        setRows(7);
        setColumns(3);
        setSpacing(true);
        setMargin(true);

        lblGID = new Label(i18n.getMessage("gid.label")); // "Name"
        lblPrefName = new Label(i18n.getMessage("prefname.label")); // "Title"
        lblLocation = new Label(i18n.getMessage("location.label")); // "Objective"
        lblGermplasmMethod = new Label(i18n.getMessage("method.label")); // "Type"
        lblCreationDate = new Label(i18n.getMessage("creationdate.label")); // "Start Date"
        lblReference = new Label(i18n.getMessage("reference.label")); // "End Date"

        addComponent(lblGID, 1, 1);
        addComponent(lblPrefName, 1, 2);
        addComponent(lblLocation, 1, 3);
        addComponent(lblGermplasmMethod, 1, 4);
        addComponent(lblCreationDate, 1, 5);
        addComponent(lblReference, 1, 6);

        Label gid = new Label(String.valueOf(gDetailModel.getGid()));
        Label prefName = new Label(gDetailModel.getGermplasmPreferredName());
        Label location = new Label( gDetailModel.getGermplasmLocation());
        Label germplasmMethod = new Label(gDetailModel.getGermplasmMethod());
        Label creationDate = new Label(String.valueOf(gDetailModel.getGermplasmCreationDate()));
        Label reference = new Label(String.valueOf( gDetailModel.getReference()));

        addComponent(gid, 2, 1);
        addComponent(prefName, 2, 2);
        addComponent(location, 2, 3);
        addComponent(germplasmMethod, 2, 4);
        addComponent(creationDate, 2, 5);
        addComponent(reference, 2, 6);
        
    }

}

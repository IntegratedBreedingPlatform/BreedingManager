package org.generationcp.browser.germplasm;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class GermplasmCharacteristicsComponent  extends VerticalLayout {
	
	public GermplasmCharacteristicsComponent(GermplasmDetailModel gDetailModel){
		
		Label lblGID = new Label("GID : "+gDetailModel.getGid());
		addComponent(lblGID);
		Label lblPrefName = new Label("Preferred Name : "+gDetailModel.getGermplasmPreferredName());
		addComponent(lblPrefName);
		Label lblLocation = new Label("Location : "+gDetailModel.getGermplasmLocation());
		addComponent(lblLocation);
		Label lblGermplasmMethod = new Label("Method : "+gDetailModel.getGermplasmMethod());
		addComponent(lblGermplasmMethod);
		Label lblCreationDate = new Label("Creation Date : "+gDetailModel.getGermplasmCreationDate());
		addComponent(lblCreationDate);
		Label lblReference = new Label("Reference : "+gDetailModel.getReference());
		addComponent(lblReference);
		setSpacing(true);
		setMargin(true);
	}

}

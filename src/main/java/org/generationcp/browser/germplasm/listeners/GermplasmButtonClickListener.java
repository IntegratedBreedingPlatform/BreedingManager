/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/

package org.generationcp.browser.germplasm.listeners;

import org.generationcp.browser.application.GermplasmBrowserOnlyApplication;
import org.generationcp.browser.application.WelcomeTab;
import org.generationcp.browser.germplasm.GermplasmBrowserMainApplication;
import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class GermplasmButtonClickListener implements Button.ClickListener{

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmButtonClickListener.class);
    private static final long serialVersionUID = 1721485345429990412L;

    private Object source;

    public GermplasmButtonClickListener(Object source) {
	this.source = source;
    }

    @Override
    public void buttonClick(ClickEvent event) {

	if ((source instanceof GermplasmBrowserMainApplication) && event.getComponent().getCaption().equals("Search")) {
	    ((GermplasmBrowserMainApplication) source).searchButtonClickAction();

	} else if ((source instanceof GermplasmBrowserOnlyApplication) && event.getComponent().getCaption().equals("Search")) {
	    ((GermplasmBrowserOnlyApplication) source).searchButtonClickAction();
	    
	} else if ((source instanceof WelcomeTab) && event.getComponent().getCaption().equals("I want to browse Germplasm information")) {
	    ((WelcomeTab) source).browserGermplasmInfoButtonClickAction();
	    
	} else if ((source instanceof WelcomeTab) && event.getComponent().getCaption().equals("I want to retrieve Germplasms by Phenotypic Data")) {
	    ((WelcomeTab) source).searchGermplasmByPhenotyicDataButtonClickAction();

	} else if ((source instanceof SearchGermplasmByPhenotypicTab) && (event.getComponent().getCaption().equals("Add Criteria"))) {
	    ((SearchGermplasmByPhenotypicTab) source).addCriteriaButtonClickAction();	

	} else if ((source instanceof SearchGermplasmByPhenotypicTab) && (event.getComponent().getCaption().equals("Delete"))) {
	    ((SearchGermplasmByPhenotypicTab) source).deleteButtonClickAction();	

	} else if ((source instanceof SearchGermplasmByPhenotypicTab) && (event.getComponent().getCaption().equals("Delete All"))) {
	    ((SearchGermplasmByPhenotypicTab) source).deleteAllButtonClickAction();	

	} else if ((source instanceof SearchGermplasmByPhenotypicTab) && (event.getComponent().getCaption().equals("Search"))) {
	    ((SearchGermplasmByPhenotypicTab) source).searchButtonClickAction();	

	}
    }
}

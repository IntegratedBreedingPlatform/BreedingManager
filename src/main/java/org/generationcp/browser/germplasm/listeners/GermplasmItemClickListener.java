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
import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.GermplasmPedigreeTreeComponent;
import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
import org.generationcp.middleware.exceptions.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

public class GermplasmItemClickListener implements ItemClickEvent.ItemClickListener{

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmItemClickListener.class);
    private static final long serialVersionUID = -1095503156046245812L;

    private Object sourceClass;
    private Component sourceComponent;

    public GermplasmItemClickListener(Object sourceClass) {
	this.sourceClass = sourceClass;
    }

    public GermplasmItemClickListener(Object sourceClass, Component sourceComponent) {
	this.sourceClass = sourceClass;
	this.sourceComponent = sourceComponent;
    }

    @Override
    public void itemClick(ItemClickEvent event) {

	if (sourceClass instanceof GermplasmBrowserMain) {
	    if (event.getButton() == ItemClickEvent.BUTTON_LEFT) {
		((GermplasmBrowserMain) sourceClass).resultTableItemClickAction((Table) event.getSource(), event.getItemId(),
			event.getItem());
	    }
	} else if (sourceClass instanceof GermplasmBrowserOnlyApplication) {
	    if (event.getButton() == ItemClickEvent.BUTTON_LEFT) {
		((GermplasmBrowserOnlyApplication) sourceClass).resultTableItemClickAction((Table) event.getSource(), event.getItemId(), event.getItem());
	    }

	} else if (sourceClass instanceof GermplasmPedigreeTreeComponent) {
	    if (event.getButton() == ItemClickEvent.BUTTON_LEFT) {
		try {
		    ((GermplasmPedigreeTreeComponent) sourceClass).displayNewGermplasmDetailTab((Integer) event.getItemId());
		} catch (QueryException e) {
		    LOG.error("Error in GermplasmDetailTabClick: " + e.getMessage());
		}
	    }

	} else if ((sourceClass instanceof SearchGermplasmByPhenotypicTab) && (event.getComponent() == sourceComponent)) {
	    ((SearchGermplasmByPhenotypicTab) sourceClass).traitTableItemClickAction((Table) event.getSource(), event.getItemId(),
		    event.getItem());
	    
	} else if ((sourceClass instanceof SearchGermplasmByPhenotypicTab) && (event.getComponent() == sourceComponent)) {
	    ((SearchGermplasmByPhenotypicTab) sourceClass).scaleTableItemClickAction((Table) event.getSource(), event.getItemId(),
		    event.getItem());
	}

    }

}

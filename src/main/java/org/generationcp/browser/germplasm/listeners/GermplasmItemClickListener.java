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

package org.generationcp.browser.germplasm.listeners;

import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.GermplasmPedigreeTreeComponent;
import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

public class GermplasmItemClickListener implements ItemClickEvent.ItemClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmItemClickListener.class);
    private static final long serialVersionUID = -1095503156046245812L;

    private Object sourceClass;
    private Component sourceComponent;

    public GermplasmItemClickListener(Object sourceClass) {
        this.sourceClass = sourceClass;
    }

    public GermplasmItemClickListener(Object sourceClass, Component sourceComponent) {
        this(sourceClass);
        this.sourceComponent = sourceComponent;
    }

    @Override
    public void itemClick(ItemClickEvent event) {

        if (sourceClass instanceof GermplasmBrowserMain) {
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                try {
                    ((GermplasmBrowserMain) sourceClass).resultTableItemClickAction((Table) event.getSource(), event.getItemId(),
                            event.getItem());
                } catch (InternationalizableException e) {  
                    LOG.error("Error in GermplasmItemClickListener: " + e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());  // TESTED
                }
            }
        } else if (sourceClass instanceof GermplasmPedigreeTreeComponent) {
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                try {
                    ((GermplasmPedigreeTreeComponent) sourceClass).displayNewGermplasmDetailTab((Integer) event.getItemId());
                } catch (InternationalizableException e) {
                    LOG.error("Error in GermplasmItemClickListener: " + e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());  // TESTED
                }
            }

        } else if (sourceClass instanceof SearchGermplasmByPhenotypicTab && event.getComponent() == sourceComponent) {
            try{
                ((SearchGermplasmByPhenotypicTab) sourceClass).traitTableItemClickAction((Table) event.getSource(), 
                        event.getItemId(), event.getItem());
            }catch (InternationalizableException e){
                LOG.error("Error in GermplasmItemClickListener: " + e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }


        } else if (sourceClass instanceof SearchGermplasmByPhenotypicTab && event.getComponent() == sourceComponent) {
            try{
                ((SearchGermplasmByPhenotypicTab) sourceClass).scaleTableItemClickAction((Table) event.getSource(), 
                        event.getItemId(), event.getItem());
            } catch (InternationalizableException e) {
                LOG.error("Error in GermplasmItemClickListener: " + e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        }
    }
    
}

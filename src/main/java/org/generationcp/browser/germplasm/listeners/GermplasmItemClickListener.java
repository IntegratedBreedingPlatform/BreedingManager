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
import org.generationcp.browser.i18n.ui.I18NTable;
import org.generationcp.middleware.exceptions.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Component;

public class GermplasmItemClickListener implements ItemClickEvent.ItemClickListener{

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmItemClickListener.class);
    private static final long serialVersionUID = -1095503156046245812L;

    private Object sourceClass;
    private Component sourceComponent;

    private I18N i18n;

    public GermplasmItemClickListener(Object sourceClass, I18N i18n) {
        this.sourceClass = sourceClass;
        this.i18n = i18n;
    }

    public GermplasmItemClickListener(Object sourceClass, Component sourceComponent, I18N i18n) {

        this(sourceClass, i18n);
        this.sourceComponent = sourceComponent;

    }

    @Override
    public void itemClick(ItemClickEvent event) {

        if (sourceClass instanceof GermplasmBrowserMain) {
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                ((GermplasmBrowserMain) sourceClass).resultTableItemClickAction((I18NTable) event.getSource(), event.getItemId(),
                        event.getItem());
            }
        } else if (sourceClass instanceof GermplasmPedigreeTreeComponent) {
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                try {
                    ((GermplasmPedigreeTreeComponent) sourceClass).displayNewGermplasmDetailTab((Integer) event.getItemId());
                } catch (QueryException e) {
                    LOG.error("Error in GermplasmDetailTabClick: " + e.getMessage());
                }
            }

        } else if (sourceClass instanceof SearchGermplasmByPhenotypicTab && event.getComponent() == sourceComponent) {
            ((SearchGermplasmByPhenotypicTab) sourceClass).traitTableItemClickAction((I18NTable) event.getSource(), event.getItemId(),
                    event.getItem());

        } else if (sourceClass instanceof SearchGermplasmByPhenotypicTab && event.getComponent() == sourceComponent) {
            ((SearchGermplasmByPhenotypicTab) sourceClass).scaleTableItemClickAction((I18NTable) event.getSource(), event.getItemId(),
                    event.getItem());
        }

    }

}

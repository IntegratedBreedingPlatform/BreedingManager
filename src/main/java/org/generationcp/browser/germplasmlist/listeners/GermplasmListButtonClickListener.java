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

package org.generationcp.browser.germplasmlist.listeners;

import org.generationcp.browser.application.WelcomeTab;
import org.generationcp.browser.germplasmlist.GermplasmListTreeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;

public class GermplasmListButtonClickListener implements Button.ClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListButtonClickListener.class);
    private static final long serialVersionUID = 2185217915388685523L;

    private Layout source;

    public GermplasmListButtonClickListener(Layout source) {
        this.source = source;
    }

    @Override
    public void buttonClick(ClickEvent event) {
        
        if (event.getButton().getData().equals(WelcomeTab.BROWSE_GERMPLASM_LIST_BUTTON_ID) // "I want to browse Germplasm List information"
                && (source instanceof WelcomeTab)) {
            ((WelcomeTab) source).browseGermplasmListInfoButtonClickAction();

        } else if (event.getButton().getData().equals(GermplasmListTreeComponent.REFRESH_BUTTON_ID) // "Refresh"
                && (source instanceof GermplasmListTreeComponent)) {
            ((GermplasmListTreeComponent) source).createTree();
        } else {
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

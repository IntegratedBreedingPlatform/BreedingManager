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

package org.generationcp.breeding.manager.crossingmanager.listeners;


import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListTreeComponent;
import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class SelectListButtonClickListener implements Button.ClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(SelectListButtonClickListener.class);
    private static final long serialVersionUID = 2185217915388685523L;

    private Object source;

    public SelectListButtonClickListener(Object source) {
        this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        
        if (event.getButton().getData().equals(SelectGermplasmListTreeComponent.REFRESH_BUTTON_ID) // "Refresh"
                && (source instanceof SelectGermplasmListTreeComponent)) {
            ((SelectGermplasmListTreeComponent) source).createTree();

        } else if (event.getButton().getData().equals(SelectGermplasmListWindow.DONE_BUTTON_ID)
                && (source instanceof SelectGermplasmListWindow)) {
            ((SelectGermplasmListWindow) source).populateParentList();
            
        } else {
            LOG.error("SelectListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

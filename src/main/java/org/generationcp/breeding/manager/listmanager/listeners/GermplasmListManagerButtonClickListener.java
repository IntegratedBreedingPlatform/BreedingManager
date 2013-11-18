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

package org.generationcp.breeding.manager.listmanager.listeners;


import org.generationcp.breeding.manager.listmanager.ListDetailComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListManagerSearchListsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;

public class GermplasmListManagerButtonClickListener implements Button.ClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListManagerButtonClickListener.class);
    private static final long serialVersionUID = 2185217915388685523L;

    private Component source;

    public GermplasmListManagerButtonClickListener(Component source) {
        this.source = source;
    }
    
    public GermplasmListManagerButtonClickListener(Layout source) {
        this.source = source;
    }
    
    
    @Override
    public void buttonClick(ClickEvent event) {
        
    	if (event.getButton().getData().equals(ListManagerSearchListsComponent.SEARCH_BUTTON)
                && (source instanceof ListManagerSearchListsComponent)) { // "Delete Germplasm List"
            ((ListManagerSearchListsComponent) source).searchButtonClickAction();   
    	} else if (event.getButton().getData().equals(ListManagerMain.BUILD_NEW_LIST_BUTTON_DATA)
                && (source instanceof ListManagerMain)) { // "Build a new list"
    		((ListManagerMain) source).showBuildNewListComponent();
        } else {
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

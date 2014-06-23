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


import org.generationcp.breeding.manager.listmanager.BrowseGermplasmTreeMenu;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListManagerSearchListsComponent;
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
    private Integer itemId;
    
    public GermplasmListManagerButtonClickListener(Component source) {
        this.source = source;
    }
    
    public GermplasmListManagerButtonClickListener(Layout source) {
        this.source = source;
    }

    public GermplasmListManagerButtonClickListener(Layout source, Integer itemId) {
        this.source = source;
        this.itemId = itemId;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        
    	Object data = event.getButton().getData();
		if (data.equals(ListManagerSearchListsComponent.SEARCH_BUTTON)
                && (source instanceof ListManagerSearchListsComponent)) { // "Delete Germplasm List"
            ((ListManagerSearchListsComponent) source).searchButtonClickAction();   
    	
    	} else if (data.equals(ListManagerMain.BUILD_NEW_LIST_BUTTON_DATA)
                && (source instanceof ListManagerMain)) { // "Build a new list"
    		((ListManagerMain) source).showBuildNewListComponent();
        
    	} else if (data.equals(BrowseGermplasmTreeMenu.SAVE_TO_LIST)
                && (source instanceof BrowseGermplasmTreeMenu)) { // "Save to List"
    		BrowseGermplasmTreeMenu component = (BrowseGermplasmTreeMenu) source;
    		ListManagerMain listManagerMain = component.getListManagerMain();
			listManagerMain.showBuildNewListComponent();
			listManagerMain.getBuildListComponent().addGermplasmToGermplasmTable(this.itemId, null);
    	} else {
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

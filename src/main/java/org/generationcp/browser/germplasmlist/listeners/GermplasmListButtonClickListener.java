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
import org.generationcp.browser.germplasmlist.GermplasmListDetailComponent;
import org.generationcp.browser.germplasmlist.GermplasmListDataComponent;
import org.generationcp.browser.germplasmlist.GermplasmListTreeComponent;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Window;

public class GermplasmListButtonClickListener implements Button.ClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListButtonClickListener.class);
    private static final long serialVersionUID = 2185217915388685523L;

    private Layout source;
    private GermplasmList germplasmList = null;

    public GermplasmListButtonClickListener(Layout source) {
        this.source = source;
    }
    
    public GermplasmListButtonClickListener(Layout source, GermplasmList germplasmList) {
        this.source = source;
        this.germplasmList = germplasmList;	
    }
    
    
    @Override
    public void buttonClick(ClickEvent event) {
        
        if (event.getButton().getData().equals(WelcomeTab.BROWSE_GERMPLASM_LIST_BUTTON_ID) // "I want to browse Germplasm List information"
                && (source instanceof WelcomeTab)) {
            ((WelcomeTab) source).browseGermplasmListInfoButtonClickAction();

        } else if (event.getButton().getData().equals(GermplasmListTreeComponent.REFRESH_BUTTON_ID) // "Refresh"
                && (source instanceof GermplasmListTreeComponent)) {
            ((GermplasmListTreeComponent) source).createTree();
            
        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.LOCK_BUTTON_ID) 
        		&& (source instanceof GermplasmListDetailComponent)) { // "Lock Germplasm List"
        	((GermplasmListDetailComponent) source).lockGermplasmList();

        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.UNLOCK_BUTTON_ID) 
        		&& (source instanceof GermplasmListDetailComponent)) { // "Unlock Germplasm List"
        	((GermplasmListDetailComponent) source).unlockGermplasmList();        	
        	
        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.DELETE_BUTTON_ID)
        		&& (source instanceof GermplasmListDetailComponent)) { // "Delete Germplasm List"
        	((GermplasmListDetailComponent) source).deleteGermplasmList();

        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.CONFIRM_DELETE_BUTTON_ID)
        		&& (source instanceof GermplasmListDetailComponent)) { // "Yes"
        	((GermplasmListDetailComponent) source).deleteGermplasmListConfirmed();

        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.CANCEL_DELETE_BUTTON_ID)
        		&& (source instanceof GermplasmListDetailComponent)) { // "Yes"
        	((GermplasmListDetailComponent) source).closeConfirmationWindow();
            
        } else if (event.getButton().getData().equals(GermplasmListDataComponent.SORTING_BUTTON_ID) // "Save Sorting"
                && (source instanceof GermplasmListDataComponent)) {
            try {
                ((GermplasmListDataComponent) source).saveSortingAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(GermplasmListDataComponent.DELETE_LIST_ENTRIES_BUTTON_ID) // "Delete List Entries"
                && (source instanceof GermplasmListDataComponent)) {
            try {
                ((GermplasmListDataComponent) source).deleteListButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(GermplasmListDataComponent.EXPORT_BUTTON_ID) // "Export List"
                && (source instanceof GermplasmListDataComponent)) {
            try {
                ((GermplasmListDataComponent) source).exportListAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }

        } else {
        	System.out.println("DEBUG - button pressed is '" + event.getButton().getData() + "'");
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

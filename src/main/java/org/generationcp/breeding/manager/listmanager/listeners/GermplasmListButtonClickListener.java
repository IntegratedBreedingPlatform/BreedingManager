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


import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListDetailComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerCopyToNewListDialog;
import org.generationcp.breeding.manager.listmanager.ListManagerDetailsLayout;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;

public class GermplasmListButtonClickListener implements Button.ClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListButtonClickListener.class);
    private static final long serialVersionUID = 2185217915388685523L;

    private Component source;
    private GermplasmList germplasmList = null;

    public GermplasmListButtonClickListener(Component source) {
        this.source = source;
    }
    
    public GermplasmListButtonClickListener(Layout source, GermplasmList germplasmList) {
        this.source = source;
        this.germplasmList = germplasmList;    
    }
    
    
    @Override
    public void buttonClick(ClickEvent event) {
        
        if (event.getButton().getData().equals(ListTreeComponent .REFRESH_BUTTON_ID) // "Refresh"
                && (source instanceof ListTreeComponent)) {
            ((ListTreeComponent) source).refreshTree();
        } else if (event.getButton().getData().equals(ListManagerDetailsLayout.CLOSE_ALL_TABS_ID)
        		&& (source instanceof ListManagerDetailsLayout)){// "Close" All Tabs
        	((ListManagerDetailsLayout) source).closeAllListDetailTabButtonClickAction();
        } else if (event.getButton().getData().equals(ListDetailComponent.LOCK_BUTTON_ID) 
                && (source instanceof ListDetailComponent)) { // "Lock Germplasm List"
            ((ListDetailComponent) source).lockGermplasmList();

        } else if (event.getButton().getData().equals(ListDetailComponent.UNLOCK_BUTTON_ID) 
                && (source instanceof ListDetailComponent)) { // "Unlock Germplasm List"
            ((ListDetailComponent) source).unlockGermplasmList();            
            
        } else if (event.getButton().getData().equals(ListDetailComponent.DELETE_BUTTON_ID)
                && (source instanceof ListDetailComponent)) { // "Delete Germplasm List"
            ((ListDetailComponent) source).deleteGermplasmList();
        }else if (event.getButton().getData().equals(ListManagerCopyToNewListDialog.SAVE_BUTTON_ID)
                    && (source instanceof ListManagerCopyToNewListDialog)) { // "Save Germplasm List"
                ((ListManagerCopyToNewListDialog) source).saveGermplasmListButtonClickAction();
        }else if (event.getButton().getData().equals(ListManagerCopyToNewListDialog.CANCEL_BUTTON_ID)
                && (source instanceof ListManagerCopyToNewListDialog)) { // "Cancel Germplasm List"
            ((ListManagerCopyToNewListDialog) source).cancelGermplasmListButtonClickAction();
        }else if (event.getButton().getData().equals(AddEntryDialog.SEARCH_BUTTON_ID)
                    && (source instanceof AddEntryDialog)){
                try {
                    ((AddEntryDialog) source).searchButtonClickAction();
                } catch (InternationalizableException e){
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
                }
            
    	}else if (event.getButton().getData().equals(AddEntryDialog.DONE_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).doneButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        }else if (event.getButton().getData().equals(AddEntryDialog.BACK_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).backButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        }else if (event.getButton().getData().equals(AddEntryDialog.NEXT_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).nextButtonClickAction(event);
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        }
        else {
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
		
    }

}

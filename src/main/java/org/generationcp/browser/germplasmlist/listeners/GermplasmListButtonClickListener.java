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
import org.generationcp.browser.germplasmlist.GermplasmListCopyToNewListDialog;
import org.generationcp.browser.germplasmlist.GermplasmListDataComponent;
import org.generationcp.browser.germplasmlist.GermplasmListDetailComponent;
import org.generationcp.browser.germplasmlist.GermplasmListTreeComponent;
import org.generationcp.browser.germplasmlist.dialogs.AddEntryDialog;
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
    
    public GermplasmListButtonClickListener(Component source) {
        this.source = source;
    }
    
    public GermplasmListButtonClickListener(Layout source, GermplasmList germplasmList) {
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
            
        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.LOCK_BUTTON_ID) 
                && (source instanceof GermplasmListDetailComponent)) { // "Lock Germplasm List"
            ((GermplasmListDetailComponent) source).lockGermplasmList();

        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.UNLOCK_BUTTON_ID) 
                && (source instanceof GermplasmListDetailComponent)) { // "Unlock Germplasm List"
            ((GermplasmListDetailComponent) source).unlockGermplasmList();            
            
        } else if (event.getButton().getData().equals(GermplasmListDetailComponent.DELETE_BUTTON_ID)
                && (source instanceof GermplasmListDetailComponent)) { // "Delete Germplasm List"
            ((GermplasmListDetailComponent) source).deleteGermplasmList();

        } else if (event.getButton().getData().equals(GermplasmListDataComponent.SORTING_BUTTON_ID) // "Save Sorting"
                && (source instanceof GermplasmListDataComponent)) {
            try {
                ((GermplasmListDataComponent) source).saveChangesAction();
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
        } else if (event.getButton().getData().equals(GermplasmListDataComponent.EXPORT_FOR_GENOTYPING_BUTTON_ID) // "Export List For Genotyping Order"
                && (source instanceof GermplasmListDataComponent)) {
            try {
                ((GermplasmListDataComponent) source).exportListForGenotypingOrderAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(GermplasmListDataComponent.COPY_TO_NEW_LIST_BUTTON_ID) // "Copy to New List"
                && (source instanceof GermplasmListDataComponent)) {
            try {
                ((GermplasmListDataComponent) source).copyToNewListAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(GermplasmListCopyToNewListDialog.SAVE_BUTTON_ID) // "Save to New List"
                && (source instanceof GermplasmListCopyToNewListDialog)) {
            try {
                ((GermplasmListCopyToNewListDialog) source).saveGermplasmListButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(GermplasmListCopyToNewListDialog.CANCEL_BUTTON_ID) // "Save to New List"
                && (source instanceof GermplasmListCopyToNewListDialog)) {
            try {
                ((GermplasmListCopyToNewListDialog) source).cancelGermplasmListButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(AddEntryDialog.SEARCH_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).searchButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(AddEntryDialog.NEXT_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).nextButtonClickAction(event);
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(AddEntryDialog.BACK_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).backButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(AddEntryDialog.DONE_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).doneButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (event.getButton().getData().equals(GermplasmListDataComponent.ADD_ENTRIES_BUTTON_ID)
                && (source instanceof GermplasmListDataComponent)){
            try {
                ((GermplasmListDataComponent) source).addEntryButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else {
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

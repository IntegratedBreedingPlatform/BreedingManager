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
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.breeding.manager.listmanager.dialog.ListManagerCopyToNewListDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

public class GermplasmListButtonClickListener implements Button.ClickListener {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListButtonClickListener.class);
    private static final long serialVersionUID = 2185217915388685523L;

    private Component source;

    public GermplasmListButtonClickListener(Component source) {
        this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        
        if (event.getButton().getData().equals(ListTreeComponent.REFRESH_BUTTON_ID) // "Refresh"
                && (source instanceof ListTreeComponent)) {
            ((ListTreeComponent) source).refreshComponent();
        }else if (event.getButton().getData().equals(ListManagerCopyToNewListDialog.SAVE_BUTTON_ID)
                    && (source instanceof ListManagerCopyToNewListDialog)) {
            // "Save Germplasm List"
                ((ListManagerCopyToNewListDialog) source).saveGermplasmListButtonClickAction();
        }else if (event.getButton().getData().equals(ListManagerCopyToNewListDialog.CANCEL_BUTTON_ID)
                && (source instanceof ListManagerCopyToNewListDialog)) {
            // "Cancel Germplasm List"
            ((ListManagerCopyToNewListDialog) source).cancelGermplasmListButtonClickAction();
        }else if (event.getButton().getData().equals(AddEntryDialog.BACK_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).backButtonClickAction();
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        }else if (event.getButton().getData().equals(AddEntryDialog.DONE_BUTTON_ID)
                && (source instanceof AddEntryDialog)){
            try {
                ((AddEntryDialog) source).nextButtonClickAction(event);
            } catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        }else {
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
		
    }

}

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

import org.generationcp.browser.application.WelcomeTab;
import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.SaveGermplasmListDialog;
import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
import org.generationcp.middleware.exceptions.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class GermplasmButtonClickListener implements Button.ClickListener {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmButtonClickListener.class);
    private static final long serialVersionUID = 1721485345429990412L;

    private Object source;

    public GermplasmButtonClickListener(Object source) {
        this.source = source;

    }

    @Override
    public void buttonClick(ClickEvent event) {

        if (source instanceof GermplasmBrowserMain && event.getButton().getData().equals(GermplasmBrowserMain.SEARCH_BUTTON_ID)) {
            ((GermplasmBrowserMain) source).searchButtonClickAction();

        }else if (source instanceof GermplasmBrowserMain && event.getButton().getData().equals(GermplasmBrowserMain.SAVE_GERMPLASMLIST_ID)) {
            ((GermplasmBrowserMain) source).saveGermplasmListButtonClickAction();

        }else if (source instanceof GermplasmBrowserMain && event.getButton().getData().equals(GermplasmBrowserMain.CLOSE_ALL_GERMPLASMDETAIL_TAB_ID)) {
            ((GermplasmBrowserMain) source).closeAllGermplasmDetailTabButtonClickAction();

        }else if (source instanceof SaveGermplasmListDialog && event.getButton().getData().equals(SaveGermplasmListDialog.SAVE_BUTTON_ID)) {
            try {
				((SaveGermplasmListDialog) source).saveGermplasmListButtonClickAction();
			} catch (QueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
        }else if (source instanceof SaveGermplasmListDialog && event.getButton().getData().equals(SaveGermplasmListDialog.CANCEL_BUTTON_ID)) {
            ((SaveGermplasmListDialog) source).cancelGermplasmListButtonClickAction();
            
        }else if (source instanceof WelcomeTab && event.getButton().getData().equals(WelcomeTab.BROWSE_GERMPLASM_BUTTON_ID)) {
            ((WelcomeTab) source).browserGermplasmInfoButtonClickAction();

        } else if (source instanceof WelcomeTab
                && event.getButton().getData().equals(WelcomeTab.BROWSE_GERMPLASM_BY_PHENO_BUTTON_ID)) {
            ((WelcomeTab) source).searchGermplasmByPhenotyicDataButtonClickAction();

        } else if (source instanceof SearchGermplasmByPhenotypicTab && event.getButton().getData().equals(SearchGermplasmByPhenotypicTab.ADD_CRITERIA_BUTTON_ID)) {
            ((SearchGermplasmByPhenotypicTab) source).addCriteriaButtonClickAction();

        } else if (source instanceof SearchGermplasmByPhenotypicTab && event.getButton().getData().equals(SearchGermplasmByPhenotypicTab.DELETE_BUTTON_ID)) {
            ((SearchGermplasmByPhenotypicTab) source).deleteButtonClickAction();

        } else if (source instanceof SearchGermplasmByPhenotypicTab && event.getButton().getData().equals(SearchGermplasmByPhenotypicTab.DELETE_ALL_BUTTON_ID)) {
            ((SearchGermplasmByPhenotypicTab) source).deleteAllButtonClickAction();

        } else if (source instanceof SearchGermplasmByPhenotypicTab && event.getButton().getData().equals(SearchGermplasmByPhenotypicTab.SEARCH_BUTTON_ID)) {
            ((SearchGermplasmByPhenotypicTab) source).searchButtonClickAction();

        } else {
        	System.out.println("WHY!2");
        }
    }   
}

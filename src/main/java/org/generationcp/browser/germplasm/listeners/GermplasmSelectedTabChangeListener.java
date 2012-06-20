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

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.germplasm.GermplasmDetail;
import org.generationcp.middleware.exceptions.QueryException;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class GermplasmSelectedTabChangeListener implements TabSheet.SelectedTabChangeListener{

    private static final long serialVersionUID = -3192436611974353597L;
    private Object source;

    public GermplasmSelectedTabChangeListener(Object source) {
        this.source = source;
    }

    @Override
    public void selectedTabChange(SelectedTabChangeEvent event) {

        if (source instanceof GermplasmStudyBrowserApplication) {
            ((GermplasmStudyBrowserApplication) source).tabSheetSelectedTabChangeAction(event.getTabSheet());

        } else if (source instanceof GermplasmDetail) {
            try {
                ((GermplasmDetail) source).selectedTabChangeAction();
            } catch (QueryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}

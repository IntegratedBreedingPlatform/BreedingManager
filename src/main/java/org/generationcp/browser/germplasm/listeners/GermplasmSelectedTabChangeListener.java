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
import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.GermplasmDetail;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class GermplasmSelectedTabChangeListener implements TabSheet.SelectedTabChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmSelectedTabChangeListener.class);
    private static final long serialVersionUID = -3192436611974353597L;
    private Object source;

    public GermplasmSelectedTabChangeListener(Object source) {
        this.source = source;
    }

    @Override
    public void selectedTabChange(SelectedTabChangeEvent event) {

        if (source instanceof GermplasmStudyBrowserApplication) {
          
            try {
                ((GermplasmStudyBrowserApplication) source).tabSheetSelectedTabChangeAction(event.getTabSheet());
            } catch (InternationalizableException e) {
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(((GermplasmStudyBrowserApplication) source).getMainWindow(), 
                        e.getCaption(), e.getDescription()); // TESTED 
            }

        } else if (source instanceof GermplasmDetail) {
            try {
                ((GermplasmDetail) source).selectedTabChangeAction();
            } catch (InternationalizableException e) {
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                e.setCaption(Message.ERROR_IN_DISPLAYING_RQUESTED_DETAIL);
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());  // TESTED
            }
        }
    }
    
}

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
        
        if (event.getButton().getData().equals(ListManagerTreeComponent.REFRESH_BUTTON_ID) // "Refresh"
                && (source instanceof ListManagerTreeComponent)) {
            ((ListManagerTreeComponent) source).createTree();
        } else {
            LOG.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}

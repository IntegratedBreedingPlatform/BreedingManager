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

import org.generationcp.browser.germplasmlist.GermplasmListAccordionMenu;
import org.generationcp.browser.germplasmlist.GermplasmListTreeComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class GermplasmListTabChangeListener implements TabSheet.SelectedTabChangeListener{
    
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTabChangeListener.class);
    private static final long serialVersionUID = -5145904396164706110L;

    private GermplasmListAccordionMenu accordionMenu;

    public GermplasmListTabChangeListener(GermplasmListAccordionMenu accordion) {
        this.accordionMenu = accordion;
    }
	@Override
    public void selectedTabChange(SelectedTabChangeEvent event){
			accordionMenu.refreshListData();
	}
    

}

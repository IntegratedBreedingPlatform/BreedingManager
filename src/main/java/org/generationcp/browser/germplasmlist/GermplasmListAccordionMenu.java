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

package org.generationcp.browser.germplasmlist;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListSelectedTabChangeListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmListAccordionMenu extends Accordion implements InitializingBean, InternationalizableComponent {
    
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListAccordionMenu.class);
    private static final long serialVersionUID = -1409312205229461614L;
    private int germplasmListId;
    private VerticalLayout layoutListData;

    private GermplasmListManager germplasmListManager;
    private GermplasmListDetailComponent germplasmListDetailComponent;
   
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmListAccordionMenu(int germplasmListId, GermplasmListManager germplasmListManager, 
            GermplasmListDetailComponent germplasmListDetailComponent) {
        this.germplasmListId = germplasmListId;
        this.germplasmListManager = germplasmListManager;
        this.germplasmListDetailComponent = germplasmListDetailComponent;
        // Have it take all space available in the layout.
    }

    public void selectedTabChangeAction() throws InternationalizableException{
        Component selected = this.getSelectedTab();
        Tab tab = this.getTab(selected);
        if (tab.getCaption().equals(layoutListData.getCaption())) { // "Germplasm List Data"
            if (layoutListData.getComponentCount() == 0) {
                //TODO: layoutListData.addComponent(new GermplasmListDataComponent(germplasmListManager, germplasmListId));
                layoutListData.setMargin(true);
                layoutListData.setSpacing(true);
            }
        }
    }
    
    @Override
    public void afterPropertiesSet() {
        this.setSizeFull();

        layoutListData = new VerticalLayout();
        this.addTab(germplasmListDetailComponent, "Germplasm List Details"); // "Germplasm List Details"
        this.addTab(layoutListData, "Germplasm List Data"); // "Germplasm List Data"

        this.addListener(new GermplasmListSelectedTabChangeListener(this));    	
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(germplasmListDetailComponent, Message.germplasm_list_details_text);
        messageSource.setCaption(layoutListData, Message.germplasm_list_data_text);
    }

}

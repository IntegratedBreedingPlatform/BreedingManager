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
    
    private static final String LIST_DETAILS = "List Details";
    private static final String LIST_DATA = "List Data";
    
    private int germplasmListId;
    private GermplasmListDetailComponent germplasmListDetailComponent;
    
    private VerticalLayout layoutListData;
    
    private boolean fromUrl;	//this is true if this component is created by accessing the Germplasm List Details page directly from the URL
   
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
	private GermplasmListManager germplasmListManager;

    public GermplasmListAccordionMenu(int germplasmListId, boolean fromUrl) {
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
    }

    public void selectedTabChangeAction() throws InternationalizableException{
        Component selected = this.getSelectedTab();
        Tab tab = this.getTab(selected);
        if (tab.getComponent() instanceof VerticalLayout) {
            if (((VerticalLayout) tab.getComponent()).getData().equals(LIST_DATA)) { // "Germplasm List Data"
                if (layoutListData.getComponentCount() == 0) {
                    layoutListData.addComponent(new GermplasmListDataComponent(germplasmListManager, germplasmListId, fromUrl));
                    layoutListData.setMargin(true);
                    layoutListData.setSpacing(true);
                }
            }
        }
    }
    
    @Override
    public void afterPropertiesSet() {
        this.setSizeFull();
        
        germplasmListDetailComponent = new GermplasmListDetailComponent(germplasmListManager, germplasmListId);
        germplasmListDetailComponent.setData(LIST_DETAILS);

        layoutListData = new VerticalLayout();
        layoutListData.setData(LIST_DATA);
        
        this.addTab(germplasmListDetailComponent, messageSource.getMessage(Message.GERMPLASM_LIST_DETAILS_TAB)); // "Germplasm List Details"
        this.addTab(layoutListData, messageSource.getMessage(Message.GERMPLASM_LIST_DATA_TAB)); // "Germplasm List Data"

        this.addListener(new GermplasmListSelectedTabChangeListener(this));    	
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    }

}

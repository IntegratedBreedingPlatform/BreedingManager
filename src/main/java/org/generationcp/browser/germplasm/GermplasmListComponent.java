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

package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.ListsForGermplasmQuery;
import org.generationcp.browser.germplasm.containers.ListsForGermplasmQueryFactory;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Table;

@Configurable
public class GermplasmListComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    

    private static final String NAME = "Name";
    private static final String DATE = "Date";
    private static final String DESCRIPTION = "Description";
    
    private GermplasmListManager dataManager;
    private Integer gid;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmListComponent(GermplasmListManager dataManager, Integer gid) {
    	this.dataManager = dataManager;
    	this.gid = gid;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ListsForGermplasmQueryFactory factory = new ListsForGermplasmQueryFactory(this.dataManager, this.gid);
        LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);
        
        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_NAME, String.class, null);
        container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_DATE, String.class, null);
        container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION, String.class, null);
        
        container.getQueryView().getItem(0); // initialize the first batch of data to be displayed
        
        setContainerDataSource(container);
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        setPageLength(15);
        setColumnHeaders(new String[] { NAME, DATE, DESCRIPTION });
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(this, NAME, Message.name_header);
        messageSource.setColumnHeader(this, DATE, Message.date_header);
        messageSource.setColumnHeader(this, DESCRIPTION, Message.description_header);
    }

}

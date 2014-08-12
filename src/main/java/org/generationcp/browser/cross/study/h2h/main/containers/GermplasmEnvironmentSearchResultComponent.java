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

package org.generationcp.browser.cross.study.h2h.main.containers;

import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.GermplasmSearchQuery;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

/**
 * Component cloned from the original GermplasmSearchResultComponent - but this one searches Germplasm
 * from a list of Environments. For the H2H Query
 * 
 * @author : not really authored - cloned and adjusted by Rebecca
 * 
 */
@Configurable
public class GermplasmEnvironmentSearchResultComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    
    private CrossStudyDataManager crossStudyDataManager;
    private String searchChoice;
    private String searchValue;
    private List<Integer> environmentIds;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;


    public GermplasmEnvironmentSearchResultComponent(CrossStudyDataManager crossStudyDataManager, String searchChoice, String searchValue, List<Integer> environmentIds) {
        this.crossStudyDataManager = crossStudyDataManager;
        this.searchChoice = searchChoice;
        this.searchValue = searchValue;
        this.environmentIds = environmentIds;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        GermplasmEnvironmentSearchQueryFactory factory = new GermplasmEnvironmentSearchQueryFactory(crossStudyDataManager, searchChoice, searchValue, environmentIds);
        LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        container.addContainerProperty(GermplasmSearchQuery.GID, String.class, null);
        container.addContainerProperty(GermplasmSearchQuery.NAMES, String.class, null);
        container.addContainerProperty(GermplasmSearchQuery.LOCATION, String.class, null);
        
        messageSource.setColumnHeader(this, (String) GermplasmSearchQuery.GID, Message.GID_LABEL);
        messageSource.setColumnHeader(this, (String) GermplasmSearchQuery.NAMES, Message.NAMES_LABEL);
        messageSource.setColumnHeader(this, (String) GermplasmSearchQuery.LOCATION, Message.LOCATION_LABEL);
        
        container.getQueryView().getItem(0); // initialize the first batch of data to be displayed
        
        setColumnWidth(GermplasmSearchQuery.GID, 100);
        setContainerDataSource(container);
        setWidth("100%");
        setHeight("200px");
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);

        this.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = 1L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.CLICK_TO_VIEW_GERMPLASM_DETAILS);
            }
        });

    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(this, (String) GermplasmSearchQuery.GID, Message.GID_LABEL);
        messageSource.setColumnHeader(this, (String) GermplasmSearchQuery.NAMES, Message.NAMES_LABEL);
        messageSource.setColumnHeader(this, (String) GermplasmSearchQuery.LOCATION, Message.LOCATION_LABEL);
    }

}

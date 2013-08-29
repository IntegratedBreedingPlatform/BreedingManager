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
import org.generationcp.browser.germplasm.containers.GroupRelativesQuery;
import org.generationcp.browser.germplasm.containers.GroupRelativesQueryFactory;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Table;

@Configurable
public class GermplasmGroupRelativesComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    private int gid;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmGroupRelativesComponent(int gid){
        this.gid = gid;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        GroupRelativesQueryFactory factory = new GroupRelativesQueryFactory(Integer.valueOf(this.gid));
        LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        container.addContainerProperty(GroupRelativesQuery.GID, String.class, null);
        container.addContainerProperty(GroupRelativesQuery.PREFERRED_NAME, String.class, null);
        
        messageSource.setColumnHeader(this, (String) GroupRelativesQuery.GID, Message.GID_LABEL);
        messageSource.setColumnHeader(this, (String) GroupRelativesQuery.PREFERRED_NAME, Message.PREFNAME_LABEL);
        
        container.getQueryView().getItem(0); // initialize the first batch of data to be displayed
        
        this.setContainerDataSource(container);
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(this, (String) GroupRelativesQuery.GID, Message.GID_LABEL);
        messageSource.setColumnHeader(this, (String) GroupRelativesQuery.PREFERRED_NAME, Message.PREFNAME_LABEL);
    }

}

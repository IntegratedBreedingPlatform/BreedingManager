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
import org.generationcp.browser.germplasm.containers.ManagementNeighborsQuery;
import org.generationcp.browser.germplasm.containers.ManagementNeighborsQueryFactory;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmManagementNeighborsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    
    private Integer gid;
    
    private Table managementNeighborsTable;
    private Label noDataAvailableLabel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmManagementNeighborsComponent(Integer gid) {
        this.gid = gid;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeComponents();
        layoutComponents();
    }
    
    private void initializeComponents(){
    	ManagementNeighborsQueryFactory factory = new ManagementNeighborsQueryFactory(gid);
        LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

        if(container.size() > 0){
        	managementNeighborsTable = new Table();
	        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
	        container.addContainerProperty(ManagementNeighborsQuery.GID, String.class, null);
	        container.addContainerProperty(ManagementNeighborsQuery.PREFERRED_NAME, String.class, null);
	        
	        container.getQueryView().getItem(0); // initialize the first batch of data to be displayed
	        
	        managementNeighborsTable.setContainerDataSource(container);
	        if(container.size() < 10){
	        	managementNeighborsTable.setPageLength(container.size());
	        } else{
	        	managementNeighborsTable.setPageLength(10);
	        }
	        managementNeighborsTable.setSelectable(true);
	        managementNeighborsTable.setMultiSelect(false);
	        managementNeighborsTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
	        managementNeighborsTable.setColumnReorderingAllowed(true);
	        managementNeighborsTable.setColumnCollapsingAllowed(true);
	        
	        managementNeighborsTable.setColumnHeader((String) ManagementNeighborsQuery.GID, messageSource.getMessage(Message.GID_LABEL));
	        managementNeighborsTable.setColumnHeader((String) ManagementNeighborsQuery.PREFERRED_NAME, messageSource.getMessage(Message.PREFNAME_LABEL));
        } else{
        	noDataAvailableLabel = new Label("There is no Management Neighbors Information for this germplasm.");
        }
    }
    
    private void layoutComponents(){
    	if(managementNeighborsTable != null){
    		addComponent(managementNeighborsTable);
    	} else{
    		addComponent(noDataAvailableLabel);
    	}
    }

    @Override
    public void updateLabels() {
    }

}

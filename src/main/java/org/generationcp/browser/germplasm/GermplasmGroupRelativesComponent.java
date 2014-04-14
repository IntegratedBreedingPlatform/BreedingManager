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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmGroupRelativesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    private int gid;
    
    private Table groupRelativesTable;
    private Label noDataAvailableLabel;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmGroupRelativesComponent(int gid){
        this.gid = gid;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeComponents();
        layoutComponents();
    }
    
    private void initializeComponents(){
    	GroupRelativesQueryFactory factory = new GroupRelativesQueryFactory(Integer.valueOf(this.gid));
        LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        container.addContainerProperty(GroupRelativesQuery.GID, String.class, null);
        container.addContainerProperty(GroupRelativesQuery.PREFERRED_NAME, String.class, null);
        
        if(container.size() > 0){
        	groupRelativesTable = new Table();
        	
	        groupRelativesTable.setColumnHeader((String) GroupRelativesQuery.GID, messageSource.getMessage(Message.GID_LABEL));
	        groupRelativesTable.setColumnHeader((String) GroupRelativesQuery.PREFERRED_NAME, messageSource.getMessage(Message.PREFNAME_LABEL));
	        
	        container.getQueryView().getItem(0); // initialize the first batch of data to be displayed
	        
	        groupRelativesTable.setContainerDataSource(container);
	        groupRelativesTable.setSelectable(true);
	        groupRelativesTable.setMultiSelect(false);
	        groupRelativesTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
	        groupRelativesTable.setColumnReorderingAllowed(true);
	        groupRelativesTable.setColumnCollapsingAllowed(true);
        } else{
        	noDataAvailableLabel = new Label("There is no Group Relatives information for this germplasm.");
        }
    }

    private void layoutComponents(){
    	if(groupRelativesTable != null){
    		addComponent(groupRelativesTable);
    	} else{
    		addComponent(noDataAvailableLabel);
    	}
    }

    @Override
    public void updateLabels() {
        
    }

}

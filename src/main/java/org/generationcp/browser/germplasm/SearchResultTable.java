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
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Configurable
public class SearchResultTable extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final String GID = "GID";
	private static final String NAMES = "NAMES";
	private static final String METHOD = "METHOD";
	private static final String LOCATION = "LOCATION";
	
    private Table resultTable;
    private IndexedContainer dataSource;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public SearchResultTable(IndexedContainer dataSource) {

        this.dataSource = dataSource;
        
    }

    public Table getResultTable() {
    	
        resultTable = new Table("", this.dataSource);
        resultTable.setWidth("100%");
        resultTable.setHeight("200px");
        // selectable
        resultTable.setSelectable(true);
        resultTable.setMultiSelect(false);
        resultTable.setImmediate(true); // react at once when something is
        // selected
        // turn on column reordering and collapsing
        resultTable.setColumnReorderingAllowed(true);
        resultTable.setColumnCollapsingAllowed(true);

        // set column headers
        resultTable.setColumnHeaders(new String[] { GID, NAMES, METHOD, LOCATION });

        resultTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.click_to_view_germplasm_details);
            }
        });

        return resultTable;
    }

    public void setDataSource(IndexedContainer dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void afterPropertiesSet() {
    	

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

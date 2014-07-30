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

package org.generationcp.browser.study;

import org.generationcp.browser.application.GermplasmStudyBrowserLayout;
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

/**
 * 
 * @author Joyce Avestro
 * 
 */
@Configurable
public class StudySearchResultTable extends VerticalLayout implements InitializingBean, 
									InternationalizableComponent, GermplasmStudyBrowserLayout {

    private static final long serialVersionUID = 1L;
    
    private static final String STUDY_ID = "ID";
    private static final String STUDY_NAME = "NAME";

    private Table resultTable;
    private IndexedContainer dataSource;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudySearchResultTable(IndexedContainer dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void afterPropertiesSet() {
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
    }
    
	@Override
	public void instantiateComponents() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutComponents() {
	}
    
    public Table getResultTable() {
        
        resultTable = new Table("", this.dataSource);
        resultTable.setWidth("98%");
        resultTable.setHeight("250px");
        resultTable.setSelectable(true);
        resultTable.setMultiSelect(false);
        resultTable.setImmediate(true); 
        resultTable.setColumnReorderingAllowed(true);
        resultTable.setColumnCollapsingAllowed(true);
        resultTable.setCaption(null);

        // set column headers
        resultTable.setColumnHeaders(new String[] { STUDY_ID, STUDY_NAME });

        resultTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.CLICK_TO_VIEW_STUDY_DETAILS);
            }
        });

        return resultTable;
    }

    public void setDataSource(IndexedContainer dataSource) {
        this.dataSource = dataSource;
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

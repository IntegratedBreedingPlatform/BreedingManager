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

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.util.TableViewerCellSelectorUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

/**
 * This creates a Vaadin sub-window that displays the Table Viewer.
 * 
 * @author Mark Agarrado
 *
 */
@Configurable
public class TableViewerComponent extends Window implements InitializingBean, InternationalizableComponent {
	
	private static final long serialVersionUID = 477658402146083181L;
	
	private Table displayTable;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public TableViewerComponent (Table displayTable) {
		this.displayTable = displayTable;
        TableViewerCellSelectorUtil tableViewerCellSelectorUtil = new TableViewerCellSelectorUtil(this, displayTable);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		assemble();
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
    	addComponent(displayTable);
	}
    
    protected void initializeValues() {
    	
    }
    
    protected void initializeLayout() {
    	setCaption(messageSource.getMessage(Message.TABLE_VIEWER_CAPTION));
    	setSizeFull();
        center();
        setResizable(true);
        setScrollable(true);
        setModal(true);
        
        // enable basic edit options on the specified table
        displayTable.setColumnCollapsingAllowed(true);
        displayTable.setColumnReorderingAllowed(true);
        displayTable.setPageLength(0); // display all rows of the table to the browser
        displayTable.setSizeFull(); // to make scrollbars appear on the Table component
    }
    
    protected void initializeActions() {
    	//attach listener code here
    }
	
	@Override
	public void updateLabels() {
		
	}

}

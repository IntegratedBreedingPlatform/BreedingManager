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

package org.generationcp.breeding.manager.crosses;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Mark Agarrado
 *
 */
@Configurable
public class NurseryTemplateConditionsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{

    /**
     * 
     */
    private static final long serialVersionUID = 6926035577490148208L;
    
    public static final String BACK_BUTTON_ID = "NurseryTemplateConditionsComponent Back Button";
    public static final String DONE_BUTTON_ID = "NurseryTemplateConditionsComponent Done Button";
    
    public static final String CONDITION_COLUMN = "Condition Column";
    public static final String DESCRIPTION_COLUMN = "Description Column";
    public static final String PROPERTY_COLUMN = "Property Column";
    public static final String SCALE_COLUMN = "Scale Column";
    public static final String VALUE_COLUMN = "Value Column";
    
    
    private Table nurseryConditionsTable;
    
    private Component buttonArea;
    private Button backButton;
    private Button doneButton;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

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
        generateConditionsTable();
        addComponent(nurseryConditionsTable);
        
        buttonArea = layoutButtonArea();
        addComponent(buttonArea);
    }
    
    protected void initializeValues() {
            
    }
    
    protected void initializeLayout() {
        setMargin(true);
        setSpacing(true);
        setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
    }
    
    protected void initializeActions() {
        
    }
    
    protected Component layoutButtonArea() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(true, false, false, false);

        backButton = new Button();
        buttonLayout.addComponent(backButton);
        doneButton = new Button();
        buttonLayout.addComponent(doneButton);
        return buttonLayout;
    }
    
    private void generateConditionsTable() {
        nurseryConditionsTable = new Table();
        nurseryConditionsTable.setStyleName("condition-rows");
        nurseryConditionsTable.setSizeFull();
        
        nurseryConditionsTable.addContainerProperty(CONDITION_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(DESCRIPTION_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(PROPERTY_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(SCALE_COLUMN, String.class, null);
        nurseryConditionsTable.addContainerProperty(VALUE_COLUMN, Component.class, null);
        
        addConditionRows();
        nurseryConditionsTable.setPageLength(nurseryConditionsTable.size());
    }
    
    private void addConditionRows() {
        //TODO: populate this table using values read from the Nursery Template file
        nurseryConditionsTable.addItem(new Object[] {
                "NID", "NURSERY SEQUENCE NUMBER", "NURSERY", "NUMBER", new TextField()
        }, 
        "nid");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDER NAME", "PRINCIPAL INVESTIGATOR", "PERSON", "DBCV", new ComboBox()
        }, 
        "breederName");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDER ID", "PRINCIPAL INVESTIGATOR", "PERSON", "DBID", new TextField()
        }, 
        "breederId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "SITE", "NURSERY SITE NAME", "LOCATION", "DBCV", new ComboBox()
        }, 
        "site");
        
        nurseryConditionsTable.addItem(new Object[] {
                "SITE ID", "NURSERY SITE ID", "LOCATION", "DBID", new TextField()
        }, 
        "siteId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDING METHOD", "Breeding method to be applied to this nursery", "METHOD", "DBCV", new ComboBox()
        }, 
        "breedingMethod");
        
        nurseryConditionsTable.addItem(new Object[] {
                "BREEDING METHOD ID", "ID of Breeding Method", "METHOD", "DBID", new TextField()
        }, 
        "breedingMethodId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "FEMALE LIST NAME", "FEMALE LIST NAME", "GERMPLASM LIST", "DBCV", new TextField()
        }, 
        "femaleListName");
        
        nurseryConditionsTable.addItem(new Object[] {
                "FEMALE LIST ID", "FEMALE LIST ID", "GERMPLASM LIST", "DBID", new TextField()
        }, 
        "femaleListId");
        
        nurseryConditionsTable.addItem(new Object[] {
                "MALE LIST NAME", "MALE LIST NAME", "GERMPLASM LIST", "DBCV", new TextField()
        }, 
        "maleListName");
        
        nurseryConditionsTable.addItem(new Object[] {
                "MALE LIST ID", "MALE LIST ID", "GERMPLASM LIST", "DBID", new TextField()
        }, 
        "maleListId");
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setColumnHeader(nurseryConditionsTable, CONDITION_COLUMN, Message.CONDITION_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, DESCRIPTION_COLUMN, Message.DESCRIPTION_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, PROPERTY_COLUMN, Message.PROPERTY_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, SCALE_COLUMN, Message.SCALE_HEADER);
        messageSource.setColumnHeader(nurseryConditionsTable, VALUE_COLUMN, Message.VALUE_HEADER);
        
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(doneButton, Message.DONE);
    }

}

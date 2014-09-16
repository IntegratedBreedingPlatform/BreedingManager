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
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


/**
 * This class opens a pop-up window for selecting attributes
 * 
 * @author Mark Agarrado
 */

@Configurable
public class FillWithAttributeWindow extends BaseSubWindow implements InternationalizableComponent,
						InitializingBean, BreedingManagerLayout {

    private static final long serialVersionUID = -8850686249688989080L;
    
    private SimpleResourceBundleMessageSource messageSource;
    
    //private Window mainWindow;
    private Table targetTable;
    private String gidPropertyId;
    private String targetPropertyId;
    private HorizontalLayout attributeLayout;
    private ComboBox attributeBox;
    private Button okButton;
    private List<UserDefinedField> attributeList;
    private ListTabComponent listDetailsComponent;
    private org.generationcp.breeding.manager.listmanager.ListBuilderComponent buildListComponent;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    public FillWithAttributeWindow(Table targetTable, String gidPropertyId, 
            String targetPropertyId, SimpleResourceBundleMessageSource messageSource) {
        this.targetTable = targetTable;
        this.gidPropertyId = gidPropertyId;
        this.targetPropertyId = targetPropertyId;
        this.messageSource = messageSource;
    }
    
    public FillWithAttributeWindow(Table targetTable, String gidPropertyId, 
            String targetPropertyId, SimpleResourceBundleMessageSource messageSource, ListTabComponent listDetailsComponent
            ,org.generationcp.breeding.manager.listmanager.ListBuilderComponent buildListComponent) {
        this.targetTable = targetTable;
        this.gidPropertyId = gidPropertyId;
        this.targetPropertyId = targetPropertyId;
        this.messageSource = messageSource;
        this.listDetailsComponent = listDetailsComponent;
        this.buildListComponent = buildListComponent;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        instantiateComponents();
        initializeValues();
        addListeners();
        layoutComponents();
    }
    
	@Override
	public void instantiateComponents() {
        attributeBox = new ComboBox();
        attributeBox.setNullSelectionAllowed(false);
        okButton = new Button();
	}

	@Override
	public void initializeValues() {
		try {
            List<Integer> gids = getGidsFromTable(targetTable);
            attributeList = germplasmDataManager.getAttributeTypesByGIDList(gids);
            
            for (UserDefinedField attribute : attributeList) {
                attributeBox.addItem(attribute.getFldno());
                attributeBox.setItemCaption(attribute.getFldno(), attribute.getFname());
            }
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	@Override
	public void addListeners() {
        okButton.addListener(new ClickListener() {
            private static final long serialVersionUID = -7472646361265849940L;
            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                fillWithAttribute((Integer) attributeBox.getValue());
                // close pop-up
                Window attributeWindow = ((Button) event.getSource()).getWindow();
                attributeWindow.getParent().removeWindow(attributeWindow);
            }
        });
	}

	@Override
	public void layoutComponents() {
		attributeBox.setWidth("300px");
        
        attributeLayout = new HorizontalLayout();
        attributeLayout.setMargin(true);
        attributeLayout.setSpacing(true);
        
        attributeLayout.addComponent(attributeBox);
        attributeLayout.addComponent(okButton);
        
        //set window properties
        setContent(attributeLayout);
        setWidth("400px");
        setHeight("30px");
        center();
        setResizable(false);
        setModal(true);
	}
    
    private void fillWithAttribute(Integer attributeType){
        if (attributeType!=null) {
            try {
                List<Integer> gids = getGidsFromTable(targetTable);
                Map<Integer, String> gidAttributeMap = germplasmDataManager.getAttributeValuesByTypeAndGIDList(attributeType, gids);
                
                List<Integer> itemIds = getItemIds(targetTable);
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    targetTable.getItem(itemId).getItemProperty(targetPropertyId).setValue(gidAttributeMap.get(gid));
                }
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }
        }

        //mark flag that changes have been made in listDataTable
        if(listDetailsComponent != null){
        	listDetailsComponent.getListComponent().setHasUnsavedChanges(true);
        }
        
        if(buildListComponent != null){
        	buildListComponent.setHasUnsavedChanges(true);
        }
     }
    
    private List<Integer> getGidsFromTable(Table table){
        List<Integer> gids = new ArrayList<Integer>();
        List<Integer> listDataItemIds = getItemIds(table);
        for(Integer itemId: listDataItemIds){
            gids.add(Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString()));
        }
        return gids;
    }
   
    @SuppressWarnings("unchecked")
    private List<Integer> getItemIds(Table table){
        List<Integer> itemIds = new ArrayList<Integer>();
        itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
        return itemIds;
    }
    
    @Override
    public void attach() {      
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(this, Message.FILL_WITH_ATTRIBUTE_WINDOW);
        messageSource.setCaption(okButton, Message.OK);
    }
}

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
package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.Iterator;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.browser.cross.study.h2h.main.listeners.SelectListButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.GermplasmListEntry;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * @author Mark Agarrado
 *
 */
@Configurable
public class SelectGermplasmListDialog extends Window implements InitializingBean, InternationalizableComponent {

    /**
     * 
     */
    private static final long serialVersionUID = -8113004135173349534L;
    
    public final static String CLOSE_BUTTON_ID = "SelectGermplasmListDialog Close Button";
    public final static String ADD_BUTTON_ID = "SelectGermplasmListDialog Add Button";
    
    private VerticalLayout mainLayout;
    private SelectGermplasmListComponent selectGermplasmList;
    private Button cancelButton;
    private Button doneButton;
    private HorizontalLayout buttonArea;
    
    private ListSelect parentList;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private Object listId;


    private String germplasmListFor;

    private boolean isTestEntry;

    private Label listnameParent;
    private Window parentWindow;
    private Component source;

    public SelectGermplasmListDialog() {
        this.parentList = new ListSelect();
    }
    public SelectGermplasmListDialog(Component source, Window parentWindow, boolean isTestEntry){
        this.source = source;
        this.parentWindow = parentWindow;
        this.isTestEntry = isTestEntry;        
    }
    /*
    public SelectGermplasmListDialog(ListSelect parentList, CrossingManagerMakeCrossesComponent makeCrossesComponent,Label listnameParent) {
        this.parentList = parentList;
        this.listnameParent=listnameParent;
    }
    
    public SelectGermplasmListWindow(NurseryTemplateConditionsComponent nurseryTemplateConditionComponent,String germplasmListFor) {
    // TODO Auto-generated constructor stub
    this.nurseryTemplateCall=true;
    this.nurseryTemplateConditionComponent=nurseryTemplateConditionComponent;
    this.germplasmListFor=germplasmListFor;
    this.parentList = new ListSelect();
        this.makeCrossesComponent = null;
    }
    */

    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        mainLayout = new VerticalLayout();
        Integer lastOpenedId;
        /*
        if (makeCrossesComponent != null) {
            lastOpenedId = makeCrossesComponent.getLastOpenedListId();
        } else {
            lastOpenedId = null;
        }
        */
        selectGermplasmList = new SelectGermplasmListComponent(null,this);
        
        buttonArea = new HorizontalLayout();
        cancelButton = new Button(); // "Cancel"
        cancelButton.setData(CLOSE_BUTTON_ID);
        doneButton = new Button(); // "Done"
        doneButton.setData(ADD_BUTTON_ID);
        doneButton.setEnabled(false);
    }
    
    public void setDoneButton(boolean bool){
    	doneButton.setEnabled(bool);
    }
    protected void initializeValues() {
        
    }

    protected void initializeLayout() {
        // set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("800px");
        setHeight("540px");
        setResizable(false);
        setCaption("Select Germplasm List");
        // center window within the browser
        center();
        
        buttonArea.setMargin(false, true, false, true);
        buttonArea.setSpacing(true);
        
        buttonArea.addComponent(doneButton);
        buttonArea.addComponent(cancelButton);
        
        mainLayout.addComponent(selectGermplasmList);
        mainLayout.addComponent(buttonArea);
        mainLayout.setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
        
        this.setContent(mainLayout);
    }
    
    protected void initializeActions() {
        doneButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction());
        cancelButton.addListener(new CloseWindowAction());
    }
    
    
    
    // called by SelectListButtonClickListener for the "Done" button
    public void populateParentList() {
        // retrieve list entries and add them to the parent ListSelect component
    	
    	if(isTestEntry){
    		((SpecifyGermplasmsComponent)source).addTestGermplasmList(selectGermplasmList.getListInfoComponent().getGermplasmListId());
    	}else{
    		((SpecifyGermplasmsComponent)source).addStandardGermplasmList(selectGermplasmList.getListInfoComponent().getGermplasmListId());
    	}
    	Table listEntryValues = selectGermplasmList.getListInfoComponent().getEntriesTable();
        // remove existing list entries if selected list has entries
        if (listEntryValues.size() == 0) {
            doneButton.setEnabled(false);
        }else{
        	doneButton.setEnabled(true);
        }
        
    	/*
        Table listEntryValues = selectGermplasmList.getListInfoComponent().getEntriesTable();
        // remove existing list entries if selected list has entries
        if (listEntryValues.size() > 0) {
            parentList.removeAllItems();
        }
        
        for (Iterator<?> i = listEntryValues.getItemIds().iterator(); i.hasNext();) {
            // retrieve entries from the table
            Integer listDataId = (Integer) i.next();
            Item item = listEntryValues.getItem(listDataId);
            Integer entryId = (Integer) item.getItemProperty(SelectGermplasmListInfoComponent.ENTRY_ID).getValue();
            Integer gid = (Integer) item.getItemProperty(SelectGermplasmListInfoComponent.GID).getValue();
            String designation = (String) item.getItemProperty(SelectGermplasmListInfoComponent.DESIGNATION).getValue();
            
            // add entries to the parent ListSelect
            GermplasmListEntry entry = new GermplasmListEntry(listDataId, gid, entryId, designation);
            parentList.addItem(entry);
            String itemCaption = entry.getEntryId()+" -> "+entry.getDesignation(); 
            parentList.setItemCaption(entry, itemCaption);
        }
        */
        // remember selected List ID 
        //listId = listEntryValues.getData();
        /*
        if (listId != null && makeCrossesComponent != null) {
            makeCrossesComponent.setLastOpenedListId((Integer) listId);
        }
        */
        //parentList.requestRepaint();
        /*
        if(nurseryTemplateCall){
            setValuesOnGermplasmNurseryConditionGermplasmList();
        }
        */
        //this.listnameParent.setValue(selectGermplasmList.getListInfoComponent().getListName());
        
    }
    /*
    private void setValuesOnGermplasmNurseryConditionGermplasmList() {
    if(germplasmListFor.equals("Female")){
        nurseryTemplateConditionComponent.getFemaleListId().setValue(String.valueOf(listId));
        nurseryTemplateConditionComponent.getFemaleListName().setValue(selectGermplasmList.getListInfoComponent().getListName());
    }else{
        nurseryTemplateConditionComponent.getMaleListId().setValue(String.valueOf(listId));
        nurseryTemplateConditionComponent.getMaleListName().setValue(selectGermplasmList.getListInfoComponent().getListName());
    }
    }
	*/
    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(cancelButton, Message.CLOSE_SCREEN_LABEL);
        messageSource.setCaption(doneButton, Message.ADD_LIST_ENTRY);
    }


}

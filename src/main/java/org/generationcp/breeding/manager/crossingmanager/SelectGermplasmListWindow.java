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
package org.generationcp.breeding.manager.crossingmanager;

import java.util.Iterator;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crosses.NurseryTemplateConditionsComponent;
import org.generationcp.breeding.manager.crossingmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectListButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
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
public class SelectGermplasmListWindow extends Window implements InitializingBean, InternationalizableComponent {

    /**
     * 
     */
    private static final long serialVersionUID = -8113004135173349534L;
    
    public final static String CANCEL_BUTTON_ID = "SelectGermplasmListWindow Cancel Button";
    public final static String DONE_BUTTON_ID = "SelectGermplasmListWindow Done Button";
    
    private VerticalLayout mainLayout;
    private SelectGermplasmListComponent selectGermplasmList;
    private Button cancelButton;
    private Button doneButton;
    private HorizontalLayout buttonArea;
    
    private ListSelect parentList;
    private CrossingManagerMakeCrossesComponent makeCrossesComponent;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private Object listId;

    private NurseryTemplateConditionsComponent nurseryTemplateConditionComponent;

    private String germplasmListFor;

    private boolean nurseryTemplateCall=false;

    private Label listnameParent;

    public SelectGermplasmListWindow() {
        this.parentList = new ListSelect();
        this.makeCrossesComponent = null;
    }
    
    public SelectGermplasmListWindow(ListSelect parentList, CrossingManagerMakeCrossesComponent makeCrossesComponent,Label listnameParent) {
        this.parentList = parentList;
        this.makeCrossesComponent = makeCrossesComponent;
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

    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        mainLayout = new VerticalLayout();
        Integer lastOpenedId;
        if (makeCrossesComponent != null) {
            lastOpenedId = makeCrossesComponent.getLastOpenedListId();
        } else {
            lastOpenedId = null;
        }
        selectGermplasmList = new SelectGermplasmListComponent(lastOpenedId);
        
        buttonArea = new HorizontalLayout();
        cancelButton = new Button(); // "Cancel"
        cancelButton.setData(CANCEL_BUTTON_ID);
        doneButton = new Button(); // "Done"
        doneButton.setData(DONE_BUTTON_ID);
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
        buttonArea.addComponent(cancelButton);
        buttonArea.addComponent(doneButton);
        
        mainLayout.addComponent(selectGermplasmList);
        mainLayout.addComponent(buttonArea);
        mainLayout.setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
        
        this.setContent(mainLayout);
    }
    
    protected void initializeActions() {
        doneButton.addListener(new SelectListButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction());
        cancelButton.addListener(new CloseWindowAction());
    }
    
    // called by SelectListButtonClickListener for the "Done" button
    public void populateParentList() {
        // retrieve list entries and add them to the parent ListSelect component
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
        // remember selected List ID 
        listId = listEntryValues.getData();
        if (listId != null && makeCrossesComponent != null) {
            makeCrossesComponent.setLastOpenedListId((Integer) listId);
        }
        
        parentList.requestRepaint();
        
        if(nurseryTemplateCall){
            setValuesOnGermplasmNurseryConditionGermplasmList();
        }
        
        this.listnameParent.setValue(selectGermplasmList.getListInfoComponent().getListName());
        
    }
    
    private void setValuesOnGermplasmNurseryConditionGermplasmList() {
    if(germplasmListFor.equals("Female")){
        nurseryTemplateConditionComponent.getFemaleListId().setValue(String.valueOf(listId));
        nurseryTemplateConditionComponent.getFemaleListName().setValue(selectGermplasmList.getListInfoComponent().getListName());
    }else{
        nurseryTemplateConditionComponent.getMaleListId().setValue(String.valueOf(listId));
        nurseryTemplateConditionComponent.getMaleListName().setValue(selectGermplasmList.getListInfoComponent().getListName());
    }
    }

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
        messageSource.setCaption(cancelButton, Message.CANCEL_LABEL);
        messageSource.setCaption(doneButton, Message.DONE_LABEL);
    }


}

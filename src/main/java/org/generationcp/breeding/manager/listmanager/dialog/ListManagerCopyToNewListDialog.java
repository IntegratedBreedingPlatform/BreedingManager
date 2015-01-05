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

package org.generationcp.breeding.manager.listmanager.dialog;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class ListManagerCopyToNewListDialog extends VerticalLayout implements InitializingBean, InternationalizableComponent,
			 Property.ValueChangeListener, AbstractSelect.NewItemHandler, BreedingManagerLayout{

    private static final Logger LOG = LoggerFactory.getLogger(ListManagerCopyToNewListDialog.class);
    private static final long serialVersionUID = 1L;
    
    private static final String FOLDER_TYPE = "FOLDER";
        
    public static final Object SAVE_BUTTON_ID = "Save New List Entries";
    public static final String CANCEL_BUTTON_ID = "Cancel Copying New List Entries";
    public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
    
    private Label labelListName;
    private Label labelDescription;
    private ComboBox comboBoxListName;
    private TextField txtDescription;
    private Label labelType;
    private TextField txtType;
    private Window dialogWindow;
    private Window mainWindow;
    private Button btnSave;
    private Button btnCancel;
    private Select selectType;
    private Table listEntriesTable;
    private String listName;
    private String designationOfListEntriesCopied;
    private int newListid;
    private String listNameValue;
    private int ibdbUserId;
    private List<GermplasmList> germplasmList;
    private Map<String, Integer> mapExistingList;
    private boolean lastAdded = false;
    private boolean existingListSelected = false;
    private boolean fromBuildNewList = false;
    private Set<String> localFolderNames = new HashSet<String>();

    @Autowired
    private GermplasmListManager germplasmListManager;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    
    private ListManagerMain listManagerMain;
    
    public ListManagerCopyToNewListDialog(Window mainWindow, Window dialogWindow,String listName, 
    		Table listEntriesTable, int ibdbUserId, ListManagerMain listManagerMain) {
        this.dialogWindow = dialogWindow;
        this.mainWindow = mainWindow;
        this.listEntriesTable = listEntriesTable;
        this.listName = listName;
        this.ibdbUserId = ibdbUserId;
        this.listManagerMain = listManagerMain;
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
		labelListName = new Label(messageSource.getMessage(Message.LIST_NAME_LABEL));
        labelListName.addStyleName("bold");
        
        labelDescription = new Label(messageSource.getMessage(Message.DESCRIPTION_LABEL));
        labelDescription.addStyleName("bold");
        
        labelType = new Label(messageSource.getMessage(Message.TYPE_LABEL));
        labelType.addStyleName("bold");
        
        comboBoxListName = new ComboBox();
        comboBoxListName.setNewItemsAllowed(true);
        comboBoxListName.setNewItemHandler(this);
        comboBoxListName.setNullSelectionAllowed(false);
        comboBoxListName.setImmediate(true);
        comboBoxListName.focus();

        txtDescription = new TextField();
        txtDescription.setWidth("400px");
        
        txtType = new TextField();
        txtType.setWidth("200px");
        
        selectType = new Select();
        selectType.setNullSelectionAllowed(false);
        
        btnSave = new Button(messageSource.getMessage(Message.SAVE_LABEL));
        btnSave.setWidth("80px");
        btnSave.setData(SAVE_BUTTON_ID);
        btnSave.setDescription("Save New Germplasm List ");
        btnSave.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        btnCancel = new Button(messageSource.getMessage(Message.CANCEL));
        btnCancel.setWidth("80px");
        btnCancel.setData(CANCEL_BUTTON_ID);
        btnCancel.setDescription("Cancel Saving New Germplasm List");
	    btnCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
    }

	@Override
	public void initializeValues() {
		populateComboBoxListName();
		populateSelectType(selectType);
	}

	@Override
	public void addListeners() {
		comboBoxListName.addListener(this);
		btnSave.addListener(new GermplasmListButtonClickListener(this));
		btnCancel.addListener(new GermplasmListButtonClickListener(this));
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		
		HorizontalLayout hButton = new HorizontalLayout();
        hButton.setSpacing(true);
        hButton.addComponent(btnCancel);
        hButton.addComponent(btnSave);
        
		GridLayout gridLayout = new GridLayout();
        gridLayout.setRows(3);
        gridLayout.setColumns(2);
        gridLayout.setSpacing(true);
        gridLayout.addComponent(labelListName, 0, 0);
        gridLayout.addComponent(comboBoxListName, 1, 0);
        gridLayout.addComponent(labelDescription, 0, 1);
        gridLayout.addComponent(txtDescription, 1, 1);
        gridLayout.addComponent(labelType, 0, 2);
        gridLayout.addComponent(selectType, 1, 2);
        
        addComponent(gridLayout);
        addComponent(hButton);
        setComponentAlignment(hButton, Alignment.MIDDLE_CENTER);
	}


    private void populateSelectType(Select selectType) {
        List<UserDefinedField> listTypes;
		try {
			listTypes = this.germplasmListManager.getGermplasmListTypes();
			
			for (UserDefinedField listType : listTypes) {
	            String typeCode = listType.getFcode();
	            selectType.addItem(typeCode);
	            selectType.setItemCaption(typeCode, listType.getFname());
	            //set "GERMPLASMLISTS" as the default value
	            if ("LST".equals(typeCode)) {
	                selectType.setValue(typeCode);
	            }
	        }
			
		} catch (MiddlewareQueryException e) {
			LOG.error(e.toString());
            LOG.error("\n" + e.getStackTrace());
		}
    }

    @Override
    public void updateLabels() {
        //not needed to override for now
    }
    
    private void populateComboBoxListName() {
        try {
			germplasmList = germplasmListManager.getAllGermplasmLists(0, (int) germplasmListManager.countAllGermplasmLists(), Database.LOCAL);
	        mapExistingList = new HashMap<String, Integer>();
	        comboBoxListName.addItem("");
	        for (GermplasmList gList : germplasmList) {
	            if(!gList.getName().equals(listName)){
	                if(!gList.getType().equals(FOLDER_TYPE)){
	                    comboBoxListName.addItem(gList.getName());
	                    mapExistingList.put(gList.getName(), new Integer(gList.getId()));
	                } else{
	                    localFolderNames.add(gList.getName());
	                }
	            }
	        }
	        comboBoxListName.select("");
	        
		} catch (MiddlewareQueryException e) {
			LOG.error(e.toString());
            LOG.error("\n" + e.getStackTrace());
		}
    }

    public void saveGermplasmListButtonClickAction() throws InternationalizableException, NumberFormatException {

        listNameValue = comboBoxListName.getValue().toString();
        String description=txtDescription.getValue().toString();
        
        Boolean proceedWithSave = true;
        
        try {
            Long matchingNamesCountOnCentral = germplasmListManager.countGermplasmListByName(listNameValue, Operation.EQUAL, Database.CENTRAL);
            String existingListMsg = "There is already an existing germplasm list with that name";
            if(matchingNamesCountOnCentral>0){
            	MessageNotifier.showRequiredFieldError(getWindow(), existingListMsg);
                proceedWithSave = false;
            }
            
            // if list name from copy source is equal to specified value in combo box
            if (!"".equals(listNameValue) && listName.equals(listNameValue)) {
            	MessageNotifier.showRequiredFieldError(getWindow(), existingListMsg);
                proceedWithSave = false; 
            }
            
            if(localFolderNames.contains(listNameValue)){
            	MessageNotifier.showRequiredFieldError(getWindow(), "There is already an existing germplasm list folder with that name");
                proceedWithSave = false;
            }
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in counting germplasm list by name.", e);
            LOG.error("\n" + e.getStackTrace());
        }
        
        if(proceedWithSave){
        
            if (listNameValue.trim().length() == 0) {
            	MessageNotifier.showRequiredFieldError(getWindow(), "Please specify a List Name before saving");
            } else if (listNameValue.trim().length() > 50) {
            	MessageNotifier.showRequiredFieldError(getWindow(), "Listname input is too large limit the name only up to 50 characters");
                comboBoxListName.setValue("");
            } else {
                
                if(!existingListSelected){
                    Date date = new Date();
                    Format formatter = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT);
                    Long currentDate = Long.valueOf(formatter.format(date));
                    GermplasmList parent = null;
                    int statusListName = 1;
                    GermplasmList listNameData = new GermplasmList(null, listNameValue, currentDate, selectType.getValue().toString(), ibdbUserId, description, parent, statusListName);
    
                    try {
                        newListid = germplasmListManager.addGermplasmList(listNameData);
                        try{
                            GermplasmList germList = germplasmListManager.getGermplasmListById(newListid);
                            addGermplasmListData(germList,1);
                            listManagerMain.getListSelectionComponent().getListTreeComponent().createTree();
                            //TODO must accommodate the expanding of the folder up to the parent of the list being opened
                            listManagerMain.getListSelectionComponent().getListTreeComponent().expandNode(ListTreeComponent.LISTS);
                            //TODO must accommodate opening in the search screen also
                            listManagerMain.getListSelectionComponent().getListTreeComponent().treeItemClickAction(newListid);
                        } catch (MiddlewareQueryException e){
                            germplasmListManager.deleteGermplasmListByListId(newListid);
                            LOG.error("Error with copying list entries", e);
                            MessageNotifier.showError(getWindow().getParent().getWindow(), "Error with copying list entries."
                                , "Copying of entries to a new list failed. " + messageSource.getMessage(Message.ERROR_REPORT_TO));
                        }
                        this.mainWindow.removeWindow(dialogWindow);
    
                    } catch (MiddlewareQueryException e) {
                        LOG.error("Error with copying list entries", e);
                        LOG.error("\n" + e.getStackTrace());
                        MessageNotifier.showError(this.getWindow().getParent().getWindow() 
                            , messageSource.getMessage(Message.UNSUCCESSFUL) 
                            , messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_FAILED));
                    }
                } else {
                
                    try {
                        String listId = String.valueOf(mapExistingList.get(comboBoxListName.getValue()));
                        GermplasmList  germList = germplasmListManager.getGermplasmListById(Integer.valueOf(listId));
                        int countOfExistingList=(int) germplasmListManager.countGermplasmListDataByListId(Integer.valueOf(listId));
                        addGermplasmListData(germList,countOfExistingList+1);
                        this.mainWindow.removeWindow(dialogWindow);
                        
                        listManagerMain.getListSelectionComponent().getListTreeComponent().createTree();
                        //TODO must accommodate the expanding of the folder up to the parent of the list being opened
                        listManagerMain.getListSelectionComponent().getListTreeComponent().expandNode(ListSelectorComponent.LISTS);
                        //TODO must accommodate opening in the search screen also
                        listManagerMain.getListSelectionComponent().getListDetailsLayout().removeTab(Integer.valueOf(listId));
                        listManagerMain.getListSelectionComponent().getListTreeComponent().treeItemClickAction(Integer.valueOf(listId));
                    } catch (MiddlewareQueryException e) {
                        LOG.error("Error with copying list entries", e);
                        LOG.error("\n" + e.getStackTrace());
                            MessageNotifier.showError(this.getWindow().getParent().getWindow() 
                                , messageSource.getMessage(Message.UNSUCCESSFUL) 
                                , messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_EXISTING_LIST_FAILED));
                    }
                }
            }
        }
    }

    private void addGermplasmListData(GermplasmList germList,int entryid) throws MiddlewareQueryException {
        int status = 0;
        int localRecordId = 0;
        designationOfListEntriesCopied="";
        Collection<?> selectedIds = (Collection<?>)listEntriesTable.getValue();
        for (final Object itemId : selectedIds) {
            Property pParentage = listEntriesTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.PARENTAGE.getName());
            Property pEntryId = listEntriesTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName());
            Property pGid= listEntriesTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GID.getName());
            Property pDesignation= listEntriesTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName());

            Button pGidButton = (Button) pGid.getValue();
            int gid=Integer.valueOf(pGidButton.getCaption().toString());
            String entryIdOfList=String.valueOf(pEntryId.getValue().toString());
            String seedSource=listName+": "+entryIdOfList;
            Button pDesigButton = (Button) pDesignation.getValue();
            String designation=String.valueOf(pDesigButton.getCaption().toString());
            designationOfListEntriesCopied+=designation+",";
            String groupName=String.valueOf(pParentage.getValue().toString());

            GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryIdOfList, seedSource,
                designation, groupName, status, localRecordId);
            germplasmListManager.addGermplasmListData(germplasmListData);
            
            entryid++;
        }
        
        designationOfListEntriesCopied=designationOfListEntriesCopied.substring(0,designationOfListEntriesCopied.length()-1);

        MessageNotifier.showMessage(this.getWindow().getParent().getWindow() 
            ,messageSource.getMessage(Message.SUCCESS)
            ,messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_SUCCESS),3000);

        logCopyToNewListEntriesToWorkbenchProjectActivity();    
    }

    private void logCopyToNewListEntriesToWorkbenchProjectActivity() throws MiddlewareQueryException {
        User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());

        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                "Copied entries into a new list.", 
                "Copied entries to list " +newListid+  " - " + listNameValue,user,new Date());
        try {
            workbenchDataManager.addProjectActivity(projAct);    
        } catch (MiddlewareQueryException e) {
            LOG.error("Error with logging workbench activity.", e);
            LOG.error("\n" + e.getStackTrace());
        }
    }

    public void cancelGermplasmListButtonClickAction() {
        this.mainWindow.removeWindow(dialogWindow);
    }

    @Override
    public void addNewItem(String newItemCaption) {
        if (!comboBoxListName.containsId(newItemCaption)) {
            if (comboBoxListName.containsId("")) {
                comboBoxListName.removeItem("");
            }
            lastAdded = true;
            comboBoxListName.addItem(newItemCaption);
            comboBoxListName.setValue(newItemCaption);
        }
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        if (!lastAdded) {
            try {
                String listNameId = String.valueOf(mapExistingList.get(comboBoxListName.getValue()));
                if (listNameId != "null") {
                    GermplasmList gList = germplasmListManager.getGermplasmListById(Integer.valueOf(listNameId));
                    txtDescription.setValue(gList.getDescription());
                    txtDescription.setEnabled(false);
                    selectType.select(gList.getType());
                    selectType.setEnabled(false);
                    this.existingListSelected = true;
                } else {
                    txtDescription.setValue("");
                    txtDescription.setEnabled(true);
                    selectType.select("LST");
                    selectType.setEnabled(true);
                }
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in retrieving germplasm list.", e);
                LOG.error("\n" + e.getStackTrace());
                MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                        messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LIST_BY_ID));
            }
        } else {
            if (existingListSelected) {
                txtDescription.setValue("");
                existingListSelected = false;
            }
            txtDescription.setEnabled(true);
            selectType.select("LST");
            selectType.setEnabled(true);
        }
        lastAdded = false;
    }
}
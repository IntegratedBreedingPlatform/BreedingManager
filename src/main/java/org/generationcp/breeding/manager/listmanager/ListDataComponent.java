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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialogSource;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.AddColumnContextMenu;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.breeding.manager.listmanager.util.ListDataPropertiesRenderer;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListDataComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, AddEntryDialogSource  {

	private static final long serialVersionUID = -2847082090222842504L;
	private static final Logger LOG = LoggerFactory.getLogger(ListDataComponent.class);
	private static final int MINIMUM_WIDTH = 10;

    public final static String SORTING_BUTTON_ID = "GermplasmListDataComponent Save Sorting Button";
    public static final String DELETE_LIST_ENTRIES_BUTTON_ID="Delete list entries";
    public final static String EXPORT_BUTTON_ID = "GermplasmListDataComponent Export List Button";
    public final static String EXPORT_FOR_GENOTYPING_BUTTON_ID = "GermplasmListDataComponent Export For Genotyping Order Button";
    public final static String COPY_TO_NEW_LIST_BUTTON_ID = "GermplasmListDataComponent Copy to New List Button";
    public final static String ADD_ENTRIES_BUTTON_ID = "GermplasmListDataComponent Add Entries Button";
    
    private ListManagerTreeMenu source;
    private Table listDataTable;
    private int germplasmListId;
    private String listName;
    private List<GermplasmListData> listDatas;
    private String designationOfListEntriesDeleted="";
    
    private String CHECKBOX_COLUMN_ID="Checkbox Column ID";
    private String MENU_SELECT_ALL="Select All"; 
    private String MENU_EXPORT_LIST="Export List"; 
    private String MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER="Export List for Genotyping Order"; 
    private String MENU_COPY_TO_NEW_LIST="Copy List Entries"; 
    private String MENU_ADD_ENTRY="Add Entry"; 
    private String MENU_SAVE_CHANGES="Save Changes"; 
    private String MENU_DELETE_SELECTED_ENTRIES="Delete Selected Entries";
    private String MENU_EDIT_LIST="Edit List";
    
    
    static final Action ACTION_SELECT_ALL = new Action("Select All");
    static final Action ACTION_DELETE = new Action("Delete selected entries");
    static final Action ACTION_EDIT = new Action("Edit Value");
    static final Action ACTION_COPY_TO_NEW_LIST= new Action("Copy to new list");
    static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE, ACTION_EDIT, ACTION_COPY_TO_NEW_LIST };
    static final Action[] ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_EDIT = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE, ACTION_COPY_TO_NEW_LIST };
    static final Action[] ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_DELETE = new Action[] { ACTION_SELECT_ALL, ACTION_COPY_TO_NEW_LIST };
    
    public static String LIST_DATA_COMPONENT_TABLE_DATA = "List Data Component Table";
    
    public ListManagerTreeMenu listManagerTreeMenu;

    private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private PedigreeDataManager pedigreeDataManager;
    
    private ListManagerMain listManagerMain;
    
    private boolean forGermplasmListWindow;
    private Integer germplasmListStatus;
    private GermplasmList germplasmList;
	private int germplasListUserId;
	private Button toolsButton;
	private Button addColumnButton;
	private ContextMenu menu;
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuExportForGenotypingOrder;
	private ContextMenuItem menuCopyToList;
	private ContextMenuItem menuAddEntry;
	private ContextMenuItem menuSaveChanges;
	private ContextMenuItem menuDeleteEntries;
	private ContextMenuItem menuEditList;
	private AbsoluteLayout toolsMenuBar;
	private Label noListDataLabel;
	private Label totalListEntries;

	private final HashMap<Object,HashMap<Object,Field>> fields = new HashMap<Object,HashMap<Object,Field>>();      
	
	private Window listManagerCopyToNewListDialog;
	private static final ThemeResource ICON_TOOLS = new ThemeResource("images/tools.png");
	private static final ThemeResource ICON_PLUS = new ThemeResource("images/plus_icon.png");
	public static final String USER_HOME = "user.home";
	public static String TOOLS_BUTTON_ID = "Tools";
	private static String TOOLS_TOOLTIP = "Tools";

	private AddColumnContextMenu addColumnContextMenu;  
	private String lastCellvalue = "";
	private long listDataCount;
	
	private CheckBox tagAllCheckBox;
	private Boolean tagAllWasJustClicked = false;
	  
	Object selectedColumn = "";
	Object selectedItemId;
	
	private BuildNewListComponent buildNewListComponent;
	
	private Map<Object, String> itemsToDelete;
	private ArrayList<Integer> gidsWithoutChildren;
	private ArrayList<Integer> gidsWithoutChildrenToDelete;
	
    public ListDataComponent(ListManagerTreeMenu source, int germplasmListId,String listName,int germplasListUserId, boolean fromUrl,boolean forGermplasmListWindow, Integer germplasmListStatus,ListManagerTreeMenu listManagerTreeMenu, ListManagerMain listManagerMain){
    	this.source = source;
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.germplasListUserId=germplasListUserId;
        this.forGermplasmListWindow=forGermplasmListWindow;
        this.germplasmListStatus=germplasmListStatus;
        this.listManagerTreeMenu = listManagerTreeMenu;
        this.listManagerMain = listManagerMain;
    }

    @Override
    public void afterPropertiesSet() throws Exception{
    	listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
    	
    	setHeight("315px");
    	
		menu = new ContextMenu();
		menu.setWidth("255px");
		
		// Generate main level items
		menu.addItem(MENU_SELECT_ALL);
		menuExportList = menu.addItem(MENU_EXPORT_LIST);
		menuExportForGenotypingOrder = menu.addItem(MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER);
		menuCopyToList = menu.addItem(MENU_COPY_TO_NEW_LIST);
		menuAddEntry = menu.addItem(MENU_ADD_ENTRY);
		menuSaveChanges = menu.addItem(MENU_SAVE_CHANGES);
		menuDeleteEntries = menu.addItem(MENU_DELETE_SELECTED_ENTRIES);
		menuEditList = menu.addItem(MENU_EDIT_LIST);
		
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;

			public void contextItemClick(ClickEvent event) {
			      // Get reference to clicked item
			      ContextMenuItem clickedItem = event.getClickedItem();
			      if(clickedItem.getName().equals(MENU_SELECT_ALL)){
			    	  listDataTable.setValue(listDataTable.getItemIds());
			      }else if(clickedItem.getName().equals(MENU_EXPORT_LIST)){
			    	  exportListAction();
			      }else if(clickedItem.getName().equals(MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER)){
			    	  exportListForGenotypingOrderAction();
			      }else if(clickedItem.getName().equals(MENU_COPY_TO_NEW_LIST)){
			    	  copyToNewListAction();
			      }else if(clickedItem.getName().equals(MENU_ADD_ENTRY)){	  
			    	  addEntryButtonClickAction();
			      }else if(clickedItem.getName().equals(MENU_SAVE_CHANGES)){	  
			    	  saveChangesAction();
			      }else if(clickedItem.getName().equals(MENU_DELETE_SELECTED_ENTRIES)){	 
			    	  deleteListButtonClickAction();
			      }else if(clickedItem.getName().equals(MENU_EDIT_LIST)){
			    	  editListButtonClickAction();
			      }
			      
			   }
			});
 
    	 toolsButton = new Button(messageSource.getMessage(Message.TOOLS));
    	 toolsButton.setData(TOOLS_BUTTON_ID);
    	 toolsButton.setIcon(ICON_TOOLS);
    	 toolsButton.setWidth("100px");
    	 toolsButton.setDescription(TOOLS_TOOLTIP);
    	 toolsButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
    	 toolsButton.addListener(new GermplasmListButtonClickListener(this, germplasmList));
 		
    	 toolsButton.addListener(new ClickListener() {
    		 private static final long serialVersionUID = 272707576878821700L;

			 @Override
    		 public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    			 menu.show(event.getClientX(), event.getClientY());
    			 
    			 if(fromUrl){
    			  menuExportForGenotypingOrder.setVisible(false);
    			  menuExportList.setVisible(false);
    			  menuCopyToList.setVisible(false);
    			 }
    			 
    			// Show "Save Sorting" button only when Germplasm List open is a local IBDB record (negative ID).
                 // and when not accessed directly from URL or popup window
    			 if (germplasmListId < 0
    					 && !fromUrl) {
    				 if(germplasmListStatus>=100){
    					 menuEditList.setVisible(false);
    					 menuDeleteEntries.setVisible(false);
    					 menuSaveChanges.setVisible(false);
    					 menuAddEntry.setVisible(false);
    				 }else{
    					 menuEditList.setVisible(true);
    					 menuDeleteEntries.setVisible(true); 
    					 menuSaveChanges.setVisible(true);
    					 menuAddEntry.setVisible(true);
    				 }
		 
    			 }else{
    				 menuEditList.setVisible(false);
    				 menuDeleteEntries.setVisible(false);
					 menuSaveChanges.setVisible(false);
					 menuAddEntry.setVisible(false);
    			 }

    		 }
    	 });
    	 listManagerTreeMenu.addComponent(menu);
    	 
    	 toolsMenuBar = new AbsoluteLayout();
    	 toolsMenuBar.setWidth("100%");
    	 toolsMenuBar.setHeight("30px");
       	 toolsMenuBar.addComponent(toolsButton, "top:0px; right:42px;");
   	 
    	 addComponent(toolsMenuBar, "top:0px; left:0px;");
    	 
    	 listDatas = new ArrayList<GermplasmListData>();

     	 // "No Germplasm List Data retrieved."
         if (listDataCount == 0) {
            noListDataLabel = new Label(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
            noListDataLabel.setWidth("250px");
			addComponent(noListDataLabel); 
         } else {
        	 totalListEntries = new Label("<b>" + messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ":</b> " 
        			 + "  " + listDataCount, Label.CONTENT_XHTML);
        	 totalListEntries.setWidth("150px");
        	 toolsMenuBar.addComponent(totalListEntries,"top:4px; left:0px;");
        	 initializeListDataTable(toolsMenuBar);    	 
             
         }
         
         //initialize the list for items that will be deleted later;
         itemsToDelete = new HashMap<Object, String>();
         this.gidsWithoutChildrenToDelete = new ArrayList<Integer>();
    }

    private void syncItemCheckBoxes(){
    	List<Integer> itemIds = getItemIds(listDataTable);
    	for(Integer itemId : itemIds){
    		CheckBox itemCheckBox = (CheckBox) listDataTable.getItem(itemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
    		itemCheckBox.setValue(false);
    	}
    	List<Integer> selectedItemIds = getSelectedItemIds(listDataTable);
    	for(Integer itemId : selectedItemIds){
    		CheckBox itemCheckBox = (CheckBox) listDataTable.getItem(itemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
    		itemCheckBox.setValue(true);
    		
    	}

    	if(tagAllWasJustClicked || listDataTable.getValue().equals(listDataTable.getItemIds())){
    		tagAllCheckBox.setValue(true);
    	} else {
			tagAllCheckBox.setValue(false);
		}
		
    	tagAllWasJustClicked = false;
    }
    
	/**
	 * Iterates through the whole table, gets selected item ID's, make sure it's sorted as seen on the UI
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getSelectedItemIds(Table table){
		List<Integer> itemIds = new ArrayList<Integer>();
		List<Integer> selectedItemIds = new ArrayList<Integer>();
		List<Integer> trueOrderedSelectedItemIds = new ArrayList<Integer>();
		
    	selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
    	itemIds = getItemIds(table);
        	
    	for(Integer itemId: itemIds){
    		if(selectedItemIds.contains(itemId)){
    			trueOrderedSelectedItemIds.add(itemId);
    		}
    	}
    	
    	return trueOrderedSelectedItemIds;
    }
    
	/**
	 * Get item id's of a table, and return it as a list 
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(Table table){
		List<Integer> itemIds = new ArrayList<Integer>();
    	itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
    	return itemIds;
	}
		
	
	private void initializeListDataTable(AbsoluteLayout toolsMenuBar)
			throws MiddlewareQueryException {
		// create the Vaadin Table to display the Germplasm List Data
		
		 listDataTable = new Table("");
		 listDataTable.setSelectable(true);
		 listDataTable.setMultiSelect(true);
		 listDataTable.setColumnCollapsingAllowed(true);
		 listDataTable.setColumnReorderingAllowed(true);
//             listDataTable.setPageLength(15); // number of rows to display in the Table
		 listDataTable.setWidth("94%");
		 listDataTable.setHeight("250px");
		 listDataTable.setDragMode(TableDragMode.ROW);
		 listDataTable.setData(LIST_DATA_COMPONENT_TABLE_DATA);
		 listDataTable.setColumnReorderingAllowed(false);
		 
         listDataTable.addListener(new Table.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
                 syncItemCheckBoxes();
             }
         });
                 
         
		 if(!fromUrl){
		         listDataTable.addActionHandler(new Action.Handler() {
		        	 private static final long serialVersionUID = -897257270314381555L;

					public Action[] getActions(Object target, Object sender) {
		             if (germplasmListId < 0 &&  germplasmListStatus < 100){
		            	 if(selectedColumn == null){
		            		 return ACTIONS_TABLE_CONTEXT_MENU;
		            	 }
		            	 else {
		            		 if( selectedColumn.toString().equals(ListDataTablePropertyID.GID.getName()) || selectedColumn.toString().equals(ListDataTablePropertyID.ENTRY_ID.getName()) ){
		                		 return ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_EDIT;
		            		 }
		            		 else{
		                		 return ACTIONS_TABLE_CONTEXT_MENU;
		                	 } 
		            	 }
		             }else{
		            	 return ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_DELETE;
		             }
		         }
       
		         @SuppressWarnings("unchecked")
		         public void handleAction(Action action, Object sender, Object target) {
		         	if (ACTION_DELETE == action) {
		         		deleteListButtonClickAction();
		         	}else if(ACTION_SELECT_ALL == action) {
		         		listDataTable.setValue(listDataTable.getItemIds());
		         	}else if(ACTION_EDIT == action){
		         		// Make the entire item editable
		                HashMap<Object,Field> itemMap = fields.get(selectedItemId);
		                if(itemMap != null){
			                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
			        			Object column = entry.getKey();
			        			if(column.equals(selectedColumn)){
			        				Field f = entry.getValue();
			        				Object fieldValue = f.getValue();
									lastCellvalue = (fieldValue != null)? fieldValue.toString() : "";
				                	f.setReadOnly(false);
				                	f.focus();
			        			}
			                }
		                }
		                
		                listDataTable.select(selectedItemId);
		         	}else if(ACTION_COPY_TO_NEW_LIST == action){
		         		listManagerMain.showBuildNewListComponent();
		         		List<Integer> gids = listManagerMain.getBuildListComponent().getSelectedGids(listDataTable, ListDataTablePropertyID.GID.getName());
	         			listManagerMain.getBuildListComponent().addGermplasmToGermplasmTable(listDataTable, null);
		         	}
		         	
		         }
		         });
		 }

		 
		 listDataTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		 
		 //make GID as link only if the page wasn't directly accessed from the URL
		 if (!fromUrl) {
		     listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		 } else {
		     listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Integer.class, null);
		 }

		 listDataTable.addContainerProperty(ListDataTablePropertyID.GID_VALUE.getName(), Integer.class, null);
		 listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		 listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		 listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		 listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), String.class, null);
		 listDataTable.addContainerProperty(ListDataTablePropertyID.GROUP_NAME.getName(), String.class, null);
//             listDataTable.addContainerProperty(ListDataTablePropertyID.STATUS.getName(), String.class, null);
       
//		 listDataTable.setColumnWidth(CHECKBOX_COLUMN_ID, 30);
//		 listDataTable.setColumnWidth(ListDataTablePropertyID.GID.getName(), 70);
//		 listDataTable.setColumnWidth(ListDataTablePropertyID.ENTRY_ID.getName(), 100);
//		 listDataTable.setColumnWidth(ListDataTablePropertyID.ENTRY_CODE.getName(), 130);
//		 listDataTable.setColumnWidth(ListDataTablePropertyID.SEED_SOURCE.getName(), 450);
//		 listDataTable.setColumnWidth(ListDataTablePropertyID.DESIGNATION.getName(), 250);
//		 listDataTable.setColumnWidth(ListDataTablePropertyID.GROUP_NAME.getName(), 250);
		 
		 messageSource.setColumnHeader(listDataTable, CHECKBOX_COLUMN_ID, Message.TAG);
		 messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
		 messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.ENTRY_ID.getName(), Message.LISTDATA_ENTRY_ID_HEADER);
		 messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
		 messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
		 messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
		 messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.GROUP_NAME.getName(), Message.LISTDATA_GROUPNAME_HEADER);
//             messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.STATUS.getName(), Message.LISTDATA_STATUS_HEADER);
		 
		 populateTable();
		 
		 if(germplasmListId < 0){
		     if(germplasmListId<0 && germplasmListStatus<100){
		         @SuppressWarnings("unused")
				FillWith fillWith = new FillWith(listManagerTreeMenu, messageSource, 
				        listDataTable, ListDataTablePropertyID.GID.getName());
		     }
		 }

		 
		 tagAllCheckBox = new CheckBox();
		 tagAllCheckBox.setImmediate(true);
		 tagAllCheckBox.setCaption(messageSource.getMessage(Message.SELECT_ALL));
		 tagAllCheckBox.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				if(((Boolean) tagAllCheckBox.getValue()).equals(true)){
					tagAllWasJustClicked = true;
					listDataTable.setValue(listDataTable.getItemIds());
				} else {
					listDataTable.setValue(null);
				}
			}
		 });
		 
		 addComponent(tagAllCheckBox, "top:295px; left:3px;"); 
		 addComponent(listDataTable, "top:35px; left:0px;");
		 
		 if(germplasmListId<0 && germplasmListStatus<100){
		     addColumnButton = new Button();
		     addColumnButton.setCaption(messageSource.getMessage(Message.ADD_COLUMN));
		     addColumnButton.setIcon(ICON_PLUS);
		     addColumnButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
			 toolsMenuBar.addComponent(addColumnButton, "top:0px; right:152px;");
			 
			 addColumnContextMenu = new AddColumnContextMenu(listManagerTreeMenu, toolsMenuBar, addColumnButton, 
			         listDataTable, ListDataTablePropertyID.GID.getName());
			 
		 }
		 
	}

	// This is needed for storing back-references
	class ItemPropertyId {
	    Object itemId;
	    Object propertyId;
	    
	    public ItemPropertyId(Object itemId, Object propertyId) {
	        this.itemId = itemId;
	        this.propertyId = propertyId;
	    }
	    
	    public Object getItemId() {
	        return itemId;
	    }
	    
	    public Object getPropertyId() {
	        return propertyId;
	    }
	}
	
    public void makeTableEditable(){
    	
    	listDataTable.setImmediate(true);
    	
    	listDataTable.addListener(new ItemClickListener(){
			private static final long serialVersionUID = 1L;
			public void itemClick(ItemClickEvent event) {
				selectedColumn = event.getPropertyId();
				selectedItemId = event.getItemId();
			}
		});
    	
    	listDataTable.setTableFieldFactory(new TableFieldFactory() {
			private static final long serialVersionUID = 1L;

			public Field createField(Container container, final Object itemId,
		            final Object propertyId, Component uiContext) {
		    	
		    	if(propertyId.equals(ListDataTablePropertyID.GID.getName()) || propertyId.equals(ListDataTablePropertyID.ENTRY_ID.getName())){
		    		return null;
		    	}
		    	
		    	final TextField tf = new TextField();
		        tf.setData(new ItemPropertyId(itemId, propertyId));
		        
		        //set the size of textfield based on text of cell
		        String value = (String) container.getItem(itemId).getItemProperty(propertyId).getValue();
		        Double d = computeTextFieldWidth(value);
				tf.setWidth(d.floatValue(), UNITS_EM);
		        
		        // Needed for the generated column
		        tf.setImmediate(true);

		        // Manage the field in the field storage
		        HashMap<Object,Field> itemMap = fields.get(itemId);
		        if (itemMap == null) {
		            itemMap = new HashMap<Object,Field>();
		            fields.put(itemId, itemMap);
		        }
		        itemMap.put(propertyId, tf);
		        
		        tf.setReadOnly(true);
		        
		        tf.addListener(new FocusListener() {
					private static final long serialVersionUID = 1L;

					public void focus(FocusEvent event) {
		                // Make the entire item editable
		                HashMap<Object,Field> itemMap = fields.get(itemId);
		                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
		                	Object column = entry.getKey();
		        			if(column.equals(selectedColumn)){		        				
		        				Field f = entry.getValue();
			                	//f.setReadOnly(false);
		        			}
		                }
		                
		                listDataTable.select(itemId);
		            }
		        });
		        tf.addListener(new BlurListener() {
					private static final long serialVersionUID = 1L;

					public void blur(BlurEvent event) {

						/*Double d = tf.getValue().toString().length() * 0.55;
        				tf.setWidth(d.floatValue(), UNITS_EM);
        				
		                // Make the entire item read-only
		                HashMap<Object,Field> itemMap = fields.get(itemId);
		                for (Field f: itemMap.values())
		                    f.setReadOnly(true);*/
		                
						HashMap<Object,Field> itemMap = fields.get(itemId);
		                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
		                	Object column = entry.getKey();
		                	Field f = entry.getValue();
		                	Object fieldValue = f.getValue();
		                	
		                	// mark list as changed if value for the cell was changed
		                	if (column.equals(selectedColumn)) {
		                	    if (!fieldValue.toString().equals(lastCellvalue)) {
		                	        source.setChanged(true);
		                	    }
		                	}
		                	
		                    // validate for designation
		        			if (column.equals(selectedColumn) && selectedColumn.equals(ListDataTablePropertyID.DESIGNATION.getName())){
		        			    Object source = event.getSource();
                                String designation = source.toString();
                                
                                // retrieve item id at event source 
                                ItemPropertyId itemProp = (ItemPropertyId) ((TextField) source).getData();
                                Object sourceItemId = itemProp.getItemId();
		        				
		        				String[] items = listDataTable.getItem(sourceItemId).toString().split(" ");
								int gid =  Integer.valueOf(items[2]);
								
								if(isDesignationValid(designation,gid)){
									Double d = computeTextFieldWidth(f.getValue().toString());
									f.setWidth(d.floatValue(), UNITS_EM);
									f.setReadOnly(true);
									listDataTable.focus();
								}
								else{
									ConfirmDialog.show(getWindow(), "Update Designation", "The value you entered is not one of the germplasm names. Are you sure you want to update Designation with new value?",
													"Yes", "No", new ConfirmDialog.Listener() {	
											private static final long serialVersionUID = 1L;	
											public void onClose(ConfirmDialog dialog) {
												if (!dialog.isConfirmed()) {
													tf.setReadOnly(false);
													tf.setValue(lastCellvalue);
												}
												else{
													Double d = computeTextFieldWidth(tf.getValue().toString());
													tf.setWidth(d.floatValue(), UNITS_EM);
												}
												tf.setReadOnly(true);
												listDataTable.focus();
											}
										}
									);
								}
		        			}
		        			else{
		        				Double d = computeTextFieldWidth(f.getValue().toString());
								f.setWidth(d.floatValue(), UNITS_EM);
		        				f.setReadOnly(true);
		        			}
		                }
		            }
		        });
		        tf.addListener(new Property.ValueChangeListener() {//this area can be used for validation
					private static final long serialVersionUID = 1L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						Double d = computeTextFieldWidth(tf.getValue().toString());
						tf.setWidth(d.floatValue(), UNITS_EM);
						tf.setReadOnly(true);
						
						/*HashMap<Object,Field> itemMap = fields.get(itemId);
		                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
		                	Object column = entry.getKey();
		        			if(column.equals(selectedColumn) && selectedColumn.equals(ListDataTablePropertyID.DESIGNATION.getName())){		        				
		        				Field f = entry.getValue();
		        				
		        				String designation = event.getProperty().getValue().toString();
		        				
		        				String[] items = listDataTable.getItem(selectedItemId).toString().split(" ");
								int gid =  Integer.valueOf(items[1]);
								
								if(isDesignationValid(designation,gid)){
									Double d = f.getValue().toString().length() * 0.55;
									f.setWidth(d.floatValue(), UNITS_EM);
									f.setReadOnly(true);
									listDataTable.focus();
								}
								else{
									ConfirmDialog.show(getWindow(), "Update Designation", "The value you entered is not one of the germplasm names. Are you sure you want to update Designation with new value?",
													"Yes", "No", new ConfirmDialog.Listener() {	
											private static final long serialVersionUID = 1L;	
											public void onClose(ConfirmDialog dialog) {
												if (!dialog.isConfirmed()) {
													tf.setReadOnly(false);
													tf.focus();
												}
												else{
													Double d = tf.getValue().toString().length() * 0.55;
													tf.setWidth(d.floatValue(), UNITS_EM);
													tf.setReadOnly(true);
													listDataTable.focus();
												}
											}
										}
									);
								}
		        			}
		                }*/
		                
					}
	        	});
		        tf.addShortcutListener(new ShortcutListener("ENTER", ShortcutAction.KeyCode.ENTER, null) {
					private static final long serialVersionUID = 1L;

					@Override
		            public void handleAction(Object sender, Object target) {
						Double d = computeTextFieldWidth(tf.getValue().toString());
						tf.setWidth(d.floatValue(), UNITS_EM);
						tf.setReadOnly(true);
		                listDataTable.focus();
		               
		            }
		        });
		        
		        return tf;
		    }

			private Double computeTextFieldWidth(String value) {
		        double multiplier = 0.55;
		        int length = 1; 
		        if (value != null && !value.isEmpty()){
		        	length = value.length();
		        	if (value.equals(value.toUpperCase())){ 
		        		multiplier = 0.75;  // if all caps, provide bigger space
		        	}	
		        }		        
				Double d = length * multiplier;
				// set a minimum textfield width
				return NumberUtils.max(new double[]{MINIMUM_WIDTH, d});
			}
		});
		
		listDataTable.setEditable(true);

    }
    
    public boolean isDesignationValid(String designation, int gid){
    	List<Name> germplasms = new ArrayList<Name>();
    	List<String> designations = new ArrayList<String>();
    	
    	try{
    		germplasms = germplasmDataManager.getNamesByGID(gid, null, null);
    		
    		for(Name germplasm : germplasms){
    			designations.add(germplasm.getNval());
    		}
    		
    		for (String nameInDb : designations) {
    		    if (GermplasmDataManagerUtil.compareGermplasmNames(designation, nameInDb)){
    		        return true;
    		    }
    		}
    		
    	}catch(Exception e){
    		e.printStackTrace();
			LOG.error("Database error!", e);
			MessageNotifier.showError(getWindow(), "Database Error!", "Error with validating designation."
					+ messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
    	}
    	
    	return false; 
    }


    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    }

    
    private void populateTable() throws MiddlewareQueryException {
        listDataTable.removeAllItems();
        long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
        listDatas = this.germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, (int) listDataCount);
        for (GermplasmListData data : listDatas) {
            Object gidObject;

            if (!fromUrl) {
                // make GID as link only if the page wasn't directly accessed from the URL
                String gid = String.format("%s", data.getGid().toString());
                Button gidButton = new Button(gid, new GidLinkButtonClickListener(gid,true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                gidButton.setDescription("Click to view Germplasm information");
                gidObject = gidButton;
                //item.addItemProperty(columnId, new ObjectProperty<Button>(gidButton));
            } else {
                gidObject = data.getGid();
            }
            
            CheckBox itemCheckBox = new CheckBox();
            itemCheckBox.setData(data.getId());
            itemCheckBox.setImmediate(true);
	   		itemCheckBox.addListener(new ClickListener() {
	 			private static final long serialVersionUID = 1L;
	 			@SuppressWarnings("unchecked")
				@Override
	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
	 					listDataTable.select(itemCheckBox.getData());
	 				} else {
	 					listDataTable.unselect(itemCheckBox.getData());
	 				}
	 			}
	 			 
	 		});

            listDataTable.addItem(new Object[] {
                    itemCheckBox, gidObject,data.getGid(),data.getEntryId(), data.getEntryCode(), data.getSeedSource(),
                    data.getDesignation(), data.getGroupName() 
//                    , data.getStatusString()
            }, data.getId());
        }

        listDataTable.sort(new Object[]{"entryId"}, new boolean[]{true});
        listDataTable.setVisibleColumns(new String[] { 
        		CHECKBOX_COLUMN_ID
        		,ListDataTablePropertyID.GID.getName()
        		,ListDataTablePropertyID.ENTRY_ID.getName()
        		,ListDataTablePropertyID.ENTRY_CODE.getName()
        		,ListDataTablePropertyID.SEED_SOURCE.getName()
        		,ListDataTablePropertyID.DESIGNATION.getName()
        		,ListDataTablePropertyID.GROUP_NAME.getName()
//        		,ListDataTablePropertyID.STATUS.getName()
        		});
        
        // render additional columns
    	ListDataPropertiesRenderer newColumnsRenderer = new ListDataPropertiesRenderer(germplasmListId, listDataTable);
    	newColumnsRenderer.render();
        makeTableEditable();
    }
    
    public void saveChangesAction() throws InternationalizableException {
        saveChangesAction(this.getWindow());
    }


    public void saveChangesAction(Window window) throws InternationalizableException {
    	
    	//selected entries to entries    	
    	if(itemsToDelete.size() > 0){
        	performListEntriesDeletion(itemsToDelete);
    	}
    	
        try {
        	long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
            listDatas = this.germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, (int) listDataCount);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES);
        }
     
        int entryId = 1;
        //re-assign "Entry ID" field based on table's sorting
        for (Iterator<?> i = listDataTable.getItemIds().iterator(); i.hasNext();) {
            //iterate through the table elements' IDs
            int listDataId = (Integer) i.next();

            //update table item's entryId
            Item item = listDataTable.getItem(listDataId);
            item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entryId);

            //then find the corresponding ListData and assign a new entryId to it
            for (GermplasmListData listData : listDatas) {
                if (listData.getId().equals(listDataId)) {
                    listData.setEntryId(entryId);
                    
                    String entryCode = (String) item.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue();
                    if(entryCode != null && entryCode.length() != 0){
                    	listData.setEntryCode(entryCode);
                    } else {
                    	listData.setEntryCode(Integer.valueOf(entryId).toString());
                    }
                    
                    String seedSource = (String) item.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).getValue();
                    if(seedSource != null && seedSource.length() != 0){
                    	listData.setSeedSource(seedSource);
                    } else {
                    	listData.setSeedSource("-");
                    }
                    
                    String designation = (String) item.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue();
                    if(designation != null && designation.length() != 0){
                    	listData.setDesignation(designation);
                    } else {
                    	listData.setDesignation("-");
                    }
                    
                    String groupName = (String) item.getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).getValue();
                    if(groupName != null && groupName.length() != 0){
                    	if(groupName.length() > 255){
                    		groupName = groupName.substring(0, 255);
                    	}
                    	listData.setGroupName(groupName);
                    } else {
                    	listData.setGroupName("-");
                    }
                    
                    break;
                }
            }
            entryId += 1;
        }
        //save the list of Germplasm List Data to the database
        try {
        	
            germplasmListManager.updateGermplasmListData(listDatas);
            germplasmListManager.saveListDataColumns(addColumnContextMenu.getListDataCollectionFromTable(listDataTable));
            
            listDataTable.requestRepaint();
            //reset flag to indicate unsaved changes
            listManagerTreeMenu.setChanged(false);
            
            MessageNotifier.showMessage(window, 
                    messageSource.getMessage(Message.SUCCESS), 
                    messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SAVING_SUCCESS)
                    ,3000, Notification.POSITION_CENTERED);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES);
        }

    } // end of saveChangesAction

    //called by GermplasmListButtonClickListener
    public void exportListAction() throws InternationalizableException {

        if(germplasmListId>0 || (germplasmListId<0 && germplasmListStatus>=100)){
        
            String tempFileName = System.getProperty( USER_HOME ) + "/temp.xls";
    
            GermplasmListExporter listExporter = new GermplasmListExporter(germplasmListId);
    
            try {
                listExporter.exportGermplasmListExcel(tempFileName);
                FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
                fileDownloadResource.setFilename(listName.replace(" ", "_") + ".xls");
    
                //Window downloadWindow = new Window();
                //downloadWindow.setWidth(0);
                //downloadWindow.setHeight(0);
                //downloadWindow.open(fileDownloadResource);
                //this.getWindow().addWindow(downloadWindow);
                this.getWindow().open(fileDownloadResource);
    
                //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
                    //File tempFile = new File(tempFileName);
                    //tempFile.delete();
            } catch (GermplasmListExporterException e) {
                    LOG.error("Error with exporting list.", e);
                listManagerTreeMenu.getBreedingManagerApplication();
				MessageNotifier.showError(this.getApplication().getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME)
                            , "Error with exporting list."    
                            , e.getMessage() + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                            , Notification.POSITION_CENTERED);
            }
        } else {
//            MessageNotifier.showError(this.getApplication().getWindow(GermplasmStudyBrowserApplication.GERMPLASMLIST_WINDOW_NAME), "Germplasm List must be locked before exporting it", "");
            ConfirmDialog.show(this.getWindow(), "Export List", messageSource.getMessage(Message.LOCK_AND_EXPORT_CONFIRM),
                "Yes", "No", new ConfirmDialog.Listener() {
			private static final long serialVersionUID = 1L;

			public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                try {
                lockList();
                germplasmListStatus=germplasmList.getStatus();
                exportListAction();
                if (source != null && source.getListManagerListDetailComponent() != null){
                	source.getListManagerListDetailComponent().recreateTab();
                }
            } catch (MiddlewareQueryException e) {
                LOG.error("Error with exporting list.", e);
                e.printStackTrace();
            }
                
                }else{

                }
            }
            });
    }
        }

    //called by GermplasmListButtonClickListener
    public void exportListForGenotypingOrderAction() throws InternationalizableException {
        if(germplasmListId>0 || (germplasmListId<0 && germplasmListStatus>=100)){
            String tempFileName = System.getProperty( USER_HOME ) + "/tempListForGenotyping.xls";
            
                GermplasmListExporter listExporter = new GermplasmListExporter(germplasmListId);
    
                try {
                        listExporter.exportListForKBioScienceGenotypingOrder(tempFileName, 96);
                        FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
                        fileDownloadResource.setFilename(listName.replace(" ", "_") + "ForGenotyping.xls");
    
                        this.getWindow().open(fileDownloadResource);
    
                        //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
                        //File tempFile = new File(tempFileName);
                        //tempFile.delete();
                } catch (GermplasmListExporterException e) {
                        listManagerTreeMenu.getBreedingManagerApplication();
						MessageNotifier.showError(this.getApplication().getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME) 
                                    , "Error with exporting list."
                                    , e.getMessage(), Notification.POSITION_CENTERED);
                }
        } else {
            listManagerTreeMenu.getBreedingManagerApplication();
			MessageNotifier.showError(this.getApplication().getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME)
                        , "Error with exporting list."    
                        , "Germplasm List must be locked before exporting it", Notification.POSITION_CENTERED);
                    
        }
    }
    
    public void deleteListButtonClickAction()  throws InternationalizableException {
        Collection<?> selectedIdsToDelete = (Collection<?>)listDataTable.getValue();
        
        if(selectedIdsToDelete.size() > 0){
        	if(listDataTable.size() == selectedIdsToDelete.size()){
        		ConfirmDialog.show(this.getWindow(), "Delete All Entries in List Data", "Are you sure you want to delete all list data entries for this list?",
	        			"Yes", "No", new ConfirmDialog.Listener() {
	        		private static final long serialVersionUID = 1L;
	        		public void onClose(ConfirmDialog dialog) {
	        			if (dialog.isConfirmed()) {
	        				removeRowsInListDataTable((Collection<?>)listDataTable.getValue());
	        			}
	        		}
	        		
	        	});
        	}
        	else{
        		removeRowsInListDataTable(selectedIdsToDelete);
        	}
        	
        }else{
            MessageNotifier.showError(this.getWindow(), "Error with deleteting entries." 
                    , messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
    }
    
    private void removeRowsInListDataTable(Collection<?> selectedIds){
    	
    	//marks that there is a change in listDataTable
    	source.setChanged(true);
    	
    	//Marks the Local Germplasm to be deleted 
    	try {
			gidsWithoutChildren = getGidsToDeletedWithOutChildren(selectedIds);
			if(gidsWithoutChildren.size() > 0){
				ConfirmDialog.show(this.getWindow(), "Delete Germplasm from Database", "Would you like to delete the germplasm(s) from the database also?",
	        			"Yes", "No", new ConfirmDialog.Listener() {
	        		private static final long serialVersionUID = 1L;
	        		public void onClose(ConfirmDialog dialog) {
	        			if (dialog.isConfirmed()) {
	        				gidsWithoutChildrenToDelete.addAll(gidsWithoutChildren);
	        				gidsWithoutChildren.clear(); // reset
	        			}
	        		}
	        		
	        	});
			}
		} catch (NumberFormatException e) {
			LOG.error("Error with deleting list entries.", e);
			e.printStackTrace();
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with deleting list entries.", e);
			e.printStackTrace();
		}
    	
    	//marks the entryId and designationId of the list entries to delete
    	for(final Object itemId : selectedIds){
    		itemsToDelete.put(itemId,
    				listDataTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue().toString());
    		listDataTable.removeItem(itemId);
    	}
    	
    	//change entry IDs on table
    	Integer entryId = 1;
        for (Iterator<?> i = listDataTable.getItemIds().iterator(); i.hasNext();) {
            int listDataId = (Integer) i.next();
            Item item = listDataTable.getItem(listDataId);
            item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entryId);
            for (GermplasmListData listData : listDatas) {
                if (listData.getId().equals(listDataId)) {
                    listData.setEntryId(entryId);
                    break;
                }
            }
            entryId += 1;
        }
        listDataTable.requestRepaint();
    	
    } // end of removeRowsinListDataTable
    
    private void performListEntriesDeletion(Map<Object, String> itemsToDelete){    	
		try {
            if(getCurrentUserLocalId()==germplasListUserId) {
                designationOfListEntriesDeleted="";
                
                for (Map.Entry<Object, String> item : itemsToDelete.entrySet()) {
                	
                	Object sLRecId = item.getKey(); 
                	String sDesignation = item.getValue();
                	
                	try {
					    int lrecId=Integer.valueOf(sLRecId.toString());
					    designationOfListEntriesDeleted += sDesignation +",";
					    germplasmListManager.deleteGermplasmListDataByListIdLrecId(germplasmListId,lrecId);
					} catch (MiddlewareQueryException e) {
						LOG.error("Error with deleting list entries.", e);
						e.printStackTrace();
					}
                }
                
                deleteGermplasmDialogBox(gidsWithoutChildrenToDelete);
                designationOfListEntriesDeleted=designationOfListEntriesDeleted.substring(0,designationOfListEntriesDeleted.length()-1);
    
                //Change entry IDs on listData
                listDatas = germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0
                            , (int) germplasmListManager.countGermplasmListDataByListId(germplasmListId));
                Integer entryId = 1;
                for (GermplasmListData listData : listDatas) {
                    listData.setEntryId(entryId);
                    entryId++;
                }
                germplasmListManager.updateGermplasmListData(listDatas);
                
                try {
                    logDeletedListEntriesToWorkbenchProjectActivity();
                } catch (MiddlewareQueryException e) {
                    LOG.error("Error logging workbench activity.", e);
                    e.printStackTrace();
                }

                //reset items to delete in listDataTable
                itemsToDelete.clear(); 
                
            } else {
            	showMessageInvalidDeletingListEntries();
            }
            
		} catch (NumberFormatException e) {
			LOG.error("Error with deleting list entries.", e);
			e.printStackTrace();
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with deleting list entries.", e);
			e.printStackTrace();
		}
		
    } // end of performListEntriesDeletion
    
    private int getCurrentUserLocalId() throws MiddlewareQueryException {
        Integer workbenchUserId = this.workbenchDataManager.getWorkbenchRuntimeData().getUserId();
        Project lastProject = this.workbenchDataManager.getLastOpenedProject(workbenchUserId);
        Integer localIbdbUserId = this.workbenchDataManager.getLocalIbdbUserId(workbenchUserId,lastProject.getProjectId());
        if (localIbdbUserId != null) {
            return localIbdbUserId;
        } else {
            return -1; // TODO: verify actual default value if no workbench_ibdb_user_map was found
        }
    }

    private void logDeletedListEntriesToWorkbenchProjectActivity() throws MiddlewareQueryException {
    	User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());

        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                "Deleted list entries.", 
                "Deleted list entries from the list id " + germplasmListId + " - " + listName,user,new Date());
        try {
            workbenchDataManager.addProjectActivity(projAct);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error with logging workbench activity.", e);
            e.printStackTrace();
        }
    }

    private void showMessageInvalidDeletingListEntries(){
    MessageNotifier.showError(this.getWindow()
        , messageSource.getMessage(Message.INVALID_DELETING_LIST_ENTRIES) 
        , messageSource.getMessage(Message.INVALID_USER_DELETING_LIST_ENTRIES)
        , Notification.POSITION_CENTERED);
    }

    public void copyToNewListAction(){
        Collection<?> listEntries = (Collection<?>) listDataTable.getValue();
        if (listEntries == null || listEntries.isEmpty()){
            MessageNotifier.showError(this.getWindow(), messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), "", Notification.POSITION_CENTERED);
            
        } else {
            listManagerCopyToNewListDialog = new Window(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
            listManagerCopyToNewListDialog.setModal(true);
            listManagerCopyToNewListDialog.setWidth("700px");
            listManagerCopyToNewListDialog.setHeight("350px");
            listManagerCopyToNewListDialog.addStyleName(Reindeer.WINDOW_LIGHT);
            
            try {
                if(forGermplasmListWindow) {
                    listManagerTreeMenu.getBreedingManagerApplication();
					listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(this.getApplication().getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME), listManagerCopyToNewListDialog,listName,listDataTable,getCurrentUserLocalId(), listManagerMain));
                    listManagerTreeMenu.getBreedingManagerApplication();
					this.getApplication().getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME).addWindow(listManagerCopyToNewListDialog);
                 
                } else {
                    
//                  listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(this.getApplication().getMainWindow(), listManagerCopyToNewListDialog,listName,listDataTable,getCurrentUserLocalId()));
//                  this.getApplication().getMainWindow().addWindow(listManagerCopyToNewListDialog);
                    listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(listManagerTreeMenu.getWindow(), listManagerCopyToNewListDialog,listName,listDataTable,getCurrentUserLocalId(), listManagerMain));
                    listManagerTreeMenu.getWindow().addWindow(listManagerCopyToNewListDialog);
                }
            } catch (MiddlewareQueryException e) {
                LOG.error("Error copying list entries.", e);
                e.printStackTrace();
            }
        }
    }
    
    public void lockList() throws MiddlewareQueryException{
        germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
        germplasmList.setStatus(germplasmList.getStatus()+100);
        try {
        germplasmListManager.updateGermplasmList(germplasmList);
    
        User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
            "Locked a germplasm list.", 
            "Locked list "+germplasmList.getId()+" - "+germplasmList.getName(), user, new Date());
        workbenchDataManager.addProjectActivity(projAct);
        
//        	deleteSelectedEntriesButton.setEnabled(false); 
//            saveSortingButton.setEnabled(false);
//            addEntriesButton.setEnabled(false);
            
            menuDeleteEntries.setVisible(false);
            menuSaveChanges.setVisible(false);
            menuAddEntry.setVisible(false);
            
           
        }catch (MiddlewareQueryException e) {
            LOG.error("Error with locking list.", e);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with locking list. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            return;
        }
    }

    public void finishAddingEntry(Integer gid) {
        GermplasmList list = null; 
        Germplasm germplasm = null;
        try {
            list = germplasmListManager.getGermplasmListById(germplasmListId);
        } catch(MiddlewareQueryException ex){
            LOG.error("Error with getting germplasm list with id: " + germplasmListId, ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting germplasm list with id: " + germplasmListId  
                    + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            return;
        }
        
        try {
            germplasm = germplasmDataManager.getGermplasmWithPrefName(gid);
        } catch(MiddlewareQueryException ex){
            LOG.error("Error with getting germplasm with id: " + gid, ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting germplasm with id: " + gid  
                    + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            return;
        }
        
        Integer maxEntryId = Integer.valueOf(0);
        if (listDataTable != null){
        	for (Iterator<?> i = listDataTable.getItemIds().iterator(); i.hasNext();) {
        		//iterate through the table elements' IDs
        		int listDataId = (Integer) i.next();
        		
        		//update table item's entryId
        		Item item = listDataTable.getItem(listDataId);
        		Integer entryId = (Integer) item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue();
        		if(maxEntryId < entryId){
        			maxEntryId = entryId;
        		}
        	}
        }
        
        
        GermplasmListData listData = new GermplasmListData();
        listData.setList(list);
        if(germplasm.getPreferredName() != null){
            listData.setDesignation(germplasm.getPreferredName().getNval());
        } else {
            listData.setDesignation("-");
        }
        listData.setEntryId(maxEntryId+1);
        listData.setGid(gid);
        listData.setLocalRecordId(Integer.valueOf(0));
        listData.setStatus(Integer.valueOf(0));
        
        String preferredId = "-";
        try{
            Name nameRecord = this.germplasmDataManager.getPreferredIdByGID(gid);
            if(nameRecord != null){
                preferredId = nameRecord.getNval();
            }
        } catch(MiddlewareQueryException ex){
            preferredId = "-";
        }
        listData.setEntryCode(preferredId);
        
        listData.setSeedSource("From Add Entry Feature of List Manager");
        
        String groupName = "-";
        try{
            groupName = this.germplasmDataManager.getCrossExpansion(gid, 1);
        } catch(MiddlewareQueryException ex){
            groupName = "-";
        }
        listData.setGroupName(groupName);
            
        Integer listDataId = null;
        try {
            listDataId = this.germplasmListManager.addGermplasmListData(listData);
            
            Object gidObject;

            if (!fromUrl) {
                    // make GID as link only if the page wasn't directly accessed from the URL
                    String gidString = String.format("%s", gid.toString());
                    Button gidButton = new Button(gidString, new GidLinkButtonClickListener(gidString,false));
                    gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                    gidButton.setDescription("Click to view Germplasm information");
                    gidObject = gidButton;
            } else {
                    gidObject = gid;
            }
            
            // create table if added entry is first listdata record
            if (listDataTable == null){
            	if (noListDataLabel != null){
            		removeComponent(noListDataLabel);
            	}
            	initializeListDataTable(toolsMenuBar);
            	
            } else {
            	listDataTable.setEditable(false);
            	
            	Object[] visibleColumns = listDataTable.getVisibleColumns();
            	
            	listDataTable.setVisibleColumns(new String[] {
            			CHECKBOX_COLUMN_ID,
            			ListDataTablePropertyID.GID.getName()
            			,ListDataTablePropertyID.ENTRY_ID.getName()
            			,ListDataTablePropertyID.ENTRY_CODE.getName()
            			,ListDataTablePropertyID.SEED_SOURCE.getName()
            			,ListDataTablePropertyID.DESIGNATION.getName()
            			,ListDataTablePropertyID.GROUP_NAME.getName()
//            		,ListDataTablePropertyID.STATUS.getName()
            	});
            	
            	
                CheckBox itemCheckBox = new CheckBox();
                itemCheckBox.setData(listData.getId());
                itemCheckBox.setImmediate(true);
    	   		itemCheckBox.addListener(new ClickListener() {
    	 			private static final long serialVersionUID = 1L;
    	 			@SuppressWarnings("unchecked")
    				@Override
    	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
    	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
    	 					listDataTable.select(itemCheckBox.getData());
    	 				} else {
    	 					listDataTable.unselect(itemCheckBox.getData());
    	 				}
    	 			}
    	 			 
    	 		});
    	   		
            	listDataTable.addItem(new Object[] {
            			itemCheckBox, gidObject,listData.getEntryId(), listData.getEntryCode(), listData.getSeedSource(),
            			listData.getDesignation(), listData.getGroupName()
//                            , listData.getStatusString()
            	}, listDataId);
            	
            	listDataTable.setVisibleColumns(visibleColumns);
            	
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.PREFERRED_ID)){
            		addColumnContextMenu.setPreferredIdColumnValues(false);            
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.LOCATIONS)){
            		addColumnContextMenu.setLocationColumnValues(false);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.PREFERRED_NAME)){
            		addColumnContextMenu.setPreferredNameColumnValues(false);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.GERMPLASM_DATE)){
                    addColumnContextMenu.setGermplasmDateColumnValues(false);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_NAME)){
                    addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NAME);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_ABBREV)){
                    addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_ABBREV);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_NUMBER)){
                    addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NUMBER);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_GROUP)){
                    addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_GROUP);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_FEMALE_GID)){
                    addColumnContextMenu.setCrossFemaleInfoColumnValues(false, AddColumnContextMenu.CROSS_FEMALE_GID);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_FEMALE_PREF_NAME)){
                    addColumnContextMenu.setCrossFemaleInfoColumnValues(false, AddColumnContextMenu.CROSS_FEMALE_PREF_NAME);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_MALE_GID)){
                    addColumnContextMenu.setCrossMaleGIDColumnValues(false);
            	}
            	if(isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_MALE_PREF_NAME)){
                    addColumnContextMenu.setCrossMalePrefNameColumnValues(false);
            	}
            	
            	saveChangesAction();
            	listDataTable.refreshRowCache();
            	listDataTable.setImmediate(true);
            	listDataTable.setEditable(true);
            }
            
            MessageNotifier.showMessage(this.getWindow(), 
                    messageSource.getMessage(Message.SUCCESS), 
                    "Successful in adding a list entry.", 3000, Notification.POSITION_CENTERED);
            
            User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());

            ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                            workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                            "Added list entry.", 
                            "Added " + gid + " as list entry to " + list.getId() + ":" + list.getName(),user,new Date());
            try {
                workbenchDataManager.addProjectActivity(projAct);
            } catch (MiddlewareQueryException e) {
                LOG.error("Error with adding workbench activity log.", e);
                MessageNotifier.showError(getWindow(), "Database Error!", "Error with adding workbench activity log. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                        , Notification.POSITION_CENTERED);
            }
            //populateTable();
            //listDataTable.requestRepaint();
//            if(this.germplasmListAccordionMenu != null)
//                this.germplasmListAccordionMenu.refreshListData();
        } catch (MiddlewareQueryException ex) {
            LOG.error("Error with adding list entry.", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with adding list entry. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            return;
        }
    }
    
    public boolean isColumnVisible(Object[] columns, String columnName){
    	
    	for(Object col : columns){
    		if(col.equals(columnName)){
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public void addEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        AddEntryDialog addEntriesDialog = new AddEntryDialog(this, parentWindow);
        addEntriesDialog.addStyleName(Reindeer.WINDOW_LIGHT);
        parentWindow.addWindow(addEntriesDialog);
    }
    
	protected void deleteGermplasmDialogBox(final List<Integer> gidsWithoutChildren) throws NumberFormatException, MiddlewareQueryException {

        if (gidsWithoutChildren!= null && gidsWithoutChildren.size() > 0){
        	ArrayList<Germplasm> gList = new ArrayList<Germplasm>();
			try {
				for(Integer gid : gidsWithoutChildren){
					Germplasm g= germplasmDataManager.getGermplasmByGID(gid);
					g.setGrplce(gid);
					gList.add(g);
				}// end loop
				
				germplasmDataManager.updateGermplasm(gList);
				
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
        }
	}
	
    protected ArrayList<Integer> getGidsToDeletedWithOutChildren(Collection<?> selectedIds) throws NumberFormatException, MiddlewareQueryException{
    	ArrayList<Integer> gids= new ArrayList<Integer>();
	    for (final Object itemId : selectedIds) {
    		 Button gidButton = (Button) listDataTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
    		 Integer germplasmID = Integer.parseInt(gidButton.getCaption());
    		
			 // only allow deletions for local germplasms
			 if(germplasmID.toString().contains("-")){
				 long count = pedigreeDataManager.countDescendants(germplasmID);
				 if(count == 0){
					gids.add(germplasmID);
				 }
			 }
	     }
	    	   			 
	   	return gids;
    }
    
    public void editListButtonClickAction(){
    	
    	listManagerMain.showBuildNewListComponent();
    	buildNewListComponent = listManagerMain.getBuildListComponent();
    	
    	//Prompt when there is an unsave changes to build list/edit list
    	boolean hasChanges = buildNewListComponent.getHasChanges();
    	if(hasChanges == true){
    		
    		String message = "";
        	if(listManagerMain.getBuildNewListTitle().getValue().equals(messageSource.getMessage(Message.BUILD_A_NEW_LIST))){
        		message = "You have unsaved changes to the current list you are building. Do you want to save your changes before proceeding to your next list to edit?";
        	}
        	else {
        		message = "You have unsaved changes to the list you are editing. Do you want to save your changes before proceeding to your next list to edit?";
        	}
        	
    		ConfirmDialog.show(getWindow(), "Unsave Changes", message, "Yes", "No", new ConfirmDialog.Listener() {
    			
					private static final long serialVersionUID = 1L;
					
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							buildNewListComponent.getSaveButton().click(); // save the existing list	
						}
						
						//close the currentTab
						TabSheet detailsTabSheet = listManagerTreeMenu.getDetailsLayout().getTabSheet();
						Tab currentTab = detailsTabSheet.getTab(detailsTabSheet.getSelectedTab());
						detailsTabSheet.removeTab(detailsTabSheet.getTab(detailsTabSheet.getTabPosition(currentTab)));
						
						buildNewListComponent.viewEditList(germplasmListId);
						buildNewListComponent.setHasChanges(false); //reset
						buildNewListComponent.getListNameText().focus();
						
					}
				}
			);
    	}
    	else{
    		
    		//close the currentTab
			TabSheet detailsTabSheet = listManagerTreeMenu.getDetailsLayout().getTabSheet();
			Tab currentTab = detailsTabSheet.getTab(detailsTabSheet.getSelectedTab());
			detailsTabSheet.removeTab(detailsTabSheet.getTab(detailsTabSheet.getTabPosition(currentTab)));
			
    		buildNewListComponent.viewEditList(germplasmListId);
    		buildNewListComponent.setHasChanges(false); //reset
    	}	
    }
    
    public Table getListDataTable(){
    	return listDataTable;
    }

    public AddColumnContextMenu getAddColumnContextMenu(){
    	return addColumnContextMenu;
    }
    
    public String getListName() {
        return listName;
    }
    
    public List<GermplasmListData> getListDatas() {
        return listDatas;
    }
    
}

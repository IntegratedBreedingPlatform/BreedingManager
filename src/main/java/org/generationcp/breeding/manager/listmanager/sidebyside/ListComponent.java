package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.listmanager.ListInventoryComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerCopyToNewListDialog;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialogSource;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
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
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
        BreedingManagerLayout, AddEntryDialogSource, SaveListAsDialogSource {
	private static final long serialVersionUID = -3367108805414232721L;

	private static final Logger LOG = LoggerFactory.getLogger(ListComponent.class);
	
	private static final int MINIMUM_WIDTH = 10;
	private final HashMap<Object,HashMap<Object,Field>> fields = new HashMap<Object,HashMap<Object,Field>>();

	private final ListManagerMain source;
	private final ListTabComponent parentListDetailsComponent;
	private GermplasmList germplasmList;
	private List<GermplasmListData> listEntries;
	private long listEntriesCount;
	private String designationOfListEntriesDeleted="";
	
	private Label listEntriesLabel;
	private Button viewHeaderButton;
	private Label totalListEntriesLabel;
	private Button toolsButton;
	private Table listDataTable;
	private TableWithSelectAllLayout listDataTableWithSelectAll;
	private Label noListDataLabel;
	
	private HorizontalLayout headerLayout;
	private HorizontalLayout subHeaderLayout;
	
	//Menu for tools button
	private ContextMenu menu; 
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuExportForGenotypingOrder;
	private ContextMenuItem menuCopyToList;
	private ContextMenuItem menuAddEntry;
	private ContextMenuItem menuSaveChanges;
	private ContextMenuItem menuDeleteEntries;
	private ContextMenuItem menuEditList;
	private ContextMenuItem menuDeleteList;
	private AddColumnContextMenu addColumnContextMenu;
	
	//Tools Menu Options
    private String MENU_SELECT_ALL="Select All";
	private String MENU_EXPORT_LIST="Export List";
	private String MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER="Export List for Genotyping Order";
	private String MENU_COPY_TO_NEW_LIST="Copy to new list";
	private String MENU_ADD_ENTRY="Add Entries";
	private String MENU_SAVE_CHANGES="Save Changes";
	private String MENU_DELETE_SELECTED_ENTRIES="Delete Selected Entries";
	private String MENU_EDIT_LIST="Edit List";
	private String MENU_EDIT_VALUE="Edit Value";
	private String MENU_DELETE_LIST="Delete List";
	private String MENU_VIEW_INVENTORY = "View Inventory";

    
    //Tooltips
  	public static String TOOLS_BUTTON_ID = "Actions";
  	public static String LIST_DATA_COMPONENT_TABLE_DATA = "List Data Component Table";
  	private final String CHECKBOX_COLUMN_ID="Checkbox Column ID";
  	
  	private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL
  	
	//Theme Resource
  	private Window listManagerCopyToNewListDialog;
	private static final ThemeResource ICON_TOOLS = new ThemeResource("images/tools.png");
	private static final String USER_HOME = "user.home";
	
	private Object selectedColumn = "";
	private Object selectedItemId;
	private String lastCellvalue = "";
	private final List<Integer> gidsWithoutChildrenToDelete;
	private final Map<Object, String> itemsToDelete;

    private Button lockButton;
    private Button unlockButton;
    private Button editHeaderButton;
    
    private ViewListHeaderWindow viewListHeaderWindow;
	
    public static String LOCK_BUTTON_ID = "Lock Germplasm List";
    public static String UNLOCK_BUTTON_ID = "Unlock Germplasm List";
	
    private static String LOCK_TOOLTIP = "Click to lock or unlock this germplasm list.";

    private ContextMenu tableContextMenu;

    @SuppressWarnings("unused")
    private ContextMenuItem tableContextMenu_SelectAll;
    @SuppressWarnings("unused")
    private ContextMenuItem tableContextMenu_CopyToNewList;
    private ContextMenuItem tableContextMenu_DeleteEntries;
    private ContextMenuItem tableContextMenu_EditCell;

    //Value change event is fired when table is populated, so we need a flag
    private Boolean doneInitializing = false;
    
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	@Autowired
    private GermplasmDataManager germplasmDataManager;
	
	@Autowired
    private PedigreeDataManager pedigreeDataManager;
	
	@Autowired
    private WorkbenchDataManager workbenchDataManager;
	
	private Integer localUserId = null;
	
	public ListComponent(ListManagerMain source, ListTabComponent parentListDetailsComponent, GermplasmList germplasmList) {
		super();
		this.source = source;
		this.parentListDetailsComponent = parentListDetailsComponent;
		this.germplasmList = germplasmList;
		this.gidsWithoutChildrenToDelete = new ArrayList<Integer>();
		this.itemsToDelete = new HashMap<Object, String>();
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
		listEntriesLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		listEntriesLabel.setWidth("120px");
		listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		viewListHeaderWindow = new ViewListHeaderWindow(germplasmList);
		
		viewHeaderButton = new Button(messageSource.getMessage(Message.VIEW_HEADER));
		viewHeaderButton.addStyleName(Reindeer.BUTTON_LINK);
		viewHeaderButton.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
		
		editHeaderButton =new IconButton("<span class='glyphicon glyphicon-pencil' style='left: 2px; top:10px; color: #7c7c7c;font-size: 16px; font-weight: bold;'></span>","Edit List Header");
		
		toolsButton = new Button(messageSource.getMessage(Message.ACTIONS));
		toolsButton.setData(TOOLS_BUTTON_ID);
		toolsButton.setIcon(ICON_TOOLS);
		toolsButton.setWidth("110px");
		toolsButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		
		try{
			listEntriesCount = germplasmListManager.countGermplasmListDataByListId(germplasmList.getId());
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with retrieving count of list entries for list: " + germplasmList.getId(), ex);
			listEntriesCount = 0;
		}
		
		if(listEntriesCount == 0) {
			noListDataLabel = new Label(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
            noListDataLabel.setWidth("250px");
		} else {
        	totalListEntriesLabel = new Label(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
        		 + "  <b>" + listEntriesCount + "</b>", Label.CONTENT_XHTML);
        	totalListEntriesLabel.setWidth("135px");
        }
		
	    unlockButton = new IconButton("<span class='bms-locked' style='position: relative; top:5px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>", LOCK_TOOLTIP);
        unlockButton.setData(UNLOCK_BUTTON_ID);
	
        lockButton = new IconButton("<span class='bms-lock-open' style='position: relative; top:5px; left: 2px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>", LOCK_TOOLTIP);
        lockButton.setData(LOCK_BUTTON_ID);
        		
        menu = new ContextMenu();
		menu.setWidth("295px");
		
		// Add Column menu will be initialized after list data table is created
        initializeListDataTable(); //listDataTable
        
		// Generate main level items
		menuAddEntry = menu.addItem(MENU_ADD_ENTRY);
		menuCopyToList = menu.addItem(MENU_COPY_TO_NEW_LIST);
		menuDeleteList = menu.addItem(MENU_DELETE_LIST);
		menuDeleteEntries = menu.addItem(MENU_DELETE_SELECTED_ENTRIES);
		menuEditList = menu.addItem(MENU_EDIT_LIST);
		menuExportList = menu.addItem(MENU_EXPORT_LIST);
		menuExportForGenotypingOrder = menu.addItem(MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER);
		menuSaveChanges = menu.addItem(MENU_SAVE_CHANGES);
		menu.addItem(MENU_SELECT_ALL);
		menu.addItem(MENU_VIEW_INVENTORY);
		
        tableContextMenu = new ContextMenu();
        tableContextMenu.setWidth("295px");

        tableContextMenu_SelectAll = tableContextMenu.addItem(MENU_SELECT_ALL);
        tableContextMenu_DeleteEntries = tableContextMenu.addItem(MENU_DELETE_SELECTED_ENTRIES);
        tableContextMenu_EditCell = tableContextMenu.addItem(MENU_EDIT_VALUE);
        tableContextMenu_CopyToNewList = tableContextMenu.addItem(MENU_COPY_TO_NEW_LIST);
	}
	
	private void initializeListDataTable(){
		listDataTableWithSelectAll = new TableWithSelectAllLayout(Long.valueOf(listEntriesCount).intValue(), getNoOfEntries(), CHECKBOX_COLUMN_ID);
		listDataTable = listDataTableWithSelectAll.getTable();
		listDataTable.setSelectable(true);
		listDataTable.setMultiSelect(true);
		listDataTable.setColumnCollapsingAllowed(true);
		listDataTable.setColumnReorderingAllowed(true);
		listDataTable.setWidth("100%");
		listDataTable.setDragMode(TableDragMode.ROW);
		listDataTable.setData(LIST_DATA_COMPONENT_TABLE_DATA);
		listDataTable.setColumnReorderingAllowed(false);
		
		listDataTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.GROUP_NAME.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		
		messageSource.setColumnHeader(listDataTable, CHECKBOX_COLUMN_ID, Message.CHECK_ICON);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.ENTRY_ID.getName(), Message.HASHTAG);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.GROUP_NAME.getName(), Message.LISTDATA_GROUPNAME_HEADER);
		
		addColumnContextMenu = new AddColumnContextMenu(parentListDetailsComponent, menu, 
                listDataTable, ListDataTablePropertyID.GID.getName());
	}
	
	public int getNoOfEntries(){
		// browse list component is null at this point when tool launched from Workbench dashboard
		ListSelectionComponent browseListsComponent = source.getListSelectionComponent();
		if(browseListsComponent== null || browseListsComponent.isVisible()){
			return 8; 
		}
		
		return 18;
	}

	@Override
	public void initializeValues() {
	    try {
            localUserId = getCurrentUserLocalId();
        } catch (MiddlewareQueryException e) {
            LOG.error("Error with retrieving local user ID", e);
            e.printStackTrace();
        }
	    
	    if(listEntriesCount > 0){
		    listEntries = new ArrayList<GermplasmListData>();
			try{
				listEntries.addAll(germplasmListManager.getGermplasmListDataByListId(germplasmList.getId(), 0, Long.valueOf(listEntriesCount).intValue()));
			} catch(MiddlewareQueryException ex){
				LOG.error("Error with retrieving list entries for list: " + germplasmList.getId(), ex);
				listEntries = new ArrayList<GermplasmListData>();
			}
			
			for(GermplasmListData entry : listEntries){
				String gid = String.format("%s", entry.getGid().toString());
                Button gidButton = new Button(gid, new GidLinkButtonClickListener(gid,true,true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                gidButton.setDescription("Click to view Germplasm information");
                
                Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(gid,true,true));
                desigButton.setStyleName(BaseTheme.BUTTON_LINK);
                desigButton.setDescription("Click to view Germplasm information");
                
                CheckBox itemCheckBox = new CheckBox();
                itemCheckBox.setData(entry.getId());
                itemCheckBox.setImmediate(true);
    	   		itemCheckBox.addListener(new ClickListener() {
    	 			private static final long serialVersionUID = 1L;
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
    	   		
    	   		Item newItem = listDataTable.addItem(entry.getId());
    	   		newItem.getItemProperty(CHECKBOX_COLUMN_ID).setValue(itemCheckBox);
    	   		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entry.getEntryId());
    	   		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(desigButton);
    	   		newItem.getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).setValue(entry.getGroupName());
    	   		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
    	   		newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
    	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(entry.getSeedSource());
    	   	}
			
			listDataTable.sort(new Object[]{ListDataTablePropertyID.ENTRY_ID.getName()}, new boolean[]{true});
			
			// render additional columns
	    	ListDataPropertiesRenderer newColumnsRenderer = new ListDataPropertiesRenderer(germplasmList.getId(), listDataTable);
	    	try{
	    		newColumnsRenderer.render();
	    	} catch(MiddlewareQueryException ex){
	    		LOG.error("Error with displaying added columns for entries of list: " + germplasmList.getId(), ex);
	    	}
		}
	    
	}

	@Override
	public void addListeners() {
		viewHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openViewListHeaderWindow();
			}
		});
		
		if(germplasmList.isLocalList() && !germplasmList.isLockedList()){
	        new FillWith(parentListDetailsComponent, parentListDetailsComponent, messageSource, listDataTable, ListDataTablePropertyID.GID.getName());
	    }
		
		makeTableEditable();
	

	
		toolsButton.addListener(new ClickListener() {
	   		 private static final long serialVersionUID = 272707576878821700L;
	
				 @Override
	   		 public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				 addColumnContextMenu.refreshAddColumnMenu();
	   			 menu.show(event.getClientX(), event.getClientY());
	   			 
	   			 if(fromUrl){
	   			  menuExportForGenotypingOrder.setVisible(false);
	   			  menuExportList.setVisible(false);
	   			  menuCopyToList.setVisible(false);
	   			 }
	   			 
				 // Show items only when Germplasm List open is a local IBDB record (negative ID),
	   			 // when the Germplasm List is not locked, and when not accessed directly from URL or popup window
	   			 if (germplasmList.isLocalList() && !germplasmList.isLockedList() && !fromUrl) {
                     menuEditList.setVisible(true);
                     menuDeleteList.setVisible(localUserIsListOwner()); //show only Delete List when user is owner
                     menuDeleteEntries.setVisible(true); 
                     menuSaveChanges.setVisible(true);
                     menuAddEntry.setVisible(true);
                     addColumnContextMenu.showHideAddColumnMenu(true);
	   			 } else {
                     menuEditList.setVisible(false);
                     menuDeleteList.setVisible(false);
                     menuDeleteEntries.setVisible(false);
                     menuSaveChanges.setVisible(false);
                     menuAddEntry.setVisible(false);
                     addColumnContextMenu.showHideAddColumnMenu(false);
	   			 }
	
	   		 }
	   	 });
		
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
	
			@Override
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
			    	  deleteEntriesButtonClickAction();
			      }else if(clickedItem.getName().equals(MENU_EDIT_LIST)){
			    	  editListButtonClickAction();
			      }else if(clickedItem.getName().equals(MENU_DELETE_LIST)){
                      deleteListButtonClickAction();
                  } else if(clickedItem.getName().equals(MENU_VIEW_INVENTORY)){
                	  viewInventoryAction();
                  }
			      
		   }
		});
		
		editHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -788407324474054727L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openSaveListAsDialog();
			}
		});
		
		lockButton.addListener(new ClickListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                lockGermplasmList();
            }
        });
        
        unlockButton.addListener(new ClickListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                unlockGermplasmList();
            }
        });

        tableContextMenu.addListener(new ContextMenu.ClickListener() {
            private static final long serialVersionUID = -2343109406180457070L;
            public void contextItemClick(ClickEvent event) {
                    String action = event.getClickedItem().getName();
                    if (action.equals(MENU_DELETE_SELECTED_ENTRIES)) {
                            deleteEntriesButtonClickAction();
                    }else if(action.equals(MENU_SELECT_ALL)) {
                            listDataTable.setValue(listDataTable.getItemIds());
                    }else if(action.equals(MENU_EDIT_VALUE)){
                    	
                    	HashMap<Object,Field> itemMap = fields.get(selectedItemId);
                    	
	                	// go through each field, set previous edited fields to blurred/readonly
	                    for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
                            Object column = entry.getKey();
                            //if(column.equals(selectedColumn)){
                                 Field f = entry.getValue();
                                 Object fieldValue = f.getValue();
                                 if(!f.isReadOnly()){
                                	f.setReadOnly(true);
                                	
                                	if(!fieldValue.equals(lastCellvalue)){
                                		parentListDetailsComponent.setChanged(true);
                                	}
                                 }
                            //}
	                    }
	                	
                        // Make the entire item editable
                        
                        if(itemMap != null){
                        	for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
                                Object column = entry.getKey();
                                if(column.equals(selectedColumn)){
                                    Field f = entry.getValue();
                                    if(f.isReadOnly()){
                                    	Object fieldValue = f.getValue();
                                    	lastCellvalue = (fieldValue != null)? fieldValue.toString() : "";
                                    	f.setReadOnly(false);
                                    	f.focus();
                                    }
                                }
                        	}
                        }

                        listDataTable.select(selectedItemId);
                    }else if(action.equals(MENU_COPY_TO_NEW_LIST)){
                        source.addSelectedPlantsToList(listDataTable);
                    }
            }
        });
        
        
	}//end of addListeners

	@Override
	public void layoutComponents() {
		//this.setSizeFull();

		headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		headerLayout.setSpacing(true);
		
		HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, listEntriesLabel);
		headerLayout.addComponent(headingLayout);
		headerLayout.addComponent(viewHeaderButton);
		headerLayout.setComponentAlignment(viewHeaderButton, Alignment.BOTTOM_RIGHT);
		
		if(germplasmList.isLocalList() && !germplasmList.isLockedList()){
			headerLayout.addComponent(editHeaderButton);
			headerLayout.setComponentAlignment(editHeaderButton, Alignment.BOTTOM_LEFT);
		}
		
		if(germplasmList.isLocalList() && localUserIsListOwner()){
			headerLayout.addComponent(lockButton);
			headerLayout.setComponentAlignment(lockButton, Alignment.BOTTOM_LEFT);
	
			headerLayout.addComponent(unlockButton);
			headerLayout.setComponentAlignment(unlockButton, Alignment.BOTTOM_LEFT);
	
			showHideOptionsForLocked();
		}

        headerLayout.setExpandRatio(headingLayout,1.0f);
		
		subHeaderLayout = new HorizontalLayout();
		subHeaderLayout.setWidth("100%");
		subHeaderLayout.setSpacing(true);
		
		if(listEntriesCount == 0) {
			subHeaderLayout.addComponent(noListDataLabel); 
			subHeaderLayout.setComponentAlignment(noListDataLabel, Alignment.MIDDLE_LEFT);
		} else{
			subHeaderLayout.addComponent(totalListEntriesLabel);
			subHeaderLayout.setComponentAlignment(totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		}
		
		subHeaderLayout.addComponent(toolsButton);
		subHeaderLayout.setComponentAlignment(toolsButton, Alignment.MIDDLE_RIGHT);
		subHeaderLayout.addStyleName("lm-list-desc");
		
		addComponent(headerLayout);
		addComponent(subHeaderLayout);
		
		//listDataTableWithSelectAll.setHeight("410px");
		listDataTable.setHeight("480px");
		
		addComponent(listDataTableWithSelectAll);
        addComponent(tableContextMenu);

		parentListDetailsComponent.addComponent(menu);
	}

	@Override
	public void updateLabels() {
		
	}
	
	private boolean localUserIsListOwner() {
        return germplasmList.getUserId().equals(localUserId);
    }
	
	public void makeTableEditable(){
    	listDataTable.addListener(new ItemClickListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void itemClick(ItemClickEvent event) {
				selectedColumn = event.getPropertyId();
				selectedItemId = event.getItemId();
				
                if(event.getButton()==ItemClickEvent.BUTTON_RIGHT){

                    tableContextMenu.show(event.getClientX(), event.getClientY());

                    if(selectedColumn.equals(CHECKBOX_COLUMN_ID) || selectedColumn.equals(ListDataTablePropertyID.GID.getName()) || selectedColumn.equals(ListDataTablePropertyID.ENTRY_ID.getName())){
                            tableContextMenu_DeleteEntries.setVisible(true);
                            tableContextMenu_EditCell.setVisible(false);
                    } else if (germplasmList.isLocalList() && !germplasmList.isLockedList()){
                            tableContextMenu_DeleteEntries.setVisible(true);
                            tableContextMenu_EditCell.setVisible(true);
                            doneInitializing = true;
                    } else {
                            tableContextMenu_DeleteEntries.setVisible(false);
                            tableContextMenu_EditCell.setVisible(false);
                    }
                }
			}
		});
    	
    	listDataTable.setTableFieldFactory(new TableFieldFactory() {
			private static final long serialVersionUID = 1L;

			@Override
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

					@Override
					public void focus(FocusEvent event) {
						listDataTable.select(itemId);
		            }
		        });
		        
		        tf.addListener(new FocusListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void focus(FocusEvent event) {
						lastCellvalue = ((TextField) event.getComponent()).getValue().toString();
					}
		        });
		        
		        tf.addListener(new BlurListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void blur(BlurEvent event) {
						HashMap<Object,Field> itemMap = fields.get(itemId);

	                	// go through each field, set previous edited fields to blurred/readonly
	                    for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
                            Object column = entry.getKey();
                            //if(column.equals(selectedColumn)){
                                 Field f = entry.getValue();
                                 Object fieldValue = f.getValue();
                                 if(!f.isReadOnly()){
                                	f.setReadOnly(true);
                                	if(!fieldValue.equals(lastCellvalue)){
                                		parentListDetailsComponent.setChanged(true);
                                	}
                                 }
                            //}
	                    }
						
		                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
		                	Object column = entry.getKey();
		                	Field f = entry.getValue();
		                	Object fieldValue = f.getValue();
		                	
		                	
		                	// mark list as changed if value for the cell was changed
		                	if (column.equals(selectedColumn)) {
		                	    if (!f.isReadOnly() && !fieldValue.toString().equals(lastCellvalue)) {
		                	        parentListDetailsComponent.setChanged(true);
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
									ConfirmDialog.show(getWindow(), "Update Designation", "The value you entered is not one of the germplasm names. "
										+ "Are you sure you want to update Designation with new value?"
										, "Yes", "No", new ConfirmDialog.Listener() {	
											private static final long serialVersionUID = 1L;	
											@Override
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
						
						if (doneInitializing && !tf.getValue().toString().equals(lastCellvalue)) {
                	        parentListDetailsComponent.setChanged(true);
                	    }
						//parentListDetailsComponent.setChanged(true);
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
    		LOG.error("Database error!", e);
			MessageNotifier.showError(getWindow(), "Database Error!", "Error with validating designation."
					+ messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
    	}
    	
    	return false;
    }
	
	public void deleteEntriesButtonClickAction()  throws InternationalizableException {
        Collection<?> selectedIdsToDelete = (Collection<?>)listDataTable.getValue();
        
        if(selectedIdsToDelete.size() > 0){
        	if(listDataTable.size() == selectedIdsToDelete.size()){
        	    ConfirmDialog.show(this.getWindow(),
                        messageSource.getMessage(Message.DELETE_ALL_ENTRIES),
                        messageSource.getMessage(Message.DELETE_ALL_ENTRIES_CONFIRM),
                        messageSource.getMessage(Message.YES),
                        messageSource.getMessage(Message.NO),
                        new ConfirmDialog.Listener() {
	        		private static final long serialVersionUID = 1L;
	        		@Override
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
            MessageNotifier.showError(this.getWindow(), messageSource.getMessage(Message.ERROR_DELETING_LIST_ENTRIES) 
                    , messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
    }
	
	private void removeRowsInListDataTable(Collection<?> selectedIds){
    	//marks that there is a change in listDataTable
    	parentListDetailsComponent.setChanged(true);
    	
    	//Marks the Local Germplasm to be deleted 
    	try {
			final List<Integer> gidsWithoutChildren = getGidsToDeletedWithoutChildren(selectedIds);
			if(gidsWithoutChildren.size() > 0){
				ConfirmDialog.show(this.getWindow(), "Delete Germplasm from Database", "Would you like to delete the germplasm(s) from the database also?",
	        			"Yes", "No", new ConfirmDialog.Listener() {
	        		private static final long serialVersionUID = 1L;
	        		@Override
					public void onClose(ConfirmDialog dialog) {
	        			if (dialog.isConfirmed()) {
	        				gidsWithoutChildrenToDelete.addAll(gidsWithoutChildren);
	        			}
	        		}
	        		
	        	});
			}
		} catch (NumberFormatException e) {
			LOG.error("Error with deleting list entries.", e);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), "Error with deleting list entries.", Notification.POSITION_CENTERED);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with deleting list entries.", e);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), "Error with deleting list entries.", Notification.POSITION_CENTERED);
		}
    	
    	//marks the entryId and designationId of the list entries to delete
    	for(final Object itemId : selectedIds){
            Button desigButton = (Button) listDataTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue();
            String designation = String.valueOf((desigButton.getCaption().toString()));
    		itemsToDelete.put(itemId, designation);
    		listDataTable.removeItem(itemId);
    	}
    	
    	renumberEntryIds();
        listDataTable.requestRepaint();
        updateListEntriesCountLabel();
    }
	
	private ArrayList<Integer> getGidsToDeletedWithoutChildren(Collection<?> selectedIds) throws NumberFormatException, MiddlewareQueryException{
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
	
	private void renumberEntryIds(){
		Integer entryId = 1;
        for (Iterator<?> i = listDataTable.getItemIds().iterator(); i.hasNext();) {
            int listDataId = (Integer) i.next();
            Item item = listDataTable.getItem(listDataId);
            item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entryId);
            entryId += 1;
        }
	}
	
	/* MENU ACTIONS */ 
	private void editListButtonClickAction() {
		final ListBuilderComponent ListBuilderComponent = source.getListBuilderComponent();
		
    	if(ListBuilderComponent.isChanged()){
    		String message = "";
    		
    		String buildNewListTitle = ListBuilderComponent.getBuildNewListTitle().getValue().toString();
    		if(buildNewListTitle.equals(messageSource.getMessage(Message.BUILD_A_NEW_LIST))){
        		message = "You have unsaved changes to the current list you are building. Do you want to save your changes before proceeding to your next list to edit?";
        	}
        	else {
        		message = "You have unsaved changes to the list you are editing. Do you want to save your changes before proceeding to your next list to edit?";
        	}
    		
    		ConfirmDialog.show(getWindow(), "Unsave Changes", message, "Yes", "No", new ConfirmDialog.Listener() {
    			
				private static final long serialVersionUID = 1L;	
				@Override
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						ListBuilderComponent.getSaveButton().click(); // save the existing list	
					}
					
					source.loadListForEditing(getGermplasmList());
				}
			});
    	}
    	else{
    		source.loadListForEditing(getGermplasmList());
    	}
	}
	
    private void exportListAction() throws InternationalizableException {
        if(!germplasmList.isLocalList() || (germplasmList.isLocalList() && germplasmList.isLockedList())){
            String tempFileName = System.getProperty( USER_HOME ) + "/temp.xls";
            GermplasmListExporter listExporter = new GermplasmListExporter(germplasmList.getId());
            try {
                listExporter.exportGermplasmListExcel(tempFileName);
                FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), source.getApplication());
                String listName = germplasmList.getName();
                fileDownloadResource.setFilename(listName.replace(" ", "_") + ".xls");
                source.getWindow().open(fileDownloadResource);
                //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
                    //File tempFile = new File(tempFileName);
                    //tempFile.delete();
            } catch (GermplasmListExporterException e) {
                LOG.error(messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
                MessageNotifier.showError(source.getWindow()
                            , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)    
                            , e.getMessage() + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                            , Notification.POSITION_CENTERED);
            }
        } else {
            MessageNotifier.showError(source.getWindow()
                    , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)
                    , messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED), Notification.POSITION_CENTERED);
        }
    }
    
    private void recreateTab() {
        try {
            parentListDetailsComponent.getListSelectionLayout().removeTab(germplasmList.getId());
			parentListDetailsComponent.getListSelectionLayout().createListDetailsTab(germplasmList.getId());
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
    }
    
    private void exportListForGenotypingOrderAction() throws InternationalizableException {
        if(!germplasmList.isLocalList() || (germplasmList.isLocalList() && germplasmList.isLockedList())){
            String tempFileName = System.getProperty( USER_HOME ) + "/tempListForGenotyping.xls";
            GermplasmListExporter listExporter = new GermplasmListExporter(germplasmList.getId());
            
            try {
                listExporter.exportListForKBioScienceGenotypingOrder(tempFileName, 96);
                FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), source.getApplication());
                String listName = germplasmList.getName();
                fileDownloadResource.setFilename(listName.replace(" ", "_") + "ForGenotyping.xls");
                
                source.getWindow().open(fileDownloadResource);
                
                //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
                //File tempFile = new File(tempFileName);
                //tempFile.delete();
            } catch (GermplasmListExporterException e) {
                MessageNotifier.showError(source.getWindow()
                        , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)
                        , e.getMessage(), Notification.POSITION_CENTERED);
            }
        } else {
            MessageNotifier.showError(source.getWindow()
                    , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)    
                    , messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED), Notification.POSITION_CENTERED);
        }
    }
    
    private void copyToNewListAction(){
        Collection<?> listEntries = (Collection<?>) listDataTable.getValue();
        if (listEntries == null || listEntries.isEmpty()){
            MessageNotifier.showError(this.getWindow(), messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), "", Notification.POSITION_CENTERED);
            
        } else {
            listManagerCopyToNewListDialog = new Window(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
            listManagerCopyToNewListDialog.setModal(true);
            listManagerCopyToNewListDialog.setWidth("617px");
            listManagerCopyToNewListDialog.setHeight("230px");
            listManagerCopyToNewListDialog.setResizable(false);
            listManagerCopyToNewListDialog.addStyleName(Reindeer.WINDOW_LIGHT);
            
            try {
                listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(
                        parentListDetailsComponent.getWindow(),
                        listManagerCopyToNewListDialog,
                        germplasmList.getName(),
                        listDataTable,
                        getCurrentUserLocalId(),
                        source,
                        false));
                parentListDetailsComponent.getWindow().addWindow(listManagerCopyToNewListDialog);
                listManagerCopyToNewListDialog.center();
            } catch (MiddlewareQueryException e) {
                LOG.error("Error copying list entries.", e);
                e.printStackTrace();
            }
        }
    }
    
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
    
    private void addEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        AddEntryDialog addEntriesDialog = new AddEntryDialog(this, parentWindow);
        addEntriesDialog.addStyleName(Reindeer.WINDOW_LIGHT);
        addEntriesDialog.focusOnSearchField();
        parentWindow.addWindow(addEntriesDialog);
    }
    
    
    @Override
    public void finishAddingEntry(Integer gid) {
    	finishAddingEntry(gid, true);
    }
    
    public Boolean finishAddingEntry(Integer gid, Boolean showSuccessMessage) {
    	
        Germplasm germplasm = null;

        try {
            germplasm = germplasmDataManager.getGermplasmWithPrefName(gid);
        } catch(MiddlewareQueryException ex){
            LOG.error("Error with getting germplasm with id: " + gid, ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting germplasm with id: " + gid  
                    + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            return false;
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
        listData.setList(germplasmList);
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
            Object desigObject;
            if (!fromUrl) {
                // make GID as link only if the page wasn't directly accessed from the URL
                String gidString = String.format("%s", gid.toString());
                Button gidButton = new Button(gidString, new GidLinkButtonClickListener(gidString,true,true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                gidButton.setDescription("Click to view Germplasm information");
                gidObject = gidButton;
                
                String desigString = listData.getDesignation();
                Button desigButton = new Button(desigString, new GidLinkButtonClickListener(gidString,true,true));
                desigButton.setStyleName(BaseTheme.BUTTON_LINK);
                desigButton.setDescription("Click to view Germplasm information");
                desigObject = desigButton;
            } else {
                gidObject = gid;
                desigObject = listData.getDesignation();
            }
            
            // create table if added entry is first listdata record
            if (listDataTable == null){
                if (noListDataLabel != null){
                    removeComponent(noListDataLabel);
                }
                initializeListDataTable();
                initializeValues();
                
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
//                  ,ListDataTablePropertyID.STATUS.getName()
                });
                
                
                CheckBox itemCheckBox = new CheckBox();
                itemCheckBox.setData(listData.getId());
                itemCheckBox.setImmediate(true);
                itemCheckBox.addListener(new ClickListener() {
                    private static final long serialVersionUID = 1L;
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
                        desigObject, listData.getGroupName()
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
                
                saveChangesAction(this.getWindow(), false);
                listDataTable.refreshRowCache();
                listDataTable.setImmediate(true);
                listDataTable.setEditable(true);
            }
            
            if(showSuccessMessage){
            	MessageNotifier.showMessage(this.getWindow(), 
                    messageSource.getMessage(Message.SUCCESS), 
                    "Successful in adding list entries.", 3000, Notification.POSITION_CENTERED);
            }
            
            User user = workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());

            ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                            workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                            "Added list entry.", 
                            "Added " + gid + " as list entry to " + germplasmList.getId() + ":" + germplasmList.getName(),user,new Date());
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
            
            
            doneInitializing = true;
            return true;
            
        } catch (MiddlewareQueryException ex) {
            LOG.error("Error with adding list entry.", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with adding list entry. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            return false;
        }
		
    }
    
    private boolean isColumnVisible(Object[] columns, String columnName){
        
        for(Object col : columns){
            if(col.equals(columnName)){
                return true;
            }
        }
        
        return false;
    }
    
    public void saveChangesAction() throws InternationalizableException {
        saveChangesAction(this.getWindow());
    }

    public Boolean saveChangesAction(Window window) throws InternationalizableException {
    	return saveChangesAction(window, true);
    }
    
    public Boolean saveChangesAction(Window window, Boolean showSuccessMessage) throws InternationalizableException {
        
        //selected entries to entries       
        if(itemsToDelete.size() > 0){
            performListEntriesDeletion(itemsToDelete);
        }
        
        try {
            listEntriesCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmList.getId());
            listEntries = this.germplasmListManager.getGermplasmListDataByListId(germplasmList.getId(), 0, (int) listEntriesCount);
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
            for (GermplasmListData listData : listEntries) {
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
                    
                    Button desigButton = (Button) (item.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName())).getValue();
                    String designation = String.valueOf((desigButton.getCaption().toString()));
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
            
            germplasmListManager.updateGermplasmListData(listEntries);
            germplasmListManager.saveListDataColumns(addColumnContextMenu.getListDataCollectionFromTable(listDataTable));
            
            listDataTable.requestRepaint();
            //reset flag to indicate unsaved changes
            parentListDetailsComponent.setChanged(false);
            
            if(showSuccessMessage){
            	MessageNotifier.showMessage(window, 
                    messageSource.getMessage(Message.SUCCESS), 
                    messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SAVING_SUCCESS)
                    ,3000, Notification.POSITION_CENTERED);
        	}
        
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES);
        }

        //Update counter
		updateListEntriesCountLabel();
        
		return true;
		
    } // end of saveChangesAction
    
    //TODO review this method as there are redundant codes here that is also in saveChangesAction()
    //might be possible to eliminate this method altogether and reduce the number of middleware calls
    private void performListEntriesDeletion(Map<Object, String> itemsToDelete){     
        try {
            if(getCurrentUserLocalId()==germplasmList.getUserId()) {
                designationOfListEntriesDeleted="";
                
                for (Map.Entry<Object, String> item : itemsToDelete.entrySet()) {
                    
                    Object sLRecId = item.getKey();
                    String sDesignation = item.getValue();
                    
                    try {
                        int lrecId=Integer.valueOf(sLRecId.toString());
                        designationOfListEntriesDeleted += sDesignation +",";
                        germplasmListManager.deleteGermplasmListDataByListIdLrecId(germplasmList.getId(), lrecId);
                    } catch (MiddlewareQueryException e) {
                        LOG.error("Error with deleting list entries.", e);
                        e.printStackTrace();
                    }
                }
                
                deleteGermplasmDialogBox(gidsWithoutChildrenToDelete);
                designationOfListEntriesDeleted=designationOfListEntriesDeleted.substring(0,designationOfListEntriesDeleted.length()-1);
    
                //Change entry IDs on listData
                List<GermplasmListData> listDatas = germplasmListManager.getGermplasmListDataByListId(germplasmList.getId(), 0
                            , (int) germplasmListManager.countGermplasmListDataByListId(germplasmList.getId()));
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
    
    private void logDeletedListEntriesToWorkbenchProjectActivity() throws MiddlewareQueryException {
        User user = workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());

        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                "Deleted list entries.", 
                "Deleted list entries from the list id " + germplasmList.getId() + " - " + germplasmList.getName(),user,new Date());
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
    
    public void deleteListButtonClickAction() {
        ConfirmDialog.show(this.getWindow(), "Delete Germplasm List:", "Are you sure that you want to delete this list?", "Yes", "No"
                , new ConfirmDialog.Listener() {
            private static final long serialVersionUID = -6641772458404494412L;

            @Override
			public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    deleteGermplasmListConfirmed();
                }
            }
        });
    }
    
    public void deleteGermplasmListConfirmed() {
        if(!germplasmList.isLockedList()){ 
            try {
                ListCommonActionsUtil.deleteGermplasmList(germplasmListManager, 
                        germplasmList, workbenchDataManager, getWindow(), messageSource, "list");
               
                source.getListSelectionComponent().getListTreeComponent().removeListFromTree(germplasmList);
                source.updateUIForDeletedList(germplasmList);
            } catch (MiddlewareQueryException e) {
                getWindow().showNotification("Error", "There was a problem deleting the germplasm list", Notification.TYPE_ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
	/*SETTERS AND GETTERS*/
	public GermplasmList getGermplasmList(){
		return germplasmList;
	}
	
	public Integer getGermplasmListId(){
		return germplasmList.getId();
	}
	
    public void lockGermplasmList() {
    	if(source.lockGermplasmList(germplasmList)){
	        showHideOptionsForLocked(); // hide enabled elements for unlocked lists
	        recreateTab();
    	}
	}
    
    private void showHideOptionsForLocked() {
        boolean locked = germplasmList.isLockedList();
        lockButton.setVisible(!locked);
        unlockButton.setVisible(locked);
        
        /*menuDeleteEntries.setVisible(!locked);
        menuSaveChanges.setVisible(!locked);
        menuAddEntry.setVisible(!locked);*/
    }
    
    public void unlockGermplasmList() {
        if(germplasmList.isLockedList()){
		    germplasmList.setStatus(germplasmList.getStatus()-100);
		    try {
		        germplasmListManager.updateGermplasmList(germplasmList);
		
		        recreateTab();
		
		        User user = workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
		        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()),
		                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()),
		                "Unlocked a germplasm list.",
		                "Unlocked list "+germplasmList.getId()+" - "+germplasmList.getName(),
		                user,
		                new Date());
		        workbenchDataManager.addProjectActivity(projAct);
		        
	                showHideOptionsForLocked(); // show disabled elements for locked lists
				
		    } catch (MiddlewareQueryException e) {
		        LOG.error("Error with unlocking list.", e);
		        MessageNotifier.showError(getWindow(), "Database Error!", "Error with unlocking list. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
		                , Notification.POSITION_CENTERED);
		    }
        }
    }

    public void openSaveListAsDialog(){
		SaveListAsDialog dialog = new SaveListAsDialog(this, germplasmList, messageSource.getMessage(Message.EDIT_LIST_HEADER));
		this.getWindow().addWindow(dialog);
	}
    
	@Override
	public void saveList(GermplasmList list) {
		try{
			String oldName = germplasmList.getName();
			GermplasmList listFromDB = this.germplasmListManager.getGermplasmListById(germplasmList.getId());
			listFromDB.setName(list.getName());
			listFromDB.setDescription(list.getDescription());
			listFromDB.setDate(list.getDate());
			listFromDB.setType(list.getType());
			listFromDB.setNotes(list.getNotes());
			listFromDB.setParent(list.getParent());
			
			Integer listId = this.germplasmListManager.updateGermplasmList(listFromDB);
			
			if(listId == null){
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
						, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
						, Notification.POSITION_CENTERED);
				return;
			} else{
				germplasmList = listFromDB;
				
				if(!oldName.equals(list.getName())){
					source.getListSelectionComponent().updateUIForRenamedList(germplasmList, list.getName());
				}
				
				source.showNodeOnTree(listFromDB.getId());
				viewListHeaderWindow = new ViewListHeaderWindow(listFromDB);
				viewHeaderButton.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
				MessageNotifier.showMessage(this.getWindow(), messageSource.getMessage(Message.SUCCESS), "Changes to list header were saved."
						, 3000, Notification.POSITION_CENTERED);
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error in updating germplasm list: " + germplasmList.getId(), ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST)
					, Notification.POSITION_CENTERED);
			return;
		}
	}
	
	public void openViewListHeaderWindow(){
		this.getWindow().addWindow(viewListHeaderWindow);
	}

	@Override
	public void finishAddingEntry(List<Integer> gids) {
		Boolean allSuccessful = true;
		for(Integer gid : gids){
			if(finishAddingEntry(gid, false).equals(false)){
				allSuccessful = false;
			}
		}
		if(allSuccessful){
			MessageNotifier.showMessage(getWindow(), 
                    messageSource.getMessage(Message.SUCCESS), 
                    messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SAVING_SUCCESS)
                    ,3000, Notification.POSITION_CENTERED);
		}
	}
	
	private void updateListEntriesCountLabel(){
		int count = listDataTable.getItemIds().size();
		if(count == 0) {
			if(totalListEntriesLabel != null){
				totalListEntriesLabel.setValue(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
				totalListEntriesLabel.setWidth("250px");
			} else if(noListDataLabel != null){
				noListDataLabel.setValue(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
				noListDataLabel.setWidth("250px");
			}
		} else {
			if(totalListEntriesLabel != null){
				totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
		        		 + "  <b>" + count + "</b>");
		    } else if(noListDataLabel != null){
	        	noListDataLabel.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
	        		 + "  <b>" + count + "</b>");
	        	noListDataLabel.setContentMode(Label.CONTENT_XHTML);
	        	noListDataLabel.setWidth("135px");
			}
        }
	}
	
	private void viewInventoryAction(){
		ListInventoryComponent listInventoryComponent = new ListInventoryComponent(this.germplasmList.getId());
		
		if(listInventoryComponent.isThereNoInventoryInfo()){
			MessageNotifier.showWarning(getWindow(), "No Data", messageSource.getMessage(Message.NO_LISTDATA_INVENTORY_RETRIEVED_LABEL), Notification.POSITION_CENTERED);
		} else{
			Window inventoryWindow = new Window("Inventory Information");
			inventoryWindow.setModal(true);
	        inventoryWindow.setWidth("810px");
	        inventoryWindow.setHeight("350px");
	        inventoryWindow.setResizable(false);
	        inventoryWindow.addStyleName(Reindeer.WINDOW_LIGHT);
	        
	        listInventoryComponent.setSizeFull();
	        listInventoryComponent.getTable().setWidth("100%");
	        inventoryWindow.setContent(listInventoryComponent);
	        
	        this.parentListDetailsComponent.getWindow().addWindow(inventoryWindow);
	        inventoryWindow.center();
		}
	}

	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
	}
}


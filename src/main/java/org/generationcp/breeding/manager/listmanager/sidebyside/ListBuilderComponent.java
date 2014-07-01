package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.inventory.ListDataAndLotDetails;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.ListManagerCopyToNewListDialog;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.ResetListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.listeners.SaveListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.breeding.manager.listmanager.util.DropHandlerMethods.ListUpdatedEvent;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
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

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;


@Configurable
public class ListBuilderComponent extends VerticalLayout implements InitializingBean, 
				BreedingManagerLayout, SaveListAsDialogSource, ReserveInventorySource, UnsavedChangesSource {
	private static final long serialVersionUID = 4997159450197570044L;

	private static final Logger LOG = LoggerFactory.getLogger(ListBuilderComponent.class);
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
	@Autowired
	private InventoryDataManager inventoryDataManager;
    
    public static final String GERMPLASMS_TABLE_DATA = "Germplasms Table Data";
    static final Action ACTION_SELECT_ALL = new Action("Select All");
    static final Action ACTION_DELETE_SELECTED_ENTRIES = new Action("Delete Selected Entries");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE_SELECTED_ENTRIES };
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU_LOCKED = new Action[] { ACTION_SELECT_ALL };
    
    public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    //Components
    private Label buildNewListTitle;
    private Label buildNewListDesc;
    private Label topLabel;
    private Label totalListEntriesLabel;
    private BreedingManagerListDetailsComponent breedingManagerListDetailsComponent;
    private TableWithSelectAllLayout tableWithSelectAllLayout;
    private Table listDataTable;
    private ViewListHeaderWindow viewListHeaderWindow;
    
    private Button editHeaderButton;
    private Button viewHeaderButton;
    private Button toolsButton;
    private Button inventoryViewToolsButton;
    private Button saveButton;
    private Button resetButton;
    private Button lockButton;
    private Button unlockButton;
    
    private FillWith fillWith;
    
    private Window listManagerCopyToNewListDialog;
    
    //String Literals
    public static String LOCK_BUTTON_ID = "Lock Germplasm List";
    public static String UNLOCK_BUTTON_ID = "Unlock Germplasm List";    
    private static String LOCK_TOOLTIP = "Click to lock or unlock this germplasm list.";
    public static String TOOLS_BUTTON_ID = "Actions";
    public static String INVENTORY_TOOLS_BUTTON_ID = "Actions";
    private static final String USER_HOME = "user.home";
    
    //Layout Component
    private AbsoluteLayout toolsButtonContainer;
    
    //Context Menus
    private ContextMenu menu;
    private ContextMenuItem menuExportList;
    private ContextMenuItem menuCopyToList;
    private ContextMenuItem menuDeleteSelectedEntries;
    private AddColumnContextMenu addColumnContextMenu;
    private Action.Handler contextMenuActionHandler;
    
    private ContextMenu inventoryViewMenu;
	private ContextMenuItem menuCopyToNewListFromInventory;
	
    //For Saving
    private ListManagerMain source;
    private GermplasmList currentlySavedGermplasmList;
    private GermplasmList currentlySetGermplasmInfo;
    private Boolean hasChanges = false;
    
    //Listener
    private SaveListButtonClickListener saveListButtonListener;
    private BuildNewListDropHandler dropHandler;
    
    //Inventory Related Variables
    private ListInventoryTable listInventoryTable;
    private ReserveInventoryWindow reserveInventory;
    private ReservationStatusWindow reservationStatus;
    private ReserveInventoryUtil reserveInventoryUtil;
    private ReserveInventoryAction reserveInventoryAction;
    private Map<ListEntryLotDetails, Double> validReservationsToSave;
    
    public ListBuilderComponent() {
        super();
    }
    
    public ListBuilderComponent(ListManagerMain source) {
        super();
        this.source = source;
        this.currentlySavedGermplasmList = null;
        this.currentlySetGermplasmInfo = null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		initializeHandlers();
    }
    
	@Override
	public void instantiateComponents() {

	    unlockButton = new IconButton("<span class='bms-locked' style='position: relative; top:5px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>", LOCK_TOOLTIP);
        unlockButton.setData(UNLOCK_BUTTON_ID);
        unlockButton.setVisible(false);
	
        lockButton = new IconButton("<span class='bms-lock-open' style='position: relative; top:5px; left: 2px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>", LOCK_TOOLTIP);
        lockButton.setData(LOCK_BUTTON_ID);
        lockButton.setVisible(false);
        
    	buildNewListTitle = new Label(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
    	buildNewListTitle.setWidth("200px");
        buildNewListTitle.setStyleName(Bootstrap.Typography.H4.styleName());
        buildNewListTitle.addStyleName(AppConstants.CssStyles.BOLD);
        
        buildNewListDesc = new Label();
        buildNewListDesc.setValue("Build or revise your list by dragging in entries from the left.");
        buildNewListDesc.setWidth("500px");
        
        topLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
        topLabel.setWidth("160px");
        topLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		totalListEntriesLabel = new Label(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
       		 + "  <b>" + 0 + "</b>", Label.CONTENT_XHTML);

        editHeaderButton = new Button(messageSource.getMessage(Message.EDIT_HEADER));
        editHeaderButton.setImmediate(true);
        editHeaderButton.setStyleName(Reindeer.BUTTON_LINK);
        
		viewHeaderButton = new Button(messageSource.getMessage(Message.VIEW_HEADER));
		viewHeaderButton.addStyleName(Reindeer.BUTTON_LINK);
		viewHeaderButton.setVisible(false);
		
		if(currentlySavedGermplasmList!=null){
        	viewListHeaderWindow = new ViewListHeaderWindow(currentlySavedGermplasmList);
        	viewHeaderButton.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
        }
        
        breedingManagerListDetailsComponent = new BreedingManagerListDetailsComponent();
        
        tableWithSelectAllLayout = new TableWithSelectAllLayout(ListDataTablePropertyID.TAG.getName());

        if(currentlySavedGermplasmList!=null)
        	listInventoryTable = new ListInventoryTable(source, currentlySavedGermplasmList.getId(), false, true);
        else
        	listInventoryTable = new ListInventoryTable(source, null, false, true);
        listInventoryTable.setVisible(false);
        
        listDataTable = tableWithSelectAllLayout.getTable();
        createGermplasmTable(listDataTable);
        
        listDataTable.setWidth("100%");
		listDataTable.setHeight("480px");
        
        menu = new ContextMenu();
        menu.setWidth("300px");
        
        addColumnContextMenu = new AddColumnContextMenu(this, menu, 
        		tableWithSelectAllLayout.getTable(), ListDataTablePropertyID.GID.getName(),true);
        menuCopyToList = menu.addItem(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
        menuDeleteSelectedEntries = menu.addItem(messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
        menuExportList = menu.addItem(messageSource.getMessage(Message.EXPORT_LIST));
        //menuExportForGenotypingOrder = menu.addItem(messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING));
        menu.addItem(messageSource.getMessage(Message.INVENTORY_VIEW));
        menu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        
        inventoryViewMenu = new ContextMenu();
        inventoryViewMenu.setWidth("300px");
        
        menuCopyToNewListFromInventory = inventoryViewMenu.addItem(messageSource.getMessage(Message.COPY_TO_NEW_LIST));
        inventoryViewMenu.addItem(messageSource.getMessage(Message.RESERVE_INVENTORY));
        inventoryViewMenu.addItem(messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
        inventoryViewMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        
        //Temporarily disable to Copy to New List in InventoryView TODO implement the function
        menuCopyToNewListFromInventory.setEnabled(false);
        
        resetMenuOptions();
        resetInventoryMenuOptions();
        
        toolsButton = new Button(messageSource.getMessage(Message.ACTIONS));
		toolsButton.setData(TOOLS_BUTTON_ID);
		toolsButton.setIcon(AppConstants.Icons.ICON_TOOLS);
		toolsButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		toolsButton.setWidth("110px");
        toolsButton.addStyleName("lm-tools-button");
        
        inventoryViewToolsButton = new Button(messageSource.getMessage(Message.ACTIONS));
        inventoryViewToolsButton.setData(TOOLS_BUTTON_ID);
        inventoryViewToolsButton.setIcon(AppConstants.Icons.ICON_TOOLS);
        inventoryViewToolsButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        inventoryViewToolsButton.setWidth("110px");
        inventoryViewToolsButton.addStyleName("lm-tools-button");        
        
        dropHandler = new BuildNewListDropHandler(source, germplasmDataManager, germplasmListManager, inventoryDataManager, tableWithSelectAllLayout.getTable());
        
        saveButton = new Button();
        saveButton.setCaption(messageSource.getMessage(Message.SAVE_LABEL));
        saveButton.setWidth("80px");
        saveButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        
        resetButton = new Button();
        resetButton.setCaption(messageSource.getMessage(Message.RESET));
        resetButton.setWidth("80px");
        resetButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());
        
        //Inventory Related Variables
        validReservationsToSave = new HashMap<ListEntryLotDetails, Double>();
	}
	
    public void resetMenuOptions(){
        //initially disabled when the current list building is not yet save or being reset
        menuExportList.setEnabled(false);
        menuCopyToList.setEnabled(false);
    }
    
    private void resetInventoryMenuOptions() {
        //Temporarily disable to Copy to New List in InventoryView TODO implement the function
        menuCopyToNewListFromInventory.setEnabled(false);
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		
		fillWith = new FillWith(this, messageSource, tableWithSelectAllLayout.getTable(), ListDataTablePropertyID.GID.getName());
		
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2331333436994090161L;

            @Override
            public void contextItemClick(ClickEvent event) {
                ContextMenuItem clickedItem = event.getClickedItem();
                Table germplasmsTable = tableWithSelectAllLayout.getTable();
                if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
                      germplasmsTable.setValue(germplasmsTable.getItemIds());
                }else if(clickedItem.getName().equals(messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))){
                      deleteSelectedEntries();
                }else if(clickedItem.getName().equals(messageSource.getMessage(Message.EXPORT_LIST))){
                      exportListAction();
                }else if(clickedItem.getName().equals(messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER))){
                      exportListForGenotypingOrderAction();
                }else if(clickedItem.getName().equals(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL))){
                      copyToNewListAction();
                }else if(clickedItem.getName().equals(messageSource.getMessage(Message.INVENTORY_VIEW))){
                	  viewInventoryAction();
                }                
            }
            
        });
		
		inventoryViewMenu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
	
			@Override
			public void contextItemClick(ClickEvent event) {
			      // Get reference to clicked item
			      ContextMenuItem clickedItem = event.getClickedItem();
			      if(clickedItem.getName().equals(messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))){
                	  viewListAction();
                  } else if(clickedItem.getName().equals(messageSource.getMessage(Message.COPY_TO_NEW_LIST))){
                	  copyToNewListFromInventoryViewAction();
				  } else if(clickedItem.getName().equals(messageSource.getMessage(Message.RESERVE_INVENTORY))){
		          	  reserveInventoryAction();
                  } else if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
                	  listInventoryTable.getTable().setValue(listInventoryTable.getTable().getItemIds());
		          }      
		    }
		});		
		
		toolsButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1345004576139547723L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
            	
                if(isCurrentListSaved()){
                    enableMenuOptionsAfterSave();
                }
                
                addColumnContextMenu.refreshAddColumnMenu();
                menu.show(event.getClientX(), event.getClientY());
                
            }
         });

		inventoryViewToolsButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1345004576139547723L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                inventoryViewMenu.show(event.getClientX(), event.getClientY());
            }
         });
		
		editHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -6306973449416812850L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openSaveListAsDialog();
			}
		});
		
		viewHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				viewListHeaderWindow = new ViewListHeaderWindow(currentlySavedGermplasmList);
				getWindow().addWindow(viewListHeaderWindow);
			}
		});
		
		saveListButtonListener = new SaveListButtonClickListener(this, germplasmListManager, tableWithSelectAllLayout.getTable(), 
										messageSource, workbenchDataManager, inventoryDataManager); 
		saveButton.addListener(saveListButtonListener);
		
		saveButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 7449465533478658983L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				if(currentlySetGermplasmInfo == null){
					openSaveListAsDialog();
				}
			}
		});
		
		resetButton.addListener(new ResetListButtonClickListener(this, messageSource));

		//Lock button action
		lockButton.addListener(new ClickListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
            	if(!currentlySavedGermplasmList.isLockedList()){
            		currentlySavedGermplasmList.setStatus(currentlySavedGermplasmList.getStatus() + 100);
        		    try {
        		    	currentlySetGermplasmInfo = currentlySavedGermplasmList;
        		    	saveListButtonListener.doSaveAction(false);
        		
        		        User user = workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
        		        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()),
        		                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()),
        		                "Locked a germplasm list.",
        		                "Locked list "+currentlySavedGermplasmList.getId()+" - "+currentlySavedGermplasmList.getName(),
        		                user,
        		                new Date());
        		        workbenchDataManager.addProjectActivity(projAct);
        		    } catch (MiddlewareQueryException e) {
        		        LOG.error("Error with unlocking list.", e);
        	            MessageNotifier.showError(getWindow(), "Database Error!", "Error with loocking list. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
        	                    , Notification.POSITION_CENTERED);
        		    }
                	setUIForLockedList();
        		}
            }
        });
        
		//Unlock button action
        unlockButton.addListener(new ClickListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
            	if(currentlySavedGermplasmList.isLockedList()){
            		currentlySavedGermplasmList.setStatus(currentlySavedGermplasmList.getStatus() - 100);
        		    try {
        		    	currentlySetGermplasmInfo = currentlySavedGermplasmList;
        		    	saveListButtonListener.doSaveAction(false);
        		
        		        User user = workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
        		        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()),
        		                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()),
        		                "Unlocked a germplasm list.",
        		                "Unlocked list "+currentlySavedGermplasmList.getId()+" - "+currentlySavedGermplasmList.getName(),
        		                user,
        		                new Date());
        		        workbenchDataManager.addProjectActivity(projAct);
        		    } catch (MiddlewareQueryException e) {
        		        LOG.error("Error with unlocking list.", e);
        	            MessageNotifier.showError(getWindow(), "Database Error!", "Error with unlocking list. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
        	                    , Notification.POSITION_CENTERED);
        		    }
                	setUIForUnlockedList();
        		}
            }
        });
		
	}

	public void setContextMenuActionHandler(final Table table){
		
		table.removeActionHandler(contextMenuActionHandler);
		
		contextMenuActionHandler = new Action.Handler() {

            private static final long serialVersionUID = 1884343225476178686L;

            @Override
			public Action[] getActions(Object target, Object sender) {
                return getContextMenuActions();
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if(ACTION_SELECT_ALL == action) {
                	table.setValue(table.getItemIds());
                } else if(ACTION_DELETE_SELECTED_ENTRIES == action) {
                    deleteSelectedEntries();
                }
            }
        };
        
        table.addActionHandler(contextMenuActionHandler);
	}
	
	public Action[] getContextMenuActions(){
		if(currentlySavedGermplasmList!=null && currentlySavedGermplasmList.isLockedList())
			return GERMPLASMS_TABLE_CONTEXT_MENU_LOCKED;
		return GERMPLASMS_TABLE_CONTEXT_MENU;
	}
	
	public void setUIForLockedList(){
    	lockButton.setVisible(false);
    	unlockButton.setVisible(true);
		tableWithSelectAllLayout.getTable().setDropHandler(null);
    	setContextMenuActionHandler(listDataTable);
    	menuDeleteSelectedEntries.setVisible(false);
    	addColumnContextMenu.setVisible(false);
    	editHeaderButton.setVisible(false);
    	viewHeaderButton.setVisible(true);
    	
    	viewListHeaderWindow = new ViewListHeaderWindow(currentlySavedGermplasmList);
    	viewHeaderButton.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
    	
    	saveButton.setEnabled(false);
    	
    	source.setUIForLockedListBuilder();
    	fillWith.setContextMenuEnabled(false);
	}
	
	public void setUIForUnlockedList(){
		lockButton.setVisible(true);
    	unlockButton.setVisible(false);
		tableWithSelectAllLayout.getTable().setDropHandler(dropHandler);
    	setContextMenuActionHandler(listDataTable);
    	menuDeleteSelectedEntries.setVisible(true);
    	addColumnContextMenu.setVisible(true);
    	editHeaderButton.setVisible(true);
    	viewHeaderButton.setVisible(false);
    	saveButton.setEnabled(true);
    	source.setUIForUnlockedListBuilder();
    	fillWith.setContextMenuEnabled(true);
	}

	public void setUIForNewList(){
		lockButton.setVisible(false);
    	unlockButton.setVisible(false);
		tableWithSelectAllLayout.getTable().setDropHandler(dropHandler);
    	setContextMenuActionHandler(listDataTable);
    	menuDeleteSelectedEntries.setVisible(true);
    	addColumnContextMenu.setVisible(true);
    	editHeaderButton.setVisible(true);
    	viewHeaderButton.setVisible(false);
    	saveButton.setEnabled(true);
	}	
	
	@Override
	public void layoutComponents() {
        this.setWidth("100%");
        this.setMargin(new MarginInfo(true,true,false,false));
        this.setSpacing(false);

		final HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_BUILD_NEW_LIST, buildNewListTitle);

        final VerticalLayout listBuilderHeadingContainer = new VerticalLayout();

        listBuilderHeadingContainer.addComponent(headingLayout);
        listBuilderHeadingContainer.addComponent(buildNewListDesc);

        buildNewListDesc.setStyleName("lm-subtitle");

        this.addComponent(listBuilderHeadingContainer);


        final Panel listBuilderPanel = new Panel();
        listBuilderPanel.setStyleName(Reindeer.PANEL_LIGHT + " "+AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
        listBuilderPanel.setCaption(null);
        listBuilderPanel.setWidth("100%");

        final VerticalLayout listDataTableLayout = new VerticalLayout();
        listDataTableLayout.setMargin(true);
        listDataTableLayout.setSpacing(true);
        listDataTableLayout.setSizeFull();
        listDataTableLayout.addStyleName("listDataTableLayout");

        listBuilderPanel.setContent(listDataTableLayout);


        final HorizontalLayout listBuilderPanelTitleContainer = new HorizontalLayout();
        listBuilderPanelTitleContainer.setWidth("100%");
        listBuilderPanelTitleContainer.setSpacing(true);

        HeaderLabelLayout listEntriesTitle = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, topLabel);
        listBuilderPanelTitleContainer.addComponent(listEntriesTitle);


        listBuilderPanelTitleContainer.addComponent(viewHeaderButton);
        listBuilderPanelTitleContainer.addComponent(editHeaderButton);
        listBuilderPanelTitleContainer.addComponent(lockButton);
        listBuilderPanelTitleContainer.addComponent(unlockButton);

        listBuilderPanelTitleContainer.setExpandRatio(listEntriesTitle,1.0f);

        listBuilderPanelTitleContainer.setComponentAlignment(viewHeaderButton,Alignment.BOTTOM_LEFT);
        listBuilderPanelTitleContainer.setComponentAlignment(editHeaderButton,Alignment.BOTTOM_LEFT);
        listBuilderPanelTitleContainer.setComponentAlignment(lockButton, Alignment.BOTTOM_RIGHT);
        listBuilderPanelTitleContainer.setComponentAlignment(unlockButton, Alignment.BOTTOM_RIGHT);

        toolsButtonContainer = new AbsoluteLayout();
        toolsButtonContainer.setHeight("27px");
        toolsButtonContainer.setWidth("120px");
        toolsButtonContainer.addComponent(toolsButton, "top:0; right:0");
        
        final HorizontalLayout toolsLayout = new HorizontalLayout();
        toolsLayout.setWidth("100%");
        toolsLayout.addComponent(totalListEntriesLabel);
        toolsLayout.addComponent(toolsButtonContainer);
        toolsLayout.setComponentAlignment(totalListEntriesLabel,Alignment.MIDDLE_LEFT);
        toolsLayout.setExpandRatio(totalListEntriesLabel, 1.0F);

        listDataTableLayout.addComponent(listBuilderPanelTitleContainer);
        listDataTableLayout.addComponent(toolsLayout);
        listDataTableLayout.addComponent(tableWithSelectAllLayout);
        listDataTableLayout.addComponent(listInventoryTable);

        this.addComponent(listBuilderPanel);

        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setMargin(new MarginInfo(false,false,true,false));
        buttons.setWidth("170px");
        buttons.addComponent(saveButton);
        buttons.addComponent(resetButton);
        buttons.setSpacing(true);
        buttons.addStyleName("lm-new-list-buttons");

        this.addComponent(buttons);
        this.addComponent(menu);
        this.addComponent(inventoryViewMenu);

	}
    
	private void addBasicTableColumns(Table table){
    	table.setData(GERMPLASMS_TABLE_DATA);
    	table.addContainerProperty(ListDataTablePropertyID.TAG.getName(), CheckBox.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.AVAIL_INV.getName(), Button.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.SEED_RES.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
    	
        messageSource.setColumnHeader(table, ListDataTablePropertyID.TAG.getName(), Message.CHECK_ICON);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.ENTRY_ID.getName(), Message.HASHTAG);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.PARENTAGE.getName(), Message.LISTDATA_GROUPNAME_HEADER);
		messageSource.setColumnHeader(table, ListDataTablePropertyID.AVAIL_INV.getName(), Message.LISTDATA_AVAIL_INV_HEADER);
		messageSource.setColumnHeader(table, ListDataTablePropertyID.SEED_RES.getName(), Message.LISTDATA_SEED_RES_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
	}
	
    private void createGermplasmTable(final Table table){
        
    	addBasicTableColumns(table);
        
    	table.setDragMode(TableDragMode.ROW);
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setWidth("100%");
        table.setHeight("280px");
        
        setContextMenuActionHandler(table);
    }
    
    
    private void initializeHandlers() {
		
    	dropHandler.addListener(new BuildNewListDropHandler.ListUpdatedListener() {
			@Override
			public void listUpdated(final ListUpdatedEvent event) {
				updateListTotal();
			}
			
		});
		
    	tableWithSelectAllLayout.getTable().setDropHandler(dropHandler);
    }
    
    @SuppressWarnings("unchecked")
    private void deleteSelectedEntries(){
        Collection<? extends Integer> selectedIdsToDelete = (Collection<? extends Integer>) tableWithSelectAllLayout.getTable().getValue();
        if(selectedIdsToDelete.size() > 0){
            if (!isCurrentListSaved()) {
                // TODO directly remove list entries if list being built is not yet saved.
            } else {
                // TODO make behavior consistent with ListDataComponent.deleteEntriesButtonClickAction()
                // for editing existing lists and after saving the new list
                
                // if list is already saved
                /*if(tableWithSelectAllLayout.getTable().size() == selectedIdsToDelete.size()) {
                    ConfirmDialog.show(this.getWindow(),
                            messageSource.getMessage(Message.DELETE_ALL_ENTRIES),
                            messageSource.getMessage(Message.DELETE_ALL_ENTRIES_CONFIRM),
                            messageSource.getMessage(Message.YES),
                            messageSource.getMessage(Message.NO),
                            new ConfirmDialog.Listener() {
                        private static final long serialVersionUID = 1L;
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                removeRowsInListDataTable((Collection<?>)listDataTable.getValue());
                            }
                        }
                        
                    });
                } else {
                    removeRowsInListDataTable(selectedIdsToDelete);
                }*/
            }
            
            //TODO replace with more advanced deletion handling
            for(Integer selectedItemId : selectedIdsToDelete){
                tableWithSelectAllLayout.getTable().removeItem(selectedItemId);
            }
            assignSerializedEntryNumber();
            setHasUnsavedChanges(true);
        }else{
            MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DELETING_LIST_ENTRIES)
                    , messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
        
        updateListTotal();
    }
    
    private void updateListTotal() {
    	final int count = tableWithSelectAllLayout.getTable().getItemIds().size();
    	if(!listInventoryTable.isVisible())
    		totalListEntriesLabel.setValue(new Label(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
	       		 + "  <b>" + count + "</b>", Label.CONTENT_XHTML));
		
	}

	/**
     * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
     */
    private void assignSerializedEntryNumber(){
        List<Integer> itemIds = getItemIds(tableWithSelectAllLayout.getTable());
                
        int id = 1;
        for(Integer itemId : itemIds){
        	tableWithSelectAllLayout.getTable().getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(id);
            id++;
        }
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
    
    public void addFromListDataTable(Table sourceTable){
    	dropHandler.addFromListDataTable(sourceTable);
    }
    
    public boolean isCurrentListSaved(){
        boolean isSaved = false;
        
        if(currentlySavedGermplasmList != null){

            isSaved = true;
        }
        
        return isSaved;
    }

    public void enableMenuOptionsAfterSave(){
        menuExportList.setEnabled(true);
        //menuExportForGenotypingOrder.setEnabled(true);
        menuCopyToList.setEnabled(true);
    }
    
	public void editList(GermplasmList germplasmList) {
		resetList(); //reset list before placing new one
		
		buildNewListTitle.setValue(messageSource.getMessage(Message.EDIT_LIST));
		
		currentlySavedGermplasmList = germplasmList;
		currentlySetGermplasmInfo = germplasmList;
		
		dropHandler.addGermplasmList(germplasmList.getId(), true);
        
        //reset the change status to false after loading the germplasm list details and list data in the screen
        resetUnsavedChangesFlag();
        
        if(germplasmList.isLockedList()){
        	setUIForLockedList();
        } else {
        	setUIForUnlockedList();
        }
    }
	
	public void addListsFromSearchResults(Set<Integer> lists) {
		dropHandler.setHasUnsavedChanges(true);
		for (Integer id : lists) {
			if(id != null){
				dropHandler.addGermplasmList(id, false);
			}
			
		}
    }
	public void resetList(){
		
		//list details fields
		breedingManagerListDetailsComponent.resetFields();
		
		//list data table
		resetGermplasmTable();
		
		//list inventory table
		listInventoryTable.reset();
		
		//disabled the menu options when the build new list table has no rows
		resetMenuOptions();
		
		//Clear flag, this is used for saving logic (to save new list or update)
		setCurrentlySavedGermplasmList(null);
		currentlySetGermplasmInfo = null;

		//Rename the Build New List Header
		buildNewListTitle.setValue(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
		
		dropHandler = new BuildNewListDropHandler(source, germplasmDataManager, germplasmListManager, inventoryDataManager, tableWithSelectAllLayout.getTable());
		initializeHandlers();
		
		//Reset Save Listener
		saveButton.removeListener(saveListButtonListener);
		saveListButtonListener = new SaveListButtonClickListener(this, germplasmListManager, tableWithSelectAllLayout.getTable(), 
									messageSource, workbenchDataManager, inventoryDataManager); 
		saveButton.addListener(saveListButtonListener);
		
		updateListTotal();
		
		//Reset the marker for changes in Build New List
		resetUnsavedChangesFlag();
		updateView(source.getModeView());
	}
	
	public void updateView(ModeView modeView) {
		if(modeView.equals(ModeView.LIST_VIEW)){
			changeToListView();
		} else if(modeView.equals(ModeView.INVENTORY_VIEW)){
			changeToInventoryView();
		}
	}

	public void resetGermplasmTable(){		
		tableWithSelectAllLayout.getTable().removeAllItems();
		for(Object col: tableWithSelectAllLayout.getTable().getContainerPropertyIds().toArray())  {
			tableWithSelectAllLayout.getTable().removeContainerProperty(col);
		}
		tableWithSelectAllLayout.getTable().setWidth("100%");
		addBasicTableColumns(tableWithSelectAllLayout.getTable());
		
		updateListTotal();
	}
	
    public GermplasmList getCurrentlySetGermplasmListInfo(){
    	if(currentlySetGermplasmInfo != null){
	        String name = currentlySetGermplasmInfo.getName();
	        if(name != null){
	            currentlySetGermplasmInfo.setName(name.trim());
	        }
	        
	        String description = currentlySetGermplasmInfo.getDescription();
	        if(description != null){
	            currentlySetGermplasmInfo.setDescription(description.trim());
	        }
    	}
        
        return currentlySetGermplasmInfo;
    }
    
    public void addGermplasm(Integer gid){
    	dropHandler.addGermplasm(gid);
    	setHasUnsavedChanges(true);
    }
    
    public List<GermplasmListData> getListEntriesFromTable(){
        List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
        
        assignSerializedEntryNumber();
        
        for(Object id : this.tableWithSelectAllLayout.getTable().getItemIds()){
            Integer entryId = (Integer) id;
            Item item = this.tableWithSelectAllLayout.getTable().getItem(entryId);
            
            GermplasmListData listEntry = new GermplasmListData();
            listEntry.setId(entryId);
            
            
            Button designationButton = (Button)  item.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue();
            String designation = designationButton.getCaption();
            if(designation != null){
                listEntry.setDesignation(designation);
            } else{
                listEntry.setDesignation("-");
            }
            
            Object entryCode = item.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue();
            if(entryCode != null){
                listEntry.setEntryCode(entryCode.toString());
            } else{
                listEntry.setEntryCode("-");
            }
            
            Button gidButton = (Button) item.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
            listEntry.setGid(Integer.parseInt(gidButton.getCaption()));
            
            Object groupName = item.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).getValue();
            if(groupName != null){
                String groupNameString = groupName.toString();
                if(groupNameString.length() > 255){
                    groupNameString = groupNameString.substring(0, 255);
                }
                listEntry.setGroupName(groupNameString);
            } else{
                listEntry.setGroupName("-");
            }
            
            listEntry.setEntryId((Integer) item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue());
            
            Object seedSource = item.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).getValue();
            if(seedSource != null){
                listEntry.setSeedSource(seedSource.toString());
            } else{
                listEntry.setSeedSource("-");
            }
            
            toreturn.add(listEntry);
        }
        return toreturn;
    }
    
    private void exportListAction() throws InternationalizableException {
        if (currentlySavedGermplasmList != null) {
            if(!currentlySavedGermplasmList.isLocalList() || (currentlySavedGermplasmList.isLocalList() && currentlySavedGermplasmList.isLockedList())){
                String tempFileName = System.getProperty( USER_HOME ) + "/temp.xls";
                GermplasmListExporter listExporter = new GermplasmListExporter(currentlySavedGermplasmList.getId());
                try {
                    listExporter.exportGermplasmListExcel(tempFileName);
                    FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), source.getApplication());
                    String listName = currentlySavedGermplasmList.getName();
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
    }
    
    private void exportListForGenotypingOrderAction() throws InternationalizableException {
        if (isCurrentListSaved()) {
            if(!currentlySavedGermplasmList.isLocalList() || (currentlySavedGermplasmList.isLocalList() && currentlySavedGermplasmList.isLockedList())){
                String tempFileName = System.getProperty( USER_HOME ) + "/tempListForGenotyping.xls";
                GermplasmListExporter listExporter = new GermplasmListExporter(currentlySavedGermplasmList.getId());
                
                try {
                    listExporter.exportListForKBioScienceGenotypingOrder(tempFileName, 96);
                    FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), source.getApplication());
                    String listName = currentlySavedGermplasmList.getName();
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
    }
    
    public void copyToNewListAction(){
        if(isCurrentListSaved()){
            Collection<?> listEntries = (Collection<?>) tableWithSelectAllLayout.getTable().getValue();
            
            if (listEntries == null || listEntries.isEmpty()){
                MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), "", Notification.POSITION_CENTERED);
            } else {
                listManagerCopyToNewListDialog = new Window(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
                listManagerCopyToNewListDialog.setModal(true);
                listManagerCopyToNewListDialog.setWidth("617px");
                listManagerCopyToNewListDialog.setHeight("230px");
                listManagerCopyToNewListDialog.setResizable(false);
                listManagerCopyToNewListDialog.addStyleName(Reindeer.WINDOW_LIGHT);
                
                try {
                    listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(
                        source.getWindow(),
                        listManagerCopyToNewListDialog,
                        currentlySavedGermplasmList.getName(),
                        tableWithSelectAllLayout.getTable(),
                        getCurrentUserLocalId(),
                        source,
                        true));
                    source.getWindow().addWindow(listManagerCopyToNewListDialog);
                    listManagerCopyToNewListDialog.center();
                } catch (MiddlewareQueryException e) {
                    LOG.error("Error copying list entries.", e);
                    e.printStackTrace();
                }
            }
        }
    }// end of copyToNewListAction
    
    private void copyToNewListFromInventoryViewAction(){
    	// TODO implement the copy to new list from the selection from listInventoryTable
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
	
	/* SETTERS AND GETTERS */

	public Label getBuildNewListTitle() {
		return buildNewListTitle;
	}
	
	public void setBuildNewListTitle(Label buildNewListTitle) {
		this.buildNewListTitle = buildNewListTitle;
	}
	
	public BreedingManagerListDetailsComponent getBreedingManagerListDetailsComponent() {
		return breedingManagerListDetailsComponent;
	}
	
    public TableWithSelectAllLayout getTableWithSelectAllLayout() {
		return tableWithSelectAllLayout;
	}

	public void setTableWithSelectAllLayout(TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;
	}

	public Table getGermplasmsTable(){
        return tableWithSelectAllLayout.getTable();
    }

	public Button getSaveButton() {
		return saveButton;
	}

	public void setSaveButton(Button saveButton) {
		this.saveButton = saveButton;
	}
	
    public GermplasmList getCurrentlySavedGermplasmList(){
        return this.currentlySavedGermplasmList;
    }
    
    @Override
    public void setCurrentlySavedGermplasmList(GermplasmList list){
        this.currentlySavedGermplasmList = list;
        if(list!=null && list.getId()!=null){
        	if(list.getStatus()<100){
        		setUIForUnlockedList();
        	} else {
        		setUIForLockedList();
        	}
        } else {
        	setUIForNewList();
        }
    }
	
    public ListManagerMain getSource(){
    	return source;
    }
    
    public AddColumnContextMenu getAddColumnContextMenu(){
    	return addColumnContextMenu;
    }

	public void openSaveListAsDialog(){
		SaveListAsDialog dialog = new SaveListAsDialog(this, currentlySavedGermplasmList, messageSource.getMessage(Message.EDIT_LIST_HEADER));
		this.getWindow().addWindow(dialog);
	}

	/**
	 * This method is called by the SaveListAsDialog window displayed when Edit Header button is clicked.
	 */
	@Override
	public void saveList(GermplasmList list) {
		currentlySetGermplasmInfo = list;
		saveListButtonListener.doSaveAction();

		((BreedingManagerApplication) getApplication()).refreshListManagerTree();
		
		resetUnsavedChangesFlag();
		source.updateView(source.getModeView());
	}
	
	public void saveList(GermplasmList list, Boolean showMessages) {
		currentlySetGermplasmInfo = list;
		saveListButtonListener.doSaveAction(showMessages, false);

		((BreedingManagerApplication) getApplication()).refreshListManagerTree();
		
		resetUnsavedChangesFlag();
		source.updateView(source.getModeView());
	}
	
	public SaveListButtonClickListener getSaveListButtonListener(){
		return saveListButtonListener;
	}
	
	public BuildNewListDropHandler getBuildNewListDropHandler(){
		return dropHandler;
	}
	
	
	/*-------------------------------------LIST INVENTORY RELATED METHODS-------------------------------------*/
	
	public void viewListAction(){
		if(!hasUnsavedChanges()){
			source.setModeView(ModeView.LIST_VIEW);
		}else{
			String message = "You have unsaved reservations for this list. " +
					"You will need to save them before changing views. " +
					"Do you want to save your changes?";
    		
			ConfirmDialog.show(getWindow(), "Unsaved Changes", message, messageSource.getMessage(Message.YES), 
						messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {   			
				private static final long serialVersionUID = 1L;
				
				@Override
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						if(currentlySavedGermplasmList == null){
							source.setModeViewOnly(ModeView.LIST_VIEW);
							openSaveListAsDialog();
						}
						else{
							saveList(currentlySavedGermplasmList);
							source.setModeView(ModeView.LIST_VIEW);
						}
					}
					else{
						resetListInventoryTableValues();
						source.setModeView(ModeView.LIST_VIEW);
					}
					
					
				}
			});
		}
	}
	
	public void changeToListView() {
		if(listInventoryTable.isVisible()){
			tableWithSelectAllLayout.setVisible(true);
			listInventoryTable.setVisible(false);
			
			toolsButtonContainer.addComponent(toolsButton, "top:0px; right:0px;");
			toolsButtonContainer.removeComponent(inventoryViewToolsButton);
			
			topLabel.setValue(messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
	        totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
	       		 + "  <b>" + listDataTable.getItemIds().size() + "</b>");
	        
	        resetUnsavedChangesFlag();
		}
	}
	
	public void changeToInventoryView(){
		if(tableWithSelectAllLayout.isVisible()){
			tableWithSelectAllLayout.setVisible(false);
			listInventoryTable.setVisible(true);
	        toolsButtonContainer.removeComponent(toolsButton);
	        toolsButtonContainer.addComponent(inventoryViewToolsButton, "top:0; right:0;");
	        
	        topLabel.setValue(messageSource.getMessage(Message.LOTS));
	        totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LOTS) + ": " 
	          		 + "  <b>" + listInventoryTable.getTable().getItemIds().size() + "</b>");
	        
	        resetUnsavedChangesFlag();
		}
	}
	
	public void viewInventoryAction(){
		
		if(currentlySavedGermplasmList!=null && hasChanges){
			String message = "You have unsaved changes to the list you are editing. You will need to save them before changing views. Do you want to save your changes?";
			
    		ConfirmDialog.show(getWindow(), "Unsaved Changes", message, "Yes", "No", new ConfirmDialog.Listener() {
    			
				private static final long serialVersionUID = 1L;	
				@Override
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						saveListButtonListener.doSaveAction();
						//reset status for list builder and its drop handler
						resetUnsavedChangesFlag();
						source.setModeView(ModeView.INVENTORY_VIEW);
					}
					else{
						//reset status for list builder and its drop handler
						resetUnsavedChangesFlag();
						source.setModeView(ModeView.INVENTORY_VIEW);
					}
					
				}
			});
		} else if(currentlySavedGermplasmList==null && listDataTable.size()>0) {
			String message = "You need to save the list that you're building before you can switch to the inventory view. Do you want to save the list?";
    		ConfirmDialog.show(getWindow(), "Unsaved Changes", message, "Yes", "No", new ConfirmDialog.Listener() {
    			
				private static final long serialVersionUID = 1L;	
				@Override
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						source.setModeViewOnly(ModeView.INVENTORY_VIEW);
						openSaveListAsDialog();
					}
					else{
						resetList();
						source.setModeView(ModeView.INVENTORY_VIEW);
					}
				}
			});
		} else {
			source.setModeView(ModeView.INVENTORY_VIEW);
		}	
	}
	
	public void viewInventoryActionConfirmed(){
		if(currentlySavedGermplasmList!=null){
			listInventoryTable.setListId(currentlySavedGermplasmList.getId());
			listInventoryTable.loadInventoryData();
		}
		
		changeToInventoryView();
		
        refreshListInventoryItemCount();
	}
	
	public void refreshListInventoryItemCount(){
        totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LOTS) + ": " 
         		 + "  <b>" + listInventoryTable.getTable().getItemIds().size() + "</b>");
	}
	
	private void reserveInventoryAction(){
		if(!inventoryViewMenu.isVisible()){//checks if the screen is in the inventory view
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.WARNING), 
					"Please change to Inventory View first.", Notification.POSITION_TOP_RIGHT);
		}
		else{
			List<ListEntryLotDetails> lotDetailsGid = listInventoryTable.getSelectedLots();
			
			if( lotDetailsGid == null || lotDetailsGid.size() == 0){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.WARNING), 
						"Please select at least 1 lot to reserve.", Notification.POSITION_TOP_RIGHT);
			}
			else{
		        //this util handles the inventory reservation related functions
		        reserveInventoryUtil = new ReserveInventoryUtil(this,lotDetailsGid);
				reserveInventoryUtil.viewReserveInventoryWindow();
			}
		}
	}
	
	public void saveReservationChangesAction() {
		
		if(hasUnsavedChanges()){
		
			List<Integer> alreadyAddedEntryIds = new ArrayList<Integer>();
			List<ListDataAndLotDetails> listDataAndLotDetails = listInventoryTable.getInventoryTableDropHandler().getListDataAndLotDetails();
			for(ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails){
				if(!alreadyAddedEntryIds.contains(listDataAndLotDetail.getEntryId())){
					dropHandler.addGermplasmFromList(listDataAndLotDetail.getListId(), listDataAndLotDetail.getSourceLrecId());
					alreadyAddedEntryIds.add(listDataAndLotDetail.getEntryId());
				}
			}
			
		    saveList(currentlySavedGermplasmList, false);
		    
		    for(ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails){
		    	listInventoryTable.getInventoryTableDropHandler().assignLrecIdToRowsFromListWithEntryId(listDataAndLotDetail.getListId(), listDataAndLotDetail.getEntryId());
			}
			
		    listInventoryTable.getInventoryTableDropHandler().resetListDataAndLotDetails();

		
			reserveInventoryAction = new ReserveInventoryAction(this);
			boolean success = reserveInventoryAction.saveReserveTransactions(getValidReservationsToSave(), currentlySavedGermplasmList.getId());
			if(success){
				refreshInventoryColumns(getValidReservationsToSave());
				resetListInventoryTableValues();
			}
		}
	}
	
private void refreshInventoryColumns(Map<ListEntryLotDetails, Double> validReservationsToSave){
		
		Set<Integer> entryIds = new HashSet<Integer>();
		for(Entry<ListEntryLotDetails, Double> details : validReservationsToSave.entrySet()){
			entryIds.add(details.getKey().getId());
		 }
		
		List<GermplasmListData> germplasmListDataEntries = new ArrayList<GermplasmListData>();
		
		try {
			if (!entryIds.isEmpty())
				germplasmListDataEntries = this.inventoryDataManager.getLotCountsForListEntries(currentlySavedGermplasmList.getId(), new ArrayList<Integer>(entryIds));
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		
		for (GermplasmListData listData : germplasmListDataEntries){
			Item item = listDataTable.getItem(listData.getId());
			
			//#1 Available Inventory
			String avail_inv = "-"; //default value
			if(listData.getInventoryInfo().getActualInventoryLotCount() != null){
				avail_inv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			Button inventoryButton = new Button(avail_inv, new InventoryLinkButtonClickListener(source, currentlySavedGermplasmList.getId(),listData.getId(), listData.getGid()));
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription("Click to view Inventory Details");
			
			if(avail_inv.equals("-")){
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription("No Lot for this Germplasm");
			}
			else{
				inventoryButton.setDescription("Click to view Inventory Details");
			}
			item.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).setValue(inventoryButton);
			
		
			// Seed Reserved
	   		String seed_res = "-"; //default value
	   		if(listData.getInventoryInfo().getReservedLotCount() != null && listData.getInventoryInfo().getReservedLotCount() != 0){
	   			seed_res = listData.getInventoryInfo().getReservedLotCount().toString().trim();
	   		}
			
	   		item.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(seed_res);
		}
		
		
	}

	@Override
	public void updateListInventoryTable(
			Map<ListEntryLotDetails, Double> validReservations, boolean withInvalidReservations) {
		for(Map.Entry<ListEntryLotDetails, Double> entry: validReservations.entrySet()){
			ListEntryLotDetails lot = entry.getKey();
			Double new_res = entry.getValue();
			
			Item itemToUpdate = listInventoryTable.getTable().getItem(lot);
			itemToUpdate.getItemProperty(ListInventoryTable.NEWLY_RESERVED_COLUMN_ID).setValue(new_res);
		}
		
		removeReserveInventoryWindow(reserveInventory);
		
		//update lot reservatios to save
		updateLotReservationsToSave(validReservations);
		
		if(validReservations.size() == 0){//if there are no valid reservations
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT), 
					messageSource.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES) + ".");
		
		} else if(!withInvalidReservations){
			MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
					"All selected entries will be reserved in their respective lots.", 
					3000, Notification.POSITION_TOP_RIGHT);
		}
	}

	@Override
	public void addReserveInventoryWindow(
			ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		source.getWindow().addWindow(this.reserveInventory);
	}

	@Override
	public void addReservationStatusWindow(
			ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		removeReserveInventoryWindow(reserveInventory);
		source.getWindow().addWindow(this.reservationStatus);
	}

	@Override
	public void removeReserveInventoryWindow(
			ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		source.getWindow().removeWindow(this.reserveInventory);
	}

	@Override
	public void removeReservationStatusWindow(
			ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		source.getWindow().removeWindow(this.reservationStatus);
		
	}
	
    public void resetListInventoryTableValues() {
    	if(currentlySavedGermplasmList != null){
    		listInventoryTable.updateListInventoryTableAfterSave();
    	}
    	else{
    		listInventoryTable.reset();
    	}
		
		resetInventoryMenuOptions();
		
		validReservationsToSave.clear();//reset the reservations to save. 
		
		resetUnsavedChangesFlag();
	}
    
	private void updateLotReservationsToSave(
			Map<ListEntryLotDetails, Double> validReservations) {
		
		for(Map.Entry<ListEntryLotDetails, Double> entry : validReservations.entrySet()){
			ListEntryLotDetails lot = entry.getKey();
			Double amountToReserve = entry.getValue();
			
			if(validReservationsToSave.containsKey(lot)){
				validReservationsToSave.remove(lot);
				
			}
			
			validReservationsToSave.put(lot,amountToReserve);
		}
		
		if(validReservationsToSave.size() > 0){
			setHasUnsavedChanges(true);
		}
	}
    
	public Map<ListEntryLotDetails, Double> getValidReservationsToSave(){
		return validReservationsToSave;
	}

	@Override
	public Component getParentComponent() {
		return source;
	}

	@Override
	public void setHasChangesMain(boolean hasChanges) {
		source.setHasUnsavedChanges(hasChanges);
	}
	
	public void setHasUnsavedChanges(Boolean hasChanges) {
		this.hasChanges = hasChanges;
		setHasChangesMain(this.hasChanges);
	}
	
	public boolean hasUnsavedChanges() {
		
		if(this.breedingManagerListDetailsComponent.isChanged()){
			setHasUnsavedChanges(true);
		}
		
		if(dropHandler.isChanged()){
			setHasUnsavedChanges(true);
		}
		
		if(listInventoryTable.getInventoryTableDropHandler().isChanged()){
			setHasUnsavedChanges(true);
		}		
		
		// TODO mark the changes in germplasmListDataTable during fill with and add column functions
		
		return hasChanges;
	}
	
	public void resetUnsavedChangesFlag(){
		breedingManagerListDetailsComponent.setChanged(false);
		dropHandler.setChanged(false);
		listInventoryTable.getInventoryTableDropHandler().setChanged(false);
		setHasUnsavedChanges(false);
	}
	
	/*-------------------------------------END OF LIST INVENTORY RELATED METHODS-------------------------------------*/
	
	public ListInventoryTable getListInventoryTable(){
		return listInventoryTable;
	}
}

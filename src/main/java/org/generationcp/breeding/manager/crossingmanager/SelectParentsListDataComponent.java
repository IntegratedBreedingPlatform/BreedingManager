package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectParentsListDataComponent extends VerticalLayout implements InitializingBean, 
							InternationalizableComponent, BreedingManagerLayout, 
							ReserveInventorySource {

	private static final Logger LOG = LoggerFactory.getLogger(SelectParentsListDataComponent.class);
	private static final long serialVersionUID = 7907737258051595316L;
	private static final String CHECKBOX_COLUMN_ID="Checkbox Column ID";
	
	public static final String LIST_DATA_TABLE_ID = "SelectParentsListDataComponent List Data Table ID";
	public static final String CROSSING_MANAGER_PARENT_TAB_INVENTORY_TABLE = "Crossing manager parent tab inventory table";
	
	private static final Action ACTION_ADD_TO_FEMALE_LIST = new Action("Add to Female List");
	private static final Action ACTION_ADD_TO_MALE_LIST = new Action("Add to Male List");
	private static final Action[] LIST_DATA_TABLE_ACTIONS = new Action[] {ACTION_ADD_TO_FEMALE_LIST, ACTION_ADD_TO_MALE_LIST};
	
	private Integer germplasmListId;
	private GermplasmList germplasmList;
	private Long count;
	private Label listEntriesLabel;
	private Label totalListEntriesLabel;
	private Label totalSelectedListEntriesLabel;

	private Table listDataTable;
	private Button viewListHeaderButton;
	private String listName;
	
	private Button actionButton;
	private ContextMenu actionMenu;
	
	private Button inventoryViewActionButton;
	private ContextMenu inventoryViewActionMenu;
	private ContextMenuItem menuCopyToNewListFromInventory;
	private ContextMenuItem menuInventorySaveChanges;
	@SuppressWarnings("unused")
	private ContextMenuItem menuListView;
	@SuppressWarnings("unused")
	private ContextMenuItem menuReserveInventory;
	
	public static String ACTIONS_BUTTON_ID = "Actions";
	
	private ViewListHeaderWindow viewListHeaderWindow;
	
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private CrossingManagerInventoryTable listInventoryTable;
	
	//Layout variables
	private HorizontalLayout headerLayout;
	private HorizontalLayout subHeaderLayout;
	
	private boolean hasChanges = false;
	
    //Inventory Related Variables
    private ReserveInventoryWindow reserveInventory;
    private ReservationStatusWindow reservationStatus;
    private ReserveInventoryUtil reserveInventoryUtil;
    private ReserveInventoryAction reserveInventoryAction;
    private Map<ListEntryLotDetails, Double> validReservationsToSave;
	
	private MakeCrossesParentsComponent makeCrossesParentsComponent;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;

	@Autowired
    private GermplasmListManager germplasmListManager;
	
	@Autowired
	private InventoryDataManager inventoryDataManager;
	
	public SelectParentsListDataComponent(Integer germplasmListId, String listName, MakeCrossesParentsComponent makeCrossesParentsComponent){
		super();
		this.germplasmListId = germplasmListId;
		this.listName = listName;
		this.makeCrossesParentsComponent = makeCrossesParentsComponent;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		
		if(makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)){
			changeToListView();
		}
		else if(makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.INVENTORY_VIEW)){
			viewInventoryActionConfirmed();
		}
	}

	@Override
	public void updateLabels() {
		
	}

	@Override
	public void instantiateComponents() {
		retrieveListDetails();
		
		listEntriesLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		listEntriesLabel.setWidth("160px");
		
		totalListEntriesLabel = new Label("", Label.CONTENT_XHTML);
       	totalListEntriesLabel.setWidth("120px");
       	updateNoOfEntries(count);
       	
       	totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		totalSelectedListEntriesLabel.setWidth("95px");
		updateNoOfSelectedEntries(0);
       	
		viewListHeaderWindow = new ViewListHeaderWindow(germplasmList);
		
		viewListHeaderButton = new Button(messageSource.getMessage(Message.VIEW_HEADER));
		viewListHeaderButton.addStyleName(Reindeer.BUTTON_LINK);
		viewListHeaderButton.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
		
		actionButton = new ActionButton();
		actionButton.setData(ACTIONS_BUTTON_ID);
		
		inventoryViewActionButton = new Button(messageSource.getMessage(Message.ACTIONS));
		inventoryViewActionButton.setData(ACTIONS_BUTTON_ID);
		inventoryViewActionButton.setIcon(AppConstants.Icons.ICON_TOOLS);
		inventoryViewActionButton.setWidth("110px");
		inventoryViewActionButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		
		actionMenu = new ContextMenu();
		actionMenu.setWidth("250px");
		actionMenu.addItem(messageSource.getMessage(Message.ADD_TO_MALE_LIST));
		actionMenu.addItem(messageSource.getMessage(Message.ADD_TO_FEMALE_LIST));
		actionMenu.addItem(messageSource.getMessage(Message.INVENTORY_VIEW));
		actionMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));
		
		inventoryViewActionMenu = new ContextMenu();
		inventoryViewActionMenu.setWidth("295px");
		menuCopyToNewListFromInventory = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.COPY_TO_NEW_LIST));
        menuReserveInventory = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.RESERVE_INVENTORY));
        menuListView = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
        menuInventorySaveChanges = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.SAVE_CHANGES));
        inventoryViewActionMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        resetInventoryMenuOptions();
        
		initializeListDataTable();
		initializeListInventoryTable(); //listInventoryTable
		
		viewListHeaderButton = new Button(messageSource.getMessage(Message.VIEW_LIST_HEADERS));
		viewListHeaderButton.setStyleName(BaseTheme.BUTTON_LINK);
		
	    //Inventory Related Variables
        validReservationsToSave = new HashMap<ListEntryLotDetails, Double>();
        
        // ListSelectionComponent is null when tool launched from BMS dashboard
        if (makeCrossesParentsComponent.getMakeCrossesMain() != null && makeCrossesParentsComponent.getMakeCrossesMain() != null){
        	SelectParentsComponent selectParentComponent = makeCrossesParentsComponent.getMakeCrossesMain().getSelectParentsComponent();
        	selectParentComponent.addUpdateListStatusForChanges(this, this.hasChanges);
        }
	}
	
	private void resetInventoryMenuOptions() {
        //disable the save button at first since there are no reservations yet
        menuInventorySaveChanges.setEnabled(false);
        
        //Temporarily disable to Copy to New List in InventoryView TODO implement the function
        menuCopyToNewListFromInventory.setEnabled(false);
	}
	
	private void initializeListDataTable(){
		tableWithSelectAllLayout = new TableWithSelectAllLayout(count.intValue(),9,CHECKBOX_COLUMN_ID);
		tableWithSelectAllLayout.setWidth("100%");
		
		listDataTable = tableWithSelectAllLayout.getTable();
		listDataTable.setWidth("100%");
		listDataTable.setData(LIST_DATA_TABLE_ID);
		listDataTable.setSelectable(true);
		listDataTable.setMultiSelect(true);
		listDataTable.setColumnCollapsingAllowed(true);
		listDataTable.setColumnReorderingAllowed(true);
		listDataTable.setImmediate(true);
		listDataTable.setDragMode(TableDragMode.MULTIROW);
		
		listDataTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.AVAILABLE_INVENTORY.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_RESERVATION.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		
		listDataTable.setColumnHeader(CHECKBOX_COLUMN_ID, messageSource.getMessage(Message.CHECK_ICON));
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_ID.getName(), messageSource.getMessage(Message.HASHTAG));
		listDataTable.setColumnHeader(ListDataTablePropertyID.DESIGNATION.getName(), messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.AVAILABLE_INVENTORY.getName(), messageSource.getMessage(Message.AVAIL_INV));
		listDataTable.setColumnHeader(ListDataTablePropertyID.SEED_RESERVATION.getName(), messageSource.getMessage(Message.SEED_RES));
		listDataTable.setColumnHeader(ListDataTablePropertyID.PARENTAGE.getName(), messageSource.getMessage(Message.LISTDATA_GROUPNAME_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_CODE.getName(), messageSource.getMessage(Message.LISTDATA_ENTRY_CODE_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.GID.getName(), messageSource.getMessage(Message.LISTDATA_GID_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.SEED_SOURCE.getName(), messageSource.getMessage(Message.LISTDATA_SEEDSOURCE_HEADER));
		
		listDataTable.setColumnWidth(CHECKBOX_COLUMN_ID, 25);
		listDataTable.setColumnWidth(ListDataTablePropertyID.ENTRY_ID.getName(), 25);
		listDataTable.setColumnWidth(ListDataTablePropertyID.DESIGNATION.getName(), 130);
		listDataTable.setColumnWidth(ListDataTablePropertyID.AVAILABLE_INVENTORY.getName(), 70);
		listDataTable.setColumnWidth(ListDataTablePropertyID.SEED_RESERVATION.getName(), 70);
		listDataTable.setColumnWidth(ListDataTablePropertyID.PARENTAGE.getName(), 130);
		listDataTable.setColumnWidth(ListDataTablePropertyID.ENTRY_CODE.getName(), 100);
		listDataTable.setColumnWidth(ListDataTablePropertyID.GID.getName(), 60);
		listDataTable.setColumnWidth(ListDataTablePropertyID.SEED_SOURCE.getName(), 110);
		
		listDataTable.setVisibleColumns(new String[] { 
        		CHECKBOX_COLUMN_ID
        		,ListDataTablePropertyID.ENTRY_ID.getName()
        		,ListDataTablePropertyID.DESIGNATION.getName()
        		,ListDataTablePropertyID.AVAILABLE_INVENTORY.getName()
        		,ListDataTablePropertyID.SEED_RESERVATION.getName()
        		,ListDataTablePropertyID.PARENTAGE.getName()
        		,ListDataTablePropertyID.ENTRY_CODE.getName()
        		,ListDataTablePropertyID.GID.getName()
        		,ListDataTablePropertyID.SEED_SOURCE.getName()});
	}
	
	private void initializeListInventoryTable(){
		listInventoryTable = new CrossingManagerInventoryTable(germplasmList.getId());
		listInventoryTable.setVisible(false);
		listInventoryTable.setMaxRows(9);
		listInventoryTable.getTable().setDragMode(TableDragMode.ROW);
		listInventoryTable.getTable().setData(CROSSING_MANAGER_PARENT_TAB_INVENTORY_TABLE);
	}

	private void retrieveListDetails() {
		try {
			germplasmList = germplasmListManager.getGermplasmListById(this.germplasmListId);
			count = germplasmListManager.countGermplasmListDataByListId(this.germplasmListId);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting list details" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void initializeValues() {
		try{
			List<GermplasmListData> listEntries = inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);
			
			for(GermplasmListData entry : listEntries){
				String gid = String.format("%s", entry.getGid().toString());
                Button gidButton = new Button(gid, new GidLinkClickListener(gid,true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                gidButton.setDescription("Click to view Germplasm information");
                
                Button desigButton = new Button(entry.getDesignation(), new GidLinkClickListener(gid,true));
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
    	   		
    			
    			//#1 Available Inventory
    			String avail_inv = "-"; //default value
    			if(entry.getInventoryInfo().getLotCount().intValue() != 0){
    				avail_inv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
    			}
    			
    			InventoryLinkButtonClickListener inventoryLinkButtonClickListener = new InventoryLinkButtonClickListener(this,germplasmList.getId(),entry.getId(), entry.getGid());
    			Button inventoryButton = new Button(avail_inv, inventoryLinkButtonClickListener);
    			inventoryButton.setData(inventoryLinkButtonClickListener);
    			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
    			inventoryButton.setDescription("Click to view Inventory Details");
    			
    			if(avail_inv.equals("-")){
    				inventoryButton.setEnabled(false);
    				inventoryButton.setDescription("No Lot for this Germplasm");
    			}
    			else{
    				inventoryButton.setDescription("Click to view Inventory Details");
    			}
    			
    			// Seed Reserved
    	   		String seed_res = "-"; //default value
    	   		if(entry.getInventoryInfo().getReservedLotCount().intValue() != 0){
    	   			seed_res = entry.getInventoryInfo().getReservedLotCount().toString().trim();
    	   		}
    	   		
    	   		Item newItem = listDataTable.getContainerDataSource().addItem(entry.getId());    			
    	   		newItem.getItemProperty(CHECKBOX_COLUMN_ID).setValue(itemCheckBox);
    	   		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entry.getEntryId());
    	   		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(desigButton);
    	   		newItem.getItemProperty(ListDataTablePropertyID.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
    	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_RESERVATION.getName()).setValue(inventoryButton);
    	   		newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(entry.getGroupName());
    	   		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
    	   		newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
    	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(entry.getSeedSource());
    	   		
//    	   		listDataTable.addItem(new Object[] {
//                        itemCheckBox, entry.getEntryId(), desigButton, inventoryButton, seed_res, entry.getGroupName(), entry.getEntryCode(), gidButton, entry.getSeedSource()}
//    	   			, entry.getId());
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting list entries for list: " + germplasmListId);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), "Error in getting list entries.");
		}
	}

	@Override
	public void addListeners() {
		
		actionButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				actionMenu.show(event.getClientX(), event.getClientY());
			}
		});
		
		actionMenu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
			@Override
			public void contextItemClick(ClickEvent event) {
			  // Get reference to clicked item
			  ContextMenuItem clickedItem = event.getClickedItem();
			  if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
				  listDataTable.setValue(listDataTable.getItemIds());
			  }else if(clickedItem.getName().equals(messageSource.getMessage(Message.ADD_TO_FEMALE_LIST))){
				  Collection<?> selectedIdsToAdd = (Collection<?>)listDataTable.getValue();
				  if(selectedIdsToAdd.size() > 0){
					  makeCrossesParentsComponent.dropToFemaleOrMaleTable(listDataTable, makeCrossesParentsComponent.getFemaleTable(), null);
					  makeCrossesParentsComponent.assignEntryNumber(makeCrossesParentsComponent.getFemaleTable());
					  makeCrossesParentsComponent.getParentTabSheet().setSelectedTab(0);
				  }
				  else{
					  MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.WARNING) 
			                    , messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
				  }
			  }else if(clickedItem.getName().equals(messageSource.getMessage(Message.ADD_TO_MALE_LIST))){
				  Collection<?> selectedIdsToAdd = (Collection<?>)listDataTable.getValue();
				  if(selectedIdsToAdd.size() > 0){
					  makeCrossesParentsComponent.dropToFemaleOrMaleTable(listDataTable, makeCrossesParentsComponent.getMaleTable(), null);
					  makeCrossesParentsComponent.assignEntryNumber(makeCrossesParentsComponent.getMaleTable());
					  makeCrossesParentsComponent.getParentTabSheet().setSelectedTab(1);
				  }
				  else{
					  MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.WARNING) 
			                    , messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
				  }
			  }else if(clickedItem.getName().equals(messageSource.getMessage(Message.INVENTORY_VIEW))){
				  viewInventoryAction();
			  }
		   }
		});
		
		inventoryViewActionButton.addListener(new ClickListener() {
	   		 private static final long serialVersionUID = 272707576878821700L;
	
				 @Override
	   		 public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
					 inventoryViewActionMenu.show(event.getClientX(), event.getClientY());
	   		 }
	   	 });
		
		inventoryViewActionMenu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
	
			@Override
			public void contextItemClick(ClickEvent event) {
			      // Get reference to clicked item
			      ContextMenuItem clickedItem = event.getClickedItem();
			      if(clickedItem.getName().equals(messageSource.getMessage(Message.SAVE_CHANGES))){	  
			    	  saveReservationChangesAction();
                  } else if(clickedItem.getName().equals(messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))){
                	  viewListAction();
                  } else if(clickedItem.getName().equals(messageSource.getMessage(Message.COPY_TO_NEW_LIST))){
                	  //copyToNewListFromInventoryViewAction();
				  } else if(clickedItem.getName().equals(messageSource.getMessage(Message.RESERVE_INVENTORY))){
		          	  reserveInventoryAction();
                  } else if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
                	  listInventoryTable.getTable().setValue(listInventoryTable.getTable().getItemIds());
		          }
		    }
		});
		
		viewListHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openViewListHeaderWindow();
			}
		});
		
		listDataTable.addActionHandler(new Action.Handler() {
			private static final long serialVersionUID = -2173636726748988046L;

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(action.equals(ACTION_ADD_TO_FEMALE_LIST)){
					makeCrossesParentsComponent.dropToFemaleOrMaleTable(listDataTable, makeCrossesParentsComponent.getFemaleTable(), null);
					makeCrossesParentsComponent.assignEntryNumber(makeCrossesParentsComponent.getFemaleTable());
					makeCrossesParentsComponent.getParentTabSheet().setSelectedTab(0);
				} else if(action.equals(ACTION_ADD_TO_MALE_LIST)){
					makeCrossesParentsComponent.dropToFemaleOrMaleTable(listDataTable, makeCrossesParentsComponent.getMaleTable(), null);
					makeCrossesParentsComponent.assignEntryNumber(makeCrossesParentsComponent.getMaleTable());
					makeCrossesParentsComponent.getParentTabSheet().setSelectedTab(1);
				}
			}
			
			@Override
			public Action[] getActions(Object target, Object sender) {
				return LIST_DATA_TABLE_ACTIONS;
			}
		});
		
        tableWithSelectAllLayout.getTable().addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateNoOfSelectedEntries();
			}
		});
        
        listInventoryTable.getTable().addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateNoOfSelectedEntries();
			}
		});
	}

	@Override
	public void layoutComponents() {
		setMargin(true);
		setSpacing(true);
		
		addComponent(actionMenu);
		addComponent(inventoryViewActionMenu);
		
		headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, listEntriesLabel);
		headerLayout.addComponent(headingLayout);
		headerLayout.addComponent(viewListHeaderButton);
		headerLayout.setComponentAlignment(headingLayout, Alignment.MIDDLE_LEFT);
		headerLayout.setComponentAlignment(viewListHeaderButton, Alignment.MIDDLE_RIGHT);
		
		HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setSpacing(true);
		leftSubHeaderLayout.addComponent(totalListEntriesLabel);
		leftSubHeaderLayout.addComponent(totalSelectedListEntriesLabel);
		leftSubHeaderLayout.setComponentAlignment(totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		leftSubHeaderLayout.setComponentAlignment(totalSelectedListEntriesLabel, Alignment.MIDDLE_LEFT);
		
		subHeaderLayout = new HorizontalLayout();
		subHeaderLayout.setWidth("100%");
		subHeaderLayout.addComponent(leftSubHeaderLayout);
		subHeaderLayout.addComponent(actionButton);
		subHeaderLayout.setComponentAlignment(leftSubHeaderLayout, Alignment.MIDDLE_LEFT);
		subHeaderLayout.setComponentAlignment(actionButton, Alignment.MIDDLE_RIGHT);
		
		addComponent(headerLayout);
		addComponent(subHeaderLayout);
		addComponent(tableWithSelectAllLayout);
		addComponent(listInventoryTable);

	}
	
	private void updateNoOfEntries(long count){
		if(makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)){
			if(count == 0) {
				totalListEntriesLabel.setValue(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
			} else {
				totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
		        		 + "  <b>" + count + "</b>");
	        }
		}
		else{//Inventory View
			totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LOTS) + ": " 
	        		 + "  <b>" + count + "</b>");
		}
	}
	
	private void updateNoOfEntries(){
		int count = 0;
		if(makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)){
			count = listDataTable.getItemIds().size();
		}
		else{//Inventory View
			count = listInventoryTable.getTable().size();
		}
		updateNoOfEntries(count);
	}
	
	private void updateNoOfSelectedEntries(int count){
		totalSelectedListEntriesLabel.setValue("<i>" + messageSource.getMessage(Message.SELECTED) + ": " 
	        		 + "  <b>" + count + "</b></i>");
	}
	
	private void updateNoOfSelectedEntries(){
		int count = 0;
		
		if(makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)){
			Collection<?> selectedItems = (Collection<?>)tableWithSelectAllLayout.getTable().getValue();
			count = selectedItems.size();
		}
		else{
			Collection<?> selectedItems = (Collection<?>)listInventoryTable.getTable().getValue();
			count = selectedItems.size();
		}
		
		updateNoOfSelectedEntries(count);
	}
	
	/*--------------------------------------INVENTORY RELATED FUNCTIONS---------------------------------------*/
	
	private void viewListAction(){
		
		if(!hasUnsavedChanges()){
			makeCrossesParentsComponent.getMakeCrossesMain().setModeView(ModeView.LIST_VIEW);
		}else{
			String message = "You have unsaved reservations for this list. " +
					"You will need to save them before changing views. " +
					"Do you want to save your changes?";
			
			makeCrossesParentsComponent.getMakeCrossesMain().showUnsavedChangesConfirmDialog(message, ModeView.LIST_VIEW);
		}
	}	
	
	public void changeToListView(){
		if(listInventoryTable.isVisible()){
			tableWithSelectAllLayout.setVisible(true);
			listInventoryTable.setVisible(false);
			
			subHeaderLayout.removeComponent(inventoryViewActionButton);
			subHeaderLayout.addComponent(actionButton);
			subHeaderLayout.setComponentAlignment(actionButton, Alignment.MIDDLE_RIGHT);
			
			listEntriesLabel.setValue(messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
	        updateNoOfEntries();
	        updateNoOfSelectedEntries();
	        
	        this.removeComponent(listInventoryTable);
	        this.addComponent(tableWithSelectAllLayout);
	        
	        this.requestRepaint();
		}
	}
	
	private void viewInventoryAction(){
		if(!hasUnsavedChanges()){
			makeCrossesParentsComponent.getMakeCrossesMain().setModeView(ModeView.INVENTORY_VIEW);
		}
		else{
			String message = "You have unsaved changes to the list you are currently editing.. " +
					"You will need to save them before changing views. " +
					"Do you want to save your changes?";
			makeCrossesParentsComponent.getMakeCrossesMain().showUnsavedChangesConfirmDialog(message, ModeView.INVENTORY_VIEW);
		}
	}
	
	public void viewInventoryActionConfirmed(){
		resetListInventoryTableValues();
		changeToInventoryView();
	}
	
	public void changeToInventoryView(){
		if(tableWithSelectAllLayout.isVisible()){
			tableWithSelectAllLayout.setVisible(false);
			listInventoryTable.setVisible(true);
			
			subHeaderLayout.removeComponent(actionButton);
	        subHeaderLayout.addComponent(inventoryViewActionButton);
	        subHeaderLayout.setComponentAlignment(inventoryViewActionButton, Alignment.MIDDLE_RIGHT);
	        
	        listEntriesLabel.setValue(messageSource.getMessage(Message.LOTS));
	        updateNoOfEntries();
	        updateNoOfSelectedEntries();
	        
	        this.removeComponent(tableWithSelectAllLayout);
	        this.addComponent(listInventoryTable);
	        
	        this.requestRepaint();
		}
	}
	
	public void reserveInventoryAction() {
		if(!inventoryViewActionMenu.isVisible()){//checks if the screen is in the inventory view
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.WARNING), 
					"Please change to Inventory View first.");
		}
		else{
			List<ListEntryLotDetails> lotDetailsGid = listInventoryTable.getSelectedLots();
			
			if( lotDetailsGid == null || lotDetailsGid.size() == 0){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.WARNING), 
						"Please select at least 1 lot to reserve.");
			}
			else{
		        //this util handles the inventory reservation related functions
		        reserveInventoryUtil = new ReserveInventoryUtil(this,lotDetailsGid);
				reserveInventoryUtil.viewReserveInventoryWindow();
			}
		}
	}

	@Override
	public void updateListInventoryTable(
			Map<ListEntryLotDetails, Double> validReservations,
			boolean withInvalidReservations) {
		for(Map.Entry<ListEntryLotDetails, Double> entry: validReservations.entrySet()){
			ListEntryLotDetails lot = entry.getKey();
			Double new_res = entry.getValue();
			
			Item itemToUpdate = listInventoryTable.getTable().getItem(lot);
			itemToUpdate.getItemProperty(ListInventoryTable.NEWLY_RESERVED_COLUMN_ID).setValue(new_res);
		}
		
		removeReserveInventoryWindow(reserveInventory);
		
		//update lot reservatios to save
		updateLotReservationsToSave(validReservations);
		
		//enable now the Save Changes option
		menuInventorySaveChanges.setEnabled(true);
		
		if(validReservations.size() == 0){//if there are no valid reservations
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT), 
					messageSource.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES) + ".");
		
		} else if(!withInvalidReservations){
			MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
					"All selected entries will be reserved in their respective lots.", 
					3000);
		}		
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

	@Override
	public void addReserveInventoryWindow(
			ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		makeCrossesParentsComponent.getWindow().addWindow(this.reserveInventory);
	}

	@Override
	public void addReservationStatusWindow(
			ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		removeReserveInventoryWindow(reserveInventory);
		makeCrossesParentsComponent.getWindow().addWindow(this.reservationStatus);
	}

	@Override
	public void removeReserveInventoryWindow(
			ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		makeCrossesParentsComponent.getWindow().removeWindow(this.reserveInventory);
	}

	@Override
	public void removeReservationStatusWindow(
			ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		makeCrossesParentsComponent.getWindow().removeWindow(this.reservationStatus);
	}
	
	public void saveReservationChangesAction() {
		if(hasUnsavedChanges()){
			reserveInventoryAction = new ReserveInventoryAction(this);
			boolean success = reserveInventoryAction.saveReserveTransactions(getValidReservationsToSave(), germplasmList.getId());
			if(success){
				refreshInventoryColumns(getValidReservationsToSave());
				resetListInventoryTableValues();
				MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
						"All reservations were saved.");
			}
		}
	}
	
	public void refreshInventoryColumns(
			Map<ListEntryLotDetails, Double> validReservationsToSave2) {
		
		Set<Integer> entryIds = new HashSet<Integer>();
		for(Entry<ListEntryLotDetails, Double> details : validReservationsToSave.entrySet()){
			entryIds.add(details.getKey().getId());
		 }
		
		List<GermplasmListData> germplasmListDataEntries = new ArrayList<GermplasmListData>();
		
		try {
			if (!entryIds.isEmpty())
				germplasmListDataEntries = this.inventoryDataManager.getLotCountsForListEntries(germplasmList.getId(), new ArrayList<Integer>(entryIds));
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		
		for (GermplasmListData listData : germplasmListDataEntries){
			Item item = listDataTable.getItem(listData.getId());
			
			//#1 Available Inventory
			String avail_inv = "-"; //default value
			if(listData.getInventoryInfo().getLotCount().intValue() != 0){
				avail_inv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			Button inventoryButton = new Button(avail_inv, new InventoryLinkButtonClickListener(makeCrossesParentsComponent, germplasmList.getId(),listData.getId(), listData.getGid()));
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
	   		if(listData.getInventoryInfo().getReservedLotCount().intValue() != 0){
	   			seed_res = listData.getInventoryInfo().getReservedLotCount().toString().trim();
	   		}
			
	   		item.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(seed_res);
		}		
	}
	
    public void resetListInventoryTableValues() {
    	if(germplasmList != null){
    		listInventoryTable.updateListInventoryTableAfterSave();
    	}
    	else{
    		listInventoryTable.reset();
    	}
		
		resetInventoryMenuOptions();
		
		validReservationsToSave.clear();//reset the reservations to save. 
		
		setHasUnsavedChanges(false);
	}
	
	/*--------------------------------END OF INVENTORY RELATED FUNCTIONS--------------------------------------*/
	
	public Map<ListEntryLotDetails, Double> getValidReservationsToSave(){
		return validReservationsToSave;
	}

	public boolean hasUnsavedChanges() {		
		return hasChanges;
	}

	private void openViewListHeaderWindow(){
		this.getWindow().addWindow(viewListHeaderWindow);
	}
	
	public Table getListDataTable(){
		return this.listDataTable;
	}

	public String getListName() {
		return this.listName;
	}
	
	public GermplasmList getGermplasmList(){
		return germplasmList;
	}
	
	public void setHasUnsavedChanges(Boolean hasChanges) {
		this.hasChanges = hasChanges;
		
		if(hasChanges){
			menuInventorySaveChanges.setEnabled(true);
		} else {
			menuInventorySaveChanges.setEnabled(false);
		}
		
		SelectParentsComponent selectParentComponent = makeCrossesParentsComponent.getMakeCrossesMain().getSelectParentsComponent();
		selectParentComponent.addUpdateListStatusForChanges(this, this.hasChanges);
	}
	
	public Integer getGermplasmListId(){
		return germplasmListId;
	}
}

package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.generationcp.breeding.manager.action.SaveGermplasmListAction;
import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.listeners.ParentsTableCheckboxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable;
import org.generationcp.breeding.manager.inventory.InventoryDropTargetContainer;
import org.generationcp.breeding.manager.inventory.ListDataAndLotDetails;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.util.InventoryTableDropHandler;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
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
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ParentTabComponent extends VerticalLayout implements InitializingBean, 
						InternationalizableComponent, BreedingManagerLayout, SaveGermplasmListActionSource, 
						SaveListAsDialogSource, ReserveInventorySource, InventoryDropTargetContainer {
	
	private static final Logger LOG = LoggerFactory.getLogger(ParentTabComponent.class);
	private static final long serialVersionUID = 2124522470629189449L;
	
	private Button editHeaderButton;
	private Label listEntriesLabel;
	private Label totalListEntriesLabel;
	
	private Button actionButton;
	private Button inventoryViewActionButton;
	
	//Layout Variables
	private HorizontalLayout subHeaderLayout;
	private HorizontalLayout headerLayout;
	
	//Tables
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private Table listDataTable;
	@SuppressWarnings("unused")
	private CheckBox selectAll;
	
	private CrossingManagerInventoryTable listInventoryTable;
	
	//Actions
	private ContextMenu actionMenu;
	private ContextMenuItem saveActionMenu;
	
	private ContextMenu inventoryViewActionMenu;
	private ContextMenuItem menuCopyToNewListFromInventory;
	private ContextMenuItem menuInventorySaveChanges;
	@SuppressWarnings("unused")
	private ContextMenuItem menuListView;
	private ContextMenuItem menuReserveInventory;
	
	private static final String TAG_COLUMN_ID = "Tag";
    private static final String ENTRY_NUMBER_COLUMN_ID = "Entry Number Column ID";
    private static final String DESIGNATION_ID = "Designation";
    private static final String AVAIL_INV_COLUMN_ID = "Avail Inv";
    private static final String SEED_RES_COLUMN_ID = "Seed Res";
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private InventoryDataManager inventoryDataManager;  
	
	private GermplasmList germplasmList;
    private String parentLabel;
    private Integer rowCount;
    private List<GermplasmListData> listEntries;
    private long listEntriesCount;
	
    private CrossingManagerMakeCrossesComponent makeCrossesMain;
    private MakeCrossesParentsComponent source;
	private CrossingManagerActionHandler parentActionListener;
	private String listNameForCrosses;
	private SaveListAsDialog saveListAsWindow;
	
	private boolean hasChanges = false;
	
    //Inventory Related Variables
    private ReserveInventoryWindow reserveInventory;
    private ReservationStatusWindow reservationStatus;
    private ReserveInventoryUtil reserveInventoryUtil;
    private ReserveInventoryAction reserveInventoryAction;
    private Map<ListEntryLotDetails, Double> validReservationsToSave;
    private ModeView prevModeView;
    
    private InventoryTableDropHandler inventoryTableDropHandler;

	public ParentTabComponent(CrossingManagerMakeCrossesComponent makeCrossesMain,
				MakeCrossesParentsComponent source, String parentLabel, Integer rowCount) {
		super();
		this.makeCrossesMain = makeCrossesMain;
		this.source = source;
		this.parentLabel = parentLabel;
		this.rowCount = rowCount;
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
		listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		listEntriesLabel.setWidth("160px");
		
        editHeaderButton = new Button(messageSource.getMessage(Message.EDIT_HEADER));
        editHeaderButton.setImmediate(true);
        editHeaderButton.setStyleName(Reindeer.BUTTON_LINK);
		
		totalListEntriesLabel = new Label(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
         		 + "  <b>0</b>", Label.CONTENT_XHTML);
		totalListEntriesLabel.setWidth("135px");
        
		actionButton = new Button(messageSource.getMessage(Message.ACTIONS));
        actionButton.setIcon(AppConstants.Icons.ICON_TOOLS);
        actionButton.setWidth("110px");
        actionButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		
		actionMenu = new ContextMenu();
        actionMenu.setWidth("250px");
        actionMenu.addItem(messageSource.getMessage(Message.INVENTORY_VIEW));
        actionMenu.addItem(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES));
        saveActionMenu = actionMenu.addItem(messageSource.getMessage(Message.SAVE_LIST));
        saveActionMenu.setEnabled(false);
        actionMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        
        inventoryViewActionButton = new Button(messageSource.getMessage(Message.ACTIONS));
        inventoryViewActionButton.setIcon(AppConstants.Icons.ICON_TOOLS);
        inventoryViewActionButton.setWidth("110px");
        inventoryViewActionButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        
		inventoryViewActionMenu = new ContextMenu();
		inventoryViewActionMenu.setWidth("295px");
		menuCopyToNewListFromInventory = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.COPY_TO_NEW_LIST));
        menuReserveInventory = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.RESERVE_INVENTORY));
        menuListView = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
        menuInventorySaveChanges = inventoryViewActionMenu.addItem(messageSource.getMessage(Message.SAVE_CHANGES));
        inventoryViewActionMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        resetInventoryMenuOptions();
        
        initializeParentTable();
        initializeListInventoryTable();
        
      //Inventory Related Variables
        validReservationsToSave = new HashMap<ListEntryLotDetails, Double>();
	}

	private void resetInventoryMenuOptions() {
        //disable the save button at first since there are no reservations yet
        menuInventorySaveChanges.setEnabled(false);
        
        //Temporarily disable to Copy to New List in InventoryView TODO implement the function
        menuCopyToNewListFromInventory.setEnabled(false);
        
        //disable the reserve inventory at first if the list is not yet saved.
        if(germplasmList == null){
            menuReserveInventory.setEnabled(false);
        }
	}

	private void initializeParentTable() {
		tableWithSelectAllLayout = new TableWithSelectAllLayout(rowCount, TAG_COLUMN_ID);
        listDataTable = tableWithSelectAllLayout.getTable();
        selectAll = tableWithSelectAllLayout.getCheckBox();
        
        listDataTable.setWidth("100%");
        listDataTable.setNullSelectionAllowed(true);
        listDataTable.setSelectable(true);
        listDataTable.setMultiSelect(true);
        listDataTable.setImmediate(true);
        listDataTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        listDataTable.addContainerProperty(ENTRY_NUMBER_COLUMN_ID, Integer.class, Integer.valueOf(0));
        listDataTable.addContainerProperty(DESIGNATION_ID, Button.class, null);
        listDataTable.addContainerProperty(AVAIL_INV_COLUMN_ID, Button.class, null);
        listDataTable.addContainerProperty(SEED_RES_COLUMN_ID, String.class, null);

        listDataTable.setColumnHeader(TAG_COLUMN_ID, messageSource.getMessage(Message.CHECK_ICON));
        listDataTable.setColumnHeader(ENTRY_NUMBER_COLUMN_ID, messageSource.getMessage(Message.HASHTAG));
        listDataTable.setColumnHeader(DESIGNATION_ID, messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER));
        listDataTable.setColumnHeader(AVAIL_INV_COLUMN_ID, messageSource.getMessage(Message.AVAIL_INV));
        listDataTable.setColumnHeader(SEED_RES_COLUMN_ID, messageSource.getMessage(Message.SEED_RES));
        
        listDataTable.setColumnWidth(TAG_COLUMN_ID, 25);
        listDataTable.setDragMode(TableDragMode.ROW);
        listDataTable.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                             
			private static final long serialVersionUID = -3207714818504151649L;

			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if(propertyId != null && propertyId == DESIGNATION_ID) {
			    	Table theTable = (Table) source;
			    	Item item = theTable.getItem(itemId);
			    	String name = (String) item.getItemProperty(DESIGNATION_ID).getValue();
			    	return name;
			    }                                                                       
			    return null;
			}
		});
	}
	
	private void initializeListInventoryTable(){
		
        if(germplasmList!=null){
        	listInventoryTable = new CrossingManagerInventoryTable(germplasmList.getId());
        }
        else{
        	listInventoryTable = new CrossingManagerInventoryTable(null);
        }
        
		listInventoryTable.setVisible(false);
		listInventoryTable.setMaxRows(rowCount);
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		setupDropHandler();
		
		parentActionListener = new CrossingManagerActionHandler(source);
		listDataTable.addActionHandler(parentActionListener);
        
        actionButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				actionMenu.show(event.getClientX(), event.getClientY());
			}
        	
        });
        
		actionMenu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
			
			@Override
			public void contextItemClick(
					org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				 ContextMenuItem clickedItem = event.getClickedItem();
				 
				 if(clickedItem.getName().equals(messageSource.getMessage(Message.INVENTORY_VIEW))){
					 viewInventoryAction();
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES))){
					 parentActionListener.removeSelectedEntriesAction(listDataTable);
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SAVE_LIST))){
					 doSaveAction();
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
					 listDataTable.setValue(listDataTable.getItemIds());
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
			public void contextItemClick(
					org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				// Get reference to clicked item
				ContextMenuItem clickedItem = event.getClickedItem();
				if(clickedItem.getName().equals(messageSource.getMessage(Message.SAVE_CHANGES))){	  
					doSaveAction();
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
		
		editHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -6306973449416812850L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openSaveListAsDialog();
			}
		});
	}

	public void doSaveAction() {
		if(hasUnsavedChanges()){
			
			if(source.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)){
				if(germplasmList == null){//new lists
					openSaveListAsDialog();
				}
				else{//existing lists
					saveList(germplasmList);
				}
			}
			else if(source.getMakeCrossesMain().getModeView().equals(ModeView.INVENTORY_VIEW)){
				if(germplasmList == null){//new list in inventory view
					openSaveListAsDialog();
				}
				else{
					if(inventoryTableDropHandler.hasChanges()){
						saveList(germplasmList);
					}
					else { //only reservations are made
						saveReservationChangesAction(true);
					}
				}
			}
			
		}
	}
	
	public void doSaveActionFromMain() {
		if(hasUnsavedChanges()){
			
			if(prevModeView.equals(ModeView.LIST_VIEW)){
				if(germplasmList == null){//new lists
					openSaveListAsDialog();
				}
				else{//existing lists
					saveList(germplasmList);
				}
			}
			else if(prevModeView.equals(ModeView.INVENTORY_VIEW)){
				if(germplasmList == null){//new list in inventory view
					openSaveListAsDialog();
				}
				else{
					if(inventoryTableDropHandler.hasChanges()){
						saveList(germplasmList);
					}
					else { //only reservations are made
						saveReservationChangesAction(true);
						makeCrossesMain.updateView(makeCrossesMain.getModeView());
					}
				}
			}
			
		}
	}
	
	private void updateListDataTableBeforeSaving(){
		List<Integer> alreadyAddedEntryIds = new ArrayList<Integer>();
		List<ListDataAndLotDetails> listDataAndLotDetails = inventoryTableDropHandler.getListDataAndLotDetails();
		
		for(ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails){
			
			if(!alreadyAddedEntryIds.contains(listDataAndLotDetail.getEntryId())){
				//dropHandler.addGermplasmFromList(listDataAndLotDetail.getListId(), listDataAndLotDetail.getSourceLrecId());
				
				try {

					GermplasmListData germplasmListData = germplasmListManager.getGermplasmListDataByListIdAndLrecId(listDataAndLotDetail.getListId(), listDataAndLotDetail.getSourceLrecId());
					
					if(germplasmListData!=null){
						
						Integer entryId = getListDataTableNextEntryId();
        				GermplasmListEntry entryObject = new GermplasmListEntry(germplasmListData.getId(),germplasmListData.getGid(), entryId, germplasmListData.getDesignation(), germplasmListData.getSeedSource());
        				
//        				//if the item is already existing in the target table, remove the existing item then add a new entry
//    		            listDataTable.removeItem(entryObject);
        				
    					Item newItem = listDataTable.addItem(entryObject);

						CheckBox tag = new CheckBox();
        				tag.addListener(new ParentsTableCheckboxListener(listDataTable, entryObject, getSelectAllCheckBox()));
    		            tag.setImmediate(true);
			            
    		            newItem.getItemProperty(TAG_COLUMN_ID).setValue(tag);
        				
						newItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(entryId);
						
						Button desigButton = new Button(germplasmListData.getDesignation(), new GidLinkClickListener(germplasmListData.getGid().toString(),true));
	                    desigButton.setStyleName(BaseTheme.BUTTON_LINK);
	                    desigButton.setDescription("Click to view Germplasm information");
	                    newItem.getItemProperty(DESIGNATION_ID).setValue(desigButton);
					}
					
				} catch (MiddlewareQueryException e) {
					e.printStackTrace();
				}
				
				alreadyAddedEntryIds.add(listDataAndLotDetail.getEntryId());
			}
		}
	}

	protected void openSaveListAsDialog() {
    	saveListAsWindow = null;
    	if(germplasmList != null){
    		saveListAsWindow = new SaveListAsDialog(this,germplasmList);
    	}
    	else{
    		saveListAsWindow = new SaveListAsDialog(this,null);
    	}
        
        saveListAsWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        saveListAsWindow.setData(this);
        this.getWindow().addWindow(saveListAsWindow);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void saveList(GermplasmList list) {
		//update the listDataTable when the user tries to change list view but has unsaved changes in inventory view
		if(prevModeView != null){
			if(prevModeView.equals(ModeView.INVENTORY_VIEW)){
				updateListDataTableBeforeSaving();
			}
		}
		else{
			//update the listdatatable in inventory view w/o changing mode
			if(source.getMakeCrossesMain().getModeView().equals(ModeView.INVENTORY_VIEW)){
				updateListDataTableBeforeSaving();
			}
		}
		
		List<GermplasmListEntry> listEntries = new ArrayList<GermplasmListEntry>();
		listEntries.addAll((Collection<GermplasmListEntry>) listDataTable.getItemIds());
		
		//TO DO correct the entryID, get from the parent table
		// Create Map <Key: "GID+ENTRYID">, <Value:CheckBox Obj>
		SaveGermplasmListAction saveListAction = new SaveGermplasmListAction(this, list, listEntries);
		try {
			germplasmList = saveListAction.saveRecords();
			updateCrossesSeedSource(germplasmList);
			source.updateUIForSuccessfulSaving(this, germplasmList);
			
			if(source.getMakeCrossesMain().getModeView().equals(ModeView.INVENTORY_VIEW)){
				if(validReservationsToSave.size() > 0){
					saveReservationChangesAction(false);
					inventoryTableDropHandler.resetListDataAndLotDetails();
				}
			}
			
			setHasUnsavedChanges(false);
			
			if(prevModeView != null){
				source.getMakeCrossesMain().updateView(source.getMakeCrossesMain().getModeView());
				
				//reset the marker
				prevModeView = null;
			}
			
			//Reserve Inventory Action will now be available after saving the list for the first time
			menuReserveInventory.setEnabled(true);
			
			//show success message for saving
			MessageNotifier.showMessage(this.source.getWindow(), messageSource.getMessage(Message.SUCCESS), 
					messageSource.getMessage(getSuccessMessage()), 3000);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in saving the Parent List",e);
			e.printStackTrace();
		}
	}

	private void setupDropHandler() {
		listDataTable.setDropHandler(new DropHandler() {
            private static final long serialVersionUID = -3048433522366977000L;

				@SuppressWarnings("unchecked")
				public void drop(DragAndDropEvent dropEvent) {
					
					//Dragged from a table
					if(dropEvent.getTransferable() instanceof TableTransferable){
						
						TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
	                       
	                    Table sourceTable = (Table) transferable.getSourceComponent();
	                    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
	                        
	                    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
	                    Object targetItemId = dropData.getItemIdOver();
	                    
	                    if(sourceTable.equals(listDataTable)){
		                    //Check first if item is dropped on top of itself
		                    if(!transferable.getItemId().equals(targetItemId)){
		                    	
		                    	Item oldItem = sourceTable.getItem(transferable.getItemId());
		                    	Object oldCheckBox = oldItem.getItemProperty(TAG_COLUMN_ID).getValue();
		    	                Object oldEntryCode = oldItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).getValue();
		    	                Object oldDesignation = oldItem.getItemProperty(DESIGNATION_ID).getValue();
		    	                Object oldAvailInv = oldItem.getItemProperty(AVAIL_INV_COLUMN_ID).getValue();
		    	                Object oldSeedRes = oldItem.getItemProperty(SEED_RES_COLUMN_ID).getValue();
		    	                
		                		sourceTable.removeItem(transferable.getItemId());
		                		
								Item newItem = targetTable.addItemAfter(targetItemId, transferable.getItemId());
								newItem.getItemProperty(TAG_COLUMN_ID).setValue(oldCheckBox);
								newItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(oldEntryCode);
								newItem.getItemProperty(DESIGNATION_ID).setValue(oldDesignation);
								newItem.getItemProperty(AVAIL_INV_COLUMN_ID).setValue(oldAvailInv);
								newItem.getItemProperty(SEED_RES_COLUMN_ID).setValue(oldSeedRes);
								
								saveActionMenu.setEnabled(true);
								setHasUnsavedChanges(true);
		                    }
	                    } else if(sourceTable.getData().equals(SelectParentsListDataComponent.LIST_DATA_TABLE_ID)){
	                    	source.dropToFemaleOrMaleTable(sourceTable, listDataTable, (Integer) transferable.getItemId());
	                    }

	                //Dragged from the tree
					} else {
						Transferable transferable = dropEvent.getTransferable();
	                    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
						
	                    try {
	                    	GermplasmList draggedListFromTree = germplasmListManager.getGermplasmListById((Integer) transferable.getData("itemId"));
	                    	if(draggedListFromTree!=null){
	                    		List<GermplasmListData> germplasmListDataFromListFromTree = draggedListFromTree.getListData();
	                    		
	                    		Integer addedCount = 0;
	                    		
	                    		for(GermplasmListData listData : germplasmListDataFromListFromTree){
	                    			if(listData.getStatus()!=9){
	                    				String parentValue = listData.getDesignation();
	                    				
	                    				Button gidButton = new Button(parentValue, new GidLinkClickListener(listData.getGid().toString(),true));
	    		                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
	    		                        gidButton.setDescription("Click to view Germplasm information");
	                    				
	                    				CheckBox tag = new CheckBox();
			                        	
	                    				GermplasmListEntry entryObject = new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(), listData.getDesignation(), draggedListFromTree.getName()+":"+listData.getEntryId());
	                    				
	                		    		if(targetTable.equals(listDataTable)){
	                		    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, getSelectAllCheckBox()));
	                		    			listNameForCrosses = draggedListFromTree.getName();
	                		    	    	updateCrossesSeedSource(draggedListFromTree);
	                		    		}
	                		    		
	                		            tag.setImmediate(true);
	                    				
	                		            //if the item is already existing in the target table, remove the existing item then add a new entry
	                		            targetTable.removeItem(entryObject);
	                		            
	                    				Item item = targetTable.addItem(entryObject);
	                    				
	                    				item.getItemProperty(DESIGNATION_ID).setValue(gidButton);
	                    				item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
	                    				
	                    				addedCount++;
	                    			} 
			                	}
	                    		
	                    		//After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this will enable/disable the save option
	                    		List<Object> itemsAfterAdding = new ArrayList<Object>();
	                    		itemsAfterAdding.addAll((Collection<? extends Integer>) targetTable.getItemIds());
	                    		
	                    		if(addedCount==itemsAfterAdding.size()){
	                    			saveActionMenu.setEnabled(false);
	                    			setHasUnsavedChanges(false);
	                    			
	                    			//updates the crossesMade.savebutton if both parents are save at least once;
	                        		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
	                        		
	                    		} else {
	                    			saveActionMenu.setEnabled(true);
	                    			setHasUnsavedChanges(true);
	                    			//femaleParentList = null;
	                    		}
	                    	}
	                    } catch(MiddlewareQueryException e) {
	                    	LOG.error("Error in getting list by GID",e);	
	                    }
					}
                    assignEntryNumber(listDataTable);
                    updateNoOfEntries(listDataTable.size());
                }

                public AcceptCriterion getAcceptCriterion() {
                	return AcceptAll.get();
                }
        });
		
		inventoryTableDropHandler = new InventoryTableDropHandler(this, germplasmDataManager, germplasmListManager, inventoryDataManager, listInventoryTable.getTable());
		listInventoryTable.getTable().setDropHandler(inventoryTableDropHandler);
	}

	public void updateNoOfEntries(int numOfEntries) {
		if(makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW)){
			totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
		       		 + "  <b>" + numOfEntries + "</b>");
		}
		else if(makeCrossesMain.getModeView().equals(ModeView.INVENTORY_VIEW)){
			totalListEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LOTS) + ": " 
		       		 + "  <b>" + numOfEntries + "</b>");
		}
	}

	@SuppressWarnings("unchecked")
	public void assignEntryNumber(Table parentTable){
		
		int entryNumber = 1;
		List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) parentTable.getItemIds());
		
		for(GermplasmListEntry entry : itemIds){
			Item item = parentTable.getItem(entry);
    		item.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(Integer.valueOf(entryNumber));
    		entry.setEntryId(entryNumber);
			entryNumber++;
		}
	}
	
	public void resetListDataTableValues(){
		listDataTable.removeAllItems();
		loadEntriesToListDataTable();
		listDataTable.requestRepaint();
	}
	
	public void loadEntriesToListDataTable(){
		try {
			listEntriesCount = germplasmListManager.countGermplasmListDataByListId(germplasmList.getId());
			
			if(listEntriesCount > 0){
				listEntries = new ArrayList<GermplasmListData>();
				getAllListEntries();
				
				updateListDataTable(germplasmList.getId(), listEntries);
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error loading list data in Parent Tab Component. " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void getAllListEntries() {
		if(germplasmList != null){
			List<GermplasmListData> entries = null;
			try{
				entries = inventoryDataManager.getLotCountsForList(germplasmList.getId(), 0, Long.valueOf(listEntriesCount).intValue());
				
				listEntries.addAll(entries);
			} catch(MiddlewareQueryException ex){
				LOG.error("Error with retrieving list entries for list: " + germplasmList.getId(), ex);
				listEntries = new ArrayList<GermplasmListData>();
			}
		}
	}

	@Override
	public void layoutComponents() {
		setMargin(true,true,false,true);
		setSpacing(true);
		
		this.addComponent(actionMenu);
		this.addComponent(inventoryViewActionMenu);
		
		HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, listEntriesLabel);
		
		headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		headerLayout.addComponent(headingLayout);
		headerLayout.addComponent(editHeaderButton);
		headerLayout.setComponentAlignment(headingLayout, Alignment.MIDDLE_LEFT);
		headerLayout.setComponentAlignment(editHeaderButton, Alignment.BOTTOM_RIGHT);
		
		subHeaderLayout = new HorizontalLayout();
		subHeaderLayout.setWidth("100%");
		subHeaderLayout.addComponent(totalListEntriesLabel);
		subHeaderLayout.addComponent(actionButton);
		subHeaderLayout.setComponentAlignment(totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		subHeaderLayout.setComponentAlignment(actionButton, Alignment.TOP_RIGHT);
		
		this.addComponent(headerLayout);
		this.addComponent(subHeaderLayout);
		this.addComponent(tableWithSelectAllLayout);
	}

	@Override
	public void updateLabels() {
		
	}
	
	public void updateListDataTable(GermplasmList germplasmList){
		Integer germplasmListId = germplasmList.getId();
		
		try {
			List<GermplasmListData> germplasmListDataFromListFromTree = inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);
			updateListDataTable(germplasmListId,germplasmListDataFromListFromTree);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in retrieving list data entries with lot counts",e);
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateListDataTable(Integer germplasmListId, List<GermplasmListData> savedListEntries) {
		List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
				
		selectedItemIds.addAll((Collection<GermplasmListEntry>) listDataTable.getValue());
		listDataTable.removeAllItems();
		
		for(GermplasmListData entry : savedListEntries){
			GermplasmListEntry itemId = new GermplasmListEntry(entry.getId(),entry.getGid(), entry.getEntryId(), entry.getDesignation(), entry.getSeedSource());
			
			Item newItem = listDataTable.addItem(itemId);
			
			// #1
			CheckBox tag = new CheckBox();
			newItem.getItemProperty(TAG_COLUMN_ID).setValue(tag);
			
			tag.addListener(new ParentsTableCheckboxListener(listDataTable, itemId, tableWithSelectAllLayout.getCheckBox()));
            tag.setImmediate(true);
            
            if(selectedItemIds.contains(itemId)){
            	listDataTable.select(itemId);
            }
            			
			// #3
			String designationName = entry.getDesignation();
			
			Button designationButton = new Button(designationName, new GidLinkClickListener(entry.getGid().toString(),true));
            designationButton.setStyleName(BaseTheme.BUTTON_LINK);
            designationButton.setDescription("Click to view Germplasm information");
			
            // #4
            String avail_inv = "-"; //default value
			if(entry.getInventoryInfo().getLotCount().intValue() != 0){
				avail_inv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			
			InventoryLinkButtonClickListener inventoryClickListener = new InventoryLinkButtonClickListener(this,germplasmListId,entry.getId(), entry.getGid());
			Button inventoryButton = new Button(avail_inv, inventoryClickListener);
			inventoryButton.setData(inventoryClickListener);
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription("Click to view Inventory Details");
			
			if(avail_inv.equals("-")){
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription("No Lot for this Germplasm");
			}
			else{
				inventoryButton.setDescription("Click to view Inventory Details");
			}
			
			// #5
			String seed_res = "-"; //default value
			if(entry.getInventoryInfo().getReservedLotCount().intValue() != 0){
				seed_res = entry.getInventoryInfo().getReservedLotCount().toString().trim();
			}
			
			newItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(entry.getEntryId());
			newItem.getItemProperty(DESIGNATION_ID).setValue(designationButton);
			newItem.getItemProperty(AVAIL_INV_COLUMN_ID).setValue(inventoryButton);
			newItem.getItemProperty(SEED_RES_COLUMN_ID).setValue(seed_res);

		}
		
		resetUnsavedChangesFlag();
		listDataTable.requestRepaint();
	}
	
	/*--------------------------------------INVENTORY RELATED FUNCTIONS---------------------------------------*/
	
	private void viewListAction(){
		
		if(!hasUnsavedChanges()){
			source.getMakeCrossesMain().setModeView(ModeView.LIST_VIEW);
		}else{
			String message = "You have unsaved reservations for this list. " +
					"You will need to save them before changing views. " +
					"Do you want to save your changes?";
			source.getMakeCrossesMain().showUnsavedChangesConfirmDialog(message, ModeView.LIST_VIEW);
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
		
		resetUnsavedChangesFlag();
	}

	public void changeToListView(){
		if(listInventoryTable.isVisible()){
			tableWithSelectAllLayout.setVisible(true);
			listInventoryTable.setVisible(false);
			
			subHeaderLayout.removeComponent(inventoryViewActionButton);
			subHeaderLayout.addComponent(actionButton);
			subHeaderLayout.setComponentAlignment(actionButton, Alignment.MIDDLE_RIGHT);
			
			listEntriesLabel.setValue(messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
			updateNoOfEntries(listDataTable.size());
	        
	        this.removeComponent(listInventoryTable);
	        this.addComponent(tableWithSelectAllLayout);
	        
	        this.requestRepaint();
		}
	}
	
	private void viewInventoryAction(){
		if(hasChanges){
			String message = "";
			if(germplasmList != null){
				message = "You have unsaved changes to the parent list you are editing. You will need to save them before changing views. Do you want to save your changes?";
			}
			else if(germplasmList == null){
				message = "You need to save the parent list that you're building before you can switch to the inventory view. Do you want to save the list?";
			}
			
			if(makeCrossesMain.areBothParentsNewListWithUnsavedChanges()){
				MessageNotifier.showError(getWindow(), "Unsaved Parent Lists", "Please save parent lists first before changing view.");
			}
			else{ 
				source.getMakeCrossesMain().showUnsavedChangesConfirmDialog(message, ModeView.INVENTORY_VIEW);
			}
		} 
		else {
			source.getMakeCrossesMain().setModeView(ModeView.INVENTORY_VIEW);
		}	
	}
	
	public void resetList() {
		//Reset list data table
		listDataTable.removeAllItems();
		
		//list inventory table
		listInventoryTable.reset();
		
		//Reset the marker for changes in Build New List
		resetUnsavedChangesFlag();
	}

	public void resetUnsavedChangesFlag() {
		inventoryTableDropHandler.setHasChanges(false);
		setHasUnsavedChanges(false);
	}

	public void viewInventoryActionConfirmed(){
		//set the listId in List Inventory Table
		if(listInventoryTable.getListId() == null && germplasmList != null){
			listInventoryTable.setListId(germplasmList.getId());
		}
		
		listInventoryTable.loadInventoryData();
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
	        updateNoOfEntries(listInventoryTable.getTable().getItemIds().size());
	        
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
			if(hasUnsavedChanges()){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.WARNING), 
						"Please save the list first before reserving an inventory.");
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
		
		setHasUnsavedChanges(true);
		
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
	
	public void saveReservationChangesAction(boolean displayReservationSuccessMessage) {
		
		if(hasUnsavedChanges()){
			reserveInventoryAction = new ReserveInventoryAction(this);
			boolean success = reserveInventoryAction.saveReserveTransactions(getValidReservationsToSave(), germplasmList.getId());
			if(success){
				refreshInventoryColumns(getValidReservationsToSave());
				resetListInventoryTableValues();
				
				if(displayReservationSuccessMessage){
					MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
							"All reservations were saved.");
				}	
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
			GermplasmListEntry itemId = new GermplasmListEntry(listData.getId(),listData.getGid(), listData.getEntryId(), listData.getDesignation(), listData.getSeedSource());
			
			Item item = listDataTable.getItem(itemId);
			
			//#1 Available Inventory
			String avail_inv = "-"; //default value
			if(listData.getInventoryInfo().getLotCount().intValue() != 0){
				avail_inv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			Button inventoryButton = new Button(avail_inv, new InventoryLinkButtonClickListener(source, germplasmList.getId(),listData.getId(), listData.getGid()));
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription("Click to view Inventory Details");
			
			if(avail_inv.equals("-")){
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription("No Lot for this Germplasm");
			}
			else{
				inventoryButton.setDescription("Click to view Inventory Details");
			}

			item.getItemProperty(AVAIL_INV_COLUMN_ID).setValue(inventoryButton);
			
		
			// Seed Reserved
	   		String seed_res = "-"; //default value
	   		if(listData.getInventoryInfo().getReservedLotCount().intValue() != 0){
	   			seed_res = listData.getInventoryInfo().getReservedLotCount().toString().trim();
	   		}
			
	   		item.getItemProperty(SEED_RES_COLUMN_ID).setValue(seed_res);
		}
		
	}
	
	/*--------------------------------END OF INVENTORY RELATED FUNCTIONS--------------------------------------*/

	public Map<ListEntryLotDetails, Double> getValidReservationsToSave(){
		return validReservationsToSave;
	}

	public ContextMenuItem getSaveActionMenu() {
		return saveActionMenu;
	}

	public Table getListDataTable() {
		return listDataTable;
	}

	public String getListNameForCrosses() {
		return listNameForCrosses;
	}

	public void setListNameForCrosses(String listNameForCrosses) {
		this.listNameForCrosses = listNameForCrosses;
	}

	public GermplasmList getGermplasmList() {
		return germplasmList;
	}

	public void setGermplasmList(GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
		germplasmList = list;
	}

	@Override
	public Component getParentComponent() {
		return makeCrossesMain.getSource();
	}
	
	private void updateCrossesSeedSource(GermplasmList germplasmList){
		source.updateCrossesSeedSource(this, germplasmList);
	}

	public Message getSuccessMessage() {
		if(parentLabel.equals("Female Parents")){
        	return Message.SUCCESS_SAVE_FOR_FEMALE_LIST;
        }
        else if(parentLabel.equals("Male Parents")){
        	return Message.SUCCESS_SAVE_FOR_MALE_LIST;
        }
		return null;
	}
	
	public void setHasUnsavedChanges(Boolean hasChanges) {
		this.hasChanges = hasChanges;
		
		if(hasChanges){
			menuInventorySaveChanges.setEnabled(true);
		} else {
			menuInventorySaveChanges.setEnabled(false);
		}
		
		inventoryTableDropHandler.setHasChanges(false);
		
		source.setHasUnsavedChanges(this.hasChanges);
	}
	
	public boolean hasUnsavedChanges() {	
		if(inventoryTableDropHandler.hasChanges()){
			hasChanges = true;
		}
		
		return hasChanges;
	}
	
	public CheckBox getSelectAllCheckBox(){
		return tableWithSelectAllLayout.getCheckBox();
	}

	public void discardChangesInListView() {
		updateListDataTable(germplasmList);
		viewInventoryActionConfirmed();
	}

	public void discardChangesInInventoryView() {
		resetListInventoryTableValues();
		changeToListView();
	}

	@Override
	public void refreshListInventoryItemCount() {
		updateNoOfEntries(listInventoryTable.getTable().getItemIds().size());
	}
	
	@SuppressWarnings("unchecked")
	private Integer getListDataTableNextEntryId(){
		int nextId = 0;
		for(GermplasmListEntry entry : (Collection<? extends GermplasmListEntry>) listDataTable.getItemIds()){
			
			Integer entryId = 0;
			Item item = listDataTable.getItem(entry);
			if(item!=null)
				entryId = (Integer) item.getItemProperty(ENTRY_NUMBER_COLUMN_ID).getValue();
			
			if(entryId > nextId)
				nextId = entryId;
			
		}
		return nextId+1;
	}

	public void setPreviousModeView(ModeView prevModeView) {
		this.prevModeView = prevModeView;
	}

	public void enableReserveInventory() {
		menuReserveInventory.setEnabled(true);
	}	
	
	public CrossingManagerInventoryTable getListInventoryTable(){
		return listInventoryTable;
	}
	
}

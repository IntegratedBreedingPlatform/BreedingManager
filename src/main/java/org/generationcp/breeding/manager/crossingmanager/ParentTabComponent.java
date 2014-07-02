package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.action.SaveGermplasmListAction;
import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.listeners.ParentsTableCheckboxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
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
						InternationalizableComponent, BreedingManagerLayout, 
						SaveListAsDialogSource, SaveGermplasmListActionSource {
	
	private static final Logger LOG = LoggerFactory.getLogger(ParentTabComponent.class);
	private static final long serialVersionUID = 2124522470629189449L;
	
	private Label listEntriesLabel;
	private Label lblNoOfEntries;
	private Button actionButton;
	private HorizontalLayout subHeadingLayout;
	
	//Tables
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private Table listDataTable;
	private CheckBox selectAll;
	
	//Actions
	private ContextMenu actionMenu;
	private ContextMenuItem saveActionMenu;
	
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
    private InventoryDataManager inventoryDataManager;  
	
	private GermplasmList germplasmList;
    private String parentLabel;
    private Integer rowCount;
	
    private CrossingManagerMakeCrossesComponent makeCrossesMain;
    private MakeCrossesParentsComponent source;
	private CrossingManagerActionHandler parentActionListener;
	private String listNameForCrosses;
	private SaveListAsDialog saveListAsWindow;

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
		listEntriesLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL).toUpperCase());
		listEntriesLabel.setStyleName(Bootstrap.Typography.H5.styleName());
		listEntriesLabel.addStyleName(AppConstants.CssStyles.BOLD);
		listEntriesLabel.setWidth("120px");
		
		lblNoOfEntries = new Label(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
         		 + "  <b>0</b>", Label.CONTENT_XHTML);
		lblNoOfEntries.setWidth("135px");
        
		initializeParentTable();
		
		actionButton = new Button(messageSource.getMessage(Message.ACTIONS));
        actionButton.setIcon(AppConstants.Icons.ICON_TOOLS);
        actionButton.setWidth("110px");
        actionButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		
		actionMenu = new ContextMenu();
        actionMenu.setWidth("250px");
        actionMenu.addItem(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES));
        saveActionMenu = actionMenu.addItem(messageSource.getMessage(Message.SAVE_LIST));
        saveActionMenu.setEnabled(false);
        actionMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));        
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

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
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
				  
				 if(clickedItem.getName().equals(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES))){
					 parentActionListener.removeSelectedEntriesAction(listDataTable);
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SAVE_LIST))){
					 saveParentList();
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
					 listDataTable.setValue(listDataTable.getItemIds());
				 }
				
			}
		});
	}

	protected void saveParentList() {
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
		List<GermplasmListEntry> listEntries = new ArrayList<GermplasmListEntry>();
		listEntries.addAll((Collection<GermplasmListEntry>) listDataTable.getItemIds());
		
		//TO DO correct the entryID, get from the parent table
		// Create Map <Key: "GID+ENTRYID">, <Value:CheckBox Obj>
		SaveGermplasmListAction saveListAction = new SaveGermplasmListAction(this, list, listEntries);
		try {
			GermplasmList savedList = saveListAction.saveRecords();
			updateCrossesSeedSource(savedList);
			source.updateUIForSuccessfulSaving(this, savedList);

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
	                		    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, selectAll));
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
	                    			
	                    			//updates the crossesMade.savebutton if both parents are save at least once;
	                        		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
	                        		
	                    		} else {
	                    			saveActionMenu.setEnabled(true);
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
	}

	public void updateNoOfEntries(int numOfEntries) {
		lblNoOfEntries.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
       		 + "  <b>" + numOfEntries + "</b>");
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

	@Override
	public void layoutComponents() {
		setMargin(true,true,false,true);
		setSpacing(true);
		
		this.addComponent(listEntriesLabel);
		
		subHeadingLayout = new HorizontalLayout();
		subHeadingLayout.setWidth("100%");
		subHeadingLayout.addComponent(lblNoOfEntries);
		subHeadingLayout.addComponent(actionButton);
		subHeadingLayout.setComponentAlignment(lblNoOfEntries, Alignment.MIDDLE_LEFT);
		subHeadingLayout.setComponentAlignment(actionButton, Alignment.TOP_RIGHT);
		
		this.addComponent(subHeadingLayout);
		this.addComponent(tableWithSelectAllLayout);
		this.addComponent(actionMenu);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateListDataTable(List<GermplasmListData> savedListEntries) {
		List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
				
		selectedItemIds.addAll((Collection<GermplasmListEntry>) listDataTable.getValue());
		listDataTable.removeAllItems();
		
		for(GermplasmListData entry : savedListEntries){
			GermplasmListEntry itemId = new GermplasmListEntry(entry.getId(),entry.getGid(), entry.getEntryId(), entry.getDesignation(), entry.getSeedSource());
			
			Item newItem = listDataTable.addItem(itemId);
			
			CheckBox tag = new CheckBox();
			newItem.getItemProperty(TAG_COLUMN_ID).setValue(tag);
			
			tag.addListener(new ParentsTableCheckboxListener(listDataTable, itemId, tableWithSelectAllLayout.getCheckBox()));
            tag.setImmediate(true);
            
            if(selectedItemIds.contains(itemId)){
            	listDataTable.select(itemId);
            }
            
			newItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(entry.getEntryId());
			
			String designationName = entry.getDesignation();
			
			Button designationButton = new Button(designationName, new GidLinkClickListener(entry.getGid().toString(),true));
            designationButton.setStyleName(BaseTheme.BUTTON_LINK);
            designationButton.setDescription("Click to view Germplasm information");
			
			newItem.getItemProperty(DESIGNATION_ID).setValue(designationButton);

		}
		
		listDataTable.requestRepaint();
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
}

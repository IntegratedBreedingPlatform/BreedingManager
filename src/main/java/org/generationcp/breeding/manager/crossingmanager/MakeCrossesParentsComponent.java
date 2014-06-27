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
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
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
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class MakeCrossesParentsComponent extends VerticalLayout implements BreedingManagerLayout,
		InitializingBean, InternationalizableComponent, SaveListAsDialogSource, SaveGermplasmListActionSource {

	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesParentsComponent.class);
	private static final long serialVersionUID = -4789763601080845176L;
	
	private static final int PARENTS_TABLE_ROW_COUNT = 10;
	private static final String MALE_PARENTS_LABEL = "Male Parents";
	private static final String FEMALE_PARENTS_LABEL = "Female Parents";
    
    private static final String TAG_COLUMN_ID = "Tag";
    private static final String ENTRY_NUMBER_COLUMN_ID = "Entry Number Column ID";
        
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    private TabSheet parentTabSheet;
    private Label parentListsLabel;
    private Label instructionForParentLists;
    
    private Label listFemaleEntriesLabel;
    private Label listMaleEntriesLabel;

    private Table femaleParents;
    private CheckBox femaleParentsTagAll;
    private Table maleParents;
    private CheckBox maleParentsTagAll;
    
    private TableWithSelectAllLayout femaleTableWithSelectAll;
    private TableWithSelectAllLayout maleTableWithSelectAll;
    
    private Label lblNoOfFemaleEntries;
    private Label lblNoOfMaleEntries;
    
    private Button actionFemaleListButton;
    private Button actionMaleListButton;
    private ContextMenu femaleListMenu;
    private ContextMenu maleListMenu;
    private ContextMenuItem saveFemaleListMenu;
    private ContextMenuItem saveMaleListMenu;
    
    private GermplasmList femaleParentList;
    private GermplasmList maleParentList;
    
    private ParentContainer femaleParentContainer;
    private ParentContainer maleParentContainer;
    
    private String femaleListNameForCrosses;
    private String maleListNameForCrosses;
    
    private SaveListAsDialog saveListAsWindow;
    
    private CrossingManagerMakeCrossesComponent makeCrossesMain;
	private CrossingManagerActionHandler femaleParentsActionListener;
	private CrossingManagerActionHandler maleParentsActionListener;
        
    public MakeCrossesParentsComponent(CrossingManagerMakeCrossesComponent parentComponent){
    	this.makeCrossesMain = parentComponent;
	}

    @Override
    public void attach() {
    	super.attach();
    	updateLabels();
    }
	@Override
	public void updateLabels() {

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
        parentListsLabel = new Label(messageSource.getMessage(Message.PARENTS_LISTS));
        parentListsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
        parentListsLabel.addStyleName(AppConstants.CssStyles.BOLD);
        
        instructionForParentLists = new Label(messageSource.getMessage(Message.INSTRUCTION_FOR_PARENT_LISTS));
        
		listFemaleEntriesLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL).toUpperCase());
		listFemaleEntriesLabel.setStyleName(Bootstrap.Typography.H5.styleName());
		listFemaleEntriesLabel.addStyleName(AppConstants.CssStyles.BOLD);
		listFemaleEntriesLabel.setWidth("120px");
		
		listMaleEntriesLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL).toUpperCase());
		listMaleEntriesLabel.setStyleName(Bootstrap.Typography.H5.styleName());
		listMaleEntriesLabel.addStyleName(AppConstants.CssStyles.BOLD);
		listMaleEntriesLabel.setWidth("120px");
		
        lblNoOfFemaleEntries = new Label(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
          		 + "  <b>0</b>", Label.CONTENT_XHTML);
        lblNoOfFemaleEntries.setWidth("135px");
        initializeFemaleParentsTable();
        
        lblNoOfMaleEntries = new Label(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
         		 + "  <b>0</b>", Label.CONTENT_XHTML);
        lblNoOfMaleEntries.setWidth("135px");
        initializeMaleParentsTable();

        actionFemaleListButton = new Button(messageSource.getMessage(Message.ACTIONS));
        actionFemaleListButton.setIcon(AppConstants.Icons.ICON_TOOLS);
        actionFemaleListButton.setWidth("110px");
        actionFemaleListButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        
        actionMaleListButton = new Button(messageSource.getMessage(Message.ACTIONS));
        actionMaleListButton.setIcon(AppConstants.Icons.ICON_TOOLS);
        actionMaleListButton.setWidth("110px");
        actionMaleListButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        
        femaleListMenu = new ContextMenu();
        femaleListMenu.setWidth("250px");
        femaleListMenu.addItem(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES));
        saveFemaleListMenu = femaleListMenu.addItem(messageSource.getMessage(Message.SAVE_LIST));
        saveFemaleListMenu.setEnabled(false);
        femaleListMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));

        maleListMenu = new ContextMenu();
        maleListMenu.setWidth("250px");
        maleListMenu.addItem(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES));
        saveMaleListMenu = maleListMenu.addItem(messageSource.getMessage(Message.SAVE_LIST));
        saveMaleListMenu.setEnabled(false);
        maleListMenu.addItem(messageSource.getMessage(Message.SELECT_ALL));

        maleParentContainer = new ParentContainer(saveMaleListMenu, maleTableWithSelectAll, 
        		MALE_PARENTS_LABEL, Message.SUCCESS_SAVE_FOR_MALE_LIST);
        femaleParentContainer = new ParentContainer(saveFemaleListMenu, femaleTableWithSelectAll, 
        		FEMALE_PARENTS_LABEL, Message.SUCCESS_SAVE_FOR_FEMALE_LIST);
        
	}

	
	private void initializeMaleParentsTable() {
		maleTableWithSelectAll = new TableWithSelectAllLayout(PARENTS_TABLE_ROW_COUNT, TAG_COLUMN_ID);
        maleParents = maleTableWithSelectAll.getTable();
        maleParentsTagAll = maleTableWithSelectAll.getCheckBox();
        
        maleParents.setWidth("100%");
        maleParents.setNullSelectionAllowed(true);
        maleParents.setSelectable(true);
        maleParents.setMultiSelect(true);
        maleParents.setImmediate(true);
        maleParents.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        maleParents.addContainerProperty(ENTRY_NUMBER_COLUMN_ID, Integer.class, Integer.valueOf(0));
        maleParents.addContainerProperty(MALE_PARENTS_LABEL, Button.class, null);
        
        maleParents.setColumnHeader(TAG_COLUMN_ID, messageSource.getMessage(Message.CHECK_ICON));
        maleParents.setColumnHeader(ENTRY_NUMBER_COLUMN_ID, messageSource.getMessage(Message.HASHTAG));
        maleParents.setColumnHeader(MALE_PARENTS_LABEL, messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER));
        
        maleParents.setColumnWidth(TAG_COLUMN_ID, 25);
        maleParents.setDragMode(TableDragMode.ROW);
        
        maleParents.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                             
			private static final long serialVersionUID = -3207714818504151649L;

			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if(propertyId != null && propertyId == MALE_PARENTS_LABEL) {
			    	Table theTable = (Table) source;
			    	Item item = theTable.getItem(itemId);
			    	String name = (String) item.getItemProperty(MALE_PARENTS_LABEL).getValue();
			    	return name;
			    }                                                                       
			    return null;
			}
		});
	}

	
	private void setupMaleTableDropHandler() {
		maleParents.setDropHandler(new DropHandler() {
            private static final long serialVersionUID = -6464944116431652229L;

				@SuppressWarnings("unchecked")
				public void drop(DragAndDropEvent dropEvent) {

					//Dragged from a table
					if(dropEvent.getTransferable() instanceof TableTransferable){
					
	                    TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
	                        
	                    Table sourceTable = (Table) transferable.getSourceComponent();
	                    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
	                    
	                    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
	                    Object targetItemId = dropData.getItemIdOver();
	
	                    if(sourceTable.equals(maleParents)){
	                    	Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) sourceTable.getValue();
	                        
		                    //Check first if item is dropped on top of itself
		                    if(!transferable.getItemId().equals(targetItemId)){
		                        
		                        GermplasmListEntry germplasmEntry = (GermplasmListEntry)transferable.getItemId();
		                        
		                        Button gidButton = new Button(germplasmEntry.getDesignation(), new GidLinkClickListener(germplasmEntry.getGid().toString(),true));
		                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		                        gidButton.setDescription("Click to view Germplasm information");
		                        
		                        GermplasmListEntry maleItemId = (GermplasmListEntry) transferable.getItemId();
		                        CheckBox tag = (CheckBox) sourceTable.getItem(maleItemId).getItemProperty(TAG_COLUMN_ID).getValue();
		                        	
		                        sourceTable.removeItem(transferable.getItemId());
		                        
								Item item = targetTable.addItemAfter(targetItemId, maleItemId);
		                      	item.getItemProperty(MALE_PARENTS_LABEL).setValue(gidButton);
		                      	item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
		                      	
		                      	if(selectedEntries.contains(maleItemId)){
									tag.setValue(true);
									tag.addListener(new ParentsTableCheckboxListener(targetTable, maleItemId, maleParentsTagAll));
						            tag.setImmediate(true);
						            targetTable.select(transferable.getItemId());
								}
		                	}
	                    } else if(sourceTable.getData().equals(SelectParentsListDataComponent.LIST_DATA_TABLE_ID)){
	                    	dropToFemaleOrMaleTable(sourceTable, maleParents, (Integer) transferable.getItemId());
	                    }
	                    
					} 
					
					assignEntryNumber(maleParents);
					updateMaleNoOfEntries(maleParents.size());
                }

                public AcceptCriterion getAcceptCriterion() {
                	return AcceptAll.get();
                }
        });
	}

	private void initializeFemaleParentsTable() {
		femaleTableWithSelectAll = new TableWithSelectAllLayout(PARENTS_TABLE_ROW_COUNT, TAG_COLUMN_ID);
        femaleParents = femaleTableWithSelectAll.getTable();
        femaleParentsTagAll = femaleTableWithSelectAll.getCheckBox();
        
        femaleParents.setWidth("100%");
        femaleParents.setNullSelectionAllowed(true);
        femaleParents.setSelectable(true);
        femaleParents.setMultiSelect(true);
        femaleParents.setImmediate(true);
        femaleParents.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        femaleParents.addContainerProperty(ENTRY_NUMBER_COLUMN_ID, Integer.class, Integer.valueOf(0));
        femaleParents.addContainerProperty(FEMALE_PARENTS_LABEL, Button.class, null);

        femaleParents.setColumnHeader(TAG_COLUMN_ID, messageSource.getMessage(Message.CHECK_ICON));
        femaleParents.setColumnHeader(ENTRY_NUMBER_COLUMN_ID, messageSource.getMessage(Message.HASHTAG));
        femaleParents.setColumnHeader(FEMALE_PARENTS_LABEL, messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER));
        
        femaleParents.setColumnWidth(TAG_COLUMN_ID, 25);
        femaleParents.setDragMode(TableDragMode.ROW);
        femaleParents.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                             
			private static final long serialVersionUID = -3207714818504151649L;

			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if(propertyId != null && propertyId == FEMALE_PARENTS_LABEL) {
			    	Table theTable = (Table) source;
			    	Item item = theTable.getItem(itemId);
			    	String name = (String) item.getItemProperty(FEMALE_PARENTS_LABEL).getValue();
			    	return name;
			    }                                                                       
			    return null;
			}
		});
	}

	private void setupFemaleDropHandler() {
		femaleParents.setDropHandler(new DropHandler() {
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
	                    
	                    if(sourceTable.equals(femaleParents)){
		                    Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) sourceTable.getValue();
		
		                    //Check first if item is dropped on top of itself
		                    if(!transferable.getItemId().equals(targetItemId)){
		                		GermplasmListEntry germplasmEntry = (GermplasmListEntry)transferable.getItemId();
		                		
		                		Button gidButton = new Button(germplasmEntry.getDesignation(), new GidLinkClickListener(germplasmEntry.getGid().toString(),true));
		                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		                        gidButton.setDescription("Click to view Germplasm information");
		                		
		                		GermplasmListEntry femaleItemId = (GermplasmListEntry) transferable.getItemId();
		                		CheckBox tag = (CheckBox) sourceTable.getItem(femaleItemId).getItemProperty(TAG_COLUMN_ID).getValue();
								
		                		sourceTable.removeItem(transferable.getItemId());
		                		
								Item item = targetTable.addItemAfter(targetItemId, transferable.getItemId());
		                    	item.getItemProperty(FEMALE_PARENTS_LABEL).setValue(gidButton);
		                      	item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
		                      	
		                      	if(selectedEntries.contains(femaleItemId)){
		                      		tag.setValue(true);
									tag.addListener(new ParentsTableCheckboxListener(targetTable, femaleItemId, femaleParentsTagAll));
						            tag.setImmediate(true);
						            targetTable.select(transferable.getItemId());
								} 	
		                      	
		                    }
	                    } else if(sourceTable.getData().equals(SelectParentsListDataComponent.LIST_DATA_TABLE_ID)){
	                    	dropToFemaleOrMaleTable(sourceTable, femaleParents, (Integer) transferable.getItemId());
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
	                    				String femaleParentValue = listData.getDesignation();
	                    				
	                    				Button gidButton = new Button(femaleParentValue, new GidLinkClickListener(listData.getGid().toString(),true));
	    		                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
	    		                        gidButton.setDescription("Click to view Germplasm information");
	                    				
	                    				CheckBox tag = new CheckBox();
			                        	
	                    				GermplasmListEntry entryObject = new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(), listData.getDesignation(), draggedListFromTree.getName()+":"+listData.getEntryId());
	                    				
	                		    		if(targetTable.equals(femaleParents)){
	                		    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, femaleParentsTagAll));
	                		    			femaleListNameForCrosses = draggedListFromTree.getName();
	                		    	    	updateCrossesSeedSource(femaleParentContainer, draggedListFromTree);
	                		    		}
	                		    		
	                		            tag.setImmediate(true);
	                    				
	                		            //if the item is already existing in the target table, remove the existing item then add a new entry
	                		            targetTable.removeItem(entryObject);
	                		            
	                    				Item item = targetTable.addItem(entryObject);
	                    				
	                    				item.getItemProperty(FEMALE_PARENTS_LABEL).setValue(gidButton);
	                    				item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
	                    				
	                    				addedCount++;
	                    			} 
			                	}
	                    		
	                    		//After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this will enable/disable the save option
	                    		List<Object> itemsAfterAdding = new ArrayList<Object>();
	                    		itemsAfterAdding.addAll((Collection<? extends Integer>) targetTable.getItemIds());
	                    		
	                    		if(addedCount==itemsAfterAdding.size()){
	                    			saveFemaleListMenu.setEnabled(false);
	                    			
	                    			//updates the crossesMade.savebutton if both parents are save at least once;
	                        		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
	                        		
	                    		} else {
	                    			saveFemaleListMenu.setEnabled(true);
	                    			//femaleParentList = null;
	                    		}
	                    	}
	                    } catch(MiddlewareQueryException e) {
	                    	LOG.error("Error in getting list by GID",e);	
	                    }
					}
                    assignEntryNumber(femaleParents);
                    updateFemaleNoOfEntries(femaleParents.size());
                }

                public AcceptCriterion getAcceptCriterion() {
                	return AcceptAll.get();
                }
        });
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		setupFemaleDropHandler();
		femaleParentsActionListener = new CrossingManagerActionHandler(this);
        femaleParents.addActionHandler(femaleParentsActionListener);
        
        setupMaleTableDropHandler();
        maleParentsActionListener = new CrossingManagerActionHandler(this);
        maleParents.addActionHandler(maleParentsActionListener);
        
        actionFemaleListButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				femaleListMenu.show(event.getClientX(), event.getClientY());
			}
        	
        });
		
		femaleListMenu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
			
			@Override
			public void contextItemClick(
					org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				 ContextMenuItem clickedItem = event.getClickedItem();
				  
				 if(clickedItem.getName().equals(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES))){
					 femaleParentsActionListener.removeSelectedEntriesAction(femaleParents);
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SAVE_LIST))){
					 saveFemaleParentList();
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
					 femaleParents.setValue(femaleParents.getItemIds());
				 }
				
			}
		});
		
		
        
        actionMaleListButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				maleListMenu.show(event.getClientX(), event.getClientY());
			}
        	
        });
        
		maleListMenu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
			
			@Override
			public void contextItemClick(
					org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				 ContextMenuItem clickedItem = event.getClickedItem();
				  
				 if(clickedItem.getName().equals(messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES))){
					 maleParentsActionListener.removeSelectedEntriesAction(maleParents);
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SAVE_LIST))){
					 saveMaleParentList();
				 }
				 else if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
					 maleParents.setValue(maleParents.getItemIds());
				 }
				
			}
		});
	}

	@Override
	public void layoutComponents() {
        setSpacing(true);
        setMargin(false,false,false,true);
        setWidth("450px");
        
        VerticalLayout femaleParentsTableLayout = createFemaleListTab();
        VerticalLayout maleParentsTableLayout = createMaleListTab();

        parentTabSheet = new TabSheet();
        parentTabSheet.addTab(femaleParentsTableLayout,messageSource.getMessage(Message.LABEL_FEMALE_PARENTS));
        parentTabSheet.addTab(maleParentsTableLayout,messageSource.getMessage(Message.LABEL_MALE_PARENTS));
        parentTabSheet.setWidth("420px");
        parentTabSheet.setHeight("460px");
        
        HeaderLabelLayout parentLabelLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES,parentListsLabel);
        addComponent(parentLabelLayout);
        addComponent(instructionForParentLists);
        addComponent(parentTabSheet);
        addComponent(femaleListMenu);
        addComponent(maleListMenu);
	}// end of layoutComponent

	private VerticalLayout createMaleListTab() {
		VerticalLayout maleParentsTableLayout = new VerticalLayout();
		maleParentsTableLayout.setMargin(true,true,false,true);
		maleParentsTableLayout.setSpacing(true);
		
		maleParentsTableLayout.addComponent(listMaleEntriesLabel);
		
		HorizontalLayout subHeadingLayout = new HorizontalLayout();
		subHeadingLayout.setWidth("100%");
		subHeadingLayout.addComponent(lblNoOfMaleEntries);
		subHeadingLayout.addComponent(actionMaleListButton);

		subHeadingLayout.setComponentAlignment(lblNoOfMaleEntries, Alignment.MIDDLE_LEFT);
		subHeadingLayout.setComponentAlignment(actionMaleListButton, Alignment.TOP_RIGHT);
		
		maleParentsTableLayout.addComponent(subHeadingLayout);
		maleParentsTableLayout.addComponent(maleTableWithSelectAll);
		return maleParentsTableLayout;
	}

	private VerticalLayout createFemaleListTab() {
		VerticalLayout femaleParentsTableLayout = new VerticalLayout();
		femaleParentsTableLayout.setMargin(true,true,false,true);
		femaleParentsTableLayout.setSpacing(true);
		
		femaleParentsTableLayout.addComponent(listFemaleEntriesLabel);
		
		HorizontalLayout subHeadingLayout = new HorizontalLayout();
		subHeadingLayout.setWidth("100%");
		subHeadingLayout.addComponent(lblNoOfFemaleEntries);
		subHeadingLayout.addComponent(actionFemaleListButton);
		subHeadingLayout.setComponentAlignment(lblNoOfFemaleEntries, Alignment.MIDDLE_LEFT);
		subHeadingLayout.setComponentAlignment(actionFemaleListButton, Alignment.TOP_RIGHT);
		
		femaleParentsTableLayout.addComponent(subHeadingLayout);
		femaleParentsTableLayout.addComponent(femaleTableWithSelectAll);
		return femaleParentsTableLayout;
	}

	@SuppressWarnings("unchecked")
	public void dropToFemaleOrMaleTable(Table sourceTable, Table targetTable, Integer transferrableItemId){
		List<Integer> selectedListEntries = new ArrayList<Integer>();
    	selectedListEntries.addAll((Collection<Integer>) sourceTable.getValue());
    	
    	if(selectedListEntries.isEmpty() && transferrableItemId != null){
    		selectedListEntries.add(transferrableItemId);
    	}
    	
    	List<Integer> entryIdsInSourceTable = new ArrayList<Integer>();
    	entryIdsInSourceTable.addAll((Collection<Integer>) sourceTable.getItemIds());
    	
    	List<Integer> initialEntryIdsInDestinationTable = new ArrayList<Integer>();
    	initialEntryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());    	
    	
    	for(Integer itemId : entryIdsInSourceTable){
    		if(selectedListEntries.contains(itemId)){
	    		Integer entryId = (Integer) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue();
	    		
	    		Button designationBtn = (Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue(); 
	    		String designation = designationBtn.getCaption();
	    		
	    		Button gidBtn = (Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
	    		Integer gid = Integer.valueOf(Integer.parseInt(gidBtn.getCaption()));
	    		
	    		String seedSource = getSeedSource(sourceTable,entryId);
	    		
	    		GermplasmListEntry entryObject = new GermplasmListEntry(itemId, gid, entryId, designation, seedSource);
	    		Item item = targetTable.addItem(entryObject);
	    		
	    		if(item != null){
	    			
	    			Button newGidButton = new Button(designation, new GidLinkClickListener(gid.toString(),true));
	    			newGidButton.setStyleName(BaseTheme.BUTTON_LINK);
	    			newGidButton.setDescription("Click to view Germplasm information");
	    			
		    		if(targetTable.equals(femaleParents)){
		    			item.getItemProperty(FEMALE_PARENTS_LABEL).setValue(newGidButton);
		    			entryObject.setFromFemaleTable(true);
		    			saveFemaleListMenu.setEnabled(true);
		    			updateFemaleNoOfEntries(femaleParents.size());
            			//femaleParentList = null;
		    		} else{
		    			item.getItemProperty(MALE_PARENTS_LABEL).setValue(newGidButton);
		    			entryObject.setFromFemaleTable(false);
		    			saveMaleListMenu.setEnabled(true);
		    			updateMaleNoOfEntries(maleParents.size());
            			//maleParentList = null;
		    		}
		    		
		    		CheckBox tag = new CheckBox();
		    		if(targetTable.equals(femaleParents)){
		    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, femaleParentsTagAll));
		    		} else{
		    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, maleParentsTagAll));
		    		}
		            tag.setImmediate(true);
		            item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
		        }
	    	}
            targetTable.requestRepaint();
        }
    	
    	List<Integer> entryIdsInDestinationTable = new ArrayList<Integer>();
    	entryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());
    	    	
    	if(initialEntryIdsInDestinationTable.size()==0 && entryIdsInSourceTable.size()==entryIdsInDestinationTable.size()){
    		if(targetTable.equals(femaleParents)){
    			GermplasmList femaleGermplasmList = ((SelectParentsListDataComponent) makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet().getSelectedTab()).getGermplasmList();
    			
    			//Checks the source list is a local list
    			if(femaleGermplasmList.getId() < 0){
    				saveFemaleListMenu.setEnabled(false);
        			femaleListNameForCrosses = femaleGermplasmList.getName();
        	    	updateCrossesSeedSource(femaleParentContainer, femaleGermplasmList);
    			}
    			else{//if the source list is a central list
    				saveFemaleListMenu.setEnabled(true);
    				femaleListNameForCrosses = "";
        			femaleParentList = null;
    			}
    			
    		} else{//if male
    			GermplasmList maleGermplasmList = ((SelectParentsListDataComponent) makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet().getSelectedTab()).getGermplasmList();
    			
    			//Checks the source list is a local list
    			if(maleGermplasmList.getId() < 0){
    				saveMaleListMenu.setEnabled(false);
        			maleListNameForCrosses = maleGermplasmList.getName();
        	    	updateCrossesSeedSource(maleParentContainer, maleGermplasmList);
    			}
    			else{//if the source list is a central list
    				saveMaleListMenu.setEnabled(true);
    				maleListNameForCrosses = "";
        			maleParentList = null;
    			}
    		}
    		
    		//updates the crossesMade.savebutton if both parents are save at least once;
    		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
    	} else {
    		if(targetTable.equals(femaleParents)){
    			saveFemaleListMenu.setEnabled(true);
    			femaleListNameForCrosses = "";
    			femaleParentList = null;
    		} else{
    			saveMaleListMenu.setEnabled(true);
    			maleListNameForCrosses = "";
    			maleParentList = null;
    		}
    	}
    	
	}
	
	@SuppressWarnings("unused")
	private boolean checkIfGIDisInTable(Table targetTable, Integer gid){
		for(Object itemId : targetTable.getItemIds()){
			GermplasmListEntry entry = (GermplasmListEntry) itemId;
			if(gid.equals(entry.getGid())){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void assignEntryNumber(Table parentsTable){
		
		int entryNumber = 1;
		List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) parentsTable.getItemIds());
		
		for(GermplasmListEntry entry : itemIds){
			Item item = parentsTable.getItem(entry);
    		item.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(Integer.valueOf(entryNumber));
    		entry.setEntryId(entryNumber);
			entryNumber++;
		}
	}
	
	private void saveFemaleParentList() {
    	saveListAsWindow = null;
    	if(femaleParentList != null){
    		saveListAsWindow = new SaveListAsDialog(this,femaleParentList);
    	}
    	else{
    		saveListAsWindow = new SaveListAsDialog(this,null);
    	}
        
        saveListAsWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        saveListAsWindow.setData(femaleParentContainer);
        this.getWindow().addWindow(saveListAsWindow);
    }
    
    private void saveMaleParentList() {
    	saveListAsWindow = null;
    	
    	if(maleParentList != null){
    		saveListAsWindow = new SaveListAsDialog(this,maleParentList);
    	}
    	else{
    		saveListAsWindow = new SaveListAsDialog(this,null);
    	}
        
        saveListAsWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        saveListAsWindow.setData(maleParentContainer);
        this.getWindow().addWindow(saveListAsWindow);
    }

	@SuppressWarnings("unchecked")
	@Override
	public void saveList(GermplasmList list) {
		ParentContainer parentContainer = (ParentContainer)saveListAsWindow.getData();
		List<GermplasmListEntry> listEntries = new ArrayList<GermplasmListEntry>();
		listEntries.addAll((Collection<GermplasmListEntry>) parentContainer.getTableWithSelectAll().getTable().getItemIds());
		
		//TO DO correct the entryID, get from the parent table
		// Create Map <Key: "GID+ENTRYID">, <Value:CheckBox Obj>
		SaveGermplasmListAction saveListAction = new SaveGermplasmListAction(this, list, listEntries);
		try {
			GermplasmList savedList = saveListAction.saveRecords();
			updateCrossesSeedSource(parentContainer, savedList);
			updateUIForSuccessfulSaving(parentContainer, savedList);

		} catch (MiddlewareQueryException e) {
			LOG.error("Error in saving the Parent List",e);
			e.printStackTrace();
		}
	}


	private void updateCrossesSeedSource(ParentContainer parentContainer,
			GermplasmList savedList) {
		if (parentContainer.equals(femaleParentContainer)){
			this.femaleParentList = savedList;
			if (femaleListNameForCrosses != null && !femaleListNameForCrosses.equals(femaleParentList.getName())){
				femaleListNameForCrosses = femaleParentList.getName();
				makeCrossesMain.updateCrossesSeedSource(femaleListNameForCrosses, 
						maleListNameForCrosses);
			}
		} else {
			this.maleParentList = savedList;
			if (maleListNameForCrosses != null && !maleListNameForCrosses.equals(maleParentList.getName())){
				maleListNameForCrosses = maleParentList.getName();
				makeCrossesMain.updateCrossesSeedSource(femaleListNameForCrosses, 
						maleListNameForCrosses);
			}
		}
	}
	
	private void updateUIForSuccessfulSaving(ParentContainer parentContainer, GermplasmList list) {
		parentContainer.getOption().setEnabled(false);
		makeCrossesMain.toggleNextButton();
		
		makeCrossesMain.getSelectParentsComponent().selectListInTree(list.getId());
		makeCrossesMain.getSelectParentsComponent().updateUIForDeletedList(list);
		
		//updates the crossesMade.savebutton if both parents are save at least once;
		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
		
		MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
				messageSource.getMessage(parentContainer.getSuccessMessage()));
	}
	
	public void updateFemaleListNameForCrosses(){
		this.femaleListNameForCrosses = getFemaleList() != null ? getFemaleList().getName() : "";
	}
	
	public void updateMaleListNameForCrosses(){
		this.maleListNameForCrosses = getMaleList() != null ? getMaleList().getName() : "";
	}
	
    public boolean isFemaleListSaved(){
    	if(femaleListNameForCrosses != null){
    		return (femaleListNameForCrosses.length() > 0);
    	}
    	return false;
    }
    
    public boolean isMaleListSaved(){
    	if(maleListNameForCrosses != null){
    		return (maleListNameForCrosses.length() > 0);
    	}
    	return false;
    }
	
	public String getSeedSource(Table table, Integer entryId){
		String seedSource = "";
		if(table.getParent().getParent() instanceof SelectParentsListDataComponent ){
			SelectParentsListDataComponent parentComponent = (SelectParentsListDataComponent) table.getParent().getParent();
			String listname = parentComponent.getListName();			
			seedSource = listname + ":" + entryId;
		}
		
		return seedSource;
	}

    
    /**
     * Implemented something similar to table.getValue(), because that method returns
     *     a collection of items, but does not follow the sorting done by the 
     *     drag n drop sorting, this one does
     * @param table
     * @return List of selected germplasm list entries
     */
    @SuppressWarnings("unchecked")
	protected List<GermplasmListEntry> getCorrectSortedValue(Table table){
    	List<GermplasmListEntry> allItemIds = new ArrayList<GermplasmListEntry>();
    	List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
    	List<GermplasmListEntry> sortedSelectedValues = new ArrayList<GermplasmListEntry>();
    	
    	allItemIds.addAll((Collection<GermplasmListEntry>) table.getItemIds());
    	selectedItemIds.addAll((Collection<GermplasmListEntry>) table.getValue());

    	for(GermplasmListEntry entry : allItemIds){
			CheckBox tag = (CheckBox) table.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
			Boolean tagValue = (Boolean) tag.getValue();
			if(tagValue.booleanValue()){
				selectedItemIds.add(entry);
			} 
		}
    	
    	for(GermplasmListEntry itemId : allItemIds){
    		for(GermplasmListEntry selectedItemId : selectedItemIds){
    			if(itemId.equals(selectedItemId))
    				sortedSelectedValues.add(selectedItemId);    			
    		}
    	}
    	return sortedSelectedValues;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void updateListDataTable(List<GermplasmListData> savedListEntries) {
		ParentContainer container = (ParentContainer) saveListAsWindow.getData();

		List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
		Table table = container.getTableWithSelectAll().getTable();
		
		selectedItemIds.addAll((Collection<GermplasmListEntry>) table.getValue());
		table.removeAllItems();
		
		for(GermplasmListData entry : savedListEntries){
			GermplasmListEntry itemId = new GermplasmListEntry(entry.getId(),entry.getGid(), entry.getEntryId(), entry.getDesignation(), entry.getSeedSource());
			
			Item newItem = table.addItem(itemId);
			
			CheckBox tag = new CheckBox();
			newItem.getItemProperty(TAG_COLUMN_ID).setValue(tag);
			
			tag.addListener(new ParentsTableCheckboxListener(table, itemId, container.getTableWithSelectAll().getCheckBox()));
            tag.setImmediate(true);
            
            if(selectedItemIds.contains(itemId)){
            	table.select(itemId);
            }
            
			newItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(entry.getEntryId());
			
			String designationName = entry.getDesignation();
			
			Button gidButton = new Button(designationName, new GidLinkClickListener(entry.getGid().toString(),true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
            gidButton.setDescription("Click to view Germplasm information");
			
			newItem.getItemProperty(container.getColumnName()).setValue(gidButton);

		}
		
		table.requestRepaint();
	}

    public void updateFemaleNoOfEntries(Integer numOfEntries){
    	lblNoOfFemaleEntries.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
        		 + "  <b>" + numOfEntries + "</b>");
    }

    public void updateMaleNoOfEntries(Integer numOfEntries){
    	lblNoOfMaleEntries.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
       		 + "  <b>" + numOfEntries + "</b>");
    }
    
	private class ParentContainer {
		private ContextMenuItem option;
		private TableWithSelectAllLayout tableWithSelectAll;
		private String columnName;
		private Message successMessage;
		
		public ParentContainer(ContextMenuItem option, TableWithSelectAllLayout tableWithSelectAll,
				String columnName, Message successMessage) {
			super();
			this.option = option;
			this.tableWithSelectAll = tableWithSelectAll;
			this.columnName = columnName;
			this.successMessage = successMessage;
		}

		public ContextMenuItem getOption() {
			return option;
		}
	
		public TableWithSelectAllLayout getTableWithSelectAll() {
			return tableWithSelectAll;
		}


		public Message getSuccessMessage() {
			return successMessage;
		}

		public String getColumnName() {
			return columnName;
		}
	}
	
	//SETTERS AND GETTERS
    public Table getFemaleTable(){
    	return femaleParents;
    }
    
    public Table getMaleTable(){
    	return maleParents;
    }
    
    public GermplasmList getFemaleList(){
    	return femaleParentList;
    }
    
    public GermplasmList getMaleList(){
    	return maleParentList;
    }
    
    public void setFemaleParentList(GermplasmList list){
    	femaleParentList = list;
    }
    
    public void setMaleParentList(GermplasmList list){
    	maleParentList = list;
    }
    
	public String getFemaleListNameForCrosses() {
		return femaleListNameForCrosses;
	}

	public String getMaleListNameForCrosses() {
		return maleListNameForCrosses;
	}

	public TabSheet getParentTabSheet() {
		return parentTabSheet;
	}

	public ContextMenuItem getSaveFemaleListMenu() {
		return saveFemaleListMenu;
	}

	public ContextMenuItem getSaveMaleListMenu() {
		return saveMaleListMenu;
	}

	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
		
	}

	@Override
	public Component getParentComponent() {
		return makeCrossesMain.getSource();
	}
	
	
	public void addListToMaleTable(Integer germplasmListId){
		
        try {
        	GermplasmList draggedListFromTree = germplasmListManager.getGermplasmListById(germplasmListId);
        	if(draggedListFromTree!=null){
        		List<GermplasmListData> germplasmListDataFromListFromTree = draggedListFromTree.getListData();
        		
        		Integer addedCount = 0;
        		
        		for(GermplasmListData listData : germplasmListDataFromListFromTree){
        			if(listData.getStatus()!=9){
        				String maleParentValue = listData.getDesignation();
        				
                        Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(),true));
                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                        gidButton.setDescription("Click to view Germplasm information");
                        
        				CheckBox tag = new CheckBox();
                    	
        				GermplasmListEntry entryObject = new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(), listData.getDesignation(), draggedListFromTree.getName()+":"+listData.getEntryId());
        				
    		    		
    		    		tag.addListener(new ParentsTableCheckboxListener(maleParents, entryObject, maleParentsTagAll));
    		    		maleListNameForCrosses = draggedListFromTree.getName();
    		    	    updateCrossesSeedSource(maleParentContainer, draggedListFromTree);
    		    		
    		    		
    		            tag.setImmediate(true);
        				
    		            //if the item is already existing in the target table, remove the existing item then add a new entry
    		            maleParents.removeItem(entryObject);
    		            
        				Item item = maleParents.addItem(entryObject);
        				item.getItemProperty(MALE_PARENTS_LABEL).setValue(gidButton);
        				item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
        				
        				addedCount++;
        			} 
            	}
        		
        		//After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this will enable/disable the save option
        		List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
        		itemsLeftAfterAdding.addAll((Collection<? extends Integer>) maleParents.getItemIds());

        		if(addedCount==itemsLeftAfterAdding.size()){
        			saveMaleListMenu.setEnabled(false);
        			
        			//updates the crossesMade.savebutton if both parents are save at least once;
            		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
            		
        		} else {
        			saveMaleListMenu.setEnabled(true);
        			//maleParentList = null;
        		}
        	}
        	
        	
        } catch(MiddlewareQueryException e) {
        	LOG.error("Error in getting list by GID",e);	
        }
        
        assignEntryNumber(maleParents);
		updateMaleNoOfEntries(maleParents.size());
        
        
	}
	
	
	public void addListToFemaleTable(Integer germplasmListId){
		
        try {
        	GermplasmList draggedListFromTree = germplasmListManager.getGermplasmListById(germplasmListId);
        	if(draggedListFromTree!=null){
        		List<GermplasmListData> germplasmListDataFromListFromTree = draggedListFromTree.getListData();
        		
        		Integer addedCount = 0;
        		
        		for(GermplasmListData listData : germplasmListDataFromListFromTree){
        			if(listData.getStatus()!=9){
        				String maleParentValue = listData.getDesignation();
        				
                        Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(),true));
                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                        gidButton.setDescription("Click to view Germplasm information");
                        
        				CheckBox tag = new CheckBox();
                    	
        				GermplasmListEntry entryObject = new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(), listData.getDesignation(), draggedListFromTree.getName()+":"+listData.getEntryId());
        				
    		    		
    		    		tag.addListener(new ParentsTableCheckboxListener(femaleParents, entryObject, femaleParentsTagAll));
    		    		femaleListNameForCrosses = draggedListFromTree.getName();
    		    	    updateCrossesSeedSource(femaleParentContainer, draggedListFromTree);
    		    		
    		    		
    		            tag.setImmediate(true);
        				
    		            //if the item is already existing in the target table, remove the existing item then add a new entry
    		            femaleParents.removeItem(entryObject);
    		            
        				Item item = femaleParents.addItem(entryObject);
        				item.getItemProperty(FEMALE_PARENTS_LABEL).setValue(gidButton);
        				item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
        				
        				addedCount++;
        			} 
            	}
        		
        		//After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this will enable/disable the save option
        		List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
        		itemsLeftAfterAdding.addAll((Collection<? extends Integer>) femaleParents.getItemIds());

        		if(addedCount==itemsLeftAfterAdding.size()){
        			saveFemaleListMenu.setEnabled(false);
        			
        			//updates the crossesMade.savebutton if both parents are save at least once;
            		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
            		
        		} else {
        			saveFemaleListMenu.setEnabled(true);
        			//maleParentList = null;
        		}
        	}
        	
        	
        } catch(MiddlewareQueryException e) {
        	LOG.error("Error in getting list by GID",e);	
        }
        
        assignEntryNumber(femaleParents);
		updateMaleNoOfEntries(femaleParents.size());
        
        
	}

}

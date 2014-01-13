package org.generationcp.breeding.manager.listmanager;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.ResetListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.SaveListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.AddColumnContextMenu;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
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
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BuildNewListComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent {

	private static final Logger LOG = LoggerFactory.getLogger(BuildNewListComponent.class);
	
	private static final long serialVersionUID = 5314653969843976836L;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String GERMPLASMS_TABLE_DATA = "Germplasms Table Data";

	private Object source;
	
    public String DEFAULT_LIST_TYPE = "LST";
	
	private Label componentDescription;

    private Label listNameLabel;
    private Label descriptionLabel;
    private Label listTypeLabel;
    private Label listDateLabel;
    private Label notesLabel;
    
    private ComboBox listTypeComboBox;
    private DateField listDateField;
    private TextField listNameText;
    private TextField descriptionText;
    private TextArea notesTextArea;
    
	private Table germplasmsTable;
	
	private Button saveButton;
	private Button resetButton;
	private Button toolsButton;
	private Button addColumnButton;
	
	private static final ThemeResource ICON_TOOLS = new ThemeResource("images/tools.png");
	private static final ThemeResource ICON_PLUS = new ThemeResource("images/plus_icon.png");
	public static String TOOLS_BUTTON_ID = "Tools";
	
	private ContextMenu menu;
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuExportForGenotypingOrder;
	private ContextMenuItem menuCopyToList;
	
    static final Action ACTION_SELECT_ALL = new Action("Select All");
    static final Action ACTION_DELETE_SELECTED_ENTRIES = new Action("Delete Selected Entries");
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE_SELECTED_ENTRIES };

	private GermplasmList currentlySavedGermplasmList;
	private Window listManagerCopyToNewListDialog;
	private int germplasmListId;
	
	private AddColumnContextMenu addColumnContextMenu;
	private SaveListButtonClickListener saveListButtonClickListener;
	
	private FillWith fillWith;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	public BuildNewListComponent(ListManagerMain source){
		this.source = source;
		this.currentlySavedGermplasmList = null;
	}
	
	@Override
	public void updateLabels() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
				
		componentDescription = new Label();
		componentDescription.setValue(messageSource.getMessage(Message.BUILD_YOUR_LIST_BY_DRAGGING_LISTS_OR_GERMPLASM_RECORDS_INTO_THIS_NEW_LIST_WINDOW));
		componentDescription.setWidth("500px");
        addComponent(componentDescription,"top:0px;left:0px");
		
        listNameLabel = new Label();
        listNameLabel.setCaption(messageSource.getMessage(Message.NAME_LABEL)+":*");
        listNameLabel.addStyleName("bold");
        addComponent(listNameLabel, "top:55px;left:0px");
        
        listNameText = new TextField();
        listNameText.setWidth("200px");
        listNameText.setMaxLength(50);
        addComponent(listNameText, "top:35px;left:50px");

        listTypeLabel = new Label();
        listTypeLabel.setCaption(messageSource.getMessage(Message.TYPE_LABEL)+":*");
        listTypeLabel.addStyleName("bold");
        addComponent(listTypeLabel, "top:55px;left:260px");
        
        listTypeComboBox = new ComboBox();
        listTypeComboBox.setWidth("200px");
        listTypeComboBox.setNullSelectionAllowed(false);
        
        List<UserDefinedField> userDefinedFieldList = germplasmListManager.getGermplasmListTypes();
        String firstId = null;
              boolean hasDefault = false;
        for(UserDefinedField userDefinedField : userDefinedFieldList){
                  //method.getMcode()
            if(firstId == null){
                          firstId = userDefinedField.getFcode();
                      }
            listTypeComboBox.addItem(userDefinedField.getFcode());
            listTypeComboBox.setItemCaption(userDefinedField.getFcode(), userDefinedField.getFname());
                  if(DEFAULT_LIST_TYPE.equalsIgnoreCase(userDefinedField.getFcode())){
                      listTypeComboBox.setValue(userDefinedField.getFcode());
                      hasDefault = true;
                  }
              }
        if(hasDefault == false && firstId != null){
            listTypeComboBox.setValue(firstId);
           }

        listTypeComboBox.setTextInputAllowed(false);
        listTypeComboBox.setImmediate(true);
        addComponent(listTypeComboBox, "top:35px;left:302px");

        listDateLabel = new Label();
        listDateLabel.setCaption(messageSource.getMessage(Message.DATE_LABEL)+":*");
        listDateLabel.addStyleName("bold");
        addComponent(listDateLabel, "top:55px;left:515px");
      
        listDateField = new DateField();
        listDateField.setDateFormat(DATE_FORMAT);
        listDateField.setResolution(DateField.RESOLUTION_DAY);
        listDateField.setValue(new Date());
        addComponent(listDateField, "top:35px;left:557px");
        
        descriptionLabel = new Label();
        descriptionLabel.setCaption(messageSource.getMessage(Message.DESCRIPTION_LABEL)+"*");
        descriptionLabel.addStyleName("bold");
        addComponent(descriptionLabel, "top:90px;left:0px");
        
        descriptionText = new TextField();
        descriptionText.setWidth("565px");
        addComponent(descriptionText, "top:70px;left:89px");
		
        notesLabel = new Label();
        notesLabel.setCaption(messageSource.getMessage(Message.NOTES)+":");
        notesLabel.addStyleName("bold");
        addComponent(notesLabel, "top:55px; left: 675px;");
        notesLabel.setVisible(true);
		
        notesTextArea = new TextArea();
        notesTextArea.setWidth("250px");
        notesTextArea.setHeight("65px");
        notesTextArea.addStyleName("noResizeTextArea");
        addComponent(notesTextArea, "top:35px; left: 725px;");
        notesTextArea.setVisible(true);

        createGermplasmTable();
        
		menu = new ContextMenu();
		menu.addItem(messageSource.getMessage(Message.SELECT_ALL));
		menu.addItem(messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
		menuExportList = menu.addItem(messageSource.getMessage(Message.EXPORT_LIST));
		menuExportForGenotypingOrder = menu.addItem(messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING));
		menuCopyToList = menu.addItem(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
		
		resetMenuOptions();
		
        toolsButton = new Button(messageSource.getMessage(Message.TOOLS));
        toolsButton.setIcon(ICON_TOOLS);
        toolsButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
   	 	toolsButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				if(isCurrentListSave()){
					enableMenuOptionsAfterSave();
				}
				menu.show(event.getClientX(), event.getClientY());
			}
		 });
	 
   	 	addComponent(menu);
   	 	
   	 	
        addColumnButton = new Button();
        addColumnButton.setCaption(messageSource.getMessage(Message.ADD_COLUMN));
        addColumnButton.setIcon(ICON_PLUS);
        addColumnButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
   	 
   	 	setupAddColumnContextMenu();
   	 	
   	 	addComponent(addColumnButton, "top:0px; right:100px;");
   	 	addComponent(toolsButton, "top:0; right:0;");		
   	 	
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void contextItemClick(ClickEvent event) {
			    ContextMenuItem clickedItem = event.getClickedItem();
			    if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
			      	germplasmsTable.setValue(germplasmsTable.getItemIds());
			    }else if(clickedItem.getName().equals(messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))){
			      	deleteSelectedEntries();
			    }else if(clickedItem.getName().equals(messageSource.getMessage(Message.EXPORT_LIST))){
			    	exportListAction();
			    }else if(clickedItem.getName().equals(messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING))){
			    	exportListForGenotypingOrderAction();
			    }else if(clickedItem.getName().equals(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL))){
			    	copyToNewListAction();
			    }				
			}
			
        });
		
       
		
		HorizontalLayout buttonRow = new HorizontalLayout();
		buttonRow.setWidth("100%");
		buttonRow.setHeight("50px");
		buttonRow.setSpacing(true);
		
		saveButton = new Button();
		saveButton.setCaption(messageSource.getMessage(Message.SAVE_LIST));
		saveButton.setWidth("80px");
		saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		setupSaveButtonClickListener();
		
		resetButton = new Button();
		resetButton.setCaption(messageSource.getMessage(Message.RESET));
		resetButton.setWidth("80px");
		resetButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		resetButton.addListener(new ResetListButtonClickListener(this, messageSource));
		
		buttonRow.addComponent(resetButton);
		buttonRow.setComponentAlignment(resetButton, Alignment.MIDDLE_RIGHT);
		buttonRow.addComponent(saveButton);
		buttonRow.setComponentAlignment(saveButton, Alignment.MIDDLE_LEFT);
		
		addComponent(buttonRow, "top:400px; left:0px;");
		
		setWidth("100%");
		setHeight("550px");
		
		setupDragSources();
		setupDropHandlers();
		setupTableHeadersContextMenu();
	}
	
	public void resetMenuOptions(){
		//initially disabled when the current list building is not yet save or being reset
		menuExportList.setEnabled(false);
		menuExportForGenotypingOrder.setEnabled(false);
		menuCopyToList.setEnabled(false);
	}

	
	public void createGermplasmTable(){
		
		germplasmsTable = new Table();
		germplasmsTable.setData(GERMPLASMS_TABLE_DATA);
		germplasmsTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		germplasmsTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		germplasmsTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		germplasmsTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		germplasmsTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), String.class, null);
		germplasmsTable.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
//		germplasmsTable.addContainerProperty(ListDataTablePropertyID.STATUS.getName(), String.class, null);
		
		messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.ENTRY_ID.getName(), Message.LISTDATA_ENTRY_ID_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.PARENTAGE.getName(), Message.LISTDATA_GROUPNAME_HEADER);
//        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.STATUS.getName(), Message.LISTDATA_STATUS_HEADER);
		
		germplasmsTable.setSelectable(true);
		germplasmsTable.setMultiSelect(true);
		germplasmsTable.setWidth("100%");
		germplasmsTable.setHeight("280px");
		
        germplasmsTable.addActionHandler(new Action.Handler() {
			private static final long serialVersionUID = 1L;

			public Action[] getActions(Object target, Object sender) {
            	return GERMPLASMS_TABLE_CONTEXT_MENU;
            }

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(ACTION_SELECT_ALL == action) {
	        		germplasmsTable.setValue(germplasmsTable.getItemIds());
				} else if(ACTION_DELETE_SELECTED_ENTRIES == action) {
					deleteSelectedEntries();
				}
			}
        });
        
        addComponent(germplasmsTable, "top:115px; left:0px;");
	}
		
	/**
	 * Setup drag sources, this will tell Vaadin which tables can the user drag rows from
	 */
	public void setupDragSources(){
		if(source instanceof ListManagerMain){
			//Browse Lists tab
			
			/**
			 * TODO: enable draggable tables here
			 */

			//Germplasm list data is initialized in that component itself, and it can be multiple instances so it's best to put it there
			
			//Search Lists and Germplasms tab
			Table matchingGermplasmsTable = ((ListManagerMain) source).getListManagerSearchListsComponent().getSearchResultsComponent().getMatchingGermplasmsTable();
			Table matchingListsTable = ((ListManagerMain) source).getListManagerSearchListsComponent().getSearchResultsComponent().getMatchingListsTable();
			
			matchingGermplasmsTable.setDragMode(TableDragMode.ROW); 
			matchingListsTable.setDragMode(TableDragMode.ROW);
			germplasmsTable.setDragMode(TableDragMode.ROW);
		}
	}
	
	
	/**
	 * Setup drop handlers, this will dictate how Vaadin will handle drops (mouse releases) on the germplasm table
	 */
	public void setupDropHandlers(){
		germplasmsTable.setDropHandler(new DropHandler() {
			private static final long serialVersionUID = -6676297159926786216L;

			public void drop(DragAndDropEvent dropEvent) {
				TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
				
				Table sourceTable = (Table) transferable.getSourceComponent();
				
				setupInheritedColumnsFromSourceTable(sourceTable, germplasmsTable);
			    
			    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
                Object droppedOverItemId = dropData.getItemIdOver();
			    
                //Handle drops from MATCHING GERMPLASMS TABLE
                if(sourceTable.getData().equals(SearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA)){
                	
                	List<Integer> selectedItemIds = getSelectedItemIds(sourceTable);
                	
                	//If table has value (item/s is/are highlighted in the source table, add that)
                	if(selectedItemIds.size()>0){
                		for(int i=0;i<selectedItemIds.size();i++){
                			if(i==0)
                				addGermplasmToGermplasmTable(selectedItemIds.get(i), droppedOverItemId);
                			else 
                				addGermplasmToGermplasmTable(selectedItemIds.get(i), selectedItemIds.get(i-1));
                		}
                	//Add dragged item itself
                	} else {
                		addGermplasmToGermplasmTable(Integer.valueOf(transferable.getItemId().toString()), droppedOverItemId);
                	}
                	
                //Handle drops from MATCHING LISTS TABLE
                } else if(sourceTable.getData().equals(SearchResultsComponent.MATCHING_LISTS_TABLE_DATA)){
                	
                	List<Integer> selectedItemIds = getSelectedItemIds(sourceTable);
                	
                	//If table has value (item/s is/are highlighted in the source table, add that)
                	if(selectedItemIds.size()>0){
                		for(int i=0;i<selectedItemIds.size();i++){
                			if(i==0)
                				addGermplasmListDataToGermplasmTable(selectedItemIds.get(i), droppedOverItemId);
                			else
                				addGermplasmListDataToGermplasmTable(selectedItemIds.get(i), selectedItemIds.get(i-1));
                		}
                	//Add dragged item itself
                	} else {
                		addGermplasmListDataToGermplasmTable(Integer.valueOf(transferable.getItemId().toString()), droppedOverItemId);
            		}
                	
                //Handle drops from MATCHING GERMPLASMS TABLE
                } else if(sourceTable.getData().equals(ListDataComponent.LIST_DATA_COMPONENT_TABLE_DATA)){
                    	
                    	addGermplasmToGermplasmTable(transferable, droppedOverItemId);
                    	
                } else if(sourceTable.getData().equals(GERMPLASMS_TABLE_DATA)){
                    //Check first if item is dropped on top of itself
                    if(!transferable.getItemId().equals(droppedOverItemId)) {
                        
                        Item oldItem = germplasmsTable.getItem(transferable.getItemId());
                        Object oldGid = oldItem.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
                        Object oldEntryCode = oldItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue();
                        Object oldSeedSource = oldItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).getValue();
                        Object oldDesignation = oldItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue();
                        Object oldParentage = oldItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).getValue();
//                        Object oldStatus = oldItem.getItemProperty(ListDataTablePropertyID.STATUS.getName()).getValue();
                        germplasmsTable.removeItem(transferable.getItemId());
                        
                        Item newItem = germplasmsTable.addItemAfter(droppedOverItemId, transferable.getItemId());
                        newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(oldGid);
                        newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(oldEntryCode);
                        newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(oldSeedSource);
                        newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(oldDesignation);
                        newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(oldParentage);
//                        newItem.getItemProperty(ListDataTablePropertyID.STATUS.getName()).setValue(oldStatus);
                        
                        assignSerializedEntryNumber();
                    }
                }
			    
                updateAddedColumnValues();
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
		});
	}

    /**
     * Should be called just before data is inserted into the destination table, this will copy
     * whatever columns are available on the source table to the destination table 
     */
    private void setupInheritedColumnsFromSourceTable(Table sourceTable, Table destinationTable){
    	for(String addablePropertyId : AddColumnContextMenu.ADDABLE_PROPERTY_IDS){
    		if(AddColumnContextMenu.propertyExists(addablePropertyId, sourceTable) && !AddColumnContextMenu.propertyExists(addablePropertyId, destinationTable)){
    			AddColumnContextMenu addColumnContextMenu = new AddColumnContextMenu(destinationTable, ListDataTablePropertyID.GID.getName());
    			addColumnContextMenu.addColumn(addablePropertyId);
    		}
    	}
    	//dennis
    }
	
    
	/**
	 * Add germplasms from a gemrplasm list to the table
	 */
	private void addGermplasmListDataToGermplasmTable(Integer listId, Object droppedOnItemIdObject){
		
		int start = 0;
        int listDataCount;
        
        List<GermplasmListData> listDatas = new ArrayList<GermplasmListData>();
		try {
			listDataCount = (int) germplasmListManager.countGermplasmListDataByListId(listId);
			listDatas = this.germplasmListManager.getGermplasmListDataByListId(listId, start, listDataCount);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        for (GermplasmListData data : listDatas) {
        
			Item newItem;
			if(droppedOnItemIdObject!=null)
				newItem = germplasmsTable.addItem(getNextListEntryId());
			else
				newItem = germplasmsTable.addItemAfter(droppedOnItemIdObject, getNextListEntryId());

			Button gidButton = new Button(String.format("%s", data.getGid()), new GidLinkButtonClickListener(data.getGid().toString(), true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			
            String crossExpansion = "";
        	try {
        		if(germplasmDataManager!=null)
        			crossExpansion = germplasmDataManager.getCrossExpansion(data.getGid(), 1);
        	} catch(MiddlewareQueryException ex){
                crossExpansion = "-";
            }

            newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
			newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("From List Manager");
			newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(data.getDesignation());
			newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(crossExpansion);
//			newItem.getItemProperty(ListDataTablePropertyID.STATUS.getName()).setValue("0");
			
        }		
        assignSerializedEntryNumber();
	}

	
	/**
	 * Add a germplasm to a table, adds it after/before a certain germplasm given the droppedOn item id
	 * @param transferable
	 * @param droppedOn
	 */
	private void addGermplasmToGermplasmTable(TableTransferable transferable, Object droppedOnItemIdObject){
		Integer itemId = (Integer) transferable.getItemId();
		Table sourceTable = (Table) transferable.getSourceComponent();
		
        List<Integer> itemIds = getSelectedItemIds(sourceTable);

        Item newItem;

        setupInheritedColumnsFromSourceTable(sourceTable, germplasmsTable);
        
        if(itemIds.size()>0){
        	for(Integer currentItemId : itemIds){
        		if(droppedOnItemIdObject!=null)
        			newItem = germplasmsTable.addItem(getNextListEntryId());
        		else
        			newItem = germplasmsTable.addItemAfter(droppedOnItemIdObject, getNextListEntryId());
        		
        		Integer gid = Integer.valueOf(((Button) sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue()).getCaption());
        		
        		Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);

                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
        		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("From List Manager");
        		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue());
        		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue());
        		newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).getValue());
                
            	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.LOCATIONS))
            		newItem.getItemProperty(AddColumnContextMenu.LOCATIONS).setValue(sourceTable.getItem(currentItemId).getItemProperty(AddColumnContextMenu.LOCATIONS).getValue());
            	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_ID))
            		newItem.getItemProperty(AddColumnContextMenu.PREFERRED_ID).setValue(sourceTable.getItem(currentItemId).getItemProperty(AddColumnContextMenu.PREFERRED_ID).getValue());
            	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_NAME))
            		newItem.getItemProperty(AddColumnContextMenu.PREFERRED_NAME).setValue(sourceTable.getItem(currentItemId).getItemProperty(AddColumnContextMenu.PREFERRED_NAME).getValue());
        		
        	}
        } else {
    		if(droppedOnItemIdObject!=null)
    			newItem = germplasmsTable.addItem(getNextListEntryId());
    		else
    			newItem = germplasmsTable.addItemAfter(droppedOnItemIdObject, getNextListEntryId());

    		Integer gid = Integer.valueOf(((Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue()).getCaption());
    		
    		Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);

            newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
    		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("From List Manager");
    		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue());
    		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue());
    		newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).getValue());
            
        	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.LOCATIONS))
        		newItem.getItemProperty(AddColumnContextMenu.LOCATIONS).setValue(sourceTable.getItem(itemId).getItemProperty(AddColumnContextMenu.LOCATIONS).getValue());
        	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_ID))
        		newItem.getItemProperty(AddColumnContextMenu.PREFERRED_ID).setValue(sourceTable.getItem(itemId).getItemProperty(AddColumnContextMenu.PREFERRED_ID).getValue());
        	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_NAME))
        		newItem.getItemProperty(AddColumnContextMenu.PREFERRED_NAME).setValue(sourceTable.getItem(itemId).getItemProperty(AddColumnContextMenu.PREFERRED_NAME).getValue());        	
        }
        
		assignSerializedEntryNumber();
	}
    

	/**
	 * Add a germplasm to a table, adds it after/before a certain germplasm given the droppedOn item id
	 * @param transferable
	 * @param droppedOn
	 */
	public void addGermplasmToGermplasmTable(Table sourceTable, Object droppedOnItemIdObject){
        List<Integer> itemIds = getSelectedItemIds(sourceTable);

        Item newItem;

        setupInheritedColumnsFromSourceTable(sourceTable, germplasmsTable);
        
        if(itemIds.size()>0){
        	for(Integer currentItemId : itemIds){
        		if(droppedOnItemIdObject!=null)
        			newItem = germplasmsTable.addItem(getNextListEntryId());
        		else
        			newItem = germplasmsTable.addItemAfter(droppedOnItemIdObject, getNextListEntryId());
        		
        		Integer gid = Integer.valueOf(((Button) sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue()).getCaption());
        		
        		Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);

                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
        		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("From List Manager");
        		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue());
        		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue());
        		newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(sourceTable.getItem(currentItemId).getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).getValue());
                
            	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.LOCATIONS))
            		newItem.getItemProperty(AddColumnContextMenu.LOCATIONS).setValue(sourceTable.getItem(currentItemId).getItemProperty(AddColumnContextMenu.LOCATIONS).getValue());
            	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_ID))
            		newItem.getItemProperty(AddColumnContextMenu.PREFERRED_ID).setValue(sourceTable.getItem(currentItemId).getItemProperty(AddColumnContextMenu.PREFERRED_ID).getValue());
            	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_NAME))
            		newItem.getItemProperty(AddColumnContextMenu.PREFERRED_NAME).setValue(sourceTable.getItem(currentItemId).getItemProperty(AddColumnContextMenu.PREFERRED_NAME).getValue());
        		
        	}
        }
		assignSerializedEntryNumber();
	}
	
	
	
	/**
	 * Add a germplasm to a table, adds it after/before a certain germplasm given the droppedOn item id
	 * @param gid
	 * @param droppedOn
	 */
	public void addGermplasmToGermplasmTable(Integer gid, Object droppedOnItemIdObject){

		try {
			
			Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);

			Item newItem;
			if(droppedOnItemIdObject!=null)
				newItem = germplasmsTable.addItem(getNextListEntryId());
			else
				newItem = germplasmsTable.addItemAfter(droppedOnItemIdObject, getNextListEntryId());
			
			Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			
            String crossExpansion = "";
            if(germplasm!=null){
            	try {
            		if(germplasmDataManager!=null)
            			crossExpansion = germplasmDataManager.getCrossExpansion(germplasm.getGid(), 1);
            	} catch(MiddlewareQueryException ex){
                    crossExpansion = "-";
                }
        	}

            List<Integer> importedGermplasmGids = new ArrayList<Integer>();
	        importedGermplasmGids.add(gid);
            Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);
            String preferredName = preferredNames.get(gid); 
            
            newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
			newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("From List Manager");
			newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(preferredName);
			newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(crossExpansion);
//			newItem.getItemProperty(ListDataTablePropertyID.STATUS.getName()).setValue("0");
			
			assignSerializedEntryNumber();
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Iterates through the whole table, and sets the entry code from 1 to n based on the row position
	 */
	private void assignSerializedEntryCode(){
		List<Integer> itemIds = getItemIds(germplasmsTable);
    	List<String> filledWithPropertyIds = fillWith.getFilledWithPropertyIds();    
		
    	int id = 1;
    	for(Integer itemId : itemIds){
    		//Check if filled with was used for this column, if so, do not change values to serialized numbers
    		if(!filledWithPropertyIds.contains(ListDataTablePropertyID.ENTRY_ID.getName()))
    			germplasmsTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(id);
    		if(!filledWithPropertyIds.contains(ListDataTablePropertyID.ENTRY_CODE.getName()))
    			germplasmsTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(id);

    		//Item tableItem = germplasmsTable.getItem(itemId);
    		//tableItem.getItemProperty(ENTRY_ID).setValue(id);
    		//if(tableItem.getItemProperty(ENTRY_CODE).getValue() == null || tableItem.getItemProperty(ENTRY_CODE).getValue().equals("")){
    		//	tableItem.getItemProperty(ENTRY_CODE).setValue(id);
    		//}
    		id++;
    	}
    }
	
	/**
	 * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
	 */
	private void assignSerializedEntryNumber(){
		List<Integer> itemIds = getItemIds(germplasmsTable);
    	    	
    	int id = 1;
    	for(Integer itemId : itemIds){
    		germplasmsTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(id);
    		id++;
    	}
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
	 * Iterates through the whole table, gets selected item GID's, make sure it's sorted as seen on the UI
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> getSelectedGids(Table table, String GIDItemId){
		List<Integer> itemIds = new ArrayList<Integer>();
		List<Integer> selectedItemIds = new ArrayList<Integer>();
		List<Integer> trueOrderedSelectedGIDs = new ArrayList<Integer>();
		
    	selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
    	itemIds = getItemIds(table);
    
    	//System.out.println("Selected Item IDs: "+selectedItemIds);
    	//System.out.println("Item IDs: "+itemIds);
    	
    	for(Integer itemId: itemIds){
    		if(selectedItemIds.contains(itemId)){
    			Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDItemId).getValue()).getCaption().toString());
    			trueOrderedSelectedGIDs.add(gid);
    		}
    	}
    	
    	return trueOrderedSelectedGIDs;
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
	
	public void setupTableHeadersContextMenu(){
		fillWith = new FillWith(this, messageSource, germplasmsTable, ListDataTablePropertyID.GID.getName());
	}
	
	
	public boolean isCurrentListSave(){
		boolean isSaved = false;
		
		if(currentlySavedGermplasmList != null){
			isSaved = true;
		}
		
		return isSaved;
	}
	
	public void enableMenuOptionsAfterSave(){
		menuExportList.setEnabled(true);
		menuExportForGenotypingOrder.setEnabled(true);
		menuCopyToList.setEnabled(true);
	}
	
    public void exportListAction() throws InternationalizableException {
    	
        if(isCurrentListSave()){
        	String tempFileName = System.getProperty( "user.home" ) + "/temp.xls";
            
            germplasmListId = currentlySavedGermplasmList.getId();
            
            GermplasmListExporter listExporter = new GermplasmListExporter(germplasmListId);
            String listName = currentlySavedGermplasmList.getName();
            
            try {
                listExporter.exportGermplasmListExcel(tempFileName);
                FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
                fileDownloadResource.setFilename(listName.replace(" ", "_") + ".xls");
    
                this.getWindow().open(fileDownloadResource);
    
                //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
                    //File tempFile = new File(tempFileName);
                    //tempFile.delete();
            } catch (GermplasmListExporterException e) {
                    LOG.error("Error with exporting list.", e);
                MessageNotifier.showError( this.getWindow()
                            , "Error with exporting list."    
                            , e.getMessage() + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                            , Notification.POSITION_CENTERED);
            }
        }
        
    }//end of exportListAction

    public void exportListForGenotypingOrderAction() throws InternationalizableException {
    	if(isCurrentListSave()){
            String tempFileName = System.getProperty( "user.home" ) + "/tempListForGenotyping.xls";
            
            germplasmListId = currentlySavedGermplasmList.getId();
            
			GermplasmListExporter listExporter = new GermplasmListExporter(germplasmListId);
			String listName = currentlySavedGermplasmList.getName();
			
			try {
			        listExporter.exportListForKBioScienceGenotypingOrder(tempFileName, 96);
			        FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
			        fileDownloadResource.setFilename(listName.replace(" ", "_") + "ForGenotyping.xls");
			
			    this.getWindow().open(fileDownloadResource);
			
			    //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
			    //File tempFile = new File(tempFileName);
			    //tempFile.delete();
			} catch (GermplasmListExporterException e) {
			        MessageNotifier.showError(this.getWindow() 
			                    , "Error with exporting list."
			                    , e.getMessage(), Notification.POSITION_CENTERED);
			}
        }
    }// end of exportListForGenotypingOrderAction
    
    public void copyToNewListAction(){
    	
    	if(isCurrentListSave()){
    		
    		String listName = this.listNameText.getValue().toString();
    		Collection<?> listEntries = (Collection<?>) germplasmsTable.getValue();
    		
            if (listEntries == null || listEntries.isEmpty()){
                MessageNotifier.showError(this.getWindow(), messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), "", Notification.POSITION_CENTERED);
            } 
            else {
                listManagerCopyToNewListDialog = new Window(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
                listManagerCopyToNewListDialog.setModal(true);
                listManagerCopyToNewListDialog.setWidth("700px");
                listManagerCopyToNewListDialog.setHeight("350px");
                
                try {
                	
	                listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(((ListManagerMain) source).getWindow(), listManagerCopyToNewListDialog, listName, germplasmsTable,getCurrentUserLocalId(), true));
	                ((ListManagerMain) source).getWindow().addWindow(listManagerCopyToNewListDialog);
                    
                } catch (MiddlewareQueryException e) {
                    LOG.error("Error copying list entries.", e);
                    e.printStackTrace();
                }
            }
            
    	}
    }// end of copyToNewListAction
    
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

    public GermplasmList getCurrentlySavedGermplasmList(){
    	return this.currentlySavedGermplasmList;
    }
    
    public void setCurrentlySavedGermplasmList(GermplasmList list){
    	this.currentlySavedGermplasmList = list;
    }
    
    public GermplasmList getCurrentlySetGermplasmListInfo(){
    	GermplasmList toreturn = new GermplasmList();
    	Object name = this.listNameText.getValue();
    	if(name != null){
    		toreturn.setName(name.toString().trim());
    	} else{
    		toreturn.setName(null);
    	}
    	Object description = this.descriptionText.getValue();
    	if(description != null){
    		toreturn.setDescription(description.toString().trim());
    	} else{
    		toreturn.setDescription(null);
    	}
    	
    	SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    	Object dateValue = this.listDateField.getValue();
    	if(dateValue != null){
    		String sDate = formatter.format(dateValue);
    		Long dataLongValue = Long.parseLong(sDate.replace("-", ""));
    		toreturn.setDate(dataLongValue);
    	} else{
    		toreturn.setDate(null);
    	}
        
        toreturn.setType(this.listTypeComboBox.getValue().toString());
        toreturn.setNotes(this.notesTextArea.getValue().toString());
    	return toreturn;
    }
    
    public List<GermplasmListData> getListEntriesFromTable(){
    	List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
    	
    	assignSerializedEntryNumber();
    	
    	for(Object id : this.germplasmsTable.getItemIds()){
    		Integer entryId = (Integer) id;
    		Item item = this.germplasmsTable.getItem(entryId);
    		
    		GermplasmListData listEntry = new GermplasmListData();
    		listEntry.setId(entryId);
    		
    		Object designation = item.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue();
    		if(designation != null){
    			listEntry.setDesignation(designation.toString());
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
    
    
    public Integer getNextListEntryId(){
    	int maxId = 0;
    	for(Object id : this.germplasmsTable.getItemIds()){
    		Integer itemId = (Integer) id;
    		if(itemId<0){
    			itemId*=-1;
    		}
    		if(itemId>maxId)
    			maxId=itemId;
    	}
    	maxId++;
    	return Integer.valueOf(maxId);
    }
    
    @SuppressWarnings("unchecked")
	private void deleteSelectedEntries(){
		List<Integer> selectedItemIds = new ArrayList<Integer>();
		selectedItemIds.addAll((Collection<? extends Integer>) germplasmsTable.getValue());
		for(Integer selectedItemId:selectedItemIds){
			germplasmsTable.removeItem(selectedItemId);
		}
		assignSerializedEntryNumber();
    }

    public void setupSaveButtonClickListener(){
    	if(saveButton!=null){
    		if(saveListButtonClickListener!=null)
    			saveButton.removeListener(saveListButtonClickListener);
    		saveListButtonClickListener = new SaveListButtonClickListener(this, germplasmListManager, germplasmsTable, messageSource, workbenchDataManager);
    		saveButton.addListener(saveListButtonClickListener);
    	}
    }
    
    private void updateAddedColumnValues(){
    	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.LOCATIONS))
    		addColumnContextMenu.setLocationColumnValues();
    	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_ID))
    		addColumnContextMenu.setPreferredIdColumnValues();
    	if(addColumnContextMenu.propertyExists(AddColumnContextMenu.PREFERRED_NAME))
    		addColumnContextMenu.setPreferredNameColumnValues();
    }
    

    
    public void setupAddColumnContextMenu(){
    	addColumnContextMenu = new AddColumnContextMenu(this, addColumnButton, germplasmsTable, ListDataTablePropertyID.GID.getName());
    }
    
    public AddColumnContextMenu getAddColumnContextMenu(){
    	return addColumnContextMenu;
    }
    
    public ComboBox getListTypeComboBox(){
    	return listTypeComboBox;
    }
    
    public DateField getListDateField(){
    	return listDateField;
    }
    
    public TextField getListNameText(){
    	return listNameText;
    }
    
    public TextField getDescriptionText(){
    	return descriptionText;
    }
    
    public TextArea getNotesTextArea(){
    	return notesTextArea;
    }
    
	public Table getGermplasmsTable(){
		return germplasmsTable;
	}

	public Object getSource(){
		return source;
	}
	
}

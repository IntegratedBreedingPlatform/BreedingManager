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
import org.generationcp.breeding.manager.listmanager.listeners.SaveListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BuildNewListComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent {

	@SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BuildNewListComponent.class);
	
	private static final long serialVersionUID = 5314653969843976836L;
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String GID = "GID";
	public static final String ENTRY_ID = "ENTRY ID";
	public static final String ENTRY_CODE = "ENTRY CODE";
	public static final String SEED_SOURCE = "SEED SOURCE";
	public static final String DESIGNATION = "DESIGNATION";
	public static final String PARENTAGE = "PARENTAGE";
	public static final String STATUS = "STATUS";
	private static final String COL8 = " ";
	private static final String COL9 = "  ";
	
	private Object source;
	
    private String DEFAULT_LIST_TYPE = "LST";
	
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
	private Button toolsButton;
	
	private static final ThemeResource ICON_TOOLS = new ThemeResource("images/tools.png");
	public static String TOOLS_BUTTON_ID = "Tools";
	private static String TOOLS_TOOLTIP = "Tools";
	
	private String MENU_EXPORT_LIST="Export List"; 
	private String MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER="Export List for Genotyping Order"; 
	private String MENU_COPY_TO_NEW_LIST="Copy to New List";
	
	private ContextMenu menu;
	private ContextMenuItem menuSelectAll;
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuExportForGenotypingOrder;
	private ContextMenuItem menuCopyToList;
	
    static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL};

	private GermplasmList currentlySavedGermplasmList;
	private Window listManagerCopyToNewListDialog;
	private int germplasmListId;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	public BuildNewListComponent(ListManagerMain source){
		this.source = source;
		this.currentlySavedGermplasmList = null;
	}
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
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
        addComponent(listNameText, "top:35px;left:46px");

        listTypeLabel = new Label();
        listTypeLabel.setCaption(messageSource.getMessage(Message.TYPE_LABEL)+":*");
        listTypeLabel.addStyleName("bold");
        addComponent(listTypeLabel, "top:55px;left:270px");
        
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
        addComponent(listTypeComboBox, "top:35px;left:310px");

        listDateLabel = new Label();
        listDateLabel.setCaption(messageSource.getMessage(Message.DATE_LABEL)+":*");
        listDateLabel.addStyleName("bold");
        addComponent(listDateLabel, "top:55px;left:540px");
      
        listDateField = new DateField();
        listDateField.setDateFormat(DATE_FORMAT);
        listDateField.setResolution(DateField.RESOLUTION_DAY);
        listDateField.setValue(new Date());
        addComponent(listDateField, "top:35px;left:580px");
        
        descriptionLabel = new Label();
        descriptionLabel.setCaption(messageSource.getMessage(Message.DESCRIPTION_LABEL)+"*");
        descriptionLabel.addStyleName("bold");
        addComponent(descriptionLabel, "top:90px;left:0px");
        
        descriptionText = new TextField();
        descriptionText.setWidth("595px");
        addComponent(descriptionText, "top:70px;left:80px");
		
        notesLabel = new Label();
        notesLabel.setCaption(messageSource.getMessage(Message.NOTES)+":");
        notesLabel.addStyleName("bold");
        addComponent(notesLabel, "top:55px; left: 720px;");
        notesLabel.setVisible(false);
		
        notesTextArea = new TextArea();
        notesTextArea.setWidth("400px");
        notesTextArea.setHeight("65px");
        notesTextArea.addStyleName("noResizeTextArea");
        addComponent(notesTextArea, "top:35px; left: 770px;");
        notesTextArea.setVisible(false);

		germplasmsTable = new Table();
		germplasmsTable.addContainerProperty(GID, Button.class, null);
		germplasmsTable.addContainerProperty(ENTRY_ID, Integer.class, null);
		germplasmsTable.addContainerProperty(ENTRY_CODE, String.class, null);
		germplasmsTable.addContainerProperty(SEED_SOURCE, String.class, null);
		germplasmsTable.addContainerProperty(DESIGNATION, String.class, null);
		germplasmsTable.addContainerProperty(PARENTAGE, String.class, null);
		germplasmsTable.addContainerProperty(STATUS, String.class, null);
		//germplasmsTable.addContainerProperty(COL8, String.class, null);
		//germplasmsTable.addContainerProperty(COL9, String.class, null);
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
				}
			}
        });
		
		menu = new ContextMenu();
		menuSelectAll = menu.addItem(messageSource.getMessage(Message.SELECT_ALL));
		menuExportList = menu.addItem(MENU_EXPORT_LIST);
		menuExportForGenotypingOrder = menu.addItem(MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER);
		menuCopyToList = menu.addItem(MENU_COPY_TO_NEW_LIST);
		
		//initially disabled when the current list building is not yet save
		menuExportList.setEnabled(false);
		menuExportForGenotypingOrder.setEnabled(false);
		menuCopyToList.setEnabled(false);
		
        toolsButton = new Button("Tools");
        toolsButton.setIcon(ICON_TOOLS);
        toolsButton.setStyleName(BaseTheme.BUTTON_LINK);
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
   	 	addComponent(toolsButton, "top:0; right:0;");		
		
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void contextItemClick(ClickEvent event) {
			    ContextMenuItem clickedItem = event.getClickedItem();
			    if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
			      	germplasmsTable.setValue(germplasmsTable.getItemIds());
			    }else if(clickedItem.getName().equals(MENU_EXPORT_LIST)){
			    	exportListAction();
			    }else if(clickedItem.getName().equals(MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER)){
			    	exportListForGenotypingOrderAction();
			    }else if(clickedItem.getName().equals(MENU_COPY_TO_NEW_LIST)){
			    	copyToNewListAction();
			    }				
			}
			
        });
		
		addComponent(germplasmsTable, "top:115px; left:0px;");
		
		VerticalLayout buttonRow = new VerticalLayout();
		buttonRow.setWidth("100%");
		buttonRow.setHeight("150px");
		
		saveButton = new Button();
		saveButton.setCaption(messageSource.getMessage(Message.SAVE_LIST));
		saveButton.setStyleName(BaseTheme.BUTTON_LINK);
		saveButton.addStyleName("gcp_button");
		saveButton.addListener(new SaveListButtonClickListener(this, germplasmListManager, germplasmsTable, messageSource, workbenchDataManager));
		
		buttonRow.addComponent(saveButton);
		buttonRow.setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
		
		addComponent(buttonRow, "top:365px; left:0px;");
		
		setWidth("100%");
		setHeight("600px");
		
		setupDragSources();
		setupDropHandlers();
		setupTableHeadersContextMenu();
	}

		
	/**
	 * Setup drag sources, this will tell Vaadin which tables can the user drag rows from
	 */
	private void setupDragSources(){
		if(source instanceof ListManagerMain){
			//Browse Lists tab
			
			/**
			 * TODO: enable draggable tables here
			 */
			
			//Search Lists and Germplasms tab
			Table matchingGermplasmsTable = ((ListManagerMain) source).getListManagerSearchListsComponent().getSearchResultsComponent().getMatchingGermplasmsTable();
			Table matchingListsTable = ((ListManagerMain) source).getListManagerSearchListsComponent().getSearchResultsComponent().getMatchingListsTable();
			
			matchingGermplasmsTable.setDragMode(TableDragMode.ROW); 
			matchingListsTable.setDragMode(TableDragMode.ROW); 
		}
	}
	
	
	/**
	 * Setup drop handlers, this will dictate how Vaadin will handle drops (mouse releases) on the germplasm table
	 */
	private void setupDropHandlers(){
		germplasmsTable.setDropHandler(new DropHandler() {
			private static final long serialVersionUID = -6676297159926786216L;

			public void drop(DragAndDropEvent dropEvent) {
				TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
				
				Table sourceTable = (Table) transferable.getSourceComponent();
			    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
			
			    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
                Object droppedOverItemId = dropData.getItemIdOver();
			    
                //TODO: add handler for source tables from "Browse Lists" tab
                
                
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
                }
			    
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
		});
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

            newItem.getItemProperty(GID).setValue(gidButton);
			//newItem.getItemProperty(SEED_SOURCE).setValue(data.getSeedSource());
            newItem.getItemProperty(SEED_SOURCE).setValue("From List Manager");
			newItem.getItemProperty(DESIGNATION).setValue(data.getDesignation());
			newItem.getItemProperty(PARENTAGE).setValue(crossExpansion);
			newItem.getItemProperty(STATUS).setValue("0");
			
        }		
        assignSerializedEntryCode();
	}
	
	/**
	 * Add a germplasm to a table, adds it after/before a certain germplasm given the droppedOn item id
	 * @param gid
	 * @param droppedOn
	 */
	private void addGermplasmToGermplasmTable(Integer gid, Object droppedOnItemIdObject){

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
            
            newItem.getItemProperty(GID).setValue(gidButton);
			//newItem.getItemProperty(SEED_SOURCE).setValue(location.getLname());
			newItem.getItemProperty(SEED_SOURCE).setValue("From List Manager");
			newItem.getItemProperty(DESIGNATION).setValue(preferredName);
			newItem.getItemProperty(PARENTAGE).setValue(crossExpansion);
			newItem.getItemProperty(STATUS).setValue("0");
			
			assignSerializedEntryCode();
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

	}	
		
	
	/**
	 * Iterates through the whole table, and sets the entry code from 1 to n based on the row position
	 */
	private void assignSerializedEntryCode(){
		List<Integer> itemIds = getItemIds(germplasmsTable);
    	    	
    	int id = 1;
    	for(Integer itemId : itemIds){
    		germplasmsTable.getItem(itemId).getItemProperty(ENTRY_ID).setValue(id);
    		germplasmsTable.getItem(itemId).getItemProperty(ENTRY_CODE).setValue(id);
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
    
    	System.out.println("Selected Item IDs: "+selectedItemIds);
    	System.out.println("Item IDs: "+itemIds);
    	
    	int i=0;
    	for(Integer itemId: itemIds){
    		if(selectedItemIds.contains(itemId)){
    			trueOrderedSelectedItemIds.add(itemId);
    			i++;
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
	
	private void setupTableHeadersContextMenu(){
		List<String> propertyIdsEnabled = new ArrayList<String>();
        propertyIdsEnabled.add(ENTRY_CODE);
        propertyIdsEnabled.add(SEED_SOURCE);
        
      	@SuppressWarnings("unused")
      	FillWith fillWith = new FillWith(this, messageSource, germplasmsTable, GID, propertyIdsEnabled);
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
                fileDownloadResource.setFilename(listName + ".xls");
    
                this.getWindow().open(fileDownloadResource);
    
                //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
                    //File tempFile = new File(tempFileName);
                    //tempFile.delete();
            } catch (GermplasmListExporterException e) {
                    LOG.error("Error with exporting list.", e);
                MessageNotifier.showError( this.getWindow()
                            , "Error with exporting list."    
                            , e.getMessage() + " .Please report to Workbench developers.", Notification.POSITION_CENTERED);
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
			        fileDownloadResource.setFilename(listName + "ForGenotyping.xls");
			
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
                listManagerCopyToNewListDialog.setWidth(700);
                listManagerCopyToNewListDialog.setHeight(350);
                
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
    	return toreturn;
    }
    
    public List<GermplasmListData> getListEntriesFromTable(){
    	List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
    	
    	for(Object id : this.germplasmsTable.getItemIds()){
    		Integer entryId = (Integer) id;
    		Item item = this.germplasmsTable.getItem(entryId);
    		
    		GermplasmListData listEntry = new GermplasmListData();
    		listEntry.setId(entryId);
    		Object designation = item.getItemProperty(DESIGNATION).getValue();
    		listEntry.setDesignation(designation.toString());
    		Object entryCode = item.getItemProperty(ENTRY_CODE).getValue();
    		listEntry.setEntryCode(entryCode.toString());
    		
    		Button gidButton = (Button) item.getItemProperty(GID).getValue();
    		listEntry.setGid(Integer.parseInt(gidButton.getCaption()));
    		
    		Object groupName = item.getItemProperty(PARENTAGE).getValue();
    		listEntry.setGroupName(groupName.toString());
    		listEntry.setEntryId((Integer) item.getItemProperty(ENTRY_ID).getValue());
    		Object seedSource = item.getItemProperty(SEED_SOURCE).getValue();
    		listEntry.setSeedSource(seedSource.toString());
    		
    		toreturn.add(listEntry);
    	}
    	return toreturn;
    }
    
    public int getNextListEntryId(){
    	Boolean isNegative = false;
        int maxId = 0;
    	for(Object id : this.germplasmsTable.getItemIds()){
    		Integer itemId = (Integer) id;
    		if(itemId<0){
    			isNegative = true;
    			itemId*=-1;
    		}
    		if(itemId>maxId)
    			maxId=itemId;
    	}
    	maxId++;
    	if(isNegative)
    		maxId*=-1;
    	return maxId;
    }
}

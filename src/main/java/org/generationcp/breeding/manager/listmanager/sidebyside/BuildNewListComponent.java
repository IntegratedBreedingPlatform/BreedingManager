package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerCopyToNewListDialog;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.ResetListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.listeners.SaveListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


@Configurable
public class BuildNewListComponent extends VerticalLayout implements InitializingBean, BreedingManagerLayout, SaveListAsDialogSource {

    private static final Logger LOG = LoggerFactory.getLogger(BuildNewListComponent.class);
    
    private static final long serialVersionUID = -7736422783255724272L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
        
    
    public static final String GERMPLASMS_TABLE_DATA = "Germplasms Table Data";
    static final Action ACTION_SELECT_ALL = new Action("Select All");
    static final Action ACTION_DELETE_SELECTED_ENTRIES = new Action("Delete Selected Entries");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE_SELECTED_ENTRIES };
    
    public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    //Components
    private Label buildNewListTitle;
    private Label buildNewListDesc;
    private Label dragInstructionLabel;
    private BreedingManagerListDetailsComponent breedingManagerListDetailsComponent;
    private TableWithSelectAllLayout tableWithSelectAllLayout;
    private Button editHeaderButton;
    private Button toolsButton;
    private Button saveButton;
    private Button resetButton;
    
    //Layout Component
    Panel listDataTablePanel;
    
    private BuildNewListDropHandler dropHandler;
    
    //Tools Button Context Menu
    private ContextMenu menu;
    private ContextMenuItem menuExportList;
    private ContextMenuItem menuExportForGenotypingOrder;
    private ContextMenuItem menuCopyToList;
    private ContextMenuItem menuAddColumn;
    
    private static final String USER_HOME = "user.home";
    private Window listManagerCopyToNewListDialog;
    
    public static String TOOLS_BUTTON_ID = "Tools";
    
    //For Saving
    private ListManagerMain source;
    private GermplasmList currentlySavedGermplasmList;
    private GermplasmList currentlySetGermplasmInfo;
    private boolean changed = false;
    
    private AddColumnContextMenu addColumnContextMenu;
    
    //Listener
    SaveListButtonClickListener saveListButtonListener;
    
    public BuildNewListComponent() {
        super();
    }
    
    public BuildNewListComponent(ListManagerMain source) {
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
    	buildNewListTitle = new Label(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
    	buildNewListTitle.setWidth("200px");
        buildNewListTitle.addStyleName(Bootstrap.Typography.H3.styleName());
        
        buildNewListDesc = new Label();
        buildNewListDesc.setValue(messageSource.getMessage(Message.CLICK_AND_DRAG_ON_PANEL_EDGES_TO_RESIZE));
        buildNewListDesc.setWidth("300px");
        
        dragInstructionLabel = new Label(messageSource.getMessage(Message.BUILD_LIST_DRAG_INSTRUCTIONS));
        
        editHeaderButton = new Button(messageSource.getMessage(Message.EDIT_HEADER));
        editHeaderButton.setImmediate(true);
        editHeaderButton.setStyleName(Reindeer.BUTTON_LINK);
        
        breedingManagerListDetailsComponent = new BreedingManagerListDetailsComponent();
        
        menu = new ContextMenu();
        menu.setWidth("300px");
        menu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        menu.addItem(messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
        menuExportList = menu.addItem(messageSource.getMessage(Message.EXPORT_LIST));
        menuExportForGenotypingOrder = menu.addItem(messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING));
        menuCopyToList = menu.addItem(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
        
        resetMenuOptions();
        
        toolsButton = new Button(messageSource.getMessage(Message.TOOLS));
        toolsButton.setIcon(AppConstants.Icons.ICON_TOOLS);
        toolsButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
             
        tableWithSelectAllLayout = new TableWithSelectAllLayout(ListDataTablePropertyID.TAG.getName());
        createGermplasmTable(tableWithSelectAllLayout.getTable());
        
        addColumnContextMenu = new AddColumnContextMenu(this, menu, 
        		tableWithSelectAllLayout.getTable(), ListDataTablePropertyID.GID.getName(),true);
        
        dropHandler = new BuildNewListDropHandler(germplasmDataManager, germplasmListManager, tableWithSelectAllLayout.getTable());
        
        saveButton = new Button();
        saveButton.setCaption(messageSource.getMessage(Message.SAVE_LABEL));
        saveButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        
        resetButton = new Button();
        resetButton.setCaption(messageSource.getMessage(Message.RESET_LIST));
        resetButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());
	}
	
    public void resetMenuOptions(){
        //initially disabled when the current list building is not yet save or being reset
        menuExportList.setEnabled(false);
        menuExportForGenotypingOrder.setEnabled(false);
        menuCopyToList.setEnabled(false);
    }

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
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
                }else if(clickedItem.getName().equals(messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING))){
                      exportListForGenotypingOrderAction();
                }else if(clickedItem.getName().equals(messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL))){
                      copyToNewListAction();
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
		
		editHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -6306973449416812850L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openSaveListAsDialog();
			}
		});
		
		saveListButtonListener = new SaveListButtonClickListener(this, germplasmListManager, tableWithSelectAllLayout.getTable(), messageSource, workbenchDataManager); 
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
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);
		
		HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_BUILD_NEW_LIST,buildNewListTitle);
        this.addComponent(headingLayout);
        
        HorizontalLayout instructionLayout = new HorizontalLayout();
        instructionLayout.setSpacing(true);
        instructionLayout.setWidth("400px");
        instructionLayout.addComponent(buildNewListDesc);
        instructionLayout.addComponent(saveButton);
        instructionLayout.setComponentAlignment(buildNewListDesc, Alignment.MIDDLE_LEFT);
        instructionLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
        
        this.addComponent(instructionLayout);
        
        listDataTablePanel = new Panel();
        listDataTablePanel.addStyleName(AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
        VerticalLayout listDataTableLayout = new VerticalLayout();
        listDataTableLayout.setMargin(true);
        listDataTableLayout.setSpacing(true);
        listDataTableLayout.setWidth("100%");
        listDataTableLayout.addComponent(dragInstructionLabel);
        
        HorizontalLayout toolsAndEditHeaderLayout = new HorizontalLayout();
        toolsAndEditHeaderLayout.setSpacing(true);
        toolsAndEditHeaderLayout.setWidth("365px");
        toolsAndEditHeaderLayout.addComponent(editHeaderButton);
        toolsAndEditHeaderLayout.setComponentAlignment(editHeaderButton, Alignment.MIDDLE_LEFT);
        toolsAndEditHeaderLayout.addComponent(toolsButton);
        toolsAndEditHeaderLayout.setComponentAlignment(toolsButton, Alignment.MIDDLE_RIGHT);
        
        listDataTableLayout.addComponent(toolsAndEditHeaderLayout);
        
        listDataTableLayout.addComponent(tableWithSelectAllLayout);
        
        listDataTablePanel.setLayout(listDataTableLayout);
        this.addComponent(listDataTablePanel);
        
        this.addComponent(resetButton);
        
        this.addComponent(menu);
	}
    
	public void addBasicTableColumns(Table table){
    	table.setData(GERMPLASMS_TABLE_DATA);
    	table.addContainerProperty(ListDataTablePropertyID.TAG.getName(), CheckBox.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
    	
        messageSource.setColumnHeader(table, ListDataTablePropertyID.TAG.getName(), Message.CHECK_ICON);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.ENTRY_ID.getName(), Message.HASHTAG);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.PARENTAGE.getName(), Message.LISTDATA_GROUPNAME_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
	}
	
    public void createGermplasmTable(final Table table){
        
    	addBasicTableColumns(table);
        
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setWidth("365px");
        table.setHeight("280px");
        
        table.addActionHandler(new Action.Handler() {

            private static final long serialVersionUID = 1884343225476178686L;

            public Action[] getActions(Object target, Object sender) {
                return GERMPLASMS_TABLE_CONTEXT_MENU;
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if(ACTION_SELECT_ALL == action) {
                	table.setValue(table.getItemIds());
                } else if(ACTION_DELETE_SELECTED_ENTRIES == action) {
                    deleteSelectedEntries();
                }
            }
        });
        
        initializeHandlers();
    }
    
    private void initializeHandlers() {
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
        }else{
            MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DELETING_LIST_ENTRIES)
                    , messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
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
        menuExportForGenotypingOrder.setEnabled(true);
        menuCopyToList.setEnabled(true);
    }
    
	public void editList(GermplasmList germplasmList) {
		resetList(); //reset list before placing new one
		
		buildNewListTitle.setValue(messageSource.getMessage(Message.EDIT_LIST));
		
		currentlySavedGermplasmList = germplasmList;
		currentlySetGermplasmInfo = germplasmList;
		
		dropHandler.addGermplasmList(germplasmList.getId(), true);
        
        //reset the change status to false after loading the germplasm list details and list data in the screen
        setChanged(false);
        dropHandler.setChanged(false);
    }
	
	public void resetList(){
		
		//list details fields
		breedingManagerListDetailsComponent.resetFields();
		
		//list data table
		resetGermplasmTable();
		
		//disabled the menu options when the build new list table has no rows
		resetMenuOptions();
		
		//Clear flag, this is used for saving logic (to save new list or update)
		setCurrentlySavedGermplasmList(null);
		currentlySetGermplasmInfo = null;

		//Rename the Build New List Header
		buildNewListTitle.setValue(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
		
		//Reset the marker for changes in Build New List
		setChanged(false);
		
		//List Data Table
		dropHandler = new BuildNewListDropHandler(germplasmDataManager, germplasmListManager, tableWithSelectAllLayout.getTable());
		initializeHandlers();
		
		//Reset Save Listener
		saveButton.removeListener(saveListButtonListener);
		saveListButtonListener = new SaveListButtonClickListener(this, germplasmListManager, tableWithSelectAllLayout.getTable(), messageSource, workbenchDataManager); 
		saveButton.addListener(saveListButtonListener);
		
	}
	
	public void resetGermplasmTable(){		
		tableWithSelectAllLayout.getTable().removeAllItems();
		for(Object col: tableWithSelectAllLayout.getTable().getContainerPropertyIds().toArray())  {
			tableWithSelectAllLayout.getTable().removeContainerProperty(col);
		}
		tableWithSelectAllLayout.getTable().setWidth("400px");
		addBasicTableColumns(tableWithSelectAllLayout.getTable());
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
    	changed = true;
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
                listManagerCopyToNewListDialog.setWidth("700px");
                listManagerCopyToNewListDialog.setHeight("350px");
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
    
    public void setCurrentlySavedGermplasmList(GermplasmList list){
        this.currentlySavedGermplasmList = list;
    }
	
    public ListManagerMain getSource(){
    	return source;
    }
    
    public AddColumnContextMenu getAddColumnContextMenu(){
    	return addColumnContextMenu;
    }

	public boolean isChanged() {
		
		if(this.breedingManagerListDetailsComponent.isChanged()){
			changed = true;
		}
		
		if(dropHandler.isChanged()){
			changed = true;
		}
		
		//TO DO mark the changes in germplasmListDataTable during fill with and add column functions
		
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
		this.breedingManagerListDetailsComponent.setChanged(changed);
		
		//TO DO mark the changes in germplasmListDataTable during fill with functions
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
	}
    
}

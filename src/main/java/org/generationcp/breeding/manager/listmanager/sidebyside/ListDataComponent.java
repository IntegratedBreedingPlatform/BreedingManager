package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListDataComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(ListDataComponent.class);
	
	private static final long serialVersionUID = 1L;

	private ListManagerMain source;
	private Integer germplasmListId;
	private Integer germplasmListStatus;
	
	private Button viewHeaderButton;
	private Label totalListEntriesLabel;
	private Button toolsButton;
	private Table listDataTable;
	private TableWithSelectAllLayout listDataTableWithSelectAll;
	private Label noListDataLabel;
	
	//Menu for tools button
	private ContextMenu menu; 
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuExportForGenotypingOrder;
	private ContextMenuItem menuCopyToList;
	private ContextMenuItem menuAddEntry;
	private ContextMenuItem menuSaveChanges;
	private ContextMenuItem menuDeleteEntries;
	private ContextMenuItem menuEditList;
	
	//Toos Menu Options
	private String MENU_SELECT_ALL="Select All"; 
    private String MENU_EXPORT_LIST="Export List"; 
    private String MENU_EXPORT_LIST_FOR_GENOTYPING_ORDER="Export List for Genotyping Order"; 
    private String MENU_COPY_TO_NEW_LIST="Copy List Entries"; 
    private String MENU_ADD_ENTRY="Add Entry"; 
    private String MENU_SAVE_CHANGES="Save Changes"; 
    private String MENU_DELETE_SELECTED_ENTRIES="Delete Selected Entries";
    private String MENU_EDIT_LIST="Edit List";
	
    //Tooltips
  	public static String TOOLS_BUTTON_ID = "Tools";
  	private static String TOOLS_TOOLTIP = "Tools";
  	public static String LIST_DATA_COMPONENT_TABLE_DATA = "List Data Component Table";
  	private String CHECKBOX_COLUMN_ID="Checkbox Column ID";
    
  	private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL
  	private long listEntriesCount;
  	
	//Theme Resource
	private static final ThemeResource ICON_TOOLS = new ThemeResource("images/tools.png");
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	public ListDataComponent() {
		super();
	}

	public ListDataComponent(ListManagerMain source, Integer listId) {
		super();
		this.source = source;
		this.germplasmListId = listId;
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
		viewHeaderButton = new Button(messageSource.getMessage(Message.VIEW_HEADER));
		viewHeaderButton.addStyleName(Reindeer.BUTTON_LINK);
		
		toolsButton = new Button(messageSource.getMessage(Message.TOOLS));
		toolsButton.setData(TOOLS_BUTTON_ID);
		toolsButton.setIcon(ICON_TOOLS);
		toolsButton.setWidth("100px");
		toolsButton.setDescription(TOOLS_TOOLTIP);
		toolsButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		
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
		
		try{
			listEntriesCount = germplasmListManager.countGermplasmListDataByListId(germplasmListId);
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with retrieving count of list entries for list: " + germplasmListId, ex);
			listEntriesCount = 0;
		}
		
		if(listEntriesCount == 0) {
			noListDataLabel = new Label(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
            noListDataLabel.setWidth("250px");
		} else {
        	totalListEntriesLabel = new Label("<b>" + messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ":</b> " 
        		 + "  " + listEntriesCount, Label.CONTENT_XHTML);
        	totalListEntriesLabel.setWidth("150px");
        }
		
		initializeListDataTable(); //listDataTable
	}
	
	private void initializeListDataTable(){
		listDataTableWithSelectAll = new TableWithSelectAllLayout(Long.valueOf(listEntriesCount).intValue(), 15, CHECKBOX_COLUMN_ID);
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
		
		messageSource.setColumnHeader(listDataTable, CHECKBOX_COLUMN_ID, Message.TAG);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.ENTRY_ID.getName(), Message.LISTDATA_ENTRY_ID_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
		messageSource.setColumnHeader(listDataTable, ListDataTablePropertyID.GROUP_NAME.getName(), Message.LISTDATA_GROUPNAME_HEADER);
	}

	@Override
	public void initializeValues() {
		if(listEntriesCount > 0){
			List<GermplasmListData> listEntries = new ArrayList<GermplasmListData>();
			try{
				listEntries.addAll(germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, Long.valueOf(listEntriesCount).intValue()));
			} catch(MiddlewareQueryException ex){
				LOG.error("Error with retrieving list entries for list: " + germplasmListId, ex);
				listEntries = new ArrayList<GermplasmListData>();
			}
			
			for(GermplasmListData entry : listEntries){
				String gid = String.format("%s", entry.getGid().toString());
                Button gidButton = new Button(gid, new GidLinkButtonClickListener(gid,true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                gidButton.setDescription("Click to view Germplasm information");
                
                Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(gid,true));
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
		}
	}

	@Override
	public void addListeners() {
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
	   			 if (germplasmListId < 0 && !fromUrl) {
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

	}//end of addListeners

	@Override
	public void layoutComponents() {
		setSpacing(true);
		setMargin(true);
		
		VerticalLayout headerLayout = new VerticalLayout();
		headerLayout.setSpacing(true);
		headerLayout.addComponent(viewHeaderButton);
		headerLayout.setComponentAlignment(viewHeaderButton, Alignment.MIDDLE_LEFT);
		
		if(listEntriesCount == 0) {
			headerLayout.addComponent(noListDataLabel); 
			headerLayout.setComponentAlignment(noListDataLabel, Alignment.MIDDLE_LEFT);
		} else{
			headerLayout.addComponent(totalListEntriesLabel);
			headerLayout.setComponentAlignment(totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		}
		
		headerLayout.addComponent(toolsButton);
		headerLayout.setComponentAlignment(toolsButton, Alignment.MIDDLE_RIGHT);
		
		addComponent(headerLayout);
		addComponent(listDataTableWithSelectAll);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}

package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.ListDataPropertiesRenderer;
import org.generationcp.commons.exceptions.InternationalizableException;
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
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListDataComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	private static final long serialVersionUID = -3367108805414232721L;

	private static final Logger LOG = LoggerFactory.getLogger(ListDataComponent.class);
	
	private static final int MINIMUM_WIDTH = 10;
	private final HashMap<Object,HashMap<Object,Field>> fields = new HashMap<Object,HashMap<Object,Field>>();

	private ListManagerMain source;
	private ListDetailsComponent parentListDetailsComponent;
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
  	
  	//TODO must i18nalize right click context menu options
  	private static final Action ACTION_SELECT_ALL = new Action("Select All");
  	private static final Action ACTION_DELETE = new Action("Delete selected entries");
  	private static final Action ACTION_EDIT = new Action("Edit Value");
  	private static final Action ACTION_COPY_TO_NEW_LIST= new Action("Copy to new list");
  	private static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE, ACTION_EDIT, ACTION_COPY_TO_NEW_LIST };
  	private static final Action[] ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_EDIT = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE, ACTION_COPY_TO_NEW_LIST };
  	private static final Action[] ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_DELETE = new Action[] { ACTION_SELECT_ALL, ACTION_COPY_TO_NEW_LIST };
    
  	private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL
  	private long listEntriesCount;
  	
	//Theme Resource
	private static final ThemeResource ICON_TOOLS = new ThemeResource("images/tools.png");
	
	private Object selectedColumn = "";
	private Object selectedItemId;
	private String lastCellvalue = "";
	private List<Integer> gidsWithoutChildrenToDelete;
	private Map<Object, String> itemsToDelete;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	@Autowired
    private GermplasmDataManager germplasmDataManager;
	
	@Autowired
    private PedigreeDataManager pedigreeDataManager;
	
	public ListDataComponent(ListManagerMain source, ListDetailsComponent parentListDetailsComponent, GermplasmList germplasmList) {
		super();
		this.source = source;
		this.parentListDetailsComponent = parentListDetailsComponent;
		this.germplasmListId = germplasmList.getId();
		this.germplasmListStatus = germplasmList.getStatus();
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
			
			listDataTable.sort(new Object[]{ListDataTablePropertyID.ENTRY_ID.getName()}, new boolean[]{true});
			
			// render additional columns
	    	ListDataPropertiesRenderer newColumnsRenderer = new ListDataPropertiesRenderer(germplasmListId, listDataTable);
	    	try{
	    		newColumnsRenderer.render();
	    	} catch(MiddlewareQueryException ex){
	    		LOG.error("Error with displaying added columns for entries of list: " + germplasmListId, ex);
	    	}
		}
	}

	@Override
	public void addListeners() {
		if(germplasmListId<0 && germplasmListStatus<100){
	        new FillWith(parentListDetailsComponent, messageSource, listDataTable, ListDataTablePropertyID.GID.getName());
	    }
		
		makeTableEditable();
		
		if(!fromUrl){
			listDataTable.addActionHandler(new Action.Handler() {
				private static final long serialVersionUID = -897257270314381555L;

				public Action[] getActions(Object target, Object sender) {
					if (germplasmListId < 0 &&  germplasmListStatus < 100){
						if(selectedColumn == null){
							return ACTIONS_TABLE_CONTEXT_MENU;
						} else {
							if(selectedColumn.equals(ListDataTablePropertyID.GID.getName()) 
									|| selectedColumn.equals(ListDataTablePropertyID.ENTRY_ID.getName())){
								return ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_EDIT;
							} else{
								return ACTIONS_TABLE_CONTEXT_MENU;
							} 
						}
					}else{
						return ACTIONS_TABLE_CONTEXT_MENU_WITHOUT_DELETE;
					}
				}
  
				public void handleAction(Action action, Object sender, Object target) {
					if (ACTION_DELETE == action) {
						deleteListButtonClickAction();
					}else if(ACTION_SELECT_ALL == action) {
						listDataTable.setValue(listDataTable.getItemIds());
					}else if(ACTION_EDIT == action){
						// Make the entire item editable
						HashMap<Object,Field> itemMap = fields.get(selectedItemId);
		                if(itemMap != null){
			                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
			        			Object column = entry.getKey();
			        			if(column.equals(selectedColumn)){
			        				Field f = entry.getValue();
			        				Object fieldValue = f.getValue();
									lastCellvalue = (fieldValue != null)? fieldValue.toString() : "";
				                	f.setReadOnly(false);
				                	f.focus();
			        			}
			                }
		                }
	                
		                listDataTable.select(selectedItemId);
					}else if(ACTION_COPY_TO_NEW_LIST == action){
						source.showBuildNewListComponent();
						//List<Integer> gids = ListCommonActionsUtil.getSelectedGidsFromListDataTable(listDataTable, ListDataTablePropertyID.GID.getName());
						//TODO call method from BuildNewListDropHandler
					}
	         	}
			});
		}
	
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
		
		parentListDetailsComponent.addComponent(menu);
	}

	@Override
	public void updateLabels() {
		
	}
	
	public void makeTableEditable(){
    	listDataTable.addListener(new ItemClickListener(){
			private static final long serialVersionUID = 1L;
			public void itemClick(ItemClickEvent event) {
				selectedColumn = event.getPropertyId();
				selectedItemId = event.getItemId();
			}
		});
    	
    	listDataTable.setTableFieldFactory(new TableFieldFactory() {
			private static final long serialVersionUID = 1L;

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

					public void focus(FocusEvent event) {
						//TODO review if this is still needed
		                // Make the entire item editable
						/*
		                HashMap<Object,Field> itemMap = fields.get(itemId);
		                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
		                	Object column = entry.getKey();
		        			if(column.equals(selectedColumn)){		        				
		        				Field f = entry.getValue();
			                	//f.setReadOnly(false);
		        			}
		                }
		                */
		                
		                listDataTable.select(itemId);
		            }
		        });
		        
		        tf.addListener(new BlurListener() {
					private static final long serialVersionUID = 1L;

					public void blur(BlurEvent event) {
						HashMap<Object,Field> itemMap = fields.get(itemId);
		                for (Map.Entry<Object, Field> entry : itemMap.entrySet()){
		                	Object column = entry.getKey();
		                	Field f = entry.getValue();
		                	Object fieldValue = f.getValue();
		                	
		                	// mark list as changed if value for the cell was changed
		                	if (column.equals(selectedColumn)) {
		                	    if (!fieldValue.toString().equals(lastCellvalue)) {
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
	
	public void deleteListButtonClickAction()  throws InternationalizableException {
        Collection<?> selectedIdsToDelete = (Collection<?>)listDataTable.getValue();
        
        if(selectedIdsToDelete.size() > 0){
        	if(listDataTable.size() == selectedIdsToDelete.size()){
        		ConfirmDialog.show(this.getWindow(), "Delete All Entries in List Data", "Are you sure you want to delete all list data entries for this list?",
	        			"Yes", "No", new ConfirmDialog.Listener() {
	        		private static final long serialVersionUID = 1L;
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
            MessageNotifier.showError(this.getWindow(), "Error with deleteting entries." 
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
    		itemsToDelete.put(itemId,
    				listDataTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue().toString());
    		listDataTable.removeItem(itemId);
    	}
    	
    	renumberEntryIds();
        listDataTable.requestRepaint();
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
	
	public Integer getGermplasmListId(){
		return germplasmListId;
	}
}

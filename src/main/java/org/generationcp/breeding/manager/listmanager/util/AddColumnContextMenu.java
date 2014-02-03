package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listmanager.BuildNewListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;

@Configurable
public class AddColumnContextMenu implements InternationalizableComponent  {
	private static final Logger LOG = LoggerFactory.getLogger(AddColumnContextMenu.class);

    @Autowired
    private GermplasmDataManager germplasmDataManager;

    private ListManagerTreeMenu listManagerTreeMenu = null;
    private AbsoluteLayout absoluteLayoutSource;
    private String GIDPropertyId;
    private Button addColumnButton;
    private Table targetTable;
    
    private ContextMenu menu;
    private ContextMenuItem menuFillWithPreferredId;
    private ContextMenuItem menuFillWithPreferredName;
    private ContextMenuItem menuFillWithLocations;
    private ContextMenuItem menuFillWithMethodInfo;
    private ContextMenuItem menuFillWithMethodName;
    private ContextMenuItem menuFillWithMethodAbbrev;
    private ContextMenuItem menuFillWithMethodNumber;
    private ContextMenuItem menuFillWithMethodGroup;
    
    public static String FILL_WITH_PREFERRED_ID = "Fill with Preferred ID";
    public static String FILL_WITH_PREFERRED_NAME = "Fill with Preferred Name";
    public static String FILL_WITH_LOCATION = "Fill with Location";
    public static String FILL_WITH_METHOD_INFO = "Fill with Breeding Method Information";
    public static String FILL_WITH_METHOD_NAME = "Fill with Breeding Method Name";
    public static String FILL_WITH_METHOD_ABBREV = "Fill with Breeding Method Abbreviation";
    public static String FILL_WITH_METHOD_NUMBER = "Fill with Breeding Method Number";
    public static String FILL_WITH_METHOD_GROUP = "Fill with Breeding Method Group";
    
    @SuppressWarnings("rawtypes")
	public static Class PREFERRED_ID_TYPE = String.class;
    public static String PREFERRED_ID = "PREFERRED ID";
    
    @SuppressWarnings("rawtypes")
    public static Class PREFERRED_NAME_TYPE = String.class;
    public static String PREFERRED_NAME = "PREFERRED NAME";
    
    @SuppressWarnings("rawtypes")
    public static Class LOCATIONS_TYPE = String.class;
    public static String LOCATIONS = "LOCATIONS";
    
    @SuppressWarnings("rawtypes")
    public static Class METHOD_NAME_TYPE = String.class;
    public static String METHOD_NAME = "METHOD NAME";
    
    @SuppressWarnings("rawtypes")
    public static Class METHOD_ABBREV_TYPE = String.class;
    public static String METHOD_ABBREV = "METHOD ABBREV";
    
    @SuppressWarnings("rawtypes")
    public static Class METHOD_NUMBER_TYPE = String.class;
    public static String METHOD_NUMBER = "METHOD NUMBER";
    
    @SuppressWarnings("rawtypes")
    public static Class METHOD_GROUP_TYPE = String.class;
    public static String METHOD_GROUP = "METHOD GROUP";
    
    private boolean fromBuildNewList;
    private BuildNewListComponent buildNewListComponent;
    
    public static String[] ADDABLE_PROPERTY_IDS = new String[] {PREFERRED_ID
        , PREFERRED_NAME
        , LOCATIONS
        , METHOD_NAME
        , METHOD_ABBREV
        , METHOD_NUMBER
        , METHOD_GROUP}; 
    
    
	/**
	 * Add "Add column" context menu to a table
	 * @param listManagerTreeMenu - tab content from list manager details section.
	 * @param source - context menu will attach to this
	 * @param addColumnButton - util will attach event listener to this
	 * @param targetTable - table where data will be manipulated
	 * @param gid - property of GID (button with GID as caption) on that table
	 */
    public AddColumnContextMenu(ListManagerTreeMenu listManagerTreeMenu, AbsoluteLayout absoluteLayoutSource, 
            Button addColumnButton, Table targetTable, String gid){
        this.listManagerTreeMenu = listManagerTreeMenu;
    	this.GIDPropertyId = gid;
    	this.targetTable = targetTable;
    	this.addColumnButton = addColumnButton;
    	this.absoluteLayoutSource = absoluteLayoutSource;
    	
    	setupContextMenu();
    }
    
    /**
     * Add "Add column" context menu to a table
     * @param source - context menu will attach to this
     * @param addColumnButton - util will attach event listener to this
     * @param targetTable - table where data will be manipulated
     * @param gid - property of GID (button with GID as caption) on that table
     */
    public AddColumnContextMenu(AbsoluteLayout absoluteLayoutSource, 
            Button addColumnButton, Table targetTable, String gid){
        this.GIDPropertyId = gid;
        this.targetTable = targetTable;
        this.addColumnButton = addColumnButton;
        this.absoluteLayoutSource = absoluteLayoutSource;
        
        setupContextMenu();
    }
    
    /**
     * Add "Add column" context menu to a table
     * @param source - context menu will attach to this
     * @param addColumnButton - util will attach event listener to this
     * @param targetTable - table where data will be manipulated
     * @param gid - property of GID (button with GID as caption) on that table
     */
    public AddColumnContextMenu(AbsoluteLayout absoluteLayoutSource, 
            Button addColumnButton, Table targetTable, String gid, boolean fromBuildNewList){
        this.GIDPropertyId = gid;
        this.targetTable = targetTable;
        this.addColumnButton = addColumnButton;
        this.absoluteLayoutSource = absoluteLayoutSource;
        this.fromBuildNewList = fromBuildNewList;
        
    	if(fromBuildNewList){
    		buildNewListComponent = ((BuildNewListComponent) absoluteLayoutSource);
    	}
        
        setupContextMenu();
        
    }
    
	/**
	 * Add "Add column" context menu to a table
	 * @param addColumnButton - util will attach event listener to this
	 * @param targetTable - table where data will be manipulated
	 * @param gid - property of GID (button with GID as caption) on that table
	 */
    public AddColumnContextMenu(Table targetTable, String gid){
    	this.GIDPropertyId = gid;
    	this.targetTable = targetTable;
    	
    	setupContextMenu();
    }
    
    private void setupContextMenu(){
    	
    	menu = new ContextMenu();
		menuFillWithPreferredId = menu.addItem(FILL_WITH_PREFERRED_ID);
		menuFillWithPreferredName = menu.addItem(FILL_WITH_PREFERRED_NAME);
		menuFillWithLocations = menu.addItem(FILL_WITH_LOCATION);
		menuFillWithMethodInfo = menu.addItem(FILL_WITH_METHOD_INFO);
		
		//breeding method sub-options
		menuFillWithMethodName = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_NAME);
		menuFillWithMethodAbbrev = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_ABBREV);
		menuFillWithMethodNumber = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_NUMBER);
		menuFillWithMethodGroup = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_GROUP);
    	
    	menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = 1L;

		    //Handle clicks on menu items
			@Override
			public void contextItemClick(ClickEvent event) {
			    ContextMenuItem clickedItem = event.getClickedItem();
			    if(clickedItem.getName().equals(FILL_WITH_PREFERRED_ID)){
			      	addPreferredIdColumn();
			    }else if(clickedItem.getName().equals(FILL_WITH_PREFERRED_NAME)){
			    	addPreferredNameColumn();
			    }else if(clickedItem.getName().equals(FILL_WITH_LOCATION)){
			    	addLocationColumn();
			    }else if(clickedItem.getName().equals(FILL_WITH_METHOD_NAME)){
                    addMethodNameColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_METHOD_ABBREV)){
                    addMethodAbbrevColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_METHOD_NUMBER)){
                    addMethodNumberColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_METHOD_GROUP)){
                    addMethodGroupColumn();
                }
			}
			
        });
    	
    	//Attach menu to whatever source passed to the constructor of this class/util
    	if(absoluteLayoutSource!=null)
    		absoluteLayoutSource.addComponent(menu);
    	
    	//Attach listener to the "Add Column" button passed to the constructor of this class/util
    	if(addColumnButton!=null){
	    	addColumnButton.addListener(new ClickListener() {
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
					
					//Check if columns already exist in the table
					if(propertyExists(PREFERRED_ID)){
						menuFillWithPreferredId.setEnabled(false);
					} else {
						menuFillWithPreferredId.setEnabled(true);
					}
					
					if(propertyExists(PREFERRED_NAME)){
						menuFillWithPreferredName.setEnabled(false);
					} else {
						menuFillWithPreferredName.setEnabled(true);
					}
					
					if(propertyExists(LOCATIONS)){
						menuFillWithLocations.setEnabled(false);
					} else {
						menuFillWithLocations.setEnabled(true);
					}
					
					if(propertyExists(METHOD_NAME)){
                        menuFillWithMethodName.setEnabled(false);
                    } else {
                        menuFillWithMethodName.setEnabled(true);
                    }
					
					if(propertyExists(METHOD_ABBREV)){
                        menuFillWithMethodAbbrev.setEnabled(false);
                    } else {
                        menuFillWithMethodAbbrev.setEnabled(true);
                    }
					
					if(propertyExists(METHOD_NUMBER)){
                        menuFillWithMethodNumber.setEnabled(false);
                    } else {
                        menuFillWithMethodNumber.setEnabled(true);
                    }
					
					if(propertyExists(METHOD_GROUP)){
                        menuFillWithMethodGroup.setEnabled(false);
                    } else {
                        menuFillWithMethodGroup.setEnabled(true);
                    }
					
					//Display context menu
					menu.show(event.getClientX(), event.getClientY());
				}
			 });
    	 }
	 
    }
    
    
    private void addPreferredIdColumn(){
    	if(!propertyExists(PREFERRED_ID)){
    		targetTable.addContainerProperty(PREFERRED_ID, PREFERRED_ID_TYPE, "");
    		setPreferredIdColumnValues(true);
    	}
    }
    
    public void setPreferredIdColumnValues(boolean fromAddColumn){
    	if(propertyExists(PREFERRED_ID)){
    		try {
    			List<Integer> itemIds = getItemIds(targetTable);
    			for(Integer itemId: itemIds){
    				Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
    				String preferredID = "";
    				Name name = germplasmDataManager.getPreferredIdByGID(gid);
    				if(name!=null && name.getNval()!=null)
    					preferredID = name.getNval();
    				targetTable.getItem(itemId).getItemProperty(PREFERRED_ID).setValue(preferredID);
    			}
			   
				//To trigger TableFieldFactory (fix for truncated data)
    			if(targetTable.isEditable()){
    				targetTable.setEditable(false);
    				targetTable.setEditable(true);
    			}
    			
		       //mark flag that changes have been made in listDataTable
		       if(listManagerTreeMenu != null){ listManagerTreeMenu.setChanged(true); }
		       
		       //mark flag that changes have been made in buildNewListTable
		       if(buildNewListComponent != null){ buildNewListComponent.setHasChanges(true); }	
		       
    		} catch (MiddlewareQueryException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void addPreferredNameColumn(){
    	if(!propertyExists(PREFERRED_NAME)){
    		targetTable.addContainerProperty(PREFERRED_NAME, PREFERRED_NAME_TYPE, "");
    		setPreferredNameColumnValues(true);
    	}
    }
    
    public void setPreferredNameColumnValues(boolean fromAddColumn){
    	if(propertyExists(PREFERRED_NAME)){
			try {
				List<Integer> itemIds = getItemIds(targetTable);
				for(Integer itemId: itemIds){
					Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
					
					String preferredName = "";
					if(germplasmDataManager.getPreferredNameByGID(gid)!=null && germplasmDataManager.getPreferredNameByGID(gid).getNval()!=null)
							preferredName = germplasmDataManager.getPreferredNameByGID(gid).getNval();
					targetTable.getItem(itemId).getItemProperty(PREFERRED_NAME).setValue(preferredName);
				}

				//To trigger TableFieldFactory (fix for truncated data)
				if(targetTable.isEditable()){
    				targetTable.setEditable(false);
    				targetTable.setEditable(true);
    			}
				
		       //mark flag that changes have been made in listDataTable
		       if(listManagerTreeMenu != null){ listManagerTreeMenu.setChanged(true); }
		       
		       //mark flag that changes have been made in buildNewListTable
		       if(buildNewListComponent != null){ buildNewListComponent.setHasChanges(true); }	
		       
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}  
    	}
    }
    
    private void addLocationColumn(){
    	if(!propertyExists(LOCATIONS)){
    		targetTable.addContainerProperty(LOCATIONS, LOCATIONS_TYPE, "");
    		setLocationColumnValues(true);
    	}
    }
    
    public void setLocationColumnValues(boolean fromAddColumn){
    	if(propertyExists(LOCATIONS)){
			try {
				List<Integer> itemIds = getItemIds(targetTable);
				
				final Map<Integer, String> allLocationNamesMap = new HashMap<Integer, String>();
				
				for(Integer itemId: itemIds){
					Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
					
					List<Integer> gids = new ArrayList<Integer>();
					gids.add(gid);
					
					Map<Integer, String> locationNamesMap = germplasmDataManager.getLocationNamesByGids(gids);
					allLocationNamesMap.putAll(locationNamesMap);
					
					if(locationNamesMap.get(gid)==null)
						targetTable.getItem(itemId).getItemProperty(LOCATIONS).setValue("");
					else
						targetTable.getItem(itemId).getItemProperty(LOCATIONS).setValue(locationNamesMap.get(gid));
				}

				//To trigger TableFieldFactory (fix for truncated data)
				if(targetTable.isEditable()){
    				targetTable.setEditable(false);
    				targetTable.setEditable(true);
    			}
					
		       //mark flag that changes have been made in listDataTable
		       if(listManagerTreeMenu != null){ listManagerTreeMenu.setChanged(true); }
		       
		       //mark flag that changes have been made in buildNewListTable
		       if(buildNewListComponent != null){ buildNewListComponent.setHasChanges(true); }	
		       
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}    	
    	}
    }
    
    private void addMethodNameColumn(){
        if(!propertyExists(METHOD_NAME)){
            targetTable.addContainerProperty(METHOD_NAME, METHOD_NAME_TYPE, "");
            setMethodInfoColumnValues(true, METHOD_NAME);
        }
    }
    
    private void addMethodAbbrevColumn(){
        if(!propertyExists(METHOD_ABBREV)){
            targetTable.addContainerProperty(METHOD_ABBREV, METHOD_ABBREV_TYPE, "");
            setMethodInfoColumnValues(true, METHOD_ABBREV);
        }
    }
    
    private void addMethodNumberColumn(){
        if(!propertyExists(METHOD_NUMBER)){
            targetTable.addContainerProperty(METHOD_NUMBER, METHOD_NUMBER_TYPE, "");
            setMethodInfoColumnValues(true, METHOD_NUMBER);
        }
    }
    
    private void addMethodGroupColumn(){
        if(!propertyExists(METHOD_GROUP)){
            targetTable.addContainerProperty(METHOD_GROUP, METHOD_GROUP_TYPE, "");
            setMethodInfoColumnValues(true, METHOD_GROUP);
        }
    }
    
    public void setMethodInfoColumnValues(boolean fromAddColumn, String columnName){
        if(propertyExists(columnName)){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                final Map<Integer, Object> allMethodsMap = new HashMap<Integer, Object>();
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
                    
                    List<Integer> gids = new ArrayList<Integer>();
                    gids.add(gid);
                    
                    Map<Integer, Object> methodsMap = germplasmDataManager.getMethodsByGids(gids);
                    allMethodsMap.putAll(methodsMap);
                    
                    if(methodsMap.get(gid)==null) {
                        targetTable.getItem(itemId).getItemProperty(columnName).setValue("");
                    } else {
                        String value = "";
                        
                        if (columnName.equals(METHOD_NAME)) {
                            value = ((Method)methodsMap.get(gid)).getMname();
                        } else if (columnName.equals(METHOD_ABBREV)) {
                            value = ((Method)methodsMap.get(gid)).getMcode();
                        } else if (columnName.equals(METHOD_NUMBER)) {
                            value = ((Method)methodsMap.get(gid)).getMid().toString();
                        } else if (columnName.equals(METHOD_GROUP)) {
                            value = ((Method)methodsMap.get(gid)).getMgrp();
                        }
                        
                        targetTable.getItem(itemId).getItemProperty(columnName).setValue(value);
                    }
                }

                //To trigger TableFieldFactory (fix for truncated data)
                if(targetTable.isEditable()){
                    targetTable.setEditable(false);
                    targetTable.setEditable(true);
                }
                    
                //mark flag that changes have been made in listDataTable
                if(listManagerTreeMenu != null){ listManagerTreeMenu.setChanged(true); }
                
                //mark flag that changes have been made in buildNewListTable
                if(buildNewListComponent != null){ buildNewListComponent.setHasChanges(true); }	
                
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in filling with Method Info values.", e);
                e.printStackTrace();
            }       
        }
    }
    
    public Boolean propertyExists(String propertyId){
    	List<String> propertyIds = getTablePropertyIds(targetTable);
    	return propertyIds.contains(propertyId);
    }

    public static Boolean propertyExists(String propertyId, Table table){
    	List<String> propertyIds = getTablePropertyIds(table);
    	return propertyIds.contains(propertyId);
    }    
    
    @SuppressWarnings("unchecked")
	public static List<String> getTablePropertyIds(Table table){
    	if(table!=null){
    		List<String> propertyIds = new ArrayList<String>();
    		propertyIds.addAll((Collection<? extends String>) table.getContainerPropertyIds());
    		return propertyIds;
    	} else {
    		return new ArrayList<String>();
    	}
    }    
    
    public List<Integer> getGidsFromTable(Table table){
    	List<Integer> gids = new ArrayList<Integer>();
    	List<Integer> listDataItemIds = getItemIds(table);
    	for(Integer itemId: listDataItemIds){
    		//gids.add((Integer) table.getItem(itemId).getItemProperty(GID_VALUE).getValue());
    		gids.add(Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString()));
    	}
    	return gids;
    }
   
	@SuppressWarnings("unchecked")
	public List<Integer> getItemIds(Table table){
		List<Integer> itemIds = new ArrayList<Integer>();
    	itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
    	return itemIds;
	}
	
    
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	} 
	
	/**
	 * Save erases all values on the table, including the added columns, use this to re-populate it with data
	 */
	public void populateAddedColumns(){
		for(String propertyId: AddColumnContextMenu.ADDABLE_PROPERTY_IDS){
			if(propertyExists(propertyId)){
				if(propertyId.equals(AddColumnContextMenu.PREFERRED_ID))
					setPreferredIdColumnValues(false);
				else if(propertyId.equals(AddColumnContextMenu.PREFERRED_NAME))
					setPreferredNameColumnValues(false);
				else if(propertyId.equals(AddColumnContextMenu.LOCATIONS))
					setLocationColumnValues(false);
				else if(propertyId.equals(AddColumnContextMenu.METHOD_NAME))
		            setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NAME);
		        else if(propertyId.equals(AddColumnContextMenu.METHOD_ABBREV))
		            setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_ABBREV);
		        else if(propertyId.equals(AddColumnContextMenu.METHOD_NUMBER))
		            setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NUMBER);
		        else if(propertyId.equals(AddColumnContextMenu.METHOD_GROUP))
		            setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_GROUP);
			}
		}
	}
	
	
    /**
     * This has to be called after the list entries has been saved, because it'll need the germplasmListEntryId
     * @return
     */
    public List<ListDataInfo> getListDataCollectionFromTable(Table table){
    	
//    	populateAddedColumns();
    	
    	List<ListDataInfo> listDataCollection = new ArrayList<ListDataInfo>();
    	
    	for(Object itemId : table.getItemIds()){
    		Item item = table.getItem(itemId);
    		List<ListDataColumn> columns = new ArrayList<ListDataColumn>();
    		for(String propertyId: ADDABLE_PROPERTY_IDS){
    			if(AddColumnContextMenu.propertyExists(propertyId, table)){
	    			if(item.getItemProperty(propertyId).getValue()!=null)
	    				columns.add(new ListDataColumn(propertyId, item.getItemProperty(propertyId).getValue().toString()));
	    			else
	    				columns.add(new ListDataColumn(propertyId, null));
    			}
    		}
    		listDataCollection.add(new ListDataInfo(Integer.valueOf(itemId.toString()),columns));
    	}
    	return listDataCollection;
    }


    /**
     * This can be used to add columns given a property ID (should be one of the addable ID's)
     */
	public void addColumn(String propertyId){
		if(propertyId.equals(AddColumnContextMenu.PREFERRED_ID))
			addPreferredIdColumn();
		else if(propertyId.equals(AddColumnContextMenu.PREFERRED_NAME))
			addPreferredNameColumn();
		else if(propertyId.equals(AddColumnContextMenu.LOCATIONS))
			addLocationColumn();
		else if(propertyId.equals(AddColumnContextMenu.METHOD_NAME))
            addMethodNameColumn();
		else if(propertyId.equals(AddColumnContextMenu.METHOD_ABBREV))
            addMethodAbbrevColumn();
		else if(propertyId.equals(AddColumnContextMenu.METHOD_NUMBER))
            addMethodNumberColumn();
		else if(propertyId.equals(AddColumnContextMenu.METHOD_GROUP))
            addMethodGroupColumn();
	}
    
}

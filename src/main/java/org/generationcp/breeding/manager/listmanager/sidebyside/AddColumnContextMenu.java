package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.*;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
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

@Configurable
public class AddColumnContextMenu implements InternationalizableComponent  {
    private static final Logger LOG = LoggerFactory.getLogger(AddColumnContextMenu.class);

    @Autowired
    private GermplasmDataManager germplasmDataManager;

    private ListTabComponent listDetailsComponent = null;
    private AbsoluteLayout absoluteLayoutSource;
    private ComponentContainer cssLayoutSource;
    private final String GIDPropertyId;
    //private Button addColumnButton;
    private final Table targetTable;
    
    //private ContextMenu menu;
    private ContextMenu sourceContextMenu;
    private ContextMenuItem addColumnItem;
    private ContextMenuItem menuFillWithPreferredId;
    private ContextMenuItem menuFillWithPreferredName;
    private ContextMenuItem menuFillWithGermplasmDate;
    private ContextMenuItem menuFillWithLocations;
    private ContextMenuItem menuFillWithMethodInfo;
    private ContextMenuItem menuFillWithMethodName;
    private ContextMenuItem menuFillWithMethodAbbrev;
    private ContextMenuItem menuFillWithMethodNumber;
    private ContextMenuItem menuFillWithMethodGroup;
    private ContextMenuItem menuFillWithCrossFemaleInfo;
    private ContextMenuItem menuFillWithCrossFemaleGID;
    private ContextMenuItem menuFillWithCrossFemalePrefName;
    private ContextMenuItem menuFillWithCrossMaleInfo;
    private ContextMenuItem menuFillWithCrossMaleGID;
    private ContextMenuItem menuFillWithCrossMalePrefName;
    
    public static String ADD_COLUMN_MENU = "Add Column";
    public static String FILL_WITH_PREFERRED_ID = "Fill with Preferred ID";
    public static String FILL_WITH_PREFERRED_NAME = "Fill with Preferred Name";
    public static String FILL_WITH_GERMPLASM_DATE = "Fill with Germplasm Dates";
    public static String FILL_WITH_LOCATION = "Fill with Location";
    public static String FILL_WITH_METHOD_INFO = "Fill with Breeding Method Information";
    public static String FILL_WITH_METHOD_NAME = "Fill with Breeding Method Name";
    public static String FILL_WITH_METHOD_ABBREV = "Fill with Breeding Method Abbreviation";
    public static String FILL_WITH_METHOD_NUMBER = "Fill with Breeding Method Number";
    public static String FILL_WITH_METHOD_GROUP = "Fill with Breeding Method Group";
    public static String FILL_WITH_CROSS_FEMALE_INFO = "Fill with Cross-Female Information";
    public static String FILL_WITH_CROSS_FEMALE_GID = "Fill with Cross-Female GID";
    public static String FILL_WITH_CROSS_FEMALE_PREF_NAME = "Fill with Cross-Female Preferred Name";
    public static String FILL_WITH_CROSS_MALE_INFO = "Fill with Cross-Male Information";
    public static String FILL_WITH_CROSS_MALE_GID = "Fill with Cross-Male GID";
    public static String FILL_WITH_CROSS_MALE_PREF_NAME = "Fill with Cross-Male Preferred Name";
    
    @SuppressWarnings("rawtypes")
    public static Class PREFERRED_ID_TYPE = String.class;
    public static String PREFERRED_ID = "PREFERRED ID";
    
    @SuppressWarnings("rawtypes")
    public static Class PREFERRED_NAME_TYPE = String.class;
    public static String PREFERRED_NAME = "PREFERRED NAME";
    
    @SuppressWarnings("rawtypes")
    public static Class GERMPLASM_DATE_TYPE = String.class;
    public static String GERMPLASM_DATE = "GERMPLASM DATE";
    
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
    
    @SuppressWarnings("rawtypes")
    public static Class CROSS_FEMALE_GID_TYPE = String.class;
    public static String CROSS_FEMALE_GID = "CROSS-FEMALE GID";
    
    @SuppressWarnings("rawtypes")
    public static Class CROSS_FEMALE_PREF_NAME_TYPE = String.class;
    public static String CROSS_FEMALE_PREF_NAME = "CROSS-FEMALE PREFERRED NAME";
    
    @SuppressWarnings("rawtypes")
    public static Class CROSS_MALE_GID_TYPE = String.class;
    public static String CROSS_MALE_GID = "CROSS-MALE GID";
    
    @SuppressWarnings("rawtypes")
    public static Class CROSS_MALE_PREF_NAME_TYPE = String.class;
    public static String CROSS_MALE_PREF_NAME = "CROSS-MALE PREFERRED NAME";
    
    private boolean fromBuildNewList;
    private ListBuilderComponent buildNewListComponent;
    
    public static List<String> ADDABLE_PROPERTY_IDS;
    
    /**
     * Add "Add column" context menu to a table
     * @param listDetailsComponent - tab content from list manager details section.
     * @param sourceContextMenu - util will attach event listener to this
     * @param targetTable - table where data will be manipulated
     * @param gid - property of GID (button with GID as caption) on that table
     */
    public AddColumnContextMenu(ListTabComponent listDetailsComponent, 
            ContextMenu sourceContextMenu, Table targetTable, String gid){
        this.listDetailsComponent = listDetailsComponent;
        this.GIDPropertyId = gid;
        this.targetTable = targetTable;
        this.sourceContextMenu = sourceContextMenu;
        //this.absoluteLayoutSource = absoluteLayoutSource; 
        
        setupContextMenu();
    }
    
    
    /**
     * Add "Add column" context menu to a table
     * @param cssLayoutSource - context menu will attach to this
     * @param sourceContextMenu - util will attach event listener to this
     * @param targetTable - table where data will be manipulated
     * @param gid - property of GID (button with GID as caption) on that table
     */
    public AddColumnContextMenu(ComponentContainer cssLayoutSource,
            ContextMenu sourceContextMenu, Table targetTable, String gid, boolean fromBuildNewList){
        this.GIDPropertyId = gid;
        this.targetTable = targetTable;
        this.sourceContextMenu = sourceContextMenu;
        this.cssLayoutSource = cssLayoutSource;
        this.fromBuildNewList = fromBuildNewList;
        
        if(fromBuildNewList){
            buildNewListComponent = ((ListBuilderComponent) cssLayoutSource);
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
    	
    	initializeAddableProperties();
        
        addColumnItem = sourceContextMenu.addItem(ADD_COLUMN_MENU);
        menuFillWithPreferredId = addColumnItem.addItem(FILL_WITH_PREFERRED_ID);
        menuFillWithPreferredName = addColumnItem.addItem(FILL_WITH_PREFERRED_NAME);
        menuFillWithGermplasmDate = addColumnItem.addItem(FILL_WITH_GERMPLASM_DATE);
        menuFillWithLocations = addColumnItem.addItem(FILL_WITH_LOCATION);
        menuFillWithMethodInfo = addColumnItem.addItem(FILL_WITH_METHOD_INFO);
        menuFillWithCrossFemaleInfo = addColumnItem.addItem(FILL_WITH_CROSS_FEMALE_INFO);
        menuFillWithCrossMaleInfo = addColumnItem.addItem(FILL_WITH_CROSS_MALE_INFO);
        
        //breeding method sub-options
        menuFillWithMethodName = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_NAME);
        menuFillWithMethodAbbrev = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_ABBREV);
        menuFillWithMethodNumber = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_NUMBER);
        menuFillWithMethodGroup = menuFillWithMethodInfo.addItem(FILL_WITH_METHOD_GROUP);
        
        //cross female sub-options
        menuFillWithCrossFemaleGID = menuFillWithCrossFemaleInfo.addItem(FILL_WITH_CROSS_FEMALE_GID);
        menuFillWithCrossFemalePrefName = menuFillWithCrossFemaleInfo.addItem(FILL_WITH_CROSS_FEMALE_PREF_NAME);
        
        //cross-male info sub-options
        menuFillWithCrossMaleGID = menuFillWithCrossMaleInfo.addItem(FILL_WITH_CROSS_MALE_GID);
        menuFillWithCrossMalePrefName = menuFillWithCrossMaleInfo.addItem(FILL_WITH_CROSS_MALE_PREF_NAME);
        
        sourceContextMenu.addListener(new ContextMenu.ClickListener() {
            private static final long serialVersionUID = 1L;

            //Handle clicks on menu items
            @Override
            public void contextItemClick(ClickEvent event) {
                ContextMenuItem clickedItem = event.getClickedItem();
                if(clickedItem.getName().equals(FILL_WITH_PREFERRED_ID)){
                      addPreferredIdColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_PREFERRED_NAME)){
                    addPreferredNameColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_GERMPLASM_DATE)){
                    addGermplasmDateColumn();
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
                }else if(clickedItem.getName().equals(FILL_WITH_CROSS_FEMALE_GID)){
                    addCrossFemaleGidColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_CROSS_FEMALE_PREF_NAME)){
                    addCrossFemalePrefNameColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_CROSS_MALE_GID)){
                	addCrossMaleGIDColumn();
                }else if(clickedItem.getName().equals(FILL_WITH_CROSS_MALE_PREF_NAME)){
                	addCrossMalePrefNameColumn();
                }
            }
            
        });
        
        //FIXME: sidebyside
        //Attach menu to whatever source passed to the constructor of this class/util
        /*if(absoluteLayoutSource!=null)
            absoluteLayoutSource.addComponent(menu);*/
        
        //Attach listener to the "Add Column" button passed to the constructor of this class/util
        /*if(this.sourceContextMenu!=null){
            sourceContextMenu.addListener(new ContextMenu.ClickListener() {
    
                private static final long serialVersionUID = -6399264383924196725L;

                @Override
                public void contextItemClick(ClickEvent event) {
                    ContextMenuItem clickedItem = event.getClickedItem();
                    if(clickedItem.getName().equals(ADD_COLUMN_MENU)){
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
                        
                        if(propertyExists(GERMPLASM_DATE)){
                            menuFillWithGermplasmDate.setEnabled(false);
                        } else {
                            menuFillWithGermplasmDate.setEnabled(true);
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
                        
                        if(propertyExists(METHOD_NAME) && propertyExists(METHOD_ABBREV)
                                && propertyExists(METHOD_NUMBER) && propertyExists(METHOD_GROUP)){
                            menuFillWithMethodInfo.setEnabled(false);
                        } else {
                            menuFillWithMethodInfo.setEnabled(true);
                        }
                        
                        if(propertyExists(CROSS_FEMALE_GID)){
                            menuFillWithCrossFemaleGID.setEnabled(false);
                        } else {
                            menuFillWithCrossFemaleGID.setEnabled(true);
                        }
                        
                        if(propertyExists(CROSS_FEMALE_PREF_NAME)){
                            menuFillWithCrossFemalePrefName.setEnabled(false);
                        } else {
                            menuFillWithCrossFemalePrefName.setEnabled(true);
                        }
                        
                        if(propertyExists(CROSS_FEMALE_GID) && propertyExists(CROSS_FEMALE_PREF_NAME)){
                            menuFillWithCrossFemaleInfo.setEnabled(false);
                        } else {
                            menuFillWithCrossFemaleInfo.setEnabled(true);
                        }
                        
                        if(propertyExists(CROSS_MALE_GID)){
                            menuFillWithCrossMaleGID.setEnabled(false);
                        } else {
                            menuFillWithCrossMaleGID.setEnabled(true);
                        }
                        
                        if(propertyExists(CROSS_MALE_PREF_NAME)){
                            menuFillWithCrossMalePrefName.setEnabled(false);
                        } else {
                            menuFillWithCrossMalePrefName.setEnabled(true);
                        }
                        
                        if(propertyExists(CROSS_MALE_GID) && propertyExists(CROSS_MALE_PREF_NAME)){
                            menuFillWithCrossMaleInfo.setEnabled(false);
                        } else {
                            menuFillWithCrossMaleInfo.setEnabled(true);
                        }
                        
                        //Display context menu
                        //menu.show(event.getClientX(), event.getClientY());
                        //sourceContextMenu.requestRepaint();
                    }
                }
            });
        }*/
    }
    
    public void initializeAddableProperties(){
    	
    	ADDABLE_PROPERTY_IDS = new ArrayList<String>();
    	
    	ADDABLE_PROPERTY_IDS.add(PREFERRED_ID);
    	ADDABLE_PROPERTY_IDS.add(PREFERRED_NAME);
    	ADDABLE_PROPERTY_IDS.add(GERMPLASM_DATE);
    	ADDABLE_PROPERTY_IDS.add(LOCATIONS);
    	ADDABLE_PROPERTY_IDS.add(METHOD_NAME);
    	ADDABLE_PROPERTY_IDS.add(METHOD_ABBREV);
    	ADDABLE_PROPERTY_IDS.add(METHOD_NUMBER);
    	ADDABLE_PROPERTY_IDS.add(METHOD_GROUP);
    	ADDABLE_PROPERTY_IDS.add(CROSS_FEMALE_GID);
    	ADDABLE_PROPERTY_IDS.add(CROSS_FEMALE_PREF_NAME);
    	ADDABLE_PROPERTY_IDS.add(CROSS_MALE_GID);
    	ADDABLE_PROPERTY_IDS.add(CROSS_MALE_PREF_NAME);
    }
    
    public void refreshAddColumnMenu() {
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
        
        if(propertyExists(GERMPLASM_DATE)){
            menuFillWithGermplasmDate.setEnabled(false);
        } else {
            menuFillWithGermplasmDate.setEnabled(true);
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
        
        if(propertyExists(METHOD_NAME) && propertyExists(METHOD_ABBREV)
                && propertyExists(METHOD_NUMBER) && propertyExists(METHOD_GROUP)){
            menuFillWithMethodInfo.setEnabled(false);
        } else {
            menuFillWithMethodInfo.setEnabled(true);
        }
        
        if(propertyExists(CROSS_FEMALE_GID)){
            menuFillWithCrossFemaleGID.setEnabled(false);
        } else {
            menuFillWithCrossFemaleGID.setEnabled(true);
        }
        
        if(propertyExists(CROSS_FEMALE_PREF_NAME)){
            menuFillWithCrossFemalePrefName.setEnabled(false);
        } else {
            menuFillWithCrossFemalePrefName.setEnabled(true);
        }
        
        if(propertyExists(CROSS_FEMALE_GID) && propertyExists(CROSS_FEMALE_PREF_NAME)){
            menuFillWithCrossFemaleInfo.setEnabled(false);
        } else {
            menuFillWithCrossFemaleInfo.setEnabled(true);
        }
        
        if(propertyExists(CROSS_MALE_GID)){
            menuFillWithCrossMaleGID.setEnabled(false);
        } else {
            menuFillWithCrossMaleGID.setEnabled(true);
        }
        
        if(propertyExists(CROSS_MALE_PREF_NAME)){
            menuFillWithCrossMalePrefName.setEnabled(false);
        } else {
            menuFillWithCrossMalePrefName.setEnabled(true);
        }
        
        if(propertyExists(CROSS_MALE_GID) && propertyExists(CROSS_MALE_PREF_NAME)){
            menuFillWithCrossMaleInfo.setEnabled(false);
        } else {
            menuFillWithCrossMaleInfo.setEnabled(true);
        }
        
        sourceContextMenu.requestRepaint();
    }
    
    private void doFixForTruncatedDataInEditableTable(){
    	if(targetTable.isEditable()){
            targetTable.setEditable(false);
            targetTable.setEditable(true);
        }
    }
    
    private void markHasChangesFlags(boolean fromAddColumn){
    	//mark flag that changes have been made in listDataTable
        if(listDetailsComponent != null && fromAddColumn){ 
        	listDetailsComponent.setChanged(true); 
        }
        
        //mark flag that changes have been made in buildNewListTable
        if(buildNewListComponent != null){ 
        	buildNewListComponent.setChanged(true); 
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
                doFixForTruncatedDataInEditableTable();
                
                markHasChangesFlags(fromAddColumn);
            } catch (MiddlewareQueryException e) {
            	LOG.error("Error in filling with preferred id values.", e);
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
                doFixForTruncatedDataInEditableTable();
                
                markHasChangesFlags(fromAddColumn);    
               
            } catch (MiddlewareQueryException e) {
            	LOG.error("Error in filling with preferred name values.", e);
            }  
        }
    }
    
    private void addGermplasmDateColumn(){
        if(!propertyExists(GERMPLASM_DATE)){
            targetTable.addContainerProperty(GERMPLASM_DATE, GERMPLASM_DATE_TYPE, "");
            //TODO: can create separate method for adding container property and the actual setting of column values,
            //      so that the middleware call below can be called only once without having the gids become null
            setGermplasmDateColumnValues(true);
        }
    }
    
    public void setGermplasmDateColumnValues(boolean fromAddColumn){
        if(propertyExists(GERMPLASM_DATE)){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
                    
                    List<Integer> gids = new ArrayList<Integer>();
                    gids.add(gid);
                    
                    //TODO can make better use of the middleware method by just calling it once and not have it inside a loop
                    Map<Integer,Integer> germplasmGidDateMap = germplasmDataManager.getGermplasmDatesByGids(gids);
                    
                    if(germplasmGidDateMap.get(gid)==null)
                        targetTable.getItem(itemId).getItemProperty(GERMPLASM_DATE).setValue("");
                    else
                        targetTable.getItem(itemId).getItemProperty(GERMPLASM_DATE).setValue(germplasmGidDateMap.get(gid));
                }
                
                //To trigger TableFieldFactory (fix for truncated data)
                doFixForTruncatedDataInEditableTable();
                
                markHasChangesFlags(fromAddColumn);
               
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in filling with Germplasm Date values.", e);
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
                doFixForTruncatedDataInEditableTable();
                    
                markHasChangesFlags(fromAddColumn);    
               
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in filling with Location values.", e);
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
                doFixForTruncatedDataInEditableTable();
                    
                markHasChangesFlags(fromAddColumn);    
                
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in filling with Method Info values.", e);
            }       
        }
    }
    
    private void addCrossMaleGIDColumn(){
        if(!propertyExists(CROSS_MALE_GID)){
            targetTable.addContainerProperty(CROSS_MALE_GID, CROSS_MALE_GID_TYPE, "-");
            setCrossMaleGIDColumnValues(true);
        }
    }
    
    public void setCrossMaleGIDColumnValues(boolean fromAddColumn){
        if(propertyExists(CROSS_MALE_GID)){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
                    
                    Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
                    
                    if(germplasm != null){
	                    if(germplasm.getGnpgs() >= 2) {
	                    	if(germplasm.getGpid2() != null && germplasm.getGpid2() != 0){
	                    		targetTable.getItem(itemId).getItemProperty(CROSS_MALE_GID).setValue(germplasm.getGpid2().toString());
	                    	} else{
	                    		targetTable.getItem(itemId).getItemProperty(CROSS_MALE_GID).setValue("-");
	                    	}
	                    }
	                    else {
	                        targetTable.getItem(itemId).getItemProperty(CROSS_MALE_GID).setValue("-");
	                    }
                    } else{
                    	targetTable.getItem(itemId).getItemProperty(CROSS_MALE_GID).setValue("-");
                    }
                }
                
                //To trigger TableFieldFactory (fix for truncated data)
                doFixForTruncatedDataInEditableTable();
                
                markHasChangesFlags(fromAddColumn);
               
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in filling with Cross-Male GID values.", e);
            }        
        }
    }
    
    private void addCrossMalePrefNameColumn(){
        if(!propertyExists(CROSS_MALE_PREF_NAME)){
            targetTable.addContainerProperty(CROSS_MALE_PREF_NAME, CROSS_MALE_PREF_NAME_TYPE, "-");
            setCrossMalePrefNameColumnValues(true);
        }
    }
    
    public void setCrossMalePrefNameColumnValues(boolean fromAddColumn){
        if(propertyExists(CROSS_MALE_PREF_NAME)){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                Map<Integer, List<Integer>> gidToItemIdMap = new HashMap<Integer, List<Integer>>();
                List<Integer> gidsToUseForQuery = new ArrayList<Integer>();
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
                    
                    Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
                    
                    if(germplasm != null){
	                    if(germplasm.getGnpgs() >= 2 && germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
	                        gidsToUseForQuery.add(germplasm.getGpid2());
	                        List<Integer> itemIdsInMap = gidToItemIdMap.get(germplasm.getGpid2());
	                        if(itemIdsInMap == null){
	                        	itemIdsInMap = new ArrayList<Integer>();
	                        	itemIdsInMap.add(itemId);
	                        	gidToItemIdMap.put(germplasm.getGpid2(), itemIdsInMap);
	                        } else{
	                        	itemIdsInMap.add(itemId);
	                        }
	                    }
	                    else {
	                        targetTable.getItem(itemId).getItemProperty(CROSS_MALE_PREF_NAME).setValue("-");
	                    }
                    } else{
                    	targetTable.getItem(itemId).getItemProperty(CROSS_MALE_PREF_NAME).setValue("-");
                    }
                }
                
                if(!gidsToUseForQuery.isEmpty()){
                	Map<Integer, String> gidToNameMap = germplasmDataManager.getPreferredNamesByGids(gidsToUseForQuery);
                	
                	for(Integer gid : gidToNameMap.keySet()){
                		String prefName = gidToNameMap.get(gid);
                		List<Integer> itemIdsInMap = gidToItemIdMap.get(gid);
                		for(Integer itemId : itemIdsInMap){
                			targetTable.getItem(itemId).getItemProperty(CROSS_MALE_PREF_NAME).setValue(prefName);
                		}
                	}
                }
                
                //To trigger TableFieldFactory (fix for truncated data)
                doFixForTruncatedDataInEditableTable();
                
                markHasChangesFlags(fromAddColumn);
               
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in filling with Cross-Male Preferred Name values.", e);
            }        
        }
    }
    
    private void addCrossFemaleGidColumn(){
        if(!propertyExists(CROSS_FEMALE_GID)){
            targetTable.addContainerProperty(CROSS_FEMALE_GID, CROSS_FEMALE_GID_TYPE, "");
            setCrossFemaleInfoColumnValues(true, CROSS_FEMALE_GID);
        }
    }
    
    private void addCrossFemalePrefNameColumn(){
        if(!propertyExists(CROSS_FEMALE_PREF_NAME)){
            targetTable.addContainerProperty(CROSS_FEMALE_PREF_NAME, CROSS_FEMALE_PREF_NAME_TYPE, "");
            setCrossFemaleInfoColumnValues(true, CROSS_FEMALE_PREF_NAME);
        }
    }
    
    public void setCrossFemaleInfoColumnValues(boolean fromAddColumn, String columnName){
        if(propertyExists(columnName)){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
                    
                    Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
                    Germplasm femaleParent = null;
                    // get female only if germplasm is created via generative process
                    if (germplasm.getGnpgs() >= 2) {
                        femaleParent = germplasmDataManager.getGermplasmByGID(germplasm.getGpid1());
                    }
                    
                    if(femaleParent == null) {
                        targetTable.getItem(itemId).getItemProperty(columnName).setValue("-");
                    } else {
                        String value = "-";
                        if (columnName.equals(CROSS_FEMALE_GID)) {
                            value = femaleParent.getGid().toString();
                        } else if (columnName.equals(CROSS_FEMALE_PREF_NAME)) {
                            Name prefName = germplasmDataManager.getPreferredNameByGID(femaleParent.getGid());
                            if (prefName != null) {
                                value = prefName.getNval();
                            }
                        }
                        targetTable.getItem(itemId).getItemProperty(columnName).setValue(value);
                    }
                }

                //To trigger TableFieldFactory (fix for truncated data)
                doFixForTruncatedDataInEditableTable();
                
                markHasChangesFlags(fromAddColumn);
                
            } catch (MiddlewareQueryException e) {
                LOG.error("Error in filling with Cross Female Info values.", e);
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
        
    } 
    
    /**
     * Save erases all values on the table, including the added columns, use this to re-populate it with data
     */
    public void populateAddedColumns(){
        for(String propertyId: AddColumnContextMenu.ADDABLE_PROPERTY_IDS){
            if(propertyExists(propertyId)){
                if(propertyId.equals(AddColumnContextMenu.PREFERRED_ID)){
                    setPreferredIdColumnValues(false);
                } else if(propertyId.equals(AddColumnContextMenu.PREFERRED_NAME)){
                    setPreferredNameColumnValues(false);
                } else if(propertyId.equals(AddColumnContextMenu.GERMPLASM_DATE)){
                    setGermplasmDateColumnValues(false);
                } else if(propertyId.equals(AddColumnContextMenu.LOCATIONS)){
                    setLocationColumnValues(false);
                } else if(propertyId.equals(AddColumnContextMenu.METHOD_NAME)){
                    setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NAME);
                } else if(propertyId.equals(AddColumnContextMenu.METHOD_ABBREV)){
                    setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_ABBREV);
                } else if(propertyId.equals(AddColumnContextMenu.METHOD_NUMBER)){
                    setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NUMBER);
                } else if(propertyId.equals(AddColumnContextMenu.METHOD_GROUP)){
                    setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_GROUP);
                } else if(propertyId.equals(AddColumnContextMenu.CROSS_FEMALE_GID)){
                    setCrossFemaleInfoColumnValues(false, AddColumnContextMenu.CROSS_FEMALE_GID);
                } else if(propertyId.equals(AddColumnContextMenu.CROSS_FEMALE_PREF_NAME)){
                    setCrossFemaleInfoColumnValues(false, AddColumnContextMenu.CROSS_FEMALE_PREF_NAME);
                } else if(propertyId.equals(AddColumnContextMenu.CROSS_MALE_GID)){
                	setCrossMaleGIDColumnValues(false);
                } else if(propertyId.equals(AddColumnContextMenu.CROSS_MALE_PREF_NAME)){
                	setCrossMalePrefNameColumnValues(false);
                }

            }
        }
    }
    
    
    /**
     * This has to be called after the list entries has been saved, because it'll need the germplasmListEntryId
     * @return
     */
    public List<ListDataInfo> getListDataCollectionFromTable(Table table){
        List<ListDataInfo> listDataCollection = new ArrayList<ListDataInfo>();
        List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(table);
        
        for(Object itemId : table.getItemIds()){
        	Item item = table.getItem(itemId);
        	List<ListDataColumn> columns = new ArrayList<ListDataColumn>();
	        for(String propertyId : propertyIds){
	        	if(ADDABLE_PROPERTY_IDS.contains(propertyId)){
	        		//System.out.println("Columns TO Save: " + propertyId);
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
        if(propertyId.equals(AddColumnContextMenu.PREFERRED_ID)){
            addPreferredIdColumn();
        } else if(propertyId.equals(AddColumnContextMenu.PREFERRED_NAME)){
            addPreferredNameColumn();
        } else if(propertyId.equals(AddColumnContextMenu.GERMPLASM_DATE)){
            addGermplasmDateColumn();
        } else if(propertyId.equals(AddColumnContextMenu.LOCATIONS)){
            addLocationColumn();
        } else if(propertyId.equals(AddColumnContextMenu.METHOD_NAME)){
            addMethodNameColumn();
        } else if(propertyId.equals(AddColumnContextMenu.METHOD_ABBREV)){
            addMethodAbbrevColumn();
        } else if(propertyId.equals(AddColumnContextMenu.METHOD_NUMBER)){
            addMethodNumberColumn();
        } else if(propertyId.equals(AddColumnContextMenu.METHOD_GROUP)){
            addMethodGroupColumn();
        } else if(propertyId.equals(AddColumnContextMenu.CROSS_FEMALE_GID)){
            addCrossFemaleGidColumn();
        } else if(propertyId.equals(AddColumnContextMenu.CROSS_FEMALE_PREF_NAME)){
            addCrossFemalePrefNameColumn();
        } else if(propertyId.equals(AddColumnContextMenu.CROSS_MALE_GID)){
        	addCrossMaleGIDColumn();
        } else if(propertyId.equals(AddColumnContextMenu.CROSS_MALE_PREF_NAME)){
        	addCrossMalePrefNameColumn();
        }

    }
    
    public void showHideAddColumnMenu(boolean visible) {
        addColumnItem.setVisible(visible);
        sourceContextMenu.requestRepaint();
    }
    
    public void setEnabled(Boolean state){
    	addColumnItem.setEnabled(state);
    }

    public void setVisible(Boolean state){
    	addColumnItem.setVisible(state);
    }
}

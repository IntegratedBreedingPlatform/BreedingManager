package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Table;

@Configurable
public class AddColumnContextMenu implements InternationalizableComponent  {
    private final class SourceContextMenuClickListener implements ContextMenu.ClickListener {
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
	}

	private static final Logger LOG = LoggerFactory.getLogger(AddColumnContextMenu.class);

    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private OntologyDataManager ontologyDataManager;

    private ListTabComponent listDetailsComponent = null;
    
    @SuppressWarnings("unused")
	private ComponentContainer cssLayoutSource;
    
    private final String gidPropertyId;
    private final Table targetTable;
    
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
    
    private static final String ADD_COLUMN_MENU = "Add Column";
    private static final String FILL_WITH_PREFERRED_ID = "Fill with Preferred ID";
    private static final String FILL_WITH_PREFERRED_NAME = "Fill with Preferred Name";
    private static final String FILL_WITH_GERMPLASM_DATE = "Fill with Germplasm Dates";
    private static final String FILL_WITH_LOCATION = "Fill with Location";
    private static final String FILL_WITH_METHOD_INFO = "Fill with Breeding Method Information";
    private static final String FILL_WITH_METHOD_NAME = "Fill with Breeding Method Name";
    private static final String FILL_WITH_METHOD_ABBREV = "Fill with Breeding Method Abbreviation";
    private static final String FILL_WITH_METHOD_NUMBER = "Fill with Breeding Method Number";
    private static final String FILL_WITH_METHOD_GROUP = "Fill with Breeding Method Group";
    private static final String FILL_WITH_CROSS_FEMALE_INFO = "Fill with Cross-Female Information";
    private static final String FILL_WITH_CROSS_FEMALE_GID = "Fill with Cross-Female GID";
    private static final String FILL_WITH_CROSS_FEMALE_PREF_NAME = "Fill with Cross-Female Preferred Name";
    private static final String FILL_WITH_CROSS_MALE_INFO = "Fill with Cross-Male Information";
    private static final String FILL_WITH_CROSS_MALE_GID = "Fill with Cross-Male GID";
    private static final String FILL_WITH_CROSS_MALE_PREF_NAME = "Fill with Cross-Male Preferred Name";
    
    @SuppressWarnings("unused")
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
        this.gidPropertyId = gid;
        this.targetTable = targetTable;
        this.sourceContextMenu = sourceContextMenu;
        
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
        this.gidPropertyId = gid;
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
        this.gidPropertyId = gid;
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
        
        sourceContextMenu.addListener(new SourceContextMenuClickListener());
    }
    
    public void initializeAddableProperties(){
    	
    	ADDABLE_PROPERTY_IDS = new ArrayList<String>();
    	
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.PREFERRED_ID.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.PREFERRED_NAME.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.GERMPLASM_DATE.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.GERMPLASM_LOCATION.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_NAME.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_NUMBER.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_GROUP.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_FEMALE_GID.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_MALE_GID.getName());
    	ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
    }
    
    public void refreshAddColumnMenu() {
      //Check if columns already exist in the table
        if(propertyExists(ColumnLabels.PREFERRED_ID.getName())){
            menuFillWithPreferredId.setEnabled(false);
        } else {
            menuFillWithPreferredId.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.PREFERRED_NAME.getName())){
            menuFillWithPreferredName.setEnabled(false);
        } else {
            menuFillWithPreferredName.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.GERMPLASM_DATE.getName())){
            menuFillWithGermplasmDate.setEnabled(false);
        } else {
            menuFillWithGermplasmDate.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.GERMPLASM_LOCATION.getName())){
            menuFillWithLocations.setEnabled(false);
        } else {
            menuFillWithLocations.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.BREEDING_METHOD_NAME.getName())){
            menuFillWithMethodName.setEnabled(false);
        } else {
            menuFillWithMethodName.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())){
            menuFillWithMethodAbbrev.setEnabled(false);
        } else {
            menuFillWithMethodAbbrev.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())){
            menuFillWithMethodNumber.setEnabled(false);
        } else {
            menuFillWithMethodNumber.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
            menuFillWithMethodGroup.setEnabled(false);
        } else {
            menuFillWithMethodGroup.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.BREEDING_METHOD_NAME.getName()) && propertyExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())
                && propertyExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName()) && propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
            menuFillWithMethodInfo.setEnabled(false);
        } else {
            menuFillWithMethodInfo.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.CROSS_FEMALE_GID.getName())){
            menuFillWithCrossFemaleGID.setEnabled(false);
        } else {
            menuFillWithCrossFemaleGID.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
            menuFillWithCrossFemalePrefName.setEnabled(false);
        } else {
            menuFillWithCrossFemalePrefName.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.CROSS_FEMALE_GID.getName()) && propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
            menuFillWithCrossFemaleInfo.setEnabled(false);
        } else {
            menuFillWithCrossFemaleInfo.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.CROSS_MALE_GID.getName())){
            menuFillWithCrossMaleGID.setEnabled(false);
        } else {
            menuFillWithCrossMaleGID.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
            menuFillWithCrossMalePrefName.setEnabled(false);
        } else {
            menuFillWithCrossMalePrefName.setEnabled(true);
        }
        
        if(propertyExists(ColumnLabels.CROSS_MALE_GID.getName()) && propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
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
        	listDetailsComponent.getListComponent().setHasUnsavedChanges(true); 
        }
        
        //mark flag that changes have been made in buildNewListTable
        if(buildNewListComponent != null){ 
        	buildNewListComponent.setHasUnsavedChanges(true);
        }
    }
    
    private void addPreferredIdColumn(){
        if(!propertyExists(ColumnLabels.PREFERRED_ID.getName())){
            targetTable.addContainerProperty(ColumnLabels.PREFERRED_ID.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.PREFERRED_ID.getName(), ColumnLabels.PREFERRED_ID.getTermNameFromOntology(ontologyDataManager));
            setPreferredIdColumnValues(true);
        }
    }
    
    public void setPreferredIdColumnValues(boolean fromAddColumn){
        if(propertyExists(ColumnLabels.PREFERRED_ID.getName())){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    String preferredID = "";
                    Name name = germplasmDataManager.getPreferredIdByGID(gid);
                    if(name!=null && name.getNval()!=null) {
                        preferredID = name.getNval();
                    }
                    targetTable.getItem(itemId).getItemProperty(ColumnLabels.PREFERRED_ID.getName()).setValue(preferredID);
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
        if(!propertyExists(ColumnLabels.PREFERRED_NAME.getName())){
            targetTable.addContainerProperty(ColumnLabels.PREFERRED_NAME.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.PREFERRED_NAME.getName(), ColumnLabels.PREFERRED_NAME.getTermNameFromOntology(ontologyDataManager));
            setPreferredNameColumnValues(true);
        }
    }
    
    public void setPreferredNameColumnValues(boolean fromAddColumn){
        if(propertyExists(ColumnLabels.PREFERRED_NAME.getName())){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    
                    String preferredName = "";
                    if(germplasmDataManager.getPreferredNameByGID(gid)!=null && germplasmDataManager.getPreferredNameByGID(gid).getNval()!=null) {
                        preferredName = germplasmDataManager.getPreferredNameByGID(gid).getNval();
                    }
                    targetTable.getItem(itemId).getItemProperty(ColumnLabels.PREFERRED_NAME.getName()).setValue(preferredName);
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
        if(!propertyExists(ColumnLabels.GERMPLASM_DATE.getName())){
            targetTable.addContainerProperty(ColumnLabels.GERMPLASM_DATE.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.GERMPLASM_DATE.getName(), ColumnLabels.GERMPLASM_DATE.getTermNameFromOntology(ontologyDataManager));
            //can create separate method for adding container property and the actual setting of column values,
            //      so that the middleware call below can be called only once without having the gids become null
            setGermplasmDateColumnValues(true);
        }
    }
    
    public void setGermplasmDateColumnValues(boolean fromAddColumn){
        if(propertyExists(ColumnLabels.GERMPLASM_DATE.getName())){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    
                    List<Integer> gids = new ArrayList<Integer>();
                    gids.add(gid);
                    
                    //can make better use of the middleware method by just calling it once and not have it inside a loop
                    Map<Integer,Integer> germplasmGidDateMap = germplasmDataManager.getGermplasmDatesByGids(gids);
                    
                    if(germplasmGidDateMap.get(gid)==null) {
                        targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_DATE.getName()).setValue("");
                    } else {
                        targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_DATE.getName()).setValue(germplasmGidDateMap.get(gid));
                    }
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
        if(!propertyExists(ColumnLabels.GERMPLASM_LOCATION.getName())){
            targetTable.addContainerProperty(ColumnLabels.GERMPLASM_LOCATION.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName(), ColumnLabels.GERMPLASM_LOCATION.getTermNameFromOntology(ontologyDataManager));
            setLocationColumnValues(true);
        }
    }
    
    public void setLocationColumnValues(boolean fromAddColumn){
        if(propertyExists(ColumnLabels.GERMPLASM_LOCATION.getName())){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                final Map<Integer, String> allLocationNamesMap = new HashMap<Integer, String>();
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    
                    List<Integer> gids = new ArrayList<Integer>();
                    gids.add(gid);
                    
                    Map<Integer, String> locationNamesMap = germplasmDataManager.getLocationNamesByGids(gids);
                    allLocationNamesMap.putAll(locationNamesMap);
                    
                    if(locationNamesMap.get(gid)==null) {
                        targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).setValue("");
                    } else {
                        targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).setValue(locationNamesMap.get(gid));
                    }
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
        if(!propertyExists(ColumnLabels.BREEDING_METHOD_NAME.getName())){
            targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName(), ColumnLabels.BREEDING_METHOD_NAME.getTermNameFromOntology(ontologyDataManager));
            setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_NAME.getName());
        }
    }
    
    private void addMethodAbbrevColumn(){
        if(!propertyExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())){
            targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(), ColumnLabels.BREEDING_METHOD_ABBREVIATION.getTermNameFromOntology(ontologyDataManager));
            setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
        }
    }
    
    private void addMethodNumberColumn(){
        if(!propertyExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())){
            targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NUMBER.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NUMBER.getName(), ColumnLabels.BREEDING_METHOD_NUMBER.getTermNameFromOntology(ontologyDataManager));
            setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_NUMBER.getName());
        }
    }
    
    private void addMethodGroupColumn(){
        if(!propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
            targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_GROUP.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_GROUP.getName(), ColumnLabels.BREEDING_METHOD_GROUP.getTermNameFromOntology(ontologyDataManager));
            setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_GROUP.getName());
        }
    }
    
    public void setMethodInfoColumnValues(boolean fromAddColumn, String columnName){
        if(propertyExists(columnName)){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                final Map<Integer, Object> allMethodsMap = new HashMap<Integer, Object>();
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    
                    List<Integer> gids = new ArrayList<Integer>();
                    gids.add(gid);
                    
                    Map<Integer, Object> methodsMap = germplasmDataManager.getMethodsByGids(gids);
                    allMethodsMap.putAll(methodsMap);
                    
                    if(methodsMap.get(gid)==null) {
                        targetTable.getItem(itemId).getItemProperty(columnName).setValue("");
                    } else {
                        String value = "";
                        
                        if (columnName.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
                            value = ((Method)methodsMap.get(gid)).getMname();
                        } else if (columnName.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
                            value = ((Method)methodsMap.get(gid)).getMcode();
                        } else if (columnName.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
                            value = ((Method)methodsMap.get(gid)).getMid().toString();
                        } else if (columnName.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
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
        if(!propertyExists(ColumnLabels.CROSS_MALE_GID.getName())){
            targetTable.addContainerProperty(ColumnLabels.CROSS_MALE_GID.getName(), String.class, "-");
            targetTable.setColumnHeader(ColumnLabels.CROSS_MALE_GID.getName(), ColumnLabels.CROSS_MALE_GID.getTermNameFromOntology(ontologyDataManager));
            setCrossMaleGIDColumnValues(true);
        }
    }
    
    public void setCrossMaleGIDColumnValues(boolean fromAddColumn){
        if(propertyExists(ColumnLabels.CROSS_MALE_GID.getName())){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    
                    Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
                    
                    if(germplasm != null){
	                    if(germplasm.getGnpgs() >= 2) {
	                    	if(germplasm.getGpid2() != null && germplasm.getGpid2() != 0){
	                    		targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName()).setValue(germplasm.getGpid2().toString());
	                    	} else{
	                    		targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName()).setValue("-");
	                    	}
	                    } else {
	                        targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName()).setValue("-");
	                    }
                    } else{
                    	targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName()).setValue("-");
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
        if(!propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
            targetTable.addContainerProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), String.class, "-");
            targetTable.setColumnHeader(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), ColumnLabels.CROSS_MALE_PREFERRED_NAME.getTermNameFromOntology(ontologyDataManager));
            setCrossMalePrefNameColumnValues(true);
        }
    }
    
    public void setCrossMalePrefNameColumnValues(boolean fromAddColumn){
        if(propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                Map<Integer, List<Integer>> gidToItemIdMap = new HashMap<Integer, List<Integer>>();
                List<Integer> gidsToUseForQuery = new ArrayList<Integer>();
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    
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
	                    } else {
	                        targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).setValue("-");
	                    }
                    } else{
                    	targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).setValue("-");
                    }
                }
                
                if(!gidsToUseForQuery.isEmpty()){
                	Map<Integer, String> gidToNameMap = germplasmDataManager.getPreferredNamesByGids(gidsToUseForQuery);
                	
                	for(Integer gid : gidToNameMap.keySet()){
                		String prefName = gidToNameMap.get(gid);
                		List<Integer> itemIdsInMap = gidToItemIdMap.get(gid);
                		for(Integer itemId : itemIdsInMap){
                			targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).setValue(prefName);
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
        if(!propertyExists(ColumnLabels.CROSS_FEMALE_GID.getName())){
            targetTable.addContainerProperty(ColumnLabels.CROSS_FEMALE_GID.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.CROSS_FEMALE_GID.getName(), ColumnLabels.CROSS_FEMALE_GID.getTermNameFromOntology(ontologyDataManager));
            setCrossFemaleInfoColumnValues(true, ColumnLabels.CROSS_FEMALE_GID.getName());
        }
    }
    
    private void addCrossFemalePrefNameColumn(){
        if(!propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
            targetTable.addContainerProperty(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), String.class, "");
            targetTable.setColumnHeader(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getTermNameFromOntology(ontologyDataManager));
            setCrossFemaleInfoColumnValues(true, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
        }
    }
    
    public void setCrossFemaleInfoColumnValues(boolean fromAddColumn, String columnName){
        if(propertyExists(columnName)){
            try {
                List<Integer> itemIds = getItemIds(targetTable);
                
                for(Integer itemId: itemIds){
                    Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString());
                    
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
                        if (columnName.equals(ColumnLabels.CROSS_FEMALE_GID.getName())) {
                            value = femaleParent.getGid().toString();
                        } else if (columnName.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
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
            gids.add(Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(gidPropertyId).getValue()).getCaption().toString()));
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
        //do nothing
    } 
    
    /**
     * Save erases all values on the table, including the added columns, use this to re-populate it with data
     */
    public void populateAddedColumns(){
        for(String propertyId: AddColumnContextMenu.ADDABLE_PROPERTY_IDS){
            if(propertyExists(propertyId)){
                if(propertyId.equals(ColumnLabels.PREFERRED_ID.getName())){
                    setPreferredIdColumnValues(false);
                } else if(propertyId.equals(ColumnLabels.PREFERRED_NAME.getName())){
                    setPreferredNameColumnValues(false);
                } else if(propertyId.equals(ColumnLabels.GERMPLASM_DATE.getName())){
                    setGermplasmDateColumnValues(false);
                } else if(propertyId.equals(ColumnLabels.GERMPLASM_LOCATION.getName())){
                    setLocationColumnValues(false);
                } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())){
                    setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_NAME.getName());
                } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())){
                    setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
                } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())){
                    setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_NUMBER.getName());
                } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
                    setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_GROUP.getName());
                } else if(propertyId.equals(ColumnLabels.CROSS_FEMALE_GID.getName())){
                    setCrossFemaleInfoColumnValues(false, ColumnLabels.CROSS_FEMALE_GID.getName());
                } else if(propertyId.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
                    setCrossFemaleInfoColumnValues(false, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
                } else if(propertyId.equals(ColumnLabels.CROSS_MALE_GID.getName())){
                	setCrossMaleGIDColumnValues(false);
                } else if(propertyId.equals(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
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
	        		if(item.getItemProperty(propertyId).getValue()!=null) {
                        columns.add(new ListDataColumn(propertyId, item.getItemProperty(propertyId).getValue().toString()));
                    } else {
                        columns.add(new ListDataColumn(propertyId, null));
                    }
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
        if(propertyId.equals(ColumnLabels.PREFERRED_ID.getName())){
            addPreferredIdColumn();
        } else if(propertyId.equals(ColumnLabels.PREFERRED_NAME.getName())){
            addPreferredNameColumn();
        } else if(propertyId.equals(ColumnLabels.GERMPLASM_DATE.getName())){
            addGermplasmDateColumn();
        } else if(propertyId.equals(ColumnLabels.GERMPLASM_LOCATION.getName())){
            addLocationColumn();
        } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())){
            addMethodNameColumn();
        } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())){
            addMethodAbbrevColumn();
        } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())){
            addMethodNumberColumn();
        } else if(propertyId.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
            addMethodGroupColumn();
        } else if(propertyId.equals(ColumnLabels.CROSS_FEMALE_GID.getName())){
            addCrossFemaleGidColumn();
        } else if(propertyId.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
            addCrossFemalePrefNameColumn();
        } else if(propertyId.equals(ColumnLabels.CROSS_MALE_GID.getName())){
        	addCrossMaleGIDColumn();
        } else if(propertyId.equals(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
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

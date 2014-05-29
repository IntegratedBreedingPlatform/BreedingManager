package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.AdditionalDetailsCrossNameComponent;
import org.generationcp.breeding.manager.listmanager.BuildNewListComponent;
import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListTabComponent;
import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
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
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class FillWith implements InternationalizableComponent  {
	private static final Logger LOG = LoggerFactory.getLogger(FillWith.class);

	//@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
    @Autowired
    private GermplasmDataManager germplasmDataManager;

    private ListManagerTreeMenu listManagerTreeMenu;
    private AbstractLayout parentLayout;
    
    private Table targetTable;
    private String GIDPropertyId;
    private List<String> filledWithPropertyIds;
    
	private ContextMenu fillWithMenu;
	private ContextMenuItem menuFillWithEmpty;
	private ContextMenuItem menuFillWithGermplasmDate;
	private ContextMenuItem menuFillWithPrefName;
	private ContextMenuItem menuFillWithPrefID;
	private ContextMenuItem menuFillWithAttribute;
	private ContextMenuItem menuFillWithLocationName;
	private ContextMenuItem menuFillWithBreedingMethodInfo;
	private ContextMenuItem menuFillWithBreedingMethodName;
	private ContextMenuItem menuFillWithBreedingMethodGroup;
	private ContextMenuItem menuFillWithBreedingMethodNumber;
	private ContextMenuItem menuFillWithBreedingMethodAbbreviation;
	private ContextMenuItem menuFillWithCrossFemaleInformation;
	private ContextMenuItem menuFillWithCrossFemaleGID;
	private ContextMenuItem menuFillWithCrossFemalePreferredName;
	private ContextMenuItem menuFillWithCrossMaleInformation;
	private ContextMenuItem menuFillWithCrossMaleGID;
	private ContextMenuItem menuFillWithCrossMalePreferredName;
	private ContextMenuItem menuFillWithCrossExpansion;
	private ContextMenuItem menuFillWithSequenceNumber;
	
	private GermplasmDetailModel germplasmDetail;
    
    private Integer crossExpansionLevel = Integer.valueOf(1);
    
    private BuildNewListComponent buildNewListComponent;
    
    private ListTabComponent listDetailsComponent;
    
    private org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent buildListComponent;
    
    public FillWith(String GIDPropertyId, Table targetTable){
    	this.GIDPropertyId = GIDPropertyId;
    	this.targetTable = targetTable;
    }
    
	/**
	 * Add Fill With context menu to a table
	 * @param listManagerTreeMenu - contextMenu will attach to this
	 * @param targetTable - table where data will be manipulated
	 * @param GIDPropertyId - property of GID (button with GID as caption) on that table
	 * @param propertyIdsContextMenuAvailableTo - list of property ID's where context menu will be available for "right clicking"
	 */
    public FillWith(ListManagerTreeMenu listManagerTreeMenu,final SimpleResourceBundleMessageSource messageSource, final Table targetTable, String GIDPropertyId){
    	this.GIDPropertyId = GIDPropertyId;
    	this.targetTable = targetTable;
    	this.listManagerTreeMenu = listManagerTreeMenu;
    	this.messageSource = messageSource;
    	this.filledWithPropertyIds = new ArrayList<String>();
    	
    	setupContextMenu();
    }
    
    
	/**
	 * Add Fill With context menu to a table
	 * @param parentLayout - contextMenu will attach to this
	 * @param targetTable - table where data will be manipulated
	 * @param GIDPropertyId - property of GID (button with GID as caption) on that table
	 * @param propertyIdsContextMenuAvailableTo - list of property ID's where context menu will be available for "right clicking"
	 */
    public FillWith(AbstractLayout parentLayout,final SimpleResourceBundleMessageSource messageSource, final Table targetTable, String GIDPropertyId){
    	this.GIDPropertyId = GIDPropertyId;
    	this.targetTable = targetTable;
    	this.parentLayout = parentLayout;
    	this.messageSource = messageSource;
    	this.filledWithPropertyIds = new ArrayList<String>();
    	
    	setupContextMenu();
    }
    
	/**
	 * Add Fill With context menu to a table
	 * @param parentLayout - contextMenu will attach to this
	 * @param targetTable - table where data will be manipulated
	 * @param GIDPropertyId - property of GID (button with GID as caption) on that table
	 * @param propertyIdsContextMenuAvailableTo - list of property ID's where context menu will be available for "right clicking"
	 * @param fromBuildNewList - specify if the creation is from BuildNewListComponent
	 */
    public FillWith(AbstractLayout parentLayout,final SimpleResourceBundleMessageSource messageSource, final Table targetTable, 
    		String GIDPropertyId, boolean fromBuildNewList){
    	this.GIDPropertyId = GIDPropertyId;
    	this.targetTable = targetTable;
    	this.parentLayout = parentLayout;
    	this.messageSource = messageSource;
    	this.filledWithPropertyIds = new ArrayList<String>();
    	
    	if(fromBuildNewList){
    		buildNewListComponent = ((BuildNewListComponent) parentLayout);
    	}
    	
    	setupContextMenu();
    }
    
    public FillWith(ListTabComponent listDetailsComponent, AbstractLayout parentLayout
    		,final SimpleResourceBundleMessageSource messageSource, final Table targetTable, String GIDPropertyId){
    	this.GIDPropertyId = GIDPropertyId;
    	this.targetTable = targetTable;
    	this.parentLayout = parentLayout;
    	this.messageSource = messageSource;
    	this.filledWithPropertyIds = new ArrayList<String>();
    	this.listDetailsComponent = listDetailsComponent;
    	
    	setupContextMenu();
    }
    
    public FillWith(org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent buildListComponent, final SimpleResourceBundleMessageSource messageSource
    		, final Table targetTable, String GIDPropertyId){
    	this.GIDPropertyId = GIDPropertyId;
    	this.targetTable = targetTable;
    	this.parentLayout = buildListComponent;
    	this.messageSource = messageSource;
    	this.filledWithPropertyIds = new ArrayList<String>();
    	this.buildListComponent = buildListComponent;
    	
    	setupContextMenu();
    }
    
    public void fillWith(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
		 if(propertyId.equals(AddColumnContextMenu.PREFERRED_ID)){
			 fillWithPreferredID(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.PREFERRED_NAME)) {
			 fillWithPreferredName(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.GERMPLASM_DATE)) {
			 fillWithGermplasmDate(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.LOCATIONS)) {
			 fillWithLocation(table, propertyId, onlyFillWithThoseHavingEmptyValues);			 
		 } else if(propertyId.equals(AddColumnContextMenu.METHOD_NAME)) {
			 fillWithMethodName(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.METHOD_ABBREV)) {			 
			 fillWithMethodAbbreviation(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.METHOD_NUMBER)) {			 
			 fillWithMethodNumber(table, propertyId, onlyFillWithThoseHavingEmptyValues);			 
		 } else if(propertyId.equals(AddColumnContextMenu.METHOD_GROUP)) {			 
			 fillWithMethodGroup(table, propertyId, onlyFillWithThoseHavingEmptyValues);			 
		 } else if(propertyId.equals(AddColumnContextMenu.CROSS_FEMALE_GID)) {			 
			 fillWithCrossFemaleGID(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.CROSS_FEMALE_PREF_NAME)) {			 
			 fillWithCrossFemalePreferredName(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.CROSS_MALE_GID)) {			 
			 fillWithCrossMaleGID(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		 } else if(propertyId.equals(AddColumnContextMenu.CROSS_MALE_PREF_NAME)) {			 
			 fillWithCrossMalePreferredName(table, propertyId, onlyFillWithThoseHavingEmptyValues);	 
		 }
    }
    
    private void setupContextMenu(){
    	
	   	 fillWithMenu = new ContextMenu();
		 
	   	 menuFillWithEmpty = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_EMPTY));
	   	 menuFillWithLocationName = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_LOCATION_NAME));
	   	 menuFillWithPrefID = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_PREF_ID));
	   	 menuFillWithGermplasmDate = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_GERMPLASM_DATE));
	   	 menuFillWithPrefName = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_PREF_NAME));
	   	 menuFillWithAttribute = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_ATTRIBUTE));
	   	 
	   	 menuFillWithBreedingMethodInfo = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_INFO));
	   	 menuFillWithBreedingMethodName = menuFillWithBreedingMethodInfo.addItem(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NAME));
	   	 menuFillWithBreedingMethodAbbreviation = menuFillWithBreedingMethodInfo.addItem(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_ABBREVIATION));
	   	 menuFillWithBreedingMethodNumber = menuFillWithBreedingMethodInfo.addItem(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NUMBER));
	   	 menuFillWithBreedingMethodGroup = menuFillWithBreedingMethodInfo.addItem(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_GROUP));
	
	   	 menuFillWithCrossFemaleInformation = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_INFORMATION));
	   	 menuFillWithCrossFemaleGID = menuFillWithCrossFemaleInformation.addItem(messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_GID));
	   	 menuFillWithCrossFemalePreferredName = menuFillWithCrossFemaleInformation.addItem(messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_PREFERRED_NAME));
	   	 
	   	 menuFillWithCrossMaleInformation = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_INFORMATION));
	   	 menuFillWithCrossMaleGID = menuFillWithCrossMaleInformation.addItem(messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_GID));
	   	 menuFillWithCrossMalePreferredName = menuFillWithCrossMaleInformation.addItem(messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_PREFERRED_NAME));
	   	 
	   	 menuFillWithCrossExpansion = fillWithMenu.addItem(messageSource.getMessage(Message.FILL_WITH_CROSS_EXPANSION));
	   	 menuFillWithSequenceNumber = fillWithMenu.addItem("Fill with Sequence Number");
	   			 
	   	 fillWithMenu.addListener(new ContextMenu.ClickListener() {
	   		private static final long serialVersionUID = -2384037190598803030L;
	
	   			public void contextItemClick(ClickEvent event) {
		   			 // Get reference to clicked item
		   			 ContextMenuItem clickedItem = event.getClickedItem();
		   			 
		   			 trackFillWith((String) fillWithMenu.getData());
		   			 
		   			 if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_EMPTY))){
		   				 fillWithEmpty(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_LOCATION_NAME))){
		   				 fillWithLocation(targetTable);
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_GERMPLASM_DATE))){
		   				 fillWithGermplasmDate(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_PREF_NAME))){
		   				 fillWithPreferredName(targetTable, (String) fillWithMenu.getData());
		   			 }else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_PREF_ID))){
		   				 fillWithPreferredID(targetTable, (String) fillWithMenu.getData());
		   			 }else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_ATTRIBUTE))){
		   			     fillWithAttribute(targetTable, (String) fillWithMenu.getData());
	   				 }else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NAME))){
		   				 fillWithMethodName(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_ABBREVIATION))){
		   				 fillWithMethodAbbreviation(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NUMBER))){
		   				 fillWithMethodNumber(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_GROUP))){
		   				 fillWithMethodGroup(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_GID))){
		   				 fillWithCrossFemaleGID(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_PREFERRED_NAME))){
		   				 fillWithCrossFemalePreferredName(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_GID))){
		   				 fillWithCrossMaleGID(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_PREFERRED_NAME))){
		   				 fillWithCrossMalePreferredName(targetTable, (String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_CROSS_EXPANSION))){
		   				 displayExpansionLevelPopupWindow((String) fillWithMenu.getData());
		   			 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.FILL_WITH_SEQUENCE_NUMBER))){
		   				 displaySequenceNumberPopupWindow((String) fillWithMenu.getData());
		   			 }
	   			}
	   	 });
	   	 
	   	 if(parentLayout!=null){
	   		 parentLayout.addComponent(fillWithMenu);
	   	 } else {
	   		 listManagerTreeMenu.addComponent(fillWithMenu);
	   	 }
	   	 
	   	 targetTable.addListener(new Table.HeaderClickListener() {
        	private static final long serialVersionUID = 4792602001489368804L;

			public void headerClick(HeaderClickEvent event) {
        		if(event.getButton() == HeaderClickEvent.BUTTON_RIGHT){
        			String column = (String) event.getPropertyId();
        			fillWithMenu.setData(column);
        			if(column.equals(ListDataTablePropertyID.ENTRY_CODE.getName())){
            			menuFillWithLocationName.setVisible(false);
            			menuFillWithCrossExpansion.setVisible(false);
            			setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
            			fillWithMenu.show(event.getClientX(), event.getClientY());
            		} else if(column.equals(ListDataTablePropertyID.SEED_SOURCE.getName())){
            			menuFillWithLocationName.setVisible(true);
            			menuFillWithCrossExpansion.setVisible(false);
            			setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
            			fillWithMenu.show(event.getClientX(), event.getClientY());
            		} else if(column.equals(ListDataTablePropertyID.GROUP_NAME.getName()) || column.equals(ListDataTablePropertyID.PARENTAGE.getName())){
            			setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(false);
            			menuFillWithLocationName.setVisible(false);
            			menuFillWithCrossExpansion.setVisible(true);
            			fillWithMenu.show(event.getClientX(), event.getClientY());
            		}
        		}
        	}
        });
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
	
	private void markHasChangesFlagsAndToggleTableEditable(Table table){
		//mark flag that changes have been made in listDataTable
		if(listManagerTreeMenu != null){ 
			listManagerTreeMenu.setChanged(true); 
		}
	       
	    //mark flag that changes have been made in buildNewListTable
	    if(buildNewListComponent != null){ 
	    	buildNewListComponent.setHasChanges(true); 
	    }
	    
	    if(listDetailsComponent != null){
	    	listDetailsComponent.setChanged(true);
	    }
	    
	    if(buildListComponent != null){
	    	buildListComponent.setChanged(true);
	    }
	    
	    //To trigger TableFieldFactory (fix for truncated data)
		if(table.isEditable()){
			table.setEditable(false);
			table.setEditable(true);
		}
	}
	
    public void fillWithEmpty(Table table, String propertyId){
       List<Integer> itemIds = getItemIds(table);
       for(Integer itemId: itemIds){
           table.getItem(itemId).getItemProperty(propertyId).setValue("");
       }
       
       markHasChangesFlagsAndToggleTableEditable(table);	
    }
    
    public void fillWithAttribute(Table table, String propertyId) {
        Window mainWindow = table.getWindow();
        Window attributeWindow = new FillWithAttributeWindow(listManagerTreeMenu, table, GIDPropertyId, propertyId, messageSource, buildNewListComponent, listDetailsComponent
        		, buildListComponent);
        attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
        mainWindow.addWindow(attributeWindow);
    }
    
    
    public void fillWithGermplasmDate(Table table, String propertyId){
    	fillWithGermplasmDate(table, propertyId, false);
    }
    
	public void fillWithGermplasmDate(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
	   try {
		   List<Integer> itemIds = getItemIds(table);
		   List<Integer> gids = getGidsFromTable(table);
		   Map<Integer,Integer> germplasmGidDateMap = germplasmDataManager.getGermplasmDatesByGids(gids);
		   
		   for(Integer itemId: itemIds){
			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
				   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
				   table.getItem(itemId).getItemProperty(propertyId).setValue(germplasmGidDateMap.get(gid));
			   } 
		   }
		   
		   markHasChangesFlagsAndToggleTableEditable(table);
	       
	   } catch (MiddlewareQueryException e) {
		   e.printStackTrace();
	   }
    }
 
	public void fillWithMethodName(Table table, String propertyId){
		fillWithMethodName(table, propertyId, false);
	}
	
    public void fillWithMethodName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
	   try {
		   List<Integer> itemIds = getItemIds(table);
		   List<Integer> gids = getGidsFromTable(table);
		   Map<Integer,Object> germplasmGidDateMap = germplasmDataManager.getMethodsByGids(gids);
		   
		   for(Integer itemId: itemIds){
			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
				   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
				   table.getItem(itemId).getItemProperty(propertyId).setValue(((Method) germplasmGidDateMap.get(gid)).getMname().toString());
			   } 
		   }
		   
		   markHasChangesFlagsAndToggleTableEditable(table);
	       
	   } catch (MiddlewareQueryException e) {
		   e.printStackTrace();
	   }
    }    

    public void fillWithMethodAbbreviation(Table table, String propertyId){
    	fillWithMethodAbbreviation(table, propertyId, false);
    }
    
    public void fillWithMethodAbbreviation(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
	   try {
		   List<Integer> itemIds = getItemIds(table);
		   List<Integer> gids = getGidsFromTable(table);
		   Map<Integer,Object> germplasmGidDateMap = germplasmDataManager.getMethodsByGids(gids);
		   
		   for(Integer itemId: itemIds){
			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
				   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
				   table.getItem(itemId).getItemProperty(propertyId).setValue(((Method) germplasmGidDateMap.get(gid)).getMcode().toString());
			   }
		   }
		   
		   markHasChangesFlagsAndToggleTableEditable(table);

	   } catch (MiddlewareQueryException e) {
		   e.printStackTrace();
	   }
    }   
    
    
    public void fillWithMethodNumber(Table table, String propertyId){
    	fillWithMethodNumber(table, propertyId, false);
    }
    
    public void fillWithMethodNumber(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
	   try {
		   List<Integer> itemIds = getItemIds(table);
		   List<Integer> gids = getGidsFromTable(table);
		   Map<Integer,Object> germplasmGidDateMap = germplasmDataManager.getMethodsByGids(gids);
		   
		   for(Integer itemId: itemIds){
			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
				   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
				   table.getItem(itemId).getItemProperty(propertyId).setValue(((Method) germplasmGidDateMap.get(gid)).getMid().toString());
			   } 
		   }
		   
		   markHasChangesFlagsAndToggleTableEditable(table);

	   } catch (MiddlewareQueryException e) {
		   e.printStackTrace();
	   }
    }       
    
    public void fillWithMethodGroup(Table table, String propertyId) {
    	fillWithMethodGroup(table, propertyId, false);
    }
    
    public void fillWithMethodGroup(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
	   try {
		   List<Integer> itemIds = getItemIds(table);
		   List<Integer> gids = getGidsFromTable(table);
		   Map<Integer,Object> germplasmGidDateMap = germplasmDataManager.getMethodsByGids(gids);
		   
		   for(Integer itemId: itemIds){
			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
				   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());			   
				   table.getItem(itemId).getItemProperty(propertyId).setValue(((Method) germplasmGidDateMap.get(gid)).getMgrp().toString());
			   } 
		   }
		   
		   markHasChangesFlagsAndToggleTableEditable(table);

	   } catch (MiddlewareQueryException e) {
		   e.printStackTrace();
	   }
    }
    
    public void fillWithCrossFemaleGID(Table table, String propertyId){
    	fillWithCrossFemaleGID(table, propertyId, false);
    }
    
    public void fillWithCrossFemaleGID(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
 	   try {
		   List<Integer> itemIds = getItemIds(table);
		   for(Integer itemId: itemIds){
			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
				   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
				   Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
				   table.getItem(itemId).getItemProperty(propertyId).setValue(germplasm.getGpid1());
			   } 
		   }
		   
		   markHasChangesFlagsAndToggleTableEditable(table);

	   } catch (MiddlewareQueryException e) {
		   e.printStackTrace();
	   }    	
    }
    
    public void fillWithCrossFemalePreferredName(Table table, String propertyId){
    	fillWithCrossFemalePreferredName(table, propertyId, false);
    }
    
    public void fillWithCrossFemalePreferredName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
  	   try {
 		   List<Integer> itemIds = getItemIds(table);
 		   for(Integer itemId: itemIds){
 			  if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
 				  Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
 				  Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
 				  List<Integer> parentGids = new ArrayList<Integer>();
 				  parentGids.add(germplasm.getGpid1());
 				  Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(parentGids);
 				  table.getItem(itemId).getItemProperty(propertyId).setValue(preferredNames.get(germplasm.getGpid1()));
 			  } 
 		   }
 		   
 		   markHasChangesFlagsAndToggleTableEditable(table);

 	   } catch (MiddlewareQueryException e) {
 		   e.printStackTrace();
 	   }    	    	
    }
    
    public void fillWithCrossMaleGID(Table table, String propertyId) {
    	fillWithCrossMaleGID(table, propertyId, false);
    }
    
    public void fillWithCrossMaleGID(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
  	   try {
 		   List<Integer> itemIds = getItemIds(table);
 		   for(Integer itemId: itemIds){
 			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
 				   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
 				   Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
 				   table.getItem(itemId).getItemProperty(propertyId).setValue(germplasm.getGpid2());
 			   }
 		   }
 		   	
		   
 		  markHasChangesFlagsAndToggleTableEditable(table);
 	       
 	   } catch (MiddlewareQueryException e) {
 		   e.printStackTrace();
 	   }    	    	
    }
    
    public void fillWithCrossMalePreferredName(Table table, String propertyId){
    	fillWithCrossMalePreferredName(table, propertyId, false);
    }
    
    public void fillWithCrossMalePreferredName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues){
   	   try {
  		   List<Integer> itemIds = getItemIds(table);
  		   for(Integer itemId: itemIds){
  			   if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
	  			   //Integer gid = (Integer) table.getItem(itemId).getItemProperty(GID_VALUE).getValue();
	  			   Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
	  			   Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);
	  			   List<Integer> parentGids = new ArrayList<Integer>();
	  			   parentGids.add(germplasm.getGpid2());
	  			   Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(parentGids);
	  			   table.getItem(itemId).getItemProperty(propertyId).setValue(preferredNames.get(germplasm.getGpid2()));
  			 	}
  		   }
  		   
		   markHasChangesFlagsAndToggleTableEditable(table);

  	   } catch (MiddlewareQueryException e) {
  		   e.printStackTrace();
  	   }        	
    }

    protected void fillWithPreferredName(Table table, String propertyId){
    	fillWithPreferredName(table, propertyId, false);
    }
    
    protected void fillWithPreferredName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
        for (Iterator<?> i = table.getItemIds().iterator(); i.hasNext();) {
            int listDataId = (Integer) i.next();
        	if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(listDataId).getItemProperty(propertyId).getValue() == null || table.getItem(listDataId).getItemProperty(propertyId).getValue().equals(""))){
	            //iterate through the table elements' IDs

	            Item item = table.getItem(listDataId);
	            Object gidObject = item.getItemProperty(GIDPropertyId).getValue();
	            Button b = (Button) gidObject;
	            String gid = b.getCaption();
	            GermplasmDetailModel gModel = getGermplasmDetails(Integer.valueOf(gid));
	            item.getItemProperty(propertyId).setValue(gModel.getGermplasmPreferredName());
        	}
        }
        
        markHasChangesFlagsAndToggleTableEditable(table);

	}
    
    protected void fillWithPreferredID(Table table, String propertyId) {
    	fillWithPreferredID(table, propertyId, false);
    }
    
    protected void fillWithPreferredID(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {    	
        for (Iterator<?> i = table.getItemIds().iterator(); i.hasNext();) {
            //iterate through the table elements' IDs
            int listDataId = (Integer) i.next();
            
            if(!onlyFillWithThoseHavingEmptyValues   
            		|| table.getItem(listDataId).getItemProperty(propertyId).getValue()==null  
            		|| table.getItem(listDataId).getItemProperty(propertyId).getValue().equals("")
            ){
	            Item item = table.getItem(listDataId);
	            Object gidObject = item.getItemProperty(GIDPropertyId).getValue();
	            Button b = (Button) gidObject;
	            String gid = b.getCaption();
	            GermplasmDetailModel gModel = getGermplasmDetails(Integer.valueOf(gid));
	            item.getItemProperty(propertyId).setValue(gModel.getPrefID());
            }
        }

        markHasChangesFlagsAndToggleTableEditable(table);	

	}
    
    protected void fillWithLocation(Table targetTable) {
    	
    	String propertyId = ListDataTablePropertyID.SEED_SOURCE.getName();
    	
        try {
            List<Integer> gidList = getGidsFromTable(targetTable);
            Map<Integer, String> gidLocations;
            gidLocations = germplasmDataManager.getLocationNamesByGids(gidList);
            
            List<Integer> itemIds = getItemIds(targetTable);
            for (Integer itemId : itemIds) {
           		Item item = targetTable.getItem(itemId);
           		Object gidObject = item.getItemProperty(GIDPropertyId).getValue();
           		Button b= (Button) gidObject;
           		String gid=b.getCaption();
           		item.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(gidLocations.get(new Integer(gid)));
            }
    		
    	    markHasChangesFlagsAndToggleTableEditable(targetTable);	

        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }
	}
    
    protected void fillWithLocation(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
    	
        try {
            List<Integer> gidList = getGidsFromTable(table);
            Map<Integer, String> gidLocations;
            gidLocations = germplasmDataManager.getLocationNamesByGids(gidList);
            
            List<Integer> itemIds = getItemIds(table);
            for (Integer itemId : itemIds) {
            	if(!onlyFillWithThoseHavingEmptyValues || (table.getItem(itemId).getItemProperty(propertyId).getValue() == null || table.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
            		Item item = table.getItem(itemId);
            		Object gidObject = item.getItemProperty(GIDPropertyId).getValue();
            		Button b= (Button) gidObject;
            		String gid=b.getCaption();
            		item.getItemProperty(propertyId).setValue(gidLocations.get(new Integer(gid)));
            	}
            }
    		
    	    //To trigger TableFieldFactory (fix for truncated data)
        	if(table.isEditable()){
    		   table.setEditable(false);
    		   table.setEditable(true);
    		}
	
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }
	}    
    
    public void fillWithSequence(String propertyId, String prefix, String suffix, int startNumber, int numOfZeros, 
    		boolean spaceBetweenPrefixAndCode, boolean spaceBetweenSuffixAndCode){
    	fillWithSequence(propertyId, prefix, suffix, startNumber, numOfZeros, spaceBetweenPrefixAndCode, spaceBetweenSuffixAndCode, false);
    }
    
    public void fillWithSequence(String propertyId, String prefix, String suffix, int startNumber, int numOfZeros, 
    		boolean spaceBetweenPrefixAndCode, boolean spaceBetweenSuffixAndCode, Boolean onlyFillWithThoseHavingEmptyValues){
    	List<Integer> itemIds = getItemIds(targetTable);
    	int number = startNumber;
        for (Integer itemId : itemIds) {
        	if(!onlyFillWithThoseHavingEmptyValues || (targetTable.getItem(itemId).getItemProperty(propertyId).getValue() == null || targetTable.getItem(itemId).getItemProperty(propertyId).getValue().equals(""))){
	            Item item = targetTable.getItem(itemId);
	            StringBuilder builder = new StringBuilder();
	            builder.append(prefix);
	            if(spaceBetweenPrefixAndCode){
	            	builder.append(" ");
	            }
	            
	            if(numOfZeros > 0){
	            	String numberString = "" + number;
	            	int numOfZerosNeeded = numOfZeros - numberString.length();
	                for (int i = 0; i < numOfZerosNeeded; i++){
	                	builder.append("0");
	                }
	            }
	            builder.append(number);
	            
	            if(suffix != null && spaceBetweenSuffixAndCode){
	            	builder.append(" ");
	            }
	            
	            if(suffix != null){
	            	builder.append(suffix);
	            }
	            
	            item.getItemProperty(propertyId).setValue(builder.toString());
	            ++number;
        	}
        }
        
        markHasChangesFlagsAndToggleTableEditable(targetTable);
    }
    
    private void displayExpansionLevelPopupWindow(final String propertyId){
    	crossExpansionLevel = Integer.valueOf(1);
    	final Window specifyCrossExpansionLevelWindow = new Window("Specify Expansion Level");
    	specifyCrossExpansionLevelWindow.setHeight("135px");
    	specifyCrossExpansionLevelWindow.setWidth("210px");
    	specifyCrossExpansionLevelWindow.setModal(true);
    	specifyCrossExpansionLevelWindow.setResizable(false);
    	
    	AbsoluteLayout layout = new AbsoluteLayout();
    	final ComboBox levelComboBox = new ComboBox();
    	for(int ctr = 1; ctr <= 5; ctr++){
    		levelComboBox.addItem(Integer.valueOf(ctr));
    	}
    	levelComboBox.setValue(Integer.valueOf(1));
    	levelComboBox.setNullSelectionAllowed(false);
    	layout.addComponent(levelComboBox, "top:10px;left:10px");
    	
    	Button okButton = new Button(messageSource.getMessage(Message.OK));
    	okButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				crossExpansionLevel = (Integer) levelComboBox.getValue();
				fillWithCrossExpansion(propertyId);
				targetTable.getWindow().removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
    	layout.addComponent(okButton, "top:50px;left:10px"); 
    	
    	Button cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
    	cancelButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				crossExpansionLevel = null;
				targetTable.getWindow().removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
    	layout.addComponent(cancelButton, "top:50px;left:60px");
    	
    	specifyCrossExpansionLevelWindow.setContent(layout);
    	
    	this.targetTable.getWindow().addWindow(specifyCrossExpansionLevelWindow);
    }
    
    private void fillWithCrossExpansion(String propertyId){
    	if(crossExpansionLevel != null){
	    	for (Iterator<?> i = targetTable.getItemIds().iterator(); i.hasNext();) {
	            //iterate through the table elements' IDs
	            int listDataId = (Integer) i.next();
	            Item item = targetTable.getItem(listDataId);
	            Object gidObject = item.getItemProperty(GIDPropertyId).getValue();
	            Button b= (Button) gidObject;
	            String gid=b.getCaption();
	            try{
	            	String crossExpansion = this.germplasmDataManager.getCrossExpansion(Integer.parseInt(gid), crossExpansionLevel.intValue());
	            	item.getItemProperty(propertyId).setValue(crossExpansion);
	            } catch(MiddlewareQueryException ex){
	            	LOG.error("Error with getting cross expansion: gid=" + gid + " level=" + crossExpansionLevel, ex);
	            	MessageNotifier.showError(targetTable.getWindow(), "Database Error!", "Error with getting Cross Expansion. "+messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
	            	return;
	            }
	        }
	    	
		   markHasChangesFlagsAndToggleTableEditable(targetTable);	

    	}
    }
    
    private void displaySequenceNumberPopupWindow(String propertyId){
    	Window specifySequenceNumberWindow = new Window("Specify Sequence Number");
    	specifySequenceNumberWindow.setHeight("320px");
    	specifySequenceNumberWindow.setWidth("530px");
    	specifySequenceNumberWindow.setModal(true);
    	specifySequenceNumberWindow.setResizable(false);
    	specifySequenceNumberWindow.setContent(new AdditionalDetailsCrossNameComponent(this, propertyId, specifySequenceNumberWindow));
    	specifySequenceNumberWindow.addStyleName(Reindeer.WINDOW_LIGHT);
    	this.targetTable.getWindow().addWindow(specifySequenceNumberWindow);
    }
    
    public GermplasmDetailModel getGermplasmDetails(int gid) throws InternationalizableException {
        try {
            germplasmDetail = new GermplasmDetailModel();
            Germplasm g = germplasmDataManager.getGermplasmByGID(new Integer(gid));
            Name name = germplasmDataManager.getPreferredNameByGID(gid);

            if (g != null) {
                germplasmDetail.setGid(g.getGid());
                germplasmDetail.setGermplasmMethod(germplasmDataManager.getMethodByID(g.getMethodId()).getMname());
                germplasmDetail.setGermplasmPreferredName(name == null ? "" : name.getNval());
                germplasmDetail.setPrefID(getGermplasmPrefID(g.getGid()));
            }
            return germplasmDetail;
        } catch (MiddlewareQueryException e) {
          
        }
		return germplasmDetail;
    }
    
    private String getGermplasmPrefID(int gid) throws InternationalizableException {
    	String prefId = "";
    	try {
    		ArrayList<Name> names = (ArrayList<Name>) germplasmDataManager.getNamesByGID(gid, 8, null);
          
    		for (Name n : names) {
    			if (n.getNstat() == 8) {
    				prefId = n.getNval();
    				break;
    			}
    		}
    		return prefId;
    	} catch (MiddlewareQueryException e) {
    		LOG.error("Error with getting preferred id of germplasm: " + gid, e);
    	}
		return prefId;
    }

	@Override
	public void updateLabels() {
		
	}
	
	public List<String> getFilledWithPropertyIds(){
		return filledWithPropertyIds;
	}

	public void trackFillWith(String propertyId){
		if(!filledWithPropertyIds.contains(propertyId))
			filledWithPropertyIds.add(propertyId);
	}

	private void setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(boolean visibility){
		this.menuFillWithBreedingMethodAbbreviation.setVisible(visibility);
		this.menuFillWithBreedingMethodGroup.setVisible(visibility);
		this.menuFillWithBreedingMethodInfo.setVisible(visibility);
		this.menuFillWithBreedingMethodName.setVisible(visibility);
		this.menuFillWithBreedingMethodNumber.setVisible(visibility);
		this.menuFillWithCrossFemaleGID.setVisible(visibility);
		this.menuFillWithCrossFemaleInformation.setVisible(visibility);
		this.menuFillWithCrossFemalePreferredName.setVisible(visibility);
		this.menuFillWithCrossMaleGID.setVisible(visibility);
		this.menuFillWithCrossMaleInformation.setVisible(visibility);
		this.menuFillWithCrossMalePreferredName.setVisible(visibility);
		this.menuFillWithEmpty.setVisible(visibility);
		this.menuFillWithGermplasmDate.setVisible(visibility);
		this.menuFillWithPrefID.setVisible(visibility);
		this.menuFillWithPrefName.setVisible(visibility);
		this.menuFillWithAttribute.setVisible(visibility);
		this.menuFillWithSequenceNumber.setVisible(visibility);
	}
	
	public int getNumberOfEntries(){
		return targetTable.getItemIds().size();
	}
}

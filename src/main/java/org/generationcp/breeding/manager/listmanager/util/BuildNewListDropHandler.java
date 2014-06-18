package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.ListSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.themes.BaseTheme;

public class BuildNewListDropHandler implements DropHandler {

	private static final Logger LOG = LoggerFactory.getLogger(BuildNewListDropHandler.class);
	private static final long serialVersionUID = 1L;
	
	 public interface ListUpdatedListener {
        public void listUpdated(final ListUpdatedEvent event);
    }

    public class ListUpdatedEvent {

        private final int listCount;

		public ListUpdatedEvent(final int listCount) {
			this.listCount = listCount;
        }

        public int getListCount() {
            return listCount;
        }
    }

    private List<ListUpdatedListener> listeners = null;
	
	private final String MATCHING_GERMPLASMS_TABLE_DATA = GermplasmSearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA;
	private final String MATCHING_LISTS_TABLE_DATA = ListSearchResultsComponent.MATCHING_LISTS_TABLE_DATA;
	private final String LIST_DATA_TABLE_DATA = ListComponent.LIST_DATA_COMPONENT_TABLE_DATA;
	
	private final GermplasmDataManager germplasmDataManager;
	private final GermplasmListManager germplasmListManager;
	private final InventoryDataManager inventoryDataManager;
	
	private Table targetTable;
	
	/** 
	 * Temporary data holders / caching instead of loading it all the time
	 */
	private Integer currentListId;
	private GermplasmListNewColumnsInfo currentColumnsInfo;
	
	private boolean changed = false;
	private ListManagerMain listManagerMain;
	
	public BuildNewListDropHandler(ListManagerMain listManagerMain, GermplasmDataManager germplasmDataManager, GermplasmListManager germplasmListManager, InventoryDataManager inventoryDataManager, Table targetTable) {
		this.listManagerMain = listManagerMain;
		this.germplasmDataManager = germplasmDataManager;
		this.germplasmListManager = germplasmListManager;
		this.inventoryDataManager = inventoryDataManager;
		this.targetTable = targetTable;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		
		if(event.getTransferable() instanceof TableTransferable){
			
			TableTransferable transferable = (TableTransferable) event.getTransferable();
	        Table sourceTable = transferable.getSourceComponent();
	        String sourceTableData = sourceTable.getData().toString();
	        AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) event.getTargetDetails());
	        targetTable = (Table) dropData.getTarget();
			
			if(sourceTableData.equals(MATCHING_GERMPLASMS_TABLE_DATA)){
				changed = true;
				
				//If table has selected items, add selected items
				if(hasSelectedItems(sourceTable))
					addSelectedGermplasmsFromTable(sourceTable);
				//If none, add what was dropped
				else
					addGermplasm((Integer) transferable.getItemId());
				
			} else if (sourceTableData.equals(MATCHING_LISTS_TABLE_DATA)){
				changed = true;
				
				//If table has selected items, add selected items
				if(hasSelectedItems(sourceTable))
					addSelectedGermplasmListsFromTable(sourceTable);
				//If none, add what was dropped
				else
					addGermplasmList((Integer) transferable.getItemId());
	
			} else if (sourceTableData.equals(LIST_DATA_TABLE_DATA)){
				changed = true;
				
				//If table has selected items, add selected items
				if(hasSelectedItems(sourceTable)){
					addFromListDataTable(sourceTable);
				} //If none, add what was dropped
				else if(transferable.getSourceComponent().getParent().getParent() instanceof ListComponent){
					Integer listId = ((ListComponent) transferable.getSourceComponent().getParent().getParent()).getGermplasmListId();
					addGermplasmFromList(listId, (Integer) transferable.getItemId());
				}

			} else if(sourceTableData.equals(ListBuilderComponent.GERMPLASMS_TABLE_DATA)){
				Object droppedOverItemId = dropData.getItemIdOver();
				
				//Check first if item is dropped on top of itself
				if(!transferable.getItemId().equals(droppedOverItemId)) {
	                Item oldItem = sourceTable.getItem(transferable.getItemId());
	                Object oldCheckBox = oldItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).getValue();
	                Object oldGid = oldItem.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
	                Object oldEntryCode = oldItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue();
	                Object oldSeedSource = oldItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).getValue();
	                Object oldDesignation = oldItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue();
	                Object oldParentage = oldItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).getValue();
	                sourceTable.removeItem(transferable.getItemId());
	                
	                Item newItem = sourceTable.addItemAfter(droppedOverItemId, transferable.getItemId());
	                newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(oldCheckBox);
	                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(oldGid);
	                newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(oldEntryCode);
	                newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(oldSeedSource);
	                newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(oldDesignation);
	                newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(oldParentage);
	                
	                assignSerializedEntryNumber();
	                
	                fireListUpdatedEvent();
	            }
			} else {
				LOG.error("Error During Drop: Unknown table data: "+sourceTableData);
			}
					
		//If source is from tree
		} else {
			Transferable transferable = event.getTransferable();
			addGermplasmList((Integer) transferable.getData("itemId"));
		}
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

	
	@SuppressWarnings("unchecked")
	private Boolean hasSelectedItems(Table table){
		List<Integer> selectedItemIds = new ArrayList<Integer>();
        selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
        if(selectedItemIds.size()>0) 
        	return true;
        return false;
	}

	private void addSelectedGermplasmListsFromTable(Table sourceTable) {
		List<Integer> selectedGermplasmListIds = getSelectedItemIds(sourceTable);
		for(Integer listId : selectedGermplasmListIds){
			addGermplasmList(listId);
		}
	}

	private void addGermplasmList(Integer listId){
		addGermplasmList(listId, false);
	}
	
	public void addGermplasmList(Integer listId, Boolean fromEditList){
		
		currentListId = listId;
		
		try {
			//Load currentColumnsInfo if cached list info is null or not matching the needed list id
			if(currentColumnsInfo==null || !currentColumnsInfo.getListId().equals(listId))
				currentColumnsInfo = germplasmListManager.getAdditionalColumnsForList(listId);
			
			
			GermplasmList germplasmList = getGermplasmList(listId);
			List<GermplasmListData> germplasmListData = germplasmList.getListData();
			
			//Fix for adding entries in reverse
			if(germplasmListData.size()>1 && germplasmListData.get(0).getEntryId()>germplasmListData.get(1).getEntryId()){
				Collections.reverse(germplasmListData);
			}
			
			for(GermplasmListData listData : germplasmListData){
				addGermplasmFromList(listId, listData.getId(), germplasmList, fromEditList);
			}
			
			changed = true;// mark that there is changes in a list that is currently building
			
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in getting germplasm list.", e);
			e.printStackTrace();
		}
		
		currentColumnsInfo = null;
		currentListId = null;
		
		fireListUpdatedEvent();
		
	}
	
	private void addSelectedGermplasmsFromTable(Table sourceTable) {
		List<Integer> selectedGermplasmIds = getSelectedItemIds(sourceTable);
		for(Integer itemId : selectedGermplasmIds){
			addGermplasm(itemId);
		}
	}

	public Integer addGermplasm(Integer gid){
        try {
            
            Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);

            final Integer newItemId = getNextListEntryId();
            Item newItem = targetTable.addItem(newItemId);
            
            Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(listManagerMain,gid.toString(), true, true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
            
            String crossExpansion = "";
            if(germplasm!=null){
                try {
                    if(germplasmDataManager!=null)
                        crossExpansion = germplasmDataManager.getCrossExpansion(germplasm.getGid(), 1);
                } catch(MiddlewareQueryException ex){
                    LOG.error("Error in retrieving cross expansion data for GID: " + germplasm.getGid() + ".", ex);
                    crossExpansion = "-";
                }
            }

            List<Integer> importedGermplasmGids = new ArrayList<Integer>();
            importedGermplasmGids.add(gid);
            Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);
            String preferredName = preferredNames.get(gid);
            Button designationButton = new Button(preferredName, new GidLinkButtonClickListener(listManagerMain,gid.toString(), true, true));
            designationButton.setStyleName(BaseTheme.BUTTON_LINK);
            designationButton.setDescription("Click to view Germplasm information");
            

            CheckBox tagCheckBox = new CheckBox();
            tagCheckBox.setImmediate(true);
            tagCheckBox.addListener(new ClickListener() {
	 			private static final long serialVersionUID = 1L;
	 			@Override
	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
	 					targetTable.select(newItemId);
	 				} else {
	 					targetTable.unselect(newItemId);
	 				}
	 			}
	 			 
	 		});
            
            //Inventory Related Columns
            
	   		//#1 Available Inventory
            String avail_inv = "";
            Integer availInvGid = getAvailInvForGID(gid); 
            if(availInvGid!=null)
            	avail_inv = availInvGid.toString();
            	
	   		Button inventoryButton = new Button(avail_inv, new InventoryLinkButtonClickListener(listManagerMain,gid));
	   		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
	   		inventoryButton.setDescription("Click to view Inventory Details");
	   		newItem.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).setValue(inventoryButton);
	   		
	   		if(avail_inv.equals("-")){
	   			inventoryButton.setEnabled(false);
	   			inventoryButton.setDescription("No Lot for this Germplasm");
	   		}
	   		else{
	   			inventoryButton.setDescription("Click to view Inventory Details");
	   		}
	   		
	   		//#2 Seed Reserved
	   		String seed_res = "-";
	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(seed_res);
	   		
            
            newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(tagCheckBox);
            if(newItem!=null && gidButton!=null)
                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
            newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("Germplasm Search");
            newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(designationButton);
            newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(crossExpansion);
            
            assignSerializedEntryNumber();
            
            FillWith FW = new FillWith(ListDataTablePropertyID.GID.getName(), targetTable);
            
        	for(String column : AddColumnContextMenu.getTablePropertyIds(targetTable)){
				FW.fillWith(targetTable, column, true);
        	}
            
        	fireListUpdatedEvent();
        	
            return newItemId;
            
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in adding germplasm to germplasm table.", e);
            e.printStackTrace();
            return null;
        }
        
	}
	
	
	private Integer getAvailInvForGID(Integer gid) {
		Integer avail_inv;
		try {
			avail_inv = inventoryDataManager.countLotsWithAvailableBalanceForGermplasm(gid);
			return avail_inv;
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Integer addGermplasmFromList(Integer listId, Integer lrecid){
		return addGermplasmFromList(listId, lrecid, null);
	}
	
	
	private Integer addGermplasmFromList(Integer listId, Integer lrecid, GermplasmList germplasmList){
		return addGermplasmFromList(listId, lrecid, germplasmList, false);
	}
	
	private Integer addGermplasmFromList(Integer listId, Integer lrecid, GermplasmList germplasmList, Boolean forEditList){
		
		currentListId = listId;
		
        try {
        	//Load currentColumnsInfo if cached list info is null or not matching the needed list id
        	if(currentColumnsInfo==null || !currentColumnsInfo.getListId().equals(listId))
				currentColumnsInfo = germplasmListManager.getAdditionalColumnsForList(listId);
        	
    		for (Entry<String, List<ListDataColumnValues>> columnEntry: currentColumnsInfo.getColumnValuesMap().entrySet()){
    			String column = columnEntry.getKey();
    			if(!AddColumnContextMenu.propertyExists(column, targetTable)){
    				targetTable.addContainerProperty(column, String.class, "");
    				targetTable.setColumnWidth(column, 250);
    			}
    		}
        	
    		//making sure that germplasmList has value
    		if(germplasmList == null){
    			germplasmList = getGermplasmList(listId);
        	} 
    		
    		GermplasmListData germplasmListData = null;
    		
    		if(germplasmList.getListData() != null && germplasmList.getListData().size() > 0){
    			for(GermplasmListData listData : germplasmList.getListData()){
        			if(listData.getId().equals(lrecid)){
        				germplasmListData = listData;
        			}
        		}
        	}
    		else{
        		germplasmListData = germplasmListManager.getGermplasmListDataByListIdAndLrecId(listId, lrecid);
    		}
    		
    		//handles the data for inventory
    		
    		
        	if(germplasmListData!=null && germplasmListData.getStatus()!=9){
        		
	            Integer gid = germplasmListData.getGid();
	            
	            Integer niid = null;
	            if(forEditList.equals(true)){
	            	niid = getNextListEntryId(germplasmListData.getId());
	            } else {
	            	niid = getNextListEntryId();
	            }
	            final Integer newItemId = niid;
	            
	            Item newItem = targetTable.addItem(newItemId);
	            
	            Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(listManagerMain,gid.toString(), true, true));
	            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
	            
	            CheckBox tagCheckBox = new CheckBox();
	            tagCheckBox.setImmediate(true);
	            tagCheckBox.addListener(new ClickListener() {
    	 			private static final long serialVersionUID = 1L;
    	 			@Override
    	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
    	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
    	 					targetTable.select(newItemId);
    	 				} else {
    	 					targetTable.unselect(newItemId);
    	 				}
    	 			}
    	 			 
    	 		});
	            
	            Button designationButton = new Button(germplasmListData.getDesignation(), new GidLinkButtonClickListener(listManagerMain,gid.toString(), true, true));
	            designationButton.setStyleName(BaseTheme.BUTTON_LINK);
	            designationButton.setDescription("Click to view Germplasm information");
	            
	            newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(tagCheckBox);
	            if(newItem!=null && gidButton!=null)
	                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
	            newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(germplasmListData.getEntryCode());
	            if(forEditList.equals(true)){
	            	newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(germplasmListData.getSeedSource());
	            } else {
	            	newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(germplasmList.getName()+": "+germplasmListData.getEntryId());
	            }
	            newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(designationButton);
	            newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(germplasmListData.getGroupName());
	            
	            //Inventory Related Columns
    	   		
    	   		//#1 Available Inventory
    	   		String avail_inv = "-";
    	   		if(germplasmListData.getInventoryInfo().getActualInventoryLotCount() != null && germplasmListData.getInventoryInfo().getActualInventoryLotCount() != 0){
    	   			avail_inv = germplasmListData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
    	   		}
    	   		Button inventoryButton = new Button(avail_inv, new InventoryLinkButtonClickListener(listManagerMain,germplasmListData.getGid()));
    	   		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
    	   		newItem.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).setValue(inventoryButton);
    	   		
    	   		if(avail_inv.equals("-")){
    	   			inventoryButton.setEnabled(false);
    	   			inventoryButton.setDescription("No Lot for this Germplasm");
    	   		}
    	   		else{
    	   			inventoryButton.setDescription("Click to view Inventory Details");
    	   		}
    	   		
    	   		//#2 Seed Reserved
    	   		String seed_res = "-";
    	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(seed_res);
    	   		
	            
	    		for (Entry<String, List<ListDataColumnValues>> columnEntry: currentColumnsInfo.getColumnValuesMap().entrySet()){
	    			String column = columnEntry.getKey();
	    			for (ListDataColumnValues columnValue : columnEntry.getValue()){
	    				if (columnValue.getListDataId().equals(germplasmListData.getId())){
	    					String value = columnValue.getValue();
	    					newItem.getItemProperty(column).setValue(value == null ? "" : value);
	    				}
	    			}
	    		}
	    			
	            assignSerializedEntryNumber();

	            FillWith FW = new FillWith(ListDataTablePropertyID.GID.getName(), targetTable);
	            
	        	for(String column : AddColumnContextMenu.getTablePropertyIds(targetTable)){
    				FW.fillWith(targetTable, column, true);
	        	}
	            
	            currentListId = null;
	            
	            fireListUpdatedEvent();
	            return newItemId;
        	}
        	
        	fireListUpdatedEvent();
        	return null;
        	
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in adding germplasm to germplasm table.", e);
            e.printStackTrace();
            
			currentColumnsInfo = null;
            currentListId = null;
            
            return null;
        }
        
	}	
	
	public void addFromListDataTable(Table sourceTable){
		List<Integer> itemIds = getSelectedItemIds(sourceTable);
		
		Integer listId = null;
		if(sourceTable.getParent() instanceof TableWithSelectAllLayout && sourceTable.getParent().getParent() instanceof ListComponent)
			listId = ((ListComponent) sourceTable.getParent().getParent()).getGermplasmListId();

		GermplasmList germplasmList = getGermplasmList(listId);
		
    	//Load currentColumnsInfo if cached list info is null or not matching the needed list id
    	if(currentColumnsInfo==null || !currentColumnsInfo.getListId().equals(listId)){
			try {
				currentColumnsInfo = germplasmListManager.getAdditionalColumnsForList(listId);
			} catch (MiddlewareQueryException e) {
				LOG.error("Error During getAdditionalColumnsForList("+listId+"): "+e);
			}
    	}
    	
		for (Entry<String, List<ListDataColumnValues>> columnEntry: currentColumnsInfo.getColumnValuesMap().entrySet()){
			String column = columnEntry.getKey();
			if(!AddColumnContextMenu.propertyExists(column, targetTable)){
				targetTable.addContainerProperty(column, String.class, "");
				targetTable.setColumnWidth(column, 250);
			}
		}
		
		for(Integer itemId : itemIds){
			
			Item itemFromSourceTable = sourceTable.getItem(itemId);
			Integer newItemId = getNextListEntryId();
			Item newItem = targetTable.addItem(newItemId);
			
			Integer gid = getGidFromButtonCaption(sourceTable, itemId);
			Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(listManagerMain,gid.toString(), true, true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
            gidButton.setDescription("Click to view Germplasm information");
            
            CheckBox itemCheckBox = new CheckBox();
            itemCheckBox.setData(newItemId);
            itemCheckBox.setImmediate(true);
	   		itemCheckBox.addListener(new ClickListener() {
	 			private static final long serialVersionUID = 1L;
	 			@Override
	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
	 					targetTable.select(itemCheckBox.getData());
	 				} else {
	 					targetTable.unselect(itemCheckBox.getData());
	 				}
	 			}
	 			 
	 		});
	   		
	   		String seedSource = (String) itemFromSourceTable.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).getValue();
	   		
	   		String designation = getDesignationFromButtonCaption(sourceTable,itemId);
	   		Button designationButton = new Button(designation, new GidLinkButtonClickListener(listManagerMain,gid.toString(), true, true));
	   		designationButton.setStyleName(BaseTheme.BUTTON_LINK);
	   		designationButton.setDescription("Click to view Germplasm information");
	   		
	   		String parentage = (String) itemFromSourceTable.getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).getValue();
	   		Integer entryId = (Integer) itemFromSourceTable.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue();
	   		String entryCode = (String) itemFromSourceTable.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue();
	   		
	   		//Inventory Related Columns
	   		
	   		//#1 Available Inventory
	   		String avail_inv = getAvailInvFromButtonCaption(sourceTable, itemId);
	   		Button inventoryButton = new Button(avail_inv, new InventoryLinkButtonClickListener(listManagerMain,gid));
	   		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
	   		inventoryButton.setDescription("Click to view Inventory Details");
	   		
	   		if(avail_inv.equals("-")){
	   			inventoryButton.setEnabled(false);
	   			inventoryButton.setDescription("No Lot for this Germplasm");
	   		}
	   		else{
	   			inventoryButton.setDescription("Click to view Inventory Details");
	   		}
	   		
	   		//#2 Seed Reserved
	   		String seed_res = "-";
	   		
	   		newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(itemCheckBox);
	   		newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(germplasmList.getName()+": "+entryId);
	   		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(designationButton);
	   		newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(parentage);
	   		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(entryCode);
	   		newItem.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).setValue(inventoryButton);
	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(seed_res);
    		for (Entry<String, List<ListDataColumnValues>> columnEntry: currentColumnsInfo.getColumnValuesMap().entrySet()){
    			String column = columnEntry.getKey();
    			for (ListDataColumnValues columnValue : columnEntry.getValue()){
    				if (columnValue.getListDataId().equals(itemId)){
    					String value = columnValue.getValue();
    					newItem.getItemProperty(column).setValue(value == null ? "" : value);
    				}
    			}
    		}
    			
		}
		
		assignSerializedEntryNumber();
		
        FillWith FW = new FillWith(ListDataTablePropertyID.GID.getName(), targetTable);
        
    	for(String column : AddColumnContextMenu.getTablePropertyIds(targetTable)){
			FW.fillWith(targetTable, column, true);
    	}
    	fireListUpdatedEvent();
	}


	/**
     * Iterates through the whole table, and sets the entry code from 1 to n based on the row position
     */
    private void assignSerializedEntryCode(){
        List<Integer> itemIds = getItemIds(targetTable);
        
        int id = 1;
        for(Integer itemId : itemIds){
        	
        	/**
        	 * TODO: If add columns is already implemented, add this checker below
        	 */
        	
            //Check if filled with was used for this column, if so, do not change values to serialized numbers
            //if(!filledWithPropertyIds.contains(ListDataTablePropertyID.ENTRY_ID.getName()))
            //    targetTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(id);
            //if(!filledWithPropertyIds.contains(ListDataTablePropertyID.ENTRY_CODE.getName()))
            //    targetTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(id);
            id++;
        }
    }
    
    /**
     * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
     */
    private void assignSerializedEntryNumber(){
        List<Integer> itemIds = getItemIds(targetTable);
                
        int id = 1;
        for(Integer itemId : itemIds){
            targetTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(id);
            
            Property entryCodeProperty = targetTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName());
           	if(entryCodeProperty!=null || entryCodeProperty.getValue()==null || entryCodeProperty.getValue().toString().equals(""))
           		targetTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(id);
            id++;
        }
    }
    
    /**
     * Get item id's of a table, and return it as a list 
     * @param table
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Integer> getItemIds(Table table){
        List<Integer> itemIds = new ArrayList<Integer>();
        itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
        return itemIds;
    }    
    
    public Integer getNextListEntryId(){
        int maxId = 0;
        for(Object id : targetTable.getItemIds()){
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
    
    public Integer getNextListEntryId(Integer lrecId){
    	
    	Integer id = 0;
    	
    	try {
			GermplasmListData entry = germplasmListManager.getGermplasmListDataByListIdAndLrecId(currentListId, lrecId);
			
			if(entry != null){
				return entry.getId();
			}
			else{
				return getNextListEntryId();
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error retrieving germplasm list data",e);
			e.printStackTrace();
		}
    	
    	return getNextListEntryId();
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
    
    public Integer getGidFromButtonCaption(Table table, Integer itemId){
    	Item item = table.getItem(itemId);
   	    if(item!=null){
    	    String buttonCaption = ((Button) item.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue()).getCaption().toString();
    	    return Integer.valueOf(buttonCaption);
    	}
    	return null;	
    }
    
    private String getAvailInvFromButtonCaption(Table table, Integer itemId){
    	Item item = table.getItem(itemId);
   	    if(item!=null){
    	    String buttonCaption = ((Button) item.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).getValue()).getCaption().toString();
    	    return buttonCaption;
    	}
    	return null;
    }
    
    private String getDesignationFromButtonCaption(Table table, Integer itemId){
    	Item item = table.getItem(itemId);
   	    if(item!=null){
    	    String buttonCaption = ((Button) item.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue()).getCaption().toString();
    	    return buttonCaption;
    	}
    	return null;	
    }

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	private void fireListUpdatedEvent() {
        if (listeners != null) {
        	final ListUpdatedEvent event = new ListUpdatedEvent(targetTable.size());
            for (ListUpdatedListener listener : listeners) {
                listener.listUpdated(event);
            }
        }
        listManagerMain.showListBuilder();
    }
	
	public void addListener(final ListUpdatedListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ListUpdatedListener>();
        }
        listeners.add(listener);
    }

    public void removeListener(final ListUpdatedListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ListUpdatedListener>();
        }
        listeners.remove(listener);
    }
    
    /**
     * Reset the germplasmList, and make sure that the inventory columns are properly filled up
     */
    public GermplasmList getGermplasmList(Integer listId){
    	GermplasmList germplasmList = null;
    	
    	try {
			germplasmList = germplasmListManager.getGermplasmListById(listId);
			
			List<GermplasmListData> germplasmListData = inventoryDataManager.getLotCountsForList(germplasmList.getId(), 0, Integer.MAX_VALUE);
			germplasmList.setListData(germplasmListData);
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

    	return germplasmList;
    }
    
}

package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListDataComponent;
import org.generationcp.breeding.manager.listmanager.SearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.themes.BaseTheme;

public class BuildNewListDropHandler implements DropHandler {

	private static final Logger LOG = LoggerFactory.getLogger(BuildNewListDropHandler.class);
	private static final long serialVersionUID = 1L;
	
	private String MATCHING_GERMPLASMS_TABLE_DATA = SearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA;
	private String MATCHING_LISTS_TABLE_DATA = SearchResultsComponent.MATCHING_LISTS_TABLE_DATA;
	private String LIST_DATA_TABLE_DATA = ListDataComponent.LIST_DATA_COMPONENT_TABLE_DATA;
	
	private GermplasmDataManager germplasmDataManager;
	private GermplasmListManager germplasmListManager;
	
	private Table targetTable;
	
	/** 
	 * Temporary data holders / caching instead of loading it all the time
	 */
	private Integer currentListId;
	private GermplasmListNewColumnsInfo currentColumnsInfo;
	
	
	public BuildNewListDropHandler(GermplasmDataManager germplasmDataManager, GermplasmListManager germplasmListManager, Table targetTable) {
		this.germplasmDataManager = germplasmDataManager;
		this.germplasmListManager = germplasmListManager;
		this.targetTable = targetTable;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		
		if(event.getTransferable() instanceof TableTransferable){
			
			TableTransferable transferable = (TableTransferable) event.getTransferable();
	        Table sourceTable = (Table) transferable.getSourceComponent();
	        String sourceTableData = sourceTable.getData().toString();
	        AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) event.getTargetDetails());
	        targetTable = (Table) dropData.getTarget();
			
			if(sourceTableData.equals(MATCHING_GERMPLASMS_TABLE_DATA)){
				
				//If table has selected items, add selected items
				if(hasSelectedItems(sourceTable))
					addSelectedGermplasmsFromTable(sourceTable);
				//If none, add what was dropped
				else
					addGermplasm((Integer) transferable.getItemId());
				
			} else if (sourceTableData.equals(MATCHING_LISTS_TABLE_DATA)){
				
				//If table has selected items, add selected items
				if(hasSelectedItems(sourceTable))
					addSelectedGermplasmListsFromTable(sourceTable);
				//If none, add what was dropped
				else
					addGermplasmList((Integer) transferable.getItemId());
	
			} else if (sourceTableData.equals(LIST_DATA_TABLE_DATA)){
				
				//If table has selected items, add selected items
				if(hasSelectedItems(sourceTable))
					addFromListDataTable(sourceTable);
				//If none, add what was dropped
				else if(transferable.getSourceComponent().getParent().getParent() instanceof ListDataComponent)
					addGermplasmFromList(((ListDataComponent) transferable.getSourceComponent().getParent().getParent()).getGermplasmListId(), (Integer) transferable.getItemId());

			} else {
				LOG.error("Error During Drop: Unknown table data: "+sourceTableData);
			}
					
		//If source is from tree
		} else {
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
		
		currentListId = listId;
		
		try {
			//Load currentColumnsInfo if cached list info is null or not matching the needed list id
			if(currentColumnsInfo==null || !currentColumnsInfo.getListId().equals(listId))
				currentColumnsInfo = germplasmListManager.getAdditionalColumnsForList(listId);
			
			GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
			List<GermplasmListData> germplasmListData = germplasmList.getListData();
			for(GermplasmListData listData : germplasmListData){
				addGermplasmFromList(listId, listData.getId(), germplasmList);
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in getting germplasm list.", e);
			e.printStackTrace();
		}
		
		currentColumnsInfo = null;
		currentListId = null;
		
	}
	
	private void addSelectedGermplasmsFromTable(Table sourceTable) {
		List<Integer> selectedGermplasmIds = getSelectedItemIds(sourceTable);
		for(Integer itemId : selectedGermplasmIds){
			addGermplasm(itemId);
		}
	}

	private Integer addGermplasm(Integer gid){
        try {
            
            Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);

            final Integer newItemId = getNextListEntryId();
            Item newItem = targetTable.addItem(newItemId);
            
            Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
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
            
            newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(tagCheckBox);
            if(newItem!=null && gidButton!=null)
                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
            newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("Germplasm Search");
            newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(preferredName);
            newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(crossExpansion);
            
            assignSerializedEntryNumber();
            
            FillWith FW = new FillWith(ListDataTablePropertyID.GID.getName());
            
        	for(String column : AddColumnContextMenu.getTablePropertyIds(targetTable)){
				FW.fillWith(targetTable, column, true);
        	}
            
            return newItemId;
            
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in adding germplasm to germplasm table.", e);
            e.printStackTrace();
            return null;
        }
		
	}
	
	
	private Integer addGermplasmFromList(Integer listId, Integer lrecid){
		return addGermplasmFromList(listId, lrecid, null);
	}
	
	private Integer addGermplasmFromList(Integer listId, Integer lrecid, GermplasmList germplasmList){
		
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
        	
    		GermplasmListData germplasmListData = null;
    		
    		if(germplasmList==null){
    			germplasmListData = germplasmListManager.getGermplasmListDataByListIdAndLrecId(listId, lrecid);
        	} else {
        		for(GermplasmListData listData : germplasmList.getListData()){
        			if(listData.getId().equals(lrecid))
        				germplasmListData = listData;
        		}
        	}
        	
        	if(germplasmListData!=null){
        		
	            Integer gid = germplasmListData.getGid();
	            final Integer newItemId = getNextListEntryId();
	            Item newItem = targetTable.addItem(newItemId);
	            
	            Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
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
	            
	            newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(tagCheckBox);
	            if(newItem!=null && gidButton!=null)
	                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
	            newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(germplasmListData.getEntryCode());
	            newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(germplasmListData.getSeedSource());
	            newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(germplasmListData.getDesignation());
	            newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(germplasmListData.getGroupName());
	            
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

	            FillWith FW = new FillWith(ListDataTablePropertyID.GID.getName());
	            
	        	for(String column : AddColumnContextMenu.getTablePropertyIds(targetTable)){
    				FW.fillWith(targetTable, column, true);
	        	}
	            
	            currentListId = null;
	            return newItemId;
        	}
        	
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
		if(sourceTable.getParent() instanceof TableWithSelectAllLayout && sourceTable.getParent().getParent() instanceof ListDataComponent)
			listId = ((ListDataComponent) sourceTable.getParent().getParent()).getGermplasmListId();
		
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
			Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
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
	   		Button designationButton = (Button) itemFromSourceTable.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue(); 
	   		String designation = designationButton.getCaption();
	   		String parentage = (String) itemFromSourceTable.getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).getValue();
	   		String entryCode = (String) itemFromSourceTable.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue();
	   		
	   		newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(itemCheckBox);
	   		newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
	   		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(seedSource);
	   		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(designation);
	   		newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(parentage);
	   		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(entryCode);
	   		
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
		
        FillWith FW = new FillWith(ListDataTablePropertyID.GID.getName());
        
    	for(String column : AddColumnContextMenu.getTablePropertyIds(targetTable)){
			FW.fillWith(targetTable, column, true);
    	}
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
           	if(entryCodeProperty==null || entryCodeProperty.getValue()==null || entryCodeProperty.getValue().toString().equals(""))
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
    private List<Integer> getItemIds(Table table){
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
    
    private Integer getGidFromButtonCaption(Table table, Integer itemId){
    	Item item = table.getItem(itemId);
   	    if(item!=null){
    	    String buttonCaption = ((Button) item.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue()).getCaption().toString();
    	    return Integer.valueOf(buttonCaption);
    	}
    	return null;	
    }
}

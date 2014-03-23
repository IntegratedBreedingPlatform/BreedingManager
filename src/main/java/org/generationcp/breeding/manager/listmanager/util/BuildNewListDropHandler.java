package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.ListDataComponent;
import org.generationcp.breeding.manager.listmanager.SearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
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
	
	public BuildNewListDropHandler(GermplasmDataManager germplasmDataManager, GermplasmListManager germplasmListManager) {
		this.germplasmDataManager = germplasmDataManager;
		this.germplasmListManager = germplasmListManager;
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
					addSelectedGermplasmsFromTable(sourceTable);
				//If none, add what was dropped
				else
					addGermplasm(getGidFromButtonCaption(sourceTable, (Integer) transferable.getItemId()));
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
		try {
			List<GermplasmListData> germplasmListData = germplasmListManager.getGermplasmListById(listId).getListData();
			for(GermplasmListData listData : germplasmListData){
				addGermplasm(listData.getGid());
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in getting germplasm list.", e);
			e.printStackTrace();
		}
		
	}
	
	
	
	
	private void addSelectedGermplasmsFromTable(Table sourceTable) {
		List<Integer> selectedGermplasmIds = getSelectedItemIds(sourceTable);
		for(Integer itemId : selectedGermplasmIds){
			addGermplasm(getGidFromButtonCaption(sourceTable, itemId));
		}
	}
	

	private Integer addGermplasm(Integer gid){
        try {
            
            Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);

            Item newItem = targetTable.addItem(getNextListEntryId());
            
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
            
            newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(tagCheckBox);
            if(newItem!=null && gidButton!=null)
                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
            newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue("Germplasm Search");
            newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(preferredName);
            newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(crossExpansion);
            
            assignSerializedEntryNumber();
            
            return getNextListEntryId();
            
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in adding germplasm to germplasm table.", e);
            e.printStackTrace();
            return null;
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

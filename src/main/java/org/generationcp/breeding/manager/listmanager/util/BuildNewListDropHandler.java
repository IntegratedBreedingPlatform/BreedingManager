package org.generationcp.breeding.manager.listmanager.util;

import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;

import com.vaadin.data.Item;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;

public class BuildNewListDropHandler extends DropHandlerMethods implements DropHandler {

	private static final long serialVersionUID = 1L;
	
	
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
	                Object oldAvailInv = oldItem.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).getValue();
	                Object oldSeedRes = oldItem.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).getValue();
	                
	                sourceTable.removeItem(transferable.getItemId());
	                
	                Item newItem = sourceTable.addItemAfter(droppedOverItemId, transferable.getItemId());
	                newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(oldCheckBox);
	                newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(oldGid);
	                newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(oldEntryCode);
	                newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(oldSeedSource);
	                newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(oldDesignation);
	                newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(oldParentage);
	                newItem.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).setValue(oldAvailInv);
	                newItem.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(oldSeedRes);
	                
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
    
}

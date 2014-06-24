package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;

import com.vaadin.data.Item;
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

public class InventoryTableDropHandler extends DropHandlerMethods implements DropHandler {

	private static final long serialVersionUID = 1L;
	
	
	public InventoryTableDropHandler(ListManagerMain listManagerMain, GermplasmDataManager germplasmDataManager, GermplasmListManager germplasmListManager, InventoryDataManager inventoryDataManager, Table targetTable) {
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
			
			if(sourceTableData.equals(ListInventoryTable.INVENTORY_TABLE_DATA) && !sourceTable.equals(targetTable)){
				changed = true;
				
				Integer listId = ((ListComponent) transferable.getSourceComponent().getParent().getParent()).getGermplasmListId();
				
				//If table has selected items, add selected items
				if(hasSelectedItems(sourceTable)){
					//addFromListDataTable(sourceTable);
				} //If none, add what was dropped
				else if(transferable.getSourceComponent().getParent().getParent() instanceof ListComponent){
					
					//addGermplasmFromList(listId, (Integer) transferable.getItemId());
				}
			
				System.out.println("Drag from list data to list manager inventory view.");
				
			} else {
				LOG.error("Error During Drop: Unknown table data: "+sourceTableData);
			}
					
		//If source is from tree
		} else {
			//Not handled
		}
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

	/**
	 * Use this to handle drop events from list inventory view of list tab to list inventory view of list builder
	 * @param selectedItemIds
	 * @param sourceTable
	 */
	private void addSelectedInventoryDetails(List<ListEntryLotDetails> selectedItemIds, Table sourceTable){
		
		List<Integer> uniqueEntryIds = getUniqueEntryNumbers(selectedItemIds, sourceTable);
		List<ListEntryLotDetails> allLotDetailsToBeAdded = new ArrayList<ListEntryLotDetails>();
		
		for(Integer uniqueEntryId : uniqueEntryIds){
			allLotDetailsToBeAdded.addAll(getLotDetailsWithEntryId(uniqueEntryId, sourceTable));
		}
		
		for(ListEntryLotDetails lotDetail : allLotDetailsToBeAdded){
			addItemToDestinationTable(lotDetail, sourceTable, targetTable);
		}
		
	}
    
	
	
	/**
	 * Get distinct entry #'s, given a list of selected item ids and the source table
	 * @param selectedItemIds
	 * @param sourceTable
	 * @return
	 */
	private List<Integer> getUniqueEntryNumbers(List<ListEntryLotDetails> selectedItemIds, Table sourceTable){
		List<Integer> uniqueEntryIds = new ArrayList<Integer>();
		for(ListEntryLotDetails lotDetail : selectedItemIds){
			if(!uniqueEntryIds.contains(lotDetail.getEntityIdOfLot()))
				uniqueEntryIds.add(lotDetail.getEntityIdOfLot());
		}
		return uniqueEntryIds;
	}
	
	/**
	 * Get lotDetails with given entry ID, given the entry ID and the source table
	 * @param entryId
	 * @param sourceTable
	 * @return
	 */
	private List<ListEntryLotDetails> getLotDetailsWithEntryId(Integer entryId, Table sourceTable){
        List<ListEntryLotDetails> allLotDetails = new ArrayList<ListEntryLotDetails>();
        List<ListEntryLotDetails> matchingLotDetails = new ArrayList<ListEntryLotDetails>();
	    allLotDetails.addAll((Collection<? extends ListEntryLotDetails>) sourceTable.getItemIds());
	    for(ListEntryLotDetails lotDetail : allLotDetails){
	    	if(lotDetail.getEntityIdOfLot().equals(entryId))
	    		matchingLotDetails.add(lotDetail);
	    }
	    return matchingLotDetails;
	}	
	
	private Item addItemToDestinationTable(ListEntryLotDetails lotDetail, Table sourceTable, final Table targetTable){
		
		Item newItem = targetTable.addItem(lotDetail);
		
		CheckBox itemCheckBox = new CheckBox();
        itemCheckBox.setData(lotDetail);
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
		
   		String desig = "";
   		String gid = "";
   		Item itemFromSourceTable = sourceTable.getItem(lotDetail);
   		if(itemFromSourceTable!=null){
   			desig = (String) itemFromSourceTable.getItemProperty(ListInventoryTable.DESIGNATION_COLUMN_ID).getValue();
   			desig = (String) itemFromSourceTable.getItemProperty(ListInventoryTable.DESIGNATION_COLUMN_ID).getValue();
   		}
   		
   		Button desigButton = new Button(String.format("%s", desig), new GidLinkButtonClickListener(listManagerMain,gid, true, true));
        desigButton.setStyleName(BaseTheme.BUTTON_LINK);
   		
   		newItem.getItemProperty(ListInventoryTable.TAG_COLUMN_ID).setValue(itemCheckBox);
		newItem.getItemProperty(ListInventoryTable.ENTRY_NUMBER_COLUMN_ID).setValue("999");
		newItem.getItemProperty(ListInventoryTable.DESIGNATION_COLUMN_ID).setValue(desigButton);
		newItem.getItemProperty(ListInventoryTable.LOCATION_COLUMN_ID).setValue(lotDetail.getLocationOfLot().getLname());
		newItem.getItemProperty(ListInventoryTable.UNITS_COLUMN_ID).setValue(lotDetail.getScaleOfLot().getName());
		newItem.getItemProperty(ListInventoryTable.AVAIL_COLUMN_ID).setValue(lotDetail.getAvailableLotBalance());
		newItem.getItemProperty(ListInventoryTable.TOTAL_COLUMN_ID).setValue(lotDetail.getActualLotBalance());
		newItem.getItemProperty(ListInventoryTable.RESERVED_COLUMN_ID).setValue(lotDetail.getReservedTotalForEntry());
		newItem.getItemProperty(ListInventoryTable.NEWLY_RESERVED_COLUMN_ID).setValue(0);
		newItem.getItemProperty(ListInventoryTable.COMMENT_COLUMN_ID).setValue(lotDetail.getCommentOfLot());
		newItem.getItemProperty(ListInventoryTable.LOT_ID_COLUMN_ID).setValue(lotDetail.getLotId());
		
		return newItem;
	}
	
}

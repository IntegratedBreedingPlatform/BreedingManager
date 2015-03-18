package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.List;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.InventoryTableDropHandler;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.themes.BaseTheme;

public class ListManagerInventoryTable extends ListInventoryTable {

	private static final Logger LOG = LoggerFactory.getLogger(ListManagerInventoryTable.class);
	
	private static final long serialVersionUID = 7827387488704418083L;
	public static final String INVENTORY_TABLE_DATA = "BuildNewListInventoryTableData";
	
	private ListManagerMain listManagerMain;
	private InventoryTableDropHandler inventoryTableDropHandler;
	private Boolean enableDragSource;
	private Boolean enableDropHandler;

	public ListManagerInventoryTable(ListManagerMain listManagerMain,
			Integer listId, Boolean enableDragSource, Boolean enableDropHandler) {
		super(listId);
		this.listManagerMain = listManagerMain;
		this.enableDragSource = enableDragSource;
		this.enableDropHandler = enableDropHandler;
	}
	
	@Override
	public void instantiateComponents() {
		super.instantiateComponents();
		
		listInventoryTable.setData(INVENTORY_TABLE_DATA);
		setDragSource();
		setDropHandler();
	}
	
	@Override
	public void displayInventoryDetails(List<GermplasmListData> inventoryDetails){
		
		listInventoryTable.removeAllItems();
		for(GermplasmListData inventoryDetail : inventoryDetails){
			
			Integer entryId = inventoryDetail.getEntryId();
			String designation = inventoryDetail.getDesignation();
			
			ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();
			@SuppressWarnings("unchecked")
			List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>)listDataInventory.getLotRows();
			
			if(lotDetails!=null){
				for(ListEntryLotDetails lotDetail : lotDetails){
					Item newItem = listInventoryTable.addItem(lotDetail);
					
					CheckBox itemCheckBox = new CheckBox();
			        itemCheckBox.setData(lotDetail);
			        itemCheckBox.setImmediate(true);
			   		itemCheckBox.addListener(new ClickListener() {
			 			private static final long serialVersionUID = 1L;
			 			@Override
			 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
			 				CheckBox itemCheckBox = (CheckBox) event.getButton();
			 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
			 					listInventoryTable.select(itemCheckBox.getData());
			 				} else {
			 					listInventoryTable.unselect(itemCheckBox.getData());
			 				}
			 			}
			 			 
			 		});
					
			   		GermplasmListData germplasmListData = null;
			   		
			   		try {
						germplasmListData = germplasmListManager.getGermplasmListDataByListIdAndLrecId(listId, lotDetail.getId());
					} catch (MiddlewareQueryException e) {
						LOG.error(e.getMessage(), e);
					}
			   		
			   		Button desigButton = new Button(String.format("%s", designation), 
			   					new GidLinkButtonClickListener(listManagerMain,germplasmListData.getGid().toString(), true, true));
		            desigButton.setStyleName(BaseTheme.BUTTON_LINK);
			   		
			   		newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
					newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);
					newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
					newItem.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).setValue(lotDetail.getLocationOfLot().getLname());
					newItem.getItemProperty(ColumnLabels.SCALE.getName()).setValue(lotDetail.getScaleOfLot().getName());
					newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(lotDetail.getAvailableLotBalance());
					newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(lotDetail.getActualLotBalance());
					newItem.getItemProperty(ColumnLabels.RESERVED.getName()).setValue(lotDetail.getReservedTotalForEntry());
					newItem.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(0);
					newItem.getItemProperty(ColumnLabels.COMMENT.getName()).setValue(lotDetail.getCommentOfLot());
					newItem.getItemProperty(ColumnLabels.LOT_ID.getName()).setValue(lotDetail.getLotId());
				}
			}
		}
	}
	
	public void setDropHandler(){
		inventoryTableDropHandler = new InventoryTableDropHandler(listManagerMain, germplasmDataManager, germplasmListManager, inventoryDataManager, listInventoryTable);
		if(enableDropHandler) {
            listInventoryTable.setDropHandler(inventoryTableDropHandler);
        }
	}
	
	public void setDragSource(){
		if(enableDragSource) {
            listInventoryTable.setDragMode(TableDragMode.ROW);
        }
	}
	
	public InventoryTableDropHandler getInventoryTableDropHandler(){
		return inventoryTableDropHandler;
	}

}

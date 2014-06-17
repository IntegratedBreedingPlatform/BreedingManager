package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

@Configurable
public class ListInventoryTable extends TableWithSelectAllLayout implements InitializingBean {

	private static final long serialVersionUID = 1L;

	public static String TAG_ID = "Tag";
	
	public static Class<?> ENTRY_NUMBER_COLUMN_TYPE = Integer.class;
	public static String ENTRY_NUMBER_COLUMN_ID = "#";
	
	public static Class<?> DESIGNATION_COLUMN_TYPE = String.class;
	public static String DESIGNATION_COLUMN_ID = "DESIGNATION";
	
	public static Class<?> LOCATION_COLUMN_TYPE = String.class;
	public static String LOCATION_COLUMN_ID = "LOCATION";
	
	public static Class<?> UNITS_COLUMN_TYPE = String.class;
	public static String UNITS_COLUMN_ID = "UNITS";

	public static Class<?> AVAIL_COLUMN_TYPE = Integer.class;
	public static String AVAIL_COLUMN_ID = "AVAIL";
	
	public static Class<?> TOTAL_COLUMN_TYPE = Integer.class;
	public static String TOTAL_COLUMN_ID = "TOTAL";
	
	public static Class<?> RESERVED_COLUMN_TYPE = Integer.class;
	public static String RESERVED_COLUMN_ID = "RES";
	
	public static Class<?> NEW_RESERVATION_COLUMN_TYPE = Integer.class;
	public static String NEW_RESERVATION_COLUMN_ID = "NEW RES";
	
	public static Class<?> COMMENT_COLUMN_TYPE = String.class;
	public static String COMMENT_COLUMN_ID = "COMMENT";
	
	public static Class<?> LOT_ID_COLUMN_TYPE = Integer.class;
	public static String LOT_ID_COLUMN_ID = "LOT ID";
	
	private Table listInventoryTable;
	private Integer listId;
	
	@Autowired
	GermplasmListManager germplasmListManager;
	
	@Autowired
	private InventoryDataManager inventoryDataManager;
	
	public ListInventoryTable() {
		super(TAG_ID);
	}
	
	public ListInventoryTable(Integer listId) {
		super(TAG_ID);
		this.listId = listId;
	}
	
	@Override
	public void instantiateComponents() {
		
		super.instantiateComponents();
		
		listInventoryTable = this.getTable();
		
		listInventoryTable.setHeight("480px");
		listInventoryTable.setWidth("100%");
		listInventoryTable.setColumnCollapsingAllowed(true);
		listInventoryTable.setColumnReorderingAllowed(false);
		
		listInventoryTable.addContainerProperty(ENTRY_NUMBER_COLUMN_ID, ENTRY_NUMBER_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(LOCATION_COLUMN_ID, LOCATION_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(UNITS_COLUMN_ID, UNITS_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(AVAIL_COLUMN_ID, AVAIL_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(TOTAL_COLUMN_ID, TOTAL_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(RESERVED_COLUMN_ID, RESERVED_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(NEW_RESERVATION_COLUMN_ID, NEW_RESERVATION_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(COMMENT_COLUMN_ID, COMMENT_COLUMN_TYPE, "");
		listInventoryTable.addContainerProperty(LOT_ID_COLUMN_ID, LOT_ID_COLUMN_TYPE, "");
		
	}

	public void loadInventoryData(){
		if(listId!=null){
			try {
				List<InventoryDetails> inventoryDetails = inventoryDataManager.getInventoryDetailsByGermplasmList(listId);
				displayInventoryDetails(inventoryDetails);
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void loadInventoryData(List<Integer> gids){
		try {
			List<InventoryDetails> inventoryDetails = inventoryDataManager.getInventoryDetailsByGids(gids);
			displayInventoryDetails(inventoryDetails);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}	
	
	private void displayInventoryDetails(List<InventoryDetails> inventoryDetails){
		listInventoryTable.removeAllItems();
		for(InventoryDetails inventoryDetail : inventoryDetails){
			
			Item newItem = listInventoryTable.addItem(inventoryDetail.getEntryId());
			newItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(inventoryDetail.getEntryId());
			newItem.getItemProperty(LOCATION_COLUMN_ID).setValue(inventoryDetail.getLocationName());
			newItem.getItemProperty(UNITS_COLUMN_ID).setValue(inventoryDetail.getScaleName());
			//newItem.getItemProperty(AVAIL_COLUMN_ID).setValue("");
			newItem.getItemProperty(TOTAL_COLUMN_ID).setValue(inventoryDetail.getAmount());
			//newItem.getItemProperty(RESERVED_COLUMN_ID).setValue("");
			//newItem.getItemProperty(NEW_RESERVATION_COLUMN_ID).setValue("");
			newItem.getItemProperty(COMMENT_COLUMN_ID).setValue(inventoryDetail.getComment());
			newItem.getItemProperty(LOT_ID_COLUMN_ID).setValue(inventoryDetail.getLotId());
		}
	}
}

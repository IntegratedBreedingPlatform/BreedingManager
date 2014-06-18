package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickListener;

@Configurable
public class ListInventoryTable extends TableWithSelectAllLayout implements InitializingBean {

	private static final long serialVersionUID = 1L;

	public static Class<?> TAG_COLUMN_TYPE = CheckBox.class;
  	public static String TAG_COLUMN_ID="Tag";
	
	public static Class<?> ENTRY_NUMBER_COLUMN_TYPE = Integer.class;
	public static String ENTRY_NUMBER_COLUMN_ID = "#";
	
	public static Class<?> DESIGNATION_COLUMN_TYPE = String.class;
	public static String DESIGNATION_COLUMN_ID = "DESIGNATION";
	
	public static Class<?> LOCATION_COLUMN_TYPE = String.class;
	public static String LOCATION_COLUMN_ID = "LOCATION";
	
	public static Class<?> UNITS_COLUMN_TYPE = String.class;
	public static String UNITS_COLUMN_ID = "UNITS";

	public static Class<?> AVAIL_COLUMN_TYPE = Double.class;
	public static String AVAIL_COLUMN_ID = "AVAIL";
	
	public static Class<?> TOTAL_COLUMN_TYPE = Double.class;
	public static String TOTAL_COLUMN_ID = "TOTAL";
	
	public static Class<?> RESERVED_COLUMN_TYPE = Double.class;
	public static String RESERVED_COLUMN_ID = "RES";
	
	public static Class<?> COMMENT_COLUMN_TYPE = String.class;
	public static String COMMENT_COLUMN_ID = "COMMENT";
	
	public static Class<?> LOT_ID_COLUMN_TYPE = Integer.class;
	public static String LOT_ID_COLUMN_ID = "LOT_ID";
	
	private Table listInventoryTable;
	private Integer listId;
	
	@Autowired
	GermplasmListManager germplasmListManager;
	
	@Autowired
	private InventoryDataManager inventoryDataManager;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public ListInventoryTable() {
		super(TAG_COLUMN_ID);
	}
	
	public ListInventoryTable(Integer listId) {
		super(TAG_COLUMN_ID);
		this.listId = listId;
	}
	
	@Override
	public void instantiateComponents() {
		
		super.instantiateComponents();
		
		listInventoryTable = this.getTable();
		listInventoryTable.setImmediate(true);
		
		listInventoryTable.setHeight("480px");
		listInventoryTable.setWidth("100%");
		listInventoryTable.setColumnCollapsingAllowed(true);
		listInventoryTable.setColumnReorderingAllowed(false);
		
		listInventoryTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
		listInventoryTable.addContainerProperty(ENTRY_NUMBER_COLUMN_ID, ENTRY_NUMBER_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(DESIGNATION_COLUMN_ID, DESIGNATION_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(LOCATION_COLUMN_ID, LOCATION_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(UNITS_COLUMN_ID, UNITS_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(AVAIL_COLUMN_ID, AVAIL_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(TOTAL_COLUMN_ID, TOTAL_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(RESERVED_COLUMN_ID, RESERVED_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(COMMENT_COLUMN_ID, COMMENT_COLUMN_TYPE, null);
		listInventoryTable.addContainerProperty(LOT_ID_COLUMN_ID, LOT_ID_COLUMN_TYPE, null);
		
		messageSource.setColumnHeader(listInventoryTable, TAG_COLUMN_ID, Message.CHECK_ICON);
		messageSource.setColumnHeader(listInventoryTable, ENTRY_NUMBER_COLUMN_ID, Message.HASHTAG);
		messageSource.setColumnHeader(listInventoryTable, DESIGNATION_COLUMN_ID, Message.LISTDATA_DESIGNATION_HEADER);
		messageSource.setColumnHeader(listInventoryTable, LOCATION_COLUMN_ID, Message.LOCATION);
		messageSource.setColumnHeader(listInventoryTable, UNITS_COLUMN_ID, Message.UNITS);
		messageSource.setColumnHeader(listInventoryTable, AVAIL_COLUMN_ID, Message.AVAIL);
		messageSource.setColumnHeader(listInventoryTable, TOTAL_COLUMN_ID, Message.TOTAL);
		messageSource.setColumnHeader(listInventoryTable, RESERVED_COLUMN_ID, Message.RES);
		messageSource.setColumnHeader(listInventoryTable, COMMENT_COLUMN_ID, Message.COMM);
		messageSource.setColumnHeader(listInventoryTable, LOT_ID_COLUMN_ID, Message.LOT_ID);
	}

	public void loadInventoryData(){
		if(listId!=null){
			try {
				List<GermplasmListData> inventoryDetails = inventoryDataManager.getLotDetailsForList(listId,0,Integer.MAX_VALUE);
				displayInventoryDetails(inventoryDetails);
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void displayInventoryDetails(List<GermplasmListData> inventoryDetails){
		listInventoryTable.removeAllItems();
		for(GermplasmListData inventoryDetail : inventoryDetails){
			
			Integer entryId = inventoryDetail.getEntryId();
			String designation = inventoryDetail.getDesignation();
			
			ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();
			@SuppressWarnings("unchecked")
			List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>)listDataInventory.getLotRows();
			
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
				
    	   		newItem.getItemProperty(TAG_COLUMN_ID).setValue(itemCheckBox);
				newItem.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(entryId);
				newItem.getItemProperty(DESIGNATION_COLUMN_ID).setValue(designation);
				newItem.getItemProperty(LOCATION_COLUMN_ID).setValue(lotDetail.getLocationOfLot().getLname());
				newItem.getItemProperty(UNITS_COLUMN_ID).setValue(lotDetail.getScaleOfLot().getName());
				newItem.getItemProperty(AVAIL_COLUMN_ID).setValue(lotDetail.getAvailableLotBalance());
				newItem.getItemProperty(TOTAL_COLUMN_ID).setValue(lotDetail.getActualLotBalance());
				newItem.getItemProperty(RESERVED_COLUMN_ID).setValue(lotDetail.getReservedTotalForEntry());
				newItem.getItemProperty(COMMENT_COLUMN_ID).setValue(lotDetail.getCommentOfLot());
				newItem.getItemProperty(LOT_ID_COLUMN_ID).setValue(lotDetail.getLotId());
			}
		}
	}
}

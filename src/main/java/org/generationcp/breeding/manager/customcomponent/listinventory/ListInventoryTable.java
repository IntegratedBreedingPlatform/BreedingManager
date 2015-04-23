package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class ListInventoryTable extends TableWithSelectAllLayout implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(ListInventoryTable.class);
	
	private static final long serialVersionUID = 1L;
	
	protected Table listInventoryTable;
	protected Integer listId;
	
	@Autowired
	GermplasmListManager germplasmListManager;

	@Autowired
	GermplasmDataManager germplasmDataManager;
	
	@Autowired
	protected InventoryDataManager inventoryDataManager;
	
	@Autowired
	protected OntologyDataManager ontologyDataManager;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public ListInventoryTable(Integer listId) {
		super(ColumnLabels.TAG.getName());
		this.listId = listId;
	}
	
	@Override
	public void instantiateComponents() {
		
		super.instantiateComponents();
		
		listInventoryTable = this.getTable();
		listInventoryTable.setMultiSelect(true);
		listInventoryTable.setImmediate(true);
		
		listInventoryTable.setHeight("480px");
		listInventoryTable.setWidth("100%");
		listInventoryTable.setColumnCollapsingAllowed(true);
		listInventoryTable.setColumnReorderingAllowed(false);
		listInventoryTable.setSelectable(true);
		listInventoryTable.setMultiSelect(true);
		
		listInventoryTable.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.LOT_LOCATION.getName(), String.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.SCALE.getName(), String.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Double.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.TOTAL.getName(), Double.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.RESERVED.getName(), Double.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.NEWLY_RESERVED.getName(), Double.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.COMMENT.getName(), String.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		listInventoryTable.addContainerProperty(ColumnLabels.LOT_ID.getName(), Integer.class, null);
		
		listInventoryTable.setColumnHeader(ColumnLabels.TAG.getName(), messageSource.getMessage(Message.CHECK_ICON));
		listInventoryTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), messageSource.getMessage(Message.HASHTAG));
		listInventoryTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), ColumnLabels.DESIGNATION.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.LOT_LOCATION.getName(), ColumnLabels.LOT_LOCATION.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.SCALE.getName(), ColumnLabels.SCALE.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(), ColumnLabels.AVAILABLE_INVENTORY.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.TOTAL.getName(), ColumnLabels.TOTAL.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.RESERVED.getName(), ColumnLabels.RESERVED.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.NEWLY_RESERVED.getName(), ColumnLabels.NEWLY_RESERVED.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.COMMENT.getName(), ColumnLabels.COMMENT.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.STOCKID.getName(), ColumnLabels.STOCKID.getTermNameFromOntology(ontologyDataManager));
		listInventoryTable.setColumnHeader(ColumnLabels.LOT_ID.getName(), ColumnLabels.LOT_ID.getTermNameFromOntology(ontologyDataManager));
	}

	public void loadInventoryData(){
		if(listId!=null){
			try {
				List<GermplasmListData> inventoryDetails = inventoryDataManager.getLotDetailsForList(listId,0,Integer.MAX_VALUE);
				displayInventoryDetails(inventoryDetails);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		
	}
	
	public void displayInventoryDetails(List<GermplasmListData> inventoryDetails){
		
		listInventoryTable.removeAllItems();
		Container listInventoryContainer = listInventoryTable.getContainerDataSource();
		for(GermplasmListData inventoryDetail : inventoryDetails){
			
			Integer entryId = inventoryDetail.getEntryId();
			String designation = inventoryDetail.getDesignation();
			
			ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();
			@SuppressWarnings("unchecked")
			List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>)listDataInventory.getLotRows();
			
			if(lotDetails!=null){
				for(ListEntryLotDetails lotDetail : lotDetails){
					Item newItem = listInventoryContainer.addItem(lotDetail);
					
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
					
			   		Button desigButton = new Button(String.format("%s", designation), 
			   					new GidLinkButtonClickListener(inventoryDetail.getGid().toString(), true));
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

					String stockIds = lotDetail.getStockIds();
					Label stockIdsLbl = new Label(stockIds);
					stockIdsLbl.setDescription(stockIds);
					newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdsLbl);

					newItem.getItemProperty(ColumnLabels.LOT_ID.getName()).setValue(lotDetail.getLotId());
				}
			}
		}
		
	}
	
	public void updateListInventoryTableAfterSave(){
		loadInventoryData(); //reset
	}
	
	public void resetRowsForCancelledReservation(
			List<ListEntryLotDetails> lotDetailsToCancel, Integer listId) {
		
		for(ListEntryLotDetails lotDetail : lotDetailsToCancel){
			Item item = listInventoryTable.getItem(lotDetail);
			
			Double availColumn = (Double) item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
			Double reservedColumn = (Double) item.getItemProperty(ColumnLabels.RESERVED.getName()).getValue();
			Double newAvailVal = availColumn + reservedColumn; 
			
			lotDetail.setAvailableLotBalance(newAvailVal);
			item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(newAvailVal);
			item.getItemProperty(ColumnLabels.RESERVED.getName()).setValue(0);
			item.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(0);
		}
	}
	
	public boolean isSelectedEntriesHasReservation(List<ListEntryLotDetails> lotDetailsGid) {
		for(ListEntryLotDetails lotDetails : lotDetailsGid){
			Item item = listInventoryTable.getItem(lotDetails);
			Double resColumn = (Double)item.getItemProperty(ColumnLabels.RESERVED.getName()).getValue();
			Double newResColumn = (Double)item.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).getValue();
			
			if(resColumn > 0 || newResColumn > 0){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public List<ListEntryLotDetails> getSelectedLots(){
		Collection<ListEntryLotDetails> selectedEntries = (Collection<ListEntryLotDetails>) listInventoryTable.getValue();
		
		List<ListEntryLotDetails> lotsSeleted = new ArrayList<ListEntryLotDetails>();
		lotsSeleted.addAll(selectedEntries);
		return lotsSeleted;
	}
	
	public void setListId(Integer listId){
		this.listId = listId;
	}
	
	public Integer getListId(){
		return listId;
	}

	public void reset() {
		listInventoryTable.removeAllItems();
	}

	public void setMaxRows(int i) {
		listInventoryTable.setHeight("100%");
		listInventoryTable.setPageLength(i);
	}
	
}


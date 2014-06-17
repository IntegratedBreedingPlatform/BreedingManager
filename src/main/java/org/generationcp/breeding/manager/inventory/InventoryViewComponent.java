package org.generationcp.breeding.manager.inventory;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class InventoryViewComponent extends VerticalLayout implements InitializingBean, 
								InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private InventoryDataManager inventoryDataManager;
    
    private Integer listId;
	private Integer recordId; //lrecId
	private Integer gid;
	
	private Label description;
	private TableLayout lotEntriesLayout;
	private Table lotEntriesTable;

    private static final String LOT_LOCATION = "lotLocation";
    private static final String LOT_UNITS = "lotUnits";
    private static final String ACTUAL_BALANCE = "actualBalance";
    private static final String AVAILABLE_BALANCE = "availableBalance";
    private static final String RES_THIS_ENTRY = "res-this-entry";
    private static final String RES_OTHER_ENTRY = "res-other-entry";
    private static final String COMMENTS = "comments";
    private static final String LOT_ID = "lotId";
    
    private boolean isThereNoInventoryInfo;
    
    public InventoryViewComponent(Integer listId){
    	this.listId = listId;
    }

    public InventoryViewComponent(Integer listId, Integer recordId, Integer gid){
    	this.listId = listId;
    	this.recordId = recordId;
    	this.gid = gid;
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();		
	}
    
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void instantiateComponents() {
		description = new Label(messageSource.getMessage(Message.LOT_DETAILS_FOR_SELECTED_ENTRIES));
	  
		lotEntriesLayout = new TableLayout(Integer.MAX_VALUE,8);
		
		lotEntriesTable = lotEntriesLayout.getTable();
		lotEntriesTable.setWidth("100%");
		
		lotEntriesTable.addContainerProperty(LOT_LOCATION, String.class, null);
		lotEntriesTable.addContainerProperty(LOT_UNITS, String.class, null);
		lotEntriesTable.addContainerProperty(ACTUAL_BALANCE, Double.class, null);
		lotEntriesTable.addContainerProperty(AVAILABLE_BALANCE, Double.class, null);
		lotEntriesTable.addContainerProperty(RES_THIS_ENTRY, String.class, null);
		lotEntriesTable.addContainerProperty(RES_OTHER_ENTRY, String.class, null);
		lotEntriesTable.addContainerProperty(COMMENTS, String.class, null);
		lotEntriesTable.addContainerProperty(LOT_ID, Integer.class, null);
	
		messageSource.setColumnHeader(lotEntriesTable, LOT_LOCATION, Message.LOT_LOCATION);
		messageSource.setColumnHeader(lotEntriesTable, LOT_UNITS, Message.LOT_UNITS);
		messageSource.setColumnHeader(lotEntriesTable, ACTUAL_BALANCE, Message.ACTUAL_BALANCE);
		messageSource.setColumnHeader(lotEntriesTable, AVAILABLE_BALANCE, Message.AVAILABLE_BALANCE);
		messageSource.setColumnHeader(lotEntriesTable, RES_THIS_ENTRY, Message.RES_THIS_ENTRY);
		messageSource.setColumnHeader(lotEntriesTable, RES_OTHER_ENTRY, Message.RES_OTHER_ENTRY);
		messageSource.setColumnHeader(lotEntriesTable, COMMENTS, Message.COMMENTS);
		messageSource.setColumnHeader(lotEntriesTable, LOT_ID, Message.LOT_ID);
	}

	@Override
	public void initializeValues() {
		try {
			List<? extends LotDetails> lotDetailEntries = (listId != null && recordId != null)? 
					inventoryDataManager.getLotDetailsForListEntry(listId,recordId,gid) 
					: inventoryDataManager.getLotDetailsForGermplasm(gid);
			
			for(LotDetails lotEntry : lotDetailEntries){
				Item newItem = lotEntriesTable.addItem(lotEntry.getLotId());
				
				String lotLocation="";
				if(lotEntry.getLocationOfLot() != null){
					if(lotEntry.getLocationOfLot().getLname() != null){
						lotLocation = lotEntry.getLocationOfLot().getLname();
					}
				}
		   		newItem.getItemProperty(LOT_LOCATION).setValue(lotLocation);
		   		
		   		String scale="";
		   		if(lotEntry.getScaleOfLot() != null){
		   			if(lotEntry.getScaleOfLot().getName() != null){
		   				scale = lotEntry.getScaleOfLot().getName();
		   			}
		   		}
		   		newItem.getItemProperty(LOT_UNITS).setValue(scale);
		   		
		   		newItem.getItemProperty(ACTUAL_BALANCE).setValue(lotEntry.getActualLotBalance());
		   		newItem.getItemProperty(AVAILABLE_BALANCE).setValue(lotEntry.getAvailableLotBalance());
		   		
		   		if(listId != null && recordId != null){
		   			ListEntryLotDetails listEntrtlotDetail = (ListEntryLotDetails) lotEntry;
		   			newItem.getItemProperty(RES_THIS_ENTRY).setValue(listEntrtlotDetail.getReservedTotalForEntry());
			   		newItem.getItemProperty(RES_OTHER_ENTRY).setValue(listEntrtlotDetail.getReservedTotalForOtherEntries());
		   		}
		   		else{
		   			newItem.getItemProperty(RES_THIS_ENTRY).setValue("-");
			   		newItem.getItemProperty(RES_OTHER_ENTRY).setValue("-");
		   		}
		   		
		   		newItem.getItemProperty(COMMENTS).setValue(lotEntry.getCommentOfLot());
		   		newItem.getItemProperty(LOT_ID).setValue(lotEntry.getLotId());
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		setMargin(true);
		setSpacing(true);
		addComponent(description);
		addComponent(lotEntriesTable);
		
	}
	
	public boolean isThereNoInventoryInfo() {
		return isThereNoInventoryInfo;
	}
	
	@Override
    public void attach() {
        super.attach();
        updateLabels();
    }
	
	public Table getTable(){
		return lotEntriesTable;
	}
	
}

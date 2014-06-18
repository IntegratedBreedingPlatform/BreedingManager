package org.generationcp.breeding.manager.inventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;

import com.vaadin.ui.Component;

public class ReserveInventoryUtil implements Serializable {
	private static final long serialVersionUID = -8873586745031285056L;
	
	private ReserveInventorySource source;
	private List<ListEntryLotDetails> lotDetailList;
	private ReserveInventoryWindow reserveInventory;
	
	public ReserveInventoryUtil(ReserveInventorySource source, List<ListEntryLotDetails> lotDetailList) {
		super();
		this.source = source;
		this.lotDetailList = lotDetailList;
	}

	public void viewReserveInventoryWindow(){
		
		boolean isSingleScaled = isLotsSingleScaled();
		Map<String, List<ListEntryLotDetails>> scaleGrouping = getScaleGrouping();
		
		reserveInventory = new ReserveInventoryWindow(source, scaleGrouping, isSingleScaled);
		source.addReserveInventoryWindow(reserveInventory);
	}
	
	public Map<String, List<ListEntryLotDetails>> getScaleGrouping(){
		Map<String, List<ListEntryLotDetails>> scaleGrouping = new HashMap<String,List<ListEntryLotDetails>>();
		
		for(ListEntryLotDetails lot: lotDetailList){
			String scale = lot.getScaleOfLot().getName();
			if(!scaleGrouping.containsKey(scale)){
				List<ListEntryLotDetails> lotList = new ArrayList<ListEntryLotDetails>();
				lotList.add(lot);
				scaleGrouping.put(scale, lotList);
			}
			else{//if the scale is already existing only add the lot to the lotlist group by scale
				List<ListEntryLotDetails> lotList = scaleGrouping.get(scale);
				lotList.add(lot);
			}
		}
		
		return scaleGrouping;
	}
	
	private boolean isLotsSingleScaled() {
		Integer firstUnit = lotDetailList.get(0).getScaleId();
		for(ListEntryLotDetails lot : lotDetailList){
			if(!firstUnit.equals(lot.getScaleId())){
				return false;
			}
		}
		return true;
	}
	

}

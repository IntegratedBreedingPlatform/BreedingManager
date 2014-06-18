package org.generationcp.breeding.manager.inventory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ReserveInventoryAction implements Serializable {
	private static final long serialVersionUID = -6868930047867345575L;
	
	@Autowired
	private InventoryDataManager inventoryDataManager;
	
	private Map<ListEntryLotDetails,Double> validLotReservations;
	private Map<ListEntryLotDetails,Double> invalidLotReservations;
	
	private ReserveInventorySource source;
	
	private ReservationStatusWindow reservationStatus;
	
	public ReserveInventoryAction(ReserveInventorySource source) {
		super();
		this.source = source;
	}
	
	public void validateReservations(Map<Double,List<ListEntryLotDetails>> reservations){
		
		//reset allocation
		validLotReservations = new HashMap<ListEntryLotDetails,Double>();
		invalidLotReservations = new HashMap<ListEntryLotDetails,Double>();
		
		for(Map.Entry<Double, List<ListEntryLotDetails>> entry : reservations.entrySet()){
			List<ListEntryLotDetails> lotList = entry.getValue();
			Double amountReserved = entry.getKey();
			
			for(ListEntryLotDetails lot : lotList){
				Double availBalance = lot.getAvailableLotBalance();
				if(availBalance < amountReserved){
					invalidLotReservations.put(lot, amountReserved);
				}
				else{
					validLotReservations.put(lot, amountReserved);
				}
			}
		}
		
		if(invalidLotReservations.size() > 0){//if there is an invalid reservation
			reservationStatus = new ReservationStatusWindow(invalidLotReservations);
			source.addReservationStatusWindow(reservationStatus);	
		}
		source.updateListInventoryTable(validLotReservations);
	}

	// SETTERS AND GETTERS
	public Map<ListEntryLotDetails, Double> getValidLotReservations() {
		return validLotReservations;
	}

	public Map<ListEntryLotDetails, Double> getInvalidLotReservations() {
		return invalidLotReservations;
	}
}

package org.generationcp.breeding.manager.inventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListComponent;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.workbench.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Window;

@Configurable
public class ReserveInventoryAction implements Serializable {
	private static final long serialVersionUID = -6868930047867345575L;
	
	@Autowired
	private InventoryDataManager inventoryDataManager;
	
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private UserDataManager userDataManager;
    
	private Map<ListEntryLotDetails,Double> validLotReservations;
	private Map<ListEntryLotDetails,Double> invalidLotReservations;
	
	private ReserveInventorySource source;
	
	private ReservationStatusWindow reservationStatus;

    private Integer wbUserId;
    private Project project;
    private Integer ibdbUserId;
	
	public ReserveInventoryAction(ReserveInventorySource source) {
		super();
		this.source = source;
	}
	
	public void validateReservations(Map<Double,List<ListEntryLotDetails>> reservations){
		
		//reset allocation
		validLotReservations = new HashMap<ListEntryLotDetails,Double>();
		invalidLotReservations = new HashMap<ListEntryLotDetails,Double>();
		
		Map<Integer,Double> duplicatedLots = getTotalReserveAmountPerLot(reservations); 
		
		List<Integer> checkedLots = new ArrayList<Integer>();
		
		for(Map.Entry<Double, List<ListEntryLotDetails>> entry : reservations.entrySet()){
			List<ListEntryLotDetails> lotList = entry.getValue();
			Double amountReserved = entry.getKey();;
			
			for(ListEntryLotDetails lot : lotList){
				Double availBalance = lot.getAvailableLotBalance();
				
				if(checkedLots.contains(lot.getLotId())){ // duplicated lots mapped to GID that has multiple entries in list entries
					Double totalAmountReserved = duplicatedLots.get(lot.getLotId());
					
					if(availBalance < totalAmountReserved){
						removeAllLotfromReservationLists(lot.getLotId());
						invalidLotReservations.put(lot, totalAmountReserved);
					}
					else{
						validLotReservations.put(lot, amountReserved);
					}
				}
				else{
					if(availBalance < amountReserved){
						invalidLotReservations.put(lot, amountReserved);
					}
					else{
						validLotReservations.put(lot, amountReserved);
					}
				}
				
				checkedLots.add(lot.getLotId()); //marked all checked lots
			}	
		}
		
		boolean withInvalidReservations = false;
		
		if(validLotReservations.size()>0 && invalidLotReservations.size() > 0){//if there is an invalid reservation
			reservationStatus = new ReservationStatusWindow(invalidLotReservations);
			source.addReservationStatusWindow(reservationStatus);
			withInvalidReservations = true;
		}
		
		source.updateListInventoryTable(validLotReservations, withInvalidReservations);
	}
	
	private void removeAllLotfromReservationLists(Integer lotId) {
		List<ListEntryLotDetails> lotDetails = new ArrayList<ListEntryLotDetails>();
		lotDetails.addAll(validLotReservations.keySet());
		lotDetails.addAll(invalidLotReservations.keySet());
		
		for(ListEntryLotDetails lot : lotDetails){
			if(lot.getLotId() == lotId){
				validLotReservations.remove(lot);
				invalidLotReservations.remove(lot);
			}
		}
		
	}

	private Map<Integer, Double> getTotalReserveAmountPerLot(
			Map<Double, List<ListEntryLotDetails>> reservations) {
		Map<Integer,Double> duplicatedLots = new HashMap<Integer,Double>();
		
		for(Map.Entry<Double, List<ListEntryLotDetails>> entry: reservations.entrySet()){
			List<ListEntryLotDetails> lotList = entry.getValue();
			Double amountReserved = entry.getKey();
			
			for(ListEntryLotDetails lot : lotList){
				Integer lotId = lot.getLotId();
				if(duplicatedLots.containsKey(lotId)){//sum up the reservations
					Double totalAmount = Double.valueOf(duplicatedLots.get(lotId)) + amountReserved;
					
					duplicatedLots.remove(lotId);
					duplicatedLots.put(lotId, totalAmount);
				}
				else{
					duplicatedLots.put(lotId, amountReserved);
				}
			}
		}
		
		return duplicatedLots;
	}

	public boolean saveReserveTransactions(Map<ListEntryLotDetails, Double> validReservationsToSave, Integer listId){
		List<Transaction> reserveTransactionList = new ArrayList<Transaction>();
		try {
			//userId
			retrieveIbdbUserId();
			
			for(Map.Entry<ListEntryLotDetails, Double> entry: validReservationsToSave.entrySet()){
				ListEntryLotDetails lotDetail = entry.getKey();
				
				Integer lotId = lotDetail.getLotId();
				Integer transactionDate = Util.getCurrentDate();
				Integer transacStatus = 0;
				Double amountToReserve = -1 * entry.getValue(); //since this is a reserve transaction
				String comments = "";
				Integer commitmentDate = transactionDate;
				String sourceType = "LIST";
				Integer sourceId = listId;// TODO
				Integer lrecId = lotDetail.getId(); 
				Double prevAmount = Double.valueOf(0); // TODO still needs to verify the final value, for now set to 0
				Integer personId = getPersonIdByUserId(ibdbUserId);

				Transaction reserveTransaction = new Transaction();
				
				reserveTransaction.setUserId(ibdbUserId);
				
				Lot lot = new Lot(lotId);
				reserveTransaction.setLot(lot);
				
				reserveTransaction.setTransactionDate(transactionDate);
				reserveTransaction.setStatus(transacStatus);
				reserveTransaction.setQuantity(amountToReserve);
				reserveTransaction.setComments(comments);
				reserveTransaction.setCommitmentDate(commitmentDate);
				reserveTransaction.setSourceType(sourceType);
				reserveTransaction.setSourceId(sourceId);
				reserveTransaction.setSourceRecordId(lrecId);
				reserveTransaction.setPreviousAmount(prevAmount);
				reserveTransaction.setPersonId(personId);
				
				reserveTransactionList.add(reserveTransaction);
			}
			
			inventoryDataManager.addTransactions(reserveTransactionList);
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public Integer getPersonIdByUserId(Integer userId){
		Integer personId = 0;
		try {
			 personId = userDataManager.getUserById(userId).getPersonid();
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return personId;
	}
	
	private void retrieveIbdbUserId() throws MiddlewareQueryException {
        this.wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
        this.project = workbenchDataManager.getLastOpenedProject(wbUserId);
        this.ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, this.project.getProjectId());
    }

	// SETTERS AND GETTERS
	public Map<ListEntryLotDetails, Double> getValidLotReservations() {
		return validLotReservations;
	}

	public Map<ListEntryLotDetails, Double> getInvalidLotReservations() {
		return invalidLotReservations;
	}
}

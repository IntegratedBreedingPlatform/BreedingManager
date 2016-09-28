
package org.generationcp.breeding.manager.inventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.ReservedInventoryKey;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class ReserveInventoryAction implements Serializable {

	private static final long serialVersionUID = -6868930047867345575L;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private UserDataManager userDataManager;

	@Resource
	private ContextUtil contextUtil;

	private final ReserveInventorySource source;

	public ReserveInventoryAction(final ReserveInventorySource source) {
		super();
		this.source = source;
	}

	public void validateReservations(Map<ReservationRowKey, List<ListEntryLotDetails>> reservations) {

		// reset allocation
		Map<ListEntryLotDetails, Double> validLotReservations = new HashMap<>();
		Map<ListEntryLotDetails, Double> invalidLotReservations = new HashMap<>();

		Map<Integer, Double> duplicatedLots = this.getTotalReserveAmountPerLot(reservations);

		List<Integer> checkedLots = new ArrayList<>();

		for (Map.Entry<ReservationRowKey, List<ListEntryLotDetails>> entry : reservations.entrySet()) {

			List<ListEntryLotDetails> lotList = entry.getValue();
			ReservationRowKey key = entry.getKey();
			Boolean isPrepareAllSeeds = key.getIsPreapareAllSeeds();

			for (ListEntryLotDetails lot : lotList) {

				Double availBalance = lot.getAvailableLotBalance();
				Double amountReserved = isPrepareAllSeeds ? availBalance : key.getAmountToReserve();

				if(GermplasmInventory.RESERVED.equals(lot.getWithdrawalStatus())){
					invalidLotReservations.put(lot, amountReserved);
				} else if (checkedLots.contains(lot.getLotId())) {
					// duplicated lots mapped to GID that has multiple entries in list entries
					Double totalAmountReserved = duplicatedLots.get(lot.getLotId());
					if (availBalance < totalAmountReserved) {
						this.removeAllLotfromReservationLists(validLotReservations, invalidLotReservations, lot.getLotId());
						invalidLotReservations.put(lot, totalAmountReserved);
					} else {
						validLotReservations.put(lot, amountReserved);
					}
				} else if (availBalance < amountReserved) {
					invalidLotReservations.put(lot, amountReserved);
				} else {
					validLotReservations.put(lot, amountReserved);
				}
				// marked all checked lots
				checkedLots.add(lot.getLotId());
			}
		}

		boolean withInvalidReservations = false;
		if (!invalidLotReservations.isEmpty()) {
			// if there is an invalid reservation
			ReservationStatusWindow reservationStatus = new ReservationStatusWindow(invalidLotReservations);
			reservationStatus.setDebugId("reservationStatus");
			this.source.addReservationStatusWindow(reservationStatus);
			withInvalidReservations = true;
		}

		this.source.updateListInventoryTable(validLotReservations, withInvalidReservations);
	}

	private void removeAllLotfromReservationLists(Map<ListEntryLotDetails, Double> validLotReservations, Map<ListEntryLotDetails, Double> invalidLotReservations, Integer lotId) {
		List<ListEntryLotDetails> lotDetails = new ArrayList<>();
		lotDetails.addAll(validLotReservations.keySet());
		lotDetails.addAll(invalidLotReservations.keySet());

		for (ListEntryLotDetails lot : lotDetails) {
			if (Objects.equals(lot.getLotId(), lotId)) {
				validLotReservations.remove(lot);
				invalidLotReservations.remove(lot);
			}
		}
	}

	private Map<Integer, Double> getTotalReserveAmountPerLot(Map<ReservationRowKey, List<ListEntryLotDetails>> reservations) {
		Map<Integer, Double> duplicatedLots = new HashMap<>();

		for (Map.Entry<ReservationRowKey, List<ListEntryLotDetails>> entry : reservations.entrySet()) {
			List<ListEntryLotDetails> lotList = entry.getValue();
			ReservationRowKey key = entry.getKey();
			Double amountReserved = key.getAmountToReserve();

			for (ListEntryLotDetails lot : lotList) {
				Integer lotId = lot.getLotId();
				if (duplicatedLots.containsKey(lotId)) {
					// sum up the reservations
					Double totalAmount = duplicatedLots.get(lotId) + amountReserved;

					duplicatedLots.remove(lotId);
					duplicatedLots.put(lotId, totalAmount);
				} else {
					duplicatedLots.put(lotId, amountReserved);
				}
			}
		}

		return duplicatedLots;
	}

	public boolean saveReserveTransactions(Map<ListEntryLotDetails, Double> validReservationsToSave, Integer listId) {
		List<Transaction> reserveTransactionList = new ArrayList<Transaction>();
		for (Map.Entry<ListEntryLotDetails, Double> entry : validReservationsToSave.entrySet()) {
			ListEntryLotDetails lotDetail = entry.getKey();

			Integer lotId = lotDetail.getLotId();
			Integer transactionDate = DateUtil.getCurrentDateAsIntegerValue();
			Integer transacStatus = 0;

			// since this is a reserve transaction
			Double amountToReserve = -1 * entry.getValue();
			String comments = "";
			String sourceType = "LIST";
			Integer lrecId = lotDetail.getId();

			Double prevAmount = 0D;
			final Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();
			final User userById = this.userDataManager.getUserById(ibdbUserId);


			Transaction reserveTransaction = new Transaction();

			reserveTransaction.setUserId(ibdbUserId);

			Lot lot = new Lot(lotId);
			reserveTransaction.setLot(lot);

			reserveTransaction.setTransactionDate(transactionDate);
			reserveTransaction.setStatus(transacStatus);
			reserveTransaction.setQuantity(amountToReserve);
			reserveTransaction.setComments(comments);
			reserveTransaction.setCommitmentDate(transactionDate);
			reserveTransaction.setSourceType(sourceType);
			reserveTransaction.setSourceId(listId);
			reserveTransaction.setSourceRecordId(lrecId);
			reserveTransaction.setPreviousAmount(prevAmount);
			reserveTransaction.setPersonId(userById.getPersonid());

			reserveTransactionList.add(reserveTransaction);
		}

		this.inventoryDataManager.addTransactions(reserveTransactionList);
		return true;
	}

	private List<ReservedInventoryKey> getLotIdAndLrecId(List<ListEntryLotDetails> listEntries) {
		List<ReservedInventoryKey> lrecIds = new ArrayList<ReservedInventoryKey>();
		int id = 1;
		for (ListEntryLotDetails lotDetail : listEntries) {
			ReservedInventoryKey key = new ReservedInventoryKey(id, lotDetail.getId(), lotDetail.getLotId());
			if (!lrecIds.contains(key)) {
				lrecIds.add(key);
				id++;
			}
		}
		return lrecIds;
	}

	public void cancelReservations(final List<ListEntryLotDetails> listEntries) {
		this.inventoryDataManager.cancelReservedInventory(this.getLotIdAndLrecId(listEntries));
	}
	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setUserDataManager(final UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}
}

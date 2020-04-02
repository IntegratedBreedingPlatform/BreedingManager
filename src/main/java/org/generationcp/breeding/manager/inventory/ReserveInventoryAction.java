package org.generationcp.breeding.manager.inventory;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.ReservedInventoryKey;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configurable
public class ReserveInventoryAction implements Serializable {

	private static final long serialVersionUID = -6868930047867345575L;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private UserService userService;

	private final ReserveInventorySource source;

	public ReserveInventoryAction(final ReserveInventorySource source) {
		super();
		this.source = source;
	}

	public void validateReservations(final Map<ReservationRowKey, List<ListEntryLotDetails>> reservations, final String notes, final Boolean commitStatus) {

		// reset allocation
		final Map<ListEntryLotDetails, Double> validLotReservations = new HashMap<>();
		final Map<ListEntryLotDetails, Double> invalidLotReservations = new HashMap<>();

		final Map<Integer, Double> duplicatedLots = this.getTotalReserveAmountPerLot(reservations);

		final List<Integer> checkedLots = new ArrayList<>();

		for (final Map.Entry<ReservationRowKey, List<ListEntryLotDetails>> entry : reservations.entrySet()) {

			final List<ListEntryLotDetails> lotList = entry.getValue();
			final ReservationRowKey key = entry.getKey();
			final Boolean isPrepareAllSeeds = key.getIsPreapareAllSeeds();

			for (final ListEntryLotDetails lot : lotList) {

				final Double availBalance = lot.getAvailableLotBalance();
				final Double amountReserved = isPrepareAllSeeds ? availBalance : key.getAmountToReserve();
				lot.setCommentOfLot(notes);
				lot.setTransactionStatus(commitStatus);
				if (GermplasmInventory.RESERVED.equals(lot.getWithdrawalStatus())) {
					invalidLotReservations.put(lot, amountReserved);
				} else if (checkedLots.contains(lot.getLotId())) {
					// duplicated lots mapped to GID that has multiple entries in list entries
					final Double totalAmountReserved = duplicatedLots.get(lot.getLotId());
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
			final ReservationStatusWindow reservationStatus = new ReservationStatusWindow(invalidLotReservations);
			reservationStatus.setDebugId("reservationStatus");
			this.source.addReservationStatusWindow(reservationStatus);
			withInvalidReservations = true;
		}

		this.source.updateListInventoryTable(validLotReservations, withInvalidReservations);
	}

	private void removeAllLotfromReservationLists(
		final Map<ListEntryLotDetails, Double> validLotReservations,
			final Map<ListEntryLotDetails, Double> invalidLotReservations, final Integer lotId) {
		final List<ListEntryLotDetails> lotDetails = new ArrayList<>();
		lotDetails.addAll(validLotReservations.keySet());
		lotDetails.addAll(invalidLotReservations.keySet());

		for (final ListEntryLotDetails lot : lotDetails) {
			if (Objects.equals(lot.getLotId(), lotId)) {
				validLotReservations.remove(lot);
				invalidLotReservations.remove(lot);
			}
		}
	}

	private Map<Integer, Double> getTotalReserveAmountPerLot(final Map<ReservationRowKey, List<ListEntryLotDetails>> reservations) {
		final Map<Integer, Double> duplicatedLots = new HashMap<>();

		for (final Map.Entry<ReservationRowKey, List<ListEntryLotDetails>> entry : reservations.entrySet()) {
			final List<ListEntryLotDetails> lotList = entry.getValue();
			final ReservationRowKey key = entry.getKey();
			final Double amountReserved = key.getAmountToReserve();

			for (final ListEntryLotDetails lot : lotList) {
				final Integer lotId = lot.getLotId();
				if (duplicatedLots.containsKey(lotId)) {
					// sum up the reservations
					final Double totalAmount = duplicatedLots.get(lotId) + amountReserved;

					duplicatedLots.remove(lotId);
					duplicatedLots.put(lotId, totalAmount);
				} else {
					duplicatedLots.put(lotId, amountReserved);
				}
			}
		}

		return duplicatedLots;
	}

	public boolean saveReserveTransactions(final Map<ListEntryLotDetails, Double> validReservationsToSave, final Integer listId) {
		final Map<Integer, Double> availableBalanceMap = this.retrieveLatestAvailableBalance(validReservationsToSave, listId);
		final List<Transaction> reserveTransactionList = new ArrayList<Transaction>();
		for (final Map.Entry<ListEntryLotDetails, Double> entry : validReservationsToSave.entrySet()) {
			final ListEntryLotDetails lotDetail = entry.getKey();

			final Integer lotId = lotDetail.getLotId();
			final Date transactionDate = DateUtil.getCurrentDate();
			Integer transacStatus = TransactionStatus.PENDING.getIntValue();
			if (lotDetail.getTransactionStatus()) {
				transacStatus =  TransactionStatus.CONFIRMED.getIntValue();
			}

			// since this is a reserve transaction
			final Double amountToReserve = -1 * entry.getValue();
			final String comments = lotDetail.getCommentOfLot();
			final String sourceType = "LIST";
			final Integer lrecId = lotDetail.getId();

			final Double prevAmount = 0D;
			final WorkbenchUser workbenchUser = this.userService.getUserById(this.contextUtil.getCurrentWorkbenchUserId());

			final Double availableBalance = availableBalanceMap.get(lotId);
			final Double reservationAmount = entry.getValue();

			if (availableBalance <= 0.0 || reservationAmount > availableBalance) {
				return false;
			}

			final Transaction reserveTransaction = new Transaction();

			reserveTransaction.setUserId(workbenchUser.getUserid());

			final Lot lot = new Lot(lotId);
			reserveTransaction.setLot(lot);

			reserveTransaction.setTransactionDate(transactionDate);
			reserveTransaction.setStatus(transacStatus);
			reserveTransaction.setQuantity(amountToReserve);
			reserveTransaction.setComments(comments);

			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(transactionDate);
			final Integer year = calendar.get(Calendar.YEAR);
			final Integer month = calendar.get(Calendar.MONTH) + 1;
			final Integer day = calendar.get(Calendar.DAY_OF_MONTH);
			final String dateString = year.toString() + month.toString() + day.toString();

			reserveTransaction.setCommitmentDate(Integer.valueOf(dateString));
			reserveTransaction.setSourceType(sourceType);
			reserveTransaction.setSourceId(listId);
			reserveTransaction.setSourceRecordId(lrecId);
			reserveTransaction.setPreviousAmount(prevAmount);

			reserveTransactionList.add(reserveTransaction);
		}

		this.inventoryDataManager.addTransactions(reserveTransactionList);
		return true;
	}

	private Map<Integer, Double> retrieveLatestAvailableBalance(
		final Map<ListEntryLotDetails, Double> validReservationsToSave, final Integer listId) {
		final Map<Integer, Double> availableBalanceMap = new HashMap<>();

		if(!CollectionUtils.isEmpty(validReservationsToSave)) {

			final List<GermplasmListData> inventoryData = this.inventoryDataManager.getLotDetailsForList(listId, 0, Integer.MAX_VALUE);

			if (!CollectionUtils.isEmpty(inventoryData)) {
				for (final GermplasmListData germplasmListData : inventoryData) {

					final ListDataInventory listDataInventory = germplasmListData.getInventoryInfo();
					final List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>) listDataInventory.getLotRows();

					if (lotDetails != null) {
						for (final ListEntryLotDetails lotDetail : lotDetails) {
							if(ListDataInventory.RESERVED.equals(lotDetail.getWithdrawalStatus())){
								availableBalanceMap.put(lotDetail.getLotId(), 0.0);
							} else {
								final Double totalAvailableBalance = lotDetail.getAvailableLotBalance();
								availableBalanceMap.put(lotDetail.getLotId(), totalAvailableBalance);
							}

						}
					}
				}
			}
		}

		return availableBalanceMap;
	}

	private List<ReservedInventoryKey> getLotIdAndLrecId(final List<ListEntryLotDetails> listEntries) {
		final List<ReservedInventoryKey> lrecIds = new ArrayList<ReservedInventoryKey>();
		int id = 1;
		for (final ListEntryLotDetails lotDetail : listEntries) {
			final ReservedInventoryKey key = new ReservedInventoryKey(id, lotDetail.getId(), lotDetail.getLotId());
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

	public void setUserService(final UserService userService) {
		this.userService = userService;
	}

}

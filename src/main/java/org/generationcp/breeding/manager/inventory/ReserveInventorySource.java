
package org.generationcp.breeding.manager.inventory;

import java.util.Map;

import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;

public interface ReserveInventorySource {

	public void updateListInventoryTable(Map<ListEntryLotDetails, Double> validReservations, boolean withInvalidReservations);

	public void addReserveInventoryWindow(ReserveInventoryWindow reserveInventory);

	public void addReservationStatusWindow(ReservationStatusWindow reservationStatus);

	public void removeReserveInventoryWindow(ReserveInventoryWindow reserveInventory);

	public void removeReservationStatusWindow(ReservationStatusWindow reservationStatus);

}

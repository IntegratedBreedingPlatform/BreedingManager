
package org.generationcp.breeding.manager.inventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;

public class ReserveInventoryUtil implements Serializable {

	private static final long serialVersionUID = -8873586745031285056L;

	private final ReserveInventorySource source;
	private final List<ListEntryLotDetails> lotDetailList;
	private ReserveInventoryWindow reserveInventory;

	public ReserveInventoryUtil(ReserveInventorySource source, List<ListEntryLotDetails> lotDetailList) {
		super();
		this.source = source;
		this.lotDetailList = lotDetailList;
	}

	public void viewReserveInventoryWindow() {

		boolean isSingleScaled = this.isLotsSingleScaled();
		Map<String, List<ListEntryLotDetails>> scaleGrouping = this.getScaleGrouping();

		this.reserveInventory = new ReserveInventoryWindow(this.source, scaleGrouping, isSingleScaled);
		this.reserveInventory.setDebugId("reserveInventory");
		this.source.addReserveInventoryWindow(this.reserveInventory);
	}

	public Map<String, List<ListEntryLotDetails>> getScaleGrouping() {
		Map<String, List<ListEntryLotDetails>> scaleGrouping = new HashMap<String, List<ListEntryLotDetails>>();

		for (ListEntryLotDetails lot : this.lotDetailList) {
			String scale = lot.getScaleOfLot().getName();
			if (!scaleGrouping.containsKey(scale)) {
				List<ListEntryLotDetails> lotList = new ArrayList<ListEntryLotDetails>();
				lotList.add(lot);
				scaleGrouping.put(scale, lotList);
			} else {// if the scale is already existing only add the lot to the lotlist group by scale
				List<ListEntryLotDetails> lotList = scaleGrouping.get(scale);
				lotList.add(lot);
			}
		}

		return scaleGrouping;
	}

	private boolean isLotsSingleScaled() {
		Integer firstUnit = this.lotDetailList.get(0).getScaleId();
		for (ListEntryLotDetails lot : this.lotDetailList) {
			if (!firstUnit.equals(lot.getScaleId())) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLotsContainsScale(List<ListEntryLotDetails> lotDetailList){
		for (ListEntryLotDetails lot : lotDetailList) {
			if(!(lot.getScaleId() != null)){
				return false;
			}
		}
		return true;
	}

}

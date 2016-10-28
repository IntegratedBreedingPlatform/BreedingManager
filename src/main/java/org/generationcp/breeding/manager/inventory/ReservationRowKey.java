
package org.generationcp.breeding.manager.inventory;

public class ReservationRowKey {

	String scale;
	Double amountToReserve;
	Boolean isPreapareAllSeeds = false;

	public ReservationRowKey(String scale, Double amountToReserve, Boolean isPreapareAllSeeds) {
		super();
		this.scale = scale;
		this.amountToReserve = amountToReserve;
		this.isPreapareAllSeeds = isPreapareAllSeeds;
	}

	public Boolean getIsPreapareAllSeeds() {return isPreapareAllSeeds;}

	public String getScale() {
		return this.scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public Double getAmountToReserve() {
		return this.amountToReserve;
	}

	public void setAmountToReserve(Double amountToReserve) {
		this.amountToReserve = amountToReserve;
	}
}

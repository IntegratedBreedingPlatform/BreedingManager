package org.generationcp.breeding.manager.inventory;

public class ReserveInventoryActionFactory {

	public ReserveInventoryAction createInstance(final ReserveInventorySource source) {
		return new ReserveInventoryAction(source);
	}

}

package org.generationcp.breeding.manager.inventory.exception;

public class SeedInventoryImportException extends Exception {

	private static final long serialVersionUID = -4976888301866579819L;

	private static final String CAPTION = "Invalid Seed Inventory File";

	public SeedInventoryImportException(String message) {
		super(message);
	}

	public SeedInventoryImportException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public String getCaption() {
		return SeedInventoryImportException.CAPTION;
	}

}

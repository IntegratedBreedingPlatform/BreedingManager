package org.generationcp.breeding.manager.inventory.exception;

public class SeedInventoryExportException extends Exception {

	private static final long serialVersionUID = -4976888301866579819L;

	public SeedInventoryExportException(String message) {
		super(message);
	}

	public SeedInventoryExportException(String message, Throwable throwable) {
		super(message, throwable);
	}
}

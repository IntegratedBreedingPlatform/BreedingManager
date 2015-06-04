
package org.generationcp.breeding.manager.listimport.exceptions;

public class InvalidFileTypeImportException extends GermplasmImportException {

	private static final long serialVersionUID = -871459489607146082L;
	private static final String CAPTION = "Invalid Import File Type";

	public InvalidFileTypeImportException(String message) {
		super(message);
	}

	@Override
	public String getCaption() {
		return InvalidFileTypeImportException.CAPTION;
	}

}

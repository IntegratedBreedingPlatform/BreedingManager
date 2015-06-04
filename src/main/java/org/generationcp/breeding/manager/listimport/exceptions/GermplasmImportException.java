
package org.generationcp.breeding.manager.listimport.exceptions;

import com.vaadin.data.Validator.InvalidValueException;

public class GermplasmImportException extends InvalidValueException {

	private static final long serialVersionUID = -7666251545250713577L;
	private static final String CAPTION = "Invalid Import File";

	public GermplasmImportException(String message) {
		super(message);
	}

	public String getCaption() {
		return GermplasmImportException.CAPTION;
	}

}


package org.generationcp.breeding.manager.service;

import org.generationcp.breeding.manager.application.Message;

/**
 * Created by cyrus on 10/21/14.
 */
public class BreedingManagerSearchException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -5673878449319255027L;
	private final Message errorMessage;

	public BreedingManagerSearchException(Message errorMessage) {
		super(errorMessage.toString());
		this.errorMessage = errorMessage;
	}

	public BreedingManagerSearchException(Message errorMessage, Throwable cause) {
		super(errorMessage.toString(), cause);
		this.errorMessage = errorMessage;
	}

	public Message getErrorMessage() {
		return this.errorMessage;
	}

}

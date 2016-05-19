
package org.generationcp.breeding.manager.cross.study.h2h.main.util;

import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class HeadToHeadDataListExportException extends Exception {

	private static final long serialVersionUID = -1639961960516233500L;

	public HeadToHeadDataListExportException(String message) {
		super(message);
	}

	public HeadToHeadDataListExportException(String message, Throwable cause) {
		super(message, cause);
	}
}

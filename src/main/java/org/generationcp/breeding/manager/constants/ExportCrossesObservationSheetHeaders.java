
package org.generationcp.breeding.manager.constants;

public enum ExportCrossesObservationSheetHeaders {

	// order the values as they are expected to appear on file
	ENTRY_ID("ENTRY"), GID("GID"), ENTRY_CODE("ENTRY CODE"), DESIG("DESIGNATION"), CROSS("CROSS"), SOURCE("SOURCE"), FEMALE("FEMALE"), MALE(
			"MALE"), FEMALE_GID("FEMALE GID"), MALE_GID("MALE GID");

	private String value;

	private ExportCrossesObservationSheetHeaders(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}

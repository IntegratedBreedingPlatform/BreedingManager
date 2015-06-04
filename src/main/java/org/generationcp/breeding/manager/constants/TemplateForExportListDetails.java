
package org.generationcp.breeding.manager.constants;

public enum TemplateForExportListDetails {

	// order the values as they are expected to appear on file
	LIST_NAME("LIST NAME"), TITLE("LIST DESCRIPTION"), LIST_TYPE("LIST TYPE"), LIST_DATE("LIST DATE");

	private String value;

	private TemplateForExportListDetails(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}

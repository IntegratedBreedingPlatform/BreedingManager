
package org.generationcp.breeding.manager.constants;

public enum TemplateVariateHeader {

	// order the values as they are expected to appear on file
	VARIATE("VARIATE"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD"), DATA_TYPE("DATA TYPE"), VALUE(
			"") // missing in template file
	, SAMPLE_LEVEL("SAMPLE LEVEL");

	private String header;

	private TemplateVariateHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return this.header;
	}

}

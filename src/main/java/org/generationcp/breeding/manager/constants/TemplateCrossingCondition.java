
package org.generationcp.breeding.manager.constants;

public enum TemplateCrossingCondition {

	// order the values as they are expected to appear on file
	NID("NID"), BREEDER_NAME("BREEDER NAME"), BREEDER_ID("BREEDER ID"), SITE("SITE"), SITE_ID("SITE ID"), BREEDING_METHOD("BREEDING METHOD"), BREEDING_METHOD_ID(
			"BREEDING METHOD ID"), FEMALE_LIST_NAME("FEMALE LIST NAME"), FEMALE_LIST_ID("FEMALE LIST ID"), MALE_LIST_NAME("MALE LIST NAME"), MALE_LIST_ID(
			"MALE LIST ID");

	private String value;

	private TemplateCrossingCondition(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}

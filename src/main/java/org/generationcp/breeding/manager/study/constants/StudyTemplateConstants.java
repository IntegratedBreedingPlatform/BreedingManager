
package org.generationcp.breeding.manager.study.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Contains string constants used in study-related templates
 *
 * @author Darla Ani
 *
 */
public enum StudyTemplateConstants {

	// Tab names
	DESCRIPTION_TAB("Description"), OBSERVATION_TAB("Observation")

	// Study Detail section
	, STUDY("STUDY"), TITLE("TITLE"), PM_KEY("PMKEY"), OBJECTIVE("OBJECTIVE"), START_DATE("START DATE"), END_DATE("END DATE"), STUDY_TYPE(
			"STUDY TYPE")

	// Main section headers
	, CONDITION("CONDITION"), FACTOR("FACTOR"), CONSTANT("CONSTANT"), VARIATE("VARIATE")

	// Common column headers
	, DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD"), DATA_TYPE("DATA TYPE"), VALUE("VALUE"), LABEL(
			"LABEL"), NESTED_IN("NESTED IN"), SAMPLE_LEVEL("SAMPLE LEVEL"), EMPTY_HEADER("")

	// constants for main factors for phenotypic type
	, TRIAL_INSTANCE("TRIAL_INSTANCE"), GERMPLASM_ENTRY("GERMPLASM ENTRY"), FIELD_PLOT("FIELD PLOT"), NUMBER("NUMBER"), NESTED_NUMBER(
			"NESTED NUMBER"), ENUMERATED("ENUMERATED"), ;

	private String header;

	private StudyTemplateConstants(String header) {
		this.header = header;
	}

	public String getHeader() {
		return this.header;
	}

	public static List<StudyTemplateConstants> getConditionHeaders() {
		return Arrays.asList(CONDITION, DESCRIPTION, PROPERTY, SCALE, METHOD, DATA_TYPE, VALUE, LABEL);
	}

	public static List<StudyTemplateConstants> getFactorHeaders() {
		return Arrays.asList(FACTOR, DESCRIPTION, PROPERTY, SCALE, METHOD, DATA_TYPE, VALUE, LABEL);
	}

	public static List<StudyTemplateConstants> getConstantsHeaders() {
		return Arrays.asList(CONSTANT, DESCRIPTION, PROPERTY, SCALE, METHOD, DATA_TYPE, NESTED_IN, SAMPLE_LEVEL);
	}

	public static List<StudyTemplateConstants> getVariateHeaders() {
		return Arrays.asList(VARIATE, DESCRIPTION, PROPERTY, SCALE, METHOD, DATA_TYPE, EMPTY_HEADER, SAMPLE_LEVEL);
	}

}

package org.generationcp.browser.cross.study.constants;

public enum NumericTraitCriteria {
	DROP_TRAIT("Drop Trait")
	, KEEP_ALL("Keep All")
	, LESS_THAN("<")
	, LESS_THAN_EQUAL("<=")
	, EQUAL("=")
	, GREATER_THAN_EQUAL(">=")
	, GREATER_THAN(">")
	, BETWEEN("Between")
	, IN("In")
	, NOT_IN("Not in");
	
	private String label;
	
	private NumericTraitCriteria(String label){
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}

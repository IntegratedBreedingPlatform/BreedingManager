package org.generationcp.browser.cross.study.constants;

public enum CategoricalVariatesCondition {
	DROP_TRAIT("Drop Trait")
	, KEEP_ALL("Keep All")
	, IN("In")
	, NOT_IN("Not in");
	
	private String label;
	
	private CategoricalVariatesCondition(String label){
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}

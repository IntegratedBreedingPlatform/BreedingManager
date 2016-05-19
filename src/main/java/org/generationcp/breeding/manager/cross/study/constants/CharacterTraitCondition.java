
package org.generationcp.breeding.manager.cross.study.constants;

public enum CharacterTraitCondition {
	DROP_TRAIT("Drop Trait"), KEEP_ALL("Keep All"), IN("In"), NOT_IN("Not in");

	private String label;

	private CharacterTraitCondition(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}

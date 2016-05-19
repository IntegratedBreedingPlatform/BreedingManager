
package org.generationcp.breeding.manager.cross.study.constants;

public enum TraitWeight {
	IMPORTANT("Important", 20), CRITICAL("Critical", 40), DESIRABLE("Desirable", 10), IGNORED("Ignored", 0);

	private String label;
	private int weight;

	private TraitWeight(String label, int weight) {
		this.label = label;
		this.weight = weight;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getWeight() {
		return this.weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}

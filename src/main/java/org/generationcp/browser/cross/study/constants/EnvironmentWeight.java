package org.generationcp.browser.cross.study.constants;

public enum EnvironmentWeight {
	IMPORTANT("Important", 2)
	,CRITICAL("Critical", 3)
	,DESIRABLE("Desirable", 1)
	,IGNORED("Ignored", 0);
	
	private String label;
	private int weight;
	
	private EnvironmentWeight(String label, int weight){
		this.label = label;
		this.weight = weight;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}

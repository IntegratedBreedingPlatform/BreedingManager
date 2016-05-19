
package org.generationcp.breeding.manager.cross.study.h2h.pojos;

import java.io.Serializable;

public class TraitForComparison implements Serializable {

	private static final long serialVersionUID = -4335466707467584859L;

	private String name;
	private Integer numberOfEnvironments;

	public TraitForComparison(String name, Integer numberOfEnvironments) {
		this.name = name;
		this.numberOfEnvironments = numberOfEnvironments;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumberOfEnvironments() {
		return this.numberOfEnvironments;
	}

	public void setNumberOfEnvironments(Integer numberOfEnvironments) {
		this.numberOfEnvironments = numberOfEnvironments;
	}
}

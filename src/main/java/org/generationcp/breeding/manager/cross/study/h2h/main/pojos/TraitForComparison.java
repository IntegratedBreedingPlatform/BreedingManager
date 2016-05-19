
package org.generationcp.breeding.manager.cross.study.h2h.main.pojos;

import java.io.Serializable;

import org.generationcp.middleware.domain.h2h.TraitInfo;

public class TraitForComparison implements Serializable {

	private static final long serialVersionUID = -4335466707467584859L;

	private TraitInfo traitInfo;
	private Integer direction;
	private boolean isDisplay;

	public TraitForComparison(TraitInfo traitInfo, Integer direction) {
		this.traitInfo = traitInfo;
		this.direction = direction;
	}

	public boolean isDisplay() {
		return this.isDisplay;
	}

	public void setDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}

	public TraitInfo getTraitInfo() {
		return this.traitInfo;
	}

	public void setTraitInfo(TraitInfo traitInfo) {
		this.traitInfo = traitInfo;
	}

	public Integer getDirection() {
		return this.direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

}

package org.generationcp.browser.cross.study.h2h.main.pojos;

import java.io.Serializable;

import org.generationcp.middleware.domain.h2h.TraitInfo;

public class TraitForComparison implements Serializable{

    private static final long serialVersionUID = -4335466707467584859L;

    private TraitInfo traitInfo;
    private Integer direction;
    
    public TraitForComparison(TraitInfo traitInfo, Integer direction){
        this.traitInfo = traitInfo;
        this.direction = direction;
    }

	public TraitInfo getTraitInfo() {
		return traitInfo;
	}

	public void setTraitInfo(TraitInfo traitInfo) {
		this.traitInfo = traitInfo;
	}

	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}
    
    
}

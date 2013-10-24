package org.generationcp.browser.cross.study.adapted.main.pojos;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.constants.TraitWeight;
import org.generationcp.middleware.domain.h2h.TraitInfo;

public class NumericTraitFilter implements Serializable {
	
	private static final long serialVersionUID = -1400001149797183987L;
	
	private TraitInfo traitInfo;
	private NumericTraitCriteria condition;
	private List<String> limits;
	private TraitWeight priority;
	
	
	public NumericTraitFilter(TraitInfo traitInfo, NumericTraitCriteria condition,
			List<String> limits, TraitWeight priority) {
		super();
		this.traitInfo = traitInfo;
		this.condition = condition;
		this.limits = limits;
		this.priority = priority;
	}
	public TraitInfo getTraitInfo() {
		return traitInfo;
	}
	public void setTraitInfo(TraitInfo traitInfo) {
		this.traitInfo = traitInfo;
	}
	public NumericTraitCriteria getCondition() {
		return condition;
	}
	public void setCondition(NumericTraitCriteria condition) {
		this.condition = condition;
	}
	public List<String> getLimits() {
		return limits;
	}
	public void setLimit(List<String> limits) {
		this.limits = limits;
	}
	public TraitWeight getPriority() {
		return priority;
	}
	public void setPriority(TraitWeight priority) {
		this.priority = priority;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NumericTraitFilter)) {
            return false;
        }

        NumericTraitFilter rhs = (NumericTraitFilter) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(traitInfo, rhs.traitInfo).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(traitInfo).toHashCode();
    }
}

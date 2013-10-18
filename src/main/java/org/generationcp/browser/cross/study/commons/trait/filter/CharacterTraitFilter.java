package org.generationcp.browser.cross.study.commons.trait.filter;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.generationcp.browser.cross.study.constants.CharacterTraitCondition;
import org.generationcp.browser.cross.study.constants.TraitWeight;

public class CharacterTraitFilter implements Serializable {

	private static final long serialVersionUID = -1400001149797183987L;
	
	private Integer traitId;
	private CharacterTraitCondition condition;
	private List<String> limits;
	private TraitWeight priority;
	
	public CharacterTraitFilter(Integer traitId,
			CharacterTraitCondition condition, List<String> limits,
			TraitWeight priority) {
		super();
		this.traitId = traitId;
		this.condition = condition;
		this.limits = limits;
		this.priority = priority;
	}

	public Integer getTraitId() {
		return traitId;
	}
	
	public void setTraitId(Integer traitId) {
		this.traitId = traitId;
	}
	
	public CharacterTraitCondition getCondition() {
		return condition;
	}
	
	public void setCondition(CharacterTraitCondition condition) {
		this.condition = condition;
	}
	
	public List<String> getLimits() {
		return limits;
	}
	
	public void setLimits(List<String> limits) {
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
        if (!(obj instanceof CharacterTraitFilter)) {
            return false;
        }

        CharacterTraitFilter rhs = (CharacterTraitFilter) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(traitId, rhs.traitId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(traitId).toHashCode();
    }
}

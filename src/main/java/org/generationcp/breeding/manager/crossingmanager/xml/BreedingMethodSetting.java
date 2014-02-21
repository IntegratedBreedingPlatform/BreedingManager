package org.generationcp.breeding.manager.crossingmanager.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class BreedingMethodSetting implements Serializable {

	private static final long serialVersionUID = -7580936794539379309L;

	private Integer methodId;
	private boolean isBasedOnStatusOfParentalLines;
	private boolean useAMethodForAllCrosses;
	
	public BreedingMethodSetting(){
		
	}
	
	public BreedingMethodSetting(Integer methodId,
			boolean isBasedOnStatusOfParentalLines,
			boolean useAMethodForAllCrosses) {
		super();
		this.methodId = methodId;
		this.isBasedOnStatusOfParentalLines = isBasedOnStatusOfParentalLines;
		this.useAMethodForAllCrosses = useAMethodForAllCrosses;
	}

	@XmlAttribute
	public Integer getMethodId() {
		return methodId;
	}

	public void setMethodId(Integer methodId) {
		this.methodId = methodId;
	}

	@XmlAttribute
	public boolean isBasedOnStatusOfParentalLines() {
		return isBasedOnStatusOfParentalLines;
	}

	public void setBasedOnStatusOfParentalLines(
			boolean isBasedOnStatusOfParentalLines) {
		this.isBasedOnStatusOfParentalLines = isBasedOnStatusOfParentalLines;
	}

	@XmlAttribute
	public boolean isUseAMethodForAllCrosses() {
		return useAMethodForAllCrosses;
	}

	public void setUseAMethodForAllCrosses(boolean useAMethodForAllCrosses) {
		this.useAMethodForAllCrosses = useAMethodForAllCrosses;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BreedingMethodSetting)) {
            return false;
        }

        BreedingMethodSetting rhs = (BreedingMethodSetting) obj;
        return new EqualsBuilder()
        		.append(methodId, rhs.methodId)
        		.append(isBasedOnStatusOfParentalLines, rhs.isBasedOnStatusOfParentalLines)
        		.append(useAMethodForAllCrosses, rhs.useAMethodForAllCrosses)
        		.isEquals();
    }

	@Override
	public String toString() {
		return "BreedingMethodSetting [methodId=" + methodId
				+ ", isBasedOnStatusOfParentalLines="
				+ isBasedOnStatusOfParentalLines + ", useAMethodForAllCrosses="
				+ useAMethodForAllCrosses + "]";
	}
	
}

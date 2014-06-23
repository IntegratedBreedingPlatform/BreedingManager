package org.generationcp.breeding.manager.crossingmanager.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class BreedingMethodSetting implements Serializable {

	private static final long serialVersionUID = -7580936794539379309L;

	private Integer methodId;
	private boolean isBasedOnStatusOfParentalLines;
	
	public BreedingMethodSetting(){
		
	}
	
	public BreedingMethodSetting(Integer methodId,
			boolean isBasedOnStatusOfParentalLines) {
		super();
		this.methodId = methodId;
		this.isBasedOnStatusOfParentalLines = isBasedOnStatusOfParentalLines;
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
        		.isEquals();
    }

	@Override
	public String toString() {
		return "BreedingMethodSetting [methodId=" + methodId
				+ ", isBasedOnStatusOfParentalLines="
				+ isBasedOnStatusOfParentalLines + "]";
	}
	
}

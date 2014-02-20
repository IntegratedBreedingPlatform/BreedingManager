package org.generationcp.breeding.manager.crossingmanager.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class CrossNameSetting implements Serializable {

	private static final long serialVersionUID = 2944997653987903733L;

	private String prefix;
	private String suffix;
	private boolean addSpaceBetweenPrefixAndCode;
	private Integer numOfDigits;
	
	public CrossNameSetting(){
		
	}

	public CrossNameSetting(String prefix, String suffix,
			boolean addSpaceBetweenPrefixAndCode, Integer numOfDigits) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
		this.addSpaceBetweenPrefixAndCode = addSpaceBetweenPrefixAndCode;
		this.numOfDigits = numOfDigits;
	}

	@XmlAttribute
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@XmlAttribute
	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@XmlAttribute
	public boolean isAddSpaceBetweenPrefixAndCode() {
		return addSpaceBetweenPrefixAndCode;
	}

	public void setAddSpaceBetweenPrefixAndCode(boolean addSpaceBetweenPrefixAndCode) {
		this.addSpaceBetweenPrefixAndCode = addSpaceBetweenPrefixAndCode;
	}

	@XmlAttribute
	public Integer getNumOfDigits() {
		return numOfDigits;
	}

	public void setNumOfDigits(Integer numOfDigits) {
		this.numOfDigits = numOfDigits;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CrossNameSetting)) {
            return false;
        }

        CrossNameSetting rhs = (CrossNameSetting) obj;
        return new EqualsBuilder()
        		.append(prefix, rhs.prefix)
        		.append(suffix, rhs.suffix)
        		.append(addSpaceBetweenPrefixAndCode, rhs.addSpaceBetweenPrefixAndCode)
        		.append(numOfDigits, rhs.numOfDigits)
        		.isEquals();
    }

	@Override
	public String toString() {
		return "CrossNameSetting [prefix=" + prefix + ", suffix=" + suffix
				+ ", addSpaceBetweenPrefixAndCode="
				+ addSpaceBetweenPrefixAndCode + ", numOfDigits=" + numOfDigits
				+ "]";
	}

}

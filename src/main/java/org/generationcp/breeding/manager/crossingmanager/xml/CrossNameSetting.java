package org.generationcp.breeding.manager.crossingmanager.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class CrossNameSetting implements Serializable {
	
	public static final String DEFAULT_SEPARATOR = "/";

	private static final long serialVersionUID = 2944997653987903733L;

	private String prefix;
	private String suffix;
	private boolean addSpaceBetweenPrefixAndCode;
	private boolean addSpaceBetweenSuffixAndCode;
	private Integer numOfDigits;
	private String separator;

	private Integer startNumber; // "transient" attribute, not saved in DB
	
	public CrossNameSetting(){
		
	}

	public CrossNameSetting(String prefix, String suffix,
			boolean addSpaceBetweenPrefixAndCode, boolean addSpaceBetweenSuffixAndCode,
			Integer numOfDigits, String separator) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
		this.addSpaceBetweenPrefixAndCode = addSpaceBetweenPrefixAndCode;
		this.addSpaceBetweenSuffixAndCode = addSpaceBetweenSuffixAndCode;
		this.numOfDigits = numOfDigits;
		this.separator = separator;
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
	public boolean isAddSpaceBetweenSuffixAndCode() {
		return addSpaceBetweenSuffixAndCode;
	}

	public void setAddSpaceBetweenSuffixAndCode(boolean addSpaceBetweenSuffixAndCode) {
		this.addSpaceBetweenSuffixAndCode = addSpaceBetweenSuffixAndCode;
	}

	@XmlAttribute
	public Integer getNumOfDigits() {
		return numOfDigits;
	}

	public void setNumOfDigits(Integer numOfDigits) {
		this.numOfDigits = numOfDigits;
	}
	
	public Integer getStartNumber() {
		return startNumber;
	}

	public void setStartNumber(Integer startNumber) {
		this.startNumber = startNumber;
	}

	@XmlAttribute
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
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
        		.append(addSpaceBetweenSuffixAndCode, rhs.addSpaceBetweenSuffixAndCode)
        		.append(numOfDigits, rhs.numOfDigits)
        		.append(separator, rhs.separator)
        		.isEquals();
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CrossNameSetting [prefix=");
		builder.append(prefix);
		builder.append(", suffix=");
		builder.append(suffix);
		builder.append(", addSpaceBetweenPrefixAndCode=");
		builder.append(addSpaceBetweenPrefixAndCode);
		builder.append(", addSpaceBetweenSuffixAndCode=");
		builder.append(addSpaceBetweenSuffixAndCode);
		builder.append(", numOfDigits=");
		builder.append(numOfDigits);
		builder.append(", separator=");
		builder.append(separator);
		builder.append("]");
		return builder.toString();
	}

}

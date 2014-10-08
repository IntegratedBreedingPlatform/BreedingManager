package org.generationcp.breeding.manager.crossingmanager.xml;

import java.io.Serializable;
import java.text.DateFormatSymbols;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class AdditionalDetailsSetting implements Serializable {

	private static final long serialVersionUID = -5167579512310794528L;
	
	private Integer harvestLocationId;
	private String harvestDate;

	public AdditionalDetailsSetting(){
		
	}
	
	public AdditionalDetailsSetting(Integer harvestLocationId, String harvestDate) {
		super();
		this.harvestLocationId = harvestLocationId;
		this.harvestDate = harvestDate;
	}

	@XmlAttribute
	public Integer getHarvestLocationId() {
		return harvestLocationId;
	}

	public void setHarvestLocationId(Integer harvestLocationId) {
		this.harvestLocationId = harvestLocationId;
	}
	
	@XmlTransient
	public String getHarvestDate() {
		return harvestDate;
	}

	public void setHarvestDate(String harvestDate) {
		this.harvestDate = harvestDate;
	}
	
	public String getHarvestMonth(){
		int month = Integer.valueOf(harvestDate.substring(4,6));
		
		if(month == 0){
			return "";
		} else{
			DateFormatSymbols dateFormat = new DateFormatSymbols(); 
			String monthString = dateFormat.getMonths()[month - 1];
			return monthString;
		}
	}
	
	public String getHarvestYear(){ 
		return harvestDate.substring(0,4);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((harvestDate == null) ? 0 : harvestDate.hashCode());
		result = prime * result + ((harvestLocationId == null) ? 0 : harvestLocationId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AdditionalDetailsSetting)) {
            return false;
        }

        AdditionalDetailsSetting rhs = (AdditionalDetailsSetting) obj;
        return new EqualsBuilder()
        		.append(harvestLocationId, rhs.harvestLocationId)
        		.isEquals();
    }

	@Override
	public String toString() {
		return "AdditionalDetailsSetting [harvestLocationId="
				+ harvestLocationId + ", harvestDate=" + harvestDate + "]";
	}
	
}

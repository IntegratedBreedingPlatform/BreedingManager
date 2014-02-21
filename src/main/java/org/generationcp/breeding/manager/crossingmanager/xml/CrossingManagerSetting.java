package org.generationcp.breeding.manager.crossingmanager.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;

@XmlRootElement
public class CrossingManagerSetting implements Serializable {

	private static final long serialVersionUID = 905356968758567192L;

	private String name;
	private BreedingMethodSetting breedingMethodSetting;
	private CrossNameSetting crossNameSetting;
	private AdditionalDetailsSetting additionalDetailsSetting;
	
	public CrossingManagerSetting(){
		
	}

	public CrossingManagerSetting(String name,
			BreedingMethodSetting breedingMethodSetting,
			CrossNameSetting crossNameSetting,
			AdditionalDetailsSetting additionalDetailsSetting) {
		super();
		this.name = name;
		this.breedingMethodSetting = breedingMethodSetting;
		this.crossNameSetting = crossNameSetting;
		this.additionalDetailsSetting = additionalDetailsSetting;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public BreedingMethodSetting getBreedingMethodSetting() {
		return breedingMethodSetting;
	}

	public void setBreedingMethodSetting(BreedingMethodSetting breedingMethodSetting) {
		this.breedingMethodSetting = breedingMethodSetting;
	}

	@XmlElement
	public CrossNameSetting getCrossNameSetting() {
		return crossNameSetting;
	}

	public void setCrossNameSetting(CrossNameSetting crossNameSetting) {
		this.crossNameSetting = crossNameSetting;
	}

	@XmlElement
	public AdditionalDetailsSetting getAdditionalDetailsSetting() {
		return additionalDetailsSetting;
	}

	public void setAdditionalDetailsSetting(
			AdditionalDetailsSetting additionalDetailsSetting) {
		this.additionalDetailsSetting = additionalDetailsSetting;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CrossingManagerSetting)) {
            return false;
        }

        CrossingManagerSetting rhs = (CrossingManagerSetting) obj;
        return new EqualsBuilder()
        		.append(name, rhs.name)
        		.append(breedingMethodSetting, rhs.breedingMethodSetting)
        		.append(crossNameSetting, rhs.crossNameSetting)
        		.append(additionalDetailsSetting, rhs.additionalDetailsSetting)
        		.isEquals();
    }

	@Override
	public String toString() {
		return "CrossingManagerSetting [name=" + name
				+ ", breedingMethodSetting=" + breedingMethodSetting
				+ ", crossNameSetting=" + crossNameSetting
				+ ", additionalDetailsSetting=" + additionalDetailsSetting
				+ "]";
	}
	
}

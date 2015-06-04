
package org.generationcp.breeding.manager.crossingmanager.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;

@XmlRootElement
public class CrossingManagerSetting implements Serializable {

	public static final String CROSSING_MANAGER_TOOL_NAME = "crossing_manager";

	private static final long serialVersionUID = 905356968758567192L;

	private String name;
	private BreedingMethodSetting breedingMethodSetting;
	private CrossNameSetting crossNameSetting;
	private AdditionalDetailsSetting additionalDetailsSetting;

	public CrossingManagerSetting() {

	}

	public CrossingManagerSetting(String name, BreedingMethodSetting breedingMethodSetting, CrossNameSetting crossNameSetting,
			AdditionalDetailsSetting additionalDetailsSetting) {
		super();
		this.name = name;
		this.breedingMethodSetting = breedingMethodSetting;
		this.crossNameSetting = crossNameSetting;
		this.additionalDetailsSetting = additionalDetailsSetting;
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public BreedingMethodSetting getBreedingMethodSetting() {
		return this.breedingMethodSetting;
	}

	public void setBreedingMethodSetting(BreedingMethodSetting breedingMethodSetting) {
		this.breedingMethodSetting = breedingMethodSetting;
	}

	@XmlElement
	public CrossNameSetting getCrossNameSetting() {
		return this.crossNameSetting;
	}

	public void setCrossNameSetting(CrossNameSetting crossNameSetting) {
		this.crossNameSetting = crossNameSetting;
	}

	@XmlElement
	public AdditionalDetailsSetting getAdditionalDetailsSetting() {
		return this.additionalDetailsSetting;
	}

	public void setAdditionalDetailsSetting(AdditionalDetailsSetting additionalDetailsSetting) {
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
		return new EqualsBuilder().append(this.name, rhs.name).append(this.breedingMethodSetting, rhs.breedingMethodSetting)
				.append(this.crossNameSetting, rhs.crossNameSetting).append(this.additionalDetailsSetting, rhs.additionalDetailsSetting)
				.isEquals();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.additionalDetailsSetting == null ? 0 : this.additionalDetailsSetting.hashCode());
		result = prime * result + (this.breedingMethodSetting == null ? 0 : this.breedingMethodSetting.hashCode());
		result = prime * result + (this.crossNameSetting == null ? 0 : this.crossNameSetting.hashCode());
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "CrossingManagerSetting [name=" + this.name + ", breedingMethodSetting=" + this.breedingMethodSetting
				+ ", crossNameSetting=" + this.crossNameSetting + ", additionalDetailsSetting=" + this.additionalDetailsSetting + "]";
	}

}

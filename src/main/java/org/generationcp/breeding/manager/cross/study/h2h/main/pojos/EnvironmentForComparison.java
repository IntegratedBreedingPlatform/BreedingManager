
package org.generationcp.breeding.manager.cross.study.h2h.main.pojos;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import org.generationcp.breeding.manager.cross.study.constants.EnvironmentWeight;

import com.vaadin.ui.ComboBox;

public class EnvironmentForComparison implements Serializable {

	private static final long serialVersionUID = -879684249019712493L;

	private Integer environmentNumber;
	private String locationName;
	private String countryName;
	private String studyName;
	private ComboBox weightComboBox;
	private Double weight;
	private LinkedHashMap<TraitForComparison, List<ObservationList>> traitAndObservationMap =
			new LinkedHashMap<TraitForComparison, List<ObservationList>>();

	public EnvironmentForComparison(Integer environmentNumber, String locationName, String countryName, String studyName,
			ComboBox weightComboBox) {
		super();
		this.environmentNumber = environmentNumber;
		this.locationName = locationName;
		this.countryName = countryName;
		this.studyName = studyName;
		this.weightComboBox = weightComboBox;
	}

	public ComboBox getWeightComboBox() {
		return this.weightComboBox;
	}

	public void setWeightComboBox(ComboBox weightComboBox) {
		this.weightComboBox = weightComboBox;
	}

	public Integer getEnvironmentNumber() {
		return this.environmentNumber;
	}

	public void setEnvironmentNumber(Integer environmentNumber) {
		this.environmentNumber = environmentNumber;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getCountryName() {
		return this.countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getStudyName() {
		return this.studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public LinkedHashMap<TraitForComparison, List<ObservationList>> getTraitAndObservationMap() {
		return this.traitAndObservationMap;
	}

	public void setTraitAndObservationMap(LinkedHashMap<TraitForComparison, List<ObservationList>> traitAndObservationMap) {
		this.traitAndObservationMap = traitAndObservationMap;
	}

	public Double getWeight() {
		return this.weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public void computeWeight(int total) {
		if (total > 0 && this.weightComboBox != null) {
			EnvironmentWeight envtWeight = (EnvironmentWeight) this.weightComboBox.getValue();
			this.weight = (double) envtWeight.getWeight() / total;
		}
	}

}


package org.generationcp.breeding.manager.cross.study.h2h.main.pojos;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.cross.study.h2h.main.ResultsComponent;
import org.generationcp.middleware.domain.h2h.Observation;

public class ObservationList {

	String key;
	List<Observation> observationList = new ArrayList<Observation>();

	public ObservationList(String key) {
		this.key = key;
	}

	public void addObservation(Observation observation) {
		this.observationList.add(observation);
	}

	// will be use for counting number of environments
	public boolean isValidObservationList() {
		for (Observation observation : this.observationList) {
			if (ResultsComponent.isValidDoubleValue(observation.getValue())) {
				return true;
			}
		}
		return false;
	}

	// will be use for getting the average
	public double getObservationAverage() {
		double total = 0;
		for (Observation observation : this.observationList) {
			if (ResultsComponent.isValidDoubleValue(observation.getValue())) {
				total += Double.parseDouble(observation.getValue());
			}
		}
		return total / this.observationList.size();
	}

	// will be use for getting the average
	public double getWeightedObservationAverage(Double weightValue) {
		double total = 0;
		for (Observation observation : this.observationList) {
			if (ResultsComponent.isValidDoubleValue(observation.getValue())) {
				if (weightValue != null) {
					total += weightValue * Double.parseDouble(observation.getValue());
				} else {
					total += Double.parseDouble(observation.getValue());
				}
			}
		}
		return total / this.observationList.size();
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Observation> getObservationList() {
		return this.observationList;
	}

	public void setObservationList(List<Observation> observationList) {
		this.observationList = observationList;
	}
}


package org.generationcp.breeding.manager.cross.study.adapted.main.pojos;

public class TraitObservationScore {

	Integer germplasmId;
	Integer noOfObservation;
	Double wtScore;

	public TraitObservationScore(Integer germplasmId, Integer noOfObservation, Double wtScore) {
		super();
		this.germplasmId = germplasmId;
		this.noOfObservation = noOfObservation;
		this.wtScore = wtScore;
	}

	public Integer getTraitId() {
		return this.germplasmId;
	}

	public void setTraitId(Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	public Integer getNoOfObservation() {
		return this.noOfObservation;
	}

	public void setNoOfObservation(Integer noOfObservation) {
		this.noOfObservation = noOfObservation;
	}

	public Double getWtScore() {
		return this.wtScore;
	}

	public void setWtScore(Double wtScore) {
		this.wtScore = wtScore;
	}

	@Override
	public String toString() {
		String toPrint =
				"TraitObservation [ " + "germplasmId= " + this.germplasmId + ", noOfObservation= " + this.noOfObservation + ", wtScore= "
						+ this.wtScore + "]";

		return toPrint;
	}
}

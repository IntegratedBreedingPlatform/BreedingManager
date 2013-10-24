package org.generationcp.browser.cross.study.adapted.main.pojos;

public class TraitObservationScore {
	Integer germplasmId;
	Integer noOfObservation;
	Double wtScore;
	
	public TraitObservationScore(Integer germplasmId, Integer noOfObservation,
			Double wtScore) {
		super();
		this.germplasmId = germplasmId;
		this.noOfObservation = noOfObservation;
		this.wtScore = wtScore;
	}
	
	public Integer getTraitId() {
		return germplasmId;
	}
	public void setTraitId(Integer germplasmId) {
		this.germplasmId = germplasmId;
	}
	public Integer getNoOfObservation() {
		return noOfObservation;
	}
	public void setNoOfObservation(Integer noOfObservation) {
		this.noOfObservation = noOfObservation;
	}
	public Double getWtScore() {
		return wtScore;
	}
	public void setWtScore(Double wtScore) {
		this.wtScore = wtScore;
	}
}

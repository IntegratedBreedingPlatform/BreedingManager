package org.generationcp.browser.cross.study.adapted.main.pojos;

import java.util.Map;

import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.domain.h2h.TraitObservation;

public class TableResultRow {
	private Integer germplasmId;
	private Map<NumericTraitFilter, TraitObservationScore> numericTOSMap;
	private Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap;
	private Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap;
	private Double combinedScore;
	
	public TableResultRow(Integer germplasmId,
			Map<NumericTraitFilter, TraitObservationScore> numericTOSMap,
			Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap,
			Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap) {
		super();
		this.germplasmId = germplasmId;
		this.numericTOSMap = numericTOSMap;
		this.characterTOSMap = characterTOSMap;
		this.categoricalTOSMap = categoricalTOSMap;
	}

	public Integer getGermplasmId() {
		return germplasmId;
	}

	public void setGermplasmId(Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	public Map<NumericTraitFilter, TraitObservationScore> getNumericTOSMap() {
		return numericTOSMap;
	}

	public void setNumericTOSMap(
			Map<NumericTraitFilter, TraitObservationScore> numericTOSMap) {
		this.numericTOSMap = numericTOSMap;
	}

	public Map<CharacterTraitFilter, TraitObservationScore> getCharacterTOSMap() {
		return characterTOSMap;
	}

	public void setCharacterTOSMap(
			Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap) {
		this.characterTOSMap = characterTOSMap;
	}

	public Map<CategoricalTraitFilter, TraitObservationScore> getCategoricalTOSMap() {
		return categoricalTOSMap;
	}

	public void setCategoricalTOSMap(
			Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap) {
		this.categoricalTOSMap = categoricalTOSMap;
	}

	public Double getCombinedScore(){
		
		Double combinedScore = 0.0;
		Double totalTraitWeight = 0.0;
		
		for(Map.Entry<NumericTraitFilter, TraitObservationScore> tos : numericTOSMap.entrySet()){
			Double traitWeight = Double.valueOf(tos.getKey().getPriority().getWeight());
			Double score = tos.getValue().wtScore;
			
			combinedScore += (traitWeight * score);
			totalTraitWeight += traitWeight;
		}
		
		for(Map.Entry<CharacterTraitFilter, TraitObservationScore> tos : characterTOSMap.entrySet()){
			Double traitWeight = Double.valueOf(tos.getKey().getPriority().getWeight());
			Double score = tos.getValue().wtScore;
			
			combinedScore += (traitWeight * score);
			totalTraitWeight += traitWeight;
		}
		
		for(Map.Entry<CategoricalTraitFilter, TraitObservationScore> tos : categoricalTOSMap.entrySet()){
			Double traitWeight = Double.valueOf(tos.getKey().getPriority().getWeight());
			Double score = tos.getValue().wtScore;
			
			combinedScore += (traitWeight * score);
			totalTraitWeight += traitWeight;
		}
		
		combinedScore = combinedScore / totalTraitWeight ;
		
		
		combinedScore = (double) Math.round(combinedScore * 100);
		combinedScore = combinedScore/100;
		
		return combinedScore;
	}
	
	@Override
	public String toString(){
		String toPrint = "";
		String numericTOS = "", characterTOS = "", categoricalTOS = "";
		
		for(Map.Entry<NumericTraitFilter, TraitObservationScore> obs : numericTOSMap.entrySet()){
			numericTOS += obs.getValue().toString() + "\n"; 
		}
		
		for(Map.Entry<CharacterTraitFilter, TraitObservationScore> obs : characterTOSMap.entrySet()){
			characterTOS += obs.getValue().toString() + "\n"; 
		}
		
		for(Map.Entry<CategoricalTraitFilter, TraitObservationScore> obs : categoricalTOSMap.entrySet()){
			categoricalTOS += obs.getValue().toString() + "\n"; 
		}
		
		toPrint = "TableResultRow: [ numericTOS: " + numericTOS + "] \n [ characterTOS: " + characterTOS + " ] \n" + "[ categoricalTOS: " + categoricalTOS + "]";
		
		return toPrint;
	}
}


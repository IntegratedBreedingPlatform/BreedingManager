
package org.generationcp.breeding.manager.cross.study.adapted.main.pojos;

import java.util.Map;

public class TableResultRow {

	private Integer germplasmId;
	private Map<NumericTraitFilter, TraitObservationScore> numericTOSMap;
	private Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap;
	private Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap;

	public TableResultRow(Integer germplasmId, Map<NumericTraitFilter, TraitObservationScore> numericTOSMap,
			Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap,
			Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap) {
		super();
		this.germplasmId = germplasmId;
		this.numericTOSMap = numericTOSMap;
		this.characterTOSMap = characterTOSMap;
		this.categoricalTOSMap = categoricalTOSMap;
	}

	public Integer getGermplasmId() {
		return this.germplasmId;
	}

	public void setGermplasmId(Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	public Map<NumericTraitFilter, TraitObservationScore> getNumericTOSMap() {
		return this.numericTOSMap;
	}

	public void setNumericTOSMap(Map<NumericTraitFilter, TraitObservationScore> numericTOSMap) {
		this.numericTOSMap = numericTOSMap;
	}

	public Map<CharacterTraitFilter, TraitObservationScore> getCharacterTOSMap() {
		return this.characterTOSMap;
	}

	public void setCharacterTOSMap(Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap) {
		this.characterTOSMap = characterTOSMap;
	}

	public Map<CategoricalTraitFilter, TraitObservationScore> getCategoricalTOSMap() {
		return this.categoricalTOSMap;
	}

	public void setCategoricalTOSMap(Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap) {
		this.categoricalTOSMap = categoricalTOSMap;
	}

	public Double getCombinedScore() {

		Double combinedScore = 0.0;
		Double totalTraitWeight = 0.0;

		for (Map.Entry<NumericTraitFilter, TraitObservationScore> tos : this.numericTOSMap.entrySet()) {
			Double traitWeight = Double.valueOf(tos.getKey().getPriority().getWeight());
			Double score = tos.getValue().wtScore;

			combinedScore += traitWeight * score;
			totalTraitWeight += traitWeight;
		}

		for (Map.Entry<CharacterTraitFilter, TraitObservationScore> tos : this.characterTOSMap.entrySet()) {
			Double traitWeight = Double.valueOf(tos.getKey().getPriority().getWeight());
			Double score = tos.getValue().wtScore;

			combinedScore += traitWeight * score;
			totalTraitWeight += traitWeight;
		}

		for (Map.Entry<CategoricalTraitFilter, TraitObservationScore> tos : this.categoricalTOSMap.entrySet()) {
			Double traitWeight = Double.valueOf(tos.getKey().getPriority().getWeight());
			Double score = tos.getValue().wtScore;

			combinedScore += traitWeight * score;
			totalTraitWeight += traitWeight;
		}

		combinedScore = combinedScore / totalTraitWeight;

		combinedScore = (double) Math.round(combinedScore * 100);
		combinedScore = combinedScore / 100;

		return combinedScore;
	}

	@Override
	public String toString() {
		String toPrint = "";
		String numericTOS = "", characterTOS = "", categoricalTOS = "";

		for (Map.Entry<NumericTraitFilter, TraitObservationScore> obs : this.numericTOSMap.entrySet()) {
			numericTOS += obs.getValue().toString();
		}

		for (Map.Entry<CharacterTraitFilter, TraitObservationScore> obs : this.characterTOSMap.entrySet()) {
			characterTOS += obs.getValue().toString();
		}

		for (Map.Entry<CategoricalTraitFilter, TraitObservationScore> obs : this.categoricalTOSMap.entrySet()) {
			categoricalTOS += obs.getValue().toString();
		}

		toPrint =
				"TableResultRow: [ numericTOS: " + numericTOS + "] [ characterTOS: " + characterTOS + " ] " + "[ categoricalTOS: "
						+ categoricalTOS + "] [ combined score: " + this.getCombinedScore() + "]";

		return toPrint;
	}
}


package org.generationcp.breeding.manager.cross.study.adapted.main.pojos;

import java.util.List;

import org.generationcp.breeding.manager.cross.study.constants.NumericTraitCriteria;

public class NumericTraitEvaluator {

	NumericTraitCriteria condition;
	List<String> limits;
	Double value;

	public NumericTraitEvaluator(NumericTraitCriteria condition, List<String> limits, Double value) {
		super();
		this.condition = condition;
		this.limits = limits;
		this.value = value;

	}

	public boolean evaluate() {
		boolean result = false;

		if (this.condition == NumericTraitCriteria.KEEP_ALL) {
			result = true;
		} else if (this.condition == NumericTraitCriteria.LESS_THAN) {
			Double limit = Double.valueOf(this.limits.get(0));
			result = this.value < limit ? true : false;
		} else if (this.condition == NumericTraitCriteria.LESS_THAN_EQUAL) {
			Double limit = Double.valueOf(this.limits.get(0));
			result = this.value <= limit ? true : false;
		} else if (this.condition == NumericTraitCriteria.EQUAL) {
			Double limit = Double.valueOf(this.limits.get(0));
			result = this.value.equals(limit) ? true : false;
		} else if (this.condition == NumericTraitCriteria.GREATER_THAN) {
			Double limit = Double.valueOf(this.limits.get(0));
			result = this.value > limit ? true : false;
		} else if (this.condition == NumericTraitCriteria.GREATER_THAN_EQUAL) {
			Double limit = Double.valueOf(this.limits.get(0));
			result = this.value >= limit ? true : false;
		} else if (this.condition == NumericTraitCriteria.BETWEEN) {
			// limit a-b or a - b
			String[] limit = this.limits.get(0).split("-");
			Double lowerLimit = Double.valueOf(limit[0].trim());
			Double upperLimit = Double.valueOf(limit[1].trim());
			result = this.value >= lowerLimit && this.value <= upperLimit ? true : false;
		} else if (this.condition == NumericTraitCriteria.IN) {
			// limit a,b ,c, d

			int size = this.limits.size();
			for (int i = 0; i < size; i++) {
				Double limitVal = Double.valueOf(this.limits.get(i));
				if (this.value.equals(limitVal)) {
					result = true;
				}
			}
		} else if (this.condition == NumericTraitCriteria.NOT_IN) {
			// limit a,b ,c, d
			int size = this.limits.size();
			boolean flag = true;
			for (int i = 0; i < size; i++) {
				Double limitVal = Double.valueOf(this.limits.get(i));
				if (this.value.equals(limitVal)) {
					flag = false;
				}
			}

			result = flag;
		}

		return result;
	}

}

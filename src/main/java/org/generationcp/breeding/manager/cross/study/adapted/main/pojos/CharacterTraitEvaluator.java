
package org.generationcp.breeding.manager.cross.study.adapted.main.pojos;

import java.util.List;

import org.generationcp.breeding.manager.cross.study.constants.CharacterTraitCondition;

public class CharacterTraitEvaluator {

	CharacterTraitCondition condition;
	List<String> limits;
	String value;

	public CharacterTraitEvaluator(CharacterTraitCondition condition, List<String> limits, String value) {
		super();
		this.condition = condition;
		this.limits = limits;
		this.value = value;
	}

	public boolean evaluate() {
		boolean result = false;

		if (this.condition == CharacterTraitCondition.KEEP_ALL) {
			result = true;
		} else if (this.condition == CharacterTraitCondition.IN) {
			// limit a,b ,c, d
			int size = this.limits.size();
			for (int i = 0; i < size; i++) {
				if (this.value.equals(this.limits.get(i))) {
					result = true;
				}
			}

		} else if (this.condition == CharacterTraitCondition.NOT_IN) {
			// limit a,b ,c, d
			int size = this.limits.size();
			boolean flag = true;
			for (int i = 0; i < size; i++) {
				if (this.value.equals(this.limits.get(i))) {
					flag = false;
				}
			}

			result = flag;
		}

		return result;
	}
}

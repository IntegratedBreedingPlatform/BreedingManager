
package org.generationcp.breeding.manager.cross.study.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kevin Manansala
 *
 */
public class TraitFilterUtil {

	/**
	 * Checks if the limits provided are in the list of actual values provided as parameter. This is for making sure that the user enters
	 * limits that are in distinct observed values of a character trait.
	 *
	 * @param traitValues
	 * @param limitsProvided
	 * @return an empty List<String> if all limits provided are included in the actual values for the trait, other wise the List<String>
	 *         contains the limits which were not included in the actual values
	 */
	public static List<String> validateCharacterTraitLimits(List<String> traitValues, List<String> limitsProvided) {
		List<String> toreturn = new ArrayList<String>();

		for (String limit : limitsProvided) {
			boolean found = false;
			for (String value : traitValues) {
				if (value.equals(limit)) {
					found = true;
				}
			}

			if (!found) {
				toreturn.add(limit);
			}
		}
		return toreturn;
	}
}

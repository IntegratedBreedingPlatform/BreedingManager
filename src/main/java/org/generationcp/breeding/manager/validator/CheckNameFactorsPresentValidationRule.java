package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.ValidationRule;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

/**
 * This validation rule checks if the Name handling window should be displayed or not.
 * In order to validate that it check if there os any imported germplasm with a Name Factor.
 * If there is one then the popup window should be shown.
 */
@Component
public class CheckNameFactorsPresentValidationRule implements ValidationRule<List<ImportedGermplasm>> {

	public static final String ERROR_MESSAGE_NO_NAMES_PRESENT = "No names were present in excel observation sheet";

	@Override
	public Optional<String> validate (List<ImportedGermplasm> target) {
		for (ImportedGermplasm importedGermplasm : target) {
			if(importedGermplasm.getNameFactors()!=null){
				for (String name : importedGermplasm.getNameFactors().values()) {
					if (!name.isEmpty()) {
						return Optional.absent();
					}
				}
			}
		}

		return Optional.of(ERROR_MESSAGE_NO_NAMES_PRESENT);
	}
}

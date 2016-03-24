package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.Executable;
import org.generationcp.middleware.components.validator.ExecutionException;
import org.springframework.stereotype.Component;
/**
 * This validation rule checks if the Name handling window should be displayed or not.
 * In order to validate that it check if there os any imported germplasm with a Name Factor.
 * If there is one then the popup window should be shown.
 */
@Component
public class CheckNameFactorsPresentValidationRule implements Executable<List<ImportedGermplasm>> {

	public static final String ERROR_MESSAGE_NO_NAMES_PRESENT = "No names were present in excel observation sheet";

	@Override
	public List<ImportedGermplasm> execute (List<ImportedGermplasm> target) throws ExecutionException {
		for (ImportedGermplasm importedGermplasm : target) {
			if(importedGermplasm.getNameFactors()!=null){
				for (String name : importedGermplasm.getNameFactors().values()) {
					if (!name.isEmpty()) {
						return target;
					}
				}
			}
		}
		throw new ExecutionException(ERROR_MESSAGE_NO_NAMES_PRESENT);
	}
}

package org.generationcp.breeding.manager.validator;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.FailFastValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportedGermplasmValidator extends FailFastValidator<ImportedGermplasm>{

	@Autowired
	public void checkGermplasmWithoutCodedNamesValidationRule (CheckGermplasmWithoutCodedNamesValidationRule checkGermplasmWithoutCodedNamesValidationRule){
		rules.add(checkGermplasmWithoutCodedNamesValidationRule);
	}

}

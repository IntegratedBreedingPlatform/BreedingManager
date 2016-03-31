package org.generationcp.breeding.manager.validator;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.ChainValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportedGermplasmValidator extends ChainValidator<ImportedGermplasm> {

	@Autowired
	public void checkGermplasmWithoutCodedNamesValidationRule (CheckGermplasmWithoutCodedNamesValidationRule checkGermplasmWithoutCodedNamesValidationRule){
		this.add(checkGermplasmWithoutCodedNamesValidationRule);
	}

}

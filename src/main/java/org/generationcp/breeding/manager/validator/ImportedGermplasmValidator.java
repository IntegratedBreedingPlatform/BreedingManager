package org.generationcp.breeding.manager.validator;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.ChainValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportedGermplasmValidator extends ChainValidator<ImportedGermplasm> {

	@Autowired
	public void setCheckGermplasmWithoutCodedNamesValidationRule(
			CheckGermplasmWithoutCodedNamesValidationRule checkGermplasmWithoutCodedNamesValidationRule) {
		this.add(checkGermplasmWithoutCodedNamesValidationRule);
	}

	@Autowired
	public void setCheckGIDNotLinkedToGermplasmWithCodedNamesValidationRule(
			CheckGIDNotLinkedToGermplasmWithCodedNamesValidationRule checkGIDNotLinkedToGermplasmWithCodedNamesValidationRule) {
		this.add(checkGIDNotLinkedToGermplasmWithCodedNamesValidationRule);
	}

}

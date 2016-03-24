package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.FailFastValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowNameHandlingPopUpValidator extends FailFastValidator<List<ImportedGermplasm>>{

	@Autowired
	public void setCheckNameFactorsPresentValidationRule (CheckNameFactorsPresentValidationRule checkNameFactorsPresentValidationRule){
		rules.add(checkNameFactorsPresentValidationRule);
	}
}

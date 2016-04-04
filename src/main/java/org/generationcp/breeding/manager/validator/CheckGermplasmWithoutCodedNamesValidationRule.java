package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.components.validator.ErrorMessage;
import org.generationcp.middleware.components.validator.ValidationRule;
import org.generationcp.middleware.manager.api.NamesDataManager;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

@Component
public class CheckGermplasmWithoutCodedNamesValidationRule implements ValidationRule<ImportedGermplasm> {

	public static final String ERROR_MESSAGE_GERMPLASM_USING_CODENAME = "GERMPLASM_PARSER_USE_CODED_NAMES";
	private CodeNamesLocator codedNamesLocator;
	private NamesDataManager manager;

	@Autowired
	public CheckGermplasmWithoutCodedNamesValidationRule(CodeNamesLocator codedNamesLocator, NamesDataManager manager) {
		this.codedNamesLocator = codedNamesLocator;
		this.manager = manager;
	}

	@Override
	public Optional<ErrorMessage> validate(ImportedGermplasm importedGermplasm)  {
		ErrorMessage message = null;

		List<Integer> typeList =  codedNamesLocator.locateCodedNamesIds();
		List<Name> names =  manager.getNamesByNvalInTypeList(importedGermplasm.getDesig(),typeList);

		if(names.size()>0){
			message= new ErrorMessage(ERROR_MESSAGE_GERMPLASM_USING_CODENAME);
			message.addParameters(importedGermplasm.getDesig());
		}
		return Optional.fromNullable(message);
	}
}

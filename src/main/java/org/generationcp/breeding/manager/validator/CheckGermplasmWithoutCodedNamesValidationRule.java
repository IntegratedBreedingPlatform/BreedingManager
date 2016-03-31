package org.generationcp.breeding.manager.validator;

import java.text.MessageFormat;
import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.components.validator.ValidationRule;
import org.generationcp.middleware.manager.api.NamesDataManager;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

@Component
public class CheckGermplasmWithoutCodedNamesValidationRule implements ValidationRule<ImportedGermplasm> {

	public static final String ERROR_MESSAGE_GERMPLASM_USING_CODENAME = "Germplasm designation value '{0}' is marked as a coded name value.";
	CodeNamesLocator codedNamesLocator;
	NamesDataManager manager;

	@Autowired
	public CheckGermplasmWithoutCodedNamesValidationRule(CodeNamesLocator codedNamesLocator, NamesDataManager manager) {
		this.codedNamesLocator = codedNamesLocator;
		this.manager = manager;
	}

	@Override
	public Optional<String> validate(ImportedGermplasm importedGermplasm)  {

		List<Integer> typeList =  codedNamesLocator.getCodedNamesIds();

		List<Name> names =  manager.getNamesByNvalInTypeList(importedGermplasm.getDesig(),typeList);

		if(names.size()>0){
			return Optional.of(MessageFormat.format(ERROR_MESSAGE_GERMPLASM_USING_CODENAME, importedGermplasm));
		}
		return Optional.absent();
	}
}

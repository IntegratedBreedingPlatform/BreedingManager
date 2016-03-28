package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.components.validator.Executable;
import org.generationcp.middleware.components.validator.ExecutionException;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.NamesDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckGermplasmWithoutCodedNamesValidationRule implements Executable<ImportedGermplasm> {

	public static final String ERROR_MESSAGE_GERMPLASM_USING_CODENAME = "Germplasm is using a coded name value.";
	CodeNamesLocator codedNamesLocator;
	NamesDataManager manager;

	@Autowired
	public CheckGermplasmWithoutCodedNamesValidationRule(CodeNamesLocator codedNamesLocator, NamesDataManager manager) {
		this.codedNamesLocator = codedNamesLocator;
		this.manager = manager;
	}

	@Override
	public ImportedGermplasm execute(ImportedGermplasm importedGermplasm) throws ExecutionException {

		List<Integer> typeList =  codedNamesLocator.getCodedNamesIds();

		List<Name> names =  manager.getNamesByNvalInTypeList(importedGermplasm.getDesig(),typeList);

		if(names.size()>0){
			throw new ExecutionException(ERROR_MESSAGE_GERMPLASM_USING_CODENAME);
		}
		return importedGermplasm;
	}
}

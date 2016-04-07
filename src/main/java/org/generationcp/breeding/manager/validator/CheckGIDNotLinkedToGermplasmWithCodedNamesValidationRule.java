package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.components.validator.ErrorMessage;
import org.generationcp.middleware.components.validator.ValidationRule;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.NamesDataManager;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

@Component
public class CheckGIDNotLinkedToGermplasmWithCodedNamesValidationRule implements ValidationRule<ImportedGermplasm> {

	public static final String ERROR_MESSAGE_GERMPLASM_USING_CODENAME = "GERMPLASM_PARSER_GID_USE_CODED_NAMES";
	private CodeNamesLocator codedNamesLocator;
	private NamesDataManager manager;
	private ContextUtil context;

	@Autowired
	public CheckGIDNotLinkedToGermplasmWithCodedNamesValidationRule(CodeNamesLocator codedNamesLocator, NamesDataManager manager,ContextUtil context) {
		this.codedNamesLocator = codedNamesLocator;
		this.manager = manager;
		this.context = context;

	}

	/**
	 * This rule checks that that the germplasm being imported should not refer to a GID with a coded name
	 *
	 * @param importedGermplasm
	 * @return
	 */
	@Override
	public Optional<ErrorMessage> validate(ImportedGermplasm importedGermplasm)  {
		ErrorMessage message = null;

		if(importedGermplasm.getGid() == null || importedGermplasm.getGid() == 0 ){
			return Optional.absent();
		}

		String cropName = context.getProjectInContext().getCropType().getCropName();
		List<String> fCodecodedNames =  codedNamesLocator.locateCodeNamesForCrop(cropName);
		List<Name> names =  manager.getNameByGIDAndCodedName(importedGermplasm.getGid(),fCodecodedNames);

		if(names.size()>0){
			message= new ErrorMessage(ERROR_MESSAGE_GERMPLASM_USING_CODENAME);
			message.addParameters(importedGermplasm.getDesig());
		}
		return Optional.fromNullable(message);
	}
}

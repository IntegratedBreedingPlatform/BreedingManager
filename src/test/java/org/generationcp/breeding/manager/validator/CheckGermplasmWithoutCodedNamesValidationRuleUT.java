package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.components.validator.ErrorMessage;
import org.generationcp.middleware.manager.api.NamesDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CheckGermplasmWithoutCodedNamesValidationRuleUT {

	public static final String DUMMY_NOT_CODED_NAME = "DUMMY_NOT_CODED_NAME";
	private static final String DUMMY_CODED_NAME = "DUMMY_CODED_NAME";
	public static final String ERROR_MESSAGE_GERMPLASM_USING_CODENAME = "GERMPLSM_PARSE_USE_CODED_NAMES";
	public static final String CROP_NAME = "CROP_NAME";
	public static final String CODE_1 = "CODE1";

	CheckGermplasmWithoutCodedNamesValidationRule rule;

	@Mock
	private NamesDataManager manager;

	@Mock
	private CodeNamesLocator codedNameLocator;

	@Mock
	private ContextUtil contextUtil;

	List<String> codedCodes;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		rule = new CheckGermplasmWithoutCodedNamesValidationRule(codedNameLocator,manager,contextUtil);
		codedCodes = Lists.newArrayList(CODE_1);
		Project project = new Project();
		CropType cropType = new CropType();
		cropType.setCropName(CROP_NAME);
		project.setCropType(cropType);
		when(contextUtil.getProjectInContext()).thenReturn(project);
		when(codedNameLocator.locateCodeNamesForCrop(CROP_NAME)).thenReturn(codedCodes);
	}

	@Test
	public void validateGermplasmWithoutCodedNameReturnEmptyOptionalError() throws Exception {
		ImportedGermplasm context = createContext(DUMMY_NOT_CODED_NAME);

		List<Name> emptyList = Lists.newArrayList();

		when(manager.getNamesByNvalInFCodeList(DUMMY_NOT_CODED_NAME, codedCodes)).thenReturn(emptyList);

		Optional<ErrorMessage> optionalError = rule.validate(context);

		assertThat(optionalError.isPresent()).isFalse();

		verify(manager).getNamesByNvalInFCodeList(DUMMY_NOT_CODED_NAME, codedCodes);
	}

	@Test
	public void validateGermplasmWithCodedNameReturnErrorMessage() throws Exception {
		ImportedGermplasm context = createContext(DUMMY_CODED_NAME);
		Name elem1 = new Name();
		List<Name> codedNamesList = Lists.newArrayList(elem1);
		when(manager.getNamesByNvalInFCodeList(DUMMY_CODED_NAME, codedCodes)).thenReturn(codedNamesList);

		Optional<ErrorMessage> optionalError = rule.validate(context);

		assertThat(optionalError.isPresent()).isTrue();
		ErrorMessage errorMessage = optionalError.get();
		assertThat(errorMessage.getKey()).isEqualToIgnoringCase(ERROR_MESSAGE_GERMPLASM_USING_CODENAME);

		verify(manager).getNamesByNvalInFCodeList(DUMMY_CODED_NAME, codedCodes);
	}

	public ImportedGermplasm createContext(String name) {
		ImportedGermplasm context = new ImportedGermplasm();
		context.setDesig(name);
		return context;
	}
}

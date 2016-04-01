package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.components.validator.ErrorMessage;
import org.generationcp.middleware.manager.api.NamesDataManager;
import org.generationcp.middleware.pojos.Name;
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

	CheckGermplasmWithoutCodedNamesValidationRule rule;

	@Mock
	private NamesDataManager manager;

	@Mock
	private CodeNamesLocator codedNameLocator;

	List<Integer> codedIds;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		rule = new CheckGermplasmWithoutCodedNamesValidationRule(codedNameLocator,manager);
		Integer elem1 = new Integer(1);
		codedIds = Lists.newArrayList(elem1);
		when(codedNameLocator.getCodedNamesIds()).thenReturn(codedIds);
	}

	@Test
	public void validateGermplasmWithoutCodedNameReturnEmptyOptionalError() throws Exception {
		ImportedGermplasm context = createContext(DUMMY_NOT_CODED_NAME);

		List<Name> emptyList = Lists.newArrayList();

		when(manager.getNamesByNvalInTypeList(DUMMY_NOT_CODED_NAME,codedIds)).thenReturn(emptyList);

		Optional<ErrorMessage> optionalError = rule.validate(context);

		assertThat(optionalError.isPresent()).isFalse();

		verify(manager).getNamesByNvalInTypeList(DUMMY_NOT_CODED_NAME,codedIds);
	}

	@Test
	public void validateGermplasmWithCodedNameReturnErrorMessage() throws Exception {
		ImportedGermplasm context = createContext(DUMMY_CODED_NAME);
		Name elem1 = new Name();
		List<Name> codedNamesList = Lists.newArrayList(elem1);
		when(manager.getNamesByNvalInTypeList(DUMMY_CODED_NAME,codedIds)).thenReturn(codedNamesList);

		Optional<ErrorMessage> optionalError = rule.validate(context);

		assertThat(optionalError.isPresent()).isTrue();
		ErrorMessage errorMessage = optionalError.get();
		assertThat(errorMessage.getKey()).isEqualToIgnoringCase(ERROR_MESSAGE_GERMPLASM_USING_CODENAME);

		verify(manager).getNamesByNvalInTypeList(DUMMY_CODED_NAME,codedIds);
	}

	public ImportedGermplasm createContext(String name) {
		ImportedGermplasm context = new ImportedGermplasm();
		context.setDesig(name);
		return context;
	}
}

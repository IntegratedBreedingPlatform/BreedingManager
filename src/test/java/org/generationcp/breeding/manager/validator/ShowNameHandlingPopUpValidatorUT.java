package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.ErrorCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ShowNameHandlingPopUpValidatorUT {

	public static final String DUMMY_MESSAGE = "DUMMY_MESSAGE";
	public static final String INVALID_ELEMENT = "Invalid element";
	public static final int EXPECTED_SIZE = 1;
	ShowNameHandlingPopUpValidator target;

	@Mock
	CheckNameFactorsPresentValidationRule validationRule;

	private ErrorCollection errors;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		target = new ShowNameHandlingPopUpValidator();
		target.setCheckNameFactorsPresentValidationRule(validationRule);
		errors = new ErrorCollection();
	}

	@Test
	public void validatorExecuteAllValidationRulesWhenValidatingContext() throws Exception {

		ImportedGermplasm item = new ImportedGermplasm();
		List<ImportedGermplasm> list = Lists.newArrayList(item);
		Optional<String> success = Optional.absent();
		when(validationRule.validate(anyListOf(ImportedGermplasm.class))).thenReturn(success);


		ErrorCollection expectedErrorCollection = target.validate(list);

		assertThat(expectedErrorCollection.isEmpty()).isTrue();

		verify(validationRule).validate(anyListOf(ImportedGermplasm.class));
	}

	@Test
	public void validatorFailsWhenAtLeastOneValidationRuleFails() {
		ImportedGermplasm item = new ImportedGermplasm();
		List<ImportedGermplasm> list = Lists.newArrayList(item);
		Optional<String> failure = Optional.of(DUMMY_MESSAGE);
		when(validationRule.validate(list)).thenReturn(failure);


		ErrorCollection expectedErrorCollection = target.validate(list);

		assertThat(expectedErrorCollection).containsOnly(DUMMY_MESSAGE);
		assertThat(expectedErrorCollection).hasSize(EXPECTED_SIZE);
	}
}

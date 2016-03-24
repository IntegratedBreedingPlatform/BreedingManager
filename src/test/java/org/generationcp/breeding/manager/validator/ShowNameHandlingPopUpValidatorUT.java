package org.generationcp.breeding.manager.validator;

import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ShowNameHandlingPopUpValidatorUT {

	public static final String DUMMY_MESSAGE = "DUMMY_MESSAGE";
	public static final String INVALID_ELEMENT = "Invalid element";
	ShowNameHandlingPopUpValidator target;

	@Mock
	CheckNameFactorsPresentValidationRule validationRule;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		target = new ShowNameHandlingPopUpValidator();
		target.setCheckNameFactorsPresentValidationRule(validationRule);
	}

	@Test
	public void validatorExecuteAllValidationRulesWhenValidatingContext() throws Exception {

		ImportedGermplasm item = new ImportedGermplasm();
		List<ImportedGermplasm> list = Lists.newArrayList(item);

		target.validate(list);

		verify(validationRule).execute(list);
	}

	@Test
	public void validatorFailsWhenAtLeastOneValidationRuleFails() throws ExecutionException {
		ImportedGermplasm item = new ImportedGermplasm();
		List<ImportedGermplasm> list = Lists.newArrayList(item);
		when(validationRule.execute(list)).thenThrow(new ExecutionException(DUMMY_MESSAGE));

		try {
			target.validate(list);
		} catch (ExecutionException e) {
			assertThat(e).hasMessage(DUMMY_MESSAGE);
		}
	}
}

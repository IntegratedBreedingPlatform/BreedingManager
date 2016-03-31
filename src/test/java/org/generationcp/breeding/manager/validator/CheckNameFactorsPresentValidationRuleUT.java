package org.generationcp.breeding.manager.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.ErrorCollection;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import static org.fest.assertions.api.Assertions.assertThat;

public class CheckNameFactorsPresentValidationRuleUT {

	private static final String ERROR_MESSAGE_NO_NAMES_PRESENT = "No names were present in excel observation sheet";
	public static final String NAME_FACTOR = "NAME_FACTOR";
	public static final String EMPTY_STRING = "";
	public static final String NAME = "NAME";

	CheckNameFactorsPresentValidationRule validationRule;

	Map<String, String> nameFactors;
	private ErrorCollection errors;

	@Before
	public void setUp() throws Exception {
		validationRule = new CheckNameFactorsPresentValidationRule();
		nameFactors = new HashMap<>();
		nameFactors.put(NAME_FACTOR, EMPTY_STRING);
		errors = new ErrorCollection();
	}

	/**
	 * This test makes sure that the validation rule fails when no NameFactor appears in the
	 * list of imported {@link ImportedGermplasm}.
	 */
	@Test
	public void failsWhenNoImportedGermplasmContainsNameFactor() {
		ImportedGermplasm importedWithoutName = new ImportedGermplasm();
		importedWithoutName.setNameFactors(nameFactors);
		List<ImportedGermplasm> target = Lists.newArrayList(importedWithoutName);

		Optional<String> errorValidationMessage = validationRule.validate(target);

		assertThat(errorValidationMessage.isPresent()).isTrue();
		assertThat(errorValidationMessage.get()).isEqualToIgnoringCase(ERROR_MESSAGE_NO_NAMES_PRESENT);

	}
	/**
	 * This test makes sure that the validation only success when at least one NameFactor appears in the
	 * list of imported {@link ImportedGermplasm}.
	 */
	@Test
	public void validationRuleSuccessWhenAtLeastOneElementHasNameFactor() throws Exception {
		ImportedGermplasm importedWithName = new ImportedGermplasm();
		nameFactors.put(NAME_FACTOR, NAME);
		importedWithName.setNameFactors(nameFactors);
		List<ImportedGermplasm> target = Lists.newArrayList(importedWithName);

		Optional<String> errorValidationMessage = validationRule.validate(target);

		assertThat(errorValidationMessage.isPresent()).isFalse();

	}
}

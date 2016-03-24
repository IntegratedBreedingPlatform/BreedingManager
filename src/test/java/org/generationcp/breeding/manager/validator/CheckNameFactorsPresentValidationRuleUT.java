package org.generationcp.breeding.manager.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.components.validator.ExecutionException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class CheckNameFactorsPresentValidationRuleUT {

	private static final String ERROR_MESSAGE_NO_NAMES_PRESENT = "No names were present in excel observation sheet";
	public static final String NAME_FACTOR = "NAME_FACTOR";
	public static final String EMPTY_STRING = "";
	public static final String NAME = "NAME";

	CheckNameFactorsPresentValidationRule validationRule;

	Map<String,String> nameFactors;

	@Before
	public void setUp() throws Exception {
		validationRule = new CheckNameFactorsPresentValidationRule();
		nameFactors = new HashMap<>();
		nameFactors.put(NAME_FACTOR, EMPTY_STRING);
	}

	@Test
	public void failsWhenNoImportedGermplasmContainsNameFactor() {
		ImportedGermplasm importedWithoutName = new ImportedGermplasm();
		importedWithoutName.setNameFactors(nameFactors);
		List<ImportedGermplasm> target = Lists.newArrayList(importedWithoutName);
		try {
			validationRule.execute(target);
			fail("Should have failed");
		} catch (ExecutionException e) {
			assertThat(e).hasMessage(ERROR_MESSAGE_NO_NAMES_PRESENT);
		}
	}

	@Test
	public void importedListIsReturnedWhenAtLeastOneElementHasNameFactor() throws Exception {
		ImportedGermplasm importedWithName = new ImportedGermplasm();
		nameFactors.put(NAME_FACTOR, NAME);
		importedWithName.setNameFactors(nameFactors);
		List<ImportedGermplasm> target = Lists.newArrayList(importedWithName);

		List<ImportedGermplasm> validated = validationRule.execute(target);

		assertThat(validated).containsExactly(importedWithName );

	}
}

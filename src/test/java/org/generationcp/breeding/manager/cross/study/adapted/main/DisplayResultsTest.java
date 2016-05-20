
package org.generationcp.breeding.manager.cross.study.adapted.main;

import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CategoricalTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.breeding.manager.cross.study.constants.CategoricalVariatesCondition;
import org.generationcp.breeding.manager.cross.study.constants.NumericTraitCriteria;
import org.generationcp.breeding.manager.cross.study.constants.TraitWeight;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.ObservationKey;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import junit.framework.Assert;

public class DisplayResultsTest {

	@Mock
	private QueryForAdaptedGermplasmMain mainScreen;

	private DisplayResults displayResults;

	@Before
	public void setUp() {
		this.displayResults = Mockito.spy(new DisplayResults(this.mainScreen));
	}

	@Test
	public void testNumericTraitVal_ForMissingValue() {
		NumericTraitFilter numericTrait = this.initNumericTraitFilter(NumericTraitCriteria.KEEP_ALL);
		Observation observation = this.initObservation(1, 2, "missing");
		Assert.assertTrue(this.displayResults.testNumericTraitVal(numericTrait, observation));
	}

	private Observation initObservation(int traitId, int envId, String value) {
		ObservationKey key = new ObservationKey(traitId, envId);
		Observation observation = new Observation(key);
		observation.setValue(value);
		return observation;
	}

	private NumericTraitFilter initNumericTraitFilter(NumericTraitCriteria criteria) {
		TraitInfo traitInfo = new TraitInfo();
		NumericTraitFilter numericTrait = new NumericTraitFilter(traitInfo, criteria, null, TraitWeight.IMPORTANT);
		return numericTrait;
	}

	@Test
	public void testCategoricalTraitVal_ForMissingValue() {
		CategoricalTraitFilter categoricalTrait = this.initCategoricalTraitFilter(CategoricalVariatesCondition.KEEP_ALL);
		Observation observation = this.initObservation(1, 2, "missing");
		Assert.assertTrue(this.displayResults.testCategoricalTraitVal(categoricalTrait, observation));
	}

	private CategoricalTraitFilter initCategoricalTraitFilter(CategoricalVariatesCondition condition) {
		TraitInfo traitInfo = new TraitInfo();
		CategoricalTraitFilter categoricalTrait = new CategoricalTraitFilter(traitInfo, condition, null, TraitWeight.IMPORTANT);
		return categoricalTrait;
	}
}

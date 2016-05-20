
package org.generationcp.breeding.manager.cross.study.util.test;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.cross.study.h2h.main.TraitsAvailableComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.breeding.manager.cross.study.util.HeadToHeadResultsUtil;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.junit.Test;

import junit.framework.Assert;

public class HeadToHeadResultsUtilTest {

	private static final int GID1 = 2434138;
	private static final int GID2 = 1356114;

	private static final int TID1 = 22544;
	private static final int TID2 = 22564;

	private static final TraitForComparison TRAIT1 = new TraitForComparison(new TraitInfo(HeadToHeadResultsUtilTest.TID1),
			TraitsAvailableComponent.INCREASING);
	private static final TraitForComparison TRAIT2 = new TraitForComparison(new TraitInfo(HeadToHeadResultsUtilTest.TID2),
			TraitsAvailableComponent.DECREASING);

	private static Map<String, ObservationList> data = MockCrossStudyDataUtil.getHeadToHeadData(HeadToHeadResultsUtilTest.TID1,
			HeadToHeadResultsUtilTest.TID2, HeadToHeadResultsUtilTest.GID1, HeadToHeadResultsUtilTest.GID2);
	private static final GermplasmPair pair = new GermplasmPair(HeadToHeadResultsUtilTest.GID1, HeadToHeadResultsUtilTest.GID2);
	private static final List<EnvironmentForComparison> equalWeightEnvts = MockCrossStudyDataUtil.getEqualEnvironmentForComparisons();
	private static final List<EnvironmentForComparison> variedWeightEnvts = MockCrossStudyDataUtil.getVariedEnvironmentForComparisons();

	private static DecimalFormat decimalFormatter = new DecimalFormat("#,##0.00");

	@Test
	public void testGetPvalue() {
		int trials =
				HeadToHeadResultsUtil.getTotalNumOfEnv(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		int successes =
				HeadToHeadResultsUtil.getTotalNumOfSup(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);

		double pValue = HeadToHeadResultsUtil.getPvalue(trials, successes);
		String pValueFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(pValue);
		Assert.assertEquals("0.09", pValueFormatted);

		System.out.println("PVAL for (trial= " + trials + ", succeses=" + successes + "): " + pValueFormatted);
	}

	@Test
	public void testGetTotalNumOfEnv() {

		Integer environmentCount =
				HeadToHeadResultsUtil.getTotalNumOfEnv(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		Assert.assertEquals(21, environmentCount.intValue());
		System.out.println("Total common environments for Trait 1 = " + environmentCount);

		environmentCount =
				HeadToHeadResultsUtil.getTotalNumOfEnv(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT2,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		Assert.assertEquals(26, environmentCount.intValue());
		System.out.println("Total common environments for Trait 2 = " + environmentCount);
	}

	@Test
	public void testGetTotalNumOfSup() {

		Integer supEnvironmentCount =
				HeadToHeadResultsUtil.getTotalNumOfSup(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		Assert.assertEquals(13, supEnvironmentCount.intValue());
		System.out.println("Total sup for Trait 1 (increasing) = " + supEnvironmentCount);

		supEnvironmentCount =
				HeadToHeadResultsUtil.getTotalNumOfSup(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT2,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		Assert.assertEquals(9, supEnvironmentCount.intValue());
		System.out.println("Total sup for Trait 2 (decreasing) = " + supEnvironmentCount);
	}

	@Test
	public void testGetMean() {

		// Trait 1 Mean values
		Double meanTest =
				HeadToHeadResultsUtil.getMeanValue(HeadToHeadResultsUtilTest.pair, 1, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		String meanTestFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanTest);
		Assert.assertEquals("2,305.19", meanTestFormatted);

		Double meanStandard =
				HeadToHeadResultsUtil.getMeanValue(HeadToHeadResultsUtilTest.pair, 2, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		String meanStdFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanStandard);
		Assert.assertEquals("1,843.20", meanStdFormatted);

		System.out.println("Trait 1 >>> MeanTest = " + meanTestFormatted + ", MeanStandard = " + meanStdFormatted);

		// Trait 2 Mean values
		meanTest =
				HeadToHeadResultsUtil.getMeanValue(HeadToHeadResultsUtilTest.pair, 1, HeadToHeadResultsUtilTest.TRAIT2,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		meanTestFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanTest);
		Assert.assertEquals("94.30", meanTestFormatted);

		meanStandard =
				HeadToHeadResultsUtil.getMeanValue(HeadToHeadResultsUtilTest.pair, 2, HeadToHeadResultsUtilTest.TRAIT2,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		meanStdFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanStandard);
		Assert.assertEquals("86.68", meanStdFormatted);

		System.out.println("Trait 2 >>> MeanTest = " + meanTestFormatted + ", MeanStandard = " + meanStdFormatted);

	}

	@Test
	public void testGetMeanDiffEqualWeights() {
		// Trait 1 Mean Diff
		Double meanDiff =
				HeadToHeadResultsUtil.getMeanDiff(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		String diffFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanDiff);
		Assert.assertEquals("461.99", diffFormatted);

		System.out.println("Trait 1 >>> MeanDiff (equal weights) = " + diffFormatted);

		// Trait 2 Mean Diff (Decreasing)
		meanDiff =
				HeadToHeadResultsUtil.getMeanDiff(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT2,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.equalWeightEnvts);
		diffFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanDiff);
		Assert.assertEquals("-7.63", diffFormatted);

		System.out.println("Trait 2 >>> MeanDiff (equal weights) = " + diffFormatted);
	}

	@Test
	public void testGetMeanDiffVaryingWeights() {
		// Trait 1 Mean Diff
		Double meanDiff =
				HeadToHeadResultsUtil.getMeanDiff(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT1,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.variedWeightEnvts);
		String meanFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanDiff);
		Assert.assertEquals("264.82", meanFormatted);

		System.out.println("Trait 1 >>> MeanDiff (varied weights) = " + meanFormatted);

		// Trait 2 Mean Diff (Decreasing)
		meanDiff =
				HeadToHeadResultsUtil.getMeanDiff(HeadToHeadResultsUtilTest.pair, HeadToHeadResultsUtilTest.TRAIT2,
						HeadToHeadResultsUtilTest.data, HeadToHeadResultsUtilTest.variedWeightEnvts);
		meanFormatted = HeadToHeadResultsUtilTest.decimalFormatter.format(meanDiff);
		Assert.assertEquals("-8.01", meanFormatted);

		System.out.println("Trait 2 >>> MeanDiff (varied weights) = " + meanFormatted);
	}

}

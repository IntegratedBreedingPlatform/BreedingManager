package org.generationcp.browser.cross.study.util.test;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.generationcp.browser.cross.study.h2h.main.TraitsAvailableComponent;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.browser.cross.study.util.HeadToHeadResultsUtil;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.junit.Test;

public class TestHeadToHeadResultsUtil {
	
	private static final int GID1 = 2434138;
	private static final int GID2 = 1356114;
	
	private static final int TID1 = 22544;
	private static final int TID2 = 22564;
	
	private static final TraitForComparison TRAIT1 = 
		new TraitForComparison(new TraitInfo(TID1), TraitsAvailableComponent.INCREASING);
	private static final TraitForComparison TRAIT2 = 
		new TraitForComparison(new TraitInfo(TID2), TraitsAvailableComponent.DECREASING);
	
	private static Map<String, ObservationList> data = MockCrossStudyDataUtil.getHeadToHeadData(TID1, TID2, GID1, GID2);
	private static final GermplasmPair pair = new GermplasmPair(GID1, GID2);
	private static final List<EnvironmentForComparison> equalWeightEnvts = MockCrossStudyDataUtil.getEqualEnvironmentForComparisons();
	private static final List<EnvironmentForComparison> variedWeightEnvts = MockCrossStudyDataUtil.getVariedEnvironmentForComparisons();
	
	private static DecimalFormat decimalFormmatter = new DecimalFormat("#,##0.00");
	
	@Test
	public void testGetPvalue(){
		int trials =HeadToHeadResultsUtil.getTotalNumOfEnv(pair, TRAIT1, data, equalWeightEnvts);
		Double standardMean = HeadToHeadResultsUtil.getMeanValue(pair, 2, TRAIT1, data, equalWeightEnvts);
		
		double pValue = HeadToHeadResultsUtil.getPvalue(trials, standardMean);
		Assert.assertTrue(pValue >= 0);
		Assert.assertTrue(pValue <= 0);
		
		System.out.println("PVAL for (trial= " + trials + ", stdMean=" + standardMean + "): "
				+ pValue);
	}

	@Test
	public void testGetTotalNumOfEnv(){
		
		Integer environmentCount = HeadToHeadResultsUtil.getTotalNumOfEnv(pair, TRAIT1, data, equalWeightEnvts);
		Assert.assertEquals(21, environmentCount.intValue());
		System.out.println("Total common environments for Trait 1 = " + environmentCount);
		
		environmentCount = HeadToHeadResultsUtil.getTotalNumOfEnv(pair, TRAIT2, data, equalWeightEnvts);
		Assert.assertEquals(26, environmentCount.intValue());
		System.out.println("Total common environments for Trait 2 = " + environmentCount);
	}
	
	@Test
	public void testGetTotalNumOfSup(){
		
		Integer supEnvironmentCount = HeadToHeadResultsUtil.getTotalNumOfSup(pair, TRAIT1, data, equalWeightEnvts);
		Assert.assertEquals(13, supEnvironmentCount.intValue());
		System.out.println("Total sup for Trait 1 (increasing) = " + supEnvironmentCount);
		
		supEnvironmentCount = HeadToHeadResultsUtil.getTotalNumOfSup(pair, TRAIT2, data, equalWeightEnvts);
		Assert.assertEquals(9, supEnvironmentCount.intValue());
		System.out.println("Total sup for Trait 2 (decreasing) = " + supEnvironmentCount);
	}
	
	@Test
	public void testGetMean(){
		
		// Trait 1 Mean values
		Double meanTest = HeadToHeadResultsUtil.getMeanValue(pair, 1, TRAIT1, data, equalWeightEnvts);
		String meanTestFormatted = decimalFormmatter.format(meanTest);
		Assert.assertEquals("2,305.19", meanTestFormatted);
		
		Double meanStandard = HeadToHeadResultsUtil.getMeanValue(pair, 2, TRAIT1, data, equalWeightEnvts);
		String meanStdFormatted = decimalFormmatter.format(meanStandard);
		Assert.assertEquals("1,843.20", meanStdFormatted);
		
		System.out.println("Trait 1 >>> MeanTest = " + meanTestFormatted + ", MeanStandard = " + meanStdFormatted);
		
		
		// Trait 2 Mean values
		meanTest = HeadToHeadResultsUtil.getMeanValue(pair, 1, TRAIT2, data, equalWeightEnvts);
		meanTestFormatted = decimalFormmatter.format(meanTest);
		Assert.assertEquals("94.30", meanTestFormatted);
		
		meanStandard = HeadToHeadResultsUtil.getMeanValue(pair, 2, TRAIT2, data, equalWeightEnvts);
		meanStdFormatted = decimalFormmatter.format(meanStandard);
		Assert.assertEquals("86.68", meanStdFormatted);
		
		System.out.println("Trait 2 >>> MeanTest = " + meanTestFormatted + ", MeanStandard = " + meanStdFormatted);
		
	}
	
	@Test
	public void testGetMeanDiffEqualWeights(){
		//Trait 1 Mean Diff
		Double meanDiff = HeadToHeadResultsUtil.getMeanDiff(pair, TRAIT1, data, equalWeightEnvts);
		String diffFormatted = decimalFormmatter.format(meanDiff);
		Assert.assertEquals("461.99", diffFormatted);
		
		System.out.println("Trait 1 >>> MeanDiff (equal weights) = " + diffFormatted);
		
		//Trait 2 Mean Diff (Decreasing)
		meanDiff = HeadToHeadResultsUtil.getMeanDiff(pair, TRAIT2, data, equalWeightEnvts);
		diffFormatted = decimalFormmatter.format(meanDiff);
		Assert.assertEquals("-7.63", diffFormatted);
		
		System.out.println("Trait 2 >>> MeanDiff (equal weights) = " + diffFormatted);
	}
	
	@Test
	public void testGetMeanDiffVaryingWeights(){
		// Trait 1 Mean Diff
		Double meanDiff = HeadToHeadResultsUtil.getMeanDiff(pair, TRAIT1, data, variedWeightEnvts);
		String meanFormatted = decimalFormmatter.format(meanDiff);
		Assert.assertEquals("264.82", meanFormatted);
		
		System.out.println("Trait 1 >>> MeanDiff (varied weights) = " + meanFormatted);
		
		// Trait 2 Mean Diff (Decreasing)
		meanDiff = HeadToHeadResultsUtil.getMeanDiff(pair, TRAIT2, data, variedWeightEnvts);
		meanFormatted = decimalFormmatter.format(meanDiff);
		Assert.assertEquals("-8.01", meanFormatted);
		
		System.out.println("Trait 2 >>> MeanDiff (varied weights) = " + meanFormatted);
	}
	
}

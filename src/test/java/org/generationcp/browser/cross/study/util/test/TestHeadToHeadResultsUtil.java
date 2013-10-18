package org.generationcp.browser.cross.study.util.test;

import junit.framework.Assert;

import org.generationcp.browser.cross.study.util.HeadToHeadResultsUtil;
import org.junit.Test;

public class TestHeadToHeadResultsUtil {
	
	@Test
	public void testGetPvalue(){
		int trials = 21;
		Double standardMean = 2305.19;
		
		double pValue = HeadToHeadResultsUtil.getPvalue(trials, standardMean);
		Assert.assertTrue(pValue >= 0);
		Assert.assertTrue(pValue <= 0);
		
		System.out.println("PVAL for (trial= " + trials + ", stdMean=" + standardMean + "): "
				+ pValue);
	}

}

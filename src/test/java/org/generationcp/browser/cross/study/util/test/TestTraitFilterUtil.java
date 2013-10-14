package org.generationcp.browser.cross.study.util.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.browser.cross.study.util.TraitFilterUtil;
import org.junit.Test;


public class TestTraitFilterUtil {
	
	@Test
	public void testValidateCharacterTraitLimits(){
		List<String> values = new ArrayList<String>();
		values.add("mild");
		values.add("strong");
		values.add("weak");
		
		List<String> correctLimits = new ArrayList<String>();
		correctLimits.add("mild");
		correctLimits.add("strong");
		
		List<String> fromValidation = TraitFilterUtil.validateCharacterTraitLimits(values, correctLimits);
		Assert.assertTrue(fromValidation.isEmpty());
		
		List<String> wrongLimits = new ArrayList<String>();
		wrongLimits.add("none");
		wrongLimits.add("mild");
		wrongLimits.add("pure");
		
		fromValidation = TraitFilterUtil.validateCharacterTraitLimits(values, wrongLimits);
		Assert.assertTrue(!fromValidation.isEmpty());
		Assert.assertTrue(fromValidation.size() == 2);
		
		String value1FromValidation = fromValidation.get(0);
		String value2FromValidation = fromValidation.get(1);
		
		Assert.assertTrue(value1FromValidation.equals("none") || value1FromValidation.equals("pure"));
		Assert.assertTrue(value2FromValidation.equals("none") || value2FromValidation.equals("pure"));
	}

}

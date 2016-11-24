package org.generationcp.breeding.manager.crossingmanager.actions;

import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.commons.ruleengine.ProcessCodeOrderedRule;
import org.generationcp.commons.ruleengine.ProcessCodeRuleFactory;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GenerateCrossNameActionTest {

	public static final Integer TEST_FEMALE_GID = 12345;
	public static final Integer TEST_MALE_GID = 54321;
	public static final Integer TEST_BREEDING_METHOD_ID = 5;
	public static final String TEST_PROCESS_CODE = "[BC]";
	public static final String PREFIX = "PREFIX";

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ProcessCodeRuleFactory processCodeRuleFactory;

	@Mock
	private PedigreeDataManager pedigreeDataManager;

	@InjectMocks
	private GenerateCrossNameAction generateCrossNameAction;

	@Mock
	private ProcessCodeOrderedRule processCodeOrderedRule;

	@Before
	public void init() {

		Mockito.when(this.processCodeRuleFactory.getRuleByProcessCode(Mockito.anyString())).thenReturn(this.processCodeOrderedRule);
		generateCrossNameAction.setSetting(this.createCrossNameSetting());

	}

	@Test
	public void testBuildDesignationNameInSequenceMethodSuffixProcessCodeIsAvailable() throws RuleException {

		final String resolvedSuffixString = "AAA";
		final int sequenceNumber = 1;

		final Method breedingMethod = new Method(TEST_BREEDING_METHOD_ID);
		breedingMethod.setSuffix(TEST_PROCESS_CODE);

		Mockito.when(this.germplasmDataManager.getMethodByID(TEST_BREEDING_METHOD_ID)).thenReturn(breedingMethod);
		Mockito.when(this.processCodeOrderedRule.runRule(Mockito.any(RuleExecutionContext.class))).thenReturn(resolvedSuffixString);

		final Germplasm germplasm = new Germplasm();
		germplasm.setGpid1(TEST_FEMALE_GID);
		germplasm.setGpid2(TEST_MALE_GID);

		final String designationName = this.generateCrossNameAction.buildNextNameInSequence(TEST_BREEDING_METHOD_ID, germplasm,
				sequenceNumber);

		Assert.assertEquals("PREFIX1AAA", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceMethodSuffixIsBlank() throws RuleException {

		final int sequenceNumber = 1;

		final Method breedingMethod = new Method(TEST_BREEDING_METHOD_ID);

		Mockito.when(this.germplasmDataManager.getMethodByID(TEST_BREEDING_METHOD_ID)).thenReturn(breedingMethod);

		final Germplasm germplasm = new Germplasm();
		germplasm.setGpid1(TEST_FEMALE_GID);
		germplasm.setGpid2(TEST_MALE_GID);

		final String designationName = this.generateCrossNameAction.buildNextNameInSequence(TEST_BREEDING_METHOD_ID, germplasm,
				sequenceNumber);

		// Since there's no process code in method's suffix, the processCodeOrderedRule should not be invoked.
		Mockito.verify(this.processCodeOrderedRule, Mockito.times(0)).runRule(Mockito.any(RuleExecutionContext.class));

		Assert.assertEquals("PREFIX1", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceMethodIsNotSpecified() throws RuleException {

		final int sequenceNumber = 1;
		// methodId is null if breeding method is not specified
		final Integer methodId = null;

		final Germplasm germplasm = new Germplasm();
		germplasm.setGpid1(TEST_FEMALE_GID);
		germplasm.setGpid2(TEST_MALE_GID);

		final String designationName = this.generateCrossNameAction.buildNextNameInSequence(methodId, germplasm,
				sequenceNumber);

		// If the method is not specified, the germplasmDataManager.getMethodByID should not be invoked.
		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getMethodByID(TEST_BREEDING_METHOD_ID);

		// Since there's no process code in method's suffix, the processCodeOrderedRule should not be invoked.
		Mockito.verify(this.processCodeOrderedRule, Mockito.times(0)).runRule(Mockito.any(RuleExecutionContext.class));

		Assert.assertEquals("PREFIX1", designationName);
	}

	private CrossNameSetting createCrossNameSetting() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix(PREFIX);
		return setting;
	}

}

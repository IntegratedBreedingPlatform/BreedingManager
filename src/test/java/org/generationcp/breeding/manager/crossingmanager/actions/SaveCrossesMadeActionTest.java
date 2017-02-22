
package org.generationcp.breeding.manager.crossingmanager.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.springframework.transaction.PlatformTransactionManager;

public class SaveCrossesMadeActionTest {

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private GermplasmDataManager germplasmManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private GermplasmGroupingService germplasmGroupingService;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	private SaveCrossesMadeAction action;

	private CrossesMade crossesMade;
	private ArrayList<Integer> germplasmIDs;
	private GermplasmList germplasmList;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.action = Mockito.spy(new SaveCrossesMadeAction());
		this.action.setContextUtil(this.contextUtil);
		this.action.setTransactionManager(this.transactionManager);
		this.action.setGermplasmListManager(this.germplasmListManager);
		this.action.setGermplasmGroupingService(this.germplasmGroupingService);
		this.action.setCrossExpansionProperties(this.crossExpansionProperties);

		this.crossesMade = new CrossesMade();
		this.crossesMade.setCrossesMap(this.getCrossesMap());
		this.crossesMade.setSetting(this.getCrossingSetting());
		this.germplasmIDs = this.getGermplasmIDs();
		this.germplasmList = this.getGermplasmList();
	}

	@Test
	public void testSaveRecords_WhenSaveAsParentageDesignationIsTrue() {

		this.crossesMade.getSetting().getCrossNameSetting().setSaveParentageDesignationAsAString(true);

		this.setUpReturnValueForSaveRecordsMethods();

		this.action.saveRecords(this.crossesMade);
		try {
			Mockito.verify(this.action, Mockito.times(1)).savePedigreeDesignationName(this.crossesMade, this.germplasmIDs);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting to save parentage designation namebut didn't.");
		}

		Mockito.verify(this.germplasmGroupingService).processGroupInheritanceForCrosses(Mockito.anyList(), Mockito.anyBoolean(),
				Mockito.anySet());
	}

	@Test
	public void testSaveRecords_WhenSaveAsParentageDesignationIsFalse() {
		this.crossesMade.getSetting().getCrossNameSetting().setSaveParentageDesignationAsAString(false);

		this.setUpReturnValueForSaveRecordsMethods();

		this.action.saveRecords(this.crossesMade);
		try {
			Mockito.verify(this.action, Mockito.times(0)).savePedigreeDesignationName(this.crossesMade, this.germplasmIDs);
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Expecting to NOT save parentage designation names but didn't.");
		}
		
		Mockito.verify(this.germplasmGroupingService).processGroupInheritanceForCrosses(Mockito.anyList(), Mockito.anyBoolean(),
				Mockito.anySet());
	}

	private void setUpReturnValueForSaveRecordsMethods() {
		Mockito.doReturn(this.germplasmIDs).when(this.action).saveGermplasmsAndNames(this.crossesMade);
		Mockito.doReturn(this.germplasmList).when(this.action).saveGermplasmListRecord(this.crossesMade);
		Mockito.doNothing().when(this.action).savePedigreeDesignationName(this.crossesMade, this.germplasmIDs);
		Mockito.doNothing().when(this.action).saveGermplasmListDataRecords(this.crossesMade, this.germplasmIDs, this.germplasmList);
	}

	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		return germplasmList;
	}

	private ArrayList<Integer> getGermplasmIDs() {
		ArrayList<Integer> germplasmIDs = new ArrayList<Integer>();
		germplasmIDs.add(1);
		germplasmIDs.add(2);
		germplasmIDs.add(3);
		return germplasmIDs;
	}

	private CrossingManagerSetting getCrossingSetting() {
		CrossingManagerSetting toreturn = new CrossingManagerSetting();

		CrossNameSetting crossNameSettingPojo = new CrossNameSetting();
		crossNameSettingPojo.setAddSpaceBetweenPrefixAndCode(true);
		crossNameSettingPojo.setAddSpaceBetweenSuffixAndCode(true);
		crossNameSettingPojo.setNumOfDigits(3);
		crossNameSettingPojo.setPrefix("BMS");
		crossNameSettingPojo.setSaveParentageDesignationAsAString(true);
		crossNameSettingPojo.setSeparator("/");
		crossNameSettingPojo.setStartNumber(1);
		crossNameSettingPojo.setSuffix("END");
		toreturn.setCrossNameSetting(crossNameSettingPojo);

		AdditionalDetailsSetting additionalDetails = new AdditionalDetailsSetting(1, Util.getCurrentDateAsStringValue());
		toreturn.setAdditionalDetailsSetting(additionalDetails);

		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting(1, true);
		toreturn.setBreedingMethodSetting(breedingMethodSetting);

		toreturn.setName("BMS Template Name");

		return toreturn;
	}

	private Map<Germplasm, Name> getCrossesMap() {
		Map<Germplasm, Name> crossesMap = new HashMap<Germplasm, Name>();

		for (int i = 1; i <= 5; i++) {
			Germplasm germplasm = new Germplasm();
			germplasm.setGid(i);
			germplasm.setGpid1(1 + i);
			germplasm.setGpid2(2 + i);

			Name name = new Name();
			name.setNval("Designation Name" + i + ", " + " List Name");
			name.setTypeId(1);

			crossesMap.put(germplasm, name);
		}

		return crossesMap;
	}
}

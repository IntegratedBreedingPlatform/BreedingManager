
package org.generationcp.breeding.manager.crossingmanager.actions;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
	private CrossExpansionProperties crossExpansionProperties;

	private SaveCrossesMadeAction action;

	private CrossesMade crossesMade;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.action = Mockito.spy(new SaveCrossesMadeAction());
		this.action.setContextUtil(this.contextUtil);
		this.action.setTransactionManager(this.transactionManager);
		this.action.setGermplasmListManager(this.germplasmListManager);
		this.action.setCrossExpansionProperties(this.crossExpansionProperties);

		this.crossesMade = new CrossesMade();
		this.crossesMade.setCrossesMap(this.getCrossesMap());
		this.crossesMade.setSetting(this.getCrossingSetting());
	}

	private CrossSetting getCrossingSetting() {
		CrossSetting toreturn = new CrossSetting();

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

		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting(1, true, false);
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


package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainer;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainerUpdateListener;
import org.generationcp.breeding.manager.crossingmanager.actions.GenerateCrossNameAction;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.util.CollectionTransformationUtil;
import org.generationcp.commons.util.CrossingUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@Configurable
public class ApplyCrossingSettingAction implements CrossesMadeContainerUpdateListener {

	private final static Logger LOG = LoggerFactory.getLogger(ApplyCrossingSettingAction.class);

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	@Autowired
	private GermplasmListManager germplasmListManager;

	private final CrossingManagerSetting setting;
	private CrossesMadeContainer container;

	public ApplyCrossingSettingAction(CrossingManagerSetting setting) {
		this.setting = setting;
	}

	@Override
	public boolean updateCrossesMadeContainer(CrossesMadeContainer container) {
		this.container = container;

		return this.applyBreedingMethodSetting() && this.applyNameSetting() && this.applyAdditionalDetailsSetting();
	}

	/**
	 * Set breeding method of germplasm based on configuration in setting. Can be same for all crosses or based on status of parental lines
	 *
	 * @return
	 */
	private boolean applyBreedingMethodSetting() {
		BreedingMethodSetting methodSetting = this.setting.getBreedingMethodSetting();

		if (this.container != null && this.container.getCrossesMade() != null && this.container.getCrossesMade().getCrossesMap() != null) {

			// Use same breeding method for all crosses
			final Set<Germplasm> germplasms = this.container.getCrossesMade().getCrossesMap().keySet();
			if (!methodSetting.isBasedOnStatusOfParentalLines()) {
				Integer breedingMethodSelected = methodSetting.getMethodId();
				for (Germplasm germplasm : germplasms) {
					germplasm.setMethodId(breedingMethodSelected);
				}

				// Use CrossingManagerUtil to set breeding method based on parents
			} else {
				final ImmutableMap<Integer, Germplasm> gidAncestry = getGermplasmAncestryAsMap(germplasms);
				for (Germplasm germplasm : germplasms) {
					Integer femaleGid = germplasm.getGpid1();
					Integer maleGid = germplasm.getGpid2();

					try {
						Germplasm female = gidAncestry.get(femaleGid);
						Germplasm male = gidAncestry.get(maleGid);

						Germplasm motherOfFemale = null;
						Germplasm fatherOfFemale = null;
						if (female != null) {
							motherOfFemale = gidAncestry.get(female.getGpid1());
							fatherOfFemale = gidAncestry.get(female.getGpid2());
						}

						Germplasm motherOfMale = null;
						Germplasm fatherOfMale = null;
						if (male != null) {
							motherOfMale = gidAncestry.get(male.getGpid1());
							fatherOfMale = gidAncestry.get(male.getGpid2());
						}
						
						germplasm.setMethodId(CrossingUtil.determineBreedingMethodBasedOnParentalLine(female, male, motherOfFemale, fatherOfFemale, motherOfMale, fatherOfMale));
					} catch (MiddlewareQueryException e) {
						LOG.error(e.getMessage(), e);
						return false;
					}

				}
			}
			Integer crossingNameTypeId = null;
			try {
				crossingNameTypeId = BreedingManagerUtil.getIDForUserDefinedFieldCrossingName(this.germplasmListManager);
			} catch (MiddlewareQueryException e) {
				ApplyCrossingSettingAction.LOG.error(e.getMessage(), e);
			}

			List<Pair<Germplasm, Name>> germplasmPairs = extractGermplasmPairList(this.container.getCrossesMade().getCrossesMap());

			CrossingUtil.applyMethodNameType(this.germplasmDataManager, germplasmPairs, crossingNameTypeId);
			return true;

		}

		return false;
	}

	private ImmutableMap<Integer, Germplasm> getGermplasmAncestryAsMap(final Set<Germplasm> germplasms) {
		final ImmutableSet<Integer> allFemaleParentGidsFromGermplasmList = CollectionTransformationUtil.getAllFemaleParentGidsFromGermplasmList(germplasms);
		final ImmutableSet<Integer> allMaleParentGidsFromGermplasmList = CollectionTransformationUtil.getAllMaleParentGidsFromGermplasmList(germplasms);
		final ImmutableSet<Integer> femaleAndMaleParentGids =
				new ImmutableSet.Builder<Integer>().addAll(allFemaleParentGidsFromGermplasmList)
						.addAll(allMaleParentGidsFromGermplasmList).build();
		final List<Germplasm> germplasmWithAllNamesAndAncestry =
				this.germplasmDataManager.getGermplasmWithAllNamesAndAncestry(femaleAndMaleParentGids, 1);
		return CollectionTransformationUtil.getGermplasmMap(germplasmWithAllNamesAndAncestry);
	}

	protected List<Pair<Germplasm, Name>> extractGermplasmPairList(Map<Germplasm, Name> germplasmNameMap) {
		List<Pair<Germplasm, Name>> returnValue = new ArrayList<>();
		for (Map.Entry<Germplasm, Name> germplasmNameEntry : germplasmNameMap.entrySet()) {
			returnValue.add(new ImmutablePair<Germplasm, Name>(germplasmNameEntry.getKey(), germplasmNameEntry.getValue()));
		}

		return returnValue;
	}

	/**
	 * Generate values for NAME record plus Germplasm List Entry designation based on cross name setting configuration
	 *
	 * @return
	 */
	private boolean applyNameSetting() {
		CrossNameSetting nameSetting = this.setting.getCrossNameSetting();

		if (this.container != null && this.container.getCrossesMade() != null && this.container.getCrossesMade().getCrossesMap() != null) {

			GenerateCrossNameAction generateNameAction = new GenerateCrossNameAction();
			int ctr = 1;
			try {
				ctr = generateNameAction.getNextNumberInSequence(nameSetting);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
				return false;
			}

			Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
			List<GermplasmListEntry> oldCrossNames = new ArrayList<GermplasmListEntry>();

			// Store old cross name and generate new names based on prefix, suffix specifications
			for (Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()) {
				Germplasm germplasm = entry.getKey();
				Name nameObject = entry.getValue();
				String oldCrossName = nameObject.getNval();
				String nextName = generateNameAction.buildNextNameInSequence(this.setting.getBreedingMethodSetting().getMethodId(), germplasm, ctr++);
				nameObject.setNval(nextName);

				Integer tempGid = germplasm.getGid();
				GermplasmListEntry oldNameEntry = new GermplasmListEntry(tempGid, tempGid, tempGid, oldCrossName);

				oldCrossNames.add(oldNameEntry);
			}
			// Only store the "original" cross names, would not store previous names on 2nd, 3rd, ... change
			if (this.container.getCrossesMade().getOldCrossNames() == null || this.container.getCrossesMade().getOldCrossNames().isEmpty()) {
				this.container.getCrossesMade().setOldCrossNames(oldCrossNames);
			}

			return true;

		}

		return false;

	}

	/**
	 * Set GERMPLSM location id and gdate and NAME location id and ndate based on harvest date and location information given in setting
	 *
	 * @return
	 */
	private boolean applyAdditionalDetailsSetting() {
		AdditionalDetailsSetting detailsSetting = this.setting.getAdditionalDetailsSetting();
		if (this.container != null && this.container.getCrossesMade() != null && this.container.getCrossesMade().getCrossesMap() != null) {

			Integer dateIntValue = 0;
			Integer harvestLocationId = 0;

			if (detailsSetting.getHarvestLocationId() != null) {
				harvestLocationId = detailsSetting.getHarvestLocationId();
			}

			if (detailsSetting.getHarvestDate() != null) {
				dateIntValue = Integer.parseInt(detailsSetting.getHarvestDate());
			}

			Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
			for (Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()) {
				Germplasm germplasm = entry.getKey();
				germplasm.setLocationId(harvestLocationId);
				germplasm.setGdate(dateIntValue);

				Name name = entry.getValue();
				name.setLocationId(harvestLocationId);
				name.setNdate(dateIntValue);
			}
			return true;
		}

		return false;
	}
}

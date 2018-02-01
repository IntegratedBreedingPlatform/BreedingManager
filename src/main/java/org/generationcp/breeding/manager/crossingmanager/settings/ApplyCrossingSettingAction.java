
package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainer;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainerUpdateListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
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

@Configurable
public class ApplyCrossingSettingAction implements CrossesMadeContainerUpdateListener {

	private static final Logger LOG = LoggerFactory.getLogger(ApplyCrossingSettingAction.class);

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	@Autowired
	private GermplasmListManager germplasmListManager;

	private CrossesMadeContainer container;

	@Override
	public boolean updateCrossesMadeContainer(final CrossesMadeContainer container) {
		this.container = container;
		return this.applyBreedingMethodSetting() && this.applyNameSetting() && this.applyAdditionalDetailsSetting();
	}

	/**
	 * Set breeding method of germplasm based on configuration in setting. Can be same for all crosses or based on status of parental lines
	 *
	 * @return
	 */
	private boolean applyBreedingMethodSetting() {
		if (this.container != null && this.container.getCrossesMade() != null && this.container.getCrossesMade().getCrossesMap() != null) {

			// Use same breeding method for all crosses
			final Set<Germplasm> germplasms = this.container.getCrossesMade().getCrossesMap().keySet();
			for (final Germplasm germplasm : germplasms) {
				//Set the method id to Single Cross(101) for now, it will be overwritten in the nursery side
				germplasm.setMethodId(101);
			}

			Integer crossingNameTypeId = null;
			try {
				crossingNameTypeId = BreedingManagerUtil.getIDForUserDefinedFieldCrossingName(this.germplasmListManager);
			} catch (final MiddlewareQueryException e) {
				ApplyCrossingSettingAction.LOG.error(e.getMessage(), e);
			}

			final List<Pair<Germplasm, Name>> germplasmPairs =
					this.extractGermplasmPairList(this.container.getCrossesMade().getCrossesMap());

			CrossingUtil.applyMethodNameType(this.germplasmDataManager, germplasmPairs, crossingNameTypeId);
			return true;

		}

		return false;
	}

	protected List<Pair<Germplasm, Name>> extractGermplasmPairList(final Map<Germplasm, Name> germplasmNameMap) {
		final List<Pair<Germplasm, Name>> returnValue = new ArrayList<>();
		for (final Map.Entry<Germplasm, Name> germplasmNameEntry : germplasmNameMap.entrySet()) {
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
		if (this.container != null && this.container.getCrossesMade() != null && this.container.getCrossesMade().getCrossesMap() != null) {
			int ctr = 1;

			final Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
			final List<GermplasmListEntry> oldCrossNames = new ArrayList<>();

			// Store old cross name and generate new names based on prefix, suffix specifications
			for (final Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()) {
				final Germplasm germplasm = entry.getKey();
				final Name nameObject = entry.getValue();
				final String oldCrossName = nameObject.getNval();
				nameObject.setNval(String.valueOf(ctr++));

				final Integer tempGid = germplasm.getGid();
				final GermplasmListEntry oldNameEntry = new GermplasmListEntry(tempGid, tempGid, tempGid, oldCrossName);

				oldCrossNames.add(oldNameEntry);
			}
			// Only store the "original" cross names, would not store previous names on 2nd, 3rd, ... change
			if (this.container.getCrossesMade().getOldCrossNames() == null
					|| this.container.getCrossesMade().getOldCrossNames().isEmpty()) {
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
		if (this.container != null && this.container.getCrossesMade() != null && this.container.getCrossesMade().getCrossesMap() != null) {
			//the date and harvest location will be overwritten in the nursery side.
			Integer dateIntValue = 0;
			Integer harvestLocationId = 0;

			final Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
			for (final Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()) {
				final Germplasm germplasm = entry.getKey();
				germplasm.setLocationId(harvestLocationId);
				germplasm.setGdate(dateIntValue);

				final Name name = entry.getValue();
				name.setLocationId(harvestLocationId);
				name.setNdate(dateIntValue);
			}
			return true;
		}

		return false;
	}
}

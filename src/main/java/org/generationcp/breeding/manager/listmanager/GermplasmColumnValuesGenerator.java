
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@Configurable
public class GermplasmColumnValuesGenerator {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private FillColumnSource fillColumnSource;
	
	public GermplasmColumnValuesGenerator(final FillColumnSource fillColumnSource){
		this.fillColumnSource = fillColumnSource;
	}

	public void setPreferredIdColumnValues() {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			String preferredID = "";
			// TODO Optimize in one-off Middleware query for all GIDs
			final Name name = this.germplasmDataManager.getPreferredIdByGID(gid);
			if (name != null && name.getNval() != null) {
				preferredID = name.getNval();
			}
			this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.PREFERRED_ID.getName(), preferredID);
		}

		this.fillColumnSource.propagateUIChanges();
	}

	public void setPreferredNameColumnValues() {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, String> gidPreferredNamesMap = this.germplasmDataManager.getPreferredNamesByGids(gids);
		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			String preferredName = "";
			if (gidPreferredNamesMap.get(gid) != null) {
				preferredName = gidPreferredNamesMap.get(gid);
			}
			this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.PREFERRED_NAME.getName(), preferredName);
		}

		this.fillColumnSource.propagateUIChanges();
	}

	public void setGermplasmDateColumnValues() {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, Integer> germplasmGidDateMap = this.germplasmDataManager.getGermplasmDatesByGids(gids);
		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);

			if (germplasmGidDateMap.get(gid) == null) {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_DATE.getName(), "");
			} else {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_DATE.getName(), germplasmGidDateMap.get(gid));
			}
		}

		this.fillColumnSource.propagateUIChanges();
	}

	public void setLocationNameColumnValues() {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, String> locationNamesMap = this.germplasmDataManager.getLocationNamesByGids(gids);

		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			if (locationNamesMap.get(gid) == null) {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_LOCATION.getName(), "");
			} else {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_LOCATION.getName(), locationNamesMap.get(gid));
			}
		}

		this.fillColumnSource.propagateUIChanges();
	}

	public void setMethodInfoColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, Object> methodsMap = this.germplasmDataManager.getMethodsByGids(gids);

		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);

			if (methodsMap.get(gid) == null) {
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, "");
			} else {
				String value = "";

				if (columnName.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
					value = ((Method) methodsMap.get(gid)).getMname();
				} else if (columnName.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
					value = ((Method) methodsMap.get(gid)).getMcode();
				} else if (columnName.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
					value = ((Method) methodsMap.get(gid)).getMid().toString();
				} else if (columnName.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
					value = ((Method) methodsMap.get(gid)).getMgrp();
				}
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, value);
			}
		}

		this.fillColumnSource.propagateUIChanges();
	}

	public void setCrossMaleGIDColumnValues() {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		ImmutableMap<Integer, Germplasm> germplasmMap = this.retrieveGermplasmAndGenerateMap(gids);
		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			final Germplasm germplasm = germplasmMap.get(gid);

			if (germplasm != null) {
				if (germplasm.getGnpgs() >= 2) {
					if (germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
						this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_GID.getName(),
								germplasm.getGpid2().toString());
					} else {
						this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_GID.getName(), "-");
					}
				} else {
					this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_GID.getName(), "-");
				}
			} else {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_GID.getName(), "-");
			}
		}

		this.fillColumnSource.propagateUIChanges();
	}

	private ImmutableMap<Integer, Germplasm> retrieveGermplasmAndGenerateMap(final List<Integer> gids) {
		final List<Germplasm> germplasmList = this.germplasmDataManager.getGermplasms(gids);
		ImmutableMap<Integer, Germplasm> germplasmMap = null;
		if (germplasmList != null) {
			germplasmMap = Maps.uniqueIndex(germplasmList, new Function<Germplasm, Integer>() {

				@Override
				public Integer apply(final Germplasm germplasm) {
					return germplasm.getGid();
				}
			});
		}
		return germplasmMap;
	}

	public void setCrossMalePrefNameColumnValues() {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		ImmutableMap<Integer, Germplasm> germplasmMap = this.retrieveGermplasmAndGenerateMap(gids);

		final Map<Integer, List<Object>> gidToItemIdMap = new HashMap<>();
		final List<Integer> gidsToUseForQuery = new ArrayList<>();

		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			final Germplasm germplasm = germplasmMap.get(gid);

			if (germplasm != null) {
				if (germplasm.getGnpgs() >= 2 && germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
					gidsToUseForQuery.add(germplasm.getGpid2());
					List<Object> itemIdsInMap = gidToItemIdMap.get(germplasm.getGpid2());
					if (itemIdsInMap == null) {
						itemIdsInMap = new ArrayList<>();
						itemIdsInMap.add(itemId);
						gidToItemIdMap.put(germplasm.getGpid2(), itemIdsInMap);
					} else {
						itemIdsInMap.add(itemId);
					}
				} else {
					this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), "-");
				}
			} else {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), "-");
			}
		}

		if (!gidsToUseForQuery.isEmpty()) {
			final Map<Integer, String> gidToNameMap = this.germplasmDataManager.getPreferredNamesByGids(gidsToUseForQuery);

			for (final Integer gid : gidToNameMap.keySet()) {
				final String prefName = gidToNameMap.get(gid);
				final List<Object> itemIdsInMap = gidToItemIdMap.get(gid);
				for (final Object itemId : itemIdsInMap) {
					this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), prefName);
				}
			}
		}

		this.fillColumnSource.propagateUIChanges();
	}

	public void setCrossFemaleInfoColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		ImmutableMap<Integer, Germplasm> germplasmMap = this.retrieveGermplasmAndGenerateMap(gids);
		
		for (final Object itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			final Germplasm germplasm = germplasmMap.get(gid);
			Germplasm femaleParent = null;
			// get female only if germplasm is created via generative process
			if (germplasm.getGnpgs() >= 2) {
				femaleParent = this.germplasmDataManager.getGermplasmByGID(germplasm.getGpid1());
			}

			if (femaleParent == null) {
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, "-");
			} else {
				String value = "-";
				if (columnName.equals(ColumnLabels.CROSS_FEMALE_GID.getName())) {
					value = femaleParent.getGid().toString();
				} else if (columnName.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
					// TODO See if this Middleware query can be done one-off
					final Name prefName = this.germplasmDataManager.getPreferredNameByGID(femaleParent.getGid());
					if (prefName != null) {
						value = prefName.getNval();
					}
				}
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, value);
			}
		}

		this.fillColumnSource.propagateUIChanges();
	}


}

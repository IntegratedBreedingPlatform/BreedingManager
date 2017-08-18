
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

@Configurable
public class GermplasmColumnValuesGenerator {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private FillColumnSource fillColumnSource;
	
	public GermplasmColumnValuesGenerator(final FillColumnSource fillColumnSource){
		this.fillColumnSource = fillColumnSource;
	}

	public void setPreferredIdColumnValues() {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		for (final Integer itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			String preferredID = "";
			// TODO Optimize in one-off Middleware query for all GIDs
			final Name name = this.germplasmDataManager.getPreferredIdByGID(gid);
			if (name != null && name.getNval() != null) {
				preferredID = name.getNval();
			}
			this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.PREFERRED_ID.getName(), preferredID);
		}

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();
	}

	public void setPreferredNameColumnValues() {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, String> gidPreferredNamesMap = this.germplasmDataManager.getPreferredNamesByGids(gids);
		for (final Integer itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			String preferredName = "";
			if (gidPreferredNamesMap.get(gid) != null) {
				preferredName = gidPreferredNamesMap.get(gid);
			}
			this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.PREFERRED_NAME.getName(), preferredName);
		}

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();

	}

	public void setGermplasmDateColumnValues() {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, Integer> germplasmGidDateMap = this.germplasmDataManager.getGermplasmDatesByGids(gids);
		for (final Integer itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);

			if (germplasmGidDateMap.get(gid) == null) {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_DATE.getName(), "");
			} else {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_DATE.getName(), germplasmGidDateMap.get(gid));
			}
		}

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();
	}

	public void setLocationNameColumnValues() {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, String> locationNamesMap = this.germplasmDataManager.getLocationNamesByGids(gids);

		for (final Integer itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			if (locationNamesMap.get(gid) == null) {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_LOCATION.getName(), "");
			} else {
				this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_LOCATION.getName(), locationNamesMap.get(gid));
			}
		}

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();
	}

	public void setMethodInfoColumnValues(final String columnName) {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final Map<Integer, Object> methodsMap = this.germplasmDataManager.getMethodsByGids(gids);

		for (final Integer itemId : itemIds) {
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

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();
	}

	public void setCrossMaleGIDColumnValues() {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final List<Germplasm> germplasmMap = this.germplasmDataManager.getGermplasms(gids);
		for (final Integer itemId : itemIds) {
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

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();
	}

	public void setCrossMalePrefNameColumnValues() {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final List<Germplasm> germplasmMap = this.germplasmDataManager.getGermplasms(gids);

		final Map<Integer, List<Integer>> gidToItemIdMap = new HashMap<>();
		final List<Integer> gidsToUseForQuery = new ArrayList<>();

		for (final Integer itemId : itemIds) {
			final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
			final Germplasm germplasm = germplasmMap.get(gid);

			if (germplasm != null) {
				if (germplasm.getGnpgs() >= 2 && germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
					gidsToUseForQuery.add(germplasm.getGpid2());
					List<Integer> itemIdsInMap = gidToItemIdMap.get(germplasm.getGpid2());
					if (itemIdsInMap == null) {
						itemIdsInMap = new ArrayList<Integer>();
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
				final List<Integer> itemIdsInMap = gidToItemIdMap.get(gid);
				for (final Integer itemId : itemIdsInMap) {
					this.fillColumnSource.setColumnValueForItem(itemId, ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), prefName);
				}
			}
		}

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();
	}

	public void setCrossFemaleInfoColumnValues(final String columnName) {
		final List<Integer> itemIds = this.fillColumnSource.getItemIdsToProcess();
		final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
		final List<Germplasm> germplasmMap = this.germplasmDataManager.getGermplasms(gids);
		
		for (final Integer itemId : itemIds) {
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

		this.fillColumnSource.resetEditableTable();
		this.fillColumnSource.setUnsavedChanges();
	}


}

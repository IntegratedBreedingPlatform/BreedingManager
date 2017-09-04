
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

@Configurable
public class GermplasmColumnValuesGenerator {

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private PedigreeService pedigreeService;
	
	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	private FillColumnSource fillColumnSource;
	
	public GermplasmColumnValuesGenerator(final FillColumnSource fillColumnSource){
		this.fillColumnSource = fillColumnSource;
	}

	public void setPreferredIdColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			final Map<Integer, String> gidPreferredIDsMap = this.germplasmDataManager.getPreferredIdsByGIDs(gids);
			
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				String preferredID = "";
				if (gidPreferredIDsMap.get(gid) != null) {
					preferredID = gidPreferredIDsMap.get(gid);
				}
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, preferredID);
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
	}

	public void setPreferredNameColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			final Map<Integer, String> gidPreferredNamesMap = this.germplasmDataManager.getPreferredNamesByGids(gids);
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				String preferredName = "";
				if (gidPreferredNamesMap.get(gid) != null) {
					preferredName = gidPreferredNamesMap.get(gid);
				}
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, preferredName);
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
	}

	public void setGermplasmDateColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			final Map<Integer, Integer> germplasmGidDateMap = this.germplasmDataManager.getGermplasmDatesByGids(gids);
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				
				if (germplasmGidDateMap.get(gid) == null) {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, "");
				} else {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, germplasmGidDateMap.get(gid));
				}
			}
			this.fillColumnSource.propagateUIChanges();
		}

	}

	public void setLocationNameColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			final Map<Integer, String> locationNamesMap = this.germplasmDataManager.getLocationNamesByGids(gids);
			
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				if (locationNamesMap.get(gid) == null) {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, "");
				} else {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, locationNamesMap.get(gid));
				}
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
	}

	public void setMethodInfoColumnValues(final String columnName, final FillWithOption option) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			final Map<Integer, Object> methodsMap = this.germplasmDataManager.getMethodsByGids(gids);
			
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				
				if (methodsMap.get(gid) == null) {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, "");
				} else {
					String value = "";
					
					if (FillWithOption.FILL_WITH_BREEDING_METHOD_NAME.equals(option)) {
						value = ((Method) methodsMap.get(gid)).getMname();
					} else if (FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.equals(option)) {
						value = ((Method) methodsMap.get(gid)).getMcode();
					} else if (FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.equals(option)) {
						value = ((Method) methodsMap.get(gid)).getMid().toString();
					} else if (FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.equals(option)) {
						value = ((Method) methodsMap.get(gid)).getMgrp();
					}
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, value);
				}
			}
			this.fillColumnSource.propagateUIChanges();
		}

	}

	public void setCrossMaleGIDColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			ImmutableMap<Integer, Germplasm> germplasmMap = this.retrieveGermplasmAndGenerateMap(gids);
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				final Germplasm germplasm = germplasmMap.get(gid);
				
				if (germplasm != null && germplasm.getGnpgs() >= 2 && germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, germplasm.getGpid2().toString());
				} else {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, "-");
				}
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
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

	public void setCrossMalePrefNameColumnValues(final String columnName) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			ImmutableMap<Integer, Germplasm> germplasmMap = this.retrieveGermplasmAndGenerateMap(gids);
			
			final Map<Integer, List<Object>> gidToItemIdMap = new HashMap<>();
			final List<Integer> gidsToUseForQuery = new ArrayList<>();
			
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				final Germplasm germplasm = germplasmMap.get(gid);
				
				if (germplasm != null && germplasm.getGnpgs() >= 2 && germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
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
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, "-");
				}
			}
			
			if (!gidsToUseForQuery.isEmpty()) {
				final Map<Integer, String> gidToNameMap = this.germplasmDataManager.getPreferredNamesByGids(gidsToUseForQuery);
				
				for (final Integer gid : gidToNameMap.keySet()) {
					final String prefName = gidToNameMap.get(gid);
					final List<Object> itemIdsInMap = gidToItemIdMap.get(gid);
					for (final Object itemId : itemIdsInMap) {
						this.fillColumnSource.setColumnValueForItem(itemId, columnName, prefName);
					}
				}
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
	}

	public void setCrossFemaleInfoColumnValues(final String columnName, final FillWithOption option) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			ImmutableMap<Integer, Germplasm> germplasmMap = this.retrieveGermplasmAndGenerateMap(gids);
			final Map<Integer, List<Object>> gidToItemIdMap = new HashMap<>();
			final List<Integer> gidsToUseForQuery = new ArrayList<>();
			
			for (final Object itemId : itemIds) {
				final Integer gid = this.fillColumnSource.getGidForItemId(itemId);
				final Germplasm germplasm = germplasmMap.get(gid);
				// get female only if germplasm is created via generative process
				final Integer femaleParentId = germplasm.getGpid1();
				if (germplasm.getGnpgs() >= 2 && femaleParentId != null && femaleParentId != 0) {
					String value = "-";
					if (FillWithOption.FILL_WITH_CROSS_FEMALE_GID.equals(option)) {
						value = femaleParentId.toString();
					} else if (FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.equals(option)) {
						gidsToUseForQuery.add(femaleParentId);
						List<Object> itemIdsInMap = gidToItemIdMap.get(femaleParentId);
						if (itemIdsInMap == null) {
							itemIdsInMap = new ArrayList<>();
							itemIdsInMap.add(itemId);
							gidToItemIdMap.put(femaleParentId, itemIdsInMap);
						} else {
							itemIdsInMap.add(itemId);
						}
					}
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, value);
				} else {
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, "-");
				}
			}
			
			if (!gidsToUseForQuery.isEmpty()) {
				final Map<Integer, String> gidToNameMap = this.germplasmDataManager.getPreferredNamesByGids(gidsToUseForQuery);
				
				for (final Integer gid : gidToNameMap.keySet()) {
					final String prefName = gidToNameMap.get(gid);
					final List<Object> itemIdsInMap = gidToItemIdMap.get(gid);
					for (final Object itemId : itemIdsInMap) {
						this.fillColumnSource.setColumnValueForItem(itemId, columnName, prefName);
					}
				}
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
	}
	
	public void fillWithEmpty(final String columnName) {
		List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) {
			for (final Object itemId : itemIds) {
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, "");
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
	}
	
	public void fillWithAttribute(final Integer attributeType, final String columnName) {
		if (attributeType != null) {
			final List<Integer> gids = this.fillColumnSource.getGidsToProcess();
			if (!gids.isEmpty()) {
				Map<Integer, String> gidAttributeMap = this.germplasmDataManager.getAttributeValuesByTypeAndGIDList(attributeType, gids);
				
				List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
				for (final Object itemId : itemIds) {
					Integer gid = this.fillColumnSource.getGidForItemId(itemId);
					this.fillColumnSource.setColumnValueForItem(itemId, columnName, gidAttributeMap.get(gid));
				}
				this.fillColumnSource.propagateUIChanges();
			}
		}

	}
	
	public void fillWithSequence(final String columnName, final String prefix, final String suffix, final int startNumber, final int numOfZeros,
			final boolean spaceBetweenPrefixAndCode, final boolean spaceBetweenSuffixAndCode) {
		final List<Object> itemIds = this.fillColumnSource.getItemIdsToProcess();
		if (!itemIds.isEmpty()) { 
			int number = startNumber;
			for (final Object itemId : itemIds) {
				StringBuilder builder = new StringBuilder();
				builder.append(prefix);
				if (spaceBetweenPrefixAndCode) {
					builder.append(" ");
				}
				
				if (numOfZeros > 0) {
					String numberString = "" + number;
					int numOfZerosNeeded = numOfZeros - numberString.length();
					for (int i = 0; i < numOfZerosNeeded; i++) {
						builder.append("0");
					}
				}
				builder.append(number);
				
				if (suffix != null && spaceBetweenSuffixAndCode) {
					builder.append(" ");
				}
				
				if (suffix != null) {
					builder.append(suffix);
				}
				this.fillColumnSource.setColumnValueForItem(itemId, columnName, builder.toString());
				++number;
			}
			
			this.fillColumnSource.propagateUIChanges();
		}
	}
	
	public void fillWithCrossExpansion(final Integer crossExpansionLevel, final String columnName) {
		if (crossExpansionLevel != null) {
			
			final Map<Integer, String> crossExpansions = bulkGeneratePedigreeString(crossExpansionLevel);

			for (Iterator<Object> i = this.fillColumnSource.getItemIdsToProcess().iterator(); i.hasNext();) {
				// iterate through the table elements' IDs
				final Integer listDataId = (Integer) i.next();
				final Integer gid = this.fillColumnSource.getGidForItemId(listDataId);
				String crossExpansion = crossExpansions.get(gid);
				this.fillColumnSource.setColumnValueForItem(listDataId, columnName, crossExpansion);
			}

			this.fillColumnSource.propagateUIChanges();

		}
	}

	private Map<Integer, String> bulkGeneratePedigreeString(final Integer crossExpansionLevel) {
		
		final Set<Integer> gidIdList = new HashSet<>(this.fillColumnSource.getGidsToProcess());
		final Iterable<List<Integer>> partition = Iterables.partition(gidIdList, 5000);

		final Map<Integer, String> crossExpansions = new HashMap<>();

		for (List<Integer> partitionedGidList : partition) {
			final Set<Integer> partitionedGidSet = new HashSet<Integer>(partitionedGidList);
			crossExpansions.putAll(this.pedigreeService.getCrossExpansions(partitionedGidSet, crossExpansionLevel.intValue(),
					this.crossExpansionProperties));
		}
		return crossExpansions;
	}

	
	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	
	public void setPedigreeService(PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}


}

/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.crossingmanager.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Creates Germplasm, GermplasmList, GermplasmListData records for crosses defined. Adds a ProjectActivity (Workbench) record for the save
 * action.
 * 
 * @author Darla Ani
 */
@Configurable
public class SaveCrossesMadeAction implements Serializable {

	private static final int PREFERRED_NAME = 1;

	private static final int PEDIGREE_NAME_TYPE = 18;

	public static final Integer GERMPLASM_LIST_STATUS = 1;

	public static final Integer GERMPLASM_GNPGS = 2;
	public static final Integer GERMPLASM_GRPLCE = 0;
	public static final Integer GERMPLASM_LGID = 0;
	public static final Integer GERMPLASM_MGID = 0;
	public static final Integer GERMPLASM_REFID = 0;

	public static final Integer NAME_REFID = 0;

	public static final String LIST_DATA_SOURCE = "Crossing Manager Tool";
	public static final Integer LIST_DATA_STATUS = 0;
	public static final Integer LIST_DATA_LRECID = 0;

	public static final String WB_ACTIVITY_NAME = "Created a list of crosses";
	public static final String WB_ACTIVITY_DESCRIPTION = "List cross id = ";

	private static final long serialVersionUID = -6273933938066390358L;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmManager;

	@Autowired
	private ContextUtil contextUtil;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	private GermplasmList germplasmList;
	private List<GermplasmListData> existingListEntries = new ArrayList<GermplasmListData>();
	private List<Germplasm> existingGermplasms = new ArrayList<Germplasm>();
	private final Map<Germplasm, GermplasmListData> germplasmToListDataMap = new LinkedHashMap<Germplasm, GermplasmListData>();

	private final List<Integer> indicesOfAddedCrosses = new ArrayList<Integer>();
	private final List<Integer> indicesOfRetainedCrosses = new ArrayList<Integer>();

	public SaveCrossesMadeAction(final GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	public SaveCrossesMadeAction() {
		super();
	}

	/**
	 * Saves records in Germplasm, GermplasmList and GermplasmListData, ProjectActivity (Workbench).
	 * 
	 * @param crossesMade where crosses information is defined
	 * @return id of new Germplasm List created
	 */
	public GermplasmList saveRecords(final CrossesMade crossesMade, final boolean applyNewGroupToCurrentCrossOnly) {
		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		return transactionTemplate.execute(new TransactionCallback<GermplasmList>() {

			@Override
			public GermplasmList doInTransaction(final TransactionStatus transactionStatus) {
				SaveCrossesMadeAction.this.updateConstantFields(crossesMade);

				final List<Integer> germplasmIDs = SaveCrossesMadeAction.this.saveGermplasmsAndNames(crossesMade);

				SaveCrossesMadeAction.this.germplasmGroupingService.processGroupInheritanceForCrosses(germplasmIDs,
						!applyNewGroupToCurrentCrossOnly, SaveCrossesMadeAction.this.crossExpansionProperties.getHybridBreedingMethods());

				if (crossesMade.getSetting().getCrossNameSetting().isSaveParentageDesignationAsAString()) {
					SaveCrossesMadeAction.this.savePedigreeDesignationName(crossesMade, germplasmIDs);
				}

				final GermplasmList list = SaveCrossesMadeAction.this.saveGermplasmListRecord(crossesMade);
				SaveCrossesMadeAction.this.saveGermplasmListDataRecords(crossesMade, germplasmIDs, list);

				// log project activity in Workbench
				if (SaveCrossesMadeAction.this.germplasmList == null) {
					SaveCrossesMadeAction.this.contextUtil.logProgramActivity(SaveCrossesMadeAction.WB_ACTIVITY_NAME,
							SaveCrossesMadeAction.WB_ACTIVITY_DESCRIPTION + list.getId());
				}

				return list;
			}
		});

	}

	// Here is where crossed germplasm is saved.
	List<Integer> saveGermplasmsAndNames(final CrossesMade crossesMade) {
		List<Integer> germplasmIDs = new ArrayList<Integer>();

		final Map<Germplasm, Name> currentCrossesMap = crossesMade.getCrossesMap();
		Map<Germplasm, Name> crossesToInsert = new LinkedHashMap<Germplasm, Name>();
		if (this.germplasmList == null) {
			crossesToInsert = currentCrossesMap;

			// when updating a list, determine which germplasms to insert
		} else {
			final GenerateCrossNameAction generateAction = new GenerateCrossNameAction();
			final CrossingManagerSetting setting = crossesMade.getSetting();
			int nextNumberInSequence = generateAction.getNextNumberInSequence(setting.getCrossNameSetting());

			this.retrieveGermplasmsOfList();
			int ctr = 0; // counter for index of added germplasms
			for (final Germplasm currentGplasm : currentCrossesMap.keySet()) {
				boolean existsAlready = false;
				int index = 0; // counter for removed listdata record(s)
				for (final Germplasm existingGplasm : this.existingGermplasms) {
					if (this.haveSameParents(currentGplasm, existingGplasm)) {
						existsAlready = true;
						this.indicesOfRetainedCrosses.add(index);
						break;
					}
					index++;
				}
				if (!existsAlready) {
					/*
					 * Regenerate name in the case entries are appended in the end and names were generated for existing listdata records
					 */
					final Name name = currentCrossesMap.get(currentGplasm);
					if (setting != null) {
						name.setNval(generateAction.buildNextNameInSequence(nextNumberInSequence++));
					}

					crossesToInsert.put(currentGplasm, name);
					this.indicesOfAddedCrosses.add(ctr); // keep track of index of new crosses
				}
				ctr++;
			}
		}

		if (!crossesToInsert.isEmpty()) {
			germplasmIDs = this.germplasmManager.addGermplasm(crossesToInsert);
		}
		return germplasmIDs;
	}

	private void retrieveGermplasmsOfList() {
		this.germplasmToListDataMap.clear();

		final List<GermplasmListData> allExistingEntries =
				this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId());

		// Add only non deleted list data
		this.existingListEntries = new ArrayList<GermplasmListData>();

		for (final GermplasmListData germplasmListData : allExistingEntries) {
			if (germplasmListData.getStatus() != 9) {
				this.existingListEntries.add(germplasmListData);
			}
		}

		final List<Integer> gids = new ArrayList<Integer>();
		for (final GermplasmListData entry : this.existingListEntries) {
			gids.add(entry.getGid());
		}
		this.existingGermplasms = this.germplasmManager.getGermplasms(gids);

		for (final Germplasm germplasm : this.existingGermplasms) {
			for (final GermplasmListData entry : this.existingListEntries) {
				if (entry.getGid().equals(germplasm.getGid())) {
					this.germplasmToListDataMap.put(germplasm, entry);
				}
			}
		}
	}

	private boolean haveSameParents(final Germplasm g1, final Germplasm g2) {
		return g1.getGpid1().equals(g2.getGpid1()) && g1.getGpid2().equals(g2.getGpid2());
	}

	GermplasmList saveGermplasmListRecord(final CrossesMade crossesMade) {
		int listId;
		final GermplasmList listToSave = crossesMade.getGermplasmList();
		listToSave.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
		if (this.germplasmList == null) {
			listToSave.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
			listId = this.germplasmListManager.addGermplasmList(listToSave);
		} else {
			// GCP-8225 : set the updates manually on List object so that list entries are not deleted
			this.germplasmList = this.germplasmListManager.getGermplasmListById(this.germplasmList.getId());

			this.germplasmList.setName(listToSave.getName());
			this.germplasmList.setDescription(listToSave.getDescription());
			this.germplasmList.setType(listToSave.getType());
			this.germplasmList.setDate(listToSave.getDate());
			this.germplasmList.setNotes(listToSave.getNotes());
			this.germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

			listId = this.germplasmListManager.updateGermplasmList(this.germplasmList);
		}

		final GermplasmList list = this.germplasmListManager.getGermplasmListById(listId);
		return list;
	}

	void saveGermplasmListDataRecords(final CrossesMade crossesMade, final List<Integer> germplasmIDs, final GermplasmList list) {

		this.deleteRemovedListData(crossesMade);
		this.addNewGermplasmListData(crossesMade, germplasmIDs, list);
	}

	private void deleteRemovedListData(final CrossesMade crossesMade) {
		final List<GermplasmListData> retainedCrosses = new ArrayList<GermplasmListData>();
		for (int i = 0; i < this.existingGermplasms.size(); i++) {
			final Germplasm existingGermplasm = this.existingGermplasms.get(i);
			for (final Germplasm currentGermplasm : crossesMade.getCrossesMap().keySet()) {
				if (this.haveSameParents(currentGermplasm, existingGermplasm)) {
					final GermplasmListData germplasmListData = this.germplasmToListDataMap.get(existingGermplasm);
					retainedCrosses.add(germplasmListData);
					break;
				}
			}
		}

		final List<GermplasmListData> listToDelete = new ArrayList<GermplasmListData>(this.existingListEntries);
		listToDelete.removeAll(retainedCrosses);

		if (!listToDelete.isEmpty()) {
			this.germplasmListManager.deleteGermplasmListData(listToDelete);
		}

		// Update "exsitingListEntries", this is used to assign the entry id
		this.existingListEntries = new ArrayList<GermplasmListData>();

		final List<GermplasmListData> allExistingEntries = new ArrayList<GermplasmListData>();

		if (this.germplasmList != null) {
			allExistingEntries.addAll(this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId()));
			Integer entryId = 1;
			for (final GermplasmListData germplasmListData : allExistingEntries) {
				if (germplasmListData.getStatus() != 9) {
					germplasmListData.setEntryId(entryId);
					this.existingListEntries.add(germplasmListData);
					entryId++;
				}
			}
		}
		this.germplasmListManager.updateGermplasmListData(this.existingListEntries);
	}

	private void addNewGermplasmListData(final CrossesMade crossesMade, final List<Integer> germplasmIDs, final GermplasmList list) {
		final Iterator<Integer> germplasmIdIterator = germplasmIDs.iterator();
		final List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
		int ctr = 0;

		int entryId = this.existingListEntries.size() + 1;
		for (final Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()) {
			if (this.germplasmList == null || this.indicesOfAddedCrosses.contains(ctr)) {
				final Integer gid = germplasmIdIterator.next();
				final String designation = entry.getValue().getNval();
				final String groupName = this.getFemaleMaleCrossName(crossesMade, designation, ctr);

				final GermplasmListData germplasmListData = this.buildGermplasmListData(list, gid, entryId, designation, groupName);

				listToSave.add(germplasmListData);
				entryId++;

			}
			ctr++;
		}

		if (!listToSave.isEmpty()) {
			this.germplasmListManager.addGermplasmListData(listToSave);
		}
	}

	void savePedigreeDesignationName(final CrossesMade crossesMade, final List<Integer> germplasmIDs) {

		final List<Name> parentageDesignationNames = new ArrayList<Name>();
		final Iterator<Integer> germplasmIdIterator = germplasmIDs.iterator();
		int ctr = 0;
		for (final Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()) {
			if (this.germplasmList == null || this.indicesOfAddedCrosses.contains(ctr)) {

				final Integer gid = germplasmIdIterator.next();
				final String designation = entry.getValue().getNval();
				final String parentageDesignation = this.getParentageDesignation(crossesMade, ctr, designation);
				final Integer locationId = crossesMade.getSetting().getAdditionalDetailsSetting().getHarvestLocationId();

				final Name parentageDesignationName = new Name();
				parentageDesignationName.setGermplasmId(gid);
				parentageDesignationName.setTypeId(SaveCrossesMadeAction.PEDIGREE_NAME_TYPE);
				parentageDesignationName.setUserId(this.contextUtil.getCurrentUserLocalId());
				parentageDesignationName.setNval(parentageDesignation);
				parentageDesignationName.setNstat(SaveCrossesMadeAction.PREFERRED_NAME);
				parentageDesignationName.setLocationId(locationId);
				parentageDesignationName.setNdate(Util.getCurrentDateAsIntegerValue());
				parentageDesignationName.setReferenceId(0);

				parentageDesignationNames.add(parentageDesignationName);
			}
			ctr++;
		}

		this.germplasmManager.addGermplasmName(parentageDesignationNames);
	}

	private String getParentageDesignation(final CrossesMade crossesMade, final int ctr, final String designation) {
		final String groupNameSplit[] = this.getFemaleMaleCrossName(crossesMade, designation, ctr).split(",");
		final String parentageDesignation = groupNameSplit[0];
		return parentageDesignation;
	}

	/*
	 * If current names were generated using prefix in tool, retrieve the <female parent>/<male parent> cross names from oldCrossNames in
	 * CrossesMade
	 */
	private String getFemaleMaleCrossName(final CrossesMade crossesMade, final String designation, final Integer ctr) {

		final List<GermplasmListEntry> oldCrossNames = crossesMade.getOldCrossNames();
		if (oldCrossNames != null) {
			return oldCrossNames.get(ctr).getDesignation();
		}
		return designation;
	}

	private GermplasmListData buildGermplasmListData(final GermplasmList list, final Integer gid, final int entryId,
			final String designation, final String groupName) {

		final String groupNameSplit[] = groupName.split(",");
		final String grpName = groupNameSplit[0];
		final String seedSource = groupNameSplit[1];

		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setList(list);
		germplasmListData.setGid(gid);
		germplasmListData.setEntryId(entryId);
		germplasmListData.setEntryCode(String.valueOf(entryId));
		germplasmListData.setSeedSource(seedSource);
		germplasmListData.setDesignation(designation);
		germplasmListData.setStatus(SaveCrossesMadeAction.LIST_DATA_STATUS);
		germplasmListData.setGroupName(grpName);
		germplasmListData.setLocalRecordId(SaveCrossesMadeAction.LIST_DATA_LRECID);

		return germplasmListData;
	}

	private void updateConstantFields(final CrossesMade crossesMade) {
		final Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();

		for (final Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()) {
			final Germplasm g = entry.getKey();
			g.setGnpgs(SaveCrossesMadeAction.GERMPLASM_GNPGS);
			g.setGrplce(SaveCrossesMadeAction.GERMPLASM_GRPLCE);
			g.setLgid(SaveCrossesMadeAction.GERMPLASM_LGID);
			g.setMgid(SaveCrossesMadeAction.GERMPLASM_MGID);
			g.setUserId(ibdbUserId);
			g.setReferenceId(SaveCrossesMadeAction.GERMPLASM_REFID);

			final Name n = entry.getValue();
			n.setReferenceId(SaveCrossesMadeAction.NAME_REFID);
			n.setUserId(ibdbUserId);
		}

		final GermplasmList list = crossesMade.getGermplasmList();
		if (list != null) {
			list.setStatus(SaveCrossesMadeAction.GERMPLASM_LIST_STATUS);
			list.setUserId(ibdbUserId);
		}

	}

	public void updateSeedSource(final Collection<CrossParents> crossParents) {
		this.retrieveGermplasmsOfList();
		for (final CrossParents parents : crossParents) {
			final Germplasm currentGermplasm = new Germplasm();
			currentGermplasm.setGpid1(parents.getFemaleParent().getGid());
			currentGermplasm.setGpid2(parents.getMaleParent().getGid());

			for (final Germplasm existingGermplasm : this.existingGermplasms) {
				if (this.haveSameParents(currentGermplasm, existingGermplasm)) {
					final GermplasmListData germplasmListData = this.germplasmToListDataMap.get(existingGermplasm);
					if (germplasmListData != null) {
						germplasmListData.setSeedSource(parents.getSeedSource());
					}
				}
			}
		}
		this.germplasmListManager.updateGermplasmListData(this.existingListEntries);
	}

	/**
	 * For Test Only
	 * 
	 * @param contextUtil
	 */
	void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	/**
	 * For Test Only
	 * 
	 * @param germplasmManager
	 */
	void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	protected void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	void setGermplasmGroupingService(final GermplasmGroupingService germplasmGroupingService) {
		this.germplasmGroupingService = germplasmGroupingService;
	}

	void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}
}

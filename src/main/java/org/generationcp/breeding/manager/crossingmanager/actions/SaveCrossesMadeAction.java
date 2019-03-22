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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.CollectionTransformationUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.Progenitor;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Creates Germplasm, GermplasmList, GermplasmListData records for crosses defined. Adds a ProjectActivity (Workbench) record for the save
 * action.
 *
 * @author Darla Ani
 */
@Configurable
public class SaveCrossesMadeAction implements Serializable {

	// Save temp list as deleted
	// TODO Refactor liststatus to bit array so a list can have multiple status
	private static final Integer GERMPLASM_LIST_STATUS = 9;

	private static final Integer GERMPLASM_GNPGS = 2;
	private static final Integer GERMPLASM_GRPLCE = 0;
	private static final Integer GERMPLASM_LGID = 0;
	private static final Integer GERMPLASM_MGID = 0;
	private static final Integer GERMPLASM_REFID = 0;

	private static final Integer NAME_REFID = 0;

	static final Integer LIST_DATA_STATUS = 0;
	static final Integer LIST_DATA_LRECID = 0;

	private static final String WB_ACTIVITY_NAME = "Created a list of crosses";
	private static final String WB_ACTIVITY_DESCRIPTION = "List cross id = ";

	private static final long serialVersionUID = -6273933938066390358L;
	static final int SEEDSOURCE_CHARACTER_LIMIT = 255;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmManager;

	@Autowired
	private ContextUtil contextUtil;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private PedigreeService pedigreeService;

	private GermplasmList germplasmList;

	public SaveCrossesMadeAction(final GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	SaveCrossesMadeAction() {
		super();
	}

	/**
	 * Saves records in Germplasm, GermplasmList and GermplasmListData, ProjectActivity (Workbench).
	 *
	 * @param crossesMade where crosses information is defined
	 * @return id of new Germplasm List created
	 */
	public GermplasmList saveRecords(final CrossesMade crossesMade) {
		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		return transactionTemplate.execute(new TransactionCallback<GermplasmList>() {

			@Override
			public GermplasmList doInTransaction(final TransactionStatus transactionStatus) {
				SaveCrossesMadeAction.this.updateConstantFields(crossesMade);

				final List<Integer> germplasmIDs = SaveCrossesMadeAction.this.saveGermplasmsAndNames(crossesMade);

				final GermplasmList list = SaveCrossesMadeAction.this.saveGermplasmListRecord(crossesMade);
				SaveCrossesMadeAction.this.addNewGermplasmListData(crossesMade, germplasmIDs, list);

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
		List<Integer> germplasmIDs = new ArrayList<>();

		final List<Triple<Germplasm, Name, List<Progenitor>>> currentCrossesList = crossesMade.getCrossesList();
		List<Triple<Germplasm, Name, List<Progenitor>>> crossesToInsert = new ArrayList<>();
		if (this.germplasmList == null) {
			crossesToInsert = currentCrossesList;
		}

		if (!crossesToInsert.isEmpty()) {
			germplasmIDs = this.germplasmManager.addGermplasm(crossesToInsert);
		}
		return germplasmIDs;
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

		return this.germplasmListManager.getGermplasmListById(listId);
	}

	private Map<Integer, String> updateWithActualPedigree(final Set<Germplasm> gids) {
		final ImmutableSet<Integer> allGidsFromGermplasmListDataList = CollectionTransformationUtil.getAllGidsFromGermplasmList(gids);

		final Iterable<List<Integer>> partition = Iterables.partition(allGidsFromGermplasmListDataList, 5000);
		final Map<Integer, String> resultMap = new HashMap<>();
		for (final List<Integer> partitionedList : partition) {
			resultMap.putAll(
					this.pedigreeService.getCrossExpansions(new HashSet<Integer>(partitionedList), null, this.crossExpansionProperties));
		}
		return resultMap;
	}

	private void addNewGermplasmListData(final CrossesMade crossesMade, final List<Integer> germplasmIDs, final GermplasmList list) {
		final Iterator<Integer> germplasmIdIterator = germplasmIDs.iterator();
		final List<GermplasmListData> listToSave = new ArrayList<>();

		int ctr = 0;
		int entryId = 1;

		final List<Triple<Germplasm, Name, List<Progenitor>>> crossesList = crossesMade.getCrossesList();
		final Set<Germplasm> germplasm = new LinkedHashSet<>();
		final Set<Name> names = new LinkedHashSet<>();
		for(final Triple<Germplasm, Name, List<Progenitor>> triple: crossesList) {
			germplasm.add(triple.getLeft());
			names.add(triple.getMiddle());
		}
		final Map<Integer, String> pedigreeMap = this.updateWithActualPedigree(germplasm);

		for (final Name name : names) {
			if (this.germplasmList == null) {
				final Integer gid = germplasmIdIterator.next();
				final String designation = name.getNval();
				final String seedsource = this.getFemaleMaleCrossName(crossesMade, designation, ctr);

				final GermplasmListData germplasmListData =
						this.buildGermplasmListData(list, gid, entryId, designation, seedsource, pedigreeMap);

				listToSave.add(germplasmListData);
				entryId++;

			}
			ctr++;
		}

		if (!listToSave.isEmpty()) {
			this.germplasmListManager.addGermplasmListData(listToSave);
		}
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

	GermplasmListData buildGermplasmListData(final GermplasmList list, final Integer gid, final int entryId,
			final String designation, String seedSource, final Map<Integer, String> pedigreeMap) {
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setList(list);
		germplasmListData.setGid(gid);
		germplasmListData.setEntryId(entryId);
		germplasmListData.setEntryCode(String.valueOf(entryId));
		if(seedSource.length() > SEEDSOURCE_CHARACTER_LIMIT) {
			seedSource = seedSource.substring(0, SEEDSOURCE_CHARACTER_LIMIT);
		}
		germplasmListData.setSeedSource(seedSource);
		germplasmListData.setDesignation(designation);
		germplasmListData.setStatus(SaveCrossesMadeAction.LIST_DATA_STATUS);
		germplasmListData.setGroupName(pedigreeMap.get(gid));
		germplasmListData.setLocalRecordId(SaveCrossesMadeAction.LIST_DATA_LRECID);

		return germplasmListData;
	}

	private void updateConstantFields(final CrossesMade crossesMade) {
		final Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();

		for (final Triple<Germplasm, Name, List<Progenitor>> triple : crossesMade.getCrossesList()) {
			final Germplasm g = triple.getLeft();
			g.setGnpgs(SaveCrossesMadeAction.GERMPLASM_GNPGS);
			g.setGrplce(SaveCrossesMadeAction.GERMPLASM_GRPLCE);
			g.setLgid(SaveCrossesMadeAction.GERMPLASM_LGID);
			g.setMgid(SaveCrossesMadeAction.GERMPLASM_MGID);
			g.setUserId(ibdbUserId);
			g.setReferenceId(SaveCrossesMadeAction.GERMPLASM_REFID);

			final Name n = triple.getMiddle();
			n.setReferenceId(SaveCrossesMadeAction.NAME_REFID);
			n.setUserId(ibdbUserId);
		}

		final GermplasmList list = crossesMade.getGermplasmList();
		if (list != null) {
			list.setStatus(SaveCrossesMadeAction.GERMPLASM_LIST_STATUS);
			list.setUserId(ibdbUserId);
		}

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
	 * @param germplasmListManager
	 */
	void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	protected void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}
}

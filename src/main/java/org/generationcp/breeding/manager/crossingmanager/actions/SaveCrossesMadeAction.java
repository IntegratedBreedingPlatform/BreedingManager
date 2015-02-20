/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package org.generationcp.breeding.manager.crossingmanager.actions;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;

/**
 * Creates Germplasm, GermplasmList, GermplasmListData records for crosses defined.
 * Adds a ProjectActivity (Workbench) record for the save action.
 *
 * @author Darla Ani
 */
@Configurable
public class SaveCrossesMadeAction implements Serializable {

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

	@Resource
	private ContextUtil contextUtil;

	private GermplasmList germplasmList;
	private List<GermplasmListData> existingListEntries = new ArrayList<GermplasmListData>();
	private List<Germplasm> existingGermplasms = new ArrayList<Germplasm>();
	private Map<Germplasm, GermplasmListData> germplasmToListDataMap = new LinkedHashMap<Germplasm, GermplasmListData>();

	private List<Integer> indicesOfAddedCrosses = new ArrayList<Integer>();
	private List<Integer> indicesOfRetainedCrosses = new ArrayList<Integer>();

	public SaveCrossesMadeAction(GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	public SaveCrossesMadeAction() {
		super();
	}

	/**
	 * Saves records in Germplasm, GermplasmList and GermplasmListData,
	 * ProjectActivity (Workbench).
	 *
	 * @param crossesMade where crosses information is defined
	 * @return id of new Germplasm List created
	 * @throws MiddlewareQueryException
	 */
	public GermplasmList saveRecords(CrossesMade crossesMade) throws MiddlewareQueryException {
		updateConstantFields(crossesMade);

		List<Integer> germplasmIDs = saveGermplasmsAndNames(crossesMade);

		GermplasmList list = saveGermplasmListRecord(crossesMade);
		saveGermplasmListDataRecords(crossesMade, germplasmIDs, list);

		// log project activity in Workbench
		if (germplasmList == null) {
			contextUtil
					.logProgramActivity(WB_ACTIVITY_NAME, WB_ACTIVITY_DESCRIPTION + list.getId());
		}

		return list;
	}

	private List<Integer> saveGermplasmsAndNames(CrossesMade crossesMade)
			throws MiddlewareQueryException {
		List<Integer> germplasmIDs = new ArrayList<Integer>();

		Map<Germplasm, Name> currentCrossesMap = crossesMade.getCrossesMap();
		Map<Germplasm, Name> crossesToInsert = new LinkedHashMap<Germplasm, Name>();
		if (this.germplasmList == null) {
			crossesToInsert = currentCrossesMap;

			//when updating a list, determine which germplasms to insert
		} else {
			GenerateCrossNameAction generateAction = new GenerateCrossNameAction();
			CrossingManagerSetting setting = crossesMade.getSetting();
			int nextNumberInSequence = generateAction
					.getNextNumberInSequence(setting.getCrossNameSetting());

			retrieveGermplasmsOfList();
			int ctr = 0; // counter for index of added germplasms
			for (Germplasm currentGplasm : currentCrossesMap.keySet()) {
				boolean existsAlready = false;
				int index = 0;  // counter for removed listdata record(s)
				for (Germplasm existingGplasm : this.existingGermplasms) {
					if (haveSameParents(currentGplasm, existingGplasm)) {
						existsAlready = true;
						indicesOfRetainedCrosses.add(index);
						break;
					}
					index++;
				}
				if (!existsAlready) {
					/*
					 * Regenerate name in the case entries are appended in the end and names
					 * were generated for existing listdata records  
					 */
					Name name = currentCrossesMap.get(currentGplasm);
					if (setting != null) {
						name.setNval(
								generateAction.buildNextNameInSequence(nextNumberInSequence++));
					}

					crossesToInsert.put(currentGplasm, name);
					indicesOfAddedCrosses.add(ctr); // keep track of index of new crosses
				}
				ctr++;
			}
		}

		if (crossesToInsert.size() > 0) {
			germplasmIDs = this.germplasmManager.addGermplasm(crossesToInsert);
		}
		return germplasmIDs;
	}

	private void retrieveGermplasmsOfList() throws MiddlewareQueryException {
		germplasmToListDataMap.clear();

		List<GermplasmListData> allExistingEntries = this.germplasmListManager
				.getGermplasmListDataByListId(this.germplasmList.getId(), 0, Integer.MAX_VALUE);

		//Add only non deleted list data
		existingListEntries = new ArrayList<GermplasmListData>();

		for (GermplasmListData germplasmListData : allExistingEntries) {
			if (germplasmListData.getStatus() != 9) {
				this.existingListEntries.add(germplasmListData);
			}
		}

		List<Integer> gids = new ArrayList<Integer>();
		for (GermplasmListData entry : existingListEntries) {
			gids.add(entry.getGid());
		}
		this.existingGermplasms = this.germplasmManager.getGermplasms(gids);

		for (Germplasm germplasm : this.existingGermplasms) {
			for (GermplasmListData entry : this.existingListEntries) {
				if (entry.getGid().equals(germplasm.getGid())) {
					germplasmToListDataMap.put(germplasm, entry);
				}
			}
		}
	}

	private boolean haveSameParents(Germplasm g1, Germplasm g2) {
		return g1.getGpid1().equals(g2.getGpid1()) && g1.getGpid2().equals(g2.getGpid2());
	}

	private GermplasmList saveGermplasmListRecord(CrossesMade crossesMade)
			throws MiddlewareQueryException {
		int listId;
		GermplasmList listToSave = crossesMade.getGermplasmList();
		if (this.germplasmList == null) {
			listId = this.germplasmListManager.addGermplasmList(listToSave);
		} else {
			// GCP-8225 : set the updates manually on List object so that list entries are not deleted
			this.germplasmList = this.germplasmListManager
					.getGermplasmListById(this.germplasmList.getId());

			this.germplasmList.setName(listToSave.getName());
			this.germplasmList.setDescription(listToSave.getDescription());
			this.germplasmList.setType(listToSave.getType());
			this.germplasmList.setDate(listToSave.getDate());
			this.germplasmList.setNotes(listToSave.getNotes());

			listId = this.germplasmListManager.updateGermplasmList(germplasmList);
		}

		GermplasmList list = this.germplasmListManager.getGermplasmListById(listId);
		return list;
	}

	private void saveGermplasmListDataRecords(CrossesMade crossesMade,
			List<Integer> germplasmIDs, GermplasmList list) throws MiddlewareQueryException {

		deleteRemovedListData(crossesMade);
		addNewGermplasmListData(crossesMade, germplasmIDs, list);
	}

	private void deleteRemovedListData(CrossesMade crossesMade) throws MiddlewareQueryException {
		List<GermplasmListData> retainedCrosses = new ArrayList<GermplasmListData>();
		for (int i = 0; i < existingGermplasms.size(); i++) {
			Germplasm existingGermplasm = existingGermplasms.get(i);
			for (Germplasm currentGermplasm : crossesMade.getCrossesMap().keySet()) {
				if (haveSameParents(currentGermplasm, existingGermplasm)) {
					GermplasmListData germplasmListData = germplasmToListDataMap
							.get(existingGermplasm);
					retainedCrosses.add(germplasmListData);
					break;
				}
			}
		}

		List<GermplasmListData> listToDelete = new ArrayList<GermplasmListData>(
				existingListEntries);
		listToDelete.removeAll(retainedCrosses);

		if (listToDelete.size() > 0) {
			this.germplasmListManager.deleteGermplasmListData(listToDelete);
		}

		//Update "exsitingListEntries", this is used to assign the entry id
		existingListEntries = new ArrayList<GermplasmListData>();

		List<GermplasmListData> allExistingEntries = new ArrayList<GermplasmListData>();

		if (germplasmList != null) {
			allExistingEntries.addAll(this.germplasmListManager
					.getGermplasmListDataByListId(this.germplasmList.getId(), 0,
							Integer.MAX_VALUE));
			Integer entryId = 1;
			for (GermplasmListData germplasmListData : allExistingEntries) {
				if (germplasmListData.getStatus() != 9) {
					germplasmListData.setEntryId(entryId);
					this.existingListEntries.add(germplasmListData);
					entryId++;
				}
			}
		}
		this.germplasmListManager.updateGermplasmListData(existingListEntries);
	}

	private void addNewGermplasmListData(CrossesMade crossesMade,
			List<Integer> germplasmIDs, GermplasmList list)
			throws MiddlewareQueryException {
		Iterator<Integer> germplasmIdIterator = germplasmIDs.iterator();
		List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
		int ctr = 0;

		int entryId = existingListEntries.size() + 1;
		for (Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()) {
			if (germplasmList == null || indicesOfAddedCrosses.contains(ctr)) {
				Integer gid = germplasmIdIterator.next();
				String designation = entry.getValue().getNval();
				String groupName = getFemaleMaleCrossName(crossesMade, designation, ctr);

				GermplasmListData germplasmListData = buildGermplasmListData(
						list, gid, entryId, designation, groupName);

				listToSave.add(germplasmListData);
				entryId++;

			}
			ctr++;
		}

		if (listToSave.size() > 0) {
			this.germplasmListManager.addGermplasmListData(listToSave);
		}
	}

	/*
	 * If current names were generated using prefix in tool, retrieve the
	 * <female parent>/<male parent> cross names from oldCrossNames in CrossesMade
	 */
	private String getFemaleMaleCrossName(CrossesMade crossesMade, String designation,
			Integer ctr) {

		List<GermplasmListEntry> oldCrossNames = crossesMade.getOldCrossNames();
		if (oldCrossNames != null) {
			return oldCrossNames.get(ctr).getDesignation();
		}
		return designation;
	}

	private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId,
			String designation, String groupName) {

		String groupNameSplit[] = groupName.split(",");
		String grpName = groupNameSplit[0];
		String seedSource = groupNameSplit[1];

		GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setList(list);
		germplasmListData.setGid(gid);
		germplasmListData.setEntryId(entryId);
		germplasmListData.setEntryCode(String.valueOf(entryId));
		germplasmListData.setSeedSource(seedSource);
		germplasmListData.setDesignation(designation);
		germplasmListData.setStatus(LIST_DATA_STATUS);
		germplasmListData.setGroupName(grpName);
		germplasmListData.setLocalRecordId(LIST_DATA_LRECID);

		return germplasmListData;
	}

	private void updateConstantFields(CrossesMade crossesMade) throws MiddlewareQueryException {
		Integer ibdbUserId = contextUtil.getCurrentUserLocalId();

		for (Map.Entry<Germplasm, Name> entry : crossesMade.getCrossesMap().entrySet()) {
			Germplasm g = entry.getKey();
			g.setGnpgs(GERMPLASM_GNPGS);
			g.setGrplce(GERMPLASM_GRPLCE);
			g.setLgid(GERMPLASM_LGID);
			g.setMgid(GERMPLASM_MGID);
			g.setUserId(ibdbUserId);
			g.setReferenceId(GERMPLASM_REFID);

			Name n = entry.getValue();
			n.setReferenceId(NAME_REFID);
			n.setUserId(ibdbUserId);
		}

		GermplasmList list = crossesMade.getGermplasmList();
		if (list != null) {
			list.setStatus(GERMPLASM_LIST_STATUS);
			list.setUserId(ibdbUserId);
		}

	}

	public void updateSeedSource(Collection<CrossParents> crossParents)
			throws MiddlewareQueryException {
		retrieveGermplasmsOfList();
		for (CrossParents parents : crossParents) {
			Germplasm currentGermplasm = new Germplasm();
			currentGermplasm.setGpid1(parents.getFemaleParent().getGid());
			currentGermplasm.setGpid2(parents.getMaleParent().getGid());

			for (Germplasm existingGermplasm : existingGermplasms) {
				if (haveSameParents(currentGermplasm, existingGermplasm)) {
					GermplasmListData germplasmListData = germplasmToListDataMap
							.get(existingGermplasm);
					if (germplasmListData != null) {
						germplasmListData.setSeedSource(parents.getSeedSource());
					}
				}
			}
		}
		germplasmListManager.updateGermplasmListData(this.existingListEntries);
	}

}

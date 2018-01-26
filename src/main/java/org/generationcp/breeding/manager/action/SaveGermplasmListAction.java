package org.generationcp.breeding.manager.action;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Configurable
public class SaveGermplasmListAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	public static final String LIST_DATA_SOURCE = "Crossing Manager Tool";
	public static final Integer LIST_DATA_STATUS = 0;
	public static final Integer LIST_DATA_LRECID = 0;

	private GermplasmList germplasmList;
	private final List<GermplasmListEntry> listEntries;

	private final SaveGermplasmListActionSource source;

	public SaveGermplasmListAction(final SaveGermplasmListActionSource source, final GermplasmList germplasmList,
			final List<GermplasmListEntry> listEntries) {
		this.source = source;
		this.germplasmList = germplasmList;
		this.listEntries = listEntries;
	}

	public GermplasmList saveRecords() {

		// set the listnms.listuid to the current user
		final Integer userId = this.contextUtil.getCurrentUserLocalId();
		this.germplasmList.setUserId(userId);
		this.germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
		this.germplasmList = this.saveGermplasmListRecord(this.germplasmList);
		this.saveGermplasmListDataRecords(this.germplasmList, this.listEntries);

		return this.germplasmList;
	}

	private GermplasmList saveGermplasmListRecord(final GermplasmList germplasmList) {
		int listId = 0;

		if (germplasmList.getId() == null) { // add new
			germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
			listId = this.germplasmListManager.addGermplasmList(germplasmList);
		} else { // update
			final GermplasmList listToUpdate = this.germplasmListManager.getGermplasmListById(germplasmList.getId());
			listId = this.germplasmListManager.updateGermplasmList(listToUpdate);
		}

		final GermplasmList list = this.germplasmListManager.getGermplasmListById(listId);

		return list;
	}

	private void saveGermplasmListDataRecords(final GermplasmList list, final List<GermplasmListEntry> listEntries) {

		final List<GermplasmListData> currentListDataEntries = new ArrayList<GermplasmListData>();

		for (final GermplasmListEntry listEntry : listEntries) {
			final int gid = listEntry.getGid();
			final int entryId = listEntry.getEntryId();
			final String designation = listEntry.getDesignation();
			final String groupName = this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties);
			final String seedSource = listEntry.getSeedSource();

			final GermplasmListData germplasmListData = this.buildGermplasmListData(list, gid, entryId, designation, groupName, seedSource);

			currentListDataEntries.add(germplasmListData); // with no ids
		}

		final List<GermplasmListData> existingListDataEntries =
				this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId());

		// get all the list to add
		List<GermplasmListData> listToAdd = new ArrayList<GermplasmListData>();
		List<GermplasmListData> listToDelete = new ArrayList<GermplasmListData>();

		if (!existingListDataEntries.isEmpty()) {
			listToAdd = this.getNewEntriesToSave(currentListDataEntries, existingListDataEntries);
			listToDelete = this.getNewEntriesToDelete(currentListDataEntries, existingListDataEntries);
		} else {
			listToAdd.addAll(currentListDataEntries);
		}

		if (!listToAdd.isEmpty()) {
			this.germplasmListManager.addGermplasmListData(listToAdd); // ADD the newly created
		}

		// DELETE non entries not part of list anymore
		this.germplasmListManager.deleteGermplasmListData(listToDelete);

		// get all the updated entries
		List<GermplasmListData> listToUpdate = new ArrayList<GermplasmListData>();

		if (!existingListDataEntries.isEmpty()) {
			listToUpdate = this.getEntriesToUpdate(currentListDataEntries, existingListDataEntries);
		}

		for (final GermplasmListData entryToUpdate : listToUpdate) {
			for (final GermplasmListData currentEntry : currentListDataEntries) {
				if (entryToUpdate.getId().equals(currentEntry.getId())) {
					entryToUpdate.setEntryId(currentEntry.getEntryId());
				}
			}
		}

		this.germplasmListManager.updateGermplasmListData(listToUpdate); // UPDATE the existing created list

		// after saving iterate through the itemIds
		final int currentSaveListCount = (int) this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId());
		final List<GermplasmListData> currentSavedList =
				this.inventoryDataManager.getLotCountsForList(this.germplasmList.getId(), 0, Long.valueOf(currentSaveListCount).intValue());
		if (this.source != null) {
			this.source.updateListDataTable(this.germplasmList.getId(), currentSavedList);
		}

	}

	private List<GermplasmListData> getNewEntriesToSave(final List<GermplasmListData> currentListDataEntries,
			final List<GermplasmListData> existingListDataEntries) {
		final List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		for (final GermplasmListData entry : currentListDataEntries) {
			if (!existingListDataEntries.contains(entry)) {
				entry.setId(null);
				toreturn.add(entry);
			}
		}
		return toreturn;
	}

	private List<GermplasmListData> getNewEntriesToDelete(final List<GermplasmListData> currentListDataEntries,
			final List<GermplasmListData> existingListDataEntries) {
		final List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		for (final GermplasmListData entry : existingListDataEntries) {
			if (!currentListDataEntries.contains(entry)) {
				toreturn.add(entry);
			}
		}
		return toreturn;
	}

	private List<GermplasmListData> getEntriesToUpdate(final List<GermplasmListData> currentListDataEntries,
			final List<GermplasmListData> existingListDataEntries) {
		final List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();
		for (final GermplasmListData entry : existingListDataEntries) {
			if (currentListDataEntries.contains(entry)) {
				toreturn.add(entry);
			}
		}
		return toreturn;
	}

	private GermplasmListData buildGermplasmListData(final GermplasmList list, final Integer gid, final int entryId,
			final String designation, final String groupName, final String seedSource) {

		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setList(list);
		germplasmListData.setGid(gid);
		germplasmListData.setEntryId(entryId);
		germplasmListData.setEntryCode(String.valueOf(entryId));
		germplasmListData.setSeedSource(seedSource);
		germplasmListData.setDesignation(designation);
		germplasmListData.setStatus(SaveGermplasmListAction.LIST_DATA_STATUS);
		germplasmListData.setGroupName(groupName);
		germplasmListData.setLocalRecordId(SaveGermplasmListAction.LIST_DATA_LRECID);

		return germplasmListData;
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setPedigreeService(final PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

}


package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class ListCommonActionsUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ListCommonActionsUtil.class);

	// so class cannot be instantiated
	private ListCommonActionsUtil() {
	}

	public static void deleteGermplasmList(final GermplasmListManager germplasmListManager, final GermplasmList germplasmList,
			final ContextUtil contextUtil, final Window window, final SimpleResourceBundleMessageSource messageSource, final String item) {

		germplasmListManager.deleteGermplasmList(germplasmList);

		contextUtil.logProgramActivity("Deleted a germplasm list.", "Deleted germplasm list with id = " + germplasmList.getId()
				+ " and name = " + germplasmList.getName() + ".");

		MessageNotifier.showMessage(window, messageSource.getMessage(Message.SUCCESS),
				messageSource.getMessage(Message.SUCCESSFULLY_DELETED_ITEM, item));

	}

	/**
	 * Iterates through the whole table, gets selected item GID's, make sure it's sorted as seen on the UI
	 */
	@SuppressWarnings("unchecked")
	public static List<Integer> getSelectedGidsFromListDataTable(final Table table, final String gidItemId) {
		final List<Integer> itemIds = new ArrayList<Integer>();
		final List<Integer> selectedItemIds = new ArrayList<Integer>();
		final List<Integer> trueOrderedSelectedGIDs = new ArrayList<Integer>();

		selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
		itemIds.addAll((Collection<Integer>) table.getItemIds());

		for (final Integer itemId : itemIds) {
			if (selectedItemIds.contains(itemId)) {
				final Integer gid =
						Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(gidItemId).getValue()).getCaption().toString());
				trueOrderedSelectedGIDs.add(gid);
			}
		}

		return trueOrderedSelectedGIDs;
	}

	public static GermplasmList overwriteList(final GermplasmList listToSave, final GermplasmListManager dataManager,
			final Component source, final SimpleResourceBundleMessageSource messageSource, final Boolean showMessages) {
		GermplasmList savedList = null;
		try {
			Integer listId = null;
			final GermplasmList listFromDB = dataManager.getGermplasmListById(listToSave.getId());
			if (listFromDB != null) {
				listFromDB.setName(listToSave.getName());
				listFromDB.setDescription(listToSave.getDescription());
				listFromDB.setDate(listToSave.getDate());
				listFromDB.setType(listToSave.getType());
				listFromDB.setNotes(listToSave.getNotes());
				listFromDB.setParent(listToSave.getParent());

				listId = dataManager.updateGermplasmList(listFromDB);
			}

			if (listId == null) {
				if (showMessages) {
					MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
							messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
				}
				return null;
			} else {
				savedList = listFromDB;

				if (source instanceof ListBuilderComponent) {
					final ListBuilderComponent component = (ListBuilderComponent) source;
					component.setCurrentlySavedGermplasmList(listFromDB);
					component.setHasUnsavedChanges(false);
					component.getSource().getListSelectionComponent().showNodeOnTree(listId);
				} else if (source instanceof ListManagerMain) {
					final ListManagerMain component = (ListManagerMain) source;
					component.getListSelectionComponent().updateUIForRenamedList(listToSave, listToSave.getName());

					component.getListSelectionComponent().showNodeOnTree(listFromDB.getId());
					MessageNotifier.showMessage(source.getWindow(), messageSource.getMessage(Message.SUCCESS),
							"Changes to list header were saved.", 3000);
				}

			}
		} catch (final MiddlewareQueryException ex) {
			ListCommonActionsUtil.LOG.error("Error in updating germplasm list: " + listToSave.getId(), ex);
			if (showMessages) {
				MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
						messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
			}
			return null;
		}
		return savedList;
	}

	public static boolean overwriteListEntries(final GermplasmList listToSave, final List<GermplasmListData> listEntries,
			final Boolean forceHasChanges, final GermplasmListManager germplasmListManager, final Component source,
			final SimpleResourceBundleMessageSource messageSource, final Boolean showMessages) {

		if (forceHasChanges) {
			return ListCommonActionsUtil.replaceListEntries(listToSave, listEntries, germplasmListManager, source, messageSource,
					showMessages);
		}

		final List<GermplasmListData> newEntries = new ArrayList<GermplasmListData>();
		final List<GermplasmListData> entriesToUpdate = new ArrayList<GermplasmListData>();
		final List<GermplasmListData> entriesToDelete = new ArrayList<GermplasmListData>();

		ListCommonActionsUtil.getNewEntriesToSaveUpdateDelete(listToSave, listEntries, forceHasChanges, newEntries, entriesToUpdate,
				entriesToDelete, germplasmListManager, source, messageSource);

		return ListCommonActionsUtil.saveListEntries(listToSave, newEntries, entriesToUpdate, entriesToDelete, germplasmListManager,
				source, messageSource, showMessages);
	}

	protected static void getNewEntriesToSaveUpdateDelete(final GermplasmList listToSave, final List<GermplasmListData> listEntries,
			final Boolean forceHasChanges, final List<GermplasmListData> newEntries, final List<GermplasmListData> entriesToUpdate,
			final List<GermplasmListData> entriesToDelete, final GermplasmListManager dataManager, final Component source,
			final SimpleResourceBundleMessageSource messageSource) {

		final Map<Integer, GermplasmListData> savedListEntriesMap =
				ListCommonActionsUtil.getSavedListEntriesMap(listToSave, listEntries, forceHasChanges, entriesToDelete, dataManager,
						source, messageSource);

		for (final GermplasmListData entry : listEntries) {
			if ((entry.getId() != null && entry.getId() > 0) && !savedListEntriesMap.isEmpty()
					&& savedListEntriesMap.get(entry.getId()) != null) {

				final GermplasmListData matchingSavedEntry = savedListEntriesMap.get(entry.getId());
				// check if it will be updated
				boolean thereIsAChange = false;
				if (!matchingSavedEntry.getDesignation().equals(entry.getDesignation())) {
					thereIsAChange = true;
					ListCommonActionsUtil.setDesignationOfMatchingSavedEntry(entry, matchingSavedEntry);
				}

				if (!matchingSavedEntry.getEntryCode().equals(entry.getEntryCode())) {
					thereIsAChange = true;
					ListCommonActionsUtil.setEntryCodeOfMatchingSavedEntry(entry, matchingSavedEntry);
				}

				if (!matchingSavedEntry.getEntryId().equals(entry.getEntryId())) {
					thereIsAChange = true;
					matchingSavedEntry.setEntryId(entry.getEntryId());
				}

				if (!matchingSavedEntry.getGroupName().equals(entry.getGroupName())) {
					thereIsAChange = true;
					ListCommonActionsUtil.setGroupNameOfMatchingSavedEntry(entry, matchingSavedEntry);
				}

				if (!matchingSavedEntry.getSeedSource().equals(entry.getSeedSource())) {
					thereIsAChange = true;
					ListCommonActionsUtil.setSeedSourceOfMatchingSavedEntry(entry, matchingSavedEntry);
				}

				if (thereIsAChange) {
					entriesToUpdate.add(matchingSavedEntry);
				}

			} else {
				// add to new entries to add
				final GermplasmListData listEntry = new GermplasmListData();
				ListCommonActionsUtil.copyFieldsToNewListEntry(listEntry, entry, listToSave);
				newEntries.add(listEntry);
			}
		}

	}

	protected static void setDesignationOfMatchingSavedEntry(final GermplasmListData entry, final GermplasmListData matchingSavedEntry) {
		final String designation = entry.getDesignation();
		if (designation != null && designation.length() != 0) {
			matchingSavedEntry.setDesignation(designation);
		} else {
			matchingSavedEntry.setDesignation("-");
		}
	}

	protected static void setEntryCodeOfMatchingSavedEntry(final GermplasmListData entry, final GermplasmListData matchingSavedEntry) {
		final String entryCode = entry.getEntryCode();
		if (entryCode != null && entryCode.length() != 0) {
			matchingSavedEntry.setEntryCode(entryCode);
		} else {
			matchingSavedEntry.setEntryCode(entry.getEntryId().toString());
		}
	}

	protected static void setSeedSourceOfMatchingSavedEntry(final GermplasmListData entry, final GermplasmListData matchingSavedEntry) {
		final String seedSource = entry.getSeedSource();
		if (seedSource != null && seedSource.length() != 0) {
			matchingSavedEntry.setSeedSource(seedSource);
		} else {
			matchingSavedEntry.setSeedSource("-");
		}
	}

	protected static void setGroupNameOfMatchingSavedEntry(final GermplasmListData entry, final GermplasmListData matchingSavedEntry) {
		String groupName = entry.getGroupName();
		if (groupName != null && groupName.length() != 0) {
			if (groupName.length() > 255) {
				groupName = groupName.substring(0, 255);
			}
			matchingSavedEntry.setGroupName(groupName);
		} else {
			matchingSavedEntry.setGroupName("-");
		}
	}

	private static Map<Integer, GermplasmListData> getSavedListEntriesMap(final GermplasmList listToSave,
			final List<GermplasmListData> listEntries, final Boolean forceHasChanges, final List<GermplasmListData> entriesToDelete,
			final GermplasmListManager dataManager, final Component source, final SimpleResourceBundleMessageSource messageSource) {
		final Map<Integer, GermplasmListData> savedListEntriesMap = new HashMap<Integer, GermplasmListData>();
		try {
			final int listDataCount = (int) dataManager.countGermplasmListDataByListId(listToSave.getId());
			final List<GermplasmListData> savedListEntries = dataManager.getGermplasmListDataByListId(listToSave.getId(), 0, listDataCount);
			if (savedListEntries != null) {
				for (final GermplasmListData savedEntry : savedListEntries) {
					// check entries to be deleted
					if (!listEntries.contains(savedEntry) || forceHasChanges) {
						entriesToDelete.add(savedEntry);
					} else {
						// add to map for possible update
						savedListEntriesMap.put(savedEntry.getId(), savedEntry);
					}
				}
			}
		} catch (final MiddlewareQueryException ex) {
			ListCommonActionsUtil.LOG.error("Error with getting the saved list entries.", ex);
			MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
					messageSource.getMessage(Message.ERROR_GETTING_SAVED_ENTRIES));
		}
		return savedListEntriesMap;
	}

	private static void copyFieldsToNewListEntry(final GermplasmListData destination, final GermplasmListData origin,
			final GermplasmList listToSave) {
		if (destination != null && origin != null) {
			destination.setDesignation(origin.getDesignation());
			destination.setEntryCode(origin.getEntryCode());
			destination.setEntryId(origin.getEntryId());
			destination.setGid(origin.getGid());
			destination.setGroupName(origin.getGroupName());
			destination.setSeedSource(origin.getSeedSource());
			destination.setList(listToSave);
			destination.setStatus(Integer.valueOf(0));
			destination.setLocalRecordId(Integer.valueOf(0));
		}
	}

	private static boolean saveListEntries(final GermplasmList listToSave, final List<GermplasmListData> newEntries,
			final List<GermplasmListData> entriesToUpdate, final List<GermplasmListData> entriesToDelete,
			final GermplasmListManager dataManager, final Component source, final SimpleResourceBundleMessageSource messageSource,
			final Boolean showMessages) {
		boolean hasError = false;

		if (!newEntries.isEmpty()) {
			try {
				final List<Integer> savedEntryPKs = dataManager.addGermplasmListData(newEntries);
				if (!(savedEntryPKs.size() == newEntries.size())) {
					hasError = true;
				}
			} catch (final MiddlewareQueryException ex) {
				ListCommonActionsUtil.LOG.error("Error in saving germplasm list entries.", ex);
				hasError = true;
			}
		}
		if (!hasError && !entriesToUpdate.isEmpty()) {
			try {
				final List<Integer> updatedEntryPKs = dataManager.updateGermplasmListData(entriesToUpdate);
				if (!(updatedEntryPKs.size() == entriesToUpdate.size())) {
					hasError = true;
				}
			} catch (final MiddlewareQueryException ex) {
				ListCommonActionsUtil.LOG.error("Error in updating germplasm list entries.", ex);
			}
		}
		if (!hasError && !entriesToDelete.isEmpty()) {
			try {
				final int deleteGermplasmListData = dataManager.deleteGermplasmListData(entriesToDelete);
				if (!(deleteGermplasmListData == entriesToDelete.size())) {
					hasError = true;
				}
			} catch (final MiddlewareQueryException ex) {
				ListCommonActionsUtil.LOG.error("Error in deleting germplasm list entries.", ex);
			}
		}
		if (hasError) {
			if (showMessages) {
				MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
						messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
			}
			return false;
		}
		return true;
	}

	private static boolean replaceListEntries(final GermplasmList listToSave, final List<GermplasmListData> listEntries,
			final GermplasmListManager dataManager, final Component source, final SimpleResourceBundleMessageSource messageSource,
			final Boolean showMessages) {

		boolean hasError = false;

		hasError = deleteExistingListEntries(listToSave, dataManager);

		if (!hasError && !listEntries.isEmpty()) {
			try {
				final List<GermplasmListData> newEntries = new ArrayList<GermplasmListData>();
				for (final GermplasmListData entry : listEntries) {
					final GermplasmListData listEntry = new GermplasmListData();
					ListCommonActionsUtil.copyFieldsToNewListEntry(listEntry, entry, listToSave);
					newEntries.add(listEntry);
				}
				final List<Integer> savedEntryPKs = dataManager.addGermplasmListData(newEntries);
				if (!(savedEntryPKs.size() == newEntries.size())) {
					hasError = true;
				}
			} catch (final MiddlewareQueryException ex) {
				ListCommonActionsUtil.LOG.error("Error in saving germplasm list entries.", ex);
				hasError = true;
			}
		}

		if (hasError) {
			if (showMessages) {
				MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
						messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST_ENTRIES));
			}
			return false;
		}
		return true;
	}

	public static boolean deleteExistingListEntries(final GermplasmList listToSave, final GermplasmListManager dataManager) {
		boolean hasError = false;
		try {
			dataManager.deleteGermplasmListDataByListId(listToSave.getId());
		} catch (final MiddlewareQueryException ex) {
			ListCommonActionsUtil.LOG.error("Error in deleting germplasm list entries.", ex);
			hasError = true;
		}
		return hasError;
	}

}

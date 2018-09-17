
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialogSource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

@Configurable
public class ListComponentAddEntryDialogSource implements AddEntryDialogSource {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	private ListComponent listComponent;
	private Table listDataTable;
	private NewGermplasmEntriesFillColumnSource newEntriesSource;
	private AddedColumnsMapper addedColumnsMapper;

	public ListComponentAddEntryDialogSource(final ListComponent listComponent, final Table listDataTable) {
		super();
		this.listComponent = listComponent;
		this.listDataTable = listDataTable;
		this.newEntriesSource = new NewGermplasmEntriesFillColumnSource(this.listDataTable);
		this.addedColumnsMapper = new AddedColumnsMapper(this.newEntriesSource);
	}

	@Override
	public void finishAddingEntry(final Integer gid) {
		this.finishAddingEntry(gid, true);
	}

	@Override
	public void finishAddingEntry(final List<Integer> gids) {
		Boolean allSuccessful = true;
		for (final Integer gid : gids) {
			if (this.finishAddingEntry(gid, false).equals(false)) {
				allSuccessful = false;
			}
		}
		if (allSuccessful) {
			MessageNotifier.showMessage(this.listComponent.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SAVING_SUCCESS), 3000);
		}
	}

	Boolean finishAddingEntry(final Integer gid, final Boolean showSuccessMessage) {

		final Germplasm germplasm = this.germplasmDataManager.getGermplasmWithPrefName(gid);

		Integer maxEntryId = 0;
		if (this.listDataTable != null) {
			for (final Iterator<?> i = this.listDataTable.getItemIds().iterator(); i.hasNext();) {
				// iterate through the table elements' IDs
				final int listDataId = (Integer) i.next();

				// update table item's entryId
				final Item item = this.listDataTable.getItem(listDataId);
				final Integer entryId = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
				if (maxEntryId < entryId) {
					maxEntryId = entryId;
				}
			}
		}

		GermplasmListData listData = new GermplasmListData();
		listData.setList(this.listComponent.getGermplasmList());
		if (germplasm.getPreferredName() != null) {
			listData.setDesignation(germplasm.getPreferredName().getNval());
		} else {
			listData.setDesignation("-");
		}
		listData.setEntryId(maxEntryId + 1);
		listData.setGid(gid);
		listData.setLocalRecordId(0);
		listData.setStatus(0);
		listData.setEntryCode(listData.getEntryId().toString());
		listData.setSeedSource(this.germplasmDataManager.getPlotCodeValue(gid));
		listData.setGroupId(germplasm.getMgid());

		String groupName = "-";
		groupName = this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties);
		listData.setGroupName(groupName);

		final Integer listDataId;
		listDataId = this.germplasmListManager.addGermplasmListData(listData);

		// create table if added entry is first listdata record
		if (this.listDataTable == null) {
			this.listComponent.initializeListDataTable(new TableWithSelectAllLayout(this.listComponent.getNoOfEntries(),
					this.listComponent.getNoOfEntries(), ColumnLabels.TAG.getName()));
			this.listComponent.initializeValues();
		} else {
			this.listDataTable.setEditable(false);
			final List<GermplasmListData> inventoryData = this.inventoryDataManager.getLotCountsForListEntries(
					this.listComponent.getGermplasmList().getId(), new ArrayList<>(Collections.singleton(listDataId)));
			if (inventoryData != null) {
				listData = inventoryData.get(0);
			}
			this.listComponent.addListEntryToTable(listData);

			// Generate values for added columns, if any
			if (this.listComponent.listHasAddedColumns()) {
				this.newEntriesSource.setAddedItemIds(Arrays.asList(listDataId));
				this.newEntriesSource.setAddedGids(Arrays.asList(gid));
				this.addedColumnsMapper.generateValuesForAddedColumns(this.listDataTable.getVisibleColumns());
			}

			this.listComponent.saveChangesAction(this.listComponent.getWindow(), false);
			this.listDataTable.refreshRowCache();
			this.listDataTable.setImmediate(true);
			this.listDataTable.setEditable(true);
		}

		if (showSuccessMessage) {
			this.listComponent.setHasUnsavedChanges(false);
			MessageNotifier.showMessage(this.listComponent.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					"Successful in adding list entries.", 3000);
		}

		this.listComponent.setDoneInitializing(true);
		return true;

	}

	@Override
	public ListManagerMain getListManagerMain() {
		return this.listComponent.getListManagerMain();
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setPedigreeService(final PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	public void setListComponent(final ListComponent listComponent) {
		this.listComponent = listComponent;
	}

	public void setListDataTable(final Table listDataTable) {
		this.listDataTable = listDataTable;
	}

	public void setNewEntriesSource(final NewGermplasmEntriesFillColumnSource newEntriesSource) {
		this.newEntriesSource = newEntriesSource;
	}

	public void setAddedColumnsMapper(final AddedColumnsMapper addedColumnsMapper) {
		this.addedColumnsMapper = addedColumnsMapper;
	}

}

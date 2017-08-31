
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
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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

		final Germplasm germplasm;

		try {
			germplasm = this.germplasmDataManager.getGermplasmWithPrefName(gid);
		} catch (final MiddlewareQueryException ex) {
			MessageNotifier.showError(this.listComponent.getWindow(), ListComponent.DATABASE_ERROR,
					"Error with getting germplasm with id: " + gid + ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			return false;
		}

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

		String groupName;
		try {
			groupName = this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties);
		} catch (final MiddlewareQueryException ex) {
			groupName = "-";
		}
		listData.setGroupName(groupName);

		final Integer listDataId;
		try {
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
				if (AddColumnContextMenu.sourceHadAddedColumn(this.listDataTable.getVisibleColumns())) {
					this.newEntriesSource.setAddedItemIds(Arrays.asList(listDataId));
					this.newEntriesSource.setAddedGids(Arrays.asList(gid));
					// Add Column > "Fill With Attribute" is disabled in View List context hence 2nd parameter is false
					this.addedColumnsMapper.generateValuesForAddedColumns(this.listDataTable.getVisibleColumns(), false);
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

		} catch (final MiddlewareQueryException ex) {
			MessageNotifier.showError(this.listComponent.getWindow(), ListComponent.DATABASE_ERROR,
					"Error with adding list entry. " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			return false;
		}

	}

	@Override
	public ListManagerMain getListManagerMain() {
		return this.listComponent.getListManagerMain();
	}

	
	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	
	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	
	public void setPedigreeService(PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	
	public void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	
	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	
	public void setCrossExpansionProperties(CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	
	public void setListComponent(ListComponent listComponent) {
		this.listComponent = listComponent;
	}

	
	public void setListDataTable(Table listDataTable) {
		this.listDataTable = listDataTable;
	}

	
	public void setNewEntriesSource(NewGermplasmEntriesFillColumnSource newEntriesSource) {
		this.newEntriesSource = newEntriesSource;
	}

	
	public void setAddedColumnsMapper(AddedColumnsMapper addedColumnsMapper) {
		this.addedColumnsMapper = addedColumnsMapper;
	}

}

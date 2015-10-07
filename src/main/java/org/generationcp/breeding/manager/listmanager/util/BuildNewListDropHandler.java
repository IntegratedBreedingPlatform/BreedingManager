
package org.generationcp.breeding.manager.listmanager.util;

import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.data.Item;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;

public class BuildNewListDropHandler extends DropHandlerMethods implements DropHandler {

	private static final Logger LOG = LoggerFactory.getLogger(BuildNewListDropHandler.class);
	private static final long serialVersionUID = 1L;
	private PlatformTransactionManager transactionManager;

	public BuildNewListDropHandler(ListManagerMain listManagerMain, GermplasmDataManager germplasmDataManager,
			GermplasmListManager germplasmListManager, InventoryDataManager inventoryDataManager, PedigreeService pedigreeService,
			CrossExpansionProperties crossExpansionProperties, Table targetTable, PlatformTransactionManager transactionManager) {
		this.listManagerMain = listManagerMain;
		this.germplasmDataManager = germplasmDataManager;
		this.germplasmListManager = germplasmListManager;
		this.inventoryDataManager = inventoryDataManager;
		this.pedigreeService = pedigreeService;
		this.crossExpansionProperties = crossExpansionProperties;
		this.targetTable = targetTable;
		this.transactionManager = transactionManager;
	}

	@Override
	public void drop(final DragAndDropEvent event) {

		final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				if (event.getTransferable() instanceof TableTransferable) {

					TableTransferable transferable = (TableTransferable) event.getTransferable();
					Table sourceTable = transferable.getSourceComponent();
					String sourceTableData = sourceTable.getData().toString();
					AbstractSelectTargetDetails dropData = (AbstractSelectTargetDetails) event.getTargetDetails();
					BuildNewListDropHandler.this.targetTable = (Table) dropData.getTarget();

					if (sourceTableData.equals(DropHandlerMethods.MATCHING_GERMPLASMS_TABLE_DATA)) {
						BuildNewListDropHandler.super.setHasUnsavedChanges(true);

						// If table has selected items, add selected items
						if (BuildNewListDropHandler.this.hasSelectedItems(sourceTable)) {
							BuildNewListDropHandler.this.addSelectedGermplasmsFromTable(sourceTable);
						} else {
							// If none, add what was dropped
							BuildNewListDropHandler.this.addGermplasm((Integer) transferable.getItemId());
						}

					} else if (sourceTableData.equals(DropHandlerMethods.MATCHING_LISTS_TABLE_DATA)) {
						BuildNewListDropHandler.super.setHasUnsavedChanges(true);

						// If table has selected items, add selected items
						if (BuildNewListDropHandler.this.hasSelectedItems(sourceTable)) {
							BuildNewListDropHandler.this.addSelectedGermplasmListsFromTable(sourceTable);
						} else {
							// If none, add what was dropped
							BuildNewListDropHandler.this.addGermplasmList((Integer) transferable.getItemId());
						}

					} else if (sourceTableData.equals(DropHandlerMethods.LIST_DATA_TABLE_DATA)) {
						BuildNewListDropHandler.super.setHasUnsavedChanges(true);

						// If table has selected items, add selected items
						if (BuildNewListDropHandler.this.hasSelectedItems(sourceTable)) {
							BuildNewListDropHandler.this.addFromListDataTable(sourceTable);
						} else if (transferable.getSourceComponent().getParent().getParent() instanceof ListComponent) {
							// If none, add what was dropped
							Integer listId =
									((ListComponent) transferable.getSourceComponent().getParent().getParent()).getGermplasmListId();
							BuildNewListDropHandler.this.addGermplasmFromList(listId, (Integer) transferable.getItemId());
						}

					} else if (sourceTableData.equals(ListBuilderComponent.GERMPLASMS_TABLE_DATA)) {
						Object droppedOverItemId = dropData.getItemIdOver();

						// Check first if item is dropped on top of itself
						if (!transferable.getItemId().equals(droppedOverItemId)) {

							BuildNewListDropHandler.super.setHasUnsavedChanges(true);

							Item oldItem = sourceTable.getItem(transferable.getItemId());
							Object oldCheckBox = oldItem.getItemProperty(ColumnLabels.TAG.getName()).getValue();
							Object oldGid = oldItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
							Object oldEntryCode = oldItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue();
							Object oldSeedSource = oldItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue();
							Object oldDesignation = oldItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
							Object oldParentage = oldItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
							Object oldAvailInv = oldItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
							Object oldSeedRes = oldItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue();

							sourceTable.removeItem(transferable.getItemId());

							Item newItem = sourceTable.addItemAfter(droppedOverItemId, transferable.getItemId());
							newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(oldCheckBox);
							newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(oldGid);
							newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(oldEntryCode);
							newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(oldSeedSource);
							newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(oldDesignation);
							newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(oldParentage);
							newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(oldAvailInv);
							newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue("");
							newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(oldSeedRes);

							BuildNewListDropHandler.this.assignSerializedEntryNumber();

							BuildNewListDropHandler.this.fireListUpdatedEvent();
						}
					} else {
						BuildNewListDropHandler.LOG.error("Error During Drop: Unknown table data: " + sourceTableData);
					}

				} else {
					// If source is from tree
					Transferable transferable = event.getTransferable();
					BuildNewListDropHandler.this.addGermplasmList((Integer) transferable.getData("itemId"));
				}
			}

		});

	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}
}

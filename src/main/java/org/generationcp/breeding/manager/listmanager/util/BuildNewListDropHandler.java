
package org.generationcp.breeding.manager.listmanager.util;

import java.util.Arrays;

import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.middleware.constant.ColumnLabels;
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

	public BuildNewListDropHandler(final ListManagerMain listManagerMain, final GermplasmDataManager germplasmDataManager,
			final GermplasmListManager germplasmListManager, final InventoryDataManager inventoryDataManager,
			final PedigreeService pedigreeService, final CrossExpansionProperties crossExpansionProperties, final Table targetTable,
			final PlatformTransactionManager transactionManager) {
		this.listManagerMain = listManagerMain;
		this.germplasmDataManager = germplasmDataManager;
		this.germplasmListManager = germplasmListManager;
		this.inventoryDataManager = inventoryDataManager;
		this.pedigreeService = pedigreeService;
		this.crossExpansionProperties = crossExpansionProperties;
		this.setTargetTable(targetTable);
		this.transactionManager = transactionManager;
	}

	@Override
	public void drop(final DragAndDropEvent event) {

		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				if (event.getTransferable() instanceof TableTransferable) {

					final TableTransferable transferable = (TableTransferable) event.getTransferable();
					final Table sourceTable = transferable.getSourceComponent();
					final String sourceTableData = sourceTable.getData().toString();
					final AbstractSelectTargetDetails dropData = (AbstractSelectTargetDetails) event.getTargetDetails();
					BuildNewListDropHandler.this.setTargetTable((Table) dropData.getTarget());

					if (sourceTableData.equals(DropHandlerMethods.MATCHING_GERMPLASMS_TABLE_DATA)) {
						BuildNewListDropHandler.super.setHasUnsavedChanges(true);

						// If table has selected items, add selected items
						if (BuildNewListDropHandler.this.hasSelectedItems(sourceTable)) {
							BuildNewListDropHandler.this.addSelectedGermplasmsFromTable(sourceTable);
						} else {
							// If none, add what was dropped

							final Item item = transferable.getSourceComponent().getItem(transferable.getItemId());
							final Integer selectedGid =
									Integer.valueOf(item.getItemProperty(ColumnLabels.GID.getName() + "_REF").getValue().toString());
							BuildNewListDropHandler.this.addGermplasm(Arrays.asList(selectedGid));
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
							final Integer listId =
									((ListComponent) transferable.getSourceComponent().getParent().getParent()).getGermplasmListId();
							BuildNewListDropHandler.this.addGermplasmFromList(listId, (Integer) transferable.getItemId());
						}

					} else if (sourceTableData.equals(ListBuilderComponent.GERMPLASMS_TABLE_DATA)) {
						final Object droppedOverItemId = dropData.getItemIdOver();

						// Check first if item is dropped on top of itself
						if (!transferable.getItemId().equals(droppedOverItemId)) {

							BuildNewListDropHandler.super.setHasUnsavedChanges(true);

							final Item oldItem = sourceTable.getItem(transferable.getItemId());
							final Object oldCheckBox = oldItem.getItemProperty(ColumnLabels.TAG.getName()).getValue();
							final Object oldGid = oldItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
							final Object oldGroupId = oldItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).getValue();
							final Object oldEntryCode = oldItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue();
							final Object oldSeedSource = oldItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue();
							final Object oldDesignation = oldItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
							final Object oldParentage = oldItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
							final Object oldAvailInv = oldItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
							final Object oldAvailable = oldItem.getItemProperty(ColumnLabels.TOTAL.getName()).getValue();
							sourceTable.removeItem(transferable.getItemId());

							final Item newItem = sourceTable.addItemAfter(droppedOverItemId, transferable.getItemId());
							newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(oldCheckBox);
							newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(oldGid);
							newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(oldGroupId);
							newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(oldEntryCode);
							newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(oldSeedSource);
							newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(oldDesignation);
							newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(oldParentage);
							newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(oldAvailInv);
							newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue("");
							newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(oldAvailable);

							BuildNewListDropHandler.this.assignSerializedEntryNumber();

							BuildNewListDropHandler.this.fireListUpdatedEvent();
						}
					} else {
						BuildNewListDropHandler.LOG.error("Error During Drop: Unknown table data: " + sourceTableData);
					}

				} else {
					// If source is from tree
					final Transferable transferable = event.getTransferable();
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

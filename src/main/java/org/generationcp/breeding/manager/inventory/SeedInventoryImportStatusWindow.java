package org.generationcp.breeding.manager.inventory;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventory;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

@Configurable
public class SeedInventoryImportStatusWindow extends BaseSubWindow implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final long serialVersionUID = -7800270790767272974L;

	private Table statusTable;

	private Button continueButton;

	private Button cancelButton;

	private VerticalLayout mainLayout;
	private Component source;

	public final static int COMMITTED_STATUS = 1;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	protected OntologyDataManager ontologyDataManager;

	@Autowired
	protected InventoryDataManager inventoryDataManager;

	private List<ImportedSeedInventory> importedSeedInventories;
	private List<Transaction> processedTransactions;
	Component listComponent;

	private Map<String, String> importStatusMessages = new HashMap<>();

	public SeedInventoryImportStatusWindow() {
		super();
	}

	public SeedInventoryImportStatusWindow(final Component source, final Component listComponent,
			List<ImportedSeedInventory> importedSeedInventories, List<Transaction> processedTransactions) {
		this.source = source;
		this.listComponent = listComponent;
		this.importedSeedInventories = importedSeedInventories;
		this.processedTransactions = processedTransactions;
	}

	@Override
	public void instantiateComponents() {
		this.setCaption(this.messageSource.getMessage(Message.SEED_IMPORT_STATUS));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);

		this.statusTable = new Table();
		this.statusTable.setDebugId("statusTable");
		this.statusTable.setWidth("100%");
		this.statusTable.setHeight("150px");
		this.statusTable.setImmediate(true);

		this.statusTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.statusTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), String.class, null);
		this.statusTable.addContainerProperty(ColumnLabels.GID.getName(), Integer.class, null);
		this.statusTable.addContainerProperty(ColumnLabels.LOT_ID.getName(), Integer.class, null);
		this.statusTable.addContainerProperty(messageSource.getMessage(Message.TRANSACTION_ID), String.class, null);
		this.statusTable.addContainerProperty(messageSource.getMessage(Message.WITHDRAWAL), String.class, null);
		this.statusTable.addContainerProperty(messageSource.getMessage(Message.BALANCE), String.class, null);
		this.statusTable.addContainerProperty(messageSource.getMessage(Message.IMPORT_PROCESSING_STATUS), Label.class, null);

		this.statusTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		this.statusTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(),
				ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager));
		this.statusTable.setColumnHeader(ColumnLabels.GID.getName(), ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager));
		this.statusTable
				.setColumnHeader(ColumnLabels.LOT_ID.getName(), ColumnLabels.LOT_ID.getTermNameFromOntology(this.ontologyDataManager));
		this.statusTable
				.setColumnHeader(messageSource.getMessage(Message.TRANSACTION_ID), messageSource.getMessage(Message.TRANSACTION_ID));
		this.statusTable.setColumnHeader(messageSource.getMessage(Message.WITHDRAWAL), messageSource.getMessage(Message.WITHDRAWAL));
		this.statusTable.setColumnHeader(messageSource.getMessage(Message.BALANCE), messageSource.getMessage(Message.BALANCE));
		this.statusTable.setColumnHeader(messageSource.getMessage(Message.IMPORT_PROCESSING_STATUS),
				messageSource.getMessage(Message.IMPORT_PROCESSING_STATUS));

		this.continueButton = new Button(this.messageSource.getMessage(Message.CONTINUE));
		this.continueButton.setDebugId("continueButton");
		this.continueButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());

	}

	@Override
	public void initializeValues() {
		importStatusMessages.put(Message.SEED_IMPORT_TRANSACTION_ALREADY_COMMITTED_ERROR.toString(),
				messageSource.getMessage(Message.SEED_IMPORT_TRANSACTION_ALREADY_COMMITTED_ERROR));
		importStatusMessages.put(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_RESERVATION_WARNING.toString(),
				messageSource.getMessage(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_RESERVATION_WARNING));
		importStatusMessages.put(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_AVAILABLE_WARNING.toString(),
				messageSource.getMessage(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_AVAILABLE_WARNING));
		importStatusMessages
				.put(Message.SEED_IMPORT_BALANCE_WARNING.toString(), messageSource.getMessage(Message.SEED_IMPORT_BALANCE_WARNING));
		importStatusMessages.put(Message.SEED_IMPORT_LOT_CLOSED.toString(), messageSource.getMessage(Message.SEED_IMPORT_LOT_CLOSED));

		for (ImportedSeedInventory importedSeedInventory : this.importedSeedInventories) {

			final Item newItem = this.statusTable.addItem(importedSeedInventory);

			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(importedSeedInventory.getEntry());
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(importedSeedInventory.getDesignation());
			newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(importedSeedInventory.getGid());
			newItem.getItemProperty(ColumnLabels.LOT_ID.getName()).setValue(importedSeedInventory.getLotID());
			newItem.getItemProperty(messageSource.getMessage(Message.TRANSACTION_ID)).setValue(importedSeedInventory.getTransactionId());
			newItem.getItemProperty(messageSource.getMessage(Message.WITHDRAWAL)).setValue(importedSeedInventory.getWithdrawalAmount());
			newItem.getItemProperty(messageSource.getMessage(Message.BALANCE)).setValue(importedSeedInventory.getBalanceAmount());

			Label processingStatusLabel = null;
			final String processingStatus = importedSeedInventory.getTransactionProcessingStatus();

			if (processingStatus == null) {
				processingStatusLabel = new Label(messageSource.getMessage(Message.SEED_IMPORT_PROCESSING_STATUS));
				processingStatusLabel.setDebugId("label");
			} else if (importedSeedInventory.getTransactionProcessingStatus()
					.equals(Message.SEED_IMPORT_TRANSACTION_ALREADY_COMMITTED_ERROR.toString()) ||
					importedSeedInventory.getTransactionProcessingStatus()
							.equals(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_AVAILABLE_WARNING.toString()) || importedSeedInventory
					.getTransactionProcessingStatus().equals(Message.SEED_IMPORT_BALANCE_WARNING.toString()) || importedSeedInventory
					.getTransactionProcessingStatus().equals(Message.SEED_IMPORT_LOT_CLOSED.toString())) {
				processingStatusLabel = new Label(messageSource.getMessage(Message.SEED_IMPORT_SKIPPING_STATUS));
				processingStatusLabel.setDescription(importStatusMessages.get(processingStatus));
				processingStatusLabel.setDebugId("label");

			} else if (importedSeedInventory.getTransactionProcessingStatus()
					.equals(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_RESERVATION_WARNING.toString())) {
				processingStatusLabel = new Label(messageSource.getMessage(Message.SEED_IMPORT_CONTINUING_STATUS));
				processingStatusLabel.setDescription(importStatusMessages.get(processingStatus));
				processingStatusLabel.setDebugId("label");
			}

			newItem.getItemProperty(messageSource.getMessage(Message.IMPORT_PROCESSING_STATUS)).setValue(processingStatusLabel);
		}
	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 2688256898854358066L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				SeedInventoryImportStatusWindow.this.cancelAction();
			}
		});

		this.continueButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -941792327552845606L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				/*
				* Synchronizing on SeedInventoryImportStatusWindow will only allow one instance of it to do importing.
				* This way concurrent users can not commit same transaction again with different quantity.
				*/
				synchronized (SeedInventoryImportStatusWindow.class) {
					SeedInventoryImportStatusWindow.this.continueAction();
				}

			}
		});

	}

	public void continueAction() {
		if (!this.processedTransactions.isEmpty()) {
			boolean flagCommittedStatus = checkAnyTransactionsAlreadyCommitted();

			if (flagCommittedStatus) {
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR),
						messageSource.getMessage(Message.SEED_IMPORT_TRANSACTION_ALREADY_COMMITTED_ERROR));
				this.close();
			} else {
				inventoryDataManager.addTransactions(this.processedTransactions);

				if (this.listComponent instanceof ListComponent) {
					((ListComponent) this.listComponent).resetListDataTableValues();
					((ListComponent) this.listComponent).resetListInventoryTableValues();
				}

			  	if (this.listComponent instanceof ListBuilderComponent) {
					((ListBuilderComponent) this.listComponent).resetListInventoryTableValues();
					((ListBuilderComponent) this.listComponent).resetListDataTableValues();
			  	}

				MessageNotifier.showMessage(this.source.getWindow(), messageSource.getMessage(Message.SUCCESS),
						messageSource.getMessage(Message.SEED_IMPORT_SUCCESS));
				this.close();
			}
		}

	}

	private boolean checkAnyTransactionsAlreadyCommitted() {
		List<Integer> transactionIdList = Lists.newArrayList();
		for (Transaction transaction : this.processedTransactions) {
			transactionIdList.add(transaction.getId());
		}

		List<Transaction> importedTransactions = inventoryDataManager.getTransactionsByIdList(transactionIdList);

		for (Transaction transaction : importedTransactions) {
			if (transaction.getStatus() == COMMITTED_STATUS) {
				return true;
			}
		}
		return false;
	}

	public void cancelAction() {
		MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR),
				messageSource.getMessage(Message.SEED_IMPORT_CANCEL));
		this.close();
	}

	@Override
	public void layoutComponents() {

		this.setHeight("310px");
		this.setWidth("880px");

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("seedInventoryImportWarningLayout");
		this.mainLayout.setSpacing(true);

		final Label forSpaceLabel = new Label();
		forSpaceLabel.setDebugId("forSpaceLabel");
		this.mainLayout.addComponent(forSpaceLabel);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.continueButton);

		this.mainLayout.addComponent(this.statusTable);
		this.mainLayout.addComponent(buttonLayout);
		this.mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void updateLabels() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public Table getTable() {
		return this.statusTable;
	}

	public void setImportedSeedInventories(List<ImportedSeedInventory> importedSeedInventories) {
		this.importedSeedInventories = importedSeedInventories;
	}

	public Map<String, String> getImportStatusMessages() {
		return importStatusMessages;
	}

	public void setProcessedTransactions(List<Transaction> processedTransactions) {
		this.processedTransactions = processedTransactions;
	}

	public Button getContinueButton() {
		return continueButton;
	}

	public void setSource(Component source) {
		this.source = source;
	}

	public void setListComponent(Component listComponent) {
		this.listComponent = listComponent;
	}

	public Button getCancelButton() {
		return cancelButton;
	}
}

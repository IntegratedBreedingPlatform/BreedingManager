package org.generationcp.breeding.manager.inventory;

import com.google.common.collect.Lists;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import org.apache.commons.io.FilenameUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.UploadField;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryImportException;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventory;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventoryList;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.InvalidFileDataException;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Configurable
public class SeedInventoryImportFileComponent extends BaseSubWindow implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(SeedInventoryImportFileComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	protected InventoryDataManager inventoryDataManager;

	private VerticalLayout mainLayout;

	private HorizontalLayout importSeedTitleLayout;
	private Button cancelButton;
	private Button finishButton;

	private Label selectFileLabel;

	private UploadField uploadSeedPreparationComponent;
	private SeedInventoryListUploader seedInventoryListUploader;

	private SeedInventoryImportStatusWindow seedInventoryImportStatusWindow;

	private List<Transaction> importedTransactions = Lists.newArrayList();

	private List<Transaction> processedTransactions = Lists.newArrayList();

	private Map<Integer, LotDetails> mapLotDetails = new HashMap<>();
	protected GermplasmList selectedGermplsmList;
	private Component source;
	private Component listComponent;

	private final Set<String> extensionSet = new HashSet<>();

	private static final String ERROR_IMPORTING = "Error importing ";
	private static final String ERROR = "Error";

	private static final String WARNING = "Warning";

	private List<GermplasmListData> selectedListReservedInventoryDetails;
	private ImportedSeedInventoryList importedSeedInventoryList;

	public SeedInventoryImportFileComponent() {
		super();
	}

	public SeedInventoryImportFileComponent(final Component source, Component listComponent, GermplasmList selectedGermplsmList) {
		this();
		this.listComponent = listComponent;
		this.source = source;
		this.selectedGermplsmList = selectedGermplsmList;
	}

	@Override
	public void instantiateComponents() {

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("importListMainLayout");
		this.mainLayout.setSpacing(true);

		this.importSeedTitleLayout = new HorizontalLayout();
		this.importSeedTitleLayout.setDebugId("importSeedTitleLayout");
		this.importSeedTitleLayout.setSpacing(true);

		this.selectFileLabel = new Label(this.messageSource.getMessage(Message.SELECT_SEED_INVENTORY_FILE) + "&nbsp");
		this.selectFileLabel.setDebugId("selectFileLabel");
		this.selectFileLabel.setContentMode(Label.CONTENT_XHTML);

		this.uploadSeedPreparationComponent = new UploadField() {

			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFinished(final Upload.FinishedEvent event) {
				super.uploadFinished(event);
				SeedInventoryImportFileComponent.this.finishButton.setEnabled(true);
			}
		};
		this.uploadSeedPreparationComponent.discard();

		this.uploadSeedPreparationComponent.setButtonCaption(this.messageSource.getMessage(Message.UPLOAD));
		this.uploadSeedPreparationComponent.setNoFileSelectedText(this.messageSource.getMessage("NO_FILE_SELECTED"));
		this.uploadSeedPreparationComponent.setSelectedFileText(this.messageSource.getMessage("SELECTED_IMPORT_FILE"));
		this.uploadSeedPreparationComponent.setDeleteCaption(this.messageSource.getMessage("CLEAR"));
		this.uploadSeedPreparationComponent.setFieldType(UploadField.FieldType.FILE);
		this.uploadSeedPreparationComponent.setButtonCaption("Browse");

		this.uploadSeedPreparationComponent.getRootLayout().setWidth("100%");
		this.uploadSeedPreparationComponent.getRootLayout().setStyleName("bms-upload-container");

		this.seedInventoryListUploader = new SeedInventoryListUploader();

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");

		this.finishButton = new Button(this.messageSource.getMessage(Message.FINISH));
		this.finishButton.setDebugId("finishButton");
		this.finishButton.setEnabled(false);
		this.finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		this.extensionSet.add("xls");
		this.extensionSet.add("xlsx");

		final List<GermplasmListData> inventoryDetails =
				this.inventoryDataManager.getReservedLotDetailsForExportList(this.selectedGermplsmList.getId());

		this.selectedListReservedInventoryDetails = inventoryDetails;

		this.mapLotDetails = ListCommonActionsUtil.createLotDetailsMap(inventoryDetails);

	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8787686200326172252L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				SeedInventoryImportFileComponent.this.cancelButtonAction();
			}

		});

		this.finishButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8787686200326172252L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				SeedInventoryImportFileComponent.this.finishButtonClickListener();
			}
		});

		this.uploadSeedPreparationComponent.setDeleteButtonListener(new Button.ClickListener() {

			private static final long serialVersionUID = -1357425494204377238L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				SeedInventoryImportFileComponent.this.finishButton.setEnabled(false);
			}
		});
		this.uploadSeedPreparationComponent.setFileFactory(this.seedInventoryListUploader);
	}

	@Override
	public void layoutComponents() {

		this.setCaption(this.messageSource.getMessage(Message.IMPORT_SEED_LIST));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("225px");
		this.setWidth("450px");

		final HorizontalLayout downloadMessageLayout = new HorizontalLayout();
		downloadMessageLayout.setDebugId("downloadMessageLayout");
		downloadMessageLayout.addComponent(this.selectFileLabel);
		downloadMessageLayout.addComponent(new Label(this.messageSource.getMessage(Message.PERIOD)));
		this.mainLayout.addComponent(downloadMessageLayout);

		this.mainLayout.addComponent(this.uploadSeedPreparationComponent);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("40px");
		buttonLayout.setSpacing(true);

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.finishButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.finishButton, Alignment.BOTTOM_LEFT);

		this.mainLayout.addComponent(buttonLayout);

		this.addComponent(mainLayout);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void updateLabels() {

	}

	protected void cancelButtonAction() {
		this.getParent().removeWindow(this);
	}

	public void finishButtonClickListener() {

		final String extension = FilenameUtils.getExtension(this.seedInventoryListUploader.getOriginalFilename()).toLowerCase();
		if (!this.extensionSet.contains(extension)) {
			MessageNotifier.showError(this.getWindow(), ERROR, this.messageSource.getMessage(Message.SEED_INVALID_FILE_EXTENSION_ERROR));
			return;
		}

		try {
			this.seedInventoryListUploader.doParseWorkbook();
			importedSeedInventoryList = this.seedInventoryListUploader.getImportedSeedInventoryList();

			validateImportedSeedInventoryList();
			processImportedInventoryTransactions();

			SeedInventoryImportStatusWindow seedInventoryImportStatusWindow =
					new SeedInventoryImportStatusWindow(this.source, this.listComponent,
							this.importedSeedInventoryList.getImportedSeedInventoryList(), this.processedTransactions);
			seedInventoryImportStatusWindow.setDebugId("seedInventoryImportStatusWindow");
			this.source.getWindow().addWindow(seedInventoryImportStatusWindow);

			this.close();

		} catch (final SeedInventoryImportException e) {
			SeedInventoryImportFileComponent.LOG.debug(ERROR_IMPORTING + e.getMessage(), e);
			MessageNotifier.showError(this.getWindow(), e.getCaption(), e.getMessage());
		} catch (final FileParsingException e) {
			SeedInventoryImportFileComponent.LOG.debug(ERROR_IMPORTING + e.getMessage(), e);
			final String message = this.messageSource.getMessage(e.getMessage(), e.getMessageParameters(), Locale.getDefault());
			MessageNotifier.showError(this.getWindow(), ERROR, message);
		} catch (final InvalidFileDataException e) {
			SeedInventoryImportFileComponent.LOG.debug(ERROR_IMPORTING + e.getMessage(), e);
			final String message = this.messageSource.getMessage(e.getMessage(), e.getMessageParameters(), Locale.getDefault());
			MessageNotifier.showError(this.getWindow(), ERROR, message);
			this.finishButton.setEnabled(false);
		}
	}

	protected void validateImportedSeedInventoryList() throws InvalidFileDataException, SeedInventoryImportException {
		List<Integer> importerTransactionsIdList = Lists.newArrayList();
		Boolean isValidWithdrawalAmountForAllReservations = false;

		if (this.selectedGermplsmList == null) {
			final String currentListEmptyError = this.messageSource.getMessage(Message.SEED_IMPORT_SELECTED_LIST_EMPTY_ERROR);
			throw new SeedInventoryImportException(currentListEmptyError);
		}

		if (this.selectedListReservedInventoryDetails.isEmpty()) {
			final String currentListNoReservationsError = this.messageSource.getMessage(Message.SEED_IMPORT_SELECTED_LIST_NO_RESERVATIONS);
			throw new SeedInventoryImportException(currentListNoReservationsError);
		}
		Map<Integer, String> mapTransactionComment = new HashMap<>();

		if (!CollectionUtils.isEmpty(this.importedSeedInventoryList.getImportedSeedInventoryList())) {
			// List name validation
			if (!this.importedSeedInventoryList.getListName().equals(this.selectedGermplsmList.getName())) {
				throw new InvalidFileDataException(Message.SEED_IMPORT_LIST_NAME_MISMATCH_ERROR.toString());
			}

			for (ImportedSeedInventory importedSeedInventory : this.importedSeedInventoryList.getImportedSeedInventoryList()) {
				Integer importEntryNo = importedSeedInventory.getEntry();
				String importedDesignation = importedSeedInventory.getDesignation();
				Integer importedGid = importedSeedInventory.getGid();
				Double importedWithdrawalAmount = importedSeedInventory.getWithdrawalAmount();
				Double importedBalanceAmount = importedSeedInventory.getBalanceAmount();
				Integer transactionID = importedSeedInventory.getTransactionId();
				String transactionComment = importedSeedInventory.getComments();

				if(importedBalanceAmount == null){
					if(importedWithdrawalAmount != null && importedWithdrawalAmount != 0){
						isValidWithdrawalAmountForAllReservations=true;
					}
				}else{
					isValidWithdrawalAmountForAllReservations = true;
				}

				boolean entryNoMatch = false;
				GermplasmListData matchedGermplsmListData = null;

				for (GermplasmListData germplasmListData : selectedListReservedInventoryDetails) {
					if (germplasmListData.getEntryId().equals(importEntryNo)) {
						entryNoMatch = true;
						matchedGermplsmListData = germplasmListData;
						break;
					}
				}

				if (entryNoMatch) {
					// designation match validation
					if (!matchedGermplsmListData.getDesignation().equals(importedDesignation)) {
						throw new InvalidFileDataException(Message.SEED_IMPORT_DESIGNATION_MATCH_ERROR.toString());
					}

					// gid match validation
					if (!matchedGermplsmListData.getGid().equals(importedGid)) {
						throw new InvalidFileDataException(Message.SEED_IMPORT_GID_MATCH_ERROR.toString());
					}

				} else {
					throw new InvalidFileDataException(Message.SEED_IMPORT_ENTRY_MATCH_ERROR.toString());
				}

				// validation of Either withdrawal or balance amount should be present
				if (importedWithdrawalAmount != null && importedBalanceAmount != null) {
					throw new InvalidFileDataException(Message.SEED_IMPORT_WITHDRAWAL_BALANCE_BOTH_ERROR.toString());
				}

				if (transactionID == null) {
					throw new InvalidFileDataException(Message.SEED_IMPORT_TRANSACTION_ID_ERROR.toString());
				}
				importerTransactionsIdList.add(transactionID);
				mapTransactionComment.put(transactionID, transactionComment);
			}

			if(!isValidWithdrawalAmountForAllReservations){
				throw new InvalidFileDataException(Message.SEED_IMPORT_WITHDRAWAL_AMOUNT_EMPTY_ERROR.toString());
			}

			importedTransactions = inventoryDataManager.getTransactionsByIdList(importerTransactionsIdList);
			Map<Integer, Transaction> transactionMap = createTransactionIdWiseMap(importedTransactions);

			for (Map.Entry<Integer, String> entry : mapTransactionComment.entrySet()) {
				Integer transactionId = entry.getKey();
				String comment = entry.getValue();

				Transaction transaction = transactionMap.get(transactionId);

				if (!Objects.equals(comment, transaction.getComments())) {
					String changeCommentsWarningMsg = this.messageSource.getMessage(Message.SEED_IMPORT_COMMENT_WARNING);
					MessageNotifier.showWarning(this.source.getWindow(), WARNING, changeCommentsWarningMsg);
					break;
				}
			}

		} else {
			final String importedListNoEmptyReservationRows =
					this.messageSource.getMessage(Message.SEED_IMPORT_NO_IMPORTED_RESERVATION_ERROR);
			throw new SeedInventoryImportException(importedListNoEmptyReservationRows);
		}

	}

	protected void processImportedInventoryTransactions() {
		Map<Integer, Transaction> transactionMap = createTransactionIdWiseMap(importedTransactions);
		List<Transaction> processedTransactions = Lists.newArrayList();
		for (ImportedSeedInventory importedSeedInventory : this.importedSeedInventoryList.getImportedSeedInventoryList()) {
			Transaction transaction = transactionMap.get(importedSeedInventory.getTransactionId());
			Double amountWithdrawn = importedSeedInventory.getWithdrawalAmount();
			Double balanceAmount = importedSeedInventory.getBalanceAmount();
			String comments = importedSeedInventory.getComments();
			LotDetails lotDetails = mapLotDetails.get(importedSeedInventory.getLotID());

			if (lotDetails == null) {
				//Skip and process next or Cancel import
				importedSeedInventory.setTransactionProcessingStatus(Message.SEED_IMPORT_LOT_CLOSED.toString());
				continue;
			}

			if (transaction.getStatus() == TransactionStatus.COMMITTED.getIntValue()) {
				//Skip and process next or Cancel import
				importedSeedInventory.setTransactionProcessingStatus(Message.SEED_IMPORT_TRANSACTION_ALREADY_COMMITTED_ERROR.toString());
				continue;
			}

			Double availableBalance = lotDetails.getAvailableLotBalance();

			if (amountWithdrawn != null && amountWithdrawn > 0) {
				Double transactionQty = transaction.getQuantity() * -1;

				if (Objects.equals(amountWithdrawn, transactionQty)) { //Actual withdrawal is same as reservation made on lot
					transaction.setStatus(TransactionStatus.COMMITTED.getIntValue());
					transaction.setCommitmentDate(DateUtil.getCurrentDateAsIntegerValue());
					transaction.setComments(comments);
					processedTransactions.add(transaction);
				} else if (amountWithdrawn < transactionQty) { // Actual withdrawal is less than reservation made on lot
					transaction.setStatus(TransactionStatus.COMMITTED.getIntValue());
					transaction.setPreviousAmount(transactionQty);
					Double updatedQty = amountWithdrawn * -1;
					transaction.setQuantity(updatedQty);
					transaction.setCommitmentDate(DateUtil.getCurrentDateAsIntegerValue());
					transaction.setComments(comments);
					processedTransactions.add(transaction);

				} else { // Actual withdrawal is greater than reservation. Need to check if extra reservation can be made or not
					if (amountWithdrawn <= transactionQty + availableBalance) {
						transaction.setStatus(TransactionStatus.COMMITTED.getIntValue());
						transaction.setPreviousAmount(transactionQty);
						Double updatedQty = amountWithdrawn * -1;
						transaction.setQuantity(updatedQty);
						transaction.setCommitmentDate(DateUtil.getCurrentDateAsIntegerValue());
						transaction.setComments(comments);
						processedTransactions.add(transaction);

						//Continue and process with amount withdrawn or Cancel import
						importedSeedInventory
								.setTransactionProcessingStatus(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_RESERVATION_WARNING.toString());

					} else {
						//Skip and process next or Cancel import
						importedSeedInventory
								.setTransactionProcessingStatus(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_AVAILABLE_WARNING.toString());
						continue;

					}
				}

			}

			if (balanceAmount != null && balanceAmount >= 0) {
				Double transactionQty = transaction.getQuantity() * -1;
				if (balanceAmount != null) {
					if (balanceAmount > 0) {
						Double explicitWithdrawalMade = lotDetails.getActualLotBalance() - balanceAmount;

						if (explicitWithdrawalMade <= transactionQty + availableBalance) {
							transaction.setStatus(TransactionStatus.COMMITTED.getIntValue());
							transaction.setPreviousAmount(lotDetails.getActualLotBalance());
							Double updatedQty = explicitWithdrawalMade * -1;
							transaction.setQuantity(updatedQty);
							transaction.setCommitmentDate(DateUtil.getCurrentDateAsIntegerValue());
							String stockAdjustmentComment =
									this.messageSource.getMessage(Message.SEED_IMPORT_STOCK_TAKING_ADJUSTMENT_COMMENT);
							transaction.setComments(stockAdjustmentComment);
							processedTransactions.add(transaction);
						} else {
							//Skip and process next or Cancel import
							importedSeedInventory.setTransactionProcessingStatus(Message.SEED_IMPORT_BALANCE_WARNING.toString());
							continue;

						}

					} else if (balanceAmount == 0) {
						// Discarding actual balance
						transaction.setStatus(TransactionStatus.COMMITTED.getIntValue());
						transaction.setPreviousAmount(transactionQty);
						Double updatedQty = -1 * lotDetails.getActualLotBalance();
						transaction.setQuantity(updatedQty);
						transaction.setCommitmentDate(DateUtil.getCurrentDateAsIntegerValue());
						transaction.setComments(this.messageSource.getMessage(Message.TRANSACTION_DISCARD_COMMENT));

						processedTransactions.add(transaction);
					}
				}
			}

		}

		this.processedTransactions = processedTransactions;
	}

	private Map<Integer, Transaction> createTransactionIdWiseMap(List<Transaction> importedTransactions) {
		Map<Integer, Transaction> mapTransaction = new HashMap<>();

		if (importedTransactions != null && !importedTransactions.isEmpty()) {
			for (Transaction transaction : importedTransactions) {
				mapTransaction.put(transaction.getId(), transaction);
			}
		}

		return mapTransaction;
	}

	public Set<String> getExtensionSet() {
		return extensionSet;
	}

	public List<GermplasmListData> getSelectedListReservedInventoryDetails() {
		return selectedListReservedInventoryDetails;
	}

	public void setSelectedListReservedInventoryDetails(List<GermplasmListData> selectedListReservedInventoryDetails) {
		this.selectedListReservedInventoryDetails = selectedListReservedInventoryDetails;
	}

	public void setImportedSeedInventoryList(ImportedSeedInventoryList importedSeedInventoryList) {
		this.importedSeedInventoryList = importedSeedInventoryList;
	}

	public void setSelectedGermplsmList(GermplasmList selectedGermplsmList) {
		this.selectedGermplsmList = selectedGermplsmList;
	}

	public List<Transaction> getProcessedTransactions() {
		return processedTransactions;
	}

	public void setSource(Component source) {
		this.source = source;
	}

	public VerticalLayout getMainLayout() {
		return mainLayout;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public Button getFinishButton() {
		return finishButton;
	}

	public UploadField getUploadSeedPreparationComponent() {
		return uploadSeedPreparationComponent;
	}

	public SeedInventoryListUploader getSeedInventoryListUploader() {
		return seedInventoryListUploader;
	}
}

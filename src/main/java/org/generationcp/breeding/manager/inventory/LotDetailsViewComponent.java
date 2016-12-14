package org.generationcp.breeding.manager.inventory;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.TableLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.report.TransactionReportRow;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class LotDetailsViewComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	protected OntologyDataManager ontologyDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	private Label lotIdLabel;
	private Label locationLabel;
	private Label location;
	private Label scaleLabel;
	private Label scale;
	private Label creationDateLabel;
	private Label creationDate;
	private Label lotStatusLabel;
	private Label lotStatus;
	private Label actualBalanceLabel;
	private Label actualBalance;
	private Label availableBalanceLabel;
	private Label availableBalance;
	private Label idLabel;

	private Label tableHeader;
	private VerticalLayout panelContentLayout;

	private TableLayout transactionDetailsLayout;
	private Table transactionDetailsTabel;

	private LotDetails lotDetails;

	private List<TransactionReportRow> transactionReportRows;

	public static final String DATE = "DATE";
	public static final String TYPE = "TYPE";
	public static final String AMOUNT = "AMOUNT";
	public static final String SEED_SOURCE = "SEED SOURCE";
	public static final String LIST_NAME = "LIST NAME";
	public static final String USER = "USER";

	public static final String LOT_CLOSED = "Lot closed";
	public static final String LOT_DISCARDED = "Discard";
	public static final String LOT_DEPOSIT = "Deposit";
	public static final String LOT_ACTIVE = "Active";

	public static final String LOT = "Lot ";
	public static final String LOCATION = "Location :";
	public static final String SCALE = "Scale :";
	public static final String CREATION_DATE = "Creation Date :";
	public static final String LOT_STATUS = "Lot Status :";
	public static final String ACTUAL_BALANCE = "Actual Balance :";
	public static final String AVAILABLE_BALANCE = "Available Balance :";

	public LotDetailsViewComponent(LotDetails lot) {
		this.lotDetails = lot;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.layoutComponents();
	}

	@Override
	public void updateLabels() {
		//No Implementation
	}

	@Override
	public void instantiateComponents() {

		this.lotIdLabel = new Label(LotDetailsViewComponent.LOT + lotDetails.getLotId());
		this.lotIdLabel.setDebugId("lotId");
		this.lotIdLabel.setHeight(25f, 0);
		this.lotIdLabel.addStyleName("lotLabel");


		this.locationLabel = new Label(LotDetailsViewComponent.LOCATION);
		this.locationLabel.setDebugId("locationLabel");
		this.locationLabel.addStyleName(AppConstants.CssStyles.BOLD);

		String lotLocation = "";
		if (lotDetails.getLocationOfLot() != null) {
			if (lotDetails.getLocationOfLot().getLname() != null) {
				lotLocation = lotDetails.getLocationOfLot().getLname();
			}
		}

		this.location = new Label(lotLocation);

		this.scaleLabel = new Label(LotDetailsViewComponent.SCALE);
		this.scaleLabel.setDebugId("scaleLabel");
		this.scaleLabel.addStyleName(AppConstants.CssStyles.BOLD);

		String lotScale = "";
		if (lotDetails.getScaleOfLot() != null) {
			if (lotDetails.getScaleOfLot().getName() != null) {
				lotScale = lotDetails.getScaleOfLot().getName();
			}
		}
		this.scale = new Label(lotScale);

		this.creationDateLabel = new Label(LotDetailsViewComponent.CREATION_DATE);
		this.creationDateLabel.setDebugId("creationDate");
		this.creationDateLabel.addStyleName(AppConstants.CssStyles.BOLD);
		this.creationDate = new Label("");

		this.lotStatusLabel = new Label(LotDetailsViewComponent.LOT_STATUS);
		this.lotStatusLabel.setDebugId("lotStatusLabel");
		this.lotStatusLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.lotStatus = new Label(LotDetailsViewComponent.LOT_ACTIVE);

		this.actualBalanceLabel = new Label(LotDetailsViewComponent.ACTUAL_BALANCE);
		this.actualBalanceLabel.setDebugId("actualBalance");
		this.actualBalanceLabel.addStyleName(AppConstants.CssStyles.BOLD);

		String actualBalance = "";
		if (lotDetails.getActualLotBalance() != null) {
			actualBalance = lotDetails.getActualLotBalance() + lotDetails.getLotScaleNameAbbr();
		}
		this.actualBalance = new Label(actualBalance);

		this.availableBalanceLabel = new Label(LotDetailsViewComponent.AVAILABLE_BALANCE);
		this.availableBalanceLabel.setDebugId("availableBalance");
		this.availableBalanceLabel.addStyleName(AppConstants.CssStyles.BOLD);

		String availableBalance = "";
		if (lotDetails.getAvailableLotBalance() != null) {
			availableBalance = lotDetails.getAvailableLotBalance() + lotDetails.getLotScaleNameAbbr();
		}
		this.availableBalance = new Label(availableBalance);

		this.idLabel = new Label(" "+lotDetails.getLotId());
		idLabel.addStyleName(AppConstants.CssStyles.BOLD);
		this.tableHeader = new Label(this.messageSource.getMessage(Message.TRANSACTION_DETAIL_HEADER_TITLE));
		this.tableHeader.setDebugId("tableHeader");

		this.transactionDetailsLayout = new TableLayout(Integer.MAX_VALUE, 0);
		this.transactionDetailsLayout.setDebugId("transactionDetailsLayout");

		this.transactionDetailsTabel = this.transactionDetailsLayout.getTable();
		initializeLotEntriesTable(this.transactionDetailsTabel);

	}

	protected void initializeLotEntriesTable(final Table table) {
		if (table != null) {
			table.setWidth("100%");

			table.addContainerProperty(LotDetailsViewComponent.DATE, String.class, null);
			table.addContainerProperty(LotDetailsViewComponent.TYPE, String.class, null);
			table.addContainerProperty(LotDetailsViewComponent.AMOUNT, String.class, null);
			table.addContainerProperty(LotDetailsViewComponent.SEED_SOURCE, String.class, null);
			table.addContainerProperty(LotDetailsViewComponent.LIST_NAME, String.class, null);
			table.addContainerProperty(LotDetailsViewComponent.USER, String.class, null);

			table.setColumnHeader(LotDetailsViewComponent.DATE, LotDetailsViewComponent.DATE);
			table.setColumnHeader(LotDetailsViewComponent.TYPE, LotDetailsViewComponent.TYPE);
			table.setColumnHeader(LotDetailsViewComponent.AMOUNT, LotDetailsViewComponent.AMOUNT);
			table.setColumnHeader(LotDetailsViewComponent.SEED_SOURCE,LotDetailsViewComponent.SEED_SOURCE);
			table.setColumnHeader(LotDetailsViewComponent.LIST_NAME, LotDetailsViewComponent.LIST_NAME);
			table.setColumnHeader(LotDetailsViewComponent.USER, LotDetailsViewComponent.USER);

		}

	}

	@Override
	public void initializeValues() {
		this.transactionReportRows = this.inventoryDataManager.getTransactionDetailsForLot(lotDetails.getLotId());

		for (TransactionReportRow transaction : this.transactionReportRows) {
			this.addTransactionDetails(transaction);
		}
	}

	public void addTransactionDetails(TransactionReportRow transaction){

		Item newItem = this.transactionDetailsTabel.addItem(transaction);

		newItem.getItemProperty(LotDetailsViewComponent.DATE).setValue(transaction.getDate());
		newItem.getItemProperty(LotDetailsViewComponent.TYPE).setValue(transaction.getLotStatus());
		newItem.getItemProperty(LotDetailsViewComponent.AMOUNT).setValue(transaction.getQuantity() + lotDetails.getLotScaleNameAbbr());
		newItem.getItemProperty(LotDetailsViewComponent.SEED_SOURCE).setValue("");
		newItem.getItemProperty(LotDetailsViewComponent.LIST_NAME).setValue(transaction.getListName());
		newItem.getItemProperty(LotDetailsViewComponent.USER).setValue(transaction.getUser());

		if (LotDetailsViewComponent.LOT_DEPOSIT.equals(transaction.getLotStatus())) {
			this.creationDate.setValue("" + transaction.getDate());
		}

		if (LotDetailsViewComponent.LOT_CLOSED.equals(transaction.getLotStatus()) || LotDetailsViewComponent.LOT_DISCARDED
				.equals(transaction.getLotStatus())) {
			this.lotStatus.setValue(transaction.getLotStatus() + " On " + transaction.getDate());
		}

	}

	@Override
	public void addListeners() {
		//No Implementation
	}

	@Override
	public void layoutComponents() {
		this.addStyleName("lot-border");
		this.addComponent(this.lotIdLabel);
		this.panelContentLayout = new VerticalLayout();
		this.panelContentLayout.setDebugId("panelContentLayout");
		this.panelContentLayout.setSpacing(true);
		this.panelContentLayout.setMargin(false,false,true,false);


		final HorizontalLayout locationAndScale = new HorizontalLayout();
		locationAndScale.setWidth(650f, 0);
		locationAndScale.setSpacing(true);
		locationAndScale.addComponent(this.locationLabel);
		locationAndScale.addComponent(this.location);
		locationAndScale.addComponent(this.scaleLabel);
		locationAndScale.addComponent(this.scale);
		locationAndScale.setComponentAlignment(this.locationLabel, Alignment.TOP_LEFT);
		locationAndScale.setComponentAlignment(this.location, Alignment.TOP_LEFT);
		locationAndScale.setComponentAlignment(this.scaleLabel, Alignment.TOP_LEFT);
		locationAndScale.setComponentAlignment(this.scale, Alignment.TOP_LEFT);

		final HorizontalLayout dateAndStatus = new HorizontalLayout();
		dateAndStatus.setWidth(650f, 0);
		dateAndStatus.setSpacing(true);
		dateAndStatus.addComponent(this.creationDateLabel);
		dateAndStatus.addComponent(this.creationDate);
		dateAndStatus.addComponent(this.lotStatusLabel);
		dateAndStatus.addComponent(this.lotStatus);
		dateAndStatus.setComponentAlignment(this.creationDateLabel, Alignment.TOP_LEFT);
		dateAndStatus.setComponentAlignment(this.creationDate, Alignment.TOP_LEFT);
		dateAndStatus.setComponentAlignment(this.lotStatusLabel, Alignment.TOP_LEFT);
		dateAndStatus.setComponentAlignment(this.lotStatus, Alignment.TOP_LEFT);

		final HorizontalLayout actualAndAvailableBalance = new HorizontalLayout();
		actualAndAvailableBalance.setWidth(650f, 0);
		actualAndAvailableBalance.setHeight("80%");
		actualAndAvailableBalance.setSpacing(true);
		actualAndAvailableBalance.addComponent(this.actualBalanceLabel);
		actualAndAvailableBalance.addComponent(this.actualBalance);
		actualAndAvailableBalance.addComponent(this.availableBalanceLabel);
		actualAndAvailableBalance.addComponent(this.availableBalance);
		actualAndAvailableBalance.setComponentAlignment(this.actualBalanceLabel, Alignment.TOP_LEFT);
		actualAndAvailableBalance.setComponentAlignment(this.actualBalance, Alignment.TOP_LEFT);
		actualAndAvailableBalance.setComponentAlignment(this.availableBalanceLabel, Alignment.TOP_LEFT);
		actualAndAvailableBalance.setComponentAlignment(this.availableBalance, Alignment.TOP_LEFT);

		final HorizontalLayout tableHeaderLabel = new HorizontalLayout();
		tableHeaderLabel.setWidth(650f, 0);
		tableHeaderLabel.setSpacing(true);
		tableHeaderLabel.addComponent(this.tableHeader);
		tableHeaderLabel.addComponent(this.idLabel);
		tableHeaderLabel.setComponentAlignment(this.tableHeader, Alignment.BOTTOM_LEFT);
		tableHeaderLabel.setComponentAlignment(this.idLabel, Alignment.BOTTOM_LEFT);

		this.panelContentLayout.addComponent(locationAndScale);
		this.panelContentLayout.addComponent(dateAndStatus);
		this.panelContentLayout.addComponent(actualAndAvailableBalance);
		this.panelContentLayout.addComponent(tableHeaderLabel);
		this.panelContentLayout.addComponent(this.transactionDetailsTabel);

		this.addComponent(this.panelContentLayout);
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	public Table getTable() {
		return this.transactionDetailsTabel;
	}
}

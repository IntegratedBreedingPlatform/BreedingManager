
package org.generationcp.breeding.manager.inventory;

import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ReservationStatusWindow extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = -8587129181683284005L;
	private static final Logger LOG = LoggerFactory.getLogger(ReservationStatusWindow.class);

	private VerticalLayout mainLayout;

	private Label statusDescriptionLabel;
	private Table statusTable;
	private Button okButton;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private Map<ListEntryLotDetails, Double> invalidLotReservations;

	public ReservationStatusWindow(final Map<ListEntryLotDetails, Double> invalidLotReservations) {
		super();
		this.invalidLotReservations = invalidLotReservations;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		// window formatting
		this.setCaption(this.messageSource.getMessage(Message.RESERVATION_STATUS));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);

		this.statusDescriptionLabel = new Label(this.messageSource.getMessage(Message.RESERVATION_STATUS_TABLE_CAPTION));

		this.statusTable = new Table();
		this.statusTable.setDebugId("statusTable");
		this.statusTable.setWidth("100%");
		this.statusTable.setHeight("150px");
		this.statusTable.setImmediate(true);

		this.statusTable.addContainerProperty(this.messageSource.getMessage(Message.LOT_ID), Integer.class, null);
		this.statusTable.addContainerProperty(this.messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER), String.class, null);
		this.statusTable.addContainerProperty(this.messageSource.getMessage(Message.LOCATION_HEADER), String.class, null);
		this.statusTable.addContainerProperty(this.messageSource.getMessage(Message.UNITS), String.class, null);
		this.statusTable.addContainerProperty(this.messageSource.getMessage(Message.AVAILABLE_BALANCE), Double.class, null);
		this.statusTable.addContainerProperty(this.messageSource.getMessage(Message.AMOUNT_TO_RESERVE), Double.class, null);
		this.statusTable.addContainerProperty(this.messageSource.getMessage(Message.RESERVATION_STATUS), String.class, null);

		this.messageSource.setColumnHeader(this.statusTable, this.messageSource.getMessage(Message.LOT_ID), Message.LOT_ID);
		this.messageSource.setColumnHeader(this.statusTable, this.messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER),
				Message.LISTDATA_DESIGNATION_HEADER);
		this.messageSource.setColumnHeader(this.statusTable, this.messageSource.getMessage(Message.LOCATION_HEADER),
				Message.LOCATION_HEADER);
		this.messageSource.setColumnHeader(this.statusTable, this.messageSource.getMessage(Message.UNITS), Message.UNITS);
		this.messageSource.setColumnHeader(this.statusTable, this.messageSource.getMessage(Message.AVAILABLE_BALANCE),
				Message.AVAILABLE_BALANCE);
		this.messageSource.setColumnHeader(this.statusTable, this.messageSource.getMessage(Message.AMOUNT_TO_RESERVE),
				Message.AMOUNT_TO_RESERVE);
		this.messageSource.setColumnHeader(this.statusTable, this.messageSource.getMessage(Message.RESERVATION_STATUS),
				Message.RESERVATION_STATUS);


		this.okButton = new Button(this.messageSource.getMessage(Message.OK));
		this.okButton.setDebugId("okButton");
		this.okButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {

		for (final Map.Entry<ListEntryLotDetails, Double> entry : this.invalidLotReservations.entrySet()) {
			final ListEntryLotDetails lot = entry.getKey();
			final Double amountToReserve = entry.getValue();

			final Item newItem = this.statusTable.addItem(lot);
			final String designation = this.getDesignation(lot.getEntityIdOfLot());
			newItem.getItemProperty(this.messageSource.getMessage(Message.LOT_ID)).setValue(lot.getLotId());
			newItem.getItemProperty(this.messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER)).setValue(designation);
			newItem.getItemProperty(this.messageSource.getMessage(Message.LOCATION_HEADER)).setValue(lot.getLocationOfLot().getLname());
			newItem.getItemProperty(this.messageSource.getMessage(Message.UNITS)).setValue(lot.getScaleOfLot().getName());
			newItem.getItemProperty(this.messageSource.getMessage(Message.AVAILABLE_BALANCE)).setValue(lot.getAvailableLotBalance());
			newItem.getItemProperty(this.messageSource.getMessage(Message.AMOUNT_TO_RESERVE)).setValue(amountToReserve);
			newItem.getItemProperty(this.messageSource.getMessage(Message.RESERVATION_STATUS)).setValue(lot.getWithdrawalStatus());
		}
	}

	@Override
	public void addListeners() {
		this.okButton.addListener(new CloseWindowAction());
	}

	@Override
	public void layoutComponents() {
		// main window
		this.setHeight("310px");
		this.setWidth("780px");

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("reservationStatMainLayout");
		this.mainLayout.setSpacing(true);

		this.mainLayout.addComponent(this.statusDescriptionLabel);
		this.mainLayout.addComponent(this.statusTable);
		this.mainLayout.addComponent(this.okButton);

		this.mainLayout.setComponentAlignment(this.okButton, Alignment.BOTTOM_CENTER);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	private String getDesignation(final Integer entityIdOfLot) {
		Name designation = null;

		try {
			designation = this.germplasmDataManager.getPreferredNameByGID(entityIdOfLot);
		} catch (final MiddlewareQueryException e) {
			ReservationStatusWindow.LOG.error(e.getMessage(), e);
		}

		return designation.getNval();
	}

	public Table getStatusTable() {
		return statusTable;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setInvalidLotReservations(Map<ListEntryLotDetails, Double> invalidLotReservations) {
		this.invalidLotReservations = invalidLotReservations;
	}

	public VerticalLayout getMainLayout() {
		return mainLayout;
	}
}

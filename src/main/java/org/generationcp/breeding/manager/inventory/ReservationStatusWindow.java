package org.generationcp.breeding.manager.inventory;

import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ReservationStatusWindow extends Window implements InitializingBean, 
							InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = -8587129181683284005L;
	
	private VerticalLayout mainLayout;
	
	private Label statusDescriptionLabel;
	private Table statusTable;
	private Button okButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	private Map<ListEntryLotDetails,Double> invalidLotReservations;
	
	public ReservationStatusWindow(Map<ListEntryLotDetails,Double> invalidLotReservations) {
		super();
		this.invalidLotReservations = invalidLotReservations;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		//window formatting
		this.setCaption(messageSource.getMessage(Message.RESERVATION_STATUS));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		
		statusDescriptionLabel = new Label("All selected entries will be reserved except for the following because of insufficient balance:");
		
		statusTable = new Table();
		statusTable.setWidth("100%");
		statusTable.setHeight("150px");
		statusTable.setImmediate(true);
		
		statusTable.addContainerProperty(messageSource.getMessage(Message.LOT_ID), Integer.class, null);
		statusTable.addContainerProperty(messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER), String.class, null);
		statusTable.addContainerProperty(messageSource.getMessage(Message.LOCATION_HEADER), String.class, null);
		statusTable.addContainerProperty(messageSource.getMessage(Message.UNITS), String.class, null);
		statusTable.addContainerProperty(messageSource.getMessage(Message.AVAILABLE_BALANCE), Double.class, null);
		statusTable.addContainerProperty(messageSource.getMessage(Message.AMOUNT_TO_RESERVE), Double.class, null);
		
		messageSource.setColumnHeader(statusTable, messageSource.getMessage(Message.LOT_ID), Message.LOT_ID);
		messageSource.setColumnHeader(statusTable, messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER), Message.LISTDATA_DESIGNATION_HEADER);
		messageSource.setColumnHeader(statusTable, messageSource.getMessage(Message.LOCATION_HEADER), Message.LOCATION_HEADER);
		messageSource.setColumnHeader(statusTable, messageSource.getMessage(Message.UNITS), Message.UNITS);
		messageSource.setColumnHeader(statusTable, messageSource.getMessage(Message.AVAILABLE_BALANCE), Message.AVAILABLE_BALANCE);
		messageSource.setColumnHeader(statusTable, messageSource.getMessage(Message.AMOUNT_TO_RESERVE), Message.AMOUNT_TO_RESERVE);
		
		okButton = new Button(messageSource.getMessage(Message.OK));
		okButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {

		for(Map.Entry<ListEntryLotDetails,Double> entry  : invalidLotReservations.entrySet()){
			ListEntryLotDetails lot = entry.getKey();
			Double amountToReserve = entry.getValue();
			
			Item newItem = statusTable.addItem(lot);
			String designation = getDesignation(lot.getEntityIdOfLot());
			newItem.getItemProperty(messageSource.getMessage(Message.LOT_ID)).setValue(lot.getLotId());
			newItem.getItemProperty(messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER)).setValue(designation);
			newItem.getItemProperty(messageSource.getMessage(Message.LOCATION_HEADER)).setValue(lot.getLocationOfLot().getLname());
			newItem.getItemProperty(messageSource.getMessage(Message.UNITS)).setValue(lot.getScaleOfLot().getName());
			newItem.getItemProperty(messageSource.getMessage(Message.AVAILABLE_BALANCE)).setValue(lot.getAvailableLotBalance());
			newItem.getItemProperty(messageSource.getMessage(Message.AMOUNT_TO_RESERVE)).setValue(amountToReserve);
		}
	}

	@Override
	public void addListeners() {
		okButton.addListener(new CloseWindowAction());
	}

	@Override
	public void layoutComponents() {
		//main window
		setHeight("310px");
		setWidth("780px");
		
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		mainLayout.addComponent(statusDescriptionLabel);
		mainLayout.addComponent(statusTable);
		mainLayout.addComponent(okButton);
		
		mainLayout.setComponentAlignment(okButton, Alignment.BOTTOM_CENTER);
		
		addComponent(mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	private String getDesignation(Integer entityIdOfLot) {
		Name designation = null;
		
		try {
			designation = germplasmDataManager.getPreferredNameByGID(entityIdOfLot);
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return designation.getNval();
	}
}

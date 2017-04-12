
package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.ControllableRefreshTable;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.Listener.LotDetailsButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

/**
 * This table is used for displaying lots in Inventory view. It is mainly used in List Manager and Crossing Manager.
 */
@Configurable
public class ListInventoryTable extends TableWithSelectAllLayout implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(ListInventoryTable.class);

	private static final long serialVersionUID = 1L;

	protected ControllableRefreshTable listInventoryTable;
	
	protected Integer listId;

	@Autowired
	GermplasmListManager germplasmListManager;

	@Autowired
	GermplasmDataManager germplasmDataManager;

	@Autowired
	protected InventoryDataManager inventoryDataManager;

	@Autowired
	protected OntologyDataManager ontologyDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	protected PedigreeService pedigreeService;

	@Resource
	protected CrossExpansionProperties crossExpansionProperties;

	public ListInventoryTable(final Integer listId) {
		super(ColumnLabels.TAG.getName());
		this.listId = listId;
	}

	@Override
	public void instantiateComponents() {

		super.instantiateComponents();

		this.listInventoryTable = this.getTable();
		this.listInventoryTable.setMultiSelect(true);
		this.listInventoryTable.setImmediate(true);
		this.listInventoryTable.setPageLength(100);

		this.listInventoryTable.setHeight("480px");
		this.listInventoryTable.setWidth("100%");
		this.listInventoryTable.setColumnCollapsingAllowed(true);
		this.listInventoryTable.setColumnReorderingAllowed(false);
		this.listInventoryTable.setSelectable(true);
		this.listInventoryTable.setMultiSelect(true);

		this.listInventoryTable.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.LOT_LOCATION.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.TOTAL.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.RESERVATION.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.COMMENT.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.LOT_ID.getName(), Button.class, null);

		this.listInventoryTable.setColumnHeader(ColumnLabels.TAG.getName(), this.messageSource.getMessage(Message.CHECK_ICON));
		this.listInventoryTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		this.listInventoryTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(),
				ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.LOT_LOCATION.getName(),
				ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.TOTAL.getName(),
				ColumnLabels.TOTAL.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.RESERVATION.getName(),
				ColumnLabels.RESERVATION.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.SEED_RESERVATION.getName(),
				ColumnLabels.SEED_RESERVATION.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.COMMENT.getName(),
				ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.STOCKID.getName(),
				ColumnLabels.STOCKID.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(),
				ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.LOT_ID.getName(),
				ColumnLabels.LOT_ID.getTermNameFromOntology(this.ontologyDataManager));
	}

	public void loadInventoryData() {
		if (this.listId != null) {
			final List<GermplasmListData> inventoryDetails =
					this.inventoryDataManager.getLotDetailsForList(this.listId, 0, Integer.MAX_VALUE);
			this.displayInventoryDetails(inventoryDetails);
		}

	}

	public void displayInventoryDetails(final List<GermplasmListData> inventoryDetails) {

		this.listInventoryTable.removeAllItems();
		
		// disable the Vaadin table from listening to update events until the whole table is filled
		listInventoryTable.disableContentRefreshing();
		final Container listInventoryContainer = this.listInventoryTable.getContainerDataSource();
		
		for (final GermplasmListData inventoryDetail : inventoryDetails) {

			Monitor monitor = MonitorFactory.start("org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable.displayInventoryDetails:processInventoryRow");
			try {
  			final Integer entryId = inventoryDetail.getEntryId();
  			final String designation = inventoryDetail.getDesignation();
  
  			final ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();
  			@SuppressWarnings("unchecked")
  			final List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>) listDataInventory.getLotRows();
  			 
  			if (lotDetails != null) {
  				for (final ListEntryLotDetails lotDetail : lotDetails) {
  					
  					final Item newItem = listInventoryContainer.addItem(lotDetail);
  
  					final CheckBox itemCheckBox = new CheckBox();
  					itemCheckBox.setData(lotDetail);
  					itemCheckBox.setImmediate(true);
  					itemCheckBox.addListener(new ClickListener() {
  
  						private static final long serialVersionUID = 1L;
  
  						@Override
  						public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
  							final CheckBox itemCheckBox = (CheckBox) event.getButton();
  							ListInventoryTable.this.toggleSelectOnLotEntries(itemCheckBox);
  						}
  
  					});
  
  					final Button desigButton =
  							new Button(String.format("%s", designation), new GidLinkButtonClickListener(
  									inventoryDetail.getGid().toString(), true));
  					desigButton.setStyleName(BaseTheme.BUTTON_LINK);
  
  					newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
  					newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);
  					newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
  
  					String lotLocation = "";
  					if (lotDetail.getLocationOfLot() != null && lotDetail.getLocationOfLot().getLname() != null) {
  						lotLocation = lotDetail.getLocationOfLot().getLname();
  					}
  
  					newItem.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).setValue(lotLocation);

					String lotScaleAbbr = "";

					if(lotDetail.getLotScaleNameAbbr() != null) {
						lotScaleAbbr = lotDetail.getLotScaleNameAbbr();
					}


  					StringBuilder available = new StringBuilder("");
					if(lotDetail.getAvailableLotBalance() != null){
						available.append(lotDetail.getAvailableLotBalance());
						available.append(lotScaleAbbr);

					}
  					newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(available.toString());

					StringBuilder reservedBalance = new StringBuilder("");
					if(lotDetail.getReservedTotalForEntry() != null){
						reservedBalance.append(lotDetail.getReservedTotalForEntry());
						reservedBalance.append(lotScaleAbbr);

					}
					newItem.getItemProperty(ColumnLabels.RESERVATION.getName()).setValue(reservedBalance.toString());

					StringBuilder committedBalance = new StringBuilder("");
					if(lotDetail.getCommittedTotalForEntry() != null){
						committedBalance.append(lotDetail.getCommittedTotalForEntry());
						committedBalance.append(lotScaleAbbr);
					}
  					newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(committedBalance);

  					newItem.getItemProperty(ColumnLabels.COMMENT.getName()).setValue(lotDetail.getCommentOfLot());
  
  					final String stockIds = lotDetail.getStockIds();
  					final Label stockIdsLbl = new Label(stockIds);
  					stockIdsLbl.setDescription(stockIds);
  					newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdsLbl);
					final Button lotButton = new Button(lotDetail.getLotId().toString(),
							new LotDetailsButtonClickListener(lotDetail.getEntityIdOfLot(), designation, this.listInventoryTable,
									lotDetail.getLotId()));
					lotButton.setStyleName(BaseTheme.BUTTON_LINK);
					newItem.getItemProperty(ColumnLabels.LOT_ID.getName()).setValue(lotButton);
  					newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(inventoryDetail.getSeedSource());
  					
  					
  				}
  			}
			} finally {
				monitor.stop();				
			} 

		}
		// we disabled this at the beginning of the method so re-enable and refresh render the table
		listInventoryTable.enableContentRefreshing(true);
		
	}

	protected void toggleSelectOnLotEntries(final CheckBox itemCheckBox) {
		if (((Boolean) itemCheckBox.getValue()).equals(true)) {
			this.listInventoryTable.select(itemCheckBox.getData());
		} else {
			this.listInventoryTable.unselect(itemCheckBox.getData());
		}
	}

	public void updateListInventoryTableAfterSave() {
		this.loadInventoryData(); // reset
	}

	public void resetRowsForCancelledReservation(final List<ListEntryLotDetails> lotDetailsToCancel, final Integer listId) {

		for (final ListEntryLotDetails lotDetail : lotDetailsToCancel) {
			final Item item = this.listInventoryTable.getItem(lotDetail);
			item.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(lotDetail.getAvailableLotBalance() + lotDetail.getLotScaleNameAbbr());
			item.getItemProperty(ColumnLabels.RESERVATION.getName()).setValue(lotDetail.getReservedTotalForEntry() + lotDetail.getLotScaleNameAbbr());
		}
	}

	public boolean isSelectedEntriesHasReservation(final List<ListEntryLotDetails> lotDetailsGid,  Map<ListEntryLotDetails, Double> validReservations) {
		if (!CollectionUtils.isEmpty(validReservations)) {
			for (final ListEntryLotDetails lotDetails : lotDetailsGid) {
				for (Map.Entry<ListEntryLotDetails, Double> entry : validReservations.entrySet()) {
					ListEntryLotDetails validReservationToSave = entry.getKey();
					if (lotDetails.getLotId().equals(validReservationToSave.getLotId())) {
						return true;
					}
				}
			}
		}

		for (final ListEntryLotDetails lotDetails : lotDetailsGid) {
			final Item item = this.listInventoryTable.getItem(lotDetails);
			final String newResColumn = (String) item.getItemProperty(ColumnLabels.RESERVATION.getName()).getValue();
			final String scaleAbbrv = lotDetails.getLotScaleNameAbbr();
			if (scaleAbbrv != null) {
				final String reservation = newResColumn.replace(scaleAbbrv, "");
				final double resAmountToCancel = Double.parseDouble(reservation);
				if (resAmountToCancel > 0) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<ListEntryLotDetails> getSelectedLots() {
		final Collection<ListEntryLotDetails> selectedEntries = (Collection<ListEntryLotDetails>) this.listInventoryTable.getValue();

		final List<ListEntryLotDetails> lotsSeleted = new ArrayList<ListEntryLotDetails>();
		lotsSeleted.addAll(selectedEntries);
		return lotsSeleted;
	}

	public void setListId(final Integer listId) {
		this.listId = listId;
	}

	public Integer getListId() {
		return this.listId;
	}

	public void reset() {
		this.listInventoryTable.removeAllItems();
	}

	public void setMaxRows(final int i) {
		this.listInventoryTable.setPageLength(i);
	}

	public void setTableHeight(final String height) {
		this.listInventoryTable.setHeight(height);
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}


}

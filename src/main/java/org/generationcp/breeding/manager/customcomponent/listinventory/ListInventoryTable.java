
package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class ListInventoryTable extends TableWithSelectAllLayout implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(ListInventoryTable.class);

	private static final long serialVersionUID = 1L;

	protected Table listInventoryTable;
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

	public ListInventoryTable(Integer listId) {
		super(ColumnLabels.TAG.getName());
		this.listId = listId;
	}

	@Override
	public void instantiateComponents() {

		super.instantiateComponents();

		this.listInventoryTable = this.getTable();
		this.listInventoryTable.setMultiSelect(true);
		this.listInventoryTable.setImmediate(true);

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
		this.listInventoryTable.addContainerProperty(ColumnLabels.UNITS.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Double.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.TOTAL.getName(), Double.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.RESERVED.getName(), Double.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.NEWLY_RESERVED.getName(), Double.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.COMMENT.getName(), String.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		this.listInventoryTable.addContainerProperty(ColumnLabels.LOT_ID.getName(), Integer.class, null);

		this.listInventoryTable.setColumnHeader(ColumnLabels.TAG.getName(), this.messageSource.getMessage(Message.CHECK_ICON));
		this.listInventoryTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		this.listInventoryTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(),
				ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.LOT_LOCATION.getName(),
				ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.UNITS.getName(),
				ColumnLabels.UNITS.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(),
				ColumnLabels.AVAILABLE_INVENTORY.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.TOTAL.getName(),
				ColumnLabels.TOTAL.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.RESERVED.getName(),
				ColumnLabels.RESERVED.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.NEWLY_RESERVED.getName(),
				ColumnLabels.NEWLY_RESERVED.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.COMMENT.getName(),
				ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.STOCKID.getName(),
				ColumnLabels.STOCKID.getTermNameFromOntology(this.ontologyDataManager));
		this.listInventoryTable.setColumnHeader(ColumnLabels.LOT_ID.getName(),
				ColumnLabels.LOT_ID.getTermNameFromOntology(this.ontologyDataManager));
	}

	public void loadInventoryData() {
		if (this.listId != null) {
			try {
				List<GermplasmListData> inventoryDetails =
						this.inventoryDataManager.getLotDetailsForList(this.listId, 0, Integer.MAX_VALUE);
				this.displayInventoryDetails(inventoryDetails);
			} catch (MiddlewareQueryException e) {
				ListInventoryTable.LOG.error(e.getMessage(), e);
			}
		}

	}

	public void displayInventoryDetails(List<GermplasmListData> inventoryDetails) {

		this.listInventoryTable.removeAllItems();
		Container listInventoryContainer = this.listInventoryTable.getContainerDataSource();
		for (GermplasmListData inventoryDetail : inventoryDetails) {

			Integer entryId = inventoryDetail.getEntryId();
			String designation = inventoryDetail.getDesignation();

			ListDataInventory listDataInventory = inventoryDetail.getInventoryInfo();
			@SuppressWarnings("unchecked")
			List<ListEntryLotDetails> lotDetails = (List<ListEntryLotDetails>) listDataInventory.getLotRows();

			if (lotDetails != null) {
				for (ListEntryLotDetails lotDetail : lotDetails) {
					Item newItem = listInventoryContainer.addItem(lotDetail);

					CheckBox itemCheckBox = new CheckBox();
					itemCheckBox.setData(lotDetail);
					itemCheckBox.setImmediate(true);
					itemCheckBox.addListener(new ClickListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
							CheckBox itemCheckBox = (CheckBox) event.getButton();
							ListInventoryTable.this.toggleSelectOnLotEntries(itemCheckBox);
						}

					});

					Button desigButton =
							new Button(String.format("%s", designation), new GidLinkButtonClickListener(
									inventoryDetail.getGid().toString(), true));
					desigButton.setStyleName(BaseTheme.BUTTON_LINK);

					newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
					newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);
					newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
					newItem.getItemProperty(ColumnLabels.LOT_LOCATION.getName()).setValue(lotDetail.getLocationOfLot().getLname());
					newItem.getItemProperty(ColumnLabels.UNITS.getName()).setValue(lotDetail.getScaleOfLot().getName());
					newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(lotDetail.getAvailableLotBalance());
					newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(lotDetail.getActualLotBalance());
					newItem.getItemProperty(ColumnLabels.RESERVED.getName()).setValue(lotDetail.getReservedTotalForEntry());
					newItem.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(0);
					newItem.getItemProperty(ColumnLabels.COMMENT.getName()).setValue(lotDetail.getCommentOfLot());

					String stockIds = lotDetail.getStockIds();
					Label stockIdsLbl = new Label(stockIds);
					stockIdsLbl.setDescription(stockIds);
					newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdsLbl);

					newItem.getItemProperty(ColumnLabels.LOT_ID.getName()).setValue(lotDetail.getLotId());
				}
			}
		}

	}

	protected void toggleSelectOnLotEntries(CheckBox itemCheckBox) {
		if (((Boolean) itemCheckBox.getValue()).equals(true)) {
			this.listInventoryTable.select(itemCheckBox.getData());
		} else {
			this.listInventoryTable.unselect(itemCheckBox.getData());
		}
	}

	public void updateListInventoryTableAfterSave() {
		this.loadInventoryData(); // reset
	}

	public void resetRowsForCancelledReservation(List<ListEntryLotDetails> lotDetailsToCancel, Integer listId) {

		for (ListEntryLotDetails lotDetail : lotDetailsToCancel) {
			Item item = this.listInventoryTable.getItem(lotDetail);

			Double availColumn = (Double) item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
			Double reservedColumn = (Double) item.getItemProperty(ColumnLabels.RESERVED.getName()).getValue();
			Double newAvailVal = availColumn + reservedColumn;

			lotDetail.setAvailableLotBalance(newAvailVal);
			item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(newAvailVal);
			item.getItemProperty(ColumnLabels.RESERVED.getName()).setValue(0);
			item.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(0);
		}
	}

	public boolean isSelectedEntriesHasReservation(List<ListEntryLotDetails> lotDetailsGid) {
		for (ListEntryLotDetails lotDetails : lotDetailsGid) {
			Item item = this.listInventoryTable.getItem(lotDetails);
			Double resColumn = (Double) item.getItemProperty(ColumnLabels.RESERVED.getName()).getValue();
			Double newResColumn = (Double) item.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).getValue();

			if (resColumn > 0 || newResColumn > 0) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<ListEntryLotDetails> getSelectedLots() {
		Collection<ListEntryLotDetails> selectedEntries = (Collection<ListEntryLotDetails>) this.listInventoryTable.getValue();

		List<ListEntryLotDetails> lotsSeleted = new ArrayList<ListEntryLotDetails>();
		lotsSeleted.addAll(selectedEntries);
		return lotsSeleted;
	}

	public void setListId(Integer listId) {
		this.listId = listId;
	}

	public Integer getListId() {
		return this.listId;
	}

	public void reset() {
		this.listInventoryTable.removeAllItems();
	}

	public void setMaxRows(int i) {
		this.listInventoryTable.setPageLength(i);
	}

	public void setTableHeight(String height) {
		this.listInventoryTable.setHeight(height);
	}

}

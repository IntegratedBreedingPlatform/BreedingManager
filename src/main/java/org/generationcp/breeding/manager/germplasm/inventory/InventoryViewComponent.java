
package org.generationcp.breeding.manager.germplasm.inventory;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class InventoryViewComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(InventoryViewComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private final Integer listId;
	private Integer recordId; // lrecId
	private Integer gid;

	private Table lotEntriesTable;

	private Label noEntriesLabel;

	public static final String LOT_LOCATION = "lotLocation";
	public static final String LOT_UNITS = "lotUnits";
	public static final String TOTAL = "actualBalance";
	public static final String AVAILABLE_BALANCE = "availableBalance";
	public static final String RES_THIS_ENTRY = "res-this-entry";

	public static final String COMMENTS = "comments";
	public static final String LOT_ID = "lotId";
	public static final String STOCKID = "stockId";

	private boolean isThereNoInventoryInfo;

	public InventoryViewComponent(Integer listId) {
		this.listId = listId;
	}

	public InventoryViewComponent(Integer listId, Integer recordId, Integer gid) {
		this.listId = listId;
		this.recordId = recordId;
		this.gid = gid;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.layoutComponents();
	}

	@Override
	public void updateLabels() {
		// inherited method - do nothing
	}

	public void instantiateComponents() {

		this.lotEntriesTable = new Table();
		this.lotEntriesTable.setWidth("90%");
		this.lotEntriesTable.setHeight("160px");

		this.noEntriesLabel =
				new Label(this.messageSource.getMessage(Message.THERE_IS_NO_INVENTORY_INFORMATION_AVAILABLE_FOR_THIS_GERMPLASM) + ".");

		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_LOCATION, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_UNITS, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.TOTAL, Double.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.AVAILABLE_BALANCE, Double.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.RES_THIS_ENTRY, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.COMMENTS, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.STOCKID, Label.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_ID, Integer.class, null);

		// Get the name of the headers from the ontology
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.LOT_LOCATION, this.getTermNameFromOntology(ColumnLabels.LOT_LOCATION));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.LOT_UNITS, this.getTermNameFromOntology(ColumnLabels.UNITS));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.COMMENTS, this.getTermNameFromOntology(ColumnLabels.COMMENT));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.STOCKID, this.getTermNameFromOntology(ColumnLabels.STOCKID));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.LOT_ID, this.getTermNameFromOntology(ColumnLabels.LOT_ID));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.RES_THIS_ENTRY, this.getTermNameFromOntology(ColumnLabels.RESERVED));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.AVAILABLE_BALANCE,
				this.getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.TOTAL, this.getTermNameFromOntology(ColumnLabels.TOTAL));
	}

	public void initializeValues() {
		List<? extends LotDetails> lotDetailEntries =
				this.listId != null && this.recordId != null ? this.inventoryDataManager.getLotDetailsForListEntry(this.listId,
						this.recordId, this.gid) : this.inventoryDataManager.getLotDetailsForGermplasm(this.gid);

		for (LotDetails lotEntry : lotDetailEntries) {
			Item newItem = this.getTable().addItem(lotEntry.getLotId());

			String lotLocation = "";
			if (lotEntry.getLocationOfLot() != null && lotEntry.getLocationOfLot().getLname() != null) {
				lotLocation = lotEntry.getLocationOfLot().getLname();
			}
			newItem.getItemProperty(InventoryViewComponent.LOT_LOCATION).setValue(lotLocation);

			String scale = "";
			if (lotEntry.getScaleOfLot() != null && lotEntry.getScaleOfLot().getName() != null) {
				scale = lotEntry.getScaleOfLot().getName();
			}
			newItem.getItemProperty(InventoryViewComponent.LOT_UNITS).setValue(scale);

			newItem.getItemProperty(InventoryViewComponent.TOTAL).setValue(lotEntry.getActualLotBalance());
			newItem.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).setValue(lotEntry.getAvailableLotBalance());

			if (this.listId != null && this.recordId != null) {
				ListEntryLotDetails listEntrtlotDetail = (ListEntryLotDetails) lotEntry;
				newItem.getItemProperty(InventoryViewComponent.RES_THIS_ENTRY).setValue(listEntrtlotDetail.getReservedTotalForEntry());
			} else {
				newItem.getItemProperty(InventoryViewComponent.RES_THIS_ENTRY).setValue(lotEntry.getReservedTotal());
			}

			newItem.getItemProperty(InventoryViewComponent.COMMENTS).setValue(lotEntry.getCommentOfLot());

			String stockIds = lotEntry.getStockIds();
			Label stockIdsLbl = new Label(stockIds);
			stockIdsLbl.setDescription(stockIds);
			newItem.getItemProperty(InventoryViewComponent.STOCKID).setValue(stockIdsLbl);

			newItem.getItemProperty(InventoryViewComponent.LOT_ID).setValue(lotEntry.getLotId());
		}
	}

	public void layoutComponents() {

		this.setSpacing(true);
		if (!this.lotEntriesTable.getItemIds().isEmpty()) {
			this.addComponent(this.lotEntriesTable);
		} else {
			this.addComponent(this.noEntriesLabel);
		}

	}

	public boolean isThereNoInventoryInfo() {
		return this.isThereNoInventoryInfo;
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	public Table getTable() {
		return this.lotEntriesTable;
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

}

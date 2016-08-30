
package org.generationcp.breeding.manager.inventory;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
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
public class InventoryViewComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(InventoryViewComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	private final Integer listId;
	private Integer recordId; // lrecId
	private Integer gid;

	private Label description;
	private TableLayout lotEntriesLayout;
	private Table lotEntriesTable;

	private static final String LOT_LOCATION = "lotLocation";
	private static final String LOT_UNITS = "lotUnits";
	private static final String ACTUAL_BALANCE = "actualBalance";
	private static final String AVAILABLE_BALANCE = "availableBalance";
	private static final String RES_THIS_ENTRY = "res-this-entry";
	private static final String RES_OTHER_ENTRY = "res-other-entry";
	private static final String COMMENTS = "comments";
	private static final String LOT_ID = "lotId";

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
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void instantiateComponents() {
		this.description = new Label(this.messageSource.getMessage(Message.LOT_DETAILS_FOR_SELECTED_ENTRIES));
		this.description.setDebugId("description");

		this.lotEntriesLayout = new TableLayout(Integer.MAX_VALUE, 8);
		this.lotEntriesLayout.setDebugId("lotEntriesLayout");

		this.lotEntriesTable = this.lotEntriesLayout.getTable();
		this.lotEntriesTable.setWidth("100%");

		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_LOCATION, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_UNITS, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.ACTUAL_BALANCE, Double.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.AVAILABLE_BALANCE, Double.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.RES_THIS_ENTRY, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.RES_OTHER_ENTRY, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.COMMENTS, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_ID, Integer.class, null);

		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.LOT_LOCATION, Message.LOT_LOCATION);
		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.LOT_UNITS, Message.LOT_UNITS);
		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.ACTUAL_BALANCE, Message.ACTUAL_BALANCE);
		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.AVAILABLE_BALANCE, Message.AVAILABLE_BALANCE);
		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.RES_THIS_ENTRY, Message.RES_THIS_ENTRY);
		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.RES_OTHER_ENTRY, Message.RES_OTHER_ENTRY);
		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.COMMENTS, Message.COMMENTS);
		this.messageSource.setColumnHeader(this.lotEntriesTable, InventoryViewComponent.LOT_ID, Message.LOT_ID);
	}

	@Override
	public void initializeValues() {
		try {
			List<? extends LotDetails> lotDetailEntries =
					this.listId != null && this.recordId != null ? this.inventoryDataManager.getLotDetailsForListEntry(this.listId,
							this.recordId, this.gid) : this.inventoryDataManager.getLotDetailsForGermplasm(this.gid);

			for (LotDetails lotEntry : lotDetailEntries) {
						Item newItem = this.lotEntriesTable.addItem(lotEntry.getLotId());

				String lotLocation = "";
						if (lotEntry.getLocationOfLot() != null) {
							if (lotEntry.getLocationOfLot().getLname() != null) {
								lotLocation = lotEntry.getLocationOfLot().getLname();
							}
						}
				newItem.getItemProperty(InventoryViewComponent.LOT_LOCATION).setValue(lotLocation);

				String scale = "";
				if (lotEntry.getScaleOfLot() != null) {
					if (lotEntry.getScaleOfLot().getName() != null) {
						scale = lotEntry.getScaleOfLot().getName();
					}
				}
				newItem.getItemProperty(InventoryViewComponent.LOT_UNITS).setValue(scale);

				newItem.getItemProperty(InventoryViewComponent.ACTUAL_BALANCE).setValue(lotEntry.getActualLotBalance());
				newItem.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).setValue(lotEntry.getAvailableLotBalance());

				if (this.listId != null && this.recordId != null) {
					ListEntryLotDetails listEntrtlotDetail = (ListEntryLotDetails) lotEntry;
					newItem.getItemProperty(InventoryViewComponent.RES_THIS_ENTRY).setValue(listEntrtlotDetail.getReservedTotalForEntry());
					newItem.getItemProperty(InventoryViewComponent.RES_OTHER_ENTRY).setValue(
							listEntrtlotDetail.getReservedTotalForOtherEntries());
				} else {
					newItem.getItemProperty(InventoryViewComponent.RES_THIS_ENTRY).setValue("-");
					newItem.getItemProperty(InventoryViewComponent.RES_OTHER_ENTRY).setValue("-");
				}

				newItem.getItemProperty(InventoryViewComponent.COMMENTS).setValue(lotEntry.getCommentOfLot());
				newItem.getItemProperty(InventoryViewComponent.LOT_ID).setValue(lotEntry.getLotId());
					}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		this.setMargin(true);
		this.setSpacing(true);
		this.addComponent(this.description);
		this.addComponent(this.lotEntriesTable);

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

}

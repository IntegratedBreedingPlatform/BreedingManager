package org.generationcp.breeding.manager.inventory;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableLayout;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
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
public class InventoryViewComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(InventoryViewComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	protected OntologyDataManager ontologyDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	private final Integer listId;
	private Integer recordId; // lrecId
	private Integer gid;

	private Label description;
	private TableLayout lotEntriesLayout;
	private Table lotEntriesTable;

	public static final String LOT_LOCATION = "lotLocation";
	public static final String ACTUAL_BALANCE = "actualBalance";
	public static final String AVAILABLE_BALANCE = "availableBalance";
	public static final String LOT_STATUS = "lotStatus";
	public static final String COMMENTS = "comments";
	public static final String STOCKID = "stockID";
	public static final String LOT_ID = "lotId";
	public static final String SEED_SOURCE = "seedSource";


	private GermplasmListData germplasmListData = null;

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
		if (this.listId != null && this.recordId != null) {
			germplasmListData = this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(this.listId, this.recordId);
		}

		String descriptionMessage = "";

		if (germplasmListData != null) {
			String designation = germplasmListData.getDesignation();
			descriptionMessage = this.messageSource.getMessage(Message.LOT_DETAILS_FOR_SELECTED_ENTRIES, designation);
		}

		this.description = new Label(descriptionMessage);
		this.description.setDebugId("description");

		this.lotEntriesLayout = new TableLayout(Integer.MAX_VALUE, 8);
		this.lotEntriesLayout.setDebugId("lotEntriesLayout");

		this.lotEntriesTable = this.lotEntriesLayout.getTable();
		initializeLotEntriesTable(this.lotEntriesTable);

	}

	protected void initializeLotEntriesTable(final Table table) {
		if (table != null) {
			table.setWidth("100%");

			table.addContainerProperty(InventoryViewComponent.LOT_LOCATION, String.class, null);
			table.addContainerProperty(InventoryViewComponent.ACTUAL_BALANCE, String.class, null);
			table.addContainerProperty(InventoryViewComponent.AVAILABLE_BALANCE, String.class, null);
			table.addContainerProperty(InventoryViewComponent.LOT_STATUS, String.class, null);
			table.addContainerProperty(InventoryViewComponent.COMMENTS, String.class, null);
			table.addContainerProperty(InventoryViewComponent.STOCKID, Label.class, null);
			table.addContainerProperty(InventoryViewComponent.LOT_ID, Integer.class, null);

			table.setColumnHeader(InventoryViewComponent.LOT_LOCATION,
					ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager));
			table.setColumnHeader(InventoryViewComponent.ACTUAL_BALANCE,
					ColumnLabels.ACTUAL_BALANCE.getTermNameFromOntology(this.ontologyDataManager));
			table.setColumnHeader(InventoryViewComponent.AVAILABLE_BALANCE,
					ColumnLabels.TOTAL.getTermNameFromOntology(this.ontologyDataManager));
			table.setColumnHeader(InventoryViewComponent.LOT_STATUS,
					ColumnLabels.LOT_STATUS.getTermNameFromOntology(this.ontologyDataManager));
			table.setColumnHeader(InventoryViewComponent.COMMENTS,
					ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager));
			table.setColumnHeader(InventoryViewComponent.STOCKID,
					ColumnLabels.STOCKID.getTermNameFromOntology(this.ontologyDataManager));
			table
					.setColumnHeader(InventoryViewComponent.LOT_ID, ColumnLabels.LOT_ID.getTermNameFromOntology(this.ontologyDataManager));

			if (this.listId != null && this.recordId != null) {

				table.addContainerProperty(InventoryViewComponent.SEED_SOURCE, String.class, null);
				table.setColumnHeader(InventoryViewComponent.SEED_SOURCE,
						ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager));
			}
		}

	}

	@Override
	public void initializeValues() {
		List<? extends LotDetails> lotDetailEntries = this.listId != null && this.recordId != null ?
				this.inventoryDataManager.getLotDetailsForListEntry(this.listId, this.recordId, this.gid) :
				this.inventoryDataManager.getLotDetailsForGermplasm(this.gid);

		for (LotDetails lotEntry : lotDetailEntries) {
			Item newItem = this.lotEntriesTable.addItem(lotEntry.getLotId());

			String lotLocation = "";
			if (lotEntry.getLocationOfLot() != null) {
				if (lotEntry.getLocationOfLot().getLname() != null) {
					lotLocation = lotEntry.getLocationOfLot().getLname();
				}
			}
			newItem.getItemProperty(InventoryViewComponent.LOT_LOCATION).setValue(lotLocation);

			String lotScaleAbbr = "";

			if(lotEntry.getLotScaleNameAbbr() != null) {
				lotScaleAbbr = lotEntry.getLotScaleNameAbbr();
			}

			StringBuilder actualBalance = new StringBuilder("");
			if(lotEntry.getActualLotBalance() != null){
				actualBalance.append(lotEntry.getActualLotBalance());
				actualBalance.append(lotScaleAbbr);
			}
			newItem.getItemProperty(InventoryViewComponent.ACTUAL_BALANCE).setValue(actualBalance);

			StringBuilder availableBalance = new StringBuilder("");
			if (lotEntry.getAvailableLotBalance() != null) {
				availableBalance.append(lotEntry.getAvailableLotBalance());
				availableBalance.append(lotScaleAbbr);
			}

			newItem.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).setValue(availableBalance);


			newItem.getItemProperty(InventoryViewComponent.LOT_STATUS).setValue(lotEntry.getLotStatus());

			newItem.getItemProperty(InventoryViewComponent.COMMENTS).setValue(lotEntry.getCommentOfLot());
			newItem.getItemProperty(InventoryViewComponent.STOCKID).setValue(lotEntry.getStockIds());
			newItem.getItemProperty(InventoryViewComponent.LOT_ID).setValue(lotEntry.getLotId());

			String seedSource = "";
			if (germplasmListData != null) {
				seedSource = germplasmListData.getSeedSource();
			}

			if (this.listId != null && this.recordId != null) {
				newItem.getItemProperty(InventoryViewComponent.SEED_SOURCE).setValue(seedSource);
			}
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


	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	public Table getTable() {
		return this.lotEntriesTable;
	}

	public void setLotEntriesTable(Table lotEntriesTable) {
		this.lotEntriesTable = lotEntriesTable;
	}

	public Table getLotEntriesTable() {
		return lotEntriesTable;
	}

	public GermplasmListData getGermplasmListData() {
		return germplasmListData;
	}

	public void setGermplasmListData(GermplasmListData germplasmListData) {
		this.germplasmListData = germplasmListData;
	}
}

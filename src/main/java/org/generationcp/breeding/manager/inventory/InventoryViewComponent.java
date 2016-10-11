
package org.generationcp.breeding.manager.inventory;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableLayout;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
public class InventoryViewComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

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

	private static final String LOT_LOCATION = "lotLocation";
	private static final String ACTUAL_BALANCE = "actualBalance";
	private static final String AVAILABLE_BALANCE = "availableBalance";
	private static final String WITHDRAWAL = "withdrawal";
	private static final String STATUS = "status";
	private static final String COMMENTS = "comments";
	private static final String STOCKID = "stockID";
	private static final String LOT_ID = "lotId";
	private static final String SEED_SOURCE = "seedSource";

	private boolean isThereNoInventoryInfo;

	private GermplasmListData germplasmListData = null;

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
		if(this.listId != null && this.recordId != null){
			germplasmListData =
					this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(this.listId, this.recordId);
		}

		String descriptionMessage = "";

		if(germplasmListData != null){
			String designation = germplasmListData.getDesignation();
			descriptionMessage = this.messageSource.getMessage(Message.LOT_DETAILS_FOR_SELECTED_ENTRIES, designation);
		}

		this.description = new Label(descriptionMessage);
		this.description.setDebugId("description");

		this.lotEntriesLayout = new TableLayout(Integer.MAX_VALUE, 8);
		this.lotEntriesLayout.setDebugId("lotEntriesLayout");

		this.lotEntriesTable = this.lotEntriesLayout.getTable();
		this.lotEntriesTable.setWidth("100%");

		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_LOCATION, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.ACTUAL_BALANCE, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.AVAILABLE_BALANCE, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.WITHDRAWAL, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.STATUS, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.COMMENTS, String.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.STOCKID, Label.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.LOT_ID, Integer.class, null);
		this.lotEntriesTable.addContainerProperty(InventoryViewComponent.SEED_SOURCE, String.class, null);

		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.LOT_LOCATION, ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.ACTUAL_BALANCE, ColumnLabels.ACTUAL_BALANCE.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.AVAILABLE_BALANCE, ColumnLabels.TOTAL.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.WITHDRAWAL, ColumnLabels.SEED_RESERVATION.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.STATUS, ColumnLabels.STATUS.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.COMMENTS, ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.STOCKID, ColumnLabels.STOCKID.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.LOT_ID, ColumnLabels.LOT_ID.getTermNameFromOntology(this.ontologyDataManager));
		this.lotEntriesTable.setColumnHeader(InventoryViewComponent.SEED_SOURCE, ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager));
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

				String actualBalance = "";
				if(lotEntry.getActualLotBalance() > 0){
					actualBalance = lotEntry.getActualLotBalance() + lotEntry.getLotScaleNameAbbr();
				}
				newItem.getItemProperty(InventoryViewComponent.ACTUAL_BALANCE).setValue(actualBalance);

				String availableBalance = "";
				if(lotEntry.getAvailableLotBalance() != null && lotEntry.getAvailableLotBalance() > 0){
					availableBalance = lotEntry.getAvailableLotBalance() + lotEntry.getLotScaleNameAbbr();
				}
				newItem.getItemProperty(InventoryViewComponent.AVAILABLE_BALANCE).setValue(availableBalance);

				if(this.listId != null && this.recordId != null) {
					String withdrawalBalance = "";
					if(lotEntry.getWithdrawalBalance() != null && lotEntry.getWithdrawalBalance() > 0){
						withdrawalBalance = lotEntry.getWithdrawalBalance() + lotEntry.getLotScaleNameAbbr();
					}
					newItem.getItemProperty(InventoryViewComponent.WITHDRAWAL).setValue(withdrawalBalance);

					String withdrawalStatus = "";
					if(lotEntry.getWithdrawalStatus() != null){
						withdrawalStatus = lotEntry.getWithdrawalStatus();
					}
					newItem.getItemProperty(InventoryViewComponent.STATUS).setValue(withdrawalStatus);
				}else{
					newItem.getItemProperty(InventoryViewComponent.WITHDRAWAL).setValue("-");
					newItem.getItemProperty(InventoryViewComponent.STATUS).setValue("-");
				}



				newItem.getItemProperty(InventoryViewComponent.COMMENTS).setValue(lotEntry.getCommentOfLot());
				newItem.getItemProperty(InventoryViewComponent.STOCKID).setValue(lotEntry.getStockIds());
				newItem.getItemProperty(InventoryViewComponent.LOT_ID).setValue(lotEntry.getLotId());

				String seedSource = "";
				if(germplasmListData != null){
					seedSource = germplasmListData.getSeedSource();
				}
				newItem.getItemProperty(InventoryViewComponent.SEED_SOURCE).setValue(seedSource);
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

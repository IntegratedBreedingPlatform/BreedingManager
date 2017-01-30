
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUtil;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class SelectParentsListDataComponent extends VerticalLayout
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, ReserveInventorySource {

	private static final String NO_LOT_FOR_THIS_GERMPLASM = "No Lot for this Germplasm";
	private static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";
	private static final String STRING_DASH = "-";

	@Autowired
	private PlatformTransactionManager transactionManager;

	private final class ListDataTableActionHandler implements Action.Handler {

		private static final long serialVersionUID = -2173636726748988046L;

		@Override
		public void handleAction(Action action, Object sender, Object target) {
			if (action.equals(SelectParentsListDataComponent.ACTION_ADD_TO_FEMALE_LIST)) {
				SelectParentsListDataComponent.this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(
						SelectParentsListDataComponent.this.listDataTable,
						SelectParentsListDataComponent.this.makeCrossesParentsComponent.getFemaleTable(), null);
				SelectParentsListDataComponent.this.makeCrossesParentsComponent
						.assignEntryNumber(SelectParentsListDataComponent.this.makeCrossesParentsComponent.getFemaleTable());
				SelectParentsListDataComponent.this.makeCrossesParentsComponent.getFemaleParentTabSheet().setSelectedTab(0);
			} else if (action.equals(SelectParentsListDataComponent.ACTION_ADD_TO_MALE_LIST)) {
				SelectParentsListDataComponent.this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(
						SelectParentsListDataComponent.this.listDataTable,
						SelectParentsListDataComponent.this.makeCrossesParentsComponent.getMaleTable(), null);
				SelectParentsListDataComponent.this.makeCrossesParentsComponent
						.assignEntryNumber(SelectParentsListDataComponent.this.makeCrossesParentsComponent.getMaleTable());
				SelectParentsListDataComponent.this.makeCrossesParentsComponent.getMaleParentTabSheet().setSelectedTab(1);
			}
		}

		@Override
		public Action[] getActions(Object target, Object sender) {
			return SelectParentsListDataComponent.LIST_DATA_TABLE_ACTIONS;
		}
	}

	private final class ActionMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(SelectParentsListDataComponent.this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// Get reference to clicked item
					ContextMenuItem clickedItem = event.getClickedItem();
					if (clickedItem.getName().equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						SelectParentsListDataComponent.this.listDataTable
								.setValue(SelectParentsListDataComponent.this.listDataTable.getItemIds());
					} else if (clickedItem.getName()
							.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.ADD_TO_FEMALE_LIST))) {
						Collection<?> selectedIdsToAdd = (Collection<?>) SelectParentsListDataComponent.this.listDataTable.getValue();
						if (!selectedIdsToAdd.isEmpty()) {
							SelectParentsListDataComponent.this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(
									SelectParentsListDataComponent.this.listDataTable,
									SelectParentsListDataComponent.this.makeCrossesParentsComponent.getFemaleTable(), null);
							SelectParentsListDataComponent.this.makeCrossesParentsComponent
									.assignEntryNumber(SelectParentsListDataComponent.this.makeCrossesParentsComponent.getFemaleTable());
							SelectParentsListDataComponent.this.makeCrossesParentsComponent.getFemaleParentTabSheet().setSelectedTab(0);
						} else {
							MessageNotifier.showWarning(SelectParentsListDataComponent.this.getWindow(),
									SelectParentsListDataComponent.this.messageSource.getMessage(Message.WARNING),
									SelectParentsListDataComponent.this.messageSource
											.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
						}
					} else if (clickedItem.getName()
							.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.ADD_TO_MALE_LIST))) {
						Collection<?> selectedIdsToAdd = (Collection<?>) SelectParentsListDataComponent.this.listDataTable.getValue();
						if (!selectedIdsToAdd.isEmpty()) {
							SelectParentsListDataComponent.this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(
									SelectParentsListDataComponent.this.listDataTable,
									SelectParentsListDataComponent.this.makeCrossesParentsComponent.getMaleTable(), null);
							SelectParentsListDataComponent.this.makeCrossesParentsComponent
									.assignEntryNumber(SelectParentsListDataComponent.this.makeCrossesParentsComponent.getMaleTable());
							SelectParentsListDataComponent.this.makeCrossesParentsComponent.getMaleParentTabSheet().setSelectedTab(1);
						} else {
							MessageNotifier.showWarning(SelectParentsListDataComponent.this.getWindow(),
									SelectParentsListDataComponent.this.messageSource.getMessage(Message.WARNING),
									SelectParentsListDataComponent.this.messageSource
											.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
						}
					} else if (clickedItem.getName()
							.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.INVENTORY_VIEW))) {
						SelectParentsListDataComponent.this.viewInventoryAction();
					} else if (clickedItem.getName()
							.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES))) {
						SelectParentsListDataComponent.this.listDataTable
								.setValue(CrossingManagerUtil.getEvenEntries(SelectParentsListDataComponent.this.listDataTable));
					} else if (clickedItem.getName()
							.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES))) {
						SelectParentsListDataComponent.this.listDataTable
								.setValue(CrossingManagerUtil.getOddEntries(SelectParentsListDataComponent.this.listDataTable));
					}
				}
			});
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(SelectParentsListDataComponent.class);
	private static final long serialVersionUID = 7907737258051595316L;
	private static final String CHECKBOX_COLUMN_ID = "Checkbox Column ID";

	public static final String LIST_DATA_TABLE_ID = "SelectParentsListDataComponent List Data Table ID";
	public static final String CROSSING_MANAGER_PARENT_TAB_INVENTORY_TABLE = "Crossing manager parent tab inventory table";

	private static final Action ACTION_ADD_TO_FEMALE_LIST = new Action("Add to Female List");
	private static final Action ACTION_ADD_TO_MALE_LIST = new Action("Add to Male List");
	private static final Action[] LIST_DATA_TABLE_ACTIONS =
			new Action[] {SelectParentsListDataComponent.ACTION_ADD_TO_FEMALE_LIST, SelectParentsListDataComponent.ACTION_ADD_TO_MALE_LIST};

	private final Integer germplasmListId;
	private GermplasmList germplasmList;
	private Long count;
	private Label listEntriesLabel;
	private Label totalListEntriesLabel;
	private Label totalSelectedListEntriesLabel;

	private Table listDataTable;
	private Button viewListHeaderButton;
	private final String listName;

	private Button actionButton;
	private ContextMenu actionMenu;

	private Button inventoryViewActionButton;
	private ContextMenu inventoryViewActionMenu;
	private ContextMenuItem menuCopyToListFromInventory;
	private ContextMenuItem menuInventorySaveChanges;
	@SuppressWarnings("unused")
	private ContextMenuItem menuListView;
	@SuppressWarnings("unused")
	private ContextMenuItem menuReserveInventory;

	public static final String ACTIONS_BUTTON_ID = "Actions";

	private ViewListHeaderWindow viewListHeaderWindow;

	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private CrossingManagerInventoryTable listInventoryTable;

	// Layout variables
	private HorizontalLayout headerLayout;
	private HorizontalLayout subHeaderLayout;

	private boolean hasChanges = false;

	// Inventory Related Variables
	private ReserveInventoryWindow reserveInventory;
	private ReservationStatusWindow reservationStatus;
	private ReserveInventoryUtil reserveInventoryUtil;
	private ReserveInventoryAction reserveInventoryAction;
	private Map<ListEntryLotDetails, Double> validReservationsToSave;

	private MakeCrossesParentsComponent makeCrossesParentsComponent;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;
	
	@Autowired
	private UserDataManager userDataManager;

	public SelectParentsListDataComponent(Integer germplasmListId, String listName,
			MakeCrossesParentsComponent makeCrossesParentsComponent) {
		super();
		this.germplasmListId = germplasmListId;
		this.listName = listName;
		this.makeCrossesParentsComponent = makeCrossesParentsComponent;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();

		if (this.makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)) {
			this.changeToListView();
		} else if (this.makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.INVENTORY_VIEW)) {
			this.viewInventoryActionConfirmed();
		}
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void instantiateComponents() {
		this.retrieveListDetails();

		this.listEntriesLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.listEntriesLabel.setDebugId("listEntriesLabel");
		this.listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.listEntriesLabel.setWidth("160px");

		this.totalListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalListEntriesLabel.setDebugId("totalListEntriesLabel");
		this.totalListEntriesLabel.setWidth("120px");
		this.updateNoOfEntries(this.count);

		this.totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedListEntriesLabel.setDebugId("totalSelectedListEntriesLabel");
		this.totalSelectedListEntriesLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.viewListHeaderWindow = new ViewListHeaderWindow(this.germplasmList, 
				BreedingManagerUtil.getAllNamesAsMap(userDataManager), germplasmListManager.getGermplasmListTypes());

		this.viewListHeaderButton = new Button(this.messageSource.getMessage(Message.VIEW_HEADER));
		this.viewListHeaderButton.setDebugId("viewListHeaderButton");
		this.viewListHeaderButton.addStyleName(BaseTheme.BUTTON_LINK);
		this.viewListHeaderButton.setDescription(this.retrieveViewListHeaderButtonDescription());

		this.actionButton = new ActionButton();
		this.actionButton.setDebugId("actionButton");
		this.actionButton.setData(SelectParentsListDataComponent.ACTIONS_BUTTON_ID);

		this.inventoryViewActionButton = new ActionButton();
		this.inventoryViewActionButton.setDebugId("inventoryViewActionButton");
		this.inventoryViewActionButton.setData(SelectParentsListDataComponent.ACTIONS_BUTTON_ID);

		this.actionMenu = new ContextMenu();
		this.actionMenu.setDebugId("actionMenu");
		this.actionMenu.setWidth("250px");
		this.actionMenu.addItem(this.messageSource.getMessage(Message.ADD_TO_MALE_LIST));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.ADD_TO_FEMALE_LIST));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.INVENTORY_VIEW));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES));

		this.inventoryViewActionMenu = new ContextMenu();
		this.inventoryViewActionMenu.setDebugId("inventoryViewActionMenu");
		this.inventoryViewActionMenu.setWidth("295px");
		this.menuCopyToListFromInventory = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.COPY_TO_LIST));
		this.menuReserveInventory = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.menuListView = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
		this.menuInventorySaveChanges = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SAVE_CHANGES));
		this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES));
		this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES));
		this.resetInventoryMenuOptions();

		this.initializeListDataTable();
		this.initializeListInventoryTable();

		this.viewListHeaderButton = new Button(this.messageSource.getMessage(Message.VIEW_LIST_HEADERS));
		this.viewListHeaderButton.setDebugId("viewListHeaderButton");
		this.viewListHeaderButton.setStyleName(BaseTheme.BUTTON_LINK);

		// Inventory Related Variables
		this.validReservationsToSave = new HashMap<ListEntryLotDetails, Double>();

		// ListSelectionComponent is null when tool launched from BMS dashboard
		if (this.makeCrossesParentsComponent.getMakeCrossesMain() != null && this.makeCrossesParentsComponent.getMakeCrossesMain() != null) {
			SelectParentsComponent selectParentComponent =
					this.makeCrossesParentsComponent.getMakeCrossesMain().getSelectParentsComponent();
			selectParentComponent.addUpdateListStatusForChanges(this, this.hasChanges);
		}
	}

	private String retrieveViewListHeaderButtonDescription() {
		if (this.viewListHeaderWindow.getListHeaderComponent() != null) {
			return this.viewListHeaderWindow.getListHeaderComponent().toString();
		}

		return "";
	}

	private void resetInventoryMenuOptions() {
		// disable the save button at first since there are no reservations yet
		this.menuInventorySaveChanges.setEnabled(false);

		// Temporarily disable to Copy to List in InventoryView
		this.menuCopyToListFromInventory.setEnabled(false);
	}

	void initializeListDataTable() {
		this.tableWithSelectAllLayout =
				new TableWithSelectAllLayout(this.count.intValue(), 5, SelectParentsListDataComponent.CHECKBOX_COLUMN_ID);
		this.tableWithSelectAllLayout.setWidth("100%");

		this.listDataTable = this.tableWithSelectAllLayout.getTable();
		this.initializeListDataTable(this.listDataTable);
	}

	void initializeListDataTable(Table listDataTable) {
		if (listDataTable != null) {
			listDataTable.setWidth("100%");
			listDataTable.setData(SelectParentsListDataComponent.LIST_DATA_TABLE_ID);
			listDataTable.setSelectable(true);
			listDataTable.setMultiSelect(true);
			listDataTable.setColumnCollapsingAllowed(true);
			listDataTable.setColumnReorderingAllowed(true);
			listDataTable.setImmediate(true);
			listDataTable.setDragMode(TableDragMode.MULTIROW);

			listDataTable.addContainerProperty(SelectParentsListDataComponent.CHECKBOX_COLUMN_ID, CheckBox.class, null);
			listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
			listDataTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
			listDataTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
			listDataTable.addContainerProperty(ColumnLabels.TOTAL.getName(), String.class, null);
			listDataTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, new Label(""));
			listDataTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
			listDataTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
			listDataTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
			listDataTable.addContainerProperty(ColumnLabels.GROUP_ID.getName(), String.class, null);
			listDataTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

			listDataTable.setColumnHeader(SelectParentsListDataComponent.CHECKBOX_COLUMN_ID, this.messageSource.getMessage(Message.CHECK_ICON));
			listDataTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
			listDataTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
			listDataTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(), this.getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY));
			listDataTable.setColumnHeader(ColumnLabels.TOTAL.getName(), this.getTermNameFromOntology(ColumnLabels.TOTAL));
			listDataTable.setColumnHeader(ColumnLabels.STOCKID.getName(), this.getTermNameFromOntology(ColumnLabels.STOCKID));
			listDataTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
			listDataTable.setColumnHeader(ColumnLabels.ENTRY_CODE.getName(), this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE));
			listDataTable.setColumnHeader(ColumnLabels.GID.getName(), this.getTermNameFromOntology(ColumnLabels.GID));
			listDataTable.setColumnHeader(ColumnLabels.GROUP_ID.getName(), this.getTermNameFromOntology(ColumnLabels.GROUP_ID));
			listDataTable.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));

			listDataTable.setColumnWidth(SelectParentsListDataComponent.CHECKBOX_COLUMN_ID, 25);
			listDataTable.setColumnWidth(ColumnLabels.ENTRY_ID.getName(), 25);
			listDataTable.setColumnWidth(ColumnLabels.DESIGNATION.getName(), 130);
			listDataTable.setColumnWidth(ColumnLabels.AVAILABLE_INVENTORY.getName(), 70);
			listDataTable.setColumnWidth(ColumnLabels.TOTAL.getName(), 70);
			listDataTable.setColumnWidth(ColumnLabels.TOTAL.getName(), 130);
			listDataTable.setColumnWidth(ColumnLabels.PARENTAGE.getName(), 130);
			listDataTable.setColumnWidth(ColumnLabels.ENTRY_CODE.getName(), 100);
			listDataTable.setColumnWidth(ColumnLabels.GID.getName(), 60);
			listDataTable.setColumnWidth(ColumnLabels.GROUP_ID.getName(), 60);
			listDataTable.setColumnWidth(ColumnLabels.SEED_SOURCE.getName(), 110);

			listDataTable.setVisibleColumns(new String[] {
				SelectParentsListDataComponent.CHECKBOX_COLUMN_ID, ColumnLabels.ENTRY_ID.getName(), ColumnLabels.DESIGNATION.getName(),
				ColumnLabels.PARENTAGE.getName(), ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.GID.getName(), ColumnLabels.GROUP_ID.getName(),
				ColumnLabels.AVAILABLE_INVENTORY.getName(), ColumnLabels.TOTAL.getName(), ColumnLabels.STOCKID.getName(), ColumnLabels.SEED_SOURCE.getName()});
		}
	}

	private void initializeListInventoryTable() {
		this.listInventoryTable = new CrossingManagerInventoryTable(this.germplasmList.getId());
		this.listInventoryTable.setDebugId("listInventoryTable");
		this.listInventoryTable.setVisible(false);
		if (this.listInventoryTable.getTable() != null) {
			this.listInventoryTable.setMaxRows(9);
			this.listInventoryTable.setTableHeight(null);
			this.listInventoryTable.getTable().setDragMode(TableDragMode.ROW);
			this.listInventoryTable.getTable().setData(SelectParentsListDataComponent.CROSSING_MANAGER_PARENT_TAB_INVENTORY_TABLE);
		}
	}

	private void retrieveListDetails() {
		try {
			this.germplasmList = this.germplasmListManager.getGermplasmListById(this.germplasmListId);
			this.count = this.germplasmListManager.countGermplasmListDataByListId(this.germplasmListId);
		} catch (MiddlewareQueryException e) {
			SelectParentsListDataComponent.LOG.error("Error getting list details" + e.getMessage(), e);
		}
	}

	@Override
	public void initializeValues() {
		try {
			List<GermplasmListData> listEntries = this.inventoryDataManager.getLotCountsForList(this.germplasmListId, 0, Integer.MAX_VALUE);

			for (GermplasmListData entry : listEntries) {
				String gid = String.format("%s", entry.getGid().toString());
				Button gidButton = new Button(gid, new GidLinkClickListener(gid, true));
				gidButton.setDebugId("gidButton");
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);
				gidButton.setDescription("Click to view Germplasm information");

				Button desigButton = new Button(entry.getDesignation(), new GidLinkClickListener(gid, true));
				desigButton.setDebugId("desigButton");
				desigButton.setStyleName(BaseTheme.BUTTON_LINK);
				desigButton.setDescription("Click to view Germplasm information");

				CheckBox itemCheckBox = new CheckBox();
				itemCheckBox.setDebugId("itemCheckBox");
				itemCheckBox.setData(entry.getId());
				itemCheckBox.setImmediate(true);
				itemCheckBox.addListener(new ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
						CheckBox itemCheckBox = (CheckBox) event.getButton();
						if (((Boolean) itemCheckBox.getValue()).equals(true)) {
							SelectParentsListDataComponent.this.getListDataTable().select(itemCheckBox.getData());
						} else {
							SelectParentsListDataComponent.this.getListDataTable().unselect(itemCheckBox.getData());
						}
					}
				});

				// #1 Available Inventory
				// default value
				String availInv = SelectParentsListDataComponent.STRING_DASH;
				if (entry.getInventoryInfo().getLotCount().intValue() != 0) {
					availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
				}

				InventoryLinkButtonClickListener inventoryLinkButtonClickListener =
						new InventoryLinkButtonClickListener(this, this.germplasmList.getId(), entry.getId(), entry.getGid());
				Button inventoryButton = new Button(availInv, inventoryLinkButtonClickListener);
				inventoryButton.setDebugId("inventoryButton");
				inventoryButton.setData(inventoryLinkButtonClickListener);
				inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
				inventoryButton.setDescription(SelectParentsListDataComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

				if (availInv.equals(SelectParentsListDataComponent.STRING_DASH)) {
					inventoryButton.setEnabled(false);
					inventoryButton.setDescription(SelectParentsListDataComponent.NO_LOT_FOR_THIS_GERMPLASM);
				} else {
					inventoryButton.setDescription(SelectParentsListDataComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
				}

				// Seed Reserved
				// default value
				String seedRes = SelectParentsListDataComponent.STRING_DASH;
				if (entry.getInventoryInfo().getReservedLotCount().intValue() != 0) {
					seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
				}
				final String stockIds = entry.getInventoryInfo().getStockIDs();

				Item newItem = this.getListDataTable().getContainerDataSource().addItem(entry.getId());
				newItem.getItemProperty(SelectParentsListDataComponent.CHECKBOX_COLUMN_ID).setValue(itemCheckBox);
				newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
				newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
				newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
				newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(seedRes); //TODO verificar si es correcto.
				newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(entry.getGroupName());
				newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
				newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
				newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(entry.getSeedSource());

				if (entry.getInventoryInfo().getStockIDs() != null) {
					Label stockIdsLabel = new Label(entry.getInventoryInfo().getStockIDs());
					stockIdsLabel.setDebugId("stockIdsLabel");
					stockIdsLabel.setDescription(entry.getInventoryInfo().getStockIDs());
					newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdsLabel);
				}

				final String groupIdDisplayValue = entry.getGroupId() == 0 ? "-" : entry.getGroupId().toString();
				newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupIdDisplayValue);

				// Available Balance
				StringBuilder available = new StringBuilder();

				if (entry.getInventoryInfo().getDistinctScaleCountForGermplsm() == 0) {
					available.append("-");
				} else if (entry.getInventoryInfo().getDistinctScaleCountForGermplsm() == 1) {
					available.append(entry.getInventoryInfo().getTotalAvailableBalance());
					available.append(" ");

					if (!StringUtils.isEmpty(entry.getInventoryInfo().getScaleForGermplsm())) {
						available.append(entry.getInventoryInfo().getScaleForGermplsm());
					}

				} else {
					available.append(ListDataInventory.MIXED);
				}
//				final Button availableButton = new Button(available.toString(),
//					new InventoryLinkButtonClickListener(this.parentListDetailsComponent, this.germplasmList.getId(), entry.getId(),
//						entry.getGid()));
//				availableButton.setStyleName(BaseTheme.BUTTON_LINK);
//				availableButton.setDescription(ListComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
				newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(available);

			}
		} catch (MiddlewareQueryException ex) {
			SelectParentsListDataComponent.LOG.error("Error with getting list entries for list: " + this.germplasmListId, ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error in getting list entries.");
		}
	}

	@Override
	public void addListeners() {

		this.actionButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				SelectParentsListDataComponent.this.actionMenu.show(event.getClientX(), event.getClientY());
			}
		});

		this.actionMenu.addListener(new ActionMenuClickListener());

		this.inventoryViewActionButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 272707576878821700L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				SelectParentsListDataComponent.this.inventoryViewActionMenu.show(event.getClientX(), event.getClientY());
			}
		});

		this.inventoryViewActionMenu.addListener(new ContextMenu.ClickListener() {

			private static final long serialVersionUID = -2343109406180457070L;

			@Override
			public void contextItemClick(final ClickEvent event) {
				final TransactionTemplate transactionTemplate =
						new TransactionTemplate(SelectParentsListDataComponent.this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						// Get reference to clicked item
						ContextMenuItem clickedItem = event.getClickedItem();
						if (clickedItem.getName()
								.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.SAVE_CHANGES))) {
							SelectParentsListDataComponent.this.saveReservationChangesAction();
						} else if (clickedItem.getName()
								.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))) {
							SelectParentsListDataComponent.this.viewListAction();
						} else if (clickedItem.getName()
								.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.COPY_TO_LIST))) {
							// no implementation yet for this method
						} else if (clickedItem.getName()
								.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.RESERVE_INVENTORY))) {
							SelectParentsListDataComponent.this.reserveInventoryAction();
						} else if (clickedItem.getName()
								.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
							SelectParentsListDataComponent.this.listInventoryTable.getTable()
									.setValue(SelectParentsListDataComponent.this.listInventoryTable.getTable().getItemIds());
						} else if (clickedItem.getName()
								.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES))) {
							SelectParentsListDataComponent.this.listInventoryTable.getTable().setValue(
									CrossingManagerUtil.getEvenEntries(SelectParentsListDataComponent.this.listInventoryTable.getTable()));
						} else if (clickedItem.getName()
								.equals(SelectParentsListDataComponent.this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES))) {
							SelectParentsListDataComponent.this.listInventoryTable.getTable().setValue(
									CrossingManagerUtil.getOddEntries(SelectParentsListDataComponent.this.listInventoryTable.getTable()));
						}
					}

				});
			}
		});

		this.viewListHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				SelectParentsListDataComponent.this.openViewListHeaderWindow();
			}
		});

		this.getListDataTable().addActionHandler(new ListDataTableActionHandler());

		this.getListDataTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				SelectParentsListDataComponent.this.updateNoOfSelectedEntries();
			}
		});

		this.listInventoryTable.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				SelectParentsListDataComponent.this.updateNoOfSelectedEntries();
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setMargin(true);
		this.setSpacing(true);

		this.addComponent(this.actionMenu);
		this.addComponent(this.inventoryViewActionMenu);

		this.headerLayout = new HorizontalLayout();
		this.headerLayout.setDebugId("headerLayout");
		this.headerLayout.setWidth("100%");
		HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.listEntriesLabel);
		headingLayout.setDebugId("headingLayout");
		this.headerLayout.addComponent(headingLayout);
		this.headerLayout.addComponent(this.viewListHeaderButton);
		this.headerLayout.setComponentAlignment(headingLayout, Alignment.MIDDLE_LEFT);
		this.headerLayout.setComponentAlignment(this.viewListHeaderButton, Alignment.MIDDLE_RIGHT);

		HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setDebugId("leftSubHeaderLayout");
		leftSubHeaderLayout.setSpacing(true);
		leftSubHeaderLayout.addComponent(this.totalListEntriesLabel);
		leftSubHeaderLayout.addComponent(this.totalSelectedListEntriesLabel);
		leftSubHeaderLayout.setComponentAlignment(this.totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		leftSubHeaderLayout.setComponentAlignment(this.totalSelectedListEntriesLabel, Alignment.MIDDLE_LEFT);

		this.subHeaderLayout = new HorizontalLayout();
		this.subHeaderLayout.setDebugId("subHeaderLayout");
		this.subHeaderLayout.setWidth("100%");
		this.subHeaderLayout.addComponent(leftSubHeaderLayout);
		this.subHeaderLayout.addComponent(this.actionButton);
		this.subHeaderLayout.setComponentAlignment(leftSubHeaderLayout, Alignment.MIDDLE_LEFT);
		this.subHeaderLayout.setComponentAlignment(this.actionButton, Alignment.MIDDLE_RIGHT);

		this.addComponent(this.headerLayout);
		this.addComponent(this.subHeaderLayout);
		this.addComponent(this.tableWithSelectAllLayout);
		this.addComponent(this.listInventoryTable);

	}

	void updateNoOfEntries(long count) {
		if (this.makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)) {
			if (count == 0) {
				this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
			} else {
				this.totalListEntriesLabel
						.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + "  <b>" + count + "</b>");
			}
			// Inventory View
		} else {
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LOTS) + ": " + "  <b>" + count + "</b>");
		}
	}

	void updateNoOfEntries() {
		int entryCount = 0;
		if (this.makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)) {
			entryCount = this.getListDataTable().getItemIds().size();

			// Inventory View
		} else {
			entryCount = this.listInventoryTable.getTable().size();
		}
		this.updateNoOfEntries(entryCount);
	}

	private void updateNoOfSelectedEntries(int count) {
		this.totalSelectedListEntriesLabel
				.setValue("<i>" + this.messageSource.getMessage(Message.SELECTED) + ": " + "  <b>" + count + "</b></i>");
	}

	void updateNoOfSelectedEntries() {
		int entryCount = 0;

		if (this.makeCrossesParentsComponent.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)) {
			Collection<?> selectedItems = (Collection<?>) this.getListDataTable().getValue();
			entryCount = selectedItems.size();
		} else {
			Collection<?> selectedItems = (Collection<?>) this.listInventoryTable.getTable().getValue();
			entryCount = selectedItems.size();
		}

		this.updateNoOfSelectedEntries(entryCount);
	}

	/*--------------------------------------INVENTORY RELATED FUNCTIONS---------------------------------------*/

	void viewListAction() {

		if (!this.hasUnsavedChanges()) {
			this.makeCrossesParentsComponent.getMakeCrossesMain().setModeView(ModeView.LIST_VIEW);
		} else {
			String message =
					"You have unsaved reservations for this list. You will need to save them before changing views. Do you want to save your changes?";

			this.makeCrossesParentsComponent.getMakeCrossesMain().showUnsavedChangesConfirmDialog(message, ModeView.LIST_VIEW);
		}
	}

	public void changeToListView() {
		if (this.listInventoryTable.isVisible()) {
			this.tableWithSelectAllLayout.setVisible(true);
			this.listInventoryTable.setVisible(false);

			this.subHeaderLayout.removeComponent(this.inventoryViewActionButton);
			this.subHeaderLayout.addComponent(this.actionButton);
			this.subHeaderLayout.setComponentAlignment(this.actionButton, Alignment.MIDDLE_RIGHT);

			this.listEntriesLabel.setValue(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();

			this.removeComponent(this.listInventoryTable);
			this.addComponent(this.tableWithSelectAllLayout);

			this.requestRepaint();
		}
	}

	void viewInventoryAction() {
		if (!this.hasUnsavedChanges()) {
			this.makeCrossesParentsComponent.getMakeCrossesMain().setModeView(ModeView.INVENTORY_VIEW);
		} else {
			String message = "You have unsaved changes to the list you are currently editing.. "
					+ "You will need to save them before changing views. " + "Do you want to save your changes?";
			this.makeCrossesParentsComponent.getMakeCrossesMain().showUnsavedChangesConfirmDialog(message, ModeView.INVENTORY_VIEW);
		}
	}

	public void viewInventoryActionConfirmed() {
		this.resetListInventoryTableValues();
		this.changeToInventoryView();
	}

	public void changeToInventoryView() {
		if (this.tableWithSelectAllLayout.isVisible()) {
			this.tableWithSelectAllLayout.setVisible(false);
			this.listInventoryTable.setVisible(true);

			this.subHeaderLayout.removeComponent(this.actionButton);
			this.subHeaderLayout.addComponent(this.inventoryViewActionButton);
			this.subHeaderLayout.setComponentAlignment(this.inventoryViewActionButton, Alignment.MIDDLE_RIGHT);

			this.listEntriesLabel.setValue(this.messageSource.getMessage(Message.INVENTORY));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();

			this.removeComponent(this.tableWithSelectAllLayout);
			this.addComponent(this.listInventoryTable);

			this.requestRepaint();
		}
	}

	public void reserveInventoryAction() {
		// checks if the screen is in the inventory view
		if (!this.listInventoryTable.isVisible()) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"Please change to Inventory View first.");
		} else {
			List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

			if (lotDetailsGid == null || lotDetailsGid.isEmpty()) {
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						"Please select at least 1 lot to reserve.");
			} else {
				// this util handles the inventory reservation related functions
				this.reserveInventoryUtil = new ReserveInventoryUtil(this, lotDetailsGid);
				this.reserveInventoryUtil.viewReserveInventoryWindow();
			}
		}
	}

	@Override
	public void updateListInventoryTable(Map<ListEntryLotDetails, Double> validReservations, boolean withInvalidReservations) {
		for (Map.Entry<ListEntryLotDetails, Double> entry : validReservations.entrySet()) {
			ListEntryLotDetails lot = entry.getKey();
			Double newRes = entry.getValue();

			Item itemToUpdate = this.listInventoryTable.getTable().getItem(lot);
			itemToUpdate.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(newRes);
		}

		this.removeReserveInventoryWindow(this.reserveInventory);

		// update lot reservatios to save
		this.updateLotReservationsToSave(validReservations);

		// enable now the Save Changes option
		this.menuInventorySaveChanges.setEnabled(true);

		// if there are no valid reservations
		if (validReservations.isEmpty()) {
			MessageNotifier.showRequiredFieldError(this.getWindow(),
					this.messageSource.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES)
							+ ".");
		} else if (!withInvalidReservations) {
			MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					"All selected entries will be reserved in their respective lots.", 3000);
		}
	}

	private void updateLotReservationsToSave(Map<ListEntryLotDetails, Double> validReservations) {
		for (Map.Entry<ListEntryLotDetails, Double> entry : validReservations.entrySet()) {
			ListEntryLotDetails lot = entry.getKey();
			Double amountToReserve = entry.getValue();

			if (this.validReservationsToSave.containsKey(lot)) {
				this.validReservationsToSave.remove(lot);

			}

			this.validReservationsToSave.put(lot, amountToReserve);
		}

		if (!this.validReservationsToSave.isEmpty()) {
			this.setHasUnsavedChanges(true);
		}
	}

	@Override
	public void addReserveInventoryWindow(ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.makeCrossesParentsComponent.getWindow().addWindow(this.reserveInventory);
	}

	@Override
	public void addReservationStatusWindow(ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.removeReserveInventoryWindow(this.reserveInventory);
		this.makeCrossesParentsComponent.getWindow().addWindow(this.reservationStatus);
	}

	@Override
	public void removeReserveInventoryWindow(ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.makeCrossesParentsComponent.getWindow().removeWindow(this.reserveInventory);
	}

	@Override
	public void removeReservationStatusWindow(ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.makeCrossesParentsComponent.getWindow().removeWindow(this.reservationStatus);
	}

	public void saveReservationChangesAction() {
		if (this.hasUnsavedChanges()) {
			this.reserveInventoryAction = new ReserveInventoryAction(this);
			boolean success =
					this.reserveInventoryAction.saveReserveTransactions(this.getValidReservationsToSave(), this.germplasmList.getId());
			if (success) {
				this.refreshInventoryColumns(this.getValidReservationsToSave());
				this.resetListInventoryTableValues();
				MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
						"All reservations were saved.");
			}
		}
	}

	public void refreshInventoryColumns(Map<ListEntryLotDetails, Double> validReservationsToSave) {

		Set<Integer> entryIds = new HashSet<Integer>();
		for (Entry<ListEntryLotDetails, Double> details : validReservationsToSave.entrySet()) {
			entryIds.add(details.getKey().getId());
		}

		List<GermplasmListData> germplasmListDataEntries = new ArrayList<GermplasmListData>();

		try {
			if (!entryIds.isEmpty()) {
				germplasmListDataEntries =
						this.inventoryDataManager.getLotCountsForListEntries(this.germplasmList.getId(), new ArrayList<Integer>(entryIds));
			}
		} catch (MiddlewareQueryException e) {
			SelectParentsListDataComponent.LOG.error(e.getMessage(), e);
		}

		for (GermplasmListData listData : germplasmListDataEntries) {
			Item item = this.getListDataTable().getItem(listData.getId());

			// #1 Available Inventory
			// default value
			String availInv = SelectParentsListDataComponent.STRING_DASH;
			if (listData.getInventoryInfo().getLotCount().intValue() != 0) {
				availInv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(this.makeCrossesParentsComponent,
					this.germplasmList.getId(), listData.getId(), listData.getGid()));
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(SelectParentsListDataComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

			if (availInv.equals(SelectParentsListDataComponent.STRING_DASH)) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription(SelectParentsListDataComponent.NO_LOT_FOR_THIS_GERMPLASM);
			} else {
				inventoryButton.setDescription(SelectParentsListDataComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
			}
			item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

			// Seed Reserved
			// default value
//			String seedRes = SelectParentsListDataComponent.STRING_DASH;
//			if (listData.getInventoryInfo().getReservedLotCount().intValue() != 0) {
//				seedRes = listData.getInventoryInfo().getReservedLotCount().toString().trim();
//			}
//
//			item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
		}
	}

	public void resetListInventoryTableValues() {
		if (this.germplasmList != null) {
			this.listInventoryTable.updateListInventoryTableAfterSave();
		} else {
			this.listInventoryTable.reset();
		}

		this.resetInventoryMenuOptions();

		// reset the reservations to save.
		this.validReservationsToSave.clear();

		this.setHasUnsavedChanges(false);
	}

	/*--------------------------------END OF INVENTORY RELATED FUNCTIONS--------------------------------------*/

	public Map<ListEntryLotDetails, Double> getValidReservationsToSave() {
		return this.validReservationsToSave;
	}

	public boolean hasUnsavedChanges() {
		return this.hasChanges;
	}

	private void openViewListHeaderWindow() {
		this.getWindow().addWindow(this.viewListHeaderWindow);
	}

	public Table getListDataTable() {
		return this.tableWithSelectAllLayout.getTable();
	}

	public String getListName() {
		return this.listName;
	}

	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	public void setHasUnsavedChanges(Boolean hasChanges) {
		this.hasChanges = hasChanges;

		if (hasChanges) {
			this.menuInventorySaveChanges.setEnabled(true);
		} else {
			this.menuInventorySaveChanges.setEnabled(false);
		}

		SelectParentsComponent selectParentComponent = this.makeCrossesParentsComponent.getMakeCrossesMain().getSelectParentsComponent();
		selectParentComponent.addUpdateListStatusForChanges(this, this.hasChanges);
	}

	public Integer getGermplasmListId() {
		return this.germplasmListId;
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	protected void setListDataTableWithSelectAll(TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;
	}

	protected TableWithSelectAllLayout getListDataTableWithSelectAll() {
		return this.tableWithSelectAllLayout;
	}

	protected void setListInventoryTable(CrossingManagerInventoryTable listInventoryTable) {
		this.listInventoryTable = listInventoryTable;
	}

	protected CrossingManagerInventoryTable getListInventoryTable() {
		return this.listInventoryTable;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Label getTotalListEntriesLabel() {
		return this.totalListEntriesLabel;
	}

	public Label getTotalSelectedListEntriesLabel() {
		return this.totalSelectedListEntriesLabel;
	}

	public Label getListEntriesLabel() {
		return this.listEntriesLabel;
	}

}

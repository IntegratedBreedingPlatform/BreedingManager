
package org.generationcp.breeding.manager.listmanager;

import com.google.common.collect.Lists;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.ExportListAsDialog;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.RemoveSelectedGermplasmAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.SortableButton;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.CloseLotDiscardInventoryAction;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListManagerInventoryTable;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.inventory.SeedInventoryImportFileComponent;
import org.generationcp.breeding.manager.inventory.SeedInventoryListExporter;
import org.generationcp.breeding.manager.inventory.exception.CloseLotException;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryExportException;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialogSource;
import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.breeding.manager.listmanager.dialog.GermplasmGroupingComponent;
import org.generationcp.breeding.manager.listmanager.dialog.GermplasmGroupingComponentSource;
import org.generationcp.breeding.manager.listmanager.dialog.ListManagerCopyToListDialog;
import org.generationcp.breeding.manager.listmanager.listcomponent.GermplasmListTableContextMenu;
import org.generationcp.breeding.manager.listmanager.listcomponent.InventoryViewActionMenu;
import org.generationcp.breeding.manager.listmanager.listcomponent.ListViewActionMenu;
import org.generationcp.breeding.manager.listmanager.listeners.AddColumnMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.breeding.manager.listmanager.util.ListDataPropertiesRenderer;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.workbench.RoleType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Configurable
public class ListComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout,
		SaveListAsDialogSource, ReserveInventorySource, GermplasmGroupingComponentSource {

	public static final String DATABASE_ERROR = "Database Error!";
	private static final String ERROR_WITH_DELETING_LIST_ENTRIES = "Error with deleting list entries.";

	private static final long serialVersionUID = -3367108805414232721L;

	private static final Logger LOG = LoggerFactory.getLogger(ListComponent.class);

	// String Literals
	private static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";

	private static final int MINIMUM_WIDTH = 10;
	private final Map<Object, Map<Object, Field>> fields = new HashMap<>();

	private ListManagerMain source;
	private ListTabComponent parentListDetailsComponent;
	private GermplasmList germplasmList;

	private List<String> attributeAndNameTypeColumns = new ArrayList<>();
	private long listEntriesCount;
	private String designationOfListEntriesDeleted = "";

	private Label topLabel;
	private Button viewHeaderButton;
	private Label totalListEntriesLabel;
	private Label totalSelectedListEntriesLabel;
	private Button actionsButton;
	private Table listDataTable;
	private TableWithSelectAllLayout listDataTableWithSelectAll;

	private HorizontalLayout headerLayout;
	private HorizontalLayout subHeaderLayout;

	// Menu for Actions button in List View
	private ListViewActionMenu menu;

	// Menu for Actions button in Inventory View
	private InventoryViewActionMenu inventoryViewMenu;

	@SuppressWarnings("unused")
	private ContextMenuItem menuListView;
	@SuppressWarnings("unused")
	private ContextMenuItem menuReserveInventory;
	@SuppressWarnings("unused")
	private ContextMenuItem menuCancelReservation;

	// Menu shown when the user right-click on the germplasm list table
	private GermplasmListTableContextMenu tableContextMenu;

	private AddColumnContextMenu addColumnContextMenu;

	// Tooltips
	public static final String TOOLS_BUTTON_ID = "Actions";
	public static final String LIST_DATA_COMPONENT_TABLE_DATA = "List Data Component Table";

	// this is true if this component is created by accessing the Germplasm List
	// Details page directly from the URL
	private boolean fromUrl;

	// Theme Resource
	private BaseSubWindow listManagerCopyToListDialog;
	private Object selectedColumn = "";
	private Object selectedItemId;
	private String lastCellvalue = "";
	private final Map<Object, String> itemsToDelete = new HashMap<>();

	private Button lockButton;
	private Button unlockButton;
	private Button editHeaderButton;

	private ViewListHeaderWindow viewListHeaderWindow;

	private HorizontalLayout toolsMenuContainer;

	private Button inventoryViewToolsButton;

	public static final String LOCK_BUTTON_ID = "Lock Germplasm List";
	public static final String UNLOCK_BUTTON_ID = "Unlock Germplasm List";

	private static final String LOCK_TOOLTIP = "Click to lock or unlock this germplasm list.";

	private static final String CLOSE_LOT_VALID = "CloseLotValid";
	private static final String CLOSE_LOT_UNCOMMITTED = "CloseLotUnCommitted";
	private static final String CLOSE_LOT_AVAILABLE_BALANCE = "CloseLotAvailableBalance";

	// Value change event is fired when table is populated, so we need a flag
	private Boolean doneInitializing = false;

	// Inventory Related Variables
	private ListManagerInventoryTable listInventoryTable;
	private ReserveInventoryWindow reserveInventory;
	private ReservationStatusWindow reservationStatus;
	private ReserveInventoryUtil reserveInventoryUtil;
	private ReserveInventoryAction reserveInventoryAction;
	private Map<ListEntryLotDetails, Double> validReservationsToSave;
	private List<ListEntryLotDetails> persistedReservationToCancel;
	private Boolean hasChanges;

	private ListDataPropertiesRenderer newColumnsRenderer;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private UserService userService;

	@Resource
	private ContextUtil contextUtil;

	private Integer localUserId = null;

	private FillWith fillWith;

	private SaveListAsDialog dialog;

	private AddEntryDialogSource addEntryDialogSource;

	private BreedingManagerApplication breedingManagerApplication;

	private CloseLotDiscardInventoryAction closeLotDiscardInventoryAction;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	public ListComponent() {
		super();
		this.reserveInventoryAction = new ReserveInventoryAction(this);
		this.closeLotDiscardInventoryAction = new CloseLotDiscardInventoryAction(this);
	}

	public ListComponent(final ListManagerMain source, final ListTabComponent parentListDetailsComponent,
			final GermplasmList germplasmList) {
		this();
		this.source = source;
		this.parentListDetailsComponent = parentListDetailsComponent;
		this.germplasmList = germplasmList;
	}

	@Override
	public void attach() {
		super.attach();
		this.breedingManagerApplication = (BreedingManagerApplication) this.getApplication();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.newColumnsRenderer = new ListDataPropertiesRenderer();

		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();

		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			this.changeToListView();
		} else if (this.source.getModeView().equals(ModeView.INVENTORY_VIEW)) {
			this.viewInventoryActionConfirmed();
		}

	}

	@Override
	public void instantiateComponents() {
		this.inventoryViewMenu = new InventoryViewActionMenu();
		this.inventoryViewMenu.setDebugId("inventoryViewMenu");

		this.topLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.topLabel.setDebugId("topLabel");
		this.topLabel.setWidth("120px");
		this.topLabel.setStyleName(Bootstrap.Typography.H4.styleName());

		this.viewListHeaderWindow = new ViewListHeaderWindow(this.germplasmList, this.userService.getAllUserIDFullNameMap(),
				this.germplasmListManager.getGermplasmListTypes());

		this.viewHeaderButton = new IconButton(
				"<span class='glyphicon glyphicon-info-sign' style='left: 2px; top:10px; color: #7c7c7c;font-size: 16px; font-weight: bold;'></span>",
				this.messageSource.getMessage(Message.VIEW_HEADER));
		this.viewHeaderButton.setDebugId("viewHeaderButton");

		if (this.viewListHeaderWindow.getListHeaderComponent() != null) {
			this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());
		}

		this.editHeaderButton = new IconButton(
				"<span class='glyphicon glyphicon-pencil' style='left: 2px; top:10px; color: #7c7c7c;font-size: 16px; font-weight: bold;'></span>",
				"Edit List Header");

		this.actionsButton = new ActionButton();
		this.actionsButton.setDebugId("actionsButton");
		this.actionsButton.setData(ListComponent.TOOLS_BUTTON_ID);

		this.inventoryViewToolsButton = new ActionButton();
		this.inventoryViewToolsButton.setDebugId("inventoryViewToolsButton");
		this.inventoryViewToolsButton.setData(ListComponent.TOOLS_BUTTON_ID);

		try {
			this.listEntriesCount = this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId());
		} catch (final MiddlewareQueryException ex) {
			ListComponent.LOG.error("Error with retrieving count of list entries for list: " + this.germplasmList.getId(), ex);
			this.listEntriesCount = 0;
		}

		this.totalListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalListEntriesLabel.setDebugId("totalListEntriesLabel");
		this.totalListEntriesLabel.setWidth("120px");

		if (this.listEntriesCount == 0) {
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
		} else {
			this.updateNoOfEntries(this.listEntriesCount);
		}

		this.totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedListEntriesLabel.setDebugId("totalSelectedListEntriesLabel");
		this.totalSelectedListEntriesLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.unlockButton = new IconButton(
				"<span class='bms-locked' style='position: relative; top:5px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
				ListComponent.LOCK_TOOLTIP);
		this.unlockButton.setData(ListComponent.UNLOCK_BUTTON_ID);

		this.lockButton = new IconButton(
				"<span class='bms-lock-open' style='position: relative; top:5px; left: 2px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
				ListComponent.LOCK_TOOLTIP);
		this.lockButton.setData(ListComponent.LOCK_BUTTON_ID);

		this.menu = new ListViewActionMenu();
		this.menu.setDebugId("menu");

		// Add Column menu will be initialized after list data table is created
		this.initializeListDataTable(new TableWithSelectAllLayout(Long.valueOf(this.listEntriesCount).intValue(), this.getNoOfEntries(),
				ColumnLabels.TAG.getName())); // listDataTable
		this.initializeListInventoryTable(); // listInventoryTable

		this.menuCancelReservation = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.CANCEL_RESERVATIONS));

		this.menuReserveInventory = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.CLOSE_LOTS));
		this.menuListView = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));

		this.inventoryViewMenu = new InventoryViewActionMenu();
		this.inventoryViewMenu.setDebugId("inventoryViewMenu");

		this.tableContextMenu = new GermplasmListTableContextMenu();
		this.tableContextMenu.setDebugId("tableContextMenu");

		// Inventory Related Variables
		this.validReservationsToSave = new HashMap<>();

		// Keep Track the changes in ListDataTable and/or ListInventoryTable
		this.hasChanges = false;

		// ListSelectionComponent is null when tool launched from BMS dashboard
		if (this.source != null && this.source.getListSelectionComponent() != null
				&& this.source.getListSelectionComponent().getListDetailsLayout() != null) {
			final ListSelectionLayout listSelection = this.source.getListSelectionComponent().getListDetailsLayout();
			listSelection.addUpdateListStatusForChanges(this, this.hasChanges);
		}

		this.addEntryDialogSource = new ListComponentAddEntryDialogSource(this, this.listDataTable);
	}

	public void initializeListDataTable(final TableWithSelectAllLayout tableWithSelectAllLayout) {

		this.setListDataTableWithSelectAll(tableWithSelectAllLayout);

		if (this.getListDataTableWithSelectAll().getTable() == null) {
			return;
		}

		this.listDataTable = this.getListDataTableWithSelectAll().getTable();
		this.listDataTable.setSelectable(true);
		this.listDataTable.setMultiSelect(true);
		this.listDataTable.setColumnCollapsingAllowed(true);
		this.listDataTable.setWidth("100%");
		this.listDataTable.setDragMode(TableDragMode.ROW);
		this.listDataTable.setData(ListComponent.LIST_DATA_COMPONENT_TABLE_DATA);
		this.listDataTable.setColumnReorderingAllowed(false);
		this.listDataTable.setImmediate(true);

		this.listDataTable.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), SortableButton.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), SortableButton.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.TOTAL.getName(), SortableButton.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.GID.getName(), SortableButton.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.GROUP_ID.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		this.listDataTable.setColumnHeader(ColumnLabels.TAG.getName(), this.messageSource.getMessage(Message.CHECK_ICON));
		this.listDataTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		this.listDataTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		this.listDataTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
		this.listDataTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(),
				this.getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY));
		this.listDataTable.setColumnHeader(ColumnLabels.TOTAL.getName(), this.getTermNameFromOntology(ColumnLabels.TOTAL));
		this.listDataTable.setColumnHeader(ColumnLabels.ENTRY_CODE.getName(), this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE));
		this.listDataTable.setColumnHeader(ColumnLabels.GID.getName(), this.getTermNameFromOntology(ColumnLabels.GID));
		this.listDataTable.setColumnHeader(ColumnLabels.GROUP_ID.getName(), this.getTermNameFromOntology(ColumnLabels.GROUP_ID));
		this.listDataTable.setColumnHeader(ColumnLabels.STOCKID.getName(), this.getTermNameFromOntology(ColumnLabels.STOCKID));
		this.listDataTable.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));

		this.initializeAddColumnContextMenu();

	}

	protected void initializeAddColumnContextMenu() {
		final ListComponentAddColumnSource addColumnSource =
				new ListComponentAddColumnSource(this, this.listDataTable, ColumnLabels.GID.getName());
		this.addColumnContextMenu =
				new AddColumnContextMenu(addColumnSource, this.menu, this.menu.getListEditingOptions(), this.messageSource);
		this.addColumnContextMenu.addListener(new AddColumnMenuItemClickListener(addColumnSource));
	}

	public void initializeListInventoryTable() {
		this.listInventoryTable = new ListManagerInventoryTable(this.source, this.germplasmList.getId(), true, false);
		this.listInventoryTable.setDebugId("listInventoryTable");
		this.listInventoryTable.setVisible(false);
	}

	public int getNoOfEntries() {
		// browse list component is null at this point when tool launched from
		// Workbench dashboard
		final ListSelectionComponent browseListsComponent = this.source.getListSelectionComponent();
		if (browseListsComponent == null || browseListsComponent.isVisible()) {
			return 8;
		}

		return 18;
	}

	@Override
	public void initializeValues() {

		try {
			this.localUserId = this.contextUtil.getCurrentWorkbenchUserId();
		} catch (final MiddlewareQueryException e) {
			ListComponent.LOG.error("Error with retrieving local user ID", e);
			ListComponent.LOG.error("\n" + e.getStackTrace());
		}

		this.loadEntriesToListDataTable();
	}

	public void resetListDataTableValues() {
		this.listDataTable.setEditable(false);
		this.listDataTable.removeAllItems();

		this.loadEntriesToListDataTable();

		this.listDataTable.refreshRowCache();
		this.listDataTable.setImmediate(true);
		this.listDataTable.setEditable(true);
		this.listDataTable.requestRepaint();
	}

	public void loadEntriesToListDataTable() {
		if (this.listEntriesCount > 0) {
			final List<GermplasmListData> listEntries = new ArrayList<>();

			this.getAllListEntries(listEntries);

			for (final GermplasmListData entry : listEntries) {
				this.addListEntryToTable(entry);
			}

			this.listDataTable.sort(new Object[] {ColumnLabels.ENTRY_ID.getName()}, new boolean[] {true});

			// render additional columns
			this.newColumnsRenderer.setListId(this.germplasmList.getId());
			this.newColumnsRenderer.setTargetTable(this.listDataTable);

			try {
				this.newColumnsRenderer.render();
			} catch (final MiddlewareQueryException ex) {
				ListComponent.LOG.error("Error with displaying added columns for entries of list: " + this.germplasmList.getId(), ex);
			}

		}

	}

	public void addListEntryToTable(final GermplasmListData entry) {
		final String gid = String.format("%s", entry.getGid().toString());
		final Button gidButton = new SortableButton(gid, new GidLinkButtonClickListener(this.source, gid, true, true));
		gidButton.setDebugId("gidButton");
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription("Click to view Germplasm information");

		final Button desigButton = new SortableButton(entry.getDesignation(), new GidLinkButtonClickListener(this.source, gid, true, true));
		desigButton.setDebugId("desigButton");
		desigButton.setStyleName(BaseTheme.BUTTON_LINK);
		desigButton.setDescription("Click to view Germplasm information");

		final CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setDebugId("itemCheckBox");
		itemCheckBox.setData(entry.getId());
		itemCheckBox.setImmediate(true);
		itemCheckBox.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				final CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (itemCheckBox.getValue().equals(true)) {
					ListComponent.this.listDataTable.select(itemCheckBox.getData());
				} else {
					ListComponent.this.listDataTable.unselect(itemCheckBox.getData());
				}
			}

		});

		final Item newItem = this.listDataTable.getContainerDataSource().addItem(entry.getId());
		newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
		newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
		newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
		newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(entry.getGroupName());
		newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
		newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
		newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(entry.getSeedSource());

		final String groupIdDisplayValue = entry.getGroupId() == 0 ? "-" : entry.getGroupId().toString();
		newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupIdDisplayValue);

		// Inventory Related Columns

		// Lots
		final Button lotButton = ListCommonActionsUtil.getLotCountButton(entry.getInventoryInfo().getLotCount(), entry.getGid(),
				entry.getDesignation(), this.parentListDetailsComponent, null);
		newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(lotButton);

		// Available Balance
		final String available = entry.getInventoryInfo().getAvailable();

		final Button availableButton =
				new SortableButton(available.toString(), new InventoryLinkButtonClickListener(this.parentListDetailsComponent,
						this.germplasmList.getId(), entry.getId(), entry.getGid()));
		availableButton.setStyleName(BaseTheme.BUTTON_LINK);
		availableButton.setDescription(ListComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
		newItem.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(availableButton);

		final String stockIds = entry.getInventoryInfo().getStockIDs();
		final Label stockIdsLbl = new Label(stockIds);
		stockIdsLbl.setDebugId("stockIdsLbl");
		if (stockIds == null) {
			stockIdsLbl.setDescription("");
			stockIdsLbl.getPropertyDataSource().setValue("");
		} else {
			stockIdsLbl.setDescription(stockIds);
		}
		newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdsLbl);

	}

	private void getAllListEntries(final List<GermplasmListData> listEntries) {
		final List<GermplasmListData> entries;
		try {
			entries = this.inventoryDataManager.getLotCountsForList(this.germplasmList.getId(), 0,
					Long.valueOf(this.listEntriesCount).intValue());

			listEntries.addAll(entries);
		} catch (final MiddlewareQueryException ex) {
			ListComponent.LOG.error("Error with retrieving list entries for list: " + this.germplasmList.getId(), ex);
			throw ex;
		}
	}

	@Override
	public void addListeners() {
		this.viewHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.openViewListHeaderWindow();
			}
		});

		final ListComponentFillWithSource fillWithSource =
				new ListComponentFillWithSource(this, this.listDataTable, ColumnLabels.GID.getName());
		this.fillWith = new FillWith(fillWithSource, this.parentListDetailsComponent, this.messageSource);
		this.fillWith.setContextMenuEnabled(this.listDataTable, !this.germplasmList.isLockedList());

		this.makeTableEditable();

		this.actionsButton.addListener(new ToolsButtonClickListener());

		this.inventoryViewToolsButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 272707576878821700L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.inventoryViewMenu.show(event.getClientX(), event.getClientY());
			}
		});

		this.menu.addListener(new MenuClickListener());

		this.inventoryViewMenu.addListener(new InventoryViewMenuClickListner());

		this.editHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -788407324474054727L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.openSaveListAsDialog();
			}
		});

		this.lockButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				try {
					ListComponent.this.toggleGermplasmListStatus();
				} catch (final MiddlewareQueryException mqe) {
					ListComponent.LOG.error("Error with locking list.", mqe);
					MessageNotifier.showError(ListComponent.this.getWindow(), ListComponent.DATABASE_ERROR,
							"Error with locking list. " + ListComponent.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				}
			}
		});

		this.unlockButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				try {
					ListComponent.this.toggleGermplasmListStatus();
				} catch (final MiddlewareQueryException mqe) {
					ListComponent.LOG.error("Error with unlocking list.", mqe);
					MessageNotifier.showError(ListComponent.this.getWindow(), ListComponent.DATABASE_ERROR,
							"Error with unlocking list. " + ListComponent.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				}
			}
		});

		this.tableContextMenu.addListener(new TableContextMenuClickListener());

		this.getListDataTableWithSelectAll().getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				ListComponent.this.updateNoOfSelectedEntries();
			}
		});

		this.listInventoryTable.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				ListComponent.this.updateNoOfSelectedEntries();
			}
		});

	}

	@Override
	public void layoutComponents() {
		this.headerLayout = new HorizontalLayout();
		this.headerLayout.setDebugId("headerLayout");
		this.headerLayout.setWidth("100%");
		this.headerLayout.setSpacing(true);

		final HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.topLabel);
		headingLayout.setDebugId("headingLayout");
		this.headerLayout.addComponent(headingLayout);

		this.toolsMenuContainer = new HorizontalLayout();
		this.toolsMenuContainer.setDebugId("toolsMenuContainer");
		this.toolsMenuContainer.setWidth("90px");
		this.toolsMenuContainer.setHeight("27px");
		this.toolsMenuContainer.addComponent(this.actionsButton);

		this.headerLayout.addComponent(this.toolsMenuContainer);
		this.headerLayout.setComponentAlignment(this.toolsMenuContainer, Alignment.BOTTOM_LEFT);

		this.headerLayout.setExpandRatio(headingLayout, 1.0f);

		final HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setDebugId("leftSubHeaderLayout");
		leftSubHeaderLayout.setSpacing(true);
		leftSubHeaderLayout.addComponent(this.totalListEntriesLabel);
		leftSubHeaderLayout.addComponent(this.totalSelectedListEntriesLabel);
		leftSubHeaderLayout.setComponentAlignment(this.totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		leftSubHeaderLayout.setComponentAlignment(this.totalSelectedListEntriesLabel, Alignment.MIDDLE_LEFT);

		final HorizontalLayout rightSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setDebugId("rightSubHeaderLayout");
		leftSubHeaderLayout.setSpacing(true);
		rightSubHeaderLayout.addComponent(this.viewHeaderButton);
		rightSubHeaderLayout.setComponentAlignment(this.viewHeaderButton, Alignment.MIDDLE_RIGHT);
		rightSubHeaderLayout.addComponent(this.editHeaderButton);
		rightSubHeaderLayout.setComponentAlignment(this.editHeaderButton, Alignment.MIDDLE_RIGHT);

		if (this.localUserIsListOwner() || this.UserHasInstancesRole(this.localUserId)) {
			rightSubHeaderLayout.addComponent(this.lockButton);
			rightSubHeaderLayout.setComponentAlignment(this.lockButton, Alignment.MIDDLE_RIGHT);

			rightSubHeaderLayout.addComponent(this.unlockButton);
			rightSubHeaderLayout.setComponentAlignment(this.unlockButton, Alignment.MIDDLE_RIGHT);
		}

		this.setLockedState(this.germplasmList.isLockedList());

		this.subHeaderLayout = new HorizontalLayout();
		this.subHeaderLayout.setDebugId("subHeaderLayout");
		this.subHeaderLayout.setWidth("100%");
		this.subHeaderLayout.setSpacing(true);
		this.subHeaderLayout.addStyleName("lm-list-desc");
		this.subHeaderLayout.addComponent(leftSubHeaderLayout);
		this.subHeaderLayout.addComponent(rightSubHeaderLayout);

		this.subHeaderLayout.setComponentAlignment(leftSubHeaderLayout, Alignment.MIDDLE_LEFT);
		this.subHeaderLayout.setComponentAlignment(rightSubHeaderLayout, Alignment.MIDDLE_RIGHT);

		this.addComponent(this.headerLayout);
		this.addComponent(this.subHeaderLayout);

		this.listDataTable.setHeight("460px");

		this.addComponent(this.getListDataTableWithSelectAll());
		this.addComponent(this.listInventoryTable);
		this.addComponent(this.tableContextMenu);

		this.parentListDetailsComponent.addComponent(this.menu);
		this.parentListDetailsComponent.addComponent(this.inventoryViewMenu);
	}

	private boolean UserHasInstancesRole(final Integer userId) {
		final WorkbenchUser workbenchUser = this.userService.getUserById(userId);
		final List<UserRole> userRoles = workbenchUser.getRoles();
		for (UserRole userRole : userRoles) {
			if (userRole.getRole().getRoleType().getId().equals(RoleType.INSTANCE.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateLabels() {
		// not yet implemented
	}

	private boolean localUserIsListOwner() {
		return this.germplasmList.getUserId().equals(this.localUserId);
	}

	public void makeTableEditable() {

		this.listDataTable.addListener(new ListDataTableItemClickListener());

		this.listDataTable.setTableFieldFactory(new ListDataTableFieldFactory());

		this.listDataTable.setEditable(true);
	}

	final class ListDataTableFieldFactory implements TableFieldFactory {

		private static final long serialVersionUID = 1L;

		@Override
		public Field createField(final Container container, final Object itemId, final Object propertyId, final Component uiContext) {

			if (this.isNonEditableColumn(propertyId)) {
				return null;
			}

			final TextField tf = new TextField();
			tf.setDebugId("tf");
			tf.setData(new ItemPropertyId(itemId, propertyId));

			// set the size of textfield based on text of cell
			final String value = (String) container.getItem(itemId).getItemProperty(propertyId).getValue();
			final Double d = this.computeTextFieldWidth(value);
			tf.setWidth(d.floatValue(), Sizeable.UNITS_EM);

			// Needed for the generated column
			tf.setImmediate(true);

			// Manage the field in the field storage
			Map<Object, Field> itemMap = ListComponent.this.fields.get(itemId);
			if (itemMap == null) {
				itemMap = new HashMap<>();
				ListComponent.this.fields.put(itemId, itemMap);
			}
			itemMap.put(propertyId, tf);

			tf.setReadOnly(true);

			tf.addListener(new FocusListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void focus(final FocusEvent event) {
					ListComponent.this.listDataTable.select(itemId);
				}
			});

			tf.addListener(new FocusListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void focus(final FocusEvent event) {
					ListComponent.this.lastCellvalue = ((TextField) event.getComponent()).getValue().toString();
				}
			});

			tf.addListener(new TextFieldBlurListener(tf, itemId));
			// this area can be used for validation
			tf.addListener(new Property.ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void valueChange(final ValueChangeEvent event) {
					final Double d = ListDataTableFieldFactory.this.computeTextFieldWidth(tf.getValue().toString());
					tf.setWidth(d.floatValue(), Sizeable.UNITS_EM);
					tf.setReadOnly(true);

					if (ListComponent.this.doneInitializing && !tf.getValue().toString().equals(ListComponent.this.lastCellvalue)) {
						ListComponent.this.setHasUnsavedChanges(true);
					}
				}
			});

			tf.addShortcutListener(new ShortcutListener("ENTER", ShortcutAction.KeyCode.ENTER, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(final Object sender, final Object target) {
					final Double d = ListDataTableFieldFactory.this.computeTextFieldWidth(tf.getValue().toString());
					tf.setWidth(d.floatValue(), Sizeable.UNITS_EM);
					tf.setReadOnly(true);
					ListComponent.this.listDataTable.focus();

				}
			});

			return tf;
		}

		boolean isNonEditableColumn(final Object propertyId) {
			return propertyId.equals(ColumnLabels.GID.getName()) || propertyId.equals(ColumnLabels.ENTRY_ID.getName())
					|| propertyId.equals(ColumnLabels.DESIGNATION.getName()) || propertyId.equals(ColumnLabels.GROUP_ID.getName())
					|| ListComponent.this.isInventoryColumn(propertyId);
		}

		private Double computeTextFieldWidth(final String value) {
			double multiplier = 0.55;
			int length = 1;
			if (value != null && !value.isEmpty()) {
				length = value.length();
				if (value.equalsIgnoreCase(value)) {
					// if all caps, provide bigger space
					multiplier = 0.75;
				}
			}
			final Double d = length * multiplier;
			// set a minimum textfield width
			return NumberUtils.max(new double[] {ListComponent.MINIMUM_WIDTH, d});
		}
	}

	protected final class InventoryViewMenuClickListner implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final ClickEvent event) {

			final ContextMenuItem clickedItem = event.getClickedItem();

			if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SAVE_RESERVATIONS))) {

				/*
				 * Save reservation needs to be synchronized on ListComponent lock object. This will ensure lock will apply to all instances
				 * of ListComponent invoking save reservation.
				 */
				synchronized (ListComponent.class) {
					final TransactionTemplate transactionTemplateForSavingReservation =
							new TransactionTemplate(ListComponent.this.transactionManager);

					transactionTemplateForSavingReservation.execute(new TransactionCallbackWithoutResult() {

						@Override
						protected void doInTransactionWithoutResult(final TransactionStatus status) {
							ListComponent.this.saveReservationChangesAction(ListComponent.this.getWindow());
						}
					});
				}

			} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.CLOSE_LOTS))) {
				synchronized (ListComponent.class) {
					final TransactionTemplate transactionTemplateForSavingReservation =
							new TransactionTemplate(ListComponent.this.transactionManager);

					transactionTemplateForSavingReservation.execute(new TransactionCallbackWithoutResult() {

						@Override
						protected void doInTransactionWithoutResult(final TransactionStatus status) {
							ListComponent.this.closeLotsActions();
						}
					});

					ListComponent.this.resetListInventoryTableValues();
					ListComponent.this.resetListDataTableValues();

				}
			} else {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(ListComponent.this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(final TransactionStatus status) {
						if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))) {
							ListComponent.this.viewListAction();
						} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.COPY_TO_LIST))) {
							ListComponent.this.copyToNewListFromInventoryViewAction();
						} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.RESERVE_INVENTORY))) {
							ListComponent.this.reserveInventoryAction();
						} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
							ListComponent.this.listInventoryTable.getTable()
									.setValue(ListComponent.this.listInventoryTable.getTable().getItemIds());
						} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.CANCEL_RESERVATIONS))) {
							ListComponent.this.cancelReservationsAction();
						} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.EXPORT_SEED_LIST))) {
							ListComponent.this.exportSeedPreparationList();
						} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.IMPORT_SEED_LIST))) {
							ListComponent.this.openImportSeedPreparationDialog();
						} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.PRINT_LABELS))) {
							ListComponent.this.createLabelsAction();
						}

					}
				});
			}

		}
	}

	private final class MenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final ClickEvent event) {

			final TransactionTemplate transactionTemplate = new TransactionTemplate(ListComponent.this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {
					// Get reference to clicked item
					final ContextMenuItem clickedItem = event.getClickedItem();
					if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						ListComponent.this.listDataTable.setValue(ListComponent.this.listDataTable.getItemIds());
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.EXPORT_LIST))) {
						ListComponent.this.exportListAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.COPY_TO_LIST))) {
						ListComponent.this.copyToNewListAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.ADD_ENTRIES))) {
						ListComponent.this.addEntryButtonClickAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SAVE_CHANGES))) {
						ListComponent.this.saveChangesAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))) {
						ListComponent.this.deleteEntriesButtonClickAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.GROUP))) {
						ListComponent.this.markLinesAsFixedAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.UNGROUP))) {
						ListComponent.this.confirmUnfixLinesAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.ASSIGN_CODES))) {
						ListComponent.this.assignCodesAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.EDIT_LIST))) {
						ListComponent.this.editListButtonClickAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.DELETE_LIST))) {
						ListComponent.this.deleteListButtonClickAction();
					} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.INVENTORY_VIEW))) {
						ListComponent.this.viewInventoryAction();
					} else if (clickedItem.getName()
							.equals(ListComponent.this.messageSource.getMessage(Message.REMOVE_SELECTED_GERMPLASM))) {
						ListComponent.this.removeSelectedGermplasmButtonClickAction();
					}
				}
			});
		}
	}

	protected void createLabelsAction() {
		ListCommonActionsUtil.handleCreateLabelsAction(this.germplasmList.getId(), this.inventoryDataManager, this.messageSource,
				this.contextUtil, this.getApplication(), this.getWindow());
	}

	private final class ToolsButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 272707576878821700L;

		@Override
		public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
			ListComponent.this.addColumnContextMenu.refreshAddColumnMenu(ListComponent.this.listDataTable);
			ListComponent.this.menu.show(event.getClientX(), event.getClientY());

			// update list view action menu based on the following criteria:
			// 1. If it is loaded directly from the url
			// 2. If the list in Build New List section is locked or not
			// 3. If the list manager main source is existing (I think 'source' will be null only for test purposes)
			ListComponent.this.menu.updateListViewActionMenu(ListComponent.this.fromUrl, ListComponent.this.source.listBuilderIsLocked(),
					ListComponent.this.source != null);

			// when the Germplasm List is not locked, and when not accessed
			// directly from URL or popup window
			if (!ListComponent.this.germplasmList.isLockedList() && !ListComponent.this.fromUrl) {
				ListComponent.this.menu.setActionMenuWhenListIsUnlocked(ListComponent.this.localUserIsListOwner());
				ListComponent.this.addColumnContextMenu.showHideAddColumnMenu(true);
			} else {
				ListComponent.this.menu.setActionMenuWhenListIsLocked();
				ListComponent.this.addColumnContextMenu.showHideAddColumnMenu(false);
			}
		}
	}

	private final class ListDataTableItemClickListener implements ItemClickListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void itemClick(final ItemClickEvent event) {
			ListComponent.this.selectedColumn = event.getPropertyId();
			ListComponent.this.selectedItemId = event.getItemId();

			// Only perform action if mouse event is right click
			if (event.getButton() != com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT) {
				return;
			}

			ListComponent.this.tableContextMenu.show(event.getClientX(), event.getClientY());

			// Note: Code change looks like I changed the logic but I've just condensed it by calculating the
			// boolean conditions and set it to the setVisible methods without going through if else code blocks
			// this solves the "Reduce the number of conditional operators (5) used in the expression" sonar violation
			final boolean isNonEditableColumn = ListComponent.this.selectedColumn.equals(ColumnLabels.TAG.getName())
					|| ListComponent.this.selectedColumn.equals(ColumnLabels.GID.getName())
					|| ListComponent.this.selectedColumn.equals(ColumnLabels.ENTRY_ID.getName())
					|| ListComponent.this.selectedColumn.equals(ColumnLabels.GROUP_ID.getName())
					|| ListComponent.this.selectedColumn.equals(ColumnLabels.DESIGNATION.getName())
					|| ListComponent.this.isInventoryColumn(ListComponent.this.selectedColumn);

			final boolean isLockedList = ListComponent.this.germplasmList.isLockedList();
			final boolean isListBuilderLocked = ListComponent.this.source.listBuilderIsLocked();
			final boolean isListComponentSourceAvailable = ListComponent.this.source != null;

			ListComponent.this.tableContextMenu.updateGermplasmListTableContextMenu(isNonEditableColumn, isLockedList, isListBuilderLocked,
					isListComponentSourceAvailable);

			// set doneInitializing to true if germplasm list is locked, else do not update doneInitializing
			ListComponent.this.doneInitializing = isLockedList ? true : ListComponent.this.doneInitializing;

		}
	}

	private final class TableContextMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(ListComponent.this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {
					final String action = event.getClickedItem().getName();
					if (action.equals(ListComponent.this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))) {
						ListComponent.this.deleteEntriesButtonClickAction();
					} else if (action.equals(ListComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						ListComponent.this.listDataTable.setValue(ListComponent.this.listDataTable.getItemIds());
					} else if (action.equals(ListComponent.this.messageSource.getMessage(Message.EDIT_VALUE))) {

						final Map<Object, Field> itemMap = ListComponent.this.fields.get(ListComponent.this.selectedItemId);

						// go through each field, set previous edited fields to
						// blurred/readonly
						for (final Map.Entry<Object, Field> entry : itemMap.entrySet()) {
							final Field f = entry.getValue();
							final Object fieldValue = f.getValue();
							if (!f.isReadOnly()) {
								f.setReadOnly(true);

								if (!fieldValue.equals(ListComponent.this.lastCellvalue)) {
									ListComponent.this.setHasUnsavedChanges(true);
								}
							}
						}

						// Make the entire item editable

						if (itemMap != null) {
							for (final Map.Entry<Object, Field> entry : itemMap.entrySet()) {
								final Object column = entry.getKey();
								if (column.equals(ListComponent.this.selectedColumn)) {
									final Field f = entry.getValue();
									if (f.isReadOnly()) {
										final Object fieldValue = f.getValue();
										ListComponent.this.lastCellvalue = fieldValue != null ? fieldValue.toString() : "";
										f.setReadOnly(false);
										f.focus();
									}
								}
							}
						}

						ListComponent.this.listDataTable.select(ListComponent.this.selectedItemId);
					} else if (action.equals(ListComponent.this.messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST))) {
						ListComponent.this.source.addSelectedPlantsToList(ListComponent.this.listDataTable);
					}
				}
			});

		}
	}

	private final class TextFieldBlurListener implements BlurListener {

		private final TextField tf;
		private final Object itemId;
		private static final long serialVersionUID = 1L;

		private TextFieldBlurListener(final TextField tf, final Object itemId) {
			this.tf = tf;
			this.itemId = itemId;
		}

		@Override
		public void blur(final BlurEvent event) {
			final Map<Object, Field> itemMap = ListComponent.this.fields.get(this.itemId);

			// go through each field, set previous edited fields to
			// blurred/readonly
			for (final Map.Entry<Object, Field> entry : itemMap.entrySet()) {
				final Field f = entry.getValue();
				final Object fieldValue = f.getValue();
				if (!f.isReadOnly()) {
					f.setReadOnly(true);
					if (!fieldValue.equals(ListComponent.this.lastCellvalue)) {
						ListComponent.this.setHasUnsavedChanges(true);
					}
				}
			}

			for (final Map.Entry<Object, Field> entry : itemMap.entrySet()) {
				final Object column = entry.getKey();
				final Field f = entry.getValue();
				final Object fieldValue = f.getValue();

				// mark list as changed if value for the cell was
				// changed
				if (column.equals(ListComponent.this.selectedColumn) && !f.isReadOnly()
						&& !fieldValue.toString().equals(ListComponent.this.lastCellvalue)) {
					ListComponent.this.setHasUnsavedChanges(true);
				}

				// validate for designation
				if (column.equals(ListComponent.this.selectedColumn)
						&& ListComponent.this.selectedColumn.equals(ColumnLabels.DESIGNATION.getName())) {
					final Object eventSource = event.getSource();
					final String designation = eventSource.toString();

					// retrieve item id at event source
					final ItemPropertyId itemProp = (ItemPropertyId) ((TextField) eventSource).getData();
					final Object sourceItemId = itemProp.getItemId();

					final String[] items = ListComponent.this.listDataTable.getItem(sourceItemId).toString().split(" ");
					final int gid = Integer.valueOf(items[2]);

					if (ListComponent.this.isDesignationValid(designation, gid)) {
						final Double d = this.computeTextFieldWidth(f.getValue().toString());
						f.setWidth(d.floatValue(), Sizeable.UNITS_EM);
						f.setReadOnly(true);
						ListComponent.this.listDataTable.focus();
					} else {
						ConfirmDialog.show(ListComponent.this.getWindow(), "Update Designation",
								"The value you entered is not one of the germplasm names. "
										+ "Are you sure you want to update Designation with new value?",
								"Yes", "No", new ConfirmDialog.Listener() {

									private static final long serialVersionUID = 1L;

									@Override
									public void onClose(final ConfirmDialog dialog) {
										if (!dialog.isConfirmed()) {
											TextFieldBlurListener.this.tf.setReadOnly(false);
											TextFieldBlurListener.this.tf.setValue(ListComponent.this.lastCellvalue);
										} else {
											final Double d = TextFieldBlurListener.this
													.computeTextFieldWidth(TextFieldBlurListener.this.tf.getValue().toString());
											TextFieldBlurListener.this.tf.setWidth(d.floatValue(), Sizeable.UNITS_EM);
										}
										TextFieldBlurListener.this.tf.setReadOnly(true);
										ListComponent.this.listDataTable.focus();
									}
								});
					}
				} else {
					final Double d = this.computeTextFieldWidth(f.getValue().toString());
					f.setWidth(d.floatValue(), Sizeable.UNITS_EM);
					f.setReadOnly(true);
				}
			}

		}

		private Double computeTextFieldWidth(final String value) {
			double multiplier = 0.55;
			int length = 1;
			if (value != null && !value.isEmpty()) {
				length = value.length();
				if (value.equalsIgnoreCase(value)) {
					// if all caps, provide bigger space
					multiplier = 0.75;
				}
			}
			final Double d = length * multiplier;
			// set a minimum textfield width
			return NumberUtils.max(new double[] {ListComponent.MINIMUM_WIDTH, d});
		}
	}

	// This is needed for storing back-references
	class ItemPropertyId {

		final Object itemId;
		final Object propertyId;

		public ItemPropertyId(final Object itemId, final Object propertyId) {
			this.itemId = itemId;
			this.propertyId = propertyId;
		}

		public Object getItemId() {
			return this.itemId;
		}

		public Object getPropertyId() {
			return this.propertyId;
		}
	}

	public boolean isDesignationValid(final String designation, final int gid) {
		final List<Name> germplasms;
		final List<String> designations = new ArrayList<>();

		try {
			germplasms = this.germplasmDataManager.getNamesByGID(gid, null, null);

			for (final Name germplasm : germplasms) {
				designations.add(germplasm.getNval());
			}

			for (final String nameInDb : designations) {
				if (GermplasmDataManagerUtil.compareGermplasmNames(designation, nameInDb)) {
					return true;
				}
			}

		} catch (final Exception e) {
			ListComponent.LOG.error("Database error!", e);
			MessageNotifier.showError(this.getWindow(), ListComponent.DATABASE_ERROR,
					"Error with validating designation." + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

		return false;
	}

	public boolean isInventoryColumn(final Object propertyId) {
		return propertyId.equals(ColumnLabels.AVAILABLE_INVENTORY.getName()) || propertyId.equals(ColumnLabels.SEED_RESERVATION.getName())
				|| propertyId.equals(ColumnLabels.STOCKID.getName());
	}

	public void deleteEntriesButtonClickAction() {
		final Collection<?> selectedIdsToDelete = (Collection<?>) this.listDataTable.getValue();

		if (!selectedIdsToDelete.isEmpty()) {
			ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.DELETE_GERMPLASM_ENTRIES),
					this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES_FROM_THE_LIST_CONFIRM), this.messageSource.getMessage(Message.YES),
					this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(final ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								ListComponent.this.removeRowsInListDataTable((Collection<?>) ListComponent.this.listDataTable.getValue());
							}
						}

					});

		} else {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DELETING_LIST_ENTRIES),
					this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
		}
	}

	public void markLinesAsFixedAction() {

		final Set<Integer> gidsToProcess = this.extractGidListFromListDataTable(this.listDataTable);

		if (!gidsToProcess.isEmpty()) {
			this.getWindow().addWindow(new GermplasmGroupingComponent(this, gidsToProcess));
		} else {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.GROUP),
					this.messageSource.getMessage(Message.ERROR_MARK_LINES_AS_FIXED_NOTHING_SELECTED));
		}
	}

	public void confirmUnfixLinesAction() {

		final Set<Integer> gidsToProcess = this.extractGidListFromListDataTable(this.listDataTable);

		if (!gidsToProcess.isEmpty()) {

			ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.UNGROUP),
					this.messageSource.getMessage(Message.CONFIRM_UNFIX_LINES), this.messageSource.getMessage(Message.YES),
					this.messageSource.getMessage(Message.NO), new ConfirmUnfixLinesListener(gidsToProcess, this));

		} else {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_UNFIX_LINES),
					this.messageSource.getMessage(Message.ERROR_UNFIX_LINES_NOTHING_SELECTED));
		}

	}

	protected void unfixLines(final Set<Integer> gidsToProcess) {

		final int numberOfGermplasmWithoutGroup = this.countGermplasmWithoutGroup(gidsToProcess);

		if (numberOfGermplasmWithoutGroup > 0) {
			MessageNotifier.showWarning(this.getWindow(), "", this.messageSource.getMessage(Message.WARNING_UNFIX_LINES));
		}

		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {

				ListComponent.this.germplasmGroupingService.unfixLines(gidsToProcess);

			}
		});

		final int numberOfUnfixedGermplasm = gidsToProcess.size() - numberOfGermplasmWithoutGroup;
		MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.UNGROUP),
				this.messageSource.getMessage(Message.SUCCESS_UNFIX_LINES, numberOfUnfixedGermplasm));

	}

	protected int countGermplasmWithoutGroup(final Set<Integer> gidsToProcess) {

		final List<Germplasm> listOfGermplasmWithoutGroup =
				this.germplasmDataManager.getGermplasmWithoutGroup(new ArrayList<Integer>(gidsToProcess));

		return listOfGermplasmWithoutGroup.size();

	}

	public void assignCodesAction() {

		final Set<Integer> gidsToProcess = this.extractGidListFromListDataTable(this.listDataTable);

		if (!gidsToProcess.isEmpty()) {
			this.getWindow().addWindow(new AssignCodesDialog(gidsToProcess));

		} else {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ASSIGN_CODES),
					this.messageSource.getMessage(Message.ERROR_ASSIGN_CODES_NOTHING_SELECTED));
		}
	}

	/**
	 * Extracts the GIDs from the ListDataTable, the order of GIDs would follow the order of entries in the table.
	 *
	 * @param listDataTable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Set<Integer> extractGidListFromListDataTable(final Table listDataTable) {

		final List<Integer> sortedList = new ArrayList();
		final List originalList = new ArrayList();

		final Collection<Integer> selectedTableRows = (Collection<Integer>) listDataTable.getValue();
		sortedList.addAll(selectedTableRows);
		originalList.addAll(listDataTable.getItemIds());

		Collections.sort(sortedList, new Comparator<Object>() {

			@Override
			public int compare(final Object o1, final Object o2) {
				return originalList.indexOf(o1) - originalList.indexOf(o2);
			}
		});
		// We use linkedHashSet to preserve the insertion order of GIDs based on order of the entries in the list.
		// It's possible that the entries don't have a sequential GIDs so we cannot base the sequence of coded name through a sorted GID
		// list.
		// The sequence number in the coded name should follow the same order as the entries in the list.
		final Set<Integer> gidsToProcess = new LinkedHashSet<>();

		for (final Integer selectedRowId : sortedList) {
			final Item selectedRowItem = this.listDataTable.getItem(selectedRowId);
			final Button gidCell = (Button) selectedRowItem.getItemProperty(ColumnLabels.GID.getName()).getValue();
			if (gidCell != null) {
				gidsToProcess.add(Integer.valueOf(gidCell.getCaption()));
			}
		}

		return gidsToProcess;

	}

	private void removeRowsInListDataTable(final Collection<?> selectedIds) {
		// marks that there is a change in listDataTable
		this.setHasUnsavedChanges(true);

		if (this.listDataTable.getItemIds().size() == selectedIds.size()) {
			this.listDataTable.getContainerDataSource().removeAllItems();
		} else {
			// marks the entryId and designationId of the list entries to delete
			for (final Object itemId : selectedIds) {
				final Button desigButton =
						(Button) this.listDataTable.getItem(itemId).getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
				final String designation = String.valueOf(desigButton.getCaption());
				this.itemsToDelete.put(itemId, designation);
				this.listDataTable.getContainerDataSource().removeItem(itemId);
			}
		}
		// reset selection
		this.listDataTable.setValue(null);

		this.renumberEntryIds();
		this.listDataTable.requestRepaint();
		this.updateNoOfEntries();
	}

	private void renumberEntryIds() {
		Integer entryId = 1;
		for (final Iterator<?> i = this.listDataTable.getItemIds().iterator(); i.hasNext();) {
			final int listDataId = (Integer) i.next();
			final Item item = this.listDataTable.getItem(listDataId);
			item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);
			entryId += 1;
		}
	}

	/* MENU ACTIONS */
	private void editListButtonClickAction() {
		final ListBuilderComponent listBuilderComponent = this.source.getListBuilderComponent();

		if (listBuilderComponent.hasUnsavedChanges()) {
			final String message;

			final String buildNewListTitle = listBuilderComponent.getBuildNewListTitle().getValue().toString();
			if (buildNewListTitle.equals(this.messageSource.getMessage(Message.BUILD_A_NEW_LIST))) {
				message =
						"You have unsaved changes to the current list you are building. Do you want to save your changes before proceeding to your next list to edit?";
			} else {
				message =
						"You have unsaved changes to the list you are editing. Do you want to save your changes before proceeding to your next list to edit?";
			}

			ConfirmDialog.show(this.getWindow(), "Unsaved Changes", message, "Yes", "No", new ConfirmDialog.Listener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClose(final ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						// save the existing list
						listBuilderComponent.getSaveButton().click();
					}

					ListComponent.this.source.loadListForEditing(ListComponent.this.getGermplasmList());
				}
			});
		} else {
			this.source.loadListForEditing(this.getGermplasmList());
		}
	}

	private void exportListAction() {
		final ExportListAsDialog exportListAsDialog = new ExportListAsDialog(this.source, this.germplasmList, this.listDataTable);
		exportListAsDialog.setDebugId("exportListAsDialog");
		this.getWindow().addWindow(exportListAsDialog);
	}

	protected void removeSelectedGermplasmButtonClickAction() {
		final Collection<?> selectedIdsToDelete = (Collection<?>) this.listDataTable.getValue();

		if (!selectedIdsToDelete.isEmpty()) {
			final RemoveSelectedGermplasmAsDialog removeSelectedGermplasmAsDialog =
					new RemoveSelectedGermplasmAsDialog(this.source, this.germplasmList, this.listDataTable, this.totalListEntriesLabel);
			removeSelectedGermplasmAsDialog.setDebugId("removeSelectedGermplasmAsDialog");
			this.getWindow().addWindow(removeSelectedGermplasmAsDialog);

		} else {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_REMOVING_GERMPLASM),
					this.messageSource.getMessage(Message.ERROR_GERMPLASM_MUST_BE_SELECTED));
		}
	}

	public void exportSeedPreparationList(final SeedInventoryListExporter seedInventoryListExporter) {

		if (!CollectionUtils.isEmpty(this.validReservationsToSave)) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.UNSAVED_RESERVATION_WARNING));
		}

		try {
			seedInventoryListExporter.exportSeedPreparationList();
		} catch (final SeedInventoryExportException ex) {
			ListComponent.LOG.debug(ex.getMessage(), ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
					"Cannot Export Seed Preparation List :" + ex.getMessage());
		}

	}

	public void exportSeedPreparationList() {
		this.exportSeedPreparationList(new SeedInventoryListExporter(this.source, this.germplasmList));
	}

	private void openImportSeedPreparationDialog() {
		final Window window = this.getWindow();
		final SeedInventoryImportFileComponent seedInventoryImportFileComponent =
				new SeedInventoryImportFileComponent(this.source, this, this.germplasmList);
		seedInventoryImportFileComponent.setDebugId("seedInventoryImportFileComponent");
		window.addWindow(seedInventoryImportFileComponent);

	}

	protected void setLockedState(final boolean locked) {
		this.lockButton.setVisible(!locked);
		this.unlockButton.setVisible(locked);
		this.editHeaderButton.setVisible(!locked);
		this.fillWith.setContextMenuEnabled(this.listDataTable, !locked);
	}

	private void copyToNewListAction() {
		final Collection<?> newListEntries = (Collection<?>) this.listDataTable.getValue();
		if (newListEntries == null || newListEntries.isEmpty()) {
			MessageNotifier.showRequiredFieldError(this.getWindow(),
					this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));

		} else {
			this.listManagerCopyToListDialog = new BaseSubWindow(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
			this.listManagerCopyToListDialog.setDebugId("listManagerCopyToListDialog");
			this.listManagerCopyToListDialog.setOverrideFocus(true);
			this.listManagerCopyToListDialog.setModal(true);
			this.listManagerCopyToListDialog.setWidth("617px");
			this.listManagerCopyToListDialog.setHeight("230px");
			this.listManagerCopyToListDialog.setResizable(false);
			this.listManagerCopyToListDialog.addStyleName(Reindeer.WINDOW_LIGHT);

			try {
				this.listManagerCopyToListDialog.addComponent(
						new ListManagerCopyToListDialog(this.parentListDetailsComponent.getWindow(), this.listManagerCopyToListDialog,
								this.germplasmList.getName(), this.listDataTable, this.contextUtil.getCurrentWorkbenchUserId(), this.source));
				this.parentListDetailsComponent.getWindow().addWindow(this.listManagerCopyToListDialog);
				this.listManagerCopyToListDialog.center();
			} catch (final MiddlewareQueryException e) {
				ListComponent.LOG.error("Error copying list entries.", e);
				ListComponent.LOG.error("\n" + e.getStackTrace());
			}
		}
	}

	private void copyToNewListFromInventoryViewAction() {
		// do nothing
	}

	private void addEntryButtonClickAction() {
		final Window parentWindow = this.getWindow();
		final AddEntryDialog addEntriesDialog = new AddEntryDialog(this.addEntryDialogSource, parentWindow);
		addEntriesDialog.setDebugId("addEntriesDialog");
		addEntriesDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		addEntriesDialog.focusOnSearchField();
		parentWindow.addWindow(addEntriesDialog);
	}

	public void saveChangesAction() {
		this.saveChangesAction(this.getWindow());
	}

	public Boolean saveChangesAction(final Window window) {
		return this.saveChangesAction(window, true);
	}

	public Boolean saveChangesAction(final Window window, final Boolean showSuccessMessage) {

		this.deleteRemovedGermplasmEntriesFromTable();
		final List<GermplasmListData> listEntries;
		try {
			listEntries = this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId());
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES);
		}

		int entryId = 1;
		// re-assign "Entry ID" field based on table's sorting
		for (final Iterator<?> i = this.listDataTable.getItemIds().iterator(); i.hasNext();) {
			// iterate through the table elements' IDs
			final int listDataId = (Integer) i.next();

			// update table item's entryId
			final Item item = this.listDataTable.getItem(listDataId);
			item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);

			// then find the corresponding ListData and assign a new entryId to
			// it
			for (final GermplasmListData listData : listEntries) {
				if (listData.getId().equals(listDataId)) {
					listData.setEntryId(entryId);

					final String entryCode = (String) item.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue();
					if (entryCode != null && entryCode.length() != 0) {
						listData.setEntryCode(entryCode);
					} else {
						listData.setEntryCode(Integer.valueOf(entryId).toString());
					}

					final String seedSource = (String) item.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue();
					if (seedSource != null && seedSource.length() != 0) {
						listData.setSeedSource(seedSource);
					} else {
						listData.setSeedSource("-");
					}

					final Button desigButton = (Button) item.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
					final String designation = String.valueOf(desigButton.getCaption());
					if (designation != null && designation.length() != 0) {
						listData.setDesignation(designation);
					} else {
						listData.setDesignation("-");
					}

					String parentage = (String) item.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
					if (parentage != null && parentage.length() != 0) {
						if (parentage.length() > 255) {
							parentage = parentage.substring(0, 255);
						}
						listData.setGroupName(parentage);
					} else {
						listData.setGroupName("-");
					}

					break;
				}
			}
			entryId += 1;
		}
		// save the list of Germplasm List Data to the database
		try {
			this.germplasmListManager.updateGermplasmListData(listEntries);
			this.germplasmListManager.saveListDataColumns(
					this.addColumnContextMenu.getListDataCollectionFromTable(this.listDataTable, this.attributeAndNameTypeColumns));

			if (!CollectionUtils.isEmpty(this.validReservationsToSave)) {
				this.reserveInventoryAction.saveReserveTransactions(this.getValidReservationsToSave(), this.germplasmList.getId());
				this.validReservationsToSave.clear();
			}

			this.listDataTable.requestRepaint();
			// reset flag to indicate unsaved changes
			this.setHasUnsavedChanges(true);

			if (showSuccessMessage) {
				MessageNotifier.showMessage(window, this.messageSource.getMessage(Message.SUCCESS),
						this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SAVING_SUCCESS), 3000);
			}

		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES);
		}

		// Update counter
		this.updateNoOfEntries();

		this.setHasUnsavedChanges(false);

		this.refreshTreeOnSave();

		return true;
	}

	// saveChangesAction()
	// might be possible to eliminate this method altogether and reduce the
	// number of middleware calls
	private void performListEntriesDeletion(final Map<Object, String> itemsToDelete) {
		try {
			this.designationOfListEntriesDeleted = "";

			for (final Map.Entry<Object, String> item : itemsToDelete.entrySet()) {

				final Object sLRecId = item.getKey();
				final String sDesignation = item.getValue();
				final int lrecId = Integer.valueOf(sLRecId.toString());
				this.designationOfListEntriesDeleted += sDesignation + ",";
				this.deleteGermplasmListDataByListIdLrecId(this.germplasmList.getId(), lrecId);

			}

			this.designationOfListEntriesDeleted =
					this.designationOfListEntriesDeleted.substring(0, this.designationOfListEntriesDeleted.length() - 1);

			// Change entry IDs on listData
			final List<GermplasmListData> listDatas = this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId());
			Integer entryId = 1;
			for (final GermplasmListData listData : listDatas) {
				listData.setEntryId(entryId);
				entryId++;
			}
			this.germplasmListManager.updateGermplasmListData(listDatas);

			this.contextUtil.logProgramActivity("Deleted list entries.",
					"Deleted list entries from the list id " + this.germplasmList.getId() + " - " + this.germplasmList.getName());

			// reset items to delete in listDataTable
			itemsToDelete.clear();

		} catch (final NumberFormatException | MiddlewareQueryException e) {
			ListComponent.LOG.error(ListComponent.ERROR_WITH_DELETING_LIST_ENTRIES, e);
			ListComponent.LOG.error("\n" + e.getStackTrace());
		}
		// end of performListEntriesDeletion
	}

	protected void deleteRemovedGermplasmEntriesFromTable() {
		if (this.listDataTable.getItemIds().isEmpty()) {

			// If the list table is empty, delete all the list entries in the database
			this.germplasmListManager.deleteGermplasmListDataByListId(this.germplasmList.getId());

		} else if (!this.itemsToDelete.isEmpty()) {

			// Delete the removed selected entries individually
			this.performListEntriesDeletion(this.itemsToDelete);
		}
	}

	protected void deleteGermplasmListDataByListIdLrecId(final int listId, final int lrecId) {
		try {
			this.germplasmListManager.deleteGermplasmListDataByListIdLrecId(this.germplasmList.getId(), lrecId);
		} catch (final MiddlewareQueryException e) {
			ListComponent.LOG.error(e.getMessage(), e);
		}
	}

	public void deleteListButtonClickAction() {
		ConfirmDialog.show(this.getWindow(), "Delete Germplasm List:", "Are you sure that you want to delete this list?", "Yes", "No",
				new ConfirmDialog.Listener() {

					private static final long serialVersionUID = -6641772458404494412L;

					@Override
					public void onClose(final ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							ListComponent.this.deleteGermplasmListConfirmed();
						}
					}
				});
	}

	public void deleteGermplasmListConfirmed() {
		if (!this.germplasmList.isLockedList()) {
			try {
				ListCommonActionsUtil.deleteGermplasmList(this.germplasmListManager, this.germplasmList, this.contextUtil, this.getWindow(),
						this.messageSource, "list");

				this.source.getListSelectionComponent().getListTreeComponent().removeListFromTree(this.germplasmList);
				this.source.updateUIForDeletedList(this.germplasmList);
			} catch (final MiddlewareQueryException e) {
				this.getWindow().showNotification("Error", "There was a problem deleting the germplasm list",
						Notification.TYPE_ERROR_MESSAGE);
				ListComponent.LOG.error("Error with deleting germplasmlist.", e);
			}
		}
	}

	/* SETTERS AND GETTERS */

	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	protected void setGermplasmList(final GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	public Integer getGermplasmListId() {
		return this.germplasmList.getId();
	}

	public void toggleGermplasmListStatus() {
		final int toggledStatus;

		if (this.germplasmList.isLockedList()) {
			toggledStatus = this.germplasmList.getStatus() - 100;
		} else {
			toggledStatus = this.germplasmList.getStatus() + 100;
		}

		this.germplasmList = this.germplasmListManager.getGermplasmListById(this.germplasmList.getId());

		this.germplasmList.setStatus(toggledStatus);
		this.germplasmListManager.updateGermplasmList(this.germplasmList);
		this.setLockedState(this.germplasmList.isLockedList());

		if (this.germplasmList.isLockedList()) {
			this.contextUtil.logProgramActivity("Locked a germplasm list.",
					"Locked list " + this.germplasmList.getId() + " - " + this.germplasmList.getName());
		} else {
			this.contextUtil.logProgramActivity("Unlocked a germplasm list.",
					"Unlocked list " + this.germplasmList.getId() + " - " + this.germplasmList.getName());
		}
		this.viewListHeaderWindow.setGermplasmListStatus(this.germplasmList.getStatus());
	}

	public void updateGermplasmListStatus() {
		this.germplasmList = this.germplasmListManager.getGermplasmListById(this.germplasmList.getId());
		this.setLockedState(this.germplasmList.isLockedList());
		this.viewListHeaderWindow.setGermplasmListStatus(this.germplasmList.getStatus());
	}

	public void openSaveListAsDialog() {
		this.dialog = new SaveListAsDialog(this, this.germplasmList, this.messageSource.getMessage(Message.EDIT_LIST_HEADER));
		this.dialog.setDebugId("dialog");
		this.getWindow().addWindow(this.dialog);
	}

	public SaveListAsDialog getSaveListAsDialog() {
		return this.dialog;
	}

	public GermplasmList getCurrentListInSaveDialog() {
		return this.dialog.getGermplasmListToSave();
	}

	@Override
	public void saveList(final GermplasmList list) {

		final GermplasmList savedList =
				ListCommonActionsUtil.overwriteList(list, this.germplasmListManager, this.source, this.messageSource, true);
		if (savedList != null) {
			if (!savedList.getId().equals(this.germplasmList.getId())) {
				ListCommonActionsUtil.overwriteListEntries(savedList, this.germplasmList.getListData(),
						this.germplasmList.getId().intValue() != savedList.getId().intValue(), this.germplasmListManager, this.source,
						this.messageSource, true);
				this.source.closeList(savedList);
			} else {
				this.germplasmList = savedList;
				this.viewListHeaderWindow = new ViewListHeaderWindow(savedList, this.userService.getAllUserIDFullNameMap(),
						this.germplasmListManager.getGermplasmListTypes());
				if (this.viewHeaderButton != null) {
					this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());
				}
			}
		}
		// Refresh tree on save
		this.refreshTreeOnSave();

	}

	@Override
	public void updateListUI() {
		ListCommonActionsUtil.updateGermplasmListStatusUI(this.source);
	}

	protected void refreshTreeOnSave() {
		this.breedingManagerApplication.refreshListManagerTree();
	}

	public void openViewListHeaderWindow() {
		this.getWindow().addWindow(this.viewListHeaderWindow);
	}

	private void updateNoOfEntries(final long count) {
		final String countLabel = "  <b>" + count + "</b>";
		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			if (count == 0) {
				this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
			} else {
				this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + countLabel);
			}
		} else {
			// Inventory View
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LOTS) + ": " + countLabel);
		}
	}

	protected void updateNoOfEntries() {
		final int count;
		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			count = this.listDataTable.getItemIds().size();
		} else {
			// Inventory View
			count = this.listInventoryTable.getTable().size();
		}
		this.updateNoOfEntries(count);
	}

	private void updateNoOfSelectedEntries(final int count) {
		this.totalSelectedListEntriesLabel
				.setValue("<i>" + this.messageSource.getMessage(Message.SELECTED) + ": " + "  <b>" + count + "</b></i>");
	}

	private void updateNoOfSelectedEntries() {
		final int count;

		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			final Collection<?> selectedItems = (Collection<?>) this.getListDataTableWithSelectAll().getTable().getValue();
			count = selectedItems.size();
		} else {
			final Collection<?> selectedItems = (Collection<?>) this.listInventoryTable.getTable().getValue();
			count = selectedItems.size();
		}

		this.updateNoOfSelectedEntries(count);
	}

	@Override
	public void setCurrentlySavedGermplasmList(final GermplasmList list) {
		// not yet implemented
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	/*-------------------------------------LIST INVENTORY RELATED METHODS-------------------------------------*/

	private void viewListAction() {
		if (!this.hasUnsavedChanges()) {
			this.source.setModeView(ModeView.LIST_VIEW);
		} else {
			final String message = "You have unsaved reservations for this list. " + "You will need to save them before changing views. "
					+ "Do you want to save your changes?";

			this.source.showUnsavedChangesConfirmDialog(message, ModeView.LIST_VIEW);

		}
	}

	void changeToListView() {
		final String inventoryLabel = this.messageSource.getMessage(Message.INVENTORY);
		if (inventoryLabel.equals(this.topLabel.getValue().toString())) {
			this.getListDataTableWithSelectAll().setVisible(true);
			this.listInventoryTable.setVisible(false);
			this.toolsMenuContainer.addComponent(this.actionsButton);
			this.toolsMenuContainer.removeComponent(this.inventoryViewToolsButton);

			this.topLabel.setValue(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();
			this.setHasUnsavedChanges(false);
		}
	}

	void changeToInventoryView() {
		final String listEntriesLabel = this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL);
		if (listEntriesLabel.equals(this.topLabel.getValue().toString())) {
			this.getListDataTableWithSelectAll().setVisible(false);
			this.listInventoryTable.setVisible(true);
			this.toolsMenuContainer.removeComponent(this.actionsButton);
			this.toolsMenuContainer.addComponent(this.inventoryViewToolsButton);

			this.topLabel.setValue(this.messageSource.getMessage(Message.INVENTORY));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();
			this.setHasUnsavedChanges(false);
		}
	}

	public void setHasUnsavedChanges(final Boolean hasChanges) {
		this.hasChanges = hasChanges;

		final ListSelectionLayout listSelection = this.source.getListSelectionComponent().getListDetailsLayout();
		listSelection.addUpdateListStatusForChanges(this, this.hasChanges);
	}

	public Boolean hasUnsavedChanges() {
		return this.hasChanges;
	}

	private void viewInventoryAction() {
		if (!this.hasUnsavedChanges()) {
			this.source.setModeView(ModeView.INVENTORY_VIEW);
		} else {
			final String message = "You have unsaved changes to the list you are currently editing.. "
					+ "You will need to save them before changing views. " + "Do you want to save your changes?";
			this.source.showUnsavedChangesConfirmDialog(message, ModeView.INVENTORY_VIEW);
		}
	}

	public void viewInventoryActionConfirmed() {
		// loading the Inventory View page : fetch data from the DB
		final Monitor monitor = MonitorFactory
				.start("org.generationcp.breeding.manager.listmanager.ListComponent.viewInventoryActionConfirmed:loadInventoryData");
		try {
			this.listInventoryTable.loadInventoryData();
		} finally {
			monitor.stop();
		}
		// add inventory values to the existing table
		final Monitor monitor2 = MonitorFactory
				.start("org.generationcp.breeding.manager.listmanager.ListComponent.viewInventoryActionConfirmed:changeToInventoryView");
		try {
			this.changeToInventoryView();
		} finally {
			monitor2.stop();
		}
	}

	public void reserveInventoryAction() {
		// checks if the screen is in the inventory view
		if (!this.inventoryViewMenu.isVisible()) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"Please change to Inventory View first.");
		} else {
			final List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

			if (lotDetailsGid == null || lotDetailsGid.isEmpty()) {
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						"Please select at least 1 lot to reserve.");
			} else {
				// this util handles the inventory reservation related functions
				this.reserveInventoryUtil = new ReserveInventoryUtil(this, lotDetailsGid);
				if (ReserveInventoryUtil.isLotsContainsScale(lotDetailsGid)) {
					this.reserveInventoryUtil.viewReserveInventoryWindow();
				} else {
					MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.RESERVATION_STATUS),
							this.messageSource
									.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES) + ".");
				}
			}
		}
	}

	// end of reserveInventoryAction

	public void saveReservationChangesAction(final Window window) {
		if (this.hasUnsavedChanges()) {
			final boolean success =
					this.reserveInventoryAction.saveReserveTransactions(this.getValidReservationsToSave(), this.germplasmList.getId());

			if (success) {
				this.cancelReservations();
				this.resetListDataTableValues();
				this.resetListInventoryTableValues();
				MessageNotifier.showMessage(window, this.messageSource.getMessage(Message.SUCCESS),
						this.messageSource.getMessage(Message.SAVE_RESERVED_AND_CANCELLED_RESERVATION));
			} else {
				MessageNotifier.showError(window, this.messageSource.getMessage(Message.ERROR),
						this.messageSource.getMessage(Message.INVENTORY_NOT_AVAILABLE_BALANCE));
			}

		}
	}

	public void refreshInventoryListDataTabel() {
		this.listInventoryTable.loadInventoryData();
	}

	public void cancelReservationsAction() {
		final List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

		if (lotDetailsGid == null || lotDetailsGid.isEmpty()) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"Please select at least 1 lot to cancel reservations.");
		} else {
			if (!this.listInventoryTable.isSelectedEntriesHasReservation(lotDetailsGid, this.getValidReservationsToSave())) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						"There are no reservations on the current selected lots.");
			} else {
				ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.CANCEL_RESERVATIONS),
						"Are you sure you want to cancel the selected reservations?", this.messageSource.getMessage(Message.YES),
						this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(final ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									ListComponent.this.userSelectedLotEntriesToCancelReservations();
								}
							}
						});
			}
		}
	}

	public void cancelReservations() {
		if (this.persistedReservationToCancel != null && !this.persistedReservationToCancel.isEmpty()) {
			this.reserveInventoryAction.cancelReservations(this.persistedReservationToCancel);
			// reset the reservation to cancel.
			this.persistedReservationToCancel.clear();
		}
	}

	public void userSelectedLotEntriesToCancelReservations() {

		final List<ListEntryLotDetails> userSelectedLotEntriesToCancel = this.listInventoryTable.getSelectedLots();

		final Iterator<ListEntryLotDetails> userSelectedLotEntriesToCancelIterator = userSelectedLotEntriesToCancel.iterator();

		final int validReservation = this.validReservationsToSave.size();

		// this will keep track of how many reservations needs to be cancelled and how many reservations needs to be undo
		while (userSelectedLotEntriesToCancelIterator.hasNext()) {

			final ListEntryLotDetails userSelectedLot = userSelectedLotEntriesToCancelIterator.next();
			final Map<ListEntryLotDetails, Double> validReservations = this.getValidReservationsToSave();

			if (validReservations.size() > 0) {
				final Iterator<Map.Entry<ListEntryLotDetails, Double>> validReservationEntriesIterator =
						validReservations.entrySet().iterator();
				while (validReservationEntriesIterator.hasNext()) {
					final Map.Entry<ListEntryLotDetails, Double> validReservationEntry = validReservationEntriesIterator.next();
					final ListEntryLotDetails validReservationLotDetail = validReservationEntry.getKey();

					if (validReservationLotDetail.getLotId().equals(userSelectedLot.getLotId())) {
						validReservationEntriesIterator.remove();
						userSelectedLotEntriesToCancelIterator.remove();
						break;
					}
				}
				this.validReservationsToSave = validReservations;
			}
		}
		// validReservationsToCancel holds the actual lot entries that needs to be canceled which is already there in database
		this.persistedReservationToCancel = userSelectedLotEntriesToCancel;

		// enables the save reservation option if there is actual lot that needs to be cancel which is already there in database
		if (!this.persistedReservationToCancel.isEmpty()) {
			this.inventoryViewMenu.setMenuInventorySaveChanges();
			this.setHasUnsavedChanges(true);
		}

		if (validReservation != this.validReservationsToSave.size()) {
			this.listInventoryTable.resetRowsForCancelledReservation(this.listInventoryTable.getSelectedLots(), this.germplasmList.getId());
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.UNSAVED_RESERVARTION_CANCELLED));
		}

	}

	@Override
	public void updateListInventoryTable(final Map<ListEntryLotDetails, Double> validReservations, final boolean withInvalidReservations) {
		for (final Map.Entry<ListEntryLotDetails, Double> entry : validReservations.entrySet()) {
			final ListEntryLotDetails lot = entry.getKey();
			final Double newRes = entry.getValue();
			final Double available = lot.getAvailableLotBalance() - newRes;
			final Item itemToUpdate = this.listInventoryTable.getTable().getItem(lot);
			if (newRes > 0) {
				if (!lot.getTransactionStatus()) {
					itemToUpdate.getItemProperty(ColumnLabels.RESERVATION.getName()).setValue(newRes + lot.getLotScaleNameAbbr());
				}
				itemToUpdate.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(available + lot.getLotScaleNameAbbr());
			}
		}
		this.removeReserveInventoryWindow(this.reserveInventory);

		// update lot reservations to save
		this.updateLotReservationsToSave(validReservations);

		// enable now the Save Changes option
		this.inventoryViewMenu.setMenuInventorySaveChanges();

		// if there are no valid reservations
		if (withInvalidReservations) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.RESERVATION_STATUS),
					this.messageSource.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES)
							+ ".");

		} else {
			MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					"All selected entries will be reserved in their respective lots.", 3000);
		}

	}

	private void updateLotReservationsToSave(final Map<ListEntryLotDetails, Double> validReservations) {

		for (final Map.Entry<ListEntryLotDetails, Double> entry : validReservations.entrySet()) {
			final ListEntryLotDetails lot = entry.getKey();
			final Double amountToReserve = entry.getValue();

			if (this.validReservationsToSave.containsKey(lot)) {
				this.validReservationsToSave.remove(lot);

			}

			this.validReservationsToSave.put(lot, amountToReserve);
		}

		if (!this.validReservationsToSave.isEmpty()) {
			this.setHasUnsavedChanges(true);
		}
	}

	public Map<ListEntryLotDetails, Double> getValidReservationsToSave() {
		return this.validReservationsToSave;
	}

	@Override
	public void addReserveInventoryWindow(final ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().addWindow(this.reserveInventory);
	}

	@Override
	public void removeReserveInventoryWindow(final ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().removeWindow(this.reserveInventory);
	}

	@Override
	public void addReservationStatusWindow(final ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.removeReserveInventoryWindow(this.reserveInventory);
		this.source.getWindow().addWindow(this.reservationStatus);
	}

	@Override
	public void removeReservationStatusWindow(final ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.source.getWindow().removeWindow(this.reservationStatus);
	}

	public void resetListInventoryTableValues() {
		this.listInventoryTable.updateListInventoryTableAfterSave();

		this.inventoryViewMenu.resetInventoryMenuOptions();

		// reset the reservations to save.
		this.validReservationsToSave.clear();

		this.setHasUnsavedChanges(false);
	}

	@Override
	public Component getParentComponent() {
		return this.source;
	}

	public AddColumnContextMenu getAddColumnContextMenu() {
		return this.addColumnContextMenu;
	}

	public void closeLotsActions() {

		if (CollectionUtils.isEmpty(this.listInventoryTable.getSelectedLots())) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.NO_LOTS_SELECTED_ERROR));
			return;
		}

		final Map<String, List<ListEntryLotDetails>> mapLotDetails = this.validateSelectedCloseLots();

		if (!CollectionUtils.isEmpty(mapLotDetails.get(ListComponent.CLOSE_LOT_UNCOMMITTED))) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.LOTS_HAVE_AVAILABLE_BALANCE_UNCOMMITTED_RESERVATION_ERROR));

			return;
		}

		final List<ListEntryLotDetails> validLotEntryDetails = mapLotDetails.get(ListComponent.CLOSE_LOT_VALID);

		if (!CollectionUtils.isEmpty(validLotEntryDetails)) {
			try {
				this.processCloseLots(validLotEntryDetails);
			} catch (final CloseLotException e) {
				final String errorMessage = this.messageSource.getMessage(e.getMessage(), null, Locale.getDefault());
				ListComponent.LOG.error(errorMessage, e);
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR), errorMessage);
				return;
			}

			MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(Message.LOTS_CLOSED_SUCCESSFULLY));
		}

		final List<ListEntryLotDetails> lotWithAvailableBalanceEntryDetails = mapLotDetails.get(ListComponent.CLOSE_LOT_AVAILABLE_BALANCE);

		if (!CollectionUtils.isEmpty(lotWithAvailableBalanceEntryDetails)) {
			this.closeLotDiscardInventoryAction.setLotDetails(lotWithAvailableBalanceEntryDetails);
			this.closeLotDiscardInventoryAction.processLotCloseWithDiscard();
		}

	}

	private List<ListEntryLotDetails> getSelectedCloseLotEntryDetails(final List<ListEntryLotDetails> selectedLots)
			throws CloseLotException {
		final List<GermplasmListData> inventoryDetails =
				this.inventoryDataManager.getLotDetailsForList(this.getGermplasmListId(), 0, Integer.MAX_VALUE);

		final List<Integer> lotIdList = Lists.newArrayList();

		for (final ListEntryLotDetails lot : selectedLots) {
			lotIdList.add(lot.getLotId());
		}

		final List<ListEntryLotDetails> selectedLotEntryDetails = Lists.newArrayList();
		final Map<Integer, ListEntryLotDetails> mapLotDetails = ListCommonActionsUtil.createListEntryLotDetailsMap(inventoryDetails);

		for (final Integer selectedLot : lotIdList) {
			if (mapLotDetails.containsKey(selectedLot)) {
				selectedLotEntryDetails.add(mapLotDetails.get(selectedLot));
			} else {
				throw new CloseLotException(Message.LOT_ALREADY_CLOSED_ERROR.toString());
			}

		}

		return selectedLotEntryDetails;
	}

	private Map<String, List<ListEntryLotDetails>> validateSelectedCloseLots() {
		final List<ListEntryLotDetails> lotsSelectedToClose = this.listInventoryTable.getSelectedLots();

		final Map<String, List<ListEntryLotDetails>> mapLotDetails = new HashMap<>();

		mapLotDetails.put(ListComponent.CLOSE_LOT_VALID, Lists.<ListEntryLotDetails>newArrayList());
		mapLotDetails.put(ListComponent.CLOSE_LOT_UNCOMMITTED, Lists.<ListEntryLotDetails>newArrayList());
		mapLotDetails.put(ListComponent.CLOSE_LOT_AVAILABLE_BALANCE, Lists.<ListEntryLotDetails>newArrayList());

		for (final ListEntryLotDetails lotDetails : lotsSelectedToClose) {
			if (lotDetails.getReservedTotal() > 0) {
				mapLotDetails.get(ListComponent.CLOSE_LOT_UNCOMMITTED).add(lotDetails);
				continue;
			}

			if (lotDetails.getActualLotBalance() > 0) {
				mapLotDetails.get(ListComponent.CLOSE_LOT_AVAILABLE_BALANCE).add(lotDetails);
			} else if (lotDetails.getActualLotBalance() == 0) {
				mapLotDetails.get(ListComponent.CLOSE_LOT_VALID).add(lotDetails);
			}
		}

		return mapLotDetails;
	}

	public void processCloseLots(final List<ListEntryLotDetails> lotsSelectedToClose) throws CloseLotException {
		final List<Transaction> closeLotTransactions = Lists.newArrayList();
		final List<Integer> listLotIds = Lists.newArrayList();

		final List<ListEntryLotDetails> selectedCloseLotEntryDetails = this.getSelectedCloseLotEntryDetails(lotsSelectedToClose);

		for (final ListEntryLotDetails lotDetail : selectedCloseLotEntryDetails) {
			listLotIds.add(lotDetail.getLotId());
		}

		final List<Lot> closeLots = this.inventoryDataManager.getLotsByIdList(listLotIds);

		for (final Lot lot : closeLots) {

			if (lot.getStatus().equals(LotStatus.CLOSED.getIntValue())) {
				throw new CloseLotException(Message.LOT_ALREADY_CLOSED_ERROR.toString());
			} else {
				lot.setStatus(LotStatus.CLOSED.getIntValue());
			}

		}

		final WorkbenchUser workbenchUser = this.userService.getUserById(this.contextUtil.getCurrentWorkbenchUserId());

		for (final ListEntryLotDetails lotDetail : selectedCloseLotEntryDetails) {

			if (lotDetail.getReservedTotal() > 0) {
				throw new CloseLotException(Message.LOTS_HAVE_AVAILABLE_BALANCE_UNCOMMITTED_RESERVATION_ERROR.toString());
			}

			final Integer lotId = lotDetail.getLotId();
			final Date transactionDate = DateUtil.getCurrentDate();

			Double closingBalance = null;
			String comments = null;

			if (lotDetail.getActualLotBalance() == 0) {
				closingBalance = 0D;
				comments = this.messageSource.getMessage(Message.TRANSACTION_CLOSE_COMMENT);
			} else if (lotDetail.getActualLotBalance() > 0) {
				closingBalance = -1 * lotDetail.getActualLotBalance();
				comments = this.messageSource.getMessage(Message.TRANSACTION_DISCARD_COMMENT);
			}

			final String sourceType = this.messageSource.getMessage(Message.SOURCE_TYPE_LIST);
			final Integer listId = this.germplasmList.getId();
			final Integer lrecId = lotDetail.getId();

			final Double prevAmount = 0D;

			final Transaction closeLotTransaction = new Transaction();

			closeLotTransaction.setUserId(workbenchUser.getUserid());

			final Lot lot = new Lot(lotId);
			lot.setStatus(LotStatus.CLOSED.getIntValue());
			closeLotTransaction.setLot(lot);

			closeLotTransaction.setTransactionDate(transactionDate);
			closeLotTransaction.setStatus(org.generationcp.middleware.pojos.ims.TransactionStatus.CONFIRMED.getIntValue());
			closeLotTransaction.setQuantity(closingBalance);
			closeLotTransaction.setComments(comments);

			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(transactionDate);
			final Integer year = calendar.get(Calendar.YEAR);
			final Integer month = calendar.get(Calendar.MONTH) + 1;
			final Integer day = calendar.get(Calendar.DAY_OF_MONTH);
			final String dateString = year.toString() + month.toString() + day.toString();

			closeLotTransaction.setCommitmentDate(Integer.valueOf(dateString));
			closeLotTransaction.setSourceType(sourceType);
			closeLotTransaction.setSourceId(listId);
			closeLotTransaction.setSourceRecordId(lrecId);
			closeLotTransaction.setPreviousAmount(prevAmount);

			closeLotTransactions.add(closeLotTransaction);
		}

		this.inventoryDataManager.addTransactions(closeLotTransactions);
		this.inventoryDataManager.updateLots(closeLots);

	}

	public ViewListHeaderWindow getViewListHeaderWindow() {
		return this.viewListHeaderWindow;
	}

	public void setViewListHeaderWindow(final ViewListHeaderWindow viewListHeaderWindow) {
		this.viewListHeaderWindow = viewListHeaderWindow;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public Table getListDataTable() {
		return this.listDataTable;
	}

	public void setListDataTable(final Table listDataTable) {
		this.listDataTable = listDataTable;
	}

	public void setAddColumnContextMenu(final AddColumnContextMenu addColumnContextMenu) {
		this.addColumnContextMenu = addColumnContextMenu;
	}

	protected TableWithSelectAllLayout getListDataTableWithSelectAll() {
		return this.listDataTableWithSelectAll;
	}

	protected void setListDataTableWithSelectAll(final TableWithSelectAllLayout listDataTableWithSelectAll) {
		this.listDataTableWithSelectAll = listDataTableWithSelectAll;
	}

	protected String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public void setValidReservationsToSave(final Map<ListEntryLotDetails, Double> validReservationsToSave) {
		this.validReservationsToSave = validReservationsToSave;
	}

	public List<ListEntryLotDetails> getValidReservationsToCancel() {
		return this.persistedReservationToCancel;
	}

	public void setInventoryViewMenu(final InventoryViewActionMenu inventoryViewMenu) {
		this.inventoryViewMenu = inventoryViewMenu;
	}

	public ListManagerInventoryTable getListInventoryTable() {
		return this.listInventoryTable;
	}

	public void setListInventoryTable(final ListManagerInventoryTable listInventoryTable) {
		this.listInventoryTable = listInventoryTable;
	}

	public ReserveInventoryAction getReserveInventoryAction() {
		return this.reserveInventoryAction;
	}

	public void setPersistedReservationToCancel(final List<ListEntryLotDetails> persistedReservationToCancel) {
		this.persistedReservationToCancel = persistedReservationToCancel;
	}

	public Map<Object, String> getItemsToDelete() {
		return this.itemsToDelete;
	}

	@Override
	public void updateGermplasmListTable(final Set<Integer> gidsProcessed) {
		// created map of gid and germplasm as a preparation for the retrieval for mgid
		final List<Germplasm> germplasms = this.germplasmDataManager.getGermplasms(new ArrayList<Integer>(gidsProcessed));
		final Map<Integer, Germplasm> germplasmMap = new HashMap<Integer, Germplasm>();
		for (final Germplasm germplasm : germplasms) {
			germplasmMap.put(germplasm.getGid(), germplasm);
		}

		// update the MGID(Group Id) of the specific rows marked as fixed lines
		// Note we are refetching the list data as we cannot lazy load the list data in the germplasm list
		// This is because the lazy load might be across transactions.
		// This is not ideal but something we must do for an interim solution
		final List<GermplasmListData> germplasmListData =
				this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId());
		for (final GermplasmListData listEntry : germplasmListData) {
			final Integer gid = listEntry.getGid();
			final Germplasm germplasm = germplasmMap.get(gid);
			if (gidsProcessed.contains(gid)) {
				final Item selectedRowItem = this.listDataTable.getItem(listEntry.getId());
				selectedRowItem.getItemProperty(ColumnLabels.GROUP_ID.getName())
						.setValue(germplasm.getMgid() == 0 ? "-" : germplasm.getMgid());
			}
		}
	}

	public ListManagerMain getListManagerMain() {
		return this.source;
	}

	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setReserveInventoryAction(final ReserveInventoryAction reserveInventoryAction) {
		this.reserveInventoryAction = reserveInventoryAction;
	}

	public void setCloseLotDiscardInventoryAction(final CloseLotDiscardInventoryAction closeLotDiscardInventoryAction) {
		this.closeLotDiscardInventoryAction = closeLotDiscardInventoryAction;
	}

	public InventoryViewActionMenu getInventoryViewMenu() {
		return this.inventoryViewMenu;
	}

	public void setDoneInitializing(final Boolean doneInitializing) {
		this.doneInitializing = doneInitializing;
	}

	protected class ConfirmUnfixLinesListener implements ConfirmDialog.Listener {

		private static final long serialVersionUID = 1L;
		private final Set<Integer> gidsToProcess;
		private final ListComponent listComponent;

		public ConfirmUnfixLinesListener(final Set<Integer> gidsToProcess, final ListComponent listComponent) {
			this.gidsToProcess = gidsToProcess;
			this.listComponent = listComponent;
		}

		@Override
		public void onClose(final ConfirmDialog dialog) {
			if (dialog.isConfirmed()) {
				this.listComponent.unfixLines(this.gidsToProcess);
				this.listComponent.updateGermplasmListTable(this.gidsToProcess);
			}
		}

	}

	protected void addAttributeAndNameTypeColumn(final String column) {
		this.attributeAndNameTypeColumns.add(column);
	}

	public List<String> getAttributeAndNameTypeColumns() {
		return this.attributeAndNameTypeColumns;
	}

	void setAttributeAndNameTypeColumns(final List<String> attributeAndNameTypeColumns) {
		this.attributeAndNameTypeColumns = attributeAndNameTypeColumns;
	}

	public Button getLockButton() {
		return this.lockButton;
	}

	public Button getUnlockButton() {
		return this.unlockButton;
	}

	public Button getEditHeaderButton() {
		return this.editHeaderButton;
	}

	public void setFillWith(final FillWith fillWith) {
		this.fillWith = fillWith;
	}
	public Boolean listHasAddedColumns() {
		return this.addColumnContextMenu.hasAddedColumn(this.listDataTable, this.attributeAndNameTypeColumns);
	}

	void setTopLabel(final Label topLabel) {
		this.topLabel = topLabel;
	}

	void setToolsMenuContainer(final HorizontalLayout toolsMenuContainer) {
		this.toolsMenuContainer = toolsMenuContainer;
	}

	void setTotalListEntriesLabel(final Label totalListEntriesLabel) {
		this.totalListEntriesLabel = totalListEntriesLabel;
	}

	void setTotalSelectedListEntriesLabel(final Label totalSelectedListEntriesLabel) {
		this.totalSelectedListEntriesLabel = totalSelectedListEntriesLabel;
	}
}

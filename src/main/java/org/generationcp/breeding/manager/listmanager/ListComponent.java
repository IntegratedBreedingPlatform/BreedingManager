
package org.generationcp.breeding.manager.listmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListManagerInventoryTable;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialogSource;
import org.generationcp.breeding.manager.listmanager.dialog.ListManagerCopyToNewListDialog;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.breeding.manager.listmanager.util.ListDataPropertiesRenderer;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.util.UserUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

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

@Configurable
public class ListComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout,
		AddEntryDialogSource, SaveListAsDialogSource, ReserveInventorySource {

	private static final long serialVersionUID = -3367108805414232721L;

	private static final Logger LOG = LoggerFactory.getLogger(ListComponent.class);

	private static final int MINIMUM_WIDTH = 10;
	private final Map<Object, HashMap<Object, Field>> fields = new HashMap<Object, HashMap<Object, Field>>();

	private final ListManagerMain source;
	private final ListTabComponent parentListDetailsComponent;
	private GermplasmList germplasmList;
	private List<GermplasmListData> listEntries;
	private long listEntriesCount;
	private String designationOfListEntriesDeleted = "";

	private Label topLabel;
	private Button viewHeaderButton;
	private Label totalListEntriesLabel;
	private Label totalSelectedListEntriesLabel;
	private Button toolsButton;
	private Table listDataTable;
	private TableWithSelectAllLayout listDataTableWithSelectAll;

	private HorizontalLayout headerLayout;
	private HorizontalLayout subHeaderLayout;

	// Menu for tools button
	private ContextMenu menu;
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuExportForGenotypingOrder;
	private ContextMenuItem menuCopyToList;
	private ContextMenuItem menuAddEntry;
	private ContextMenuItem menuSaveChanges;
	private ContextMenuItem menuDeleteEntries;
	private ContextMenuItem menuEditList;
	private ContextMenuItem menuDeleteList;
	@SuppressWarnings("unused")
	private ContextMenuItem menuInventoryView;
	private AddColumnContextMenu addColumnContextMenu;

	private ContextMenu inventoryViewMenu;
	private ContextMenuItem menuCopyToNewListFromInventory;
	private ContextMenuItem menuInventorySaveChanges;
	@SuppressWarnings("unused")
	private ContextMenuItem menuListView;
	@SuppressWarnings("unused")
	private ContextMenuItem menuReserveInventory;
	@SuppressWarnings("unused")
	private ContextMenuItem menuCancelReservation;

	// Tooltips
	public static final String TOOLS_BUTTON_ID = "Actions";
	public static final String LIST_DATA_COMPONENT_TABLE_DATA = "List Data Component Table";
	private static final String CHECKBOX_COLUMN_ID = "Checkbox Column ID";

	// this is true if this component is created by accessing the Germplasm List Details page directly from the URL
	private boolean fromUrl;

	// Theme Resource
	private BaseSubWindow listManagerCopyToNewListDialog;
	private static final String USER_HOME = "user.home";

	private Object selectedColumn = "";
	private Object selectedItemId;
	private String lastCellvalue = "";
	private final List<Integer> gidsWithoutChildrenToDelete;
	private final Map<Object, String> itemsToDelete;

	private Button lockButton;
	private Button unlockButton;
	private Button editHeaderButton;

	private ViewListHeaderWindow viewListHeaderWindow;

	private HorizontalLayout toolsMenuContainer;

	private Button inventoryViewToolsButton;

	public static final String LOCK_BUTTON_ID = "Lock Germplasm List";
	public static final String UNLOCK_BUTTON_ID = "Unlock Germplasm List";

	private static final String LOCK_TOOLTIP = "Click to lock or unlock this germplasm list.";

	private ContextMenu tableContextMenu;

	@SuppressWarnings("unused")
	private ContextMenuItem tableContextMenu_SelectAll;

	private ContextMenuItem tableContextMenu_CopyToNewList;
	private ContextMenuItem tableContextMenu_DeleteEntries;
	private ContextMenuItem tableContextMenu_EditCell;

	// Value change event is fired when table is populated, so we need a flag
	private Boolean doneInitializing = false;

	// Inventory Related Variables
	private ListManagerInventoryTable listInventoryTable;
	private ReserveInventoryWindow reserveInventory;
	private ReservationStatusWindow reservationStatus;
	private ReserveInventoryUtil reserveInventoryUtil;
	private ReserveInventoryAction reserveInventoryAction;
	private Map<ListEntryLotDetails, Double> validReservationsToSave;
	private Boolean hasChanges;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	private Integer localUserId = null;

	private FillWith fillWith;

	private SaveListAsDialog dialog;

	public ListComponent(ListManagerMain source, ListTabComponent parentListDetailsComponent, GermplasmList germplasmList) {
		super();
		this.source = source;
		this.parentListDetailsComponent = parentListDetailsComponent;
		this.germplasmList = germplasmList;
		this.gidsWithoutChildrenToDelete = new ArrayList<Integer>();
		this.itemsToDelete = new HashMap<Object, String>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
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
		this.topLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.topLabel.setWidth("120px");
		this.topLabel.setStyleName(Bootstrap.Typography.H4.styleName());

		this.viewListHeaderWindow = new ViewListHeaderWindow(this.germplasmList);

		this.viewHeaderButton = new Button(this.messageSource.getMessage(Message.VIEW_HEADER));
		this.viewHeaderButton.addStyleName(Reindeer.BUTTON_LINK);
		this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());

		this.editHeaderButton =
				new IconButton(
						"<span class='glyphicon glyphicon-pencil' style='left: 2px; top:10px; color: #7c7c7c;font-size: 16px; font-weight: bold;'></span>",
						"Edit List Header");

		this.toolsButton = new ActionButton();
		this.toolsButton.setData(TOOLS_BUTTON_ID);

		this.inventoryViewToolsButton = new ActionButton();
		this.inventoryViewToolsButton.setData(TOOLS_BUTTON_ID);

		try {
			this.listEntriesCount = this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId());
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with retrieving count of list entries for list: " + this.germplasmList.getId(), ex);
			this.listEntriesCount = 0;
		}

		this.totalListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalListEntriesLabel.setWidth("120px");

		if (this.listEntriesCount == 0) {
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
		} else {
			this.updateNoOfEntries(this.listEntriesCount);
		}

		this.totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedListEntriesLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.unlockButton =
				new IconButton(
						"<span class='bms-locked' style='position: relative; top:5px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
						LOCK_TOOLTIP);
		this.unlockButton.setData(UNLOCK_BUTTON_ID);

		this.lockButton =
				new IconButton(
						"<span class='bms-lock-open' style='position: relative; top:5px; left: 2px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
						LOCK_TOOLTIP);
		this.lockButton.setData(LOCK_BUTTON_ID);

		this.menu = new ContextMenu();
		this.menu.setWidth("295px");

		// Add Column menu will be initialized after list data table is created
		this.initializeListDataTable(); // listDataTable
		this.initializeListInventoryTable(); // listInventoryTable

		// Generate main level items
		this.menuAddEntry = this.menu.addItem(this.messageSource.getMessage(Message.ADD_ENTRIES));
		this.menuCopyToList = this.menu.addItem(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST));
		this.menuDeleteList = this.menu.addItem(this.messageSource.getMessage(Message.DELETE_LIST));
		this.menuDeleteEntries = this.menu.addItem(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
		this.menuEditList = this.menu.addItem(this.messageSource.getMessage(Message.EDIT_LIST));
		this.menuExportList = this.menu.addItem(this.messageSource.getMessage(Message.EXPORT_LIST));
		this.menuExportForGenotypingOrder = this.menu.addItem(this.messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER));
		this.menuInventoryView = this.menu.addItem(this.messageSource.getMessage(Message.INVENTORY_VIEW));
		this.menuSaveChanges = this.menu.addItem(this.messageSource.getMessage(Message.SAVE_CHANGES));
		this.menu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));

		this.inventoryViewMenu = new ContextMenu();
		this.inventoryViewMenu.setWidth("295px");
		this.menuCancelReservation = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.CANCEL_RESERVATIONS));
		this.menuCopyToNewListFromInventory = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST));
		this.menuReserveInventory = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.menuListView = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
		this.menuInventorySaveChanges = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.SAVE_RESERVATIONS));
		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));

		this.resetInventoryMenuOptions();

		this.tableContextMenu = new ContextMenu();
		this.tableContextMenu.setWidth("295px");
		this.tableContextMenu_SelectAll = this.tableContextMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.tableContextMenu_DeleteEntries = this.tableContextMenu.addItem(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
		this.tableContextMenu_EditCell = this.tableContextMenu.addItem(this.messageSource.getMessage(Message.EDIT_VALUE));
		this.tableContextMenu_CopyToNewList =
				this.tableContextMenu.addItem(this.messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST));

		// Inventory Related Variables
		this.validReservationsToSave = new HashMap<ListEntryLotDetails, Double>();

		// Keep Track the changes in ListDataTable and/or ListInventoryTable
		this.hasChanges = false;

		// ListSelectionComponent is null when tool launched from BMS dashboard
		if (this.source != null && this.source.getListSelectionComponent() != null) {
			ListSelectionLayout listSelection = this.source.getListSelectionComponent().getListDetailsLayout();
			listSelection.addUpdateListStatusForChanges(this, this.hasChanges);
		}
	}

	private void resetInventoryMenuOptions() {
		// disable the save button at first since there are no reservations yet
		this.menuInventorySaveChanges.setEnabled(false);

		// Temporarily disable to Copy to New List in InventoryView TODO implement the function
		this.menuCopyToNewListFromInventory.setEnabled(false);
	}

	private void initializeListDataTable() {
		this.listDataTableWithSelectAll =
				new TableWithSelectAllLayout(Long.valueOf(this.listEntriesCount).intValue(), this.getNoOfEntries(), CHECKBOX_COLUMN_ID);
		this.listDataTable = this.listDataTableWithSelectAll.getTable();
		this.listDataTable.setSelectable(true);
		this.listDataTable.setMultiSelect(true);
		this.listDataTable.setColumnCollapsingAllowed(true);
		this.listDataTable.setWidth("100%");
		this.listDataTable.setDragMode(TableDragMode.ROW);
		this.listDataTable.setData(LIST_DATA_COMPONENT_TABLE_DATA);
		this.listDataTable.setColumnReorderingAllowed(false);

		this.listDataTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.GROUP_NAME.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.AVAIL_INV.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_RES.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);

		this.messageSource.setColumnHeader(this.listDataTable, CHECKBOX_COLUMN_ID, Message.CHECK_ICON);
		this.messageSource.setColumnHeader(this.listDataTable, ListDataTablePropertyID.ENTRY_ID.getName(), Message.HASHTAG);
		this.messageSource.setColumnHeader(this.listDataTable, ListDataTablePropertyID.DESIGNATION.getName(),
				Message.LISTDATA_DESIGNATION_HEADER);
		this.messageSource.setColumnHeader(this.listDataTable, ListDataTablePropertyID.GROUP_NAME.getName(),
				Message.LISTDATA_GROUPNAME_HEADER);
		this.messageSource.setColumnHeader(this.listDataTable, ListDataTablePropertyID.AVAIL_INV.getName(),
				Message.LISTDATA_AVAIL_INV_HEADER);
		this.messageSource
				.setColumnHeader(this.listDataTable, ListDataTablePropertyID.SEED_RES.getName(), Message.LISTDATA_SEED_RES_HEADER);
		this.messageSource.setColumnHeader(this.listDataTable, ListDataTablePropertyID.ENTRY_CODE.getName(),
				Message.LISTDATA_ENTRY_CODE_HEADER);
		this.messageSource.setColumnHeader(this.listDataTable, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
		this.messageSource.setColumnHeader(this.listDataTable, ListDataTablePropertyID.SEED_SOURCE.getName(),
				Message.LISTDATA_SEEDSOURCE_HEADER);

		this.addColumnContextMenu =
				new AddColumnContextMenu(this.parentListDetailsComponent, this.menu, this.listDataTable,
						ListDataTablePropertyID.GID.getName());
	}

	public void initializeListInventoryTable() {
		this.listInventoryTable = new ListManagerInventoryTable(this.source, this.germplasmList.getId(), true, false);
		this.listInventoryTable.setVisible(false);
	}

	public int getNoOfEntries() {
		// browse list component is null at this point when tool launched from Workbench dashboard
		ListSelectionComponent browseListsComponent = this.source.getListSelectionComponent();
		if (browseListsComponent == null || browseListsComponent.isVisible()) {
			return 8;
		}

		return 18;
	}

	@Override
	public void initializeValues() {

		try {
			this.localUserId = UserUtil.getCurrentUserLocalId(this.workbenchDataManager);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with retrieving local user ID", e);
			LOG.error("\n" + e.getStackTrace());
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
			this.listEntries = new ArrayList<GermplasmListData>();

			this.getAllListEntries();

			for (GermplasmListData entry : this.listEntries) {
				this.addListEntryToTable(entry);
			}

			this.listDataTable.sort(new Object[] {ListDataTablePropertyID.ENTRY_ID.getName()}, new boolean[] {true});

			// render additional columns
			ListDataPropertiesRenderer newColumnsRenderer = new ListDataPropertiesRenderer(this.germplasmList.getId(), this.listDataTable);
			try {
				newColumnsRenderer.render();
			} catch (MiddlewareQueryException ex) {
				LOG.error("Error with displaying added columns for entries of list: " + this.germplasmList.getId(), ex);
			}

		}

	}

	private void addListEntryToTable(GermplasmListData entry) {
		String gid = String.format("%s", entry.getGid().toString());
		Button gidButton = new Button(gid, new GidLinkButtonClickListener(this.source, gid, true, true));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription("Click to view Germplasm information");

		Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(this.source, gid, true, true));
		desigButton.setStyleName(BaseTheme.BUTTON_LINK);
		desigButton.setDescription("Click to view Germplasm information");

		CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setData(entry.getId());
		itemCheckBox.setImmediate(true);
		itemCheckBox.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (((Boolean) itemCheckBox.getValue()).equals(true)) {
					ListComponent.this.listDataTable.select(itemCheckBox.getData());
				} else {
					ListComponent.this.listDataTable.unselect(itemCheckBox.getData());
				}
			}

		});

		Item newItem = this.listDataTable.getContainerDataSource().addItem(entry.getId());
		newItem.getItemProperty(CHECKBOX_COLUMN_ID).setValue(itemCheckBox);
		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entry.getEntryId());
		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(desigButton);
		newItem.getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).setValue(entry.getGroupName());
		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
		newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(entry.getSeedSource());

		// Inventory Related Columns

		// #1 Available Inventory
		// default value
		String avail_inv = "-";
		if (entry.getInventoryInfo().getLotCount().intValue() != 0) {
			avail_inv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
		}
		Button inventoryButton =
				new Button(avail_inv, new InventoryLinkButtonClickListener(this.parentListDetailsComponent, this.germplasmList.getId(),
						entry.getId(), entry.getGid()));
		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
		inventoryButton.setDescription("Click to view Inventory Details");
		newItem.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).setValue(inventoryButton);

		if (avail_inv.equals("-")) {
			inventoryButton.setEnabled(false);
			inventoryButton.setDescription("No Lot for this Germplasm");
		} else {
			inventoryButton.setDescription("Click to view Inventory Details");
		}

		// #2 Seed Reserved
		String seed_res = "-"; // default value
		if (entry.getInventoryInfo().getReservedLotCount().intValue() != 0) {
			seed_res = entry.getInventoryInfo().getReservedLotCount().toString().trim();
		}
		newItem.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(seed_res);
	}

	private void getAllListEntries() {
		List<GermplasmListData> entries = null;
		try {
			entries =
					this.inventoryDataManager.getLotCountsForList(this.germplasmList.getId(), 0, Long.valueOf(this.listEntriesCount)
							.intValue());

			this.listEntries.addAll(entries);
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with retrieving list entries for list: " + this.germplasmList.getId(), ex);
			this.listEntries = new ArrayList<GermplasmListData>();
		}
	}

	@Override
	public void addListeners() {
		this.viewHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.openViewListHeaderWindow();
			}
		});

		if (this.germplasmList.isLocalList() && !this.germplasmList.isLockedList()) {
			this.fillWith =
					new FillWith(this.parentListDetailsComponent, this.parentListDetailsComponent, this.messageSource, this.listDataTable,
							ListDataTablePropertyID.GID.getName());
		}

		this.makeTableEditable();

		this.toolsButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 272707576878821700L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.addColumnContextMenu.refreshAddColumnMenu();
				ListComponent.this.menu.show(event.getClientX(), event.getClientY());

				if (ListComponent.this.fromUrl) {
					ListComponent.this.menuExportForGenotypingOrder.setVisible(false);
					ListComponent.this.menuExportList.setVisible(false);
					ListComponent.this.menuCopyToList.setVisible(false);
				}

				if (ListComponent.this.source != null) {
					ListComponent.this.menuCopyToList.setVisible(!ListComponent.this.source.listBuilderIsLocked());
				}

				// Show items only when Germplasm List open is a local IBDB record (negative ID),
				// when the Germplasm List is not locked, and when not accessed directly from URL or popup window
				if (ListComponent.this.germplasmList.isLocalList() && !ListComponent.this.germplasmList.isLockedList()
						&& !ListComponent.this.fromUrl) {
					ListComponent.this.menuEditList.setVisible(true);
					ListComponent.this.menuDeleteList.setVisible(ListComponent.this.localUserIsListOwner()); // show only Delete List when
																												// user is owner
					ListComponent.this.menuDeleteEntries.setVisible(true);
					ListComponent.this.menuSaveChanges.setVisible(true);
					ListComponent.this.menuAddEntry.setVisible(true);
					ListComponent.this.addColumnContextMenu.showHideAddColumnMenu(true);
				} else {
					ListComponent.this.menuEditList.setVisible(false);
					ListComponent.this.menuDeleteList.setVisible(false);
					ListComponent.this.menuDeleteEntries.setVisible(false);
					ListComponent.this.menuSaveChanges.setVisible(false);
					ListComponent.this.menuAddEntry.setVisible(false);
					ListComponent.this.addColumnContextMenu.showHideAddColumnMenu(false);
				}

			}
		});

		this.inventoryViewToolsButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 272707576878821700L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.inventoryViewMenu.show(event.getClientX(), event.getClientY());
			}
		});

		this.menu.addListener(new ContextMenu.ClickListener() {

			private static final long serialVersionUID = -2343109406180457070L;

			@Override
			public void contextItemClick(ClickEvent event) {
				// Get reference to clicked item
				ContextMenuItem clickedItem = event.getClickedItem();
				if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
					ListComponent.this.listDataTable.setValue(ListComponent.this.listDataTable.getItemIds());
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.EXPORT_LIST))) {
					ListComponent.this.exportListAction();
				} else if (clickedItem.getName().equals(
						ListComponent.this.messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER))) {
					ListComponent.this.exportListForGenotypingOrderAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.COPY_TO_NEW_LIST))) {
					ListComponent.this.copyToNewListAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.ADD_ENTRIES))) {
					ListComponent.this.addEntryButtonClickAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SAVE_CHANGES))) {
					ListComponent.this.saveChangesAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))) {
					ListComponent.this.deleteEntriesButtonClickAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.EDIT_LIST))) {
					ListComponent.this.editListButtonClickAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.DELETE_LIST))) {
					ListComponent.this.deleteListButtonClickAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.INVENTORY_VIEW))) {
					ListComponent.this.viewInventoryAction();
				}

			}
		});

		this.inventoryViewMenu.addListener(new ContextMenu.ClickListener() {

			private static final long serialVersionUID = -2343109406180457070L;

			@Override
			public void contextItemClick(ClickEvent event) {
				// Get reference to clicked item
				ContextMenuItem clickedItem = event.getClickedItem();
				if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SAVE_RESERVATIONS))) {
					ListComponent.this.saveReservationChangesAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))) {
					ListComponent.this.viewListAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.COPY_TO_NEW_LIST))) {
					ListComponent.this.copyToNewListFromInventoryViewAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.RESERVE_INVENTORY))) {
					ListComponent.this.reserveInventoryAction();
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
					ListComponent.this.listInventoryTable.getTable()
							.setValue(ListComponent.this.listInventoryTable.getTable().getItemIds());
				} else if (clickedItem.getName().equals(ListComponent.this.messageSource.getMessage(Message.CANCEL_RESERVATIONS))) {
					ListComponent.this.cancelReservationsAction();
				}
			}
		});

		this.editHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -788407324474054727L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.openSaveListAsDialog();
			}
		});

		this.lockButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.lockGermplasmList();
			}
		});

		this.unlockButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListComponent.this.unlockGermplasmList();
			}
		});

		this.tableContextMenu.addListener(new ContextMenu.ClickListener() {

			private static final long serialVersionUID = -2343109406180457070L;

			@Override
			public void contextItemClick(ClickEvent event) {
				String action = event.getClickedItem().getName();
				if (action.equals(ListComponent.this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))) {
					ListComponent.this.deleteEntriesButtonClickAction();
				} else if (action.equals(ListComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
					ListComponent.this.listDataTable.setValue(ListComponent.this.listDataTable.getItemIds());
				} else if (action.equals(ListComponent.this.messageSource.getMessage(Message.EDIT_VALUE))) {

					HashMap<Object, Field> itemMap = ListComponent.this.fields.get(ListComponent.this.selectedItemId);

					// go through each field, set previous edited fields to blurred/readonly
					for (Map.Entry<Object, Field> entry : itemMap.entrySet()) {
						Field f = entry.getValue();
						Object fieldValue = f.getValue();
						if (!f.isReadOnly()) {
							f.setReadOnly(true);

							if (!fieldValue.equals(ListComponent.this.lastCellvalue)) {
								ListComponent.this.setHasUnsavedChanges(true);
							}
						}
					}

					// Make the entire item editable

					if (itemMap != null) {
						for (Map.Entry<Object, Field> entry : itemMap.entrySet()) {
							Object column = entry.getKey();
							if (column.equals(ListComponent.this.selectedColumn)) {
								Field f = entry.getValue();
								if (f.isReadOnly()) {
									Object fieldValue = f.getValue();
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

		this.listDataTableWithSelectAll.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListComponent.this.updateNoOfSelectedEntries();
			}
		});

		this.listInventoryTable.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListComponent.this.updateNoOfSelectedEntries();
			}
		});

	}// end of addListeners

	@Override
	public void layoutComponents() {
		this.headerLayout = new HorizontalLayout();
		this.headerLayout.setWidth("100%");
		this.headerLayout.setSpacing(true);

		HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.topLabel);
		this.headerLayout.addComponent(headingLayout);
		this.headerLayout.addComponent(this.viewHeaderButton);
		this.headerLayout.setComponentAlignment(this.viewHeaderButton, Alignment.BOTTOM_RIGHT);

		if (this.germplasmList.isLocalList()) {
			this.headerLayout.addComponent(this.editHeaderButton);
			this.headerLayout.setComponentAlignment(this.editHeaderButton, Alignment.BOTTOM_LEFT);
		}

		if (this.germplasmList.isLocalList() && this.localUserIsListOwner()) {
			this.headerLayout.addComponent(this.lockButton);
			this.headerLayout.setComponentAlignment(this.lockButton, Alignment.BOTTOM_LEFT);

			this.headerLayout.addComponent(this.unlockButton);
			this.headerLayout.setComponentAlignment(this.unlockButton, Alignment.BOTTOM_LEFT);
		}

		this.setLockedState(this.germplasmList.isLockedList());

		this.headerLayout.setExpandRatio(headingLayout, 1.0f);

		this.toolsMenuContainer = new HorizontalLayout();
		this.toolsMenuContainer.setWidth("90px");
		this.toolsMenuContainer.setHeight("27px");
		this.toolsMenuContainer.addComponent(this.toolsButton);

		HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setSpacing(true);
		leftSubHeaderLayout.addComponent(this.totalListEntriesLabel);
		leftSubHeaderLayout.addComponent(this.totalSelectedListEntriesLabel);
		leftSubHeaderLayout.setComponentAlignment(this.totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		leftSubHeaderLayout.setComponentAlignment(this.totalSelectedListEntriesLabel, Alignment.MIDDLE_LEFT);

		this.subHeaderLayout = new HorizontalLayout();
		this.subHeaderLayout.setWidth("100%");
		this.subHeaderLayout.setSpacing(true);
		this.subHeaderLayout.addStyleName("lm-list-desc");
		this.subHeaderLayout.addComponent(leftSubHeaderLayout);
		this.subHeaderLayout.addComponent(this.toolsMenuContainer);
		this.subHeaderLayout.setComponentAlignment(leftSubHeaderLayout, Alignment.MIDDLE_LEFT);
		this.subHeaderLayout.setComponentAlignment(this.toolsMenuContainer, Alignment.MIDDLE_RIGHT);

		this.addComponent(this.headerLayout);
		this.addComponent(this.subHeaderLayout);

		this.listDataTable.setHeight("480px");

		this.addComponent(this.listDataTableWithSelectAll);
		this.addComponent(this.listInventoryTable);
		this.addComponent(this.tableContextMenu);

		this.parentListDetailsComponent.addComponent(this.menu);
		this.parentListDetailsComponent.addComponent(this.inventoryViewMenu);
	}

	@Override
	public void updateLabels() {

	}

	private boolean localUserIsListOwner() {
		return this.germplasmList.getUserId().equals(this.localUserId);
	}

	public void makeTableEditable() {
		this.listDataTable.addListener(new ItemClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event) {
				ListComponent.this.selectedColumn = event.getPropertyId();
				ListComponent.this.selectedItemId = event.getItemId();

				if (event.getButton() == ItemClickEvent.BUTTON_RIGHT) {

					ListComponent.this.tableContextMenu.show(event.getClientX(), event.getClientY());

					if (ListComponent.this.selectedColumn.equals(CHECKBOX_COLUMN_ID)
							|| ListComponent.this.selectedColumn.equals(ListDataTablePropertyID.GID.getName())
							|| ListComponent.this.selectedColumn.equals(ListDataTablePropertyID.ENTRY_ID.getName())
							|| ListComponent.this.selectedColumn.equals(ListDataTablePropertyID.DESIGNATION.getName())
							|| ListComponent.this.selectedColumn.equals(ListDataTablePropertyID.SEED_RES.getName())
							|| ListComponent.this.selectedColumn.equals(ListDataTablePropertyID.AVAIL_INV.getName())) {
						ListComponent.this.tableContextMenu_DeleteEntries.setVisible(!ListComponent.this.germplasmList.isLockedList());
						ListComponent.this.tableContextMenu_EditCell.setVisible(false);
						if (ListComponent.this.source != null) {
							ListComponent.this.tableContextMenu_CopyToNewList.setVisible(!ListComponent.this.source.listBuilderIsLocked());
						}
					} else if (ListComponent.this.germplasmList.isLocalList() && !ListComponent.this.germplasmList.isLockedList()) {
						ListComponent.this.tableContextMenu_DeleteEntries.setVisible(true);
						ListComponent.this.tableContextMenu_EditCell.setVisible(true);
						if (ListComponent.this.source != null) {
							ListComponent.this.tableContextMenu_CopyToNewList.setVisible(!ListComponent.this.source.listBuilderIsLocked());
						}
						ListComponent.this.doneInitializing = true;
					} else {
						ListComponent.this.tableContextMenu_DeleteEntries.setVisible(false);
						ListComponent.this.tableContextMenu_EditCell.setVisible(false);
						if (ListComponent.this.source != null) {
							ListComponent.this.tableContextMenu_CopyToNewList.setVisible(!ListComponent.this.source.listBuilderIsLocked());
						}
					}
				}
			}
		});

		this.listDataTable.setTableFieldFactory(new TableFieldFactory() {

			private static final long serialVersionUID = 1L;

			@Override
			public Field createField(Container container, final Object itemId, final Object propertyId, Component uiContext) {

				if (propertyId.equals(ListDataTablePropertyID.GID.getName())
						|| propertyId.equals(ListDataTablePropertyID.ENTRY_ID.getName())
						|| propertyId.equals(ListDataTablePropertyID.DESIGNATION.getName())
						|| propertyId.equals(ListDataTablePropertyID.AVAIL_INV.getName())
						|| propertyId.equals(ListDataTablePropertyID.SEED_RES.getName())) {
					return null;
				}

				final TextField tf = new TextField();
				tf.setData(new ItemPropertyId(itemId, propertyId));

				// set the size of textfield based on text of cell
				String value = (String) container.getItem(itemId).getItemProperty(propertyId).getValue();
				Double d = this.computeTextFieldWidth(value);
				tf.setWidth(d.floatValue(), UNITS_EM);

				// Needed for the generated column
				tf.setImmediate(true);

				// Manage the field in the field storage
				HashMap<Object, Field> itemMap = ListComponent.this.fields.get(itemId);
				if (itemMap == null) {
					itemMap = new HashMap<Object, Field>();
					ListComponent.this.fields.put(itemId, itemMap);
				}
				itemMap.put(propertyId, tf);

				tf.setReadOnly(true);

				tf.addListener(new FocusListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void focus(FocusEvent event) {
						ListComponent.this.listDataTable.select(itemId);
					}
				});

				tf.addListener(new FocusListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void focus(FocusEvent event) {
						ListComponent.this.lastCellvalue = ((TextField) event.getComponent()).getValue().toString();
					}
				});

				tf.addListener(new BlurListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void blur(BlurEvent event) {
						HashMap<Object, Field> itemMap = ListComponent.this.fields.get(itemId);

						// go through each field, set previous edited fields to blurred/readonly
						for (Map.Entry<Object, Field> entry : itemMap.entrySet()) {
							Field f = entry.getValue();
							Object fieldValue = f.getValue();
							if (!f.isReadOnly()) {
								f.setReadOnly(true);
								if (!fieldValue.equals(ListComponent.this.lastCellvalue)) {
									ListComponent.this.setHasUnsavedChanges(true);
								}
							}
						}

						for (Map.Entry<Object, Field> entry : itemMap.entrySet()) {
							Object column = entry.getKey();
							Field f = entry.getValue();
							Object fieldValue = f.getValue();

							// mark list as changed if value for the cell was changed
							if (column.equals(ListComponent.this.selectedColumn)) {
								if (!f.isReadOnly() && !fieldValue.toString().equals(ListComponent.this.lastCellvalue)) {
									ListComponent.this.setHasUnsavedChanges(true);
								}
							}

							// validate for designation
							if (column.equals(ListComponent.this.selectedColumn)
									&& ListComponent.this.selectedColumn.equals(ListDataTablePropertyID.DESIGNATION.getName())) {
								Object source = event.getSource();
								String designation = source.toString();

								// retrieve item id at event source
								ItemPropertyId itemProp = (ItemPropertyId) ((TextField) source).getData();
								Object sourceItemId = itemProp.getItemId();

								String[] items = ListComponent.this.listDataTable.getItem(sourceItemId).toString().split(" ");
								int gid = Integer.valueOf(items[2]);

								if (ListComponent.this.isDesignationValid(designation, gid)) {
									Double d = computeTextFieldWidth(f.getValue().toString());
									f.setWidth(d.floatValue(), UNITS_EM);
									f.setReadOnly(true);
									ListComponent.this.listDataTable.focus();
								} else {
									ConfirmDialog.show(ListComponent.this.getWindow(), "Update Designation",
											"The value you entered is not one of the germplasm names. "
													+ "Are you sure you want to update Designation with new value?", "Yes", "No",
											new ConfirmDialog.Listener() {

												private static final long serialVersionUID = 1L;

												@Override
												public void onClose(ConfirmDialog dialog) {
													if (!dialog.isConfirmed()) {
														tf.setReadOnly(false);
														tf.setValue(ListComponent.this.lastCellvalue);
													} else {
														Double d = computeTextFieldWidth(tf.getValue().toString());
														tf.setWidth(d.floatValue(), UNITS_EM);
													}
													tf.setReadOnly(true);
													ListComponent.this.listDataTable.focus();
												}
											});
								}
							} else {
								Double d = computeTextFieldWidth(f.getValue().toString());
								f.setWidth(d.floatValue(), UNITS_EM);
								f.setReadOnly(true);
							}
						}
					}
				});
				// this area can be used for validation
				tf.addListener(new Property.ValueChangeListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						Double d = computeTextFieldWidth(tf.getValue().toString());
						tf.setWidth(d.floatValue(), UNITS_EM);
						tf.setReadOnly(true);

						if (ListComponent.this.doneInitializing && !tf.getValue().toString().equals(ListComponent.this.lastCellvalue)) {
							ListComponent.this.setHasUnsavedChanges(true);
						}
					}
				});

				tf.addShortcutListener(new ShortcutListener("ENTER", ShortcutAction.KeyCode.ENTER, null) {

					private static final long serialVersionUID = 1L;

					@Override
					public void handleAction(Object sender, Object target) {
						Double d = computeTextFieldWidth(tf.getValue().toString());
						tf.setWidth(d.floatValue(), UNITS_EM);
						tf.setReadOnly(true);
						ListComponent.this.listDataTable.focus();

					}
				});

				return tf;
			}

			private Double computeTextFieldWidth(String value) {
				double multiplier = 0.55;
				int length = 1;
				if (value != null && !value.isEmpty()) {
					length = value.length();
					if (value.equals(value.toUpperCase())) {
						// if all caps, provide bigger space
						multiplier = 0.75;
					}
				}
				Double d = length * multiplier;
				// set a minimum textfield width
				return NumberUtils.max(new double[] {MINIMUM_WIDTH, d});
			}
		});

		this.listDataTable.setEditable(true);
	}

	// This is needed for storing back-references
	class ItemPropertyId {

		Object itemId;
		Object propertyId;

		public ItemPropertyId(Object itemId, Object propertyId) {
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

	public boolean isDesignationValid(String designation, int gid) {
		List<Name> germplasms = new ArrayList<Name>();
		List<String> designations = new ArrayList<String>();

		try {
			germplasms = this.germplasmDataManager.getNamesByGID(gid, null, null);

			for (Name germplasm : germplasms) {
				designations.add(germplasm.getNval());
			}

			for (String nameInDb : designations) {
				if (GermplasmDataManagerUtil.compareGermplasmNames(designation, nameInDb)) {
					return true;
				}
			}

		} catch (Exception e) {
			LOG.error("Database error!", e);
			MessageNotifier.showError(this.getWindow(), "Database Error!",
					"Error with validating designation." + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

		return false;
	}

	public void deleteEntriesButtonClickAction() throws InternationalizableException {
		Collection<?> selectedIdsToDelete = (Collection<?>) this.listDataTable.getValue();

		if (selectedIdsToDelete.size() > 0) {
			if (this.listDataTable.size() == selectedIdsToDelete.size()) {
				ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.DELETE_ALL_ENTRIES),
						this.messageSource.getMessage(Message.DELETE_ALL_ENTRIES_CONFIRM), this.messageSource.getMessage(Message.YES),
						this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									ListComponent.this.removeRowsInListDataTable((Collection<?>) ListComponent.this.listDataTable
											.getValue());
								}
							}

						});
			} else {
				this.removeRowsInListDataTable(selectedIdsToDelete);
			}

		} else {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DELETING_LIST_ENTRIES),
					this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
		}
	}

	private void removeRowsInListDataTable(Collection<?> selectedIds) {
		// marks that there is a change in listDataTable
		this.setHasUnsavedChanges(true);

		// Marks the Local Germplasm to be deleted
		try {
			final List<Integer> gidsWithoutChildren = this.getGidsToDeletedWithoutChildren(selectedIds);
			if (gidsWithoutChildren.size() > 0) {
				ConfirmDialog.show(this.getWindow(), "Delete Germplasm from Database",
						"Would you like to delete the germplasm(s) from the database also?", "Yes", "No", new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									ListComponent.this.gidsWithoutChildrenToDelete.addAll(gidsWithoutChildren);
								}
							}

						});
			}
		} catch (NumberFormatException e) {
			LOG.error("Error with deleting list entries.", e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error with deleting list entries.");
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with deleting list entries.", e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error with deleting list entries.");
		}

		if (this.listDataTable.getItemIds().size() == selectedIds.size()) {
			this.listDataTable.getContainerDataSource().removeAllItems();
		} else {
			// marks the entryId and designationId of the list entries to delete
			for (final Object itemId : selectedIds) {
				Button desigButton =
						(Button) this.listDataTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName())
								.getValue();
				String designation = String.valueOf(desigButton.getCaption().toString());
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

	private ArrayList<Integer> getGidsToDeletedWithoutChildren(Collection<?> selectedIds) throws NumberFormatException,
			MiddlewareQueryException {
		ArrayList<Integer> gids = new ArrayList<Integer>();
		for (final Object itemId : selectedIds) {
			Button gidButton =
					(Button) this.listDataTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
			Integer germplasmID = Integer.parseInt(gidButton.getCaption());

			// only allow deletions for local germplasms
			if (germplasmID.toString().contains("-")) {
				long count = this.pedigreeDataManager.countDescendants(germplasmID);
				if (count == 0) {
					gids.add(germplasmID);
				}
			}
		}

		return gids;
	}

	private void renumberEntryIds() {
		Integer entryId = 1;
		for (Iterator<?> i = this.listDataTable.getItemIds().iterator(); i.hasNext();) {
			int listDataId = (Integer) i.next();
			Item item = this.listDataTable.getItem(listDataId);
			item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entryId);
			entryId += 1;
		}
	}

	/* MENU ACTIONS */
	private void editListButtonClickAction() {
		final ListBuilderComponent ListBuilderComponent = this.source.getListBuilderComponent();

		if (ListBuilderComponent.hasUnsavedChanges()) {
			String message = "";

			String buildNewListTitle = ListBuilderComponent.getBuildNewListTitle().getValue().toString();
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
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						ListBuilderComponent.getSaveButton().click(); // save the existing list
					}

					ListComponent.this.source.loadListForEditing(ListComponent.this.getGermplasmList());
				}
			});
		} else {
			this.source.loadListForEditing(this.getGermplasmList());
		}
	}

	private void exportListAction() throws InternationalizableException {
		if (!this.germplasmList.isLocalList() || this.germplasmList.isLocalList() && this.germplasmList.isLockedList()) {
			String tempFileName = System.getProperty(USER_HOME) + "/temp.xls";
			GermplasmListExporter listExporter = new GermplasmListExporter(this.germplasmList.getId());
			try {
				listExporter.exportGermplasmListExcel(tempFileName);
				FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.source.getApplication());
				String listName = this.germplasmList.getName();
				fileDownloadResource.setFilename(listName.replace(" ", "_") + ".xls");
				this.source.getWindow().open(fileDownloadResource);

				// TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download

			} catch (GermplasmListExporterException e) {
				LOG.error(this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
						e.getMessage() + ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			}
		} else {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
					this.messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED));
		}
	}

	private void setLockedState(boolean locked) {
		this.lockButton.setVisible(!locked);
		this.unlockButton.setVisible(locked);

		if (this.germplasmList.isLocalList()) {
			this.editHeaderButton.setVisible(!locked);
		}

		if (this.fillWith != null) {
			this.fillWith.setContextMenuEnabled(!locked);
		}
	}

	private void exportListForGenotypingOrderAction() throws InternationalizableException {
		if (!this.germplasmList.isLocalList() || this.germplasmList.isLocalList() && this.germplasmList.isLockedList()) {
			String tempFileName = System.getProperty(USER_HOME) + "/tempListForGenotyping.xls";
			GermplasmListExporter listExporter = new GermplasmListExporter(this.germplasmList.getId());

			try {
				listExporter.exportListForKBioScienceGenotypingOrder(tempFileName, 96);
				FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.source.getApplication());
				String listName = this.germplasmList.getName();
				fileDownloadResource.setFilename(listName.replace(" ", "_") + "ForGenotyping.xls");

				this.source.getWindow().open(fileDownloadResource);

				// TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download

			} catch (GermplasmListExporterException e) {
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
						e.getMessage());
			}
		} else {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
					this.messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED));
		}
	}

	private void copyToNewListAction() {
		Collection<?> listEntries = (Collection<?>) this.listDataTable.getValue();
		if (listEntries == null || listEntries.isEmpty()) {
			MessageNotifier.showRequiredFieldError(this.getWindow(),
					this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));

		} else {
			this.listManagerCopyToNewListDialog = new BaseSubWindow(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
			this.listManagerCopyToNewListDialog.setOverrideFocus(true);
			this.listManagerCopyToNewListDialog.setModal(true);
			this.listManagerCopyToNewListDialog.setWidth("617px");
			this.listManagerCopyToNewListDialog.setHeight("230px");
			this.listManagerCopyToNewListDialog.setResizable(false);
			this.listManagerCopyToNewListDialog.addStyleName(Reindeer.WINDOW_LIGHT);

			try {
				this.listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(this.parentListDetailsComponent
						.getWindow(), this.listManagerCopyToNewListDialog, this.germplasmList.getName(), this.listDataTable, UserUtil
						.getCurrentUserLocalId(this.workbenchDataManager), this.source, false));
				this.parentListDetailsComponent.getWindow().addWindow(this.listManagerCopyToNewListDialog);
				this.listManagerCopyToNewListDialog.center();
			} catch (MiddlewareQueryException e) {
				LOG.error("Error copying list entries.", e);
				LOG.error("\n" + e.getStackTrace());
			}
		}
	}

	private void copyToNewListFromInventoryViewAction() {
		// TODO implement the copy to new list from the selection from listInventoryTable
	}

	private void addEntryButtonClickAction() {
		Window parentWindow = this.getWindow();
		AddEntryDialog addEntriesDialog = new AddEntryDialog(this, parentWindow);
		addEntriesDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		addEntriesDialog.focusOnSearchField();
		parentWindow.addWindow(addEntriesDialog);
	}

	@Override
	public void finishAddingEntry(Integer gid) {
		this.finishAddingEntry(gid, true);
	}

	public Boolean finishAddingEntry(Integer gid, Boolean showSuccessMessage) {

		Germplasm germplasm = null;

		try {
			germplasm = this.germplasmDataManager.getGermplasmWithPrefName(gid);
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with getting germplasm with id: " + gid, ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!", "Error with getting germplasm with id: " + gid + ". "
					+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			return false;
		}

		Integer maxEntryId = Integer.valueOf(0);
		if (this.listDataTable != null) {
			for (Iterator<?> i = this.listDataTable.getItemIds().iterator(); i.hasNext();) {
				// iterate through the table elements' IDs
				int listDataId = (Integer) i.next();

				// update table item's entryId
				Item item = this.listDataTable.getItem(listDataId);
				Integer entryId = (Integer) item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue();
				if (maxEntryId < entryId) {
					maxEntryId = entryId;
				}
			}
		}

		GermplasmListData listData = new GermplasmListData();
		listData.setList(this.germplasmList);
		if (germplasm.getPreferredName() != null) {
			listData.setDesignation(germplasm.getPreferredName().getNval());
		} else {
			listData.setDesignation("-");
		}
		listData.setEntryId(maxEntryId + 1);
		listData.setGid(gid);
		listData.setLocalRecordId(Integer.valueOf(0));
		listData.setStatus(Integer.valueOf(0));
		listData.setEntryCode(listData.getEntryId().toString());
		listData.setSeedSource("From Add Entry Feature of List Manager");

		String groupName = "-";
		try {
			groupName = this.germplasmDataManager.getCrossExpansion(gid, 1);
		} catch (MiddlewareQueryException ex) {
			LOG.error("\n" + ex.getStackTrace());
			groupName = "-";
		}
		listData.setGroupName(groupName);

		Integer listDataId = null;
		try {
			listDataId = this.germplasmListManager.addGermplasmListData(listData);

			// create table if added entry is first listdata record
			if (this.listDataTable == null) {
				this.initializeListDataTable();
				this.initializeValues();
			} else {
				this.listDataTable.setEditable(false);
				List<GermplasmListData> inventoryData =
						this.inventoryDataManager.getLotCountsForListEntries(this.germplasmList.getId(),
								new ArrayList<Integer>(Collections.singleton(listDataId)));
				if (inventoryData != null) {
					listData = inventoryData.get(0);
				}
				this.addListEntryToTable(listData);

				Object[] visibleColumns = this.listDataTable.getVisibleColumns();
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.PREFERRED_ID)) {
					this.addColumnContextMenu.setPreferredIdColumnValues(false);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.LOCATIONS)) {
					this.addColumnContextMenu.setLocationColumnValues(false);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.PREFERRED_NAME)) {
					this.addColumnContextMenu.setPreferredNameColumnValues(false);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.GERMPLASM_DATE)) {
					this.addColumnContextMenu.setGermplasmDateColumnValues(false);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_NAME)) {
					this.addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NAME);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_ABBREV)) {
					this.addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_ABBREV);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_NUMBER)) {
					this.addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_NUMBER);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.METHOD_GROUP)) {
					this.addColumnContextMenu.setMethodInfoColumnValues(false, AddColumnContextMenu.METHOD_GROUP);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_FEMALE_GID)) {
					this.addColumnContextMenu.setCrossFemaleInfoColumnValues(false, AddColumnContextMenu.CROSS_FEMALE_GID);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_FEMALE_PREF_NAME)) {
					this.addColumnContextMenu.setCrossFemaleInfoColumnValues(false, AddColumnContextMenu.CROSS_FEMALE_PREF_NAME);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_MALE_GID)) {
					this.addColumnContextMenu.setCrossMaleGIDColumnValues(false);
				}
				if (this.isColumnVisible(visibleColumns, AddColumnContextMenu.CROSS_MALE_PREF_NAME)) {
					this.addColumnContextMenu.setCrossMalePrefNameColumnValues(false);
				}

				this.saveChangesAction(this.getWindow(), false);
				this.listDataTable.refreshRowCache();
				this.listDataTable.setImmediate(true);
				this.listDataTable.setEditable(true);
			}

			if (showSuccessMessage) {
				this.setHasUnsavedChanges(false);
				MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
						"Successful in adding list entries.", 3000);
			}

			this.doneInitializing = true;
			return true;

		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with adding list entry.", ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!",
					"Error with adding list entry. " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			return false;
		}

	}

	private boolean isColumnVisible(Object[] columns, String columnName) {

		for (Object col : columns) {
			if (col.equals(columnName)) {
				return true;
			}
		}

		return false;
	}

	public void saveChangesAction() throws InternationalizableException {
		this.saveChangesAction(this.getWindow());
	}

	public Boolean saveChangesAction(Window window) throws InternationalizableException {
		return this.saveChangesAction(window, true);
	}

	public Boolean saveChangesAction(Window window, Boolean showSuccessMessage) throws InternationalizableException {

		this.deleteRemovedGermplasmEntriesFromTable();

		try {
			this.listEntriesCount = this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId());
			this.listEntries =
					this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId(), 0, (int) this.listEntriesCount);
		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES);
		}

		int entryId = 1;
		// re-assign "Entry ID" field based on table's sorting
		for (Iterator<?> i = this.listDataTable.getItemIds().iterator(); i.hasNext();) {
			// iterate through the table elements' IDs
			int listDataId = (Integer) i.next();

			// update table item's entryId
			Item item = this.listDataTable.getItem(listDataId);
			item.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entryId);

			// then find the corresponding ListData and assign a new entryId to it
			for (GermplasmListData listData : this.listEntries) {
				if (listData.getId().equals(listDataId)) {
					listData.setEntryId(entryId);

					String entryCode = (String) item.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue();
					if (entryCode != null && entryCode.length() != 0) {
						listData.setEntryCode(entryCode);
					} else {
						listData.setEntryCode(Integer.valueOf(entryId).toString());
					}

					String seedSource = (String) item.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).getValue();
					if (seedSource != null && seedSource.length() != 0) {
						listData.setSeedSource(seedSource);
					} else {
						listData.setSeedSource("-");
					}

					Button desigButton = (Button) item.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue();
					String designation = String.valueOf(desigButton.getCaption().toString());
					if (designation != null && designation.length() != 0) {
						listData.setDesignation(designation);
					} else {
						listData.setDesignation("-");
					}

					String groupName = (String) item.getItemProperty(ListDataTablePropertyID.GROUP_NAME.getName()).getValue();
					if (groupName != null && groupName.length() != 0) {
						if (groupName.length() > 255) {
							groupName = groupName.substring(0, 255);
						}
						listData.setGroupName(groupName);
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

			this.germplasmListManager.updateGermplasmListData(this.listEntries);
			this.germplasmListManager.saveListDataColumns(this.addColumnContextMenu.getListDataCollectionFromTable(this.listDataTable));

			this.listDataTable.requestRepaint();
			// reset flag to indicate unsaved changes
			this.setHasUnsavedChanges(true);

			if (showSuccessMessage) {
				MessageNotifier.showMessage(window, this.messageSource.getMessage(Message.SUCCESS),
						this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SAVING_SUCCESS), 3000);
			}

		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES);
		}

		// Update counter
		this.updateNoOfEntries();

		this.setHasUnsavedChanges(false);

		return true;
		// end of saveChangesAction
	}

	// TODO review this method as there are redundant codes here that is also in saveChangesAction()
	// might be possible to eliminate this method altogether and reduce the number of middleware calls
	private void performListEntriesDeletion(Map<Object, String> itemsToDelete) {
		try {
			this.designationOfListEntriesDeleted = "";

			for (Map.Entry<Object, String> item : itemsToDelete.entrySet()) {

				Object sLRecId = item.getKey();
				String sDesignation = item.getValue();

				try {
					int lrecId = Integer.valueOf(sLRecId.toString());
					this.designationOfListEntriesDeleted += sDesignation + ",";
					this.germplasmListManager.deleteGermplasmListDataByListIdLrecId(this.germplasmList.getId(), lrecId);
				} catch (MiddlewareQueryException e) {
					LOG.error("Error with deleting list entries.", e);
					LOG.error("\n" + e.getStackTrace());
				}
			}

			this.deleteGermplasmDialogBox(this.gidsWithoutChildrenToDelete);
			this.designationOfListEntriesDeleted =
					this.designationOfListEntriesDeleted.substring(0, this.designationOfListEntriesDeleted.length() - 1);

			// Change entry IDs on listData
			List<GermplasmListData> listDatas =
					this.germplasmListManager.getGermplasmListDataByListId(this.germplasmList.getId(), 0,
							(int) this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId()));
			Integer entryId = 1;
			for (GermplasmListData listData : listDatas) {
				listData.setEntryId(entryId);
				entryId++;
			}
			this.germplasmListManager.updateGermplasmListData(listDatas);

			try {
				this.logDeletedListEntriesToWorkbenchProjectActivity();
			} catch (MiddlewareQueryException e) {
				LOG.error("Error logging workbench activity.", e);
				LOG.error("\n" + e.getStackTrace());
			}

			// reset items to delete in listDataTable
			itemsToDelete.clear();

		} catch (NumberFormatException e) {
			LOG.error("Error with deleting list entries.", e);
			LOG.error("\n" + e.getStackTrace());
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with deleting list entries.", e);
			LOG.error("\n" + e.getStackTrace());
		}
		// end of performListEntriesDeletion
	}

	protected void deleteGermplasmDialogBox(final List<Integer> gidsWithoutChildren) throws NumberFormatException, MiddlewareQueryException {

		if (gidsWithoutChildren != null && gidsWithoutChildren.size() > 0) {
			ArrayList<Germplasm> gList = new ArrayList<Germplasm>();
			try {
				for (Integer gid : gidsWithoutChildren) {
					Germplasm g = this.germplasmDataManager.getGermplasmByGID(gid);
					g.setGrplce(gid);
					gList.add(g);
				}// end loop

				this.germplasmDataManager.updateGermplasm(gList);

			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
		}
	}

	private void logDeletedListEntriesToWorkbenchProjectActivity() throws MiddlewareQueryException {
		User user = this.workbenchDataManager.getUserById(this.workbenchDataManager.getWorkbenchRuntimeData().getUserId());

		ProjectActivity projAct =
				new ProjectActivity(new Integer(this.workbenchDataManager
						.getLastOpenedProject(this.workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()),
						this.workbenchDataManager.getLastOpenedProject(this.workbenchDataManager.getWorkbenchRuntimeData().getUserId()),
						"Deleted list entries.", "Deleted list entries from the list id " + this.germplasmList.getId() + " - "
								+ this.germplasmList.getName(), user, new Date());
		try {
			this.workbenchDataManager.addProjectActivity(projAct);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with logging workbench activity.", e);
			e.printStackTrace();
		}
	}

	public void deleteListButtonClickAction() {
		ConfirmDialog.show(this.getWindow(), "Delete Germplasm List:", "Are you sure that you want to delete this list?", "Yes", "No",
				new ConfirmDialog.Listener() {

					private static final long serialVersionUID = -6641772458404494412L;

					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							ListComponent.this.deleteGermplasmListConfirmed();
						}
					}
				});
	}

	public void deleteGermplasmListConfirmed() {
		if (!this.germplasmList.isLockedList()) {
			try {
				ListCommonActionsUtil.deleteGermplasmList(this.germplasmListManager, this.germplasmList, this.workbenchDataManager,
						this.getWindow(), this.messageSource, "list");

				this.source.getListSelectionComponent().getListTreeComponent().removeListFromTree(this.germplasmList);
				this.source.updateUIForDeletedList(this.germplasmList);
			} catch (MiddlewareQueryException e) {
				this.getWindow().showNotification("Error", "There was a problem deleting the germplasm list",
						Notification.TYPE_ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	protected void deleteRemovedGermplasmEntriesFromTable() {
		if (this.listDataTable.getItemIds().isEmpty()) {

			// If the list table is empty, delete all the list entries in the database
			try {
				this.germplasmListManager.deleteGermplasmListDataByListId(this.germplasmList.getId());
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}

		} else if (!this.itemsToDelete.isEmpty()) {

			// Delete the removed selected entries individually
			this.performListEntriesDeletion(this.itemsToDelete);
		}
	}

	/* SETTERS AND GETTERS */
	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	public Integer getGermplasmListId() {
		return this.germplasmList.getId();
	}

	public void lockGermplasmList() {
		if (this.source.lockGermplasmList(this.germplasmList)) {
			this.setLockedState(this.germplasmList.isLockedList());
		}
	}

	public void unlockGermplasmList() {
		if (this.germplasmList.isLockedList()) {
			this.germplasmList.setStatus(this.germplasmList.getStatus() - 100);
			try {
				this.germplasmListManager.updateGermplasmList(this.germplasmList);

				this.setLockedState(this.germplasmList.isLockedList());

				User user = this.workbenchDataManager.getUserById(this.workbenchDataManager.getWorkbenchRuntimeData().getUserId());
				ProjectActivity projAct =
						new ProjectActivity(new Integer(this.workbenchDataManager
								.getLastOpenedProject(this.workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId()
								.intValue()), this.workbenchDataManager.getLastOpenedProject(this.workbenchDataManager
								.getWorkbenchRuntimeData().getUserId()), "Unlocked a germplasm list.", "Unlocked list "
								+ this.germplasmList.getId() + " - " + this.germplasmList.getName(), user, new Date());
				this.workbenchDataManager.addProjectActivity(projAct);
			} catch (MiddlewareQueryException e) {
				LOG.error("Error with unlocking list.", e);
				MessageNotifier.showError(this.getWindow(), "Database Error!",
						"Error with unlocking list. " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			}
		}
	}

	public void openSaveListAsDialog() {
		this.dialog = new SaveListAsDialog(this, this.germplasmList, this.messageSource.getMessage(Message.EDIT_LIST_HEADER));
		this.getWindow().addWindow(this.dialog);
	}

	public SaveListAsDialog getSaveListAsDialog() {
		return this.dialog;
	}

	public GermplasmList getCurrentListInSaveDialog() {
		return this.dialog.getGermplasmListToSave();
	}

	@Override
	public void saveList(GermplasmList list) {
		list = ListCommonActionsUtil.overwriteList(list, this.germplasmListManager, this.source, this.messageSource, true);
		if (list != null) {
			if (!list.getId().equals(this.germplasmList.getId())) {
				ListCommonActionsUtil.overwriteListEntries(list, this.listEntries, this.germplasmList.getId().intValue() != list.getId()
						.intValue(), this.germplasmListManager, this.source, this.messageSource, true);
				this.source.closeList(list);
			} else {
				this.viewListHeaderWindow = new ViewListHeaderWindow(list);
				if (this.viewHeaderButton != null) {
					this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());
				}
			}
		}
		// Refresh tree on save
		((BreedingManagerApplication) this.getApplication()).getListManagerMain().getListSelectionComponent().getListTreeComponent()
				.refreshComponent();
	}

	public void openViewListHeaderWindow() {
		this.getWindow().addWindow(this.viewListHeaderWindow);
	}

	@Override
	public void finishAddingEntry(List<Integer> gids) {
		Boolean allSuccessful = true;
		for (Integer gid : gids) {
			if (this.finishAddingEntry(gid, false).equals(false)) {
				allSuccessful = false;
			}
		}
		if (allSuccessful) {
			MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SAVING_SUCCESS), 3000);
		}
	}

	private void updateNoOfEntries(long count) {
		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			if (count == 0) {
				this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
			} else {
				this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + "  <b>" + count
						+ "</b>");
			}
		} else {// Inventory View
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LOTS) + ": " + "  <b>" + count + "</b>");
		}
	}

	private void updateNoOfEntries() {
		int count = 0;
		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			count = this.listDataTable.getItemIds().size();
		} else {// Inventory View
			count = this.listInventoryTable.getTable().size();
		}
		this.updateNoOfEntries(count);
	}

	private void updateNoOfSelectedEntries(int count) {
		this.totalSelectedListEntriesLabel.setValue("<i>" + this.messageSource.getMessage(Message.SELECTED) + ": " + "  <b>" + count
				+ "</b></i>");
	}

	private void updateNoOfSelectedEntries() {
		int count = 0;

		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			Collection<?> selectedItems = (Collection<?>) this.listDataTableWithSelectAll.getTable().getValue();
			count = selectedItems.size();
		} else {
			Collection<?> selectedItems = (Collection<?>) this.listInventoryTable.getTable().getValue();
			count = selectedItems.size();
		}

		this.updateNoOfSelectedEntries(count);
	}

	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
	}

	/*-------------------------------------LIST INVENTORY RELATED METHODS-------------------------------------*/

	private void viewListAction() {
		if (!this.hasUnsavedChanges()) {
			this.source.setModeView(ModeView.LIST_VIEW);
		} else {
			String message =
					"You have unsaved reservations for this list. " + "You will need to save them before changing views. "
							+ "Do you want to save your changes?";

			this.source.showUnsavedChangesConfirmDialog(message, ModeView.LIST_VIEW);

		}
	}

	public void changeToListView() {
		if (this.listInventoryTable.isVisible()) {
			this.listDataTableWithSelectAll.setVisible(true);
			this.listInventoryTable.setVisible(false);
			this.toolsMenuContainer.addComponent(this.toolsButton);
			this.toolsMenuContainer.removeComponent(this.inventoryViewToolsButton);

			this.topLabel.setValue(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();
			this.setHasUnsavedChanges(false);
		}
	}

	public void changeToInventoryView() {
		if (this.listDataTableWithSelectAll.isVisible()) {
			this.listDataTableWithSelectAll.setVisible(false);
			this.listInventoryTable.setVisible(true);
			this.toolsMenuContainer.removeComponent(this.toolsButton);
			this.toolsMenuContainer.addComponent(this.inventoryViewToolsButton);

			this.topLabel.setValue(this.messageSource.getMessage(Message.LOTS));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();
			this.setHasUnsavedChanges(false);
		}
	}

	public void setHasUnsavedChanges(Boolean hasChanges) {
		this.hasChanges = hasChanges;

		ListSelectionLayout listSelection = this.source.getListSelectionComponent().getListDetailsLayout();
		listSelection.addUpdateListStatusForChanges(this, this.hasChanges);
	}

	public Boolean hasUnsavedChanges() {
		return this.hasChanges;
	}

	private void viewInventoryAction() {
		if (!this.hasUnsavedChanges()) {
			this.source.setModeView(ModeView.INVENTORY_VIEW);
		} else {
			String message =
					"You have unsaved changes to the list you are currently editing.. "
							+ "You will need to save them before changing views. " + "Do you want to save your changes?";
			this.source.showUnsavedChangesConfirmDialog(message, ModeView.INVENTORY_VIEW);
		}
	}

	public void viewInventoryActionConfirmed() {
		this.listInventoryTable.loadInventoryData();

		this.changeToInventoryView();
	}

	public void reserveInventoryAction() {
		if (!this.inventoryViewMenu.isVisible()) {// checks if the screen is in the inventory view
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"Please change to Inventory View first.");
		} else {
			List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

			if (lotDetailsGid == null || lotDetailsGid.size() == 0) {
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						"Please select at least 1 lot to reserve.");
			} else {
				// this util handles the inventory reservation related functions
				this.reserveInventoryUtil = new ReserveInventoryUtil(this, lotDetailsGid);
				this.reserveInventoryUtil.viewReserveInventoryWindow();
			}
		}
	}// end of reserveInventoryAction

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

	public void cancelReservationsAction() {
		List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

		if (lotDetailsGid == null || lotDetailsGid.size() == 0) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"Please select at least 1 lot to cancel reservations.");
		} else {
			if (!this.listInventoryTable.isSelectedEntriesHasReservation(lotDetailsGid)) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						"There are no reservations on the current selected lots.");
			} else {
				ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.CANCEL_RESERVATIONS),
						"Are you sure you want to cancel the selected reservations?", this.messageSource.getMessage(Message.YES),
						this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									ListComponent.this.cancelReservations();
								}
							}
						});
			}
		}
	}

	public void cancelReservations() {
		List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();
		this.reserveInventoryAction = new ReserveInventoryAction(this);
		try {
			this.reserveInventoryAction.cancelReservations(lotDetailsGid);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

		this.refreshInventoryColumns(this.getLrecIds(lotDetailsGid));
		this.listInventoryTable.resetRowsForCancelledReservation(lotDetailsGid, this.germplasmList.getId());

		MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
				"All selected reservations were cancelled successfully.");
	}

	private Set<Integer> getLrecIds(List<ListEntryLotDetails> lotDetails) {
		Set<Integer> lrecIds = new HashSet<Integer>();

		for (ListEntryLotDetails lotDetail : lotDetails) {
			if (!lrecIds.contains(lotDetail.getId())) {
				lrecIds.add(lotDetail.getId());
			}
		}
		return lrecIds;
	}

	private void refreshInventoryColumns(Set<Integer> entryIds) {
		List<GermplasmListData> germplasmListDataEntries = new ArrayList<GermplasmListData>();
		try {
			if (!entryIds.isEmpty()) {
				germplasmListDataEntries =
						this.inventoryDataManager.getLotCountsForListEntries(this.germplasmList.getId(), new ArrayList<Integer>(entryIds));
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

		for (GermplasmListData listData : germplasmListDataEntries) {
			Item item = this.listDataTable.getItem(listData.getId());

			// #1 Available Inventory
			String avail_inv = "-"; // default value
			if (listData.getInventoryInfo().getLotCount().intValue() != 0) {
				avail_inv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			Button inventoryButton =
					new Button(avail_inv, new InventoryLinkButtonClickListener(this.parentListDetailsComponent, this.germplasmList.getId(),
							listData.getId(), listData.getGid()));
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription("Click to view Inventory Details");

			if (avail_inv.equals("-")) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription("No Lot for this Germplasm");
			} else {
				inventoryButton.setDescription("Click to view Inventory Details");
			}
			item.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).setValue(inventoryButton);

			Button gidButton = (Button) item.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
			String gidString = "";

			if (gidButton != null) {
				gidString = gidButton.getCaption();
			}

			this.updateAvailInvValues(Integer.valueOf(gidString), avail_inv);

			// Seed Reserved
			String seed_res = "-"; // default value
			if (listData.getInventoryInfo().getReservedLotCount().intValue() != 0) {
				seed_res = listData.getInventoryInfo().getReservedLotCount().toString().trim();
			}

			item.getItemProperty(ListDataTablePropertyID.SEED_RES.getName()).setValue(seed_res);
		}
	}

	private void refreshInventoryColumns(Map<ListEntryLotDetails, Double> validReservationsToSave) {

		Set<Integer> entryIds = new HashSet<Integer>();
		for (Entry<ListEntryLotDetails, Double> details : validReservationsToSave.entrySet()) {
			entryIds.add(details.getKey().getId());
		}

		this.refreshInventoryColumns(entryIds);
	}

	@Override
	public void updateListInventoryTable(Map<ListEntryLotDetails, Double> validReservations, boolean withInvalidReservations) {
		for (Map.Entry<ListEntryLotDetails, Double> entry : validReservations.entrySet()) {
			ListEntryLotDetails lot = entry.getKey();
			Double new_res = entry.getValue();

			Item itemToUpdate = this.listInventoryTable.getTable().getItem(lot);
			itemToUpdate.getItemProperty(ListInventoryTable.NEWLY_RESERVED_COLUMN_ID).setValue(new_res);
		}

		this.removeReserveInventoryWindow(this.reserveInventory);

		// update lot reservatios to save
		this.updateLotReservationsToSave(validReservations);

		// enable now the Save Changes option
		this.menuInventorySaveChanges.setEnabled(true);

		if (validReservations.size() == 0) {// if there are no valid reservations
			MessageNotifier
					.showRequiredFieldError(
							this.getWindow(),
							this.messageSource
									.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES) + ".");

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

		if (this.validReservationsToSave.size() > 0) {
			this.setHasUnsavedChanges(true);
		}
	}

	public Map<ListEntryLotDetails, Double> getValidReservationsToSave() {
		return this.validReservationsToSave;
	}

	@Override
	public void addReserveInventoryWindow(ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().addWindow(this.reserveInventory);
	}

	@Override
	public void removeReserveInventoryWindow(ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().removeWindow(this.reserveInventory);
	}

	@Override
	public void addReservationStatusWindow(ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.removeReserveInventoryWindow(this.reserveInventory);
		this.source.getWindow().addWindow(this.reservationStatus);
	}

	@Override
	public void removeReservationStatusWindow(ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.source.getWindow().removeWindow(this.reservationStatus);
	}

	public void resetListInventoryTableValues() {
		this.listInventoryTable.updateListInventoryTableAfterSave();

		this.resetInventoryMenuOptions();

		this.validReservationsToSave.clear();// reset the reservations to save.

		this.setHasUnsavedChanges(false);
	}

	@Override
	public Component getParentComponent() {
		return this.source;
	}

	public AddColumnContextMenu getAddColumnContextMenu() {
		return this.addColumnContextMenu;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());

		return itemIds;
	}

	private void updateAvailInvValues(Integer gid, String availInv) {
		List<Integer> itemIds = this.getItemIds(this.listDataTable);
		for (Integer itemId : itemIds) {
			Item item = this.listDataTable.getItem(itemId);
			Button gidButton = (Button) item.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();

			String currentGid = "";
			if (gidButton != null) {
				currentGid = gidButton.getCaption();
			}

			if (currentGid.equals(gid)) {
				((Button) item.getItemProperty(ListDataTablePropertyID.AVAIL_INV.getName()).getValue()).setCaption(availInv);
			}
		}
		this.listDataTable.requestRepaint();
	}

	public ViewListHeaderWindow getViewListHeaderWindow() {
		return this.viewListHeaderWindow;
	}

	public void setViewListHeaderWindow(ViewListHeaderWindow viewListHeaderWindow) {
		this.viewListHeaderWindow = viewListHeaderWindow;
	}

}

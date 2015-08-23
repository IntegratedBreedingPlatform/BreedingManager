
package org.generationcp.breeding.manager.listmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.ExportListAsDialog;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesSource;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListManagerInventoryTable;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.inventory.InventoryDropTargetContainer;
import org.generationcp.breeding.manager.inventory.ListDataAndLotDetails;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.dialog.ListManagerCopyToNewListDialog;
import org.generationcp.breeding.manager.listmanager.listeners.ResetListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.SaveListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.breeding.manager.listmanager.util.DropHandlerMethods.ListUpdatedEvent;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
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
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListBuilderComponent extends VerticalLayout implements InitializingBean, BreedingManagerLayout, SaveListAsDialogSource,
		ReserveInventorySource, UnsavedChangesSource, InventoryDropTargetContainer {

	private final class LockButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
			if (!ListBuilderComponent.this.currentlySavedGermplasmList.isLockedList()) {
				ListBuilderComponent.this.currentlySavedGermplasmList.setStatus(ListBuilderComponent.this.currentlySavedGermplasmList
						.getStatus() + 100);
				try {
					ListBuilderComponent.this.currentlySetGermplasmInfo = ListBuilderComponent.this.currentlySavedGermplasmList;
					ListBuilderComponent.this.saveListButtonListener.doSaveAction(false);

					ListBuilderComponent.this.contextUtil.logProgramActivity("Locked a germplasm list.", "Locked list "
							+ ListBuilderComponent.this.currentlySavedGermplasmList.getId() + " - "
							+ ListBuilderComponent.this.currentlySavedGermplasmList.getName());

				} catch (MiddlewareQueryException e) {
					ListBuilderComponent.LOG.error("Error with unlocking list.", e);
					MessageNotifier.showError(ListBuilderComponent.this.getWindow(), "Database Error!", "Error with loocking list. "
							+ ListBuilderComponent.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				}
				ListBuilderComponent.this.setUIForLockedList();
			}
		}
	}

	private final class UnlockButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
			if (ListBuilderComponent.this.currentlySavedGermplasmList.isLockedList()) {
				ListBuilderComponent.this.currentlySavedGermplasmList.setStatus(ListBuilderComponent.this.currentlySavedGermplasmList
						.getStatus() - 100);
				try {
					ListBuilderComponent.this.currentlySetGermplasmInfo = ListBuilderComponent.this.currentlySavedGermplasmList;
					ListBuilderComponent.this.saveListButtonListener.doSaveAction(false);

					ListBuilderComponent.this.contextUtil.logProgramActivity("Unlocked a germplasm list.", "Unlocked list "
							+ ListBuilderComponent.this.currentlySavedGermplasmList.getId() + " - "
							+ ListBuilderComponent.this.currentlySavedGermplasmList.getName());

				} catch (MiddlewareQueryException e) {
					ListBuilderComponent.LOG.error("Error with unlocking list.", e);
					MessageNotifier.showError(ListBuilderComponent.this.getWindow(), "Database Error!", "Error with unlocking list. "
							+ ListBuilderComponent.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				}
				ListBuilderComponent.this.setUIForUnlockedList();
			}
		}
	}

	private final class ToolsButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1345004576139547723L;

		@Override
		public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {

			if (ListBuilderComponent.this.isCurrentListSaved()) {
				ListBuilderComponent.this.enableMenuOptionsAfterSave();
			}

			ListBuilderComponent.this.addColumnContextMenu.refreshAddColumnMenu();
			ListBuilderComponent.this.menu.show(event.getClientX(), event.getClientY());

		}
	}

	private final class InventoryViewMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// Get reference to clicked item
					ContextMenuItem clickedItem = event.getClickedItem();
					if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))) {
						ListBuilderComponent.this.viewListAction();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.COPY_TO_NEW_LIST))) {
						ListBuilderComponent.this.copyToNewListFromInventoryViewAction();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.RESERVE_INVENTORY))) {
						ListBuilderComponent.this.reserveInventoryAction();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						ListBuilderComponent.this.listInventoryTable.getTable().setValue(
								ListBuilderComponent.this.listInventoryTable.getTable().getItemIds());
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.CANCEL_RESERVATIONS))) {
						ListBuilderComponent.this.cancelReservationsAction();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.RESET_LIST))) {
						ListBuilderComponent.this.resetButton.click();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SAVE_LIST))) {
						ListBuilderComponent.this.saveButton.click();
					}
				}
			});

		}
	}

	private final class MenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2331333436994090161L;

		@Override
		public void contextItemClick(final ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					ContextMenuItem clickedItem = event.getClickedItem();
					Table germplasmsTable = ListBuilderComponent.this.tableWithSelectAllLayout.getTable();
					if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						germplasmsTable.setValue(germplasmsTable.getItemIds());
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))) {
						ListBuilderComponent.this.deleteSelectedEntries();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.EXPORT_LIST))) {
						ListBuilderComponent.this.exportListAction();
					} else if (clickedItem.getName().equals(
							ListBuilderComponent.this.messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER))) {
						ListBuilderComponent.this.exportListForGenotypingOrderAction();
					} else if (clickedItem.getName().equals(
							ListBuilderComponent.this.messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL))) {
						ListBuilderComponent.this.copyToNewListAction();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.INVENTORY_VIEW))) {
						ListBuilderComponent.this.viewInventoryAction();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.RESET_LIST))) {
						ListBuilderComponent.this.resetButton.click();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SAVE_LIST))) {
						ListBuilderComponent.this.saveButton.click();
					}
				}
			});
		}
	}

	private static final long serialVersionUID = 4997159450197570044L;

	private static final Logger LOG = LoggerFactory.getLogger(ListBuilderComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private ContextUtil contextUtil;
	
	@Resource
	private PlatformTransactionManager transactionManager;

	public static final String GERMPLASMS_TABLE_DATA = "Germplasms Table Data";
	static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action ACTION_DELETE_SELECTED_ENTRIES = new Action("Delete Selected Entries");
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] {ListBuilderComponent.ACTION_SELECT_ALL,
			ListBuilderComponent.ACTION_DELETE_SELECTED_ENTRIES};
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU_LOCKED = new Action[] {ListBuilderComponent.ACTION_SELECT_ALL};

	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	// Components
	private Label buildNewListTitle;
	private Label buildNewListDesc;
	private Label topLabel;
	private Label totalListEntriesLabel;
	private Label totalSelectedListEntriesLabel;
	private BreedingManagerListDetailsComponent breedingManagerListDetailsComponent;
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private Table listDataTable;
	private ViewListHeaderWindow viewListHeaderWindow;

	private Button editHeaderButton;
	private Button viewHeaderButton;
	private Button toolsButton;
	private Button inventoryViewToolsButton;
	private Button saveButton;
	private Button resetButton;
	private Button lockButton;
	private Button unlockButton;

	private FillWith fillWith;

	private Window listManagerCopyToNewListDialog;

	// String Literals
	public static final String LOCK_BUTTON_ID = "Lock Germplasm List";
	public static final String UNLOCK_BUTTON_ID = "Unlock Germplasm List";
	private static final String LOCK_TOOLTIP = "Click to lock or unlock this germplasm list.";
	public static final String TOOLS_BUTTON_ID = "Actions";
	public static final String INVENTORY_TOOLS_BUTTON_ID = "Actions";
	private static final String USER_HOME = "user.home";

	// Layout Component
	private AbsoluteLayout toolsButtonContainer;

	// Context Menus
	private ContextMenu menu;
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuCopyToList;
	private ContextMenuItem menuDeleteSelectedEntries;
	private AddColumnContextMenu addColumnContextMenu;
	private Action.Handler contextMenuActionHandler;

	private ContextMenu inventoryViewMenu;
	private ContextMenuItem menuCopyToNewListFromInventory;
	private ContextMenuItem menuReserveInventory;
	private ContextMenuItem menuCancelReservation;

	private SaveListAsDialog dialog;

	// For Saving
	private ListManagerMain source;
	private GermplasmList currentlySavedGermplasmList;
	private GermplasmList currentlySetGermplasmInfo;
	private Boolean hasChanges = false;

	// Listener
	private SaveListButtonClickListener saveListButtonListener;
	private BuildNewListDropHandler dropHandler;

	// Inventory Related Variables
	private ListManagerInventoryTable listInventoryTable;
	private ReserveInventoryWindow reserveInventory;
	private ReservationStatusWindow reservationStatus;
	private ReserveInventoryUtil reserveInventoryUtil;
	private ReserveInventoryAction reserveInventoryAction;
	private Map<ListEntryLotDetails, Double> validReservationsToSave;

	public ListBuilderComponent() {
		super();
	}

	public ListBuilderComponent(ListManagerMain source) {
		super();
		this.source = source;
		this.currentlySavedGermplasmList = null;
		this.currentlySetGermplasmInfo = null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
		this.initializeHandlers();
	}

	@Override
	public void instantiateComponents() {

		this.unlockButton =
				new IconButton(
						"<span class='bms-locked' style='position: relative; top:5px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
						ListBuilderComponent.LOCK_TOOLTIP);
		this.unlockButton.setData(ListBuilderComponent.UNLOCK_BUTTON_ID);
		this.unlockButton.setVisible(false);

		this.lockButton =
				new IconButton(
						"<span class='bms-lock-open' style='position: relative; top:5px; left: 2px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
						ListBuilderComponent.LOCK_TOOLTIP);
		this.lockButton.setData(ListBuilderComponent.LOCK_BUTTON_ID);
		this.lockButton.setVisible(false);

		this.buildNewListTitle = new Label(this.messageSource.getMessage(Message.BUILD_A_NEW_LIST));
		this.buildNewListTitle.setWidth("200px");
		this.buildNewListTitle.setStyleName(Bootstrap.Typography.H4.styleName());
		this.buildNewListTitle.addStyleName(AppConstants.CssStyles.BOLD);

		this.buildNewListDesc = new Label();
		this.buildNewListDesc.setValue("Build or revise your list by dragging in entries from the left.");
		this.buildNewListDesc.addStyleName("lm-word-wrap");
		this.buildNewListDesc.setWidth("100%");
		this.buildNewListDesc.setHeight("55px");

		this.topLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.topLabel.setWidth("130px");
		this.topLabel.setStyleName(Bootstrap.Typography.H4.styleName());

		this.totalListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalListEntriesLabel.setWidth("110px");
		this.updateNoOfEntries(0);

		this.totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.updateNoOfSelectedEntries(0);

		this.editHeaderButton = new Button(this.messageSource.getMessage(Message.EDIT_HEADER));
		this.editHeaderButton.setImmediate(true);
		this.editHeaderButton.setStyleName(BaseTheme.BUTTON_LINK);

		this.viewHeaderButton = new Button(this.messageSource.getMessage(Message.VIEW_HEADER));
		this.viewHeaderButton.addStyleName(BaseTheme.BUTTON_LINK);
		this.viewHeaderButton.setVisible(false);

		if (this.currentlySavedGermplasmList != null) {
			this.viewListHeaderWindow = new ViewListHeaderWindow(this.currentlySavedGermplasmList);
			this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());
		}

		this.breedingManagerListDetailsComponent = new BreedingManagerListDetailsComponent();

		this.tableWithSelectAllLayout = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());

		if (this.currentlySavedGermplasmList != null) {
			this.listInventoryTable = new ListManagerInventoryTable(this.source, this.currentlySavedGermplasmList.getId(), false, true);
		} else {
			this.listInventoryTable = new ListManagerInventoryTable(this.source, null, false, true);
		}
		this.listInventoryTable.setVisible(false);

		this.listDataTable = this.tableWithSelectAllLayout.getTable();
		this.createGermplasmTable(this.listDataTable);

		this.listDataTable.setWidth("100%");
		this.listDataTable.setHeight("480px");

		this.menu = new ContextMenu();
		this.menu.setWidth("300px");

		this.addColumnContextMenu =
				new AddColumnContextMenu(this, this.menu, this.tableWithSelectAllLayout.getTable(), ColumnLabels.GID.getName(), true);
		this.menuCopyToList = this.menu.addItem(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
		this.menuDeleteSelectedEntries = this.menu.addItem(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
		this.menuExportList = this.menu.addItem(this.messageSource.getMessage(Message.EXPORT_LIST));

		this.menu.addItem(this.messageSource.getMessage(Message.INVENTORY_VIEW));
		this.menu.addItem(this.messageSource.getMessage(Message.RESET_LIST));
		this.menu.addItem(this.messageSource.getMessage(Message.SAVE_LIST));
		this.menu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));

		this.inventoryViewMenu = new ContextMenu();
		this.inventoryViewMenu.setWidth("300px");
		this.menuCancelReservation = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.CANCEL_RESERVATIONS));
		this.menuCopyToNewListFromInventory = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST));
		this.menuReserveInventory = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.RESET_LIST));
		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.SAVE_LIST));
		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));

		// Temporarily disable to Copy to New List in InventoryView
		this.menuCopyToNewListFromInventory.setEnabled(false);

		this.resetMenuOptions();
		this.resetInventoryMenuOptions();

		this.toolsButton = new ActionButton();
		this.toolsButton.setData(ListBuilderComponent.TOOLS_BUTTON_ID);

		this.inventoryViewToolsButton = new ActionButton();
		this.inventoryViewToolsButton.setData(ListBuilderComponent.TOOLS_BUTTON_ID);

		this.dropHandler =
				new BuildNewListDropHandler(this.source, this.germplasmDataManager, this.germplasmListManager, this.inventoryDataManager,
						this.pedigreeService, this.crossExpansionProperties, this.tableWithSelectAllLayout.getTable(), transactionManager);

		this.saveButton = new Button();
		this.saveButton.setCaption(this.messageSource.getMessage(Message.SAVE_LABEL));
		this.saveButton.setWidth("80px");
		this.saveButton.addStyleName(Bootstrap.Buttons.INFO.styleName());

		this.resetButton = new Button();
		this.resetButton.setCaption(this.messageSource.getMessage(Message.RESET));
		this.resetButton.setWidth("80px");
		this.resetButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());

		// Inventory Related Variables
		this.validReservationsToSave = new HashMap<ListEntryLotDetails, Double>();

		// reset the marker for unsaved changes on initial loading
		this.resetUnsavedChangesFlag();
	}

	public void resetMenuOptions() {
		// initially disabled when the current list building is not yet save or being reset
		this.menuExportList.setEnabled(false);
		this.menuCopyToList.setEnabled(false);
		this.menuCancelReservation.setEnabled(false);
	}

	private void resetInventoryMenuOptions() {
		// Temporarily disable to Copy to New List in InventoryView
		this.menuCopyToNewListFromInventory.setEnabled(false);

		if (!this.isCurrentListSaved()) {
			this.menuReserveInventory.setEnabled(false);
		}
	}

	@Override
	public void initializeValues() {
		// do nothing

	}

	@Override
	public void addListeners() {

		this.fillWith = new FillWith(this, this.messageSource, this.tableWithSelectAllLayout.getTable(), ColumnLabels.GID.getName());

		this.menu.addListener(new MenuClickListener());

		this.inventoryViewMenu.addListener(new InventoryViewMenuClickListener());

		this.toolsButton.addListener(new ToolsButtonClickListener());

		this.inventoryViewToolsButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1345004576139547723L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListBuilderComponent.this.inventoryViewMenu.show(event.getClientX(), event.getClientY());
			}
		});

		this.editHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -6306973449416812850L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListBuilderComponent.this.openSaveListAsDialog();
			}
		});

		this.viewHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListBuilderComponent.this.viewListHeaderWindow =
						new ViewListHeaderWindow(ListBuilderComponent.this.currentlySavedGermplasmList);
				ListBuilderComponent.this.getWindow().addWindow(ListBuilderComponent.this.viewListHeaderWindow);
			}
		});

		this.saveListButtonListener = new SaveListButtonClickListener(this, this.tableWithSelectAllLayout.getTable(), this.messageSource);
		this.saveButton.addListener(this.saveListButtonListener);

		this.saveButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 7449465533478658983L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				if (ListBuilderComponent.this.currentlySetGermplasmInfo == null) {
					ListBuilderComponent.this.openSaveListAsDialog();
				}
			}
		});

		this.resetButton.addListener(new ResetListButtonClickListener(this, this.messageSource));

		// Lock button action
		this.lockButton.addListener(new LockButtonClickListener());

		// Unlock button action
		this.unlockButton.addListener(new UnlockButtonClickListener());

		this.tableWithSelectAllLayout.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListBuilderComponent.this.updateNoOfSelectedEntries();
			}
		});

		this.listInventoryTable.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListBuilderComponent.this.updateNoOfSelectedEntries();
			}
		});

	}

	public void setContextMenuActionHandler(final Table table) {

		table.removeActionHandler(this.contextMenuActionHandler);

		this.contextMenuActionHandler = new Action.Handler() {

			private static final long serialVersionUID = 1884343225476178686L;

			@Override
			public Action[] getActions(Object target, Object sender) {
				return ListBuilderComponent.this.getContextMenuActions();
			}

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (ListBuilderComponent.ACTION_SELECT_ALL == action) {
					table.setValue(table.getItemIds());
				} else if (ListBuilderComponent.ACTION_DELETE_SELECTED_ENTRIES == action) {
					ListBuilderComponent.this.deleteSelectedEntries();
				}
			}
		};

		table.addActionHandler(this.contextMenuActionHandler);
	}

	public Action[] getContextMenuActions() {
		if (this.currentlySavedGermplasmList != null && this.currentlySavedGermplasmList.isLockedList()) {
			return ListBuilderComponent.GERMPLASMS_TABLE_CONTEXT_MENU_LOCKED;
		}
		return ListBuilderComponent.GERMPLASMS_TABLE_CONTEXT_MENU;
	}

	public void setUIForLockedList() {
		if (this.userIsListOwner()) {
			this.lockButton.setVisible(false);
			this.unlockButton.setVisible(true);
		} else {
			this.lockButton.setVisible(false);
			this.unlockButton.setVisible(false);
		}
		this.tableWithSelectAllLayout.getTable().setDropHandler(null);
		this.setContextMenuActionHandler(this.listDataTable);
		this.menuDeleteSelectedEntries.setVisible(false);
		this.addColumnContextMenu.setVisible(false);
		this.editHeaderButton.setVisible(false);
		this.viewHeaderButton.setVisible(true);

		this.viewListHeaderWindow = new ViewListHeaderWindow(this.currentlySavedGermplasmList);
		this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());

		this.saveButton.setEnabled(false);

		this.source.setUIForLockedListBuilder();
		this.fillWith.setContextMenuEnabled(false);
	}

	public void setUIForUnlockedList() {
		if (this.userIsListOwner()) {
			this.lockButton.setVisible(true);
			this.unlockButton.setVisible(false);
		} else {
			this.lockButton.setVisible(false);
			this.unlockButton.setVisible(false);
		}
		this.tableWithSelectAllLayout.getTable().setDropHandler(this.dropHandler);
		this.setContextMenuActionHandler(this.listDataTable);
		this.menuDeleteSelectedEntries.setVisible(true);
		this.addColumnContextMenu.setVisible(true);
		this.editHeaderButton.setVisible(true);
		this.viewHeaderButton.setVisible(false);
		this.saveButton.setEnabled(true);
		this.source.setUIForUnlockedListBuilder();
		this.fillWith.setContextMenuEnabled(true);
	}

	public void setUIForNewList() {
		this.lockButton.setVisible(false);
		this.unlockButton.setVisible(false);
		this.tableWithSelectAllLayout.getTable().setDropHandler(this.dropHandler);
		this.setContextMenuActionHandler(this.listDataTable);
		this.menuDeleteSelectedEntries.setVisible(true);
		this.addColumnContextMenu.setVisible(true);
		this.editHeaderButton.setVisible(true);
		this.viewHeaderButton.setVisible(false);
		this.saveButton.setEnabled(true);
	}

	@Override
	public void layoutComponents() {
		this.setWidth("100%");
		this.setMargin(new MarginInfo(true, true, false, false));
		this.setSpacing(false);

		final HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_BUILD_NEW_LIST, this.buildNewListTitle);

		final VerticalLayout listBuilderHeadingContainer = new VerticalLayout();

		listBuilderHeadingContainer.addComponent(headingLayout);
		listBuilderHeadingContainer.addComponent(this.buildNewListDesc);

		this.addComponent(listBuilderHeadingContainer);

		final Panel listBuilderPanel = new Panel();
		listBuilderPanel.setStyleName(Reindeer.PANEL_LIGHT + " " + AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
		listBuilderPanel.setCaption(null);
		listBuilderPanel.setWidth("100%");

		final VerticalLayout listDataTableLayout = new VerticalLayout();
		listDataTableLayout.setMargin(true);
		listDataTableLayout.setSpacing(true);
		listDataTableLayout.addStyleName("listDataTableLayout");

		listBuilderPanel.setContent(listDataTableLayout);

		final HorizontalLayout listBuilderPanelTitleContainer = new HorizontalLayout();
		listBuilderPanelTitleContainer.setWidth("100%");
		listBuilderPanelTitleContainer.setSpacing(true);

		HeaderLabelLayout listEntriesTitle = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.topLabel);
		listBuilderPanelTitleContainer.addComponent(listEntriesTitle);

		listBuilderPanelTitleContainer.addComponent(this.viewHeaderButton);
		listBuilderPanelTitleContainer.addComponent(this.editHeaderButton);
		listBuilderPanelTitleContainer.addComponent(this.lockButton);
		listBuilderPanelTitleContainer.addComponent(this.unlockButton);

		listBuilderPanelTitleContainer.setExpandRatio(listEntriesTitle, 1.0f);

		listBuilderPanelTitleContainer.setComponentAlignment(this.viewHeaderButton, Alignment.BOTTOM_RIGHT);
		listBuilderPanelTitleContainer.setComponentAlignment(this.editHeaderButton, Alignment.BOTTOM_RIGHT);
		listBuilderPanelTitleContainer.setComponentAlignment(this.lockButton, Alignment.BOTTOM_RIGHT);
		listBuilderPanelTitleContainer.setComponentAlignment(this.unlockButton, Alignment.BOTTOM_RIGHT);

		HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setSpacing(true);
		leftSubHeaderLayout.addComponent(this.totalListEntriesLabel);
		leftSubHeaderLayout.addComponent(this.totalSelectedListEntriesLabel);
		leftSubHeaderLayout.setComponentAlignment(this.totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		leftSubHeaderLayout.setComponentAlignment(this.totalSelectedListEntriesLabel, Alignment.MIDDLE_LEFT);

		this.toolsButtonContainer = new AbsoluteLayout();
		this.toolsButtonContainer.setHeight("27px");
		this.toolsButtonContainer.setWidth("90px");
		this.toolsButtonContainer.addComponent(this.toolsButton, "top:0; right:0");

		final HorizontalLayout subHeaderLayout = new HorizontalLayout();
		subHeaderLayout.setWidth("100%");
		subHeaderLayout.addComponent(leftSubHeaderLayout);
		subHeaderLayout.addComponent(this.toolsButtonContainer);
		subHeaderLayout.setComponentAlignment(leftSubHeaderLayout, Alignment.MIDDLE_LEFT);
		subHeaderLayout.setExpandRatio(leftSubHeaderLayout, 1.0F);

		listDataTableLayout.addComponent(listBuilderPanelTitleContainer);
		listDataTableLayout.addComponent(subHeaderLayout);
		listDataTableLayout.addComponent(this.tableWithSelectAllLayout);
		listDataTableLayout.addComponent(this.listInventoryTable);

		this.addComponent(listBuilderPanel);

		final HorizontalLayout buttons = new HorizontalLayout();
		buttons.setMargin(new MarginInfo(false, false, true, false));
		buttons.setWidth("170px");
		buttons.addComponent(this.saveButton);
		buttons.addComponent(this.resetButton);
		buttons.setSpacing(true);
		buttons.addStyleName("lm-new-list-buttons");

		this.addComponent(buttons);
		this.addComponent(this.menu);
		this.addComponent(this.inventoryViewMenu);

	}

	protected void addBasicTableColumns(Table table) {
		table.setData(ListBuilderComponent.GERMPLASMS_TABLE_DATA);
		table.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		table.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		table.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		table.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		table.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		table.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		table.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		table.setColumnHeader(ColumnLabels.TAG.getName(), this.messageSource.getMessage(Message.CHECK_ICON));
		table.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		table.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		table.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
		table.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(), this.getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY));
		table.setColumnHeader(ColumnLabels.SEED_RESERVATION.getName(), this.getTermNameFromOntology(ColumnLabels.SEED_RESERVATION));
		table.setColumnHeader(ColumnLabels.ENTRY_CODE.getName(), this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE));
		table.setColumnHeader(ColumnLabels.GID.getName(), this.getTermNameFromOntology(ColumnLabels.GID));
		table.setColumnHeader(ColumnLabels.STOCKID.getName(), this.getTermNameFromOntology(ColumnLabels.STOCKID));
		table.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));
	}

	private void createGermplasmTable(final Table table) {

		this.addBasicTableColumns(table);

		table.setDragMode(TableDragMode.ROW);
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setWidth("100%");
		table.setHeight("280px");
		table.setImmediate(true);
		table.setDebugId("vaadin-newlistdata-tbl");
		this.setContextMenuActionHandler(table);
	}

	private void initializeHandlers() {

		this.dropHandler.addListener(new BuildNewListDropHandler.ListUpdatedListener() {

			@Override
			public void listUpdated(final ListUpdatedEvent event) {
				ListBuilderComponent.this.updateNoOfEntries();
				ListBuilderComponent.this.updateNoOfSelectedEntries();
				ListBuilderComponent.this.tableWithSelectAllLayout.syncItemCheckBoxes();
			}

		});

		this.tableWithSelectAllLayout.getTable().setDropHandler(this.dropHandler);
	}

	@SuppressWarnings("unchecked")
	private void deleteSelectedEntries() {
		Collection<? extends Integer> selectedIdsToDelete =
				(Collection<? extends Integer>) this.tableWithSelectAllLayout.getTable().getValue();
		if (!selectedIdsToDelete.isEmpty()) {
			if (this.listDataTable.getItemIds().size() == selectedIdsToDelete.size()) {
				this.tableWithSelectAllLayout.getTable().getContainerDataSource().removeAllItems();
			} else {
				for (Integer selectedItemId : selectedIdsToDelete) {
					this.tableWithSelectAllLayout.getTable().getContainerDataSource().removeItem(selectedItemId);
				}
			}
			this.assignSerializedEntryNumber();
			this.setHasUnsavedChanges(true);
		} else {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DELETING_LIST_ENTRIES),
					this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
		}

		// reset value
		this.tableWithSelectAllLayout.getTable().setValue(null);

		this.updateNoOfEntries();
		this.updateNoOfSelectedEntries();
	}

	private void updateNoOfEntries(long count) {
		String countLabel = "  <b>" + count + "</b>";
		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + countLabel);
		} else if (this.source.getModeView().equals(ModeView.INVENTORY_VIEW)) {
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LOTS) + ": " + countLabel);
		}
	}

	public void updateNoOfEntries() {
		int count = 0;
		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			count = this.listDataTable.getItemIds().size();
		} else {
			// Inventory View
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
			Collection<?> selectedItems = (Collection<?>) this.listDataTable.getValue();
			count = selectedItems.size();
		} else {
			Collection<?> selectedItems = (Collection<?>) this.listInventoryTable.getTable().getValue();
			count = selectedItems.size();
		}

		this.updateNoOfSelectedEntries(count);
	}

	/**
	 * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
	 */
	private void assignSerializedEntryNumber() {
		List<Integer> itemIds = this.getItemIds(this.tableWithSelectAllLayout.getTable());

		int id = 1;
		for (Integer itemId : itemIds) {
			this.tableWithSelectAllLayout.getTable().getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(id);
			id++;
		}
	}

	/**
	 * Get item id's of a table, and return it as a list
	 * 
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());

		return itemIds;
	}

	public void addFromListDataTable(Table sourceTable) {
		this.dropHandler.addFromListDataTable(sourceTable);
	}

	public boolean isCurrentListSaved() {
		boolean isSaved = false;

		if (this.currentlySavedGermplasmList != null) {

			isSaved = true;
		}

		return isSaved;
	}

	public void enableMenuOptionsAfterSave() {
		this.menuExportList.setEnabled(true);
		this.menuCopyToList.setEnabled(true);
		this.menuReserveInventory.setEnabled(true);
		this.menuCancelReservation.setEnabled(true);
	}

	public void editList(GermplasmList germplasmList) {
		// reset list before placing new one
		this.resetList();

		this.buildNewListTitle.setValue(this.messageSource.getMessage(Message.EDIT_LIST));

		this.currentlySavedGermplasmList = germplasmList;
		this.currentlySetGermplasmInfo = germplasmList;

		this.dropHandler.addGermplasmList(germplasmList.getId(), true);

		// reset the change status to false after loading the germplasm list details and list data in the screen
		this.resetUnsavedChangesFlag();

		if (germplasmList.isLockedList()) {
			this.setUIForLockedList();
		} else {
			this.setUIForUnlockedList();
		}

		this.enableMenuOptionsAfterSave();
	}

	public void addListsFromSearchResults(Set<Integer> lists) {
		this.dropHandler.setHasUnsavedChanges(true);
		for (Integer id : lists) {
			if (id != null) {
				this.dropHandler.addGermplasmList(id, false);
			}

		}
	}

	public void resetList() {

		// list details fields
		this.breedingManagerListDetailsComponent.resetFields();

		// list data table
		this.resetGermplasmTable();

		// list inventory table
		this.listInventoryTable.reset();

		// disabled the menu options when the build new list table has no rows
		this.resetMenuOptions();

		// Clear flag, this is used for saving logic (to save new list or update)
		this.setCurrentlySavedGermplasmList(null);
		this.currentlySetGermplasmInfo = null;

		// Rename the Build New List Header
		this.buildNewListTitle.setValue(this.messageSource.getMessage(Message.BUILD_A_NEW_LIST));

		this.dropHandler =
				new BuildNewListDropHandler(this.source, this.germplasmDataManager, this.germplasmListManager, this.inventoryDataManager,
						this.pedigreeService, this.crossExpansionProperties, this.tableWithSelectAllLayout.getTable(), transactionManager);
		this.initializeHandlers();

		// Reset Save Listener
		this.saveButton.removeListener(this.saveListButtonListener);
		this.saveListButtonListener = new SaveListButtonClickListener(this, this.tableWithSelectAllLayout.getTable(), this.messageSource);
		this.saveButton.addListener(this.saveListButtonListener);

		this.updateNoOfEntries();
		this.updateNoOfSelectedEntries();

		this.resetInventoryMenuOptions();

		// Reset the marker for changes in Build New List
		this.resetUnsavedChangesFlag();
		this.updateView(this.source.getModeView());
	}

	public void updateView(ModeView modeView) {
		if (modeView.equals(ModeView.LIST_VIEW)) {
			this.changeToListView();
		} else if (modeView.equals(ModeView.INVENTORY_VIEW)) {
			this.changeToInventoryView();
		}
	}

	public void resetGermplasmTable() {
		this.tableWithSelectAllLayout.getTable().removeAllItems();
		for (Object col : this.tableWithSelectAllLayout.getTable().getContainerPropertyIds().toArray()) {
			this.tableWithSelectAllLayout.getTable().removeContainerProperty(col);
		}
		this.tableWithSelectAllLayout.getTable().setWidth("100%");
		this.addBasicTableColumns(this.tableWithSelectAllLayout.getTable());

		this.updateNoOfEntries();
		this.updateNoOfSelectedEntries();
	}

	public GermplasmList getCurrentlySetGermplasmListInfo() {
		if (this.currentlySetGermplasmInfo != null) {
			String name = this.currentlySetGermplasmInfo.getName();
			if (name != null) {
				this.currentlySetGermplasmInfo.setName(name.trim());
			}

			String description = this.currentlySetGermplasmInfo.getDescription();
			if (description != null) {
				this.currentlySetGermplasmInfo.setDescription(description.trim());
			}
		}

		return this.currentlySetGermplasmInfo;
	}

	public void addGermplasm(Integer gid) {
		this.dropHandler.addGermplasm(gid);
		this.setHasUnsavedChanges(true);
	}

	public List<GermplasmListData> getListEntriesFromTable() {
		List<GermplasmListData> toreturn = new ArrayList<GermplasmListData>();

		this.assignSerializedEntryNumber();

		for (Object id : this.tableWithSelectAllLayout.getTable().getItemIds()) {
			Integer entryId = (Integer) id;
			Item item = this.tableWithSelectAllLayout.getTable().getItem(entryId);

			GermplasmListData listEntry = new GermplasmListData();
			//listEntry.setId(entryId);

			Button designationButton = (Button) item.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
			String designation = designationButton.getCaption();
			if (designation != null) {
				listEntry.setDesignation(designation);
			} else {
				listEntry.setDesignation("-");
			}

			Object entryCode = item.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue();
			if (entryCode != null) {
				listEntry.setEntryCode(entryCode.toString());
			} else {
				listEntry.setEntryCode("-");
			}

			Button gidButton = (Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue();
			listEntry.setGid(Integer.parseInt(gidButton.getCaption()));

			Object groupName = item.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
			if (groupName != null) {
				String groupNameString = groupName.toString();
				if (groupNameString.length() > 255) {
					groupNameString = groupNameString.substring(0, 255);
				}
				listEntry.setGroupName(groupNameString);
			} else {
				listEntry.setGroupName("-");
			}

			listEntry.setEntryId((Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());

			Object seedSource = item.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue();
			if (seedSource != null) {
				listEntry.setSeedSource(seedSource.toString());
			} else {
				listEntry.setSeedSource("-");
			}

			toreturn.add(listEntry);
		}
		return toreturn;
	}

	private void exportListAction() {
		ExportListAsDialog exportListAsDialog = new ExportListAsDialog(this.source, this.currentlySavedGermplasmList, this.listDataTable);
		this.getWindow().addWindow(exportListAsDialog);
	}

	private void exportListForGenotypingOrderAction() {
		if (this.isCurrentListSaved()) {
			if (this.currentlySavedGermplasmList.isLockedList()) {
				String tempFileName = System.getProperty(ListBuilderComponent.USER_HOME) + "/tempListForGenotyping.xls";
				GermplasmListExporter listExporter = new GermplasmListExporter(this.currentlySavedGermplasmList.getId());

				try {
					listExporter.exportKBioScienceGenotypingOrderXLS(tempFileName, 96);
					FileDownloadResource fileDownloadResource =
							new FileDownloadResource(new File(tempFileName), this.source.getApplication());
					String listName = this.currentlySavedGermplasmList.getName();
					fileDownloadResource.setFilename(FileDownloadResource.getDownloadFileName(listName,
							BreedingManagerUtil.getApplicationRequest()).replace(" ", "_")
							+ "ForGenotyping.xls");

					this.source.getWindow().open(fileDownloadResource);

					// must figure out other way to clean-up file because deleting it here makes it unavailable for download

				} catch (GermplasmListExporterException e) {
					MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
							e.getMessage());
					ListBuilderComponent.LOG.error(e.getMessage(), e);
				}
			} else {
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
						this.messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED));
			}
		}
	}

	public void copyToNewListAction() {
		if (this.isCurrentListSaved()) {
			Collection<?> listEntries = (Collection<?>) this.tableWithSelectAllLayout.getTable().getValue();

			if (listEntries == null || listEntries.isEmpty()) {
				MessageNotifier.showRequiredFieldError(this.source.getWindow(),
						this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
			} else {
				this.listManagerCopyToNewListDialog =
						new BaseSubWindow(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
				this.listManagerCopyToNewListDialog.setModal(true);
				this.listManagerCopyToNewListDialog.setWidth("617px");
				this.listManagerCopyToNewListDialog.setHeight("230px");
				this.listManagerCopyToNewListDialog.setResizable(false);
				this.listManagerCopyToNewListDialog.addStyleName(Reindeer.WINDOW_LIGHT);

				try {
					this.listManagerCopyToNewListDialog.addComponent(new ListManagerCopyToNewListDialog(this.source.getWindow(),
							this.listManagerCopyToNewListDialog, this.currentlySavedGermplasmList.getName(), this.tableWithSelectAllLayout
									.getTable(), this.contextUtil.getCurrentUserLocalId(), this.source));
					this.source.getWindow().addWindow(this.listManagerCopyToNewListDialog);
					this.listManagerCopyToNewListDialog.center();
				} catch (MiddlewareQueryException e) {
					ListBuilderComponent.LOG.error("Error copying list entries.", e);
				}
			}
		}
	}

	private void copyToNewListFromInventoryViewAction() {
		// do nothing
	}

	/* SETTERS AND GETTERS */

	public Label getBuildNewListTitle() {
		return this.buildNewListTitle;
	}

	public void setBuildNewListTitle(Label buildNewListTitle) {
		this.buildNewListTitle = buildNewListTitle;
	}

	public BreedingManagerListDetailsComponent getBreedingManagerListDetailsComponent() {
		return this.breedingManagerListDetailsComponent;
	}

	public TableWithSelectAllLayout getTableWithSelectAllLayout() {
		return this.tableWithSelectAllLayout;
	}

	public void setTableWithSelectAllLayout(TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;
	}

	public Table getGermplasmsTable() {
		return this.tableWithSelectAllLayout.getTable();
	}

	public Button getSaveButton() {
		return this.saveButton;
	}

	public void setSaveButton(Button saveButton) {
		this.saveButton = saveButton;
	}

	public GermplasmList getCurrentlySavedGermplasmList() {
		return this.currentlySavedGermplasmList;
	}

	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
		this.currentlySavedGermplasmList = list;
		if (list != null && list.getId() != null) {
			if (list.getStatus() < 100) {
				this.setUIForUnlockedList();
			} else {
				this.setUIForLockedList();
			}
		} else {
			this.setUIForNewList();
		}
	}

	public ListManagerMain getSource() {
		return this.source;
	}

	public AddColumnContextMenu getAddColumnContextMenu() {
		return this.addColumnContextMenu;
	}

	public void openSaveListAsDialog() {
		this.dialog = new SaveListAsDialog(this, this.currentlySavedGermplasmList, this.messageSource.getMessage(Message.EDIT_LIST_HEADER));
		this.getWindow().addWindow(this.dialog);
	}

	public SaveListAsDialog getSaveListAsDialog() {
		return this.dialog;
	}

	public GermplasmList getCurrentListInSaveDialog() {
		return this.dialog.getGermplasmListToSave();
	}

	/**
	 * This method is called by the SaveListAsDialog window displayed when Edit Header button is clicked.
	 */
	@Override
	public void saveList(GermplasmList list) {
		this.currentlySetGermplasmInfo = list;
		this.saveListButtonListener.doSaveAction();

		((BreedingManagerApplication) this.getApplication()).refreshListManagerTree();

		this.resetUnsavedChangesFlag();
		this.source.updateView(this.source.getModeView());
		this.saveListButtonListener.setForceHasChanges(false);
		this.enableMenuOptionsAfterSave();
	}

	public void saveList(GermplasmList list, Boolean showMessages) {
		this.currentlySetGermplasmInfo = list;
		this.saveListButtonListener.doSaveAction(showMessages, false);

		((BreedingManagerApplication) this.getApplication()).refreshListManagerTree();

		this.resetUnsavedChangesFlag();
		this.source.updateView(this.source.getModeView());
		this.enableMenuOptionsAfterSave();
	}

	public SaveListButtonClickListener getSaveListButtonListener() {
		return this.saveListButtonListener;
	}

	public BuildNewListDropHandler getBuildNewListDropHandler() {
		return this.dropHandler;
	}

	/*-------------------------------------LIST INVENTORY RELATED METHODS-------------------------------------*/

	public void viewListAction() {
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
			this.tableWithSelectAllLayout.setVisible(true);
			this.listInventoryTable.setVisible(false);

			this.toolsButtonContainer.addComponent(this.toolsButton, "top:0px; right:0px;");
			this.toolsButtonContainer.removeComponent(this.inventoryViewToolsButton);

			this.topLabel.setValue(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();
			this.resetUnsavedChangesFlag();
		}
	}

	public void changeToInventoryView() {
		if (this.tableWithSelectAllLayout.isVisible()) {
			this.tableWithSelectAllLayout.setVisible(false);
			this.listInventoryTable.setVisible(true);
			this.toolsButtonContainer.removeComponent(this.toolsButton);
			this.toolsButtonContainer.addComponent(this.inventoryViewToolsButton, "top:0; right:0;");

			this.topLabel.setValue(this.messageSource.getMessage(Message.LOTS));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();
			this.resetUnsavedChangesFlag();
		}
	}

	public void viewInventoryAction() {
		if (this.hasUnsavedChanges()) {
			String message = "";
			if (this.currentlySavedGermplasmList != null) {
				message =
						"You have unsaved changes to the list you are editing. You will need to save them before changing views. Do you want to save your changes?";
			} else if (this.currentlySavedGermplasmList == null) {
				message =
						"You need to save the list that you're building before you can switch to the inventory view. Do you want to save the list?";
			}
			this.source.showUnsavedChangesConfirmDialog(message, ModeView.INVENTORY_VIEW);
		} else {
			this.source.setModeView(ModeView.INVENTORY_VIEW);
		}
	}

	public void viewInventoryActionConfirmed() {
		if (this.currentlySavedGermplasmList != null) {
			this.listInventoryTable.setListId(this.currentlySavedGermplasmList.getId());
			this.listInventoryTable.loadInventoryData();
		}

		this.changeToInventoryView();
	}

	private void reserveInventoryAction() {
		// checks if the screen is in the inventory view
		if (!this.inventoryViewMenu.isVisible()) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"Please change to Inventory View first.");
		} else {

			if (this.hasUnsavedChanges()) {
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						"Please save the list first before reserving an inventory.");
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
	}

	public void saveReservationChangesAction() {

		if (this.hasUnsavedChanges()) {

			List<Integer> alreadyAddedEntryIds = new ArrayList<Integer>();
			List<ListDataAndLotDetails> listDataAndLotDetails =
					this.listInventoryTable.getInventoryTableDropHandler().getListDataAndLotDetails();
			for (ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails) {
				if (!alreadyAddedEntryIds.contains(listDataAndLotDetail.getEntryId())) {
					this.dropHandler.addGermplasmFromList(listDataAndLotDetail.getListId(), listDataAndLotDetail.getSourceLrecId());
					alreadyAddedEntryIds.add(listDataAndLotDetail.getEntryId());
				}
			}

			this.saveList(this.currentlySavedGermplasmList, false);

			for (ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails) {
				this.listInventoryTable.getInventoryTableDropHandler().assignLrecIdToRowsFromListWithEntryId(
						listDataAndLotDetail.getListId(), listDataAndLotDetail.getEntryId());
			}

			this.listInventoryTable.getInventoryTableDropHandler().resetListDataAndLotDetails();

			this.reserveInventoryAction = new ReserveInventoryAction(this);
			boolean success =
					this.reserveInventoryAction.saveReserveTransactions(this.getValidReservationsToSave(),
							this.currentlySavedGermplasmList.getId());
			if (success) {
				this.refreshInventoryColumns(this.getValidReservationsToSave());
				this.resetListInventoryTableValues();
			}
		}
	}

	public void cancelReservationsAction() {
		List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

		if (lotDetailsGid == null || lotDetailsGid.isEmpty()) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"Please select at least 1 lot to cancel reservations.");
		} else {
			if (!this.listInventoryTable.isSelectedEntriesHasReservation(lotDetailsGid)) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						"There is no reservation to the current selected lots.");
			} else {
				ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.CANCEL_RESERVATIONS),
						"Are you sure you want to cancel the selected reservations?", this.messageSource.getMessage(Message.YES),
						this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							ListBuilderComponent.this.cancelReservations();
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
			ListBuilderComponent.LOG.error(e.getMessage(), e);
		}

		this.refreshInventoryColumns(this.getLrecIds(lotDetailsGid));
		this.listInventoryTable.resetRowsForCancelledReservation(lotDetailsGid, this.currentlySavedGermplasmList.getId());

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
						this.inventoryDataManager.getLotCountsForListEntries(this.currentlySavedGermplasmList.getId(),
								new ArrayList<Integer>(entryIds));
			}
		} catch (MiddlewareQueryException e) {
			ListBuilderComponent.LOG.error(e.getMessage(), e);
		}

		for (GermplasmListData listData : germplasmListDataEntries) {
			Item item = this.listDataTable.getItem(listData.getId());

			// #1 Available Inventory

			// default value
			String availInv = "-";
			if (listData.getInventoryInfo().getLotCount().intValue() != 0) {
				availInv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			Button inventoryButton =
					new Button(availInv, new InventoryLinkButtonClickListener(this.source, this.currentlySavedGermplasmList.getId(),
							listData.getId(), listData.getGid()));
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription("Click to view Inventory Details");

			if ("-".equalsIgnoreCase(availInv)) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription("No Lot for this Germplasm");
			} else {
				inventoryButton.setDescription("Click to view Inventory Details");
			}
			item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

			Button gidButton = (Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue();
			String gidString = "";

			if (gidButton != null) {
				gidString = gidButton.getCaption();
			}

			this.updateAvailInvValues(Integer.valueOf(gidString), availInv);

			// Seed Reserved

			// default value
			String seedRes = "-";
			if (listData.getInventoryInfo().getReservedLotCount().intValue() != 0) {
				seedRes = listData.getInventoryInfo().getReservedLotCount().toString().trim();
			}

			item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
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
			Double newRes = entry.getValue();

			Item itemToUpdate = this.listInventoryTable.getTable().getItem(lot);
			itemToUpdate.getItemProperty(ColumnLabels.NEWLY_RESERVED.getName()).setValue(newRes);
		}

		this.removeReserveInventoryWindow(this.reserveInventory);

		// update lot reservatios to save
		this.updateLotReservationsToSave(validReservations);

		// if there are no valid reservations
		if (validReservations.isEmpty()) {
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

	@Override
	public void addReserveInventoryWindow(ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().addWindow(this.reserveInventory);
	}

	@Override
	public void addReservationStatusWindow(ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.removeReserveInventoryWindow(this.reserveInventory);
		this.source.getWindow().addWindow(this.reservationStatus);
	}

	@Override
	public void removeReserveInventoryWindow(ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().removeWindow(this.reserveInventory);
	}

	@Override
	public void removeReservationStatusWindow(ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.source.getWindow().removeWindow(this.reservationStatus);

	}

	public void resetListInventoryTableValues() {
		if (this.currentlySavedGermplasmList != null) {
			this.listInventoryTable.updateListInventoryTableAfterSave();
		} else {
			this.listInventoryTable.reset();
		}

		this.resetInventoryMenuOptions();

		// reset the reservations to save
		this.validReservationsToSave.clear();

		this.resetUnsavedChangesFlag();
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

	public Map<ListEntryLotDetails, Double> getValidReservationsToSave() {
		return this.validReservationsToSave;
	}

	@Override
	public Component getParentComponent() {
		return this.source;
	}

	@Override
	public void setHasUnsavedChangesMain(boolean hasChanges) {
		this.source.setHasUnsavedChangesMain(hasChanges);
	}

	@Override
	public void setHasUnsavedChanges(Boolean hasChanges) {
		this.hasChanges = hasChanges;
		this.setHasUnsavedChangesMain(this.hasChanges);
	}

	public boolean hasUnsavedChanges() {

		if (this.breedingManagerListDetailsComponent.isChanged()) {
			this.setHasUnsavedChanges(true);
		}

		if (this.dropHandler.isChanged()) {
			this.setHasUnsavedChanges(true);
		}

		if (this.listInventoryTable.getInventoryTableDropHandler().isChanged()) {
			this.setHasUnsavedChanges(true);
		}

		return this.hasChanges;
	}

	public void resetUnsavedChangesFlag() {
		this.breedingManagerListDetailsComponent.setChanged(false);
		this.dropHandler.setChanged(false);
		this.listInventoryTable.getInventoryTableDropHandler().setChanged(false);
		this.setHasUnsavedChanges(false);
	}

	private void updateAvailInvValues(Integer gid, String availInv) {
		List<Integer> itemIds = this.getItemIds(this.listDataTable);
		for (Integer itemId : itemIds) {
			Item item = this.listDataTable.getItem(itemId);
			Button gidButton = (Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue();

			String currentGid = "";
			if (gidButton != null) {
				currentGid = gidButton.getCaption();
			}

			if (currentGid.equals(gid)) {
				((Button) item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue()).setCaption(availInv);
			}
		}
		this.listDataTable.requestRepaint();
	}

	/*-------------------------------------END OF LIST INVENTORY RELATED METHODS-------------------------------------*/

	public ListManagerInventoryTable getListInventoryTable() {
		return this.listInventoryTable;
	}

	private boolean userIsListOwner() {
		try {
			return this.currentlySavedGermplasmList.getUserId().equals(this.contextUtil.getCurrentUserLocalId());
		} catch (MiddlewareQueryException e) {
			ListBuilderComponent.LOG.error(e.getMessage(), e);
			return false;
		}
	}

	public void discardChangesInListView() {
		this.editList(this.currentlySavedGermplasmList);
		this.viewInventoryActionConfirmed();
	}

	public void discardChangesInInventoryView() {
		this.resetListInventoryTableValues();
		this.changeToListView();
	}

	public void enableReserveInventory() {
		this.menuReserveInventory.setEnabled(true);
	}

	@Override
	public void refreshListInventoryItemCount() {
		this.updateNoOfEntries();
		this.updateNoOfSelectedEntries();
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabel) {
		return columnLabel.getTermNameFromOntology(this.ontologyDataManager);
	}

	protected void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}


package org.generationcp.breeding.manager.listmanager;

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
import org.generationcp.breeding.manager.customcomponent.SortableButton;
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
import org.generationcp.breeding.manager.inventory.SeedInventoryImportFileComponent;
import org.generationcp.breeding.manager.inventory.SeedInventoryListExporter;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryExportException;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.dialog.ListManagerCopyToListDialog;
import org.generationcp.breeding.manager.listmanager.listeners.AddColumnMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.ResetListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.SaveListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.breeding.manager.listmanager.util.DropHandlerMethods.ListUpdatedEvent;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.VaadinFileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configurable
public class ListBuilderComponent extends VerticalLayout implements InitializingBean, BreedingManagerLayout, SaveListAsDialogSource,
		ReserveInventorySource, UnsavedChangesSource, InventoryDropTargetContainer {

	public static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";

	private final class LockButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
			if (!ListBuilderComponent.this.currentlySavedGermplasmList.isLockedList()) {
				ListBuilderComponent.this.currentlySavedGermplasmList
						.setStatus(ListBuilderComponent.this.currentlySavedGermplasmList.getStatus() + 100);
				try {
					ListBuilderComponent.this.currentlySetGermplasmInfo = ListBuilderComponent.this.currentlySavedGermplasmList;
					ListBuilderComponent.this.saveListButtonListener.doSaveAction(false);

					ListBuilderComponent.this.contextUtil.logProgramActivity("Locked a germplasm list.",
							"Locked list " + ListBuilderComponent.this.currentlySavedGermplasmList.getId() + " - "
									+ ListBuilderComponent.this.currentlySavedGermplasmList.getName());

				} catch (final MiddlewareQueryException e) {
					ListBuilderComponent.LOG.error("Error with unlocking list.", e);
					MessageNotifier.showError(ListBuilderComponent.this.getWindow(), "Database Error!",
							"Error with loocking list. " + ListBuilderComponent.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				}
				ListBuilderComponent.this.setUIForLockedList();
			}
		}
	}

	private final class UnlockButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
			if (ListBuilderComponent.this.currentlySavedGermplasmList.isLockedList()) {
				ListBuilderComponent.this.currentlySavedGermplasmList
						.setStatus(ListBuilderComponent.this.currentlySavedGermplasmList.getStatus() - 100);
				try {
					ListBuilderComponent.this.currentlySetGermplasmInfo = ListBuilderComponent.this.currentlySavedGermplasmList;
					ListBuilderComponent.this.saveListButtonListener.doSaveAction(false);

					ListBuilderComponent.this.contextUtil.logProgramActivity("Unlocked a germplasm list.",
							"Unlocked list " + ListBuilderComponent.this.currentlySavedGermplasmList.getId() + " - "
									+ ListBuilderComponent.this.currentlySavedGermplasmList.getName());

				} catch (final MiddlewareQueryException e) {
					ListBuilderComponent.LOG.error("Error with unlocking list.", e);
					MessageNotifier.showError(ListBuilderComponent.this.getWindow(), "Database Error!",
							"Error with unlocking list. " + ListBuilderComponent.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				}
				ListBuilderComponent.this.setUIForUnlockedList();
			}
		}
	}

	private final class ToolsButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1345004576139547723L;

		@Override
		public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {

			if (ListBuilderComponent.this.isCurrentListSaved()) {
				ListBuilderComponent.this.enableMenuOptionsAfterSave();
			}

			ListBuilderComponent.this.addColumnContextMenu.refreshAddColumnMenu(ListBuilderComponent.this.listDataTable);
			ListBuilderComponent.this.menu.show(event.getClientX(), event.getClientY());

		}
	}

	protected final class InventoryViewMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final ClickEvent event) {
			final ContextMenuItem clickedItem = event.getClickedItem();

			if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SAVE_RESERVATIONS))) {

				/*
				 * Save reservation needs to be synchronized on ListBuilderComponent lock object. This will ensure lock will apply to all
				 * instances of ListBuilderComponent invoking save reservation.
				 */

				synchronized (ListBuilderComponent.class) {
					final TransactionTemplate transactionTemplateForSavingReservation =
							new TransactionTemplate(ListBuilderComponent.this.transactionManager);
					transactionTemplateForSavingReservation.execute(new TransactionCallbackWithoutResult() {

						@Override
						protected void doInTransactionWithoutResult(final TransactionStatus status) {
							ListBuilderComponent.this.saveReservationsAction();
						}
					});
				}

			} else {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(ListBuilderComponent.this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(final TransactionStatus status) {
						// Get reference to clicked item

						if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))) {
							ListBuilderComponent.this.viewListAction();
						} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.COPY_TO_LIST))) {
							ListBuilderComponent.this.copyToNewListFromInventoryViewAction();
						} else if (clickedItem.getName()
								.equals(ListBuilderComponent.this.messageSource.getMessage(Message.RESERVE_INVENTORY))) {
							ListBuilderComponent.this.reserveInventoryAction();
						} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
							ListBuilderComponent.this.listInventoryTable.getTable()
									.setValue(ListBuilderComponent.this.listInventoryTable.getTable().getItemIds());
						} else if (clickedItem.getName()
								.equals(ListBuilderComponent.this.messageSource.getMessage(Message.CANCEL_RESERVATIONS))) {
							ListBuilderComponent.this.cancelReservationsAction();
						} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.RESET_LIST))) {
							ListBuilderComponent.this.resetButton.click();
						} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SAVE_LIST))) {
							ListBuilderComponent.this.saveButton.click();
						} else if (clickedItem.getName()
								.equals(ListBuilderComponent.this.messageSource.getMessage(Message.EXPORT_SEED_LIST))) {
							ListBuilderComponent.this.exportSeedPreparationList();
						} else if (clickedItem.getName()
								.equals(ListBuilderComponent.this.messageSource.getMessage(Message.IMPORT_SEED_LIST))) {
							ListBuilderComponent.this.openImportSeedPreparationDialog();
						} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.PRINT_LABELS))) {
							ListBuilderComponent.this.createLabelsAction();
						}
					}
				});
			}

		}
	}

	private final class MenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2331333436994090161L;

		@Override
		public void contextItemClick(final ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(ListBuilderComponent.this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {
					final ContextMenuItem clickedItem = event.getClickedItem();
					final Table germplasmsTable = ListBuilderComponent.this.tableWithSelectAllLayout.getTable();
					if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						germplasmsTable.setValue(germplasmsTable.getItemIds());
					} else if (clickedItem.getName()
							.equals(ListBuilderComponent.this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES))) {
						ListBuilderComponent.this.deleteSelectedEntries();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.EXPORT_LIST))) {
						ListBuilderComponent.this.exportListAction();
					} else if (clickedItem.getName()
							.equals(ListBuilderComponent.this.messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER))) {
						ListBuilderComponent.this.exportListForGenotypingOrderAction();
					} else if (clickedItem.getName().equals(ListBuilderComponent.this.messageSource.getMessage(Message.COPY_TO_LIST))) {// changed
						// label
						ListBuilderComponent.this.copyToNewListAction();
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
	private GermplasmListManager germplasmListManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private UserService userService;

	@Resource
	private PlatformTransactionManager transactionManager;

	public static final String GERMPLASMS_TABLE_DATA = "Germplasms Table Data";
	static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action ACTION_DELETE_SELECTED_ENTRIES = new Action("Delete Selected Entries");
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU =
			new Action[] {ListBuilderComponent.ACTION_SELECT_ALL, ListBuilderComponent.ACTION_DELETE_SELECTED_ENTRIES};
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

	private Window listManagerCopyToListDialog;

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
	private ContextMenuItem menuCopyToListFromInventory;
	private ContextMenuItem menuReserveInventory;
	private ContextMenuItem menuSaveReserveInventory;
	private ContextMenuItem menuCancelReservation;
	private ContextMenuItem listEditingOptions;
	private ContextMenuItem listEditingOptionsForLots;
	private ContextMenuItem inventoryManagementOptions;
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
	private List<ListEntryLotDetails> persistedReservationToCancel;

	private long listEntriesCount;
	protected List<String> attributeAndNameTypeColumns = new ArrayList<>();

	public ListBuilderComponent() {
		super();
		this.reserveInventoryAction = new ReserveInventoryAction(this);
	}

	public ListBuilderComponent(final ListManagerMain source) {
		this();
		this.source = source;
		this.currentlySavedGermplasmList = null;
		this.currentlySetGermplasmInfo = null;
		this.setDebugId("ListBuilderComponent");
	}

	protected void createLabelsAction() {
		if (this.currentlySavedGermplasmList != null) {

			ListCommonActionsUtil.handleCreateLabelsAction(this.currentlySavedGermplasmList.getId(), this.inventoryDataManager,
					this.messageSource, this.contextUtil, this.getApplication(), this.getWindow());
		} else {

			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.ERROR_COULD_NOT_CREATE_LABELS));
		}

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

		this.unlockButton = new IconButton(
				"<span class='bms-locked' style='position: relative; top:5px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
				ListBuilderComponent.LOCK_TOOLTIP);
		this.unlockButton.setData(ListBuilderComponent.UNLOCK_BUTTON_ID);
		this.unlockButton.setVisible(false);

		this.lockButton = new IconButton(
				"<span class='bms-lock-open' style='position: relative; top:5px; left: 2px; left: 2px; color: #666666;font-size: 16px; font-weight: bold;'></span>",
				ListBuilderComponent.LOCK_TOOLTIP);
		this.lockButton.setData(ListBuilderComponent.LOCK_BUTTON_ID);
		this.lockButton.setVisible(false);

		this.buildNewListTitle = new Label(this.messageSource.getMessage(Message.BUILD_A_NEW_LIST));
		this.buildNewListTitle.setDebugId("buildNewListTitle");
		this.buildNewListTitle.setWidth("200px");
		this.buildNewListTitle.setStyleName(Bootstrap.Typography.H4.styleName());
		this.buildNewListTitle.addStyleName(AppConstants.CssStyles.BOLD);

		this.buildNewListDesc = new Label();
		this.buildNewListDesc.setDebugId("buildNewListDesc");
		this.buildNewListDesc.setValue("Build or revise your list by dragging in entries from the left.");
		this.buildNewListDesc.addStyleName("lm-word-wrap");
		this.buildNewListDesc.setWidth("100%");
		this.buildNewListDesc.setHeight("55px");

		this.topLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.topLabel.setDebugId("topLabel");
		this.topLabel.setWidth("130px");
		this.topLabel.setStyleName(Bootstrap.Typography.H4.styleName());

		this.totalListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalListEntriesLabel.setDebugId("totalListEntriesLabel");
		this.totalListEntriesLabel.setWidth("110px");
		this.updateNoOfEntries(0);

		this.totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedListEntriesLabel.setDebugId("totalSelectedListEntriesLabel");
		this.updateNoOfSelectedEntries(0);

		this.editHeaderButton = new Button(this.messageSource.getMessage(Message.EDIT_HEADER));
		this.editHeaderButton.setDebugId("editHeaderButton");
		this.editHeaderButton.setImmediate(true);
		this.editHeaderButton.setStyleName(BaseTheme.BUTTON_LINK);

		this.viewHeaderButton = new Button(this.messageSource.getMessage(Message.VIEW_HEADER));
		this.viewHeaderButton.setDebugId("viewHeaderButton");
		this.viewHeaderButton.addStyleName(BaseTheme.BUTTON_LINK);
		this.viewHeaderButton.setVisible(false);

		if (this.currentlySavedGermplasmList != null) {
			this.viewListHeaderWindow = new ViewListHeaderWindow(this.currentlySavedGermplasmList,
				this.userService.getAllUserIDFullNameMap(), this.germplasmListManager.getGermplasmListTypes());
			this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());
		}

		this.breedingManagerListDetailsComponent = new BreedingManagerListDetailsComponent();
		this.breedingManagerListDetailsComponent.setDebugId("breedingManagerListDetailsComponent");

		this.tableWithSelectAllLayout = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		this.tableWithSelectAllLayout.setDebugId("tableWithSelectAllLayout");

		if (this.currentlySavedGermplasmList != null) {
			this.listInventoryTable = new ListManagerInventoryTable(this.source, this.currentlySavedGermplasmList.getId(), false, true);
			this.listInventoryTable.setDebugId("listInventoryTable");
		} else {
			this.listInventoryTable = new ListManagerInventoryTable(this.source, null, false, true);
			this.listInventoryTable.setDebugId("listInventoryTable");
		}
		this.listInventoryTable.setVisible(false);

		this.listDataTable = this.tableWithSelectAllLayout.getTable();
		this.createGermplasmTable(this.listDataTable);

		this.listDataTable.setWidth("100%");
		this.listDataTable.setHeight("480px");

		this.menu = new ContextMenu();
		this.menu.setDebugId("menu");
		this.menu.setWidth("300px");

		// re-arranging Action menu items
		this.listEditingOptions = this.menu.addItem(this.messageSource.getMessage(Message.LIST_EDITING_OPTIONS));
		this.listEditingOptions.addItem(this.messageSource.getMessage(Message.SAVE_LIST));
		this.listEditingOptions.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.menuDeleteSelectedEntries = this.listEditingOptions.addItem(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
		this.menuCopyToList = this.listEditingOptions.addItem(this.messageSource.getMessage(Message.COPY_TO_LIST));
		this.listEditingOptions.addItem(this.messageSource.getMessage(Message.RESET_LIST));

		this.initializeAddColumnContextMenu();

		this.menuExportList = this.menu.addItem(this.messageSource.getMessage(Message.EXPORT_LIST));

		this.inventoryViewMenu = new ContextMenu();
		this.inventoryViewMenu.setDebugId("inventoryViewMenu");
		this.inventoryViewMenu.setWidth("300px");

		//// Re-arranging Menu Items for Lots view
		this.listEditingOptionsForLots = this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.LIST_EDITING_OPTIONS));
		this.listEditingOptionsForLots.addItem(this.messageSource.getMessage(Message.SAVE_LIST));
		this.listEditingOptionsForLots.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.menuCopyToListFromInventory = this.listEditingOptionsForLots.addItem(this.messageSource.getMessage(Message.COPY_TO_LIST));
		this.listEditingOptionsForLots.addItem(this.messageSource.getMessage(Message.RESET_LIST));

		this.inventoryManagementOptions =
				this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.INVENTORY_MANAGEMENT_OPTIONS));
		this.menuReserveInventory = this.inventoryManagementOptions.addItem(this.messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.menuSaveReserveInventory = this.inventoryManagementOptions.addItem(this.messageSource.getMessage(Message.SAVE_RESERVATIONS));
		this.menuCancelReservation = this.inventoryManagementOptions.addItem(this.messageSource.getMessage(Message.CANCEL_RESERVATIONS));

		this.inventoryViewMenu.addItem(this.messageSource.getMessage(Message.PRINT_LABELS));

		// Temporarily disable to Copy to List in InventoryView
		this.menuCopyToListFromInventory.setEnabled(false);

		this.resetMenuOptions();
		this.resetInventoryMenuOptions();

		this.toolsButton = new ActionButton();
		this.toolsButton.setDebugId("toolsButton");
		this.toolsButton.setData(ListBuilderComponent.TOOLS_BUTTON_ID);

		this.inventoryViewToolsButton = new ActionButton();
		this.inventoryViewToolsButton.setDebugId("inventoryViewToolsButton");
		this.inventoryViewToolsButton.setData(ListBuilderComponent.TOOLS_BUTTON_ID);

		this.dropHandler = new BuildNewListDropHandler(this.source, this.tableWithSelectAllLayout.getTable());

		this.saveButton = new Button();
		this.saveButton.setDebugId("saveButton");
		this.saveButton.setCaption(this.messageSource.getMessage(Message.SAVE_LABEL));
		this.saveButton.setWidth("80px");
		this.saveButton.addStyleName(Bootstrap.Buttons.INFO.styleName());

		this.resetButton = new Button();
		this.resetButton.setDebugId("resetButton");
		this.resetButton.setCaption(this.messageSource.getMessage(Message.RESET));
		this.resetButton.setWidth("80px");
		this.resetButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());

		// Inventory Related Variables
		this.validReservationsToSave = new HashMap<>();

		// reset the marker for unsaved changes on initial loading
		this.resetUnsavedChangesFlag();
	}

	void initializeAddColumnContextMenu() {
		final ListBuilderAddColumnSource addColumnSource =
				new ListBuilderAddColumnSource(this, this.tableWithSelectAllLayout.getTable(), ColumnLabels.GID.getName());
		this.addColumnContextMenu = new AddColumnContextMenu(addColumnSource, this.menu, this.listEditingOptions, this.messageSource);
		this.addColumnContextMenu.addListener(new AddColumnMenuItemClickListener(addColumnSource));
	}

	public void resetMenuOptions() {
		// initially disabled when the current list building is not yet save or
		// being reset
		this.menuExportList.setEnabled(false);
		this.menuCopyToList.setEnabled(false);
		this.menuCancelReservation.setEnabled(false);
	}

	private void resetInventoryMenuOptions() {

		this.menuSaveReserveInventory.setEnabled(false);
		// Temporarily disable to Copy to List in InventoryView
		this.menuCopyToListFromInventory.setEnabled(false);

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
		final ListBuilderFillWithSource fillWithSource =
				new ListBuilderFillWithSource(this, this.listDataTable, ColumnLabels.GID.getName());
		this.fillWith = new FillWith(fillWithSource, this, this.messageSource);
		this.fillWith.setTableHeaderListener(this.listDataTable);

		this.menu.addListener(new MenuClickListener());

		this.inventoryViewMenu.addListener(new InventoryViewMenuClickListener());

		this.toolsButton.addListener(new ToolsButtonClickListener());

		this.inventoryViewToolsButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1345004576139547723L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				ListBuilderComponent.this.inventoryViewMenu.show(event.getClientX(), event.getClientY());
			}
		});

		this.editHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -6306973449416812850L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				ListBuilderComponent.this.openSaveListAsDialog();
			}
		});

		this.viewHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				ListBuilderComponent.this.viewListHeaderWindow =
						new ViewListHeaderWindow(ListBuilderComponent.this.currentlySavedGermplasmList,
							ListBuilderComponent.this.userService.getAllUserIDFullNameMap(),
								ListBuilderComponent.this.germplasmListManager.getGermplasmListTypes());
				ListBuilderComponent.this.getWindow().addWindow(ListBuilderComponent.this.viewListHeaderWindow);
			}
		});

		this.saveListButtonListener = new SaveListButtonClickListener(this, this.tableWithSelectAllLayout.getTable(), this.messageSource);
		this.saveButton.addListener(this.saveListButtonListener);

		this.saveButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 7449465533478658983L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
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
			public void valueChange(final ValueChangeEvent event) {
				ListBuilderComponent.this.updateNoOfSelectedEntries();
			}
		});

		this.listInventoryTable.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				ListBuilderComponent.this.updateNoOfSelectedEntries();
			}
		});

	}

	public void setContextMenuActionHandler(final Table table) {

		table.removeActionHandler(this.contextMenuActionHandler);

		this.contextMenuActionHandler = new Action.Handler() {

			private static final long serialVersionUID = 1884343225476178686L;

			@Override
			public Action[] getActions(final Object target, final Object sender) {
				return ListBuilderComponent.this.getContextMenuActions();
			}

			@Override
			public void handleAction(final Action action, final Object sender, final Object target) {
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

		this.viewListHeaderWindow = new ViewListHeaderWindow(this.currentlySavedGermplasmList,
			this.userService.getAllUserIDFullNameMap(), this.germplasmListManager.getGermplasmListTypes());
		this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());

		this.saveButton.setEnabled(false);

		this.source.setUIForLockedListBuilder();
		this.fillWith.setContextMenuEnabled(this.listDataTable, false);
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
		this.fillWith.setContextMenuEnabled(this.listDataTable, true);
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
		headingLayout.setDebugId("headingLayout");
		headingLayout.setDebugId("headingLayout");

		final VerticalLayout listBuilderHeadingContainer = new VerticalLayout();
		listBuilderHeadingContainer.setDebugId("listBuilderHeadingContainer");

		listBuilderHeadingContainer.addComponent(headingLayout);
		listBuilderHeadingContainer.addComponent(this.buildNewListDesc);

		this.addComponent(listBuilderHeadingContainer);

		final Panel listBuilderPanel = new Panel();
		listBuilderPanel.setDebugId("listBuilderPanel");
		listBuilderPanel.setStyleName(Reindeer.PANEL_LIGHT + " " + AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
		listBuilderPanel.setCaption(null);
		listBuilderPanel.setWidth("100%");

		final VerticalLayout listDataTableLayout = new VerticalLayout();
		listDataTableLayout.setDebugId("listDataTableLayout");
		listDataTableLayout.setMargin(true);
		listDataTableLayout.setSpacing(true);
		listDataTableLayout.addStyleName("listDataTableLayout");

		listBuilderPanel.setContent(listDataTableLayout);

		final HorizontalLayout listBuilderPanelTitleContainer = new HorizontalLayout();
		listBuilderPanelTitleContainer.setDebugId("listBuilderPanelTitleContainer");
		listBuilderPanelTitleContainer.setWidth("100%");
		listBuilderPanelTitleContainer.setSpacing(true);

		final HeaderLabelLayout listEntriesTitle = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.topLabel);
		listEntriesTitle.setDebugId("listEntriesTitle");
		listEntriesTitle.setDebugId("listEntriesTitle");
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

		final HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setDebugId("leftSubHeaderLayout");
		leftSubHeaderLayout.setSpacing(true);
		leftSubHeaderLayout.addComponent(this.totalListEntriesLabel);
		leftSubHeaderLayout.addComponent(this.totalSelectedListEntriesLabel);
		leftSubHeaderLayout.setComponentAlignment(this.totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		leftSubHeaderLayout.setComponentAlignment(this.totalSelectedListEntriesLabel, Alignment.MIDDLE_LEFT);

		this.toolsButtonContainer = new AbsoluteLayout();
		this.toolsButtonContainer.setDebugId("toolsButtonContainer");
		this.toolsButtonContainer.setHeight("27px");
		this.toolsButtonContainer.setWidth("90px");
		this.toolsButtonContainer.addComponent(this.toolsButton, "top:0; right:0");

		final HorizontalLayout subHeaderLayout = new HorizontalLayout();
		subHeaderLayout.setDebugId("subHeaderLayout");
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
		buttons.setDebugId("buttons");
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

	protected void addBasicTableColumns(final Table table) {
		table.setData(ListBuilderComponent.GERMPLASMS_TABLE_DATA);
		table.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		table.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		table.addContainerProperty(ColumnLabels.DESIGNATION.getName(), SortableButton.class, null);
		table.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), SortableButton.class, null);
		table.addContainerProperty(ColumnLabels.TOTAL.getName(), SortableButton.class, null);
		table.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.GID.getName(), SortableButton.class, null);
		table.addContainerProperty(ColumnLabels.GROUP_ID.getName(), String.class, null);
		table.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		table.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		table.setColumnHeader(ColumnLabels.TAG.getName(), this.messageSource.getMessage(Message.CHECK_ICON));
		table.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		table.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		table.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
		table.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(), this.getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY));
		table.setColumnHeader(ColumnLabels.TOTAL.getName(), this.getTermNameFromOntology(ColumnLabels.TOTAL));
		table.setColumnHeader(ColumnLabels.ENTRY_CODE.getName(), this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE));
		table.setColumnHeader(ColumnLabels.GID.getName(), this.getTermNameFromOntology(ColumnLabels.GID));
		table.setColumnHeader(ColumnLabels.GROUP_ID.getName(), this.getTermNameFromOntology(ColumnLabels.GROUP_ID));
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
	protected void deleteSelectedEntries() {
		final Collection<? extends Integer> selectedIdsToDelete =
				(Collection<? extends Integer>) this.tableWithSelectAllLayout.getTable().getValue();
		if (!selectedIdsToDelete.isEmpty()) {
			ConfirmDialog.show(
				this.getWindow(), this.messageSource.getMessage(Message.DELETE_GERMPLASM_ENTRIES),
					this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES_FROM_THE_LIST_CONFIRM), this.messageSource.getMessage(Message.YES),
					this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(final ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								ListBuilderComponent.this.doDeleteSelectedEntries();
							}
						}
					});
		} else {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DELETING_LIST_ENTRIES),
					this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
		}
	}

	@SuppressWarnings("unchecked")
	void doDeleteSelectedEntries() {
		final Collection<? extends Integer> selectedIdsToDelete =
				(Collection<? extends Integer>) this.tableWithSelectAllLayout.getTable().getValue();
		if (this.listDataTable.getItemIds().size() == selectedIdsToDelete.size()) {
			this.tableWithSelectAllLayout.getTable().getContainerDataSource().removeAllItems();
		} else {
			for (final Integer selectedItemId : selectedIdsToDelete) {
				this.tableWithSelectAllLayout.getTable().getContainerDataSource().removeItem(selectedItemId);
			}
		}
		this.assignSerializedEntryNumber();
		this.setHasUnsavedChanges(true);
		this.listDataTable.focus();
		// reset value
		this.tableWithSelectAllLayout.getTable().setValue(null);

		this.updateNoOfEntries();
		this.updateNoOfSelectedEntries();
	}

	private void updateNoOfEntries(final long count) {
		final String countLabel = "  <b>" + count + "</b>";
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

	private void updateNoOfSelectedEntries(final int count) {
		this.totalSelectedListEntriesLabel
				.setValue("<i>" + this.messageSource.getMessage(Message.SELECTED) + ": " + "  <b>" + count + "</b></i>");
	}

	private void updateNoOfSelectedEntries() {
		int count = 0;

		if (this.source.getModeView().equals(ModeView.LIST_VIEW)) {
			final Collection<?> selectedItems = (Collection<?>) this.listDataTable.getValue();
			count = selectedItems.size();
		} else {
			final Collection<?> selectedItems = (Collection<?>) this.listInventoryTable.getTable().getValue();
			count = selectedItems.size();
		}

		this.updateNoOfSelectedEntries(count);
	}

	/**
	 * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
	 */
	private void assignSerializedEntryNumber() {
		final List<Integer> itemIds = this.getItemIds(this.tableWithSelectAllLayout.getTable());

		int id = 1;
		for (final Integer itemId : itemIds) {
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
	private List<Integer> getItemIds(final Table table) {
		final List<Integer> itemIds = new ArrayList<>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());

		return itemIds;
	}

	public void addFromListDataTable(final Table sourceTable) {
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

	public void editList(final GermplasmList germplasmList) {
		// reset list before placing new one
		this.resetList();

		this.buildNewListTitle.setValue(this.messageSource.getMessage(Message.EDIT_LIST));

		this.currentlySavedGermplasmList = germplasmList;
		this.currentlySetGermplasmInfo = germplasmList;

		this.dropHandler.addGermplasmList(germplasmList.getId(), true);

		// reset the change status to false after loading the germplasm list
		// details and list data in the screen
		this.resetUnsavedChangesFlag();

		if (germplasmList.isLockedList()) {
			this.setUIForLockedList();
		} else {
			this.setUIForUnlockedList();
		}

		this.enableMenuOptionsAfterSave();
	}

	public void addListsFromSearchResults(final Set<Integer> lists) {
		this.dropHandler.setHasUnsavedChanges(true);
		for (final Integer id : lists) {
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

		// Clear flag, this is used for saving logic (to save new list or
		// update)
		this.setCurrentlySavedGermplasmList(null);
		this.currentlySetGermplasmInfo = null;

		// Rename the Build New List Header
		this.buildNewListTitle.setValue(this.messageSource.getMessage(Message.BUILD_A_NEW_LIST));

		this.dropHandler = new BuildNewListDropHandler(this.source, this.tableWithSelectAllLayout.getTable());
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

		// clears unsaved reservations if there are any.
		if (!CollectionUtils.isEmpty(this.getValidReservationsToSave())) {
			this.getValidReservationsToSave().clear();
		}
	}

	public void updateView(final ModeView modeView) {
		if (modeView.equals(ModeView.LIST_VIEW)) {
			this.changeToListView();
		} else if (modeView.equals(ModeView.INVENTORY_VIEW)) {
			this.changeToInventoryView();
		}
	}

	public void resetGermplasmTable() {
		this.tableWithSelectAllLayout.getTable().removeAllItems();
		for (final Object col : this.tableWithSelectAllLayout.getTable().getContainerPropertyIds().toArray()) {
			this.tableWithSelectAllLayout.getTable().removeContainerProperty(col);
		}
		this.tableWithSelectAllLayout.getTable().setWidth("100%");
		this.addBasicTableColumns(this.tableWithSelectAllLayout.getTable());

		this.updateNoOfEntries();
		this.updateNoOfSelectedEntries();
	}

	public GermplasmList getCurrentlySetGermplasmListInfo() {
		if (this.currentlySetGermplasmInfo != null) {
			final String name = this.currentlySetGermplasmInfo.getName();
			if (name != null) {
				this.currentlySetGermplasmInfo.setName(name.trim());
			}

			final String description = this.currentlySetGermplasmInfo.getDescription();
			if (description != null) {
				this.currentlySetGermplasmInfo.setDescription(description.trim());
			}
		}

		return this.currentlySetGermplasmInfo;
	}

	public void addGermplasm(final List<Integer> gids) {
		this.dropHandler.addGermplasm(gids);
		this.setHasUnsavedChanges(true);
	}

	public List<GermplasmListData> getListEntriesFromTable() {
		final List<GermplasmListData> toreturn = new ArrayList<>();

		this.assignSerializedEntryNumber();

		for (final Object id : this.tableWithSelectAllLayout.getTable().getItemIds()) {
			final Integer entryId = (Integer) id;
			final Item item = this.tableWithSelectAllLayout.getTable().getItem(entryId);

			final GermplasmListData listEntry = new GermplasmListData();
			listEntry.setId(entryId);
			final Button designationButton = (Button) item.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
			final String designation = designationButton.getCaption();
			if (designation != null) {
				listEntry.setDesignation(designation);
			} else {
				listEntry.setDesignation("-");
			}

			final Object entryCode = item.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue();
			if (entryCode != null) {
				listEntry.setEntryCode(entryCode.toString());
			} else {
				listEntry.setEntryCode("-");
			}

			final Button gidButton = (Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue();
			listEntry.setGid(Integer.parseInt(gidButton.getCaption()));

			final Object groupName = item.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
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

			final Object seedSource = item.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue();
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
		final ExportListAsDialog exportListAsDialog =
				new ExportListAsDialog(this.source, this.currentlySavedGermplasmList, this.listDataTable);
		this.getWindow().addWindow(exportListAsDialog);
	}

	public void exportSeedPreparationList() {
		this.exportSeedPreparationList(new SeedInventoryListExporter(this.source, this.currentlySavedGermplasmList));
	}

	public void exportSeedPreparationList(final SeedInventoryListExporter seedInventoryListExporter) {

		if (this.currentlySavedGermplasmList != null) {

			if (!CollectionUtils.isEmpty(this.validReservationsToSave)) {
				MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.WARNING),
						this.messageSource.getMessage(Message.UNSAVED_RESERVATION_WARNING));
			}

			try {
				seedInventoryListExporter.exportSeedPreparationList();
			} catch (final SeedInventoryExportException ex) {
				ListBuilderComponent.LOG.debug(ex.getMessage(), ex);
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
						"Cannot Export Seed Preparation List :" + ex.getMessage());
			}
		} else {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.ERROR_SAVE_LIST_BEFORE_EXPORTING_LIST));
		}
	}

	protected void openImportSeedPreparationDialog() {

		if (this.currentlySavedGermplasmList != null) {

			final Window window = this.getWindow();
			final SeedInventoryImportFileComponent seedInventoryImportFileComponent =
					new SeedInventoryImportFileComponent(this.source, this, this.currentlySavedGermplasmList);
			seedInventoryImportFileComponent.setDebugId("seedInventoryImportFileComponent");
			window.addWindow(seedInventoryImportFileComponent);
		} else {

			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.ERROR_SAVE_LIST_BEFORE_IMPORTING_LIST));
		}
	}

	private void exportListForGenotypingOrderAction() {
		if (this.isCurrentListSaved()) {
			final String tempFileName = System.getProperty(ListBuilderComponent.USER_HOME) + "/tempListForGenotyping.xls";
			final GermplasmListExporter listExporter = new GermplasmListExporter();

			try {
				listExporter.exportKBioScienceGenotypingOrderXLS(this.currentlySavedGermplasmList.getId(), tempFileName, 96);

				final String listName = this.currentlySavedGermplasmList.getName();

				final VaadinFileDownloadResource fileDownloadResource = new VaadinFileDownloadResource(new File(tempFileName),
					listName.replace(" ", "_") + "ForGenotyping.xls", this.source.getApplication());

				this.source.getWindow().open(fileDownloadResource);

				// must figure out other way to clean-up file because
				// deleting it here makes it unavailable for download

			} catch (final GermplasmListExporterException e) {
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
					e.getMessage());
				ListBuilderComponent.LOG.error(e.getMessage(), e);
			}
		}
	}

	public void copyToNewListAction() {
		if (this.isCurrentListSaved()) {
			final Collection<?> listEntries = (Collection<?>) this.tableWithSelectAllLayout.getTable().getValue();

			if (listEntries == null || listEntries.isEmpty()) {
				MessageNotifier.showRequiredFieldError(this.source.getWindow(),
						this.messageSource.getMessage(Message.ERROR_LIST_ENTRIES_MUST_BE_SELECTED));
			} else {
				this.listManagerCopyToListDialog = new BaseSubWindow(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST_WINDOW_LABEL));
				this.listManagerCopyToListDialog.setDebugId("listManagerCopyToListDialog");
				this.listManagerCopyToListDialog.setModal(true);
				this.listManagerCopyToListDialog.setWidth("617px");
				this.listManagerCopyToListDialog.setHeight("230px");
				this.listManagerCopyToListDialog.setResizable(false);
				this.listManagerCopyToListDialog.addStyleName(Reindeer.WINDOW_LIGHT);

				try {
					this.listManagerCopyToListDialog.addComponent(new ListManagerCopyToListDialog(this.source.getWindow(),
							this.listManagerCopyToListDialog, this.currentlySavedGermplasmList.getName(),
							this.tableWithSelectAllLayout.getTable(), this.contextUtil.getCurrentWorkbenchUserId(), this.source));
					this.source.getWindow().addWindow(this.listManagerCopyToListDialog);
					this.listManagerCopyToListDialog.center();
				} catch (final MiddlewareQueryException e) {
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

	public void setBuildNewListTitle(final Label buildNewListTitle) {
		this.buildNewListTitle = buildNewListTitle;
	}

	public BreedingManagerListDetailsComponent getBreedingManagerListDetailsComponent() {
		return this.breedingManagerListDetailsComponent;
	}

	public TableWithSelectAllLayout getTableWithSelectAllLayout() {
		return this.tableWithSelectAllLayout;
	}

	public void setTableWithSelectAllLayout(final TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;
	}

	public Table getGermplasmsTable() {
		return this.tableWithSelectAllLayout.getTable();
	}

	public Button getSaveButton() {
		return this.saveButton;
	}

	public void setSaveButton(final Button saveButton) {
		this.saveButton = saveButton;
	}

	public GermplasmList getCurrentlySavedGermplasmList() {
		return this.currentlySavedGermplasmList;
	}

	@Override
	public void setCurrentlySavedGermplasmList(final GermplasmList list) {
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

	@Override
	public void updateListUI() {
		ListCommonActionsUtil.updateGermplasmListStatusUI(this.source);
	}

	public ListManagerMain getSource() {
		return this.source;
	}

	public AddColumnContextMenu getAddColumnContextMenu() {
		return this.addColumnContextMenu;
	}

	public void openSaveListAsDialog() {
		this.dialog = new SaveListAsDialog(this, this.currentlySavedGermplasmList, this.messageSource.getMessage(Message.EDIT_LIST_HEADER));
		this.dialog.setDebugId("dialog");
		this.getWindow().addWindow(this.dialog);
	}

	public SaveListAsDialog getSaveListAsDialog() {
		return this.dialog;
	}

	/**
	 * This method is called by the SaveListAsDialog window displayed when Edit Header button is clicked.
	 */
	@Override
	public void saveList(final GermplasmList list) {
		this.currentlySetGermplasmInfo = list;
		this.saveListButtonListener.doSaveAction();

		((BreedingManagerApplication) this.getApplication()).refreshListManagerTree();

		this.resetUnsavedChangesFlag();
		this.source.updateView(this.source.getModeView());
		this.saveListButtonListener.setForceHasChanges(false);
		this.enableMenuOptionsAfterSave();
	}

	public void saveList(final GermplasmList list, final Boolean showMessages) {
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
			final String message = "You have unsaved reservations for this list. " + "You will need to save them before changing views. "
					+ "Do you want to save your changes?";
			this.source.showUnsavedChangesConfirmDialog(message, ModeView.LIST_VIEW);
		}
	}

	void changeToListView() {
		final String inventoryLabel = this.messageSource.getMessage(Message.INVENTORY);
		if (inventoryLabel.equals(this.topLabel.getValue().toString())) {
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

	void changeToInventoryView() {
		final String listEntriesLabel = this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL);
		if (listEntriesLabel.equals(this.topLabel.getValue().toString())) {
			this.tableWithSelectAllLayout.setVisible(false);
			this.listInventoryTable.setVisible(true);
			this.toolsButtonContainer.removeComponent(this.toolsButton);
			this.toolsButtonContainer.addComponent(this.inventoryViewToolsButton, "top:0; right:0;");

			this.topLabel.setValue(this.messageSource.getMessage(Message.INVENTORY));
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

	protected void reserveInventoryAction() {
		// checks if the screen is in the inventory view
		if (!this.inventoryViewMenu.isVisible()) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.ERROR_RESERVE_INVENTORY_CHANGE_TO_INVENTORY_VIEW));
		} else {
			if (this.listInventoryTable.getInventoryTableDropHandler().isChanged()) {
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.WARNING),
						this.messageSource.getMessage(Message.ERROR_SAVE_LIST_BEFORE_RESERVING_INVENTORY));
			} else {
				final List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

				if (lotDetailsGid == null || lotDetailsGid.isEmpty()) {
					MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
							this.messageSource.getMessage(Message.ERROR_RESERVE_INVENTORY_IF_NO_LOT_IS_SELECTED));
				} else {
					// this util handles the inventory reservation related
					// functions
					this.reserveInventoryUtil = new ReserveInventoryUtil(this, lotDetailsGid);
					if (ReserveInventoryUtil.isLotsContainsScale(lotDetailsGid)) {
						this.reserveInventoryUtil.viewReserveInventoryWindow();
					} else {
						MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.RESERVATION_STATUS),
								this.messageSource.getMessage(
										Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES) + ".");
					}
				}
			}
		}
	}

	public boolean saveListAction() {

		if (this.validReservationsToSave.size() > 0) {
			// If there are unsaved reservations,list will not be saved.
			return false;
		} else {
			if (this.hasUnsavedChanges()) {

				final List<Integer> alreadyAddedEntryIds = new ArrayList<>();
				final List<ListDataAndLotDetails> listDataAndLotDetails =
						this.listInventoryTable.getInventoryTableDropHandler().getListDataAndLotDetails();
				for (final ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails) {
					if (!alreadyAddedEntryIds.contains(listDataAndLotDetail.getEntryId())) {
						this.dropHandler.addGermplasmFromList(listDataAndLotDetail.getListId(), listDataAndLotDetail.getSourceLrecId());
						alreadyAddedEntryIds.add(listDataAndLotDetail.getEntryId());
					}
				}

				this.saveList(this.currentlySavedGermplasmList, false);

				for (final ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails) {
					this.listInventoryTable.getInventoryTableDropHandler()
							.assignLrecIdToRowsFromListWithEntryId(listDataAndLotDetail.getListId(), listDataAndLotDetail.getEntryId());
				}

				this.listInventoryTable.getInventoryTableDropHandler().resetListDataAndLotDetails();
			}
			return true;
		}

	}

	public void saveReservationsAction() {
		if (this.hasUnsavedChanges()) {

			final boolean success = this.reserveInventoryAction.saveReserveTransactions(this.getValidReservationsToSave(),
					this.currentlySavedGermplasmList.getId());
			if (success) {
				this.cancelReservations();
				this.resetListDataTableValues();
				this.resetListInventoryTableValues();
				MessageNotifier.showMessage(this.source.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
						this.messageSource.getMessage(Message.SAVE_RESERVED_AND_CANCELLED_RESERVATION));
			} else {
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
						this.messageSource.getMessage(Message.INVENTORY_NOT_AVAILABLE_BALANCE));
			}

		}
	}

	public void cancelReservationsAction() {

		if (this.listInventoryTable.getInventoryTableDropHandler().isChanged()) {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.ERROR_SAVE_LIST_BEFORE_CANCELLING_RESERVATION));
		} else {
			final List<ListEntryLotDetails> lotDetailsGid = this.listInventoryTable.getSelectedLots();

			if (lotDetailsGid == null || lotDetailsGid.isEmpty()) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						this.messageSource.getMessage(Message.WARNING_CANCEL_RESERVATION_IF_NO_LOT_IS_SELECTED));
			} else {
				if (!this.listInventoryTable.isSelectedEntriesHasReservation(lotDetailsGid, this.getValidReservationsToSave())) {
					MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
							this.messageSource.getMessage(Message.WARNING_IF_THERE_IS_NO_RESERVATION_FOR_SELECTED_LOT));
				} else {
					ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.CANCEL_RESERVATIONS),
							this.messageSource.getMessage(Message.CONFIRM_CANCEL_RESERVATION), this.messageSource.getMessage(Message.YES),
							this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

								private static final long serialVersionUID = 1L;

								@Override
								public void onClose(final ConfirmDialog dialog) {
									if (dialog.isConfirmed()) {
										ListBuilderComponent.this.userSelectedLotEntriesToCancelReservations();
									}
								}
							});
				}
			}
		}
	}

	public void userSelectedLotEntriesToCancelReservations() {

		final List<ListEntryLotDetails> userSelectedLotEntriesToCancel = this.listInventoryTable.getSelectedLots();

		final Iterator<ListEntryLotDetails> userSelectedLotEntriesToCancelIterator = userSelectedLotEntriesToCancel.iterator();

		final int validReservation = this.validReservationsToSave.size();

		// this will keep track of how many reservations needs to be cancelled
		// and how many reservations needs to be undo
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
		// validReservationsToCancel holds the actual lot entries that needs to
		// be canceled which is already there in database
		this.persistedReservationToCancel = userSelectedLotEntriesToCancel;

		// enables the save reservation option if there is actual lot that needs
		// to be cancel which is already there in database
		if (!this.persistedReservationToCancel.isEmpty()) {
			this.setMenuInventorySaveChanges();
			this.setHasUnsavedChanges(true);
		}

		if (validReservation != this.validReservationsToSave.size()) {
			this.listInventoryTable.resetRowsForCancelledReservation(this.listInventoryTable.getSelectedLots(),
					this.currentlySavedGermplasmList.getId());
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.UNSAVED_RESERVARTION_CANCELLED));
		}

	}

	public void cancelReservations() {

		if (this.persistedReservationToCancel != null && !this.persistedReservationToCancel.isEmpty()) {
			this.reserveInventoryAction.cancelReservations(this.persistedReservationToCancel);
			// reset the reservation to cancel.
			this.persistedReservationToCancel.clear();
		}
	}

	public void resetListDataTableValues() {

		this.listDataTable.setEditable(false);
		this.loadEntriesToListDataTable();
		this.listDataTable.refreshRowCache();
		this.listDataTable.setImmediate(true);
		this.listDataTable.requestRepaint();

	}

	public void loadEntriesToListDataTable() {
		try {
			this.listEntriesCount = this.germplasmListManager.countGermplasmListDataByListId(this.currentlySavedGermplasmList.getId());
		} catch (final MiddlewareQueryException ex) {
			ListBuilderComponent.LOG
					.error("Error with retrieving count of list entries for list: " + this.currentlySavedGermplasmList.getId(), ex);
			this.listEntriesCount = 0;
		}

		if (this.listEntriesCount > 0) {
			final List<GermplasmListData> listEntries = new ArrayList<>();
			this.getAllListEntries(listEntries);

			for (final GermplasmListData entry : listEntries) {
				this.addListEntryToTable(entry);
			}
		}
	}

	private void getAllListEntries(final List<GermplasmListData> listEntries) {
		final List<GermplasmListData> entries;
		try {
			entries = this.inventoryDataManager.getLotCountsForList(this.currentlySavedGermplasmList.getId(), 0,
					Long.valueOf(this.listEntriesCount).intValue());
			listEntries.addAll(entries);
		} catch (final MiddlewareQueryException ex) {
			ListBuilderComponent.LOG.error("Error with retrieving list entries for list: " + this.currentlySavedGermplasmList.getId(), ex);
			throw ex;
		}
	}

	void addListEntryToTable(final GermplasmListData listData) {
		final Item item = this.listDataTable.getItem(listData.getId());

		// #1 Available Inventory

		// default value
		String availInv = "-";
		if (listData.getInventoryInfo().getLotCount().intValue() != 0) {
			availInv = listData.getInventoryInfo().getLotCount().toString().trim();
		}

		final Button lotButton = ListCommonActionsUtil.getLotCountButton(listData.getInventoryInfo().getLotCount().intValue(),
				listData.getGid(), listData.getDesignation(), this.source, null);
		item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(lotButton);

		// LOTS
		final String available = listData.getInventoryInfo().getAvailable();

		final Button availableButton = new SortableButton(available.toString(), new InventoryLinkButtonClickListener(this.source,
				this.currentlySavedGermplasmList.getId(), listData.getId(), listData.getGid()));

		availableButton.setStyleName(BaseTheme.BUTTON_LINK);
		availableButton.setDescription(ListBuilderComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
		item.getItemProperty(ColumnLabels.TOTAL.getName()).setValue(availableButton);

		final Button gidButton = (SortableButton) item.getItemProperty(ColumnLabels.GID.getName()).getValue();
		String gidString = "";

		if (gidButton != null) {
			gidString = gidButton.getCaption();
		}

		this.updateAvailInvValues(Integer.valueOf(gidString), availInv);

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

		this.setMenuInventorySaveChanges();

		// update lot reservatios to save
		this.updateLotReservationsToSave(validReservations);

		// if there are no valid reservations
		if (validReservations.isEmpty()) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.RESERVATION_STATUS),
					this.messageSource.getMessage(Message.COULD_NOT_MAKE_ANY_RESERVATION_ALL_SELECTED_LOTS_HAS_INSUFFICIENT_BALANCES)
							+ ".");

		} else if (!withInvalidReservations) {
			MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					"All selected entries will be reserved in their respective lots.", 3000);
		}
	}

	@Override
	public void addReserveInventoryWindow(final ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().addWindow(this.reserveInventory);
	}

	@Override
	public void addReservationStatusWindow(final ReservationStatusWindow reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.removeReserveInventoryWindow(this.reserveInventory);
		this.source.getWindow().addWindow(this.reservationStatus);
	}

	@Override
	public void removeReserveInventoryWindow(final ReserveInventoryWindow reserveInventory) {
		this.reserveInventory = reserveInventory;
		this.source.getWindow().removeWindow(this.reserveInventory);
	}

	@Override
	public void removeReservationStatusWindow(final ReservationStatusWindow reservationStatus) {
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
	public Component getParentComponent() {
		return this.source;
	}

	@Override
	public void setHasUnsavedChangesMain(final boolean hasChanges) {
		this.source.setHasUnsavedChangesMain(hasChanges);
	}

	@Override
	public void setHasUnsavedChanges(final Boolean hasChanges) {
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

	private void updateAvailInvValues(final Integer gid, final String availInv) {
		final List<Integer> itemIds = this.getItemIds(this.listDataTable);
		for (final Integer itemId : itemIds) {
			final Item item = this.listDataTable.getItem(itemId);
			final Button gidButton = (SortableButton) item.getItemProperty(ColumnLabels.GID.getName()).getValue();

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
			return this.currentlySavedGermplasmList.getUserId().equals(this.contextUtil.getCurrentWorkbenchUserId());
		} catch (final MiddlewareQueryException e) {
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

	protected String getTermNameFromOntology(final ColumnLabels columnLabel) {
		return columnLabel.getTermNameFromOntology(this.ontologyDataManager);
	}

	protected void setUserService(final UserService userService) {
		this.userService = userService;
	}

	/*
	 * For test purposes
	 */
	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	void setSource(final ListManagerMain source) {
		this.source = source;
	}

	void setListDataTable(final Table listDataTable) {
		this.listDataTable = listDataTable;
	}

	void setTotalListEntriesLabel(final Label totalListEntriesLabel) {
		this.totalListEntriesLabel = totalListEntriesLabel;
	}

	void setTotalSelectedListEntriesLabel(final Label totalSelectedListEntriesLabel) {
		this.totalSelectedListEntriesLabel = totalSelectedListEntriesLabel;
	}

	public void setMenuInventorySaveChanges() {
		this.menuSaveReserveInventory.setEnabled(true);
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setBreedingManagerListDetailsComponent(final BreedingManagerListDetailsComponent breedingManagerListDetailsComponent) {
		this.breedingManagerListDetailsComponent = breedingManagerListDetailsComponent;
	}

	public void setEditHeaderButton(final Button editHeaderButton) {
		this.editHeaderButton = editHeaderButton;
	}

	public void setViewHeaderButton(final Button viewHeaderButton) {
		this.viewHeaderButton = viewHeaderButton;
	}

	public void setLockButton(final Button lockButton) {
		this.lockButton = lockButton;
	}

	public void setDropHandler(final BuildNewListDropHandler dropHandler) {
		this.dropHandler = dropHandler;
	}

	public void setUnlockButton(final Button unlockButton) {
		this.unlockButton = unlockButton;
	}

	public void setFillWith(final FillWith fillWith) {
		this.fillWith = fillWith;
	}

	public void setMenuDeleteSelectedEntries(final ContextMenuItem menuDeleteSelectedEntries) {
		this.menuDeleteSelectedEntries = menuDeleteSelectedEntries;
	}

	public void setAddColumnContextMenu(final AddColumnContextMenu addColumnContextMenu) {
		this.addColumnContextMenu = addColumnContextMenu;
	}

	public void setMenuCopyToListFromInventory(final ContextMenuItem menuCopyToListFromInventory) {
		this.menuCopyToListFromInventory = menuCopyToListFromInventory;
	}

	public void setMenuReserveInventory(final ContextMenuItem menuReserveInventory) {
		this.menuReserveInventory = menuReserveInventory;
	}

	public void setListInventoryTable(final ListManagerInventoryTable listInventoryTable) {
		this.listInventoryTable = listInventoryTable;
	}

	public void setMenuSaveReserveInventory(final ContextMenuItem menuSaveReserveInventory) {
		this.menuSaveReserveInventory = menuSaveReserveInventory;
	}

	public void setReserveInventoryAction(final ReserveInventoryAction reserveInventoryAction) {
		this.reserveInventoryAction = reserveInventoryAction;
	}

	public void setValidReservationsToSave(final Map<ListEntryLotDetails, Double> validReservationsToSave) {
		this.validReservationsToSave = validReservationsToSave;
	}

	public ReserveInventoryAction getReserveInventoryAction() {
		return this.reserveInventoryAction;
	}

	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public Table getListDataTable() {
		return this.listDataTable;
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setMenuExportList(final ContextMenuItem menuExportList) {
		this.menuExportList = menuExportList;
	}

	public void setMenuCopyToList(final ContextMenuItem menuCopyToList) {
		this.menuCopyToList = menuCopyToList;
	}

	public void setMenuCancelReservation(final ContextMenuItem menuCancelReservation) {
		this.menuCancelReservation = menuCancelReservation;
	}

	public List<ListEntryLotDetails> getPersistedReservationToCancel() {
		return this.persistedReservationToCancel;
	}

	public ContextMenu getInventoryViewMenu() {
		return this.inventoryViewMenu;
	}

	public void setInventoryViewMenu(final ContextMenu inventoryViewMenu) {
		this.inventoryViewMenu = inventoryViewMenu;
	}

	public void setContextMenu(final ContextMenu menu) {
		this.menu = menu;
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

	public Boolean listHasAddedColumns() {
		return this.addColumnContextMenu.hasAddedColumn(this.listDataTable, this.attributeAndNameTypeColumns);
	}

	void setTopLabel(final Label topLabel) {
		this.topLabel = topLabel;
	}

	void setToolsButtonContainer(final AbsoluteLayout toolsButtonContainer) {
		this.toolsButtonContainer = toolsButtonContainer;
	}

}

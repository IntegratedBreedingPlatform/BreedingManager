
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.action.SaveGermplasmListAction;
import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.listeners.ParentsTableCheckboxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUtil;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.breeding.manager.inventory.InventoryDropTargetContainer;
import org.generationcp.breeding.manager.inventory.ListDataAndLotDetails;
import org.generationcp.breeding.manager.inventory.ReservationStatusWindow;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryUtil;
import org.generationcp.breeding.manager.inventory.ReserveInventoryWindow;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.util.InventoryTableDropHandler;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ParentTabComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout,
		SaveGermplasmListActionSource, SaveListAsDialogSource, ReserveInventorySource, InventoryDropTargetContainer {

	@Autowired
	private OntologyDataManager ontologyDataManager;
	
	@Autowired
	private PlatformTransactionManager transactionManager;

	private final class ListDataTableDropHandler implements DropHandler {

		private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";
		private static final long serialVersionUID = -3048433522366977000L;

		@Override
		@SuppressWarnings("unchecked")
		public void drop(DragAndDropEvent dropEvent) {

			// Dragged from a table
			if (dropEvent.getTransferable() instanceof TableTransferable) {

				TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();

				Table sourceTable = transferable.getSourceComponent();
				Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();

				AbstractSelectTargetDetails dropData = (AbstractSelectTargetDetails) dropEvent.getTargetDetails();
				Object targetItemId = dropData.getItemIdOver();

				if (sourceTable.equals(ParentTabComponent.this.listDataTable)) {
					// Check first if item is dropped on top of itself
					if (!transferable.getItemId().equals(targetItemId)) {

						Item oldItem = sourceTable.getItem(transferable.getItemId());
						Object oldCheckBox = oldItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).getValue();
						Object oldEntryCode = oldItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
						Object oldDesignation = oldItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
						Object oldAvailInv = oldItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
						Object oldSeedRes = oldItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue();
						Object oldStockId = oldItem.getItemProperty(ColumnLabels.STOCKID.getName()).getValue();

						sourceTable.removeItem(transferable.getItemId());

						Item newItem = targetTable.addItemAfter(targetItemId, transferable.getItemId());
						newItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(oldCheckBox);
						newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(oldEntryCode);
						newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(oldDesignation);
						newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(oldAvailInv);
						newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(oldSeedRes);
						newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(oldStockId);

						ParentTabComponent.this.saveActionMenu.setEnabled(true);
						ParentTabComponent.this.setHasUnsavedChanges(true);

						// Checker if list is modified and list is central, force new list to be saved
						if (ParentTabComponent.this.germplasmList != null && ParentTabComponent.this.germplasmList.getId() > 0) {
							ParentTabComponent.this.isTreatAsNewList = true;
						}

					}
				} else if (sourceTable.getData().equals(SelectParentsListDataComponent.LIST_DATA_TABLE_ID)) {
					ParentTabComponent.this.source.dropToFemaleOrMaleTable(sourceTable, ParentTabComponent.this.listDataTable,
							(Integer) transferable.getItemId());
				}

				// Dragged from the tree
			} else {
				Transferable transferable = dropEvent.getTransferable();
				Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();

				try {
					GermplasmList draggedListFromTree =
							ParentTabComponent.this.germplasmListManager.getGermplasmListById((Integer) transferable.getData("itemId"));
					if (draggedListFromTree != null) {
						List<GermplasmListData> germplasmListDataFromListFromTree = draggedListFromTree.getListData();

						Integer addedCount = 0;

						for (GermplasmListData listData : germplasmListDataFromListFromTree) {
							if (listData.getStatus() != 9) {
								String parentValue = listData.getDesignation();

								Button gidButton = new Button(parentValue, new GidLinkClickListener(listData.getGid().toString(), true));
								gidButton.setStyleName(BaseTheme.BUTTON_LINK);
								gidButton.setDescription(ListDataTableDropHandler.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

								CheckBox tag = new CheckBox();

								GermplasmListEntry entryObject =
										new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(),
												listData.getDesignation(), draggedListFromTree.getName() + ":" + listData.getEntryId());

								if (targetTable.equals(ParentTabComponent.this.listDataTable)) {
									tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, ParentTabComponent.this
											.getSelectAllCheckBox()));
									ParentTabComponent.this.listNameForCrosses = draggedListFromTree.getName();
									ParentTabComponent.this.updateCrossesSeedSource(draggedListFromTree);
								}

								tag.setImmediate(true);

								// if the item is already existing in the target table, remove the existing item then add a new entry
								targetTable.removeItem(entryObject);

								Item item = targetTable.getContainerDataSource().addItem(entryObject);

								item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(gidButton);
								item.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(tag);

								addedCount++;
							}
						}

						// After adding, check if the # of items added on the table, is equal to the number of list data of the dragged
						// list, this will enable/disable the save option
						List<Object> itemsAfterAdding = new ArrayList<Object>();
						itemsAfterAdding.addAll(targetTable.getItemIds());

						if (addedCount == itemsAfterAdding.size()) {
							ParentTabComponent.this.saveActionMenu.setEnabled(false);
							ParentTabComponent.this.setHasUnsavedChanges(false);

							// updates the crossesMade save button if both parents are save at least once
							ParentTabComponent.this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();

						} else {
							ParentTabComponent.this.saveActionMenu.setEnabled(true);
							ParentTabComponent.this.setHasUnsavedChanges(true);
							ParentTabComponent.this.isTreatAsNewList = true;
						}
					}
				} catch (MiddlewareQueryException e) {
					ParentTabComponent.LOG.error("Error in getting list by GID", e);
				}
			}
			ParentTabComponent.this.assignEntryNumber(ParentTabComponent.this.listDataTable);
			ParentTabComponent.this.updateNoOfEntries(ParentTabComponent.this.listDataTable.size());
		}

		@Override
		public AcceptCriterion getAcceptCriterion() {
			return AcceptAll.get();
		}
	}

	private final class InventoryViewActionMenClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// Get reference to clicked item
					ContextMenuItem clickedItem = event.getClickedItem();
					if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SAVE_CHANGES))) {
						ParentTabComponent.this.doSaveAction();
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW))) {
						ParentTabComponent.this.viewListAction();
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.COPY_TO_NEW_LIST))) {
						// no implementation yet for this condition
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.RESERVE_INVENTORY))) {
						ParentTabComponent.this.reserveInventoryAction();
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						ParentTabComponent.this.listInventoryTable.getTable().setValue(
								ParentTabComponent.this.listInventoryTable.getTable().getItemIds());
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES))) {
						ParentTabComponent.this.listInventoryTable.getTable().setValue(
								CrossingManagerUtil.getEvenEntries(ParentTabComponent.this.listInventoryTable.getTable()));
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES))) {
						ParentTabComponent.this.listInventoryTable.getTable().setValue(
								CrossingManagerUtil.getOddEntries(ParentTabComponent.this.listInventoryTable.getTable()));
					}
				}
			});

		}
	}

	private final class ActionMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					ContextMenuItem clickedItem = event.getClickedItem();
					if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.INVENTORY_VIEW))) {
						ParentTabComponent.this.viewInventoryAction();
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES))) {
						ParentTabComponent.this.parentActionListener.removeSelectedEntriesAction(ParentTabComponent.this.listDataTable);
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SAVE_LIST))) {
						ParentTabComponent.this.doSaveAction();
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						ParentTabComponent.this.listDataTable.setValue(ParentTabComponent.this.listDataTable.getItemIds());
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES))) {
						ParentTabComponent.this.listDataTable.setValue(CrossingManagerUtil.getEvenEntries(ParentTabComponent.this.listDataTable));
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES))) {
						ParentTabComponent.this.listDataTable.setValue(CrossingManagerUtil.getOddEntries(ParentTabComponent.this.listDataTable));
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.CLEAR_ALL))) {
						ParentTabComponent.this.listDataTable.setValue(ParentTabComponent.this.listDataTable);
						ParentTabComponent.this.parentActionListener.removeSelectedEntriesAction(ParentTabComponent.this.listDataTable);
					}
				}
			});
		}
	}

	private static final String MALE_PARENTS = "Male Parents";
	private static final String FEMALE_PARENTS = "Female Parents";
	private static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";
	private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";
	private static final Logger LOG = LoggerFactory.getLogger(ParentTabComponent.class);
	private static final long serialVersionUID = 2124522470629189449L;

	private Button editHeaderButton;
	private Label listEntriesLabel;
	private Label totalListEntriesLabel;
	private Label totalSelectedListEntriesLabel;

	private Button actionButton;
	private Button inventoryViewActionButton;

	// Layout Variables
	private HorizontalLayout subHeaderLayout;
	private HorizontalLayout headerLayout;

	// Tables
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private Table listDataTable;
	@SuppressWarnings("unused")
	private CheckBox selectAll;

	private CrossingManagerInventoryTable listInventoryTable;

	// Actions
	private ContextMenu actionMenu;
	private ContextMenuItem saveActionMenu;

	private ContextMenu inventoryViewActionMenu;
	private ContextMenuItem menuCopyToNewListFromInventory;
	private ContextMenuItem menuInventorySaveChanges;
	@SuppressWarnings("unused")
	private ContextMenuItem menuListView;
	private ContextMenuItem menuReserveInventory;

	private static final String NO_LOT_FOR_THIS_GERMPLASM = "No Lot for this Germplasm";
	private static final String STRING_DASH = "-";
	private static final String TAG_COLUMN_ID = "Tag";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	protected PedigreeService pedigreeService;

	@Resource
	protected CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	private GermplasmList germplasmList;
	private final String parentLabel;
	private Integer rowCount;
	private List<GermplasmListData> listEntries;
	private long listEntriesCount;

	private final CrossingManagerMakeCrossesComponent makeCrossesMain;
	private final MakeCrossesParentsComponent source;
	private CrossingManagerActionHandler parentActionListener;
	private String listNameForCrosses;

	private SaveListAsDialog saveListAsWindow;

	private boolean hasChanges = false;

	// if a germplasm list should be treated as new during saving
	private boolean isTreatAsNewList = false;

	// Inventory Related Variables
	private ReserveInventoryWindow reserveInventory;
	private ReservationStatusWindow reservationStatus;
	private ReserveInventoryUtil reserveInventoryUtil;
	private ReserveInventoryAction reserveInventoryAction;
	private Map<ListEntryLotDetails, Double> validReservationsToSave;
	private ModeView prevModeView;

	private InventoryTableDropHandler inventoryTableDropHandler;

	public ParentTabComponent(CrossingManagerMakeCrossesComponent makeCrossesMain, MakeCrossesParentsComponent source, String parentLabel,
			Integer rowCount) {
		super();
		this.makeCrossesMain = makeCrossesMain;
		this.source = source;
		this.parentLabel = parentLabel;
		this.rowCount = rowCount;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.initializeMainComponents();
		this.resetInventoryMenuOptions();

		this.initializeParentTable(new TableWithSelectAllLayout(this.rowCount, ParentTabComponent.TAG_COLUMN_ID));
		this.initializeListInventoryTable(new CrossingManagerInventoryTable(this.germplasmList != null ? this.germplasmList.getId() : null));

		// Inventory Related Variables
		this.validReservationsToSave = new HashMap<ListEntryLotDetails, Double>();
	}

	/**
	 * Exposed for usage in tests
	 */
	protected void initializeMainComponents() {
		this.listEntriesLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.listEntriesLabel.setWidth("160px");

		this.editHeaderButton = new Button(this.messageSource.getMessage(Message.EDIT_HEADER));
		this.editHeaderButton.setImmediate(true);
		this.editHeaderButton.setStyleName(BaseTheme.BUTTON_LINK);
		this.editHeaderButton.setVisible(false);

		this.totalListEntriesLabel =
				new Label(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + "  <b>0</b>", Label.CONTENT_XHTML);
		this.totalListEntriesLabel.setWidth("120px");

		this.totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedListEntriesLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.actionButton = new ActionButton();

		this.actionMenu = new ContextMenu();
		this.actionMenu.setWidth("250px");
		this.actionMenu.addItem(this.messageSource.getMessage(Message.CLEAR_ALL));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.INVENTORY_VIEW));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES));
		this.saveActionMenu = this.actionMenu.addItem(this.messageSource.getMessage(Message.SAVE_LIST));
		this.saveActionMenu.setEnabled(false);
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES));

		this.inventoryViewActionButton = new ActionButton();

		this.inventoryViewActionMenu = new ContextMenu();
		this.inventoryViewActionMenu.setWidth("295px");
		this.menuCopyToNewListFromInventory = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST));
		this.menuReserveInventory = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.menuListView = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
		this.menuInventorySaveChanges = this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SAVE_CHANGES));
		this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES));
		this.inventoryViewActionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES));
	}

	private void resetInventoryMenuOptions() {
		// disable the save button at first since there are no reservations yet
		this.menuInventorySaveChanges.setEnabled(false);

		// Temporarily disable to Copy to New List in InventoryView
		this.menuCopyToNewListFromInventory.setEnabled(false);

		// disable the reserve inventory at first if the list is not yet saved.
		if (this.germplasmList == null) {
			this.menuReserveInventory.setEnabled(false);
		}
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	protected void initializeParentTable(final TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;

		this.listDataTable = this.tableWithSelectAllLayout.getTable();
		this.selectAll = this.tableWithSelectAllLayout.getCheckBox();

		this.listDataTable.setWidth("100%");
		this.listDataTable.setNullSelectionAllowed(true);
		this.listDataTable.setSelectable(true);
		this.listDataTable.setMultiSelect(true);
		this.listDataTable.setImmediate(true);
		this.listDataTable.addContainerProperty(ParentTabComponent.TAG_COLUMN_ID, CheckBox.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, Integer.valueOf(0));
		this.listDataTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);

		this.listDataTable.setColumnHeader(ParentTabComponent.TAG_COLUMN_ID, this.messageSource.getMessage(Message.CHECK_ICON));
		this.listDataTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		this.listDataTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		this.listDataTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(),
				this.getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY));
		this.listDataTable.setColumnHeader(ColumnLabels.SEED_RESERVATION.getName(),
				this.getTermNameFromOntology(ColumnLabels.SEED_RESERVATION));
		this.listDataTable.setColumnHeader(ColumnLabels.STOCKID.getName(), this.getTermNameFromOntology(ColumnLabels.STOCKID));

		this.listDataTable.setColumnWidth(ParentTabComponent.TAG_COLUMN_ID, 25);
		this.listDataTable.setDragMode(TableDragMode.ROW);
		this.listDataTable.setItemDescriptionGenerator(new ItemDescriptionGenerator() {

			private static final long serialVersionUID = -3207714818504151649L;

			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if (propertyId != null && propertyId == ColumnLabels.DESIGNATION.getName()) {
					Table theTable = (Table) source;
					Item item = theTable.getItem(itemId);
					return (String) item.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
				}
				return null;
			}
		});
	}

	public TableWithSelectAllLayout getTableWithSelectAllLayout() {
		return this.tableWithSelectAllLayout;
	}

	public void setTableWithSelectAllLayout(final TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;
	}

	protected void initializeListInventoryTable(final CrossingManagerInventoryTable listInventoryTable) {
		this.listInventoryTable = listInventoryTable;
		this.listInventoryTable.setVisible(false);
		this.listInventoryTable.setMaxRows(this.rowCount);
		this.listInventoryTable.setTableHeight(null);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		this.setupDropHandler();

		this.parentActionListener = new CrossingManagerActionHandler(this.source);
		this.listDataTable.addActionHandler(this.parentActionListener);

		this.actionButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				ParentTabComponent.this.actionMenu.show(event.getClientX(), event.getClientY());
			}

		});

		this.actionMenu.addListener(new ActionMenuClickListener());

		this.inventoryViewActionButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 272707576878821700L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ParentTabComponent.this.inventoryViewActionMenu.show(event.getClientX(), event.getClientY());
			}
		});

		this.inventoryViewActionMenu.addListener(new InventoryViewActionMenClickListener());

		this.editHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -6306973449416812850L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ParentTabComponent.this.openSaveListAsDialog();
			}
		});

		this.tableWithSelectAllLayout.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ParentTabComponent.this.updateNoOfSelectedEntries();
			}
		});

		this.listInventoryTable.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ParentTabComponent.this.updateNoOfSelectedEntries();
			}
		});
	}

	public void doSaveAction() {
		this.doSave(this.source.getMakeCrossesMain().getModeView());
	}

	public void doSaveActionFromMain() {
		doSave(this.prevModeView);
		if (this.isOnlyReservationsMade(this.prevModeView)){
			this.makeCrossesMain.updateView(this.makeCrossesMain.getModeView());
		}
	}

	private void doSave(ModeView modeView) {
		// do nothing if there were no unsaved changes
		if (!this.hasUnsavedChanges()) {
			return;
		}

		if (modeView.equals(ModeView.LIST_VIEW)) {
			if (this.germplasmList == null || this.isTreatAsNewList) {
				this.openSaveListAsDialog();
			} else {
				this.saveList(this.germplasmList);
			}
		} else {
			// Inventory view
			if (this.germplasmList == null || this.isTreatAsNewList) {
				// new list in inventory view
				this.openSaveListAsDialog();
			} else if (this.inventoryTableDropHandler.hasChanges()) {
				this.saveList(this.germplasmList);
			}
		}

		if (this.isOnlyReservationsMade(modeView)) {
			this.saveReservationChangesAction(true);
		}
	}

	private boolean isOnlyReservationsMade(ModeView modeView){
		return this.hasUnsavedChanges() && modeView.equals(ModeView.INVENTORY_VIEW) && this.germplasmList != null &&
				!this.inventoryTableDropHandler.hasChanges();
	}

	private void updateListDataTableBeforeSaving() {
		List<Integer> alreadyAddedEntryIds = new ArrayList<Integer>();
		List<ListDataAndLotDetails> listDataAndLotDetails = this.inventoryTableDropHandler.getListDataAndLotDetails();

		for (ListDataAndLotDetails listDataAndLotDetail : listDataAndLotDetails) {

			if (!alreadyAddedEntryIds.contains(listDataAndLotDetail.getEntryId())) {
				try {

					GermplasmListData germplasmListData =
							this.germplasmListManager.getGermplasmListDataByListIdAndLrecId(listDataAndLotDetail.getListId(),
									listDataAndLotDetail.getSourceLrecId());

					if (germplasmListData != null) {

						Integer entryId = this.getListDataTableNextEntryId();
						GermplasmListEntry entryObject =
								new GermplasmListEntry(germplasmListData.getId(), germplasmListData.getGid(),
										this.listDataTable.size() + 1, germplasmListData.getDesignation(),
										germplasmListData.getSeedSource());

						Item newItem = this.listDataTable.getContainerDataSource().addItem(entryObject);

						if (newItem != null) {
							CheckBox tag = new CheckBox();
							tag.addListener(new ParentsTableCheckboxListener(this.listDataTable, entryObject, this.getSelectAllCheckBox()));
							tag.setImmediate(true);

							newItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(tag);

							newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entryId);

							Button desigButton =
									new Button(germplasmListData.getDesignation(), new GidLinkClickListener(germplasmListData.getGid()
											.toString(), true));
							desigButton.setStyleName(BaseTheme.BUTTON_LINK);
							desigButton.setDescription(ParentTabComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);
							newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
						}
					}

				} catch (MiddlewareQueryException e) {
					ParentTabComponent.LOG.error(e.getMessage(), e);
				}

				alreadyAddedEntryIds.add(listDataAndLotDetail.getEntryId());
			}
		}
	}

	protected void openSaveListAsDialog() {
		this.saveListAsWindow = new SaveListAsDialog(this, this.germplasmList);
		this.saveListAsWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		this.saveListAsWindow.setData(this);
		this.getWindow().addWindow(this.saveListAsWindow);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveList(GermplasmList list) {
		// update the listDataTable when the user tries to change list view but has unsaved changes in inventory view
		if (this.prevModeView != null) {
			if (this.prevModeView.equals(ModeView.INVENTORY_VIEW)) {
				this.updateListDataTableBeforeSaving();
			}
		} else {
			// update the listdatatable in inventory view w/o changing mode
			if (this.source.getMakeCrossesMain().getModeView().equals(ModeView.INVENTORY_VIEW)) {
				this.updateListDataTableBeforeSaving();
			}
		}

		List<GermplasmListEntry> currentListEntries = new ArrayList<GermplasmListEntry>();
		currentListEntries.addAll((Collection<GermplasmListEntry>) this.listDataTable.getItemIds());

		// Please correct the entryID, get from the parent table
		// Create Map <Key: "GID+ENTRYID">, <Value:CheckBox Obj>
		SaveGermplasmListAction saveListAction = new SaveGermplasmListAction(this, list, currentListEntries);
		try {
			this.germplasmList = saveListAction.saveRecords();
			this.updateCrossesSeedSource(this.germplasmList);
			this.source.updateUIForSuccessfulSaving(this, this.germplasmList);

			if (this.source.getMakeCrossesMain().getModeView().equals(ModeView.INVENTORY_VIEW) && !this.validReservationsToSave.isEmpty()) {

				this.saveReservationChangesAction(false);
				this.inventoryTableDropHandler.resetListDataAndLotDetails();

			}

			//<------- Reset Markers after save is complete ----------->
			this.setHasUnsavedChanges(false);
			this.isTreatAsNewList = false;

			if (this.prevModeView != null) {
				this.source.getMakeCrossesMain().updateView(this.source.getMakeCrossesMain().getModeView());

				// reset the marker
				this.prevModeView = null;
			}

			// Reserve Inventory Action will now be available after saving the list for the first time
			this.menuReserveInventory.setEnabled(true);

			// Edit Header Section will also be visible to the user
			this.editHeaderButton.setVisible(true);

			// show success message for saving
			MessageNotifier.showMessage(this.source.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(this.getSuccessMessage()), 3000);
		} catch (MiddlewareQueryException e) {
			ParentTabComponent.LOG.error(e.getMessage(), e);
		}
	}

	private void setupDropHandler() {
		this.listDataTable.setDropHandler(new ListDataTableDropHandler());

		this.inventoryTableDropHandler =
				new InventoryTableDropHandler(this, this.germplasmDataManager, this.germplasmListManager, this.inventoryDataManager,
						this.pedigreeService, this.crossExpansionProperties, this.listInventoryTable.getTable());
		this.listInventoryTable.getTable().setDropHandler(this.inventoryTableDropHandler);
	}

	public void updateNoOfEntries(int count) {
		String noOfEntries = "  <b>" + count + "</b>";
		if (this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW)) {
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + noOfEntries);
		} else {
			// Inventory View
			this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LOTS) + ": " + noOfEntries);
		}
	}

	public void updateNoOfEntries() {
		int count = 0;
		if (this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW)) {
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

		if (this.source.getMakeCrossesMain().getModeView().equals(ModeView.LIST_VIEW)) {
			Collection<?> selectedItems = (Collection<?>) this.tableWithSelectAllLayout.getTable().getValue();
			count = selectedItems.size();
		} else {
			Collection<?> selectedItems = (Collection<?>) this.listInventoryTable.getTable().getValue();
			count = selectedItems.size();
		}

		this.updateNoOfSelectedEntries(count);
	}

	@SuppressWarnings("unchecked")
	public void assignEntryNumber(Table parentTable) {

		int entryNumber = 1;
		List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) parentTable.getItemIds());

		for (GermplasmListEntry entry : itemIds) {
			Item item = parentTable.getItem(entry);
			item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(Integer.valueOf(entryNumber));
			entry.setEntryId(entryNumber);
			entryNumber++;
		}
	}

	public void resetListDataTableValues() {
		this.listDataTable.removeAllItems();
		this.loadEntriesToListDataTable();
		this.listDataTable.requestRepaint();
	}

	public void loadEntriesToListDataTable() {
		try {
			this.listEntriesCount = this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId());

			if (this.listEntriesCount > 0) {
				this.listEntries = new ArrayList<GermplasmListData>();
				this.getAllListEntries();

				this.updateListDataTable(this.germplasmList.getId(), this.listEntries);
			}
		} catch (MiddlewareQueryException e) {
			ParentTabComponent.LOG.error("Error loading list data in Parent Tab Component. " + e.getMessage(), e);
		}
	}

	private void getAllListEntries() {
		if (this.germplasmList != null) {
			List<GermplasmListData> entries = null;
			try {
				entries =
						this.inventoryDataManager.getLotCountsForList(this.germplasmList.getId(), 0, Long.valueOf(this.listEntriesCount)
								.intValue());

				this.listEntries.addAll(entries);
			} catch (MiddlewareQueryException ex) {
				ParentTabComponent.LOG.error("Error with retrieving list entries for list: " + this.germplasmList.getId(), ex);
				this.listEntries = new ArrayList<GermplasmListData>();
			}
		}
	}

	@Override
	public void layoutComponents() {
		this.setMargin(true, true, false, true);
		this.setSpacing(true);

		this.addComponent(this.actionMenu);
		this.addComponent(this.inventoryViewActionMenu);

		HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.listEntriesLabel);

		this.headerLayout = new HorizontalLayout();
		this.headerLayout.setWidth("100%");
		this.headerLayout.addComponent(headingLayout);
		this.headerLayout.addComponent(this.editHeaderButton);
		this.headerLayout.setComponentAlignment(headingLayout, Alignment.MIDDLE_LEFT);
		this.headerLayout.setComponentAlignment(this.editHeaderButton, Alignment.BOTTOM_RIGHT);

		HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
		leftSubHeaderLayout.setSpacing(true);
		leftSubHeaderLayout.addComponent(this.totalListEntriesLabel);
		leftSubHeaderLayout.addComponent(this.totalSelectedListEntriesLabel);
		leftSubHeaderLayout.setComponentAlignment(this.totalListEntriesLabel, Alignment.MIDDLE_LEFT);
		leftSubHeaderLayout.setComponentAlignment(this.totalSelectedListEntriesLabel, Alignment.MIDDLE_LEFT);

		this.subHeaderLayout = new HorizontalLayout();
		this.subHeaderLayout.setWidth("100%");
		this.subHeaderLayout.addComponent(leftSubHeaderLayout);
		this.subHeaderLayout.addComponent(this.actionButton);
		this.subHeaderLayout.setComponentAlignment(leftSubHeaderLayout, Alignment.MIDDLE_LEFT);
		this.subHeaderLayout.setComponentAlignment(this.actionButton, Alignment.TOP_RIGHT);

		this.addComponent(this.headerLayout);
		this.addComponent(this.subHeaderLayout);
		this.addComponent(this.tableWithSelectAllLayout);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void updateListDataTable(GermplasmList germplasmList) {
		Integer germplasmListId = germplasmList.getId();

		try {
			List<GermplasmListData> germplasmListDataFromListFromTree =
					this.inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);
			this.updateListDataTable(germplasmListId, germplasmListDataFromListFromTree);
		} catch (MiddlewareQueryException e) {
			ParentTabComponent.LOG.error("Error in retrieving list data entries with lot counts", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateListDataTable(Integer germplasmListId, List<GermplasmListData> savedListEntries) {
		List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();

		selectedItemIds.addAll((Collection<GermplasmListEntry>) this.listDataTable.getValue());
		this.listDataTable.removeAllItems();

		for (GermplasmListData entry : savedListEntries) {
			GermplasmListEntry itemId =
					new GermplasmListEntry(entry.getId(), entry.getGid(), entry.getEntryId(), entry.getDesignation(), entry.getSeedSource());

			Item newItem = this.listDataTable.getContainerDataSource().addItem(itemId);

			// #1
			CheckBox tag = new CheckBox();
			newItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(tag);

			tag.addListener(new ParentsTableCheckboxListener(this.listDataTable, itemId, this.tableWithSelectAllLayout.getCheckBox()));
			tag.setImmediate(true);

			if (selectedItemIds.contains(itemId)) {
				this.listDataTable.select(itemId);
			}

			// #3
			String designationName = entry.getDesignation();

			Button designationButton = new Button(designationName, new GidLinkClickListener(entry.getGid().toString(), true));
			designationButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationButton.setDescription(ParentTabComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			// #4
			// default value
			String availInv = ParentTabComponent.STRING_DASH;
			if (entry.getInventoryInfo().getLotCount().intValue() != 0) {
				availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}

			InventoryLinkButtonClickListener inventoryClickListener =
					new InventoryLinkButtonClickListener(this, germplasmListId, entry.getId(), entry.getGid());
			Button inventoryButton = new Button(availInv, inventoryClickListener);
			inventoryButton.setData(inventoryClickListener);
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(ParentTabComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

			if (availInv.equals(ParentTabComponent.STRING_DASH)) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription(ParentTabComponent.NO_LOT_FOR_THIS_GERMPLASM);
			} else {
				inventoryButton.setDescription(ParentTabComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
			}

			// #5
			// default value
			String seedRes = ParentTabComponent.STRING_DASH;
			if (entry.getInventoryInfo().getReservedLotCount().intValue() != 0) {
				seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
			}

			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
			newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(entry.getInventoryInfo().getStockIDs());

		}

		this.resetUnsavedChangesFlag();
		this.listDataTable.requestRepaint();
	}

	/*--------------------------------------INVENTORY RELATED FUNCTIONS---------------------------------------*/

	private void viewListAction() {

		if (!this.hasUnsavedChanges()) {
			this.source.getMakeCrossesMain().setModeView(ModeView.LIST_VIEW);
		} else {
			String message =
					"You have unsaved reservations for this list. " + "You will need to save them before changing views. "
							+ "Do you want to save your changes?";
			this.source.getMakeCrossesMain().showUnsavedChangesConfirmDialog(message, ModeView.LIST_VIEW);
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

		this.resetUnsavedChangesFlag();
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

	private void viewInventoryAction() {
		if (this.hasChanges) {
			if (this.makeCrossesMain.areBothParentsNewListWithUnsavedChanges()) {
				MessageNotifier.showError(this.getWindow(), "Unsaved Parent Lists", "Please save parent lists first before changing view.");
			} else {
				this.source.getMakeCrossesMain().showUnsavedChangesConfirmDialog("You have unsaved changes to the male/female list you are "
						+ "editing. You will need to save them before changing views. Do you want to save your changes?",
						ModeView.INVENTORY_VIEW);
			}
		} else {
			this.source.getMakeCrossesMain().setModeView(ModeView.INVENTORY_VIEW);
		}
	}

	public void resetList() {
		this.updateNoOfEntries(0);
		this.updateNoOfSelectedEntries(0);

		// Reset list data table
		this.listDataTable.removeAllItems();

		// list inventory table
		this.listInventoryTable.reset();

		// Reset the marker for changes in Build New List
		this.resetUnsavedChangesFlag();
	}

	public void resetUnsavedChangesFlag() {
		this.inventoryTableDropHandler.setHasChanges(false);
		this.setHasUnsavedChanges(false);
	}

	public void viewInventoryActionConfirmed() {
		// set the listId in List Inventory Table
		if (this.listInventoryTable.getListId() == null && this.germplasmList != null) {
			this.listInventoryTable.setListId(this.germplasmList.getId());
		}

		this.listInventoryTable.loadInventoryData();
		this.changeToInventoryView();
	}

	public void changeToInventoryView() {
		if (this.tableWithSelectAllLayout.isVisible()) {
			this.tableWithSelectAllLayout.setVisible(false);
			this.listInventoryTable.setVisible(true);

			this.subHeaderLayout.removeComponent(this.actionButton);
			this.subHeaderLayout.addComponent(this.inventoryViewActionButton);
			this.subHeaderLayout.setComponentAlignment(this.inventoryViewActionButton, Alignment.MIDDLE_RIGHT);

			this.listEntriesLabel.setValue(this.messageSource.getMessage(Message.LOTS));
			this.updateNoOfEntries();
			this.updateNoOfSelectedEntries();

			this.removeComponent(this.tableWithSelectAllLayout);
			this.addComponent(this.listInventoryTable);

			this.requestRepaint();
		}
	}

	public void reserveInventoryAction() {
		// checks if the screen is in the inventory view
		if (!this.inventoryViewActionMenu.isVisible()) {
			// checks if the screen is in the inventory view
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

		this.setHasUnsavedChanges(true);

		// if there are no valid reservations
		if (validReservations.isEmpty()) {
			MessageNotifier
					.showRequiredFieldError(this.getWindow(), this.messageSource
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

		if (!this.validReservationsToSave.isEmpty()) {
			this.setHasUnsavedChanges(true);
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

	public void saveReservationChangesAction(boolean displayReservationSuccessMessage) {

		if (this.hasUnsavedChanges()) {
			this.reserveInventoryAction = new ReserveInventoryAction(this);
			boolean success =
					this.reserveInventoryAction.saveReserveTransactions(this.getValidReservationsToSave(), this.germplasmList.getId());
			if (success) {
				this.refreshInventoryColumns(this.getValidReservationsToSave());
				this.resetListInventoryTableValues();

				if (displayReservationSuccessMessage) {
					MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
							"All reservations were saved.");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void refreshInventoryColumns(Map<ListEntryLotDetails, Double> validReservationsToSave2) {

		Set<Integer> entryIds = new HashSet<Integer>();
		for (Entry<ListEntryLotDetails, Double> details : this.validReservationsToSave.entrySet()) {
			entryIds.add(details.getKey().getId());
		}

		List<GermplasmListData> germplasmListDataEntries = new ArrayList<GermplasmListData>();

		try {
			if (!entryIds.isEmpty()) {
				germplasmListDataEntries =
						this.inventoryDataManager.getLotCountsForListEntries(this.germplasmList.getId(), new ArrayList<Integer>(entryIds));
			}
		} catch (MiddlewareQueryException e) {
			ParentTabComponent.LOG.error(e.getMessage(), e);
		}

		Collection<? extends GermplasmListEntry> itemIds = (Collection<? extends GermplasmListEntry>) this.listDataTable.getItemIds();
		for (GermplasmListData listData : germplasmListDataEntries) {
			GermplasmListEntry itemId = this.getGermplasmListEntry(listData.getEntryId(), itemIds);
			Item item = this.listDataTable.getItem(itemId);

			// #1 Available Inventory
			// default value
			String availInv = ParentTabComponent.STRING_DASH;
			if (listData.getInventoryInfo().getLotCount().intValue() != 0) {
				availInv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
			}
			Button inventoryButton =
					new Button(availInv, new InventoryLinkButtonClickListener(this.source, this.germplasmList.getId(), listData.getId(),
							listData.getGid()));
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(ParentTabComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

			if (availInv.equals(ParentTabComponent.STRING_DASH)) {
				inventoryButton.setEnabled(false);
				inventoryButton.setDescription(ParentTabComponent.NO_LOT_FOR_THIS_GERMPLASM);
			} else {
				inventoryButton.setDescription(ParentTabComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
			}

			item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

			// Seed Reserved
			// default value
			String seedRes = ParentTabComponent.STRING_DASH;
			if (listData.getInventoryInfo().getReservedLotCount().intValue() != 0) {
				seedRes = listData.getInventoryInfo().getReservedLotCount().toString().trim();
			}

			item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
		}

	}

	/*--------------------------------END OF INVENTORY RELATED FUNCTIONS--------------------------------------*/

	private GermplasmListEntry getGermplasmListEntry(Integer entryId, Collection<? extends GermplasmListEntry> itemIds) {
		for (GermplasmListEntry entry : itemIds) {
			if (entry.getEntryId().equals(entryId)) {
				return entry;
			}
		}
		return null;
	}

	public Map<ListEntryLotDetails, Double> getValidReservationsToSave() {
		return this.validReservationsToSave;
	}

	public ContextMenuItem getSaveActionMenu() {
		return this.saveActionMenu;
	}

	public Table getListDataTable() {
		return this.listDataTable;
	}

	public String getListNameForCrosses() {
		return this.listNameForCrosses;
	}

	public void setListNameForCrosses(String listNameForCrosses) {
		this.listNameForCrosses = listNameForCrosses;
	}

	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	public void setGermplasmList(GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
		this.germplasmList = list;
	}

	@Override
	public Component getParentComponent() {
		return this.makeCrossesMain.getSource();
	}

	private void updateCrossesSeedSource(GermplasmList germplasmList) {
		this.source.updateCrossesSeedSource(this, germplasmList);
	}

	public Message getSuccessMessage() {
		if (this.parentLabel.equals(ParentTabComponent.FEMALE_PARENTS)) {
			return Message.SUCCESS_SAVE_FOR_FEMALE_LIST;
		} else if (this.parentLabel.equals(ParentTabComponent.MALE_PARENTS)) {
			return Message.SUCCESS_SAVE_FOR_MALE_LIST;
		}
		return null;
	}

	@Override
	public void setHasUnsavedChanges(Boolean hasChanges) {
		this.hasChanges = hasChanges;

		if (hasChanges) {
			this.menuInventorySaveChanges.setEnabled(true);
		} else {
			this.menuInventorySaveChanges.setEnabled(false);
		}

		this.inventoryTableDropHandler.setHasChanges(false);

		this.source.setHasUnsavedChanges(this.hasChanges);
	}

	public boolean hasUnsavedChanges() {
		if (this.inventoryTableDropHandler.hasChanges()) {
			this.hasChanges = true;
		}

		return this.hasChanges;
	}

	public void setHasChanges(boolean hasChanges) {
		this.hasChanges = hasChanges;
	}

	public CheckBox getSelectAllCheckBox() {
		return this.tableWithSelectAllLayout.getCheckBox();
	}

	public void discardChangesInListView() {
		this.updateListDataTable(this.germplasmList);
		this.viewInventoryActionConfirmed();
	}

	public void discardChangesInInventoryView() {
		this.resetListInventoryTableValues();
		this.changeToListView();
	}

	@Override
	public void refreshListInventoryItemCount() {
		this.updateNoOfEntries(this.listInventoryTable.getTable().getItemIds().size());
	}

	@SuppressWarnings("unchecked")
	private Integer getListDataTableNextEntryId() {
		int nextId = 0;
		for (GermplasmListEntry entry : (Collection<? extends GermplasmListEntry>) this.listDataTable.getItemIds()) {

			Integer entryId = 0;
			Item item = this.listDataTable.getItem(entry);
			if (item != null) {
				entryId = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			}

			if (entryId > nextId) {
				nextId = entryId;
			}

		}
		return nextId + 1;
	}

	public void updateUIforDeletedList(GermplasmList germplasmList) {
		if (this.germplasmList.getName().equals(germplasmList.getName())) {
			this.getWindow().removeWindow(this.saveListAsWindow);
			// refresh the list tree in select parents
			this.makeCrossesMain.showNodeOnTree(germplasmList.getId());
			this.saveListAsWindow = null;
			this.setGermplasmList(null);
			this.resetList();

			String message = "";
			if (this.parentLabel.equals(ParentTabComponent.FEMALE_PARENTS)) {
				message = "Female Parent List was successfully deleted.";
			} else if (this.parentLabel.equals(ParentTabComponent.MALE_PARENTS)) {
				message = "Male Parent List was successfully deleted.";
			}

			MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS), message);
		}
	}

	public void setPreviousModeView(ModeView prevModeView) {
		this.prevModeView = prevModeView;
	}

	public void enableReserveInventory() {
		this.menuReserveInventory.setEnabled(true);
	}

	public void enableEditListHeaderOption() {
		this.editHeaderButton.setVisible(true);
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public CrossingManagerInventoryTable getListInventoryTable() {
		return this.listInventoryTable;
	}

	public InventoryTableDropHandler getInventoryTableDropHandler() {
		return this.inventoryTableDropHandler;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public boolean isTreatAsNewList() {
		return this.isTreatAsNewList;
	}

	public void setIsTreatAsNewList(boolean isTreatAsNewList) {
		this.isTreatAsNewList = isTreatAsNewList;
	}

	public SaveListAsDialog getSaveListAsWindow() {
		return this.saveListAsWindow;
	}
}

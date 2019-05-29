
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.listeners.ParentsTableCheckboxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUtil;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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

@Configurable
public class ParentTabComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout,
	SaveGermplasmListActionSource {
	
	@Autowired
    private OntologyDataManager ontologyDataManager;

	@Autowired
	private PlatformTransactionManager transactionManager;


	private final class ListDataTableDropHandler implements DropHandler {

		private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";
		private static final long serialVersionUID = -3048433522366977000L;

		@Override
		public void drop(final DragAndDropEvent dropEvent) {

			// Dragged from a table
			if (dropEvent.getTransferable() instanceof TableTransferable) {

				final TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();

				final Table sourceTable = transferable.getSourceComponent();
				final Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();

				final AbstractSelectTargetDetails dropData = (AbstractSelectTargetDetails) dropEvent.getTargetDetails();
				final Object targetItemId = dropData.getItemIdOver();

				if (sourceTable.equals(ParentTabComponent.this.listDataTable)) {
					// Check first if item is dropped on top of itself
					if (!transferable.getItemId().equals(targetItemId)) {

						final Item oldItem = sourceTable.getItem(transferable.getItemId());
						final Object oldCheckBox = oldItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).getValue();
						final Object oldEntryCode = oldItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
						final Object oldDesignation = oldItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
						final Object oldAvailInv = oldItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
						final Object oldparentage = oldItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();
						final Object oldStockId = oldItem.getItemProperty(ColumnLabels.STOCKID.getName()).getValue();

						sourceTable.removeItem(transferable.getItemId());

						final Item newItem = targetTable.addItemAfter(targetItemId, transferable.getItemId());
						newItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(oldCheckBox);
						newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(oldEntryCode);
						newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(oldDesignation);
						newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(oldparentage);
						newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(oldAvailInv);
						newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(oldStockId);

					}
				} else if (sourceTable.getData().equals(SelectParentsListDataComponent.LIST_DATA_TABLE_ID)) {
					ParentTabComponent.this.source.dropToFemaleOrMaleTable(sourceTable, ParentTabComponent.this.listDataTable,
						(Integer) transferable.getItemId());
				}

				// Dragged from the tree
			} else {
				final Transferable transferable = dropEvent.getTransferable();
				final Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();

				try {
					final GermplasmList draggedListFromTree =
						ParentTabComponent.this.germplasmListManager.getGermplasmListById((Integer) transferable.getData("itemId"));
					if (draggedListFromTree != null) {
						final List<GermplasmListData> germplasmListDataFromListFromTree = draggedListFromTree.getListData();

						Integer addedCount = 0;

						for (final GermplasmListData listData : germplasmListDataFromListFromTree) {
							if (listData.getStatus() != 9) {
								final String parentValue = listData.getDesignation();

								final Button gidButton =
									new Button(parentValue, new GidLinkClickListener(listData.getGid().toString(), true));
								gidButton.setStyleName(BaseTheme.BUTTON_LINK);
								gidButton.setDescription(ListDataTableDropHandler.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

								final CheckBox tag = new CheckBox();
								tag.setDebugId("tag");

								final GermplasmListEntry entryObject =
									new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(),
										listData.getDesignation(), draggedListFromTree.getName() + ":" + listData.getEntryId());

								if (targetTable.equals(ParentTabComponent.this.listDataTable)) {
									tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject,
										ParentTabComponent.this.getSelectAllCheckBox()));
									ParentTabComponent.this.listNameForCrosses = draggedListFromTree.getName();
								}

								tag.setImmediate(true);

								// if the item is already existing in the target table, remove the existing item then add a new entry
								targetTable.removeItem(entryObject);

								final Item item = targetTable.getContainerDataSource().addItem(entryObject);

								item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(gidButton);
								item.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(tag);

								addedCount++;
							}
						}

						// After adding, check if the # of items added on the table, is equal to the number of list data of the dragged
						// list, this will enable/disable the save option
						final List<Object> itemsAfterAdding = new ArrayList<>();
						itemsAfterAdding.addAll(targetTable.getItemIds());

						if (addedCount == itemsAfterAdding.size()) {
							// updates the crossesMade save button if both parents are save at least once
							ParentTabComponent.this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();

						} 
					}
				} catch (final MiddlewareQueryException e) {
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
	
	private final class ActionMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(ParentTabComponent.this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {
					final ContextMenuItem clickedItem = event.getClickedItem();
					if (clickedItem.getName().equals(
						ParentTabComponent.this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES))) {
						ParentTabComponent.this.parentActionListener.removeSelectedEntriesAction(ParentTabComponent.this.listDataTable);
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
						ParentTabComponent.this.listDataTable.setValue(ParentTabComponent.this.listDataTable.getItemIds());
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES))) {
						ParentTabComponent.this.listDataTable.setValue(CrossingManagerUtil
							.getEvenEntries(ParentTabComponent.this.listDataTable));
					} else if (clickedItem.getName().equals(ParentTabComponent.this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES))) {
						ParentTabComponent.this.listDataTable.setValue(CrossingManagerUtil
							.getOddEntries(ParentTabComponent.this.listDataTable));
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
	private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";
	private static final Logger LOG = LoggerFactory.getLogger(ParentTabComponent.class);
	private static final long serialVersionUID = 2124522470629189449L;

	private Label listEntriesLabel;
	private Label totalListEntriesLabel;
	private Label totalSelectedListEntriesLabel;

	private Button actionButton;

	// Layout Variables
	private HorizontalLayout subHeaderLayout;
	private HorizontalLayout headerLayout;

	// Tables
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private Table listDataTable;
	@SuppressWarnings("unused")
	private CheckBox selectAll;

	// Actions
	private ContextMenu actionMenu;

	static final String TAG_COLUMN_ID = "Tag";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

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

	public ParentTabComponent(
		final CrossingManagerMakeCrossesComponent makeCrossesMain, final MakeCrossesParentsComponent source,
		final String parentLabel, final Integer rowCount) {
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

		this.initializeParentTable(new TableWithSelectAllLayout(this.rowCount, ParentTabComponent.TAG_COLUMN_ID));
	}

	/**
	 * Exposed for usage in tests
	 */
	public void initializeMainComponents() {
		this.listEntriesLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.listEntriesLabel.setDebugId("listEntriesLabel");
		this.listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.listEntriesLabel.setWidth("160px");

		this.totalListEntriesLabel =
			new Label(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + "  <b>0</b>", Label.CONTENT_XHTML);
		this.totalListEntriesLabel.setWidth("120px");

		this.totalSelectedListEntriesLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedListEntriesLabel.setDebugId("totalSelectedListEntriesLabel");
		this.totalSelectedListEntriesLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.actionButton = new ActionButton();
		this.actionButton.setDebugId("actionButton");

		this.actionMenu = new ContextMenu();
		this.actionMenu.setDebugId("actionMenu");
		this.actionMenu.setWidth("250px");
		this.actionMenu.addItem(this.messageSource.getMessage(Message.CLEAR_ALL));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES));
		this.actionMenu.addItem(this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES));
	}

	public void setRowCount(final Integer rowCount) {
		this.rowCount = rowCount;
	}

	/**
	 * Exposed for usage in tests
	 */
	public void initializeParentTable(final TableWithSelectAllLayout tableWithSelectAllLayout) {
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
		this.listDataTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);

		this.listDataTable.setColumnHeader(ParentTabComponent.TAG_COLUMN_ID, this.messageSource.getMessage(Message.CHECK_ICON));
		this.listDataTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		this.listDataTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		this.listDataTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
		this.listDataTable.setColumnHeader(ColumnLabels.STOCKID.getName(), this.getTermNameFromOntology(ColumnLabels.STOCKID));

		this.listDataTable.setColumnWidth(ParentTabComponent.TAG_COLUMN_ID, 25);
		this.listDataTable.setDragMode(TableDragMode.ROW);
		this.listDataTable.setItemDescriptionGenerator(new ItemDescriptionGenerator() {

			private static final long serialVersionUID = -3207714818504151649L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				if (propertyId != null && propertyId == ColumnLabels.DESIGNATION.getName()) {
					final Table theTable = (Table) source;
					final Item item = theTable.getItem(itemId);
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

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		this.listDataTable.setDropHandler(new ListDataTableDropHandler());

		this.parentActionListener = new CrossingManagerActionHandler(this.source);
		this.listDataTable.addActionHandler(this.parentActionListener);

		this.actionButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ParentTabComponent.this.actionMenu.show(event.getClientX(), event.getClientY());
			}

		});

		this.actionMenu.addListener(new ActionMenuClickListener());

		this.tableWithSelectAllLayout.getTable().addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				ParentTabComponent.this.updateNoOfSelectedEntries();
			}
		});

	}

	public void updateNoOfEntries(final int count) {
		final String noOfEntries = "  <b>" + count + "</b>";
		this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + noOfEntries);
	}

	public void updateNoOfEntries() {
		this.updateNoOfEntries(this.listDataTable.getItemIds().size());
	}

	private void updateNoOfSelectedEntries(final int count) {
		this.totalSelectedListEntriesLabel.setValue("<i>" + this.messageSource.getMessage(Message.SELECTED) + ": " + "  <b>" + count
			+ "</b></i>");
	}

	private void updateNoOfSelectedEntries() {
		int count = 0;

		final Collection<?> selectedItems = (Collection<?>) this.tableWithSelectAllLayout.getTable().getValue();
		count = selectedItems.size();

		this.updateNoOfSelectedEntries(count);
	}

	@SuppressWarnings("unchecked")
	public void assignEntryNumber(final Table parentTable) {

		int entryNumber = 1;
		final List<GermplasmListEntry> itemIds = new ArrayList<>();
		itemIds.addAll((Collection<GermplasmListEntry>) parentTable.getItemIds());

		for (final GermplasmListEntry entry : itemIds) {
			final Item item = parentTable.getItem(entry);
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
				this.listEntries = new ArrayList<>();
				this.getAllListEntries();

				this.updateListDataTable(this.germplasmList.getId(), this.listEntries);
			}
		} catch (final MiddlewareQueryException e) {
			ParentTabComponent.LOG.error("Error loading list data in Parent Tab Component. " + e.getMessage(), e);
		}
	}

	private void getAllListEntries() {
		if (this.germplasmList != null) {
			List<GermplasmListData> entries = null;
			try {
				entries = this.inventoryDataManager.getLotCountsForList(this.germplasmList.getId(), 0,
					Long.valueOf(this.listEntriesCount).intValue());

				this.listEntries.addAll(entries);
			} catch (final MiddlewareQueryException ex) {
				ParentTabComponent.LOG.error("Error with retrieving list entries for list: " + this.germplasmList.getId(), ex);
				this.listEntries = new ArrayList<>();
			}
		}
	}

	@Override
	public void layoutComponents() {
		this.setMargin(true, true, false, true);
		this.setSpacing(true);

		this.addComponent(this.actionMenu);

		final HeaderLabelLayout headingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.listEntriesLabel);
		headingLayout.setDebugId("headingLayout");

		this.headerLayout = new HorizontalLayout();
		this.headerLayout.setDebugId("headerLayout");
		this.headerLayout.setWidth("100%");
		this.headerLayout.addComponent(headingLayout);
		this.headerLayout.setComponentAlignment(headingLayout, Alignment.MIDDLE_LEFT);

		final HorizontalLayout leftSubHeaderLayout = new HorizontalLayout();
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
		this.subHeaderLayout.setComponentAlignment(this.actionButton, Alignment.TOP_RIGHT);

		this.addComponent(this.headerLayout);
		this.addComponent(this.subHeaderLayout);
		this.addComponent(this.tableWithSelectAllLayout);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void updateListDataTable(final GermplasmList germplasmList) {
		final Integer germplasmListId = germplasmList.getId();

		try {
			final List<GermplasmListData> germplasmListDataFromListFromTree =
				this.inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);
			this.updateListDataTable(germplasmListId, germplasmListDataFromListFromTree);
		} catch (final MiddlewareQueryException e) {
			ParentTabComponent.LOG.error("Error in retrieving list data entries with lot counts", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateListDataTable(final Integer germplasmListId, final List<GermplasmListData> savedListEntries) {
		final List<Integer> selectedEntryIds = this.getSelectedEntryIds((Collection<GermplasmListEntry>) this.listDataTable.getValue());
		this.listDataTable.removeAllItems();

		for (final GermplasmListData entry : savedListEntries) {
			final GermplasmListEntry itemId =
				new GermplasmListEntry(entry.getId(), entry.getGid(), entry.getEntryId(), entry.getDesignation(), entry.getSeedSource());

			final Item newItem = this.listDataTable.getContainerDataSource().addItem(itemId);

			// #1
			final CheckBox tag = new CheckBox();
			tag.setDebugId("tag");
			newItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(tag);

			tag.addListener(new ParentsTableCheckboxListener(this.listDataTable, itemId, this.tableWithSelectAllLayout.getCheckBox()));
			tag.setImmediate(true);

			// #3
			final String designationName = entry.getDesignation();

			final Button designationButton = new Button(designationName, new GidLinkClickListener(entry.getGid().toString(), true));
			designationButton.setDebugId("designationButton");
			designationButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationButton.setDescription(ParentTabComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			// #4
			// default value
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(designationButton);
			newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(entry.getGroupName());
			newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(entry.getInventoryInfo().getStockIDs());

		}

		// move selection of previously checked entries when all the items are already added
		// as checkbox fires value change immediately
		this.preserveSelectedEntriesBeforeSaving(selectedEntryIds);

		this.listDataTable.requestRepaint();
	}

	@SuppressWarnings("unchecked")
	private void preserveSelectedEntriesBeforeSaving(final List<Integer> selectedEntryIds) {
		final Collection<GermplasmListEntry> entries =
			(Collection<GermplasmListEntry>) this.listDataTable.getContainerDataSource().getItemIds();
		for (final GermplasmListEntry entry : entries) {
			if (selectedEntryIds.contains(entry.getEntryId())) {
				this.listDataTable.select(entry);
			}
		}
	}

	private List<Integer> getSelectedEntryIds(final Collection<GermplasmListEntry> selectedGermplasmListEntries) {
		final List<Integer> selectedEntryIds = new ArrayList<>();
		if (selectedGermplasmListEntries != null) {
			for (final GermplasmListEntry germplasmListEntry : selectedGermplasmListEntries) {
				selectedEntryIds.add(germplasmListEntry.getEntryId());
			}
		}
		return selectedEntryIds;
	}

	public void resetList() {
		this.updateNoOfEntries(0);
		this.updateNoOfSelectedEntries(0);

		// Reset list data table
		this.listDataTable.removeAllItems();
	}

	public Table getListDataTable() {
		return this.listDataTable;
	}

	public String getListNameForCrosses() {
		return this.listNameForCrosses;
	}

	public void setListNameForCrosses(final String listNameForCrosses) {
		this.listNameForCrosses = listNameForCrosses;
	}

	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	public void setGermplasmList(final GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	public CheckBox getSelectAllCheckBox() {
		return this.tableWithSelectAllLayout.getCheckBox();
	}

	public void updateUIforDeletedList(final GermplasmList germplasmList) {
		if (this.germplasmList.getName().equals(germplasmList.getName())) {
			// refresh the list tree in select parents
			this.makeCrossesMain.showNodeOnTree(germplasmList.getId());
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

	protected String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}

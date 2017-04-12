package org.generationcp.breeding.manager.crossingmanager;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import org.generationcp.breeding.manager.action.SaveGermplasmListActionFactory;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.listeners.ParentsTableCheckboxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesSource;
import org.generationcp.breeding.manager.inventory.ReserveInventoryActionFactory;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configurable
public class MakeCrossesParentsComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean,
	InternationalizableComponent, UnsavedChangesSource {

	private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";
	private static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";
	private static final String STRING_DASH = "-";
	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesParentsComponent.class);
	private static final long serialVersionUID = -4789763601080845176L;

	private static final int PARENTS_TABLE_ROW_COUNT = 5;

	private static final String TAG_COLUMN_ID = "Tag";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	private TabSheet femaleParentTabSheet;
	private TabSheet maleParentTabSheet;

	private Label parentListsLabel;
	private Label instructionForParentLists;

	private ParentTabComponent femaleParentTab;
	private ParentTabComponent maleParentTab;

	private CrossingManagerMakeCrossesComponent makeCrossesMain;

	public MakeCrossesParentsComponent(final CrossingManagerMakeCrossesComponent parentComponent) {
		this.makeCrossesMain = parentComponent;
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		// do nothing
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
		this.parentListsLabel = new Label(this.messageSource.getMessage(Message.PARENTS_LISTS));
		this.parentListsLabel.setDebugId("parentListsLabel");
		this.parentListsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.parentListsLabel.addStyleName(AppConstants.CssStyles.BOLD);
		this.parentListsLabel.setWidth("230px");

		this.instructionForParentLists = new Label(this.messageSource.getMessage(Message.INSTRUCTION_FOR_PARENT_LISTS));
		this.instructionForParentLists.setDebugId("instructionForParentLists");

		this.femaleParentTab =
			new ParentTabComponent(this.makeCrossesMain, this, this.messageSource.getMessage(Message.LABEL_FEMALE_PARENTS),
				MakeCrossesParentsComponent.PARENTS_TABLE_ROW_COUNT, new SaveGermplasmListActionFactory(),
				new ReserveInventoryActionFactory());

		this.maleParentTab = new ParentTabComponent(this.makeCrossesMain, this, this.messageSource.getMessage(Message.LABEL_MALE_PARENTS),
			MakeCrossesParentsComponent.PARENTS_TABLE_ROW_COUNT, new SaveGermplasmListActionFactory(),
			new ReserveInventoryActionFactory());
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		this.setMargin(false, false, false, true);
		this.setWidth("100%");

		this.femaleParentTabSheet = new TabSheet();
		this.femaleParentTabSheet.setDebugId("femaleParentTabSheet");
		this.femaleParentTabSheet.addTab(this.femaleParentTab, this.messageSource.getMessage(Message.LABEL_FEMALE_PARENTS));
		this.femaleParentTabSheet.setWidth("420px");
		this.femaleParentTabSheet.setHeight("365px");

		this.maleParentTabSheet = new TabSheet();
		this.maleParentTabSheet.setDebugId("maleParentTabSheet");
		this.maleParentTabSheet.addTab(this.maleParentTab, this.messageSource.getMessage(Message.LABEL_MALE_PARENTS));
		this.maleParentTabSheet.setWidth("420px");
		this.maleParentTabSheet.setHeight("365px");

		HorizontalLayout parentListHLayout = new HorizontalLayout();
		parentListHLayout.setWidth("100%");
		parentListHLayout.setDebugId("parentListHLayout");
		parentListHLayout.addComponent(femaleParentTabSheet);
		parentListHLayout.setSpacing(true);
		parentListHLayout.addComponent(maleParentTabSheet);

		final HeaderLabelLayout parentLabelLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.parentListsLabel);
		parentLabelLayout.setDebugId("parentLabelLayout");
		this.addComponent(parentLabelLayout);
		this.addComponent(this.instructionForParentLists);
		this.addComponent(parentListHLayout);
	}

	// end of layoutComponent

	@SuppressWarnings("unchecked")
	public void dropToFemaleOrMaleTable(final Table sourceTable, final Table targetTable, final Integer transferrableItemId) {
		final List<Integer> selectedListEntries = new ArrayList<Integer>();
		selectedListEntries.addAll((Collection<Integer>) sourceTable.getValue());

		if (selectedListEntries.isEmpty() && transferrableItemId != null) {
			selectedListEntries.add(transferrableItemId);
		}

		final List<Integer> entryIdsInSourceTable = new ArrayList<Integer>();
		entryIdsInSourceTable.addAll((Collection<Integer>) sourceTable.getItemIds());

		final List<Integer> initialEntryIdsInDestinationTable = new ArrayList<Integer>();
		initialEntryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());

		for (final Integer itemId : entryIdsInSourceTable) {
			if (selectedListEntries.contains(itemId)) {
				final Integer entryId = (Integer) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();

				final String parentage = (String) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue();

				final Button designationBtn =
					(Button) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
				final String designation = designationBtn.getCaption();

				final Button gidBtn = (Button) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName()).getValue();
				final Integer gid = Integer.valueOf(Integer.parseInt(gidBtn.getCaption()));

				final String seedSource = this.getSeedSource(sourceTable, entryId);

				final GermplasmListEntry entryObject = new GermplasmListEntry(itemId, gid, entryId, designation, seedSource);
				final Item item = targetTable.addItem(entryObject);

				if (item != null) {
					final Button newGidButton = new Button(designation, new GidLinkClickListener(gid.toString(), true));
					newGidButton.setDebugId("newGidButton");
					newGidButton.setStyleName(BaseTheme.BUTTON_LINK);
					newGidButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

					item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(newGidButton);
					if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
						entryObject.setFromFemaleTable(true);
						this.femaleParentTab.updateNoOfEntries(this.femaleParentTab.getListDataTable().size());
						this.femaleParentTab.setHasUnsavedChanges(true);
					} else {
						entryObject.setFromFemaleTable(false);
						this.maleParentTab.updateNoOfEntries(this.maleParentTab.getListDataTable().size());
						this.maleParentTab.setHasUnsavedChanges(true);
					}

					final CheckBox tag = new CheckBox();
					tag.setDebugId("tag");
					if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
						tag.addListener(
							new ParentsTableCheckboxListener(targetTable, entryObject, this.femaleParentTab.getSelectAllCheckBox()));
					} else {
						tag.addListener(
							new ParentsTableCheckboxListener(targetTable, entryObject, this.maleParentTab.getSelectAllCheckBox()));
					}
					tag.setImmediate(true);
					tag.setValue(true);
					item.getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).setValue(tag);

					final Object columnValue = sourceTable.getItem(itemId).getItemProperty(ColumnLabels.STOCKID.getName()).getValue();
					final Label stockIdLabel = (Label) columnValue;
					final Label newStockIdLabel = new Label(stockIdLabel.getValue().toString());
					newStockIdLabel.setDebugId("newStockIdLabel");
					newStockIdLabel.setDescription(stockIdLabel.getValue().toString());
					item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(newStockIdLabel);

					item.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(parentage);

					Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) targetTable.getValue();
					Set<GermplasmListEntry> entriesToSelect = new HashSet<GermplasmListEntry>();

					if (selectedEntries != null) {
						entriesToSelect.addAll(selectedEntries);
						entriesToSelect.add(entryObject);
					} else {
						entriesToSelect.add(entryObject);
					}
					targetTable.setValue(entriesToSelect);
				}
			}
			targetTable.requestRepaint();
		}

		final List<Integer> entryIdsInDestinationTable = new ArrayList<Integer>();
		entryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());

		// drag all entries of a list to the parent list
		if (initialEntryIdsInDestinationTable.isEmpty() && entryIdsInSourceTable.size() == entryIdsInDestinationTable.size()) {
			if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
				final GermplasmList femaleGermplasmList =
					((SelectParentsListDataComponent) this.makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet()
						.getSelectedTab()).getGermplasmList();
				this.updateFemaleParentList(femaleGermplasmList);
				this.femaleParentTab.setHasUnsavedChanges(false);
			} else {
				final GermplasmList maleGermplasmList =
					((SelectParentsListDataComponent) this.makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet()
						.getSelectedTab()).getGermplasmList();
				this.updateMaleParentList(maleGermplasmList);
				this.maleParentTab.setHasUnsavedChanges(false);
			}

			// updates the crosses made save button if both parents are save at least once
			this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();

		} else {
			// drag some entries of a list to the parent list
			this.updateParentTabForUnsavedChanges(targetTable);
		}
	}

	void updateParentTabForUnsavedChanges(final Table targetTable) {
		// just add the new entry to the parent table
		if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
			this.femaleParentTab.setHasUnsavedChanges(true);
		} else {
			this.maleParentTab.setHasUnsavedChanges(true);
		}
	}

	@SuppressWarnings("unchecked")
	public void assignEntryNumber(final Table parentsTable) {

		int entryNumber = 1;
		final List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) parentsTable.getItemIds());

		for (final GermplasmListEntry entry : itemIds) {
			final Item item = parentsTable.getItem(entry);
			item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(Integer.valueOf(entryNumber));
			entry.setEntryId(entryNumber);
			entryNumber++;
		}
	}

	public void updateCrossesSeedSource(final ParentTabComponent parentTab, final GermplasmList savedList) {
		if (parentTab.equals(this.femaleParentTab)) {
			this.femaleParentTab.setGermplasmList(savedList);
			if (this.femaleParentTab.getListNameForCrosses() != null && !this.femaleParentTab.getListNameForCrosses()
				.equals(this.femaleParentTab.getGermplasmList().getName())) {
				this.femaleParentTab.setListNameForCrosses(this.femaleParentTab.getGermplasmList().getName());
				this.makeCrossesMain
					.updateCrossesSeedSource(this.femaleParentTab.getListNameForCrosses(), this.maleParentTab.getListNameForCrosses());
			}
		} else {
			this.maleParentTab.setGermplasmList(savedList);
			if (this.maleParentTab.getListNameForCrosses() != null && !this.maleParentTab.getListNameForCrosses()
				.equals(this.maleParentTab.getGermplasmList().getName())) {
				this.maleParentTab.setListNameForCrosses(this.maleParentTab.getGermplasmList().getName());
				this.makeCrossesMain
					.updateCrossesSeedSource(this.femaleParentTab.getListNameForCrosses(), this.maleParentTab.getListNameForCrosses());
			}
		}
	}

	public void updateUIForSuccessfulSaving(final ParentTabComponent parentTab, final GermplasmList list) {
		this.makeCrossesMain.toggleNextButton();

		this.makeCrossesMain.getSelectParentsComponent().selectListInTree(list.getId());
		this.makeCrossesMain.getSelectParentsComponent().updateUIForDeletedList(list);

		// updates the crosses made save button if both parents are save at least once
		this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
	}

	public void updateFemaleListNameForCrosses() {
		String femaleListNameForCrosses = "";
		femaleListNameForCrosses = this.getFemaleList() != null ? this.getFemaleList().getName() : "";
		this.femaleParentTab.setListNameForCrosses(femaleListNameForCrosses);
	}

	public void updateMaleListNameForCrosses() {
		String maleListNameForCrosses = "";
		maleListNameForCrosses = this.getMaleList() != null ? this.getMaleList().getName() : "";
		this.maleParentTab.setListNameForCrosses(maleListNameForCrosses);
	}

	public boolean isFemaleListSaved() {
		if (this.femaleParentTab.getListNameForCrosses() != null) {
			return this.femaleParentTab.getListNameForCrosses().length() > 0;
		}
		return false;
	}

	public boolean isMaleListSaved() {
		if (this.maleParentTab.getListNameForCrosses() != null) {
			return this.maleParentTab.getListNameForCrosses().length() > 0;
		}
		return false;
	}

	public String getSeedSource(final Table table, final Integer entryId) {
		String seedSource = "";
		if (table.getParent() != null && table.getParent().getParent() instanceof SelectParentsListDataComponent) {
			final SelectParentsListDataComponent parentComponent = (SelectParentsListDataComponent) table.getParent().getParent();
			final String listname = parentComponent.getListName();
			seedSource = listname + ":" + entryId;
		}

		return seedSource;
	}

	/**
	 * Implemented something similar to table.getValue(), because that method returns a collection of items, but does not follow the sorting
	 * done by the drag n drop sorting, this one does
	 *
	 * @param table
	 * @return List of selected germplasm list entries
	 */
	@SuppressWarnings("unchecked")
	protected List<GermplasmListEntry> getCorrectSortedValue(final Table table) {
		final List<GermplasmListEntry> allItemIds = new ArrayList<GermplasmListEntry>();
		final List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
		final List<GermplasmListEntry> sortedSelectedValues = new ArrayList<GermplasmListEntry>();

		allItemIds.addAll((Collection<GermplasmListEntry>) table.getItemIds());
		selectedItemIds.addAll((Collection<GermplasmListEntry>) table.getValue());

		for (final GermplasmListEntry entry : allItemIds) {
			final CheckBox tag = (CheckBox) table.getItem(entry).getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).getValue();
			final Boolean tagValue = (Boolean) tag.getValue();
			if (tagValue.booleanValue()) {
				selectedItemIds.add(entry);
			}
		}

		for (final GermplasmListEntry itemId : allItemIds) {
			for (final GermplasmListEntry selectedItemId : selectedItemIds) {
				if (itemId.equals(selectedItemId)) {
					sortedSelectedValues.add(selectedItemId);
				}
			}
		}
		return sortedSelectedValues;
	}

	@SuppressWarnings("unchecked")
	public void addListToMaleTable(final Integer germplasmListId) {

		try {
			final GermplasmList listFromTree = this.germplasmListManager.getGermplasmListById(germplasmListId);
			if (listFromTree != null) {
				final List<GermplasmListData> germplasmListDataFromListFromTree =
					this.inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);

				Integer addedCount = 0;

				for (final GermplasmListData listData : germplasmListDataFromListFromTree) {
					if (listData.getStatus() != 9) {
						final String maleParentValue = listData.getDesignation();

						final Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(), true));
						gidButton.setDebugId("gidButton");
						gidButton.setStyleName(BaseTheme.BUTTON_LINK);
						gidButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

						final CheckBox tag = new CheckBox();
						tag.setDebugId("tag");

						final GermplasmListEntry entryObject =
							new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(),
								listData.getDesignation(), listFromTree.getName() + ":" + listData.getEntryId());

						tag.addListener(new ParentsTableCheckboxListener(this.maleParentTab.getListDataTable(), entryObject,
							this.maleParentTab.getSelectAllCheckBox()));
						tag.setImmediate(true);

						// if the item is already existing in the target table, remove the existing item then add a new entry
						this.maleParentTab.getListDataTable().removeItem(entryObject);

						final Item item = this.maleParentTab.getListDataTable().addItem(entryObject);
						item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(gidButton);
						item.getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).setValue(tag);

						final Label stockIdLabel = new Label(listData.getInventoryInfo().getStockIDs());
						stockIdLabel.setDebugId("stockIdLabel");
						stockIdLabel.setDescription(listData.getInventoryInfo().getStockIDs());
						item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdLabel);

						addedCount++;
					}
				}

				// After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this
				// will enable/disable the save option
				final List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
				itemsLeftAfterAdding.addAll(this.maleParentTab.getListDataTable().getItemIds());

				if (addedCount == itemsLeftAfterAdding.size()) {
					this.maleParentTab.setHasUnsavedChanges(false);

					// updates the crosses made save button if both parents are save at least once
					this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
				} else {
					this.maleParentTab.setHasUnsavedChanges(true);
				}
			}

			// set up the Germplasm List in Parent Tab
			this.updateMaleParentList(listFromTree);

		} catch (final MiddlewareQueryException e) {
			MakeCrossesParentsComponent.LOG.error("Error in getting list by GID", e);
		}

		this.assignEntryNumber(this.maleParentTab.getListDataTable());
		this.maleParentTabSheet.setSelectedTab(1);

		if (this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW)) {
			this.maleParentTab.updateNoOfEntries(this.maleParentTab.getListDataTable().size());
		}

	}

	@SuppressWarnings("unchecked")
	public void addListToFemaleTable(final Integer germplasmListId) {

		try {
			final GermplasmList listFromTree = this.germplasmListManager.getGermplasmListById(germplasmListId);

			if (listFromTree != null) {

				final List<GermplasmListData> germplasmListDataFromListFromTree =
					this.inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);

				Integer addedCount = 0;

				for (final GermplasmListData listData : germplasmListDataFromListFromTree) {
					if (listData.getStatus() != 9) {
						final String maleParentValue = listData.getDesignation();

						final Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(), true));
						gidButton.setDebugId("gidButton");
						gidButton.setStyleName(BaseTheme.BUTTON_LINK);
						gidButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

						final CheckBox tag = new CheckBox();
						tag.setDebugId("tag");

						final GermplasmListEntry entryObject =
							new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(),
								listData.getDesignation(), listFromTree.getName() + ":" + listData.getEntryId());

						tag.addListener(new ParentsTableCheckboxListener(this.femaleParentTab.getListDataTable(), entryObject,
							this.femaleParentTab.getSelectAllCheckBox()));
						tag.setImmediate(true);

						// if the item is already existing in the target table, remove the existing item then add a new entry
						this.femaleParentTab.getListDataTable().removeItem(entryObject);

						final Item item = this.femaleParentTab.getListDataTable().addItem(entryObject);
						item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(gidButton);
						item.getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).setValue(tag);

						final Label stockIdLabel = new Label(listData.getInventoryInfo().getStockIDs());
						stockIdLabel.setDebugId("stockIdLabel");
						stockIdLabel.setDescription(listData.getInventoryInfo().getStockIDs());
						item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdLabel);

						addedCount++;
					}
				}

				// After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this
				// will enable/disable the save option
				final List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
				itemsLeftAfterAdding.addAll(this.femaleParentTab.getListDataTable().getItemIds());

				if (addedCount == itemsLeftAfterAdding.size()) {
					this.femaleParentTab.setHasUnsavedChanges(false);

					// updates the crosses made save button if both parents are save at least once
					this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();

				} else {
					this.femaleParentTab.setHasUnsavedChanges(true);
				}
			}

			// set up the Germplasm List in Parent Tab
			this.updateFemaleParentList(listFromTree);

		} catch (final MiddlewareQueryException e) {
			MakeCrossesParentsComponent.LOG.error("Error in getting list by GID", e);
		}

		this.assignEntryNumber(this.femaleParentTab.getListDataTable());
		this.femaleParentTabSheet.setSelectedTab(0);

		if (this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW)) {
			this.femaleParentTab.updateNoOfEntries(this.femaleParentTab.getListDataTable().size());
		}
	}

	protected void updateMaleParentList(final GermplasmList listFromTree) {
		// whenever we add a list to male parent tab, only the first list added will be marked as the working list
		if (this.maleParentTab.getGermplasmList() == null) {
			this.maleParentTab.setGermplasmList(listFromTree);
			this.maleParentTab.setListNameForCrosses(listFromTree.getName());
			this.updateCrossesSeedSource(this.maleParentTab, listFromTree);
			this.maleParentTab.enableReserveInventory();
		}

		this.maleParentTab.updateNoOfEntries();
	}

	protected void updateFemaleParentList(final GermplasmList listFromTree) {
		// whenever we add a list to female parent tab, only the first list added will be marked as the working list
		if (this.femaleParentTab.getGermplasmList() == null) {
			this.femaleParentTab.setGermplasmList(listFromTree);
			this.femaleParentTab.setListNameForCrosses(listFromTree.getName());
			this.updateCrossesSeedSource(this.femaleParentTab, listFromTree);
			this.femaleParentTab.enableReserveInventory();
		}
		this.femaleParentTab.updateNoOfEntries();
	}

	// SETTERS AND GETTERS
	public Table getFemaleTable() {
		return this.femaleParentTab.getListDataTable();
	}

	public Table getMaleTable() {
		return this.maleParentTab.getListDataTable();
	}

	public GermplasmList getFemaleList() {
		return this.femaleParentTab.getGermplasmList();
	}

	public GermplasmList getMaleList() {
		return this.maleParentTab.getGermplasmList();
	}

	public String getFemaleListNameForCrosses() {
		return this.femaleParentTab.getListNameForCrosses();
	}

	public String getMaleListNameForCrosses() {
		return this.maleParentTab.getListNameForCrosses();
	}

	public ParentTabComponent getFemaleParentTab() {
		return this.femaleParentTab;
	}

	public void setFemaleParentTab(final ParentTabComponent femaleParentTab) {
		this.femaleParentTab = femaleParentTab;
	}

	public ParentTabComponent getMaleParentTab() {
		return this.maleParentTab;
	}

	public void setMaleParentTab(final ParentTabComponent maleParentTab) {
		this.maleParentTab = maleParentTab;
	}

	public void setMakeCrossesMain(final CrossingManagerMakeCrossesComponent makeCrossesMain) {
		this.makeCrossesMain = makeCrossesMain;
	}

	public void updateViewForAllParentLists(final ModeView modeView) {
		if (modeView.equals(ModeView.LIST_VIEW)) {
			this.femaleParentTab.changeToListView();
			this.maleParentTab.changeToListView();
		} else if (modeView.equals(ModeView.INVENTORY_VIEW)) {
			this.femaleParentTab.viewInventoryActionConfirmed();
			this.maleParentTab.viewInventoryActionConfirmed();
		}
	}

	public CrossingManagerMakeCrossesComponent getMakeCrossesMain() {
		return this.makeCrossesMain;
	}

	public Boolean hasUnsavedChanges() {
		return this.femaleParentTab.hasUnsavedChanges() || this.maleParentTab.hasUnsavedChanges();
	}

	@Override
	public void setHasUnsavedChangesMain(final boolean hasChanges) {
		if (this.hasUnsavedChanges()) {
			this.makeCrossesMain.setHasUnsavedChangesMain(true);
		} else {
			this.makeCrossesMain.setHasUnsavedChangesMain(hasChanges);
		}
	}

	public void updateHasChangesForAllParentList() {
		this.femaleParentTab.resetUnsavedChangesFlag();
		this.maleParentTab.resetUnsavedChangesFlag();
	}

	public void updateUIForDeletedList(final GermplasmList germplasmList) {
		if (this.femaleParentTab.getGermplasmList() != null && this.femaleParentTab.getGermplasmList().getName()
			.equals(germplasmList.getName())) {

			this.femaleParentTab.updateUIforDeletedList(germplasmList);

		}

		if (this.maleParentTab.getGermplasmList() != null && this.maleParentTab.getGermplasmList().getName()
			.equals(germplasmList.getName())) {
			this.maleParentTab.updateUIforDeletedList(germplasmList);

		}
	}

	public TabSheet getFemaleParentTabSheet() {
		return femaleParentTabSheet;
	}

	public TabSheet getMaleParentTabSheet() {
		return maleParentTabSheet;
	}

}


package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
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

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class MakeCrossesParentsComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean,
		InternationalizableComponent, UnsavedChangesSource {

	private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";
	private static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";
	private static final String STRING_DASH = "-";
	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesParentsComponent.class);
	private static final long serialVersionUID = -4789763601080845176L;

	private static final int PARENTS_TABLE_ROW_COUNT = 9;

	private static final String TAG_COLUMN_ID = "Tag";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	private TabSheet parentTabSheet;
	private Label parentListsLabel;
	private Label instructionForParentLists;

	private ParentTabComponent femaleParentTab;
	private ParentTabComponent maleParentTab;

	private CrossingManagerMakeCrossesComponent makeCrossesMain;

	private Boolean hasChanges;

	public MakeCrossesParentsComponent(CrossingManagerMakeCrossesComponent parentComponent) {
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
		this.parentListsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.parentListsLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.instructionForParentLists = new Label(this.messageSource.getMessage(Message.INSTRUCTION_FOR_PARENT_LISTS));

		this.femaleParentTab =
				new ParentTabComponent(this.makeCrossesMain, this, this.messageSource.getMessage(Message.LABEL_FEMALE_PARENTS),
						MakeCrossesParentsComponent.PARENTS_TABLE_ROW_COUNT, new SaveGermplasmListActionFactory(),
						new ReserveInventoryActionFactory());

		this.maleParentTab =
				new ParentTabComponent(this.makeCrossesMain, this, this.messageSource.getMessage(Message.LABEL_MALE_PARENTS),
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
		this.setSpacing(true);
		this.setMargin(false, false, false, true);
		this.setWidth("450px");

		this.parentTabSheet = new TabSheet();
		this.parentTabSheet.addTab(this.femaleParentTab, this.messageSource.getMessage(Message.LABEL_FEMALE_PARENTS));
		this.parentTabSheet.addTab(this.maleParentTab, this.messageSource.getMessage(Message.LABEL_MALE_PARENTS));
		this.parentTabSheet.setWidth("420px");
		this.parentTabSheet.setHeight("465px");

		HeaderLabelLayout parentLabelLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES, this.parentListsLabel);
		this.addComponent(parentLabelLayout);
		this.addComponent(this.instructionForParentLists);
		this.addComponent(this.parentTabSheet);
	}

	// end of layoutComponent

	@SuppressWarnings("unchecked")
	public void dropToFemaleOrMaleTable(Table sourceTable, Table targetTable, Integer transferrableItemId) {
		List<Integer> selectedListEntries = new ArrayList<Integer>();
		selectedListEntries.addAll((Collection<Integer>) sourceTable.getValue());

		Boolean isCopyAllEntries = selectedListEntries.size() == sourceTable.getItemIds().size() && targetTable.getItemIds().isEmpty();

		if (selectedListEntries.isEmpty() && transferrableItemId != null) {
			selectedListEntries.add(transferrableItemId);
		}

		List<Integer> entryIdsInSourceTable = new ArrayList<Integer>();
		entryIdsInSourceTable.addAll((Collection<Integer>) sourceTable.getItemIds());

		List<Integer> initialEntryIdsInDestinationTable = new ArrayList<Integer>();
		initialEntryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());

		for (Integer itemId : entryIdsInSourceTable) {
			if (selectedListEntries.contains(itemId)) {
				Integer entryId = (Integer) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();

				Button designationBtn = (Button) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
				String designation = designationBtn.getCaption();

				Button gidBtn = (Button) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName()).getValue();
				Integer gid = Integer.valueOf(Integer.parseInt(gidBtn.getCaption()));

				String seedSource = this.getSeedSource(sourceTable, entryId);

				GermplasmListEntry entryObject = new GermplasmListEntry(itemId, gid, entryId, designation, seedSource);
				Item item = targetTable.addItem(entryObject);

				if (item != null) {
					Button newGidButton = new Button(designation, new GidLinkClickListener(gid.toString(), true));
					newGidButton.setStyleName(BaseTheme.BUTTON_LINK);
					newGidButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

					item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(newGidButton);
					if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
						entryObject.setFromFemaleTable(true);
						this.femaleParentTab.getSaveActionMenu().setEnabled(true);
						this.femaleParentTab.updateNoOfEntries(this.femaleParentTab.getListDataTable().size());
						this.femaleParentTab.setHasUnsavedChanges(true);
					} else {
						entryObject.setFromFemaleTable(false);
						this.maleParentTab.getSaveActionMenu().setEnabled(true);
						this.maleParentTab.updateNoOfEntries(this.maleParentTab.getListDataTable().size());
						this.maleParentTab.setHasUnsavedChanges(true);
					}

					CheckBox tag = new CheckBox();
					if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
						tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, this.femaleParentTab
								.getSelectAllCheckBox()));
					} else {
						tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, this.maleParentTab
								.getSelectAllCheckBox()));
					}
					tag.setImmediate(true);
					item.getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).setValue(tag);

					Button sourceAvailInvButton =
							(Button) sourceTable.getItem(itemId).getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
					Button newAvailInvButton = new Button();

					newAvailInvButton.setCaption(sourceAvailInvButton.getCaption());
					newAvailInvButton.addListener((InventoryLinkButtonClickListener) sourceAvailInvButton.getData());
					newAvailInvButton.setStyleName(BaseTheme.BUTTON_LINK);
					newAvailInvButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

					String seedRes = MakeCrossesParentsComponent.STRING_DASH;
					if (sourceTable.getItemIds().size() == selectedListEntries.size()) {
						seedRes =
								sourceTable.getItem(itemId).getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue().toString();
					}

					item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(newAvailInvButton);
					item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

					if (isCopyAllEntries) {

						Object columnValue = sourceTable.getItem(itemId).getItemProperty(ColumnLabels.STOCKID.getName()).getValue();
						Label stockIdLabel = (Label) columnValue;
						Label newStockIdLabel = new Label(stockIdLabel.getValue().toString());
						newStockIdLabel.setDescription(stockIdLabel.getValue().toString());
						item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(newStockIdLabel);

					} else {
						item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(new Label(""));
					}

				}
			}

			targetTable.requestRepaint();
		}

		List<Integer> entryIdsInDestinationTable = new ArrayList<Integer>();
		entryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());

		// drag all entries of a list to the parent list
		if (initialEntryIdsInDestinationTable.isEmpty() && entryIdsInSourceTable.size() == entryIdsInDestinationTable.size()) {
			if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
				GermplasmList femaleGermplasmList =
						((SelectParentsListDataComponent) this.makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet()
								.getSelectedTab()).getGermplasmList();
				this.updateFemaleParentList(femaleGermplasmList);
				this.femaleParentTab.getSaveActionMenu().setEnabled(false);
				this.femaleParentTab.setHasUnsavedChanges(false);
			} else {
				GermplasmList maleGermplasmList =
						((SelectParentsListDataComponent) this.makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet()
								.getSelectedTab()).getGermplasmList();
				this.updateMaleParentList(maleGermplasmList);
				this.maleParentTab.getSaveActionMenu().setEnabled(false);
				this.maleParentTab.setHasUnsavedChanges(false);
			}

			// updates the crosses made save button if both parents are save at least once
			this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();

		} else {
			// drag some entries of a list to the parent list
			this.updateParentTabForUnsavedChanges(targetTable);
		}
	}

	void updateParentTabForUnsavedChanges(Table targetTable) {
		// just add the new entry to the parent table
		if (targetTable.equals(this.femaleParentTab.getListDataTable())) {
			this.femaleParentTab.getSaveActionMenu().setEnabled(true);
			this.femaleParentTab.setHasUnsavedChanges(true);
			this.clearSeedReservationValues(this.femaleParentTab.getListDataTable());
		} else {
			this.maleParentTab.getSaveActionMenu().setEnabled(true);
			this.maleParentTab.setHasUnsavedChanges(true);
			this.clearSeedReservationValues(this.maleParentTab.getListDataTable());
		}
	}

	void clearSeedReservationValues(Table table) {
		for (Object itemId : table.getItemIds()) {
			table.getItem(itemId).getItemProperty(ColumnLabels.SEED_RESERVATION.getName())
					.setValue(MakeCrossesParentsComponent.STRING_DASH);
		}
	}

	@SuppressWarnings("unchecked")
	public void assignEntryNumber(Table parentsTable) {

		int entryNumber = 1;
		List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) parentsTable.getItemIds());

		for (GermplasmListEntry entry : itemIds) {
			Item item = parentsTable.getItem(entry);
			item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(Integer.valueOf(entryNumber));
			entry.setEntryId(entryNumber);
			entryNumber++;
		}
	}

	public void updateCrossesSeedSource(ParentTabComponent parentTab, GermplasmList savedList) {
		if (parentTab.equals(this.femaleParentTab)) {
			this.femaleParentTab.setGermplasmList(savedList);
			if (this.femaleParentTab.getListNameForCrosses() != null
					&& !this.femaleParentTab.getListNameForCrosses().equals(this.femaleParentTab.getGermplasmList().getName())) {
				this.femaleParentTab.setListNameForCrosses(this.femaleParentTab.getGermplasmList().getName());
				this.makeCrossesMain.updateCrossesSeedSource(this.femaleParentTab.getListNameForCrosses(),
						this.maleParentTab.getListNameForCrosses());
			}
		} else {
			this.maleParentTab.setGermplasmList(savedList);
			if (this.maleParentTab.getListNameForCrosses() != null
					&& !this.maleParentTab.getListNameForCrosses().equals(this.maleParentTab.getGermplasmList().getName())) {
				this.maleParentTab.setListNameForCrosses(this.maleParentTab.getGermplasmList().getName());
				this.makeCrossesMain.updateCrossesSeedSource(this.femaleParentTab.getListNameForCrosses(),
						this.maleParentTab.getListNameForCrosses());
			}
		}
	}

	public void updateUIForSuccessfulSaving(ParentTabComponent parentTab, GermplasmList list) {
		parentTab.getSaveActionMenu().setEnabled(false);
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

	public String getSeedSource(Table table, Integer entryId) {
		String seedSource = "";
		if (table.getParent() != null && table.getParent().getParent() instanceof SelectParentsListDataComponent) {
			SelectParentsListDataComponent parentComponent = (SelectParentsListDataComponent) table.getParent().getParent();
			String listname = parentComponent.getListName();
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
	protected List<GermplasmListEntry> getCorrectSortedValue(Table table) {
		List<GermplasmListEntry> allItemIds = new ArrayList<GermplasmListEntry>();
		List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
		List<GermplasmListEntry> sortedSelectedValues = new ArrayList<GermplasmListEntry>();

		allItemIds.addAll((Collection<GermplasmListEntry>) table.getItemIds());
		selectedItemIds.addAll((Collection<GermplasmListEntry>) table.getValue());

		for (GermplasmListEntry entry : allItemIds) {
			CheckBox tag = (CheckBox) table.getItem(entry).getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).getValue();
			Boolean tagValue = (Boolean) tag.getValue();
			if (tagValue.booleanValue()) {
				selectedItemIds.add(entry);
			}
		}

		for (GermplasmListEntry itemId : allItemIds) {
			for (GermplasmListEntry selectedItemId : selectedItemIds) {
				if (itemId.equals(selectedItemId)) {
					sortedSelectedValues.add(selectedItemId);
				}
			}
		}
		return sortedSelectedValues;
	}

	@SuppressWarnings("unchecked")
	public void addListToMaleTable(Integer germplasmListId) {

		try {
			GermplasmList listFromTree = this.germplasmListManager.getGermplasmListById(germplasmListId);
			if (listFromTree != null) {
				List<GermplasmListData> germplasmListDataFromListFromTree =
						this.inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);

				Integer addedCount = 0;

				for (GermplasmListData listData : germplasmListDataFromListFromTree) {
					if (listData.getStatus() != 9) {
						String maleParentValue = listData.getDesignation();

						Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(), true));
						gidButton.setStyleName(BaseTheme.BUTTON_LINK);
						gidButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

						CheckBox tag = new CheckBox();

						GermplasmListEntry entryObject =
								new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(),
										listData.getDesignation(), listFromTree.getName() + ":" + listData.getEntryId());

						tag.addListener(new ParentsTableCheckboxListener(this.maleParentTab.getListDataTable(), entryObject,
								this.maleParentTab.getSelectAllCheckBox()));
						tag.setImmediate(true);

						// if the item is already existing in the target table, remove the existing item then add a new entry
						this.maleParentTab.getListDataTable().removeItem(entryObject);

						// #1 Available Inventory
						// default value
						String availInv = MakeCrossesParentsComponent.STRING_DASH;
						if (listData.getInventoryInfo().getLotCount().intValue() != 0) {
							availInv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
						}

						InventoryLinkButtonClickListener inventoryClickListener =
								new InventoryLinkButtonClickListener(this, germplasmListId, listData.getId(), listData.getGid());
						Button inventoryButton = new Button(availInv, inventoryClickListener);
						inventoryButton.setData(inventoryClickListener);
						inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
						inventoryButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

						if (availInv.equals(MakeCrossesParentsComponent.STRING_DASH)) {
							inventoryButton.setEnabled(false);
							inventoryButton.setDescription("No Lot for this Germplasm");
						} else {
							inventoryButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
						}

						// Seed Reserved
						// default value
						String seedRes = MakeCrossesParentsComponent.STRING_DASH;
						if (listData.getInventoryInfo().getReservedLotCount().intValue() != 0) {
							seedRes = listData.getInventoryInfo().getReservedLotCount().toString().trim();
						}

						Item item = this.maleParentTab.getListDataTable().addItem(entryObject);
						item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(gidButton);
						item.getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).setValue(tag);

						item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
						item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

						Label stockIdLabel = new Label(listData.getInventoryInfo().getStockIDs());
						stockIdLabel.setDescription(listData.getInventoryInfo().getStockIDs());
						item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdLabel);

						addedCount++;
					}
				}

				// After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this
				// will enable/disable the save option
				List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
				itemsLeftAfterAdding.addAll(this.maleParentTab.getListDataTable().getItemIds());

				if (addedCount == itemsLeftAfterAdding.size()) {
					this.maleParentTab.getSaveActionMenu().setEnabled(false);
					this.maleParentTab.setHasUnsavedChanges(false);

					// updates the crosses made save button if both parents are save at least once
					this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
				} else {
					this.maleParentTab.getSaveActionMenu().setEnabled(true);
					this.maleParentTab.setHasUnsavedChanges(true);
					this.clearSeedReservationValues(this.maleParentTab.getListDataTable());
				}
			}

			// set up the Germplasm List in Parent Tab
			this.updateMaleParentList(listFromTree);

		} catch (MiddlewareQueryException e) {
			MakeCrossesParentsComponent.LOG.error("Error in getting list by GID", e);
		}

		this.assignEntryNumber(this.maleParentTab.getListDataTable());
		this.parentTabSheet.setSelectedTab(1);

		if (this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW)) {
			this.maleParentTab.updateNoOfEntries(this.maleParentTab.getListDataTable().size());
		}

	}

	@SuppressWarnings("unchecked")
	public void addListToFemaleTable(Integer germplasmListId) {

		try {
			GermplasmList listFromTree = this.germplasmListManager.getGermplasmListById(germplasmListId);

			if (listFromTree != null) {

				List<GermplasmListData> germplasmListDataFromListFromTree =
						this.inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);

				Integer addedCount = 0;

				for (GermplasmListData listData : germplasmListDataFromListFromTree) {
					if (listData.getStatus() != 9) {
						String maleParentValue = listData.getDesignation();

						Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(), true));
						gidButton.setStyleName(BaseTheme.BUTTON_LINK);
						gidButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_GERMPLASM_INFORMATION);

						CheckBox tag = new CheckBox();

						GermplasmListEntry entryObject =
								new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(),
										listData.getDesignation(), listFromTree.getName() + ":" + listData.getEntryId());

						tag.addListener(new ParentsTableCheckboxListener(this.femaleParentTab.getListDataTable(), entryObject,
								this.femaleParentTab.getSelectAllCheckBox()));
						tag.setImmediate(true);

						// if the item is already existing in the target table, remove the existing item then add a new entry
						this.femaleParentTab.getListDataTable().removeItem(entryObject);

						// #1 Available Inventory
						// default value
						String availInv = MakeCrossesParentsComponent.STRING_DASH;
						if (listData.getInventoryInfo().getLotCount().intValue() != 0) {
							availInv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
						}

						InventoryLinkButtonClickListener inventoryClickListener =
								new InventoryLinkButtonClickListener(this, germplasmListId, listData.getId(), listData.getGid());
						Button inventoryButton = new Button(availInv, inventoryClickListener);
						inventoryButton.setData(inventoryClickListener);
						inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
						inventoryButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

						if (availInv.equals(MakeCrossesParentsComponent.STRING_DASH)) {
							inventoryButton.setEnabled(false);
							inventoryButton.setDescription("No Lot for this Germplasm");
						} else {
							inventoryButton.setDescription(MakeCrossesParentsComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);
						}

						// Seed Reserved
						// default value
						String seedRes = MakeCrossesParentsComponent.STRING_DASH;
						if (listData.getInventoryInfo().getReservedLotCount().intValue() != 0) {
							seedRes = listData.getInventoryInfo().getReservedLotCount().toString().trim();
						}

						Item item = this.femaleParentTab.getListDataTable().addItem(entryObject);
						item.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(gidButton);
						item.getItemProperty(MakeCrossesParentsComponent.TAG_COLUMN_ID).setValue(tag);

						item.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
						item.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);

						Label stockIdLabel = new Label(listData.getInventoryInfo().getStockIDs());
						stockIdLabel.setDescription(listData.getInventoryInfo().getStockIDs());
						item.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockIdLabel);

						addedCount++;
					}
				}

				// After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this
				// will enable/disable the save option
				List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
				itemsLeftAfterAdding.addAll(this.femaleParentTab.getListDataTable().getItemIds());

				if (addedCount == itemsLeftAfterAdding.size()) {
					this.femaleParentTab.getSaveActionMenu().setEnabled(false);
					this.femaleParentTab.setHasUnsavedChanges(false);

					// updates the crosses made save button if both parents are save at least once
					this.makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();

				} else {
					this.femaleParentTab.getSaveActionMenu().setEnabled(true);
					this.femaleParentTab.setHasUnsavedChanges(true);
					this.clearSeedReservationValues(this.femaleParentTab.getListDataTable());
				}
			}

			// set up the Germplasm List in Parent Tab
			this.updateFemaleParentList(listFromTree);

		} catch (MiddlewareQueryException e) {
			MakeCrossesParentsComponent.LOG.error("Error in getting list by GID", e);
		}

		this.assignEntryNumber(this.femaleParentTab.getListDataTable());
		this.parentTabSheet.setSelectedTab(0);

		if (this.makeCrossesMain.getModeView().equals(ModeView.LIST_VIEW)) {
			this.femaleParentTab.updateNoOfEntries(this.femaleParentTab.getListDataTable().size());
		}
	}

	protected void updateMaleParentList(GermplasmList listFromTree) {
		// whenever we add a list to male parent tab, only the first list added will be marked as the working list
		if (this.maleParentTab.getGermplasmList() == null) {
			this.maleParentTab.setGermplasmList(listFromTree);
			this.maleParentTab.setListNameForCrosses(listFromTree.getName());
			this.updateCrossesSeedSource(this.maleParentTab, listFromTree);
			this.maleParentTab.enableReserveInventory();
			this.maleParentTab.enableEditListHeaderOption();
		}

		this.maleParentTab.updateNoOfEntries();
	}

	protected void updateFemaleParentList(GermplasmList listFromTree) {
		// whenever we add a list to female parent tab, only the first list added will be marked as the working list
		if (this.femaleParentTab.getGermplasmList() == null) {
			this.femaleParentTab.setGermplasmList(listFromTree);
			this.femaleParentTab.setListNameForCrosses(listFromTree.getName());
			this.updateCrossesSeedSource(this.femaleParentTab, listFromTree);
			this.femaleParentTab.enableReserveInventory();
			this.femaleParentTab.enableEditListHeaderOption();
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

	public void setFemaleParentList(GermplasmList list) {
		this.femaleParentTab.setGermplasmList(list);
	}

	public void setMaleParentList(GermplasmList list) {
		this.maleParentTab.setGermplasmList(list);
	}

	public String getFemaleListNameForCrosses() {
		return this.femaleParentTab.getListNameForCrosses();
	}

	public String getMaleListNameForCrosses() {
		return this.maleParentTab.getListNameForCrosses();
	}

	public TabSheet getParentTabSheet() {
		return this.parentTabSheet;
	}

	public ParentTabComponent getFemaleParentTab() {
		return this.femaleParentTab;
	}

	public void setFemaleParentTab(ParentTabComponent femaleParentTab) {
		this.femaleParentTab = femaleParentTab;
	}

	public ParentTabComponent getMaleParentTab() {
		return this.maleParentTab;
	}

	public void setMaleParentTab(ParentTabComponent maleParentTab) {
		this.maleParentTab = maleParentTab;
	}

	public void setMakeCrossesMain(final CrossingManagerMakeCrossesComponent makeCrossesMain) {
		this.makeCrossesMain = makeCrossesMain;
	}

	public void updateViewForAllParentLists(ModeView modeView) {
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

		this.hasChanges = false;

		if (this.femaleParentTab.hasUnsavedChanges()) {
			this.hasChanges = true;
		} else if (this.maleParentTab.hasUnsavedChanges()) {
			this.hasChanges = true;
		}

		return this.hasChanges;
	}

	public void setHasUnsavedChanges(boolean hasChanges) {
		this.hasChanges = hasChanges;
		this.setHasUnsavedChangesMain(this.hasChanges);
	}

	@Override
	public void setHasUnsavedChangesMain(boolean hasChanges) {
		if (this.femaleParentTab.hasUnsavedChanges() || this.maleParentTab.hasUnsavedChanges()) {
			this.makeCrossesMain.setHasUnsavedChangesMain(true);
		} else {
			this.makeCrossesMain.setHasUnsavedChangesMain(hasChanges);
		}
	}

	public void updateHasChangesForAllParentList() {
		this.femaleParentTab.resetUnsavedChangesFlag();
		this.maleParentTab.resetUnsavedChangesFlag();
	}

	public void updateUIForDeletedList(GermplasmList germplasmList) {
		if (this.femaleParentTab.getGermplasmList() != null
				&& this.femaleParentTab.getGermplasmList().getName().equals(germplasmList.getName())) {

			this.femaleParentTab.updateUIforDeletedList(germplasmList);

		}

		if (this.maleParentTab.getGermplasmList() != null
				&& this.maleParentTab.getGermplasmList().getName().equals(germplasmList.getName())) {
			this.maleParentTab.updateUIforDeletedList(germplasmList);

		}
	}
}


package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTableUtil;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

public class MakeCrossesParentsComponentTest {

	private static final int GERMPLASM_LIST_ID = 1;

	private static final int NO_OF_ENTRIES = 5;

	private static final String STOCK_ID = "STOCK ID here";
	private static final String TAG_COLUMN_ID = "Tag";
	private static final String STRING_DASH = "-";
	private static final String CHECKBOX_COLUMN_ID = "Checkbox Column ID";
	private static final String CLICK_TO_VIEW_INVENTORY_DETAILS = "Click to view Inventory Details";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;
	@Mock
	private SelectParentsComponent selectParentsComponent;
	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private InventoryDataManager inventoryDataManager;
	@Mock
	private ParentTabComponent femaleParentTab;
	@Mock
	private ParentTabComponent maleParentTab;
	@Mock
	private SelectParentsListDataComponent selectedTab;
	@Mock
	private MakeCrossesTableComponent crossesTable;

	@InjectMocks
	private MakeCrossesParentsComponent makeCrossesParentsComponent = new MakeCrossesParentsComponent(this.makeCrossesMain);

	private GermplasmList germplasmList;
	private Table femaleParent;
	private Table maleParent;
	private Table sourceTable;
	private TabSheet listDetailsTabSheet;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.doReturn("Parent List").when(this.messageSource).getMessage(Message.PARENTS_LISTS);
		Mockito.doReturn("Reserve Inventory").when(this.messageSource).getMessage(Message.RESERVE_INVENTORY);

		this.femaleParent = this.createParentTable();
		this.maleParent = this.createParentTable();
		Mockito.doReturn(this.femaleParent).when(this.femaleParentTab).getListDataTable();
		Mockito.doReturn(this.maleParent).when(this.maleParentTab).getListDataTable();

		this.makeCrossesParentsComponent.instantiateComponents();
		this.makeCrossesParentsComponent.layoutComponents();

		// injecting manual mocks
		this.makeCrossesParentsComponent.setMaleParentTab(this.maleParentTab);
		this.makeCrossesParentsComponent.setFemaleParentTab(this.femaleParentTab);
		this.makeCrossesParentsComponent.setMakeCrossesMain(this.makeCrossesMain);

		this.germplasmList = this.createGermplasmList();
		this.sourceTable = this.createSourceTable();
		this.createContextMenuOnParentTab(this.femaleParentTab);
		this.createContextMenuOnParentTab(this.maleParentTab);

		this.listDetailsTabSheet = new TabSheet();
		this.listDetailsTabSheet.addComponent(this.selectedTab);
		Mockito.doReturn(this.selectParentsComponent).when(this.makeCrossesMain).getSelectParentsComponent();
		Mockito.doReturn(this.listDetailsTabSheet).when(this.selectParentsComponent).getListDetailsTabSheet();
		Mockito.doReturn(this.germplasmList).when(this.selectedTab).getGermplasmList();
		Mockito.doReturn(this.crossesTable).when(this.makeCrossesMain).getCrossesTableComponent();
	}

	private void createContextMenuOnParentTab(ParentTabComponent parentTab) {
		ContextMenu menu = new ContextMenu();
		ContextMenuItem saveActionMenu = menu.addItem("Save Action Menu");
		Mockito.doReturn(saveActionMenu).when(parentTab).getSaveActionMenu();
	}

	private Table createSourceTable() {
		Table sourceTable = new Table();
		sourceTable.setSelectable(true);
		sourceTable.setMultiSelect(true);

		sourceTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		sourceTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		sourceTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		sourceTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		sourceTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		sourceTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, new Label(""));
		sourceTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		sourceTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		sourceTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		sourceTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		sourceTable.setColumnHeader(CHECKBOX_COLUMN_ID, this.messageSource.getMessage(Message.CHECK_ICON));
		sourceTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		sourceTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), ColumnLabels.DESIGNATION.getName());
		sourceTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(), ColumnLabels.AVAILABLE_INVENTORY.getName());
		sourceTable.setColumnHeader(ColumnLabels.SEED_RESERVATION.getName(), ColumnLabels.SEED_RESERVATION.getName());
		sourceTable.setColumnHeader(ColumnLabels.STOCKID.getName(), ColumnLabels.STOCKID.getName());
		sourceTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), ColumnLabels.PARENTAGE.getName());
		sourceTable.setColumnHeader(ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.ENTRY_CODE.getName());
		sourceTable.setColumnHeader(ColumnLabels.GID.getName(), ColumnLabels.GID.getName());
		sourceTable.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), ColumnLabels.SEED_SOURCE.getName());

		// init entries
		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
			Item newItem = sourceTable.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);

			Button desigButton = new Button();
			desigButton.setCaption("Designation");
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);

			String availInv = String.valueOf(i);
			InventoryLinkButtonClickListener inventoryLinkButtonClickListener =
					new InventoryLinkButtonClickListener(this.makeCrossesParentsComponent, this.germplasmList.getId(), i, i);
			Button inventoryButton = new Button(availInv, inventoryLinkButtonClickListener);
			inventoryButton.setData(inventoryLinkButtonClickListener);
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(CLICK_TO_VIEW_INVENTORY_DETAILS);

			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(STRING_DASH);

			Button gidButton = new Button();
			gidButton.setCaption(String.valueOf(i));
			newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
		}

		return sourceTable;
	}

	private Table createParentTable() {
		Table parentTable = new Table();

		// init columns
		parentTable.addContainerProperty(MakeCrossesParentsComponentTest.TAG_COLUMN_ID, CheckBox.class, null);
		parentTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, Integer.valueOf(0));
		parentTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		parentTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		parentTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		parentTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);

		parentTable.setColumnHeader(MakeCrossesParentsComponentTest.TAG_COLUMN_ID, this.messageSource.getMessage(Message.CHECK_ICON));
		parentTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		parentTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), ColumnLabels.DESIGNATION.getName());
		parentTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(), ColumnLabels.AVAILABLE_INVENTORY.getName());
		parentTable.setColumnHeader(ColumnLabels.SEED_RESERVATION.getName(), ColumnLabels.SEED_RESERVATION.getName());
		parentTable.setColumnHeader(ColumnLabels.STOCKID.getName(), ColumnLabels.STOCKID.getName());

		// init entries
		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
			this.addItemToParentTable(i, parentTable);
		}
		return parentTable;
	}

	private GermplasmList createGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(GERMPLASM_LIST_ID);
		germplasmList.setName("List Name");
		germplasmList.setDescription("This is a sample list.");
		germplasmList.setDate(20150109L);

		return germplasmList;
	}

	@Test
	public void testUpdateMaleParentList() {
		this.makeCrossesParentsComponent.updateMaleParentList(this.germplasmList);
		try {
			Mockito.verify(this.maleParentTab, Mockito.atLeast(1)).setGermplasmList(this.germplasmList);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting the germplasm list in male parent tab is set but didn't");
		}
	}

	@Test
	public void testUpdateFemaleParentList() {
		this.makeCrossesParentsComponent.updateFemaleParentList(this.germplasmList);
		try {
			Mockito.verify(this.femaleParentTab, Mockito.atLeast(1)).setGermplasmList(this.germplasmList);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting the germplasm list in female parent tab is set but didn't");
		}
	}

	@Test
	public void testUpdateParentTabForUnsavedChangesForFemaleParent() {
		this.testUpdateParentTabForUnsavedChanges(this.femaleParentTab, this.femaleParent, "Female");
	}

	@Test
	public void testUpdateParentTabForUnsavedChangesForMaleParent() {
		this.testUpdateParentTabForUnsavedChanges(this.maleParentTab, this.maleParent, "Male");
	}

	private void testUpdateParentTabForUnsavedChanges(ParentTabComponent parentTab, Table parentTable, String parentType) {
		Mockito.doNothing().when(parentTab).setHasUnsavedChanges(true);

		// test method
		this.makeCrossesParentsComponent.updateParentTabForUnsavedChanges(parentTable);

		try {
			Mockito.verify(parentTab, Mockito.times(1)).setHasUnsavedChanges(true);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting that the " + parentType + "ParentTab's Save List option is enabled but didn't.");
		}
	}

	@Test
	public void testClearSeedReservationValues() {
		GermplasmListEntry entryObject = this.addItemToParentTable(1, this.femaleParent);
		this.makeCrossesParentsComponent.clearSeedReservationValues(this.femaleParent);
		String actualValue =
				(String) this.femaleParent.getItem(entryObject).getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue();
		Assert.assertEquals("Expecting that the value is set to '-' but didn't.", STRING_DASH, actualValue);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAssignEntryNumber() {
		this.makeCrossesParentsComponent.assignEntryNumber(this.femaleParent);

		List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) this.femaleParent.getItemIds());

		int expectedEntryID = 1;
		for (GermplasmListEntry entry : itemIds) {
			Item item = this.femaleParent.getItem(entry);
			int actualEntryID = (int) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			Assert.assertEquals("Expecting that the id is set to " + expectedEntryID + " but set to " + actualEntryID + " instead.",
					expectedEntryID, actualEntryID);
			expectedEntryID++;

		}
	}

	@Test
	public void testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTableToFemaleTable() {
		this.testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTable(this.femaleParent, "Female");
	}

	@Test
	public void testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTableToMaleTable() {
		this.testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTable(this.maleParent, "Male");
	}

	private void testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTable(Table parentTable, String parentType) {
		this.sourceTable.select(1);
		this.sourceTable.select(2);

		int beforeSize = parentTable.size();
		this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(this.sourceTable, parentTable, 1);
		int afterSize = parentTable.size();

		Assert.assertEquals("Expecting that the size of the " + parentType + "Table is increased by 2 but didn't.", beforeSize + 2,
				afterSize);
	}

	@Test
	public void testDropToFemaleOrMaleTable_AddingAllEntriesOfSourceTableToFemaleTable() {
		this.testDropToFemaleOrMaleTable_AddingAllEntriesOfSourceTable(this.femaleParentTab, this.femaleParent, "Female");
	}

	@Test
	public void testDropToFemaleOrMaleTable_AddingAllEntriesOfSourceTableToMaleTable() {
		this.testDropToFemaleOrMaleTable_AddingAllEntriesOfSourceTable(this.maleParentTab, this.maleParent, "Male");
	}

	private void testDropToFemaleOrMaleTable_AddingAllEntriesOfSourceTable(ParentTabComponent parentTab, Table parentTable,
			String parentType) {
		parentTable.removeAllItems();

		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
			this.sourceTable.select(i);
		}

		this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(this.sourceTable, parentTable, 1);

		Assert.assertEquals("Expecting that the entries of sourceTable has the same number of entries of " + parentType + " table",
				this.sourceTable.size(), parentTable.size());
		Assert.assertFalse("Expecting that the Save List option in " + parentType + "tab is disabled but didn't. ", parentTab
				.getSaveActionMenu().isEnabled());
	}

	@Test
	public void testUpdateCrossesSeedSource_FemaleParent() {
		this.testUpdateCrossesSeedSource(this.femaleParentTab, "Female");
	}

	@Test
	public void testUpdateCrossesSeedSource_MaleParent() {
		this.testUpdateCrossesSeedSource(this.maleParentTab, "Male");
	}

	private void testUpdateCrossesSeedSource(ParentTabComponent parentTab, String parentType) {
		Mockito.doReturn(parentType + " Parent List").when(parentTab).getListNameForCrosses();
		Mockito.doReturn(this.germplasmList).when(parentTab).getGermplasmList();
		this.makeCrossesParentsComponent.updateCrossesSeedSource(parentTab, this.germplasmList);

		try {
			Mockito.verify(parentTab, Mockito.times(1)).setListNameForCrosses(this.germplasmList.getName());
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting that the list name for crosses " + parentType
					+ "ParentTab is set to name of the germplasm list but didn't.");
		}
	}

	@Test
	public void testAddListToMaleTable() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager).getGermplasmListById(GERMPLASM_LIST_ID);
		Mockito.doReturn(ListInventoryTableUtil.createGermplasmListDataWithInventoryDetails()).when(this.inventoryDataManager)
				.getLotCountsForList(GERMPLASM_LIST_ID, 0, Integer.MAX_VALUE);

		int beforeSize = this.maleParent.size();
		this.makeCrossesParentsComponent.addListToMaleTable(GERMPLASM_LIST_ID);
		int afterSize = this.maleParent.size();

		Assert.assertEquals("Expecting that all entries of source germplasm list are added to male table but didn't.", beforeSize
				+ NO_OF_ENTRIES, afterSize);
	}

	@Test
	public void testAddListToFemaleTable() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager).getGermplasmListById(GERMPLASM_LIST_ID);
		Mockito.doReturn(ListInventoryTableUtil.createGermplasmListDataWithInventoryDetails()).when(this.inventoryDataManager)
				.getLotCountsForList(GERMPLASM_LIST_ID, 0, Integer.MAX_VALUE);

		int beforeSize = this.femaleParent.size();
		this.makeCrossesParentsComponent.addListToFemaleTable(GERMPLASM_LIST_ID);
		int afterSize = this.femaleParent.size();

		Assert.assertEquals("Expecting that all entries of source germplasm list are added to female table but didn't.", beforeSize
				+ NO_OF_ENTRIES, afterSize);
	}

	private GermplasmListEntry addItemToParentTable(int id, Table parentTable) {
		GermplasmListEntry entryObject = new GermplasmListEntry(id, id, id, "Designation", "List Name: " + id);
		Item newItem = parentTable.addItem(entryObject);
		newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(STOCK_ID);
		return entryObject;
	}
}

package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.runners.MockitoJUnitRunner;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

@Ignore("test commented until finish the ticket BMS-3785")
@RunWith(MockitoJUnitRunner.class)
public class MakeCrossesParentsComponentTest {

	private static final int GERMPLASM_LIST_ID = 1;

	private static final int NO_OF_ENTRIES = 5;

	private static final String STOCK_ID = "STOCKID:";
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
	private final MakeCrossesParentsComponent makeCrossesParentsComponent = new MakeCrossesParentsComponent(this.makeCrossesMain);

	private GermplasmList germplasmList;
	private Table femaleParent;
	private Table maleParent;
	private Table sourceTable;
	private TabSheet listDetailsTabSheet;
	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;

	@Before
	public void setUp() {
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
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

		this.germplasmList = this.germplasmListTestDataInitializer.createGermplasmList(MakeCrossesParentsComponentTest.GERMPLASM_LIST_ID);
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

	private void createContextMenuOnParentTab(final ParentTabComponent parentTab) {
		final ContextMenu menu = new ContextMenu();
		final ContextMenuItem saveActionMenu = menu.addItem("Save Action Menu");
		Mockito.doReturn(saveActionMenu).when(parentTab).getSaveActionMenu();
	}

	private Table createSourceTable() {
		final Table sourceTable = new Table();
		sourceTable.setSelectable(true);
		sourceTable.setMultiSelect(true);

		sourceTable.addContainerProperty(MakeCrossesParentsComponentTest.CHECKBOX_COLUMN_ID, CheckBox.class, null);
		sourceTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		sourceTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		sourceTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		sourceTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		sourceTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, new Label(""));
		sourceTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		sourceTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		sourceTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		sourceTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		sourceTable.setColumnHeader(MakeCrossesParentsComponentTest.CHECKBOX_COLUMN_ID, this.messageSource.getMessage(Message.CHECK_ICON));
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
		for (int i = 1; i <= MakeCrossesParentsComponentTest.NO_OF_ENTRIES; i++) {
			final Item newItem = sourceTable.getContainerDataSource().addItem(i);
			newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(i);

			final Button desigButton = new Button();
			desigButton.setCaption("Designation");
			newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);

			final String availInv = String.valueOf(i);
			final InventoryLinkButtonClickListener inventoryLinkButtonClickListener =
					new InventoryLinkButtonClickListener(this.makeCrossesParentsComponent, this.germplasmList.getId(), i, i);
			final Button inventoryButton = new Button(availInv, inventoryLinkButtonClickListener);
			inventoryButton.setData(inventoryLinkButtonClickListener);
			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
			inventoryButton.setDescription(MakeCrossesParentsComponentTest.CLICK_TO_VIEW_INVENTORY_DETAILS);

			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(MakeCrossesParentsComponentTest.STRING_DASH);

			final Button gidButton = new Button();
			gidButton.setCaption(String.valueOf(i));
			newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);

			newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(MakeCrossesParentsComponentTest.STOCK_ID + i);
		}

		return sourceTable;
	}

	private Table createParentTable() {
		final Table parentTable = new Table();

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
		for (int i = 1; i <= MakeCrossesParentsComponentTest.NO_OF_ENTRIES; i++) {
			this.addItemToParentTable(i, parentTable);
		}
		return parentTable;
	}

	@Test
	public void testUpdateMaleParentList() {
		this.makeCrossesParentsComponent.updateMaleParentList(this.germplasmList);
		try {
			Mockito.verify(this.maleParentTab, Mockito.atLeast(1)).setGermplasmList(this.germplasmList);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting the germplasm list in male parent tab is set but didn't");
		}
	}

	@Test
	public void testUpdateFemaleParentList() {
		this.makeCrossesParentsComponent.updateFemaleParentList(this.germplasmList);
		try {
			Mockito.verify(this.femaleParentTab, Mockito.atLeast(1)).setGermplasmList(this.germplasmList);
		} catch (final TooLittleActualInvocations e) {
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

	private void testUpdateParentTabForUnsavedChanges(final ParentTabComponent parentTab, final Table parentTable,
			final String parentType) {
		Mockito.doNothing().when(parentTab).setHasUnsavedChanges(true);

		// test method
		this.makeCrossesParentsComponent.updateParentTabForUnsavedChanges(parentTable);

		try {
			Mockito.verify(parentTab, Mockito.times(1)).setHasUnsavedChanges(true);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the " + parentType + "ParentTab's Save List option is enabled but didn't.");
		}
	}

	@Test
	public void testClearSeedReservationValues() {
		final GermplasmListEntry entryObject = this.addItemToParentTable(1, this.femaleParent);
		this.makeCrossesParentsComponent.clearSeedReservationValues(this.femaleParent);
		final String actualValue =
				(String) this.femaleParent.getItem(entryObject).getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue();
		Assert.assertEquals("Expecting that the value is set to '-' but didn't.", MakeCrossesParentsComponentTest.STRING_DASH, actualValue);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAssignEntryNumber() {
		this.makeCrossesParentsComponent.assignEntryNumber(this.femaleParent);

		final List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) this.femaleParent.getItemIds());

		int expectedEntryID = 1;
		for (final GermplasmListEntry entry : itemIds) {
			final Item item = this.femaleParent.getItem(entry);
			final int actualEntryID = (int) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			Assert.assertEquals("Expecting that the id is set to " + expectedEntryID + " but set to " + actualEntryID + " instead.",
					expectedEntryID, actualEntryID);
			expectedEntryID++;

		}
	}

	@Test
	public void testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTableToFemaleTable() {
		this.testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTable(this.femaleParent, "Female");
		Item targetLastItem = this.femaleParent.getItem(this.femaleParent.lastItemId());

		Assert.assertEquals(7, this.femaleParent.size());

		Button designation = (Button) targetLastItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue();
		Assert.assertEquals("Designation", designation.getCaption());

		Button available = (Button) targetLastItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).getValue();
		Assert.assertEquals("2", available.getCaption().toString());

		Assert.assertEquals(MakeCrossesParentsComponentTest.STRING_DASH,
				targetLastItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).getValue());

		Label stockID = (Label) targetLastItem.getItemProperty(ColumnLabels.STOCKID.getName()).getValue();
		Assert.assertEquals("STOCKID:2", stockID.getValue().toString());

	}

	@Test
	public void testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTableToMaleTable() {
		this.testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTable(this.maleParent, "Male");
	}

	private void testDropToFemaleOrMaleTable_AddingPartialEntriesOfSourceTable(final Table parentTable, final String parentType) {
		this.sourceTable.select(1);
		this.sourceTable.select(2);

		final int beforeSize = parentTable.size();
		this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(this.sourceTable, parentTable, 1);
		final int afterSize = parentTable.size();

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

	private void testDropToFemaleOrMaleTable_AddingAllEntriesOfSourceTable(final ParentTabComponent parentTab, final Table parentTable,
			final String parentType) {
		parentTable.removeAllItems();

		for (int i = 1; i <= MakeCrossesParentsComponentTest.NO_OF_ENTRIES; i++) {
			this.sourceTable.select(i);
		}

		this.makeCrossesParentsComponent.dropToFemaleOrMaleTable(this.sourceTable, parentTable, 1);

		Assert.assertEquals("Expecting that the entries of sourceTable has the same number of entries of " + parentType + " table",
				this.sourceTable.size(), parentTable.size());
		Assert.assertFalse("Expecting that the Save List option in " + parentType + "tab is disabled but didn't. ",
				parentTab.getSaveActionMenu().isEnabled());
	}

	@Test
	public void testUpdateCrossesSeedSource_FemaleParent() {
		this.testUpdateCrossesSeedSource(this.femaleParentTab, "Female");
	}

	@Test
	public void testUpdateCrossesSeedSource_MaleParent() {
		this.testUpdateCrossesSeedSource(this.maleParentTab, "Male");
	}

	private void testUpdateCrossesSeedSource(final ParentTabComponent parentTab, final String parentType) {
		Mockito.doReturn(parentType + " Parent List").when(parentTab).getListNameForCrosses();
		Mockito.doReturn(this.germplasmList).when(parentTab).getGermplasmList();
		this.makeCrossesParentsComponent.updateCrossesSeedSource(parentTab, this.germplasmList);

		try {
			Mockito.verify(parentTab, Mockito.times(1)).setListNameForCrosses(this.germplasmList.getName());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the list name for crosses " + parentType
					+ "ParentTab is set to name of the germplasm list but didn't.");
		}
	}

	@Test
	public void testAddListToMaleTable() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
				.getGermplasmListById(MakeCrossesParentsComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails()).when(this.inventoryDataManager)
				.getLotCountsForList(MakeCrossesParentsComponentTest.GERMPLASM_LIST_ID, 0, Integer.MAX_VALUE);

		final int beforeSize = this.maleParent.size();
		this.makeCrossesParentsComponent.addListToMaleTable(MakeCrossesParentsComponentTest.GERMPLASM_LIST_ID);
		final int afterSize = this.maleParent.size();

		Assert.assertEquals("Expecting that all entries of source germplasm list are added to male table but didn't.",
				beforeSize + MakeCrossesParentsComponentTest.NO_OF_ENTRIES, afterSize);
	}

	@Test
	public void testAddListToFemaleTable() {
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
				.getGermplasmListById(MakeCrossesParentsComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails()).when(this.inventoryDataManager)
				.getLotCountsForList(MakeCrossesParentsComponentTest.GERMPLASM_LIST_ID, 0, Integer.MAX_VALUE);

		final int beforeSize = this.femaleParent.size();
		this.makeCrossesParentsComponent.addListToFemaleTable(MakeCrossesParentsComponentTest.GERMPLASM_LIST_ID);
		final int afterSize = this.femaleParent.size();

		Assert.assertEquals("Expecting that all entries of source germplasm list are added to female table but didn't.",
				beforeSize + MakeCrossesParentsComponentTest.NO_OF_ENTRIES, afterSize);
	}

	private GermplasmListEntry addItemToParentTable(final int id, final Table parentTable) {
		final GermplasmListEntry entryObject = new GermplasmListEntry(id, id, id, "Designation", "List Name: " + id);
		final Item newItem = parentTable.addItem(entryObject);
		newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(MakeCrossesParentsComponentTest.STOCK_ID);
		return entryObject;
	}
}

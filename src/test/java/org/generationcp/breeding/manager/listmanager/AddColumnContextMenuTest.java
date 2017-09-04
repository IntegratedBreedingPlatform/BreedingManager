
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Table;

import junit.framework.Assert;

public class AddColumnContextMenuTest {

	private static final String ADD_COLUMN = "Add Column";

	private static final String FILL_WITH_ATTRIBUTE = "Fill with Attribute";

	private static final String[] STANDARD_COLUMNS =
			{ColumnLabels.GID.getName(), ColumnLabels.DESIGNATION.getName(), ColumnLabels.SEED_SOURCE.getName(),
					ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.GROUP_ID.getName(), ColumnLabels.STOCKID.getName()};

	private static final List<String> ADDED_COLUMNS = Arrays.asList(ColumnLabels.PREFERRED_NAME.getName(),
			ColumnLabels.PREFERRED_ID.getName(), ColumnLabels.GERMPLASM_DATE.getName(), ColumnLabels.GERMPLASM_LOCATION.getName(),
			ColumnLabels.BREEDING_METHOD_NAME.getName(), ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
			ColumnLabels.BREEDING_METHOD_NUMBER.getName(), ColumnLabels.BREEDING_METHOD_GROUP.getName(),
			ColumnLabels.CROSS_FEMALE_GID.getName(), ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
			ColumnLabels.CROSS_MALE_GID.getName(), ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private AddColumnSource addColumnSource;

	@Mock
	private Table table;

	@Spy
	private ContextMenu sourceMenu;

	private ContextMenuItem sourceSubMenuItem;
	private AddColumnContextMenu addColumnMenu;
	
	@Mock
	private ContextMenuItem menuFillWithPreferredId;
	@Mock
	private ContextMenuItem menuFillWithPreferredName;
	@Mock
	private ContextMenuItem menuFillWithGermplasmDate;
	@Mock
	private ContextMenuItem menuFillWithLocations;
	@Mock
	private ContextMenuItem menuFillWithMethodInfo;
	@Mock
	private ContextMenuItem menuFillWithMethodName;
	@Mock
	private ContextMenuItem menuFillWithMethodAbbrev;
	@Mock
	private ContextMenuItem menuFillWithMethodNumber;
	@Mock
	private ContextMenuItem menuFillWithMethodGroup;
	@Mock
	private ContextMenuItem menuFillWithCrossFemaleInfo;
	@Mock
	private ContextMenuItem menuFillWithCrossFemaleGID;
	@Mock
	private ContextMenuItem menuFillWithCrossFemalePrefName;
	@Mock
	private ContextMenuItem menuFillWithCrossMaleInfo;
	@Mock
	private ContextMenuItem menuFillWithCrossMaleGID;
	@Mock
	private ContextMenuItem menuFillWithCrossMalePrefName;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		Mockito.doReturn(AddColumnContextMenuTest.ADD_COLUMN).when(this.messageSource).getMessage(Message.ADD_COLUMN);
		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey());
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey());
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey());
		Mockito.doReturn(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey());
		Mockito.doReturn(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey());
		Mockito.doReturn(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey());
		Mockito.doReturn(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
	}

	@Test
	public void testSetupContextMenuWithNullSourceSubMenuItem() {
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);

		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		Assert.assertNotNull(addColumnMenuItem);
		Mockito.verify(this.sourceMenu).addItem(AddColumnContextMenuTest.ADD_COLUMN);
		
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_MALE_INFO.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
	}

	@Test
	public void testSetupContextMenuWithSourceSubMenuItem() {
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.sourceSubMenuItem = this.sourceMenu.addItem("List Editing Options");
		final ContextMenuItem spySubMenuItem = Mockito.spy(this.sourceSubMenuItem);
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, spySubMenuItem, this.messageSource);

		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		Assert.assertNotNull(addColumnMenuItem);
		Mockito.verify(this.sourceMenu, Mockito.never()).addItem(AddColumnContextMenuTest.ADD_COLUMN);
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.ADD_COLUMN);
	}

	@Test
	public void testSetupSubMenuItems() {
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithPreferredId() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_PREFERRED_ID)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithPreferredName() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_PREFERRED_NAME)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithGermplasmDate() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_GERMPLASM_DATE)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithGermplasmLocation() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_LOCATION)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithBreedingMethodInfo() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithCrossFemaleInfo() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithCrossMaleInfo() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_CROSS_MALE_INFO)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithAttribute() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_ATTRIBUTE)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		final ContextMenuItem addColumnMenuItem = this.addColumnMenu.getAddColumnItem();
		final ContextMenuItem spySubMenuItem = Mockito.spy(addColumnMenuItem);

		this.addColumnMenu.setAddColumnItem(spySubMenuItem);
		this.addColumnMenu.setupSubMenuItems();

		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(spySubMenuItem).addItem(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name());
		Mockito.verify(spySubMenuItem, Mockito.never()).addItem(AddColumnContextMenuTest.FILL_WITH_ATTRIBUTE);
	}

	@Test
	public void testSourceHadAddedColumnWhenNoAddedColumns() {
		final boolean hasAddedColumn = AddColumnContextMenu.sourceHadAddedColumn(AddColumnContextMenuTest.STANDARD_COLUMNS);
		Assert.assertFalse(hasAddedColumn);
	}

	@Test
	public void testSourceHadAddedColumnWhenThereAreAddedColumns() {
		final List<String> columns = new ArrayList<>(Arrays.asList(AddColumnContextMenuTest.STANDARD_COLUMNS));
		columns.add(ColumnLabels.PREFERRED_NAME.getName());
		columns.add(ColumnLabels.GERMPLASM_LOCATION.getName());

		final boolean hasAddedColumn = AddColumnContextMenu.sourceHadAddedColumn(columns.toArray());
		Assert.assertTrue(hasAddedColumn);
	}

	@Test
	public void testRefreshMenuWhenNoColumnsAdded() {
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		this.setupMenuItemMocks();
		Mockito.doReturn(Arrays.asList(AddColumnContextMenuTest.STANDARD_COLUMNS)).when(this.table).getContainerPropertyIds();

		this.addColumnMenu.refreshAddColumnMenu(this.table);
		this.verifyMenuItemsAreEnabled(true);
	}

	@Test
	public void testRefreshMenuWhenColumnsAdded() {
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);
		this.setupMenuItemMocks();

		final List<String> columns = new ArrayList<>(Arrays.asList(AddColumnContextMenuTest.STANDARD_COLUMNS));
		columns.addAll(AddColumnContextMenuTest.ADDED_COLUMNS);
		Mockito.doReturn(columns).when(this.table).getContainerPropertyIds();

		this.addColumnMenu.refreshAddColumnMenu(this.table);
		this.verifyMenuItemsAreEnabled(false);
	}

	protected void verifyMenuItemsAreEnabled(final boolean isEnabled) {
		Mockito.verify(this.menuFillWithPreferredId).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithPreferredName).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithGermplasmDate).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithLocations).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithMethodInfo).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithMethodName).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithMethodAbbrev).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithMethodNumber).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithMethodGroup).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithCrossFemaleInfo).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithCrossFemaleGID).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithCrossFemalePrefName).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithCrossMaleInfo).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithCrossMaleGID).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithCrossMalePrefName).setEnabled(isEnabled);
	}

	protected void setupMenuItemMocks() {
		this.addColumnMenu.setMenuFillWithPreferredId(this.menuFillWithPreferredId);
		this.addColumnMenu.setMenuFillWithPreferredName(this.menuFillWithPreferredName);
		this.addColumnMenu.setMenuFillWithGermplasmDate(this.menuFillWithGermplasmDate);
		this.addColumnMenu.setMenuFillWithLocations(this.menuFillWithLocations);
		this.addColumnMenu.setMenuFillWithMethodInfo(this.menuFillWithMethodInfo);
		this.addColumnMenu.setMenuFillWithMethodName(this.menuFillWithMethodName);
		this.addColumnMenu.setMenuFillWithMethodAbbrev(this.menuFillWithMethodAbbrev);
		this.addColumnMenu.setMenuFillWithMethodNumber(this.menuFillWithMethodNumber);
		this.addColumnMenu.setMenuFillWithMethodGroup(this.menuFillWithMethodGroup);
		this.addColumnMenu.setMenuFillWithCrossFemaleInfo(this.menuFillWithCrossFemaleInfo);
		this.addColumnMenu.setMenuFillWithCrossFemaleGID(this.menuFillWithCrossFemaleGID);
		this.addColumnMenu.setMenuFillWithCrossFemalePrefName(this.menuFillWithCrossFemalePrefName);
		this.addColumnMenu.setMenuFillWithCrossMaleInfo(this.menuFillWithCrossMaleInfo);
		this.addColumnMenu.setMenuFillWithCrossMaleGID(this.menuFillWithCrossMaleGID);
		this.addColumnMenu.setMenuFillWithCrossMalePrefName(this.menuFillWithCrossMalePrefName);
	}

}


package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

import junit.framework.Assert;

public class AddColumnContextMenuTest {

	private static final String ADD_COLUMN = "Add Column";

	private static final String FILL_WITH_ATTRIBUTE = "Fill with Attribute";
	
	private static final String FILL_WITH_GERMPLASM_NAME = "Fill with Germplasm Name";

	private static final String[] STANDARD_COLUMNS =
			{ColumnLabels.GID.getName(), ColumnLabels.DESIGNATION.getName(), ColumnLabels.SEED_SOURCE.getName(),
					ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.GROUP_ID.getName(), ColumnLabels.STOCKID.getName()};

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

	@Mock
	private ContextMenuItem menuFillWithGroupSourceInfo;
	@Mock
	private ContextMenuItem menuFillWithGroupSourceGID;
	@Mock
	private ContextMenuItem menuFillWithGroupSourcePreferredName;
	@Mock
	private ContextMenuItem menuFillWithImmediateSourceGID;
	@Mock
	private ContextMenuItem menuFillWithImmediateSourceInfo;
	@Mock
	private ContextMenuItem menuFillWithImmediateSourcePreferredName;

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
		Mockito.doReturn(AddColumnContextMenuTest.FILL_WITH_GERMPLASM_NAME).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GERMPLASM_NAME.getMessageKey());

		Mockito.doReturn(FillWithOption.FILL_WITH_GROUP_SOURCE_INFO.name()).when(this.messageSource)
			.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_GID.getName()).when(this.messageSource)
			.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).when(this.messageSource)
			.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_PREFERRED_NAME.getMessageKey());

		Mockito.doReturn(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_INFO.name()).when(this.messageSource)
			.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).when(this.messageSource)
			.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).when(this.messageSource)
			.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_PREFERRED_NAME.getMessageKey());
		
		this.addColumnMenu = new AddColumnContextMenu(this.addColumnSource, this.sourceMenu, null, this.messageSource);

	}

	@Test
	public void testSetupContextMenuWithNullSourceSubMenuItem() {
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
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(FillWithOption.FILL_WITH_GROUP_SOURCE_INFO.name());
		Mockito.verify(spySubMenuItem).addItem(AddColumnContextMenuTest.FILL_WITH_GERMPLASM_NAME);
	}

	@Test
	public void testSetupSubMenuItemsExcludeFillWithPreferredId() {
		Mockito.doReturn(Arrays.asList(FillWithOption.FILL_WITH_PREFERRED_ID)).when(this.addColumnSource).getColumnsToExclude();
		// Need to spy "Add Column" item to verify if sub menu items were added. Cannot use mock because of NPE when adding items to submenu
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
	public void testRefreshMenuWhenNoColumnsAdded() {
		this.setupMenuItemMocks();
		Mockito.doReturn(Arrays.asList(AddColumnContextMenuTest.STANDARD_COLUMNS)).when(this.table).getContainerPropertyIds();

		this.addColumnMenu.refreshAddColumnMenu(this.table);
		this.verifyMenuItemsAreEnabled(true);
	}

	@Test
	public void testRefreshMenuWhenColumnsAdded() {
		this.setupMenuItemMocks();

		final List<String> columns = new ArrayList<>(Arrays.asList(AddColumnContextMenuTest.STANDARD_COLUMNS));
		columns.addAll(ColumnLabels.getAddableGermplasmColumns());
		Mockito.doReturn(columns).when(this.table).getContainerPropertyIds();

		this.addColumnMenu.refreshAddColumnMenu(this.table);
		this.verifyMenuItemsAreEnabled(false);
	}
	
	@Test
	public void testGetListDataCollectionFromTableWhenNoAddedColumn() {
		Mockito.doReturn(Arrays.asList(STANDARD_COLUMNS)).when(this.table).getContainerPropertyIds();
		final List<Integer> tableEntryIds = Arrays.asList(1, 2, 3);
		Mockito.doReturn(tableEntryIds).when(this.table).getItemIds();

		final List<ListDataInfo> listDataInfo = this.addColumnMenu.getListDataCollectionFromTable(this.table, new ArrayList<String>());
		Assert.assertEquals(tableEntryIds.size(), listDataInfo.size());
		for (final ListDataInfo info : listDataInfo) {
			Assert.assertTrue(tableEntryIds.contains(info.getListDataId()));
			Assert.assertTrue(info.getColumns().isEmpty());
		}
	}
	
	@Test
	public void testGetListDataCollectionFromTableWithAddedColumns() {
		final String noteAttributeField = "NOTE_ATTRIBUTE";
		final String notesPrefix = "NOTES ";
		final String preferredNamePrefix = "PREFERRED NAME ";
		final List<String> columns = new ArrayList<>(Arrays.asList(STANDARD_COLUMNS));
		columns.add(ColumnLabels.PREFERRED_NAME.getName());
		columns.add(noteAttributeField);
		Mockito.doReturn(columns).when(this.table).getContainerPropertyIds();
		final List<Integer> tableEntryIds = Arrays.asList(1, 2, 3);
		Mockito.doReturn(tableEntryIds).when(this.table).getItemIds();
		
		for (final Integer id: tableEntryIds) {
			final Item item = Mockito.mock(Item.class);
			Mockito.doReturn(item).when(this.table).getItem(id);
			final Property property1 = Mockito.mock(Property.class);
			Mockito.doReturn(property1).when(item).getItemProperty(ColumnLabels.PREFERRED_NAME.getName());
			Mockito.doReturn(new String(preferredNamePrefix + id)).when(property1).getValue();
			final Property property2 = Mockito.mock(Property.class);
			Mockito.doReturn(property2).when(item).getItemProperty(noteAttributeField);
			Mockito.doReturn(new String(notesPrefix + id)).when(property2).getValue();
		}
		
		final List<ListDataInfo> listDataInfo = this.addColumnMenu.getListDataCollectionFromTable(this.table, Arrays.asList(noteAttributeField));
		Assert.assertEquals(tableEntryIds.size(), listDataInfo.size());
		for (final ListDataInfo info : listDataInfo) {
			final Integer id = info.getListDataId();
			Assert.assertTrue(tableEntryIds.contains(id));
			Assert.assertEquals(2, info.getColumns().size());
			Assert.assertEquals(preferredNamePrefix + id, info.getColumns().get(0).getValue());
			Assert.assertEquals(notesPrefix + id, info.getColumns().get(1).getValue());
		}
	}
	
	@Test
	public void testGetAddedColumnsWhenNoAddedColumn() {
		Mockito.doReturn(Arrays.asList(STANDARD_COLUMNS)).when(this.table).getContainerPropertyIds();
		final List<String> addedColumns = this.addColumnMenu.getAddedColumns(this.table, new ArrayList<String>());
		Assert.assertTrue(addedColumns.isEmpty());
	}
	
	@Test
	public void testGetAddedColumnsWhenThereAreAddedColumns() {
		final String noteAttributeField = "NOTE_ATTRIBUTE";
		final List<String> columns = new ArrayList<>(Arrays.asList(STANDARD_COLUMNS));
		columns.add(ColumnLabels.PREFERRED_NAME.getName());
		columns.add(noteAttributeField);
		Mockito.doReturn(columns).when(this.table).getContainerPropertyIds();
		final List<String> addedColumns = this.addColumnMenu.getAddedColumns(this.table, Arrays.asList(noteAttributeField));
		Assert.assertEquals(Arrays.asList(ColumnLabels.PREFERRED_NAME.getName(), noteAttributeField), addedColumns);
	}
	
	@Test
	public void testHasAddedColumnsWhenNoAddedColumn() {
		Mockito.doReturn(Arrays.asList(STANDARD_COLUMNS)).when(this.table).getContainerPropertyIds();
		Assert.assertFalse(this.addColumnMenu.hasAddedColumn(this.table, new ArrayList<String>()));
	}
	
	@Test
	public void testHasAddedColumnsWhenThereIsPredefinedColumnAdded() {
		final List<String> columns = new ArrayList<>(Arrays.asList(STANDARD_COLUMNS));
		columns.add(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.doReturn(columns).when(this.table).getContainerPropertyIds();
		Assert.assertTrue(this.addColumnMenu.hasAddedColumn(this.table, new ArrayList<String>()));
	}
	
	@Test
	public void testHasAddedColumnsWhenThereIsAttributeColumnAdded() {
		final String noteAttributeField = "NOTE_ATTRIBUTE";
		final List<String> columns = new ArrayList<>(Arrays.asList(STANDARD_COLUMNS));
		columns.add(noteAttributeField);
		Mockito.doReturn(columns).when(this.table).getContainerPropertyIds();
		Assert.assertTrue(this.addColumnMenu.hasAddedColumn(this.table, Arrays.asList(noteAttributeField)));
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

		Mockito.verify(this.menuFillWithGroupSourceInfo).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithGroupSourceGID).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithGroupSourcePreferredName).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithImmediateSourceInfo).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithImmediateSourceGID).setEnabled(isEnabled);
		Mockito.verify(this.menuFillWithImmediateSourcePreferredName).setEnabled(isEnabled);
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

		this.addColumnMenu.setMenuFillWithGroupSourceInfo(this.menuFillWithGroupSourceInfo);
		this.addColumnMenu.setMenuFillWithGroupSourceGID(this.menuFillWithGroupSourceGID);
		this.addColumnMenu.setMenuFillWithGroupSourcePreferredName(this.menuFillWithGroupSourcePreferredName);
		this.addColumnMenu.setMenuFillWithImmediateSourceInfo(this.menuFillWithImmediateSourceInfo);
		this.addColumnMenu.setMenuFillWithImmediateSourceGID(this.menuFillWithImmediateSourceGID);
		this.addColumnMenu.setMenuFillWithImmediateSourcePreferredName(this.menuFillWithImmediateSourcePreferredName);

	}

}

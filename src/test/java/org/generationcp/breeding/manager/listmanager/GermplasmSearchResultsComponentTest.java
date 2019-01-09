package org.generationcp.breeding.manager.listmanager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.containers.GermplasmQuery;
import org.generationcp.breeding.manager.customcomponent.PagedTableWithSelectAllLayout;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.vaadin.peter.contextmenu.ContextMenu;

import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmSearchResultsComponentTest {

	private static final String GERMPLASM_NAMES_WITH_MORE_THAN_20_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final String GERMPLASM_NAMES_WITH_20_CHARS = "ABCDEFGHIJKLMNOPQRST";

	@Mock
	private Window window;

	@Mock
	private Window parentWindow;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private PagedTableWithSelectAllLayout tableWithSelectAllLayout;

	@Mock
	private ListManagerMain listManagerMain;

	@Mock
	private PagedBreedingManagerTable pagedTable;

	@Mock
	private Indexed dataSource;

	@Captor
	private ArgumentCaptor<Object[]> captor;

	@InjectMocks
	private GermplasmSearchResultsComponent germplasmSearchResultsComponent;

	@Before
	public void setUp() {

		// menu item
		Mockito.doReturn("Add New Entry").when(this.messageSource).getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST);
		Mockito.doReturn("Select All").when(this.messageSource).getMessage(Message.SELECT_ALL);
		Mockito.doReturn("Total Result").when(this.messageSource).getMessage(Message.TOTAL_RESULTS);
		Mockito.doReturn("Total Selected Result").when(this.messageSource).getMessage(Message.SELECTED);
		Mockito.doReturn("Add Column").when(this.messageSource).getMessage(Message.ADD_COLUMN);
		Mockito.doReturn("DUMMY MESSAGE").when(this.messageSource).getMessage("VALIDATION_INTEGER_FORMAT");

		Mockito.doReturn(this.pagedTable).when(this.tableWithSelectAllLayout).getTable();
		Mockito.doReturn(this.dataSource).when(this.pagedTable).getContainerDataSource();

		Mockito.when(parentWindow.getWindow()).thenReturn(window);
		germplasmSearchResultsComponent.setParent(parentWindow);

	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testInitMatchingGermplasmTableVerifyTableSettings() {
		final PagedBreedingManagerTable actualTable = new PagedBreedingManagerTable(1, 20);
		Mockito.doReturn(actualTable).when(this.tableWithSelectAllLayout).getTable();
		
		this.germplasmSearchResultsComponent.initMatchingGermplasmTable();
		Assert.assertTrue(actualTable.isColumnCollapsingAllowed());
		Assert.assertTrue(actualTable.isImmediate());
		Assert.assertTrue(actualTable.isSelectable());
		Assert.assertTrue(actualTable.isMultiSelect());
		Assert.assertEquals(TableDragMode.ROW, actualTable.getDragMode());
		Assert.assertFalse(actualTable.isColumnReorderingAllowed());
		
		Assert.assertEquals(GermplasmSearchResultsComponent.MATCHING_GEMRPLASM_TABLE_DATA, actualTable.getData());
		final Object[] visibleColumns = {GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID, GermplasmSearchResultsComponent.NAMES,
				ColumnLabels.PARENTAGE.getName(), ColumnLabels.AVAILABLE_INVENTORY.getName(), ColumnLabels.TOTAL.getName(),
				ColumnLabels.STOCKID.getName(), ColumnLabels.GID.getName(), ColumnLabels.GROUP_ID.getName(),
				ColumnLabels.GERMPLASM_LOCATION.getName(), ColumnLabels.BREEDING_METHOD_NAME.getName()};
		Assert.assertEquals(visibleColumns, actualTable.getVisibleColumns());
	}

	@Test
	public void testInitMatchingGermplasmTableHeaderNameExistsFromOntology() throws MiddlewareQueryException {
		final PagedBreedingManagerTable actualTable = new PagedBreedingManagerTable(1, 20);
		Mockito.doReturn(actualTable).when(this.tableWithSelectAllLayout).getTable();

		Mockito.doReturn(this.createTerm("PARENTAGE")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		Mockito.doReturn(this.createTerm("GID")).when(this.ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		Mockito.doReturn(this.createTerm("STOCKID")).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(this.createTerm("LOCATIONS")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.GERMPLASM_LOCATION.getTermId().getId());
		Mockito.doReturn(this.createTerm("METHOD_NAME")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.BREEDING_METHOD_NAME.getTermId().getId());
		Mockito.doReturn(this.createTerm("AVAILABLE")).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());

		this.germplasmSearchResultsComponent.initMatchingGermplasmTable();

		final Table table = this.germplasmSearchResultsComponent.getMatchingGermplasmTableWithSelectAll().getTable();

		Assert.assertEquals(10, table.getColumnHeaders().length);

		Assert.assertEquals("Tag All Column", table.getColumnHeader(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID));
		Assert.assertEquals("NAMES", table.getColumnHeader(GermplasmSearchResultsComponent.NAMES));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("LOCATIONS", table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertEquals("METHOD_NAME", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		Assert.assertEquals("AVAILABLE", table.getColumnHeader(ColumnLabels.TOTAL.getName()));

	}

	@Test
	public void testInitMatchingGermplasmTableHeaderNameDoesntExistFromOntology() throws MiddlewareQueryException {
		final PagedBreedingManagerTable actualTable = new PagedBreedingManagerTable(1, 20);
		Mockito.doReturn(actualTable).when(this.tableWithSelectAllLayout).getTable();

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_RESERVATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.GERMPLASM_LOCATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.BREEDING_METHOD_NAME.getTermId().getId());

		this.germplasmSearchResultsComponent.initMatchingGermplasmTable();

		final Table table = this.germplasmSearchResultsComponent.getMatchingGermplasmTableWithSelectAll().getTable();

		Assert.assertEquals("Tag All Column", table.getColumnHeader(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID));
		Assert.assertEquals("NAMES", table.getColumnHeader(GermplasmSearchResultsComponent.NAMES));
		Assert.assertEquals(ColumnLabels.PARENTAGE.getName(), table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ColumnLabels.AVAILABLE_INVENTORY.getName(), table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ColumnLabels.SEED_RESERVATION.getName(), table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals(ColumnLabels.STOCKID.getName(), table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals(ColumnLabels.GID.getName(), table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ColumnLabels.GERMPLASM_LOCATION.getName(), table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertEquals(ColumnLabels.BREEDING_METHOD_NAME.getName(),
				table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));

	}

	@Test
	public void testGetShortenedNamesIfNameLengthIsAtLeast20() {
		final String shortenedNames = this.germplasmSearchResultsComponent
				.getShortenedNames(GermplasmSearchResultsComponentTest.GERMPLASM_NAMES_WITH_MORE_THAN_20_CHARS);

		Assert.assertEquals("Expecting to return string with only 20 characters with ellipsis(...) at the end.",
				GermplasmSearchResultsComponentTest.GERMPLASM_NAMES_WITH_MORE_THAN_20_CHARS.substring(0, 20).concat("..."), shortenedNames);
	}

	@Test
	public void testGetShortenedNamesIfNameLengthIsAtMost20() {
		final String shortenedNames =
				this.germplasmSearchResultsComponent.getShortenedNames(GermplasmSearchResultsComponentTest.GERMPLASM_NAMES_WITH_20_CHARS);
		Assert.assertEquals("Expecting to return the same name.", GermplasmSearchResultsComponentTest.GERMPLASM_NAMES_WITH_20_CHARS,
				shortenedNames);
	}

	@Test
	public void testApplyGermplasmResultsNoMatch() {
		// instantiate other needed components in the page
		this.createDummyComponents();

		final GermplasmSearchParameter searchParameter = new GermplasmSearchParameter("ABC", Operation.EQUAL);
		try {
			this.germplasmSearchResultsComponent.applyGermplasmResults(searchParameter);
			Assert.fail("Expecting BreedingManagerSearchException to be thrown for empty search but wasn't.");

		} catch (final BreedingManagerSearchException e) {
			Assert.assertEquals(Message.NO_SEARCH_RESULTS, e.getErrorMessage());
			// Verify key actions done, prior to throwing exception
			Mockito.verify(this.pagedTable, Mockito.times(1)).setCurrentPage(1);

			// Setting of visible columns set twice
			// 1) during table initialization and 2) in applyGermplasmResults method
			Mockito.verify(this.pagedTable, Mockito.times(2)).setVisibleColumns(this.captor.capture());

			// Check that GID_REF property is not visible
			Assert.assertEquals(10, this.captor.getValue().length);
			for (final Object property : this.captor.getValue()) {
				if (GermplasmQuery.GID_REF_PROPERTY.equals(property)) {
					Assert.fail("Expecting GID_REF column to be hidden on table but was not.");
				}
			}

			// Check that Select All checkboxes states were updated
			Mockito.verify(this.tableWithSelectAllLayout, Mockito.times(1)).updateSelectAllCheckboxes();

			// Expecting that 'Select All' and 'Add New Entry' context menu items are disabled if
			// there's no germplasm result matched
			Assert.assertFalse(this.germplasmSearchResultsComponent.getMenuSelectAll().isEnabled());
			Assert.assertFalse(this.germplasmSearchResultsComponent.getMenuAddNewEntry().isEnabled());
		}
	}

	@Test
	public void testApplyGermplasmResultsWithMatch() throws BreedingManagerSearchException {
		// instantiate other needed components in the page
		this.createDummyComponents();

		final GermplasmSearchParameter searchParameter = new GermplasmSearchParameter("ABC", Operation.EQUAL);
		Mockito.when(this.pagedTable.getItemIds()).thenReturn(this.createDummyItemIds());

		this.germplasmSearchResultsComponent.applyGermplasmResults(searchParameter);

		Mockito.verify(this.pagedTable, Mockito.times(1)).setCurrentPage(1);

		// Setting of visible columns set twice
		// 1) during table initialization and 2) in applyGermplasmResults method
		Mockito.verify(this.pagedTable, Mockito.times(2)).setVisibleColumns(this.captor.capture());

		// Check that GID_REF property is not visible
		Assert.assertEquals(10, this.captor.getValue().length);
		for (final Object property : this.captor.getValue()) {
			if (GermplasmQuery.GID_REF_PROPERTY.equals(property)) {
				Assert.fail("Expecting GID_REF column to be hidden on table but was not.");
			}
		}

		// Expecting that 'Select All' and 'Add New Entry' context menu items are enabled if
		// there are germplasm result matched.
		Assert.assertTrue(this.germplasmSearchResultsComponent.getMenuSelectAll().isEnabled());
		Assert.assertTrue(this.germplasmSearchResultsComponent.getMenuAddNewEntry().isEnabled());

	}

	@Test
	public void testCreateTotalMatchingGermplasmLabel() {

		final Label label = this.germplasmSearchResultsComponent.createTotalMatchingGermplasmLabel();
		Assert.assertEquals("", label.getValue());
		Assert.assertEquals("totalMatchingGermplasmLabel", label.getDebugId());
		Assert.assertEquals(Double.doubleToLongBits(120.0), Double.doubleToLongBits(label.getWidth()));
		Assert.assertEquals(Label.CONTENT_XHTML, label.getContentMode());

	}

	@Test
	public void testUpdateNoOfEntries() {

		this.createDummyComponents();
		this.germplasmSearchResultsComponent.updateNoOfEntries(100);
		Assert.assertEquals("Total Result:   <b>100</b>", this.germplasmSearchResultsComponent.getTotalMatchingGermplasmLabel().getValue());

	}

	@Test
	public void testTotalSelectedMatchingGermplasmLabel() {

		final Label label = this.germplasmSearchResultsComponent.createTotalSelectedMatchingGermplasmLabel();
		Assert.assertEquals("", label.getValue());
		Assert.assertEquals("totalSelectedMatchingGermplasmLabel", label.getDebugId());
		Assert.assertEquals(Double.doubleToLongBits(95.0), Double.doubleToLongBits(label.getWidth()));
		Assert.assertEquals(Label.CONTENT_XHTML, label.getContentMode());

	}

	@Test
	public void testUpdateNoOfSelectedEntries() {

		final Collection dummySelectedItems = new ArrayList();
		dummySelectedItems.add(new Object());

		Mockito.when(this.pagedTable.getValue()).thenReturn(dummySelectedItems);

		this.createDummyComponents();
		this.germplasmSearchResultsComponent.updateNoOfSelectedEntries();
		Assert.assertEquals("<i>Total Selected Result:   <b>1</b></i>",
				this.germplasmSearchResultsComponent.getTotalSelectedMatchingGermplasmLabel().getValue());

	}

	@Test
	public void testCreateActionMenu() {

		this.germplasmSearchResultsComponent.createActionMenu();

		// Check the following components if they are properly initialized
		Assert.assertEquals("actionButton", this.germplasmSearchResultsComponent.getActionButton().getDebugId());

		Assert.assertEquals("menu", this.germplasmSearchResultsComponent.getMenu().getDebugId());
		Assert.assertEquals(Double.doubleToLongBits(295.0),
				Double.doubleToLongBits(this.germplasmSearchResultsComponent.getMenu().getWidth()));

	}

	@Test
	public void testAddActionMenuItems() {

		final AddColumnContextMenu addColumnContextMenu = Mockito.mock(AddColumnContextMenu.class);

		this.germplasmSearchResultsComponent.createActionMenu();
		this.germplasmSearchResultsComponent.addActionMenuItems(addColumnContextMenu);

		// Check the following components if they are properly initialized
		Assert.assertEquals("Add New Entry", this.germplasmSearchResultsComponent.getMenuAddNewEntry().getName());
		Assert.assertEquals("Select All", this.germplasmSearchResultsComponent.getMenuSelectAll().getName());

		Assert.assertEquals(GermplasmSearchResultsComponent.TableRightClickHandler.class,
				this.germplasmSearchResultsComponent.getRightClickActionHandler().getClass());

		Assert.assertNotNull(this.germplasmSearchResultsComponent.getAddColumnContextMenu());

		Assert.assertFalse(this.germplasmSearchResultsComponent.getMenuAddNewEntry().isEnabled());
		Assert.assertFalse(this.germplasmSearchResultsComponent.getMenuSelectAll().isEnabled());
	}

	@Test
	public void testTableRightClickHandlerCopyToNewList() {

		final GermplasmSearchResultsComponent mockGermplasmSearchResultsComponent = Mockito.mock(GermplasmSearchResultsComponent.class);

		final GermplasmSearchResultsComponent.TableRightClickHandler rightClickHandler =
				new GermplasmSearchResultsComponent().new TableRightClickHandler(mockGermplasmSearchResultsComponent);

		rightClickHandler.handleAction(GermplasmSearchResultsComponent.ACTION_COPY_TO_NEW_LIST, null, null);

		Mockito.verify(mockGermplasmSearchResultsComponent, Mockito.times(1)).addSelectedEntriesToNewList();

	}

	@Test
	public void testTableRightClickHandlerSelectAll() {

		final GermplasmSearchResultsComponent mockGermplasmSearchResultsComponent = Mockito.mock(GermplasmSearchResultsComponent.class);
		final PagedTableWithSelectAllLayout mockPagedTableWithSelectAllLayout = Mockito.mock(PagedTableWithSelectAllLayout.class);

		Mockito.when(mockGermplasmSearchResultsComponent.getMatchingGermplasmTableWithSelectAll())
				.thenReturn(mockPagedTableWithSelectAllLayout);

		final GermplasmSearchResultsComponent.TableRightClickHandler rightClickHandler =
				new GermplasmSearchResultsComponent().new TableRightClickHandler(mockGermplasmSearchResultsComponent);

		rightClickHandler.handleAction(GermplasmSearchResultsComponent.ACTION_SELECT_ALL, null, null);

		Mockito.verify(mockPagedTableWithSelectAllLayout, Mockito.times(1)).selectAllEntriesOnCurrentPage();

	}

	@Test
	public void testAddSelectedEntriesToNewListWithSelectedItems() {

		final List<Integer> selectedItemIds = new ArrayList<>();
		final List<Integer> expectedGids = new ArrayList<>();

		final int firstItemId = 1001;
		final int secondItemId = 1002;
		final int firstItemGid = 1111;
		final int secondItemGid = 2222;

		selectedItemIds.add(firstItemId);
		selectedItemIds.add(secondItemId);
		expectedGids.add(firstItemGid);
		expectedGids.add(secondItemGid);

		Mockito.when(this.pagedTable.getValue()).thenReturn(selectedItemIds);

		final Property firstItemProperty = Mockito.mock(Property.class);
		Mockito.when(firstItemProperty.getValue()).thenReturn(firstItemGid);
		final Property secondItemProperty = Mockito.mock(Property.class);
		Mockito.when(secondItemProperty.getValue()).thenReturn(secondItemGid);

		final Item firstItem  = Mockito.mock(Item.class);
		Mockito.when(firstItem.getItemProperty(GermplasmQuery.GID_REF_PROPERTY)).thenReturn(firstItemProperty);
		final Item secondItem  = Mockito.mock(Item.class);
		Mockito.when(secondItem.getItemProperty(GermplasmQuery.GID_REF_PROPERTY)).thenReturn(secondItemProperty);

		Mockito.when(this.pagedTable.getItem(firstItemId)).thenReturn(firstItem);
		Mockito.when(this.pagedTable.getItem(secondItemId)).thenReturn(secondItem);

		this.germplasmSearchResultsComponent.addSelectedEntriesToNewList();

		Mockito.verify(this.listManagerMain).addPlantsToList(expectedGids);

	}

	@Test
	public void testAddSelectedEntriesToNewListNoSelectedItems() {

		final String expectedErrorMessage = "some error message";

		final List<Integer> selectedItemIds = new ArrayList<>();

		Mockito.when(this.messageSource.getMessage(Message.ERROR_GERMPLASM_MUST_BE_SELECTED)).thenReturn(expectedErrorMessage);
		Mockito.when(this.pagedTable.getValue()).thenReturn(selectedItemIds);

		this.germplasmSearchResultsComponent.addSelectedEntriesToNewList();

		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.ERROR_GERMPLASM_MUST_BE_SELECTED);
		Mockito.verify(this.window, Mockito.times(1)).showNotification(Mockito.any(Window.Notification.class));
		Mockito.verify(this.listManagerMain, Mockito.times(0)).addPlantsToList(ArgumentMatchers.<List<Integer>>any());
		Mockito.verify(this.pagedTable, Mockito.times(0)).getItem(Mockito.anyInt());

	}

	private void createDummyComponents() {
		this.germplasmSearchResultsComponent.initMatchingGermplasmTable();
		final Label totalEntriesLabel = new Label();
		this.germplasmSearchResultsComponent.setTotalEntriesLabel(totalEntriesLabel);
		this.germplasmSearchResultsComponent
				.setTotalSelectedEntriesLabel(this.germplasmSearchResultsComponent.createTotalSelectedMatchingGermplasmLabel());
		this.germplasmSearchResultsComponent
				.setTotalMatchingGermplasmLabel(this.germplasmSearchResultsComponent.createTotalMatchingGermplasmLabel());

		final ContextMenu.ContextMenuItem menuSelectAll = new ContextMenu().addItem("");
		menuSelectAll.setEnabled(false);
		this.germplasmSearchResultsComponent.setMenuSelectAll(menuSelectAll);
		final ContextMenu.ContextMenuItem menuAddNewEntry = new ContextMenu().addItem("");
		menuAddNewEntry.setEnabled(false);
		this.germplasmSearchResultsComponent.setMenuAddNewEntry(menuAddNewEntry);

	}

	private Term createTerm(final String name) {
		final Term term = new Term();
		term.setName(name);
		term.setId(0);
		return term;
	}

	private Collection createDummyItemIds() {
		Collection itemIds = new ArrayList();
		itemIds.add(new Object());
		itemIds.add(new Object());
		return itemIds;
	}

}

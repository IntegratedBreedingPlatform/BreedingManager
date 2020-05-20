
package org.generationcp.breeding.manager.listmanager;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.SortableButton;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListBuilderComponentTest {

	private static final String AVAIL_INV = "AVAIL_INV";
	private static final String TOTAL = "AVAILABLE";
	private static final String HASH = "#";
	private static final String CHECK = "CHECK";
	private static final String SEED_SOURCE = "SEED_SOURCE";
	private static final String CROSS = "CROSS";
	private static final String DESIG = "DESIG";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	private static final String GID = "GID";
	private static final String STOCKID = "STOCKID";
	private static final String CAPTION = "2";
	public static final int CURRENT_USER_ID = 1;
	public static final String INVENTORY_VIEW = "Inventory View";
	public static final String LIST_ENTRIES_VIEW = "List Entries View";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private TableWithSelectAllLayout tableWithSelectAllLayout;

	@Mock
	private BreedingManagerTable breedingManagerTable;

	@InjectMocks
	private ListBuilderComponent listBuilderComponent;

	@Mock
	private ListManagerMain listManagerMain;

	@Mock
	private BreedingManagerListDetailsComponent breedingManagerListDetailsComponent;

	@Mock
	private BuildNewListDropHandler dropHandler;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private SortableButton button;

	@Mock
	private GermplasmList currentlySavedGermplasmList;

	@Mock
	private ContextMenuItem menuDeleteSelectedEntries;

	@Mock
	private AddColumnContextMenu addColumnContextMenu;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private FillWith fillWith;

	@Mock
	private Item item;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private Label listEntriesLabel;

	@Mock
	private ContextMenuItem contextMenuItem;

	@Mock
	private ContextMenu contextMenu;

	@Mock
	private UserService userService;

	@Mock
	private ContextUtil contextUtil;


	private static final Integer TEST_GERMPLASM_LIST_ID = 111;
	private static final Integer TEST_GERMPLASM_NO_OF_ENTRIES = 5;
	private static final long LIST_ENTRIES_COUNT = 1;

	@Before
	public void setUp() {
		this.listBuilderComponent.setOntologyDataManager(this.ontologyDataManager);
		this.listBuilderComponent.setMessageSource(this.messageSource);
		this.listBuilderComponent.setTransactionManager(this.transactionManager);
		this.listBuilderComponent.setGermplasmListManager(this.germplasmListManager);
		this.listBuilderComponent.setGermplasmListManager(this.germplasmListManager);
		this.listBuilderComponent.setUserService(this.userService);
		this.listBuilderComponent.setContextUtil(this.contextUtil);

		when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn(ListBuilderComponentTest.CHECK);
		when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn(ListBuilderComponentTest.HASH);
		when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.AVAIL_INV, ""));
		when(this.ontologyDataManager.getTermById(TermId.GID.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.GID, ""));
		when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.ENTRY_CODE, ""));
		when(this.ontologyDataManager.getTermById(TermId.DESIG.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.DESIG, ""));
		when(this.ontologyDataManager.getTermById(TermId.CROSS.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.CROSS, ""));
		when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.SEED_SOURCE, ""));
		when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId()))
				.thenReturn(new Term(1, ListBuilderComponentTest.STOCKID, ""));
		this.listBuilderComponent.setTableWithSelectAllLayout(this.tableWithSelectAllLayout);

		this.listBuilderComponent.setToolsButtonContainer(Mockito.mock(AbsoluteLayout.class));
		final Table listDataTable = Mockito.mock(Table.class);
		this.listBuilderComponent.setListDataTable(listDataTable);
		this.listBuilderComponent.setTotalListEntriesLabel(Mockito.mock(Label.class));

		this.listBuilderComponent.setTotalSelectedListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.setBreedingManagerListDetailsComponent(Mockito.mock(BreedingManagerListDetailsComponent.class));
		this.listBuilderComponent.setDropHandler(Mockito.mock(BuildNewListDropHandler.class));
		this.listBuilderComponent.setTopLabel(new Label());
	}

	@Test
	public void testAddBasicTableColumns() {

		final Table table = new Table();
		this.listBuilderComponent.addBasicTableColumns(table);

		Assert.assertEquals(ListBuilderComponentTest.CHECK, table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals(ListBuilderComponentTest.HASH, table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.AVAIL_INV,
				table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ListBuilderComponentTest.TOTAL, table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(ListBuilderComponentTest.GID, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.ENTRY_CODE,
				table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.DESIG, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ListBuilderComponentTest.CROSS, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.SEED_SOURCE,
				table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.STOCKID, table.getColumnHeader(ColumnLabels.STOCKID.getName()));

	}

	@Test
	public void testDeleteSelectedEntriesWithNoSelectedEntries() {
		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);

		when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);
		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] {});
		when(this.breedingManagerTable.getValue()).thenReturn(selectedItems);

		this.listBuilderComponent.deleteSelectedEntries();
		try {
			Mockito.verify(source, Mockito.times(1)).getWindow();
		} catch (final WantedButNotInvoked e) {
			Assert.fail("Expecting to show 'no selected germplasm entry' error but didn't.");
		}
	}

	@Test
	public void testDeleteSelectedEntriesWithSelectedEntries() {
		when(this.tableWithSelectAllLayout.getTable()).thenReturn(this.breedingManagerTable);

		final Container container = Mockito.mock(Container.class);
		when(this.breedingManagerTable.getContainerDataSource()).thenReturn(container);

		final Collection<? extends Integer> selectedItems = Arrays.asList(new Integer[] { 1 });
		when(this.breedingManagerTable.getValue()).thenReturn(selectedItems);

		final Table listDataTable = Mockito.mock(Table.class);
		listDataTable.addItem(1);
		listDataTable.addItem(2);
		when(listDataTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setListDataTable(listDataTable);

		final ListManagerMain source = Mockito.mock(ListManagerMain.class);
		this.listBuilderComponent.setSource(source);

		this.listBuilderComponent.setTotalListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.setTotalSelectedListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.doDeleteSelectedEntries();

		try {
			Mockito.verify(container, Mockito.times(1)).removeItem(Matchers.any());
		} catch (final WantedButNotInvoked e) {
			Assert.fail("Expecting to show 'no selected germplasm entry' error but didn't.");
		}
	}

	@Test
	public void testInitializeAddColumnContextMenu() {

		this.listBuilderComponent.setContextMenu(this.contextMenu);
		final ContextMenuItem item = Mockito.mock(ContextMenuItem.class);
		this.addColumnContextMenu.setAddColumnItem(item);

		this.listBuilderComponent.setAddColumnContextMenu(this.addColumnContextMenu);
		when(item.addItem(Matchers.anyString())).thenReturn(item);
		when(this.contextMenu.addItem(ArgumentMatchers.isNull(String.class))).thenReturn(item);
		when(this.contextMenu.addItem(Matchers.any(String.class))).thenReturn(item);
		when(this.messageSource.getMessage(ArgumentMatchers.isNull(Message.class))).thenReturn("Bye");
		when(this.messageSource.getMessage(Matchers.any(Message.class))).thenReturn("Hi");

		this.listBuilderComponent.initializeAddColumnContextMenu();
		Mockito.verify(this.contextMenu).addListener(Matchers.any(ContextMenu.ClickListener.class));
	}

	@Test
	public void testAddAttributeAndNameTypeColumn() {
		final List<String> attributeAndNameTypes = new ArrayList<>();
		this.listBuilderComponent.setAttributeAndNameTypeColumns(attributeAndNameTypes);
		final String column = "PASSPORT ATTRIBUTE";
		this.listBuilderComponent.addAttributeAndNameTypeColumn(column);
		Assert.assertFalse(this.listBuilderComponent.getAttributeAndNameTypeColumns().isEmpty());
		Assert.assertTrue(this.listBuilderComponent.getAttributeAndNameTypeColumns().contains(column));
	}

	@Test
	public void testListHasAddedColumns() {
		final Table table = new Table();
		final List<String> attributeAndNameTypes = new ArrayList<>();
		this.listBuilderComponent.setListDataTable(table);
		this.listBuilderComponent.setAttributeAndNameTypeColumns(attributeAndNameTypes);
		this.listBuilderComponent.setAddColumnContextMenu(this.addColumnContextMenu);

		Mockito.doReturn(true).when(this.addColumnContextMenu).hasAddedColumn(table, attributeAndNameTypes);
		Assert.assertTrue(this.listBuilderComponent.listHasAddedColumns());

		Mockito.doReturn(false).when(this.addColumnContextMenu).hasAddedColumn(table, attributeAndNameTypes);
		Assert.assertFalse(this.listBuilderComponent.listHasAddedColumns());

	}


}

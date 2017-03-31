package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListSelectionComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.doReturn;

public class RemoveSelectedGermplasmAsDialogTest {

	private static final int NO_OF_LIST_ENTRIES = 10;
	private static final Integer USER_ID = 1;
	private static final Integer TEST_GERMPLASM_LIST_ID = 2;

	private static Table listDataTable;
	private static List<GermplasmListData> listEntries;

	@Mock
	private ListManagerMain source;

	@Mock
	private Label totalListEntriesLabel;

	@Mock
	private Window window;

	@Mock
	private Application application;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@InjectMocks
	private RemoveSelectedGermplasmAsDialog dialog;

	@BeforeClass
	public static void setUpClass() {
		RemoveSelectedGermplasmAsDialogTest.listEntries = RemoveSelectedGermplasmAsDialogTest.generateListEntries();
	}

	@Before
	public void setUp() throws MiddlewareQueryException, GermplasmListExporterException {

		MockitoAnnotations.initMocks(this);
		RemoveSelectedGermplasmAsDialogTest.listDataTable = RemoveSelectedGermplasmAsDialogTest.generateTestTable();
		ListSelectionComponent listSelectionComponent = Mockito.mock(ListSelectionComponent.class);
		ListManagerTreeComponent listManagerTreeComponent = Mockito.mock(ListManagerTreeComponent.class);

		Mockito.when(this.source.getListSelectionComponent()).thenReturn(listSelectionComponent);
		Mockito.when(this.source.getListSelectionComponent().getListTreeComponent()).thenReturn(listManagerTreeComponent);

		this.dialog = new RemoveSelectedGermplasmAsDialog(this.source, RemoveSelectedGermplasmAsDialogTest.getGermplasmList(),
			RemoveSelectedGermplasmAsDialogTest.listDataTable, this.totalListEntriesLabel);

		this.dialog.setGermplasmListManager(this.germplasmListManager);
		this.dialog.setMessageSource(this.messageSource);
		this.dialog.setTransactionManager(this.transactionManager);

		doReturn("Remove selected germplasm").when(this.messageSource).getMessage(Message.REMOVE_SELECTED_GERMPLASM);
		doReturn("Are you sure you want to delete the selected germplasm from the list? The deletion will be permanent.")
			.when(this.messageSource).getMessage(Message.REMOVE_SELECTED_GERMPLASM_CONFIRM);
		doReturn("Error Removing germplasm").when(this.messageSource).getMessage(Message.ERROR_REMOVING_GERMPLASM);
		doReturn("Warning!").when(this.messageSource).getMessage(Message.WARNING);
		doReturn("Success!").when(this.messageSource).getMessage(Message.SUCCESS);
		doReturn("Error").when(this.messageSource).getMessage(Message.ERROR);

		doReturn(this.window).when(this.source).getWindow();
		doReturn(this.application).when(this.source).getApplication();

		this.dialog.instantiateComponents();
		this.dialog.initializeValues();
	}

	private static GermplasmList getGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(RemoveSelectedGermplasmAsDialogTest.USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(RemoveSelectedGermplasmAsDialogTest.generateListEntries());
		germplasmList.setId(RemoveSelectedGermplasmAsDialogTest.TEST_GERMPLASM_LIST_ID);
		germplasmList.setStatus(100);

		return germplasmList;
	}

	private static Table generateTestTable() {
		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		RemoveSelectedGermplasmAsDialogTest.listDataTable = tableWithSelectAll.getTable();
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setSelectable(true);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setMultiSelect(true);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setColumnCollapsingAllowed(true);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setWidth("100%");
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setDragMode(Table.TableDragMode.ROW);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setData(ListComponent.LIST_DATA_COMPONENT_TABLE_DATA);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setColumnReorderingAllowed(false);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setImmediate(true);

		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable
			.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);
		RemoveSelectedGermplasmAsDialogTest.listDataTable.setColumnCollapsingAllowed(true);

		RemoveSelectedGermplasmAsDialogTest.loadEntriesToListDataTable(RemoveSelectedGermplasmAsDialogTest.listDataTable);

		return RemoveSelectedGermplasmAsDialogTest.listDataTable;
	}

	private static List<GermplasmListData> generateListEntries() {
		final List<GermplasmListData> entries = new ArrayList<>();

		for (int x = 1; x <= RemoveSelectedGermplasmAsDialogTest.NO_OF_LIST_ENTRIES; x++) {
			final GermplasmListData germplasmListData = ListInventoryDataInitializer.createGermplasmListData(x);
			entries.add(germplasmListData);
		}

		return entries;
	}

	private static void loadEntriesToListDataTable(final Table listDataTable) {
		for (final GermplasmListData entry : RemoveSelectedGermplasmAsDialogTest.listEntries) {
			RemoveSelectedGermplasmAsDialogTest.addListEntryToTable(entry, listDataTable);
		}

		listDataTable.sort(new Object[] {ColumnLabels.ENTRY_ID.getName()}, new boolean[] {true});
	}

	private static void addListEntryToTable(final GermplasmListData entry, final Table listDataTable) {
		final String gid = String.format("%s", entry.getGid().toString());
		final Button gidButton = new Button(gid, new GidLinkButtonClickListener(null, gid, true, true));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription("Click to view Germplasm information");

		final Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(null, gid, true, true));
		desigButton.setStyleName(BaseTheme.BUTTON_LINK);
		desigButton.setDescription("Click to view Germplasm information");

		final CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setData(entry.getId());
		itemCheckBox.setImmediate(true);
		itemCheckBox.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				final CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (((Boolean) itemCheckBox.getValue()).equals(true)) {
					listDataTable.select(itemCheckBox.getData());
				} else {
					listDataTable.unselect(itemCheckBox.getData());
				}
			}

		});

		final Item newItem = listDataTable.getContainerDataSource().addItem(entry.getId());
		newItem.getItemProperty(ColumnLabels.TAG.getName()).setValue(itemCheckBox);
		newItem.getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(entry.getEntryId());
		newItem.getItemProperty(ColumnLabels.DESIGNATION.getName()).setValue(desigButton);
		newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(entry.getGroupName());
		newItem.getItemProperty(ColumnLabels.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
		newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
		newItem.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(entry.getSeedSource());

		// #1 Available Inventory
		// default value
		String availInv = "-";
		if (entry.getInventoryInfo().getLotCount().intValue() != 0) {
			availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
		}
		final Button inventoryButton =
			new Button(availInv, new InventoryLinkButtonClickListener(null, null, entry.getId(), entry.getGid()));
		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
		inventoryButton.setDescription(null);
		newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);

		if ("-".equals(availInv)) {
			inventoryButton.setEnabled(false);
			inventoryButton.setDescription("No Lot for this Germplasm");
		} else {
			inventoryButton.setDescription(null);
		}

		// #2 Seed Reserved
		// default value
		String seedRes = "-";
		if (entry.getInventoryInfo().getReservedLotCount().intValue() != 0) {
			seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
		}
		newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
	}

	@Test
	public void testAcceptButtonListener() {
		this.dialog.deleteGermplasmsAction((Collection<? extends Integer>) RemoveSelectedGermplasmAsDialogTest.listDataTable.getItemIds());
		this.dialog.getAcceptButton().click();
		Mockito.verify(this.messageSource).getMessage(Message.REMOVE_SELECTED_GERMPLASM);
	}

	@Test
	public void testRemovedAllSelectedGermplasm() {
		final List<Integer> SelectedDeletdGids = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		doReturn(SelectedDeletdGids).when(this.dialog.getGermplasmListManager()).deleteGermplasms(Mockito.anyList(), Mockito.anyInt());
		assertThat(this.dialog.getListDataTable().getItemIds(), hasSize(10));
		this.dialog.deleteGermplasmsAction((Collection<? extends Integer>) this.dialog.getListDataTable().getItemIds());
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS);
		assertThat(this.dialog.getListDataTable().getItemIds(), is(empty()));
		assertThat(this.dialog.getListDataTable().getItemIds(), hasSize(0));
	}

	@Test
	public void testCouldNotRemovedAllSelectedGermplasms() {
		final List<Integer> SelectedDeletdGids = Arrays.asList(1, 2);
		doReturn(SelectedDeletdGids).when(this.dialog.getGermplasmListManager()).deleteGermplasms(Mockito.anyList(), Mockito.anyInt());
		assertThat(this.dialog.getListDataTable().getItemIds(), hasSize(10));
		this.dialog.deleteGermplasmsAction((Collection<? extends Integer>) this.dialog.getListDataTable().getItemIds());
		Mockito.verify(this.messageSource).getMessage(Message.WARNING);
		assertThat(this.dialog.getListDataTable().getItemIds(), is(not(empty())));
		assertThat(this.dialog.getListDataTable().getItemIds(), hasSize(8));
	}

	@Test
	public void testCouldNotRemovedAnySelectedGermplasms() {
		final List<Integer> SelectedDeletdGids = new ArrayList();
		doReturn(SelectedDeletdGids).when(this.dialog.getGermplasmListManager()).deleteGermplasms(Mockito.anyList(), Mockito.anyInt());
		assertThat(this.dialog.getListDataTable().getItemIds(), hasSize(10));
		this.dialog.deleteGermplasmsAction((Collection<? extends Integer>) this.dialog.getListDataTable().getItemIds());
		Mockito.verify(this.messageSource).getMessage(Message.ERROR);
		assertThat(this.dialog.getListDataTable().getItemIds(), is(not(empty())));
		assertThat(this.dialog.getListDataTable().getItemIds(), hasSize(10));
	}

}

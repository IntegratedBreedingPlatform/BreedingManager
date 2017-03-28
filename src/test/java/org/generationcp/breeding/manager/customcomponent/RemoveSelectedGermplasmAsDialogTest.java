package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
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
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;

public class RemoveSelectedGermplasmAsDialogTest {

    private static final int NO_OF_LIST_ENTRIES = 10;
    private static final Integer USER_ID = 1;
    private static final Integer TEST_GERMPLASM_LIST_ID = 2;

    private static Table listDataTable;
    private static List<GermplasmListData> listEntries;

    @Mock
    private Component source;

    @Mock
    private Window window;

    @Mock
    private Application application;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private SimpleResourceBundleMessageSource messageSource;

    @InjectMocks
    private RemoveSelectedGermplasmAsDialog dialog;


    @BeforeClass
    public static void setUpClass() {
        RemoveSelectedGermplasmAsDialogTest.listEntries = RemoveSelectedGermplasmAsDialogTest.generateListEntries();
    }

    @Before
    public void setUp() throws MiddlewareQueryException, GermplasmListExporterException {

        GermplasmDataManager germplasmDataManager = Mockito.mock(GermplasmDataManager.class);
        GermplasmListManager germplasmListManager = Mockito.mock(GermplasmListManager.class);


        MockitoAnnotations.initMocks(this);
        RemoveSelectedGermplasmAsDialogTest.listDataTable = RemoveSelectedGermplasmAsDialogTest.generateTestTable();
        this.dialog = new RemoveSelectedGermplasmAsDialog(this.source, RemoveSelectedGermplasmAsDialogTest.getGermplasmList(), RemoveSelectedGermplasmAsDialogTest.listDataTable);

        this.dialog.setGermplasmListManager(germplasmListManager);
        this.dialog.setGermplasmDataManager(germplasmDataManager);
        this.dialog.setMessageSource(this.messageSource);
        this.dialog.setTransactionManager(this.transactionManager);

        Mockito.doReturn("Remove selected germplasm").when(this.messageSource).getMessage(Message.REMOVE_SELECTED_GERMPLASM);
        Mockito.doReturn("Are you sure you want to delete the selected germplasm from the list? The deletion will be permanent.").when(this.messageSource).getMessage(Message.REMOVE_SELECTED_GERMPLASM_CONFIRM);
        Mockito.doReturn("Error Removing germplasm").when(this.messageSource).getMessage(Message.ERROR_REMOVING_GERMPLASM);
        Mockito.doReturn("Warning!").when(this.messageSource).getMessage(Message.WARNING);
        Mockito.doReturn("Success!").when(this.messageSource).getMessage(Message.SUCCESS);
        Mockito.doReturn("Error").when(this.messageSource).getMessage(Message.ERROR);


        Mockito.doReturn(this.window).when(this.source).getWindow();
        Mockito.doReturn(this.application).when(this.source).getApplication();

        dialog.instantiateComponents();
        dialog.initializeValues();
    }

    private static GermplasmList getGermplasmList() {
        GermplasmList germplasmList = new GermplasmList();
        germplasmList.setName("Sample List");
        germplasmList.setUserId(RemoveSelectedGermplasmAsDialogTest.USER_ID);
        germplasmList.setDescription("Sample description");
        germplasmList.setType("LST");
        germplasmList.setDate(20141112L);
        germplasmList.setNotes("Sample Notes");
        germplasmList.setListData(generateListEntries());
        germplasmList.setId(TEST_GERMPLASM_LIST_ID);
        germplasmList.setStatus(100);

        return germplasmList;
    }

    private static Table generateTestTable() {
        final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
        tableWithSelectAll.instantiateComponents();

        listDataTable = tableWithSelectAll.getTable();
        listDataTable.setSelectable(true);
        listDataTable.setMultiSelect(true);
        listDataTable.setColumnCollapsingAllowed(true);
        listDataTable.setWidth("100%");
        listDataTable.setDragMode(Table.TableDragMode.ROW);
        listDataTable.setData(ListComponent.LIST_DATA_COMPONENT_TABLE_DATA);
        listDataTable.setColumnReorderingAllowed(false);
        listDataTable.setImmediate(true);

        listDataTable.addContainerProperty(ColumnLabels.TAG.getName(), CheckBox.class, null);
        listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
        listDataTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
        listDataTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
        listDataTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null);
        listDataTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
        listDataTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
        listDataTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
        listDataTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);
        listDataTable.setColumnCollapsingAllowed(true);


        RemoveSelectedGermplasmAsDialogTest.loadEntriesToListDataTable(listDataTable);

        return listDataTable;
    }

    private static List<GermplasmListData> generateListEntries() {
        List<GermplasmListData> entries = new ArrayList<>();

        for (int x = 1; x <= RemoveSelectedGermplasmAsDialogTest.NO_OF_LIST_ENTRIES; x++) {
            GermplasmListData germplasmListData = ListInventoryDataInitializer.createGermplasmListData(x);
            entries.add(germplasmListData);
        }

        return entries;
    }

    private static void loadEntriesToListDataTable(Table listDataTable) {
        for (GermplasmListData entry : RemoveSelectedGermplasmAsDialogTest.listEntries) {
            RemoveSelectedGermplasmAsDialogTest.addListEntryToTable(entry, listDataTable);
        }

        listDataTable.sort(new Object[]{ColumnLabels.ENTRY_ID.getName()}, new boolean[]{true});
    }

    private static void addListEntryToTable(GermplasmListData entry, final Table listDataTable) {
        String gid = String.format("%s", entry.getGid().toString());
        Button gidButton = new Button(gid, new GidLinkButtonClickListener(null, gid, true, true));
        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
        gidButton.setDescription("Click to view Germplasm information");

        Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(null, gid, true, true));
        desigButton.setStyleName(BaseTheme.BUTTON_LINK);
        desigButton.setDescription("Click to view Germplasm information");

        CheckBox itemCheckBox = new CheckBox();
        itemCheckBox.setData(entry.getId());
        itemCheckBox.setImmediate(true);
        itemCheckBox.addListener(new Button.ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                CheckBox itemCheckBox = (CheckBox) event.getButton();
                if (((Boolean) itemCheckBox.getValue()).equals(true)) {
                    listDataTable.select(itemCheckBox.getData());
                } else {
                    listDataTable.unselect(itemCheckBox.getData());
                }
            }

        });

        Item newItem = listDataTable.getContainerDataSource().addItem(entry.getId());
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
        Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(null, null, entry.getId(), entry.getGid()));
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
        RemoveSelectedGermplasmAsDialog.AcceptButtonListener acceptButtonListener = new RemoveSelectedGermplasmAsDialog.AcceptButtonListener(this.dialog);
        acceptButtonListener.removeSelectedGermplasmAsDialog.setListDataTable(this.dialog.getListDataTable());
        acceptButtonListener.buttonClick(null);

        Mockito.verify(messageSource).getMessage(Message.ERROR_REMOVING_GERMPLASM);
        assertThat((Collection<?>) acceptButtonListener.removeSelectedGermplasmAsDialog.getListDataTable().getValue(), is(empty()));

    }

    @Test
    public void testRemovedAllSelectedGermplasm() {
        List<?> deletedGids = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Mockito.when(this.dialog.getGermplasmDataManager().deleteGermplasms(Mockito.anyList())).thenReturn(deletedGids);
        this.dialog.deleteGermplasmsAction(RemoveSelectedGermplasmAsDialogTest.listDataTable.getItemIds());
        Mockito.verify(messageSource).getMessage(Message.SUCCESS);
    }

    @Test
    public void testCouldNotRemovedAllGermplasms() {
        List<?> deletedGids = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Mockito.when(this.dialog.getGermplasmDataManager().deleteGermplasms(Mockito.anyList())).thenReturn(deletedGids);
        this.dialog.deleteGermplasmsAction(RemoveSelectedGermplasmAsDialogTest.listDataTable.getItemIds());
        Mockito.verify(messageSource).getMessage(Message.WARNING);
    }


    @Test
    public void testCouldNotRemovedAnyGermplasms() {
        List<?> deletedGids = new ArrayList<>();
        Mockito.when(this.dialog.getGermplasmDataManager().deleteGermplasms(Mockito.anyList())).thenReturn(deletedGids);
        this.dialog.deleteGermplasmsAction(RemoveSelectedGermplasmAsDialogTest.listDataTable.getItemIds());
        Mockito.verify(messageSource).getMessage(Message.ERROR);
    }
}

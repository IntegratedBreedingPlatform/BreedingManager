package org.generationcp.breeding.manager.customcomponent;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class ExportListAsDialogTest {

	private static final int NO_OF_LIST_ENTRIES = 10;
	private static final Integer USER_ID = 1;
	
	private static Table listDataTable;
	private static List<GermplasmListData>  listEntries;
	private static GermplasmList germplasmList;
	
	@Mock
	private Component source;
	
	@Mock
	private Window window;
	
	@Mock
	private FileDownloadResource fileDownloadResource;
	
	@Mock
	private GermplasmListExporter listExporter;
	
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	private ExportListAsDialog dialog;
	
	private static Table emptyTable;
	
	@BeforeClass
	public static void setUpClass() {
		emptyTable = new Table();
		listEntries = generateListEntries();
		germplasmList = getGermplasmList();		
	}
	
	@Before
	public void setUp() throws MiddlewareQueryException, GermplasmListExporterException {
		
		MockitoAnnotations.initMocks(this);
		
		listDataTable = generateTestTable();
		dialog = spy(new ExportListAsDialog(source,germplasmList,listDataTable));
		
		listExporter = Mockito.mock(GermplasmListExporter.class);
		dialog.setListExporter(listExporter);
		dialog.setMessageSource(messageSource);
		
		doNothing().when(dialog).showMessage(ExportListAsDialog.EXPORT_WARNING_MESSAGE);
		doReturn(Mockito.mock(FileOutputStream.class)).when(listExporter).exportGermplasmListXLS(ExportListAsDialog.TEMP_FILENAME,listDataTable);
		
		doReturn(fileDownloadResource).when(dialog).createFileDownloadResource();
		doReturn(window).when(source).getWindow();
		doNothing().when(window).open(fileDownloadResource);
	}
	
	@Test
	public void testIsARequiredColumn(){
		Assert.assertTrue("GID is a required column.",dialog.isARequiredColumn(ListDataTablePropertyID.GID.getName()));
		Assert.assertTrue("ENTRY_ID is a required column.",dialog.isARequiredColumn(ListDataTablePropertyID.ENTRY_ID.getName()));
		Assert.assertTrue("DESIGNATION is a required column.",dialog.isARequiredColumn(ListDataTablePropertyID.DESIGNATION.getName()));
		Assert.assertFalse("SEED_SOURCE is a not required column.",dialog.isARequiredColumn(ListDataTablePropertyID.SEED_SOURCE.getName()));
	}
	
	@Test
	public void testIsARequiredColumnHidden(){
		Assert.assertFalse("Expecting no required columns is hidden but didn't.",dialog.isARequiredColumnHidden(listDataTable));
		
		listDataTable.setColumnCollapsed(ListDataTablePropertyID.GID.getName(), true);
		Assert.assertTrue("Expecting one of the required columns is hidden but didn't.",dialog.isARequiredColumnHidden(listDataTable));
	}

	
	@Test
	public void testShowWarningMessageWhenListDataTableHasHiddenRequiredColumns(){
		listDataTable.setColumnCollapsed(ListDataTablePropertyID.GID.getName(), true);
		
		Assert.assertTrue(dialog.isARequiredColumnHidden(listDataTable));
		dialog.showWarningMessage(listDataTable);
		verify(dialog,times(1)).showMessage(ExportListAsDialog.EXPORT_WARNING_MESSAGE);
	}
	
	@Test
	public void testShowWarningMessageWhenListDataTableDoNotHaveHiddenRequiredColumns(){
		Assert.assertFalse(dialog.isARequiredColumnHidden(listDataTable));
		dialog.showWarningMessage(listDataTable);
		verify(dialog,times(0)).showMessage(ExportListAsDialog.EXPORT_WARNING_MESSAGE);
	}
	
	@Test
	public void testExportListAsXLS()
			throws GermplasmListExporterException, MiddlewareQueryException {
		dialog.exportListAsXLS(listDataTable);
		verify(listExporter,times(1)).exportGermplasmListXLS(ExportListAsDialog.TEMP_FILENAME, listDataTable);
	}
	
	@Test
	public void testExportListAsXLSWithException()
			throws GermplasmListExporterException, MiddlewareQueryException {
		doThrow(new GermplasmListExporterException()).when(listExporter).exportGermplasmListXLS(ExportListAsDialog.TEMP_FILENAME,emptyTable);
		dialog.exportListAsXLS(emptyTable);
		verify(window,times(0)).open(fileDownloadResource);
	}
	
	@Test
	public void testExportListAsCSV() throws GermplasmListExporterException{
		
		dialog.exportListAsCSV(listDataTable);
		verify(listExporter,times(1)).exportGermplasmListCSV(ExportListAsDialog.TEMP_FILENAME, listDataTable);
	}
	
	@Test
	public void testExportListAsCSVWithException() throws GermplasmListExporterException{
		doThrow(new GermplasmListExporterException()).when(listExporter).exportGermplasmListCSV(ExportListAsDialog.TEMP_FILENAME,emptyTable);
		dialog.exportListAsCSV(emptyTable);
		verify(window,times(0)).open(fileDownloadResource);
	}
	
	private static GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(generateListEntries());
		
		return germplasmList;
	}
	
	private static Table generateTestTable(){
		Table listDataTable = new Table();
		
		listDataTable.addContainerProperty(ListDataTablePropertyID.TAG.getName(), CheckBox.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.AVAILABLE_INVENTORY.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_RESERVATION.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		listDataTable.setColumnCollapsingAllowed(true);
		
		loadEntriesToListDataTable(listDataTable);
		
		return listDataTable;
	}
	
	private static List<GermplasmListData> generateListEntries(){
		List<GermplasmListData> entries = new ArrayList<>();
    	
    	for (int x=1; x <= NO_OF_LIST_ENTRIES; x++){
    		GermplasmListData germplasmListData = new GermplasmListData();
    		germplasmListData.setId(x);
    		germplasmListData.setEntryId(x);
    		germplasmListData.setDesignation(ListDataTablePropertyID.DESIGNATION.getName() + x);
    		germplasmListData.setGroupName(ListDataTablePropertyID.PARENTAGE.getName() + x);
    		ListDataInventory inventoryInfo = new ListDataInventory(x,x);
    		inventoryInfo.setLotCount(1);
    		inventoryInfo.setReservedLotCount(1);
    		inventoryInfo.setActualInventoryLotCount(1);
    		germplasmListData.setInventoryInfo(inventoryInfo);
    		germplasmListData.setEntryCode(ListDataTablePropertyID.ENTRY_CODE.getName() + x);
    		germplasmListData.setSeedSource(ListDataTablePropertyID.SEED_SOURCE.getName() + x);
    		germplasmListData.setGid(x);
    		entries.add(germplasmListData);
    	}
    	
    	return entries;
	}

	private static void loadEntriesToListDataTable(Table listDataTable){
		for(GermplasmListData entry : listEntries){
			addListEntryToTable(entry, listDataTable);
	   	}
		
		listDataTable.sort(new Object[]{ListDataTablePropertyID.ENTRY_ID.getName()}, new boolean[]{true});    
	}

	private static void addListEntryToTable(GermplasmListData entry, final Table listDataTable) {
		String gid = String.format("%s", entry.getGid().toString());
		Button gidButton = new Button(gid, new GidLinkButtonClickListener(null, gid,true,true));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription("Click to view Germplasm information");
		
		Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(null, gid,true,true));
		desigButton.setStyleName(BaseTheme.BUTTON_LINK);
		desigButton.setDescription("Click to view Germplasm information");
		
		CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setData(entry.getId());
		itemCheckBox.setImmediate(true);
		itemCheckBox.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				CheckBox itemCheckBox = (CheckBox) event.getButton();
				if(((Boolean) itemCheckBox.getValue()).equals(true)){
					listDataTable.select(itemCheckBox.getData());
				} else {
					listDataTable.unselect(itemCheckBox.getData());
				}
			}
			 
		});
		
		Item newItem = listDataTable.getContainerDataSource().addItem(entry.getId());
		newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(itemCheckBox);
		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(entry.getEntryId());
		newItem.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).setValue(desigButton);
		newItem.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).setValue(entry.getGroupName());
		newItem.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).setValue(entry.getEntryCode());
		newItem.getItemProperty(ListDataTablePropertyID.GID.getName()).setValue(gidButton);
		newItem.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).setValue(entry.getSeedSource());
		
		//#1 Available Inventory
        //default value
		String availInv = "-";
		if(entry.getInventoryInfo().getLotCount().intValue() != 0){
			availInv = entry.getInventoryInfo().getActualInventoryLotCount().toString().trim();
		}
		Button inventoryButton = new Button(availInv, new InventoryLinkButtonClickListener(null,null,entry.getId(), entry.getGid()));
		inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
		inventoryButton.setDescription(null);
		newItem.getItemProperty(ListDataTablePropertyID.AVAILABLE_INVENTORY.getName()).setValue(inventoryButton);
		
		if("-".equals(availInv)){
			inventoryButton.setEnabled(false);
			inventoryButton.setDescription("No Lot for this Germplasm");
		} else {
			inventoryButton.setDescription(null);
		}
		
		//#2 Seed Reserved
		//default value
		String seedRes = "-"; 
		if(entry.getInventoryInfo().getReservedLotCount().intValue() != 0){
			seedRes = entry.getInventoryInfo().getReservedLotCount().toString().trim();
		}
		newItem.getItemProperty(ListDataTablePropertyID.SEED_RESERVATION.getName()).setValue(seedRes);
	}

}

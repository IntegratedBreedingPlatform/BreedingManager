package org.generationcp.breeding.manager.listmanager.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.service.impl.ExportServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GermplasmListExporterTest {
	
	private static final String FILE_NAME = "testGermplasmListExporter.csv";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	private GermplasmListExporter germplasmListExporter;
	
	private ExportService exportService;
	
	private static Table listDataTable;
	private static List<GermplasmListData>  listEntries;
	
	@BeforeClass
	public static void setUpClass() {
		
		listEntries = generateListEntries();
		listDataTable = generateTestTable();
				
	}
	
	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
		
		exportService = spy(new ExportServiceImpl());
		germplasmListExporter = spy(new GermplasmListExporter(1));
		germplasmListExporter.setExportService(exportService);
		germplasmListExporter.setMessageSource(messageSource);
		
		doReturn("#").when(messageSource).getMessage(ListDataTablePropertyID.ENTRY_ID.getColumnDisplay());
		doReturn("DESIGNATION").when(messageSource).getMessage(ListDataTablePropertyID.DESIGNATION.getColumnDisplay());
		doReturn("CROSS").when(messageSource).getMessage(ListDataTablePropertyID.PARENTAGE.getColumnDisplay());
		doReturn("ENTRY CODE").when(messageSource).getMessage(ListDataTablePropertyID.ENTRY_CODE.getColumnDisplay());
		doReturn("GID").when(messageSource).getMessage(ListDataTablePropertyID.GID.getColumnDisplay());
		doReturn("SOURCE").when(messageSource).getMessage(ListDataTablePropertyID.SEED_SOURCE.getColumnDisplay());
		
	}
	
	@Test
	public void testExportGermplasmListCSV(){
		
		try {
			
			germplasmListExporter.exportGermplasmListCSV(FILE_NAME, listDataTable);
			//make sure that generateCSVFile is called and without errors
			verify(exportService, times(1)).generateCSVFile(any(List.class), any(List.class), anyString());
			
		} catch (GermplasmListExporterException | IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected = GermplasmListExporterException.class)
	public void testExportGermplasmListCSVWithException() throws GermplasmListExporterException, IOException {
			
		doThrow(new IOException()).when(exportService).generateCSVFile(any(List.class), any(List.class), anyString());
		germplasmListExporter.exportGermplasmListCSV(FILE_NAME, listDataTable);
			
	}
	
	@Test
	public void testGetExportColumnHeadersFromTable_AllColumnsAreVisible(){
	
		List<ExportColumnHeader> exportColumnHeaders = germplasmListExporter.getExportColumnHeadersFromTable(listDataTable);
		
		
		//no columns in the table are hidden/collapsed, therefore the isDisplay is always true for all headers
		assertEquals(6, exportColumnHeaders.size());
		assertTrue(exportColumnHeaders.get(0).isDisplay());
		assertTrue(exportColumnHeaders.get(1).isDisplay());
		assertTrue(exportColumnHeaders.get(2).isDisplay());
		assertTrue(exportColumnHeaders.get(3).isDisplay());
		assertTrue(exportColumnHeaders.get(4).isDisplay());
		assertTrue(exportColumnHeaders.get(5).isDisplay());
		
		//make sure the header in CSV is same as the header in table
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.ENTRY_ID.getColumnDisplay()), exportColumnHeaders.get(0).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.GID.getColumnDisplay()), exportColumnHeaders.get(1).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.ENTRY_CODE.getColumnDisplay()), exportColumnHeaders.get(2).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.DESIGNATION.getColumnDisplay()), exportColumnHeaders.get(3).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.PARENTAGE.getColumnDisplay()), exportColumnHeaders.get(4).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.SEED_SOURCE.getColumnDisplay()), exportColumnHeaders.get(5).getName());
		
			
	}
	
	@Test
	public void testGetExportColumnHeadersFromTable_NoColumnsAreVisible(){
	
		Table collapsedColumnTable = generateTestTable();
		collapsedColumnTable.setColumnCollapsed((Object)ListDataTablePropertyID.ENTRY_ID.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ListDataTablePropertyID.DESIGNATION.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ListDataTablePropertyID.PARENTAGE.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ListDataTablePropertyID.GID.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ListDataTablePropertyID.ENTRY_CODE.getName(), true);
		collapsedColumnTable.setColumnCollapsed((Object)ListDataTablePropertyID.SEED_SOURCE.getName(), true);
		
		List<ExportColumnHeader> exportColumnHeaders = germplasmListExporter.getExportColumnHeadersFromTable(collapsedColumnTable);
		
		//EntryID, GID and DESIGNATION are all required columns, so their isDisplay is always true.
		assertEquals(6, exportColumnHeaders.size());
		assertTrue(exportColumnHeaders.get(0).isDisplay());
		assertTrue(exportColumnHeaders.get(1).isDisplay());
		assertFalse(exportColumnHeaders.get(2).isDisplay());
		assertTrue(exportColumnHeaders.get(3).isDisplay());
		assertFalse(exportColumnHeaders.get(4).isDisplay());
		assertFalse(exportColumnHeaders.get(5).isDisplay());
		
		//make sure the header in CSV is same as the header in table
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.ENTRY_ID.getColumnDisplay()), exportColumnHeaders.get(0).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.GID.getColumnDisplay()), exportColumnHeaders.get(1).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.ENTRY_CODE.getColumnDisplay()), exportColumnHeaders.get(2).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.DESIGNATION.getColumnDisplay()), exportColumnHeaders.get(3).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.PARENTAGE.getColumnDisplay()), exportColumnHeaders.get(4).getName());
		assertEquals(messageSource.getMessage(ListDataTablePropertyID.SEED_SOURCE.getColumnDisplay()), exportColumnHeaders.get(5).getName());
		
	}
	
	@Test
	public void testGetExportColumnValuesFromTable(){
		
	
		List<Map<Integer, ExportColumnValue>> exportColumnValues = germplasmListExporter.getExportColumnValuesFromTable(listDataTable);
		assertEquals(10, exportColumnValues.size());
		
		//check if the values from the table match the created pojos
		for(int x = 0; x < exportColumnValues.size(); x++) {
			Map<Integer, ExportColumnValue> row = exportColumnValues.get(x);
			assertEquals(listEntries.get(x).getEntryId().toString(), row.get(0).getValue());
			assertEquals(listEntries.get(x).getGid().toString(), row.get(1).getValue());
			assertEquals(listEntries.get(x).getEntryCode().toString(), row.get(2).getValue());
			assertEquals(listEntries.get(x).getDesignation().toString(), row.get(3).getValue());
			assertEquals(listEntries.get(x).getGroupName().toString(), row.get(4).getValue());
			assertEquals(listEntries.get(x).getSeedSource().toString(), row.get(5).getValue());
	   	}
		
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
    	
    	for (int x=1; x < 11; x++){
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

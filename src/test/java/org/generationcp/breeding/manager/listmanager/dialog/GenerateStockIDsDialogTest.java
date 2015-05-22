package org.generationcp.breeding.manager.listmanager.dialog;

import junit.framework.Assert;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class GenerateStockIDsDialogTest {

	@Mock
	SimpleResourceBundleMessageSource messageSource;
	
	@Mock
	SpecifyGermplasmDetailsComponent source;
	
	@Mock
	StockService stockService;
	
	@InjectMocks
	GenerateStockIDsDialog generateStockIDsDialog = Mockito.spy(new GenerateStockIDsDialog(source, null));
	
	@Before
	public void setUp() throws Exception{
		
		MockitoAnnotations.initMocks(this);
		
		Mockito.doReturn("SID1-").when(stockService).calculateNextStockIDPrefix("SID", "-");
		Mockito.doReturn("TEST1-").when(stockService).calculateNextStockIDPrefix("TEST", "-");
		
	}
	
	
	@Test
	public void testApplyStockIdToImportedGermplasmDefaultPrefix(){
		
		List<ImportedGermplasm> list = createImportedGermplasmList();
		
		generateStockIDsDialog.applyStockIdToImportedGermplasm("", list);
		
		//If there is no prefix specified by the user, the default prefix will be SID. 
		Assert.assertEquals("SID1-1", list.get(0).getInventoryId());
		Assert.assertEquals(null, list.get(1).getInventoryId());
		Assert.assertEquals("SID1-2", list.get(2).getInventoryId());
		Assert.assertEquals("SID1-3", list.get(3).getInventoryId());
	}
	
	@Test
	public void testApplyStockIdToImportedGermplasmUserSpecifiedPrefix(){
		
		List<ImportedGermplasm> list = createImportedGermplasmList();
		
		generateStockIDsDialog.applyStockIdToImportedGermplasm("TEST", list);
		
		Assert.assertEquals("TEST1-1", list.get(0).getInventoryId());
		Assert.assertEquals(null, list.get(1).getInventoryId());
		Assert.assertEquals("TEST1-2", list.get(2).getInventoryId());
		Assert.assertEquals("TEST1-3", list.get(3).getInventoryId());
	}
	
	@Test
	public void testUpdateSampleStockIdDefaultPrefix() throws Exception {
		
		generateStockIDsDialog.afterPropertiesSet();
		generateStockIDsDialog.updateSampleStockId("");
		
		Assert.assertEquals("SID1", generateStockIDsDialog.getLblExampleNextPrefixInSequence().getValue().toString());
		Assert.assertEquals("SID1-1", generateStockIDsDialog.getLblExampleStockIdForThisList().getValue().toString());
	}
	
	@Test
	public void testUpdateSampleStockIdUserSpecifiedPrefix() throws Exception {
		
		generateStockIDsDialog.afterPropertiesSet();
		generateStockIDsDialog.updateSampleStockId("TEST");
		
		Assert.assertEquals("TEST1", generateStockIDsDialog.getLblExampleNextPrefixInSequence().getValue().toString());
		Assert.assertEquals("TEST1-1", generateStockIDsDialog.getLblExampleStockIdForThisList().getValue().toString());
	}
	
	
	private List<ImportedGermplasm> createImportedGermplasmList(){
		
		List<ImportedGermplasm> list = new ArrayList<>();
		
		ImportedGermplasm importedGermplasm1 = new ImportedGermplasm();
		importedGermplasm1.setSeedAmount(100D);
		list.add(importedGermplasm1);
		
		ImportedGermplasm importedGermplasm2 = new ImportedGermplasm();
		importedGermplasm2.setSeedAmount(0D);
		list.add(importedGermplasm2);
		
		ImportedGermplasm importedGermplasm3 = new ImportedGermplasm();
		importedGermplasm3.setSeedAmount(500D);
		list.add(importedGermplasm3);
		
		ImportedGermplasm importedGermplasm4 = new ImportedGermplasm();
		importedGermplasm4.setSeedAmount(20D);
		list.add(importedGermplasm4);
		
		return list;
		
	}
	
	
	
}

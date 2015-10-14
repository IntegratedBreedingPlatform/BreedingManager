
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.ArrayList;
import java.util.List;

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

public class GenerateStockIDsDialogTest {

	@Mock
	SimpleResourceBundleMessageSource messageSource;

	@Mock
	SpecifyGermplasmDetailsComponent source;

	@Mock
	StockService stockService;

	@InjectMocks
	GenerateStockIDsDialog generateStockIDsDialog = Mockito.spy(new GenerateStockIDsDialog(this.source, null));

	private static final String inventoryId = "SID1-";

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		Mockito.doReturn(inventoryId).when(this.stockService).calculateNextStockIDPrefix("SID", "-");
		Mockito.doReturn("TEST1-").when(this.stockService).calculateNextStockIDPrefix("TEST", "-");

	}

	@Test
	public void testApplyStockIdToImportedGermplasmDefaultPrefix() {

		List<ImportedGermplasm> list = this.createImportedGermplasmList();

		this.generateStockIDsDialog.applyStockIdToImportedGermplasm("", list);

		// If there is no prefix specified by the user, the default prefix will be SID.
		Assert.assertEquals("SID1-1", list.get(0).getInventoryId());
		Assert.assertEquals(null, list.get(1).getInventoryId());
		Assert.assertEquals("SID1-2", list.get(2).getInventoryId());
		Assert.assertEquals("SID1-3", list.get(3).getInventoryId());
	}

	@Test
	public void testApplyStockIdToImportedGermplasmUserSpecifiedPrefix() {

		List<ImportedGermplasm> list = this.createImportedGermplasmList();

		this.generateStockIDsDialog.applyStockIdToImportedGermplasm("TEST", list);

		Assert.assertEquals("TEST1-1", list.get(0).getInventoryId());
		Assert.assertEquals(null, list.get(1).getInventoryId());
		Assert.assertEquals("TEST1-2", list.get(2).getInventoryId());
		Assert.assertEquals("TEST1-3", list.get(3).getInventoryId());
	}

	@Test
	public void testUpdateSampleStockIdDefaultPrefix() throws Exception {

		this.generateStockIDsDialog.afterPropertiesSet();
		this.generateStockIDsDialog.updateSampleStockId("");

		Assert.assertEquals("SID1", this.generateStockIDsDialog.getLblExampleNextPrefixInSequence().getValue().toString());
		Assert.assertEquals("SID1-1", this.generateStockIDsDialog.getLblExampleStockIdForThisList().getValue().toString());
	}

	@Test
	public void testUpdateSampleStockIdUserSpecifiedPrefix() throws Exception {

		this.generateStockIDsDialog.afterPropertiesSet();
		this.generateStockIDsDialog.updateSampleStockId("TEST");

		Assert.assertEquals("TEST1", this.generateStockIDsDialog.getLblExampleNextPrefixInSequence().getValue().toString());
		Assert.assertEquals("TEST1-1", this.generateStockIDsDialog.getLblExampleStockIdForThisList().getValue().toString());
	}

	private List<ImportedGermplasm> createImportedGermplasmList() {

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

	@Test
	public void testIsValidPrefix_ForEmptryString() {
		Assert.assertTrue("Expecting that no validation will be made for emtpy string.", this.generateStockIDsDialog.isValidPrefix(""));
	}

	@Test
	public void testIsValidPrefix_ForStringWithCharactersOtherThanLetters() {
		// start
		String prefix = "?STK";
		Assert.assertFalse("Expecting that prefix with characters other than letters is invalid.",
				this.generateStockIDsDialog.isValidPrefix(prefix));

		// end
		prefix = "STK?";
		Assert.assertFalse("Expecting that prefix with characters other than letters is invalid.",
				this.generateStockIDsDialog.isValidPrefix(prefix));

		// middle
		prefix = "ST?K";
		Assert.assertFalse("Expecting that prefix with characters other than letters is invalid.",
				this.generateStockIDsDialog.isValidPrefix(prefix));
	}

	@Test
	public void testIsValidPrefix_ForStringWithLettersOnly() {
		String prefix = "STK";
		Assert.assertTrue("Expecting that prefix with characters other than letters is invalid.",
				this.generateStockIDsDialog.isValidPrefix(prefix));
	}
}

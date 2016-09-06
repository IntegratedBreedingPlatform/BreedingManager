
package org.generationcp.breeding.manager.pojos;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class ImportedGermplasmListTest {

	private static final String inventoryId = "SID-";

	private ImportedGermplasmList list = null;

	@Before
	public void setUp() {
		this.list = new ImportedGermplasmList(null, null, null, null, null);
		this.list.setHasStockIDValues(true);
	}

	/**
	 * This test will check missing Inventory Variable. Result: this will check that inventory amount is empty and return true if value is
	 * missing.
	 */
	@Test
	public void testMissingInventoryVariable() {

		this.list.setImportedGermplasm(this.generateImportGermplasmListDataWithSeedAmountOrNot(3, false));

		Assert.assertEquals(this.list.hasMissingInventoryVariable(), true);
	}

	/**
	 * This test will check Inventory Variable is available. Result: this will check that inventory amount is available and return false if
	 * value exists.
	 */
	@Test
	public void testNotMissingInventoryVariable() {

		this.list.setImportedGermplasm(this.generateImportGermplasmListDataWithSeedAmountOrNot(3, true));

		Assert.assertEquals(this.list.hasMissingInventoryVariable(), false);
	}

	/**
	 * This will generate list of ImportedGermplasm.
	 * 
	 * @param numberOfEntries number of entries need to be created.
	 * @param generateSeedAmount flag to determine that need to set seed amount blank or value.
	 * @return List of ImportedGermplasm.
	 */
	private List<ImportedGermplasm> generateImportGermplasmListDataWithSeedAmountOrNot(Integer numberOfEntries, boolean generateSeedAmount) {
		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();

		ImportedGermplasm importedGermplasm;
		for (int count = 1; count <= numberOfEntries; count++) {
			importedGermplasm = new ImportedGermplasm();
			importedGermplasm.setInventoryId(inventoryId + count);

			if (generateSeedAmount) {
				importedGermplasm.setSeedAmount((double) count);
			} else {
				importedGermplasm.setSeedAmount(0.0);
			}

			importedGermplasms.add(importedGermplasm);
		}

		return importedGermplasms;
	}

}

package org.generationcp.breeding.manager.inventory;

import junit.framework.Assert;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class SeedInventoryImportListParserTest {

	public static final String TEST_FILE_NAME = "ListName-Seed Prep.xls";

	@InjectMocks
	private final SeedInventoryImportListParser parser = new SeedInventoryImportListParser();

	@Before
	public void setUp(){
		parser.setOriginalFilename(TEST_FILE_NAME);
	}

	@Test
	public void testParseWorkbook() throws Exception{
		final File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(SeedInventoryImportListParserTest.TEST_FILE_NAME).toURI());

		assert workbookFile.exists();
		final Workbook workbook = WorkbookFactory.create(workbookFile);
		parser.parseWorkbook(workbook, null);

		Assert.assertEquals("List name should match with Imported Inventory list name",
				"Expanded List1", parser.importedSeedInventoryList.getListName());

		Assert.assertEquals("File name should match with Imported Inventory File name",
				TEST_FILE_NAME, parser.importedSeedInventoryList.getFilename());

		Assert.assertEquals("The size of imported reservation should match with imported inventories.", 1, parser.importedSeedInventoryList.getImportedSeedInventoryList().size());
		ImportedSeedInventory importedSeedInventory = parser.importedSeedInventoryList.getImportedSeedInventoryList().get(0);

		Assert.assertEquals("The entry no of imported reservation should match with imported reservation.",
				"1", importedSeedInventory.getEntry().toString());
		Assert.assertEquals("The designation of imported reservation should match with imported reservation.",
				"(CML454 X CML451)-B-3-1-1", importedSeedInventory.getDesignation());
		Assert.assertEquals("The GID of imported reservation should match with imported reservation.",
				"28", importedSeedInventory.getGid().toString());
		Assert.assertEquals("The LotID of imported reservation should match with imported reservation.",
				"29", importedSeedInventory.getLotID().toString());
		Assert.assertEquals("The TransactionID of imported reservation should match with imported reservation.",
				"60", importedSeedInventory.getTransactionId().toString());
		Assert.assertEquals("The Withdrawal Amount of imported reservation should match with imported reservation.",
				"3.0", importedSeedInventory.getReservationAmount().toString());
		Assert.assertEquals("The Amount Withdrawal of imported reservation should match with imported reservation.",
				"3.0", importedSeedInventory.getWithdrawalAmount().toString());
		Assert.assertNull("The balance of imported reservation should be null.",
				importedSeedInventory.getBalanceAmount());

		Assert.assertEquals("The comments of imported reservation should match with imported reservation.",
				"3 reserved", importedSeedInventory.getComments());

	}

}

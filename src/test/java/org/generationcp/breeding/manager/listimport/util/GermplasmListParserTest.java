
package org.generationcp.breeding.manager.listimport.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.listimport.validator.StockIDValidator;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.CrossesListDescriptionSheetParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.UserDefinedFieldTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by cyrus on 5/7/15. Unit test will only cover the observation sheet parsing as we will have a separate unit test for parsing
 * Description Sheet (see the equivalent unit test for {@link CrossesListDescriptionSheetParser}) But test still
 * promises at least 50% coverage for {@link GermplasmListParser}
 */
@RunWith(MockitoJUnitRunner.class)
public class GermplasmListParserTest {

	private static final int NO_OF_ENTRIES = 5;
	private static final String SEED_AMOUNT_G = "SEED_AMOUNT_G";
	private static final String INVENTORY_AMOUNT = "INVENTORY AMOUNT";
	public static final String TEST_FILE_NAME = "GermplasmImportTemplate-StockIDs-only.xls";
	public static final String OBSERVATION_NO_STOCK_ID_FILE = "GermplasmImportTemplate-StockIDs-missing-stock-id-column.xls";
	public static final String OBSERVATION_NO_STOCK_ID_VALUES_FILE = "GermplasmImportTemplate-StockIDs-missing-stock-id-values.xls";
	public static final String NO_INVENTORY_COL_FILE = "GermplasmImportTemplate-StockIDs-no-inventory-column.xls";
	public static final String DUPLICATE_STOCK_ID_FILE = "GermplasmImportTemplate-StockIDs-duplicate-stock-ids.xls";
	public static final String ADDITIONAL_NAME_FILE = "GermplasmImportTemplate-additional-name.xls";
	private static final int EXPECTED_DESCRIPTION_SHEET_VARIABLE_COUNT = 12;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private StockIDValidator stockIdValidator;

	@InjectMocks
	private final GermplasmListParser parser = new GermplasmListParser();

	private ImportedGermplasmList importedGermplasmList;
	private final UserDefinedFieldTestDataInitializer userDefinedFieldTestDataInitializer = new UserDefinedFieldTestDataInitializer();
	private final ImportedGermplasmListDataInitializer importedGermplasmListInitializer = new ImportedGermplasmListDataInitializer();

	@Before
	public void setUp() throws Exception {

		Mockito.when(this.ontologyDataManager.isSeedAmountVariable(Matchers.eq(INVENTORY_AMOUNT))).thenReturn(true);
		Mockito.when(this.ontologyDataManager.isSeedAmountVariable(AdditionalMatchers.not(Matchers.eq(INVENTORY_AMOUNT))))
				.thenReturn(false);
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(Matchers.anyInt())).thenReturn(
				GermplasmTestDataInitializer.createGermplasm(1));
		Mockito.when(this.inventoryDataManager.getSimilarStockIds(Matchers.anyList())).thenReturn(new ArrayList<String>());
		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(
				this.userDefinedFieldTestDataInitializer.getValidListType());

	}

	/**
	 * This is the default case, the template has a stock id factor
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithStockIdFactor() throws Exception {
		final File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.TEST_FILE_NAME).toURI());

		assert workbookFile.exists();

		final Workbook defaultWorkbook = WorkbookFactory.create(workbookFile);
		this.importedGermplasmList = this.parser.parseWorkbook(defaultWorkbook, null);

		Assert.assertNotNull("Parser was not able to properly retrieve the germplasm list for import from the file",
				this.importedGermplasmList);
		Assert.assertEquals("This template has blank list date, should be eq to current date", DateUtil.getCurrentDateInUIFormat(),
				DateUtil.getDateInUIFormat(this.importedGermplasmList.getDate()));
		assert this.parser.hasStockIdFactor();
	}

	@Test
	public void testProperHeaderValidationSetup() throws Exception {

		final File workbookFile =
				new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.ADDITIONAL_NAME_FILE).toURI());

		assert workbookFile.exists();

		final Workbook noStockIDWorkbook = WorkbookFactory.create(workbookFile);
		this.parser.parseWorkbook(noStockIDWorkbook, null);

		Assert.assertEquals(
				"Header validation setup does not properly recognize the right amount of expected headers for the observation sheet",
				EXPECTED_DESCRIPTION_SHEET_VARIABLE_COUNT, this.parser.getDescriptionVariableNames().size());

	}

	/**
	 * Test when we have no stock id column in observation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTemplateMissingObservationHeader() throws Exception {
		try {
			final File workbookFile =
					new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.OBSERVATION_NO_STOCK_ID_FILE).toURI());

			assert workbookFile.exists();

			final Workbook noStockIDWorkbook = WorkbookFactory.create(workbookFile);
			this.importedGermplasmList = this.parser.parseWorkbook(noStockIDWorkbook, null);
			Assert.fail("Header error not properly recognized by parser");
		} catch (final FileParsingException e) {
			Assert.assertEquals("A different error from the one expected was thrown by the parser", "GERMPLASM_PARSE_HEADER_ERROR",
					e.getMessage());
		}

	}

	/**
	 * Test when we have no stock id column in observation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithNoInventoryColumn() throws Exception {
		final File workbookFile =
				new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.NO_INVENTORY_COL_FILE).toURI());

		assert workbookFile.exists();
		final Workbook noInventoryWorkbook = WorkbookFactory.create(workbookFile);

		this.importedGermplasmList = this.parser.parseWorkbook(noInventoryWorkbook, null);

		Assert.assertTrue("Unable to properly provide warning for templates with no inventory column", this.parser.getNoInventoryWarning()
				.contains("StockIDs can only be added for germplasm if it has existing inventory in the BMS"));
	}

	/**
	 * Test when we have stock id column but contain missing values
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithMissingStockIdValuesInObservation() throws Exception {
		try {
			final File workbookFile =
					new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.OBSERVATION_NO_STOCK_ID_VALUES_FILE)
							.toURI());
			final Workbook missingStockIDValuesWorkbook = WorkbookFactory.create(workbookFile);
			this.importedGermplasmList = this.parser.parseWorkbook(missingStockIDValuesWorkbook, null);
			Assert.fail("Unable to properly recognize error condition regarding missing stock ID values in observation sheet");
		} catch (final FileParsingException e) {
			Assert.assertEquals("A different error from the one expected was thrown by the parser",
					"GERMPLSM_PARSE_GID_MISSING_SEED_AMOUNT_VALUE", e.getMessage());
		}
	}

	/**
	 * Test when we have stock id column but contain duplicate values
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithDuplicateIdsInObservation() throws Exception {
		try {
			Mockito.doThrow(new FileParsingException("GERMPLASM_PARSE_DUPLICATE_STOCK_ID")).when(this.stockIdValidator)
					.validate(Matchers.anyString(), (ImportedGermplasmList) Matchers.any());
			final File workbookFile =
					new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.DUPLICATE_STOCK_ID_FILE).toURI());
			final Workbook duplicateStockIdWorkbook = WorkbookFactory.create(workbookFile);
			this.importedGermplasmList = this.parser.parseWorkbook(duplicateStockIdWorkbook, null);
			Assert.fail("Unable to properly recognize error condition regarding duplicate IDs in observation sheet");
		} catch (final FileParsingException e) {
			Assert.assertEquals("A different error from the one expected was thrown by the parser", "GERMPLASM_PARSE_DUPLICATE_STOCK_ID",
					e.getMessage());
		}
	}

	@Test
	public void testTemplateWithAdditionalNames() throws Exception {
		final File workbookFile =
				new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.ADDITIONAL_NAME_FILE).toURI());
		final Workbook workbook = WorkbookFactory.create(workbookFile);

		this.importedGermplasmList = this.parser.parseWorkbook(workbook, null);
		final ImportedGermplasm germplasm = this.importedGermplasmList.getImportedGermplasm().get(0);
		Assert.assertEquals("Unable to properly recognize additional name factors associated with germplasm", 2, germplasm.getNameFactors()
				.size());

	}

	@Test
	public void testValidateListTypeFound() {
		for (final Map.Entry<String, String> item : this.userDefinedFieldTestDataInitializer.validListTypeMap.entrySet()) {
			Assert.assertTrue("The listType should be accepted", this.parser.validateListType(item.getKey()));
		}
	}

	@Test
	public void testValidateListTypeNotFound() {
		Assert.assertFalse("The return value should be false.", this.parser.validateListType("LIST"));
	}

	@Test
	public void testHasInventoryVariableIfTheVariableIsNotSet() {
		this.parser.setSeedAmountVariate("");
		Assert.assertFalse("Returns false when the inventory variable is not set.", this.parser.hasInventoryVariable());
	}

	@Test
	public void testHasInventoryVariableIfTheVariableIsSet() {
		this.parser.setSeedAmountVariate(SEED_AMOUNT_G);
		Assert.assertTrue("Returns true when the inventory variable is set.", this.parser.hasInventoryVariable());
	}

	@Test
	public void testHasInventoryAmountIfThereIsNoInventoryVariableSet() {
		this.parser.setSeedAmountVariate("");
		Assert.assertFalse("Returns false when there is no inventory variable set.", this.parser.hasInventoryAmount());
	}

	@Test
	public void testHasInventoryAmount() {

		this.parser.setSeedAmountVariate(SEED_AMOUNT_G);

		final ImportedGermplasmList importedGermplasmList = this.createImportedGermplasmListWithSeedAmount();
		this.parser.setImportedGermplasmList(importedGermplasmList);

		Assert.assertTrue("Returns true when there is at least one imported germplasm row with seed inventory value.",
				this.parser.hasInventoryAmount());
	}

	private ImportedGermplasmList createImportedGermplasmListWithSeedAmount() {
		final ImportedGermplasmList importedGermplasmList =
				this.importedGermplasmListInitializer.createImportedGermplasmList(NO_OF_ENTRIES, true);
		final List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasm();
		// initialize seed amount from imported germplasm
		Double seedAmount = 1.0D;
		for (final ImportedGermplasm importedGermplasm : importedGermplasms) {
			importedGermplasm.setSeedAmount(seedAmount);
			seedAmount++;
		}
		return importedGermplasmList;
	}

	@Test
	public void hasAtLeastOneRowWithInventoryAmountButNoDefinedStockID() {
		this.parser.setSeedAmountVariate(SEED_AMOUNT_G);

		final ImportedGermplasmList importedGermplasmList = this.createImportedGermplasmListWithSeedAmount();
		this.parser.setImportedGermplasmList(importedGermplasmList);

		Assert.assertTrue("Returns true when there is at least one row with seed amount but no defined stock id value.",
				this.parser.hasAtLeastOneRowWithInventoryAmountButNoDefinedStockID());
	}

	@Test
	public void hasAtLeastOneRowWithInventoryAmountButNoDefinedStockIDReturnsFalseWhenStockIdsHasValuesForAllRows() {
		this.parser.setSeedAmountVariate(SEED_AMOUNT_G);

		final ImportedGermplasmList importedGermplasmList = this.createImportedGermplasmListWithSeedAmount();

		final String inventoryID = "INV-";
		int count = 1;
		final List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasm();
		for (final ImportedGermplasm importedGermplasm : importedGermplasms) {
			importedGermplasm.setInventoryId(inventoryID + count);
			count++;
		}

		this.parser.setImportedGermplasmList(importedGermplasmList);

		Assert.assertFalse("Returns false when all rows in stockID columns have values.",
				this.parser.hasAtLeastOneRowWithInventoryAmountButNoDefinedStockID());
	}

}

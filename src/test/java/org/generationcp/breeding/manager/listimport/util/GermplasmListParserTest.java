package org.generationcp.breeding.manager.listimport.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.listimport.validator.StockIDValidator;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
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
 * Description Sheet (see the equivalent unit test for {@link org.generationcp.commons.parsing.DescriptionSheetParser}) But test still
 * promises at least 50% coverage for {@link GermplasmListParser}
 */
@RunWith(MockitoJUnitRunner.class)
public class GermplasmListParserTest {

	public static final String TEST_FILE_NAME = "GermplasmImportTemplate-StockIDs-only.xls";
	public static final String OBSERVATION_NO_STOCK_ID_FILE = "GermplasmImportTemplate-StockIDs-missing-stock-id-column.xls";
	public static final String OBSERVATION_NO_STOCK_ID_VALUES_FILE = "GermplasmImportTemplate-StockIDs-missing-stock-id-values.xls";
	public static final String NO_INVENTORY_COL_FILE = "GermplasmImportTemplate-StockIDs-no-inventory-column.xls";
	public static final String DUPLICATE_STOCK_ID_FILE = "GermplasmImportTemplate-StockIDs-duplicate-stock-ids.xls";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@InjectMocks
	private StockIDValidator stockIDValidator = Mockito.spy(new StockIDValidator());

	@InjectMocks
	private final GermplasmListParser parser = Mockito.spy(new GermplasmListParser());

	private Workbook defaultWorkbook;
	private Workbook noStockIDWorkbook;
	private Workbook missingStockIDValuesWorkbook;
	private Workbook noInventoryWorkbook;
	private Workbook duplicateStockIdWorkbook;

	private ImportedGermplasmList importedGermplasmList;

	@Before
	public void setUp() throws Exception {
		File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.TEST_FILE_NAME).toURI());
		File workbookFile2 =
				new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.OBSERVATION_NO_STOCK_ID_FILE).toURI());
		File workbookFile3 = new File(
				ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.OBSERVATION_NO_STOCK_ID_VALUES_FILE).toURI());
		File workbookFile4 =
				new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.NO_INVENTORY_COL_FILE).toURI());
		File workbookFile5 =
				new File(ClassLoader.getSystemClassLoader().getResource(GermplasmListParserTest.DUPLICATE_STOCK_ID_FILE).toURI());

		assert workbookFile.exists();
		assert workbookFile2.exists();
		assert workbookFile3.exists();
		assert workbookFile4.exists();
		assert workbookFile5.exists();

		this.defaultWorkbook = WorkbookFactory.create(workbookFile);
		this.noStockIDWorkbook = WorkbookFactory.create(workbookFile2);
		this.missingStockIDValuesWorkbook = WorkbookFactory.create(workbookFile3);
		this.noInventoryWorkbook = WorkbookFactory.create(workbookFile4);
		this.duplicateStockIdWorkbook = WorkbookFactory.create(workbookFile5);

		Mockito.when(this.ontologyDataManager.isSeedAmountVariable(Matchers.eq("INVENTORY AMOUNT"))).thenReturn(true);
		Mockito.when(this.ontologyDataManager.isSeedAmountVariable(AdditionalMatchers.not(Matchers.eq("INVENTORY AMOUNT"))))
				.thenReturn(false);
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(Matchers.anyInt())).thenReturn(Mockito.mock(Germplasm.class));
		Mockito.when(this.inventoryDataManager.getSimilarStockIds(Matchers.anyList())).thenReturn(new ArrayList<String>());

		Map<Integer, String> preferredNames = Mockito.mock(Map.class);
		Mockito.when(preferredNames.get(Matchers.any())).thenReturn("TEST DESIG");
		Mockito.when(this.germplasmDataManager.getPreferredNamesByGids(Matchers.anyList())).thenReturn(preferredNames);

	}

	/**
	 * This is the default case, the template has a stock id factor
	 *
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithStockIdFactor() throws Exception {
		this.importedGermplasmList = this.parser.parseWorkbook(this.defaultWorkbook, null);

		Assert.assertNotNull(this.importedGermplasmList);
		Assert.assertEquals("This template has blank list date, should be eq to current date", DateUtil.getCurrentDateInUIFormat(),
				DateUtil.getDateInUIFormat(this.importedGermplasmList.getDate()));
		assert this.parser.hasStockIdFactor();
	}

	/**
	 * Test when we have no stock id column in observation
	 *
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithNoStockIdInObservation() throws Exception {
		try {
			this.importedGermplasmList = this.parser.parseWorkbook(this.noStockIDWorkbook, null);
			Assert.fail();
		} catch (FileParsingException e) {
			Assert.assertEquals("GERMPLASM_PARSE_STOCK_COLUMN_MISSING", e.getMessage());
		}

	}

	/**
	 * Test when we have no stock id column in observation
	 *
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithNoInventoryColumn() throws Exception {
		this.importedGermplasmList = this.parser.parseWorkbook(this.noInventoryWorkbook, null);

		Assert.assertTrue(this.parser.getNoInventoryWarning()
				.contains("StockIDs can only be added for germplasm if it has existing inventory in the BMS"));
	}

	/**
	 * Test when we have stock id colum but contain missing values
	 *
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithMissingStockIdValuesInObservation() throws Exception {
		try {
			this.importedGermplasmList = this.parser.parseWorkbook(this.missingStockIDValuesWorkbook, null);
			Assert.fail();
		} catch (FileParsingException e) {
			Assert.assertEquals("GERMPLSM_PARSE_GID_MISSING_STOCK_ID_VALUE", e.getMessage());
		}
	}

	/**
	 * Test when we have stock id colum but contain missing values
	 *
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithDuplicateIdsInObservation() throws Exception {
		try {
			this.importedGermplasmList = this.parser.parseWorkbook(this.duplicateStockIdWorkbook, null);
			Assert.fail();
		} catch (FileParsingException e) {
			Assert.assertEquals("GERMPLASM_PARSE_DUPLICATE_STOCK_ID", e.getMessage());
		}
	}

}
